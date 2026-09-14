# ADR-0073: JVM Platform-Native Surface, Exact Contract Preservation, and External Technology Boundary

## Status

Proposed

## Date

2026-09-15

## Related

- *What Contract Is*
- ADR-0046: IDL-First Interface Contract Frontend and 1D Contract Catalog
- ADR-0047: One-Dimensional Contract Presentations and Pipeline-Slot Selection
- ADR-0053: Version Contract
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0072: JVM Collection Contract Preservation, Aggregate Semantics, and Deterministic Equality
- Kontrakt Compiler Total Architecture Map
- Kontrakt Compiler Material and IR Architecture Review Checklist
- *Modern Compiler Architecture 01–15*

---

# 1. Context

Kontrakt is a Contract compiler hosted on the JVM.

That host choice creates a narrow compatibility obligation.

A Java or Kotlin user must be able to express ordinary Contract-facing values with the basic platform types that are
unavoidable in normal JVM programs. Kontrakt must not require a second wrapper universe for primitive values, strings,
arrays, ordinary platform collections, arbitrary-precision numbers, or the ordinary immutable date/time values that are
already part of the platform language surface.

The same obligation does not extend to the entire JDK, Java SE API, Kotlin standard library, or JVM ecosystem.

Those environments contain capabilities, runtime services, provider mechanisms, I/O, concurrency facilities,
reflection, foreign-memory access, lazy computation, framework integration, and other behavior whose meaning is not one
closed Contract value.

Kontrakt therefore needs one explicit law for the small part of Java and Kotlin that is admitted directly because the
JVM is the selected host platform.

The law must also protect the opposite boundary.

When a user selects an admitted Java or Kotlin surface, Kontrakt may not weaken, reinterpret, or silently normalize the
contract that surface already promises. The compiler may change representation. It may not change remaining observable
meaning merely because another backend representation is easier to optimize.

This is a compiler-wide concern.

Frontend resolution must know what platform contract was selected. Resolved Contract HIR must retain every distinction
that later authority or legal user code still needs. Establishment must preserve source-owned meaning under the owning
Contract law. User realization must observe the platform contract it was promised. Optimization and backend lowering may
replace representation only after the required meaning is independently preserved.

This ADR defines that boundary.

---

# 2. Problem

The boundary cannot be inferred from package or module membership.

`java.base` contains language fundamentals and immutable values, but it also contains reflection, foreign access,
class-loading support, reference processing, runtime services, I/O-related facilities, and other mechanisms that are not
plain Contract values.

Kotlin has the same problem. `kotlin-stdlib` contains basic values and collections, but also lazy sequences, I/O
helpers,
threading helpers, atomics, and other operations whose legality cannot be inferred from library membership alone.

A second problem is that type support and operation support are different.

An immutable value can be admissible while one method associated with that type reads a clock, default locale, provider,
random source, system property, or another hidden input. Admitting the value must not transitively admit every operation
reachable from its class or package.

A third problem is source-language mapping.

Java and Kotlin can expose different source-level contracts over the same JVM runtime representation. Kotlin maps many
Java types at compile time, including collection interfaces. Runtime class identity is therefore insufficient to recover
the contract the user selected.

A fourth problem is role legality.

A concrete mutable platform class can be a legal external carrier while being illegal as an established immutable Fact
surface. A platform value can be legal in user realization while one capability-acquiring operation on that value
remains
illegal. One boolean `supported` flag cannot represent these distinctions.

A fifth problem appears in HIR and optimization.

If the frontend retains only a Java class name, JVM descriptor, Kotlin type name, or current runtime object, later
stages
must reopen the platform or reconstruct meaning from implementation detail. If the compiler lowers too early, a physical
representation can erase an equality, order, scale, range, failure, or other obligation that legal observation still
requires.

A sixth problem is platform evolution.

Java, Kotlin, and the JDK continue to add APIs. Supporting a new platform version cannot mean only that Kontrakt
compiles
against a newer SDK. A new version can add a surface, alter availability, change a source-language mapping, or change
the
observable contract that the compiler must preserve.

Kontrakt needs an exact, deterministic, version-aware platform boundary that remains subordinate to Contract authority.

---

# 3. Decision Drivers

Determinism has priority over convenience.

The same Contract source, the same explicit platform target, and the same other semantic inputs must resolve to the same
platform obligations regardless of the JDK that happens to run the compiler, classpath discovery order, loaded classes,
reflection order, cache state, worker schedule, or physical compiler layout.

Platform compatibility is narrow.

Kontrakt recognizes only the Java and Kotlin surface required to provide a natural JVM programming experience. This does
not grant native status to arbitrary standard-library modules, optional libraries, providers, frameworks, or vendor
APIs.

Platform meaning is preserved exactly where it remains legally observable.

Input and user realization do not receive authority to replace a JVM contract with a preferred Kontrakt interpretation.
A later 1D Contract may establish different meaning under its own law. The change must occur at that explicit authority
boundary.

Frontend meaning must be complete before Visible HIR.

Establishment and later compiler consumers must not infer missing platform semantics from current host classes or
backend
representation.

Physical realization remains replaceable.

A platform object may disappear after its observable obligations have been represented independently. Physical
representation may change when the required meaning remains preserved.

Unknown meaning fails closed.

Kontrakt does not guess that a type is native because it resembles a supported value or implements one familiar
interface.

---

# 4. Core Model

## 4.1. Platform-Native Surface

Kontrakt defines a **JVM Platform-Native Surface**.

