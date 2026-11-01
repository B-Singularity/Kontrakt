# Kontrakt Primitive Slabbing·메모리 지역성·생명주기·캐시·증분 저장 통합 연구 TODO

**상태:** 연구·설계·구현 준비용 통합 TODO. **ADR이 아니며 새로운 Accepted 결정을 만들지 않는다.**  
**작성 기준일:** 2026-11-01  
**범위:** V1의 기존 물리 메모리·캐시 기반을 보존하면서 V2의 증분·영속 재사용까지 열어 두는 컴파일러 내부 아키텍처.  
**권한 우선순위:** `What Contract Is`와 현행 Accepted ADR이 우선한다. 이 문서는 Contract 의미나 Establishment 권한을 변경하지 않는다.

## 0. 목적과 작성 기준

Kontrakt는 이미 Primitive Slabbing의 Payload, Padding, Stride, Field Offset, Capacity, Indexing 및 내부 Region/Arena 관리를 직접 설계해 왔다. FFM을 검토하는 주된 이유는 이 설계를 대체하거나 `MemoryLayout`에 Record 정의를 맡기려는 것이 아니다. **일반 Java Heap 배열로는 보장하기 어려운 실제 메모리 시작 주소의 캐시라인 정렬을 확보하여, 이미 계산한 물리적 배치를 정확히 실현하려는 것**이다. FFM이나 DirectByteBuffer는 필요할 때 선택하는 하위 물리 수단이며, 배치와 생명주기의 논리적 책임은 Kontrakt에 남는다.

이 문서는 지금까지 논의한 모든 주요 영역을 하나의 작업 기반으로 묶는다. 기존 `kontrakt-ffm-backed-primitive-slabbing-and-storage-migration-todo.md`, `kontrakt-compiler-reuse-incremental-v1-v2-todo.md`, L1/L2 설계, Lane 설계, Runtime Memory Envelope, Query-oriented 설계 및 V2 증분 아키텍처 후보를 대체하지 않고 **상호 연결과 미결정 사항을 보완**한다. 이미 논의하고 적용 대상으로 둔 **S3-FIFO와 SIEVE를 새로 발견한 후보처럼 취급하지 않는다.** 두 기술의 역할은 기존 조사 결과를 실제 Cache Admission·Retention·Eviction·메모리 회수 체계에 연결하고 검증하는 것이다.

이후의 서술은 **기존 법칙**, **기존 설계**, **추가 연구 후보**, **결정이 필요한 사항**을 구별한다. 구조를 선택하는 데 구현 난이도는 평가 기준으로 사용하지 않는다. 대신 실제 성능, RAM 점유·회수량, CPU Cache/TLB 비용, I/O 비용, 동시성, 장기적인 JVM API 안정성과 의미적 동등성을 평가한다. 성능이 좋다는 이유로 물리 구현이 새로운 Contract 권한을 얻어서는 안 된다.

### 0.1 근거 문서와 상태

| 문서 | 현재 확인한 지위 | 이 TODO에서 사용하는 근거 |
|---|---|---|
| `docs/the-most-important-thing/what-contract-is.md` | 최상위 의미 기준 | Contract는 구현에 종속되지 않는다 |
| ADR-0063, ADR-0071, ADR-0075, ADR-0077 | 제공된 현행 파일에서 Accepted | Establishment와 HIR의 경계, Producer 소유의 의미, Protocol을 통한 적법한 소비·재사용 |
| ADR-0031, ADR-0034, ADR-0035 | 확인한 사본에서 Accepted | Planning L1/L2, 정확한 Key, Join Lifecycle, 결정적 M:N 완료 전달 |
| ADR-0041 `Stable Metadata Identity...` | **Proposed** | HID·BLAKE3·Metadata Interning 연구. 확정 법칙으로 격상하지 않는다 |
| ADR-0042 `Mechanical Sympathy...` | **Proposed** | Primitive 물리 구현과 수명·소유권의 분리 연구 |
| ADR-0044 `Unified Runtime Memory Envelope...` | **Draft** | 통합 RAM Envelope와 Reserve/Retirement의 상세 설계 기준. 상태 정합성 확인 필요 |
| FFM TODO, Query-oriented 설계, V2 증분 TODO, 전체 아키텍처 Map | Working Design/TODO | 현행 구현 후보와 V1/V2 확장 경계 |
| LMAX·Aeron·Chronicle·Arrow·Folly·DuckDB·RocksDB 등 | 외부 사례 | 부분 기법의 타당성·비용 비교, Contract 의미의 근거는 아님 |

**상태 정합성 주의:** 과거 대화에서 ADR-0044를 Accepted처럼 언급한 부분이 있었으나, 이번에 확인한 파일의 상태는 Draft다. 현행 설계 방향으로 참고하되, 확정된 규범으로 기술할 때는 실제 Acceptance 및 상위 문서와의 정합성을 먼저 확인한다.

### 0.2 글·수정 스타일

한 개념은 소유 절에서 충분히 설명하고 다른 절에서는 참조한다. 표와 체크리스트는 구체적인 실험·판정 항목을 제시하기 위해 사용하며, 설명할 내용을 명사 나열로 대체하지 않는다. Contract와 컴파일러 구현을 구별한다. 이미 확정된 의미를 SOTA 구현에 맞추어 고치지 않는다. V2 후보는 최종안처럼 쓰지 않는다. 수치와 성능 주장은 원래 시스템의 워크로드·하드웨어·측정 조건을 명시하고 Kontrakt에서의 효과와 혼동하지 않는다. 기존 내용을 수정할 때는 먼저 원본의 소유권과 중복 항목을 확인하고, 결과를 요약하거나 자의적으로 삭제하지 않는다.

## 1. 전역 아키텍처에서 메모리의 위치

컴파일러 흐름은 `IDL·1D 선언·적격 SDK 의미 → Resolved Contract HIR → Establishment → Canonical Contract World → Generated Interaction/Operation API → 사용자 Realization 취득 → MIR·분석·검증·Execution MIR → JVM LIR → Classfile`이다. Diagnostic·PBT·Reference·Security는 필요한 결과를 독립적으로 생산·소비하는 경로를 갖는다. HIR은 후보 의미이고 Canonical Contract World는 Established 권한 자료이며, JVM LIR은 실제 독립 IR 단계다. 저장 방식은 이 구별을 바꿀 수 없다.

각 재사용 가능 결과는 생산자가 소유하는 의미, 결정 입력, 비교·유효성·공개 규칙을 가진다. 소비자는 적법한 Protocol Observation을 통해 필요한 정보를 얻는다. Query Engine은 요청·의존성·유효성·재사용·스케줄링을 매개하지만, 결과의 의미를 소유하지 않는다. Hot Loop가 CFG Edge 하나 또는 FactRef 하나를 읽을 때마다 글로벌 Query를 거칠 필요도 없다.

```text
소비자의 필요한 정보·보장
    → Kontrakt-controlled Mediation / 필요한 경우 Typed Query
    → Producer-owned Semantic Protocol + 유효성 판단
    → 승인된 의미 Observation / 파생 Hot Projection
    → Physical Locator (Dense Ordinal / Index / Range)
    → Kontrakt가 직접 구현한 Slab 접근
```

이 그림은 **논리적 의미 흐름**이다. 각 화살표가 별도 JVM 호출·객체·동적 디스패치가 되어야 한다는 뜻이 아니다. 구현은 Monomorphic Accessor, Inlining, Specialized Code로 비용을 없앨 수 있지만 Protocol의 의미와 검증 가능성을 제거할 수는 없다.

- [ ] HIR, Establishment, SDK Meaning, MIR, Verification, JVM LIR, PBT, Diagnostic, Query의 Producer별 입력·출력·적법한 Observation·Equality·Validity를 식별한다.
- [ ] `OperationRef`, `FactRef`, `InvariantRef`, `OperationParameterRef` 등 의미 참조와 HID, Stable Intern ID, Generation-local Ordinal, Byte Offset, Native Address를 분리한다.
- [ ] 파생 Hot View를 허용하되 기존 Fact/Invariant/Applicable Contract 판단을 추가 생성하는 권한으로 해석하지 않는다.
- [ ] 물리 배치·메모리 Backend를 변경해도 같은 Contract/Compiler Product 의미를 관찰한다는 교차 구현 Conformance Test를 정의한다.

## 2. CPU 캐시라인과 정확한 주소 정렬

캐시라인 크기가 2의 거듭제곱 `L`일 때 주소 `A`가 속하는 라인의 시작 주소는 `A & ~(L-1)`이고, 해당 영역의 끝은 `start + L`의 직전이다. x86-64에서 64B가 흔하지만 Kontrakt는 이를 고정 법칙으로 쓰지 않고 **컴파일러가 실행되는 Host Hardware Profile**을 통해 결정한다. 하나의 Load가 서로 다른 두 라인을 가로지르면 두 라인을 건드릴 수 있다. 반대로 64B 정렬 자체가 실제 Cache Hit, 정확히 한 번의 Memory Transfer, TLB Hit, 원자성을 보장하지는 않는다.

```text
L = HardwareProfile.cacheLineBytes
baseAddress % L == 0
allocatedBytes % L == 0
logicalPayloadBytes <= allocatedBytes
```

첫 두 조건을 만족하면 할당 영역의 시작과 끝을 캐시라인 경계에 맞출 수 있다. 마지막 Padding은 실제 Payload가 아니므로 소비자에게 노출되지 않아야 한다. Slab이 정렬되더라도 24B Record를 Stride 24B로 채우면 일부 Record는 경계를 넘을 수 있다. 반대로 모든 24B Record를 64B로 벌리면 불필요한 캐시라인 접근과 TLB 부담이 증가할 수 있다. **Alignment, Stride, Record Packing, Padding, False-sharing Separation은 각각 다른 결정**이다.

