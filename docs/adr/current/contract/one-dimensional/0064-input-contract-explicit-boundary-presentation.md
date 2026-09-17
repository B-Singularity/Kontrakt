# ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0073: JVM Platform-Native Surface Exact Contract Preservation and External Technology Boundary
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0067: Lowering Contract
- ADR-0066: Canonicalization Contract
- ADR-0065: Admission Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

---

## 1. Context

Input is the first one-dimensional Contract of the inbound airlock.

It declares which outside presentation may appear for an Operation, which distinctions the boundary must preserve, and
which values later Contracts may judge.

Input does not make outside material true and does not place it inside the core.

Admission still decides whether the presented material may continue. Canonicalization may establish a stable same-shape
representative when selected. Lowering later declares how directly named Input coordinates may participate in candidate
Operation-parameter Fact formation.

A host declaration may already state useful external contract evidence. Closed scalar values, finite alternatives,
presence distinctions, bounded presentations, and other explicitly defined Input forms do not become silent merely
because Kotlin, Java, or another frontend carries them.

The host declaration and its object instances do not receive Kontrakt authority directly.

The declaration nominates external evidence. The selected source must be resolved into complete compiler-semantic Input
candidate meaning, and that candidate must cross the Input-owned Establishment boundary before it becomes authoritative
Input definition material.

---

## 2. Problem

The boundary must accept ordinary external presentations without letting host representation become Contract authority.

The machine must not infer Input meaning from object identity, constructor behavior, getters, runtime interfaces,
framework lifecycle, reflection, mutable storage, lazy evaluation, proxy activation, or snapshot timing.

The machine also must not require the user to restate the same presentation in a second Kontrakt-specific DTO when a
supported host declaration already states the needed external shape exactly enough to refine.

The V1 boundary therefore needs a narrow ratifiable presentation law.

Input must remain finite, explicit, and directly inspectable before Admission. One Input judgment must depend on one
complete and coherent presentation whose established meaning cannot be rewritten by later external mutation.

Outside structured or behavioral material may remain outside Kontrakt, but it must be formed through an explicit
replaceable presentation-formation boundary before authoritative Input judgment depends on it. Copying, ownership
transfer, snapshotting, pinning, persistent representation, copy-on-write, verified zero-copy, or another equivalent
formation strategy remains realization when it preserves the same Input meaning.

---

## 3. Decision Drivers

Input is presentation, not truth.

Outside source evidence may nominate a Contract, but only Input-owned Established Definition material receives Input
authority.

The Input boundary must judge one complete and coherent presentation. Physical carrier immutability is useful evidence
but is not itself the Contract determinant. If mutable or aliased storage participates, the realization must ensure that
the judgment observes one coherent presentation and that later external mutation cannot retroactively change the
established Input meaning.

V1 keeps Input flat at the Contract-authority boundary so a nested carrier, reference graph, inherited relation, or
runtime implementation topology does not become hidden Contract structure. This does not require every semantic value
topology to be physically flat. A bounded aggregate presentation may remain one direct coordinate when its complete
presentation law is closed without recursive authority discovery or adoption of an outside object graph.

The same resolved semantic basis under the same exact Input Authority and Version must have one deterministic Input
interpretation.

Compiler acquisition, planning, storage, or backend structure must not create a second semantic authority path.

A source that cannot be resolved into complete deterministic Input candidate meaning must fail before Input Definition
Establishment. A resolved candidate that does not satisfy the Input-owned Definition law establishes no authoritative
Input definition.

An Established Input Definition whose required boundary material is unavailable, incoherent, malformed, or otherwise
does not realize the declared presentation must stop before Admission.

Cross-cutting Policy, Governance, Budget, or Capacity outcomes remain owned by those Contracts.

---

## 4. Decision

### 4.1. Input Meaning

Input is the boundary presentation Contract.

```text
outside presentation evidence
    ↓ exact source and role resolution
Resolved Input Definition Candidate
+
Resolved IDL Input Binding Candidate
    ↓ Input-owned Definition Establishment
Established Input Definition
+
Authoritative Input Binding
    ↓
one complete coherent Input presentation
    ↓
Input boundary judgment
    ↓
Admission
```

Input declares the finite presentation surface that may appear.

It preserves the distinctions that later Contracts are allowed to observe.

Input does not declare that the values may continue. It does not canonicalize them. It does not lower them into core
Fact meaning. It does not invoke the user Operation.

### 4.2. Input Definition Meaning and Determinants

One Input Definition owns one complete boundary-presentation meaning.

Input Definition Meaning is determined only by Contract-visible presentation distinctions owned by Input. Those
determinants include the exact Input-local coordinate identities, the complete presentation meaning of each coordinate,
explicitly declared presence or absence, closed finite alternatives, presentation-defining bounds, aggregate
distinctions owned by Input, and any resolved platform distinction that changes Input-visible presentation meaning.

