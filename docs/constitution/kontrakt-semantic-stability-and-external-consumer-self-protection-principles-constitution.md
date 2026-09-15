# Kontrakt Semantic Stability and External Consumer Self-Protection Principles

## Status

Working Constitution draft.

This document is not an ADR.

It isolates project-wide laws that protect Kontrakt from becoming the kind of unstable external platform that ADR-0073
is currently learning to defend against.

This document belongs under `constitution/`.

It remains a working draft until the project-wide laws are sufficiently closed.

---

# 1. Purpose

Kontrakt is currently examining the JVM as an external platform.

That work exposes a broader problem.

Any successful software system eventually becomes another system's platform.

Its users depend on:

- public APIs,
- serialized material,
- generated artifacts,
- compiler behavior,
- protocol semantics,
- failure behavior,
- versioning rules,
- extension points,
- compatibility promises,
- and behavior that was never intended to become a promise.

The same problem appears in compilers, kernels, databases, build systems, distributed systems, wire protocols,
observability standards, security systems, and software supply chains.

A system can begin with a clean internal architecture and still become difficult to evolve because implementation
accidents escape into its public meaning.

Kontrakt must prevent that failure in itself.

The core requirement is:

```text
declared semantic obligation
    → stable authority

implementation accident
environment accident
execution-order accident
cache accident
host-platform accident
extension accident
    ↛ stable authority
```

This document defines the project-wide principles required to preserve that separation.

It does not define one physical implementation.

---

# 2. Authority Order

This document does not replace existing Contract semantics.

The authority order remains:

```text
What Contract Is
    ↓
Accepted current Contract ADRs
    ↓
current compiler architecture laws
    ↓
this Constitution draft
    ↓
Design / implementation documents
    ↓
external engineering references
```

External systems and papers are evidence.

They do not override Kontrakt Contract law.

---

# 3. Existing Kontrakt Basis

The project already contains most of the required foundations.

*What Contract Is* establishes that observed behavior does not become Contract merely because a consumer depends on it.

It separates declared obligation from realization.

It also requires implementation replaceability.

ADR-0063 establishes source-owned authority.

Compiler analysis, storage, query state, realization topology, and optimization knowledge do not become Contract
authority.

ADR-0071 establishes Resolved Contract HIR as compiler-semantic material rather than Contract authority.

Resolved meaning and physical representation remain separate.

ADR-0041 separates semantic identity from HID, fingerprints, hashes, storage identities, and interning machinery.

The current compiler architecture additionally requires:

```text
clean compilation
cached compilation
parallel compilation
incremental compilation
```

to preserve the same Contract meaning.

Prediction and telemetry may influence work order or profitability.

They may not change correctness or Contract meaning.

This document generalizes those existing rules to every outward-facing Kontrakt surface.

---

# 4. The General Failure Pattern

A system becomes unstable when its consumers can no longer tell which observations are promised meaning and which
observations are accidental realization.

A common progression is:

```text
implementation chooses one behavior
    ↓
behavior is externally observable
    ↓
consumer depends on it
    ↓
behavior becomes ecosystem compatibility debt
    ↓
implementation can no longer change freely
```

This does not require a bad API.

It can happen through:

```text
iteration order
exception timing
filesystem order
thread scheduling
default locale
current provider
class-loading order
serialization order
generated names
hash choice
diagnostic order
cache history
runtime reflection
plugin discovery
environment variables
implicit version selection
```

The root problem is authority leakage.

The consumer begins treating a realization fact as semantic authority.

Kontrakt must prevent that leakage where possible and explicitly classify it where it cannot be prevented.

---

# 5. Cross-Domain SOTA Evidence

The same architectural problem appears in mature systems under different names.

The examples below are not templates to copy.

They are evidence for recurring engineering laws.

---

## 5.1. Production Compilers

Modern compilers preserve language meaning while repeatedly changing representation.

LLVM, GCC, rustc, Swift, Graal, and MLIR all separate higher-level semantic material from lower-level target
representation.

The important principle is not the number of IRs.

The important principle is:

```text
a lower representation may change
only under a preservation obligation
```

rustc incremental compilation adds another relevant law.

A cached query is reusable because its determining inputs and result are unchanged.

The cache is not semantic authority.

The rustc red-green model explicitly depends on compiler determinism: unchanged inputs must produce the same result.

Fingerprint equality accelerates reuse.

It does not define Rust language meaning.

Kontrakt already follows the same direction.

```text
Contract meaning
    ≠
query result identity
    ≠
fingerprint
    ≠
physical IR node
```

Recent verified-compilation work strengthens this separation.

HELIX, published in 2026, verifies semantic preservation through multiple intermediate languages down to LLVM IR rather
than assuming that target representation preserves high-level meaning automatically.

The lesson for Kontrakt is not that V1 must use Coq.

The lesson is that every representation-changing boundary needs an explicit meaning-preservation story.

Sources:

- LLVM documentation: <https://llvm.org/docs/>
- rustc incremental compilation: <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>
- rustc incremental compilation in
  detail: <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation-in-detail.html>
- HELIX, 2026: <https://arxiv.org/abs/2604.18593>

---

## 5.2. Operating Systems and Stable ABIs

Operating systems demonstrate the long-term cost of public behavior.

