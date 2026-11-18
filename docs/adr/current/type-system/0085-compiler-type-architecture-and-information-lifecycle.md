# ADR-0085: Compiler Type Architecture and Information Lifecycle

**Status:** Draft

**Date:** 2026-11-04

## 1. Context

Kontrakt interprets selected Java/Kotlin one-dimensional (1D) source in order to form complete Contract candidates.
ADR-0079 owns the required source-language interpretation and semantic binding. ADR-0084 owns the acquisition and
qualification of selected SDK operation meaning. Neither source typing nor qualified SDK knowledge establishes a
Contract.

A production frontend needs an internal type system to carry out that interpretation. It also needs declaration
resolution: the compiler must find the declarations available in the selected language and library environment before it
can determine what a source use denotes. These responsibilities cooperate, but their work is different. The internal
organization of type knowledge has not yet been decided by ADR-0079.

After source interpretation, each 1D forms its own candidate meaning. Resolved Contract HIR preserves that complete
meaning under ADR-0071. The owning Establishment then decides whether authoritative Definition or relation material
exists under ADR-0063. The Canonical Contract World makes already-established Definition meaning and its direct
relations observable. These boundaries do not require a general-purpose Java/Kotlin type model inside HIR or the World.

Kontrakt also compiles execution products from established Contracts. Host API generation, realization verification,
execution formation, diagnostics, and later optimization can need type information which is not itself Contract meaning.
The frontend must account for those actual information needs without extending the authority or payload of Established
Material.

## 2. Problem

Treating the resolver as one undivided type subsystem obscures two separate jobs. Declaration resolution obtains
relevant evidence and determines the exact source target. Type interpretation constructs types, solves constraints, and
judges the type relations needed to make that selection valid. In Java and Kotlin these jobs can be mutually dependent.
A one-way pipeline in which all declarations are selected before typing is not generally sufficient.

The opposite design is also unsound: a separate Type Engine cannot independently select source declarations or determine
Contract meaning from type shapes. It needs the declaration and binding context supplied by source resolution. Its
results are compiler knowledge under ADR-0079, not a new Contract authority.

There is a second problem after source typing succeeds. Some type information is consumed to form HIR meaning and is no
longer needed. Other distinctions can be lost before a later consumer needs them. Keeping every provisional inference
state is wasteful, but discarding all higher-level type information at HIR publication can make a legitimate later use
impossible. Neither outcome justifies copying the source type system into Established Material.

Finally, Kontrakt can combine type knowledge with established Contract meaning to derive useful information about a
proposed realization. Such analysis may identify a representation that cannot express a required value, show that a
runtime type test cannot establish a required property, or support a verified execution specialization. These are new
*compiler judgments about realization*. They must not be mistaken for new Contract meanings or for facts already owned
by HIR or the World.

## 3. Decision

### 3.1. Frontend Type Engine and Declaration Resolver

The 1D frontend SHALL distinguish **Declaration/Binding Resolution** from **Type Interpretation** as separate logical
responsibilities. They cooperate under the source-semantic obligations of ADR-0079. The distinction does not prescribe
two processes, two permanent object graphs, or a fixed pass order.

The **Declaration/Binding Resolver** owns discovery and contextual selection of source declarations. It acquires the
required evidence from the selected source and dependency environment, including the applicable Java platform and Kotlin
library surfaces. It determines scope, accessibility, candidate sufficiency, and the exact selected declaration or
callable. A declaration reader supplies evidence; evidence alone is not a completed binding.

The **Frontend Type Engine** owns the compiler's type-calculation machinery for supported Java/Kotlin source. It
interprets type constructions, calculates expression and use-site types, manages inference constraints, and answers the
exact type-relation questions requested by source resolution. This includes generic application and conversions where
the language rules require them. ADR-0079, not this ADR, determines the meaning and correctness of those judgments.