A presentation-level ordering distinction is a determinant only when the owning Input law declares that ordering as
observable meaning. Source coordinate declaration order, constructor parameter order, record-component order, traversal
order, or compiler discovery order is not semantic ordering by default.

Interface identity, Operation identity, the Input slot, IDL Binding identity, Policy or Governance selection, source
path or span, host class identity, runtime object identity, constructor or accessor identity, generated host machinery,
platform-profile row identity, compiler generation, HIR row, HID, dense handle, cache state, backend layout, and
realization strategy do not determine Input Definition Meaning merely because they participate in compilation or use.

Contract Version remains a separate semantic coordinate from local Definition Meaning. Every version-sensitive Input
Contract Authority owns its explicit Version history under ADR-0053. Two different Input Versions may resolve to equal
local Definition Meaning and still remain different exact Versioned Input Definitions. Content equality, structural
equality, HIDs, fingerprints, or compiler reuse do not merge Version Identity.

For an exact Versioned Input Definition, the Input Authority, its resolved Version Binding, and any authority-local
Definition coordinate required by ADR-0063 remain recoverable independently of compiler representation.

### 4.3. IDL Input Binding

Input uses the operation-local slot-selection law of ADR-0047.

A host declaration does not become Input because of its shape. One exact operation-scoped Input slot selects one exact
Versioned Input Definition candidate for the Input role at that machine position.

The Input Definition owns presentation meaning. The IDL Input Binding owns exact machine-position selection. The Binding
grants role and scope; it does not add, copy, reinterpret, or replace the Definition's presentation meaning.

One Established Input Definition may participate in more than one authoritative Input Binding when its exact Versioned
Definition identity is unchanged. Conversely, the existence of an Established Input Definition does not make it
applicable to every Operation. Application at one machine position requires that position's exact authoritative Input
Binding.

Changing the operation-scoped Input slot or changing the selected exact Versioned Input Definition changes the Input
Binding. Two selected Definitions with equal local presentation meaning but different Version Identities remain
different binding targets.

Input does not redefine the common IDL selection law. ADR-0047 owns operation-local slot selection and scope. ADR-0053
owns Version sovereignty. ADR-0063 owns Definition and Version reference semantics. This ADR requires those exact
relations to remain recoverable wherever Input judgment depends on them.

IDL Input Binding is distinct from ADR-0063 Owning Authority Binding, Version Binding, Required-Basis Binding, and
Applicability. Physical compiler representation may co-locate or fuse these relations, but their semantic roles do not
merge.

### 4.4. Flat V1 Presentation

V1 keeps the selected Input presentation flat at the Contract-authority boundary.

The presentation exposes one finite set of directly named coordinates.

A user-owned nested carrier, embedded Value Object, inherited part, interface-typed part, sealed runtime hierarchy,
recursive type, recursively discovered collection object graph, or shared-reference graph does not become a second
Contract inside Input.

A collection presentation is not excluded merely because a host language realizes it with an array or collection
carrier. It may participate directly only when the applicable Input law defines its complete bounded presentation
without recursive authority discovery, hidden equality or ordering semantics, mutable backing authority, or another
undeclared host convention.

Outside material whose complete presentation cannot be established under that law must be formed into a supported
declared presentation before authoritative Input judgment depends on it.

The flatness rule prevents host topology from becoming hidden Contract ancestry. It does not require every admitted
aggregate value to use one physically flat storage representation.

### 4.5. Source Evidence Is Not Authority

A supported Java or Kotlin declaration may nominate Input evidence.

The declaration remains source evidence. A runtime object remains a carrier.

Input identity and meaning are not derived from the class name alone, runtime type, object identity, constructor
execution, property accessor behavior, declaration position, or storage layout.

Equivalent local presentation meaning may later come from another frontend, schema compiler, serialization system, or
language without changing that local Input Definition Meaning. Exact Versioned Definition identity still remains
governed by the Input Authority and Version law rather than by content equality alone.

---

## 5. Resolved Input HIR and Definition Establishment

### 5.1. Resolved Input Definition Candidate

Frontend resolution produces one complete Resolved Input Definition Candidate before Input Definition Establishment.

The candidate preserves the exact resolved Input Authority under which Establishment will occur, the exact resolved
Authority-scoped Version Binding, and any authority-local Definition coordinate required by the Input identity law under
ADR-0063.

The candidate also preserves the complete Input-owned Definition Meaning. That meaning includes the exact Input-local
coordinate identities, each coordinate's complete presentation meaning, presence or absence, closed finite alternatives,
presentation-defining bounds, aggregate distinctions when applicable, and every exact semantic reference required to
interpret those meanings without reopening source.

