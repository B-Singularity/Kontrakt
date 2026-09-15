# ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary

## Status

Proposed

## Date

2026-09-15

## Related

- *What Contract Is*
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
implicitly native, capability, lifecycle, environment, and external-technology contracts can enter the Core through the
host platform.

It can also accept too little. If ordinary JVM values require artificial wrappers only to cross a Contract-facing
boundary, Kontrakt stops behaving like a JVM compiler and forces users to abandon normal Java and Kotlin value surfaces.

The required boundary is therefore narrow.

Kontrakt must decide which JVM surfaces it directly ratifies. When Contract theory applies to one of those surfaces,
Kontrakt is responsible for interpreting the relevant platform contract correctly. Where Kontrakt has no Contract reason
to reinterpret or transform platform behavior, it acts conservatively and leaves that behavior to the JVM.

Everything outside the ratified native boundary remains Adapter-only or unsupported.

---

# 3. Decision

Kontrakt defines an explicitly ratified **JVM Platform-Native Surface**.

Platform-Native status is owned by Kontrakt. It is not selected by the user and is not inferred from package membership,
classpath presence, runtime availability, inheritance, or popularity.

A ratified surface may participate directly in a Contract-facing or legal user-realization boundary only within the role
that Kontrakt has admitted for it.

Ratification does not transfer Contract authority to Java, Kotlin, or the JVM. It creates a preservation obligation for
the platform meaning that Kontrakt has chosen to admit.

Kontrakt may apply its own 1D Contract law at an explicit later Contract boundary. It may not silently rewrite the
earlier
platform meaning to make compilation or optimization easier.

---

# 4. Native Boundary Principle

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

These may contain both ordinary values and capabilities. Namespace membership is therefore evidence only.

---

## 4.2. Contract-Relevant Interpretation

Kontrakt does not need to re-model the whole JVM contract of every ratified surface.

It must understand every platform distinction that remains legally observable through the Contract boundary or user
realization in which the surface is admitted.

A JVM implementation detail that no legal observer can depend on does not become Contract meaning.

A guaranteed platform distinction that the user can legally rely on cannot be dropped merely because Kontrakt has a more
convenient internal representation.

---

## 4.3. Later 1D Authority

Platform preservation does not freeze one value meaning for the whole pipeline.

Input and user realization preserve an admitted JVM surface where that surface is exposed to the user. A later 1D
Contract may reject the material or establish a different meaning under its own authority.

For example, Canonicalization may establish a new equivalence relation that was not the equality relation of the
original
platform value. That does not retroactively change what the earlier platform-facing value meant.

---

## 4.4. Conservative JVM Delegation

Kontrakt does not replace platform behavior merely because replacement is possible.

When Contract-established knowledge does not justify a transformation, the platform operation or representation remains
intact and normal JVM optimization remains responsible for it.

When Kontrakt does transform a ratified platform surface, it must preserve every platform and Contract observation that
remains legal after that point.

If that preservation cannot be established, Kontrakt does not perform the transformation.

---

# 5. Ratification Law

Ratification is governed by three laws.

## 5.1. Closure

Kontrakt must be able to close the relevant meaning of the admitted surface.

The compiler must know the observable obligations needed by the admitted role and the constituent meaning required to
interpret them. A known outer type does not make an unknown constituent legal.

`List<ExternalResource>` therefore does not become native merely because `List` is a ratified platform surface.

---

## 5.2. Authority Isolation

Ratification may not import undeclared authority into the governed machine.

A surface is not an ordinary Native value surface when its admitted meaning requires a live external resource, ambient
environment, provider, lifecycle owner, framework runtime, vendor facility, or another hidden capability.

This rule applies even when the API is shipped as part of Java SE or Kotlin stdlib.

---

## 5.3. Preservation

Kontrakt may directly admit only the meaning it can preserve for every remaining legal observer.

The compiler may keep the original JVM behavior when that is the safest realization. It may use another realization only
when the relevant observations remain equivalent.

Uncertainty is not permission to guess. An unresolved surface remains Adapter-only or unsupported.

---

# 6. Ratification Verification

## 6.1. Verification Domain

Platform ratification is compiler verification work.

It belongs in the Verification domain, but it is not Contract Establishment and it does not become Contract authority.

