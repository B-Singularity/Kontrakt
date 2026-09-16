# ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary

## Status

Accepted

## Date

2026-09-16

## Related

- *What Contract Is*
- ADR-0041: Canonical Semantic Identity, HID, and Deterministic Identity Substrate
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary
- ADR-0068: Fact Contract
- ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0072: JVM Collection Contract Preservation, Aggregate Semantics, and Deterministic Equality
- Kontrakt Compiler Total Architecture Map
- Kontrakt Compiler Material and IR Architecture Review Checklist
- *Modern Compiler Architecture 01–15*

---

# 1. Context

*What Contract Is* treats external contract infiltration as a direct threat to Contract authority.

Kontrakt nevertheless runs on the JVM, so ordinary Java and Kotlin value surfaces must remain usable without forcing
every value through a Kontrakt-specific wrapper. That host-platform convenience cannot become a second source of
authority.

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

A third problem appears when Kontrakt itself consumes versioned platform data while forming, validating, specializing,
or retaining compiler-produced meaning. Time-zone rules, Unicode data, locale data, or other platform tables may differ
across release, vendor, machine, deployment, or execution time. A broad JDK version is not a sufficient semantic
identity
for every compiler dependency. The same platform data need not become a Kontrakt dependency when the corresponding
admitted operation is left unchanged and its execution remains governed by the applicable platform specification.

A fourth problem is that physical JVM observability is broader than the meaning that a Contract boundary needs. Object
identity is one example. Platform execution can expose other distinctions whose meaning is already owned by Java,
Kotlin,
or the JVM. Kontrakt needs to know when such a distinction constrains a boundary mapping or a compiler transformation
without turning it into a second Contract model.

The opposite ownership error is equally serious. A platform operation that Kontrakt does not reinterpret or transform
must remain platform-owned execution. If Kontrakt rewrites JMM ordering or class initialization as internal Contract
law,
it duplicates the host platform and makes compiler realization part of Contract authority.

Kontrakt must therefore decide which JVM surfaces may cross a Contract-facing boundary directly, which external
authority
must remain behind an Adapter, which platform-owned distinctions constrain Kontrakt-owned transformation or projection,
and which platform data becomes a compiler dependency only because Kontrakt actually consumed it.

Everything outside the ratified Native boundary remains Adapter-required or unsupported.

# 3. Decision

Kontrakt inserts an explicit JVM platform-ratification boundary between host-language resolution and direct
Contract-facing use.

The boundary classifies platform material and operations without turning Java, Kotlin, or JVM semantics into a second
Kontrakt Contract. Sections 4 through 17 own the normative details.

# 4. Platform-Native Boundary

## 4.1. Explicit Ratification

A platform surface is Native only after Kontrakt has explicitly ratified it. Ratification status is compiler-owned; user
code cannot mark an arbitrary platform surface Native.

The following do not establish Native status:

```text
java.*
java.base
kotlin.*
kotlin-stdlib
default imports
runtime classpath presence
```

Namespace or module membership is evidence only. A standard namespace can contain both closed value surfaces and
authority-bearing mechanisms.

Provider-backed, environment-relative, lifecycle-bearing, capability-bearing, or otherwise incompletely understood
surface is Adapter-required or unsupported by default. Native treatment requires successful explicit ratification.

## 4.2. Contract-Relevant Interpretation

Kontrakt does not re-model the whole JVM contract of every ratified surface. It records only the platform knowledge
needed to decide a Kontrakt boundary or to preserve behavior that Kontrakt itself changes.

Sections 5.8 through 5.12 own Legal Platform Observation. Section 4.4 separately owns the distinction between platform
semantic ownership and Core admissibility.

## 4.3. Later 1D Authority

Platform preservation does not freeze one value meaning for the whole pipeline.

A later 1D Contract may reject the material or establish a different meaning under its own authority.

For example, Canonicalization may establish a new equivalence relation that differs from the equality relation of the
original platform value. That does not retroactively change what the earlier platform-facing value meant.

## 4.4. Platform Ownership and Core Admissibility Separation

The applicable platform specification defines the semantics of platform behavior. Kontrakt separately decides whether a
user realization may rely on that behavior inside the governed Core.

A behavior does not become Core-admissible merely because it is standardized, executable by the JVM, or fully specified.
Refusing a use inside Core does not redefine or weaken the platform behavior itself.

Post-admission platform realization freedom is governed by Sections 9.9 and 14.

# 5. Ratification Laws

Ratification is governed by Closure, Authority Isolation, and Preservation. Within those laws, Kontrakt distinguishes
three semantic kinds:

```text
Closed Platform Value
Platform Capability or Resource
Platform Operation
```

The kind describes the meaning being admitted. It is not a JVM class hierarchy and it is not one global property of a
carrier.

## 5.1. Closure Law

Kontrakt must be able to close the relevant meaning of the admitted surface.

The compiler must know the observable obligations needed by the admitted role and the constituent meaning required to
interpret them.