CPU가 사용하는 가상 주소와 물리 주소는 일반적인 페이지 매핑에서 페이지 내부 Offset을 공유한다. 캐시라인 크기가 페이지 크기를 나누는 환경에서는 적법하게 정렬된 가상 주소만으로 해당 물리 주소의 라인 경계 정렬을 달성할 수 있다. 전체 Slab이 물리적으로 연속된 페이지를 사용한다는 뜻은 아니다. 현대 L1 캐시는 가상 주소와 물리 주소를 병행 활용할 수도 있으므로 단순한 `가상→물리 변환 후 캐시 조회` 순서만 가정하지 않는다.

**실제 최적화 목표는 캐시 히트율이라는 단일 비율이 아니라 작업당 데이터 이동량·기다림·실행시간이다.** 한 Cacheline에 4B만 쓰고 다음 라인으로 이동하는 구조는 시작 주소를 잘 맞추어도 비효율적이다. 캐시 미스가 존재하더라도 독립된 접근을 동시에 진행하는 방식이 더 빠를 수도 있다.

- [ ] Host Hardware Profile에 실제 Line Size, Cache Hierarchy, Set Associativity, Coherence Interference, Page/TLB, NUMA, Vector Capability를 필요한 범위만 명시하고 Unknown/Unavailable을 구별한다.
- [ ] **Compiler Host Profile**과 **Generated Code Target Profile**을 구별한다. Cross Compilation 및 재현 가능한 빌드에서는 필요한 Profile을 명시적으로 Pin한다.
- [ ] `alignUp`, `size + alignment - 1`, `ordinal * stride + fieldOffset`, Range 끝 계산의 오버플로·경계 실패를 검증한다.
- [ ] Base, Allocated End, Field·Column Alignment, Record Boundary, Tail Padding의 성질을 별도 Proof/Golden Vector로 검증한다.
- [ ] Split Load, 접근한 Cacheline 수, 실제 Working Set, Conflict Miss, False Sharing, DTLB Miss를 서로 다른 지표로 측정한다.
- [ ] 큰 2의 거듭제곱 Stride가 특정 Cache Set에 집중되는지, 의도적으로 약간 다른 Stride/Tile을 사용했을 때 이득이 있는지 측정한다.
- [ ] 캐시라인 정렬과 내부 Padding 때문에 발생한 실제 낭비 바이트를 Runtime Memory Envelope에 계상한다.

## 3. FFM과 최소 정렬 기능의 정확한 역할

현재 기본 설계는 **Kontrakt Primitive Slabbing이 주체이고 FFM은 Aligned Memory Substrate 후보**라는 것이다. 고카디널리티 자료는 Fixed-stride Hot Slab, Variable-length Range Slab, Cold Side Slab, Canonical Byte Slab, HID/Index Slab 등으로 나뉜다. 예를 들어 `OperationSlab[41]`에 `factStart=120`, `factCount=3`이 있고, 이를 `FactRefSlab[120..123)`으로 조회한다. 이 Range는 물리 위치이며 해당 Fact의 의미적 동일성이 아니다.

Kontrakt가 정말로 필요한 최하위 기능은 `allocateAlignedRegion(usableBytes, alignment)` 및 필요할 경우 명시적인 회수다. Record의 Field Offset·Stride·Payload·Padding은 이미 Kontrakt가 정한다. FFM의 `MemoryLayout`을 의미 스키마로 사용하거나 `VarHandle`에 필드 정의를 맡길 필요는 없다.

### 3.1 최소 API 후보의 차이

| 물리 수단 | 실제 주소 정렬 | 메모리 수명·접근의 제약 | 평가 위치 |
|---|---|---|---|
| `DirectByteBuffer + alignmentOffset()` | 원본을 `L-1`B 여유 할당하고 정렬 Base Offset을 계산할 수 있음 | 공개 API만으로 임의 시점 즉시 Native Free를 보장할 수 없음 | 정렬 기능만 필요한 최소 의존성 후보 |
| `DirectByteBuffer.alignedSlice(L)` | 원본과 메모리를 공유하는 정렬 View | Position/Limit에 따라 Slice 길이가 달라지며 원본을 함께 보유할 수 있음 | 명시적 정렬 View 후보 |
| `FFM Arena.allocate(size, alignment)` | 정렬된 Segment를 직접 할당 가능 | 실제 회수는 하위 Arena 수명·접근 규칙과 결합됨 | 정확한 정렬과 실제 명시적 회수에 유력 |
| Heap Primitive Array | 연속 Primitive 저장 가능 | Java 공개 API로 데이터 시작 주소의 캐시라인 정렬을 보장할 수 없음 | 정렬 달성의 동등한 대안은 아님; 실행 성능 비교 기준선 |

`ByteBuffer.alignmentOffset`과 `alignedSlice`는 Java 9 이후의 표준 API다. DirectByteBuffer는 Native Memory를 사용하지만, Kontrakt가 논리적 Pool에서 참조를 제거했다고 곧바로 실제 메모리가 해제되는 것은 아니다. 한 개 Buffer의 Capacity는 `int` 범위라는 제약도 있다. 현재 논리적 Arena를 Kontrakt가 소유하더라도 **최종 물리 Memory의 소유·해제 책임은 하위 API의 실제 성질과 결합해 설계**해야 한다. FFM을 사용할 경우 하나의 Generation/Worker에 대응하는 큰 영역을 하위 Arena에서 할당하고 내부 Slab을 직접 Suballocate할 수 있다. 모든 작은 객체에 FFM Arena를 각각 만들 이유는 없다.

순수 Java에서 임의 Native Address를 원하는 방식으로 무검사 역참조하는 보편적 표준 수단은 없다. 정렬만 외부 API로 해결해도 읽고 쓰는 데에는 `MemorySegment.get/set`, DirectBuffer의 Primitive Access 또는 적절한 접근 API가 필요하다. `VarHandle`은 반드시 사용해야 하는 필드 모델이 아니다. `sun.misc.Unsafe`의 Memory-access 메서드는 제거 예정으로 지정됐고, `jdk.internal.misc.Unsafe`도 장기 공개 API가 아니다. **JNI/C/C++/Rust Native Kernel은 이번 TODO의 구현 후보에서 제외한다.**

- [ ] `AlignedRegion`의 최소 계약을 `usableBytes`, 보장 Alignment, Actual Reserved Bytes, Logical Owner, Release Semantics, Failure, Access Bounds로 구체화한다.
- [ ] DirectBuffer의 `alignmentOffset` 기반 Base Offset, `alignedSlice`, FFM Aligned Allocation을 같은 내부 Slab Layout으로 비교해 순수 API 효과를 측정한다.
- [ ] Layout(AoS/SoA/AoSoA/Stride) 변경 효과와 Memory Access API 변경 효과를 별도 실험으로 분리한다.
- [ ] 직접 관리하는 논리적 Arena의 종료와 실제 메모리 Free가 일치하는지 확인한다. 불일치한다면 그 지연·RSS·Peak 비용을 명시한다.
- [ ] ByteBuffer Slice가 거대한 원본을 Pin하는 경우와 FFM Segment Slice가 소유 영역을 연장하는 경우를 테스트한다.
- [ ] 작은/빈 Slab, `int` 초과 용량, 요청 Alignment 미지원, Off-heap 예산 부족, 초기화 실패, 오버플로를 명시적으로 처리한다.
- [ ] Compiler Host JDK의 FFM 지원 버전과 생성된 응용 프로그램의 Target JVM Version을 분리한다. 내부 FFM 사용만으로 Generated ABI의 요구 버전이 상승해서는 안 된다.

## 4. Layout 선택: AoS·SoA·AoSoA·Hot/Cold·Range

Physical Layout Planner는 **Access Profile + Compiler Host Hardware Profile**을 바탕으로 실제 위치를 결정하는 비권한적 구현 경계다. Access Profile은 함께 읽는 Field, 연속 Scan, Random Lookup, Cold Metadata, 읽기/쓰기 비율, Worker Ownership, Working Set, 반복 횟수를 기술한다. 소비자가 특정 64B Offset을 Contract 요구사항으로 선언하도록 해서는 안 된다. Planner는 동일한 의미 자료로 서로 다른 Hot View를 만들어도 된다.

| 표현 | 주된 이득 | 실제 위험·검증 과제 |
|---|---|---|
| Fixed-stride AoS | 한 Entity의 Hot Field를 함께 읽음 | Cold Field 동반 적재, 한 필드 대량 Scan 불리 |
| SoA | 같은 Field의 연속 Scan·SIMD·Column 처리 | 여러 Field를 함께 읽는 단일 Entity 조회에서 라인 분산 |
| AoSoA / Cache-sized Tile | 작은 Batch의 Field 묶음 접근 | Tile 크기가 틀리면 Working Set·임시 변환 비용 증가 |
| Hot/Cold Split | 반복 접근에서 Source Span·이름·긴 설명 제거 | Cold 정보가 필요할 때 2차 접근 발생 |
| `start/count` CSR형 Range | 관계를 연속 Slab으로 순회 | 일부 관계만 재구성할 때 Range 재배치 필요 |
| Dense Ordinal + Direct Projection | 여러 단계의 Reference Chasing 절감 | Generation·Projection 변경 시 Ordinal 갱신 필요 |
| Bitmap/Array/Run 적응형 | 집합 밀도에 맞춰 바이트·연산 감소 | 표현 변경·Decode 비용 |
| Packed/Sorted Immutable Chunk | Batch Scan·Join·범위 탐색 | 불규칙 Point Lookup 저하 가능 |
| Immutable Versioned Chunk | Generation 간 불변 자료 공유 | Pin·Chunk Metadata·파편화 |

