# Kontrakt Security Constitution

## Status

Accepted

## Date

2026-09-30

## Role

This document defines the security obligations that Kontrakt must preserve as a compiler and Contract Machine
implementation.

This is a Constitution document. It defines the security properties and boundaries that must remain true across valid
architectures, and it records the states that a valid implementation must never enter.

The Constitution deliberately stops before mechanism choice. It does not decide the compiler's stage topology or IR
representation. Storage and caching remain implementation concerns. Isolation, repository enforcement, cryptographic
choice, and verification tooling also remain below this level.

Architecture decisions that realize this Constitution belong in the independent Security ADR series, numbered
`SADR-xxxx`. Concrete mechanisms belong in Security Design and implementation documents.

`What Contract Is` and the Contract Constitution continue to own Contract meaning. This Security Constitution does not
create another Contract authority.

---

## 0. Intention

Kontrakt is intended to make obligations explicit instead of letting implementation accidents decide what the machine
means. Security has to follow the same discipline.

A secure compiler is not defined by the presence of one familiar defense. An edge validator may be useful, as may
process isolation or a release checklist, but none of those mechanisms is the security model by itself.

Security is a property of the whole machine. Source first acquires an interpretation, after which Contract meaning is
established and realization is examined. The compiler then derives knowledge and may transform its representation before
producing artifacts or retaining results for later reuse.

Security obligations continue across those boundaries. Diagnostics can expose information while explaining a failure,
and release artifacts may later run in an environment that no longer matches the one in which they were produced. A
local component can therefore behave reasonably while a property is lost in the transition to the next responsibility.

The security model therefore starts with obligations that survive replacement of the mechanism.

Kontrakt must make the boundary of each security claim explicit. That includes what the machine may reveal and which
changes to meaning are permitted. The claim must also identify the work the machine promises to remain able to perform,
the authority on which a decision depends, and the evidence that justifies relying on it.

The purpose is not to claim protection against every possible attacker or physical effect. Such a claim would say almost
nothing. Kontrakt instead requires claims narrow enough to test, assumptions visible enough to challenge, and
transitions explicit enough that a later architecture cannot weaken the property by accident.

When a security claim depends on an adversary or fault model, that model is part of the claim. Anything the model leaves
out remains an excluded condition or an unresolved assumption; omission is not evidence that the capability cannot
exist.

A subsystem may refine a broader model for the responsibility it owns. It must not, however, narrow the conditions of an
upstream claim without making that change explicit.

---

# 1. Constitutional Position

## 1.1. Security Does Not Own Contract Meaning

A Contract obligation remains owned by the Contract authority that declares and establishes it. Security does not gain
the right to redefine that obligation because a security review discovers a dangerous architecture or because an
implementation mechanism would be easier to secure under different semantics.

Security may determine that a realization is unacceptable when the declared Contract cannot be implemented without
violating a constitutional security obligation. In that case the compiler may have to reject the realization. If the
contradiction comes from the declared semantics themselves, the owning Contract ADR must be revisited rather than
reinterpreted inside security architecture.

It may not silently patch Contract meaning.

Trust placed in implementation or assurance material does not give that material Contract authority. A verifier result
or security label remains evidence about another responsibility.

Retained compiler state does not cross that boundary either. Cache state and diagnostics may support later work, while
build attestations, signatures, and source review can support assurance claims. Runtime representations, generated
artifacts, and platform decisions may also be trusted for a limited purpose. None of them acquires Contract authority
from that trust.

This follows the same line already used throughout Kontrakt:

```text
Contract obligation
    constrains
security-preserving realization

security evidence
    supports a claim about realization
    does not become Contract authority
```

If this Security Constitution and the Contract Constitution require incompatible things, the conflict is constitutional.
It must be resolved at the constitutional level or by changing the affected Contract law. A Security ADR is not an
escape hatch from that conflict.

## 1.2. Security Is Normative Without Becoming Architecture

This Constitution may state precisely what the compiler must preserve. Ordinary output equality, for example, is not
enough to justify a transformation that changes protected meaning. Stored verification evidence does not stay current
merely because it remains available, and internal compiler material does not become externally observable just because a
diagnostic path can reach it.

Those are constitutional obligations because removing them changes the security property of the machine.

How those obligations are proved or enforced remains below the constitutional level. The Constitution does not fix the
number of IRs, the placement of a verifier, or the representation of cache state. It also does not choose process
isolation, memory-publication machinery, or repository review enforcement. Those choices remain replaceable architecture
or implementation.

The dividing test is the same one used by `What Contract Is`:

```text
If replacing the mechanism while preserving the obligation is legal,
the mechanism does not belong in the Constitution.
```

## 1.3. Security Applies to More Than Hostile Input

An adversary is not required for a security property to be broken.

Security can fail without a deliberate attacker. A parser may be exploited by hostile source, but a compiler bug can
also bind the same name incorrectly on its own. Maintenance and reuse create similar risks: a compromised maintainer can
add a backdoor, while an ordinary refactor can invalidate a verifier assumption or a stale cache can replay an old
result.

Concurrency and diagnostics introduce different failure paths. A race can expose partially published material, and an
otherwise legitimate diagnostic path can reveal information that was never meant to leave its original boundary.

When the same protected property can be broken by malice or by fault, the property remains a security concern in both
cases. Threat modeling and fault modeling may use different evidence, but they do not create two different meanings of
integrity or confidentiality.

---

# 2. What Security Means in Kontrakt

In Kontrakt, security means preserving four things across the lifetime of compiler material and products: information is
used only as authorized, protected state is changed only under valid authority, supported operation remains dependable
within its declared envelope, and trust claims remain justified.

