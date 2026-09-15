# ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary

## Status

Proposed

## Date

2026-09-16

## Related

- *What Contract Is*
- ADR-0041: Canonical Semantic Identity, HID, and Deterministic Identity Substrate
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0072: JVM Collection Contract Preservation, Aggregate Semantics, and Deterministic Equality
- Kontrakt Compiler Total Architecture Map
- Kontrakt Compiler Material and IR Architecture Review Checklist
- *Modern Compiler Architecture 01–15*

---

# 1. Context

*What Contract Is* treats external contract infiltration as a direct threat to Contract authority.

An external API does not become a Core contract because it is familiar, typed, standardized, or widely used. Frameworks,
libraries, runtime mechanisms, providers, and vendor APIs remain outside unless an explicit boundary admits material
from
them.

Kontrakt nevertheless runs on the JVM. Java and Kotlin therefore expose a small set of ordinary platform surfaces that
users must be able to use without wrapping every value in a Kontrakt-specific type.

This is a host-platform constraint. It is not general permission for external contracts to enter the governed machine.

ADR-0073 defines the narrow JVM-native exception and the boundary that separates it from Adapter-only technology.

---

# 2. Problem

Kontrakt can violate its own boundary in two opposite ways.

It can accept too much. If Java SE, `java.base`, Kotlin stdlib, or another familiar platform namespace is treated as
implicitly Native, capability, lifecycle, environment, provider, and external-technology contracts can enter the Core
through the host platform.

It can also accept too little. If ordinary JVM values require artificial wrappers only to cross a Contract-facing
boundary, Kontrakt stops behaving like a JVM compiler and forces users to abandon normal Java and Kotlin value surfaces.

The boundary is complicated by a second problem. A value-like host type may expose operations that consult a provider,
acquire ambient state, open a resource, or validate against versioned platform data. The host type, the value meaning,
and the operation that produced or resolved it are not the same semantic question.

A third problem appears when platform meaning depends on versioned data. Time-zone rules, Unicode data, locale data, or
other platform tables may differ across JDK update, vendor, machine, deployment, or execution time. JDK version alone is
not a sufficient semantic identity for every such meaning.

Kontrakt must therefore decide:

```text
what JVM surface may be admitted directly,
what authority or provider state must remain outside,
what platform meaning must be preserved,
and what semantic basis makes a versioned platform judgment reusable.
```

Everything outside the ratified Native boundary remains Adapter-required or unsupported.

---

# 3. Decision

Kontrakt defines an explicitly ratified **JVM Platform-Native Surface**.

Platform-Native status is owned by Kontrakt. It is not selected by the user and is not inferred from package membership,
classpath presence, runtime availability, inheritance, assignability, or popularity.

Kontrakt separates three semantic kinds when it ratifies JVM surfaces:

```text
Closed Platform Value
Platform Capability or Resource
Platform Operation
```

A Closed Platform Value may participate directly in a Contract-facing role when its admitted meaning is closed.

A Capability or Resource remains behind an Adapter or another explicitly approved external boundary even when it is part
of Java SE or Kotlin stdlib.

A Platform Operation is judged independently from its receiver, argument, or result type. A Native value does not make
every operation on that value Native.

Ratification does not transfer Contract authority to Java, Kotlin, the JVM, a provider, or a platform-data release. It
creates a preservation obligation for the platform meaning that Kontrakt has chosen to admit.

Kontrakt may apply its own 1D Contract law at a later explicit Contract boundary. It may not silently rewrite earlier
platform meaning to make compilation, verification, or optimization easier.

---

# 4. Platform-Native Boundary

## 4.1. Explicit Ratification

A platform surface is Native only after Kontrakt has explicitly ratified it.

The following do not establish Native status:

```text
java.*
java.base
kotlin.*
kotlin-stdlib
default imports
runtime classpath presence
```

Namespace or module membership is evidence only.

Provider-backed, environment-relative, lifecycle-bearing, capability-bearing, or otherwise incompletely understood
surface is Adapter-required or unsupported by default. Native treatment requires successful explicit ratification.

---