When a platform-native distinction changes Input-visible presentation meaning, that resolved distinction participates in
the Definition Candidate meaning. When platform material is needed only to preserve a generated surface, verify a legal
realization, reproduce frontend resolution, or prove a downstream preservation obligation, HIR may retain that resolved
obligation without making it an Input Definition determinant.

Source provenance may remain associated with the candidate for diagnostics, source navigation, explanation, and
incremental work. Provenance does not become Input Definition Meaning, Version Identity, or authoritative Definition
identity.

The Resolved Input Definition Candidate does not acquire Interface, Operation, Input-slot, Policy, Governance,
runtime-occurrence, compiler-storage, or backend identity merely for consumer convenience.

### 5.2. Resolved IDL Input Binding Candidate

Frontend resolution also preserves the exact IDL Input selection relation separately from the Input Definition
Candidate.

Conceptually, one Resolved IDL Input Binding Candidate preserves:

```text
exact operation-scoped Input slot
    → exact selected Versioned Input Definition Candidate
```

The enclosing Interface and selected Operation subject must remain exactly recoverable through the resolved slot
subject. The Binding Candidate does not copy the selected Input Definition's coordinate or presentation payload.

A changed operation-scoped Input slot or a changed exact Versioned Input Definition target changes the Binding
Candidate. Source provenance, host representation, compiler identity, and runtime occurrence identity do not.

The Interface source does not repeat or mint Contract Version. Resolving the selected Input Contract also resolves its
declared Authority-scoped Version under ADR-0053, and the Binding Candidate preserves the exact Versioned target
selected by that resolution.

### 5.3. HIR Information-Loss Boundary

Visible Resolved Contract HIR must contain enough Input compiler-semantic material that Input Definition Establishment
never needs to reopen Java, Kotlin, `.kontrakt`, runtime classes, platform documentation, or backend representation to
answer an Input semantic question.

HIR may erase host and compiler implementation detail that no remaining legal consumer requires. It may not erase a
distinction while that distinction remains required by Input Definition meaning, Establishment, generated API
preservation, legal user-realization verification, diagnostics or provenance relation, or another declared downstream
preservation proof.

A missing Input semantic distinction or required resolved platform obligation is a frontend/HIR defect. Later
Establishment, verification, optimization, or backend work does not repair it by reinterpretation.

HIR physical schema remains compiler realization. Semantic separation does not require one object, allocation, table,
row, pointer, or compiler product per distinction.

### 5.4. Definition Establishment

An Input definition receives authority only after one complete Resolved Input Definition Candidate has been judged under
the Input Contract law.

```text
Resolved Input Definition Candidate
    ↓
Input-owned Definition judgment
    ↓
Established Input Definition
```

The Resolved IDL Input Binding Candidate remains a separate candidate relation. Successful Input Definition
Establishment does not merge machine-position binding into Definition Meaning. The authoritative Input Binding and the
Established Input Definition meet only when one exact machine position applies that exact Versioned Definition.

The established meaning must contain every distinction required by the Input law and no distinction derived only from
the current compiler representation.

A source or candidate that is incomplete, ambiguous, unsupported, open, unresolved, or otherwise incompatible with the
Input law establishes no Input definition authority.

The compiler must preserve one authoritative semantic interpretation of the Established Input Definition. It must not
recover Input meaning later from host-object identity, source declaration order, compiler topology, planning state,
storage position, generated code shape, cache state, or backend-local representation.

Acquisition, intermediate representation, planning, storage, publication, and code-generation structures are replaceable
realization. No particular compiler path, graph, frozen image, provider, handle, table, or generated artifact is part of
Input identity or Input authority.

Compiler machinery may still reject source behavior or structure that cannot be erased without changing Input meaning.
Constructor execution, polymorphic expansion, recursive member traversal, collection expansion, cycle handling, and
other source or compiler mechanisms do not become Input semantics merely because one realization can process them.

Contract Version remains explicit through Establishment. Equal local Input Definition Meaning under different Version
Identities may share compiler representation or analysis when proven safe, but Establishment does not merge those
Versioned Definitions or rewrite their history.

---

## 6. Invocation Boundary

For one applicable Operation interaction, Input judges the actual material supplied at that Operation's Input position
under the exact authoritative Input Binding and its exact Established Versioned Input Definition.

```text
authoritative operation-scoped Input Binding
    → exact Established Versioned Input Definition
+
actual material supplied at the Input position
    ↓
one complete coherent presentation for judgment
    ↓
Input judgment
    ↓
declared presentation shape satisfied
    -> Input success
    -> Admission may judge the presentation

declared presentation shape not satisfied
    -> Input refusal
    -> Admission is not reached
```

Input judges only whether the actual supplied material realizes the declared Input presentation shape. It does not judge
whether the presented values may continue. That authority belongs to Admission.