단일 64B 정렬 Slab이라고 모든 Entity를 64B 간격으로 놓아야 하는 것은 아니다. Record를 여러 개 한 라인에 조밀하게 넣을지, Write-heavy Control Word를 라인별로 분리할지는 Access Pattern에 따라 달라야 한다. Canonical Material을 굳이 Hot Consumer의 전체 데이터 형태로 바꿀 필요도 없다. 필요한 정보만 포함한 파생 Projection을 별도로 생성할 수 있다.

- [ ] HIR, Established World, SDK Catalog, Fact/Invariant Relation, HID Index, Query Metadata, CFG/SSA, Function/Whole-Machine Summary, Verification, PBT/Diagnostic, JVM LIR별 Cardinality·Read/Write·Field Coaccess를 수집한다.
- [ ] Material Family마다 AoS·SoA·AoSoA·Hot/Cold·Range 설계를 경쟁시킨다. 한 형식을 모든 저장소에 강제하지 않는다.
- [ ] `Stride`와 Field Offset을 상수로 갖는 Generated Monomorphic Accessor를 일반 Dynamic Layout Decoder와 비교한다. Chronicle Values·LLVM TableGen 계열의 **Code Generation 원리**를 참고하되 Schema는 Kontrakt가 소유한다.
- [ ] 자주 쓰는 직접 Projection을 허용하되 파생·재계산 가능·비권한적 상태로 유지하고 실제 추가 RAM 비용을 측정한다.
- [ ] CFG/SCC/Dependency의 물리 Ordinal 재배열을 반복 사용 횟수와 재배열 비용을 함께 놓고 검증한다.
- [ ] Source Location, 긴 진단 문자열, Debug Metadata는 실제 요구가 빈번하지 않다면 Cold Slab으로 분리한다.

## 5. HID·Head-first Lookup·정확 비교·Interning

Kontrakt의 조회 전략은 이미 **저비용 헤드로 후보를 줄이고, 통과한 후보만 정확한 전체 Key를 확인하는 2단계 방법**이다. `route64`나 HID 일부, 짧은 Hash Tag는 Shard·Bucket 선택, 빠른 Pre-screen에 사용한다. 충돌은 정상적인 탐색 현상이며, 같은 헤드를 가진다고 의미적으로 같은 항목이 되지 않는다. `PlanCacheKey`의 전체 결정 요소, `route64`, HID, Canonical Ordering Key, Stable Intern ID는 구별한다. 기존 Metadata Interning의 Provisional Handle·정확 충돌 검증·결정적 Publication·Bounded Cold Collision도 보존해야 한다.

Google SwissTable의 Control-byte Group, Meta Folly F14의 SIMD Tag Filtering은 현재 구조의 **빠른 후보 검색 단계**를 개선할 수 있는 외부 사례다. 그러나 정확한 Full HID/Key 비교, Qualifying Version, Producer Validity가 생략되는 것은 아니다. 고정 SDK Catalog에는 Generated Index/Perfect Hash/Sorted Table이 더 적합할 수도 있고, 정렬 범위를 요구하는 조회에는 SIMD B-tree가 별도 후보다. 정확한 Lookup 이후 Hot Loop에서는 검증된 Dense Ordinal을 재사용해 반복 Hash Lookup을 피할 수 있다.

- [ ] `route64/head → Partition·Shard·Bucket·Metadata Group → Exact Full-Key Match` 흐름을 현행 설계대로 유지한다.
- [ ] 기존 2-Phase Bucket과 Swiss/F14형 Grouped Metadata, Static Generated Index, Sorted Index, SIMD B-tree를 워크로드별 비교한다.
- [ ] Probe 수, 건드린 Cacheline 수, Tag False Positive, Full-Key 비교 바이트, Load Factor, 충돌 폭주 및 적대적 입력을 측정한다.
- [ ] Head 충돌·HID 충돌이 있어도 정확한 비교 결과, Stable Intern ID, Publication 순서가 바뀌지 않는 Golden Vector를 둔다.
- [ ] Interning의 ID가 Insert Order·Thread Completion·Native Address·임시 Ordinal에 의존하지 않도록 한다.
- [ ] Batch HID Probe로 여러 독립 Lookup의 Memory-level Parallelism을 확보하는 방법을 비교한다. Java에서 범용 Prefetch Intrinsic이 표준으로 존재한다고 가정하지 않는다.
- [ ] Revision/Applicability에 묶인 Negative Lookup 재사용을 검토하되, 이전 Generation의 Miss를 현재의 부재 판단에 재사용하지 않는다.

## 6. Planning L1·L2와 글로벌 Query Engine의 관계

**Planning L1**은 단순 완성 결과 캐시가 아니라 Worker/Session-local Primitive 실행 상태다. `ActiveStack`, `GreyMap`, `NodeIdIndexer`, RMQ, Cost Ledger, Epoch Reset 및 성공·실패·Abort의 Zero-residue Cleanup을 포함한다. 이를 Query Engine으로 대체할 이유가 없다.

**Planning L2**는 정확한 Key에 대한 Canonical Plan Reuse와 Interning, `Partition→Shard→Bucket`, In-flight Builder/Waiter Join, Commit/Drop, Governance, 제한된 저장 수명을 결합한다. 새 **Global Query Engine**은 요청된 Compiler Product의 Evaluation·Dependency·Validity·Reuse를 매개한다. Query Result Caching과 Canonical Interning은 일부 저장·중복 계산 기능이 겹치더라도 같은 책임이 아니다. Query Engine은 모든 물리 필드 접근에 개입하지 않는다.

또한 프로젝트에서 `L1/L2` 용어를 서로 다른 의미로 사용했다. 기존 Planning L1/L2와 향후 `Session / Local Persistent / Shared·Remote` 저장 계층의 L0/L1/L2는 동일한 계층 정의가 아니다. 새 문서에서는 `Planning L1/L2`와 `Query Cache Tier`를 명시적으로 구분한다.

- [ ] 기존 L1/L2의 각 메서드·상태를 Query Orchestration, Producer-owned Interner, Worker Scratch, Scheduler, Completion Plane에 매핑하고 중복 캐시를 식별한다.
- [ ] L1의 Primitive Hot Data, Epoch Rollover, Sentinel, Capacity Budget, 요청별 Success/Fault/Abort Cleanup을 보존한다.
- [ ] Query는 Product Identity·실제 읽은 Dependency·Result State·Generation Validity를 Typed Table로 관리할 수 있게 설계한다. 범용 `Object payload`나 Boxing 기반 Hot Lookup을 필수로 만들지 않는다.
- [ ] Producer가 Equality·Validity·적법한 Publication을 소유한다. Query Cache Hit만으로 이전 Result를 승인하지 않는다.
- [ ] Query Memo, In-flight Entry, Dependency Edge, Store Index에 드는 RAM도 통합 예산에 포함한다.
- [ ] L2의 정확한 Match·중복 Builder 통제·One-way Terminalization과 Query In-flight Dedup을 연결하되 같은 Product를 두 곳에서 무한히 보유하지 않는다.

## 7. M:N Dispatch Lane·비동기 Callback·False Sharing

ADR-0035에 따른 구조는 Shard가 Routing과 In-flight Coordination을 소유하고, **Lane은 완료 전달의 실행 책임만 가진다.** Shard→Lane 대응은 Adapter 수명 내 고정이고, Lane의 Mutable State는 해당 Lane이 단일 작성자로 관리한다. 임의 호출자 Thread에서 Callback을 직접 실행하거나 완료 전달의 권한을 Work Stealing 대상으로 바꿀 수 없다. Query 계산 작업을 어느 Worker에서 실행할지의 문제와 완료 Callback을 어느 Lane이 소유하는지는 구분한다.

LMAX Disruptor가 참고가 되는 부분은 Ring Sequence, 사전 할당, Batch 처리, Cacheline-separated Mutable Counter와 False Sharing 방지다. Aeron/Agrona에서는 Primitive-oriented Queue와 Bounded Work를 비교한다. 다만 **읽기 전용 Frozen Material의 모든 Record까지 라인별 Padding할 필요는 없다.** Ring Slot에서 처리가 끝난 참조를 계속 보유하여 메모리를 Pin하는 현상도 점검해야 한다.

- [ ] Shard Affinity, Lane Count·Mapping, Bounded Queue, Single-writer State, Late Completion, Close/Drop의 기존 소유권을 보존한다.
- [ ] `등록 → 대기 → 신호 → Queue → Delivery → Terminal → 안전한 Reclamation`을 분리하고 Timeout·Cancel·Saturation·Replay·Close Race를 검증한다.
- [ ] Callback 완료와 Waiter/Slot/Result의 실제 메모리 회수 가능 시점을 동일시하지 않는다.
- [ ] Padded Counter와 Compact Counter, SPSC/MPSC Ring, Preallocated Sequence Slot을 실제 Contention과 False Sharing Counter로 비교한다.
- [ ] Waiter, Builder Handle, Speculative Lease, Completion Command, Continuation, Retired Entry의 동시 최대 생존량을 예산에 계상한다.
- [ ] Lane의 완료 순서·Terminal Authority·Caller-thread 비실행 법칙을 성능상의 편의로 우회하지 않는다.

## 8. 생명주기·회수·파편화·RAM 효율