The protected subject is broader than an end-user secret. Contract definitions and Established Meaning can be
security-sensitive because corrupting them changes what the machine believes. State, Transition, occurrence attribution,
Failure origin, and outward claims can matter for the same reason.

Compiler-produced knowledge has its own risks. Verification results and diagnostic evidence may affect later decisions,
while source provenance and build inputs determine what a release claim can legitimately say. Emitted or retained
artifacts, release metadata, and credentials that carry operational authority can therefore require protection even when
none of them contains a conventional secret.

One piece of material may need more than one kind of protection. A diagnostic record, for example, needs integrity if it
must identify the correct occurrence. The same record may need confidentiality when it contains sensitive context, and
an evidence-retention obligation can also make its availability relevant for a defined lifetime.

Security requirements must therefore name the property they protect instead of relying on the word `secure` as a global
label.

Kontrakt uses Confidentiality, Integrity, and Availability as the top-level description of what is protected.
Authentication, Authorization, and Auditing describe how security-relevant action is attributed, permitted, and made
accountable.

Those concepts establish the vocabulary but do not complete the compiler security model. Compilation repeatedly changes
representation and may retain or reuse derived knowledge before the resulting machine executes. Security law must
therefore say when those transformations preserve the property being claimed.

Every security property also needs an observation boundary. Integrity and confidentiality are meaningless against an
undefined observer, and ordinary output equality does not make two executions security-equivalent.

The claim must identify the observations that matter to it. An externally visible action or failure may be relevant even
when the returned value is unchanged. Timing, resource behavior, and other effects belong in the claim when the
protected property depends on those distinctions.

Threat and fault models qualify a security claim; they do not redefine the underlying property. Integrity can be
evaluated while assuming a benign platform, under hostile input, or against a compromised build chain. Those assumptions
produce claims of different strength.

Kontrakt must not present a result proved under one model as if it had also survived a stronger model that was never
analyzed.

---

# 3. Confidentiality

Confidentiality means that information is disclosed only under an authorization that actually covers that disclosure.

Classification comes first. Kontrakt cannot rely on developers to infer which compiler material is sensitive from
implementation context.

External information does not acquire redistribution permission merely by entering the compiler. Until an explicit
policy, Contract authority, or other legitimate disclosure rule widens the observation scope, the implementation must
preserve the narrower boundary. Material created by Kontrakt can require the same protection even when no outside party
supplied it.

Implementation access and disclosure authority are different things. Material does not become publishable because an
object can reach it, because it can be serialized, because it has been retained, or because a debugger can inspect it.

Derived information follows the same rule. Replacing source material with a summary or fingerprint does not declassify
it. Even a count or membership fact can reveal protected structure.

Other derived forms can expose different parts of the source relation. A hash may reveal equality, a proof result can
reveal whether a condition held, and a cache record or failure reason can expose existence or policy outcome. Provenance
may reveal still more about origin and timing. A derived value therefore needs its own disclosure justification even
when it carries less information than its source.

Kontrakt must therefore preserve this distinction:

```text
information exists
    ≠
information may be observed

information was derived
    ≠
information was declassified
```

Contract Publication has a specific semantic role: it determines which authoritative Contract material may become an
outward Contract claim. That decision does not automatically expose the compiler representation behind the claim.

The same separation applies in the opposite direction. Logging, debugging, telemetry, profiling, crash reporting, and
build metadata are not Contract Publication, but renaming an output channel does not exempt it from confidentiality
review.

A confidentiality claim must state which observation channels it covers. Protection of stored data does not
automatically make a statement about timing or resource use. Allocation patterns, error distinctions, cache behavior,
and microarchitectural effects likewise remain outside the claim unless they are modeled.

When one of those side channels matters for a supported use case, it needs an explicit property with assumptions and
preservation obligations of its own.

Disclosure can occur without returning protected data directly. Distinct error classes or refusal reasons may reveal
whether protected state exists. Retry behavior and resource consumption can become similar oracles when an observer can
correlate them with that state.

Such channels are not automatically part of every confidentiality claim. When the claim depends on them, however, the
relevant distinction must be named and preserved.

Kontrakt must minimize unnecessary disclosure, but least information is not a license to remove information required for
another declared obligation. Confidentiality is preserved by exposing no more than is justified, not by destroying
evidence or Contract meaning that the machine is required to retain.

---

# 4. Integrity

Integrity means that protected material keeps the meaning, judgment, authority, and provenance that legitimately belong
to it. It also means that the material is not reused after the conditions that made those properties valid have ceased
to hold.

For Kontrakt, byte immutability is only one possible implementation fact. Identical bytes can be invalid when consumed
under the wrong Version or Policy World. They can also be stale for a different occurrence, realization binding,
compiler generation, source revision, or security assumption.

Conversely, recomputation can preserve integrity even when the object address, storage location, or physical encoding
changes.

The governing question is not whether the representation stayed the same. It is whether the exact protected meaning and
its validity conditions stayed true.

A judgment must apply to the material that was actually judged. A later consumer must not substitute a materially
different representation relation after the judgment and still rely on the old result. If a transformation changes a
distinction that the judgment depended on, the result must be re-established or otherwise shown to remain valid under
the new relation.

Compiler representability does not make material authoritative. Tentative or recovered material remains below that
boundary, as does material that is poisoned, speculative, incomplete, or only partially constructed.

Candidate material and Established Material therefore remain different security states. Merely failing to observe an
error does not establish anything.

Freshness becomes part of integrity whenever validity depends on changing context. Time may matter, as may Version,
generation, current Policy, or current State.

Authenticity does not cancel staleness. A correctly signed old artifact is still old, and verification performed against
an earlier realization does not validate its replacement. State movement authorized from one source State also loses
that basis when the source changes unless the owning law explicitly permits reuse.