The Platform-Native Surface is the explicitly supported Java and Kotlin surface that may participate directly in
Contract-facing declarations or user realization without an external-technology Adapter.

It exists only because the JVM is the selected host platform.

```text
JVM host constraint
    ↓
explicit Platform-Native Surface
    ↓
resolved platform obligation
    ↓
Kontrakt Contract / realization processing
```

Platform-Native status is not inherited from package membership, library popularity, classpath presence, or runtime
availability.

A surface is native only when the applicable Platform-Native Profile admits it for the exact role in which it is used.

---

## 4.2. Platform-Native Profile

A **Platform-Native Profile** is versioned compiler-semantic knowledge that states which Java and Kotlin surfaces
Kontrakt
supports for one explicit platform target.

The profile is not Contract authority.

It tells the frontend which external platform obligations must be preserved when an owning Contract or legal user
realization uses that surface.

The profile may be represented by generated tables, frozen metadata, source-defined compiler data, or another
deterministic form. Its physical schema does not define meaning.

The profile does not enter the Canonical Contract World as one independent Contract Definition.

---

## 4.3. Native Surface, Carrier, Operation, and Mapping Are Different

Kontrakt distinguishes four concerns.

A **Native Declaration Surface** is a Java or Kotlin type surface that may appear directly where the profile and owning
Contract permit it.

A **Native Carrier** is one actual host value that may be observed to form the declared surface at a boundary.

A **Native Operation** is one exact Java or Kotlin operation that the profile permits without crossing the external
technology boundary.

A **Platform Mapping** is an exact source-language relation where Java and Kotlin expose one runtime representation
through different source-level surfaces.

These categories do not imply one another.

A third-party object can sometimes be a carrier of an admitted standard interface without making the third-party class a
Native Declaration Surface. A native type does not make every method native. A JVM runtime class does not erase the
source-level distinction between a Java declaration and a Kotlin mapped declaration.

---

## 4.4. No Transitive Native Authority

Native status never propagates merely by reachability.

A native collection whose element type is external does not make that element type native. A native type returned by a
framework does not make the framework native. A native receiver does not make a third-party extension function native.

User-defined Contract declarations are also not classified as external technology merely because they are not platform
native. Their legality comes from the relevant Kontrakt Contract law.

The Platform-Native Surface is therefore neither a universal allow-list nor the complete type system of Kontrakt.

---

## 4.5. Resolved Platform Obligation

A **Resolved Platform Obligation** is compiler-semantic material produced when one admitted Native Declaration Surface
or
Native Operation is resolved under one exact Platform Target Binding.

It is not Established Material.

It states the platform contract that the owning Contract candidate or legal user-realization boundary must preserve.

The obligation is typed by the surface being resolved. Kontrakt does not force every platform type into one universal
metadata tuple.

Every resolved obligation must nevertheless make the following questions answerable without reopening the platform:

```text
Which exact platform surface was selected?
Under which exact target binding?
For which role is it legal?
Which observable distinctions must be preserved?
Which exact operations are admitted when operation use is relevant?
Which capability boundary remains forbidden?
Which source-language mapping produced this surface?
```

The obligation can be projected into smaller consumer-specific views. A projection remains derived compiler material and
may not drop a distinction its consumer still requires.

---

# 5. Platform Target and Profile Identity

## 5.1. Platform Target Claim

Platform meaning is resolved against an explicit **Platform Target Claim**.

The claim identifies the platform basis against which user-facing Java and Kotlin surfaces are interpreted.

Where applicable, it includes the Java SE API release, JVM/classfile target, Kotlin language surface, and Kotlin
standard
library surface required to interpret the declaration.

The exact compiler command-line or build-tool syntax is implementation.

The semantic requirement is that the target is explicit compiler input rather than ambient host state.

The JDK currently running Kontrakt does not silently become the target.

---

## 5.2. Platform Target Binding

Frontend resolution converts a valid Platform Target Claim into one exact **Platform Target Binding**.

The binding selects the Platform-Native Profile material used for semantic resolution.

```text
Platform Target Claim
    ↓ deterministic target resolution
Platform Target Binding
    ↓
Native Surface resolution
```

The binding is compiler-semantic basis. It is not Contract Version.

`Contract Version`, `Platform Target Binding`, compiler version, profile storage schema version, and classfile format
version
remain different coordinates.

---

## 5.3. Profile Revision Is Not Platform Meaning

Kontrakt may revise the compiler data used to describe one unchanged platform release.

A profile revision can fix a compiler bug, add a previously unsupported entry, improve diagnostics, or change physical
encoding without changing the external platform specification.

That revision is a compiler-product input. It is not automatically Contract meaning.

A profile revision that changes the resolved observable obligation of an existing declaration invalidates the affected
HIR product and requires clean semantic recomputation.

The compiler must not preserve an old result merely because the Java or Kotlin version string is unchanged.

---

## 5.4. Definition Determinant Boundary

The existence of a Platform Target Binding does not mean that every platform version number becomes part of every
Contract Definition identity.

The owning 1D law continues to decide Definition determinants under ADR-0071.

Platform information is definition-determining only to the extent that it changes the resolved Contract-visible meaning
of that Definition Candidate.

Two target profiles may therefore resolve one declaration to semantically equal candidate meaning without creating two
meanings merely because their platform version coordinates differ.

The compiler still retains enough target attribution to reproduce and validate that resolution.

---

## 5.5. Exact Native Surface Reference

