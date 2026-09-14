# ADR-0073: JVM Platform-Native Surface, Contract Preservation, and External Technology Boundary

## Status

Proposed

## Date

2026-09-15

## Related

- *What Contract Is*
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations and Pipeline-Slot Selection
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0072: JVM Collection Contract Preservation, Aggregate Semantics, and Deterministic Equality
- Kontrakt Compiler Total Architecture Map
- *Modern Compiler Architecture 01–15*

---

# 1. Context

Kontrakt runs on the JVM and exposes Java and Kotlin user surfaces.

That platform choice creates a narrow compatibility obligation.

A user must be able to declare ordinary JVM values without replacing every basic Java or Kotlin type with a
Kontrakt-specific wrapper. At the same time, Kontrakt must not treat the entire Java SE, JDK, Kotlin standard library,
or
third-party ecosystem as Contract-native technology.

The JVM platform contains both plain value surfaces and live capabilities. They cannot share one admission rule.

A `BigDecimal` is an immutable value with a defined equality and arithmetic contract. A system clock, environment
lookup,
file handle, reflective object, executor, or network client owns behavior that depends on runtime state or an external
resource.

Both may be part of a standard library. Only the first kind is a candidate for direct Contract-facing use.

Kontrakt therefore needs an explicit boundary between the JVM surface it recognizes directly and technology that must
enter through an Adapter or another declared external boundary.

This boundary is also required for optimization.

A supported platform value may later be represented by a slab, primitive lanes, a compact table, or another backend
shape. That freedom is legal only when the platform contract that remains observable has already been resolved and is
preserved independently of the physical representation.

---

# 2. Problem

A package name cannot define the boundary.

`java.base` is the foundational Java SE module, but it contains reflection, foreign-memory access, I/O support, runtime
services, system access, and other APIs that are not plain Contract values.

Kotlin has the same problem. Membership in `kotlin-stdlib` or a default import does not make an API suitable for direct
Contract use. The standard library includes lazy sequences and I/O helpers in addition to basic values and collections.

A second problem appears when type admission and operation admission are treated as the same thing.

A value type may be suitable for direct use while some operations on that type read hidden environment state. An
`Instant` value is deterministic material. Obtaining the current instant from a system clock is not the same kind of
operation.

A third problem appears if Kontrakt preserves too little.

When the user selects a supported JVM surface, Kontrakt must not weaken the observable contract merely because a smaller
internal model is easier to optimize. Equality, order, scale, range, or another visible distinction cannot disappear
before an authority is allowed to change it.

The opposite error is also dangerous.

Supporting a JVM value does not make Java or Kotlin a new Contract authority. A later 1D Contract may reject,
canonicalize, lower, or replace that value meaning under its own explicit law.

Kontrakt needs one narrow platform rule that preserves what the host platform actually promises without allowing
platform
technology to escape its proper boundary.

---

# 3. Decision Drivers

Kontrakt remains a Contract machine rather than a Java framework.

The JVM platform is a host constraint. Platform compatibility does not grant general authority to Java libraries,
Kotlin libraries, frameworks, providers, or runtime services.

Ordinary Java and Kotlin values must remain natural to declare and use.

A supported platform contract must be preserved exactly until an explicit Kontrakt authority establishes different
meaning.

The same source and the same explicit platform basis must produce the same Contract meaning regardless of runtime
schedule, environment accident, allocation, cache state, or physical compiler layout.

Frontend resolution must close platform meaning before HIR becomes visible.

Established meaning must remain independent of the physical representation selected by optimization or backend
realization.

Unknown or only partially understood platform behavior must fail closed rather than being approximated as a familiar
value.

The supported JVM surface must evolve deliberately with supported Java and Kotlin versions.

---

# 4. Decision

## 4.1. Platform-Native Surface Is a Narrow Compatibility Boundary

Kontrakt recognizes an explicit **JVM Platform-Native Surface**.

This surface exists because Kontrakt is hosted on the JVM and must provide ordinary Java and Kotlin Contract-facing
APIs.
It is not a general permission for external technology to enter the governed machine.

A platform type or operation belongs to the Platform-Native Surface only when Kontrakt has explicitly audited and
supported its relevant contract for the selected platform version.