## 4.2. Contract-Relevant Interpretation

Kontrakt does not re-model the whole JVM contract of every ratified surface.

It must understand every platform distinction that remains legally observable through the Contract boundary or user
realization in which the surface is admitted.

A JVM implementation detail that no legal observer can depend on does not become Contract meaning.

A guaranteed platform distinction that a legal observer may rely on cannot be dropped merely because Kontrakt has a more
convenient internal representation.

---

## 4.3. Later 1D Authority

Platform preservation does not freeze one value meaning for the whole pipeline.

A later 1D Contract may reject the material or establish a different meaning under its own authority.

For example, Canonicalization may establish a new equivalence relation that differs from the equality relation of the
original platform value. That does not retroactively change what the earlier platform-facing value meant.

---

# 5. Ratification Laws

Ratification is governed by Closure, Authority Isolation, and Preservation. The more specific laws in this section
refine
those three requirements.

## 5.1. Closure Law

Kontrakt must be able to close the relevant meaning of the admitted surface.

The compiler must know the observable obligations needed by the admitted role and the constituent meaning required to
interpret them.

A known outer surface does not make an unknown constituent legal.

`List<ExternalResource>` therefore does not become Native merely because the standard `List` surface is ratified.

---

## 5.2. Closed Value Law

A Platform-Native value must be semantically closed over the admitted value and its admitted constituents.

Observing, copying, comparing, or otherwise using that value within its admitted contract must not, by possession alone,
grant authority over live external state, acquire ambient state, require an independently evolving lifecycle owner, or
invoke externally supplied behavior.

Immutability, final fields, serializability, or value-like syntax do not by themselves establish closure.

---

## 5.3. Authority Isolation Law

Explicit presentation does not convert a capability into a value.

If possession or invocation grants access to live external state, external behavior, lifecycle control, environment,
provider state, or another effect authority, the surface is a Capability regardless of its Java representation.

This rule applies even when the capability is passed as an explicit parameter rather than acquired ambiently.

---

## 5.4. Resource Law

A live entity with independent state or lifecycle is not a Platform-Native Contract value.

A handle, reference, token, or object that grants authority over that entity does not become a value merely because the
carrier is immutable, serializable, or represented by `String`, `byte[]`, UUID-shaped material, or another ordinary
Native carrier.

A descriptive coordinate and authority over the target identified by that coordinate are separate meanings.

---

## 5.5. No Laundering Law

A Native aggregate, wrapper, interface, identifier shape, or carrier does not launder a Capability or Resource
constituent into Native value meaning.

The same rule applies to generic constituents, nested aggregates, optional containers, arrays, maps, and user-defined
carriers.

Carrier shape never upgrades the authority class of the contained meaning.

---

## 5.6. Operation Independence Law

Ratification of a value surface does not ratify every constructor, factory, parser, validator, lookup, resolver, method,
or static operation associated with that value.

An operation is judged from its own semantic inputs, authority reach, legal observations, and result meaning.

A Native result type does not make the operation that produces it Native.

---

## 5.7. Preservation Law

Kontrakt may directly admit only the meaning it can preserve for every remaining legal observer.

When Contract-established knowledge does not justify transformation, Kontrakt keeps the original platform behavior and
leaves ordinary JVM optimization to the JVM.

When Kontrakt transforms a ratified platform surface, every remaining legal platform and Contract observation must be
preserved.

If preservation cannot be established, the transformation is not performed.

Uncertainty is not permission to guess.

---

# 6. Descriptor, Formation, Validation, and Resolution

A descriptor meaning, the host carrier that presents it, the operation that forms it, and the operation that resolves it
are separate semantic questions.

## 6.1. Descriptor Separation Law

A stable descriptor does not acquire the semantics of the external or versioned knowledge that the descriptor may
identify.

A time-zone identifier is not the same meaning as the time-zone rules associated with that identifier. A currency code
is
not the same meaning as locale-sensitive currency metadata. A URI-like coordinate is not authority to dereference the
resource it names.

A descriptor may therefore be a Closed Platform Value even when some operations that validate or resolve it are not
Native.