The Resolver may request type judgments to filter or select candidates. The Type Engine may request declaration evidence
and applicable binding context to interpret a type or expression. The selected callable and its completed type
application must agree. A candidate set cannot be considered sufficient simply because the Type Engine found one
applicable signature.

The selected SDK operation's calculation meaning is obtained from the qualified knowledge governed by ADR-0084. Neither
the Resolver nor the Type Engine may infer that meaning from the method name, result type, or an erased JVM descriptor.
The owning 1D separately decides whether that operation is a lawful Contract expression.

This allocation is internal to the **1D Contract frontend**. User Operation realization remains on its separately
governed host-compilation path. It may use host-compiler type evidence without importing the 1D Type Engine as its type
authority.

### 3.2. Type Knowledge and Completed Source Results

The Type Engine may maintain shared type and declaration-related structures while a source judgment is being computed.
Its inference variables, provisional bounds, and candidate-specific working state are local compiler knowledge. Their
existence does not make them independent successful results.

A completed source use must be consumable with the distinctions required by that use. These can include the resolved
expression type, the applied type arguments, and the conversion or adjustment selected by the language rules. Exact
callable selection remains the Resolver's result, even when it depended on Type Engine judgments. The producer
boundaries may be physically fused provided their claims stay attributable and coherent.

A reusable type-related result is always relative to the actual semantic basis required for its claim. Shared
declaration evidence and a contextual use result do not have the same validity conditions. In particular, an existing
declaration does not prove that a previous call selection remains valid after a competing overload becomes available.

A type calculation is not complete merely because one plausible type can be written down. An unresolved inference,
unknown applicable candidate, unsupported source use, or unavailable required evidence cannot be published as a
successful source judgment. ADR-0079 and ADR-0074 own those unsuccessful distinctions; ADR-0075 owns their
producer-result consumption and reuse boundaries.

This ADR introduces no universal `TypeInfo` schema and no second source-language type law. The Type Engine's physical
representation, canonical type sharing, and indexing are Frontend Design decisions. An implementation may use interning,
compact handles, or on-demand calculation when the required semantic distinctions remain intact.

### 3.3. HIR and Establishment Handoff

Each owning 1D consumes completed source interpretation and qualified operation meaning to form its candidate. The Type
Engine does not decide Input, Admission, Canonicalization, Lowering, Fact, or another 1D's Contract law. A successful
source type judgment does not establish a Definition, satisfy a Required Basis, or authorize an Occurrence.

Resolved Contract HIR must retain every distinction required by the owning 1D's candidate meaning. An exact reference to
separately owned meaning is sufficient only where that 1D's existing law permits it. The candidate cannot rely on a
later call to the Resolver or Type Engine to complete its meaning after the HIR seal.

Establishment consumes the completed candidate under its owning law. It does not repeat source inference, reselect a
callable, or use type compatibility to invent a Contract relation. The Canonical Contract World contains the resulting
Established Definition meaning and its established direct relations, not a duplicate type database.

The nominal distinctions already owned by HIR, Established Material, and the State/Fact/Operation laws remain theirs.
They may be represented through typed compiler APIs, but such representation does not establish an additional Contract
type system.

### 3.4. Contract-Relative Use of Type Knowledge

Outside source resolution, type knowledge may be combined with an exact Contract observation to answer a **new compiler
question**. The new result is justified only if it cannot be obtained by merely restating an existing HIR field,
Established Material, or source type judgment. ADR-0075 governs ownership of independently consumed derived knowledge.

Two questions are especially relevant to subsequent realization work:

1. **Representation feasibility.** Given an exact Contract requirement, a selected Host representation, and any
   explicitly permitted correspondence, can that representation express the values required by the Contract? A proved
   incompatibility may inform realization admission or diagnostics. It does not change the Contract's own value domain
   or create an implicit Lowering relation.
2. **Runtime observation sufficiency.** Given an established judgment and an available Host/JVM observation, is the
   observation capable of deciding what that judgment requires? A Java/Kotlin generic type argument, for example, is not
   generally proved by testing the runtime class of an erased `List`. Additional lawful value observation may be
   required. This does not replace the owning Input or other occurrence judgment.