```text
JVM / Java / Kotlin declaration
    ↓
Platform-Native Profile
    ↓
resolved platform contract
    ↓
Kontrakt Contract processing
```

Anything outside that profile remains external until an explicit Adapter or other approved boundary converts it into
material that Kontrakt can admit.

---

## 4.2. Module, Package, or Library Membership Is Not Admission

No Java or Kotlin namespace is admitted as a whole.

`java.base`, `java.*`, `kotlin.*`, `kotlin-stdlib`, default imports, or installation with a supported JDK are evidence
of
platform origin. They do not establish Contract suitability.

Kontrakt classifies the supported surface by contract behavior rather than namespace alone.

A standard API may still remain outside the native Contract surface. This is the case when its meaning depends on a live
capability or external environment rather than one closed value.

A third-party library does not become native merely because it wraps a supported platform value.

---

## 4.3. Value Admission and Operation Admission Are Separate

Admitting a value type does not admit every operation defined on that type.

Kontrakt distinguishes the contract of the value from operations that acquire hidden state or external capability.

For example, an established `Instant` value may be legal Contract material. Reading the current time from a system clock
is an external capability unless that time enters through an explicit Contract basis or Adapter.

A deterministic operation over explicit admitted operands may be supported when its complete observable law is known.
An operation that depends on hidden environment state is not admitted merely because it is declared on an otherwise
supported type.

This distinction applies equally to static helpers, constructors, factories, extension functions, and instance methods.

---

## 4.4. V1 Native Value Baseline

V1 directly supports the JVM value surface that is already necessary for ordinary Java and Kotlin Contract declarations.

The baseline includes primitive values and their ordinary boxed value form. `String`, arrays, primitive arrays, and
enum values are also native candidates.

Standard Java and Kotlin collection values are governed by ADR-0072. Collection support preserves the actual supported
platform contract rather than replacing it with one smaller Kontrakt container contract.

`BigInteger` and `BigDecimal` are native numeric values. Their exact Java contract remains visible where the owning
Contract has not established different meaning.

Major immutable `java.time` value types are also native value candidates. Their value meaning is distinct from
operations
that consult a clock, default time zone, provider, or other environment state.

The exact supported type set is recorded by the versioned Platform-Native Profile rather than frozen as one permanent
list in this ADR.

---

## 4.5. Platform Capability and External Technology Remain Outside

A type or operation that primarily represents a live capability does not become Contract-native value material.

File and network access are outside this boundary. The same is true for operations that acquire time, randomness,
environment state, reflection capability, native access, or execution resources from the running platform.

The same rule applies above the JVM platform. A framework or optional library remains external even when it is widely
used or presents ordinary Java and Kotlin types at its API boundary.

Such technology may participate only through the explicit Adapter, binding, or realization boundaries allowed by the
surrounding architecture.

The Adapter may produce admitted values. It does not transfer its technology contract into the governed Contract
machine.

---

## 4.6. Stable Platform Surface Is Required

The native baseline uses stable platform contracts.

Preview, incubating, experimental, vendor-specific, or implementation-specific APIs are not automatically admitted.
Their presence in one JDK or Kotlin distribution does not make them part of the portable Kontrakt surface.

A future decision may support one deliberately, but that support must be explicit and version-scoped.

Unknown platform behavior is not guessed.

```text
not classified
    ↓
not native
    ↓
Adapter or unsupported declaration
```

---

## 4.7. Platform Contract Preservation Does Not Create Platform Authority

The Platform-Native Profile tells the compiler which external platform distinctions must be respected.

It does not create a new Contract authority.

If Input admits a supported JVM value, Input owns the established Input meaning while preserving the relevant platform
contract selected by the user.

A later 1D Contract remains sovereign over its own result.

Canonicalization may establish a new representative relation. Lowering may establish target meaning that no longer
preserves every source distinction. Fact may impose its own sameness law.

The change is legal because the later authority establishes new meaning explicitly.

It is not legal for frontend resolution, HIR compaction, backend lowering, or optimization to perform the same change
silently.

---

# 5. Platform-Native Profile

## 5.1. Versioned Compiler Knowledge

The Platform-Native Profile is version-sensitive compiler knowledge.

It records the supported Java and Kotlin surface for the configured platform target. A new JDK or Kotlin version may add
new candidate types, change availability, or introduce a stronger public contract that Kontrakt must understand before
it
claims support.