Before Establishment, a resolved native declaration or operation has one exact current compiler-semantic reference.

The reference denotes the exact profile entry and resolved surface selected under the current Platform Target Binding.

A string class name, package name, reflection `Class`, JVM object identity, class-loader identity, ordinal, table index,
HID, or fingerprint is not by itself that semantic reference.

A physical implementation may map the exact reference to a generation-local dense handle for fast access. The dense
handle does not become persistent identity or Contract authority.

---

## 5.6. Native Surface Entry Contract

One Platform-Native Profile entry denotes one exact audited platform surface.

A valid entry preserves enough compiler-semantic information to determine its target availability, exact declared
surface, supported role, observable obligation, and operation boundary when operations are supported.

For a mapped Java/Kotlin surface, it also preserves the exact mapping relation needed to distinguish source-level
meaning
from runtime representation.

For an operation entry, the exact callable meaning and hidden-input boundary are part of the entry.

The profile entry does not need to store this material in one object. It may use typed tables and shared indexes. The
logical contract remains the same.

Entry equality, physical row identity, and semantic obligation equality are different. Two entries or revisions may
produce equal consumer-visible obligations without becoming the same physical profile record.

---

## 5.7. Platform Binding Singularity

One visible Contract HIR generation and one linked Whole-Machine compilation use one effective Platform Target Binding
for
Platform-Native resolution.

V1 does not compose two conflicting native platform worlds inside one machine.

Persisted or imported compiler material formed under another binding must be revalidated against the current target. It
may be reused only when the producer can prove that its required native obligations remain valid under the current
binding.

This is compiler compatibility law. It does not create a new Contract World or Policy World.

---

# 6. V1 Native Value Baseline

## 6.1. Mandatory Baseline

V1 must directly understand the ordinary JVM value surface required for normal Java and Kotlin Contract declarations.

The mandatory baseline includes primitive values, the ordinary boxed value aspect of primitive wrappers, `String`, Java
and Kotlin arrays including primitive arrays, and the finite named value aspect of language enum declarations.

Standard Java and Kotlin collection surfaces are admitted through ADR-0072 and this ADR together.

`BigInteger` and `BigDecimal` are mandatory native numeric values. Their Java-visible distinctions remain preserved
until
an owning later Contract explicitly establishes different meaning.

The baseline also includes the ordinary immutable ISO-oriented `java.time` value surface that can be interpreted without
acquiring ambient time or provider state. The initial mandatory set includes `Instant`, `Duration`, `LocalDate`,
`LocalTime`, `LocalDateTime`, `OffsetTime`, `OffsetDateTime`, `ZoneOffset`, `Period`, `Year`, `YearMonth`, `MonthDay`,
`Month`, and `DayOfWeek`.

`MathContext` is admitted as the immutable explicit arithmetic context required by supported `BigDecimal` operations.
`RoundingMode` is already covered by the enum rule.

This list is the V1 minimum. A profile may support additional audited vanilla values without changing the admission law
of this ADR.

---

## 6.2. Value-Based Classification Is Evidence, Not Admission

Java's value-based-class classification is useful evidence because it rejects identity-sensitive interpretation for
classes such as primitive wrappers and many `java.time` values.

It is not sufficient for Platform-Native admission.

A value-based class can still be outside the V1 baseline or expose operations that consult hidden state.

Kontrakt therefore never implements the rule:

```text
Java value-based class
    → automatically Platform-Native
```

For an admitted value-based surface, reference identity, monitor identity, identity hash, or another identity-sensitive
observation does not become native value meaning merely because one JVM object happens to exist.

---

## 6.3. Numeric Contract Preservation

A numeric platform value is preserved according to its actual admitted platform contract rather than a simplified
mathematical approximation.

For `BigDecimal`, numerical comparison and `equals` are not the same relation. Scale remains observable where the Java
contract makes it observable.

Kontrakt may later canonicalize or lower a decimal under a different 1D law. Input, frontend resolution, HIR compaction,
or backend convenience may not pretend that the earlier `BigDecimal` surface already had that later equality.

---

## 6.4. Date and Time Value Boundary

Admitting a date/time value does not admit a time source.

An `Instant` value can be native material. An operation that obtains the current instant from an ambient clock crosses a
different boundary.

`ZoneId`, `ZonedDateTime`, `Clock`, provider-backed calendar surfaces, and other values or operations whose complete
behavior can depend on external zone-rule or provider state are not part of the mandatory V1 baseline until separately
audited.

This is conservative by design. A later profile may admit them with an explicit platform-basis law.

---

## 6.5. Top Types Do Not Widen the Native Surface

`Object`, Kotlin `Any`, `Number`, `Comparable`, `Serializable`, `Cloneable`, and another broad host supertype do not
grant
native status to every value assignable to them.

If one Contract-facing declaration permits several runtime alternatives, the owning Contract law must still close the
allowed semantic alternatives or provide another explicit type law.

A broad JVM supertype is not an escape hatch through which external library objects enter the governed machine.

---

# 7. Native Operation Law

## 7.1. Operation Admission Is Exact

Native operation admission applies to one exact operation meaning.

It is not inherited from the receiver type, package, superclass, interface, or source-language convenience syntax.

The profile resolves the operation sufficiently to distinguish overload, receiver contract, argument meaning, result
meaning, and the platform version in which that operation is supported.

For Kotlin extension functions, the declaring standard-library function is part of the resolved operation identity. An
extension from user code is user realization. An extension from a third-party library is not platform native merely
because its receiver is native.