RAM 중심 컴파일러에는 Staging, Worker Scratch, Generation-local Material, Published Frozen Result, Retained Query Product, In-flight Delivery, Retired-but-pinned Epoch, Diagnostic, Quarantine, Persistent Metadata처럼 서로 다른 수명 영역이 있다. **논리적으로 제거한 상태, Query에서 더 이상 보이지 않는 상태, Retired 상태, 마지막 Reader가 떠난 상태, 실제 바이트를 재사용할 수 있는 상태는 다르다.** 자체 Arena를 설계하더라도 이 구별을 검증할 수 있어야 한다.

ADR-0044 Draft의 메모리 Envelope를 기준으로 검토할 항목은 Active Slice 외에 Transient, Publication, Reclamation, Retired Epoch, Continuation/Restart, Quarantine, Emergency Diagnostic Reserve다. 단일 `maxBytes` 값으로 이 자원을 뭉개서는 안 된다. 동시 실행에서 같은 Reserve를 이중 사용하거나, 곧 회수될 것이라고 예상한 메모리를 현재 사용 가능한 것으로 간주하면 안 된다. 물리 Backing Region이 공유될 때는 중복 계상을 피하되 살아 있는 실제 영역을 빠뜨리지 않는다.

### 8.1 수명별 회수 전략 후보

| 방식 | 적합한 상태 | 성능 비용·위험 |
|---|---|---|
| Worker-local Epoch Reset | Scratch·Worklist | Epoch Rollover, 이전 작업 State 잔류, Clear 비용 |
| Generation Bulk Retire | 세대 단위 Frozen Product | Reader 하나가 큰 세대를 Pin할 수 있음 |
| Chunk Lease/Reference Tracking | 독립적으로 공유되는 Immutable Chunk | Atomic Metadata·Pin 관리 비용 |
| RCU/Epoch Grace Period | 다수 Reader의 낮은 읽기 비용 | 지연 회수와 Retired RAM 누적 |
| Hazard Pointer 계열 | 소수 공유 구조의 정밀 보호 | 보호/스캔 비용, 추가 메타데이터 |
| Copy·Compact 후 Retire | 파편화된 Slab | 원본과 새 Slab 동시 생존으로 Peak 증가 |
| CacheLib식 Size-class Pool/Rebalance | 크기별 점유 편차 | Handle Pin, Object Relocation, 빈 Slab 부족 |

Meta CacheLib의 Slab Rebalance는 가용 공간을 다른 크기 Class로 재배정하는 사례다. Linux RCU와 Folly Hazard Pointer는 읽기 비용과 회수 지연의 트레이드오프를 보여준다. 어느 하나를 전역에서 무조건 선택할 근거는 없다. Worker Scratch에는 Coarse Reset이 적합할 수 있고, 여러 Generation이 공유하는 Immutable Chunk에는 별도의 Pin/Lease 또는 Epoch 추적이 필요할 수 있다. 모든 Record마다 개별 Refcount를 붙이는 것은 Primitive Slab의 밀도를 해칠 수 있다.

- [ ] 수명 클래스별 Owner, Allocation, Publication, Reader, Retire Trigger, Safe Reclaim Proof, Failure Path를 작성한다.
- [ ] `payload`, `padding`, `reserved`, `committed`, `active`, `pinned`, `retired`, `staging`, `metadata`, `peak concurrent` 바이트를 구별한다. JVM Heap·Direct/Native Memory·Mapped/Page Cache도 이중 계상 없이 보고한다.
- [ ] Old Generation이 살아 있는 상태에서 New Generation Build, Compaction, Cold RAM 승격, Disk Restore가 동시에 일어나는 Peak를 사전 계산한다.
- [ ] Region Bulk Drop, Immutable Chunk 공유, 부분 Compaction, Slab Size-class·Free-list 정책의 Fragmentation/회수 비용을 비교한다.
- [ ] 작은 Slice 하나 때문에 대형 Allocation이 계속 살아 있는 문제를 Memory Report와 Soak Test로 탐지한다.
- [ ] 장기 Compiler Daemon, 취소된 Worker, 정체된 Reader, Late Callback, Quarantine 반복, 장애·메모리 압박 상태를 테스트한다.
- [ ] Kontrakt의 논리적 `close/drop`과 OS에서 실제 메모리가 다시 사용 가능한 시점의 차이를 문서화하고 계측한다.

## 9. RAM 캐시 보존 정책: S3-FIFO·SIEVE는 이미 연구된 후보

**S3-FIFO와 SIEVE는 기존 조사 및 적용 대상이다.** 이 절에서 새로 채택 여부를 처음 논의하지 않는다. 남은 작업은 현재 정확 Key·Shard·Lane·Memory Envelope에 연결하고 실제 Kontrakt Query Trace로 검증하는 것이다. S3-FIFO의 일회성 접근 항목 빠른 퇴출과 SIEVE의 저렴한 Cache-hit 갱신은 메모리 효율과 공유 Cache Metadata 비용을 줄이려는 기존 목표와 관련된다.

Cache Retention Policy가 어떤 항목을 버릴지 결정할 수 있어도, **사용자가 읽고 있는 Published Material, In-flight/Continuation 소유 자료, Grace Period가 끝나지 않은 Retired Region을 곧바로 해제할 권한은 없다.** Eviction은 선택적 보존의 종료이고, Reclamation은 안전하게 실제 저장공간을 다시 사용하는 별개의 단계다.

- [ ] 기존 S3-FIFO·SIEVE 조사 및 선택 조건을 Cache Admission/Retention/Retirement 설계에 편입하고, 원본 근거·이미 결정된 사항을 보존한다.
- [ ] S3-FIFO, SIEVE, CLOCK/HyperClock, Partition Bulk Drop, 비용 기반 Retention을 **현행 실제 Trace**에서 경쟁시킨다. LRU는 비교 기준이지 강제 기본값이 아니다.
- [ ] 반복 사용하는 SDK Catalog·Function Summary·Whole-Machine Summary가 일회성 임시 Product에 밀려나는 문제를 측정한다.
- [ ] Global LRU의 Hit마다 공유 목록 갱신하는 비용을 회피할 수 있는 Shard-local/지연 메타데이터 갱신 방식을 평가한다.
- [ ] Miss Ratio 외에 Byte-weighted Hit Ratio, 실제 재계산 시간 절약, Hit Metadata Write, Cacheline Ping-pong, Retired/Pinned Peak를 측정한다.
- [ ] `다시 계산할 예상 비용 / 점유 바이트`에 Validity 유지 확률·검증 비용·디스크 복원 비용을 추가한 Retention Value는 후보 비용 모델로 검증하되 의미적 유효성 법칙으로 만들지 않는다.
- [ ] Cache Eviction과 동시에 발생하는 Query Reader, Exact-key Join, Cancellation, Callback 처리의 Race를 검증한다.

## 10. 정렬 외의 캐시 지역성 극대화 기법

정확한 캐시라인 정렬이 기반이지만 가장 큰 추가 이익은 실제 접근 순서와 배치의 결합에서 나올 수 있다. 한 라인에 사용될 값만 묶고, 자주 함께 사용되는 Field를 가깝게 두며, Cold Data와 일시적 중간 자료를 Hot Working Set에서 제거한다. 다만 L1 Hit Rate만 높이는 최적화는 목표가 아니다. **최종 처리량·지연시간·메모리 대역폭·용량 효율**로 판단한다.

| 기법 | 줄이는 비용 | 주의할 역효과 |
|---|---|---|
| Hot/Cold Split·Compact Dense Ref | 불필요한 바이트 적재·간접 참조 | Decode·Cold Access 발생 |
| Direct Hot Projection·Late Materialization | 반복 Relation Chasing·불필요한 전체 Material 생성 | 중복 파생 RAM·유효성 관리 |
| AoSoA·Cache-sized Tiling | Batch 내 캐시 재사용·Vectorization | 부정확한 Tile Size와 재배열 비용 |
| Vectorized Batch·Selection Vector | Per-record 분기·반복 호출 | 필터 뒤 빈 Batch가 다수 생김 |
| Logical Chunk Compaction | 희소 Batch의 실제 작업 밀도 개선 | Compaction 비용이 이득보다 클 수 있음 |
| Operator Fusion | 중간 Result Materialization·메모리 왕복 | 코드 크기·파생 결과 재사용성 감소 |
| Graph/SCC/CFG Reordering | 연달아 방문하는 정점의 물리 거리 축소 | 재배열·Reverse Index 작성 비용 |
| Swiss/F14 Metadata Lookup | 랜덤 Full-key 접근 수 감소 | Tag False Positive, 충돌 및 Probe 분포 |
| Batched Hash Probing·Prefetch | Memory-level Parallelism으로 지연 은닉 | 캐시 오염·추가 Working Set |
| Roaring Array/Bitmap/Run | 집합 밀도별 바이트·연산 수 최소화 | Container 변환·해석 비용 |
| Cache-aware SIMD B-tree·압축 Index | 정렬 키의 Lookup·범위 조회 | 단순 Hash/Dense Index보다 느릴 수 있음 |
| Huge Page·NUMA Placement | TLB·원격 메모리 비용 감소 | 메모리 낭비·운영 환경 의존 |
| Cache-set Alias/Conflict 회피 | Associativity Conflict Miss 감소 | Layout 특화·프로파일링 필요 |
| Lazy Derived-View Repair | 사용되지 않을 View 재구성 방지 | 첫 요청 지연·유효성 메타데이터 |

최근 SIGMOD 2025의 *Data Chunk Compaction in Vectorized Execution*은 DuckDB류 Vector Batch에서 남은 유효 Tuple이 적어졌을 때 작은 Chunk를 합치는 전략을 다룬다. PPoPP 2025의 *Frontier-guided Graph Reordering*은 동시에 활성화되는 그래프 정점의 지역성에 따라 물리 순서를 재배열한다. 이러한 결과는 Kontrakt Verifier·CFG·Dependency Graph에 적용할 연구 근거지만, 해당 논문의 속도 향상을 Kontrakt에서 재현했다고 주장할 수는 없다.

