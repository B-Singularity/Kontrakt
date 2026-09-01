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

A host declaration may already state useful external contract evidence. Primitive values, strings, enums, nullability,
and approved closed immutable scalar types do not become silent merely because Kotlin or Java carries them.

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
does not become hidden Contract structure.

Input must reuse the existing acquisition and planning pipeline rather than create another source-analysis machine.

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
recursive type, collection object graph, or shared-reference graph does not become a second Contract inside Input.

Outside material with those shapes must be flattened into the declared presentation by an explicit replaceable adapter
before it reaches the runtime Input boundary.

The flatness rule prevents host topology from becoming hidden Contract ancestry.

### 4.3. Source Evidence Is Not Authority

A supported Java or Kotlin declaration may nominate Input evidence.

The declaration remains source evidence. A runtime object remains a carrier.

Input identity and meaning are not derived from the class name alone, runtime type, object identity, constructor
execution, property accessor behavior, or storage layout.

Equivalent flat presentation material may later come from another frontend, schema compiler, serialization system, or
language without changing the Input Contract.

---

## 5. Definition-Time Authority Path

Input reuses the established compiler path.

```text
slot-selected flat Input declaration
-> existing adapter-neutral metamodel acquisition
-> FrozenMetamodelImage
-> planning-facing frozen providers
-> direct-coordinate projection and source-profile judgment
-> adapter-erased lowered Input material
-> ratified contract graph unit
-> ContractImage-visible Input material
-> generated boundary realization
```

`FrozenMetamodelImage` remains operation-neutral source and metamodel material.

Input planning does not reopen reflection, KSP, bytecode, source AST, PSI, or backend-local handles.

Input does not create another `TypeReference` closure frontier, cycle-identity acquisition path, raw-fact image, graph
engine, readiness bridge, budget ledger, or frozen publication protocol.

Generation-oriented constructor selection, polymorphic expansion, recursive member traversal, collection expansion, and
cycle truncation do not become Input semantics merely because compiler machinery may already support such operations
elsewhere.

The operation-specific result is lowered into Kontrakt-owned Input material. A source declaration, DTO object, backend
handle, or planning node does not become Input identity.

The resolved Input presentation is the logical operation-specific planning and lowering result. It is not another frozen
metamodel image.

---

## 6. Invocation Boundary

At invocation time the path is:

```text
already-formed flat immutable Input presentation
-> generated boundary reader or static gate
   -> declared presentation formed -> Admission
   -> declared presentation refused -> declared Input stop
   -> delegated cross-cutting gate stops -> result owned by that Contract
```

The runtime Input boundary accepts only already-formed immutable presentation material.

A mutable object, lazy value, proxy, framework-bound object, live collection view, nested carrier graph, or
lifecycle-dependent carrier is not repaired by Input.

The generated boundary realization must not choose a capture moment, invoke lazy behavior, snapshot mutable state,
traverse an outside object graph, or derive stable meaning from a live carrier.

Presentation formation that is necessary to reach the flat Input surface happens before this boundary.

The immutable presentation is not a second frozen image, contract graph unit, or mandatory universal
`FormedBoundaryMaterial` domain type. Runtime invocation must not repeat metamodel acquisition, source resolution,
planning, graph ratification, canonical identity derivation, or Contract publication.

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
silently chooses one. A direct nullable coordinate, enum, approved scalar, or bounded opaque leaf may be accepted only
through one fixed frontend profile. A user-owned nested type, collection carrier, sealed hierarchy, generic object
structure, or interface-based part is not guessed into a flat Input law. It is rejected or flattened by an explicit
adapter before Input.

**Hidden movement** exists when observing the alleged Input performs behavior or depends on time. Callbacks, lazy
loading, live streams, futures, suppliers, services, capabilities, and resource handles are not Input material. A source
profile must exclude, project, or reject them; it must not silently ratify them as boundary data.

Slot nomination does not guarantee ratification. Input owns two distinct refusal boundaries. A source that cannot be
deterministically refined is rejected during planning or lowering and never receives ContractImage-visible authority. A
ratified Input Contract may still refuse an invocation when the actual carrier cannot make the declared presentation
available. Admission begins only after Input formation succeeds.

An Input source candidate is ratifiable only when it satisfies the conditions below.

### 7.1. Root Selection and Coordinate Closure

The operation's `input` slot selects one exact Input source.

Selection must resolve to one exact source symbol at definition time.

Package convention, annotation scanning, runtime lookup, assignable-type search, service registration, implementation
discovery, or naming convention must not complete the binding.

Every contract-visible coordinate must be closed under the supported Input source profile.

No coordinate may remain raw, wildcarded, star-projected, platform-erased, dynamically typed, or otherwise open.

### 7.2. Direct Coordinate Sorts

V1 may directly refine primitive values, strings, enums, nullability, and approved closed immutable scalar profiles when
their complete presentation law is known.

A direct coordinate is accepted because Kontrakt can determine its presentation meaning. It is not accepted because the
JVM happens to have a convenient type.

A source profile must preserve the distinctions Input declares.

### 7.3. Explicit Immutability

Input presentation must already be immutable at the runtime boundary.

Runtime snapshot timing, mutable backing storage, live views, lazy materialization, proxy activation, and framework
lifecycle do not become Input meaning.

A host source declaration may be convenient for presentation formation. That does not authorize hidden mutation or
observation.

### 7.4. Carrier and Contract Separation

A carrier does not own Input meaning.

Constructors, factories, getters, generated methods, `equals`, `hashCode`, `toString`, delegation, and implementation
helpers remain host mechanics unless a supported frontend profile explicitly refines a piece of source evidence into
Contract material.

