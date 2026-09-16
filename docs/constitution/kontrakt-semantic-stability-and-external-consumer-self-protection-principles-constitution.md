# Kontrakt Semantic Stability and External Consumer Self-Protection Principles

## Status

Working Constitution draft.

This document belongs under `constitution/`. It is not an ADR and does not record one local architecture decision. Its
purpose is broader: it defines project-wide constraints that future ADRs, compiler protocols, designs, public APIs,
generated artifacts, and backend implementations must preserve.

The document remains a draft. The principles are intentionally stronger and more durable than the engineering mechanisms
that will eventually implement them.

---

# 1. Purpose

ADR-0073 began with a JVM-specific problem: Kontrakt must use a host platform without allowing the host platform's
accidental behavior to become Contract authority. That investigation exposes the same risk in the opposite direction. If
Kontrakt succeeds, other software will eventually treat Kontrakt as a platform. Those consumers may depend on its APIs,
generated code, serialized material, diagnostics, protocols, compiler behavior, compatibility rules, or other observable
behavior.

The dangerous point is not simply that public behavior exists. The dangerous point is that an implementation choice can
escape, remain observable for long enough, and become something consumers rely on even though the project never intended
to promise it. At that moment a realization detail has turned into compatibility debt.

Kontrakt therefore needs a self-protection rule that applies above one host language, one backend, and one compiler
implementation:

> **Declared semantic authority must remain stable, while realization remains replaceable.**

The rest of this document explains what that means. It does not require one physical representation, one query engine,
one binary format, or one verification technique.

---

# 2. Authority Order

This Constitution draft does not create new Contract authority above *What Contract Is*. The existing authority order
remains intact.

*What Contract Is* is the highest semantic authority. Accepted Contract ADRs refine it. Current compiler architecture
laws constrain how those semantics are represented and processed. This Constitution draft adds cross-cutting
self-protection laws that every outward-facing Kontrakt surface should obey. Design and implementation documents then
choose concrete mechanisms under those laws. External systems and research are evidence only; they never override
project law.

---

# 3. Existing Kontrakt Basis

Most of the required foundation already exists.

*What Contract Is* distinguishes declared obligation from observed behavior and keeps realization replaceable. ADR-0063
gives each authority source ownership of the meaning it establishes. Compiler analysis, storage, query state,
realization topology, and optimization knowledge may support that meaning, but they do not create it. ADR-0071 applies
the same separation to Resolved Contract HIR: HIR carries resolved compiler-semantic material before Establishment, but
it is not itself Contract authority.

ADR-0041 makes the same distinction for identity infrastructure. HID, fingerprints, hashes, interning state, and storage
identities can make lookup and persistence efficient, but semantic equality remains owned by semantic law.

The compiler architecture extends this separation to execution. A clean build, a cached build, a parallel build, and a
future incremental build must not disagree on Contract meaning merely because the work was scheduled or reused
differently. Prediction and telemetry may influence work order or profitability. They may not decide correctness.

This document takes those existing laws and applies them to every surface that another system might observe or depend
on.

---

# 4. The General Failure Pattern

The recurring failure pattern is simple.

An implementation chooses one behavior. The behavior becomes externally visible. A consumer begins to depend on it. The
behavior then becomes difficult to change, even though it was never part of the original semantic design.

The detail that escapes may look harmless. Iteration order is a common example. Exception timing, filesystem
enumeration, generated names, diagnostic ordering, class-loading order, serialization order, hash traversal, cache
history, or a host default can produce the same problem. None of these is automatically wrong. The problem appears when
the system never states whether the observation is promised, unstable, or irrelevant, yet allows consumers to treat it
as stable.

The root failure is authority leakage. A fact about realization starts being used as though it were a fact about
meaning.

Kontrakt should prevent that leakage where it can. Where an observation genuinely must be public, Kontrakt should make
the obligation explicit enough that later implementations know what they must preserve.

---

# 5. Cross-Domain SOTA Evidence

The same problem appears in mature systems under different names. The following systems are useful because they expose
recurring engineering laws from different directions. None of them is a template for Kontrakt.

## 5.1. Production Compilers

Production compilers continuously change representation while preserving language meaning. LLVM, GCC, rustc, Swift,
Graal, and MLIR all rely on this separation even though they organize their IRs and optimization pipelines differently.

The important lesson is not that Kontrakt should copy a particular pass manager or IR stack. The important lesson is
that a representation-changing step needs a preservation obligation. If a transform lowers, folds, reorders,
specializes, or erases a distinction, the compiler must know why the remaining program still means the same thing for
the observations the language promises.

rustc incremental compilation adds a second lesson. A cached query result is reusable because the compiler has enough
evidence that the determining inputs and the result remain valid. Cache state itself does not define Rust semantics.
Fingerprints accelerate equality and invalidation decisions; they do not become the language's authority.

Recent verified-compilation work makes the same point more formally. HELIX, published in 2026, verifies preservation
through several intermediate languages down to LLVM IR. Kontrakt does not need to adopt HELIX's proof technology in V1.
What matters is the architecture it reinforces: representation change and semantic preservation are different
responsibilities, and the second must not be assumed merely because the first succeeded.