Linux explicitly classifies ABI surfaces by stability.

Stable userspace interfaces receive compatibility commitments that internal kernel structures do not receive.

The kernel may change internally while preserving the declared userspace ABI.

That boundary is critical.

Without it, kernel implementation freedom would collapse into public compatibility debt.

Linux also demonstrates that one system does not need to promise stability for every exposed experimental surface.

Stability level itself is part of the public contract.

Source:

- Linux ABI documentation: <https://www.kernel.org/doc/html/latest/admin-guide/abi.html>

This supports two Kontrakt rules:

```text
stable public semantic surface
    ≠
all observable implementation behavior
```

and:

```text
stability status must itself be explicit
```

---

## 5.3. Capability Security

seL4 and Capsicum show another form of the same boundary.

Authority is not inferred from object shape.

A capability is an explicit token of authority.

Possession of the capability permits access to a resource.

Ambient process state is therefore different from explicitly held authority.

This matters to Kontrakt because a public API may accidentally grant authority through:

```text
default registries
global service lookup
current environment
implicit filesystem access
current clock
runtime class loading
```

even when the API appears value-oriented.

Kontrakt must not confuse syntactic convenience with semantic closure.

Sources:

- seL4 capabilities: <https://docs.sel4.systems/Tutorials/capabilities.html>
- FreeBSD Capsicum: <https://docs.freebsd.org/en/books/handbook/security/>

Recent capability research continues to separate authority, aliasing, and resource lifetime.

`Typestate via Revocable Capabilities` demonstrates in 2025 that stateful-resource correctness requires capability
lifetime and state-transition reasoning rather than a simple pure/impure classification.

Source:

- Typestate via Revocable Capabilities, 2025: <https://arxiv.org/abs/2510.08889>

---

## 5.4. Databases

Databases demonstrate that hidden semantic basis drift can corrupt persisted meaning.

PostgreSQL records the provider version associated with a collation.

When the current provider version differs from the recorded version, PostgreSQL warns because existing indexes may have
been built under a different ordering relation.

The physical bytes can still exist.

The schema can still exist.

The provider can still be available.

The semantic ordering basis has changed.

That is enough to make previously valid derived structure unsafe.

Source:

- PostgreSQL `ALTER COLLATION`: <https://www.postgresql.org/docs/17/sql-altercollation.html>

The general lesson is:

```text
same type
same object
same identifier
    ≠
same semantic basis
```

A second database lesson comes from consistency models.

A consistency model describes the histories that are legal.

It does not prescribe one internal replication algorithm.

This is the correct separation between externally promised behavior and realization freedom.

Reference:

- Jepsen consistency models: <https://jepsen.io/consistency>

---

## 5.5. Deterministic Distributed-System Testing

FoundationDB uses deterministic simulation to reproduce distributed failures.

Its simulator controls time, randomness, networking, processes, and failures under a deterministic seed.

The production system is distributed and operationally nondeterministic.

The test model makes the chosen experiment reproducible.

FoundationDB therefore demonstrates an important distinction:

```text
real-world scheduling nondeterminism
    ≠
semantic nondeterminism
```

and:

```text
controlled entropy
    → reproducible judgment
```

A system does not need one physical execution order.

It needs stable rules about which outcomes are legal.

Sources:

- FoundationDB simulation and testing: <https://apple.github.io/foundationdb/testing.html>
- FoundationDB engineering: <https://apple.github.io/foundationdb/engineering.html>

Jepsen complements this model.

Jepsen does not prove implementation correctness.

It captures real concurrent histories and checks them against declared consistency models.

The declared model remains the reference.

The observed run is evidence.

Source:

- Jepsen: <https://jepsen.io/>

---

## 5.6. Hermetic Build Systems

Bazel and Nix make hidden inputs a first-class correctness problem.

Bazel defines hermeticity in terms of isolation from undeclared host state.

The same declared inputs should not produce different meaning because another machine has a different compiler, library,
or ambient environment.

Nix derivations similarly define build steps from explicit inputs.

A derivation describes what executable runs on what declared material to produce outputs.

These systems show why a cache is only safe when the true determining inputs are explicit.

Sources:

- Bazel hermeticity: <https://bazel.build/concepts/hermeticity>
- Bazel remote execution: <https://bazel.build/docs/remote-execution>
- Nix store derivations: <https://nix.dev/manual/nix/2.34/store/derivation/>

The lesson for Kontrakt is broader than build reproducibility:

```text
hidden semantic input
    → invalid reuse
    → invalid comparison
    → invalid compatibility claim
```

---

## 5.7. Reproducible Builds and Software Supply Chains

Reproducible-build work separates source trust from artifact trust.

The same source is not sufficient if build time, filesystem ordering, random state, environment state, archive metadata,
compiler behavior, or packaging state can change the artifact.

The Java ecosystem still exhibits these failures at scale.

The 2025 study `Canonicalization for Unreproducible Builds in Java` develops a six-cause taxonomy of unreproducibility
and evaluates mitigation on 12,283 unreproducible artifacts.

Its Chains-Rebuild technique raises successful reproducibility in that dataset from 9.48% to 26.89%.

This is direct evidence that deterministic artifact production cannot be assumed merely because the source language and
build tool are mature.

Source:

- Canonicalization for Unreproducible Builds in Java, 2025: <https://arxiv.org/abs/2504.21679>

SLSA and in-toto add provenance.

They track what produced an artifact and under what process.

That provenance is important evidence.

It is not the artifact's semantic meaning.

Sources:

- SLSA provenance: <https://slsa.dev/spec/v1.2/provenance>
- in-toto: <https://in-toto.io/docs/what-is-in-toto/>

This supports a Kontrakt separation:

```text
semantic identity
    ≠
production provenance
```

Both may be required.

They remain different.

---

## 5.8. Public API Evolution

Kubernetes treats API stability as an explicit lifecycle.

Stable API versions receive stronger compatibility guarantees than alpha or beta surfaces.

Evolution therefore happens through declared version boundaries and deprecation policy rather than silent
reinterpretation.

Source:

- Kubernetes API: <https://kubernetes.io/docs/concepts/overview/kubernetes-api/>

Linux follows a similar pattern for ABI maturity.

OpenTelemetry goes further and separates stability of:

```text
API
SDK
semantic conventions
instrumentation
telemetry output
```

Stable components cannot casually inherit instability from experimental components.

Sources:

- OpenTelemetry versioning and stability: <https://opentelemetry.io/docs/specs/otel/versioning-and-stability/>
- OpenTelemetry semantic convention
  groups: <https://opentelemetry.io/docs/specs/semconv/general/semantic-convention-groups/>
- OpenTelemetry telemetry stability: <https://opentelemetry.io/docs/specs/otel/telemetry-stability/>

This is strong evidence against one global statement such as:

```text
Kontrakt API is stable
```

Different outward surfaces may require different compatibility laws.

The stability level must not be inferred from package location or accidental longevity.

---

## 5.9. Wire Protocols and Schema Evolution

Protocol Buffers preserve unknown fields in their binary representation.

An older consumer can parse a newer message, retain fields it does not understand, and serialize them again.

However, field-by-field reconstruction or conversion through JSON can lose that unknown material.

Source:

- Protocol Buffers unknown fields: <https://protobuf.dev/programming-guides/proto3/>

This demonstrates an important information-preservation law.

A consumer that does not understand one distinction is not automatically authorized to erase it.

If the outer compatibility contract promises round-trip preservation, ignorance is not permission to discard
information.

Kontrakt already has the same idea in HIR information-loss rules.

The principle should apply to public compatibility surfaces as well.

---

## 5.10. Component and Resource Models

The WebAssembly Component Model separates high-level component types from Core WebAssembly representation.

The Canonical ABI defines lifting and lowering between those worlds.

It also separates ordinary values from resources and resource handles.

The fact that a resource handle may be physically represented as an integer does not make the resource an integer
semantic value.

Sources:

- WebAssembly Component Model: <https://github.com/WebAssembly/component-model>
- Canonical ABI: <https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md>

This directly supports Kontrakt's existing rule:

```text
representation equality
    ≠
semantic equality
```

---

## 5.11. Observability Standards

OpenTelemetry demonstrates that even telemetry becomes an external semantic API.

Dashboards, alerts, and analysis systems depend on attribute names, event names, metric identities, and well-known
values.

Changing emitted telemetry can therefore be a breaking change even when program behavior and binary API remain
unchanged.

OpenTelemetry explicitly defines which produced telemetry is stable and which is not.

This matters to Kontrakt diagnostics and evidence.

A diagnostic code, evidence field, generated metric, or machine-readable result can become a public contract if external
tooling is expected to consume it.

Human wording and machine semantic identity should not be conflated.

Source:

- OpenTelemetry semantic conventions: <https://opentelemetry.io/docs/specs/semconv/>

---

## 5.12. Software-Ecosystem Research

Recent research shows that syntactic compatibility is the easy part.

The 2026 systematic literature review `Breaking Changes in Software Ecosystems` synthesizes 97 primary studies across
Maven/Java, npm/JavaScript, Python, Web APIs, and Linux distributions.

It reports that tools detect syntactic breaks much better than behavioral breaks.

It also identifies semantic-versioning trust, behavioral-break detection, and transitive dependency propagation as
unresolved ecosystem problems.

Source:

- Breaking Changes in Software Ecosystems, 2026: <https://arxiv.org/abs/2605.24397>

The 2025 `Roseau` work reaches high accuracy for source-based Java API breaking-change analysis and demonstrates that
rich semantic API models are more effective than coarse binary-shape comparison.

Source:

- Roseau, 2025: <https://arxiv.org/abs/2507.17369>

The Kontrakt consequence is clear.

```text
signature compatibility
    ≠
semantic compatibility
```

Semantic stability must be defined at the level users are promised.

---

# 6. Recurring Failure Classes

The cross-domain survey exposes a small set of repeated failure patterns.

These patterns are more important than any one technology.

---

## 6.1. Ambient Semantic Input

Meaning depends on material that was never declared as an input.

Examples include:

```text
current locale
current timezone
filesystem state
provider registry
environment variable
wall clock
random source
current classpath
loaded plugin set
host tool version
```

The result may look deterministic on one machine.

It is not semantically closed.

---

## 6.2. Representation Leakage

Consumers depend on:

```text
class identity
object layout
field order
hash order
IR node shape
generated class name
storage row number
table layout
binary encoding accident
```