A third use is **verified specialization**. A type fact may help narrow execution alternatives when combined with an
established Contract and an exact realization-value correspondence. Its applicability to an SSA value, execution path,
or Occurrence must be proved by the responsible realization analysis. A type match, equal source name, or successful
Definition Establishment alone supplies no such correspondence.

These questions do not introduce a `ContractType`, `RefinedContractType`, or general `Type Qualification` authority. The
source-language type is unchanged. The established Contract remains the source of obligations. The combined result is
compiler-derived knowledge owned by the relevant analysis, realization admission, verification, or execution-formation
producer, not by HIR, Establishment, or the Type Engine.

A type-aware analysis may fail to prove its claim. Failure to prove feasibility or an optimization precondition does not
by itself establish impossibility or a Contract refusal. The owning consumer decides whether it needs a proof for a
legal realization or must retain an unoptimized path. No unknown result may be used as a successful guarantee.

### 3.5. Compiler and Runtime Consumption Boundary

A consumer must first identify which information it actually needs. If its question is answered by HIR or Established
Material, it consumes the owning semantic protocol. If it needs a distinct higher-level source type fact, it consumes a
lawful result from the frontend producer. A derived analysis combines those approved observations under its own result
contract.

The following responsibilities remain distinct:

| Consumer                               | Legitimate type-related use                                                                    | Meaning owner                |
|----------------------------------------|------------------------------------------------------------------------------------------------|------------------------------|
| 1D candidate formation                 | Exact source types and selected operations needed to complete candidate meaning                | Owning 1D and ADR-0071       |
| Generated Host API                     | Host-facing type representation not already fixed by the established Interface and output laws | API/Platform realization     |
| Realization admission and verification | Selected implementation's actual type/ABI correspondence and executable closure                | Realization and Verification |
| Execution formation                    | Observations and guards sufficient to realize existing judgments                               | Execution IR and owning 1D   |
| Optimizer                              | Type facts, established guarantees, and proved value/path correspondence                       | Analysis and Optimization    |
| Diagnostics and tooling                | Source-specific type evidence needed to explain a completed or unsuccessful judgment           | Diagnostic producer          |

The Canonical Contract World need not retain those source type results for the consumers to obtain them. Conversely, HIR
or the World must not be weakened merely because a separate source-type result happens to remain available.

At runtime, the World supplies already-established Definition meaning. Actual values and occurrence-specific judgments
are evaluated under their owning laws. Runtime class observations, profiles, and speculative type assumptions remain
execution knowledge. A profile of frequent receiver classes cannot establish Contract applicability or satisfy an Input
or Admission judgment. Any specialization requiring such an assumption must preserve the applicable execution and
verification guarantees, including its guard or fallback requirements.

This ADR does not require a runtime Type Engine, runtime re-resolution of Java/Kotlin source, dynamic recompilation, or
a new Contract-relative type system. MIR, LIR, JVM verification, runtime optimization, and their distinct type rules
remain under their own architecture and Design.

### 3.6. Type Information Lifecycle

Information is retained for the guarantee it must supply, not because it participated in type inference. Three logical
lifetimes must be distinguished:

- **Working knowledge** serves one incomplete source judgment. It need not survive the completed interpretation unless
  another producer has a justified use for it.
- **Completed source knowledge** supports exact Binding and 1D candidate formation. It remains usable only under the
  semantic context and result-validity conditions of its producer.
- **Residual source type knowledge** is higher-level type information still needed by an identified consumer after the
  necessary Contract candidate meaning has been formed. It may be consumed before erasure, retained as a producer-owned
  result, or recomputed from qualified evidence when legal.

The third category is not mandatory for every type or compilation. A consumer must identify a distinction it cannot
obtain from its existing Contract or compiler result. The frontend must not preserve arbitrary solver state or source
syntax merely in anticipation of an unspecified future use.