Relevant sources are LLVM documentation, the rustc incremental-compilation guide, and HELIX 2026.

## 5.2. Operating Systems and Stable ABIs

Operating systems show what happens when implementation and public compatibility are not separated early.

Linux explicitly distinguishes stable userspace interfaces from internal kernel structures. The kernel can change its
internal data structures, algorithms, and organization while preserving the ABI commitments made to userspace.
Experimental or unstable surfaces can have weaker guarantees.

The important point for Kontrakt is that "public" is not one stability category. A system can expose several surfaces
while promising different levels of compatibility for each. The stability level itself becomes part of the outward
contract.

This is a useful model for Kontrakt because Contract semantics, generated Java/Kotlin APIs, diagnostics, tooling
interfaces, persistent artifacts, and extension SPIs will not necessarily need the same compatibility law.

## 5.3. Capability Security

Capability systems show why object shape is not enough to reason about authority.

In seL4 and Capsicum, authority is explicit. Possessing a capability grants access to a resource; the system does not
infer that authority merely because some value has a convenient type or familiar API.

The same problem appears in ordinary software through ambient state. A method can look like a simple value operation
while internally consulting a global registry, the current environment, a filesystem, a clock, or a runtime class
loader. Syntactic convenience does not make that behavior semantically closed.

Recent capability research continues to separate resource state, authority, and lifetime rather than collapsing them
into a single pure/impure distinction. `Typestate via Revocable Capabilities` is one recent example. Kontrakt should
preserve the same distinction at its own public boundaries: a value-like API must not become an invisible authority
channel.

## 5.4. Databases

Databases show how hidden semantic-basis drift can corrupt persisted meaning.

PostgreSQL stores the provider version associated with a collation. If the provider later changes its ordering rules, an
existing index may still be physically present and structurally valid, yet semantically wrong for the new ordering
relation. PostgreSQL warns because the stored derived structure was built under a different semantic basis.

This is a particularly strong example for Kontrakt. The same type, identifier, and bytes do not guarantee the same
meaning when a hidden semantic basis has changed.

Databases also provide a second lesson through consistency models. A consistency model defines which histories are legal
without prescribing one replication algorithm. That is exactly the separation Kontrakt needs between outward semantics
and replaceable realization.

## 5.5. Deterministic Distributed-System Testing

FoundationDB demonstrates that operational nondeterminism and semantic nondeterminism are not the same thing.

Production execution is distributed. Scheduling, message delivery, failures, and timing vary. FoundationDB's
deterministic simulator controls those sources for testing so that a failing scenario can be replayed from a seed. The
simulator does not claim that production has one physical schedule. It creates a reproducible experiment around a system
whose legal behavior is broader than one trace.

Jepsen complements this model from the outside. It records real concurrent histories and checks them against a declared
consistency model. The history is evidence; the model remains the reference.

For Kontrakt, the lesson is that determinism should first be defined at the level of judgment. Parallel execution,
randomized exploration, and different work schedules can remain implementation choices as long as they do not change
which authoritative semantic results are legal.

## 5.6. Hermetic Build Systems

Bazel and Nix make hidden inputs a correctness problem rather than merely a build-speed problem.

A hermetic build tries to ensure that the declared inputs are the inputs that matter. If host libraries, environment
state, undeclared files, or tool versions can silently change the result, then caching and remote execution become
unreliable because the cache key does not describe the real computation.

Kontrakt has the same requirement at a semantic level. If a judgment depends on a provider, versioned dataset,
environment value, or other external basis, that dependency must be identifiable before reuse or compatibility can be
trusted.

This does not mean every implementation detail should be added to semantic identity. It means that anything capable of
changing authoritative meaning cannot remain an undeclared input.

## 5.7. Reproducible Builds and Software Supply Chains

Reproducible-build work shows that source equality does not imply artifact equality. Build time, filesystem order,
environment state, random state, archive metadata, compiler behavior, and packaging choices can all leak into output.

The 2025 study `Canonicalization for Unreproducible Builds in Java` analyzes a large Java corpus and evaluates repair
techniques on 12,283 unreproducible artifacts. Its results are a useful reminder that a mature ecosystem does not become
reproducible automatically.

Supply-chain systems such as SLSA and in-toto add provenance. They record who or what produced an artifact and under
which process. That information is important, but it is not the artifact's semantic meaning.

Kontrakt therefore needs to keep semantic identity, artifact identity, and production provenance separate. They may be
related and may all matter, but none should silently stand in for the others.

## 5.8. Public API Evolution

Kubernetes, Linux, and OpenTelemetry all demonstrate that stability must be scoped.

Kubernetes ties stronger compatibility guarantees to stable API versions and uses explicit lifecycle stages for less
mature surfaces. OpenTelemetry goes further by separating the stability of APIs, SDKs, semantic conventions,
instrumentation, and produced telemetry.