even though none of these were intended to define meaning.

The implementation then becomes difficult to replace.

---

## 6.3. Accidental Observation

A system leaves one implementation detail observable.

Consumers build dependencies on it.

The detail becomes compatibility debt even though it was never declared.

This is especially dangerous for:

```text
iteration order
diagnostic order
exception timing
serialization order
thread timing
generated names
```

---

## 6.4. Moving-Target Selection

A semantic decision is based on:

```text
latest
current
default
first discovered
highest available
nearest compatible
first on classpath
```

without pinning the selected meaning.

A later environment change silently changes the result.

---

## 6.5. Version-Label Substitution

A version label is treated as proof of semantic compatibility.

Examples include:

```text
same major version
same JDK family
same provider version string
same package version
```

without proving that the relevant semantic obligation is unchanged.

Version coordinates are useful references.

They are not universal semantic proofs.

---

## 6.6. Shape-Only Compatibility

Two surfaces have the same:

```text
method signature
wire field shape
runtime class
binary descriptor
schema shape
```

and are therefore assumed to mean the same thing.

Behavioral compatibility is ignored.

This is a recurring source of ecosystem breakage.

---

## 6.7. Extension Infiltration

A plugin, subclass, callback, provider, proxy, runtime-discovered implementation, or dynamically loaded component gains
the ability to modify stable core meaning.

The extension mechanism becomes an undeclared authority mechanism.

---

## 6.8. Cache Authority

A cache hit, fingerprint equality, memoized query, previous successful build, or persistent artifact is treated as proof
that meaning is still valid.

The determining semantic basis is no longer checked.

Performance state has become correctness authority.

---

## 6.9. Provenance / Identity Collapse

Two products are considered semantically different because they were produced by different machines or sessions.

Or they are considered semantically equal only because their provenance is similar.

Production history and semantic identity become confused.

---

## 6.10. Information-Loss by Ignorance

A consumer does not understand one piece of material and drops it.

Later consumers would have understood it.

Forward compatibility is broken by an intermediate layer that had no authority to reinterpret the information.

---

## 6.11. Operational Nondeterminism Becoming Semantic Nondeterminism

Different worker schedules or network orders are allowed to change an authoritative answer.

Parallelism stops being an implementation choice.

It becomes semantic authority.

---

## 6.12. Evidence Becoming Authority

Tests, logs, diagnostics, traces, observed runtime behavior, or reference implementations become the de facto source of
meaning.

They are useful evidence.

They must not silently replace the declared law they are supposed to check.

---

# 7. Core Self-Protection Laws

The following laws are the proposed cross-cutting core of this document.

They describe obligations.

They do not prescribe one data structure, query engine, API class, storage form, or backend.

---

## 7.1. Declared Semantic Authority Law

A behavior becomes stable Kontrakt semantic authority only through the authority mechanism that owns that meaning.

Observed behavior, generated artifacts, test expectations, runtime experiments, implementation classes, plugins, or
downstream reliance do not establish new Contract meaning.

```text
declared obligation
    → may be authority

observed realization
    → evidence only
```

If Kontrakt intentionally wants to guarantee an observed behavior, the guarantee must be declared by an owning law.

---

## 7.2. Semantic Determinism Law

For the same complete semantic inputs, applicable Contract world, explicit semantic basis, and owning law, Kontrakt must
establish the same authoritative semantic result.

The result must not depend on:

```text
worker completion order
thread scheduling
cache state
memory address
object identity
filesystem enumeration
hash-table traversal
classpath enumeration
reflection order
current host defaults
unbound provider state
```

unless one of those is explicitly part of the governing semantic basis.

This law applies to semantic judgment.

It does not require every physical execution to use the same schedule.

---

## 7.3. Operational Nondeterminism Containment Law

Kontrakt may use parallelism, concurrency, speculation, randomized testing, nondeterministic resource scheduling, or
target-specific execution.

Those mechanisms may change:

```text
work order
latency
resource usage
optimization choice
test exploration path
```

They must not change which authoritative semantic outcomes are legal.

Operational nondeterminism stays in realization unless an owning Contract explicitly models it.

---

## 7.4. Explicit Semantic Input Law

Every input that may change authoritative meaning must be explicit at the semantic boundary that owns that judgment.

An ambient input may not silently influence semantic authority.

Examples include:

```text
clock
random source
environment
provider set
toolchain semantic data
default locale
runtime registry
external configuration
```

An implementation may read ambient material for non-semantic purposes.

That read must not change Contract meaning.

---

## 7.5. Semantic Basis Completeness Law

When one judgment depends on external or versioned semantic knowledge, the complete relevant basis must be identifiable.

A coarse platform label is not sufficient merely because it correlates with the real semantic data.

```text
JDK 26
```

does not automatically identify:

```text
TZDB meaning
Unicode meaning
CLDR meaning
provider configuration
vendor-specific semantic data
```

The basis must be no wider than necessary and no narrower than correctness requires.

---

## 7.6. Representation Non-Authority Law

Physical realization does not define semantic authority.

This includes:

```text
Kotlin class
Java class
JVM descriptor
IR node
table row
HID
hash
cache key
storage offset
generated source
classfile shape
backend object
```

A representation may carry or accelerate access to meaning.

