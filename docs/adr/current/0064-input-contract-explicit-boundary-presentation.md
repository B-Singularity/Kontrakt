# ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0067: Lowering Contract
- ADR-0066: Canonicalization Contract
- ADR-0065: Admission Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
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

The declaration nominates external evidence. Kontrakt must refine and ratify that evidence before it becomes Input
authority.

---

## 2. Problem

The boundary must accept ordinary external presentations without letting host representation become Contract authority.

The machine must not infer Input meaning from object identity, constructor behavior, getters, runtime interfaces,
framework lifecycle, reflection, mutable storage, lazy evaluation, proxy activation, or snapshot timing.

The machine also must not require the user to restate the same presentation in a second Kontrakt-specific DTO when a
supported host declaration already states the needed external shape exactly enough to refine.

The V1 boundary therefore needs a narrow ratifiable presentation law.

Input must remain finite, explicit, immutable, and directly inspectable before Admission.

Outside structured or behavioral material may remain outside Kontrakt, but it must be converted through an explicit
replaceable presentation-formation boundary before invocation.

---

## 3. Decision Drivers

Input is presentation, not truth.

Outside source evidence may nominate a Contract, but only Kontrakt-owned ratified material receives authority.

The runtime boundary must receive already-formed immutable presentation. It must not choose a capture moment or repair a
live object.

V1 must keep Input flat so a nested carrier, reference graph, inherited relation, or runtime implementation topology
does not become hidden Contract structure. Flatness does not require every coordinate to be scalar. A bounded collection
presentation may remain one direct coordinate when its complete presentation law is closed without recursive discovery
or adoption of an outside object graph.

The same selected source meaning must have one authoritative Input interpretation.

Compiler acquisition, planning, storage, or backend structure must not create a second semantic authority path.

A source that cannot be deterministically refined must fail at definition time.

A ratified Input whose required boundary material is unavailable or malformed at invocation time must stop before
Admission.

Cross-cutting Policy, Governance, Budget, or Capacity outcomes remain owned by those Contracts.

---

## 4. Decision

### 4.1. Input Meaning

Input is the boundary presentation Contract.

```text
outside presentation evidence
-> ratified Input Contract
-> already-formed immutable Input presentation
-> Input boundary judgment
-> Admission
```

Input declares the finite presentation surface that may appear.

It preserves the distinctions that later Contracts are allowed to observe.

Input does not declare that the values may continue. It does not canonicalize them. It does not lower them into core
Fact meaning. It does not invoke the user Operation.

### 4.2. Flat V1 Presentation

V1 keeps the selected Input presentation flat.

The presentation exposes one finite set of directly named coordinates.

A user-owned nested carrier, embedded Value Object, inherited part, interface-typed part, sealed runtime hierarchy,
recursive type, recursively discovered collection object graph, or shared-reference graph does not become a second
Contract inside Input.

A collection presentation is not excluded merely because a host language realizes it with an array or collection
carrier. It may participate directly only when the applicable Input law defines its complete bounded presentation
without recursive object discovery, hidden equality or ordering semantics, mutable backing authority, or another
undeclared host convention.

Outside material whose complete presentation cannot be established under that law must be formed into a supported
declared presentation before it reaches the runtime Input boundary.

The flatness rule prevents host topology from becoming hidden Contract ancestry.

### 4.3. Source Evidence Is Not Authority

A supported Java or Kotlin declaration may nominate Input evidence.

The declaration remains source evidence. A runtime object remains a carrier.

Input identity and meaning are not derived from the class name alone, runtime type, object identity, constructor
execution, property accessor behavior, or storage layout.

Equivalent flat presentation material may later come from another frontend, schema compiler, serialization system, or
language without changing the Input Contract.

---

## 5. Definition Establishment

An Input definition receives authority only after the exact Input source selected for the Operation has been interpreted
completely and deterministically under the Input Contract.

```text
selected Operation Input binding
+
exact selected Input declaration
    ↓
complete deterministic interpretation under the Input law
    ↓
complete legal Input definition meaning
    ↓
authoritative Input definition
```