Platform support therefore requires a compatibility audit rather than only successful compilation against the new SDK.

The profile itself is not Contract authority and is not a runtime service registry.

It is compiler knowledge used to resolve a host declaration into explicit semantic obligations.

---

## 5.2. Profile Classification

For each supported surface, the compiler must know whether Kontrakt supports the value, the relevant operations, or
both.

The profile must also preserve the observable distinctions required by that support. It may record platform availability
and the Contract-facing roles in which the surface is legal.

The physical schema of the profile is not fixed by this ADR.

A generated table, frozen metadata, compiler source table, or another deterministic representation may implement the
same profile law.

---

## 5.3. Kotlin/JVM Mapping Does Not Weaken Java Meaning

Kotlin/JVM may expose Java platform types through Kotlin language-facing types or mapped collection interfaces.

That mapping does not permit Kontrakt to weaken the underlying observable contract.

The frontend resolves the actual supported surface selected by the user and preserves every distinction required at that
boundary.

Kotlin convenience syntax is not an independent authority for changing Java platform meaning, and Java implementation
detail is not an authority for changing Kotlin-declared meaning.

---

# 6. Frontend and Resolved Contract HIR

## 6.1. Platform Resolution Happens Before Visible HIR

The frontend resolves a supported platform declaration before the owning Contract candidate becomes Visible HIR.

Later stages do not reopen Java or Kotlin source, reflect over a runtime class, or inspect one current JDK
implementation
to recover missing meaning.

```text
host declaration
    ↓
Platform-Native Profile resolution
    ↓
complete Contract candidate meaning
    ↓
Visible HIR
```

If the platform contract cannot be resolved completely enough for the selected role, the declaration is not admitted as
native Contract material.

---

## 6.2. Platform Contract Distinctions Are HIR Meaning When Required

HIR may erase implementation mechanics that do not determine Contract meaning.

It may not erase an admitted platform distinction that is still required by the owning Contract or by a later legal user
observer.

A concrete object layout may disappear. A required equality, scale, range, order, or other observable law may not.

The HIR representation does not need to reproduce the host type hierarchy. It must preserve enough resolved meaning to
interpret the candidate without reopening that hierarchy.

---

## 6.3. Platform Surface Binding and Semantic Meaning Remain Distinct

The semantic meaning consumed by Establishment is not the same thing as the JVM surface that may later be presented to
user code.

HIR may therefore need to preserve both the resolved semantic obligation and the exact platform-facing binding needed by
later legal boundaries.

Those meanings must not be collapsed merely because one physical table can store them together.

A platform surface reference also does not become the identity of the Contract Definition unless the owning Contract law
makes that surface definition-determining.

---

## 6.4. HIR Information-Loss Rule

A compiler representation may discard a platform distinction only after that distinction is no longer required by any
remaining authority or legal observation boundary.

This is an information-loss boundary.

The burden is on the transformation that removes the distinction. Physical convenience is not proof that the meaning is
unused.

V1 does not require one universal proof object for this rule. The semantic obligation itself is fixed here; analysis and
verification machinery remain compiler design.

---

# 7. Establishment

## 7.1. Establishment Preserves Admitted Source Meaning

ADR-0063 remains the general law.

When resolved candidate material includes an admitted platform contract distinction, Establishment preserves that
distinction as part of the meaning owned by the establishing Contract.

Establishment does not normalize, weaken, or replace the platform contract for compiler convenience.

The resulting Established Material remains Kontrakt material owned by its source Contract.

The platform does not become an independent established authority.

---

## 7.2. Later Authority May Establish Different Meaning

Preservation at one boundary does not prohibit later Contract judgment.

A later authority may reject the value or establish a new semantic relation under its own explicit law.

For example, Input may preserve the exact equality of an admitted numeric presentation while Canonicalization later
establishes one canonical representative under its own Contract.

The later result does not retroactively change what Input meant.

---

# 8. User Realization Boundary

## 8.1. Declared Platform Contracts Remain Observable to User Code

When Kontrakt exposes a supported platform-native surface to user realization, that surface must satisfy the contract
the
user declared.

Internal optimization does not grant permission to provide a weaker substitute.

