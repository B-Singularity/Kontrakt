# Kontrakt Security Constitution

## Status

**Draft — not yet accepted.**

## Date

2026-09-30

## Role

This document defines the security obligations that Kontrakt must preserve as a compiler and Contract Machine implementation.

It is a Constitution document. It defines properties, boundaries, and prohibitions that must remain true across valid architectures and implementations. It does not prescribe a compiler stage topology, an IR layout, a storage engine, a cache key, a sandbox mechanism, a repository setting, a cryptographic primitive, or a specific verification tool.

Architecture decisions that realize this Constitution belong in the independent Security ADR series, numbered `SADR-xxxx`. Concrete mechanisms belong in Security Design and implementation documents.

`What Contract Is` and the Contract Constitution continue to own Contract meaning. This Security Constitution does not create another Contract authority.

---

## 0. Intention

Kontrakt is intended to make obligations explicit instead of letting implementation accidents decide what the machine means. Security has to follow the same discipline.

A secure compiler is not one that has a validator at the edge, a sandbox around a process, or a security checklist attached before release. Those things may be useful mechanisms. None of them is the security model.

Security is a property of the whole machine. Source is interpreted, Contract meaning is established, realization is inspected, compiler knowledge is derived, transformations change representation, products are emitted, results are reused, diagnostics explain failures, artifacts are built, and releases are consumed later under environments that may no longer match the conditions under which they were produced. A security property can be lost at any of those transitions even when every local component appears reasonable.

The security model therefore starts with obligations that survive replacement of the mechanism.

Kontrakt must know what information it is allowed to reveal, what meaning it is allowed to preserve or change, what work it is obliged to remain able to perform, what identities and authorities a security decision depends on, what evidence supports a security claim, and where that claim stops.

The purpose is not to claim that Kontrakt is secure against every possible attacker or physical effect. That would be meaningless. The purpose is to make every security claim narrow enough to be testable, every trust assumption visible enough to be challenged, and every security-sensitive transition explicit enough that a later architecture cannot weaken it by accident.

Where a security claim depends on an adversary or fault model, that model is part of the claim. An attacker capability, failure mode, or observation channel omitted from the model is an excluded condition or unresolved assumption, not evidence that the capability cannot exist. A subsystem may refine a broader model for its own responsibility, but it may not silently narrow the conditions under which an upstream security claim was made.

---

# 1. Constitutional Position

## 1.1. Security Does Not Own Contract Meaning

A Contract obligation remains owned by the Contract authority that declares and establishes it. Security does not gain the right to redefine that obligation because a security review discovers a dangerous architecture or because an implementation mechanism would be easier to secure under different semantics.

Security may constrain whether a realization is acceptable. It may show that a current Contract composition cannot be implemented without violating a constitutional security obligation. It may require the compiler to reject an unsupported realization. It may require an owning Contract ADR to be revisited when the declared semantics themselves create an unresolved contradiction.

It may not silently patch Contract meaning.

A verifier result, security label, cache record, build attestation, signature, source review, diagnostic record, runtime object, generated artifact, operating-system decision, or hardware property does not establish Contract authority merely because Kontrakt trusts it for another purpose.

This follows the same line already used throughout Kontrakt:

```text
Contract obligation
    constrains
security-preserving realization

security evidence
    supports a claim about realization
    does not become Contract authority
```

If this Security Constitution and the Contract Constitution require incompatible things, the conflict is constitutional. It must be resolved at the constitutional level or by changing the affected Contract law. A Security ADR is not an escape hatch from that conflict.

## 1.2. Security Is Normative Without Becoming Architecture

This Constitution may be specific about what a compiler must preserve. A compiler transformation must not change protected meaning merely because the transformed program still returns the same ordinary value. A stale verification result must not become current merely because it is still stored. Internal compiler material must not become externally observable merely because a diagnostic formatter can reach it.

Those are constitutional obligations because removing them changes the security property of the machine.

The Constitution does not decide how the compiler proves those facts. It does not decide how many IRs exist, where a verifier runs, how a cache is indexed, which process is isolated, how memory is published, or how a repository enforces code review. Those decisions remain replaceable architecture or implementation.

The dividing test is the same one used by `What Contract Is`:

```text
If replacing the mechanism while preserving the obligation is legal,
the mechanism does not belong in the Constitution.
```

## 1.3. Security Applies to More Than Hostile Input

An adversary is not required for a security property to be broken.

A malicious source file may exploit a parser. A compiler bug may misbind the same name without an attacker. A compromised maintainer may introduce a backdoor. A harmless refactor may accidentally invalidate a verifier assumption. A stale cache may replay an old result. A data race may expose partially published material. A diagnostic path may print data that no one intended to disclose.

When the same protected property can be broken by malice or by fault, the property remains a security concern in both cases. Threat modeling and fault modeling may use different evidence, but they do not create two different meanings of integrity or confidentiality.

---

# 2. What Security Means in Kontrakt

Security is the preservation of authorized information use, authorized change, dependable operation, and justified trust across the complete lifetime of Kontrakt material and products.

The protected assets are not limited to end-user secrets. Contract definitions, established meaning, State and Transition meaning, occurrence attribution, failure origin, outward claims, compiler knowledge, verification results, source provenance, diagnostic evidence, build inputs, emitted artifacts, retained results, release metadata, and authority-bearing operational credentials can all be security-sensitive for different reasons.