A known outer surface does not make an unknown constituent legal.

`List<ExternalResource>` therefore does not become Native merely because the standard `List` surface is ratified.

---

## 5.2. Closed Value Law

A Platform-Native value may cross a Contract-facing boundary directly only when the material presented to the owning
Contract is closed over the admitted value and its admitted constituents.

Possession of that material must not preserve live authority over external state, require an independently evolving
lifecycle owner, or retain externally supplied behavior that can later change the material seen by the Contract. A
mutable external carrier therefore cannot remain the authority for established meaning merely because its reference was
accepted once.

Physical mutability is not the determinant. A point-in-time external value may be used when a stable Contract-facing
presentation exists before authoritative Contract judgment begins. Later mutation through the original carrier or
another external alias must not change the meaning of that occurrence.

The presentation must correspond to one legal observation of the source material. Material assembled from mutually
inconsistent observations does not satisfy closure merely because the resulting representation is immutable.

The Contract requires semantic isolation, coherence, and preservation of every downstream distinction that remains
authoritative. It does not prescribe the physical stabilization mechanism. Internal temporary mutation that does not
escape into Contract authority remains realization and is not prohibited by this law.

An immutable or final carrier is not sufficient evidence of closure. The same is true of a read-only view or an
unmodifiable wrapper. A deferred carrier remains outside Closed Value treatment when later execution can still reach
behavior, mutable backing state, provider state, or resource lifetime.

## 5.3. Authority Isolation Law

Explicit presentation does not convert a capability into a value.

If possession or invocation grants access to live external state, external behavior, lifecycle control, environment,
provider state, revocable authority, or another effect authority, the surface is a Capability regardless of its Java
representation.

This rule applies even when the capability is passed as an explicit parameter rather than acquired ambiently.

Freezing, serializing, copying, or otherwise stabilizing the current representation does not convert live authority into
a Closed Platform Value. Such a transformation may produce a closed observation about the authority, but that
observation
is not the authority itself.

## 5.4. Resource Law

A live entity with independent identity, continuing state, lifecycle, revocation, or future state transition is not a
Platform-Native Contract value when those properties remain part of its meaning.

A handle, reference, token, or object that grants authority over that entity does not become a value merely because the
carrier is immutable, serializable, or represented by `String`, `byte[]`, UUID-shaped material, or another ordinary
Native carrier.

A descriptive coordinate and authority over the target identified by that coordinate are separate meanings. A closed
observation of a live entity may cross a Contract boundary under an owning Contract law, but the live entity itself
remains outside Closed Value meaning.

## 5.5. No Laundering Law

A Native aggregate, wrapper, interface, identifier shape, or carrier does not launder a Capability or Resource
constituent into Native value meaning.

The same rule applies to generic constituents, nested aggregates, optional containers, arrays, maps, and user-defined
carriers.

Carrier shape never upgrades the authority class of the contained meaning.

---

## 5.6. Operation Independence Law

A platform operation is ratified independently from the type that receives or returns its values.

The operation audit must consider platform execution that can affect boundary legality. A static call may trigger class
initialization before its body runs. Another operation may consult process-wide state or invoke behavior supplied from
outside the closed realization. Looking only at the apparent receiver and result is therefore insufficient.

This requirement does not transfer those execution semantics to Kontrakt. The audit asks only whether unchanged platform
execution crosses a Kontrakt authority boundary and whether a later Kontrakt transformation would need to preserve a
platform-owned distinction.

A Native result type does not make the operation that produces it Native.

## 5.7. Preservation Law

The Preservation Law applies when Kontrakt changes, substitutes for, or re-projects platform behavior after the
corresponding use has passed Core admissibility. It does not require Kontrakt to re-prove behavior that remains owned
and
executed behind the admitted platform boundary.

A Kontrakt-owned transformation is legal only when the resulting behavior remains within the behavior permitted by the
applicable Java, Kotlin, or JVM contract for every platform distinction that is still relevant to that transformation or
to a Contract-facing projection. Preservation is not limited to comparing final values. A source-language order or an
abrupt completion point may constrain the observable execution prefix even when the eventual value would otherwise be
the same.

When the platform contract permits more than one behavior, Kontrakt may choose a deterministic refinement only when the
choice remains within that permitted behavior for every relevant observation. A compiler choice does not become a new
platform guarantee merely because Kontrakt always makes the same choice.

Contract-established knowledge may justify a stronger realization only within the meaning owned by that Contract. It
does not rewrite unrelated platform semantics. If Kontrakt cannot establish preservation for a transformation, the
transformation is not performed. This fallback does not bypass Core admissibility; it is available only for a platform
use
that was already legal without the rejected transform.

## 5.8. Legal Observation Determination Law

A Legal Platform Observation is not an intrinsic property of a JVM type. It is the platform-owned distinction exposed by
the exact Java, Kotlin, or JVM source-semantic surface relevant to the admitted use.