This is directly relevant to Kontrakt. Saying "Kontrakt is stable" would be too vague. Contract semantics may require
stronger guarantees than a compiler tooling API. Generated host APIs may evolve under different rules from
machine-readable diagnostics. An experimental backend SPI should not weaken a stable Contract surface merely because
both are shipped by the same project.

## 5.9. Wire Protocols and Schema Evolution

Protocol Buffers provide a useful example of information preservation.

An older consumer can read a newer binary message, retain fields it does not understand, and later serialize them again.
The older consumer does not gain permission to erase unknown material merely because it cannot interpret that material
itself. By contrast, some conversions, such as a field-by-field rebuild or a JSON round trip, can lose those unknown
fields.

Kontrakt already has the same principle in HIR: ignorance is not authority to discard a distinction that a later legal
consumer may need. The same law should protect public compatibility surfaces, serialized material, and adapter
translations.

## 5.10. Component and Resource Models

The WebAssembly Component Model separates high-level component meaning from Core WebAssembly representation. The
Canonical ABI defines how values are lowered and lifted between those levels. It also separates ordinary values from
resources and resource handles.

This is relevant because physical similarity does not establish semantic equality. A resource handle can be represented
by an integer and still not be an integer value in the semantic model.

Kontrakt already depends on the same distinction. A JVM class, an integer handle, a table row, or an HID may carry
meaning, but representation equality is not semantic equality.

## 5.11. Observability Standards

OpenTelemetry shows that telemetry itself can become an API.

Dashboards, alerts, collectors, and analysis pipelines can depend on metric identities, event names, attributes, and
well-known values. Changing that emitted material can therefore be a breaking change even when the program's binary API
remains unchanged.

Kontrakt diagnostics and evidence will face the same issue. A human-readable explanation can evolve more freely than a
stable diagnostic code or machine-readable evidence field. If external tooling is expected to consume a diagnostic
surface, the project must state which parts are stable and which are presentation.

## 5.12. Software-Ecosystem Research

Recent ecosystem research shows that syntactic compatibility is the easy part.

The 2026 systematic literature review `Breaking Changes in Software Ecosystems` synthesizes 97 primary studies across
several ecosystems. One of its recurring findings is that tools are much better at detecting structural or syntactic
breaks than behavioral breaks. Semantic-versioning trust, behavioral compatibility, and transitive dependency effects
remain difficult.

The 2025 Roseau work on Java API breakage points in the same direction. Rich semantic models outperform coarse
shape-only comparison.

For Kontrakt, the lesson is straightforward: the fact that an API still compiles or a descriptor still has the same
shape does not prove that the promised meaning is unchanged.

---

# 6. Recurring Failure Classes

The cross-domain survey reveals a small set of failures that appear repeatedly.

## 6.1. Ambient Semantic Input

A semantic result is unstable when it depends on material that was never declared as part of the judgment. A current
locale or timezone is an obvious example. The same applies to a provider registry, a wall clock, a random source, the
current classpath, or a host tool version.

The point is not that these inputs are forbidden. Some legitimate behavior needs them. The problem is allowing them to
change authoritative meaning while remaining invisible to the law that owns that judgment.

## 6.2. Representation Leakage

Representation leakage occurs when consumers begin to depend on details such as class identity, object layout, field
order, hash traversal, an IR node shape, a generated class name, a table slot, or a binary encoding accident.

Once that happens, replacing the realization becomes much harder because compatibility is no longer defined only in
terms of the intended semantics.

## 6.3. Accidental Observation

A system can leak compatibility obligations even when no one exposes an explicit "internal" API. Repeatedly stable
iteration order, diagnostic ordering, exception timing, serialization order, or generated naming is enough for consumers
to form dependencies.

This is why documentation silence is not always an adequate defense. If an observation is easy to consume and remains
stable for years, the ecosystem may treat it as a contract whether the project intended that or not.

## 6.4. Moving-Target Selection

Names such as "latest", "current", "default", "preferred", or "first found" are useful for convenience, but they are
dangerous semantic selectors.

If the selected meaning can change because the environment changes, an authoritative judgment must resolve that moving
target to an exact selection before the result becomes stable. Otherwise the same declaration may mean something
different tomorrow without any explicit semantic event.

## 6.5. Version-Label Substitution

A version label can be useful provenance, but it is not automatically proof of semantic compatibility.

The same major version may contain behavioral changes. The same JDK family may contain different semantic datasets. A
provider version string may be only an imperfect proxy for the actual data. Compatibility must therefore be judged
against the obligation that matters, not inferred solely from a convenient version coordinate.

## 6.6. Shape-Only Compatibility

Two APIs may have the same signature while differing in failure behavior, ordering, lifecycle, callback behavior, or
other legal observations. Two wire schemas may have the same field shape while assigning different meaning to a field.

Shape is evidence about compatibility. It is not sufficient authority for semantic compatibility.

## 6.7. Extension Infiltration

Plugins, callbacks, subclasses, providers, proxies, and runtime-discovered implementations are useful extension
mechanisms. They become dangerous when merely being loadable gives them the ability to redefine stable core meaning.