The same material can be an asset under more than one property. A diagnostic record may require integrity because it must explain the correct occurrence, confidentiality because it may contain sensitive context, and availability because a declared evidence obligation may require it to remain accessible for a defined lifetime.

Security requirements must therefore name the property they protect instead of relying on the word `secure` as a global label.

Kontrakt adopts the classic Confidentiality, Integrity, and Availability model as the top-level statement of what is protected. Authentication, Authorization, and Auditing form the corresponding Gold Standard for controlling and accounting for security-relevant action. These six ideas are a starting vocabulary, not the end of the architecture. Compiler security requires additional preservation rules because meaning and evidence are transformed, reused, lowered, cached, and emitted before the machine finally runs.

A security property also needs an observation boundary. Integrity or confidentiality cannot be preserved against an undefined observer, and two executions are not security-equivalent merely because their ordinary outputs match. The claim must make clear which actions, outputs, failures, timing or resource observations, and external effects are in scope whenever those distinctions matter to the property.

Threat and fault models qualify claims rather than redefining them. The same integrity property can be evaluated under a benign-platform assumption, a malicious-input model, or a compromised-build model, but the strength of the claim changes with those assumptions. Kontrakt must not present a claim made under one model as though it survived a stronger model that was never analyzed.

---

# 3. Confidentiality

Confidentiality means that information is disclosed only under an authorization that actually covers that disclosure.

The first obligation is classification. Kontrakt must not depend on developers guessing which material is sensitive. Externally supplied information does not acquire redistribution permission merely because it entered the compiler. Until an explicit policy, Contract authority, or other legitimate disclosure rule establishes a wider observation scope, the implementation must not broaden the audience or channel through which that information is exposed. Information created inside Kontrakt may also require confidentiality even when no external party supplied it directly.

Availability to the implementation does not imply permission to disclose. Reachability does not imply permission to disclose. Serialization does not imply permission to disclose. Retention does not imply permission to disclose. Debugger visibility does not imply permission to disclose.

The same rule applies to derived information. A summary, hash, count, fingerprint, graph, proof result, cache entry, failure reason, branch fact, or provenance relation does not become public merely because it contains less information than its source. Derivation is not declassification. A derived value may reveal the existence, structure, membership, timing, policy outcome, or other property of protected material even when the original value is absent.

Kontrakt must therefore preserve this distinction:

```text
information exists
    ≠
information may be observed

information was derived
    ≠
information was declassified
```

Contract Publication has a specific semantic role: it decides which authoritative Contract material may become an outward Contract claim. That authority does not make every compiler representation, diagnostic record, State-machine detail, provenance edge, verifier fact, cache entry, trace, or debug artifact associated with the published meaning public. Conversely, an implementation channel that is not Contract Publication does not escape confidentiality review merely because it is called logging, debugging, telemetry, profiling, crash reporting, or build metadata.

Confidentiality claims must state the observation channels they cover. A claim about stored information does not silently include timing, resource use, allocation behavior, error distinctions, cache-hit behavior, microarchitectural effects, or other side channels that were not modeled. If a side-channel property matters for a supported use case, it must be stated as a separate property with its own assumptions and preservation obligations.

Observation includes more than successful data return. Error classes, refusal reasons, existence checks, retry behavior, resource consumption, and other oracle-like distinctions can disclose protected information when an observer can correlate them with protected state. They are not automatically in scope, but a confidentiality claim that depends on them must name and preserve the relevant distinction.

Kontrakt must minimize unnecessary disclosure, but least information is not a license to remove information required for another declared obligation. Confidentiality is preserved by exposing no more than is justified, not by destroying evidence or Contract meaning that the machine is required to retain.

---

# 4. Integrity

Integrity means that protected information, meaning, judgment, authority, and provenance are not changed, substituted, widened, narrowed, or reused outside the conditions under which they are valid.

For Kontrakt, integrity is broader than byte immutability. An unchanged byte sequence can be wrong if it is used under the wrong Version, Policy World, occurrence, realization binding, compiler generation, source revision, or security assumption. A recomputed object can be correct even when its address, storage location, or physical encoding changed completely.

The governing question is not whether the representation stayed the same. It is whether the exact protected meaning and its validity conditions stayed true.

A judgment must apply to the material that was actually judged. A later consumer must not substitute a materially different representation relation after the judgment and still rely on the old result. If a transformation changes a distinction that the judgment depended on, the result must be re-established or otherwise shown to remain valid under the new relation.

Tentative, recovered, poisoned, speculative, incomplete, or partially constructed material does not become authoritative because the compiler can represent it. Candidate material and Established Material remain different security states. The absence of a detected error is not Establishment.

Freshness is part of integrity whenever time, version, generation, current policy, current State, or another changing context affects validity. Authentic old material is still old material. A valid signature on a stale artifact does not make the artifact current. A verifier result proven against an earlier realization does not validate a replacement realization. A previously authorized State movement does not remain authorized after its source State changes unless the owning law explicitly permits that reuse.

Integrity of a security decision includes coherence between the decision and the action it authorizes or justifies. If a relevant State, policy, generation, realization, or other determinant can change between check and use, the later action must still be bound to the context that was checked or the decision must be established again. A valid decision is not portable across a time-of-check/time-of-use gap by default.

A result assembled from several individually valid pieces is valid only if their compatibility is also established. Mixing material from different generations, revisions, worlds, or trust epochs can create a state that was never valid as a whole. Coherent publication and consumption therefore protect combinations as well as individual records.