It does not create that meaning.

---

## 7.7. Legal Observation Law

A stable public surface must define which observations consumers are entitled to rely on.

Compatibility is judged against those legal observations.

A legal observation may include:

```text
value distinction
equality
ordering
multiplicity
failure category
state transition
published field
protocol message
stable diagnostic code
consistency guarantee
```

Implementation details that are outside the legal observation set remain replaceable.

A public surface with an undefined observation boundary invites accidental contracts.

---

## 7.8. Explicit Underspecification Law

If Kontrakt does not guarantee one observable distinction, that non-guarantee should be explicit where the distinction
can realistically become consumer-visible.

Kontrakt must not rely on documentation silence while repeatedly producing one stable accidental behavior and then
assume no consumer will depend on it.

Where practical, implementation should prevent accidental stability from masquerading as a guarantee.

A private deterministic choice may be used internally.

It must not be published as though the semantic law required it.

---

## 7.9. No Moving Target Law

Authoritative semantic selection must not depend on an unpinned moving target.

The following are not sufficient semantic selectors by themselves:

```text
latest
current
default
preferred
first found
highest installed
nearest compatible
```

A convenience layer may use such language only if the resulting exact semantic selection is resolved and pinned before
it can become authoritative.

Compiler and runtime convenience must not create implicit version authority.

---

## 7.10. Stable Semantic Identity Law

Once Kontrakt has assigned one exact semantic identity to one Contract meaning, a later compiler release must not
silently reinterpret that same identity as a different Contract meaning.

A semantic change requires an explicit semantic event.

Examples include:

```text
new Contract Version
new Policy World
new explicit semantic basis
declared migration
declared compatibility relation
```

A compiler implementation version is not a substitute for Contract Version.

A bug fix may correct an incorrect prior implementation.

It must not pretend that the historical incorrect behavior was always the same authoritative meaning.

---

## 7.11. Compatibility Is a Judgment Law

Compatibility is not inferred from:

```text
same name
same signature
same major version
same hash prefix
same storage shape
small textual diff
same runtime class
```

Compatibility must be defined relative to an exact obligation or legal observation set.

It may be directional.

Two exact identities may differ while still being compatible for one consumer.

Two surfaces may have identical shape while being semantically incompatible.

Unknown compatibility fails closed where correctness requires a proof.

---

## 7.12. Behavioral Compatibility Law

A public API is not compatible merely because existing clients still compile.

Behavioral obligations are part of compatibility when they are part of the stable surface.

This includes relevant:

```text
failure behavior
ordering
consistency
lifecycle
callback behavior
resource ownership
state transition
semantic output
```

The 2026 software-ecosystem literature review identifies behavioral break detection as substantially weaker than
syntactic-break detection.

Kontrakt should design stable APIs so that behavioral obligations are explicit enough to verify.

---

## 7.13. Information-Loss Burden Law

A layer may discard a distinction only when no remaining legal consumer, authority, round-trip obligation, or
compatibility promise requires it.

Ignorance is not authority to erase information.

This law generalizes the existing HIR information-loss boundary.

It applies to:

```text
frontend lowering
generated API projection
serialization
adapter translation
protocol migration
diagnostic evidence projection
future compatibility material
```

The transformation that removes information carries the burden of proving that the distinction is no longer legally
observable.

---

## 7.14. Canonical External Projection Law

When Kontrakt publishes material whose external bytes, ordering, identity, or structured representation are themselves
stable public observations, that projection must be canonical under its declared semantic basis.

Canonical projection must not depend on:

```text
allocation order
worker schedule
hash iteration
filesystem order
cache state
process identity
```

This law does not require every internal compiler object to have canonical bytes.

It applies only when external representation itself is part of the promised surface.

ADR-0041 remains the identity substrate where its accepted scope applies.

HID does not replace the semantic law that determines what is canonical.

---

## 7.15. Provenance Separation Law

Kontrakt may retain rich provenance describing:

```text
source origin
compiler version
builder
toolchain
semantic basis
transformation history
artifact production
```

Provenance supports diagnostics, verification, supply-chain integrity, and reproduction.

Provenance does not become semantic identity unless an owning law explicitly makes one provenance distinction
semantically relevant.

```text
same semantics
different provenance
```

can be valid.

```text
same provenance
different semantics
```

can also be valid.

---

## 7.16. Extension Isolation Law

An extension mechanism does not receive Contract authority merely because Kontrakt can load or call it.

This applies to:

```text
plugin
adapter
backend
provider
callback
subclass
generated implementation
service registry
compiler extension
```

Stable semantic extension must occur through an explicit authority mechanism.

Implementation extension remains implementation.

An unstable extension must not silently weaken the guarantees of a stable surface.

---

## 7.17. Capability Isolation Law

Possession or invocation of a public Kontrakt value must not silently grant authority over an unrelated external
resource unless that authority is part of the explicitly declared surface.

Global registries, default services, ambient environment, hidden callbacks, and runtime discovery must not become
invisible authority channels.

This law applies to Kontrakt itself as well as to external platforms consumed by Kontrakt.

---

## 7.18. Cache, Prediction, and Telemetry Non-Authority Law

Caching, historical telemetry, profile data, machine-learning prediction, incremental state, and prior successful
judgments may improve compiler execution.