Static source context remains part of that determination when the source language makes it semantically relevant. One
generic compiler relation must not collapse platform-defined relations that behave differently. Reference identity and
concrete carrier class likewise do not become Contract identity merely because an admitted value is represented by a JVM
object. A source type test may be legal without making `getClass()` or reflection legal at the same boundary.

## 5.9. Admitted Observer Law

A legal platform observer is the source-semantic operation or language construct that the platform specification allows
to distinguish two platform states or outcomes. It is not the compiler component that happens to inspect a
representation.

Physical visibility alone does not create admission. A debugger, reflection facility, runtime mechanism, or compiler
subsystem may be able to inspect a distinction without giving governed code a right to depend on it. If such a facility
is itself considered for admission, it is judged as its own exact platform surface under this ADR.

## 5.10. Observation Domain Law

ADR-0073 distinguishes three places where a platform-owned distinction may matter.

In governed user realization, the admitted host-language operation retains its platform-defined meaning. At an outward
generated Java or Kotlin surface, the host contract promised to the external caller determines what may be
distinguished. At a Contract-facing boundary, only the distinction required to form the owning Contract's candidate
material is relevant.

A separate product that publishes its own stable external contract must define its own compatibility surface. None of
these domains grants Contract authority to the physical representation used to realize it.

## 5.11. Observation Lifetime and Discharge Law

A platform-owned distinction remains a compiler preservation constraint only while an admitted host-language observer, a
Contract-facing projection, or an outward host promise can still depend on it.

The constraint may end when no such use remains. It may also end at a Contract boundary after the owning authority has
formed and established new meaning that no longer depends on the original carrier distinction. Earlier platform
semantics remain correct for the execution that produced the material; discharge does not rewrite them.

The original carrier may be removed earlier when another representation preserves every still-required observation. That
is representation replacement rather than semantic discharge.

Cache state, current class loading, runtime profile, observed monomorphism, or another speculative implementation fact
does not establish discharge.

## 5.12. Guarantee Admission Law

Ratification preserves the kind of guarantee that the applicable platform specification actually makes. A guarantee may
be exact, may permit several outcomes, may depend on a closed basis, or may be advisory or best-effort. Observed
implementation behavior does not strengthen that guarantee.

The existence of a platform guarantee does not require every Kontrakt boundary to admit the corresponding observer.
Admission follows the exact boundary law. If Kontrakt later substitutes for admitted behavior, Section 5.7 governs
preservation of the applicable guarantee.

# 6. Descriptor, Formation, Validation, and Resolution

A descriptor meaning, the host carrier that presents it, the operation that forms it, the basis that validates it, and
an operation that resolves it are separate semantic questions.

## 6.1. Descriptor Separation

A stable descriptor does not acquire the semantics of the external or versioned knowledge that it may identify.

A time-zone identifier is not the time-zone rules associated with that identifier. A currency code is not
locale-sensitive currency metadata. A URI-like coordinate is not authority to dereference the resource it names.

A descriptor may therefore be a Closed Platform Value even when formation, validation, or resolution operations around
it are not Native.

## 6.2. Formation and Operation Independence

Ratification of a descriptor or result value does not ratify the constructor, factory, parser, lookup, validation, or
other operation used to obtain one host carrier for that value.

A Platform Operation is Native only when every semantic input required for the admitted behavior is either an explicit
admitted operand or an explicitly ratified Closed Platform Semantic Basis. Installed provider sets, default filesystems,
current time zones, host locales, service registries, network environments, mutable registries, or other unbound ambient
sources do not satisfy that requirement.

Provider-backed formation is therefore classified independently from its result. An Adapter operation may still produce
an independently closed Native value. Conversely, Native arguments or a Native result shape do not make a
capability-producing operation Native.

## 6.3. Validation Basis and Non-Elevation

Validation is Native only to the extent that the domain against which validation occurs is closed under the admitted
basis. A fixed grammar or another closed rule set may satisfy that condition. Runtime-selected, dynamically
discoverable, mutable, environment-relative, or otherwise unbound provider state does not. Validation against versioned
platform data is governed by Section 7.

Successful validation establishes only the meaning guaranteed by that exact basis. It does not establish future provider
availability, future resolution equivalence, resource existence, stable external state, or capability authority beyond
the validation basis.

## 6.4. Resolution and Dereference

An operation that turns a descriptor into live external state, provider-owned semantic state, resource authority, or
another capability crosses the Adapter boundary unless Kontrakt has separately ratified that exact closed operation.

Descriptor possession therefore does not imply resolution authority.

# 7. Closed Versioned Platform Data

Some Kontrakt compiler work may consume released platform semantic data instead of leaving the corresponding operation
to unchanged JVM execution. This occurs when Kontrakt computes or retains a result whose meaning depends on a released
platform dataset.

Only in that situation does ADR-0073 model the consumed data as a **Platform Semantic Basis** for the Kontrakt-owned
judgment or product.

## 7.1. Platform Semantic Basis and Closed Data

