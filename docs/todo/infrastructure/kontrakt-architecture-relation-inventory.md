# Kontrakt Compiler Architecture — Relation Inventory TODO

```text
[Compiler Driver / Session]
  -.-> [IDL and 1D authoring]
  -.-> [Realization Acquisition and Admission]
  -.-> [Queries / Product Orchestration / Reuse]

01  CONTRACT FRONTEND AND HIR
  [IDL and 1D authoring] --> [Resolved Contract HIR]

02–03  CONTRACT AUTHORITIES
  [Resolved Contract HIR] --> [Definition Establishment]
                          --> [IDL Binding Establishment]
  [Version / Policy / Whole-Machine law] -.-> [Definition Establishment]
                                         -.-> [IDL Binding Establishment]
  [Definition Establishment] --> [Canonical Contract World]
  [IDL Binding Establishment] --> [Canonical Contract World]
  [Definition Establishment] -.-> [Occurrence Establishment]

04  SEMANTIC OBSERVATIONS
  [Canonical Contract World] --> [Producer-owned Protocol Views]
  [Occurrence Establishment] --> [Occurrence Views]
                             --> [Producer-owned Protocol Views]

05  GENERATED API AND REALIZATION FRONTEND
  [Canonical Contract World]
    --> [Generated Interaction / Operation APIs]
    --> [Host Compilation and Composition]
    --> [Realization Acquisition and Admission]
    --> [Realization Body IR]
  [User Operation Implementation] --> [Host Compilation and Composition]

06–07  MIDDLE-END
  [Realization Body IR] --> [Structural and Contract-aware Analysis]
  [Canonical Contract World] --> [Structural and Contract-aware Analysis]
  [Structural and Contract-aware Analysis]
    --> [Closure Verification and Overlay] -----------+
    --> [Whole-Machine / Specialization Knowledge] --+--> [Execution Formation]
  [Execution Formation]
    --> [Contract-aware Execution IR]
    --> [Optimization and IR Validation]

08  JVM BACKEND
  [Optimization and IR Validation]
    --> [JVM Plan and Legalization]
    --> [Direct Classfile Product]
    --> [JVM Handoff]

09–10  SUPPORTING SUBSYSTEMS AND CROSS-REGION OBSERVATIONS
  [Producer-owned Protocol Views] -.-> [ADR-0075 Mediation]
  [ADR-0075 Mediation] -.-> [CRI optional relation calculation]
  [CRI optional relation calculation] -.-> [ADR-0075 Mediation]
  [ADR-0075 Mediation] -.-> [Structural and Contract-aware Analysis]
  [ADR-0075 Mediation] -.-> [Occurrence Establishment]
  [Canonical Contract World] -.-> [Reference / PBT / Independent QA]
  [Optimization and IR Validation] -.-> [Reference / PBT / Independent QA]
  [Direct Classfile Product] -.-> [Diagnostics / Provenance / Security]
  [Closure Verification and Overlay] -.-> [Diagnostics / Provenance / Security]
```

> Logical architecture only. Arrows show material or observation relationships, not a required physical pass schedule.
> Dotted connections do not transfer Contract authority.

**Status:** Working inventory. Not an Accepted ADR. **Scope:** B+ architecture coverage; CRI ownership is reviewed
separately.

## Inventory index