They may not establish Contract meaning.

They may affect:

```text
reuse
scheduling
work priority
optimization profitability
speculation
```

They must not affect semantic legality.

Removing all cache and profile state must not change the authoritative answer.

---

## 7.19. Stability-Domain Separation Law

Kontrakt must not use one vague stability promise for all outward surfaces.

Different surfaces may have different stability contracts.

Possible domains include:

```text
Contract semantic surface
IDL language surface
generated host API
compiler tooling API
persistent artifact format
machine-readable diagnostics
adapter / backend SPI
experimental extension surface
```

This document does not assign final stability levels.

It requires that the domains be distinguishable before public compatibility promises are made.

An unstable domain must not silently contaminate a stable one.

---

## 7.20. Independent Conformance Law

Production realization must remain checkable against an independently derived semantic reference.

The exact verification mechanism may vary.

Candidates include:

```text
Reference Judgment
property-based testing
golden semantic vectors
differential execution
translation validation
formal proof
cross-backend comparison
```

The checker is evidence.

The declared Contract remains authority.

The production implementation and its primary optimization analyses must not be the only source used to prove themselves
correct where independent checking is practical.

---

# 8. Determinism Is Not One Thing

The word `determinism` can hide several different obligations.

Kontrakt should keep them separate.

---

## 8.1. Semantic Determinism

Same complete semantic basis produces the same authoritative meaning.

This is mandatory.

---

## 8.2. Judgment Determinism

The same exact judgment inputs produce the same judgment result.

This is mandatory for Contract judgment.

---

## 8.3. Artifact Reproducibility

The same artifact inputs produce bit-identical output.

This is required only where the artifact contract says its bytes are canonical or reproducible.

It is highly desirable for persistent compiler products and release artifacts.

It is not the definition of Contract semantics.

---

## 8.4. Execution-Schedule Determinism

The same program uses the same thread or operation schedule.

This is generally not required.

A system may be semantically deterministic while using different schedules.

---

## 8.5. Diagnostic Determinism

Machine-readable diagnostic identity and semantic evidence should remain deterministic under the same failing semantic
case.

Human wording, presentation order, or auxiliary context may have a different stability law.

This distinction should be explicit before diagnostic APIs become public.

---

# 9. Public API Self-Protection

Kontrakt should assume that every sufficiently convenient public API will eventually be depended upon.

The safest strategy is not to hide all behavior.

It is to publish the smallest precise obligation that users genuinely need.

---

## 9.1. Public API Must Name Meaning

A public API should expose semantic concepts where those concepts are the stable obligation.

It should avoid exposing implementation-only identity as though that identity were semantic.

Conceptually preferred:

```text
Contract Id
Version Id
exact semantic reference
judgment result
stable failure category
explicit semantic basis
```

Conceptually dangerous:

```text
compiler object address
internal IR class
table slot
cache generation
backend node
runtime proxy identity
```

This does not require one specific Java API design.

It defines the direction.

---

## 9.2. Generated APIs Remain Projections

Generated Java or Kotlin artifacts may provide user ergonomics.

They do not become the source of Contract authority.

If generated shape changes while the declared Contract obligation is preserved, that may be an implementation or
generated-API compatibility question.

It is not automatically a Contract semantic change.

The stability law for generated APIs must therefore be explicit and separate.

---

## 9.3. Defaults Must Not Become Hidden Semantics

Convenience defaults are dangerous when they select meaning.

A default may be used only when either:

```text
the default cannot change semantic meaning
```

or:

```text
the default is resolved to one exact explicit semantic selection
before authority is established
```

The system must not rely on:

```text
whatever policy is current
whatever backend is installed
whatever provider answers first
whatever version is newest
```

as invisible semantic law.

---

## 9.4. Stable APIs Need Behavioral Tests

Signature compatibility is insufficient.

Every stable semantic API should have tests derived from its legal observation set.

Those tests should cover:

```text
value behavior
failure behavior
version behavior
compatibility behavior
information preservation
authority isolation
```

where applicable.

The exact harness belongs to Design and QA.

The obligation belongs here.

---

# 10. Versioning and Evolution

Software must evolve.

The goal is not immobility.

The goal is controlled semantic change.

---

## 10.1. New Version Does Not Rewrite Old Meaning

A new implementation or specification version may add new behavior.

It must not silently change the meaning already assigned to an older exact semantic coordinate.

If old material is no longer supported, that is a support-policy decision.

It is not permission to reinterpret old meaning.

---

## 10.2. Compatibility Must Be Explicitly Scoped

A compatibility relation should answer:

```text
compatible for what observation?
compatible in which direction?
under what basis?
for which lifetime?
```

One global `compatible=true` is usually too weak.

---

## 10.3. Migration Is Not Equality

A migration may lawfully transform old material into new material.

That does not mean the two semantic identities were always equal.

```text
old meaning
    ↓ explicit migration
new meaning
```

is different from:

```text
old meaning == new meaning
```

This distinction protects history and diagnostics.

---

## 10.4. Deprecation Does Not Remove Meaning Retroactively

A deprecated public semantic surface may stop being recommended.

Its historical meaning remains the meaning consumers were previously promised.

Removal and replacement need an explicit compatibility or migration rule where persisted or external consumers are
involved.

---