- [ ] Hot Field와 작업당 실제 Cacheline 접근 수부터 측정하고 추가 최적화 우선순위를 정한다.
- [ ] SIMD/Vector API·JIT Auto-vectorization을 실제 컴파일된 기계어와 함께 비교한다. 표준 Java에서 범용 명시적 Data Prefetch를 가정하지 않는다.
- [ ] Sparse Batch에서는 Physical Copy 대신 Selection Ordinal·Bitmap만 재배치하는 Logical Compaction을 검토한다.
- [ ] DuckDB의 Vector Size(대표적으로 2,048)를 그대로 복사하지 않고 Hot Column Width와 L1/L2 Working Set에 따라 크기를 선택한다.
- [ ] Graph Reordering은 원래 Semantic Identity와 분리된 Physical Ordinal View로만 적용하고 반복 작업에서 이득이 나는지 측정한다.
- [ ] NUMA-aware Worker Scheduling과 Memory Placement가 Lane의 결정적 소유권을 변경하지 않도록 한다.

## 11. V2 증분, Fingerprint와 Early Cutoff

V1은 Query-oriented Product, Frozen/HID/BLAKE3 및 L1/L2 재사용을 기반으로 하며, V2에서 의존성 표현·Change Frontier·복구 알고리즘·스케줄링·Persistent Store를 고도화할 수 있게 한다. 기존 V2 조사에 제시된 여섯 후보는 열린 상태를 유지한다.

- **A. HID Red-Green Query:** 요청된 결과의 이전 Dependency를 검증하고, 필요한 경우 다시 계산한 Result Fingerprint로 변화 여부를 판단한다.
- **B. HID Change Frontier:** 변경된 입력에서 영향받은 소비자 방향으로 활성 Frontier를 전파하되 변화가 없는 경계에서는 중단한다.
- **C. Domain-local Hybrid:** HIR, Analysis, Verification, Backend 등 생산자별로 알맞은 증분 알고리즘을 선택한다.
- **D. Differential Analysis + Query Shell:** 관계·Reachability·집합 등 Delta가 실제로 유리한 영역에 유지형 계산을 적용한다.
- **E. Elastic Incremental/Full-Rebuild:** 전체 비용과 예산을 사전 판단한 뒤 적법한 재계산/증분 방식을 선택한다.
- **F. Prediction-guided Cut-first:** 성공적인 Cutoff를 만들 가능성이 높은 작업을 우선 평가하는 보조 스케줄링을 검토한다.

이 후보들은 서로 배타적인 전체 구현체가 아닐 수 있다. C/E를 중심으로 다른 원리를 결합하는 방향은 검토 가치가 있지만 아직 확정된 우승안이 아니다. rustc의 Red-Green은 부분 기술 참고일 뿐 V2의 증분 전체 형태를 결정하지 않는다.

### 11.1 RAM에 Payload가 없어도 가능한 변경 없음 판단

이전 Product의 실제 내용이 RAM에서 회수됐더라도 **작은 Key·Dependency·Validity·Fingerprint Metadata**가 남아 있다면 어떤 조건에서는 이전 결과를 역직렬화하지 않고 변경이 없다는 사실을 판정할 수 있다. 하지만 특정 소비자가 그 Product의 실제 내용을 요구하면 RAM 사본, 디스크 복원, 새 계산 중 하나가 필요하다. Fingerprint의 우연한 같음, 일부 Dependency의 변화 없음, Key Head의 같음만으로 전체 제품의 의미적 동일성을 승인하지 않는다.

```text
정확한 Query Key + Determining Input/Version
    → 이전 Dependency와 현재 Validity Qualification
    → Producer가 규정한 Result Equality Evidence
      ├─ 변화 없음 + 실제 Payload 불필요: Downstream 전파 중단
      ├─ 변화 없음 + 실제 Payload 필요: RAM Hit / Disk Restore / Recompute
      └─ 변화 있음 또는 증거 불충분: Recompute / 허용된 Delta Repair
```

`Product Key`, `HID`, `Result Fingerprint`, `Storage Content Digest`, `Physical Layout Fingerprint`, `Schema Version`, `Publication Generation`의 역할은 다르다. 저장 시스템은 동일한 Hash 구현을 재사용할 수 있지만 의미적 동일성·저장된 Blob의 무결성·현 환경에서의 재사용 적격성을 혼동하지 않는다.

- [ ] Product Family별 Query Granularity, Determining Inputs, Dynamic Dependency Trace, Result Equality, Publication Boundary를 정의한다.
- [ ] Metadata-only Cutoff가 성립하는 정확한 범위와 이전 Dependency Set 변화 처리 방식을 검증한다.
- [ ] 이전 Payload를 비교하려는 목적으로 대형 디스크 Product를 반드시 읽지 않도록 Fingerprint/Validity 경로를 평가한다.
- [ ] Merkle Change Localization, Projection Firewall, Dependency Compression, Shared Arrangement, Lazy Repair, Domain-specific Frontier를 비교한다.
- [ ] 기존 Occurrence가 새로운 판단을 요구하는 경우와 재사용 가능한 Definition/Compiler Knowledge를 분리한다.
- [ ] Clean Full Rebuild는 독립적인 정확성 기준으로 유지한다. 메모리 실패 후 무조건 Full Rebuild로 진입하지 않고 전환 전 Peak Budget를 확인한다.
- [ ] Early Cutoff Rate만이 아니라 실제 생략된 Producer Work, Invalidation 전파량, 유지 Metadata의 RAM 비용을 측정한다.

### 11.2 전역 Versioning과 Product별 유효성의 분리

Versioning은 SDK Meaning에만 있는 기능이 아니라 컴파일러 전체에 걸친다. 그러나 전역 Version 기반 시설이 모든 Producer의 의미를 하나의 Version Counter로 통합하는 것은 아니다. Contract Version, HIR/Established Protocol Revision, SDK Semantic Profile, Product Encoding/Fingerprint Schema, Query Evaluation Generation, JVM Backend/Target Profile, Physical Layout Revision, Persistent Blob Format은 서로 다른 변화 축이다. 어떤 축이 변할 때 어느 Product를 재계산해야 하는지는 그 Producer의 Determining Input과 Validity 법칙으로 정한다.

공통 Query/Database Infrastructure는 변경·의존성·재사용·저장 위치를 중개하고, Material Producer는 정확한 유효성 판단과 동일성 의미를 유지한다. CAS에 존재하는 Blob이 과거에 올바르게 생성됐다는 사실은 현재 Compiler Run에서 그 Blob이 적격하다는 증명이 아니다. 반대로 Host Hardware Profile만 바뀌었다면 의미적으로 같은 Product를 유지하면서 물리 Slab Projection만 다시 생성할 수도 있다.

- [ ] Version/Revision/Generation/Schema/Fingerprint/Physical Layout의 독립 축을 Product Family별로 정의하고 하나의 정수로 합치지 않는다.
- [ ] SDK Catalog·HIR·Established World·Function Summary·Verification·Execution MIR·JVM LIR·PBT·Diagnostic의 변경 원인과 재사용 유효성 표를 작성한다.
- [ ] Query Cache Key, Material Content Digest, Disk Locator 및 Memory Layout Fingerprint를 필요할 때 서로 조합하되 어느 것도 단독으로 Contract Authority나 Product Equality가 되지 않게 한다.
- [ ] 세션을 넘어 재사용되는 Result와 세션에만 유효한 Dense Ordinal·Arena Address·Reader Lease를 분리한다.

## 12. RAM 중심 + 선택적 디스크 영속 재사용

**Kontrakt의 기본은 RAM에서 작업을 수행하는 컴파일러이며 디스크는 비싼 보조 재사용 경로다.** 디스크에 결과가 존재한다는 사실만으로 해당 결과를 읽는 것이 항상 이득은 아니다. Random Read, Metadata Seek, Page Fault, File Open, fsync, Serialization/Deserialization, Compression, Index Lookup, CAS GC, Write Amplification은 일부 Compiler Product의 재계산보다 비쌀 수 있다.

실제 디스크 사용에는 다음 기준이 필요하다. 재사용이 예상되는 Product만 선택적으로 기록하고, 다시 필요할 때에는 **RAM Hit → 적합한 Persisted Metadata 확인 → 선택적 Restore 또는 Recompute**를 비용에 따라 판단한다. 가벼운 Result를 매번 디스크에 쓰고 읽는 구조를 기본값으로 만들지 않는다.

| 저장 형태 | 유리한 점 | 주요 비용과 함정 |
|---|---|---|
| RAM-only Query·Frozen Slab | 세션 내 가장 단순하고 빠른 재사용 경로 | 컴파일러 재시작 후 재계산 |
| Metadata-only Persistence | 결과를 읽지 않고 Dependency/Fingerprint 확인 가능 | 유효성·Version·Index 관리 필요 |
| Selective Immutable Product CAS | 값비싼 불변 Product의 세션 간 재사용 | CAS Locator·Index·작은 객체 분할·Random I/O |
| Append-only Packed Segment | 작은 Product를 묶어서 쓰기·관리 비용 상쇄 | Live/Dead Segment Compaction 및 Write Amplification |
| 큰 Product별 개별 File | Backend Artifact 등 큰 결과의 단순한 저장 | 작은 파일 다수에서 Stat/Open/Directory 비용 |
| Read-only Mapped Slab | OS Demand Paging, 필요한 영역의 직접 조회 가능 | Page Fault·수명·OS Page Cache·Format 이식성 |
| Compressed Cold RAM | 디스크 전에 RAM 용량 효율 확대 | 압축 해제 CPU·동시 두 사본·Promotion 비용 |
| Page/Block 기반 저장 | 수많은 작은 Index·Summary를 압축 관리 | Block Cache·Lookup Layer·I/O Amplification |