A user realization may rely on the legal behavior of the declared Java or Kotlin surface. Kontrakt must either preserve
that behavior or reject the unsupported declaration before execution.

---

## 8.2. User Realization Does Not Admit External Technology Transitively

Using a platform-native value in an Operation does not make arbitrary operations reachable from that value legal inside
the governed realization.

The realization verifier still enforces the external-technology boundary.

A legal value can coexist with illegal capability acquisition.

The presence of one native type is therefore not a bridge through which reflection, file access, current-time reads,
framework interception, or another external mechanism can enter the pipeline.

---

# 9. Realization and Optimization Boundary

Kontrakt may realize an established platform-native value with a different physical representation.

A JVM object may disappear. A collection may become primitive lanes. A numeric value may be split into compact fields.
Those choices are implementation.

The optimization is legal only while every remaining Contract-visible and user-visible obligation is preserved.

```text
established semantic obligation
    ↓ remains authoritative
optimized physical material
    ↓ may change freely
legal observation boundary
    ↓ sees the required contract
```

Slabbing is therefore not semantic erasure.

It is a physical realization beneath an intact semantic obligation.

HIR, later IR, analysis, or another compiler product must retain whatever semantic anchor is required to prove that the
optimized material still realizes the correct meaning.

This ADR does not choose MIR, LIR, slab layout, projection strategy, or verification algorithm.

---

# 10. Adapter Boundary

An Adapter converts external technology into material that may legally cross a Kontrakt boundary.

The Adapter remains outside the governed Contract machine unless another ADR explicitly grants a narrower role.

A framework annotation, serializer behavior, database session, network client, service provider, or runtime proxy cannot
be treated as native merely because it eventually produces a supported Java value.

The value may cross after formation. The external technology does not cross with it.

This preserves the single-airlock direction already required by the surrounding architecture.

---

# 11. Platform Evolution

Support for a new Java or Kotlin version requires a Platform-Native Profile audit.

A newly introduced standard value abstraction may be added when its relevant contract can be resolved, represented, and
preserved through the existing Contract and compiler boundaries.

A newly introduced capability API remains outside even if it is part of the standard platform.

A version upgrade must not silently change the meaning of an already supported Contract surface.

If the platform itself changes a relevant observable contract, Kontrakt treats that change as version-sensitive compiler
knowledge and applies the normal compatibility and versioning rules before claiming support.

---

# 12. Rejected Directions

## 12.1. Admit All of `java.base`

Rejected.

`java.base` contains fundamental values and platform mechanisms. Module membership is too broad to define the Contract
boundary.

---

## 12.2. Admit All of `kotlin-stdlib`

Rejected.

The standard library contains ordinary values and APIs that perform lazy computation, I/O, or other behavior unsuitable
for direct Contract admission.

---

## 12.3. Treat Every Method on an Admitted Type as Native

Rejected.

A type may be a legal value while one of its operations consults a clock, environment, provider, or another hidden
input.
Type support and operation support remain separate.

---

## 12.4. Preserve Only a Kontrakt-Preferred Subset of Platform Meaning

Rejected.

Kontrakt may not claim to support a Java or Kotlin surface while silently weakening the contract the user selected.
Different meaning requires a later explicit Contract authority.

---

## 12.5. Treat Standard-Library Capability as Native Because It Ships With the JDK

Rejected.

Installation origin does not turn I/O, reflection, threading, environment access, or another capability into plain
Contract value material.

---

## 12.6. Treat Third-Party Libraries as Platform-Native

Rejected by default.

External libraries and frameworks remain Adapter technologies even when they are common, deterministic in one use case,
or built entirely from Java and Kotlin types.

---

## 12.7. Recover Platform Meaning in the Backend

Rejected.

Backend inspection of classes, methods, or current runtime behavior cannot replace frontend resolution and HIR semantic
preservation.

---

# 13. Consequences

Java and Kotlin user APIs can remain natural without making the entire JVM ecosystem part of Contract authority.

Frontend work becomes more explicit. Supported platform surfaces require deterministic resolution through a versioned
profile instead of heuristic recognition.

HIR must retain platform obligations that remain semantically or observationally relevant. This gives later
optimization more freedom because physical representation can change without losing the meaning needed to verify that
change.

The Adapter boundary becomes stricter. Standard-library origin is no longer enough to bypass it.