How the user creates or forms the supplied presentation is realization outside Input authority. Constructor, factory,
builder, adapter, serializer, generated factory, copying, ownership transfer, snapshotting, pinning, persistent
representation, copy-on-write, verified zero-copy, or another formation pattern does not establish Input meaning by
itself.

The actual carrier is realization material subjected to the Input judgment. Its class identity, object identity,
allocation identity, construction path, getter behavior, declaration position, or storage layout does not become Input
authority.

One Input judgment must observe one complete coherent presentation under the applicable Input law. Physical mutability
or aliasing does not by itself grant or deny Input authority. If mutable or aliased storage participates, the
realization must prevent torn observation and must ensure that later external mutation cannot retroactively change the
Input meaning established from that judgment.

A lazy value, proxy, framework-bound object, lifecycle-dependent carrier, live view, or runtime object graph is not
accepted merely because it can eventually produce compatible data. It may participate only when the declared Input
presentation can be formed and judged without hidden behavior, unresolved authority, runtime graph discovery, or
unstable observation.

Definition-time verification and Establishment determine that the selected declaration provides a legal Input
definition. They do not authorize arbitrary runtime material merely because that material has a compatible host type.
The actual material participating in the Interaction must satisfy the applicable Input law.

After successful Input judgment, later inbound processing may change physical representation only through the declared
Contract relations that own those changes. The material eventually supplied to the user Operation must preserve the
meaning lawfully derived through the complete declared inbound pipeline. Undeclared substitution, mutation of
established meaning, reinterpretation, or introduction of different meaning is not a valid realization.

The compiler and runtime must prove or check the required integrity at the boundaries where the applicable law cannot be
guaranteed otherwise. This obligation does not require one generated reader, static gate, wrapper, token, copying
strategy, snapshot algorithm, or mandatory recheck strategy. Equivalent realization may use static proof, generated
checks, controlled material, specialized invocation, or another mechanism that preserves the same Contract result.

An independently applicable Policy, Governance, Budget, Capacity, or other Contract retains ownership of its own
judgment and result. Its stop does not become Input refusal.

Occurrence-time Input judgment uses the already-authoritative Versioned Input Definition selected by the exact
authoritative Input Binding. It must not reinterpret authored source, silently reselect another Version, or reconstruct
Contract meaning from compiler acquisition, planning, graph, cache, runtime class, or backend representation.

---

## 7. V1 Ratifiability Law

The V1 admissibility law is:

```text
Input may accept external presentation evidence only when the frontend can resolve it into one explicit,
finite, inspectable, loss-accounted presentation over directly named coordinates without allowing external
implementation mechanics, nested Contract authority, pointer topology, hidden movement, unstable observation,
or another Contract role to survive refinement.
```

The authoring law is:

```text
Do not make the user declare the same flat presentation twice.
Treat the selected host declaration as external contract evidence.
Refine its direct coordinates through one deterministic Kontrakt law or reject it.
```

This law is how Input carries the discipline of *What Contract Is*. Kontrakt must not force users to restate a flat
external presentation they already selected through a host declaration. For each supported direct coordinate sort, the
selected frontend must provide one deterministic refinement law, remove external implementation mechanics from the
result, and reject the source or require an adapter when no safe refinement exists.

Three forms of hidden meaning are rejected.

**Hidden authority** exists when Kontrakt lets external implementation structure decide Kontrakt Contract meaning. A
host source may contain inheritance, override dispatch, constructor execution, getter algorithms, framework annotations,
serializer conventions, collection implementations, `equals`, `hashCode`, or proxy behavior, but those mechanics cannot
survive refinement as Input authority.

**Hidden choice** exists when the same selected declaration admits more than one Kontrakt interpretation and the machine
silently chooses one. A nullable source form, enum-like source form, approved scalar, bounded opaque value, or supported
collection carrier may be accepted only when the frontend can refine it completely into one already-defined Input
presentation meaning. A user-owned nested type, unsupported collection carrier, sealed hierarchy, generic object
structure, or interface-based part is not guessed into a flat Input law. It is rejected or formed into a supported
presentation before Input.

**Hidden movement** exists when observing the alleged Input performs behavior or depends on undeclared timing or
lifecycle. Callbacks, lazy loading, live streams, futures, suppliers, services, capabilities, and resource handles are
not Input material. A source profile must exclude, project, or reject them; it must not silently ratify them as boundary
data.

Slot nomination does not guarantee Establishment. Exact slot selection, frontend resolution, Input Definition
Establishment, and invocation-time Input judgment remain distinct boundaries. A source that cannot be resolved
completely does not produce a valid visible candidate. A resolved candidate that fails the Input-owned Definition law
receives no Input definition authority. An Established Input Definition may still refuse one invocation when the actual
material does not satisfy the declared presentation. Admission begins only after Input judgment succeeds.