A security decision must stay coherent with the action that relies on it. If the relevant State or Policy can change
between checking and use, the later action must remain bound to the checked context. The same rule applies when
generation or realization is part of the decision basis.

Otherwise the decision has to be established again. Validity does not cross a time-of-check/time-of-use gap by default.

Several individually valid pieces do not necessarily form a valid whole. Material from different generations can
conflict even when each record is authentic in isolation. The same problem arises across revisions, Policy Worlds, or
trust epochs.

Publication and consumption must therefore establish compatibility for the combination, not only validity of each
component.

Provenance is protected when another security decision relies on it. A result that claims a particular origin must
remain bound to that origin, whether the origin is source, compiler, build, reviewer, authority, or occurrence.

Provenance does not create the meaning it describes. False provenance is still dangerous because it can cause the
machine or an operator to trust the wrong material.

Integrity evidence over bytes is only as strong as the interpretation of those bytes. A signature or hash can
authenticate an encoding while two consumers still derive different security-relevant structures from it.

Whenever authenticity or provenance is used to justify meaning, the relying context therefore needs an unambiguous
interpretation relation.

Physical properties cannot silently stand in for semantic integrity. Object or class identity may be useful
implementation evidence, but they are not enough by themselves. The same restriction applies to addresses and paths,
query reachability, cache presence, generated types, and backend shape.

Any such property is sufficient only when an explicit semantic or security rule says so.

---

# 5. Availability

Availability means that Kontrakt remains capable of delivering the operations and security properties it claims under
its declared operating conditions and finite resource limits.

Kontrakt is finite, and its availability model has to say so. Frontend work consumes resources before any Contract
meaning can be established, while later analysis and verification can amplify a small input into much larger work.

The same risk exists in diagnostic production, specialization, persistence, artifact generation, and retained compiler
state. A finite input surface is therefore not enough if the architecture allows the work behind it to grow without a
bound.

Availability includes resistance to both hostile and accidental amplification. An accepted workload must have a
defensible resource envelope for the work it can trigger. Memory and storage count as part of that envelope, as do
retained evidence and externally induced activity.

The Constitution does not fix the numerical budgets or their enforcement mechanism. It does require the architecture to
avoid hiding unbounded work behind a surface that appears finite.

Resource exhaustion is not a semantic answer. A timeout cannot be translated into Contract success or Contract refusal
merely because computation stopped. Cancellation and unavailable infrastructure are subject to the same restriction.

Verifier exhaustion, memory failure, and similar stops likewise cannot fabricate Established Material or another meaning
that was never established.

Availability does not mean unconditional uptime. Refusing an unsupported program can be correct, as can stopping before
a declared resource bound is exceeded. A realization whose security cannot be justified may also have to be rejected.

Such refusal keeps the machine inside its supported operating envelope instead of preserving apparent progress by
silently weakening the guarantee.

Security mechanisms are themselves subject to availability. A protection that disappears whenever it becomes expensive
is not a dependable protection, but one that makes supported work impossible is not a workable design either.

If the architecture provides a fallback, that path needs a guarantee that is valid for the fallback itself. It cannot
continue to use the name or evidence of a stronger guarantee that no longer holds.

---

# 6. The Gold Standard

Confidentiality, Integrity, and Availability describe the properties Kontrakt protects. Authentication identifies the
actor when identity matters to a decision. Authorization determines whether the protected action is permitted, and
Auditing preserves the evidence needed for later accountability.

These responsibilities must remain conceptually separate even when one implementation mechanism participates in more
than one of them.

## 6.1. Authentication

Authentication establishes the identity of a principal when a security decision depends on who or what is acting.

A principal is whatever acts in a security-relevant capacity. A human user can be a principal, but so can a service or
process. Tools, build systems, and automated agents may also act as principals when a decision depends on their
identity.

That notion is separate from Contract semantic identity. It is also separate from the identity of an artifact and from
provenance that records where material came from.

```text
principal authentication
    ≠ Contract semantic identity
    ≠ artifact identity
    ≠ provenance
```

These relations may be linked, but one cannot stand in for the others without an explicit rule.

Successful authentication says who acted; it does not say that the action was good or permitted. A contributor account
can be genuine while the proposed change is malicious, and a genuine release signer may still lack authority over a
particular artifact.

Artifact identity creates a different relation. Exact digest equality does not identify the producing principal, just as
exact Contract Definition identity does not require a human principal to participate in its meaning.

When a security decision needs actor identity, ambiguous or unbound identity must not be treated as a successful
authentication merely because the requested action otherwise looks valid.

## 6.2. Authorization

Authorization determines whether an authenticated principal, or another explicitly qualified security subject, may
perform a protected action or observation.

Authentication grants no permission by itself, and one permission does not imply another. Implementation capability is
not permission either.

A process may physically read a file without being authorized to disclose it. A plugin may reach a compiler object
without owning the authority represented there. The same principle applies when a maintainer can propose a change or
when a service possesses a signing credential: adjacent capability does not widen the granted authority.

Authorization applies wherever a protected action needs permission. That can include observing or modifying material,
causing execution, approving a change, publishing or releasing an artifact, or performing an administrative action. It
may also restrict who is allowed to cause a security-sensitive transition.

Security authorization cannot define Contract meaning. It may govern who is allowed to invoke or operate the machinery
that realizes a Contract, but the meaning of a Fact still belongs to its Contract authority. State existence,
established Failure, and Publication meaning remain under their own owning laws as well.

The default must be denial where a required authorization cannot be established. That denial remains a security or
implementation result unless an owning Contract law explicitly gives it Contract meaning.