Persisted Format이 임의의 FFM `MemorySegment` 주소, JVM Object Address, Native Endianness, 임시 Dense Ordinal을 그대로 의미적 표현으로 사용해서는 안 된다. 정해진 Canonical Product Encoding 또는 별도의 호환성 검사가 있는 Versioned Physical Chunk Format이 필요하다. **캐시 파일을 지워도 Contract 결과는 바뀌지 않아야 한다.** Hardware Profile이 달라질 때도 Semantic Product를 재사용할 수 있지만 이전 Physical Layout을 다시 만들어야 할 수 있다.

### 12.1 디스크 I/O 비용을 최소화하기 위한 TODO

- [ ] `Full Product Key`, `Result Fingerprint`, `Blob Digest`, `Disk Locator`, `Schema/Encoding Version`, `Compiler/Protocol Revision`, `Host Layout Profile`, `Generation`의 역할을 구별한다.
- [ ] RAM에 두는 작은 Persistent Metadata Index를 설계한다. Bloom/Short Head/Sorted Index 등은 후보 필터에만 사용하며 정확 Key 확인을 생략하지 않는다.
- [ ] 저장할 가치가 있는 Product Family와 최소 크기·예상 재사용·재계산 비용·영속 쓰기 비용의 Admission 경계를 정의한다.
- [ ] Cold SSD, OS Page Cache Warm, Memory Mapping, 이미 RAM 적재된 경우를 별도로 측정한다. OS Page Cache 메모리도 공짜로 취급하지 않는다.
- [ ] Random Read 수, 실제 읽고 쓴 Byte, fsync/Flush, Metadata Seek, Serialization/Compression CPU, Restore Latency, Write Amplification, Crash Recovery를 측정한다.
- [ ] **디스크에 저장하지 않는 Fast Path**를 1급 정책으로 둔다. 쓰기나 복원을 피한 것이 재계산보다 더 경제적인지 직접 평가한다.
- [ ] 많은 작은 Result는 Packed Segment/Batch Write를, 큰 불변 Result는 Content-addressed Chunk를 경쟁시키고 자동으로 모두 CAS 객체로 만들지 않는다.
- [ ] Storage Digest의 일치와 Product의 의미·현재 유효성을 혼동하지 않는다. Stale, 손상, 불신뢰 Cache, 호환되지 않는 Schema는 분명히 거부한다.
- [ ] Atomic Manifest/Index Publication, 불완전 Write, Crash, Corrupt Chunk, Cache GC, Orphaned File, 재시작 복구 및 삭제 정책을 실험한다.
- [ ] 여러 Query가 같은 Product를 디스크에서 복원하는 경우에는 적합한 In-flight Join을 허용하되 I/O Worker와 Callback Lane의 역할을 분리한다.
- [ ] Cache Maintenance가 SSD를 과도하게 사용하거나 한정된 RAM에서 Pending I/O·Callback을 무한히 축적하지 않도록 한다.

## 13. RAM Retention과 Persistent Product의 비용 모델

Product를 RAM에 남길지, RAM에서 회수하고 Metadata만 유지할지, Cold RAM에 압축할지, 디스크에 저장할지, 다시 계산할지는 **서로 다른 결정**이다. Product가 유효하더라도 현재 Payload가 필요 없을 수 있고, 디스크에 존재해도 Restore가 더 비쌀 수 있다. 이는 조회·생명주기·증분 선택을 함께 고려한 비용 모델의 대상이다.

```text
RAM Retention의 기대 이득
  = 재계산·복원 절감 예상 비용
  - 조회/Metadata/보존 비용
  - 다른 Hot Result를 밀어내는 기회비용

디스크 Persist의 기대 이득
  = 다음 세션에서 피할 수 있는 재계산 비용
  - Serialize + Write + Index + Read + Verify + Restore
  - Cache GC/Compaction/Storage 관리 비용
```

위 식은 실험용 개념 모델이며 확정된 정책 수식이 아니다. Actual Lifetime, Product Size, 재사용 예측, Invalidated될 확률, Reader Pin, Memory Envelope, I/O 경쟁을 반영해야 한다.

- [ ] 단순 사용 빈도, 크기, Producer 재계산 비용, 유효성 생존, Disk Restore 비용을 결합하는 Retention 정책을 비교한다.
- [ ] `Hot Uncompressed RAM`, 선택적인 `Cold Compressed RAM`, `Persistent Metadata`, `Persistent Content`를 별도 역할로 설명하되 무조건 4단계 계층을 만들지는 않는다.
- [ ] Compression/Decompression·Slab Compaction에서 원본과 새 사본이 동시에 살아 있는 Peak RAM을 회계한다.
- [ ] Disk Restore 실패·Slow SSD·동일 Key 요청 폭주 시 무제한 Join 및 Retained Callback이 발생하지 않도록 Admission한다.
- [ ] 실제 Product 없이 Fingerprint만 남았을 때 가능한 판단과 불가능한 소비를 분명히 표시한다.

## 14. V1/V2 아키텍처 비교와 보존 조건

V1에서는 현재 Primitive Slabbing·Exact Head-first Lookup·Planning L1/L2·M:N Lane·Frozen/HID·Query-oriented Reuse를 보존하고, Aligned Physical Allocation과 Producer-owned Protocol 적합성을 닫는 것이 우선이다. V1이 반드시 정교한 Hardware Auto-tuning이나 완전한 영속 CAS를 가져야 하는 것은 아니다. 자체 Region Lifetime과 실제 하위 메모리 회수의 정합성은 V1부터 필요하다.

V2에서는 Generation/Chunk 간 공유, Metadata-only Early Cutoff, Selected Product Persistence, Compressed Cold RAM, 도메인별 증분 Repair, Cost-based Scheduling, Access-trace 기반 Layout/Graph Reordering, NUMA 등을 도입할 수 있다. 이때 Logical Product Contract와 Physical Store를 교체 가능하게 유지한다.

| V2 후보 | RAM에서 필요한 것 | 핵심 증분 방식 | 디스크·Retention 연구 |
|---|---|---|---|
| A. Red-Green Query | Memo·Dependency Index | Demand Validation, Fingerprint Cutoff | Metadata 우선 영속·선별 복원 |
| B. Active Frontier | Reverse Edge·Changed Set | Push Invalidation, 변화 전파 정지 | 압축 이전 Dependency Index |
| C. Domain-local Hybrid | 도메인별 Index·View | 각각 다른 증분 알고리즘 | Producer별 저장·회수 정책 |
| D. Differential Analysis | Arrangement·Delta State | Relation/Reachability의 차분 유지 | 유지 상태를 저장할 가치 검증 |
| E. Elastic Mode | 모드별 Scratch·Baseline Pin | Full/Incremental의 비용 선택 | Restore·Recompute 전환비용 |
| F. Prediction Cut-first | 이전 실행의 비용 통계 | Cutoff 가능성 중심 Priority | 작은 통계·Fingerprint Metadata |

- [ ] 여섯 후보 각각에 대해 추가 RAM Index/Arrangement, Stale Retention, Reader Pin, Peak Rebuild, Disk I/O 비용을 포함한 Total Cost를 계산한다.
- [ ] 후보 C/E의 결합을 검토하되 ADR 없이 확정하지 않는다. A/B/D/F의 국소 기법도 유효한 대안으로 유지한다.
- [ ] Semantic Product와 Layout Fingerprint가 다르게 바뀌는 상황을 분리한다. Host Hardware Profile이 바뀌어도 동일한 Semantic Product의 재사용이 가능한 경우를 보존한다.

## 15. 비교할 SOTA 소프트웨어와 연구 근거

아래는 **외부에서 실제로 사용하는 기법 또는 발표된 연구**의 비교 목록이다. 외부 시스템이 보고한 성능 수치는 해당 워크로드에서만 의미가 있으며 Kontrakt에서 자동으로 보장되는 값이 아니다.