The Platform Boundary Verifier is a sibling of Contract and realization verification concerns. It verifies whether the
compiler may rely on one platform surface under the laws of this ADR.

---

## 6.2. Full Ratification Audit

Kontrakt performs the expensive audit when support for a platform surface or platform version is created or changed.

That audit applies the ratification laws and checks the relevant standard contract. It is not repeated from first
principles for every user compilation.

The audit must be able to reject a surface when its relevant contract cannot be closed or when a capability boundary
cannot be isolated.

---

## 6.3. Ratified Native Surface Catalog

Successful audit produces compiler-owned **Ratified Native Surface Catalog** material.

The catalog records already-audited platform knowledge needed for later compilation. It is not a flat class-name
whitelist and it is not Contract authority.

Catalog knowledge is about the platform surface itself. It does not duplicate one entry for every Kontrakt role or every
generic instantiation in which that surface may later appear.

At minimum, the compiler must be able to recover from catalog material:

```text
the exact ratified platform surface
its platform classification
the observable obligations Kontrakt has ratified
its relation to any reusable operation profile
any exact operation-specific exception
whether direct use is Native, delegated, Adapter-required, or unsupported
```

A missing catalog entry does not imply permission. Unknown material still fails closed.

The physical representation of this catalog is implementation work.

Deleting or rebuilding the catalog cannot change which result is correct. A wrong catalog entry is a compiler defect.

---

## 6.4. Compilation Surface Gate

Normal user compilation does not rerun the full ratification audit.

The compiler resolves only the platform surfaces actually encountered by the compilation and checks them against the
ratified catalog.

The compilation gate still performs context-sensitive checks that cannot be precomputed globally. In particular, it must
verify the requested Kontrakt role, the closure of resolved constituent meaning, and the owning boundary in which the
platform surface is being used.

Platform knowledge and use legality are different products. A platform surface may be ratified once and then be legal in
one Kontrakt role, carrier-only in another, or rejected in another without duplicating the platform audit.

The same ratified surface used many times in one compilation may reuse one valid classification result.

---

## 6.5. External Technology Isolation Gate

Platform ratification and external-technology isolation are related but different checks.

The compilation or realization boundary must still reject Adapter-only capability, framework, provider, or external
technology that attempts to cross into governed material through a ratified platform surface.

A native carrier does not legalize the external authority that produced it.

---

## 6.6. Independent Catalog Validation

The catalog itself requires independent verification appropriate to a compiler release or platform-support update.

Kontrakt should be able to compare its built-in platform knowledge with the normative platform surface and run positive
and negative conformance tests without relying only on the same fast compilation lookup path.

The exact validation tool is implementation work.

---

# 7. Ratification Unit and Use Legality

Ratification is not attached to a package, module, runtime class hierarchy, or one global class-level boolean.

Kontrakt separates stable platform knowledge from compilation-specific use legality.

## 7.1. Exact Platform Surface

The release-time audit is anchored to an **Exact Platform Surface**.

An Exact Platform Surface is the smallest platform declaration surface for which Kontrakt can state one coherent
ratification result without importing unrelated platform behavior. It may represent a value or type surface, an
interface surface, or an exact callable surface.

Package or namespace membership is too broad. Runtime class identity alone is also insufficient when Java and Kotlin
source contracts expose distinctions that share one JVM representation.

The exact physical key used to identify the surface is compiler implementation work. Ratification requires enough
identity to prevent overloads, unrelated owners, or source-language distinctions from being conflated.

---

## 7.2. Platform Surface and Kontrakt Role Are Separate

The platform contract of a surface is audited independently from the Kontrakt role in which a user later requests it.

The catalog therefore does not pre-expand one platform surface into every Input, Fact, Operation, Output, or later
Contract role.

Normal compilation combines the ratified platform knowledge with the requested role and owning boundary to determine
use legality.

This prevents a platform fact from being rewritten merely because Kontrakt adds a new Contract role later.

---

## 7.3. Constituents Are Checked by Closure

Generic arguments, array components, map keys and values, nested aggregates, and other constituent meaning are not
pre-expanded into every possible catalog combination.

The outer platform surface is audited once. Actual compilation verifies that every resolved constituent required by the
use also satisfies the Native boundary.