Every protected action or observation needs authorization for the subject and action actually being performed under the
context that matters to the decision. A previous check does not authorize a later action after that relation has
materially changed.

Alternate implementation paths do not inherit permission from the ordinary path. Debug facilities and caches remain
subject to the same rule, as do direct storage access, native or reflective paths, recovery machinery, and
administrative tooling.

Delegation may narrow authority. It must not widen authority unless an independent rule grants the additional power. A
more privileged component acting on behalf of a less privileged requester must remain bound to the requester's
authorized intent and must not use ambient authority to perform adjacent work that the request did not justify. This
protects Kontrakt from confused-deputy behavior without requiring one particular capability mechanism.

## 6.3. Auditing

Auditing preserves reliable evidence when later accountability depends on knowing what happened. The same evidence may
also support incident analysis, trust review, or another form of retrospective inspection.

An audit record remains evidence rather than authority. Recording an action does not make it legal, and recording a
Contract judgment does not establish that judgment. A release record likewise cannot prove that the released material
was benign or correct.

Audit evidence has security requirements of its own. Its integrity must be protected because later conclusions may
depend on it.

Confidentiality still applies when the evidence contains protected context. An audit purpose does not justify retaining
secrets or private source material that it does not need, nor does it justify exposing internal topology merely because
the audit system can record it.

Auditing must be sufficient for the accountability Kontrakt claims, but that does not require recording every material
access. Indiscriminate logging can itself create confidentiality or availability failures.

Architecture must therefore decide which security-relevant events need durable or reviewable evidence and what
assumptions are made about the trustworthiness of that evidence.

Audit evidence should preserve the distinction between authentication and authorization. A bare statement that an action
was permitted loses important context.

Where accountability requires it, the record should be able to relate the action to the acting principal, the permission
relied upon, the affected subject, and the security context in which the decision was made.

---

# 7. Trust Is Scoped

Kontrakt must not use `trusted` as an unqualified global property.

Trust is always scoped to a subject, a property, and the assumptions under which the claim is made. For example,
Kontrakt can rely on the JVM to preserve specified JVM semantics without treating the JVM as Contract authority. A
verifier can be trusted for a supported IR subset without being assumed sound for transformations it does not model.

The same distinction applies outside the compiler core. A build service may provide trustworthy provenance without
deciding whether the source is benign, and subsystem review authority does not automatically include release authority.

Every meaningful security claim must therefore be capable of answering these questions:

```text
What subject is protected?
What property is claimed, and which observations define it?
Against what adversary or fault model?
Under what assumptions and validity context does the claim hold?
What evidence justifies the claim?
What happens when required evidence or assumptions are unavailable?
```

A claim may also need to identify channels it deliberately excludes and the conditions that invalidate it. Words such as
`secure`, `verified`, `trusted`, or `hardened` are therefore incomplete unless the missing scope is supplied.

The trusted computing base is property-specific for the same reason. Kontrakt should reduce the amount of machinery
whose correctness must simply be assumed, while remaining explicit about the assumptions that cannot be removed.

Formal proof does not eliminate every dependency. Neither do isolation, cryptography, memory safety, or independent
validation. Their value includes making the remaining trust smaller and easier to inspect.

Assumptions are dependencies rather than footnotes. If an assurance result relies on another claim, that relation must
be visible enough to invalidate the result when its support disappears.

The supporting condition might come from the platform or from verifier semantics. It may instead be a credential, a
source-governance rule, or a build property. Whatever the source, a retained security label must not outlive the
assumption that justified it.

Local assurance does not compose automatically into assurance of the whole system. Two components can each satisfy a
correct local claim while their interface breaks the combined property. Shared state can create the same problem, as can
assumptions that disagree across the boundary.

A cross-component claim is justified only when the guarantees and assumptions at that boundary are compatible under the
same observation and threat model.

Security must survive public knowledge of the mechanism. Because Kontrakt is open source, an attacker may know how its
enforcement works and may inspect the architecture in detail.

Secrets still remain secret. Private keys and credentials are obvious examples, and confidential runtime material may be
another. The design itself, however, cannot function as a hidden key.

Sharing physical machinery does not merge trust domains. Several responsibilities may legally use the same cache or
registry, for example, without gaining access to one another's authority. The same applies to shared scheduling,
logging, process boundaries, or storage.

Any shared mechanism must preserve the original limits on observation and authority. It must also avoid widening failure
propagation or trust merely because the implementation is common.

---

# 8. Information, Observation, and Disclosure

Security-sensitive observation is a security-authorization boundary even when no Contract meaning changes. It is not
Contract Publication and does not create another Contract authority.

A consumer is entitled only to the observation that its declared purpose requires. Needing one derived relation does not
grant visibility into the full material family from which that relation was produced.

The rule applies to user-facing output and diagnostics, but it also governs compiler analyses and verification. Build
metadata, administrative tooling, and a future remote or multi-tenant service must establish their observation authority
on the same basis.

Observation authority does not grow merely because implementation reachability grows. A consumer given a narrow view
cannot use a backing pointer or global registry to reconstruct producer authority. Provenance internals and debug
indexes cannot be used as an accidental path around the same boundary.

If a later responsibility genuinely requires a broader view, that broader observation has to be authorized in its own
right.

Internal compiler material is non-exportable by default, and that rule reaches beyond ordinary user secrets.
State-machine evidence or occurrence attribution can expose internal meaning. Rejected candidates and compiler
dependency relations can reveal structure that was never intended as an outward interface.

Verification detail, optimization knowledge, provenance structure, and cache metadata can create similar disclosure or
attack surfaces. Calling such material internal rather than secret does not make export harmless.

