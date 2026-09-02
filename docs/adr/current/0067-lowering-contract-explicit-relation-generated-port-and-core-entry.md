# ADR-0067: Lowering Contract, Explicit Source-to-Operation Relation, Generated Port, and Core Entry Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0066: Canonicalization Contract
- ADR-0065: Admission Contract
- ADR-0064: Input Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

---

## 1. Context

Lowering is the final representation boundary of the inbound airlock.

Input, Admission, and selected Canonicalization operate over the Contract-visible external presentation.

Admission may permit that presentation to continue. Canonicalization may establish a stable same-shape representative.

Neither ends external-presentation authority.

Lowering declares the complete permitted relation by which selected boundary coordinates may form candidate material for
the ordinary host types declared by the selected Operation parameters.

The physical representation formation behind that relation is explicitly replaceable implementation.

The generated machine retains ownership of relation completeness, candidate completion, applicable standing judgment,
legal movement, Fact establishment, refusal routing, and the handoff to the user Operation.

---

## 2. Problem

Lowering can easily collapse into an arbitrary mapper.

If that happens, implementation code starts deciding which source values matter, which target coordinates participate,
how Facts are formed, and when the core may trust the result.

That would return Contract authority to user code.

The opposite extreme is also wrong. The compiler must not infer conversion authority from matching names, equal types, a
conversion catalog, classpath discovery, backend support, or framework convention.

Lowering therefore needs two distinct surfaces.

The Contract surface is immutable explicit relation material.

The realization surface is one generated retained host-language port with exactly one explicitly supplied
implementation.

The relation owns meaning.

The implementation only realizes representation formation.

---

## 3. Decision Drivers

Lowering is not business computation.

Lowering is not an Invariant, State Transition, Publication rule, or result producer.

The IDL relation must contain immutable data only.

Every target coordinate formed by the inbound airlock must have one explicit source binding.

Equal names and equal host types do not create hidden relations.

One source coordinate must not fan out into several Contract-visible target coordinates in V1.

Several source coordinates must not be combined into one target coordinate in V1.

The target surface must be intelligible without retaining the external Input declaration or source object graph.

One generated port preserves the implementation boundary.

Exactly one implementation is bound explicitly.

The compiler may derive planning, ABI, completion checks, verification, diagnostics, caching, and optimization from the
relation, but it must not invent the conversion implementation.

---

## 4. Decision

The Lowering authority path is:

```text
external presentation authority
-> explicit one-to-one source-to-Operation-parameter-Fact relation
-> exactly one supplied realization behind a generated port
-> complete candidate Operation-parameter material
-> applicable standing Invariant and movement judgment
-> input Fact establishment
-> ordinary user Operation invocation
```

Lowering ends external-presentation authority only after the declared relation is realized, candidate material is
complete, and the required standing judgment and legal movement succeed.

The supplied implementation forms candidate representation.

It does not own Fact meaning or establishment.

---

## 5. Boundary Source and Target

The Lowering Contract and its generated realization port are the only Lowering surfaces allowed to know both sides.

```text
interface scope:
    one closed `facts` vocabulary
    one standing `invariants` declaration

boundary side:
    selected Input presentation
    optional same-shape canonical representative

Lowering Contract:
    immutable explicit one-to-one source-to-Operation-parameter-Fact relation

generated realization boundary:
    retained plain host-language port
    exactly one explicitly supplied implementation
    compiler-derived LoweringPlan
    candidate-material completion and declared failure surface

judgment and core-entry side:
    applicable interface-level Invariant judgment
    applicable movement judgment
    input Fact establishment
    ordinary user Operation parameter values
```

The selected Input schema remains the source schema even when Canonicalization is present because Canonicalization does
not change the Contract-visible coordinates.

When Canonicalization is selected, its representative values become the source values.

When Canonicalization is absent, the admitted Input values are supplied unchanged.

Lowering does not introduce a separate `CoreMaterial`, `OperationStart`, `LoweringTarget`, or user-visible candidate
schema.

Targets are addressed through the ordinary parameter slots declared by the selected Operation.

Each target parameter type must resolve to one Fact kind declared through the enclosing interface `facts` vocabulary.

The Operation manifest does not repeat that vocabulary and does not gain a `fact` or `invariant` slot merely for
Lowering.

---

## 6. Target Fact Independence