---

## 7.2. Hidden-Input Prohibition

A Native Operation may depend on its explicit receiver, explicit arguments, platform constants fixed by the selected
profile, and newly created local state allowed by the surrounding realization law.

It may not silently obtain semantic input from the current clock, default locale, default time zone, environment
variables, system properties, random source, provider registry, filesystem, network, process state, class-loader state,
reflection discovery, foreign memory, foreign functions, or another undeclared capability.

If such material is semantically required, it must enter through an explicit Adapter, Contract basis, or another
approved boundary before the governed computation relies on it.

This law is about semantic input. It does not prohibit ordinary allocation or compiler-generated temporary storage when
those mechanisms are not observable as Contract meaning.

---

## 7.3. Local Mutation Is Not External Capability

Not every state change is external technology.

A supported standard collection used as local user-realization working state may perform its standard local mutation
when the surrounding realization and 1D laws permit that use.

The mutation does not grant permission to publish mutable aliasing as Fact authority or to retain hidden external state
across Contract boundaries.

Role legality therefore remains distinct from operation legality.

---

## 7.4. Exceptional Behavior Is Observable Where the Platform Defines It

If a supported Native Operation has a specified exceptional result for one explicit operand condition, optimization may
not silently replace that behavior with another result.

How an implementation exception is attributed to the Failure Contract remains owned by ADR-0057 and the realization
boundary.

This ADR fixes only the preservation obligation: admitted platform operation semantics include their legal success and
failure observations where those observations remain visible to user code.

---

# 8. Role-Qualified Admission

## 8.1. Native Status Is Role-Specific

The Platform-Native Profile records role-qualified support.

At minimum, Kontrakt distinguishes whether a surface may be used as a Contract-facing declaration, an external boundary
carrier, a generated/user API surface, or a user-realization operation surface.

The physical profile schema may encode these roles differently. The semantic distinction must remain available.

A role grant from this profile is necessary but not sufficient. The owning 1D Contract may impose a stricter law.

---

## 8.2. Concrete Implementation and Declared Surface Are Different

A concrete Java or Kotlin implementation can be accepted as a carrier of a supported declaration without importing the
implementation's entire contract into the governed machine.

For example, an object implementing a supported `List` surface can be observed through that declared surface when the
boundary can form a complete coherent value without triggering an external capability.

Kontrakt does not thereby establish the concrete class identity, private storage strategy, framework lifecycle, or
additional methods as Contract meaning.

If the user declares the concrete implementation class itself as the Contract-facing surface, Kontrakt must either
preserve the complete relevant contract of that declaration for the requested role or reject the declaration. Silent
widening to a smaller interface is not support.

---

## 8.3. Carrier Observation Must Be Closed

A carrier is directly usable only when the observation required to form the native value is complete at the boundary.

Reading the carrier must not require lazy database loading, network access, provider lookup, future completion, stream
consumption with hidden producer state, or another undeclared capability.

A third-party object may therefore satisfy a standard interface as a plain carrier in one case and require an Adapter in
another.

The deciding question is whether the admitted platform surface can be observed completely and coherently without
importing the carrier's external technology contract.

---

# 9. External Technology Boundary

## 9.1. Capability Surface

A **Capability Surface** is a type or operation whose useful meaning depends on access to a live resource, ambient
runtime state, external system, lifecycle authority, scheduler, provider, or effectful service beyond one closed value.

Capability surfaces are not Platform-Native Contract values merely because Java SE or Kotlin ships them.

I/O handles, sockets, filesystem access, clocks as time sources, random generators as stateful sources, threads,
executors, atomics as shared mutable synchronization state, reflection objects, class loaders, service/provider
discovery,
foreign memory, and foreign functions remain outside the native Contract surface unless a later ADR explicitly grants a
narrower role.

---

## 9.2. External Libraries and Frameworks

Spring, Jackson, Hibernate, Guava, Reactor, `kotlinx.*` libraries, vendor APIs, application-server APIs, and other
optional
libraries are not Platform-Native by default.

Their types may appear outside the governed machine. Their behavior may be consumed by an Adapter. They do not enter the
native profile merely because they are common, deterministic in one application, or implemented entirely in Java or
Kotlin.

A future explicit integration decision may define an Adapter or another boundary. It does not retroactively enlarge the
Platform-Native Surface.

---

## 9.3. Adapter Output Does Not Import Adapter Authority

An Adapter may produce a native platform value or user-defined Contract material.

After that output crosses the appropriate boundary, later Contract processing judges the produced material under its own
law.

The framework, database, serializer, network client, or provider that produced the value does not accompany it as hidden
Contract authority.

Diagnostic provenance may retain the external origin without making that origin semantic identity.

---

# 10. Frontend Resolution Contract

## 10.1. Frontend Input

Platform resolution consumes explicit source declaration material, the exact Platform Target Binding, and the current
Platform-Native Profile revision required to interpret that target.

It may also consume source-language semantic information already established by the Java or Kotlin frontend.

It does not consume current runtime class discovery as semantic authority.

Reflection, class loading, compiler host APIs, or loaded bytecode may be used as acquisition evidence in one frontend
implementation. They cannot override the profile or fill semantic gaps heuristically.

---

## 10.2. Exact Resolution

A platform declaration is resolved only when one exact supported surface can be selected for the requested role.

Resolution closes source-language mapping, generic constituent meaning, relevant nullability/presence relation, and the
observable platform obligations required by the owning candidate.