| Area      | Architecture region                                                                                | Original relations | Added coverage candidates |   Total |
|-----------|----------------------------------------------------------------------------------------------------|-------------------:|--------------------------:|--------:|
| 01        | [Contract Frontend and Resolved HIR](#01-contract-frontend-and-resolved-hir)                       |                 12 |                         2 |      14 |
| 02        | [Contract Establishment and 1D Semantics](#02-contract-establishment-and-1d-semantics)             |                 38 |                         0 |      38 |
| 03        | [Policy, Version, and Whole-Machine Contracts](#03-policy-version-and-whole-machine-contracts)     |                 14 |                         0 |      14 |
| 04        | [Canonical World and Semantic Consumption](#04-canonical-world-and-semantic-consumption)           |                 13 |                         1 |      14 |
| 05        | [Generated Interfaces and Realization Frontend](#05-generated-interfaces-and-realization-frontend) |                 14 |                         1 |      15 |
| 06        | [Middle-end Analysis and Verification](#06-middle-end-analysis-and-verification)                   |                 12 |                         2 |      14 |
| 07        | [Execution Formation and Optimization](#07-execution-formation-and-optimization)                   |                  6 |                         4 |      10 |
| 08        | [JVM Backend and Runtime Handoff](#08-jvm-backend-and-runtime-handoff)                             |                  3 |                         2 |       5 |
| 09        | [Driver, Product Orchestration, and Reuse](#09-driver-product-orchestration-and-reuse)             |                 13 |                         1 |      14 |
| 10        | [Diagnostics, Security, and Independent QA](#10-diagnostics-security-and-independent-qa)           |                 15 |                         5 |      20 |
| **Total** |                                                                                                    |            **140** |                    **18** | **158** |

**Review labels:** `Retain owner` = original owner remains responsible; `Shared-work review` = test for independent
common relation calculation; `B+ extension review` = examine a contract-architecture boundary; `CRI protocol boundary` =
consumption or derived-product interface; `Deferred` = unsettled owning law. New `N` entries identify gaps in the
previous inventory, not approved new Contract Relations.

**Basis labels:** `Accepted basis` identifies an established source decision, `Design basis` a working architecture
requirement, `Proposed basis` an unaccepted proposal, and `Open decision` an unresolved boundary. These labels do not
approve CRI ownership.

## Region inventories

### 01. Contract Frontend and Resolved HIR

```text
[IDL / Contract carriers]
  --> [Source and carrier acquisition]
  --> [Resolution of slots and nominal references]
  --> [Resolved Contract HIR]
  --> [Seal / visible HIR projection]
```

#### Authoring and source acquisition

| ID  | Relation                                           | Purpose                                                                         | Current owner                  | CRI review   | Basis          | Open check                                                                    |
|-----|----------------------------------------------------|---------------------------------------------------------------------------------|--------------------------------|--------------|----------------|-------------------------------------------------------------------------------|
| A01 | IDL Interface → Declared Interaction               | An Interaction belongs to the Interface that declares it.                       | IDL/Interface Frontend         | Retain owner | Accepted basis | Check exact ownership and conflicting declarations across Interfaces.         |
| A02 | IDL Interface → Declared Operation                 | An Operation has a nominal coordinate within its declaring Interface.           | IDL/Interface Frontend         | Retain owner | Accepted basis | Keep OperationRef distinct from its generated host representation.            |
| A05 | 1D Carrier Declaration → Definition Candidate      | A carrier is authoring input, not an authoritative Definition.                  | Contract Frontend·HIR Producer | Retain owner | Accepted basis | Check identity when the same carrier is reused in different IDLs.             |
| A09 | Source Declaration → Resolved HIR Subject          | Frontend resolution turns source declarations into HIR candidates.              | Frontend·HIR Producer          | Retain owner | Accepted basis | Keep the author's source representation separate from semantic identity.      |
| N01 | Contract authoring input → Source/Carrier material | Authoring sources are acquired as frontend material before semantic resolution. | Contract Frontend              | Retain owner | Design basis   | Confirm that parser and carrier acquisition do not create Contract authority. |

#### Declared coordinates and nominal references

| ID  | Relation                                          | Purpose                                                                           | Current owner             | CRI review   | Basis          | Open check                                                                |
|-----|---------------------------------------------------|-----------------------------------------------------------------------------------|---------------------------|--------------|----------------|---------------------------------------------------------------------------|
| A06 | Manifest Operation Handle → Resolved OperationRef | The frontend resolves an IDL operation handle to its exact OperationRef.          | IDL Frontend·HIR Producer | Retain owner | Accepted basis | Do not substitute a generated method handle for the nominal coordinate.   |
| A07 | Operation → Parameter/Result Coordinate           | An Operation defines the exact parameter and result coordinates used by lowering. | IDL/HIR Producer          | Retain owner | Accepted basis | Distinguish a ParameterRef from a ResultRef and its Fact-kind obligation. |
| A11 | Definition Candidate → Owner/Authority Coordinate | A Definition Candidate retains the coordinate of its owning Authority.            | HIR Resolution·Owning Law | Retain owner | Accepted basis | Do not derive authority from a host-language package or class shape.      |

#### IDL selection and HIR candidates

| ID  | Relation                                  | Purpose                                                                               | Current owner                           | CRI review   | Basis          | Open check                                                                         |
|-----|-------------------------------------------|---------------------------------------------------------------------------------------|-----------------------------------------|--------------|----------------|------------------------------------------------------------------------------------|
| A03 | Interaction → Selected Contract Slot      | An Interaction selects Contract roles through declared slots.                         | IDL Slot Resolution·Owning Binding Law  | Retain owner | Accepted basis | Check the permitted positions and selection scope for each slot kind.              |
| A04 | Slot → 1D Definition Candidate            | A slot selects a Definition Candidate without establishing that Definition's meaning. | Frontend HIR Binding Candidate Producer | Retain owner | Accepted basis | Preserve both the nominal slot coordinate and the referenced Definition Candidate. |
| A08 | IDL Use → Referenced Definition Candidate | An IDL reference must resolve to the intended Definition Candidate.                   | Frontend Resolution                     | Retain owner | Accepted basis | Do not expose an unresolved or erroneous reference as valid visible HIR.           |

#### HIR provenance, sealing, and visibility

| ID  | Relation                                     | Purpose                                                                             | Current owner                    | CRI review   | Basis          | Open check                                                                              |
|-----|----------------------------------------------|-------------------------------------------------------------------------------------|----------------------------------|--------------|----------------|-----------------------------------------------------------------------------------------|
| A10 | HIR Candidate → Origin/Provenance            | HIR retains provenance without making it part of semantic identity.                 | HIR Producer·Provenance Producer | Retain owner | Accepted basis | Check that changes in source location do not alter the meaning of an unchanged subject. |
| A12 | HIR Generation → Visible Consumer Projection | A consumer observes only a completed and visible HIR generation.                    | HIR Producer Protocol            | Retain owner | Accepted basis | Check visibility, exact references, and current eligibility separately.                 |
| N02 | Resolved HIR → Seal verification/visibility  | A HIR generation is visible only after its resolution invariants have been checked. | HIR Producer / Seal Verification | Retain owner | Accepted basis | Check the producer seal separately from independent conformance.                        |

### 02. Contract Establishment and 1D Semantics

```text
[Resolved HIR]
  --> [Definition establishment] -----------+
  --> [IDL slot binding establishment] -----+--> [Required Basis law]
                                                     |
                                                     v
                                  [Owning applicability / completeness judgment]
                                                     |
                                                     v
                                        [Established occurrence]
                                                     |
                                                     v
                                        [Failure / Publication / Output law]
```

#### Definition establishment and occurrence identity

| ID  | Relation                                             | Purpose                                                                                 | Current owner                             | CRI review         | Basis          | Open check                                                                            |
|-----|------------------------------------------------------|-----------------------------------------------------------------------------------------|-------------------------------------------|--------------------|----------------|---------------------------------------------------------------------------------------|
| A13 | IDL Candidate → Established Slot Binding             | An IDL binding candidate becomes authoritative only through its owning judgment.        | Owning Binding Law·Establishment          | Retain owner       | Accepted basis | Do not replace the Binding Law with a CRI lookup.                                     |
| B01 | Resolved Definition Candidate → Owning Establishment | An owning judgment establishes a Definition from its resolved candidate.                | Owning Contract Authority                 | Retain owner       | Accepted basis | Do not treat successful CRI computation as successful Establishment.                  |
| B02 | Version Binding → Owning Definition Reference        | A Version Binding identifies its exact authoritative Definition.                        | Version Law·Owning Establishment          | Retain owner       | Accepted basis | Audit version-claim resolution separately under D01.                                  |
| B03 | Owning Authority → Established Definition Identity   | The owning law determines established Definition identity.                              | Owning Contract Law                       | Retain owner       | Accepted basis | Keep semantic identity independent of fingerprints, storage locations, and use sites. |
| B04 | Established Definition → Exact Definition Reference  | An exact Definition Reference continues to designate the same established meaning.      | Owning Establishment·Established Protocol | Retain owner       | Accepted basis | Check its meaning across World views and storage changes.                             |
| B05 | Definition → Requirement Law                         | A Definition preserves the law governing requirements of later applications.            | Owning law of each 1D Contract            | Retain owner       | Accepted basis | Do not establish the basis of a future Occurrence at Definition time.                 |
| B12 | Definition + Required Basis → Occurrence Application | A Definition and a particular application are different legal subjects.                 | Owning Occurrence Law                     | Retain owner       | Accepted basis | Keep reusable Definition analysis separate from an actual Occurrence application.     |
| B13 | Occurrence → Identity/Attribution                    | Each Occurrence preserves its exact attribution even when Definitions are shared.       | Owning Contract Law                       | Retain owner       | Accepted basis | Do not use value equality or the current thread as Occurrence identity.               |
| B14 | Judgment → Established Occurrence Result             | Only the owning Authority establishes the result of an Occurrence judgment.             | Owning Contract Authority                 | Retain owner       | Accepted basis | Do not automatically place Occurrence Material in the Canonical Definition World.     |
| B19 | Definition/Occurrence → Direct Established Relation  | Consumers may observe a direct relation only where the owning Authority established it. | Established Semantic Protocol             | Retain owner       | Accepted basis | Do not invent a universal membership relation for materials visible in one World.     |
| B20 | Prior Established Material → Later Legal Basis       | Previously established material may form the legal basis of a later application.        | Owning Basis Law                          | Shared-work review | Accepted basis | Distinguish lawful later reuse from a prohibited semantic cycle.                      |

#### Required Basis and source-domain relations

| ID  | Relation                                                   | Purpose                                                                                         | Current owner                                                              | CRI review         | Basis          | Open check                                                                              |
|-----|------------------------------------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|--------------------|----------------|-----------------------------------------------------------------------------------------|
| B06 | Required Basis → Source Domain                             | The owning Basis Law defines which established sources may be candidates.                       | Owning Basis Law                                                           | Shared-work review | Accepted basis | Determine whether CRI can inspect that domain without inventing its rules.              |
| B07 | Required Basis → Exact Source Candidate                    | A source candidate must be attributed to the exact Required Basis it may satisfy.               | Relation resolution candidate; legal meaning remains with the Owning Law   | Shared-work review | Open decision  | Isolate any reusable compiler calculation from the owning legal judgment.               |
| B08 | Required Basis → Basis Binding                             | The owning judgment establishes an actual Basis Binding.                                        | Owning Basis/Composition Law                                               | Retain owner       | Accepted basis | Keep candidate preparation separate from the authoritative binding decision.            |
| B09 | Bound Source + Applicable Context → Applicability Judgment | Binding a source does not itself establish applicability in the current context.                | Owning Applicability Law                                                   | Retain owner       | Accepted basis | Do not let CRI infer Applicability from a successful source match.                      |
| B10 | Candidate Domain → Domain Completeness Evidence            | A completeness claim must account for the relevant candidate domain.                            | CRI computation candidate; domain guarantees depend on the source Producer | Shared-work review | Open decision  | Determine how to verify scope, absent sources, and newly introduced candidates.         |
| B11 | Applicable Basis → Complete Basis                          | The owning Composition Law decides whether applicable basis satisfies the complete requirement. | Owning Contract/Composition Authority                                      | Retain owner       | Accepted basis | Do not let CRI produce an authoritative Complete Basis judgment.                        |
| B15 | Dependency → Semantic Prerequisite Order                   | Semantic prerequisites constrain the order in which contract meaning can be established.        | Owning Contract/Composition Law                                            | Retain owner       | Accepted basis | Keep semantic dependencies distinct from compiler query dependencies.                   |
| B17 | One Source → Multiple Required Basis Uses                  | One established source may support several independent judgments.                               | Each independent Owning Judgment                                           | Retain owner       | Accepted basis | Do not merge their authority or occurrence identity merely because they share a source. |
| B18 | Unresolved Relation → Judgment Non-entry                   | A judgment cannot establish a result when its required legal basis has not been resolved.       | Owning Establishment·Compiler Result Boundary                              | Retain owner       | Accepted basis | Keep compiler non-completion separate from an authoritative refusal.                    |

#### Input, Admission, Canonicalization, and Lowering

| ID  | Relation                                               | Purpose                                                                                | Current owner                            | CRI review         | Basis          | Open check                                                                         |
|-----|--------------------------------------------------------|----------------------------------------------------------------------------------------|------------------------------------------|--------------------|----------------|------------------------------------------------------------------------------------|
| C01 | External Presentation → Input Subject                  | Input concerns the external boundary presentation rather than an Operation parameter.  | Input Law·Boundary Adapter               | Retain owner       | Accepted basis | Do not confuse a raw host object with an established input meaning.                |
| C02 | Final Boundary Presentation → Admission Required Basis | Admission requires the final lawful boundary presentation as its basis.                | Admission Law·Inbound Composition        | Shared-work review | Open decision  | Reconcile the older ADR-0065 input-basis language with the newer inbound decision. |
| C03 | Input Presented → Selected Canonicalization            | When selected, Canonicalization precedes Admission in the inbound composition.         | Canonicalization Law·Inbound Composition | Shared-work review | Open decision  | Track the unamended portions of ADR-0065 against ADR-0066 §4.5.                    |
| C04 | Canonicalization Law → Stable Representative           | The owning Canonicalization Law determines stable representatives and canonical bytes. | Canonicalization Law                     | Retain owner       | Accepted basis | Do not let compiler reference normalization replace that authority.                |
| C05 | Lowering Source → Operation Parameter Coordinate       | Lowering connects a lawful source to the exact Operation parameter Fact coordinate.    | Lowering Law                             | Shared-work review | Accepted basis | Preserve the V1 one-to-one cardinality specified by ADR-0067.                      |
| C06 | Operation Result → Fact Kind                           | The declared Operation result is associated with the Fact Kind that receives judgment. | Operation/Fact/Lowering Law              | Shared-work review | Open decision  | Determine which owning law establishes the result binding.                         |
| C20 | Contract Law → Canonicalization Built-in Profile       | A built-in canonicalization declaration selects an exact lawful profile.               | Canonicalization Catalog Law             | Retain owner       | Accepted basis | Do not confuse a catalog selection with established canonical bytes.               |

#### Fact and Invariant

| ID  | Relation                                       | Purpose                                                                  | Current owner | CRI review         | Basis          | Open check                                                                     |
|-----|------------------------------------------------|--------------------------------------------------------------------------|---------------|--------------------|----------------|--------------------------------------------------------------------------------|
| C07 | Immutable Fact → Fact Identity/Sameness        | The Fact Law determines identity and sameness for immutable Facts.       | Fact Law      | Retain owner       | Accepted basis | Do not equate structural equality with Fact identity.                          |
| C08 | Fact → Invariant Requirement                   | An Invariant is applied to an exact Fact subject.                        | Invariant Law | Shared-work review | Accepted basis | Check the source Fact and the coordinate of each invariant application.        |
| C09 | Invariant Judgment → Standing Integrity Result | Invariant judgments establish their own results under the Invariant Law. | Invariant Law | Retain owner       | Accepted basis | Do not reinterpret a compiler verification result as an established Invariant. |

#### State, Budget, and Capacity

| ID  | Relation                                        | Purpose                                                                  | Current owner        | CRI review   | Basis          | Open check                                                                       |
|-----|-------------------------------------------------|--------------------------------------------------------------------------|----------------------|--------------|----------------|----------------------------------------------------------------------------------|
| C10 | State Machine Manifest → State/Transition Roles | The State Machine manifest declares the roles of states and transitions. | State-Machine Law    | Retain owner | Accepted basis | Distinguish a declared transition reference from a discovered runtime candidate. |
| C11 | Current State → Transition Prerequisite         | A transition judgment requires the applicable current established state. | State/Transition Law | Retain owner | Open decision  | Separate compile-time reference preparation from runtime-state judgment.         |
| C12 | Transition Judgment → Next Established State    | Only an authoritative transition judgment can establish the next state.  | State/Transition Law | Retain owner | Accepted basis | CRI must neither update state nor invent a transition.                           |
| C13 | Budget Definition → Subject Allowance           | Budget allowances belong to a declared subject and scope.                | Budget Law           | Retain owner | Accepted basis | Keep contractual allowances separate from compiler planning budgets.             |
| C14 | Capacity Definition → Occupancy/Count Subject   | Capacity constraints apply to the declared occupancy or count subject.   | Capacity Law         | Retain owner | Accepted basis | Keep runtime resource accounting outside CRI.                                    |

#### Failure, Publication, and Output

| ID  | Relation                               | Purpose                                                                 | Current owner           | CRI review   | Basis          | Open check                                                           |
|-----|----------------------------------------|-------------------------------------------------------------------------|-------------------------|--------------|----------------|----------------------------------------------------------------------|
| C15 | Judgment Failure → Failure Attribution | A Failure Law attributes an authoritative refusal to its proper origin. | Failure Law             | Retain owner | Accepted basis | Keep such failures distinct from unsuccessful compiler results.      |
| C16 | Core Exit → Publication Judgment       | Publication authorizes the external meaning released at Core exit.      | Publication Law         | Retain owner | Accepted basis | Do not confuse compiler-result visibility with Contract Publication. |
| C17 | Publication → Output Presentation      | Output presents the meaning authorized by Publication.                  | Output Presentation Law | Retain owner | Accepted basis | Output must not bypass Publication to expose a source directly.      |

### 03. Policy, Version, and Whole-Machine Contracts

```text
[Version / Policy] --> [Authorized binding context]
[Governance candidate] -.-> [Authorized binding context]
[Authorized binding context] --> [Whole-Machine composition]
[Core requirements] ----------> [Whole-Machine composition]
[Whole-Machine composition] --> [Explicit cross-scope source relation]
```

#### Version and policy worlds

| ID  | Relation                                         | Purpose                                                                                     | Current owner            | CRI review   | Basis          | Open check                                                             |
|-----|--------------------------------------------------|---------------------------------------------------------------------------------------------|--------------------------|--------------|----------------|------------------------------------------------------------------------|
| D01 | Contract Id + Version Claim → Definition Meaning | Version Binding maps the Contract ID and Version Claim to authoritative Definition meaning. | Version Law              | Retain owner | Accepted basis | Do not use a fingerprint or compiler generation as Version Identity.   |
| D02 | Established Definition → Durable History Record  | Recording durable history is distinct from establishing current meaning.                    | Version/History Producer | Retain owner | Accepted basis | Audit the relationship between a failed build and history publication. |
| D03 | Interface → Named Policy World                   | An Interface declares the named Policy Worlds available to it.                              | Policy Law               | Retain owner | Accepted basis | Check that the compiler does not choose an undeclared default World.   |
| D04 | Policy World → Contract Binding                  | A Contract Binding is interpreted within the selected Policy World.                         | Policy/Binding Law       | Retain owner | Accepted basis | Check whether reuse across Worlds preserves the exact binding.         |
| D05 | Policy World → Allowed Contract Combination      | The Policy Law decides whether a combination is allowed in its World.                       | Policy Law               | Retain owner | Accepted basis | CRI must not invent combination validity.                              |

#### Governance candidates

| ID  | Relation                                  | Purpose                                                                       | Current owner             | CRI review | Basis          | Open check                                                                       |
|-----|-------------------------------------------|-------------------------------------------------------------------------------|---------------------------|------------|----------------|----------------------------------------------------------------------------------|
| D06 | Governance Selection → Exact Scope        | Governance selects the exact scope in which its authority applies.            | Governance Law (Proposed) | Deferred   | Proposed basis | Recheck these candidate relations when Governance is closed.                     |
| D07 | Governance → Binding/Validity/Singularity | Governance constrains authoritative binding and singularity within its scope. | Governance Law (Proposed) | Deferred   | Proposed basis | Review whether any CRI responsibility duplicates the proposed Governance duties. |

#### Whole-Machine and cross-scope composition

| ID  | Relation                                                  | Purpose                                                                                        | Current owner                     | CRI review         | Basis          | Open check                                                                          |
|-----|-----------------------------------------------------------|------------------------------------------------------------------------------------------------|-----------------------------------|--------------------|----------------|-------------------------------------------------------------------------------------|
| D08 | Higher Scope → Explicit Lower-Scope Basis                 | A higher-scope application can use lower-scope basis only through an explicit lawful relation. | Owning Higher-scope Law           | Shared-work review | Open decision  | Verify source scope and dependent scope without creating a hidden cross-scope edge. |
| D09 | Whole-Machine → Core Membership/Selection                 | Whole-Machine composition declares which Cores belong to the machine.                          | Whole-Machine/IDL Composition Law | Retain owner       | Accepted basis | Do not infer membership merely from co-location in compiler storage.                |
| D10 | Core → Declared Required Input                            | Each Core declares the inputs required by Whole-Machine composition.                           | Whole-Machine Composition Law     | Shared-work review | Accepted basis | Keep declared source relations separate from implicit pipeline access.              |
| D11 | Cross-Core Source → Dependent Requirement                 | A dependent Core may use an explicit lawful source from another Core.                          | Whole-Machine Owning Law          | Shared-work review | Accepted basis | Do not infer legal basis from globally shared objects.                              |
| D12 | Core A Completion → Core B Prerequisite                   | Only a contractually declared prerequisite creates semantic ordering between Cores.            | Whole-Machine Owning Law          | Shared-work review | Open decision  | Do not convert scheduler waiting into contractual dependency.                       |
| D13 | Whole-Machine Outputs → Publication/External Exit         | Whole-Machine results reach the external boundary through the lawful exit and Publication.     | Whole-Machine·Publication Law     | Retain owner       | Accepted basis | Do not introduce undeclared aggregation or merging rules.                           |
| D14 | Cross-scope Established Materials → Higher-scope Judgment | Established material from another scope can support a lawful higher-scope judgment.            | Owning Higher-scope Authority     | Shared-work review | Accepted basis | The source's Authority must not transfer to the later judgment.                     |

### 04. Canonical World and Semantic Consumption

```text
[Established definitions / occurrences]
  --> [Canonical world / occurrence views]
  --> [Producer-owned protocol]
  --> [ADR-0075 mediation]
  --> [Consumer observation]

[ADR-0075 mediation] -.-> [CRI relation calculation]
[CRI relation calculation] -.-> [CRI-owned result when needed]
```

#### Established world and occurrence views

| ID  | Relation                                                     | Purpose                                                                                            | Current owner                     | CRI review   | Basis          | Open check                                                                  |
|-----|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------|-----------------------------------|--------------|----------------|-----------------------------------------------------------------------------|
| E01 | Established Definition → Canonical Contract World Visibility | The Canonical Contract World exposes established Definition meaning without establishing it again. | Canonical Contract World Producer | Retain owner | Accepted basis | Co-location in a World must not create a new legal relation.                |
| E02 | Established Occurrence → Occurrence Material Access          | Occurrence Material remains distinguishable from the Definition World.                             | Owning Occurrence Protocol        | Retain owner | Accepted basis | Do not turn the Canonical Contract World into a universal Occurrence store. |
| E07 | Canonical World View → Definition Reference Resolution       | A World view permits exact references to independently established Definitions.                    | Canonical World Producer          | Retain owner | Accepted basis | Do not infer a Binding between Definitions because both are visible.        |
| N03 | Established Definition → Frozen world projection             | Immutable world projections preserve established meaning without granting new authority.           | Canonical Contract World Producer | Retain owner | Design basis   | Differentiate logical visibility from physical freezing or storage.         |
| H13 | Partial World Visibility → Consumer Closure Requirement      | A consumer may use a complete relevant part of the World without materializing all of it.          | World Producer·Legal Consumer     | Retain owner | Accepted basis | Unrelated missing Definitions must not block valid local consumption.       |

#### Producer-owned observation protocol

| ID  | Relation                                            | Purpose                                                                                     | Current owner                           | CRI review   | Basis          | Open check                                                                      |
|-----|-----------------------------------------------------|---------------------------------------------------------------------------------------------|-----------------------------------------|--------------|----------------|---------------------------------------------------------------------------------|
| E03 | Producer Result → Consumer Requirement              | Consumers ask for semantic guarantees, not named Producer implementations.                  | ADR-0075 Consumption Mediation          | Retain owner | Accepted basis | Apply the same mediation rule when CRI consumes another result.                 |
| E04 | Consumer Requirement → Legal Projection             | The Producer Protocol exposes only projections lawful for the request.                      | Producer-owned Protocol·Mediation       | Retain owner | Accepted basis | Do not grant access to the full underlying material by default.                 |
| E05 | Protocol Observation → Current Use Eligibility      | An observation's present eligibility is distinct from the meaning originally established.   | Producer Validity·Mediation             | Retain owner | Accepted basis | Check relevant context and allowed scope before reuse.                          |
| E06 | Semantic Reference → Visible Projection             | Knowing an exact semantic reference does not grant observation authority.                   | Producer Protocol·Security Boundary     | Retain owner | Accepted basis | Do not introduce a universal material resolver.                                 |
| E11 | Compiler Material → Independent Verifier Projection | Independent verification requires lawful access to evidence beyond the result under review. | Verification Consumer·Producer Protocol | Retain owner | Design basis   | Prevent a verifier from trusting only the CRI assertion it is supposed to test. |
| H14 | Reused Material → Exact Context Qualification       | Reused material must satisfy the exact context in which it is observed.                     | Result Producer·Mediation               | Retain owner | Accepted basis | Compiler scheduling or cache state must not change contract results.            |

#### CRI as consumer and conditional producer

| ID  | Relation                                          | Purpose                                                                            | Current owner                                                           | CRI review            | Basis         | Open check                                                                         |
|-----|---------------------------------------------------|------------------------------------------------------------------------------------|-------------------------------------------------------------------------|-----------------------|---------------|------------------------------------------------------------------------------------|
| E08 | Established Producer Projection → CRI Observation | CRI consumes only authorized observations sufficient for its relation calculation. | Producer Protocol·ADR-0075                                              | CRI protocol boundary | Open decision | State required guarantees without binding CRI to a Producer category or name.      |
| E09 | CRI Result → Legal Consumer Observation           | CRI owns the protocol for a genuinely independent compiler result it produces.     | CRI Producer, only if it owns an independent result                     | CRI protocol boundary | Open decision | Determine the result's meaning, sufficiency, equality, and current-use conditions. |
| E10 | CRI Evidence → Owning Judgment Input              | CRI evidence supports, but never replaces, the owning judgment.                    | CRI computation and the Owning Judgment, each within its responsibility | CRI protocol boundary | Open decision | Define what makes the handoff sufficient without enlarging authority.              |

### 05. Generated Interfaces and Realization Frontend

```text
[Canonical Contract World]
  --> [Generated APIs]
  --> [Host compile / composition]
  --> [Realization acquisition]
  --> [Admitted implementation binding]
  --> [Realization Body IR]

[Platform / external boundary] -.-> [Realization acquisition]
```

#### Generated host API products

| ID  | Relation                                           | Purpose                                                                                          | Current owner                  | CRI review   | Basis          | Open check                                                                  |
|-----|----------------------------------------------------|--------------------------------------------------------------------------------------------------|--------------------------------|--------------|----------------|-----------------------------------------------------------------------------|
| A14 | IDL Selection → Generated API Requirement          | Generated host APIs follow resolved contract selections.                                         | Generated API Product Producer | Retain owner | Design basis   | Check that the generated API does not redefine a selected role.             |
| F01 | Contract Operation → Generated Operation Interface | A generated Operation interface projects the nominal Contract Operation for host implementation. | Generated API Producer         | Retain owner | Accepted basis | The generated interface shape must not become an Authority.                 |
| F02 | Contract Interaction → Generated External API      | A generated external API projects the Contract Interaction boundary.                             | Generated API Producer         | Retain owner | Accepted basis | Keep an external Interaction distinct from the internal Operation boundary. |
| F03 | Generated API Product → Host Compilation Input     | Host compilation depends on the generated API product.                                           | Compiler Product Driver        | Retain owner | Design basis   | Do not reinterpret a build dependency as a semantic prerequisite.           |

#### Acquisition and implementation binding

| ID  | Relation                                                   | Purpose                                                                                      | Current owner                              | CRI review          | Basis         | Open check                                                                  |
|-----|------------------------------------------------------------|----------------------------------------------------------------------------------------------|--------------------------------------------|---------------------|---------------|-----------------------------------------------------------------------------|
| F04 | Host Classfile → Realization Body IR                       | Realization Acquisition makes a host classfile available as analyzable Body IR.              | Realization Acquisition Producer           | Retain owner        | Design basis  | Do not infer contract meaning from the classfile's structure.               |
| F05 | OperationRef → Realization Candidate                       | A Realization Candidate must correspond to the exact declared OperationRef.                  | Realization Acquisition/Admission          | B+ extension review | Open decision | Determine whether CRI can reuse only the candidate-resolution calculation.  |
| F06 | External DI/Composition → Effective Implementation Binding | External composition supplies an effective implementation binding at the admission boundary. | External Composition·Realization Admission | Retain owner        | Design basis  | Do not introduce runtime proxies as authority inside the contract pipeline. |
| F07 | Realization Candidate → Admitted Binding                   | Realization Admission judges whether a candidate implementation is permitted.                | Realization Admission/Verification         | Retain owner        | Design basis  | Do not promote candidate lookup into an admissibility judgment.             |

#### Host use and realization analysis

| ID  | Relation                                                     | Purpose                                                                                           | Current owner                              | CRI review          | Basis          | Open check                                                                            |
|-----|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------|--------------------------------------------|---------------------|----------------|---------------------------------------------------------------------------------------|
| F08 | User Call Site → External Technology Effect                  | Call analysis identifies effects that may bypass the governed boundary.                           | Realization Analysis/Verifier              | Retain owner        | Design basis   | Do not transfer the general implementation call graph to CRI.                         |
| F09 | Platform Native Surface → Ratified Use Profile               | Platform Ratification defines the supported profile of native platform use.                       | Platform Ratification Law                  | Retain owner        | Accepted basis | Ratification must not replace Contract Authority.                                     |
| F10 | Platform Descriptor → Semantic Operation Use                 | Platform descriptors constrain the lawful use of host operations.                                 | Platform Ratification/Consumer Protocol    | B+ extension review | Open decision  | Determine whether any common source resolution is needed beyond protocol consumption. |
| F11 | Adapter → Governed Boundary Presentation                     | Adapters present external effects through governed boundary values.                               | Adapter/Boundary Admission                 | Retain owner        | Accepted basis | CRI must not read raw external objects as established Facts.                          |
| F12 | Realization Body IR → Origin/Effect Facts                    | Realization Analysis provides origin and effect information for implementation bodies.            | Realization Analysis Producer              | Retain owner        | Design basis   | Distinguish implementation-origin evidence from contract provenance.                  |
| E12 | External Observation → Legal Contract Ingress                | External observations become legal inputs only through their authorized boundary.                 | Input/Boundary/Platform Ratification       | Retain owner        | Accepted basis | Do not promote raw external facts directly into established meaning.                  |
| N15 | Contract/realization isolation gates → Admitted observations | External technology passes its own boundary checks before a protected compiler stage consumes it. | Frontend Isolation / Realization Isolation | Retain owner        | Design basis   | Keep both gates separate from the eventual Contract judgment.                         |

### 06. Middle-end Analysis and Verification

```text
[Realization Body IR] --> [CFG / SSA / effect analysis]
[CFG / SSA / effect analysis] --> [Contract-aware analysis]
[Established World] ----------> [Contract-aware analysis]

[Contract-aware analysis] --> [Closure / independent verification]
[Contract-aware analysis] --> [Whole-Machine summaries]
[Closure / independent verification] --> [Verification overlay]
```

#### Body IR and local analyses

| ID  | Relation                               | Purpose                                                                    | Current owner            | CRI review   | Basis        | Open check                                                              |
|-----|----------------------------------------|----------------------------------------------------------------------------|--------------------------|--------------|--------------|-------------------------------------------------------------------------|
| G01 | Body Instruction → CFG Successor       | CFG successors describe executable control flow within Body IR.            | Body IR / CFG Producer   | Retain owner | Design basis | Keep this an IR relation rather than a contract source relation.        |
| G02 | SSA Definition → Value Use             | SSA definitions are connected to the uses of their values.                 | IR Producer/Verifier     | Retain owner | Design basis | Do not transfer general def-use tracking to CRI.                        |
| G03 | Call Site → Reachable Target           | Call analysis establishes possible targets of an implementation call site. | Call/Effect Analysis     | Retain owner | Design basis | Distinguish implementation call graphs from Contract Operation binding. |
| G04 | Operation Body → Effect/Origin Summary | Shared Analysis produces an effect or origin summary for verification.     | Shared Analysis Producer | Retain owner | Design basis | Do not treat an incomplete summary as a safety proof.                   |

#### Contract-aware and whole-machine analysis

| ID  | Relation                                                  | Purpose                                                                                        | Current owner                          | CRI review   | Basis        | Open check                                                              |
|-----|-----------------------------------------------------------|------------------------------------------------------------------------------------------------|----------------------------------------|--------------|--------------|-------------------------------------------------------------------------|
| G08 | Established Meaning → Contract-aware Analysis Request     | Contract-aware Analysis consumes lawful projections of established meaning.                    | Analysis Consumer·ADR-0075             | Retain owner | Design basis | Analysis must not reinterpret the Contract World.                       |
| G11 | Whole-Machine Analysis → Core Summary                     | Whole-Machine Analysis summarizes implementation effects across Cores.                         | IPA / Whole-Machine Analysis           | Retain owner | Design basis | Keep these summaries separate from contractual Whole-Machine relations. |
| N04 | Whole-Machine Summary → Selective realization-body demand | Whole-machine analysis can demand only the implementation bodies needed for a lawful analysis. | Whole-Machine Analysis / Body Producer | Retain owner | Design basis | A compiler demand edge must not become a Contract prerequisite.         |

#### Verification requests, results, and assumptions

| ID  | Relation                                                     | Purpose                                                                                               | Current owner                                                         | CRI review            | Basis         | Open check                                                                        |
|-----|--------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|-----------------------|---------------|-----------------------------------------------------------------------------------|
| F13 | Contract World + Realization Binding → Verification Subject  | A verification request joins authoritative contract meaning with its admitted implementation subject. | Verification Request/Mediation                                        | B+ extension review   | Open decision | Decide whether this needs relation resolution or only legal consumption.          |
| G05 | Contract Requirement → Realization Verification Obligation   | A verification obligation states which contract guarantee is being checked against realization.       | Verification Architecture                                             | B+ extension review   | Open decision | Establish whether any CRI relation resolution is actually necessary.              |
| G06 | Verification Result → Verified Realization Overlay           | A verification overlay preserves conclusions separately from Body IR meaning.                         | Verification Producer                                                 | Retain owner          | Design basis  | Define when the overlay is eligible for the exact body and context.               |
| G07 | Verification Result → Proof/Assumption Dependencies          | Verification results depend on explicit assumptions and proof obligations.                            | Verification Producer                                                 | Retain owner          | Design basis  | Reject success when an assumption is no longer valid.                             |
| N05 | Verification summary → Whole-Machine verification obligation | Aggregate verification consumes sound summaries without treating them as contractual judgments.       | Verification Architecture                                             | Retain owner          | Design basis  | Check completeness and assumptions before using summaries.                        |
| I06 | Verification Obligation → Independent Evidence               | Independent assurance checks the claims made by the component under test.                             | Verification Producer/QA                                              | Retain owner          | Design basis  | Do not accept a CRI conclusion as proof of itself.                                |
| I12 | CRI Relation Result → Verification Target                    | CRI's independent results must admit verification of the accuracy they claim.                         | CRI owns local correctness; independent Verification remains external | CRI protocol boundary | Open decision | Define the legitimate source observations and trust assumptions for verification. |

### 07. Execution Formation and Optimization

```text
[Verified realization] ----------> [Execution formation]
[Specialization and legal analyses] --> [Execution formation]
[Execution formation]
  --> [Contract-aware Execution IR]
  --> [Optimization / cleanup]
  --> [Verified IR generation]
```

#### Execution formation and specialization

| ID  | Relation                                                 | Purpose                                                                                        | Current owner                             | CRI review   | Basis        | Open check                                                                     |
|-----|----------------------------------------------------------|------------------------------------------------------------------------------------------------|-------------------------------------------|--------------|--------------|--------------------------------------------------------------------------------|
| F14 | Verified Realization → Executable Plan Basis             | Execution Formation relies on legally verified realization information.                        | Execution Formation Producer              | Retain owner | Design basis | Do not treat a compiler verifier's conclusion as established contract meaning. |
| G10 | Analysis → Specialization Knowledge                      | Specialization derives compiler knowledge under a fixed contract context.                      | Specialization Analysis Producer          | Retain owner | Design basis | Do not convert a specialization into a Contract Law.                           |
| G12 | Execution Formation → Contract-aware Execution IR        | Execution Formation derives executable IR from admitted and verified inputs.                   | Execution IR Producer                     | Retain owner | Design basis | This derived IR must not create new Contract Authority.                        |
| N06 | Specialization knowledge → Formation-time pruning        | Execution formation may eliminate work only when its applicable obligations are discharged.    | Execution Formation / Optimization        | Retain owner | Design basis | Keep static discharge distinct from a runtime refusal.                         |
| N17 | Undischarged Contract obligation → Runtime judgment plan | Execution formation retains the runtime judgment required when static discharge is not lawful. | Execution Formation / Owning Contract Law | Retain owner | Design basis | Do not replace a required runtime judgment with a compiler inference.          |

#### Legality, profitability, and analysis validity

| ID  | Relation                                   | Purpose                                                                                     | Current owner                           | CRI review   | Basis        | Open check                                                       |
|-----|--------------------------------------------|---------------------------------------------------------------------------------------------|-----------------------------------------|--------------|--------------|------------------------------------------------------------------|
| G09 | Analysis Result → Optimization Legality    | Optimization requires currently valid analysis supporting its legality claim.               | Optimization Producer·Analysis Validity | Retain owner | Design basis | Do not reuse obsolete analysis solely because it remains cached. |
| G14 | IR Transform → Preservation Claim          | A pass declares which analysis results remain valid after transformation.                   | Pass/Analysis Validity Architecture     | Retain owner | Design basis | Do not accept stale analysis under a false preservation claim.   |
| N07 | Legality evidence → Profitability decision | Optimization profitability is considered only after transformation legality is established. | Optimization Planning                   | Retain owner | Design basis | A cost estimate must never establish semantic legality.          |

#### IR generations and stage invariants

| ID  | Relation                                                   | Purpose                                                                    | Current owner                | CRI review   | Basis        | Open check                                                                      |
|-----|------------------------------------------------------------|----------------------------------------------------------------------------|------------------------------|--------------|--------------|---------------------------------------------------------------------------------|
| G13 | IR Generation N → Transformed Generation N+1               | A transform must preserve the required meaning between IR generations.     | Transform Producer           | Retain owner | Design basis | Check the legal comparison and refinement obligation for both generations.      |
| N11 | IR formation/transformation → Stage-invariant verification | Every published IR generation must satisfy the invariant of its own stage. | IR Producer / Stage Verifier | Retain owner | Design basis | Keep producer well-formedness separate from independent translation validation. |

### 08. JVM Backend and Runtime Handoff

```text
[Optimized execution IR] --> [JVM legalization]
[Target capability] -------> [JVM legalization]
[JVM legalization]
  --> [JVM plan / IR]
  --> [Direct classfile product]
  --> [JVM verification / handoff]
```

#### JVM lowering and target legality

| ID  | Relation                                         | Purpose                                                           | Current owner                | CRI review   | Basis        | Open check                                                  |
|-----|--------------------------------------------------|-------------------------------------------------------------------|------------------------------|--------------|--------------|-------------------------------------------------------------|
| G15 | High-level Execution IR → JVM Plan/IR            | JVM Lowering translates executable IR to a JVM-oriented plan.     | JVM Lowering Producer        | Retain owner | Design basis | Do not confuse backend lowering with the Lowering Contract. |
| N08 | JVM target capability → Legalization requirement | Backend legalization must honor the available JVM target profile. | JVM Backend / Target Profile | Retain owner | Design basis | Do not make host-platform choices into Contract Law.        |

#### Classfile emission and executable boundary

| ID  | Relation                                             | Purpose                                                                     | Current owner                      | CRI review   | Basis        | Open check                                                             |
|-----|------------------------------------------------------|-----------------------------------------------------------------------------|------------------------------------|--------------|--------------|------------------------------------------------------------------------|
| G16 | JVM Plan → Classfile Product                         | Classfile emission must implement the approved JVM plan.                    | JVM Backend/Emitter                | Retain owner | Design basis | Do not treat emitted classfiles as sources of contract meaning.        |
| G17 | Classfile Product → JVM Verification/Handoff         | A classfile must satisfy the JVM's execution acceptance requirements.       | Backend Validation·JVM             | Retain owner | Design basis | JVM bytecode verification does not replace Contract Verification.      |
| N12 | Executable classfile → Declared Interaction behavior | The final JVM product implements the declared external Interaction surface. | Backend / External API Realization | Retain owner | Design basis | Generated interface shape and JVM output cannot redefine the Contract. |

### 09. Driver, Product Orchestration, and Reuse

```text
[Driver / product demand] --> [Product orchestration]
[Product orchestration] --> [Producer-owned compiler results]
[Producer-owned compiler results] --> [Reuse / validity / equality]
[Reuse / validity / equality] --> [Product orchestration]
[Producer-owned compiler results] --> [Compiler product dependencies]
[Driver / product demand] --> [Cancellation and resource ownership]
```

#### Driver and product graph

| ID  | Relation                                               | Purpose                                                                       | Current owner                         | CRI review   | Basis        | Open check                                                       |
|-----|--------------------------------------------------------|-------------------------------------------------------------------------------|---------------------------------------|--------------|--------------|------------------------------------------------------------------|
| H01 | Consumer Demand → Required Compiler Product            | Product Orchestration obtains compiler products required by a legal demand.   | Compiler Driver/Product Orchestration | Retain owner | Design basis | Do not turn CRI into the general product scheduler.              |
| H02 | Product A → Product B Dependency                       | Compiler products may depend on other compiler products.                      | Query/Product Engine                  | Retain owner | Design basis | Do not interpret a product dependency as Required Basis.         |
| H12 | Generated API → User Code → Classfile Build Dependency | Generated APIs, user code, and classfiles have compiler product dependencies. | Compiler Driver/Build Product Owner   | Retain owner | Design basis | Do not feed those dependencies into the contract semantic graph. |
| N13 | Session demand → Work ownership/cancellation           | The driver owns the lifetime and cancellation of demanded compiler work.      | Compiler Driver / Session             | Retain owner | Design basis | Cancelled work cannot publish a completed product.               |

#### Computation dependence and semantic dependence

| ID  | Relation                                                     | Purpose                                                                | Current owner                         | CRI review   | Basis          | Open check                                                                                   |
|-----|--------------------------------------------------------------|------------------------------------------------------------------------|---------------------------------------|--------------|----------------|----------------------------------------------------------------------------------------------|
| B16 | Contract Semantic Relation → Compiler Dependency Observation | Compiler work may depend on an already established semantic relation.  | Compiler Result Producer·Reuse Engine | Retain owner | Accepted basis | Confirm that the derived compiler dependency does not feed authority back into the contract. |
| H03 | Computation → Observed Input Determinants                    | Reuse records the input observations that determine a computed result. | Query/Reuse Engine·Result Producer    | Retain owner | Design basis   | Distinguish meaningful dependencies from arbitrary memory reads.                             |

#### Equality, validity, and reuse

| ID  | Relation                                           | Purpose                                                                                       | Current owner                            | CRI review         | Basis          | Open check                                                                    |
|-----|----------------------------------------------------|-----------------------------------------------------------------------------------------------|------------------------------------------|--------------------|----------------|-------------------------------------------------------------------------------|
| H04 | Result Generation → Semantic Equality              | The Producer defines whether two versions of its result are equal.                            | Each Result Producer                     | Retain owner       | Accepted basis | A generic comparator must not decide semantic identity.                       |
| H05 | Cached Result → Current Use Eligibility            | A cache hit does not itself establish current eligibility.                                    | Result Producer·ADR-0075 Mediation       | Retain owner       | Accepted basis | Check scope and context before making a cached result available.              |
| H06 | Result → Invalidation/Preservation                 | A Producer's validity conditions determine when its result must be reconsidered.              | Producer Validity·Reuse Engine           | Retain owner       | Design basis   | Do not prescribe a single global invalidation unit.                           |
| H07 | Negative Domain Observation → Change Validity      | A negative or unique-domain result may become invalid when a new candidate appears.           | Domain Producer/CRI Result Validity      | Shared-work review | Open decision  | Include phantom candidates and omitted sources in validity review.            |
| H08 | Result → Previous/Current Generation Compatibility | Results from different generations may be combined only when their observations are coherent. | Producer/Mediation·Compiler Driver       | Retain owner       | Accepted basis | Do not require identical physical generation numbers without semantic reason. |
| H11 | Result → Persistence Identity/Compatibility        | Persisted compiler results must remain compatible with current guarantees.                    | Producer·Persistent Reuse Infrastructure | Retain owner       | Design basis   | A fingerprint alone cannot establish legal currentness.                       |

#### Completion and unsuccessful compiler results

| ID  | Relation                                        | Purpose                                                                         | Current owner                                    | CRI review   | Basis          | Open check                                                        |
|-----|-------------------------------------------------|---------------------------------------------------------------------------------|--------------------------------------------------|--------------|----------------|-------------------------------------------------------------------|
| H09 | Cancelled Work → No Completed Visibility        | Cancelled or partial work cannot become a completed visible result.             | Result Producer·Compiler Engine                  | Retain owner | Accepted basis | Check current eligibility independently when late results arrive. |
| H10 | Compiler Failure → Unsuccessful Compiler Result | Unsuccessful compiler results do not constitute authoritative Contract Refusal. | ADR-0074 (Proposed) Compiler Result Architecture | Deferred     | Proposed basis | Keep the provisional ADR-0074 failure model provisional.          |

### 10. Diagnostics, Security, and Independent QA

```text
[Semantic / execution products] --> [Diagnostics and provenance]
[Semantic / execution products] --> [Reference / PBT / coverage]
[Semantic / execution products] --> [Independent verification / QA]
[Semantic / execution products] --> [Security and trust qualification]

[Diagnostics and provenance] --> [Reports / released artifact evidence]
[Independent verification / QA] --> [Reports / released artifact evidence]
```

#### Diagnostics and evidence lifecycle

| ID  | Relation                                          | Purpose                                                                                | Current owner                                  | CRI review   | Basis          | Open check                                                                  |
|-----|---------------------------------------------------|----------------------------------------------------------------------------------------|------------------------------------------------|--------------|----------------|-----------------------------------------------------------------------------|
| C18 | Owning Judgment → Diagnostic Evidence Requirement | Diagnostic evidence is derived from the owning judgment's requirements.                | Diagnostic Evidence Law (Proposed)             | Deferred     | Proposed basis | Separate reviewed rules from unreviewed Diagnostic proposals.               |
| C19 | Diagnostic Evidence → Retention Requirement       | Evidence retention is governed independently of evidence formation.                    | Diagnostic Retention Law (Proposed)            | Deferred     | Proposed basis | Confirm the status and ownership of the relevant ADR-0060 provisions.       |
| I01 | Judgment Result → Diagnostic Evidence             | Diagnostics explain established judgments instead of reconstructing their authority.   | Diagnostic Evidence Law / Compiler Diagnostics | Retain owner | Proposed basis | Confirm the split between Contract Diagnostics and compiler diagnostics.    |
| I02 | HIR Provenance → Diagnostic Source Location       | Diagnostic locations refer back to the provenance of HIR subjects.                     | Provenance/Compiler Diagnostics                | Retain owner | Design basis   | Do not merge source positions into semantic identity.                       |
| I03 | Compiler Failure → Compiler Diagnostic            | Compiler diagnostics explain the stage and cause of compiler failures.                 | Compiler Diagnostics (Proposed)                | Retain owner | Proposed basis | Keep the proposed status of ADR-0061 explicit.                              |
| I04 | Contract Failure → Machine Diagnostic Projection  | External diagnostics project existing Contract Failure meaning.                        | Contract Failure/Diagnostic Owner              | Retain owner | Proposed basis | A diagnostic must not perform a new Failure judgment.                       |
| I05 | Diagnostic Evidence → Retention/Redaction         | Evidence retention and disclosure follow their own lawful limits.                      | Diagnostic Retention/Privacy Boundary          | Retain owner | Proposed basis | Do not expose private evidence merely because it was computed.              |
| N14 | Optimization decision → Optimization remark       | Optimization remarks explain the producer decision without becoming legality evidence. | Optimization Diagnostics Producer              | Retain owner | Design basis   | Do not feed diagnostic narration back as authoritative analysis.            |
| N18 | Transform output → Preserved diagnostic origin    | Transformed artifacts preserve the origin information needed for lawful diagnostics.   | Transform Producer / Diagnostic Provenance     | Retain owner | Design basis   | Keep origin mapping separate from semantic identity and Contract authority. |

#### Reference judgment, PBT, and coverage

| ID  | Relation                                               | Purpose                                                                                          | Current owner                       | CRI review   | Basis         | Open check                                                                              |
|-----|--------------------------------------------------------|--------------------------------------------------------------------------------------------------|-------------------------------------|--------------|---------------|-----------------------------------------------------------------------------------------|
| G18 | Execution IR → Reference Judgment/PBT Compatibility    | Reference Judgment and property-based tests compare execution against the same contract meaning. | Reference Judgment·PBT Producers    | Retain owner | Design basis  | Keep product dependencies distinct from authoritative contract judgments.               |
| I07 | Reference Judgment ↔ Optimized Execution Result        | Differential tests compare reference behavior with optimized execution.                          | Reference QA / Differential Testing | Retain owner | Design basis  | Check for correlated bugs that make two paths agree incorrectly.                        |
| N09 | Reference judgment → PBT expected result               | Property-based tests take expected contract meaning from an independent reference judgment.      | Reference / PBT Producers           | Retain owner | Design basis  | Do not use optimized backend output as the oracle.                                      |
| N10 | Established meaning → Contract coverage product        | Coverage reports derive their obligations from authoritative contract meaning.                   | Contract Coverage Product           | Retain owner | Design basis  | Coverage is a sibling compiler product, not a new authority.                            |
| N16 | Established World → Human-readable Contract projection | A future document projection can present established meaning without becoming its source.        | Contract Projection Product         | Deferred     | Open decision | Preserve the authority of the machine-readable Contract and do not assume a V1 product. |

#### Independent verification and transform validation

| ID  | Relation                                 | Purpose                                                          | Current owner           | CRI review   | Basis        | Open check                                                         |
|-----|------------------------------------------|------------------------------------------------------------------|-------------------------|--------------|--------------|--------------------------------------------------------------------|
| I08 | Transform Claim → Translation Validation | Translation validation checks a particular transformation claim. | Transform Validation/QA | Retain owner | Design basis | Do not claim that an unexamined transform has been proved correct. |

#### Authorization and security boundaries

| ID  | Relation                                          | Purpose                                                              | Current owner                        | CRI review   | Basis          | Open check                                                        |
|-----|---------------------------------------------------|----------------------------------------------------------------------|--------------------------------------|--------------|----------------|-------------------------------------------------------------------|
| I09 | Protocol Observation → Consumer Authorization     | A consumer may observe only what its protocol authorization permits. | Producer Protocol·Mediation·Security | Retain owner | Accepted basis | Prevent CRI from acting as a confused deputy.                     |
| I10 | Reference Identity → Observation Permission       | Possession of a reference does not confer universal read permission. | Security Boundary·Protocol           | Retain owner | Accepted basis | Do not let a resolver expose another owner's internal material.   |
| I11 | Runtime/Host Capability → Contract Boundary Gate  | Host capabilities enter through ratified or governed boundaries.     | Platform/Realization/Boundary Owners | Retain owner | Accepted basis | Do not use relation interpretation to permit hidden I/O.          |
| I13 | Compromised Cached Evidence → Trust Qualification | Stored evidence may be corrupted or come from an untrusted source.   | Storage/Trust·Producer Protocol      | Retain owner | Accepted basis | An integrity check alone does not establish semantic correctness. |

#### Release and provenance assurance

| ID  | Relation                                    | Purpose                                                            | Current owner           | CRI review   | Basis        | Open check                                               |
|-----|---------------------------------------------|--------------------------------------------------------------------|-------------------------|--------------|--------------|----------------------------------------------------------|
| I14 | Released Artifact → Source/Build Provenance | Released artifacts retain accountable build and source provenance. | Build/Release Assurance | Retain owner | Design basis | Do not promote build provenance into Contract Authority. |

## Architecture coverage review

| Architecture boundary                                                    | Mapped inventory region | Coverage action                                                        |
|--------------------------------------------------------------------------|-------------------------|------------------------------------------------------------------------|
| Contract authoring, IDL, source resolution, HIR seals                    | 01                      | Verify HIR lifecycle and nominal-coordinate distinctions.              |
| Establishment and 1D pipeline                                            | 02                      | Audit actual Required Basis obligations against owning 1D ADRs.        |
| Version, policy, governance, Whole-Machine contract composition          | 03                      | Keep proposed Governance relations open.                               |
| Canonical World, occurrences, legal producer/consumer protocols          | 04                      | Check producer-owned observation and visibility without new authority. |
| Generated API, host compilation, platform gates, realization acquisition | 05                      | Preserve the admission boundary and product-generation dependency.     |
| Realization analysis, contract-aware analysis, closure verification      | 06                      | Separate analysis summaries from verification judgments.               |
| Execution formation, specialization, optimization, IR invariants         | 07                      | Confirm transformation legality and independent validation.            |
| JVM planning, classfile emission, JVM execution boundary                 | 08                      | Check target legality and implementation of the declared Interaction.  |
| Driver, material/product/query graphs, reuse, generations, cancellation  | 09                      | Preserve distinct dependency types and result validity.                |
| Diagnostics, reference/PBT, coverage, security, QA, release              | 10                      | Check assurance independence and unauthorized observation.             |

### Remaining source checks

- [ ] Reconcile the Accepted inbound ordering in ADR-0066 with the older wording still present in ADR-0065. Do not
  reassign these laws through CRI.
- [ ] Complete the 1D-by-1D audit for source scope, cardinality, absence, applicability, and Complete Basis. Treat
  proposed Governance and Diagnostics provisions as unaccepted.
- [ ] Compare architecture regions 01–10 against *Kontrakt Compiler Architecture Map* §§3–54 and *Modern Compiler
  Architecture* chapter 15 §§51–80 and 111–112. This inventory covers their principal logical subsystems, but not every
  internal implementation edge.
- [ ] Check the security constitution and the host-compilation security architecture for consumer authorization,
  provenance, trust, cancellation, and cross-scope information exposure.
- [ ] Verify that the `N` coverage candidates are genuine independent architectural obligations rather than alternate
  names for existing relations. Merge duplicates without deleting their required guarantees.
- [ ] Confirm that B+ coverage does not make IDL Binding, Establishment, Realization Admission, Analysis, Verification,
  or Backend relations CRI-owned by default.

## CRI assignment checklist

- [ ] Identify the governing law or existing subsystem owner before discussing CRI.
- [ ] Determine whether the needed relation is already available as a lawful producer-owned observation.
- [ ] Where a separate source calculation remains, specify the required source domain and the exact guarantee. Do not
  give CRI selection or Establishment authority.
- [ ] Decide whether the relation fits a narrow common calculation without requiring the identities of Producers or
  Consumers.
- [ ] Validate source completeness, current eligibility, negative evidence, and failure without prescribing the index or
  scheduler.
- [ ] Keep contract semantic relations distinct from product dependencies, dataflow edges, and realization relations.
- [ ] Preserve independent Verification, coherent generation observations, and security-scoped protocol projections.
- [ ] Record the CRI result as `included`, `shared calculation only`, `original owner`, or `open`, with its exact owning
  source clause.

**Exit condition:** Every relation has a legal or architectural owner. CRI receives only specifically justified common
source-resolution work. No implementation-dependent edge is promoted to Contract meaning.