An extension mechanism should extend implementation unless an explicit semantic authority mechanism says otherwise.

## 6.8. Cache Authority

A cache hit or matching fingerprint can only justify reuse if the semantic assumptions behind that reuse remain valid.

If previous success, persistent state, or a digest starts being treated as proof that meaning is still correct without
re-establishing the required basis, performance infrastructure has become correctness authority.

## 6.9. Provenance / Identity Collapse

Production history and semantic identity answer different questions.

Two artifacts can have the same meaning while coming from different builders. Two artifacts can also have similar
provenance while carrying different meaning. A design that collapses provenance into semantic identity, or semantic
identity into provenance, loses both distinctions.

## 6.10. Information-Loss by Ignorance

A consumer may encounter material it does not understand. That lack of understanding does not grant permission to erase
the material when a later legal consumer may still need it.

This problem appears in protocol forwarding, frontend lowering, generated projections, and compatibility layers. The
layer that removes information carries the burden of showing that no remaining legal observer needs the distinction.

## 6.11. Operational Nondeterminism Becoming Semantic Nondeterminism

Different worker schedules, message orders, or thread timings may produce different physical executions. That is not
automatically a problem.

The problem begins when those scheduling differences change the authoritative judgment. At that point realization has
become semantic authority.

## 6.12. Evidence Becoming Authority

Tests, traces, diagnostics, logs, runtime experiments, and reference implementations are evidence. They can reveal bugs
and strengthen confidence.

They should not silently replace the declared law they are intended to check. A test suite can be incomplete. A
reference implementation can be wrong. An observed run is not the constitution.

---

# 7. Core Self-Protection Laws

The following laws are the normative core of this draft. They describe obligations and leave physical mechanisms open.

## 7.1. Declared Semantic Authority Law

A behavior becomes stable Kontrakt semantic authority only through the authority mechanism that owns that meaning.

Generated artifacts, runtime observations, tests, plugins, implementation classes, or downstream dependence may reveal
that a behavior exists. None of them establishes new Contract meaning by itself. If Kontrakt intends to guarantee an
observed behavior, an owning law must make that guarantee explicit.

## 7.2. Semantic Determinism Law

For the same complete semantic inputs, applicable Contract world, applicable semantic basis, and owning law, Kontrakt
must establish the same authoritative semantic result.

Worker order, cache state, memory address, filesystem enumeration, hash-table traversal, reflection order, classpath
enumeration, and other realization facts must not change that result unless an owning law explicitly makes one of them
part of the semantic basis.

This law concerns meaning, not physical schedule.

## 7.3. Operational Nondeterminism Containment Law

Kontrakt may use concurrency, parallelism, speculation, randomized testing, target-specific execution, or
nondeterministic resource scheduling.

Those mechanisms may change latency, resource use, exploration order, or optimization choice. They must not change which
semantic outcomes are legal unless the relevant Contract explicitly models that nondeterminism.

## 7.4. Explicit Semantic Input Law

Anything that can change authoritative meaning must be visible to the semantic boundary that owns the judgment.

A clock, random source, provider set, environment value, toolchain semantic dataset, or other external state may be a
legitimate input. It may not remain ambient if it changes Contract meaning.

An implementation may still read ambient state for logging, scheduling, or another non-semantic purpose. Such reads
remain realization.

## 7.5. Semantic Basis Completeness Law

When a judgment depends on external or versioned semantic knowledge, the relevant basis must be identifiable completely
enough to explain the judgment.

A broad label such as "JDK 26" is not automatically sufficient if the actual meaning depends on TZDB, Unicode data, CLDR
data, provider configuration, or vendor-specific semantic material. The basis should include what correctness requires
and exclude unrelated environment detail.

## 7.6. Representation Non-Authority Law

Physical representation does not define semantic authority.

A Kotlin class, Java class, JVM descriptor, IR node, table row, HID, hash, cache key, storage offset, generated source
file, classfile shape, or backend object may represent or index meaning. None of those forms creates the meaning it
carries.

## 7.7. Legal Observation Law

Every stable public semantic surface must make clear which observations consumers are entitled to rely on.

The relevant observation may be value equality, ordering, multiplicity, a failure category, a state transition, a
published field, a protocol message, a stable diagnostic identity, or a consistency guarantee. The exact set depends on
the surface.

Compatibility is judged against these legal observations. Anything outside that set remains replaceable implementation
unless another law says otherwise.

## 7.8. Explicit Underspecification Law

If a distinction is externally visible but intentionally not guaranteed, Kontrakt should say so where practical.

The project should not repeatedly emit one accidental ordering or naming scheme for years and then rely on documentation
silence as proof that no consumer may have depended on it. Where an accidental observation is likely to become sticky,
implementation should either hide it, vary it safely, or document that it is not a semantic guarantee.

This does not mean making internal execution nondeterministic for its own sake. A deterministic internal choice is fine
as long as it is not published as though Contract law required it.

## 7.9. No Moving Target Law