The established meaning must contain every distinction required by the Input law and no distinction derived only from
the current compiler representation.

A selected source that is incomplete, ambiguous, unsupported, open, or otherwise incompatible with the Input law
establishes no Input definition authority.

The compiler must preserve one authoritative semantic interpretation of the selected source. It must not recover Input
meaning later from host-object identity, compiler topology, planning state, storage position, generated code shape, or
backend-local representation.

Acquisition, intermediate representation, planning, storage, publication, and code-generation structures are replaceable
realization. No particular compiler path, graph, frozen image, provider, handle, table, or generated artifact is part of
Input identity or Input authority.

Compiler machinery may still reject source behavior or structure that cannot be erased without changing Input meaning.
Constructor execution, polymorphic expansion, recursive member traversal, collection expansion, cycle handling, and
other source or compiler mechanisms do not become Input semantics merely because one realization can process them.

---

## 6. Invocation Boundary

For one applicable Operation interaction, Input judges the actual material supplied at that Operation's Input position
under the applicable Input definition.

```text
selected Operation interaction
+
actual material supplied at the Input position
+
applicable Input definition
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

How the user creates the supplied instance is realization outside Input authority. Constructor, factory, builder,
adapter, serializer, generated factory, or another creation pattern does not establish Input meaning by itself.

The actual instance is realization material subjected to the Input judgment. Its class identity, object identity,
allocation identity, construction path, getter behavior, or storage layout does not become Input authority.

The runtime Input boundary accepts only a flat immutable presentation that satisfies the applicable Input law. A mutable
object, lazy value, proxy, framework-bound object, live collection view, nested carrier graph, or lifecycle-dependent
carrier is not repaired, snapshotted, or reinterpreted by Input.

Definition-time verification establishes that the selected declaration can provide a legal Input definition. It does not
authorize arbitrary material merely because that material has a compatible host type. The actual material participating
in the Interaction must satisfy the applicable Input law.

After successful Input judgment, later inbound processing may change physical representation only through the declared
Contract relations that own those changes. The material eventually supplied to the user Operation must preserve the
meaning lawfully derived through the complete declared inbound pipeline. Undeclared substitution, mutation,
reinterpretation, or introduction of different meaning is not a valid realization.

The compiler and runtime must prove or check the required integrity at the boundaries where the applicable law cannot be
guaranteed otherwise. This obligation does not require one generated reader, static gate, wrapper, token, or mandatory
recheck strategy. Equivalent realization may use static proof, generated checks, controlled immutable material,
specialized invocation, or another mechanism that preserves the same Contract result.

An independently applicable Policy, Governance, Budget, Capacity, or other Contract retains ownership of its own
judgment and result. Its stop does not become Input refusal.

Occurrence-time Input judgment uses the already-authoritative Input definition. It must not reinterpret authored source
or reconstruct Contract meaning from compiler acquisition, planning, graph, cache, or backend representation.

---

## 7. V1 Ratifiability Law

The V1 admissibility law is:

```text
Input may accept an external presentation contract only when it can be deterministically refined into one explicit,
finite, inspectable, loss-accounted, flat set of directly named immutable coordinates without allowing external
implementation mechanics, nested contract composition, pointer topology, hidden movement, or another contract role to
survive refinement.
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

**Hidden movement** exists when observing the alleged Input performs behavior or depends on time. Callbacks, lazy
loading, live streams, futures, suppliers, services, capabilities, and resource handles are not Input material. A source
profile must exclude, project, or reject them; it must not silently ratify them as boundary data.

Slot nomination does not guarantee ratification. Input owns two distinct refusal boundaries. A source that cannot be
deterministically refined under the Input law is rejected at definition time and receives no Input definition authority.
A ratified Input Contract may still refuse an invocation when the actual material does not satisfy the declared
presentation. Admission begins only after Input judgment succeeds.

An Input source candidate is ratifiable only when it satisfies the conditions below.

### 7.1. Coordinate Closure

The applicable Input binding supplies one exact selected source under ADR-0047.