| 시스템/연구 | 확인할 핵심 기법 | Kontrakt의 질문 |
|---|---|---|
| LMAX Disruptor | Preallocated Ring, Sequence, Cacheline Padding, Batch | Worker·Lane Mutable Control의 False Sharing을 줄일 수 있는가 |
| Aeron / Agrona | Primitive Buffer·Queue, Bounded Work | Callback·Hot Loop의 객체/동기화 비용을 얼마나 줄일 수 있는가 |
| OpenHFT Chronicle Values | Field Alignment, Generated Flyweight | 직접 구현한 Layout에 Specialized Accessor를 붙이는 이익은 무엇인가 |
| Apache Arrow | Aligned/Padded Columnar Buffer | 정렬된 SoA/Range Slab의 Scan을 어떻게 활용할 것인가 |
| Google Abseil SwissTable | Grouped Metadata·짧은 Hash Tag | 현재 Head-first 조회의 Cacheline 접근을 더 줄일 수 있는가 |
| Meta Folly F14 | SIMD Probe Group·Chunk별 저장 | Exact HID 비교 전 후보를 얼마나 싸게 거를 수 있는가 |
| DuckDB / Meta Velox | Vector Batch·Dictionary·Selection·Lazy Materialization | Verifier와 Relation 처리의 Batch/Fusion·Hot Projection 효과는 무엇인가 |
| ClickHouse | Cache-aware Hash Table·Batched Probe·Prefetch 연구 | 무작위 HID 조회의 Memory Latency를 은닉할 수 있는가 |
| Roaring Bitmap | Array/Bitmap/Run 적응형 컨테이너 | CFG·Dataflow·Applicability Set의 RAM과 실행시간은 어떻게 변하는가 |
| Meta CacheLib | RAM Slab Pool·Size-class·Rebalance | 파편화된 Slab과 Reader/Handle Pin을 어떻게 회수할 것인가 |
| RocksDB HyperClock / Tiered Cache | 저비용 Hit Metadata, 저장 계층 | RAM Cache의 공유 쓰기와 Cold Data를 어떻게 줄일 것인가 |
| **S3-FIFO (SOSP 2023)** | Quick Demotion·3 FIFO Queue | **기존 적용 후보**: 일회성 Product의 오염을 막는가 |
| **SIEVE (NSDI 2024)** | Lazy Eviction·저비용 Cache Hit | **기존 적용 후보**: Shard/Partition에서 관리비용이 줄어드는가 |
| Linux RCU / Folly Hazard Pointer | Reader Protection·지연 회수 | 빠른 읽기와 Retired RAM 누적을 어떻게 절충하는가 |
| rustc Incremental | Red-Green, Fingerprint, 선택적 이전 Result 복원 | 실제 Payload 없이도 Early Cutoff를 판단할 수 있는가 |
| Buck2 Modern DICE | Shared Async Evaluation, Result State | 기존 In-flight/Lane과 Query Eval을 어떻게 분리 연결하는가 |
| LLVM CAS·Analysis Preservation | Content-addressed Material, Validity | Product와 Physical Storage의 독립성을 어떻게 검증하는가 |
| SIGMOD 2025, *Data Chunk Compaction in Vectorized Execution* | 동적 Logical Compaction | Sparse Batch가 많은 Verifier·Join에 이득이 있는가 |
| PPoPP 2025, *Frontier-guided Graph Reordering* | 활성 Frontier 기반 Vertex 재배열 | CFG·SCC·Dependency Traversal의 Locality가 개선되는가 |
| SEA 2025 Index 비교 | SIMD B-tree·압축·Learned Index | 압축률과 Lookup Latency 중 어느 것이 Hot Index에 유리한가 |
| 2026 Buffer Pool Locality/Fast-path 연구 | 위치 예측과 정확 검증 | 반복 Hash Lookup을 검증된 직접 위치로 대체할 수 있는가 |

**외부 근거 시작점:**

- S3-FIFO: <https://pdl.cmu.edu/PDL-FTP/Storage/FIFOqueues-SOSP23_abs.shtml>
- SIEVE: <https://www.usenix.org/conference/nsdi24/presentation/zhang-yazhuo>
- Java `alignmentOffset()`/`alignedSlice()`: <https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/nio/ByteBuffer.html>
- Unsafe API 제거 예정 관련 JDK 문서: <https://docs.oracle.com/en/java/javase/26/migrate/removed-tools-components.html>
- SIGMOD 2025 Chunk Compaction 논문: <https://people.iiis.tsinghua.edu.cn/~huanchen/publications/data-chunk-compaction-sigmod25.pdf>
- PPoPP 2025 Graph Reordering: <https://www.sigplan.org/OpenTOC/ppopp25.html>

이 외의 LMAX·Aeron·Chronicle·Arrow·Velox·ClickHouse·F14·CacheLib·RocksDB 구현은 실제 적용 실험을 시작할 때 정확한 Source Commit과 Hardware 조건을 Evidence Ledger에 추가해야 한다. 이번 문서는 특정 구현 Revision을 검증한 성능표인 것처럼 취급하지 않는다.

## 16. 벤치마크와 성능·메모리·I/O 평가

정렬이나 4B Load만 측정하는 Microbenchmark는 원인을 분리하는 데 유용하지만 최종 선택 근거로 충분하지 않다. 동일한 Semantic Product와 실제 Compiler Workload를 사용해 **주소 정렬 효과, Layout 효과, Access API 효과, Query 재사용 효과, Worker 동시성 효과**를 별도로 확인한다. 실제 Compile Time과 Peak Memory가 최종 기준이다.

### 16.1 물리 및 Cache Benchmark

| 워크로드 | 비교 | 필수 지표 |
|---|---|---|
| Split Load | Aligned/Misaligned, 경계 횡단 여부 | Access Latency, Split Load, Cycle |
| Operation→Fact Range | Current Slab, AoS/SoA/AoSoA, Direct Projection | Line 수, Range Scan/s, RAM Footprint |
| HID→Ordinal | 기존 Two-phase, Grouped Tag, Static/Sorted Index | Probe, Full-key Compare, Cache Miss, P95/P99 |
| CFG/SSA Traversal | 원래 Ordinal, Locality Reorder, Tile Size | LLC/DTLB, Block/s, Reorder Cost |
| Verifier Batch | Scalar, Vector, Selection, Compaction, Fusion | Tuple/s, 유효 밀도, Scratch Peak |
| Worker/Lane | Padded/Unpadded Counter, Queue/Shard 구조 | Coherence, Callback/s, Queue Tail |
| Slab 생성·종료 | Heap, Aligned DirectBuffer, FFM | Allocation, Init, Actual RSS, Reclaim Latency |
| Generation Turnover | Single/Overlap/Long Reader | Active+Retired Peak, Pin Duration |
| Cold Reuse | RAM-only, Metadata-only, CAS, Packed, Mapped | Disk Read/Write, Restore, Recompute Cost |
| Memory Pressure | Working Set 대비 제한 RAM | Valid Result, Bounded Failure, Peak Envelope |

### 16.2 V2 시나리오 매트릭스

- 소스 변경 비율: `0%, 0.1%, 1%, 5%, 20%, 60%`.
- RAM 상한: Working Set의 `25%, 50%, 100%, 200%`.
- Query 요청: 균일, Hot-key 편향, 단발성 대량 요청, Exact Collision 유도.
- 실행: 단일/다중 Generation, 장기 Reader, 동시 동일 Key Join 폭주, Callback 정체.
- 디스크: 완전히 Cold SSD, OS Page Cache Warm, 압축/비압축, Cache 손상/중단.
- 증분: Exact Hit, Early Cutoff, Partial Repair, Full Recompute, Disk Restore.

이 수치는 **실험 축**이지 제품 기본값이 아니다. 측정 결과에는 Compile Duration, P50/P95/P99 Query Latency, CPU Cycles, Instructions, Cache/DTLB/Branch Miss, Memory Bandwidth, Actual Used/Reserved/Pinned/Retired/Fragmented/Metadata Peak Bytes, GC·Direct RSS, 회수 지연, 정확 비교 건수, 중복 계산 절감량, Disk IOPS/Bytes, Write Amplification, 복원/직렬화 시간을 기록한다.

- [ ] JDK Version, x86-64/AArch64, CPU Frequency, NUMA, Huge Page, Heap·Direct 제한, JIT Warmup을 기록하고 Host/Target Profile을 혼동하지 않는다.
- [ ] JIT가 실험 코드를 제거하거나 상수 접기하지 않았는지 검증하고 실제 Memory Address Alignment를 매 실험에서 확인한다.
- [ ] Disk Persist는 두 번째 Build의 가속뿐 아니라 최초 Write·Flush·Cache GC 비용, Cold Start Regression을 보고한다.
- [ ] Cache Hit가 증가했지만 작업 시간이 늘어난 사례도 제외하지 않는다. Profile은 원인 파악 자료이지 결과 자체가 아니다.
- [ ] 장시간 Daemon 및 Memory Pressure 시험에서 Pinned/Retired Peak와 Reclaim Latency를 별도 1급 지표로 둔다.

## 17. Correctness·Failure·Conformance

모든 물리 최적화는 같은 의미 자료에 대해 Producer-owned Protocol과 Clean Recompute의 요구 결과를 보존해야 한다. Hash·HID·Cache·FFM·Stored Blob이 올바른 판단을 대신하지 않는다. 물리 배치가 달라도 같아야 할 항목과 Storage Format 때문에 달라도 되는 항목을 구별해 Golden Vector로 검증한다.

- [ ] Canonical Bytes, HID Derivation, Stable Ordering, Exact Collision Comparison, Semantic Reference, 해당 Contract Judgment의 동일성을 확인한다.
- [ ] HIR Candidate와 Canonical Contract World의 Observation을 구별하고, Store 교체가 Authority를 이동시키지 않는지 확인한다.
- [ ] 새로운 Occurrence의 의미 판단이 필요한 경우에는 이전 Occurrence 자체를 재사용하지 않고 허용된 Definition/Compiler Knowledge만 재사용한다.
- [ ] Verification, Reference, PBT, Diagnostic, JVM LIR/Classfile의 의미와 필요한 재현성을 확인한다.
- [ ] Bounds, Alignment, Endianness, Sentinel, Padding, Integer Overflow, Capacity, Use-after-release, Cross-generation Contamination을 테스트한다.
- [ ] 동일 Head의 서로 다른 Full Key를 정확히 처리하고, Negative Cache·Stale Fingerprint가 잘못된 Hit로 전환되지 않도록 한다.
- [ ] Staging/Partial Publication 노출 금지, Cache Blob 손상, Write 중 Crash, Index/Manifest 불일치, Rebuild 중 실패를 검증한다.
- [ ] Callback/Reader/Reclaimer Race: Cancel, Timeout, Double Clear, Late Completion, Retired Reader, Compaction 실패, Pin 잔류를 검사한다.
- [ ] Clean Full Recompute와 RAM Hit, Persistent Restore, Early Cutoff, Delta Update, Parallel Scheduler의 동일 결과를 확인한다.
- [ ] 모든 Cache를 비워도 결과의 의미가 바뀌지 않으며, Cache가 가득 차더라도 Error/Diagnostic Reserve가 보호되는지 확인한다.

