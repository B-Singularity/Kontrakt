# SADR-0002: Collection Observation Integrity, Protected Changes, and Incremental Reuse

## Status

Proposed

## Date

2026-11-10

## Related

- `docs/constitution/security/security-constitution.md`, especially §§1, 4, 7–13 and 17
- `docs/the-most-important-thing/what-contract-is.md`
- `docs/sadr/0001-admission-security-architecture.md`
- ADR-0050 State/Transition; ADR-0051 Budget; ADR-0052 Capacity; ADR-0055 Whole Machine; ADR-0057 Failure
- ADR-0063 Establishment; ADR-0064 Input; ADR-0065 Admission; ADR-0067 Lowering; ADR-0068 Fact; ADR-0069 Invariant
- ADR-0071 Resolved Contract HIR; ADR-0073 JVM Platform; ADR-0075 Compiler-Produced Material
- ADR-0078 Numeric; ADR-0079 Type Interpretation; ADR-0081 Collections and Structured Data (Draft); ADR-0084 SDK
  Qualification
- `docs/todo/compiler-engine/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `docs/todo/compiler-engine/kontrakt-v2-incremental-architecture-research-todo.md`
- Primitive storage, FFM and compiler architecture working Design/TODO; implementation studies are non-normative
- Modern Security Architecture study, 2026-10-01; Kontrakt V1/V2 Compiler Implementation study, 2026-11-04
  (non-normative)

---

# 1. Purpose and Ownership

Collections are used in author-written Contract calculations, at the runtime Interaction boundary, inside user Operation
realization, and throughout the compiler. A partially changed collection can leave a wrong result, but it can also
contaminate the meaning of another result that read it. A later rollback of the first calculation does not by itself
repair the second result. The threat is especially significant when a current World or compiler product is assembled
from separately produced results, or when incremental work reuses information from an earlier computation.

This SADR examines those failures as integrity and, where relevant, confidentiality and availability problems. It states
the evidence needed to claim that an implementation preserves the meaning established by its owning Contract. The
Security Constitution controls security obligations. ADR-0081 controls common Collection meaning. The 1D ADRs control
judgment and establishment; ADR-0057 and other owning contracts control failure attribution, scope and recovery;
ADR-0075 controls compiler-result validity and reuse. This SADR does not invent their missing decisions.

A physical layout can expose a fault, but it cannot define the Contract. The compiler may implement collections with any
suitable storage, index, mutable workspace or persistent structure. The security question is whether a fault, aborted
computation, stale reference or concurrent update lets a consumer observe a result it could not lawfully observe.
Physical techniques and benchmarks remain in Design/TODO and implementation tests.

The proposal commonly called **A+C** is the agreed working direction for subsequent Transaction/Rollback design:
Kontrakt-owned, actually controllable protected changes should ordinarily be reversible on failure, while external
objects and services participate only through explicit, supported arrangements. Scope boundaries, activation,
completion, savepoints, external API and the exact rollback policy are not decided here. Ordinary JVM realization may
retain ordinary JVM effects outside a declared protection; this SADR must not claim universal interception or
sandboxing.

# 2. Protected Properties and Security Claims

A security claim must identify the specific subject, observer, validity context, and supported failure model. The
following are independently testable properties rather than one generic `secure` flag.

## 2.1. Result integrity

A completed Collection result must support the observations that its selected source operation and consuming Contract
actually require. These can include an element's value, its presence or absence, the number of occurrences, order,
selected representative, group membership, an aggregate, or the completeness of a qualifying set. One completed result
cannot expose mutually inconsistent observations of its constituents.

An element already computed in an interrupted transformation is not automatically the completed result of that
transformation. A different operation may legally produce an independent prefix result; whether that result is complete
follows its own declared meaning. The distinction is made by the owner, not by the number of elements already written.

## 2.2. Protected-change observation

Where another owning law has established a protected change, a consumer must not mistake a pending Collection effect for
a committed effect. If that change is withdrawn under its actual protection law, an observation of the protected
Collection must satisfy that law's restoration requirements. Restoring a count while leaving a different order, key
association, representative or admitted nested constituent is insufficient when those differences are observable.

This claim is conditional on the true protection boundary. It does not assert that every `MutableList`, every JVM
object, or every database transaction is automatically protected. It does not require a universal rollback algorithm.

## 2.3. Identity, membership and currentness

Contract identity, an element's value, a constituent's occurrence in a particular Collection, a position, and a current
membership relation are distinct. A retained address, ordinal, hash or equal object value does not prove that an element
is in the current result. A cached product requires its producer's actual currentness evidence; neither historical
success nor stored bytes establish present authority.

## 2.4. Dependent-consumer integrity

A result derived from a pending or withdrawn Collection observation may lose its justification even if its own stored
bytes are intact. This includes direct readers, transitive consumers, and calculations that relied on a member's absence
or on a complete candidate set. A later valid re-evaluation may establish that the same result is reusable; its earlier
production does not itself prove that fact.

Which consumer is rolled back, canceled, revalidated or independently retained belongs to the relevant protection,
producer-result and scope laws. This SADR tests for unlawful reliance, not a universal dependency traversal policy.

## 2.5. Isolation of established meaning

An interrupted computation must not alter the meaning of material already lawfully established outside its own allowed
change relation. An independent established Definition cannot become invalid merely because a separate World formation
failed (ADR-0063). Conversely, a consumer cannot combine results from incompatible states and claim they were jointly
valid.

## 2.6. Availability and bounded work

Collection operations can generate unbounded or unexpectedly large work through nesting, grouping, sorting, callbacks,
materialization, copying, conflict handling or recovery. Budget, Capacity and compiler-internal resource management have
distinct authorities and measurement rules. A resource stop must not be recoded into a successful Collection result or a
false Admission judgment. A recovery path must be examined for its own resource requirements.

## 2.7. Confidentiality and attributable failure

A Collection contains information even when only a count, membership result, hash, group name or failure cause is
exposed. Security evidence and diagnostics must obey the disclosure boundary of the original material and any derived
material. Failure attribution should identify the principal failed judgment or producer and the relevant causal
observations. A full traversal of execution stacks is not the default diagnostic guarantee; unrelated internal details
should not be exposed merely because they are available.

The established Failure Contract owns the Failure meaning and diagnostic attribution law. The proposed security controls
below are assurance requirements, not a second Failure Contract.

---

# 3. Trust Boundaries and Failure Surfaces

## 3.1. Contract compilation

The authored Java/Kotlin expression is resolved and qualified before HIR. The HIR candidate becomes authoritative only
through the applicable Establishment. Compiler-produced products may consume established material only under their
producer-owned validity and protocol guarantees. During World formation, independently established results can coexist
with failed candidates without giving the failed candidates authority. An explicitly protected wider formation, if later
supported by its owning law, changes the pending results' acceptance conditions; it does not retroactively rewrite
already established unrelated meaning.

The main security handoffs are **source meaning → HIR candidate**, **candidate → Established Definition**, **established
definitions → coherent World observation**, and **producer product → consumer/reuser**. Compromise at any handoff can
make an otherwise correct consumer apply the wrong Contract.

## 3.2. Runtime machine

An external Java/Kotlin Collection is a carrier. Kontrakt-controlled Ingress may form a complete presentation before
Input judgment; an Input judgment cannot legitimize an unlawful mutable carrier by freezing it after judgment begins
(ADR-0064). The required Admission, Canonicalization, Lowering, Fact, Invariant, State/Transition and other judgments
have their own applicability and authority boundaries. Their physical execution order cannot be inferred from a single
universal diagram.

A selected Operation can have no Input, no outward Result, or only a state effect. Those forms must not evade the same
relevant completion conditions simply because there is no returned object. A local Collection computation may finish
while a necessary later judgment or Whole-Machine condition fails. Its successful calculation then cannot be taken as
proof that the protected change has committed.

## 3.3. JVM and external integration

A user JVM Operation may invoke existing frameworks, use mutable objects, write a database, send messages or call remote
services. Kontrakt does not own the surrounding system merely because its Interaction initiated that work. External
changes receive transaction protection only where a declared integration can genuinely provide it. An annotation name, a
callback registration or a `rollback()` method does not prove coordinated atomic commit across independent resources.

For example, a Spring `@Transactional` method may return a structured Kontrakt failure normally; the default Spring
rollback rules need not interpret that as an exception. Similarly, a database may commit before the application receives
confirmation. Compensating an external effect after the fact is not the same claim as preventing an uncommitted effect
from becoming visible.

## 3.4. Internal compiler computation

The compiler uses working collections for graph formation, relation assembly, analysis, optimization, indexing, cache
management and incremental maintenance. These are compiler-engineering structures. The required security claim concerns
the completed product and authorized consumption, not the storage layout, address, generation counter or update
algorithm. An implementation may use physical versioning, logs, separate candidate regions or other methods; this SADR
does not select one.

---

# 4. Detailed Threat and Fault Cases

The identifiers below are stable references for review and test planning. They are not new Contract identities. A case
describes a possible vulnerability, not evidence that Kontrakt's current implementation has that bug.

## 4.1. Compilation and World formation — W01–W12

| ID  | Failure scenario                                                                                                 | Property violated if mishandled                                                      |
|-----|------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| W01 | A candidate Collection is partially formed, then exposed as a completed HIR product.                             | The consumer interprets incomplete source meaning as a completed candidate.          |
| W02 | One Definition finishes while another fails; a consumer assumes the complete World succeeded.                    | A failed basis silently participates in a supposedly valid World observation.        |
| W03 | A later World formation fails and an unrelated, already established Definition is discarded.                     | Valid established meaning is revoked without its owning law.                         |
| W04 | A user-declared wider protected formation contains individually completed calculations, but a late member fails. | Pending member results are accepted despite the wider declared completion condition. |
| W05 | A result's constituents and lookup relation are derived from different candidate states.                         | Two observations of one claimed result disagree.                                     |
| W06 | A failed candidate remains in the set of applicable bindings or in an absence/uniqueness query.                  | Complete Basis and Applicable Binding judgments rely on a nonexistent member.        |
| W07 | A new Applicable Binding appears; the old Binding is unchanged and a cached exactly-one judgment is retained.    | A complete-set assertion is reused without its new determinant.                      |
| W08 | A failed producer is canceled, but a previously scheduled worker finishes and exposes its output.                | Work beyond its lawful completion state becomes visible.                             |
| W09 | An established result shares mutable working constituents with a new candidate.                                  | Later failed work changes authoritative prior meaning.                               |
| W10 | A prior position or physical handle is reused for a different candidate.                                         | A historical reference is mistaken for current semantic identity.                    |
| W11 | A current reader combines a previous result with a new result that was never jointly valid.                      | No coherent World observation supports the result.                                   |
| W12 | Compilation crashes during persisted product formation and restart treats incomplete material as complete.       | Recovery promotes unfinished work into authority.                                    |

W03 and W04 illustrate a necessary distinction. Ordinary Establishment preserves independent completed meaning. If a
wider protected scope is explicitly established by its owner, the completed calculations within it can remain pending
that scope's acceptance. Neither case gives physical storage or SADR-0002 the power to decide the scope.

## 4.2. Source Collection behavior and runtime acquisition — R01–R15

| ID  | Failure scenario                                                                                            | Property violated if mishandled                                                               |
|-----|-------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| R01 | A mutable carrier changes during acquisition; elements are collected from incompatible moments.             | Input receives no lawful complete presentation.                                               |
| R02 | A destination-mutating transformation throws after some additions, removals or replacements.                | Its partial source effects are ignored or incorrectly reported as a complete Contract result. |
| R03 | A grouping operation changes an existing group and creates a new one, then fails.                           | A consumer sees inconsistent group membership or a new group that never completed.            |
| R04 | A backed view is treated as a detached value while its source changes.                                      | The acquired value depends on a live external relationship.                                   |
| R05 | The outer Collection is copied but a mutable nested element remains shared.                                 | An external alias changes the meaning of a supposedly stable constituent.                     |
| R06 | Two positions refer to the same mutable object; changing one is visible through the other.                  | A consumer mistakes reference aliasing for independent completed value occurrences.           |
| R07 | A mutable Map key changes its equality/hash-relevant state after insertion.                                 | The selected source lookup semantics are no longer justified by stable key assumptions.       |
| R08 | A temporary window escapes the lifetime permitted by its SDK operation.                                     | Later observation of the window does not denote the claimed result.                           |
| R09 | A one-shot iterator or Stream is consumed twice for retry or validation.                                    | The second computation is assumed equivalent without evidence.                                |
| R10 | A lazy member throws when first accessed, after the Collection object was already admitted as complete.     | Object existence is mistaken for completed constituent meaning.                               |
| R11 | A short-circuit callback is forced to run although the SDK could legally elide it.                          | Failure and mandatory process observations are changed.                                       |
| R12 | A callback that changes external state is treated as a pure mapping function.                               | The result is not closed under the admitted source/1D law.                                    |
| R13 | Concurrent or weakly consistent traversal is treated as an atomic snapshot.                                 | Incompatible observations are assembled into one Contract presentation.                       |
| R14 | External GC, time or iteration order changes a value that is claimed deterministic.                         | An undeclared external determinant enters Contract meaning.                                   |
| R15 | Different implementations of the same interface leave different partial mutations after a failing callback. | The exact selected API's behavior is replaced by a fabricated generic Collection law.         |

The security review must distinguish SDK-authorized partial effects from unauthorized publication of a Contract result.
In particular, an unprotected user JVM call is not silently converted into a transaction merely because it uses `List`,
`Map`, `replaceAll` or `removeIf`. Source qualification, not a proxy policy engine, decides the admissible semantics of
a source expression.

## 4.3. Protected runtime changes and downstream consumers — P01–P13

| ID  | Failure scenario                                                                                                      | Property violated if mishandled                                                           |
|-----|-----------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| P01 | A protected Collection is modified and a later Invariant rejects the proposed change.                                 | A local calculation's success is mistaken for overall change acceptance.                  |
| P02 | A protected update changes two Collection-valued Facts, but only one becomes externally visible.                      | The owning Change Proposal's indivisibility is violated.                                  |
| P03 | A `Unit`/`void` Operation updates state and later fails.                                                              | No-return-value execution is incorrectly exempted from protected-change handling.         |
| P04 | A no-Input Operation reads existing state and performs a protected mutation.                                          | The absence of Input is incorrectly equated with the absence of an effect or determinant. |
| P05 | Budget stops an in-progress Collection change after some elements are processed.                                      | Pending effects are accepted because a physical prefix completed.                         |
| P06 | Capacity rejects growth needed by the operation while previous elements remain valid.                                 | The failed growth either contaminates the result or erases unrelated established meaning. |
| P07 | A reader consumes a pending element; the writer subsequently rolls back.                                              | The reader's derived result survives without a current lawful basis.                      |
| P08 | A second computation consumes P07's derived value.                                                                    | A transitive dependency escapes review.                                                   |
| P09 | A reader observes that an element is absent during a pending deletion.                                                | The deletion's rollback invalidates an absence judgment.                                  |
| P10 | A reader observes the count, representative or aggregate of a pending Collection.                                     | An aggregate dependency is overlooked because no single element value was recorded.       |
| P11 | A pending result is read outside the declared protection relation.                                                    | Scope escape defeats the claimed isolation.                                               |
| P12 | A protected Collection is restored, but an external alias already observed its pending state.                         | Value restoration is falsely claimed to erase the earlier external observation.           |
| P13 | A reader is canceled solely because a sibling computation failed even though its basis was independently established. | Failure propagation destroys an unrelated valid result.                                   |

P07–P13 do not settle cascading rollback, nested scopes, savepoints, isolation levels, or the treatment of
already-observed external effects. The owning protection and Failure/Governance laws must establish those policies. This
SADR requires that an implementation's claim be precise about whether pending reads are prevented, permitted within a
guarded scope, or subsequently revalidated.

## 4.4. Identity, absence, completeness and representative selection — I01–I12

| ID  | Failure scenario                                                                                   | Property violated if mishandled                                    |
|-----|----------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| I01 | Two independent Contract subjects have equal Collection values and are merged as one identity.     | Value equality is promoted into Contract identity.                 |
| I02 | The same subject identity is retained while a determining input changes.                           | Prior calculated meaning is treated as current solely by identity. |
| I03 | A removed member remains in retained physical state and is still returned as current.              | Historical existence becomes current membership.                   |
| I04 | A sequence insertion shifts positions, but old indices are treated as constituent identities.      | The wrong occurrence is selected.                                  |
| I05 | One duplicate occurrence is removed while the remaining duplicates are correctly valued.           | Multiplicity and the result's actual cardinality are lost.         |
| I06 | The selected representative is removed but the old representative remains cached.                  | A current selection violates the source tie/selection law.         |
| I07 | A Map key's semantic relation differs from the host's `equals` or comparator relation.             | Unrelated equality domains are collapsed.                          |
| I08 | A new qualifying member makes an exactly-one or complete-set assertion false.                      | Negative/complete-set dependencies are missing.                    |
| I09 | A grouping change moves a member between groups without changing the number of groups.             | Superficial equality conceals changed constituents.                |
| I10 | A prefix/window calculation is assumed unchanged after an earlier member changes.                  | Derived positional dependence is missed.                           |
| I11 | A stale material handle refers to a new subject after storage reuse.                               | Physical reuse impersonates semantic identity.                     |
| I12 | A failed candidate later reappears with equal values and inherits its failed predecessor's status. | Current qualification is bypassed by apparent continuity.          |

A result's asserted completeness can change even if no previously enumerated member changes. This is a Collection
observation property. How a producer records or validates such negative dependencies is not a Collection Contract
decision. The Owner must also preserve the distinction between a Value, an Occurrence and a current applicability
relation.

## 4.5. V1/V2 computation, caching and optimization — V01–V14

| ID  | Failure scenario                                                                                           | Property violated if mishandled                                   |
|-----|------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| V01 | A cached product is reused because its hash matches, although the current meaning context differs.         | Hash equality is substituted for validity.                        |
| V02 | An incrementally repaired nested result combines new outer membership with old inner constituents.         | The output describes no lawful complete computation.              |
| V03 | A differential deletion leaves a representative or aggregate that depended on the deleted member.          | Incremental update differs from clean recomputation.              |
| V04 | A producer's historical dynamic read set omits a newly relevant element.                                   | Early cutoff ignores negative or complete-set dependencies.       |
| V05 | A failed incremental repair publishes some changed relations as a successful product.                      | Pending work acquires consumer-visible authority.                 |
| V06 | A canceled worker writes after a new computation begins.                                                   | An unrelated result is contaminated by late execution.            |
| V07 | A consumer reads a current result combined with incompatible cached evidence.                              | The claimed composite validity has never been established.        |
| V08 | An index or derivation remains usable after its underlying result is withdrawn.                            | Retained auxiliary information becomes stale authority.           |
| V09 | An optimizer changes the number or order of mandatory callbacks.                                           | Source-visible process obligations are violated.                  |
| V10 | Parallel or vectorized reduction changes a required numeric result or failure condition.                   | Numeric law is replaced by an implementation-dependent reduction. |
| V11 | An e-graph rewrite merges two equal-looking computations with distinct required effects or identities.     | Equality reasoning erases a mandated distinction.                 |
| V12 | A fused operation performs an effect before a judgment that previously prevented it.                       | Reordering exposes an effect on a path that must not reach it.    |
| V13 | A one-shot source is silently replayed after a failed optimization or verification attempt.                | Retrying implementation work changes the legal JVM operation.     |
| V14 | A repair result is accepted because its output bytes are equal, but its provenance/qualification is stale. | Result equality is mistaken for sufficient security evidence.     |

V01–V14 must be evaluated for full recomputation, validated reuse, pull and push dependency discovery, domain-local
hybrid, differential maintenance and elastic recomputation where those methods are supported. The security claim is
equivalence of permitted observations under the same current determinants. This SADR does not prescribe one incremental
graph, equality procedure, or optimization pass.

## 4.6. Physical implementation, lifecycle and recovery — M01–M12

The following cases originate in candidate implementation techniques. The Contract-facing property remains the coherent
observation and identity guaranteed by the owner. The physical names appear here solely to identify engineering tests.

| ID  | Fault mechanism                                                             | Security consequence                                                     |
|-----|-----------------------------------------------------------------------------|--------------------------------------------------------------------------|
| M01 | A partially written physical record is read as complete.                    | An invalid constituent becomes observable.                               |
| M02 | A record and its lookup metadata are updated at different moments.          | A consumer receives incompatible descriptions of one result.             |
| M03 | A canceled allocation/append leaves auxiliary lookup entries behind.        | A failed candidate is discoverable by later work.                        |
| M04 | A retained offset or ordinal is reused for a different record.              | Old readers access the wrong subject.                                    |
| M05 | An old reader outlives the storage that supports its completed observation. | Memory safety or result integrity is violated.                           |
| M06 | Two logically separate results share a mutable physical constituent.        | Later updates change previously established meaning.                     |
| M07 | A metadata or size counter survives a failed partial update.                | Subsequent readers disagree about the available constituents.            |
| M08 | A memory reservation fails during expansion or compaction.                  | The old valid result is corrupted during unsuccessful replacement.       |
| M09 | Cleanup fails after a completed change was already accepted.                | The implementation mislabels a successful result as uncommitted or lost. |
| M10 | A crash interrupts writing a persistent representation.                     | Recovery accepts a result whose completion evidence is missing.          |
| M11 | An ABA-style handle reuse passes a weak validity test.                      | Currentness is fabricated from apparent handle equality.                 |
| M12 | Storage is reused without removing protected residual information.          | A later tenant or diagnostic can disclose prior material.                |

M01–M12 are a required implementation-assurance inventory, not a mandate for slabbing, generation numbers, persistent
arrays, full snapshots, undo logs or a particular memory API. FFM range and lifetime checks do not establish semantic
membership or current validity. Alternatives are legal when they satisfy the same properties.

## 4.7. External transactions and mixed systems — E01–E13

| ID  | Failure scenario                                                                                        | Boundary error                                                         |
|-----|---------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| E01 | Kontrakt treats a Spring annotation as proof that every involved resource has transactional protection. | Actual participant scope is unknown.                                   |
| E02 | A structured Contract failure is returned normally while the framework commits.                         | Framework rollback policy and Contract result are disconnected.        |
| E03 | External DB commits first, then a Kontrakt-owned protected change fails to complete.                    | One resource is committed and the other is not.                        |
| E04 | Kontrakt-owned state commits first, then the DB transaction aborts.                                     | Independent commit order is mistaken for atomicity.                    |
| E05 | The DB commits but its acknowledgement is lost.                                                         | Outcome unknown is falsely treated as aborted or safely repeatable.    |
| E06 | DB rollback succeeds but a mutable Java Collection modified by the method remains changed.              | External rollback is claimed to cover an unregistered JVM object.      |
| E07 | A remote HTTP request or message was already delivered before local failure.                            | Compensation is confused with isolation or atomic rollback.            |
| E08 | A compensated request later arrives and is applied again.                                               | A supposedly reversed external effect returns.                         |
| E09 | Retry repeats a non-idempotent callback or user Operation.                                              | An effect occurs twice although the logical change was requested once. |
| E10 | Spring proxy self-invocation bypasses the intended transaction interceptor.                             | Annotation presence is mistaken for actual participation.              |
| E11 | Transaction context is not propagated to a worker or reactive execution context.                        | A supposedly participating operation runs outside the transaction.     |
| E12 | A nested operation is assumed to have a savepoint where the resource does not support one.              | Partial rollback assurance is stronger than the actual resource.       |
| E13 | The integration publishes success before every required external participant confirms.                  | A local success is advertised as a distributed commit.                 |

An adapter can convey the Contract's failure outcome to an external manager without making exceptions the sole Contract
Failure representation. However, `beforeCommit` and `afterCommit` callbacks cannot by themselves turn an ordinary JVM
object and an independent database into one atomic resource. The upper integration design must advertise the real
participation and recovery level. External outcomes that cannot be determined remain indeterminate until the responsible
system resolves them.

---

# 5. Proposed Security Architecture Requirements

These are assurance requirements for review against the owning ADRs. A requirement cannot be treated as a new 1D
obligation if the owning law does not support it.

### SR-01 — Evidence must identify the selected source operation

The compiler must not infer callback behavior, partial mutation, encounter order, key discrimination or failure
reachability from a common Collection interface alone. Qualification evidence identifies the exact selected callable and
relevant platform profile under ADR-0079/0084.

### SR-02 — Source effects and Contract results remain separate

A Java/Kotlin operation may leave its mutable destination partly changed on failure. That observation must not be hidden
in source interpretation. Conversely, a failed Contract calculation cannot be presented as a successful established
result merely because the source changed an object.

### SR-03 — Completed results must be observed coherently

A consumer relying on one completed Collection result cannot receive mutually incompatible constituent relations. The
owning producer must establish a valid observation before consumption; this statement does not require a single physical
publication technique.

### SR-04 — Previous established results are not working state

New compiler work and new runtime candidate formation cannot change the established meaning of an unrelated prior
result. Reuse and structural sharing are permissible only if they preserve the previous result's legal observations.

### SR-05 — Protection claims must identify real participants

For every proposed protected Collection effect, assurance states which state is owned by Kontrakt and which external
participants actually cooperate. No claim covers arbitrary external aliases, frameworks or services by default.

### SR-06 — Pending reads cannot silently become established evidence

If a consumer depends on an observation withdrawn or invalidated by the owning change, the resulting claim must be
reconsidered under that consumer's law. The choice to cancel, recompute, revalidate or roll back is not made here.

### SR-07 — Negative observations and complete sets are real dependencies

A producer that claims absence, uniqueness, a complete grouping or a selected representative must demonstrate current
validity after relevant membership changes, including the appearance of previously unseen candidates.

### SR-08 — Semantic identity survives physical replacement

Identity and current membership must be derived from their owning semantic determinants. A stale position, reused
handle, equal value, hash or cache key cannot acquire authority through physical continuity.

### SR-09 — Partial work cannot be laundered through recovery

A crash, cancellation, resource stop, recovery process or fallback cannot turn partial Collection or compiler-product
work into a completed result. The preexisting lawful result is retained or not retained according to its own currentness
law.

### SR-10 — Reuse must be justified in the current context

Clean recomputation, cached reuse, partial repair and early cutoff must satisfy the same required legal observations.
Stored proof, historical read sets, output equality and successful prior execution are individually insufficient.

### SR-11 — Optimization must preserve mandatory observations

Fusion, specialization, vectorization, parallel reduction and equality-based rewriting must preserve mandatory
callbacks, legally observable failures, state effects, representation-independent identities and the selected Collection
result meaning. Optional source-elided calls need not be forced to execute.

### SR-12 — Resource failures preserve their actual owner

A Budget Stop, Capacity Wall, compiler memory failure and JVM `OutOfMemoryError` may arise in related work but do not
become interchangeable Contract failures. Each has its applicable measurement, owner and subsequent handling rules.

### SR-13 — External atomicity is never inferred from callbacks

A framework adapter must state whether it propagates failure, coordinates guarded changes, participates in an actual
atomic resource protocol or offers only compensation. These are materially different guarantees.

### SR-14 — Unknown external outcomes remain unknown

If an external commit or message effect cannot be confirmed or rejected, neither local rollback nor automatic retry
establishes its actual outcome. Recovery and idempotency obligations remain with the external integration design.

### SR-15 — Diagnostics remain scoped and causal

The primary failure owner should report the failed subject and relevant determinants, with provenance suited to the
permitted disclosure boundary. Full stack traversal, complete Collection dumps and internal dependency graphs are not
routine outward evidence. This requirement applies the existing Failure and Security Constitution rules rather than
replacing them.

### SR-16 — The security proof must name its model

A producer may claim integrity for a single-process V1 execution, a crash-recovery context, a concurrent reader or a
distributed external transaction only when evidence covers that model. Evidence from one context cannot silently justify
a stronger context.

---

# 6. Assurance Plan and Conformance Evidence

Security conformance is separate from declaring Contract meaning. The tests below demonstrate failure modes in candidate
implementations; passing them does not replace the owning law.

## 6.1. Required differential and fault-injection families

| Family                   | Representative injection                                                              | Expected assurance question                                                               |
|--------------------------|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| Source operations        | Interrupt `replaceAll`, `removeIf`, Kotlin `mapTo`/`groupByTo` at different elements. | Is the exact Source partial effect interpreted, and is a failed Contract result withheld? |
| Nested/aliased values    | Mutate one shared nested element after lawful acquisition.                            | Does already established meaning remain stable?                                           |
| Backed/lazy views        | Invalidate `subList`, escape an ephemeral Kotlin window, reconsume a one-shot source. | Are Source lifetime and Contract completion distinguished?                                |
| Absence/closure          | Add a newly qualifying member without changing previously read members.               | Is a uniqueness/complete-set conclusion reconsidered?                                     |
| Duplicate/representative | Remove one equal occurrence or the chosen first representative.                       | Does current membership/selection match the owning semantics?                             |
| World formation          | Fail one Definition while unrelated Definitions complete.                             | Are valid independent results preserved and invalid dependent results withheld?           |
| Protected change         | Fail after local Collection success but before a required Invariant/State judgment.   | Is local success incorrectly accepted as overall protected completion?                    |
| Dependent consumers      | Let B read a pending A and C read B; withdraw A.                                      | Are invalid derived results prevented from claiming current validity?                     |
| Concurrency              | Cancel a producer while another worker is still operating; vary completion order.     | Can a late result corrupt or impersonate a completed product?                             |
| Storage replacement      | Fail at arbitrary physical update boundaries with live readers.                       | Do all legal observations remain coherent regardless of the representation?               |
| V2 variants              | Compare clean, cache, pull, push, differential and fallback paths where supported.    | Is the exact Contract result preserved, including negative observations?                  |
| Optimization             | Rewrite/fuse a computation with a guarded throwing callback or external effect.       | Are all required source-visible conditions preserved?                                     |
| Resources                | Stop at Budget/Capacity boundaries and during cleanup.                                | Is the actual stop attributed correctly and incomplete work withheld?                     |
| Framework participation  | Return a structured failure from a Spring-managed operation.                          | Does the actual external transaction obey the declared participation guarantee?           |
| Distributed outcome      | Lose confirmation after DB commit; replay a requested effect.                         | Is outcome uncertainty preserved and duplicate effect risk handled explicitly?            |
| Recovery                 | Restart from a partially persisted result and attempt to reuse its proof.             | Does retained material still have justified current completion and assurance?             |
| Confidentiality          | Trigger errors on sensitive membership and grouping facts.                            | Are only the permitted causal facts disclosed?                                            |

## 6.2. Evidence to retain

Each test or proof must name the producer and protected result, the exact source/SDK profile where relevant, the current
Contract and validation context, the authorized observer, the injected failure, and the expected legal observation. For
reuse tests, retain enough information to distinguish the earlier result's existence from current validity. For external
integration, record the real transaction participant and the actual guarantees it advertised.

Comparison against a clean reference path is useful but not sufficient when both paths share the same incorrect source
qualification. Differential tests should include implementations with different evaluation order, caching state and,
when the source law permits, different legal tie outcomes. For resource limits, record which controller owns the limit
rather than assigning every stop to a Collection judgment.

Security reviews should also check that these tests can run with V1's replaceable material architecture and V2's
proposed incremental engines. No benchmark or memory layout is elevated to a Contract requirement.

---

# 7. Open Decisions and Ownership Handoff

The project has agreed on the A+C direction, but the following details are not closed by this SADR.

| Open decision                                            | Owning document or subsystem                        | Security review question                                                                                          |
|----------------------------------------------------------|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Explicit protected scopes at World formation             | Establishment / Governance / future Transaction law | Which independently computed results remain pending, and which already-established results cannot be revoked?     |
| Rollback propagation through direct and indirect readers | Failure / Governance / Transaction law              | When is a dependent result canceled, reconsidered or independently valid?                                         |
| Nested protection, scope escape and savepoint meaning    | Owning protection law                               | Can a narrower result be visible before a wider condition succeeds?                                               |
| Protected runtime state changes without Input or Result  | Whole Machine / State / Transaction law             | What is the actual success/commit boundary for state-only effects?                                                |
| External transaction participation API                   | Framework Integration Design                        | How are Spring, JTA, JDBC, reactive contexts and ordinary JVM effects represented without false atomicity claims? |
| Indeterminate external commit and compensation           | Transaction/Failure/Integration Design              | Which recovery and idempotency guarantees are supported?                                                          |
| Internal result validity after withdrawal                | ADR-0075 / Producer Protocol / V2 Design            | How is currentness established for directly and transitively dependent results?                                   |
| Compiler resource and retained-result safety             | Compiler Memory / Cache Design                      | How is incomplete work separated from safely reusable products?                                                   |
| Collection family semantics and legal observation        | ADR-0081 and the owning 1D                          | Which distinctions are actually part of the resulting Collection value?                                           |
| Java/Kotlin callable coverage                            | ADR-0079/0084 and SDK Qualification                 | Which exact source operations, callbacks and partial failure modes have been qualified?                           |
| Security claim validation and governance                 | Security Constitution / Verification Design         | What claims are supported by tests, independent proofs and runtime evidence?                                      |

Physical storage topics such as Primitive Slabbing, FFM arenas, indexes, epoch retirement and recovery algorithms belong
to the compiler's implementation research. They remain important test surfaces, but their names do not appear as new
Collection Contract subjects or protected semantic coordinates.

# 8. Consequences and Acceptance Conditions

This proposal makes collection-specific security review apply at both Contract compilation and runtime without claiming
that Kontrakt controls all surrounding JVM behavior. It requires a reviewer to ask not only whether an aborted
Collection was restored, but also whether another result already consumed an invalid observation. It applies the same
caution to the compiler's own reuse and incremental maintenance.

The SADR is ready for acceptance review only when: (1) every protected claim has a named owner and observation boundary;
(2) the currently supported source operations have per-callable failure qualification; (3) World formation and runtime
state-only operations have distinct completion tests; (4) directly and transitively dependent results have documented
validity handling; (5) implementation candidates pass coherent-observation and failure-injection tests; (6) framework
integration advertises only its actual guarantees; and (7) missing rollback/scope semantics are resolved by their
rightful ADRs rather than silently assumed here.

Until then, this document records the threat model, proposed assurance requirements, and outstanding work. It does not
turn source-language effects, internal optimizations, storage layouts or external transaction capabilities into Contract
Authority.

# 9. Sources and Evidence Limits

**Project sources:** the current project ZIP's Accepted ADRs and Security Constitution, the existing SADR-0001, the
available ADR-0081 Draft, the V1/V2 incremental TODO, and the two project research PDFs named above. The research PDFs
are engineering references, not adopted requirements. The designated 243-line D08 revised ADR-0081 file was not
available in this session; the accessible 393-line project ZIP Draft was used for source comparison. This affects
textual reconciliation, not the authority of the Accepted ADRs.

**External comparison leads, not adopted algorithms:** PostgreSQL's transaction isolation and predicate dependencies;
FoundationDB's optimistic transactions, conflict ranges and uncertain commit results; RocksDB's atomic write batches;
Spring Framework's transaction propagation and synchronization APIs; Clojure STM's guarded updates; Buck2 DICE and
Salsa's incremental reuse; and DBSP's differential relational computation. These systems illuminate failure modes, but
their behavior does not define Kontrakt's contracts. Particular API versions, performance claims and newer research
results require independent confirmation before being used as implementation selection evidence.

**Evidence status:** This SADR is an architectural review derived from the documents and discussion. It is not a
completed proof, fault-injection report, full source/SDK conformance run, or verification of the current compiler's
runtime protections.