Public Contract meaning can still have a private compiler representation. Making a State visible through the Contract
does not expose how the compiler stores that State or what revision history it retained. Rejected transition evidence
and synchronization metadata remain internal unless another rule authorizes their disclosure.

The same holds for an outward result. Publication of the result does not publish the proof that justified it, the cache
entry that retained it, the source path that produced it, or diagnostic internals associated with it.

Declassification must be explicit about both purpose and scope. Formatting does not imply it, and neither does hashing
or aggregation. Truncation, serialization, or transport into another subsystem are also insufficient to establish
permission to disclose.

Security ADRs may establish the architecture that mediates disclosure. This Constitution only requires that no
implementation path obtain disclosure authority by accident.

---

# 9. Meaning, Judgment, Context, and Freshness

Kontrakt is a compiler for explicit Contract meaning. Security must preserve the relation between a judgment and the
exact material that justified it.

Each security-relevant judgment boundary must have one authoritative interpretation of the material it judges. Once raw
or serialized input has been parsed, resolved, and used to support a judgment, a later subsystem cannot reinterpret the
same bytes under different rules and simply inherit that judgment.

If reparsing can produce a materially different security interpretation, that reparse forms a new judgment boundary and
needs its own validity basis.

A result is valid only for the subject and context under which it was established or verified. If the security-relevant
determinants change, the result cannot be reused merely because its stored representation still matches or because a
cache lookup succeeds.

This rule applies wherever later use depends on security-sensitive validity. Contract judgment and compiler verification
are obvious cases, but optimization knowledge and authorization can also depend on changing context. Build evidence has
the same problem.

The determinant set is owned by the semantic or security responsibility that produces the result. The Constitution
therefore does not impose one universal validity key.

Stable or authentic encoding is not the same thing as unambiguous meaning. Canonical bytes and deterministic encoding
can stabilize a representation, while hashes and signatures can provide evidence about those bytes.

None of those mechanisms defines Contract Canonicalization by itself. The relying consumer still needs the
interpretation and context to which the evidence applies.

A later representation may erase distinctions only after those distinctions are proven irrelevant to every obligation
that still depends on them. If an earlier judgment distinguished values that a later representation merges, or a later
consumer distinguishes values that an earlier judgment treated as equivalent, the architecture must show that judgment
and use remain coherent.

Implementation convenience cannot repair a missing semantic relation. A lookup that happens to return the current or
nearest match is not a substitute for the relation the law requires. Choosing the latest version or a reachable object
has the same problem.

Cache presence and physical adjacency are implementation facts as well. They cannot replace an exact required relation
merely because preserving that relation is expensive.

History is protected from retroactive reinterpretation. New material may justify a later decision, but it cannot change
what an earlier occurrence meant. It also cannot rewrite the State that was observed at that time, the source revision
that was reviewed, or the evidence on which the earlier claim depended.

---

# 10. Compiler Preservation Law

A compiler is allowed to change representation aggressively. It is not allowed to change protected meaning or a security
property that it claims to preserve.

The preservation law follows compiler work even when the implementation reorganizes that work. It begins when source is
parsed and resolved, continues through semantic representation and lowering, and remains in force while analysis or
optimization changes the program.

Backend transformation and generated products do not end the obligation. It continues to the runtime handoff even if the
physical stage boundaries are redesigned.

Ordinary functional equality is not a general security proof. A transformation can return the same value while changing
where a Failure is attributed or when an effect occurs. It may also change information flow even if no ordinary result
differs.

Timing, resource amplification, and speculative behavior create further distinctions. Whenever one of those properties
is part of the supported security claim, preservation of the ordinary value is insufficient and the additional property
must be justified separately.

The same principle works in the other direction. Kontrakt is not required to claim every possible security property for
every program. A property that is not supported must be excluded explicitly rather than implied by broad language such
as `secure compilation`.

Security stated at source level does not automatically survive compilation. Writing code in a constant-time style, for
example, does not prove that the generated target remains constant-time.

The same caution applies to effects. Their absence in source does not rule out behavior introduced by a framework
callback, native boundary, runtime transformation, or backend substitution. A claim that crosses lowering or another
target boundary extends only as far as its preservation argument and platform assumptions.

Executable behavior can only be claimed over the closure that the security argument actually covers. Native code
reachable from the program must either be modeled or explicitly excluded. Dynamic loading and reflection create the same
requirement, as do runtime callbacks and generated code.

External processes or other opaque execution remain outside the claim unless the analysis and assumptions bring them
inside. Unknown executable behavior is not evidence that the unknown part is harmless.

Recovery and speculation do not receive special authority. Recovered syntax or a guessed binding may help the compiler
continue, but neither can support a stronger claim than the available evidence. Profile information and heuristic
ranking are subject to the same limit.

Optimization may speculate about performance. It cannot speculate about Contract truth or security authorization and
later publish that speculation as fact.

A concrete realization should not introduce a protected violation that was absent from the abstract obligation it claims
to realize. Where a target environment has stronger observation or attack capabilities than the source model, the
security claim must account for that difference or narrow its scope.

Preservation claims are specific to both the property and the adversary model. Kontrakt does not have to promise that
every source property survives every target context.

When a claim does cross lowering, linking, runtime integration, or interaction with hostile surrounding code, however,
the target-side observation power must be stated clearly enough to match the property being claimed at the source
boundary.

---

# 11. Verification and Assurance

Verification is scoped evidence about a property. It is not a universal stamp of safety.

A verifier must state the property it checks and the subject over which the check is valid. Its model and assumptions
are part of the claim.

If input lies outside the supported model, verification has not succeeded. The same is true when analysis is incomplete,
a solver returns unknown, the verifier times out or crashes, or relevant target behavior was deliberately excluded.