An authoritative semantic decision must not remain bound to a moving selector such as "latest", "current", "default", or
"first discovered".

Convenience APIs may use such selectors, but before authority is established the chosen meaning must be resolved to an
exact semantic selection. Otherwise environment drift becomes silent semantic drift.

## 7.10. Stable Semantic Identity Law

Once an exact semantic identity has been assigned to a Contract meaning, a later compiler release must not silently
reinterpret that identity as a different meaning.

A real semantic change requires an explicit semantic event, such as a new Contract Version, a new Policy World, a new
semantic basis, a declared migration, or another explicit compatibility mechanism. A compiler implementation version is
not a substitute for Contract Version.

A bug fix may correct a previously incorrect implementation. It should not rewrite history by pretending that the
incorrect behavior and the corrected meaning were always the same implementation fact.

## 7.11. Compatibility Is a Judgment Law

Compatibility is itself a semantic judgment.

It is not established merely because names, signatures, runtime classes, storage shapes, or major versions match. The
judgment must be tied to an exact obligation or legal observation set, and it may be directional.

Two different identities may be compatible for one consumer. Two identical-looking shapes may be incompatible because
they promise different behavior. Where correctness requires proof, unknown compatibility fails closed.

## 7.12. Behavioral Compatibility Law

A public API is not semantically compatible merely because old source still compiles or old binaries still link.

If the stable surface promises ordering, failures, lifecycle, callback behavior, ownership, consistency, state
transitions, or another behavioral property, compatibility must preserve that property as well.

Recent ecosystem research shows that behavioral break detection is much weaker than syntactic break detection. Kontrakt
should reduce that ambiguity by making important behavioral obligations explicit enough to reason about.

## 7.13. Information-Loss Burden Law

A layer may discard a distinction only after establishing that no remaining legal authority, consumer, round-trip
obligation, or compatibility promise still needs it.

This generalizes the existing HIR information-loss rule. It applies equally to frontend lowering, generated projections,
serialization, adapter translation, protocol migration, and diagnostic evidence.

The layer that removes information carries the burden of proof. A layer that simply does not understand the distinction
has not met that burden.

## 7.14. Canonical External Projection Law

When an external representation itself is part of a stable promise, its projection must be canonical under the semantic
basis that governs it.

For example, if published bytes or ordering are promised to be reproducible, allocation order, worker scheduling, hash
iteration, filesystem enumeration, cache state, and process identity must not leak into those bytes.

This law does not require every internal compiler object to have canonical bytes. It applies only when the external
representation is itself a legal observation.

ADR-0041 remains the identity substrate where its scope applies. HID can identify canonical material efficiently; it
does not define what the semantic law considers canonical.

## 7.15. Provenance Separation Law

Kontrakt may retain rich provenance about source origin, compiler version, builder identity, toolchain, semantic basis,
transformation history, or artifact production.

That material is useful for diagnostics, reproduction, verification, and supply-chain integrity. It is not automatically
part of semantic identity.

Two artifacts with the same semantics may have different provenance. Two artifacts with similar provenance may still
have different semantics.

## 7.16. Extension Isolation Law

An extension does not receive Contract authority merely because Kontrakt can load, call, or bind it.

Plugins, adapters, backends, providers, callbacks, subclasses, generated implementations, and compiler extensions remain
implementation mechanisms unless an explicit semantic authority mechanism grants a narrower role.

An unstable extension surface must not silently weaken a stable semantic surface.

## 7.17. Capability Isolation Law

A public Kontrakt value or operation must not silently grant authority over an unrelated external resource unless that
authority is part of the declared surface.

Global registries, default services, ambient environment, hidden callbacks, and runtime discovery are common ways this
mistake appears. The rule applies both when Kontrakt consumes an external platform and when external software consumes
Kontrakt.

## 7.18. Cache, Prediction, and Telemetry Non-Authority Law

Caching, historical telemetry, profile data, machine-learning prediction, incremental state, and prior successful
judgments may improve compiler execution.

They may decide whether to reuse work, which work to schedule first, or which optimization looks profitable. They must
not decide semantic legality.

A useful test is simple: removing all caches and profiles may make the compiler slower, but it must not change the
authoritative answer.

## 7.19. Stability-Domain Separation Law

Kontrakt must not apply one vague stability promise to every outward-facing surface.

Contract semantics, the IDL language, generated host APIs, compiler tooling APIs, persistent artifact formats,
machine-readable diagnostics, backend or adapter SPIs, and experimental extensions have different evolution pressures.
The final stability levels remain open, but the domains themselves must be distinguishable before long-term
compatibility promises are made.

Instability in one domain must not silently contaminate another domain that has stronger guarantees.

## 7.20. Independent Conformance Law

Production realization should remain checkable against an independently derived semantic reference.

The project may use a Reference Judgment, property-based tests, golden semantic vectors, differential execution,
translation validation, formal proof, or cross-backend comparison depending on the risk and maturity of the subsystem.
No single technique is mandated here.

The important rule is that the production implementation and the analysis used to optimize it should not be the only
evidence used to prove themselves correct when an independent check is practical.