A direct host presentation can serve as both source evidence and runtime carrier only when the complete visible Input
surface satisfies the selected V1 law.

### 7.5. Interface and Inheritance Boundary

A user-defined interface, runtime implementation relation, inherited member, override, default method, or subtype
discovery does not provide Input authority.

No Input coordinate is inherited from a parent Contract.

No runtime implementation can add Input meaning.

Any accepted host declaration must independently satisfy the Input source law after implementation and ancestry
mechanics are erased.

### 7.6. Nullability, Absence, Defaults, and Finite Choice

Null, absence, presence, finite alternatives, and defaults are presentation distinctions when the selected source
exposes them.

Input must not silently collapse them.

A host default value does not become an invisible Input rule merely because the language can supply it.

A finite choice must be closed and deterministically represented if it participates in Input. Unknown runtime variants
cannot be accepted by subtype discovery.

### 7.7. References and Opaque Values

Runtime references do not provide semantic identity.

An approved opaque scalar or leaf profile may be accepted only when Kontrakt owns enough versioned law to identify the
presentation distinctions that matter.

A reference to an arbitrary user object is not an opaque scalar profile.

### 7.8. Presentation-Only Authority

Input may declare coordinate names, direct sorts, presence, finite scalar choices, approved opaque scalar profiles,
declared references represented as values, and bounds.

Input must not decide validity, canonical identity, core meaning, State legality, Publication permission, Policy
selection, or movement. Those authorities belong to other Contracts.

### 7.9. Supported Direct-Coordinate Condition

A direct coordinate may use a primitive, `String`, a closed enum, or an explicitly approved closed immutable scalar
profile such as a pinned UUID, decimal, date, instant, bounded text, bounded bytes, token, identifier, path, or URI
presentation.

Approval belongs to the frontend profile, not to a familiar host type name. Host methods, object identity, equality,
hashing, locale, timezone, scale, parsing, normalization, and serialization behavior do not become Input law.

### 7.10. V1 User-Facing Authoring Classes

The V1 user-facing policy has four classes.

| Authoring class                                 | V1 treatment                                                                         | Examples                                                                                                                                                           | Contract effect                                                                                                                                                            |
|-------------------------------------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct flat immutable presentation              | Refine and ratify directly when every coordinate is direct, immutable, and supported | Primitive and `String` coordinates, closed enums, approved closed immutable scalar leaves, flat Kotlin data class, flat final Kotlin carrier, flat Java record     | The selected declaration supplies external Contract evidence and the runtime value already satisfies the Input boundary; no duplicate presentation declaration is required |
| Outside structured presentation                 | Require explicit flattening or presentation formation before Input                   | Nested DTO, embedded Value Object, inherited product, sealed hierarchy, array, ordinary `List`, `Set`, or `Map`, recursive tree, graph, dynamic JSON object        | An adapter removes outside structure and produces the declared flat Input coordinates; Kontrakt does not recursively adopt the outside Contract                            |
| Implementation-shaped carrier                   | Require explicit adapter or reject the source                                        | Mutable JavaBean, framework DTO, proxy, entity, custom getter, delegated property, interface root, runtime-discovered implementation, third-party lifecycle object | Host conventions, capture timing, lifecycle, and implementation relationships are removed before the Input boundary                                                        |
| Behavior, capability, movement, or role leakage | Reject from Input                                                                    | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle        | The material belongs to another role or is not Contract data                                                                                                               |

### 7.11. Zero-Adapter Direct Carrier

A selected host declaration may serve as both source evidence and runtime carrier without a second generated
presentation when it is already a legal flat immutable V1 presentation.

For the JVM V1 zero-adapter source profile, Kontrakt uses the selected host declaration itself as source evidence rather
than requiring a second generated presentation declaration. A Kotlin data class, a final Kotlin class whose selected
coordinates are primary-constructor `val` properties, or a Java 17+ record may satisfy the direct-source conditions only
when it is flat and every direct coordinate uses a supported immutable sort. `data class`, `val`, `final`, and `record`
are shape evidence; none grants authority by itself.

The host shape remains evidence, not authority. Kontrakt does not admit the class or constructor as the Contract. It
retains the resolved direct coordinate names, sorts, presence, declaration order, approved scalar profiles, and bounds.
Constructor behavior and generated host machinery are erased from authority.

For a qualifying Kotlin carrier, coordinate order follows the selected primary-constructor property order. For a
qualifying Java record, coordinate order follows record-component order.

A user-defined interface root, interface-dispatch surface, inherited carrier shape, nested traversal, or runtime
implementation search does not qualify for this direct path.

Equivalent flat Input material may later come from another language, schema system, serialization system, or frontend
without changing the Input Contract.

Outside nested, inherited, framework-bound, collection-shaped, proxied, third-party lifecycle, recursive, or dynamic
material must be converted before invocation. Input does not choose a snapshot moment, flatten a live object graph, or
traverse that material at the runtime boundary.

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

In that case no authoritative Input material, ContractImage-visible Input, or runtime Input realization is produced.

Invocation-time Input refusal occurs when a ratified Input Contract exists but the supplied presentation cannot satisfy
the declared boundary. Required material may be unavailable, structurally incompatible, malformed under the declared
representation, or missing a required distinction.

Invocation-time Input refusal happens before Admission.

A mutable, lazy, proxied, nested, collection-backed, or lifecycle-dependent object is not repaired at this point. It was
not a legal direct Input presentation.

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

ADR-0048 remains the owner of the shared inbound-airlock composition and core-entry relation.