Integrity also protects provenance when provenance is used as evidence. A result that claims to come from a particular source, compiler, build, reviewer, authority, or occurrence must remain bound to that origin. Provenance does not create the meaning it describes, but false provenance can cause the machine or its operators to trust the wrong thing.

Integrity evidence over an encoding is not stronger than the interpretation of that encoding. Signed, hashed, or otherwise authenticated bytes do not establish one intended meaning if different legal consumers can parse those bytes into materially different security-relevant structures. Where authenticity or provenance is used to justify meaning, the interpretation relation must be unambiguous for the relying context.

No physical property may silently substitute for semantic integrity. Object identity, memory address, class identity, file path, query reachability, cache presence, generated type, or backend shape can be implementation evidence only where an explicit security or semantic rule says it is sufficient.

---

# 5. Availability

Availability means that Kontrakt remains capable of delivering the operations and security properties it claims under its declared operating conditions and finite resource limits.

Kontrakt is not an infinite machine. Security must not pretend otherwise. Parsing, resolution, establishment, analysis, verification, diagnostics, transformation, persistence, and artifact production all consume bounded resources. An input that is small in bytes may still expand into unbounded graph work, diagnostics, specialization, proof search, cache growth, or other computation if the architecture permits uncontrolled amplification.

Availability therefore includes resistance to adversarial and accidental amplification. The machine must have a defensible account of the work, memory, storage, retained evidence, and external activity that an accepted workload can induce. The exact budgets and enforcement mechanisms belong below the Constitution, but unbounded work hidden behind a finite declared surface is not a valid security design.

A resource stop is not a semantic answer. Timeout, memory exhaustion, cancellation, unavailable infrastructure, verifier resource exhaustion, and similar conditions must not fabricate Contract success, Contract refusal, Established Material, or any other meaning they did not establish.

Availability is also not absolute uptime. A compiler may legitimately refuse an unsupported program, stop before exceeding a declared bound, or reject a realization whose security cannot be justified. Such behavior preserves availability honestly by remaining inside the system's supported operating envelope instead of continuing under a weaker hidden guarantee.

Security mechanisms themselves are subject to availability. A protection that can be bypassed whenever it becomes expensive is not a protection. A protection that makes all supported work impossible is not useful engineering. Where a fallback exists, the fallback must carry a guarantee that is explicit and valid for that path; it must not quietly keep the name of a stronger guarantee that no longer holds.

---

# 6. The Gold Standard

Confidentiality, Integrity, and Availability describe what must be protected. Authentication, Authorization, and Auditing describe how security-relevant action is bound to actors, permission, and accountability.

These responsibilities must remain conceptually separate even when one implementation mechanism participates in more than one of them.

## 6.1. Authentication

Authentication establishes the identity of a principal when a security decision depends on who or what is acting.

A principal is an actor capable of security-relevant action: a person, service, process, tool, build system, automated agent, or another acting entity. Authentication is not the same thing as Contract semantic identity. It is also not the same thing as artifact identity or provenance.

```text
principal authentication
    ≠ Contract semantic identity
    ≠ artifact identity
    ≠ provenance
```

These relations may be linked, but one cannot stand in for the others without an explicit rule.

A contributor account may be authenticated while the proposed change is malicious. A release signer may be authenticated while the signer lacks authority to release that artifact. A compiler artifact may have an exact digest while its producing principal is unknown. A Contract Definition may have exact semantic identity without any human principal being involved in its meaning.

When a security decision needs actor identity, ambiguous or unbound identity must not be treated as a successful authentication merely because the requested action otherwise looks valid.

## 6.2. Authorization

Authorization determines whether an authenticated principal, or another explicitly qualified security subject, may perform a protected action or observation.

Authentication alone grants no permission. Possession of one permission grants no unrelated permission. Physical capability grants no permission. A process that can read a file, a plugin that can reach a compiler object, a maintainer who can propose a change, or a service that holds a signing credential does not thereby gain every authority adjacent to that capability.

Authorization may govern observation, modification, execution, approval, publication, release, administration, or other protected actions. It may restrict who can cause a security-sensitive transition or who can observe protected material.

Security authorization does not create Contract authority. It can decide who may invoke, operate, or influence machinery that realizes a Contract, but it cannot decide what a Fact means, what State exists, what Failure was established, or what Publication means unless the owning Contract law separately establishes that meaning.

The default must be denial where a required authorization cannot be established. That denial remains a security or implementation result unless an owning Contract law explicitly gives it Contract meaning.

A protected action or observation must be mediated by an authorization that covers the exact subject, action, and relevant context. A prior check does not authorize a materially different later action, and an alternate path through debugging, caching, direct storage access, native code, reflection, recovery, or administrative tooling does not inherit permission merely because the ordinary path was authorized.

Delegation may narrow authority. It must not widen authority unless an independent rule grants the additional power. A more privileged component acting on behalf of a less privileged requester must remain bound to the requester's authorized intent and must not use ambient authority to perform adjacent work that the request did not justify. This protects Kontrakt from confused-deputy behavior without requiring one particular capability mechanism.

## 6.3. Auditing

Auditing preserves reliable evidence of security-relevant action when later inspection, accountability, incident analysis, or trust review requires it.

An audit record is evidence, not authority. Recording that an action occurred does not make the action legal. Recording a Contract judgment does not establish the judgment. Recording a release does not prove that the release was benign or correct.

Audit evidence must itself satisfy integrity requirements. Where confidentiality applies, the evidence must not defeat the property it is meant to protect by retaining secrets, raw private material, unnecessary internal topology, or other information that the audit purpose does not require.