The checker remains evidence. Declared Contract law remains authority.

---

# 8. Determinism Is Not One Thing

Kontrakt should not use the word `determinism` as though it described one property.

## 8.1. Semantic Determinism

Semantic determinism means that the same complete semantic basis produces the same authoritative meaning. This is a
property of the semantic system, not of one physical execution schedule.

## 8.2. Judgment Determinism

Judgment determinism applies the same requirement to one exact judgment. When the complete judgment inputs are the same,
the judgment result must be the same. This is mandatory for Contract judgment.

## 8.3. Artifact Reproducibility

Artifact reproducibility is narrower. It means that the same artifact inputs produce bit-identical output. Release
artifacts or persistent compiler products may require this property, but bit identity is not the definition of Contract
semantics.

## 8.4. Execution-Schedule Determinism

A parallel or distributed realization does not normally need to execute work in the same order every time. Different
schedules are acceptable as long as they preserve the semantic law that defines which results are legal.

## 8.5. Diagnostic Determinism

Diagnostics have a related but separate stability problem. A machine-readable diagnostic identity or semantic evidence
should not change merely because workers finished in a different order. Human wording and presentation may have weaker
compatibility requirements. That distinction should be explicit before diagnostics become an ecosystem API.

---

# 9. Public API Self-Protection

Kontrakt should assume that any convenient public API may eventually be depended upon. The safest response is not to
hide all behavior. It is to expose the smallest semantic obligation users genuinely need and avoid exposing
implementation identity as though it were meaning.

## 9.1. Public API Must Name Meaning

A stable API should name semantic concepts when those concepts are what consumers need. Contract Id, Version Id, an
exact semantic reference, a judgment result, a stable failure category, or an explicit semantic basis are examples.
Internal compiler-object identity, table slots, cache generations, backend nodes, or runtime proxy identity should not
become substitutes for them.

## 9.2. Generated APIs Remain Projections

Generated Java or Kotlin APIs remain projections. They can carry compatibility promises of their own, but they do not
become the source of Contract authority. A change in generated shape may therefore be a generated-API compatibility
event without being a Contract semantic change.

## 9.3. Defaults Must Not Become Hidden Semantics

Defaults require the same care. A convenience default is safe only when it cannot change semantic meaning or when the
default is resolved to one exact choice before authority is established. "Whatever provider answers first" or "whatever
policy is current" is not an acceptable invisible semantic law.

## 9.4. Stable APIs Need Behavioral Tests

Stable public APIs need behavioral tests derived from their legal observation sets. Signature tests are necessary but
not sufficient. The exact harness belongs in Design and QA; the obligation to test the promised behavior belongs here.

---

# 10. Versioning and Evolution

Software must evolve. This Constitution does not try to freeze Kontrakt. The rule is that evolution must not silently
rewrite old meaning.

## 10.1. New Version Does Not Rewrite Old Meaning

A new implementation or specification version may add capabilities, optimize more aggressively, or introduce new
Contract versions. It may also stop supporting an old surface according to an explicit support policy. None of those
actions gives it permission to assign a different meaning to an old exact semantic identity without an explicit semantic
event.

## 10.2. Compatibility Must Be Explicitly Scoped

Compatibility must say what it is compatibility for. The relevant observation, direction, semantic basis, and lifetime
matter. A global `compatible=true` is usually too weak to carry that meaning safely.

## 10.3. Migration Is Not Equality

An explicit migration can lawfully transform old material into new material. That does not mean the two semantic
identities were always equal. Migration is a relation between meanings, not retroactive proof that no semantic change
occurred.

## 10.4. Deprecation Does Not Remove Meaning Retroactively

A deprecated surface may stop being recommended or may eventually stop being supported. Its historical promised meaning
does not disappear merely because the project now prefers another surface.

---

# 11. External Artifact Self-Protection

Kontrakt may eventually publish many kinds of artifacts: generated APIs, contract metadata, machine-readable reports,
diagnostic evidence, persistent compiler products, backend products, test plans, or cross-language descriptors.

Each artifact needs a clear status. It may be semantic authority, a projection of authority, compiler-owned derived
material, provenance, or implementation-private material. The fact that an artifact can be read by external tooling does
not by itself make it authoritative.

This distinction matters because artifact formats tend to become sticky. Once external tools begin to parse one internal
format, implementation detail can quickly become a de facto protocol. Kontrakt should decide that boundary deliberately
rather than discover it after consumers have already depended on it.

---

# 12. Machine-Readable Diagnostics and Evidence

Diagnostics are especially vulnerable to accidental compatibility.

Human messages naturally evolve. External tools, however, prefer stable identifiers and structured evidence. Kontrakt
should therefore separate stable diagnostic identity from human explanation and presentation.

A stable diagnostic code should not depend on worker order, source enumeration accidents, or which equivalent verifier
witness happened to be discovered first. The exact canonical-witness strategy remains a Diagnostic design question. The
requirement that machine-readable meaning remain deterministic does not.

---