---

## 6.2. Formation Independence Law

Ratification of a descriptor or result value does not ratify the constructor, factory, parser, lookup, or validation
operation used to obtain one host carrier for that value.

Formation is independently judged under the Operation Closure Law.

This prevents provider-backed creation from inheriting Native status merely because the resulting carrier is value-like.

---

## 6.3. Operation Closure Law

A Platform operation is Native only when every semantic input required for its admitted behavior is either:

```text
an explicit admitted operand,
or
an explicitly ratified Closed Platform Semantic Basis.
```

An installed provider set, default filesystem, current time zone, host locale, service registry, network environment,
mutable registry, or another unbound ambient source does not satisfy this law.

---

## 6.4. Validation Basis Law

Validation is Native only to the extent that the domain against which validation is performed is closed under the
ratified semantic basis.

Validation against a fixed grammar or another closed rule set may be Native.

Validation against runtime-selected, dynamically discoverable, mutable, environment-relative, or otherwise unbound
provider state is Adapter-required.

Validation against versioned platform data may be Native only when that exact semantic basis satisfies Section 7.

---

## 6.5. Provider-Validated Formation Law

An operation that constructs or validates a descriptor by consulting provider-backed or environment-selected state is
not
made Native by the fact that its result is a Closed Descriptor.

The formation operation and the resulting descriptor are classified independently.

---

## 6.6. Resolution and Dereference Law

An operation that turns a descriptor into live external state, provider-owned semantic state, resource authority, or
another capability crosses the Adapter boundary unless Kontrakt has separately ratified that exact closed operation.

Descriptor possession therefore does not imply resolution authority.

---

## 6.7. Validation Non-Elevation Law

Successful validation establishes only the meaning guaranteed by the exact validation basis.

It does not establish future provider availability, future resolution equivalence, resource existence, stable external
state, or capability authority beyond that basis.

Validation cannot be used to promote a later provider lookup into a Closed Platform operation.

---

## 6.8. Result Separation Law

Operation classification and result classification are independent.

An Adapter operation may produce a Native closed value when the resulting admitted meaning is independently closed and
carries no external authority with it.

Conversely, Native arguments or Native result shape do not make a capability-producing operation Native.

---

# 7. Closed Versioned Platform Data

Some platform meaning depends on released semantic data without depending on a live provider during every observation.
Such data is not automatically a Capability.

Kontrakt calls the exact meaning required for one such judgment a **Platform Semantic Basis**.

## 7.1. Platform Semantic Basis Law

A Platform Semantic Basis is the minimal closed set of specification-owned, released, or otherwise ratified semantic
inputs required to determine the admitted meaning of one Platform surface or operation for one legal observation.

The basis is domain-scoped.

The entire JDK, JVM installation, or host machine is not automatically the basis merely because one relevant data table
is distributed with that runtime.

---

## 7.2. Closed Platform Data Law

Versioned platform data may participate in Native semantics only when the relevant data meaning is closed, explicitly
ratified, and stable for the legal observation that depends on it.

A versioned data snapshot is not a live capability merely because it originated outside Kontrakt.

A runtime-discovered, mutable, replaceable, environment-selected, or provider-selected source is not Closed Platform
Data.

---

## 7.3. No Ambient Provider Law

Runtime-selected provider state, installed-provider contents, service-loader results, host defaults, host locale,
runtime time zone, classpath discovery, system properties, or another ambient provider set must not determine
Contract-visible Native meaning merely because the operation is syntactically explicit.

Syntactic explicitness is not semantic closure.

---

## 7.4. Basis Identity and Compatibility Separation Law

Exact Platform Semantic Basis identity and semantic compatibility are different judgments.

Basis identity asks whether two judgments consumed the same ratified semantic basis.

Compatibility asks whether an actual basis satisfies the semantic requirement of a particular legal observation or
consumer.

Compatibility is therefore consumer-relative and may be directional. It is not a single global equivalence relation
between platform releases.

---

## 7.5. Compatibility Burden Law

Different basis identity is incompatible by default.

Compatibility exists only when Kontrakt has explicit evidence sufficient for the exact admitted observation.