Auditing must be strong enough to support the accountability Kontrakt claims, but the Constitution does not require indiscriminate logging of every material access. Excessive logging can create a new confidentiality and availability failure. The architecture must identify which security-relevant events require durable or reviewable evidence and what trust is placed in that evidence.

Authentication and authorization decisions should remain distinguishable in audit evidence. A record that says only that an action was permitted is weaker than a record that can show who acted, what permission was relied upon, what subject was affected, and under what security context the action occurred.

---

# 7. Trust Is Scoped

Kontrakt must not use `trusted` as an unqualified global property.

Trust is always trust in a subject for a property under assumptions. A JVM may be trusted to preserve specified JVM semantics while not being trusted to enforce Contract authority. A verifier may be trusted for a particular IR subset while being unsupported for interprocedural transformation. A build service may be trusted to produce provenance while not being trusted to decide whether the source is benign. A maintainer may be trusted to review one subsystem without holding release authority.

Every meaningful security claim must therefore be capable of answering these questions:

```text
What subject is protected?
What property is claimed, and which observations define it?
Against what adversary or fault model?
Under what assumptions and validity context does the claim hold?
What evidence justifies the claim?
What happens when required evidence or assumptions are unavailable?
```

Where relevant, the claim must also state excluded channels and invalidation conditions. `Secure`, `verified`, `trusted`, `hardened`, and similar words are not complete claims by themselves.

A trusted computing base is likewise property-specific. Kontrakt should minimize the elements whose correctness must be assumed for a security claim, but it must not pretend that formal proof, sandboxing, cryptography, memory safety, or independent validation removes all assumptions. Strong assurance is useful partly because it can make the remaining assumptions smaller and clearer.

Assumptions are dependencies, not footnotes. If one assurance claim relies on another claim, platform property, credential, verifier semantics, source-governance condition, or build property, the dependency must be explicit enough that loss of the supporting condition can invalidate the dependent claim. A retained security label must not outlive the assumption that made it justified.

Local assurance does not automatically compose into system assurance. Two components can each satisfy their own claims while their interfaces, shared state, or mismatched assumptions violate the combined property. A cross-component claim requires the guarantees and assumptions at the boundary to be compatible under the same observation and threat model.

The security of a mechanism must not depend on attackers being unable to inspect its design. Kontrakt is open source, and its security model must tolerate public knowledge of its algorithms, architecture, and enforcement strategy. Secrets such as private keys, credentials, and secret runtime material remain secret; the mechanism itself does not become a secret key.

Physical sharing also does not merge trust domains. A common cache, registry, scheduler, log sink, process, or storage region may be a legal implementation, but shared machinery must not silently widen observation, authority, failure propagation, or trust merely because several responsibilities use it.

---

# 8. Information, Observation, and Disclosure

Security-sensitive observation is a security-authorization boundary even when no Contract meaning changes. It is not Contract Publication and does not create another Contract authority.

A consumer does not gain the right to observe a whole material family merely because it legitimately needs one relation derived from that family. Observation should be limited to what is necessary for the declared purpose. This principle applies to user-facing output, diagnostics, compiler analyses, verification, build metadata, administrative tooling, and future remote or multi-tenant services.

Observation authority is monotonic unless a separate owning rule grants more. A consumer given a narrow observation must not reconstruct a broader observation or producer authority by following backing pointers, global registries, provenance internals, debug indexes, or other implementation reachability. If a later responsibility legitimately needs broader information, that broader access must be established as its own authorized relation.

Internal compiler material is non-exportable by default. That rule is broader than user secrets. Internal State-machine evidence, occurrence attribution, rejected candidates, compiler dependency relations, verification details, optimization knowledge, provenance graphs, cache metadata, and other internal structure can expose protected information or attack surface even when none of their fields is called a secret.

A legitimate public fact can have a private representation. A Contract-visible State does not make the compiler's State storage, revision history, rejected transition evidence, or synchronization metadata public. A published result does not make the compiler's proof, cache entry, source path, or diagnostic internals public.

Declassification, where needed, must be explicit in purpose and scope. It cannot be inferred from formatting, hashing, aggregation, truncation, serialization, or transport to a different subsystem.

Security ADRs may establish the architecture that mediates disclosure. This Constitution only requires that no implementation path obtain disclosure authority by accident.

---

# 9. Meaning, Judgment, Context, and Freshness

Kontrakt is a compiler for explicit Contract meaning. Security must preserve the relation between a judgment and the exact material that justified it.

Security-relevant material must also have one authoritative interpretation at each judgment boundary. Once raw input or serialized material has been parsed, resolved, and judged, a later subsystem must not reparse the raw representation under different rules and inherit the earlier judgment as though nothing changed. If reparsing can produce a new security-relevant interpretation, it is a new judgment boundary and requires its own validity basis.

A result is valid only for the subject and context under which it was established or verified. If the security-relevant determinants change, the result cannot be reused merely because its stored representation still matches or because a cache lookup succeeds.

This applies to Contract judgment, compiler verification, optimization knowledge, authorization, build evidence, and other security-sensitive results. The exact determinant set is owned by the corresponding semantic or security responsibility; the Constitution does not prescribe one universal key.

Canonical bytes, deterministic encoding, hashing, and signatures may make a representation stable or authentic. They do not by themselves define Contract Canonicalization or eliminate semantic ambiguity. The relying consumer still needs the interpretation and context to which the security evidence applies.