A Platform Semantic Basis is the minimal closed set of platform-owned released semantic data actually consumed by one
Kontrakt-owned compiler result or transformation. It is scoped to that consumer rather than to the entire JDK, JVM
installation, or host machine.

Consumed versioned data must be explicitly identified and sufficient for the meaning Kontrakt derives from it. A
released data snapshot is not a live capability merely because it originated outside Kontrakt. A runtime-discovered,
mutable, replaceable, environment-selected, or provider-selected source is not Closed Platform Data merely because the
current JVM can access it.

Runtime-selected provider contents, service-loader results, host defaults, host locale, runtime time zone, classpath
discovery, system properties, or another ambient provider set therefore cannot serve as closed compiler semantic input
merely because the operation that reaches them is syntactically explicit.

These rules do not constrain ordinary runtime providers used by unchanged admitted platform execution. They govern data
that Kontrakt itself consumes.

## 7.2. Basis Identity, Version, and Compatibility

Exact basis identity and semantic compatibility are different compiler judgments. Identity asks whether two
Kontrakt-owned computations consumed the same identified data. Compatibility asks whether a different actual basis is
sufficient for a consumer whose derived meaning depended on the earlier basis.

Compatibility is consumer-relative and may be directional. It is not one global equivalence relation between platform
releases. Different basis identity is incompatible by default until sufficient compatibility evidence exists for the
exact consumer.

A release label, JDK update number, vendor name, or similar coordinate is not compatibility evidence by itself. A
version coordinate identifies a basis only when ratification establishes that it completely identifies the data the
consumer actually used. Version strings remain identifiers or cache coordinates rather than Contract or platform
semantic authority.

## 7.3. Consumer Scope and Basis Consumption

Closed Platform Data is closed only for the consumer whose required basis is fixed or explicitly compatible. A result
safe within one process is not automatically safe for persistence or reuse under another runtime; the consumer's reuse
boundary determines how long the basis obligation survives.

A Platform Semantic Basis becomes a compiler dependency only when a judgment, transformation, specialization,
validation, or retained result actually consumes semantic information from it. Platform data that the compiler does not
consult is not made a dependency merely because the host JVM contains it. Leaving an operation unchanged for JVM
execution therefore does not bind the compiled result to the compiler host's corresponding platform data.

## 7.4. Compilation, Execution, and Basis Drift

When a compiler product embeds, preserves, specializes, validates, or reuses meaning derived from versioned platform
data, execution or reuse under another basis is legal only when the actual basis satisfies the recorded semantic
requirement.

The response to incompatibility may be recomputation, revalidation, fallback to an unspecialized path, or refusal. The
choice is Design and backend work; incompatible basis must not be treated as unchanged meaning.

A change in relevant basis requires every dependent judgment to be reconsidered. It does not imply that every downstream
Contract or compiler product changed. Recalculation may establish that the consumer-relevant result is unchanged and
stop further invalidation. The exact repair and early-cutoff mechanism remains outside this ADR.

## 7.5. Identity Substrate Delegation

When a Platform Semantic Basis requires stable compiler identity, canonical bytes, HID, fingerprinting, or persistent
identity substrate, ADR-0041 owns that mechanism.

ADR-0073 defines the semantic material that constitutes the basis and the compatibility relation required by its
consumer. ADR-0041 may provide compact identity for that already-ratified material.

Neither equal HID nor unequal HID independently defines semantic compatibility.

# 8. Ratification Verification

## 8.1. Verification Domain

Platform ratification is compiler verification work.

It belongs in the Verification domain, but it is not Contract Establishment and it does not become Contract authority.

Platform Boundary Verification is a sibling of Contract and realization verification concerns. It verifies whether the
compiler may rely on one platform surface under the laws of this ADR.

---

## 8.2. Full Ratification Audit

Kontrakt performs the expensive audit when support for a platform surface, operation, compiler-consumed semantic basis,
or relevant platform release is created or changed.

The audit reads the official specification source that owns the exact semantic subject. Java language constructs are
not owned by the same document as JVM execution, and library APIs have their own specification surface. Kotlin source
semantics and Kotlin/JVM mapping may likewise require different official material. No document becomes a universal
platform authority merely because it is broader or closer to execution. If the required official sources cannot be
reconciled soundly for the supported surface, ratification fails closed rather than filling the gap from runtime
experiments.

Observed OpenJDK, HotSpot, Kotlin compiler, or vendor behavior may provide conformance evidence. Observation does not
elevate behavior that lacks an applicable specification guarantee into the stable Native boundary. Vendor-specific or
implementation-specific behavior requires a separately explicit target if Kontrakt ever chooses to support it.

The audit must reject direct Contract-facing use when the relevant boundary meaning cannot be closed, when external
authority cannot be isolated, or when Kontrakt-owned compiler work consumes semantic data that cannot be identified
soundly. The audit is not repeated from first principles for every user compilation.

## 8.3. Ratified Native Surface Catalog