`List<BigDecimal>` therefore does not require a dedicated release-time catalog entry. `List<ExternalResource>` does not
become legal merely because the outer `List` surface is ratified.

---

## 7.4. Operations Are Separate from Type Ratification

Ratifying a value or type surface does not ratify every method, factory, constructor, or static operation reachable from
it.

An operation that reads ambient state, acquires a capability, depends on a provider, exposes lifecycle authority, or has
another materially different platform contract must be classified independently from the receiver or result type.

The operation audit is anchored to an exact callable identity sufficient to distinguish owner, overload, and signature.
The ADR does not prescribe the physical encoding of that identity.

---

## 7.5. Semantic Operation Profile

Repeated operation meaning may be represented by a reusable **Semantic Operation Profile**.

The profile summarizes already-audited platform obligations shared by multiple exact callables. It exists to avoid
duplicating the same platform analysis and verification knowledge across the catalog.

The profile is compiler knowledge. It is not Contract authority and it does not define the platform contract.

The direction is:

```text
platform callable contract
    -> ratification audit
    -> reusable semantic operation profile
```

It is never:

```text
semantic operation profile
    -> platform meaning
```

An exact callable-specific fact or exception takes precedence over a shared profile. A profile cannot make a capability
operation Native merely because neighboring operations of the same type are Native.

The exact vocabulary and contents of Semantic Operation Profiles remain open for further review.

---

## 7.6. Delegated Operations

The catalog is not intended to become a complete reimplementation of the Java or Kotlin API.

When an admitted operation needs no Contract-specific reinterpretation or transformation, Kontrakt may conservatively
delegate the original platform behavior to the JVM.

Delegation is not inferred from the absence of catalog knowledge. It must follow from ratified platform knowledge that
the operation may remain untouched under the current boundary. Unknown operations still fail closed.

Delegated behavior remains subject to the same preservation law. Kontrakt may not later transform it in a way that loses
a legal platform or Contract observation.

---

## 7.7. Platform Use Verification

The compilation-time result is **Platform Use Verification**, not a new platform audit.

Conceptually it consumes:

```text
Ratified Platform Surface
Requested Kontrakt Role
Resolved Constituents
Relevant Owning Boundary
```

and establishes whether that use is directly legal, carrier-only, Adapter-required, or unsupported.

The exact compiler product name and storage representation remain implementation work.

---

## 7.8. Audit Questions

The full audit must answer enough questions to expose hidden contract infiltration rather than merely confirm that a
surface looks value-like.

It checks whether all required constituent meaning can be closed, whether the surface can import ambient authority or
lifecycle, whether external mutation can invalidate an earlier judgment, whether object identity is required by the
admitted meaning, and whether the platform leaves an observation unspecified or implementation-dependent.

Java/Kotlin source mappings are also part of the audit when one JVM representation exposes different source-level
contracts.

These questions are verification criteria. They do not require one universal runtime metadata object.

---

# 8. Native, Carrier, Adapter, and Unsupported Are Different Results

Ratification is not one global boolean attached to a JVM class.

A platform surface may be directly Native for one declared role. A concrete implementation may be accepted only as a
carrier of that surface. Another operation may require an Adapter even though its receiver is Native.

An unknown or incompletely understood surface is unsupported until Kontrakt explicitly decides otherwise.

This ADR therefore distinguishes the following semantic outcomes:

```text
Native Surface
Carrier Only
Adapter Required
Unsupported
```

The implementation does not need to encode these outcomes as one enum.

---

# 9. Initial Native Baseline

The initial V1 baseline includes the JVM value surfaces already considered unavoidable and sufficiently understood for
ordinary Java and Kotlin Contract usage.

It includes primitive values, the ordinary value aspect of primitive wrappers, `String`, arrays including primitive
arrays, language enum values, the standard Java and Kotlin collection surfaces governed by ADR-0072, `BigInteger`,
`BigDecimal`, and the major immutable value-oriented `java.time` surfaces that can be used without acquiring ambient
time
or provider state.

This list establishes the initial audit scope. It does not imply that every operation reachable from those types is
Native.

Additional surfaces require the same ratification process.

---

# 10. Adapter Boundary