A later representation may erase distinctions only after those distinctions are proven irrelevant to every obligation that still depends on them. If an earlier judgment distinguished values that a later representation merges, or a later consumer distinguishes values that an earlier judgment treated as equivalent, the architecture must show that judgment and use remain coherent.

The machine must not repair a missing semantic relation with implementation convenience. Current lookup, nearest match, latest version, reachable object, cache hit, or physical adjacency cannot replace an exact required relation merely because the exact relation is expensive to preserve.

History also matters. Later material may justify later decisions. It does not retroactively rewrite what an earlier occurrence meant, which State was observed, which source was reviewed, or which evidence supported an earlier claim.

---

# 10. Compiler Preservation Law

A compiler is allowed to change representation aggressively. It is not allowed to change protected meaning or a security property that it claims to preserve.

This obligation applies across parsing, resolution, semantic representation, lowering, analysis, optimization, backend transformation, generated products, and runtime handoff. The physical boundaries may change over time. The preservation obligation does not.

Functional output equality is not a universal security proof. A transformation can preserve ordinary return values while changing failure attribution, evaluation order, externally visible effects, information flow, timing behavior, resource amplification, or speculative behavior. When one of those properties is within the supported security claim, it must be preserved separately.

The same principle works in the other direction. Kontrakt is not required to claim every possible security property for every program. A property that is not supported must be excluded explicitly rather than implied by broad language such as `secure compilation`.

Source-level security does not automatically survive compilation. A source pattern intended to be constant-time does not prove target-level constant-time behavior. A source-level absence of an effect does not prove that a framework callback, native boundary, runtime transformation, or backend substitution cannot introduce one. If Kontrakt claims a property across a lowering or target boundary, that claim extends only as far as the preservation argument and its platform assumptions extend.

A claim about executable behavior must also account for the executable closure that can affect the property. Reachable native code, dynamic loading, reflection, runtime callbacks, external processes, generated code, or other opaque behavior must either be covered by the analysis and assumptions for the claim or remain an explicit excluded boundary. Unknown executable behavior is not evidence of harmless behavior.

Compiler recovery and speculation are subject to the same law. Recovered syntax, guessed binding, speculative optimization, profile information, heuristic ranking, or generated convenience material cannot gain stronger authority than the evidence supports. An optimization may speculate about performance. It may not speculate about Contract truth or security authorization and then publish the speculation as fact.

A concrete realization should not introduce a protected violation that was absent from the abstract obligation it claims to realize. Where a target environment has stronger observation or attack capabilities than the source model, the security claim must account for that difference or narrow its scope.

The preservation obligation is property-specific and adversary-specific. Kontrakt need not promise robust preservation of every source property against every target context, but when it claims that a property survives lowering, linking, runtime integration, or hostile surrounding code, the target observation power used by that claim must be at least as explicit as the source-side property being preserved.

---

# 11. Verification and Assurance

Verification is scoped evidence about a property. It is not a universal stamp of safety.

A verifier must define what it checks, over what subject, with what model, and under which assumptions. Unsupported input, incomplete modeling, timeout, solver unknown, verifier crash, or an excluded target behavior cannot be reinterpreted as successful verification.

Verification strength cannot exceed the model it verifies. A proof that an implementation refines a model does not establish that the model captured every relevant channel, platform behavior, or attacker capability. Model adequacy and the binding from the verified model to the actual artifact or execution are part of the assurance argument even when the proof itself is machine-checked.

Verification results are subject to integrity, context, and freshness. A proof or certificate that was valid for one program version, Contract World, platform profile, compiler semantics version, or realization binding may be invalid for another. Storage does not preserve validity by itself.

Kontrakt should minimize the trusted base of important security conclusions. A complex analyzer may produce useful evidence without every line of that analyzer needing to become an unquestioned root of trust if a smaller independent condition can validate the conclusion. The exact architecture belongs in SADRs and Design, but the constitutional obligation is to understand where trust actually resides.

A verification result that depends on another checker, certificate format, translator, model generator, or runtime enforcement inherits the relevant assumptions of those elements. Moving complexity outside a verifier can reduce the trusted base, but it does not make the displaced dependency disappear.

Independent validation is valuable because common-mode failure is real. Independence is not absolute duplication. It means that a critical conclusion should not be accepted solely because the same mistaken rule was executed twice under different names. Where independent validation is part of a claim, its shared assumptions must remain visible.

Verification claims compose only when their subjects, contexts, assumptions, and guaranteed properties compose. `Verified A` and `verified B` do not by themselves prove `verified A+B`, and a downstream claim must not reuse an upstream verification result after changing a determinant that the upstream proof treated as fixed.

Proofs, reference paths, differential tests, fuzzing, runtime checks, formal models, and external attestations can all contribute evidence. None is automatically sufficient for every property. A test corpus is not a Contract definition. A formal model is not the running machine. A reference implementation that calls the same faulty helper is not independent merely because it lives in another package.

The stronger the security claim, the more precisely its evidence boundary must be stated.

---

# 12. Failure, Uncertainty, and Recovery

Security-sensitive uncertainty must remain uncertainty until an owning rule resolves it.

Missing material, corrupt state, unsupported features, inconclusive verification, stale evidence, resource exhaustion, crash, interrupted publication, and similar conditions must not silently become success. `Fail closed` means that a stronger permission or claim is not granted without its required basis. It does not mean inventing a Contract Failure that no Contract authority established.