An Input source candidate is ratifiable only when it satisfies the conditions below.

### 7.1. Coordinate Closure

The applicable IDL Input Binding selects one exact Versioned Input Definition source under ADR-0047 and ADR-0053.

Input does not infer, broaden, replace, or silently re-version that selection.

Every contract-visible Input coordinate must resolve to one complete and closed presentation meaning.

No coordinate may remain semantically open, erased, dynamically unresolved, or dependent on runtime type discovery.

Frontend-specific source forms are admissible only when the frontend can refine them completely into that closed
meaning. Otherwise a complete Resolved Input Definition Candidate is not formed. Java raw or wildcarded types and Kotlin
star-projected forms are examples of source forms that require such complete refinement.

### 7.2. Direct Coordinate Meaning

Every direct Input coordinate must resolve to one complete and closed presentation meaning.

Input authority depends on that meaning, not on the host type used to present it.

A frontend may admit a source form only when it preserves every distinction required by the declared Input meaning.
Primitive types, strings, enum forms, nullable forms, and other host-language categories are frontend evidence, not
Input Contract vocabulary by themselves.

### 7.3. Presentation Stability and Coherent Observation

Input owns the stability of the presentation meaning judged at its boundary. It does not require one physical
immutability mechanism.

Material participating in one Input judgment must provide one complete coherent presentation. One judgment must not
observe a mixture of externally changing states that never existed as one complete presentation under the applicable
Input law.

Later external mutation, aliasing, live backing state, or lifecycle change must not retroactively change the Input
meaning established from that judgment.

The realization may satisfy this obligation through already-immutable material, ownership transfer, copying,
snapshotting, pinning, persistent representation, copy-on-write, verified zero-copy, or another equivalent strategy.
Input does not make any one strategy authoritative.

Input does not normalize, canonicalize, repair, or otherwise change presentation meaning while forming a judgeable
boundary value. A formation mechanism may only preserve the declared Input meaning. Any semantic transformation remains
owned by its declared Contract authority.

The stability obligation covers the complete declared Input presentation, including every constituent value of an
admitted collection or bounded presentation.

Host-language immutability mechanisms are evidence used by a frontend or realization. They do not define the Contract
obligation.

### 7.4. Carrier and Contract Separation

A host carrier does not own Input meaning or Input authority.

Object identity, class identity, allocation identity, construction path, declaration position, storage layout, generated
shape, or host behavior does not become Input identity or authority.

Constructors, factories, builders, serializers, adapters, copying, snapshotting, pinning, ownership transfer, and
similar mechanisms may form a judgeable value at or before the Input boundary when they preserve the declared meaning.
They remain host or realization mechanics and do not establish Input meaning.

Accessors or generated bridges may expose already-declared source evidence to a frontend or realization. Their existence
does not create Input coordinates. Custom getters, delegation, `equals`, `hashCode`, `toString`, arbitrary helper
methods, or other executable host behavior do not define Input meaning.

A frontend may interpret supported source evidence as an expression of already-defined Input meaning. The frontend does
not create that meaning or authority.

A host declaration may serve as both source evidence and a direct carrier only when its complete declared presentation
satisfies the applicable V1 Input law and one coherent presentation can be guaranteed for judgment. That coincidence is
a frontend or realization convenience, not an Input Contract requirement.

### 7.5. Inheritance and Polymorphism Boundary

Input meaning must be complete in the selected Input definition.

An Input definition does not acquire coordinates or other meaning through Contract inheritance, host-language
inheritance, runtime implementation discovery, subtype discovery, or executable dispatch.

Runtime realization may not add, replace, or reinterpret Input meaning. The actual material is judged against the
already-authoritative Input definition; its runtime implementation type does not enlarge that definition.

A frontend may admit a host source only when the complete Input presentation can be determined without inherited
semantic authority, runtime subtype discovery, override behavior, default behavior, or another implementation-dependent
mechanism.

Frontend constructs such as interfaces, inherited members, overrides, and default methods remain subject to this law.
Their host-language existence does not provide Input authority. A future frontend may statically resolve and erase such
host structure only when doing so preserves one complete explicit Input meaning; V1 support policy may remain narrower
than that semantic possibility.

### 7.6. Presence, Absence, and Finite Choice

Input may declare presence, absence, and finite alternatives as explicit presentation distinctions.

Input must preserve every distinction required by the declared presentation meaning and must not silently collapse
distinct alternatives.

Presence or absence is Contract meaning only when the Input law declares that distinction. A host-language `null`,
nullable type, optional wrapper, sentinel value, or another source representation does not acquire Input meaning by
itself. A frontend may map such a source form to declared Input meaning only when that mapping is complete and
unambiguous.