Frameworks, optional libraries, vendor APIs, live resource handles, ambient environment access, and external technology
do not become Native merely because they execute on the JVM.

Such material enters through an explicit Adapter or another separately approved external boundary before it can
influence governed Contract processing.

An Adapter may produce a ratified platform value or user-defined candidate material. The external authority that
produced
that value does not accompany the value into the Core.

Unknown external technology fails closed.

---

# 11. Frontend and HIR Obligation

Frontend resolution is responsible for recognizing a referenced JVM surface and applying the Platform Boundary
Verification result.

HIR does not need to reproduce Java or Kotlin library internals. It must preserve the platform distinction that a later
Contract authority or legal user-realization observation still requires.

The frontend or HIR may discard implementation detail once that detail has no remaining semantic role.

Later consumers do not reopen runtime classes or platform documentation to repair a platform distinction that frontend
resolution failed to preserve.

The exact HIR schema remains owned by ADR-0071 and later IR design.

---

# 12. Establishment Boundary

Platform ratification is not Contract Establishment.

This ADR does not create a parallel `Established Platform Material` authority.

The owning Contract establishes its own meaning under ADR-0063. When that meaning depends on an admitted platform
obligation, the required distinction must already be resolved before Establishment.

A later Contract authority may establish different meaning under its own law. It does not rewrite the earlier platform
contract.

---

# 13. User Realization Boundary

User realization may use a ratified JVM surface where the relevant role permits it.

Kontrakt preserves the platform behavior that user code is legally allowed to observe at that boundary. Internal
representation freedom does not permit a weaker substitute.

A Native value does not open a tunnel to arbitrary JVM capability or framework operations. Realization verification
still
applies the Adapter boundary.

---

# 14. Optimization Non-Interference

Kontrakt optimization is strongest where Contract-established knowledge gives the compiler information that the JVM does
not have.

That advantage does not grant permission to replace unrelated platform behavior.

A transformation that changes a ratified platform realization is legal only when every remaining legal platform and
Contract observation is preserved. If the compiler cannot establish that preservation, the original platform behavior is
kept and the JVM retains optimization responsibility.

This ADR does not choose MIR, LIR, physical layout, object elimination, collection realization, or another optimization
mechanism.

---

# 15. Reuse and Incremental Boundary

The expensive ratification audit is not a per-use compilation activity.

V1 may ship pre-audited catalog material and reuse valid Exact Platform Surface classification and Platform Use
Verification results within one compiler generation. The query or memoization mechanism is compiler realization and
remains replaceable.

Stable platform knowledge and context-specific use legality remain separate reuse boundaries. A source location change
does not by itself change the ratified meaning of `BigDecimal`, and a new Kontrakt role does not require re-auditing the
platform contract of every previously ratified type.

V2 may persist or incrementally repair these verification products when their explicit inputs and validity law permit
it. V2 does not change the semantic result of ratification and does not make cache state, query topology, a Semantic
Operation Profile, or a catalog row into authority.

Clean recomputation remains the correctness reference.

The exact persistent key, invalidation granularity, scheduling policy, and repair algorithm remain open.

---

# 16. Refusal Boundary

An unsupported or ambiguous platform surface is rejected before it becomes Contract authority.

A surface that is known but requires an Adapter is rejected when it is used in a direct Native role without that
boundary.

A forbidden platform or external-technology operation reached by user realization is a realization-verification failure,
not a valid Contract Failure during governed execution.

Diagnostics explain the violated boundary. They do not decide Native status.

---

# 17. Determinism

Platform ratification and compilation-time classification are deterministic compiler products.

For the same explicit compiler inputs and supported platform knowledge, worker scheduling, cache state, filesystem
order,
reflection order, runtime provider order, or the currently executing host JDK cannot change the result.

A fast cached path and a clean uncached path must agree.

---

# 18. Intentionally Open

The exact catalog schema remains open.

The exact Java and Kotlin frontend integration remains open.

The exact platform-version representation remains open until the related compiler target model is decided.

The exact vocabulary and minimum semantic contents of Semantic Operation Profiles remain open.

The exact set of additional Native values and Native operations remains open and will be refined through the
ratification
questions defined by this ADR.

HIR, MIR, LIR, backend representation, translation validation, and physical optimization remain owned by their
respective
ADR and Design work.