Contract refusal, compiler failure, security denial, unsupported capability, resource stop, crash, and verifier inconclusive may have different meanings. The implementation may map them to common physical control flow, but their semantic and security distinctions must not be erased where later reasoning depends on them.

Partial work does not become final work because recovery is inconvenient. A partially constructed generation, incomplete proof, half-written artifact, or interrupted update must not be consumed as if its completion condition had been satisfied.

Recovery must preserve historical integrity. It may reconstruct material, discard material, retry work, or fall back to a weaker supported operation when an explicit rule permits it. It may not rewrite an earlier occurrence, hide that an assurance condition was lost, or reuse derived knowledge whose validity depended on the failed state without a justification that survives the failure.

A fallback path is a real path with a real guarantee. It is not a quiet downgrade.

---

# 13. Reuse, Caching, Incremental Work, and Determinism

Reuse changes work. It does not change authority.

A cached result, retained analysis, persistent record, incremental repair, parallel computation, remote result, or early-cutoff decision may avoid recomputation only while the validity relation of that result still holds. Reuse is not justified by storage presence, hash equality, or previous success alone.

Security-sensitive validity may depend on inputs that do not change the ordinary output bytes. A verifier version, security policy, trust root, realization capability, disclosure rule, source governance condition, or platform assumption can invalidate a security result even when generated code happens to remain identical.

Kontrakt is deterministic-first. For the same declared determinants, a semantic or security decision that is required to be deterministic must not change because of worker scheduling, cache history, traversal order, memory address, unrelated process state, wall-clock accident, or another hidden input.

This does not prohibit security mechanisms that require entropy. Cryptographic randomness and similar security inputs may be necessary. They must remain explicit security inputs to the mechanism and must not silently become determinants of Contract meaning.

An optimized, cached, incremental, parallel, or remote path must preserve the required observable and security properties of the corresponding clean or reference computation for the same validity context. The Constitution does not require one reference architecture or one equality algorithm. It requires that optimization never be a license to weaken the result.

---

# 14. Source, Build, Release, and Supply-Chain Integrity

Kontrakt is an open-source compiler. The integrity of the compiler users run is part of the security model.

Source revision, reviewed revision, build input, generated source, dependency, toolchain, built artifact, signed artifact, published release, and installed executable are related subjects. They are not interchangeable names for one thing.

When release assurance depends on a build, the effective build-input closure must cover every input that can materially influence the claimed artifact or its security evidence. This can include authored source, generated material, build and packaging logic, plugins, dependencies, toolchains, configuration, and environment-sensitive inputs. An input that cannot be bound or accounted for is an explicit assumption or assurance gap; it is not invisible merely because it was not in the source tree.

A security claim about a released compiler must be able to bind the artifact to the actual material and process on which the claim depends. A reviewed repository revision does not prove that an independently prepared release archive contained only that revision. A valid signature does not prove that the signed bytes are benign. Provenance does not prove that the source was correct. Reproducibility does not make malicious source safe. Pinning a dependency does not make that dependency trustworthy.

Provenance is useful only relative to an expectation. Knowing how an artifact was produced does not establish that the process was authorized, current, or acceptable unless the relying policy states what provenance is required. Likewise, individually authentic source, metadata, and artifacts do not form a trustworthy release when they were never valid together as one build or release state.

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

Open-source contribution is not ambient trust. The ability to propose a change does not imply authority to approve, merge, alter security policy, modify release machinery, sign, or publish. A legitimate account can be compromised. A trusted contributor can act maliciously. A correctly authenticated maintainer can still propose a harmful change.

Kontrakt must therefore preserve separation between security-relevant authorities where collapsing them would allow one mistake or compromise to defeat the protection. The exact contributor roles, review rules, release process, repository controls, and credential mechanisms belong in SADRs and Design.

The project must be able to revoke compromised authority, identify affected artifacts, correct vulnerable releases, and communicate security-relevant changes without pretending that deletion of a source branch or old artifact rewrites what was already distributed.

---

# 15. Open Design and Open Source

Kontrakt being open source is not itself a security guarantee and is not a security weakness by definition.

The security design must assume that an attacker can read the source, understand the architecture, inspect the verifier, study error behavior, and reproduce public build logic. Protection that works only while those details remain obscure is not a valid constitutional basis.

Public design also creates an opportunity for external review, but review is evidence, not proof. Popularity, contributor count, repository visibility, or long project history does not convert unknown behavior into trusted behavior.

Open design does not mean open secrets. Credentials, private keys, confidential user material, unpublished vulnerability details where temporary confidentiality is necessary, and other intentionally protected information remain subject to confidentiality.

The project should make security claims, limitations, known assumptions, and vulnerability corrections visible enough that users can understand what they are relying on. Hiding a known limitation behind a broad `secure` label is a security failure even when no exploit is currently known.

---

# 16. Secure Defaults and User Burden

Kontrakt must not make its own security guarantees depend on users discovering hidden hardening steps that the product could reasonably enforce itself.

A supported default path should preserve the security properties Kontrakt claims for that path. If a user deliberately selects an operating mode with weaker guarantees, the weaker scope must be explicit and must not retain the language or evidence of the stronger mode.

This principle does not mean Kontrakt owns the security policy of every application built with it. Application policy remains the application's concern where the Contract model assigns it there. It means that Kontrakt is responsible for the security consequences of its own compiler behavior, defaults, generated products, verification claims, and release process.