A release label, JDK update number, vendor name, or similar coordinate is not evidence of compatibility by itself.

Unknown compatibility fails closed.

---

## 7.6. Version Non-Authority Law

A JDK, TZDB, Unicode, CLDR, provider, or other version coordinate may identify a Platform Semantic Basis only when the
owning ratification law establishes that the coordinate completely identifies the semantic data relevant to the
observation.

Version strings do not become Contract authority merely because they are convenient cache keys or deployment labels.

---

## 7.7. Observation-Scope Closure Law

Closed Platform Data is closed only for an observation scope in which every semantic basis required by that observation
is fixed or explicitly compatible.

The relevant scope may cross time, process lifetime, runtime, machine, vendor, deployment, persistence, or artifact
reuse.

Process-local stability and cross-runtime stability are not assumed to be the same property.

---

## 7.8. Basis Consumption Law

A Platform Semantic Basis becomes a dependency of a compiler judgment or compiler product only when that judgment,
transformation, specialization, validation, or retained result actually consumes semantic information from that basis.

Platform data that the compiler does not consult is not made a compiler dependency merely because the current host JVM
contains it.

Delegating an operation unchanged to the JVM therefore does not automatically bind the compiled result to the compiler
host's corresponding platform data.

---

## 7.9. Compilation and Execution Compatibility Law

When a compiler product embeds, preserves, specializes, validates, or reuses meaning derived from Versioned Platform
Data, execution or reuse under another basis is legal only when the actual basis satisfies the recorded semantic
requirement.

The required response to incompatibility may be recomputation, revalidation, fallback to an unspecialized path, or
refusal. The choice is Design and backend work.

The law is only that incompatible basis must not be treated as unchanged meaning.

---

## 7.10. Basis Drift Law

A change in relevant Platform Semantic Basis requires every dependent judgment to be reconsidered.

It does not imply that every downstream Contract or compiler product has changed. A dependent recomputation may
establish
that the consumer-relevant semantic result is unchanged and may then stop further invalidation.

The exact reuse and early-cutoff mechanism is outside this ADR.

---

## 7.11. Identity Substrate Delegation Law

When a Platform Semantic Basis requires stable compiler identity, canonical bytes, HID, fingerprinting, or persistent
identity substrate, ADR-0041 owns that mechanism.

ADR-0073 first defines what semantic material constitutes the basis and what compatibility means.

ADR-0041 may then provide compact identity for the already-ratified material.

Neither equal HID nor unequal HID independently defines semantic compatibility.

---

# 8. Ratification Verification

## 8.1. Verification Domain

Platform ratification is compiler verification work.

It belongs in the Verification domain, but it is not Contract Establishment and it does not become Contract authority.

Platform Boundary Verification is a sibling of Contract and realization verification concerns. It verifies whether the
compiler may rely on one platform surface under the laws of this ADR.

---

## 8.2. Full Ratification Audit

Kontrakt performs the expensive audit when support for a platform surface, operation, semantic basis, or relevant
platform
release is created or changed.

The audit applies the laws of this ADR and checks the normative platform contract. It is not repeated from first
principles for every user compilation.

The audit must reject a surface when its relevant meaning cannot be closed, when authority cannot be isolated, or when
required semantic basis cannot be identified soundly.

---

## 8.3. Ratified Native Surface Catalog

Successful audit produces compiler-owned **Ratified Native Surface Catalog** material.

The catalog records already-audited platform knowledge needed for later compilation. It is not a flat class-name
whitelist and it is not Contract authority.

Catalog knowledge concerns the platform surface itself. It does not duplicate one entry for every Kontrakt role or every
generic instantiation in which the surface may later appear.

The compiler must be able to recover enough audited knowledge to determine:

```text
the exact ratified platform surface,
its platform classification,
the observable obligations Kontrakt has ratified,
its required Platform Semantic Basis when one exists,
its relation to reusable operation knowledge,
any exact operation-specific exception,
and whether direct use is Native, delegated, Adapter-required, or unsupported.
```

A missing catalog entry does not imply permission.