Successful audit produces compiler-owned **Ratified Native Surface Catalog** material.

The catalog records the boundary and preservation knowledge that later compiler work needs. It is not a flat class-name
whitelist, it is not a reimplementation of Java or Kotlin semantics, and it is not Contract authority.

Catalog knowledge concerns the platform surface itself. It does not duplicate one entry for every Kontrakt role or every
generic instantiation in which the surface may later appear.

The compiler must be able to recover the exact source-semantic surface and the boundary classification that was audited
for it. The same knowledge must identify any authority hazard that prevents direct use. When Kontrakt itself transforms
or
projects platform behavior, the catalog must also identify the platform distinction that constrains that work. A
Platform
Semantic Basis is recorded only when Kontrakt actually consumed such data. Reusable operation knowledge remains compiler
evidence rather than a second semantic model.

A missing catalog entry does not imply permission. The physical representation of the catalog is Design work. Deleting
or rebuilding the catalog cannot change which result is correct. A wrong catalog entry is a compiler defect.

## 8.4. Compilation Surface Gate

Normal user compilation does not rerun the full ratification audit.

The compiler resolves only the platform surfaces actually encountered by the compilation and checks them against the
ratified catalog. The gate performs the context-sensitive checks that cannot be precomputed globally by combining the
requested Kontrakt role, constituent closure, the owning boundary, and any compiler-side Platform Semantic Basis
requirement established under Section 7.

This gate produces the use-legality result consumed by later compilation work.

## 8.5. External Technology Isolation Gate

Platform ratification and external-technology isolation are related but different checks.

The isolation gate applies Sections 5.3 through 5.5 and the realization-closure rules of Section 9 to framework,
provider, dynamic-execution, and other external technology reached through a ratified platform surface. It prevents a
Native carrier from becoming an alternate route around the Adapter boundary.

## 8.6. Independent Catalog Validation

The catalog itself requires validation appropriate to a compiler release or platform-support update.

Kontrakt must be able to compare built-in platform knowledge with the normative sources that the catalog claims to
support and to run positive and negative conformance tests without relying only on the same fast compilation lookup
path.
This validates Kontrakt's catalog and boundary classification. It does not re-prove unchanged JVM execution or make the
validator a second platform authority.

The exact validation tooling is Design and quality work.

## 8.7. Verification Is Not Runtime Mediation

A verifier is a conformity checker for realization and boundary law. It is not a runtime Contract authority and it does
not make an otherwise open realization legal by continuously intercepting execution.

Verification may occur at compilation, linking, plan construction, or another realization boundary. The exact placement
is implementation work, but admission must be established before governed execution relies on the material being
verified.

Runtime proxying, interception, or repeated permission checks must not substitute for unresolved realization closure. If
new realization material would expand executable or authority reachability, that material requires a new admission
boundary before it participates in governed execution.

Runtime Contract judgments over occurrence values are different. Input, Admission, Invariant, State, and other Contract
authorities may judge each occurrence because that judgment is the declared machine semantics. A verifier observing user
execution after admission is not a replacement for those authorities.

# 9. Ratification Unit and Use Legality

This section separates stable platform knowledge from compilation-specific use legality.

## 9.1. Exact Platform Surface

The release-time audit is anchored to an **Exact Platform Surface**.

An Exact Platform Surface is the smallest source-semantic platform declaration for which Kontrakt can state one coherent
ratification result without importing unrelated behavior. For a type-like surface, this may be the value meaning exposed
by a type or interface. For behavior, it may be one exact source-semantic operation.

Java and Kotlin may present different source-level contracts over the same JVM representation. Compiler-generated
adaptation forms therefore do not define platform semantic identity merely because they are callable on the JVM. This
includes bridge or synthetic methods, as well as representation-specific forms introduced for mangling, boxing, or
default-method support.

The source-semantic surface is resolved first. JVM declarations and descriptors may then identify how that surface is
realized for analysis or code generation. Those realization identities do not replace the source-semantic identity.

The physical key used to record either identity is compiler implementation work.

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

Operation audit is anchored to the exact source-semantic operation that Java or Kotlin exposes to the user program.
Overload resolution, static type, language mapping, and other source-level rules remain part of that identity whenever
they change the operation's meaning.

One source-semantic operation may map to more than one JVM realization form. A callable introduced only to realize that
mapping does not acquire independent semantic authority merely because it exists in bytecode.

Several operations may share audited semantic knowledge without becoming the same operation. The representation used to
record the operation and its JVM mapping remains Design work.

---

## 9.5. Semantic Operation Profile

Repeated boundary-relevant knowledge about platform operations may be represented by a reusable **Semantic Operation
Profile**.

The profile is a conservative compiler summary used by Platform Boundary Verification. It is not a purity enum, a
universal JVM effect system, or an internal replacement for the platform specification. It records only the facts that
Kontrakt needs to decide authority isolation, boundary closure, transformation preservation, or another compiler-owned
question.