## 18. 작업 순서

작업을 나누되 이미 구현한 내용을 처음부터 다시 설계하지 않는다. 아래 순서는 의존성에 따른 연구·검증 순서이며 구현 난이도에 따른 순위가 아니다.

### A. V1 메모리 기반의 실제 상태 검증

- [ ] 기존 Primitive Slabbing의 Payload, Padding, Stride, Hot/Cold Field, Sentinel, Capacity, Region Allocation 구현을 코드 단위로 Inventory한다.
- [ ] 정렬만 제공하는 최소 `AlignedRegion` 경계를 정하고 DirectBuffer와 FFM의 주소·수명 조건을 비교한다.
- [ ] 작은 Host Hardware Profile, Access Profile, Physical Layout Plan과 Produced Material의 Protocol 경계를 명시한다.
- [ ] Cacheline·Memory Lifetime·동일 의미 Conformance Golden Vector를 만든다.
- [ ] ADR-0041·0042·0044의 현행 Status와 상위 규칙을 확인하여 기존 Memory Envelope의 결정과 연구 후보를 분리한다.

### B. 기존 L1/L2·HID·Lane 통합

- [ ] L1 Scratch를 유지하고 L2의 Interning과 Query Product Cache가 중복하는 부분을 정밀하게 분리한다.
- [ ] `Head/Route → Exact Key`를 보존한 상태로 Grouped Metadata Index와 Batched Probe를 실측한다.
- [ ] Lane-owned Delivery와 Query In-flight Join을 연결하되 소유권·Callback Lifecycle을 변경하지 않는다.
- [ ] 이미 조사한 S3-FIFO·SIEVE를 실제 Producer별 Admission·Retention·Reclamation 실험에 반영한다.

### C. 고밀도·캐시 지역성 향상

- [ ] Material별 AoS/SoA/AoSoA, Hot/Cold, Direct Projection, Specialized Accessor, Compact Ordinal을 비교한다.
- [ ] Vector Batch, Sparse Selection, Operator Fusion, Roaring형 Container를 반복 분석에 적용 가능한지 검증한다.
- [ ] CFG·Relation Reordering, Batched Probe, Huge Page·NUMA, Cache-set Conflict는 프로파일링에서 병목이 확인된 곳부터 시험한다.

### D. V2 증분·영속 인터페이스

- [ ] Product Key·Equality·Fingerprint·Validity·Schema·Persistent Encoding을 물리 Slab Layout에서 분리한다.
- [ ] RAM-only, Metadata-only Persistence, Selective Immutable CAS/Packed Segment, 필요 시 Compressed RAM/Mapped Product를 경쟁시킨다.
- [ ] Disk Admission, Restore Join, Atomic Commit, Corruption Recovery, Cache GC, 과도한 I/O 방지 정책을 설계한다.
- [ ] V2 A~F 후보별 Dependency Metadata·Arrangement·Old Generation Peak·재계산 절약·Disk 비용을 계산한다.

### E. 문서화·결정 승격

- [ ] 실제 측정과 Semantic Equivalence를 통과한 공통 원칙만 ADR 후보로 제안한다.
- [ ] Exact Field Offset, Layout Heuristic, Cache Policy, Memory API, Disk Format, Threshold는 교체 가능한 Design/Implementation 문서에 둔다.
- [ ] 오래된 Heap/FFM 기준을 가진 ADR-0041/0042, Release Readiness TODO, Compiler Map은 충돌을 확인한 뒤 상태에 맞게 Migration한다.

## 19. 미결정 사항 레지스터

| ID | 결정해야 할 내용 | 닫는 데 필요한 증거 |
|---|---|---|
| MEM-01 | 최소 정렬용 Backend가 DirectBuffer인지 FFM인지 | 주소 정렬, Access Cost, 명시적 Free, Daemon Peak |
| MEM-02 | Region·Chunk·Slab의 소유·회수 단위 | Pinned Waste, 회수 지연, 파편화 |
| MEM-03 | Material Family별 AoS/SoA/AoSoA·Hot/Cold | Field Coaccess, Cacheline Touch, 전체 컴파일 시간 |
| MEM-04 | Host Hardware Profile Schema와 Pinning | Host/Target 구별, 재현성, Unsupported 항목 |
| MEM-05 | 파생 Hot Projection의 재사용·갱신·회수 | 중복 RAM, 재생성 시간, Invalidity |
| MEM-06 | HID Index 종류·Tag/Load Threshold | 실제 조회 Trace, Exact Collision, Probe |
| MEM-07 | Worker/Lane Padding과 Ring 방식 | Coherence, Queue Tail, 결정적 처리 |
| MEM-08 | 각 수명별 Epoch·Lease·Reclaim 방식 | Grace Period, Retired Peak, Fault Recovery |
| MEM-09 | S3-FIFO·SIEVE의 적용 범위·Admission | 기존 연구와 실제 Byte/Cost Trace |
| MEM-10 | V2 Query Granularity와 Cutoff Metadata | Dependency 정확성, Saved Work, RAM Metadata |
| MEM-11 | Cold Compressed RAM 도입 여부 | 압축 비용, Hot 승격, Double Peak |
| MEM-12 | Disk Product·Index·Chunk의 저장 단위 | Random I/O, Write Amplification, Schema |
| MEM-13 | Restore와 Recompute의 선택 기준 | Cold/Warm Disk, Producer Cost, Validity |
| MEM-14 | Disk GC·Compaction·Crash Recovery | Atomic Publication, Orphan Chunk, Tail |
| MEM-15 | V2 A~F/Hybrid의 실행 선택 | End-to-end, RAM/Retired Cost, Early Cutoff |
| MEM-16 | NUMA·Huge Page·Graph Reorder 시점 | Actual Hardware Profiling, Cache/TLB 비용 |
| MEM-17 | 물리 Backend 교체와 Protocol Compatibility | 같은 Semantic Observation·JDK별 Test |
| MEM-18 | ADR-0041/0042/0044 상태 정합성 | 현재 승인 상태 및 Normative Source 확인 |

## 20. 누락 검사용 Coverage Matrix

이 표는 중복 설명이 아니라 항목 누락 점검용이다. 이후 수정 시 항목이 실제 소유 절에 남아 있는지 확인한다.

| 지금까지 논의한 주제 | 본문 위치 |
|---|---|
| 계약/구현 분리, Producer-owned Protocol, Semantic/Physical Identity | §1, §17 |
| CPU 캐시라인 시작·끝, Split Load, 가상 주소·페이지, False Sharing | §2, §7 |
| Hardware Profile과 Compiler Host/Target JVM 구별 | §2, §3, §16 |
| 기존 Payload·Padding·Stride·직접 Arena 구현 | §3, §4 |
| FFM은 정렬된 주소 확보용 물리 수단이라는 실제 목적 | §3 |
| DirectBuffer alignmentOffset/alignedSlice와 실제 회수 제약 | §3 |
| Heap Primitive 비교의 올바른 위치, Unsafe 제거 예정, Native Kernel 제외 | §3, §16 |
| Fixed-stride / Range / Cold / Canonical Byte / HID Index Slab | §3, §4 |
| AoS·SoA·AoSoA, Hot/Cold, Generated Accessor, Tile | §4, §10 |
| HID Head-first, Exact Full-Key, Swiss/F14, SDK Static Lookup | §5 |
| Planning L1/L2, Global Query Cache 및 Interning 구분 | §6 |
| M:N Lane, Callback, Shard·Worker 책임과 Sequence Ring | §7 |
| 자체 Region, Epoch/RCU/Hazard Pointer, CacheLib, 파편화 | §8 |
| RAM Envelope, Retired/Pinned, Reserve, Continuation/Quarantine | §8, §17 |
| **S3-FIFO·SIEVE 기존 조사·적용 대상** | §9 |
| LMAX/Agrona/Arrow/Chronicle/DuckDB/Velox/ClickHouse/Roaring | §7, §10, §15 |
| Prefetch, SIMD, Graph Reordering, NUMA/Huge Page/TLB | §10, §15, §16 |
| V2 A~F, Metadata-only Early Cutoff, Fingerprint·Validity 구별 | §11, §14 |
| 비싼 Disk I/O, CAS/Packed/Mmap/Compressed RAM, Crash Recovery | §12, §13 |
| RAM-first Retention, Disk Restore versus Recompute, Versioned Schema | §12, §13 |
| CPU/RAM/디스크 통합 Benchmark, Clean Recompute, Fault Test | §16, §17 |
| V1 보존, V2 확장, 우선 작업, 미결정 사항 | §14, §18, §19 |

## 21. 완료 조건

이 TODO의 연구·설계 준비가 끝났다고 판단하려면 각 Material Family마다 **생산자가 제공하는 의미와 Protocol, 정확한 조회 Key, 물리 Layout과 Hardware Profile, Address Alignment 및 Field/Range 배치, 소유·공개·Pin·Retire·Reclaim 시점, Cache Hit/Miss·실패·Cancel 처리, RAM 퇴출 후 남기는 Metadata, Disk 복원 또는 재계산 선택, Memory Envelope, 실제 벤치마크와 Clean Recompute 동등성**을 설명할 수 있어야 한다.

이 문서는 특정 FFM API, S3-FIFO/SIEVE 중 하나, CAS 형식 또는 V2 증분 알고리즘을 새로 확정하지 않는다. 목적은 V1에서 축적한 Primitive Slabbing·정확 Key·Lane 소유권·Frozen Result를 버리지 않고, **캐시라인 정렬뿐 아니라 RAM 생명주기와 선택적 디스크 재사용까지 전체 시스템 비용으로 비교할 수 있는 결정 기반**을 만드는 것이다.