A proof cannot be stronger than the model it proves. Refinement of a model does not show that the model captured every
security-relevant channel or platform behavior, and it says nothing about attacker capability that the model excluded.

Assurance therefore includes the adequacy of the model and the binding from that model to the actual artifact or
execution, even when the proof itself is machine-checked.

Verification evidence remains subject to integrity and freshness. A proof that applied to one program version can become
irrelevant after the program changes. The same can happen when the Contract World, platform profile, compiler semantics,
or realization binding changes.

Retention of the proof does not preserve its validity.

Kontrakt should minimize the trusted base of important security conclusions. A complex analyzer may produce useful
evidence without every line of that analyzer needing to become an unquestioned root of trust if a smaller independent
condition can validate the conclusion. The exact architecture belongs in SADRs and Design, but the constitutional
obligation is to understand where trust actually resides.

Dependencies moved outside a verifier remain part of the assurance argument. If the result relies on another checker or
on a certificate format, those elements contribute assumptions that the relying claim must carry.

A translator or model generator can introduce the same dependency, as can runtime enforcement. Moving such work out of
the core verifier may reduce its trusted base, but it does not make the dependency disappear.

Independent validation helps because two paths can otherwise share the same mistake. Independence does not require
complete duplication of the implementation; it requires enough separation that a critical conclusion is not accepted
merely because the same faulty rule ran twice under different names.

Whenever independence supports a security claim, the assumptions still shared by the validation paths must remain
visible.

Verification claims compose only when their scopes compose. The subjects and contexts have to be compatible, and the
same is true of assumptions and guaranteed properties.

`Verified A` together with `verified B` therefore does not prove `verified A+B` by itself. A downstream consumer also
cannot retain an upstream result after changing a determinant that the upstream proof treated as fixed.

Security evidence can come from many kinds of analysis. Formal proof may be appropriate for one property, while
differential testing or fuzzing may be more useful for another. Runtime checks, reference paths, and external
attestations can also contribute.

None of those forms is universally sufficient. A test corpus does not define the Contract, a formal model is not
identical to the running machine, and a reference path that depends on the same faulty helper is not independent merely
because it sits in another package.

The stronger the security claim, the more precisely its evidence boundary must be stated.

---

# 12. Failure, Uncertainty, and Recovery

Security-sensitive uncertainty must remain uncertainty until an owning rule resolves it.

A missing or corrupted basis cannot silently become success. Unsupported behavior and inconclusive verification remain
unresolved rather than being promoted to a stronger result. Stale evidence is subject to the same rule.

Resource exhaustion or a crash also cannot create meaning that was never established, and interrupted Publication does
not become complete by convention. `Fail closed` means withholding the stronger permission or claim when its required
basis is absent; it does not authorize the compiler to invent a Contract Failure.

Different stops may have different meanings even when the implementation uses common control flow to handle them.
Contract refusal is not automatically the same as compiler failure, and a security denial differs from an unsupported
capability.

Resource stops, crashes, and verifier inconclusive results may also need to remain distinct. Their differences must
survive whenever later reasoning depends on them.

Recovery cannot promote partial work into final work. A generation that was only partly constructed remains incomplete,
and a proof that did not finish is not a proof. The same condition applies to an artifact that was only partly written
or an update interrupted before its completion rule was satisfied.

Recovery must preserve historical integrity. It may reconstruct or discard material when the owning rule allows that
response, and it may retry work. An explicitly supported fallback may also be legal.

What recovery cannot do is rewrite an earlier occurrence or hide the loss of an assurance condition. Derived knowledge
that depended on the failed state cannot be reused unless its validity still has a justification after the failure.

A fallback path is a real path with a real guarantee. It is not a quiet downgrade.

---

# 13. Reuse, Caching, Incremental Work, and Determinism

Reuse changes work. It does not change authority.

Reuse may avoid recomputation only while the reused result remains valid. That applies whether the retained material is
a cache entry or persistent record and whether the avoided work would otherwise be local, incremental, parallel, remote,
or cut off early.

Storage presence is therefore insufficient. Hash equality or the fact that the result succeeded previously cannot by
themselves justify reuse.

Security validity can depend on inputs that leave ordinary output bytes unchanged. A verifier upgrade can invalidate old
evidence without changing generated code. Changes to security policy or trust roots can do the same.

Realization capability and disclosure rules may also be determinants. Source-governance conditions and platform
assumptions must be treated likewise when the result relied on them.

Kontrakt remains deterministic-first. When a semantic or security decision is declared deterministic for a set of
determinants, its result cannot depend on how workers happened to run or which cache entries were already present.

Traversal order and physical address cannot become hidden inputs. Neither can unrelated process state or an accidental
wall-clock value.

This does not prohibit security mechanisms that require entropy. Cryptographic randomness and similar security inputs
may be necessary. They must remain explicit security inputs to the mechanism and must not silently become determinants
of Contract meaning.

A faster execution path must preserve the required result for the same validity context. Caching and incremental repair
are subject to that rule, as are parallel or remote execution.

The Constitution does not require a particular reference architecture or equality algorithm. It requires every optimized
path to preserve the observable and security properties that the corresponding clean or reference computation is
supposed to establish.

---

# 14. Source, Build, Release, and Supply-Chain Integrity

Kontrakt is an open-source compiler. The integrity of the compiler users run is part of the security model.

A release moves through several distinct subjects before a user executes it. The source revision may differ from the
revision that was actually reviewed. Build inputs include more than source, and generated material or dependencies may
enter before the toolchain produces an artifact.

Signing and publication add further relations, and the installed executable is yet another subject. Security arguments
must not collapse those stages into interchangeable names.