The physical representation of the catalog is Design work.

Deleting or rebuilding the catalog cannot change which result is correct. A wrong catalog entry is a compiler defect.

---

## 8.4. Compilation Surface Gate

Normal user compilation does not rerun the full ratification audit.

The compiler resolves only the platform surfaces actually encountered by the compilation and checks them against the
ratified catalog.

The compilation gate performs the context-sensitive legality checks that cannot be precomputed globally. These include
the requested Kontrakt role, constituent closure, owning boundary, and any semantic-basis requirement that is relevant
to
that use.

Platform knowledge and use legality are different products.

---

## 8.5. External Technology Isolation Gate

Platform ratification and external-technology isolation are related but different checks.

Compilation or realization verification must still reject Adapter-only capability, framework, provider, or external
technology that attempts to cross into governed material through a ratified platform surface.

A Native carrier does not legalize the external authority that produced or remains reachable through it.

---

## 8.6. Independent Catalog Validation

The catalog itself requires independent validation appropriate to a compiler release or platform-support update.

Kontrakt must be able to compare built-in platform knowledge with the normative platform surface and run positive and
negative conformance tests without relying only on the same fast compilation lookup path.

The exact validation tooling is Design and quality work.

---

# 9. Ratification Unit and Use Legality

Ratification is not attached to a package, module, runtime class hierarchy, or one global class-level boolean.

Kontrakt separates stable platform knowledge from compilation-specific use legality.

## 9.1. Exact Platform Surface

The release-time audit is anchored to an **Exact Platform Surface**.

An Exact Platform Surface is the smallest platform declaration surface for which Kontrakt can state one coherent
ratification result without importing unrelated behavior.

It may represent a value or type surface, an interface surface, or an exact callable surface.

Package membership is too broad. Runtime class identity alone is also insufficient when Java and Kotlin expose different
source-level contracts over one JVM representation.

The physical key used to identify the surface is compiler implementation work.

---

## 9.2. Platform Surface and Kontrakt Role Are Separate

The platform contract of a surface is audited independently from the Kontrakt role in which a user later requests it.

The catalog therefore does not pre-expand one surface into every Input, Fact, Operation, Output, or later Contract role.

Normal compilation combines ratified platform knowledge with the requested role and owning boundary to determine use
legality.

---

## 9.3. Constituents Are Checked by Closure

Generic arguments, array components, map keys and values, nested aggregates, and other constituent meaning are not
pre-expanded into every possible catalog combination.

The outer platform surface is audited once. Actual compilation verifies the resolved constituents required by the use.

`List<BigDecimal>` therefore does not require a dedicated release-time catalog entry.

---

## 9.4. Operations Are Separate from Type Ratification

Operation audit is anchored to an exact callable identity sufficient to distinguish owner, overload, and signature.

This exact identity is required even when several callables share the same audited semantic knowledge.

The ADR does not prescribe the physical encoding of that identity.

---

## 9.5. Semantic Operation Profile

Repeated platform-operation meaning may be represented by a reusable **Semantic Operation Profile**.

The profile is a conservative summary of already-audited platform potential and obligation required for Platform
Boundary
Verification. It is not a purity enum and it is not a universal JVM effect system.

The profile may summarize the platform facts needed to decide authority isolation, legal observation, control or
lifecycle boundary, and preservation.

The direction is:

```text
platform callable contract
    -> ratification audit
    -> reusable Semantic Operation Profile
```

It is never:

```text
Semantic Operation Profile
    -> platform meaning
```

An exact callable-specific fact or exception takes precedence over a shared profile.

Concrete call targets, alias sets, escape paths, memory Mod/Ref sets, synchronization edges, exception paths, capability
propagation, and other user-program execution facts remain Derived Realization Analysis rather than Platform Profile
meaning.

The exact profile vocabulary and physical schema are Design work.

---

## 9.6. Delegated Operations

The catalog is not a reimplementation of the Java or Kotlin API.

When an admitted operation requires no Contract-specific reinterpretation or transformation, Kontrakt may conservatively
delegate the original platform behavior to the JVM.