Input does not apply defaults. A constructor default, language default, factory default, serializer default, or another
creation-time default remains pre-boundary formation and does not become Input authority. Input judges the actual
presentation supplied at its boundary.

If omission is legal, absence must be an explicit declared alternative. If a concrete coordinate is required, the
supplied presentation must already contain it.

A finite choice must be closed. Every declared alternative must be completely and unambiguously distinguishable under
the Input law. The actual runtime material may not enlarge, replace, or reinterpret the declared alternative set through
subtype discovery or implementation-specific behavior.

How a frontend or backend represents those alternatives is realization.

### 7.7. Opaque Values and External Denotation

Host object-reference identity does not provide Input meaning or semantic identity.

An Input coordinate may carry a declared stable value presentation that denotes something outside the Input
presentation. Input owns only the declared presentation of that value. The existence, identity, validity, ownership, or
behavior of the denoted target is not established by Input.

A reference-valued Input coordinate does not by itself create a Definition Reference, Occurrence Reference, object
relation, lookup obligation, or dereference authority under ADR-0063.

An opaque value is admissible only when its complete Input-visible presentation distinctions are explicitly defined by
an applicable semantic profile. Opaque treatment does not permit Input to adopt an arbitrary user object while ignoring
its behavior, instability, lifecycle, inheritance, graph structure, or runtime identity.

A frontend may map a supported host type to an applicable presentation profile. The host type does not define the
profile, and its methods, equality, hashing, parsing, normalization, or serialization behavior do not become Input law.

Input does not establish canonical identity or normalized meaning for an opaque value. Those meanings remain with the
Contract that owns them.

### 7.8. Presentation-Only Authority

Input owns only the distinctions required to determine whether the actual material supplied at the Input boundary
realizes the declared presentation shape.

Those distinctions may include declared coordinates, their complete closed presentation meanings, explicit presence or
absence, closed finite alternatives, presentation bounds, aggregate distinctions, and Input-visible distinctions defined
by applicable semantic profiles.

A presentation bound constrains the form in which Input material may be presented. It does not decide whether an
otherwise correctly presented value may continue. Structural extent, cardinality, or another bound may therefore belong
to Input when that bound is part of the declared presentation form; value-domain, business, Policy, or
operation-specific admissibility conditions do not.

A value may denote something outside the Input presentation. Input owns only the declared presentation of that value and
does not acquire authority over the denoted target.

Input does not decide whether a correctly presented value may continue. That judgment belongs to Admission.

Input does not establish canonical identity, Fact or core meaning, State or Transition legality, Policy or Governance
selection, Publication authority, Output meaning, or machine movement. Those meanings remain with their owning Contract
authorities or, for machine movement, the State-Machine axis.

### 7.9. Supported Presentation Profiles

An Input coordinate is admissible only when its complete presentation meaning is closed, deterministically
interpretable, and capable of participating in one stable coherent Input judgment under an applicable Input presentation
profile.

A presentation profile may define a closed scalar meaning, a closed finite alternative, bounded text, bounded bytes, or
another explicitly defined bounded presentation. Flat Contract-authority topology does not make Input scalar-only. A
supported collection presentation may remain one direct coordinate when its complete presentation law is explicit and
closed; host collection shape alone does not supply that law.

Potential V1 profile examples include pinned UUID, decimal, date, instant, bounded text, bounded bytes, token,
identifier, path, or URI presentations. These names are examples only. Each requires an explicit applicable presentation
profile; a familiar host type name does not grant Input authority.

Presentation bounds belong to Input when they define the declared form of the material. Conditions that decide whether
correctly presented values may continue belong to Admission or another owning authority.

A frontend may admit a host-language form only when it can map that form completely to the applicable Input presentation
meaning. Primitive types, strings, enums, arrays, collection carriers, records, value classes, or other host-language
forms do not define Input meaning by themselves.

Host methods, object identity, equality, hashing, parsing, normalization, locale, timezone, serialization behavior, or
another implementation convention does not become Input law merely because the host type provides it. A host-level
distinction becomes relevant only when the owning Input presentation law independently declares that distinction as part
of the Contract meaning.

### 7.10. V1 User-Facing Authoring Classes

The V1 user-facing policy classifies host forms only to determine whether they can express an already-declared Input
presentation. The host category does not create Contract meaning.