When release assurance depends on a build, the claim must account for every build input that can materially affect the
artifact or its security evidence. Authored source is only one such input. Generated material and packaging logic can
change the result, as can plugins or dependencies.

Toolchains and configuration also matter when they influence the output. Environment-sensitive inputs need the same
treatment. Anything the assurance argument cannot bind or account for remains an explicit assumption or gap rather than
disappearing merely because it sits outside the source tree.

A release claim must bind the artifact to the material and process on which that claim relies. Review of a repository
revision does not show that an independently prepared archive contains only the reviewed material.

Other forms of evidence prove different things. A valid signature authenticates bytes but does not make them benign.
Provenance can describe origin without proving source correctness, and reproducibility can faithfully reproduce
malicious source. Pinning a dependency identifies what was used; it does not establish that the dependency is
trustworthy.

Provenance becomes useful only in relation to an expectation. Knowing how an artifact was produced does not say whether
that process was authorized or current unless the relying policy defines what provenance it requires.

Authenticity of individual pieces is also insufficient. Source, metadata, and an artifact may each be genuine while
still failing to form a release state that was ever valid as a whole.

The following distinctions are constitutional:

```text
authentic
    ≠ authorized
    ≠ reviewed
    ≠ provenance-known
    ≠ reproducible
    ≠ benign
    ≠ secure
```

Each fact can strengthen an assurance case. None silently proves the others.

Open-source contribution does not create ambient trust. Permission to propose a change is different from authority to
approve or merge it. Security policy and release machinery require their own authority, as do signing and publication.

Identity does not remove the risk. A legitimate account can be compromised, and a trusted contributor can still act
maliciously. Correct authentication of a maintainer therefore cannot by itself establish that the proposed change is
safe.

Kontrakt must separate security-relevant authorities when combining them would let one mistake or compromise defeat the
protection. Exactly how contributor roles and review are enforced belongs in SADRs and Design. The same is true for
release procedure, repository controls, and credential handling.

Compromise must be recoverable without falsifying history. The project needs to revoke affected authority and determine
which artifacts depended on it. Vulnerable releases then need correction, and users need enough security information to
understand the change.

Removing an old source branch or artifact does not rewrite what was already distributed.

---

# 15. Open Design and Open Source

Kontrakt being open source is not itself a security guarantee and is not a security weakness by definition.

Open design assumes that an attacker can study how Kontrakt works. Source code and architecture may be read in detail,
including verifier behavior and public build logic. Error behavior may also be studied.

A protection that depends on those details remaining obscure is not a valid constitutional basis.

Public design makes external review possible, but review remains evidence rather than proof. A widely visible repository
is not automatically trustworthy, and neither project age nor contributor count turns unknown behavior into a verified
property.

Open design does not require secrets to become public. Credentials and private keys remain confidential. User material
can require the same treatment, and vulnerability details may justifiably remain private for a limited period when
coordinated handling depends on that confidentiality.

Users should be able to understand the security claim they are relying on. That requires visible limitations and
assumptions as well as the claim itself. When a vulnerability is corrected, the security-relevant change should also be
communicated clearly.

A broad `secure` label cannot be used to hide a known limitation merely because no exploit is currently public.

---

# 16. Secure Defaults and User Burden

Kontrakt must not make its own security guarantees depend on users discovering hidden hardening steps that the product
could reasonably enforce itself.

A supported default path should preserve the security properties Kontrakt claims for that path. If a user deliberately
selects an operating mode with weaker guarantees, the weaker scope must be explicit and must not retain the language or
evidence of the stronger mode.

This principle does not transfer application security policy to Kontrakt. When the Contract model assigns a policy
decision to the application, the application continues to own it.

Kontrakt does own the security consequences of its compiler behavior and defaults. The same responsibility covers
generated products, the claims made by verification, and the release process through which users receive the compiler.

A user should not need to understand internal security architecture merely to keep ordinary compiler diagnostics from
disclosing protected material. Nor should a publicly claimed guarantee depend on an undocumented release flag that users
are expected to infer.

If a compatibility mode deliberately weakens the guarantee, it must not silently present itself as equivalent to the
secure supported path.

Security belongs in the product obligation, not in folklore.

---

# 17. Lifecycle, Change, and Revocation

Security claims have lifetimes.

Security claims change over time because their determinants change. A new source revision can replace an old one, and
credentials can lose authority through revocation. A verifier model can also evolve until an earlier proof no longer
applies.

Persistent technical state has similar lifecycle risks. A cache schema can become unsafe, a dependency can later be
found vulnerable, and an artifact can remain authentic while no longer satisfying current policy.

Retention and validity are different. Keeping material does not keep its authority, freshness, or assurance current.
Deleting material does not rewrite a historical fact that was validly established or an event that actually occurred.

Freshness may require explicit anti-rollback and anti-mix-and-match protection. When current acceptance depends on a
newer trusted state, an older artifact cannot regain current status merely because its signature still verifies. The
same applies to an old key set, policy, or metadata state.

Anti-mix-and-match protection is needed for the same reason. Authenticity of the individual pieces is not enough when
those pieces were never jointly valid.

Where continued security depends on receiving newer security metadata or a vulnerability correction, indefinite
withholding can be an availability or freshness failure rather than a neutral absence of change. The exact update
mechanism belongs below the Constitution, but the security claim must say whether freshness is part of acceptance.

When a determinant of a security claim changes, the claim has to be reconsidered. Recalculation may be appropriate, or
the architecture may invalidate or migrate the affected material. Some cases require reverification or retirement
instead.

Cost is not a justification for preserving a security label whose basis no longer holds.