Input does not infer, broaden, or replace that selection.

Every contract-visible Input coordinate must resolve to one complete and closed presentation meaning.

No coordinate may remain semantically open, erased, dynamically unresolved, or dependent on runtime type discovery.

Frontend-specific source forms are admissible only when the frontend can refine them completely into that closed
meaning. Otherwise the Input definition is not established. Java raw or wildcarded types and Kotlin star-projected forms
are examples of source forms that require such complete refinement.

### 7.2. Direct Coordinate Meaning

Every direct Input coordinate must resolve to one complete and closed presentation meaning.

Input authority depends on that meaning, not on the host type used to present it.

A frontend may admit a source form only when it preserves every distinction required by the declared Input meaning.
Primitive types, strings, enum forms, nullable forms, and other host-language categories are frontend evidence, not
Input Contract vocabulary by themselves.

### 7.3. Explicit Immutability

An Operation's Input is immutable.

Material participating in an Input occurrence must already provide the complete declared presentation without
contract-visible mutation when it reaches the Input boundary.

Input judges that presentation. It does not create an admissible Input by copying, snapshotting, freezing, repairing,
normalizing, or otherwise transforming mutable incoming material.

Construction, adaptation, copying, or other formation may occur before the Input boundary. Such work remains outside
Input authority.

The immutability obligation covers the complete declared Input presentation, including every constituent value of an
admitted collection or bounded presentation.

The presentation must not depend on mutable backing state, externally mutable aliases, live views, lazy materialization,
proxy activation, or framework lifecycle.

Host-language immutability mechanisms are evidence used by a frontend or realization. They do not define the Contract
obligation.

### 7.4. Carrier and Contract Separation

A host carrier does not own Input meaning or Input authority.

Object identity, class identity, allocation identity, construction path, storage layout, generated shape, or host
behavior does not become Input identity or authority.

Constructors, factories, builders, serializers, adapters, and similar mechanisms may form the value before the Input
boundary. They remain host mechanics and do not establish Input meaning.

Accessors or generated bridges may expose already-declared source evidence to a frontend or realization. Their existence
does not create Input coordinates. Custom getters, delegation, `equals`, `hashCode`, `toString`, arbitrary helper
methods, or other executable host behavior do not define Input meaning.

A frontend may interpret supported source evidence as an expression of already-defined Input meaning. The frontend does
not create that meaning or authority.

A host declaration may serve as both source evidence and a direct carrier only when its complete declared presentation
satisfies the applicable V1 Input law. That coincidence is a frontend convenience, not an Input Contract requirement.

### 7.5. Inheritance and Polymorphism Boundary

Input meaning must be complete in the selected Input definition.

An Input definition does not acquire coordinates or other meaning through Contract inheritance, host-language
inheritance, runtime implementation discovery, subtype discovery, or executable dispatch.

Runtime realization may not add, replace, or reinterpret Input meaning. The actual material is judged against the
already-authoritative Input definition; its runtime implementation type does not enlarge that definition.

A frontend may admit a host source only when the complete Input presentation can be determined without inherited
semantic material, runtime subtype discovery, override behavior, default behavior, or another implementation-dependent
mechanism.

Frontend constructs such as interfaces, inherited members, overrides, and default methods remain subject to this law.
Their host-language existence does not provide Input authority.

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

An Input coordinate may carry a declared immutable value that denotes something outside the Input presentation. Input
owns only the declared presentation of that value. The existence, identity, validity, ownership, or behavior of the
denoted target is not established by Input.

A reference-valued Input coordinate does not by itself create a Definition Reference, Occurrence Reference, object
relation, lookup obligation, or dereference authority under ADR-0063.

An opaque value is admissible only when its complete Input-visible presentation distinctions are explicitly defined by
an applicable semantic profile. Opaque treatment does not permit Input to adopt an arbitrary user object while ignoring
its behavior, mutability, lifecycle, inheritance, graph structure, or runtime identity.