# 11. External Artifact Self-Protection

Kontrakt may eventually publish more than executable classfiles.

Possible external artifacts include:

```text
contract metadata
generated APIs
persistent compiler products
diagnostic evidence
machine-readable reports
test plans
cacheable artifacts
backend products
cross-language descriptors
```

Each artifact must state whether it is:

```text
semantic authority
projection of authority
compiler-owned derived material
provenance
implementation-private material
```

No artifact format should acquire authority merely because consumers can read it.

---

# 12. Machine-Readable Diagnostics and Evidence

Diagnostics are especially vulnerable to accidental contracts.

Human messages naturally evolve.

External tools prefer stable identifiers.

Kontrakt should therefore distinguish:

```text
stable diagnostic identity
semantic evidence
human explanation
presentation formatting
```

before diagnostics become an ecosystem API.

A stable diagnostic code must not depend on source ordering, worker order, or which verifier happens to fail first when
multiple equivalent witnesses exist.

The exact canonical-witness policy remains a Diagnostic design question.

The determinism obligation does not.

---

# 13. Supply-Chain and Provenance Implication

Kontrakt should be able to explain how one published artifact was produced without making production history part of
Contract meaning.

This suggests a future separation:

```text
semantic identity
artifact identity
production provenance
builder identity
toolchain identity
```

These may be linked.

They should not be collapsed.

SLSA and in-toto provide strong external evidence for this separation.

The exact attestation format remains outside this document.

---

# 14. Conformance Strategy

No single correctness technique is sufficient.

A SOTA-grade Kontrakt should combine independent techniques according to risk.

A likely long-term structure is:

```text
declared Contract law
        ↓
Reference Judgment
        ↓
production compiler / runtime

plus

property-based tests
differential tests
golden vectors
artifact reproducibility checks
target-specific conformance tests
translation validation where valuable
```

FoundationDB shows the value of deterministic simulation.

Jepsen shows the value of checking observed histories against a declared model.

Verified-compilation research shows the value of preservation proof across lowering boundaries.

Kontrakt should preserve the architectural seams needed to use stronger methods later.

V1 does not need to adopt every method.

---

# 15. Proposed Self-Protection Invariants

The following invariants summarize the document.

```text
same semantic inputs
+
same Contract world
+
same applicable semantic basis
    ↓
same authoritative semantic result
```

```text
same implementation representation
    ↛ same semantic meaning
```

```text
same semantic meaning
    ↛ same physical representation
```

```text
observed behavior
    ↛ declared contract
```

```text
cache hit
    ↛ authority
```

```text
version similarity
    ↛ compatibility
```

```text
plugin availability
    ↛ semantic extension
```

```text
provenance equality
    ↛ semantic equality
```

```text
runtime scheduling difference
    ↛ semantic difference
```

```text
unknown distinction
    ↛ permission to discard
```

```text
implementation upgrade
    ↛ silent reinterpretation
```

---

# 16. Consequences for Kontrakt Architecture

This document strengthens several existing directions.

Contract semantics remain independent from Kotlin/JVM.

Resolved Contract HIR remains compiler-semantic material rather than authority.

Canonical Contract World remains the authority substrate for established Contract definition meaning.

Generated host APIs remain projections.

Query and cache state remain non-authoritative.

HID remains an identity substrate rather than semantic equality authority.

Backend representation remains replaceable.

External platform behavior must be resolved before it can affect Contract meaning.

Public compatibility must be defined in terms of legal observations rather than implementation shape.

Different outward Kontrakt surfaces need explicit stability domains before long-term promises are made.

Reference Judgment and conformance remain independent enough to detect production implementation errors.

---

# 17. What This Document Does Not Decide

This document intentionally does not freeze:

```text
public package layout
Java/Kotlin API class hierarchy
wire format
persistent metadata format
diagnostic JSON schema
exact compatibility matrix
exact semantic fingerprint
HID encoding
query keys
cache design
incremental algorithm
backend API
plugin ABI
adapter SPI
conformance test harness
translation-validation engine
formal proof technology
artifact attestation format
```

Those are Design or later protocol decisions.

This document defines the obligations those mechanisms must preserve.

---

# 18. Questions Still Open Before This Becomes Accepted Project Law

The project still needs to decide the exact stability domains exposed to external users.

The project must decide which outward artifacts are long-term supported interfaces and which are compiler-private.

The project must define the legal observation set for each stable public semantic surface.

The project must define how Contract Version, IDL Language Version, compiler version, generated-API version, and
external artifact-format version relate without collapsing them.

The project must define the long-term Reference Judgment and cross-backend conformance policy.

The project must define the minimum provenance required for release and persistent artifact verification.

The project must decide when this Constitution draft is sufficiently complete to become accepted project law.

Those decisions should happen after ADR-0073 is sufficiently closed.

---

# 19. Rejected Directions

## 19.1. "Everything Observable Is Contract"

Rejected.

That rule destroys implementation replaceability.

Observed behavior can create ecosystem debt.

It does not automatically create Contract authority.

---

## 19.2. "Documentation Silence Is Enough"

Rejected.

Repeated stable accidental behavior can become a practical dependency.

Where one distinction is materially observable, Kontrakt should define whether it is guaranteed, unstable, or
intentionally unspecified.