Delegation is a ratified result. It is not inferred from missing catalog knowledge.

If a delegated operation later becomes subject to a Kontrakt transformation, the Preservation Law applies before that
transformation is legal.

---

## 9.7. Platform Use Verification

Compilation-time Platform Use Verification combines:

```text
Ratified Platform Surface
Requested Kontrakt Role
Resolved Constituents
Relevant Owning Boundary
Applicable Platform Semantic Basis requirement
```

and determines whether that use is directly legal, carrier-only, Adapter-required, or unsupported.

The exact compiler product name, query shape, and storage representation remain Design work.

---

# 10. Native, Carrier, Adapter, and Unsupported Are Different Results

Ratification is not one global boolean attached to a JVM class.

A platform surface may be directly Native for one role. A concrete implementation may be accepted only as a carrier of
that surface. Another operation may require an Adapter even though its receiver and result are Native values.

An unknown or incompletely understood surface is unsupported until Kontrakt explicitly decides otherwise.

This ADR therefore distinguishes these semantic outcomes:

```text
Native Surface
Carrier Only
Adapter Required
Unsupported
```

The implementation does not need to encode them as one enum.

---

# 11. Initial V1 Audit Scope

The initial V1 audit scope includes the JVM value surfaces already considered unavoidable for ordinary Java and Kotlin
Contract usage.

It includes primitive values, the ordinary value aspect of primitive wrappers, `String`, arrays including primitive
arrays, language enum values, the standard Java and Kotlin collection surfaces governed by ADR-0072, `BigInteger`,
`BigDecimal`, and selected value-oriented `java.time` surfaces whose admitted meaning can satisfy this ADR.

This scope does not imply that every operation reachable from those types is Native.

Provider-backed formation, validation, resolution, ambient time acquisition, environment lookup, and similar operations
remain independently classified.

Additional surfaces require the same ratification process.

---

# 12. Adapter Boundary

Frameworks, optional libraries, vendor APIs, live resource handles, ambient environment access, provider authority, and
external technology do not become Native merely because they execute on the JVM.

Such material enters through an explicit Adapter or another separately approved external boundary before it may
influence governed Contract processing.

An Adapter may produce a ratified platform value or user-defined candidate material. The external authority that
produced
the value does not accompany a closed result into the Core.

Unknown external technology fails closed.

---

# 13. Frontend, HIR, Establishment, and Realization Boundary

Frontend resolution recognizes the exact referenced JVM surface and carries enough resolved information for Platform
Boundary Verification.

HIR does not reproduce Java or Kotlin library internals. It preserves the platform distinctions that a later Contract
authority or legal user-realization observation still requires.

Platform ratification is not Contract Establishment. This ADR does not create a parallel `Established Platform Material`
authority.

The owning Contract establishes its own meaning under ADR-0063. A later Contract authority may establish different
meaning under its own law without rewriting the earlier platform contract.

User realization may use a ratified JVM surface where the applicable role permits it. Realization verification still
applies the Adapter boundary to actual implementation behavior.

The exact producer-consumer relationship between HIR products, Established Material, Realization Analysis, and Platform
Boundary Verification remains open until the remaining ratification questions are closed. Existing semantic products
should be reused rather than reconstructed where doing so preserves ownership and verification independence.

---

# 14. Optimization Non-Interference

Kontrakt optimization is strongest where Contract-established knowledge gives the compiler information that the JVM does
not have.

That advantage does not grant permission to replace unrelated platform behavior.

A transformation that changes a ratified platform realization is legal only when every remaining legal platform and
Contract observation is preserved.

If the compiler cannot establish preservation, the original platform behavior is kept and the JVM retains optimization
responsibility.

This ADR does not choose MIR, LIR, physical layout, object elimination, collection realization, or another optimization
mechanism.

---

# 15. Reuse and Incremental Boundary

The expensive ratification audit is not a per-use compilation activity.

V1 may ship pre-audited catalog material and reuse valid Exact Platform Surface classification and Platform Use
Verification results within one compiler generation.

Stable platform knowledge, Platform Semantic Basis identity, and context-specific use legality remain separate reuse
boundaries.