Assignability alone is not enough. Package prefix matching is not enough. Simple class-name matching is not enough.

If multiple native meanings remain possible, the candidate is not resolved.

---

## 10.3. Kotlin/JVM Mapping Is Source Meaning

Kotlin/JVM mapped types are resolved from the Kotlin declaration the user wrote, not reconstructed later from the erased
or runtime Java class.

A Kotlin read-only `List` surface and a Java `List` declaration can share one JVM runtime interface while exposing
different source-level affordances. Kontrakt preserves the declared source-language surface needed by legal observation.

The frontend may also record the JVM realization mapping required by later code generation. That mapping does not
replace
the source-level contract.

---

## 10.4. Generic and Aggregate Closure

A native generic container does not make its constituents native automatically.

Every constituent type required to interpret the declaration must independently resolve under the relevant Contract,
platform, or user-defined type law.

The frontend must reject a candidate whose outer native surface is known but whose constituent meaning remains external,
ambiguous, or unresolved for that role.

ADR-0072 owns the additional collection law.

---

## 10.5. Frontend Refusal Boundary

The following conditions fail before authority is established:

```text
unsupported Platform Target Claim
unknown or unsupported native surface
surface known but illegal for the requested role
operation known but not admitted
ambiguous Java/Kotlin mapping
native outer type with unresolved constituent meaning
use of preview / experimental / vendor-specific surface without explicit support
```

These are compiler/frontend refusal conditions. They are not Input occurrence failures and do not create Contract
Failure
material merely because compilation stopped.

Diagnostics may explain the reason. Diagnostics do not decide support.

---

## 10.6. Frontend Determinism

For the same source declaration, Platform Target Binding, profile revision, and other explicit frontend inputs, platform
resolution must produce the same resolved meaning.

Classpath enumeration order, current host JDK, loaded implementation class, runtime provider set, reflection order,
filesystem order, worker schedule, and cache state cannot select the result.

A clean frontend computation remains the correctness reference for incremental reuse.

---

## 10.7. Platform Contract Source and Runtime Evidence

The Platform-Native Profile is derived from the supported Java, JVM, and Kotlin language/library contracts for the exact
target version.

Observed behavior of one JDK build, one vendor implementation, or one runtime experiment may be used as verification
evidence. It does not define an unspecified platform guarantee.

If the standard leaves one behavior unspecified, Kontrakt may choose a deterministic private realization when that
choice
remains unobservable as source-platform meaning. It may not publish the private choice as though the platform had
promised it.

A mismatch between the profile and the normative supported platform contract is a compiler defect.

---

# 11. Resolved Contract HIR Contract

## 11.1. Resolved Platform Obligation Is Compiler-Semantic Material

When a platform-native distinction participates in one Contract Definition Candidate, Visible HIR preserves that
resolved distinction as compiler-semantic candidate meaning.

The platform profile itself does not become HIR authority.

HIR carries the resolved obligation required to interpret the candidate without reopening the Java or Kotlin platform.

```text
source platform declaration
    ↓ frontend resolution
resolved platform obligation
    ↓ participates in
Resolved Contract HIR candidate
```

---

## 11.2. HIR Candidate Sufficiency

A candidate that uses a Platform-Native Surface is sufficient for later Establishment only when HIR can state every
platform distinction that the owning law needs to interpret that candidate.

HIR consumers must not need to ask:

```text
what does this Java class mean on this JDK?
what did Kotlin map this source type to?
was this operation using ambient state?
which platform contract did this collection surface promise?
```

Those questions belong to frontend resolution.

---

## 11.3. Platform Surface Binding and Contract Meaning Remain Separate

HIR distinguishes the resolved platform-facing obligation from the owning Contract candidate meaning that consumes it.

A platform surface may be required later to regenerate a user-facing API or verify user realization while not itself
being Definition identity.

Conversely, one platform distinction can be part of Definition meaning when the owning 1D law makes that distinction a
determinant.

Physical co-location of those materials does not merge their semantic roles.

---

## 11.4. HIR Information-Loss Boundary

HIR may erase implementation details that no remaining authority or legal observer requires.

HIR may not erase a platform-native distinction while that distinction remains required by:

- the owning Contract candidate,
- Establishment,
- a later legal user-realization boundary,
- a generated API surface,
- or a downstream preservation proof.

The list names consumers of the law. It does not require one universal metadata record.

The transformation that removes a distinction carries the burden of proving that no remaining legal observation depends
on it.

---

## 11.5. HIR Equality and Reuse

Platform provenance, target attribution, candidate semantic equality, and physical profile identity remain separate.

Two HIR projections can be semantically equal even when they were resolved under different profile revisions, provided
current resolution proves the same consumer-visible meaning.

A changed profile revision invalidates only the products whose determining resolution can change.

HID, fingerprint, serialized bytes, or profile row identity may accelerate comparison. None of them establishes HIR
semantic equality by itself.

---

## 11.6. No Runtime Platform Re-Interpretation

After Visible HIR is formed, Establishment, verifier, optimizer, backend, diagnostics, and query consumers do not reopen
runtime classes or standard-library documentation to rediscover native meaning.

A missing obligation is a frontend/HIR defect. It is not repaired by later interpretation.

---

# 12. Establishment Contract

## 12.1. No Independent Established Platform Authority

This ADR does not create `Established Platform Material` as a new Contract authority.