The direction is:

```text
normative platform contract
    -> ratification audit
    -> reusable Semantic Operation Profile
```

It is never:

```text
Semantic Operation Profile
    -> platform meaning
```

An exact operation-specific fact or exception takes precedence over a shared profile. Actual user-program facts, such
as reachable call targets or aliasing, remain Derived Realization Analysis. Other execution facts needed by verification
belong there for the same reason rather than becoming Platform Profile meaning.

The exact profile vocabulary and physical schema are Design work.

## 9.6. Unchanged Platform Execution

The catalog is not a reimplementation of the Java or Kotlin API.

For a platform use that has already passed Core admissibility and realization closure, Kontrakt may preserve the
resolved source-semantic call without substituting an internal semantic model for its execution. The applicable platform
specification then continues to define that execution.

This is an explicit ratified compilation path rather than a fallback inferred from missing catalog knowledge. Section
5.7 applies only if Kontrakt later substitutes for the behavior.

## 9.7. Platform Use Verification

Compilation-time Platform Use Verification begins from the ratified source-semantic surface and determines whether that
surface may be used at the requested Kontrakt boundary.

It combines constituent closure, external-authority isolation, the requested role, and any compatibility obligation
already established under Section 7. When a Kontrakt-owned transformation or outward host projection depends on a
platform distinction, Sections 5.8 through 5.12 supply the relevant observation information.

The result is one of the boundary classifications in Section 10. The exact compiler product name, query shape, and
storage representation remain Design work.

## 9.8. Dispatch Closure and Non-Elevation

Ratifying a source-semantic declaration does not grant Contract authority to every implementation that may answer an
indirect execution edge. Callback-based execution is subject to the same closure requirement.

If governed realization directly executes an indirect target, realization verification must soundly establish that the
reachable target set remains inside the permitted realization boundary. This check asks only whether the
platform-permitted edge can reach forbidden external authority or opaque implementation; it does not redefine JVM
dispatch, callback ordering, or library callback semantics.

Dispatch Closure is derived realization knowledge. An override, functional object, lazy carrier, or another host
mechanism cannot elevate behavior that would otherwise be illegal merely because it is reachable through a ratified
standard surface. If the target set remains open where closure is required, the use is not a closed Core operation.

Runtime speculation may guide an optimization whose own execution model supplies the necessary guards and recovery. It
does not establish Dispatch Closure.

The concrete call-graph algorithm, target-set representation, and verification data structure remain Design work.

## 9.9. Verified Realization Closure

A Core realization is admissible only when executable and authority reachability controlled by user realization is
closed before admission.

Runtime selection among alternatives already included in that closure is legal. After admission, user-controlled
behavior must not introduce an executable target, implementation authority, external capability, or another authority
path that was absent from the verified set.

This requirement stops at the ratified platform boundary. Kontrakt does not close or reproduce the internal
implementation topology used by a conforming JDK, JVM, or JIT to realize an admitted platform operation.

Verification timing and the prohibition on continuing runtime mediation are governed by Section 8.7.

# 10. Native, Carrier, Adapter, and Unsupported Are Different Results

Ratification is not one global boolean attached to a JVM class. Use legality has four distinct boundary results:

```text
Native Surface
Carrier Only
Adapter Required
Unsupported
```

**Native Surface** means the exact use can cross the requested boundary directly under this ADR.

**Carrier Only** means the JVM carrier itself cannot become Contract authority, but Contract-facing material may be
formed from what it presents under Section 5.2. This classification does not override the capability and resource rules
in Sections 5.3 through 5.5.

**Adapter Required** means the relevant authority or effect must remain behind an explicit external boundary.

**Unsupported** means Kontrakt has not established a legal direct or Adapter-mediated path for the requested use.

The implementation does not need to encode these results as one enum.

# 11. Initial V1 Audit Scope

The initial V1 audit scope includes the JVM value surfaces that are unavoidable for ordinary Java and Kotlin
Contract-facing use. The scope is an audit commitment, not a declaration that every listed carrier or operation is
Direct Native.

Primitive values and ordinary wrappers are part of the V1 audit. `String`, language enum values, `BigInteger`, and
`BigDecimal` are included as well. Collection surfaces are audited with ADR-0072. Selected value-oriented `java.time`
surfaces may be admitted after the same boundary review. A platform-defined relation is preserved when a Kontrakt
transform or projection depends on it, but that relation does not become Contract identity.

Java and Kotlin arrays are audited but are **Carrier Only by default at a Contract-facing boundary in V1**. An array is
a
mutable JVM object with possible aliasing and, for reference arrays, runtime component-type behavior. The original array
therefore cannot serve as Contract authority. A stable Contract-facing aggregate presentation must exist before the
owning Contract judges the material. How that presentation is realized is outside this ADR. User realization may still
use arrays under ordinary JVM semantics when the realization itself has passed the applicable closure checks.