The target Fact surface must not name, import, embed, extend, or retain a reference to the Input declaration, source
DTO, transport protocol, host object, Lowering declaration, framework context, adapter type, or source-coordinate
handle.

The target must be intelligible through its own Fact coordinates, sorts, presence, alternatives, relations, ordering,
bounds, schema, and version material.

A supplied implementation may physically read source values and construct ordinary target values.

Source type names, getters, constructors, collection implementations, object identity, runtime references, and
realization machinery have no factual authority.

The generated port source is a retained build artifact. It may remain an ordinary adapter boundary if Kontrakt is
removed.

---

## 7. Immutable Operation-Local Relation

The Lowering Contract contains no translation implementation and no business algorithm.

The enclosing interface declares `facts` and `invariants`.

The Operation manifest selects one Lowering handle.

One sibling `lowering` declaration inside that Operation states the exact relation.

```text
interface DepositContract {
    policy        DepositPolicy
    governance    DepositGovernance
    budget        DepositBudget
    capacity      DepositCapacity
    facts         DepositFacts
    invariants    DepositInvariants

    operation deposit(command: DepositCommand): DepositRecorded {
        manifest {
            flow:
                input             DepositInput
                admission         DepositAdmission
                canonicalization  DepositCanonicalization
                lowering          DepositLowering
        }

        lowering DepositLowering {
            accountIdText -> command.accountId
            amountText    -> command.amountMinor
        }
    }
}
```

`manifest` and `lowering` are sibling declarations.

The relation body does not repeat Input carriers, Operation parameter declarations, Fact declarations, or Invariant
declarations.

It contains only a finite set of exact coordinate relations.

Every relation is explicit.

Equal spelling does not create a relation.

Different spelling does not prevent a relation.

---

## 8. V1 Cardinality Law

The Contract-visible V1 relation is:

```text
one selected Input coordinate
-> one Operation-parameter Fact coordinate
```

Every target coordinate formed by the inbound airlock is bound to exactly one selected Input coordinate.

Each selected Input coordinate may appear in at most one binding.

V1 permits no `1:N` or `N:1` Contract-visible Lowering relation.

A source coordinate must not create several factual coordinates.

Several source coordinates must not be combined to derive one factual coordinate.

Those forms introduce cross-coordinate meaning that belongs to core computation unless a later Contract design
explicitly creates another authority.

Physical storage may still be split, packed, flattened, or combined behind the same Contract relation. Physical layout
does not change Contract cardinality.

---

## 9. Type Relation and Realization

Source and target host types may be identical.

Equal types do not authorize implicit copying and do not remove the generated port.

Source and target types may also differ.

In both cases the relation remains explicit.

The exactly supplied implementation performs the physical representation formation behind the generated port.

The compiler does not select a conversion from the raw type pair.

A conversion catalog does not provide authority.

A library does not select itself.

A naming convention does not provide authority.

Classpath contents do not provide authority.

Backend support does not close the implementation boundary.

The implementation may use ordinary reusable libraries when they realize only the declared representation formation.

It must not perform repository lookup, environmental resolution, business computation, Invariant judgment, State
movement, Publication, or undeclared capability access.

---

## 10. Representation Refinement, Not Core Computation

Lowering changes representation and authority domain without deriving new cross-coordinate business meaning. At the
Contract-visible boundary, V1 forms each Operation-parameter Fact coordinate from exactly one explicitly bound Input
coordinate.

Permitted one-to-one refinement families include:

```text
declared external scalar presentation
    -> immutable Fact scalar of the same declared meaning

nullable or optional presentation
    -> one explicit optional Fact coordinate

closed external alternative
    -> one explicit finite-alternative Fact coordinate

approved bounded opaque leaf presentation
    -> one immutable Fact coordinate under the same declared leaf meaning

direct selected Input coordinate
    -> one explicitly addressed Operation-parameter Fact coordinate

declared external identifier or reference presentation
    -> one Fact representation of that same declared identifier or reference meaning
```

The supplied realization may decode, parse, range-check, copy, freeze an approved bounded leaf, make an already-declared
presence distinction explicit, or select a fixed internal representation. Those actions remain Lowering only when the
Input Contract already declares the meaning being represented and the target Fact kind preserves that meaning. A generic
text coordinate does not become a date, account identity, money, or another business concept merely because an
implementation or library can parse it.

The following cardinalities are prohibited in V1:

```text
one source coordinate -> multiple contract-visible Fact coordinates
multiple source coordinates -> one contract-visible Fact coordinate
```

The prohibition is semantic, not physical. A backend may decompose one Fact coordinate into several machine words or
pack several coordinates into one region. Those choices remain backend layout and do not create `1:N` or `N:1` Lowering
relations.

Lowering must not create a new business proposition, consult mutable current machine state, execute core computation,
produce Operation Result Material, decide whether movement is legal, evaluate an Invariant, select a Transition, grant
Publication authority, apply business Policy, infer missing material, combine coordinates, or split one coordinate into
several Contract-visible meanings. The following are not Lowering:

```text
birth date -> current age
price + customer grade -> discounted price
score -> risk category
year + month + day -> business date coordinate
name text -> database lookup -> current company identity
timestamp -> separate business-visible seconds and nanos coordinates
current balance - withdrawal amount -> new balance
Fact set -> Result
Result -> outward presentation
```

The distinction is:

```text
Lowering Contract:
    one boundary coordinate -> one Operation-parameter Fact coordinate of the same declared meaning

Lowering realization:
    explicitly performs the permitted representation formation behind the generated port

core realization:
    combines, separates, and computes over established Facts and other declared immutable material

Invariant:
    judges one complete candidate Fact under the standing interface-level law for its exact kind

State and Transition:
    govern legal movement and the availability of material in a machine condition

Publication:
    governs the permitted outward claim
```

A conforming Lowering realization must be deterministic over its declared source domain and return either complete
candidate material or one declared Lowering failure. The generated machine supplies only the declared source
coordinates. Budget, Capacity, Version, Governance, and other cross-cutting gates remain outside the implementation and
retain their own authority and attribution.

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit target
coordinate of the same declared reference meaning. Resolution against another coordinate, a mutable registry, or current
core state belongs to core realization or a later judgment and must not be disguised as formation.

---

## 11. Definition-Time Completeness and Identity

The ratified Lowering material must close at least:

```text
the flow-selected Input schema
the selected Canonicalization law, when present
the enclosing interface's Fact vocabulary and standing Invariants
the selected Operation signature and every target parameter slot
the resolved Fact kind of every target Operation parameter
every explicit Input-coordinate to Operation-parameter-Fact-coordinate binding
one source coordinate and one target address for every binding
explicit binding even when source and target names or host types are equal
source-sort and target-sort structural compatibility
target-coordinate completeness and uniqueness
Input source-coordinate uniqueness across bindings
the generated plain host-language realization-port ABI
exactly one implementation binding for that port
finite depth, cardinality, intermediate storage, output, and work bounds
applicable schema, Version, and Governance material
declared failure and cross-contract stop attribution
```

Every target coordinate formed by this airlock must be formed exactly once under one complete flat Lowering Contract.
Every Input coordinate may appear in at most one binding. An Input coordinate may remain unused; that does not
implicitly create a binding or require the Operation parameter Fact surface to expose external material it does not
need.

Silent target defaults, fallback constructors, same-name auto-mapping, same-type auto-copying, catalog selection,
structural guessing, package scanning, annotations, assignability, inheritance, and discovery from implementation shape
are prohibited.

The frontend must reject missing or duplicate target formation, reused source coordinates, unknown source or target
coordinates, `1:N` or `N:1` relations, structurally incompatible sorts, hidden absence, recursive host-object traversal,
unbounded collection work, environment-dependent Contract meaning, executable material inside the relation body, or a
target type absent from the enclosing interface's Fact vocabulary.

Missing or duplicate realization implementations, port ABI mismatch, or unresolved assembly bindings are link-time
definition failures rather than runtime Lowering refusals.

The accepted IDL declaration is lowered into one flat, immutable, adapter-erased Lowering Contract material. Lowering
identity includes the source-presentation schema identity, optional Canonicalization law identity, selected Operation
identity, target parameter slots and Fact-kind identities, the canonicalized explicit one-to-one binding set, bounds,
refusal law, and relevant flow-world coordinates.

The supplied implementation identity is not part of Contract meaning. It belongs to executable machine assembly and must
conform to the generated port.

From ratified Lowering material, the compiler derives one immutable semantic `LoweringPlan`. The plan closes the exact
source read set, target write set, port ABI and invocation set, candidate-completion checks, declared failure branches,
Invariant and movement handoff, establishment requirements, cache dependencies, and backend layout requirements
legitimately implied by the Contract. The plan does not own conversion semantics or acquire Contract authority.