A compiler error message should not require users to understand internal security architecture merely to avoid accidental disclosure. A release should not require users to infer which undocumented flag preserves a guarantee that Kontrakt publicly claims. A dangerous compatibility mode should not silently look equivalent to the secure supported path.

Security belongs in the product obligation, not in folklore.

---

# 17. Lifecycle, Change, and Revocation

Security claims have lifetimes.

A source revision can be superseded. A credential can be revoked. A verifier model can change. A proof can become inapplicable. A cache schema can become unsafe to read. A dependency can acquire a vulnerability. A release can remain authentic while becoming unacceptable under current policy.

Retention and validity are different. Keeping material does not keep its authority, freshness, or assurance current. Deleting material does not rewrite a historical fact that was validly established or an event that actually occurred.

Freshness can require anti-rollback and anti-mix-and-match behavior. Where current policy or security posture depends on a newer trusted state, an older but authentic artifact, key set, policy, or metadata set must not silently regain current acceptance merely because its authenticity still verifies. Likewise, individually authentic pieces must not be assembled into a combination that was never jointly valid.

Where continued security depends on receiving newer security metadata or a vulnerability correction, indefinite withholding can be an availability or freshness failure rather than a neutral absence of change. The exact update mechanism belongs below the Constitution, but the security claim must say whether freshness is part of acceptance.

When a change affects a determinant of a security claim, that claim must be reconsidered. The architecture may recompute, invalidate, migrate, reverify, or retire the affected material. It may not preserve the old security label simply because migration is expensive.

Revocation changes future acceptance or authority under the applicable security policy. It does not falsify history. Incident response needs both facts: what was trusted at the time, and what is no longer trusted now.

When a claim depends on revoked authority or an invalidated assumption, derived certificates, caches, attestations, or other assurance results that rely on it must be reconsidered according to that dependency. Revocation must not invalidate unrelated Contract meaning merely because the same physical artifact carried both.

Security engineering continues through update, migration, deprecation, and retirement. A system is not secure only at the instant it is released.

---

# 18. Constitutional Prohibitions

The following are not valid sources of security authority in Kontrakt.

Implementation reachability cannot grant permission to observe or change protected material. A cache hit cannot grant authority or preserve validity by itself. A generated API cannot become Contract authority. A successful parse cannot become Establishment. A verifier success cannot extend beyond its property and model. A signature cannot prove benign semantics. Provenance cannot prove correctness. Reproducibility cannot prove trustworthiness. Source review cannot prove that unreviewed release-only material is safe. Authentication cannot substitute for authorization. Audit evidence cannot legalize the action it records.

A security-sensitive parser decision must not be inherited across a materially different re-interpretation of the same bytes. An authorization must not be separated from its use so far that the relevant context can change unnoticed. Individually valid records from incompatible generations must not be treated as a coherent state. A narrow observation or delegated capability must not be widened through ambient implementation access. An older authentic artifact must not be treated as current solely because it remains authentic. Unknown external executable behavior must not be called safe merely because analysis did not model it.

Kontrakt must not silently fail open when a required security condition is unknown or unsupported. It must not silently declassify information because it was transformed, hashed, summarized, or moved to a different product. It must not reuse security-sensitive results outside their validity context. It must not let optimization, lowering, caching, incremental work, backend convenience, runtime substitution, or external technology weaken a claimed property without changing or rejecting the claim.

Security architecture must not redefine Contract meaning to make enforcement easier. Contract architecture must not assume that implementation security will repair an obligation whose semantics are internally contradictory.

No SADR or Design document may declare an exception to these rules merely because the preferred mechanism cannot satisfy them. If the obligation is wrong, the Constitution must change explicitly. If the obligation is right, the architecture must change or the unsupported path must be rejected.

---

# 19. Security Documentation Authority

Kontrakt keeps security decisions in a document family separate from existing Contract and compiler ADR numbering.

Security Architecture Decision Records use the identifier form:

```text
SADR-0001
SADR-0002
...
```

The separate numbering does not create a second semantic universe. It keeps security architecture decisions traceable without pretending that they own Contract meaning.

A Security ADR applies this Constitution to an architectural subject. It may decide responsibility boundaries, trust boundaries, security-relevant material relations, supported security scope, failure behavior, assurance requirements, and the architecture needed to preserve them. It may reference existing Contract ADRs and compiler ADRs. It may not duplicate or replace the meaning those documents own.

A Security Design document realizes one or more SADRs. It may choose data structures, algorithms, storage layouts, cryptographic mechanisms, sandboxing, process boundaries, verifier implementations, repository settings, CI controls, release tools, operating-system facilities, JVM options, or other concrete mechanisms. Those mechanisms remain replaceable while the constitutional and SADR obligations remain satisfied.

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

A security review may discover that an existing Contract ADR or compiler ADR cannot satisfy this Constitution. The finding must be sent to the document that owns the conflicting meaning or architecture. A SADR does not patch another document by creating shadow semantics.

Research references, including *Modern Security Architecture*, remain non-normative. They can discover failure modes, challenge assumptions, and justify a change. They do not become law until the applicable Constitution, ADR, SADR, or Design document adopts the result at the correct level.

---

# 20. Constitutional Review Test

A proposed security rule belongs in this Constitution only when it survives replacement of the current architecture and implementation.

The review question is:

```text
If Kontrakt changed its IR family, query engine, cache design, JVM strategy,
repository provider, verification tool, build system, or storage mechanism,
would violating this rule still make the machine less secure in the same way?
```

If the answer is yes, the rule may be constitutional.