Platform resolution produces compiler-semantic obligation material. The owning Contract establishes its own meaning
under ADR-0063.

```text
resolved platform obligation
+
complete owning Contract candidate
    ↓
owning Establishment law
    ↓
Established owning material
```

The platform remains external source meaning that the owning authority is required to respect where applicable.

---

## 12.2. Establishment Input Requirement

If one platform-native distinction is required to interpret the candidate, the Establishment input must already carry an
exact resolved relation to that distinction.

Establishment may not resolve an unresolved Java class, inspect one runtime object, select one Kotlin mapping, or
consult
one provider to complete candidate meaning.

An unresolved or partial platform obligation makes the Establishment input incomplete.

---

## 12.3. Establishment Output Requirement

Successful Establishment preserves only the meaning owned by the establishing Contract.

Where an admitted platform distinction is part of that meaning, the output preserves it exactly until another authority
explicitly establishes different meaning.

Establishment does not additionally establish:

```text
platform profile authority
backend representation
native operation reachability
optimization legality
adapter provenance as semantic identity
```

Those remain separate concerns.

---

## 12.4. Later 1D Authority May Change the Domain

Platform preservation at one boundary does not freeze meaning for the whole pipeline.

Admission may refuse material. Canonicalization may establish another representative relation. Lowering may establish a
target meaning that no longer carries every source distinction. Fact may apply its own sameness law.

That change is legal only because the later authority owns a new judgment or result meaning.

It does not rewrite what the earlier platform-facing Input or user-realization surface meant.

---

## 12.5. Canonical Contract World Boundary

The Canonical Contract World contains the Established Definition meaning owned by Contracts.

It does not need to retain the whole Platform-Native Profile.

It retains only the established meaning and exact relations required by the owning Contract. Compiler-side platform
surface bindings may remain in separate products when generated APIs, realization verification, diagnostics, or backend
projection still need them.

This separation prevents platform metadata from becoming Contract authority while preserving downstream sufficiency.

---

# 13. User Realization Contract

## 13.1. Declared Native Surface Is Preserved

When user realization receives or returns an admitted native surface, Kontrakt preserves the observable contract that
the
user was legally allowed to rely on at that boundary.

Internal representation freedom does not permit a weaker substitute.

If Kontrakt cannot realize the declared surface without losing an observable obligation, that realization is invalid.
The compiler must reject it rather than silently narrowing the contract.

---

## 13.2. Native Value Does Not Open a Capability Tunnel

The presence of a native value in user realization does not make arbitrary reachable platform operations legal.

The realization verifier still applies the Native Operation law and the external-technology boundary.

A legal `Instant` parameter does not authorize current-time acquisition. A legal collection does not authorize a lazy
framework-backed collection that reaches a database while being observed.

Only the declared native obligation crosses the boundary.

---

## 13.3. Local Platform Values

User realization may create and manipulate supported local Java or Kotlin values when those operations are legal under
the platform profile and surrounding realization law.

Such local values do not gain Contract authority merely because they exist inside the implementation.

When one local result is proposed back into a Contract-visible boundary, the receiving Contract judges that result under
its own law.

This keeps ordinary JVM implementation ergonomics without allowing local object state to become hidden Contract state.

---

## 13.4. Result Re-entry

A user realization result that carries a native platform surface is candidate material until the next owning boundary
accepts or establishes it.

User code cannot establish Fact, Publication, or another Contract result merely by constructing the corresponding Java
or
Kotlin object.

The platform surface remains usable. Authority still comes from the Contract pipeline.

---

# 14. Realization and Optimization Preservation

## 14.1. Observation-Preservation Law

A physical realization is valid only when every platform-native obligation that remains legally observable is preserved.

```text
resolved / established semantic obligation
    ↓ constrains
optimized representation
    ↓ observed through
legal Contract or user boundary
```

Representation equality is not the criterion.

The criterion is preservation of the applicable semantic and platform-visible observation law.

---

## 14.2. Representation Replacement Law

A Java or Kotlin object may disappear when object identity is not part of the admitted meaning.

The physical representation may be replaced only when every distinction that remains observable under the established
platform and Contract obligations is preserved. The replacement itself does not establish new meaning and does not
become new semantic authority.

This ADR does not choose the replacement representation or the mechanism used to realize it.

---

## 14.3. Semantic Anchor Must Survive Required Observation

Hot execution material does not need to carry full frontend metadata beside every value.

It must retain an exact relation, plan obligation, verified projection, or another sufficient semantic anchor so that
the
compiler can justify the realized behavior against the required platform contract.

An implementation may encode that relation in a compact form. The physical identifier or encoding is not the obligation
itself.

---

## 14.4. No Early Loss for Target Convenience

Later IR and backend transformations may progressively remove high-level material only after the removed distinction is
proven unnecessary for every remaining legal observation.

Target convenience is not a proof.

The current JVM representation also does not define the highest legal information-loss point.

---

## 14.5. Transformation Validation Obligation

A transformation that can affect a platform-visible obligation must have a validation path appropriate to its risk.

V1 does not mandate one proof technology.

Reference execution, structural verification, differential checking, pass-specific preservation checks, or later formal
proof may be used. The architecture must preserve the input and output semantic relation needed to perform that
checking.

This keeps V1 practical while leaving a direct seam for stronger translation validation.

---

# 15. Failure and Diagnostic Boundary

## 15.1. Unsupported Surface Is a Compile-Time Refusal

An unknown platform type, unsupported target, illegal role, forbidden native operation, or ambiguous mapping fails
before
Contract authority is established.