A frontend may map a supported host type to an applicable presentation profile. The host type does not define the
profile, and its methods, equality, hashing, parsing, normalization, or serialization behavior do not become Input law.

Input does not establish canonical identity or normalized meaning for an opaque value. Those meanings remain with the
Contract that owns them.

### 7.8. Presentation-Only Authority

Input owns only the distinctions required to determine whether the actual material supplied at the Input boundary
realizes the declared presentation shape.

Those distinctions may include declared coordinates, their complete closed presentation meanings, explicit presence or
absence, closed finite alternatives, presentation bounds, and Input-visible distinctions defined by applicable semantic
profiles.

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

An Input coordinate is admissible only when its complete presentation meaning is closed, immutable, and
deterministically interpretable under an applicable Input presentation profile.

A presentation profile may define a closed scalar meaning, a closed finite alternative, bounded text, bounded bytes, or
another explicitly defined bounded presentation. Flatness does not make Input scalar-only. A supported collection
presentation may remain one direct coordinate when its complete presentation law is explicit and closed; host collection
shape alone does not supply that law.

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

| Authoring class                                 | V1 treatment                                                                                     | Examples                                                                                                                                                                                                                                           | V1 frontend consequence                                                                                                                                                           |
|-------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct closed immutable presentation            | Refine and ratify directly when every declared distinction is complete, immutable, and supported | Primitive and `String` source forms, closed enum source forms, approved immutable scalar leaves, qualifying bounded presentation profiles, flat Kotlin data class, flat final Kotlin carrier, flat Java record                                     | The selected declaration may supply the external Contract evidence without a duplicate presentation declaration; the actual invocation material remains subject to Input judgment |
| Outside or unsupported structured presentation  | Require explicit formation into a supported presentation before Input                            | Nested DTO, embedded Value Object, inherited product, unsupported sealed hierarchy, recursively nested collection/object structure, recursive tree, graph, dynamic JSON object, host collection carrier without an applicable closed Input profile | Outside structure is not recursively adopted as Input meaning; a supported declared presentation must exist before the Input boundary                                             |
| Implementation-shaped carrier                   | Require explicit formation or reject the source                                                  | Mutable JavaBean, framework DTO, proxy, entity, custom getter, delegated property, interface root, runtime-discovered implementation, third-party lifecycle object                                                                                 | Host conventions, mutable observation, lifecycle, and implementation relationships do not become Input meaning                                                                    |
| Behavior, capability, movement, or role leakage | Reject from Input                                                                                | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle                                                                                        | The material belongs to another role or is not Input presentation data                                                                                                            |

An array, `List`, `Set`, `Map`, or another collection-shaped host form is not accepted or rejected merely by its host
category. Direct admissibility depends on an explicit closed immutable Input presentation profile and the frontend's
ability to prove that the selected source form realizes that profile without hidden host semantics. The exact V1
collection profiles remain open for separate review.

### 7.11. Zero-Adapter Direct Carrier

A selected host declaration may serve as both source evidence and runtime carrier without a second generated
presentation when it is already a legal flat immutable V1 presentation.

For the JVM V1 zero-adapter source profile, Kontrakt uses the selected host declaration itself as source evidence rather
than requiring a second generated presentation declaration. A Kotlin data class, a final Kotlin class whose selected
coordinates are primary-constructor `val` properties, or a Java 17+ record may satisfy the direct-source conditions only
when it is flat and every direct coordinate uses a supported immutable presentation profile. `data class`, `val`,
`final`, and `record` are shape evidence; none grants authority by itself.

The host shape remains evidence, not authority. Kontrakt does not admit the class or constructor as the Contract. It
retains the resolved direct coordinate names, declared presentation meanings, presence, declaration order, applicable
presentation profiles, and bounds. Constructor behavior and generated host machinery are erased from authority.

For a qualifying Kotlin carrier, coordinate order follows the selected primary-constructor property order. For a
qualifying Java record, coordinate order follows record-component order.

A user-defined interface root, interface-dispatch surface, inherited carrier shape, nested traversal, or runtime
implementation search does not qualify for this direct path.