---

## 19.3. "Semantic Versioning Solves Compatibility"

Rejected.

Versioning is communication and coordination.

It is not a proof of behavioral compatibility.

Recent ecosystem research continues to identify behavioral breaking changes and transitive compatibility as difficult
problems.

---

## 19.4. "Same API Signature Means Same Contract"

Rejected.

Behavioral obligations may differ while shape remains identical.

---

## 19.5. "Canonical Bytes Define Meaning"

Rejected.

Canonical bytes may represent already-defined meaning.

They do not determine the semantic law that makes two values equal.

---

## 19.6. "Cache Equality Defines Semantic Equality"

Rejected.

Cache reuse is compiler realization.

Semantic equality remains owned by semantic law.

---

## 19.7. "One Stable API Promise Covers Everything"

Rejected.

Contract semantics, tooling, generated artifacts, diagnostics, plugin APIs, and experimental features have different
evolution pressures.

They require separate stability domains.

---

## 19.8. "All Nondeterminism Is Forbidden"

Rejected.

Parallel execution, distributed scheduling, random test exploration, and target-specific optimization may be
operationally nondeterministic.

The required law is that such nondeterminism does not silently alter authoritative semantics.

---

## 19.9. "Tests Define the Contract"

Rejected.

Tests verify declared law.

They do not replace it.

A test suite can be incomplete or wrong.

---

## 19.10. "Implementation Upgrade May Reinterpret Old Meaning"

Rejected.

A new compiler may fix bugs.

It may introduce new versions.

It may not silently redefine an existing exact semantic identity.

---

# 20. Non-Normative Engineering Basis

The following sources informed this document.

They are evidence rather than Kontrakt authority.

## Kontrakt project material

- *What Contract Is*
- ADR-0041: Stable Metadata Identity, BLAKE3 HID, and Protocol-Owned Interning
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0071: Resolved Contract HIR
- ADR-0073: JVM Platform-Native Contract Ratification work
- Kontrakt Compiler Total Architecture Map
- *Modern Compiler Architecture 01–15*

## Compilers

- LLVM documentation: <https://llvm.org/docs/>
- rustc incremental compilation: <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>
- HELIX: Verified compilation of cyber-physical control systems to LLVM IR, 2026: <https://arxiv.org/abs/2604.18593>

## Operating systems and capabilities

- Linux ABI documentation: <https://www.kernel.org/doc/html/latest/admin-guide/abi.html>
- seL4 capabilities: <https://docs.sel4.systems/Tutorials/capabilities.html>
- FreeBSD Capsicum: <https://docs.freebsd.org/en/books/handbook/security/>
- Typestate via Revocable Capabilities, 2025: <https://arxiv.org/abs/2510.08889>

## Databases and distributed systems

- PostgreSQL collation versioning: <https://www.postgresql.org/docs/17/sql-altercollation.html>
- FoundationDB simulation: <https://apple.github.io/foundationdb/testing.html>
- FoundationDB engineering: <https://apple.github.io/foundationdb/engineering.html>
- Jepsen consistency models: <https://jepsen.io/consistency>

## Build systems and software supply chain

- Bazel hermeticity: <https://bazel.build/concepts/hermeticity>
- Bazel remote execution: <https://bazel.build/docs/remote-execution>
- Nix derivations: <https://nix.dev/manual/nix/2.34/store/derivation/>
- SLSA provenance: <https://slsa.dev/spec/v1.2/provenance>
- in-toto: <https://in-toto.io/docs/what-is-in-toto/>
- SOURCE_DATE_EPOCH: <https://reproducible-builds.org/specs/source-date-epoch/>
- Canonicalization for Unreproducible Builds in Java, 2025: <https://arxiv.org/abs/2504.21679>

## API, protocol, and semantic evolution

- Kubernetes API: <https://kubernetes.io/docs/concepts/overview/kubernetes-api/>
- Protocol Buffers: <https://protobuf.dev/programming-guides/proto3/>
- OpenTelemetry Versioning and Stability: <https://opentelemetry.io/docs/specs/otel/versioning-and-stability/>
- OpenTelemetry Semantic Conventions: <https://opentelemetry.io/docs/specs/semconv/>
- WebAssembly Component Model: <https://github.com/WebAssembly/component-model>
- Roseau: Fast, Accurate, Source-based API Breaking Change Analysis in Java, 2025: <https://arxiv.org/abs/2507.17369>
- Breaking Changes in Software Ecosystems: A Systematic Literature Review, 2026: <https://arxiv.org/abs/2605.24397>

---

# 21. Final Working Law

Kontrakt must be designed as though another independent system will eventually depend on every public semantic promise
it makes.

That dependency must remain safe across compiler rewrites, backend replacement, host-platform change, parallel
execution, incremental compilation, plugin growth, and ecosystem evolution.

The project therefore protects one boundary above all others:

```text
declared semantic authority
        ↓
stable legal observation
        ↓
replaceable realization
```

The reverse direction is forbidden.

```text
realization accident
ambient environment
cache state
execution schedule
extension behavior
toolchain accident
        ↛
declared semantic authority
```

Determinism begins at meaning.

Reproducibility, canonical artifacts, compatibility, provenance, verification, and optimization are downstream
obligations that must preserve that meaning without becoming its source.