That refusal is a compiler/frontend result.

It is not an Input occurrence failure and does not create an Established Failure merely because the user sees a
diagnostic.

---

## 15.2. Runtime Carrier Violation Belongs to the Owning Boundary

A supported declaration can still receive one runtime carrier that cannot form the declared value coherently.

The owning boundary decides that occurrence result.

ADR-0064 and ADR-0072 own Input-time carrier refusal for Input and collection cases. This ADR does not create a parallel
platform runtime failure authority.

---

## 15.3. Forbidden User Realization Operation Is Verification Failure

If user implementation reaches an Adapter-only capability or an operation outside the permitted native surface, the
realization is not admitted as a legal governed implementation.

The verifier reports the exact forbidden boundary relation.

It does not reinterpret the operation as a Contract Failure that occurred during a valid run.

---

## 15.4. Backend Preservation Failure Is a Compiler Defect or Rejection

If the backend cannot preserve an already-supported native obligation, it cannot silently emit a weaker machine.

The compiler must reject the unsupported realization or treat the discrepancy as a compiler correctness defect.

A user Contract does not absorb backend miscompilation as ordinary domain failure.

---

# 16. Platform Evolution

## 16.1. New Platform Version Audit

Supporting a new Java or Kotlin version requires a semantic compatibility audit.

The audit checks newly available surfaces, removed or changed guarantees, Java/Kotlin mapping changes, experimental or
preview status, hidden-input behavior, role legality, HIR representability, and preservation through user realization.

Successful compilation against the SDK is not sufficient evidence.

---

## 16.2. Adding One Native Surface

A new profile entry is admitted only when Kontrakt can state its relevant observable contract completely enough for
every
role it claims to support.

The audit must answer:

```text
What exact declaration or operation is being admitted?
Which explicit platform target owns that contract?
What observations are guaranteed?
What hidden inputs or capabilities can it reach?
Which Contract-facing and realization roles are legal?
What HIR meaning must survive?
Where may information first be discarded?
How is preservation checked after optimization?
```

If those questions cannot be answered, the surface remains Adapter-only or unsupported.

---

## 16.3. Stable, Preview, Experimental, and Vendor Surfaces

Stable standard Java and Kotlin contracts are candidates for the native profile.

Preview, incubating, experimental, vendor-specific, JDK-specific, or implementation-specific APIs are excluded from the
baseline unless a later explicit decision adds one with a version-scoped compatibility law.

Experimental Kotlin stdlib status and JDK availability are evidence for this classification. They do not replace the
Kontrakt audit.

---

## 16.4. Vendor Neutrality

Kontrakt native meaning follows the admitted standard platform contract, not one vendor's undocumented behavior.

A vendor-specific extension remains external unless explicitly supported outside the baseline.

A compiler running on one vendor JDK must not change native meaning merely because that implementation exposes
additional
classes, providers, or behavior.

---

# 17. Reuse and Incremental Law

Platform resolution is a compiler product and may be reused.

Reuse is valid only for the exact determining source declaration, Platform Target Binding, relevant profile revision,
source-language mapping, role context, and other producer-owned semantic inputs.

A profile change does not require invalidating unrelated HIR merely because one global profile file changed physically.
The producer may expose finer semantic projections so that only affected native-surface resolutions change.

Incremental evaluation cannot disagree with a clean resolution.

The profile row, query node, cache key, fingerprint, or persistent artifact does not become platform or Contract
authority.

---

# 18. Consumer Sufficiency

Frontend resolution must leave enough material for downstream consumers to do their own jobs without reinterpreting the
platform.

Establishment needs the exact resolved obligation required by the owning Contract candidate.

Generated API formation needs the platform-facing surface that remains part of the legal user boundary.

User-realization verification needs the admitted operation and capability boundary.

Optimization needs the observation-preservation obligation that limits information loss.

Diagnostics needs exact provenance and classification sufficient to explain why one surface is native, Adapter-only, or
unsupported without becoming authority itself.

No consumer may add a platform distinction merely because the producer omitted it.

---

# 19. Verification and QA Obligation

A claimed native surface requires executable evidence in addition to documentation.

Kontrakt must maintain conformance tests for supported target versions and negative tests for capability boundaries.
Where a platform operation is optimized or re-realized, differential or reference checking must be possible against the
unoptimized legal behavior.

Clean compilation and incremental compilation must agree on resolved native meaning.

Cross-version tests must verify that adding support for a new Java or Kotlin target does not silently change meaning for
an older explicit target.

Determinism tests must vary worker scheduling, cache state, physical ordering, and host runtime where practical while
requiring identical semantic resolution for identical explicit inputs.

The exact test harness is implementation. The obligations are not optional for a surface that Kontrakt claims to
support.

---

# 20. Intentionally Open

The physical schema of the Platform-Native Profile remains open.

The exact Kotlin frontend integration and Java frontend integration remain implementation work.

Additional vanilla value candidates such as `UUID`, `Optional`, URI-related values, Kotlin `Duration`, `ZoneId`, and
`ZonedDateTime` remain open until separately audited. Their apparent value-like shape is not sufficient for automatic
admission.

MIR, LIR, JVM Plan/IR, physical layout, representation replacement, collection realization, reprojection strategy, and
backend specialization remain Design work.

The exact translation-validation technology remains open.

Framework-specific Adapter generation remains outside this ADR.

---

# 21. Rejected Directions

## 21.1. Admit All of `java.base`