Equivalent flat Input material may later come from another language, schema system, serialization system, or frontend
without changing the Input Contract.

Outside nested, inherited, framework-bound, proxied, third-party lifecycle, recursive, dynamic, or unsupported
collection-shaped material must be formed into a supported presentation before invocation. Input does not choose a
snapshot moment, flatten a live object graph, or traverse that material at the runtime boundary.

### 7.12. Behavior and Capability Are Not Opaque Leaves

Callbacks, services, repositories, live resources, async control surfaces, and other executable capabilities are not
rescued by labeling them opaque.

Input may carry a declared identifier, token, coordinate, source text, bounded bytes, or another approved value that
refers to something outside the machine. The execution capability or resource ownership represented by that value
remains outside Input authority.

---

## 8. Authoring Boundary

The user should not declare the same flat presentation twice.

The selected host-facing Input declaration may serve as the external evidence when the frontend can refine it
completely.

This is a source convenience, not authority.

Other source shapes remain legal outside Kontrakt. They must be converted before invocation into the same flat immutable
Input presentation.

The Input authoring surface must not force a rich object model into the core.

---

## 9. Refusal Boundary

Input has two distinct refusal times.

Definition-time refusal occurs when the selected source cannot be ratified under the Input law.

In that case no authoritative Input definition is established and no invocation may proceed under that definition.

Invocation-time Input refusal occurs when a ratified Input Contract exists but the supplied presentation cannot satisfy
the declared boundary. Required material may be unavailable, structurally incompatible, malformed under the declared
representation, or missing a required distinction.

Invocation-time Input refusal happens before Admission.

A mutable, lazy, proxied, nested, unsupported collection-backed, or lifecycle-dependent object is not repaired at this
point. It was not a legal direct Input presentation.

A Policy, Governance, Budget, Capacity, or other cross-cutting stop remains owned by the supplying Contract.

---

## 10. Relationship to Later Contracts

Input establishes only judgeable boundary presentation.

Admission judges continuation over that presentation.

Selected Canonicalization may establish a stable same-shape representative without changing the Input coordinate
surface.

Lowering later binds Input coordinates to candidate Operation-parameter Fact coordinates.

Input does not perform those later jobs.

An Input refusal therefore does not become Admission rejection, Canonicalization refusal, or Lowering refusal.

---

## 11. Open in This Section

The final token spelling and exact public Java or Kotlin carrier syntax for Input remain open.

The exact V1 collection presentation families, their constituent laws, and the host forms that may directly realize them
remain open for separate review. Collection support must not infer ordering, equality, multiplicity, key semantics,
mutability, or another Contract distinction merely from a host collection type.

Any future expansion beyond the V1 flat direct-coordinate profile requires explicit review. It must not reintroduce
nested Contract authority, runtime graph traversal, hidden snapshot semantics, or backend-owned meaning.

---

## 12. Consequences

Input becomes an explicit immutable boundary presentation rather than a host object treated as Contract authority.

The machine can accept ordinary external evidence without making Java, Kotlin, reflection, object identity, or framework
lifecycle part of Contract meaning.

The flat V1 profile reduces hidden topology and makes later Admission, Canonicalization, Lowering, diagnostics,
verification, and optimization operate on a bounded declared surface.

Some convenient application objects cannot cross the boundary directly. They require explicit presentation formation
before invocation.

That cost is intentional. The boundary rejects hidden lifecycle and representation authority instead of pretending it is
safe.

---

## 13. Migration History

This ADR was extracted mechanically from the Input-owned material of ADR-0048.

The extraction itself does not change the accepted Input Contract semantics.

A 2026-09-02 review clarified the Contract/frontend boundary for coordinate closure, direct presentation meaning,
immutability, carrier separation, inheritance and polymorphism, presence and finite choice, opaque values, presentation
bounds, and supported presentation profiles. The review also removed the scalar-only implication from flatness without
yet fixing the exact V1 collection taxonomy.

ADR-0048 remains the owner of the shared inbound-airlock composition and core-entry relation.