## 12. Generated Port Boundary

Every selected Lowering declaration generates one required retained plain Java or Kotlin realization port.

Exactly one implementation is supplied during machine assembly.

Binding must be exact and must not depend on runtime discovery.

The port exposes only the source material and candidate target surface that the declared relation requires.

The implementation does not own the relation, target participation, Fact meaning, establishment, or pipeline authority.

The generated port source is a retained build artifact. It may be committed, inspected, implemented, and called as
ordinary Kotlin or Java code. It contains no Contract authority and no Kontrakt-specific Fact wrapper. If Kontrakt is
removed, the retained port and its implementation may remain as ordinary adapter code while automatic Contract
validation, judgment orchestration, establishment, diagnostics, generated tests, and optimization disappear.

After assembly is closed, runtime performs no classpath scan, reflection, symbol lookup, coordinate-name search,
type-pair catalog lookup, service discovery, DI resolution, or implementation selection. The generated execution path
uses the one closed implementation reference or an equivalent direct backend binding.

The supplied realization receives only the source values admitted by the declared relation and returns complete
candidate material for the declared Operation parameter surface or one declared Lowering failure. It does not receive a
repository, service, State store, framework context, runtime registry, Fact population, Invariant callback, Publication
callback, or arbitrary machine context.

The exact method decomposition, result carrier, declared-failure encoding, and assembly API remain backend/API
decisions.

Any chosen ABI must preserve the explicit relation, exact implementation binding, and absence of runtime discovery.

---

## 13. Compiler-Derived Material

From the declared Lowering relation the compiler may derive:

```text
semantic LoweringPlan
generated port ABI
source-read closure
target-completion checks
candidate-completion plan
judgment and establishment handoff
verification obligations
automatic tests
diagnostic mapping
cache plan
optimization opportunities
```

These are derived compiler material.

They do not become another authored Lowering Contract.

The compiler does not infer or own the supplied conversion implementation.

---

## 14. Judgment and Establishment Boundary

The Lowering realization does not establish Fact authority merely by returning target-shaped values.

The generated machine verifies relation coverage and candidate completion.

It then applies the applicable standing Invariant and movement judgment required for the inbound handoff.

Only after those authorities succeed may the corresponding input Fact authority be established and the ordinary user
Operation be invoked.

A successful realization call therefore does not equal successful Lowering handoff.

Implementation completion is evidence available to the later generated judgment path. It is not authority by itself.

Candidate formation is not Fact establishment. The generated pipeline must verify that:

```text
every declared target coordinate is complete exactly once
the returned material conforms to the resolved Operation parameter type and Fact kind
every target coordinate is justified by its one explicitly bound Input coordinate
the material is immutable and no mutable external alias remains
presence, alternatives, relations, and ordering are explicit where the Fact surface declares them
applicable schema, Version, Governance, Budget, and Capacity gates have succeeded under their own authority
every interface-level Invariant applicable to the candidate Fact kind holds
every applicable movement judgment succeeds
no back-reference to the external presentation or realization machinery remains
```

Only after those obligations succeed does the generated machine establish input Fact authority and invoke the user
Operation with the same ordinary host value. Allocation, constructor completion, port return, or builder completion
alone does not create Fact authority.

The supplied implementation may use temporary mutable builders, scratch buffers, ordinary parsing libraries, offset
tables, sorting workspaces, or staged regions. Those objects belong exclusively to implementation. They hold no factual
authority and must never be exposed as established Facts.

---

## 15. Refusal Boundary

Lowering refuses when the declared relation and exactly supplied realization cannot provide the complete candidate
material required for the selected Operation handoff.

Lowering refusal does not mean Input refusal, Admission rejection, Canonicalization refusal, Invariant refusal, or a
Budget or Capacity result.

A target that is incomplete does not become partially established Fact material.

Lowering refusal, user Operation failure, Invariant refusal, and Publication refusal are distinct:

```text
Lowering refusal:
    no complete and establishable input Fact material exists

user Operation failure:
    established input Facts exist,
    but the replaceable implementation does not complete with its declared result

Invariant refusal:
    complete candidate Fact material exists,
    but a standing law for that exact Fact kind does not hold

Publication refusal:
    the Operation result Fact exists,
    but the declared outward claim is not permitted
```