# 13. Supply-Chain and Provenance Implication

Kontrakt should eventually be able to explain how a published artifact was produced without making that production
history part of Contract meaning.

This implies a lasting distinction among semantic identity, artifact identity, builder or toolchain provenance, and the
semantic basis that was consumed during production. SLSA and in-toto provide useful engineering evidence for that
separation.

The exact attestation format is not decided here.

---

# 14. Conformance Strategy

No single verification technique is sufficient for every Kontrakt subsystem.

A mature implementation will likely combine several approaches. A Reference Judgment can provide an independent semantic
interpretation. Property-based tests can explore broad input spaces. Golden semantic vectors can protect especially
important invariants. Differential execution can compare independent implementations or backends. Translation validation
can check individual transformations. Formal proof may be valuable for some high-risk components.

FoundationDB demonstrates the value of deterministic simulation for complex execution spaces. Jepsen demonstrates the
value of checking observed histories against a declared model. Verified-compilation research demonstrates the value of
explicit preservation arguments across lowering boundaries.

V1 does not need to implement every technique. It should preserve architectural seams so stronger checks can be added
without redefining Contract meaning.

---

# 15. Proposed Self-Protection Invariants

The previous laws can be reduced to a few compact invariants.

For the same complete semantic inputs, Contract world, and applicable semantic basis, the authoritative semantic result
must be the same. Different physical representations may carry the same meaning, and identical-looking representations
may carry different meaning. Observation alone does not create a Contract. A cache hit does not create authority.
Version similarity does not prove compatibility. Plugin availability does not create semantic extension. Provenance
equality does not prove semantic equality.

The same separation applies in the other direction. A different runtime schedule does not by itself imply different
semantics. A layer that does not understand a distinction does not gain permission to erase it. A compiler upgrade does
not gain permission to reinterpret an old semantic identity.

These invariants are deliberately semantic. They are intended to remain valid even if the implementation language,
backend, storage architecture, query engine, or incremental algorithm changes.

---

# 16. Consequences for Kontrakt Architecture

This Constitution draft strengthens several decisions already present in the project.

Contract semantics remain independent from Kotlin/JVM. Resolved Contract HIR remains compiler-semantic material rather
than Contract authority. Canonical Contract World remains the authoritative substrate for established Contract
definition meaning. Generated host APIs remain projections.

Query and cache state remain non-authoritative. HID remains an identity substrate rather than a semantic-equality law.
Backend representation remains replaceable.

External platform behavior must be resolved before it is allowed to affect Contract meaning. Public compatibility must
be judged through legal observations rather than implementation shape. Outward Kontrakt surfaces need explicit stability
domains before strong long-term promises are made. Production realization should remain independently checkable enough
to detect implementation errors rather than merely reproduce them.

---

# 17. What This Document Does Not Decide

This Constitution draft intentionally stops before concrete mechanism.

It does not choose the public Java/Kotlin package layout, a class hierarchy, a wire format, a persistent metadata
format, a diagnostic JSON schema, a compatibility table, an HID encoding, query keys, cache structures, an incremental
algorithm, a plugin ABI, an adapter SPI, a backend API, an attestation format, or one conformance harness.

It also does not mandate translation validation or formal proof technology.

Those choices belong to later Design or protocol work. They are free to change as long as they preserve the laws defined
here.

---

# 18. Questions Still Open Before This Becomes Accepted Project Law

Several project-wide questions remain open.

Kontrakt still needs to define the exact stability domains it exposes externally and identify which artifacts are
supported long-term interfaces rather than compiler-private material. Each stable semantic surface will need an explicit
legal observation set.

The project also needs a clean relationship among Contract Version, IDL Language Version, compiler version,
generated-API version, and external artifact-format version. Those versions should not collapse into one number merely
for convenience.

The long-term Reference Judgment and cross-backend conformance policy remain open, as does the minimum provenance
required for release and persistent-artifact verification.

These questions should be revisited after ADR-0073 and the surrounding compiler boundaries are sufficiently closed.

---

# 19. Rejected Directions

## 19.1. "Everything Observable Is Contract"

Rejected.

If every observable behavior automatically became Contract, implementation replaceability would disappear. Observation
can create ecosystem debt, but it does not create semantic authority.

## 19.2. "Documentation Silence Is Enough"

Rejected.

Repeated accidental stability can become a practical dependency. Where an observation is likely to matter, Kontrakt
should make clear whether it is guaranteed, unstable, or intentionally unspecified.

## 19.3. "Semantic Versioning Solves Compatibility"

Rejected.

Versioning communicates intent and coordinates releases. It does not prove behavioral compatibility. Recent ecosystem
research continues to show that semantic breakage and transitive compatibility are harder than version labels suggest.

## 19.4. "Same API Signature Means Same Contract"

Rejected.

A signature can remain unchanged while ordering, failure behavior, lifecycle, resource ownership, or another legal
observation changes.

## 19.5. "Canonical Bytes Define Meaning"

Rejected.

Canonical bytes can represent meaning after semantic law has decided what counts as equal. The bytes do not create that
equality law.

