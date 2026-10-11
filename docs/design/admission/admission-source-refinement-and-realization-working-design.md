# Admission Source Refinement and Realization — Working Design

## Status

Working Design

## Date

2026-10-11

## Governing Documents

- ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary.
- ADR-0064: Input Contract.
- ADR-0066: Canonicalization Contract.
- ADR-0048: Inbound 1D composition.
- ADR-0053: Version Contract.
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition.
- ADR-0071: Resolved Contract HIR semantic boundary.
- ADR-0073: JVM platform-native semantic ratification and external Contract infiltration boundary.
- ADR-0075: Compiler result ownership, legal consumption, and deterministic realization.
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`: compiler-wide working architecture.

---

# 1. Purpose and Ownership

This Design records the compiler work required to acquire ordinary Java or Kotlin Admission source, resolve it into
complete Admission meaning, expose the required HIR and Established observations, and realize the resulting judgment
without retaining host execution as authority. It also records source-coverage candidates, implementation alternatives,
evidence requirements, and decisions that must be made before V1 implementation.

ADR-0065 determines which Admission judgments are legal and what they mean. This Design determines how compiler
components may establish and realize those semantics. A source form is not supported merely because it is listed here,
compiles on the JVM, or can be executed as a Boolean callback. Every supported form must satisfy the applicable
ADR-defined meaning and the corresponding compiler qualification.

The user should write ordinary supported Java or Kotlin instead of constructing Kontrakt IR or learning a second
judgment language. Internal semantic references, operation profiles, summaries, compiler handles, and projections remain
compiler concerns. The declaration's host type, method body, and generated interface do not independently establish
Contract authority.

The Admission Authoring API will be documented separately. That document will choose exact Java/Kotlin spellings,
supported overloads, legal declaration patterns, and user-facing diagnostics. This Design retains the implementation
requirements and candidate cases needed to make those choices; it does not approve a particular public API.

Admission user authoring is intended to remain Input-shaped whether Canonicalization is omitted, selected on some
coordinates, or selected throughout the applicable Input. Its exact semantic operand and Basis must follow the governing
inbound Contract decisions, not the host carrier object. The existing ADR-0065 text still contains the older
direct-Input path; do not implement that obsolete ordering as if it were a new decision. The correct current order is
Input → optional Canonicalization → Admission. Operand, Observation Requirement, Basis, Occurrence attribution, and
protocol details remain to be reconciled in the owning ADRs.

---

# 2. Source Capability Qualification Record

Each Java/Kotlin source construct or recognized platform callable considered for V1 needs a qualification record. This
record supports implementation and API decisions; it does not create a new Admission semantic catalog or delegate
authority to host libraries.

## 2.1. Exact source subject

Identify the language construct or exact callable and its overload, receiver, argument, value domain, language/platform
version, and any implicit behavior. A familiar operation name is not a sufficiently exact subject. For an inlined
helper, include every semantically reachable callee and initializer. Record Kotlin and Java differences when they change
resolution or execution meaning.

## 2.2. Input observation and actual operand

Describe which Input-declared coordinates and constituent observations the construct requires. Distinguish Input-facing
authoring syntax from the final legally determining Boundary Presentation supplied to Admission. Check omitted,
partially selected, and fully selected Canonicalization independently. Host field compatibility or JVM assignability
cannot establish semantic compatibility.

## 2.3. Closed operation meaning

Record the exact value relations, arithmetic and comparison behavior, conditional reachability, exceptional domains,
ordering, equality, and any platform semantic profile required to resolve the source. A selected Java/Kotlin library
implementation is evidence for analysis, not an implicit Contract law. ADR-0073 governs ratification of exact platform
meaning.

## 2.4. Effect, alias, and escape closure

Account for property access, overload dispatch, initialization, capture, iteration, user-defined equality or comparison,
nested calls, temporary mutation, and observable effects. Show that no reachable operation imports a hidden external
determinant or exports local mutable state as new authority. `val`, `final`, an annotation, and a nominally pure API do
not by themselves prove closure.

## 2.5. Completion and work

Establish the relevant domain guards, completion conditions, finite iteration or progress, and supported
pattern-matching work law. Distinguish semantic finiteness from acceptable physical CPU or memory cost. Keep compiler
analysis exhaustion and runtime resource enforcement separate from Admission `Admitted` or `Rejected`.

## 2.6. Disposition and falsifiable evidence

Record whether the exact form is implemented, conditionally supportable, unsupported for V1, or deferred pending
evidence. Preserve representative positive and negative cases, adversarial cases where relevant, and the reason for any
rejected qualification. This document currently identifies candidates and qualification conditions, not a completed V1
support matrix.

---

# 3. V1 Source Coverage Candidates

The following inventory provides planning coverage for the frontend and later Admission Authoring API. The target is
ordinary value-oriented judgment code whose complete meaning can be resolved under ADR-0065; none of the entries grants
blanket support to every host expression in its category.

| Candidate area                          | Source forms to qualify                                                                     | Main qualification issue                                                                               |
|-----------------------------------------|---------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Root judgment and Boolean control       | One root Boolean result, conjunction, disjunction, finite conditional choices, early return | Preserve reachability, short-circuit semantics, and exactly one Admission result                       |
| Fixed-width integers                    | Comparisons, arithmetic, bit operations, narrowing, shifts, division                        | Close signedness, widths, overflow, shift rules, division domains, and guards                          |
| Floating and decimal values             | Arithmetic, comparison, classification, large numbers, decimal values                       | Preserve NaN, infinities, signed zero, raw bits where observable, numeric vs presentation equality     |
| Closed alternatives                     | Enum-like choices, presence, absence, nullability, closed tagged choices                    | Read only Input-exposed discriminants and payload distinctions                                         |
| Text, character, and bytes              | Exact comparison, containment, slicing, codecs, Unicode and binary operations               | Fix encoding, position, equality, length, and external semantic profile                                |
| Closed domain values                    | Temporal values, identifiers, closed references                                             | Determine exact versioned operation meaning and any external semantic material                         |
| Finite array and Input collection forms | Size, indexed/keyed lookup, containment, quantification, aggregation                        | Respect Input `Sequence`, `Membership`, and `Association` semantics; do not invent order               |
| Local control and temporary state       | `for`, guarded `while`, branches, fresh accumulators, derived temporary material            | Prove finite progress, non-escape, no Input alias, and deterministic result                            |
| Helpers and standard-library calls      | Exact-target helpers, selected library operations, locally resolved lambdas                 | Close reachable calls, initialization, captures, dispatch, implicit effects, and platform profiles     |
| Java Stream and Kotlin Sequence         | Input-derived finite pipelines, filters, mappings, reductions                               | Recognize every stage, eliminate live host pipeline authority, preserve binder invocation and ordering |
| Patterns and regular expressions        | Matching, finite captures, supported pattern operations                                     | Close pattern syntax, Unicode behavior, capture semantics, finite supported work, and resource risk    |

Parallel or potentially open-ended streams are outside the initial V1 coverage unless the same semantic and completion
guarantees can actually be established. Open recursion and state-dependent loops remain unsupported when the required
completion proof is unavailable. Supporting an additional ordinary source form later does not itself modify Admission
meaning; adding an Admission-observable determinant or result requires an owning Contract decision.

## 3.1. Working source example

The following illustrates a possible frontend acquisition subject, not an approved public API declaration:

```kotlin
package example.calculate