The machine must preserve exact failure and diagnostic attribution to the owning authority.

---

## 16. V1 Optimization Boundary

Kontrakt owns the Lowering relation, `LoweringPlan`, port ABI, assembly relation, completion checks, generated judgment
handoff, verification, diagnostics, test derivation, cache planning, and other generated orchestration.

The exactly supplied implementation remains replaceable realization.

Kontrakt may inspect, devirtualize, inline, specialize, or erase the port only when the closed binding and
implementation body allow a proven equivalent result.

Where equivalence is proven, backend realization may include:

```text
devirtualization of the exactly bound port implementation
inlining or specialization of a statically analyzable realization
port-object erasure after closed linking
Canonicalization-Lowering orchestration fusion
intermediate presentation elimination
dead source-coordinate read elimination
direct source access and direct candidate-region writes
exact buffer and region sizing
single-allocation or bounded-allocation formation
primitive and finite-alternative specialization
packed relation and presence layouts
structural cache keys from the exact declared read set
AOT-generated invocation, completion, judgment, and establishment paths
```

When implementation behavior cannot be proven safe for a transformation, the backend preserves the explicit port call.
Optimization authority does not permit guessing, replacing the supplied implementation with a catalog entry, or changing
the declared relation.

Otherwise the explicit port call remains.

Lowering authority does not extend into the user core realization.

ADR-0070 separately governs analysis and optimization after the legal Operation handoff. Any transformation performed
there must preserve the Fact authority established by this Lowering boundary and mus

---

## 17. Handoff to the User Operation

Successful Lowering means:

```text
declared source-to-target relation realized
+ complete candidate Operation input
+ applicable standing judgment succeeded
+ applicable movement judgment succeeded
+ input Fact authority established
+ external-presentation authority ended
= legal Operation handoff
```

The supplied Lowering realization yields complete candidate material in the ordinary host types declared by the
Operation parameters. The generated machine applies candidate completion, the interface-level Invariants for each exact
Fact kind, and every applicable movement judgment. Successful Lowering ends only when those obligations succeed and the
candidate receives established input Fact authority.

The Operation receives ordinary declared parameter values.

It does not receive the Input presentation, canonical presentation object, Lowering declaration, source-to-target
relation table, `LoweringPlan`, generated port metadata, candidate wrapper, established-Fact wrapper, staging object, or
host-language execution context.

```text
external presentation authority:
    ended

candidate Operation input material:
    complete under the declared relation

standing judgment:
    every applicable Invariant and movement obligation succeeded

input Fact authority:
    established outside the host value representation

user Operation invocation:
    ordinary parameter values only

Result and Publication:
    not yet established or authorized
```

The Operation may produce its ordinary declared result.

Result-side Fact establishment, result-side movement, Publication, and Output Presentation belong to later authorities.

The core must not reopen the erased Input or Lowering declaration as sources of factual meaning.

---

## 18. Open in This Section

The exact generated port method decomposition is open.

The exact result carrier and declared-failure encoding are open.

The exact assembly API is open.

Those decisions must not weaken the fixed Lowering law: one operation-local immutable relation, exact one-to-one
coordinate bindings, one required retained port, exactly one explicit implementation, no runtime discovery, no implicit
conversion authority, and a generated judgment and establishment handoff.

The complete authoring surface for additional Fact participation, Operation Result Material, result-side change
formation, Publication, and Output Presentation remains outside this ADR.

---

## 19. Consequences

Lowering becomes an explicit representation-boundary Contract instead of a mapper callback.

External and core vocabularies may remain independent because every relation is authored directly.

Equal host types do not create hidden copying authority.

Different host types do not force the compiler to guess conversion meaning.

The retained generated port keeps physical realization explicit and replaceable.

The compiler gains a closed relation from which it can derive verification, diagnostics, tests, caching,
candidate-completion checks, and optimization without stealing the implementation's job.

The cost is explicit binding. V1 deliberately rejects `1:N`, `N:1`, same-name inference, and implicit converter
selection because those mechanisms would hide Contract meaning.

---

## 20. Migration History

This ADR was extracted mechanically from the Lowering-owned material of ADR-0048.

The extraction itself does not change the accepted Lowering Contract semantics.

ADR-0048 remains the owner of the shared inbound-airlock composition and the final relation from successful Lowering to
legal core entry.