If the rule only says which component performs a check, which protocol carries a field, which tool signs an artifact, which branch setting is enabled, which syscall is blocked, or which table stores evidence, it belongs below the Constitution.

Compiler-specific language is allowed when it names an enduring obligation. The Constitution may say that a compiler transformation must preserve an in-scope security property. It should not say which optimization pass runs a validator. It may say that cached security evidence must remain valid for the current context. It should not prescribe the cache key. It may say that released artifacts must be traceable to the actual source and build inputs that justify the release claim. It should not prescribe one provenance format.

This boundary is part of the Constitution's own discipline. Security should not become another place where implementation leaks upward and hardens into authority.

---

# 21. Research Basis

This draft was formed by comparing Kontrakt's existing constitutional and architectural direction with established systems-security principles, production compiler practice, formal verification work, secure-compilation research, and current software-supply-chain guidance.

The project sources that materially shaped the draft are `What Contract Is`, the current Establishment, Publication, Failure, Diagnostic Evidence, HIR, JVM/external-technology, compiler-product, and related ADR/design work, *Modern Compiler Architecture 01–15*, and *Modern Security Architecture 01–15*. The important project constraint carried through all of them is that meaning remains owned by the responsibility that declares it, while compiler representation and realization remain replaceable.

The external systems-security basis includes the classic protection principles of Saltzer and Schroeder; NIST SP 800-160 Vol. 1 Rev. 1 on trustworthy systems security engineering; NIST SP 800-218 SSDF Version 1.1 as the current final SSDF and SP 800-218 Rev. 1 / SSDF Version 1.2 as a 2025 initial public draft used only as freshness input; the C-I-A and Authentication/Authorization/Auditing model used in *Designing Secure Software*; seL4's explicit proof statements and assumption discipline; and CHERI's capability monotonicity, provenance, authority attenuation, and confused-deputy lessons. These sources support explicit claim scope, non-bypassable mediation, scoped trust, visible assumptions, and authority attenuation without requiring Kontrakt to copy their mechanisms.

The compiler-assurance basis includes CompCert's end-to-end semantic-preservation model; Alive2's scoped translation validation and explicit unsupported cases; robust property and hyperproperty preservation research in secure compilation; the 2026 *SoK: Robust Properties, Robust Abstractions and Back-Translations*; 2025 work showing modern optimizers can break source-level constant-time protections; *SNIP* on preservation of speculative non-interference across compiler transformations; Optimuzz on continuous translation validation; and the 2025 AEE work on reducing verifier trust through smaller enforcement/checking boundaries. Together they reinforce that functional correctness, security-property preservation, verifier soundness, and target-level assurance are related but distinct claims.

The supply-chain and product-security basis includes SLSA 1.2's approved Source and Build tracks; the OpenSSF OSPS Baseline current at this draft date and OpenSSF source-control guidance; The Update Framework's rollback, freeze, delegation, and mix-and-match threat model; NIST cybersecurity supply-chain guidance; and CISA Secure by Design and Product Security Bad Practices guidance. These sources support exact source/build/release binding, complete build-input accounting, separation of privilege, freshness and revocation, secure defaults, and explicit producer responsibility without turning one provenance, signing, CI, or repository mechanism into constitutional law.

These references are evidence and research input. They are not incorporated wholesale. Kontrakt adopts only the obligations that fit its own declared purpose and authority model.

The research basis should remain live. A later paper, platform change, attack, or production incident may show that one of the assumptions behind this Constitution is too weak. That should trigger review. It should not silently rewrite the Constitution.

---

# 22. Draft Closure Questions

This draft is not ready for acceptance merely because its principles sound reasonable. Before it becomes constitutional law, the project should answer a small number of remaining questions at the same abstraction level.

First, the exact relationship between this Security Constitution and the existing Contract Constitution must be made explicit enough that a contradiction has one recognized escalation path and cannot be resolved by whichever document was edited last.

Second, the project should confirm the default disclosure rule for externally supplied information. The current text does not classify every external value as confidential merely because it is external; it instead denies automatic widening of audience or disclosure channel until an explicit rule justifies that widening. The project should confirm that this distinction is strong enough for unclassified external material without turning security classification into a second Contract meaning system.

Third, Availability needs a final statement of scope. The Constitution should continue to reject unbounded amplification and hidden fail-open behavior without accidentally promising service under arbitrary physical failure or exhaustion outside the declared operating envelope.

Fourth, the project should decide how much of the secure-default obligation belongs here rather than in the first SADR. The enduring principle that Kontrakt should not shift its own security burden onto users is constitutional; the exact default modes are not.

Fifth, V1's supported security profile must be defined without turning this Constitution into a promise of constant-time execution, full non-interference, malicious-OS or malicious-hardware tolerance, speculative-side-channel resistance, or distributed Byzantine resilience. This Constitution requires honest claim boundaries; a SADR must state which of those properties V1 actually supports, assumes away, or explicitly excludes.

Sixth, the relation between Contract Diagnostic Evidence and implementation/security audit evidence must be confirmed. They may share provenance or explanatory inputs, but neither should silently inherit the other's retention, authority, disclosure, or publication law merely because both are called evidence.

Seventh, the project should verify every prohibition against current Accepted ADRs. Any conflict with current Contract meaning is a finding against the owning law, not something to be papered over inside this document.

Once those questions are closed, the first Security ADR should define the authority, scope, and document interaction rules for the security architecture itself before subsystem-specific SADRs are opened.