Revocation changes future acceptance or authority under the applicable security policy. It does not falsify history.
Incident response needs both facts: what was trusted at the time, and what is no longer trusted now.

If a claim depended on authority that has been revoked, its derived assurance may need to be reconsidered as well.
Invalidated assumptions propagate in the same way when certificates, caches, attestations, or other results relied on
them.

That propagation must remain scoped to the dependency. Revocation does not erase unrelated Contract meaning merely
because one physical artifact happened to carry both.

Security engineering continues through update, migration, deprecation, and retirement. A system is not secure only at
the instant it is released.

---

# 18. Constitutional Prohibitions

The following are not valid sources of security authority in Kontrakt.

Implementation reachability is not permission to observe or modify protected material, and cache presence does not
create authority. Generated APIs likewise remain products rather than Contract authority, while a successful parse
remains below Establishment.

Assurance evidence has its own limits. Verifier success cannot be extended beyond the property and model actually
checked. A signature says nothing by itself about benign semantics, and provenance does not prove correctness.
Reproducibility cannot establish trustworthiness, source review cannot cover material that never passed through that
review, authentication cannot replace authorization, and an audit record cannot legalize the action it records.

Interpretation and use must remain bound to the conditions that justified them. A security-sensitive parser result
cannot simply survive a materially different reinterpretation of the same bytes. Authorization likewise cannot be
checked so early that the relevant context changes before use without being noticed.

Coherence also applies across retained material. Records from incompatible generations do not form one valid state
merely because each is valid alone. Narrow observation and delegated authority cannot be widened through ambient
implementation access. Old but authentic artifacts do not become current on authenticity alone, and executable behavior
omitted from analysis cannot be called safe merely because it was unknown.

Kontrakt must fail closed when a required security condition is unknown or unsupported, but it must do so without
inventing unrelated Contract meaning. Information cannot be silently declassified merely because it was transformed or
hashed. Summarization and movement into another product do not grant disclosure authority either.

Security-sensitive results cannot be reused outside their validity context. Optimization and lowering remain subject to
the same restriction, as do caching and incremental work. Backend convenience, runtime substitution, or adoption of
external technology cannot weaken a claimed property unless the claim is correspondingly changed or rejected.

Security architecture must not redefine Contract meaning to make enforcement easier. Contract architecture must not
assume that implementation security will repair an obligation whose semantics are internally contradictory.

No SADR or Design document may declare an exception to these rules merely because the preferred mechanism cannot satisfy
them. If the obligation is wrong, the Constitution must change explicitly. If the obligation is right, the architecture
must change or the unsupported path must be rejected.

---

# 19. Security Documentation Authority

Kontrakt keeps security decisions in a document family separate from existing Contract and compiler ADR numbering.

Security Architecture Decision Records use the identifier form:

```text
SADR-0001
SADR-0002
...
```

The separate numbering does not create a second semantic universe. It keeps security architecture decisions traceable
without pretending that they own Contract meaning.

A Security ADR applies this Constitution to a concrete architectural subject. It can assign responsibility or define a
trust boundary when the constitutional obligation requires one. It may also state the relation among security-sensitive
material, the supported scope of a claim, or the behavior required when assurance fails.

Those decisions can impose architectural requirements, including requirements on assurance. They may refer to Contract
and compiler ADRs, but the Security ADR cannot duplicate or replace the meaning owned by those documents.

Security Design realizes one or more SADRs through replaceable mechanisms. A design may choose a data structure or
algorithm, and it may decide how storage is organized. Cryptographic protection and isolation mechanisms also belong at
this level.

The same is true of verifier implementation, repository or CI enforcement, release tooling, platform facilities, and JVM
configuration. Those choices remain replaceable as long as the constitutional and SADR obligations continue to hold.

The relation is:

```text
Security Constitution
    defines security obligations

SADR
    chooses architecture that satisfies those obligations

Security Design
    chooses mechanisms that realize the architecture

Implementation / Verification / QA
    provides the running system and evidence
```

Security review can reveal that an existing Contract or compiler ADR cannot satisfy this Constitution. Such a finding
has to return to the document that owns the conflicting meaning or architecture. Creating shadow semantics in a SADR is
not a valid repair.

Research remains non-normative, including *Modern Security Architecture*. It can reveal failure modes and challenge an
assumption, and it can provide evidence for changing a design or law. Authority changes only when the appropriate
Constitution, ADR, SADR, or Design document adopts the result at the level it owns.

---

# 20. Constitutional Review Test

A proposed security rule belongs in this Constitution only when it survives replacement of the current architecture and
implementation.

The review question is:

```text
If Kontrakt changed its IR family, query engine, cache design, JVM strategy,
repository provider, verification tool, build system, or storage mechanism,
would violating this rule still make the machine less secure in the same way?
```

If the answer is yes, the rule may be constitutional.

A rule belongs below the Constitution when it chooses the mechanism rather than the enduring security obligation. Which
component performs a check is an architectural decision. So is the protocol field that carries evidence or the table
that stores it.

Tool choice is also below this level. The Constitution should not select the signing tool, branch-control setting, or
blocked syscall used to satisfy a broader obligation.

Compiler-specific language is appropriate when it names an obligation that should survive architectural replacement.
Requiring a transformation to preserve an in-scope security property is therefore constitutional; naming the
optimization pass that runs a validator is not.

The same boundary applies to reuse and releases. Cached security evidence must remain valid for its current context, but
the Constitution need not prescribe the cache key. Released artifacts must remain traceable to the source and build
inputs that justify the release claim, while the particular provenance format remains a lower-level choice.

This boundary is part of the Constitution's own discipline. Security should not become another place where
implementation leaks upward and hardens into authority.