| Authoring class                                 | V1 treatment                                                                                        | Examples                                                                                                                                                                                                                                                                                     | V1 frontend consequence                                                                                                                                                                              |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct closed declarative presentation          | Refine directly when every declared distinction is complete, explicit, behavior-free, and supported | Primitive and `String` source forms, closed enum source forms, approved scalar leaves, qualifying bounded presentation profiles, flat Kotlin data class, flat final Kotlin carrier, flat Java record                                                                                         | The selected declaration may supply the external Contract evidence without a duplicate presentation declaration; runtime material remains separately subject to the Input stability and judgment law |
| Outside or unsupported structured presentation  | Require explicit formation into a supported presentation before authoritative Input judgment        | Nested DTO, embedded Value Object, inherited product whose meaning cannot be statically closed, unsupported sealed hierarchy, recursively nested collection/object structure, recursive tree, graph, dynamic JSON object, host collection carrier without an applicable closed Input profile | Outside structure is not recursively adopted as Input meaning; a supported declared presentation must exist before Input judgment                                                                    |
| Implementation-shaped carrier                   | Require explicit formation or reject the source                                                     | Mutable JavaBean with behavioral accessors, framework DTO, proxy, entity, custom getter, delegated property, runtime-discovered implementation, third-party lifecycle object                                                                                                                 | Host conventions, unstable observation, lifecycle, and implementation relationships do not become Input meaning                                                                                      |
| Behavior, capability, movement, or role leakage | Reject from Input                                                                                   | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle                                                                                                                                  | The material belongs to another role or is not Input presentation data                                                                                                                               |

An array, `List`, `Set`, `Map`, or another collection-shaped host form is not accepted or rejected merely by its host
category or physical mutability. Direct admissibility depends on an explicit closed Input presentation profile, exact
platform obligations when relevant, and the boundary's ability to obtain one complete coherent presentation without
hidden host semantics. The exact V1 collection profiles remain open for separate review.

### 7.11. Zero-Adapter Direct Carrier

A selected host declaration may serve as both source evidence and runtime carrier without a second generated
presentation when it is already a legal V1 source presentation and the runtime boundary can guarantee one complete
coherent presentation under the established Input law.

For the JVM V1 zero-adapter source profile, Kontrakt uses the selected host declaration itself as source evidence rather
than requiring a second generated presentation declaration. A Kotlin data class, a final Kotlin class whose selected
coordinates are primary-constructor properties, or a Java 17+ record may satisfy the direct-source conditions only when
every selected coordinate resolves completely to supported Input presentation meaning. `data class`, `val`, `final`, and
`record` are source-shape evidence; none grants authority or occurrence stability by itself.

The host shape remains evidence, not authority. Kontrakt does not admit the class or constructor as the Contract. It
retains the resolved Input-local coordinate identities, declared presentation meanings, presence, applicable
presentation profiles, bounds, and any other Input-owned distinctions. Constructor behavior, source declaration order,
generated host machinery, and compiler traversal order are erased from Input authority unless an independently declared
Input presentation law makes a particular ordering distinction semantic.

A qualifying Kotlin primary-constructor order or Java record-component order may be retained as provenance or
generated-surface information when a legal consumer needs it. It is not Input semantic order by default.

A user-defined interface root, interface-dispatch surface, inherited carrier shape, nested runtime traversal, or runtime
implementation search does not qualify for this direct path merely because the compiler can inspect it. A frontend may
support statically resolved and fully erased host structure only when the resulting Input meaning is complete,
deterministic, and independent of runtime discovery.

Equivalent local Input presentation meaning may later come from another language, schema system, serialization system,
or frontend. That semantic equality does not merge distinct Contract Authorities or distinct Version Identities.

Outside nested, inherited, framework-bound, proxied, third-party lifecycle, recursive, dynamic, or unsupported
collection-shaped material must be formed into a supported presentation before authoritative Input judgment. Input does
not adopt a live object graph or hidden lifecycle as Contract meaning. The realization may choose a lawful formation
strategy when it preserves the exact declared Input meaning and coherent-observation law.

### 7.12. Behavior and Capability Are Not Opaque Leaves

Callbacks, services, repositories, live resources, async control surfaces, and other executable capabilities are not
rescued by labeling them opaque.

Input may carry a declared identifier, token, coordinate, source text, bounded bytes, or another approved value that
refers to something outside the machine. The execution capability or resource ownership represented by that value
remains outside Input authority.

---

## 8. Authoring Boundary

The user should not declare the same flat presentation twice.

The selected host-facing Input declaration may serve as the external evidence when the frontend can resolve it
completely into one Resolved Input Definition Candidate.

This is a source convenience, not authority.

Other source shapes remain legal outside Kontrakt. They must be formed before authoritative Input judgment into the same
declared Input presentation meaning while satisfying the Input stability and coherent-observation law.

The Input authoring surface must not force a rich object model into the core.

---

## 9. Refusal Boundary

Input has two Contract-owned refusal times after successful frontend resolution, while source-resolution and
HIR-formation failures remain compiler/frontend failures rather than Input occurrence refusal.