## 19.6. "Cache Equality Defines Semantic Equality"

Rejected.

A cache is compiler realization. Semantic equality remains owned by the semantic domain whose result is being cached.

## 19.7. "One Stable API Promise Covers Everything"

Rejected.

Contract semantics, generated artifacts, diagnostics, tooling APIs, backend SPIs, and experimental extensions evolve
under different pressures. They require separate stability domains.

## 19.8. "All Nondeterminism Is Forbidden"

Rejected.

Parallel execution, distributed scheduling, randomized test exploration, and target-specific optimization may remain
operationally nondeterministic. The requirement is that they do not silently change authoritative semantics.

## 19.9. "Tests Define the Contract"

Rejected.

Tests check declared law. They can be incomplete or wrong and therefore cannot replace that law.

## 19.10. "Implementation Upgrade May Reinterpret Old Meaning"

Rejected.

A new compiler may fix bugs or introduce new semantic versions. It may not silently assign a different meaning to an
existing exact semantic identity.

---

# 20. Non-Normative Engineering Basis

The following sources informed this draft. They are engineering evidence rather than Kontrakt authority.

### Kontrakt project material

*What Contract Is*; ADR-0041, *Stable Metadata Identity, BLAKE3 HID, and Protocol-Owned Interning*; ADR-0063, *Contract
Establishment, Occurrence, Applicability, and Semantic Dependency*; ADR-0071, *Resolved Contract HIR*; the current
ADR-0073 work; the Kontrakt Compiler Total Architecture Map; and *Modern Compiler Architecture 01–15*.

### Compilers

LLVM documentation: <https://llvm.org/docs/>

rustc incremental compilation: <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>

rustc incremental compilation in
detail: <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation-in-detail.html>

HELIX, 2026: <https://arxiv.org/abs/2604.18593>

### Operating systems and capabilities

Linux ABI documentation: <https://www.kernel.org/doc/html/latest/admin-guide/abi.html>

seL4 capabilities: <https://docs.sel4.systems/Tutorials/capabilities.html>

FreeBSD Capsicum: <https://docs.freebsd.org/en/books/handbook/security/>

`Typestate via Revocable Capabilities`, 2025: <https://arxiv.org/abs/2510.08889>

### Databases and distributed systems

PostgreSQL `ALTER COLLATION` and collation versioning: <https://www.postgresql.org/docs/17/sql-altercollation.html>

FoundationDB simulation: <https://apple.github.io/foundationdb/testing.html>

FoundationDB engineering: <https://apple.github.io/foundationdb/engineering.html>

Jepsen: <https://jepsen.io/>

Jepsen consistency models: <https://jepsen.io/consistency>

### Build systems and software supply chain

Bazel hermeticity: <https://bazel.build/concepts/hermeticity>

Bazel remote execution: <https://bazel.build/docs/remote-execution>

Nix derivations: <https://nix.dev/manual/nix/2.34/store/derivation/>

SLSA provenance: <https://slsa.dev/spec/v1.2/provenance>

in-toto: <https://in-toto.io/docs/what-is-in-toto/>

SOURCE_DATE_EPOCH: <https://reproducible-builds.org/specs/source-date-epoch/>

`Canonicalization for Unreproducible Builds in Java`, 2025: <https://arxiv.org/abs/2504.21679>

### API, protocol, and semantic evolution

Kubernetes API: <https://kubernetes.io/docs/concepts/overview/kubernetes-api/>

Protocol Buffers: <https://protobuf.dev/programming-guides/proto3/>

OpenTelemetry versioning and stability: <https://opentelemetry.io/docs/specs/otel/versioning-and-stability/>

OpenTelemetry semantic convention
groups: <https://opentelemetry.io/docs/specs/semconv/general/semantic-convention-groups/>

OpenTelemetry telemetry stability: <https://opentelemetry.io/docs/specs/otel/telemetry-stability/>

OpenTelemetry semantic conventions: <https://opentelemetry.io/docs/specs/semconv/>

WebAssembly Component Model: <https://github.com/WebAssembly/component-model>

WebAssembly Canonical ABI: <https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md>

Roseau, 2025: <https://arxiv.org/abs/2507.17369>

`Breaking Changes in Software Ecosystems`, 2026: <https://arxiv.org/abs/2605.24397>

---

# 21. Final Working Law

Kontrakt should be designed on the assumption that another independent system will eventually rely on every public
semantic promise it makes.

That reliance must remain safe across compiler rewrites, backend replacement, host-platform change, parallel and
incremental compilation, extension growth, and ecosystem evolution.

The project therefore protects one boundary above all others: declared semantic authority may create stable legal
observations, and those observations constrain replaceable realization. The reverse direction is not allowed. An
implementation accident, ambient environment, cache state, execution schedule, extension behavior, or toolchain accident
does not become semantic authority merely because it was observable.

**Determinism begins at meaning.** Reproducibility, canonical artifacts, compatibility, provenance, verification, and
optimization are downstream obligations. They must preserve the meaning without becoming its source.