V2 may persist or incrementally repair these products when their explicit inputs and validity law permit it.

A basis change requires dependent judgments to be reconsidered, but consumer-relevant equality may stop invalidation
when
recomputation establishes that the relevant result is unchanged.

Cache state, query topology, catalog rows, Semantic Operation Profiles, and fingerprints do not become authority.

Clean recomputation remains the correctness reference.

The exact persistent key, scheduling policy, invalidation algorithm, semantic-basis encoding, and repair mechanism
remain
Design work.

---

# 16. Refusal Boundary

An unsupported or ambiguous platform surface is rejected before it becomes Contract authority.

A surface that requires an Adapter is rejected when it is used in a direct Native role without that boundary.

An incompatible or unresolved Platform Semantic Basis is not treated as unchanged meaning.

A forbidden platform or external-technology operation reached by user realization is a realization-verification failure,
not a valid Contract Failure during governed execution.

Diagnostics explain the violated boundary. They do not decide Native status.

---

# 17. Determinism

Platform ratification and compilation-time classification are deterministic compiler products.

For the same explicit compiler inputs, ratified platform knowledge, and relevant Platform Semantic Basis, worker
scheduling, cache state, filesystem order, reflection order, runtime provider enumeration order, or unrelated host-JDK
state cannot change the result.

A fast cached path and a clean uncached path must agree.

---

# 18. Intentionally Open

The exact Ratified Native Surface Catalog schema remains open.

The exact Semantic Operation Profile vocabulary and physical representation remain open.

The exact compiler representation of Platform Semantic Basis remains open.

The exact mechanism for discovering, canonicalizing, storing, or checking TZDB, Unicode, CLDR, charset, provider, or
other
versioned semantic data remains open.

The exact runtime or backend response to basis incompatibility remains open.

The exact HIR fields, query keys, memoization structure, incremental dependency graph, and persistent reuse format
remain
owned by their respective ADR and Design work.

The exact producer-consumer relationship among HIR, Established Material, Realization Analysis, and Platform Boundary
Verification will be decided after the remaining ratification questions are closed.

The exact set of additional Native values and Native operations remains open.

Framework-specific Adapter generation remains outside this ADR.

---

# 19. Rejected Directions

## 19.1. Native by Namespace

Rejected.

A standard namespace contains both ordinary values and capability or runtime mechanisms.

---

## 19.2. Native by Runtime Availability

Rejected.

Classpath presence, host-JDK availability, inheritance, or assignability does not prove ratification.

---

## 19.3. Immutable Means Closed

Rejected.

An immutable or final carrier may still depend on provider state, represent a live capability, or expose lifecycle
authority.

---

## 19.4. Explicit Capability Means Native

Rejected.

Passing a capability explicitly does not convert it into a Closed Platform Value.

---

## 19.5. Native Type Means Every Operation Is Native

Rejected.

A legal value surface may expose provider-backed formation, ambient acquisition, resolution, or capability operations.

---

## 19.6. Descriptor Means Formation Is Native

Rejected.

A descriptor may be closed while the operation that constructs or validates one consults live provider state.

---

## 19.7. Validation Elevates Future Resolution

Rejected.

Successful validation does not establish future provider availability, future equivalence, or authority over a resolved
resource.

---

## 19.8. JDK Version as Universal Semantic Basis

Rejected.

Different platform domains may carry different versioned semantic data, and the relevant semantic basis may change
without one global JDK coordinate fully identifying that change.

---

## 19.9. Version Label as Semantic Authority

Rejected.

A version label identifies relevant semantic data only when the owning ratification law establishes that it completely
identifies that basis.

---

## 19.10. Global Compatibility Between Platform Releases

Rejected.

Compatibility is judged for the exact consumer and legal observation. It is not one universal relation between two JDK,
TZDB, Unicode, CLDR, or provider releases.

---

## 19.11. Full Audit on Every Compilation Use

Rejected.

The expensive platform audit belongs to compiler/platform-support work. Normal compilation performs exact lookup and
context-sensitive legality checks only for surfaces actually used.