Read-only or unmodifiable views are not assumed to provide a stable observation when a shared backing store can still
change what the view exposes. A deferred carrier is also not a Direct Native Contract value merely because its library
type is standard. The platform continues to own its deferred execution semantics, while Contract-facing use must still
satisfy Closure and Authority Isolation.

Host-language null does not automatically mean Contract-declared absence. The same is true of an empty container or a
missing mapping. A platform representation acquires Contract absence meaning only when the owning Contract boundary
explicitly establishes that mapping.

This scope does not imply that every operation reachable from an audited type is Native. An operation that reaches live
provider or environment state remains independently classified. A standard JVM mechanism that can expand user-controlled
execution or authority reachability after verification is likewise not admitted merely because its semantics are
platform-defined.

The V1 stable Native catalog excludes Java preview/incubator and Kotlin pre-stable surfaces from stable ratification.
A fully specified current-release feature does not obtain stable Native status merely because its present semantics are
known. Future support may use an explicitly release-pinned experimental ratification without giving it the compatibility
promise of the stable catalog.

Additional surfaces require the same ratification process.

# 12. Adapter Boundary

Frameworks, optional libraries, vendor APIs, live resource handles, ambient environment access, provider authority, and
other external technology remain outside governed Contract processing unless an explicit Adapter or another approved
external boundary admits material from them.

An Adapter may produce Contract-facing candidate material only when that result independently satisfies the closure
rules of Section 5. It does not carry the external authority that produced the result across the boundary.

Unknown external technology fails closed.

# 13. Frontend, HIR, Establishment, and Realization Boundary

Frontend resolution identifies the exact Java or Kotlin source-semantic surface selected by the host language and the
JVM realization mapping needed by later compiler work. Neither result becomes Contract authority.

HIR does not reproduce Java, Kotlin, or JVM execution semantics. It carries the resolved platform identity and only the
boundary or preservation facts that downstream Kontrakt work still requires.

Platform ratification is not Contract Establishment. This ADR creates no parallel `Established Platform Material`
authority. When platform-derived material reaches a Contract-facing boundary, the owning Contract receives the candidate
material permitted by its law and establishes its own meaning under ADR-0063.

After establishment, authoritative communication inside the machine is Contract-to-Contract and proceeds through
Contract-established material. Compiler subsystems may derive realization knowledge from it, but they do not reconstruct
a competing Contract meaning from JVM classes, catalog entries, HIR implementation fields, or runtime objects.

Realization verification consumes the applicable boundary laws and may derive alias, escape, target, or other execution
facts needed to prove them. Those facts remain realization knowledge. Verification timing is governed by Section 8.7.

# 14. Optimization Non-Interference

Optimization belongs to realization. Contract-established knowledge may give a Kontrakt-owned transform stronger facts
than a generic JVM optimizer has. This section governs only those Kontrakt-owned transformations; unchanged admitted
platform execution is covered by Section 9.6.

A Kontrakt transformation is legal only under the Preservation Law in Section 5.7 and any Contract meaning that the
transformation is allowed to consume. Platform rules such as JMM ordering or class initialization become compiler
constraints only when the transformation can affect them.

Speculative runtime evidence may influence profitability or a guarded realization only after the semantic prerequisites
of Sections 5.7, 5.11, and 9.8 are satisfied.

The exact validation technique and all physical optimization mechanisms remain Design and quality work.

# 15. Reuse and Incremental Boundary

The expensive ratification audit is not a per-use compilation activity.

V1 may ship pre-audited catalog material and reuse valid Exact Platform Surface classification and Platform Use
Verification results within one compiler generation. Stable platform knowledge, Platform Semantic Basis identity, and
context-specific use legality remain separate reuse boundaries.

V2 may persist or incrementally repair these products when their explicit inputs and validity law permit it.
Basis-dependent invalidation follows Section 7.10.

Cache state, query topology, catalog rows, Semantic Operation Profiles, and fingerprints remain rebuildable compiler
products. Clean recomputation is the correctness reference.

The exact persistent key, scheduling policy, invalidation algorithm, semantic-basis encoding, and repair mechanism
remain Design work.

# 16. Refusal Boundary

Unsupported or ambiguous direct platform use is rejected before it becomes governed Contract processing.

A use is refused when its required boundary classification cannot be established, when an Adapter-required surface is
used without its boundary, when the closure or authority-isolation laws of Section 5 fail, when realization closure
under Section 9 fails, or when a compiler-owned basis obligation under Section 7 cannot be satisfied.

A forbidden platform or external-technology operation reached by user realization is a realization-verification failure
rather than a valid Contract Failure during governed execution. Section 8.7 forbids converting such a failure into
runtime legality through continuing mediation.

Diagnostics explain the violated boundary. They do not decide Native status or establish Contract meaning.

# 17. Determinism

Platform ratification and Kontrakt's compilation-time boundary judgments are deterministic compiler products.