Framework-specific Adapter generation remains outside this ADR.

---

# 19. Rejected Directions

## 19.1. Native by Namespace

Rejected.

A standard namespace contains both ordinary values and capability or runtime mechanisms.

---

## 19.2. Native by Runtime Availability

Rejected.

Classpath presence, host JDK availability, inheritance, or assignability does not prove ratification.

---

## 19.3. Full Audit on Every Compilation Use

Rejected.

The expensive platform audit belongs to compiler/platform support work. Normal compilation performs exact lookup and
context-sensitive legality checks only for surfaces that are actually used.

---

## 19.4. Catalog as Authority

Rejected.

The catalog is verified compiler knowledge. It does not define Contract meaning and cannot make an illegal surface legal
because one row exists.

---

## 19.5. Native Type Means Every Operation Is Native

Rejected.

A legal value surface can expose an operation that acquires ambient state or another capability.

---

## 19.6. Platform Optimization by Default Replacement

Rejected.

Kontrakt transforms platform behavior only when the relevant preservation obligation is established. Otherwise the JVM
retains that responsibility.

---

## 19.7. One Global Native Boolean per JVM Class

Rejected.

A value surface, a carrier, and an operation can have different legal results. Class-level admission is too coarse to
represent those distinctions.

---

## 19.8. Role-Expanded Platform Catalog

Rejected.

Platform knowledge is not duplicated for every Kontrakt role. Role legality is checked when the ratified surface is
used.

---

## 19.9. Catalog Entry per Generic Instantiation

Rejected.

Generic and aggregate combinations are checked by constituent closure during compilation. Pre-expanding every
combination would duplicate platform knowledge and create unbounded catalog growth.

---

## 19.10. One Independent Semantic Model per Callable

Rejected.

Exact callable identity is required, but repeated platform obligations may share a Semantic Operation Profile. Exact
callable exceptions remain independently expressible.

---

## 19.11. Delegation by Catalog Absence

Rejected.

Unknown operations fail closed. JVM delegation is a ratified result, not a default created by missing compiler
knowledge.

---

# 20. Consequences

Java and Kotlin remain natural user surfaces without making the JVM ecosystem part of Contract authority.

Platform support becomes explicit compiler verification work rather than scattered hard-coded type checks.

Normal compilation stays cheap because expensive platform ratification is performed outside the ordinary per-use path.
The compiler examines only the surfaces that a compilation actually uses and may reuse stable surface knowledge and
context-specific use-verification results independently.

The catalog does not explode across Kontrakt roles or generic instantiations. Exact callable identity remains available
where operation semantics differ, while repeated operation obligations may share audited semantic profiles.

The Adapter airlock remains strong. Framework, resource, provider, environment, and vendor authority cannot enter merely
through a standard-looking JVM type.

Frontend and HIR gain a preservation duty but do not become platform reimplementation layers.

Optimization remains conservative at the JVM boundary. Kontrakt exploits Contract-specific knowledge where it has real
semantic authority and otherwise leaves platform optimization to the JVM.

The detailed ratification criteria, additional Native surfaces, and version-evolution law remain open for the next
review
of this ADR.

---

# 21. Non-Normative Engineering Basis

The architecture follows a pattern used by production compilers and systems: expensive platform knowledge is audited or
generated outside the ordinary hot compilation path, while actual compilation performs narrow lookup and legality
checking over the referenced surface.

LLVM TargetLibraryInfo separates compiler-known library semantics from target-specific availability and binds known
library behavior to exact functions rather than an entire namespace. Rust compiler-known library items are explicit
rather than inferred from all of the standard library, and associated items are resolved from exact semantic owners.
Clang also generates compact compiler tables for large builtin surfaces instead of rediscovering their semantics from
source on every compilation.

These systems also show why identity precision and semantic reuse should be separate. A compiler can bind an exact
callable while sharing generated or summarized semantic knowledge across many callables. Kontrakt adopts that principle
without making another compiler's builtin model part of Contract authority.

Bazel and Nix show the corresponding failure mode from build systems: undeclared ambient inputs break correctness and
reproducibility even when the underlying tool or filesystem makes them conveniently available.

These systems are references for separation of concerns. They do not define Kontrakt Contract authority.