---

## 19.12. Catalog as Authority

Rejected.

The catalog is verified compiler knowledge. It does not define Contract meaning.

---

## 19.13. One Global Native Boolean per JVM Class

Rejected.

Value meaning, carrier legality, formation, validation, resolution, and other operations may have different results.

---

## 19.14. Role-Expanded Platform Catalog

Rejected.

Platform knowledge is not duplicated for every Kontrakt role. Role legality is checked when the ratified surface is
used.

---

## 19.15. Catalog Entry per Generic Instantiation

Rejected.

Generic and aggregate combinations are checked by constituent closure during compilation.

---

## 19.16. One Independent Semantic Model per Callable

Rejected.

Exact callable identity is required, but repeated platform obligations may share a Semantic Operation Profile. Exact
callable exceptions remain independently expressible.

---

## 19.17. Delegation by Catalog Absence

Rejected.

Unknown operations fail closed. JVM delegation is a ratified result.

---

## 19.18. Platform Optimization by Default Replacement

Rejected.

Kontrakt transforms platform behavior only when preservation is established. Otherwise the JVM retains that
responsibility.

---

# 20. Consequences

Java and Kotlin remain natural user surfaces without making the JVM ecosystem part of Contract authority.

The Native boundary is stricter than a type whitelist. It distinguishes closed value meaning, capability or resource
authority, and the operations that form, validate, resolve, or transform those meanings.

Provider-backed and environment-relative behavior cannot enter merely through a value-like standard type.

Versioned platform data can participate in Native semantics when its exact semantic basis is closed and ratified,
without
turning all provider-backed behavior into Adapter-only behavior.

Compiler products depend only on the Platform Semantic Basis they actually consume. Unrelated host-platform data does
not
become an accidental compiler dependency.

Exact basis identity and semantic compatibility remain separate. ADR-0041 supplies identity substrate only after this
ADR
has defined the semantic basis.

Normal compilation stays cheap because expensive platform ratification is performed outside the ordinary per-use path.

The Adapter airlock remains strong. Framework, resource, provider, environment, and vendor authority cannot enter merely
through a standard-looking JVM carrier.

Frontend and HIR gain a preservation duty but do not become platform reimplementation layers.

Optimization remains conservative at the JVM boundary. Kontrakt exploits Contract-specific knowledge where it has actual
semantic authority and otherwise leaves platform behavior to the JVM.

---

# 21. Non-Normative Engineering Basis

Production compilers commonly separate expensive compiler-known platform knowledge from the hot user-compilation path.
LLVM TargetLibraryInfo binds known library semantics to exact functions and target availability rather than treating an
entire library namespace as uniformly special. Rust compiler-known items are explicit rather than inferred from the
whole
standard library. Clang generates compact builtin knowledge rather than rediscovering standard surfaces from source on
every compilation.

Object-capability systems provide a related security principle. No-Ambient-Authority reasoning distinguishes possession
of explicit descriptive material from implicit authority to reach external state. ADR-0073 applies that separation to
the
JVM host boundary without adopting an object-capability runtime architecture.

WebAssembly Component Model similarly distinguishes plain values from resources and resource handles. This supports the
semantic separation between closed value material and lifetime-bearing authority.

PostgreSQL collation handling demonstrates why provider-backed semantic data cannot be identified only by a broad
runtime
version. Provider-version drift can invalidate previously built semantic artifacts such as indexes. ICU likewise treats
collation and other data versions as relevant to persisted semantic results.

Build systems such as Bazel, Buck2, and Nix demonstrate the corresponding dependency law: hidden ambient inputs
undermine
correct reuse. Kontrakt applies the same principle at a finer semantic level by recording only the Platform Semantic
Basis
that a compiler judgment actually consumes.

Recent Java reproducibility work reports 12,283 unreproducible artifacts within its unreproducible subset and shows that
environment and toolchain variation are practical sources of non-reproducibility. This supports treating environment and
platform data as explicit semantic dependencies when they actually affect compiler-produced meaning.

These systems and studies are engineering references. They do not define Kontrakt Contract authority.