Source provenance may be retained for diagnostics without becoming Type Identity or Contract Identity. A canonical type
representation, a persistent product fingerprint, a generation-local handle, and an authoritative Definition Reference
do not acquire one equality law by being stored together.

Information loss across source lowering, Host compilation, HIR formation, or later realization lowering must be assessed
against actual consumers. Information required to complete HIR cannot be deferred past the HIR seal. Information
required only by a later independent consumer does not thereby become HIR or Established meaning.

### 3.7. Validity, Evolution, and V1/V2 Boundary

A successful type result may be reused only when its original claim remains valid for the current source-semantic
environment. Relevant changes can include declaration visibility, candidate availability, selected library profiles, and
language options. The Type Engine or Resolver owns the exact dependencies for its claim. ADR-0075 governs mediated
consumption, coherence, reuse, and the clean reference path.

V1 must provide complete, correct typing and binding for its declared 1D source support and deliver sufficient
information to form the required HIR candidate. V1 does not require a permanent type database, persisted inference
graph, separate Type Product family, or speculative runtime specialization.

V2 may improve type sharing, demand-driven evaluation, incremental invalidation, and preservation of independently
consumed type summaries. Those are replaceable compiler techniques. A changed type result does not automatically change
a Contract Definition; conversely, an unchanged erased JVM signature does not prove that the source meaning or qualified
operation is unchanged. Reuse must respect each producer's actual observation equality and validity requirements.

The division of physical components, storage formats, query granularity, host-compiler evidence adapters, and
specialization algorithms remains Design work. A separate retained Type Knowledge producer is warranted only when a
concrete consumer requires an independent guarantee not already supplied by an existing producer. The division between a
Resolver and Type Engine is logical and does not by itself demand such a product.

### 3.8. Verification and Open Design Questions

The architecture must be validated against cases that expose both missing type meaning and false reuse. At minimum,
Frontend Verification should check a generic call whose member type depends on receiver substitution, an overload whose
candidate set changes without removing the original callable, and Java/Kotlin interoperation where erased signatures do
not preserve the source distinction. The resulting 1D HIR must remain complete without later source re-resolution.

For derived use, Verification should compare an exact established requirement with a Host representation that cannot
express it under the selected correspondence, and a generic collection requirement that cannot be proved by a runtime
class test alone. These are **analysis and realization conformance cases**, not new 1D Contract laws. A missing proof
must not silently become a successful result.

The following choices remain open pending consumer evidence and measured costs:

- Which completed source type distinctions require independent consumption after HIR formation.
- Which producer retains or reconstructs those distinctions, and for how long.
- Whether several consumers justify a shared type-related result rather than local, request-specific calculation.
- Which type/Contract joint analyses are profitable or required by realization verification.
- Which V2 incremental and physical representations are worth introducing.

No choice in this list permits a change to the source-language judgments of ADR-0079, the qualified SDK law of ADR-0084,
or the meaning established by the owning Contract.

## 4. Consequences

The frontend gains a distinct Type Engine responsibility without introducing a second language interpretation authority.
Declaration selection and type calculation can evolve independently while producing one coherent, correct source
judgment.

1D candidates remain complete before Establishment. Existing HIR and Established Material retain their authority and
information boundaries. The Canonical Contract World does not become a source type repository.

Type knowledge can support real Contract-aware compiler analyses, including representation feasibility and runtime
observation sufficiency. Those analyses establish their own limited compiler claims and cannot revise Contract meaning
or turn execution profiles into proofs.

The main cost is a sharper producer boundary: Frontend Design must coordinate declaration evidence, type calculations,
selected bindings, and completion without exposing inconsistent results. Consumers must justify retaining higher-level
type information rather than depending on whatever frontend object happens to remain alive.

V1 can use a simple realization of these responsibilities. V2 can add sharing, incremental calculation, and retained
summaries without changing the authority model. Whether those optimizations are worthwhile remains subject to
verification and measurement.