For the same explicit compiler inputs, ratified platform knowledge, and any Platform Semantic Basis that Kontrakt
actually consumed, unrelated scheduling, cache state, or host discovery order cannot change the compiler's
classification
or Contract result. A fast cached path and a clean uncached path must agree.

This compiler determinism does not require Kontrakt to strengthen admitted platform execution that intentionally permits
more than one behavior. When Kontrakt leaves that execution unchanged, the platform contract continues to define the
permitted runtime behavior.

# 18. Intentionally Open

The following implementation choices remain open:

- the physical Ratified Native Surface Catalog schema;
- the exact Semantic Operation Profile vocabulary and representation;
- the compiler representation of Platform Semantic Basis and the mechanism used to discover, canonicalize, store,
  compare, or revalidate TZDB, Unicode, CLDR, charset, provider, or other platform data that Kontrakt actually consumes;
- the runtime or backend response to a compiler-owned basis incompatibility;
- the representation and liveness machinery for platform-preservation obligations, including how a compiler proves that
  a transformed or projected path no longer depends on a platform distinction;
- the physical occurrence-stabilization mechanism, including copying, ownership transfer, persistent representation,
  copy-on-write, versioned snapshotting, verified zero-copy, or another sound mechanism;
- the analysis used to establish alias isolation, escape, target closure, coherent capture, or other realization facts;
- HIR fields, query keys, memoization structures, incremental dependency graphs, persistent reuse formats, and the
  producer-consumer mechanism among HIR, Established Material, Realization Analysis, Platform Boundary Verification,
  optimization, and backend emission;
- MIR, LIR, physical layout, object elimination, collection realization, and other optimization mechanisms;
- the additional Native values and operations that future platform-support work may ratify;
- framework-specific Adapter generation.

These choices may change without changing Contract meaning. What is not open is the ownership direction established by
this ADR: Contract authority constrains realization, while replaceable compiler and platform realization do not define
Contract authority.

# 19. Rejected Directions

The following alternatives are explicitly rejected. The referenced section owns the positive law and its rationale; this
section preserves the rejected design history without restating that law.

| Rejected direction                             | Governing section |
|------------------------------------------------|-------------------|
| Native by Namespace                            | §4.1              |
| Native by Runtime Availability                 | §4.1              |
| Immutable Means Closed                         | §§5.2–5.4         |
| Explicit Capability Means Native               | §5.3              |
| Native Type Means Every Operation Is Native    | §5.6              |
| Descriptor Means Formation Is Native           | §6.2              |
| Validation Elevates Future Resolution          | §6.3              |
| JDK Version as Universal Semantic Basis        | §§7.1–7.2         |
| Version Label as Semantic Authority            | §7.2              |
| Global Compatibility Between Platform Releases | §7.2              |
| Full Audit on Every Compilation Use            | §§8.2, 8.4        |
| Catalog as Authority                           | §8.3              |
| One Global Native Boolean per JVM Class        | §10               |
| Role-Expanded Platform Catalog                 | §9.2              |
| Catalog Entry per Generic Instantiation        | §9.3              |
| One Independent Semantic Model per Callable    | §§9.4–9.5         |
| Unchanged JVM Execution by Catalog Absence     | §§8.3, 9.6        |
| Platform Optimization by Default Replacement   | §§5.7, 14         |
| Legal Observation by Type                      | §5.8              |
| Physical Observability as Admission            | §5.9              |
| Runtime Speculation as Semantic Proof          | §§5.11, 9.8, 14   |
| JVM Semantics as Kontrakt Contract             | §§4.2, 4.4        |
| Re-Proving Unchanged JVM Execution             | §§8.6, 9.6        |
| JVM-Supported Means Core-Admissible            | §4.4              |
| Freeze Every Mutable Carrier                   | §§5.2–5.4, 18     |
| Runtime Verification as Closure Substitute     | §8.7              |
| Proxy or Interceptor as Contract Authority     | §8.7              |

# 20. Consequences

Ordinary Java and Kotlin value surfaces can remain natural user-facing material without turning the surrounding JVM
ecosystem into Contract authority.

Platform support becomes explicit compiler-maintenance work. New or changed surfaces require ratification evidence,
while normal compilations reuse that knowledge and perform only context-specific legality checks.

V1 may handle uncertain aliasing or mutable carriers conservatively. Stronger future analysis can remove copies or admit
additional closed realizations without changing the Contract law established here.

Platform-data drift invalidates only compiler products that actually consumed the affected basis. This limits accidental
coupling to the compiler host and gives incremental compilation a precise dependency boundary.

External frameworks, providers, resources, and dynamic execution mechanisms have a visible integration cost because they
must remain behind an Adapter or fail verification when they reopen Core authority.

The verifier is not part of the runtime semantic path. This avoids making Kontrakt a proxy-based DbC system and leaves
admitted execution available to replaceable backend, JVM, and JIT implementations.

The resulting architecture is a narrow host-platform admission boundary with explicit maintenance and verification
obligations, rather than a second JVM contract model or a permanent JVM implementation policy.

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