Definition-time Input refusal occurs when one complete Resolved Input Definition Candidate exists but does not satisfy
the Input-owned Definition law.

In that case no authoritative Input definition is established and no invocation may proceed under that candidate as an
Input Definition.

Invocation-time Input refusal occurs when an Established Input Definition and exact authoritative Input Binding exist
but the supplied presentation cannot satisfy the declared boundary. Required material may be unavailable, structurally
incompatible, malformed under the declared representation, incoherent for one judgment, missing a required distinction,
or otherwise unable to realize the established presentation law.

Invocation-time Input refusal happens before Admission.

A lazy, proxied, lifecycle-dependent, behavior-producing, runtime-discovered, incoherently observed, or otherwise
unsupported carrier is not repaired into a different semantic meaning at this point. Physical mutability alone is not
the refusal criterion; failure to obtain and preserve one lawful coherent presentation is.

A Policy, Governance, Budget, Capacity, or other cross-cutting stop remains owned by the supplying Contract.

The exact relation between unsuccessful Input judgment material and ADR-0057 Failure remains owned by the later Failure
integration work. This section does not create a second failure taxonomy.

---

## 10. Relationship to Later Contracts

Input establishes only judgeable boundary presentation.

Admission judges continuation over that presentation.

Selected Canonicalization may establish a stable same-shape representative without changing the Input coordinate
surface.

Lowering later binds Input coordinates to candidate Operation-parameter Fact coordinates.

Input does not perform those later jobs.

An Input refusal therefore does not become Admission rejection, Canonicalization refusal, or Lowering refusal.

An exact Input Authority and Version remain attributable wherever later authoritative interpretation depends on which
Versioned Input Definition supplied the presentation. Later Policy, Governance, compiler generation, or backend state
does not retroactively rewrite that established Version identity.

---

## 11. Open in This Section

The final token spelling and exact public Java or Kotlin carrier syntax for Input remain open.

The exact V1 collection presentation families, their constituent laws, and the host forms that may directly realize them
remain open for separate review. Collection support must not infer ordering, equality, multiplicity, key semantics,
mutability, or another Contract distinction merely from a host collection type.

Any future expansion beyond the V1 flat Contract-authority profile requires explicit review. It must not reintroduce
nested Contract authority, runtime graph discovery, hidden observation timing, unstable presentation meaning, or
backend-owned meaning.

The exact Input-owned Required Basis and Applicability law, if any, remains to be reviewed against ADR-0063 rather than
inferred from IDL Binding, Version, platform resolution, or compiler reachability.

The exact occurrence-owned Established Material, unsuccessful-judgment handoff, downstream preservation lifetime, and
discharge boundary remain to be closed in later Input review. This ADR does not infer those meanings merely from runtime
execution.

---

## 12. Consequences

Input becomes an explicit, stable, coherent boundary presentation rather than a host object treated as Contract
authority.

The machine can accept ordinary external evidence without making Java, Kotlin, reflection, object identity, framework
lifecycle, source declaration order, or physical carrier mutability part of Contract meaning by themselves.

The flat V1 Contract-authority profile reduces hidden topology and makes later Admission, Canonicalization, Lowering,
diagnostics, verification, and optimization operate on a bounded declared surface.

Input Definition Meaning, exact Versioned Definition identity, and exact IDL machine-position Binding remain distinct.
Equal local meaning may be reused physically without merging Contract Version sovereignty or authoritative Binding
identity.

Some convenient application objects cannot cross the boundary directly. They require explicit presentation formation
before authoritative Input judgment.

That cost is intentional. The boundary rejects hidden lifecycle and representation authority instead of pretending it is
safe, while leaving the physical formation and preservation mechanism replaceable.

---

## 13. Migration History

This ADR was extracted mechanically from the Input-owned material of ADR-0048.

The extraction itself does not change the accepted Input Contract semantics.

A 2026-09-02 review clarified the Contract/frontend boundary for coordinate closure, direct presentation meaning,
immutability, carrier separation, inheritance and polymorphism, presence and finite choice, opaque values, presentation
bounds, and supported presentation profiles. The review also removed the scalar-only implication from flatness without
yet fixing the exact V1 collection taxonomy.

A 2026-09-17 review aligned Input with ADR-0053, ADR-0063, ADR-0071, and ADR-0073. It separated Input Definition meaning
from IDL Input Binding, made Authority-scoped Version identity explicit without equating it with local semantic-content
equality, defined Input Definition determinants and non-determinants, introduced Resolved Input Definition and Binding
Candidate obligations before Establishment, fixed the HIR information-loss boundary, removed source declaration order as
default Input semantics, and replaced physical-immutability requirements with presentation stability and
coherent-observation law while preserving realization freedom.

ADR-0048 remains the owner of the shared inbound-airlock composition and core-entry relation.