data class CalculateInput(
    val x: Int,
    val limit: Int,
    val flags: Int,
)

object XGreaterThanOne {
    private const val MINIMUM = 1
    private const val REQUIRED_FLAGS = 0b0011

    fun admit(input: CalculateInput): Boolean {
        val requiredFlagsPresent =
            (input.flags and REQUIRED_FLAGS) == REQUIRED_FLAGS

        return input.x > MINIMUM &&
                input.x <= input.limit &&
                requiredFlagsPresent
    }
}
```

The frontend would need to resolve the constant values, Input-facing observations, bitwise operation, comparisons,
Boolean short-circuit behavior, and method reachability. The `object`, method, local variable, and host Boolean remain
source mechanics. Whether this exact declaration syntax appears in the final API is a separate Authoring API decision.

---

# 4. Frontend Acquisition and Refinement

## 4.1. Resolve the selected declaration

Resolve the exact Admission declaration selected through IDL and identify its single root judgment. Find the complete
semantically reachable source needed to express that judgment. Source provenance and formation dependencies must remain
distinguishable from Admission Definition determinants.

## 4.2. Resolve Input-facing observations

Translate supported field or property reads and nested constituent accesses into precise Input-declared Observation
Requirements. The frontend may use approved Input HIR observations for this work without importing the consulted Input
Candidate's identity into Admission Definition meaning. An observation is not authorized by a matching field name or
host type alone.

The mapping from those Input-facing observations to the final Boundary Presentation requires the revised inbound
composition law. Until that law is closed, the compiler must not assume that Admission always reads an uncanonicalized
`presentedInput` value.

## 4.3. Close reachable source behavior

Resolve the exact targets and semantics of reachable helpers, operators, library calls, lambdas, captures,
initialization, and derived operations. Obtain platform semantic knowledge through the ADR-0073 boundary where required.
Unresolved or effectful reachable behavior cannot be left as an arbitrary runtime callback. Do not execute application
code or application initialization to discover its meaning.

The frontend may eliminate provably unreachable source. If unreachability is not proven, rejecting unsupported source is
a permitted implementation coverage outcome rather than permission to execute it speculatively.

## 4.4. Produce complete HIR candidates

Lower closed authoring evidence into the complete Admission Definition Candidate and exact IDL Binding Candidate
governed by ADR-0071 and ADR-0065. The resulting HIR must express the full judgment law and Admission-owned Observation
Requirement. Host AST objects, host method identity, mutable runtime carriers, and transient compiler handles cannot
substitute for that meaning.

Recovery-tainted, incomplete, or unsupported source may remain available for diagnostics but must not cross the Visible
HIR boundary or enter Admission Definition Establishment. Once the Definition is established, downstream execution
formation consumes only legally exposed material and observations.

Source formatting, helper factoring, temporary allocation, analysis choices, and backend layout may change without
changing Admission Definition meaning when all legal observations remain the same. A change in Admission-owned semantic
determinants follows the existing identity and Version law.

---

# 5. Analysis and Source-Closure Work

The compiler may select CFG/SSA-based dataflow, effect analysis, exact call summaries, abstract interpretation,
guarded-domain analysis, termination reasoning, or another sound combination. This document does not prescribe one proof
technology. The implementation must nevertheless obtain sufficient evidence for every relevant legality obligation.

## 5.1. Reachability, dispatch, and initialization

Analyze only semantically reachable paths, but account for implicit getter execution, overloads, dynamic call targets,
initialization, callbacks, and platform helpers on those paths. A reachable call target that cannot be resolved under
the supported source profile makes the candidate unsupported. No fallback through reflection, MethodHandle invocation,
DI resolution, live class initialization, or host runtime evaluation may create Admission meaning.

## 5.2. Mutation, aliasing, and temporary structures

Allow fresh judgment-local accumulation only when relevant writes and reads are closed, the temporary state cannot alias
mutable Input or Definition material, and no reference escapes or becomes shared. Temporary collections or objects need
not survive execution formation. Verify these properties from the resolved behavior, not from host-language immutability
markings.

## 5.3. Partial operations, effects, and completion

Track guards and the paths on which partial operations are defined. Establish finite progress for supported loops and
bounded Input-derived traversal, and inspect exceptional behavior independently from normal Boolean control flow. An
exception handler cannot be treated as an implementation technique for manufacturing an Admission `Rejected` result.
Blocking, synchronization, external progress, and unclosed recursive cycles require explicit exclusion or separately
justified qualification.

## 5.4. Collection binders and ordering

Close each binder's invocation count, binding domain, and ordering semantics. For `Membership` and `Association`, do not
infer source order from incidental host iterators. A temporary derived order is usable only when the required ordering
and tie behavior are exact, or when the final judgment is invariant under every legal unresolved permutation. A derived
local relation is not automatically an Input-owned observation.

## 5.5. Pattern and library operation profiles

Close the exact callable or pattern semantics before selecting an implementation. Regex matching may be implemented by
an automaton, specialized matcher, or another method only if result, captures, Unicode basis, and work obligations are
preserved. Different library releases or overloads must not silently alter Admission meaning.

## 5.6. Analysis resource controls

Record the compiler's response to path explosion, deep helper structure, pattern complexity, nested collection
operations, and excessive summary growth. Source may have finite semantics while analysis is impractical. Exhausting a
compiler work limit must not be reported as Admission `Rejected` or as successful Definition Establishment.

---

# 6. HIR, Establishment, and Protocol Realization

The compiler must support the Admission Definition Candidate and IDL Binding Candidate reference domains, together with
the Established Definition and Occurrence reference domains. A physical row index, query key, object identity, address,
cache record, or fingerprint is not itself a Contract identity.

Complete and narrow logical projections need not each occupy separate objects or tables. Primitive backing may be shared
across projections when typed reference checking, producer-owned equality, completeness, and coherent visibility remain
recoverable. The same principle applies to compact basis and composition backing. A reverse index may accelerate
consumption without creating a reverse Contract relation.

ADR-0075 may govern qualification, delivery, and reuse of compiler observations. Query layout, dependency recording,
HID/fingerprints, persistence, lazy materialization, and physical access paths remain implementation choices. A
dedicated Admission manager or Composition manager is not required merely because the logical contracts are distinct.

Current Basis Binding and Applicability must be derived from legally established relations. Compiler reachability,
source type compatibility, or an earlier successful cached observation cannot establish them. The direct Input-only
Basis and occurrence attribution in the pre-revision ADR-0065 must be reconciled with Canonicalization before
implementing the final relation. Do not invent an extra Contract Authority or Material domain merely to simplify a
physical lookup.

Consumers must obtain legally required meaning through the producer-owned protocol rather than recovering it from the
author's method body, an internal table layout, a generated artifact, or an old compiler result.

---

# 7. Judgment Realization and Optimization

After legal Definition and Occurrence prerequisites are available, realization may replace host methods, helper
structure, collection pipelines, temporary objects, and a host regex engine with any implementation that preserves the
complete Admission-visible judgment and attribution.

Potential realization strategies include expression specialization, scalarization, constant evaluation, local-object
elimination, fused scans, primitive or columnar access, indexed lookup, vectorization, and alternate pattern matchers.
Input-derived pipelines need not materialize intermediate host collections. These are options for analysis and
experimentation, not mandated backend passes.

A transformation must preserve the exact result law, observed distinctions, reachable guards, partial-operation domains,
and failure/non-entry attribution. Side-effect freedom alone does not justify changing evaluation order, reassociating
floating arithmetic, parallelizing a reduction, or substituting a matcher with different Unicode or capture behavior.
Moving work across Canonicalization and Admission boundaries also requires proof that Contract-visible entry and refusal
semantics remain intact.

Establish an independently checkable reference-judgment path for comparing optimized and alternative realizations. The
verification approach may evolve, but an inconclusive proof or unsupported operation cannot be converted into a
successful Admission judgment. Resource-enforcement machinery remains owned by its applicable authority or
implementation boundary; runtime exhaustion does not become `Rejected`.

---

# 8. Diagnostics and Security Integration

The authoring API should not change merely because Canonicalization is absent, partially selected, or fully selected.
Diagnostic implementation must be able to relate the authored Input coordinate, any legally selected Canonical
Representative, the evaluated Admission condition, the actual result, and source provenance. It may expose original and
representative values only when the owning producers make those observations available and disclosure policy permits
their presentation.

Static diagnosis must distinguish a proven contradiction from a possible user-intent mismatch. Runtime diagnostics must
not claim to know which subcondition failed unless the necessary evidence is legally available. Compiler instrumentation
must not change the Admission judgment or promote diagnostic evidence into Contract authority.

The new Admission SADR will own the threat model, trust boundaries, attack paths, mitigation architecture, assurance
evidence, residual risk, and V1/V2 security scope. Design work must supply concrete implementation candidates and
negative tests for hidden source execution, ambient state, mutable aliasing, stale or incorrect basis, refusal bypass,
analysis amplification, ReDoS, invalid optimization, and diagnostic disclosure. Those controls must not silently
redefine the Admission result vocabulary.

---

# 9. V1 Realization and V2 Extension

V1 can use immutable or frozen observations, HID-based reuse, query-oriented compiler products, physical table packing,
reusable analysis, and parallel work where current qualification is exact. Reuse must be bound to the correct World,
Version, composition, Basis, and Admission Application; stale results cannot become current authority through a cache
hit.

V2 may change dependency representation, invalidation, scheduling, repair, and proof reuse without changing Admission
Definition or Occurrence meaning. Neither rustc-style pull evaluation nor a particular table schema, persistent format,
cache key, or realization manager is fixed here. Compiler product equality and physical reuse identity remain distinct
from producer-owned semantic equality.

Resource policy, verification depth, and performance targets will be specified in the owning compiler and security
designs. Finite semantic work is not a promise of constant-time execution, bounded implementation cost, or side-channel
resistance.

---

# 10. Open Implementation Decisions

| Subject                         | Decision required                                                                                                    | Owning or dependent document                                         |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Admission authoring surface     | Final Kotlin/Java declaration syntax, overloads, supported spellings, and all three Canonicalization selection cases | Separate Admission Authoring API Design; ADR-0065 controls meaning   |
| V1 source coverage              | Which exact callable/construct variants meet the qualification record; which remain unsupported                      | Frontend capability matrix; ADR-0073 for platform semantics          |
| Source closure                  | Practical analysis combination, summary boundaries, iteration/alias checks, negative diagnostics                     | Compiler Frontend/Verification Design                                |
| Input observation mapping       | Map Input-facing authored observations to the final legally determining operand                                      | ADR-0065, ADR-0066, ADR-0048; then Frontend Design                   |
| Admission Basis and attribution | Final selected/omitted Canonicalization relations, required cardinality, exact occurrence attribution                | ADR-0065, ADR-0063, ADR-0048; then Protocol Design                   |
| Protocol backing                | Compact primitive representation, shared projections, reverse indexes, coherent materialization                      | ADR-0075 and Compiler Material Design                                |
| Execution and performance       | Reference evaluator, optimization candidates, cost model, matchers, resource enforcement                             | Compiler Execution/Optimization Design; owning Budget/Capacity rules |
| Threat mitigation               | Trusted analysis inputs, proof integrity, resource amplification, stale material, diagnostics exposure               | New Admission SADR and Security Design                               |
| V2 incremental support          | Dependencies, revalidation, repair, and proof reuse without new Contract identity                                    | Compiler V2 TODO and Incremental Design                              |

A candidate can be marked implemented only after its exact meaning, negative cases, and conformance evidence are
available. Open ADR-dependent questions must not be decided implicitly by a convenient frontend representation or
backend shortcut.

---

# 11. Implementation Readiness and Conformance Evidence

Before selecting a V1 source construct or implementation strategy, test that normal and adversarial examples agree with
the governing Admission semantics. The minimum evidence must distinguish supported source from compiler-unsupported
source, correctly attributed refusal and non-entry from `Rejected`, and successful reference evaluation from optimized
realization.

Cover Input-only observations, partly Canonicalized observations, and fully Canonicalized observations once their Basis
laws are finalized. Exercise closed helper calls, implicit getters, initializers, dynamic dispatch, mutable captures,
open streams, exceptional paths, nonterminating or excessive work, unordered collection traversal, floating reductions,
Unicode and regex behavior, and resource exhaustion. Test revalidation of reused material under changed World, Version,
composition, and input application context.

A completed implementation qualification should identify its exact source subject, relevant ADR obligations, supporting
semantic evidence, negative and adversarial tests, resource limits, and any unsupported corner cases. Findings that
require a different Admission operand, authority, result, or established relation must return to the owning ADR rather
than being hidden in a compiler implementation decision.