Supporting a new JDK or Kotlin version now includes a semantic compatibility audit. That is additional maintenance, but
it prevents silent contract drift as the platform evolves.

ADR-0064 and ADR-0072 must refer to this ADR for the definition of a supported JVM platform-native surface.
ADR-0071 requires a small corresponding clarification in its HIR information-loss law.

---

# 14. Intentionally Open

The exact physical schema of the Platform-Native Profile remains open.

The complete V1 table of supported platform types and deterministic operations remains compiler-owned versioned data.
This ADR fixes the admission law rather than freezing that table into prose.

Additional standard-library candidates remain open until their contracts are audited. This includes apparently simple
value types when their behavior can depend on locale, zone rules, providers, or another platform service.

MIR, LIR, slabbing, object elimination, reprojection, and backend specialization remain Design work.

The exact verifier used to prove preservation across optimization remains open.

Adapter generation and framework-specific integration remain outside this ADR.

---

# 15. Non-Normative Research Notes

The decision follows Kontrakt project law first. External systems are used as evidence for boundary quality and failure
modes rather than as Contract authority.

Java SE separates the portable `java.*` platform APIs from JDK-specific `jdk.*` APIs, but that split is still too broad
for Kontrakt. `java.base` contains both fundamental value classes and runtime mechanisms such as reflection and foreign
access. The platform module boundary is therefore useful provenance, not a sufficient admission law.

Kotlin shows the same problem from another direction. Its default imports include ordinary collections together with I/O
and lazy sequence facilities. Standard-library or default-import status cannot define the native Contract surface.
Kotlin also marks experimental standard-library APIs as outside the usual compatibility guarantee, which supports a
stable-profile default rather than automatic admission.

Rust's `core` library is useful as a boundary precedent. It deliberately excludes heap allocation, concurrency, and I/O
because those require platform integration. Kontrakt has different goals, but the same separation supports
distinguishing
plain value semantics from capabilities.

The WebAssembly Component Model makes a similar distinction between abstract value types and resource handles. It also
keeps canonical lifting and lowering separate from the higher-level value meaning. That is evidence for preserving
platform obligations above replaceable physical realization.

`BigInteger` and `BigDecimal` provide direct JVM examples of value contracts that should not be simplified casually.
`BigInteger` defines immutable arbitrary-precision integer semantics. `BigDecimal` preserves scale-sensitive equality in
ways that differ from numeric comparison. Kontrakt must preserve such distinctions until an explicit later Contract law
changes the semantic domain.

The Java time API gives a direct example of why type admission and operation admission must differ. `Instant` is an
immutable value, while acquiring the current instant consults a clock. The value can be Contract material without making
the clock a native hidden input.

Recent work on RichWasm preserves richer type and ownership meaning in an intermediate language before lowering to
normal
WebAssembly. The implementation details differ from Kontrakt, but the architectural lesson is relevant: lower-level
representation does not justify discarding higher-level obligations that are still needed for safe interoperability.

Recent research on C-to-Rust I/O translation reports that superficially corresponding standard-library calls can encode
different resource origins and capabilities. That result supports keeping I/O and similar platform mechanisms behind an
explicit Adapter rather than treating standard-library membership as semantic equivalence.

Recent cross-compilation studies on WebAssembly report semantic differences caused by standard-library implementation,
unsupported system APIs, and compiler behavior. This is further evidence that platform compatibility requires explicit
contract preservation rather than class-name translation.

Recent Java reproducible-build research identifies environment-dependent and representation-dependent behavior as
important causes of unreproducible artifacts. Kontrakt therefore treats hidden platform input as a determinism problem,
not only as an API-design problem.

---

# 16. Final Rule

Kontrakt recognizes only an explicitly audited JVM Platform-Native Surface.

It preserves the admitted Java or Kotlin contract exactly where that contract remains observable. It does not grant the
rest of the Java, Kotlin, JDK, framework, or library ecosystem direct access to the governed Contract machine.

```text
supported platform value
    ↓
complete platform-aware resolution
    ↓
Resolved Contract HIR
    ↓
Contract-owned Establishment
    ↓
replaceable deterministic realization

platform capability or external technology
    ↓
Adapter / explicit external boundary
```

Representation may change.

Authority and required meaning may not disappear with it.