Rejected.

The module contains both fundamental values and capability/runtime mechanisms.

---

## 21.2. Admit All of `java.*` or Java SE

Rejected.

Standard-platform status is wider than the Contract-native value boundary.

---

## 21.3. Admit All of `kotlin-stdlib`

Rejected.

The library contains ordinary values together with lazy computation, I/O helpers, concurrency support, and other
behavior that requires a separate legality decision.

---

## 21.4. Native by Package, Interface, or Assignability

Rejected.

Namespace, inheritance, and assignability do not prove complete semantic compatibility or role legality.

---

## 21.5. Native by Value-Based Classification

Rejected.

Value-based status is useful evidence. It is not a complete capability, operation, version, or role audit.

---

## 21.6. Native Type Means Every Operation Is Native

Rejected.

A legal value can expose an operation that obtains hidden environment state.

---

## 21.7. Third-Party Contract Preservation Inside Core

Rejected by default.

External library and framework contracts enter through Adapter or another explicit external boundary. The narrow JVM
native exception does not generalize to the ecosystem.

---

## 21.8. Backend Reconstructs Platform Meaning

Rejected.

Backend inspection cannot repair missing frontend semantic resolution.

---

## 21.9. Lower to JVM Representation Before Preservation Obligations Are Closed

Rejected.

Physical JVM shape is not sufficient evidence that all source-level Java or Kotlin obligations have been retained.

---

## 21.10. Platform Target Inferred from Compiler Host

Rejected.

The current JDK, installed provider set, or runtime classpath cannot silently define compilation meaning.

---

# 22. Consequences

Java and Kotlin remain natural user surfaces without making the JVM ecosystem part of Contract authority.

The frontend gains a stronger responsibility. It must resolve platform-native meaning exactly rather than treat host
classes as opaque type names.

HIR becomes more complete. It retains admitted platform obligations only as long as later authority or legal observation
requires them, while remaining free to erase implementation mechanics.

Establishment remains clean. It consumes resolved candidate meaning and does not create a parallel Java authority.

User realization remains practical. Ordinary native values and legal standard operations can be used directly, while
capability acquisition and external technology remain behind the airlock.

Optimization gains representation freedom because the observable preservation obligation is separated from JVM object
topology.

Platform upgrades become explicit compatibility work. This adds maintenance cost, but it prevents host-version drift and
silent semantic change.

ADR-0064, ADR-0071, and ADR-0072 require corresponding references to this boundary. The future realization design must
consume the operation and observation-preservation laws defined here.

---

# 23. Non-Normative Engineering Basis

Kontrakt project law remains authoritative. The external systems below are engineering evidence only.

Java SE distinguishes portable `java.*` APIs from JDK-specific `jdk.*` APIs, but the portable platform is still wider
than Kontrakt's native value boundary. `java.base` itself includes fundamental language classes together with
reflection,
foreign access, runtime support, and reference-processing facilities.

Java's value-based-class guidance is useful because it explicitly separates value substitution from object identity for
classes such as primitive wrappers and many date/time values. It also demonstrates why JVM object identity cannot be the
semantic identity of admitted value material.

`BigDecimal` demonstrates that a platform value can expose more than one important equality-related relation. Numerical
ordering and `equals` differ because scale is observable. This is direct evidence against replacing platform meaning
with
a smaller compiler-preferred numerical model.

Kotlin/JVM mapped types demonstrate that source-level language meaning cannot be recovered from runtime class identity
alone. Kotlin distinguishes read-only and mutable collection surfaces even though Java runtime representations are
mapped underneath them.

Rust `core` is useful as a boundary precedent because it deliberately excludes heap allocation, concurrency, and I/O
from its minimal portable foundation. Kontrakt uses a different boundary, but the example supports separating basic
language values from platform integration.

The WebAssembly Component Model similarly distinguishes ordinary value types from resource handles and keeps canonical
lifting/lowering separate from higher-level type meaning. This supports preserving semantic obligation above replaceable
physical realization.

Recent work on cross-language and cross-target compilation reinforces the same boundary. Forcrat reports that C and Rust
I/O APIs require explicit origin and capability analysis because library functions that appear analogous do not preserve
the same resource semantics automatically. Wasm cross-compilation studies have found silent semantic differences caused
by standard-library implementations, unsupported system APIs, and compiler defects. Java reproducible-build research
shows that hidden or unstable build inputs remain a practical source of nondeterminism at ecosystem scale.

Recent verified-compilation work such as HELIX and cross-level RISC-V refinement work further support keeping explicit
semantic relations across multiple lowering levels instead of assuming that target representation preserves source
meaning automatically. Kontrakt does not require those proof systems in V1, but it preserves the semantic seams needed
for later translation validation.

---

# 24. Final Law

Kontrakt accepts a narrow, explicitly audited JVM Platform-Native Surface because Java and Kotlin are unavoidable parts
of
its host programming environment.

That exception ends at the exact admitted platform contract.

```text
explicit platform target
    ↓
exact native-surface resolution
    ↓
Resolved Contract HIR obligation
    ↓
owning Contract Establishment
    ↓
representation may change
    ↓
remaining legal observations preserve the required contract
```

Java, Kotlin, the JDK, and their libraries do not become general Contract authority.

A later 1D Contract may establish new meaning under its own law. External technology enters through Adapter or another
explicit boundary. Compiler optimization may replace physical form, never the meaning that a remaining authority or
legal
observer is still entitled to see.