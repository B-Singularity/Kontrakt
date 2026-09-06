# ADR-0067: Lowering Contract, Explicit Source-to-Operation Relation, Compiler-Derived Realization, and Core Entry Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `../../the-most-important-thing/what-contract-is.md`
- ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization
- ADR-0069: Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate
- ADR-0068: Fact Contract, Explicit Immutable Core Information, Sameness, and Uniqueness
- ADR-0066: Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission
- ADR-0065: Admission Contract, Continuation Judgment
- ADR-0064: Input Contract, Explicit Boundary Presentation
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0057: Failure Contract
- ADR-0050: State / Transition Contract
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary

---

## Amendment

This ADR previously required every selected Lowering declaration to produce one retained generated realization port with
exactly one user-supplied implementation.

That model is removed.

Lowering is a declarative one-dimensional Contract expression. The user declares the exact source-to-target relation and
the Contract material required to make that relation complete. Kontrakt resolves and establishes that meaning, derives
the required Lowering realization knowledge, and the compiler/backend performs the actual value formation.

The user does not implement a Lowering mapper, adapter, converter port, callback, or generated Lowering interface.

The ordinary user-supplied realization boundary is Operation. The backend forms the actual Operation-parameter values
required by the established Lowering Contract and binds those values to the user Operation invocation.

An internal generated helper, ABI, table, specialized function, or direct bytecode path may exist when useful. Such
material belongs to compiler/backend realization. It is not a retained user implementation requirement and it does not
acquire Contract authority.

This amendment does not change the explicit one-to-one V1 relation, source and target independence, prohibition on
same-name or same-type inference, prohibition on hidden cross-coordinate business computation, candidate-versus-Fact
separation, standing Invariant and movement handoff, or refusal attribution decided below.

---

## 1. Context

Lowering is the final representation boundary of the inbound airlock.

Input, Admission, and selected Canonicalization operate over the Contract-visible external presentation.

Admission may permit that presentation to continue. Canonicalization may establish a stable same-shape representative.

Neither ends external-presentation authority.

Lowering declares the complete permitted relation by which selected boundary coordinates may form candidate material for
the ordinary host types declared by the selected Operation parameters.

The declaration is Contract expression. It is not a request for the user to write representation-formation code.

Kontrakt resolves the relation against the selected Input, optional Canonicalization law, enclosing Fact vocabulary, and
Operation signature. From that established meaning the compiler derives the exact source reads, target writes, required
representation refinements, candidate-completion obligations, and the later judgment handoff.

The backend performs the physical value formation.

The generated machine retains ownership of relation completeness, candidate completion, applicable standing judgment,
legal movement, Fact establishment, refusal routing, and the handoff to the user Operation.

---

## 2. Problem

Lowering can easily collapse into an arbitrary mapper.

If that happens, implementation code starts deciding which source values matter, which target coordinates participate,
how Facts are formed, and when the core may trust the result.

That would return Contract authority to user code.

A generated mapper interface does not solve the problem. It merely moves the hidden meaning into a callback selected at
assembly time.

The opposite extreme is also wrong. The compiler must not infer conversion authority from matching names, equal types, a
raw host-type pair, classpath discovery, backend support, or framework convention.

Lowering therefore needs one declarative Contract surface and one replaceable compiler/backend realization boundary.

The Contract surface owns the exact permitted relation and every semantic refinement required to make that relation
complete.

The compiler/backend realizes that established meaning over actual runtime values.

If the declared Contract does not contain enough meaning to determine a legal Lowering realization, the definition is
incomplete. Kontrakt must reject it or the Contract language must gain an explicit way to state the missing meaning.
User implementation must not silently fill the gap.

---

## 3. Decision Drivers

Lowering is not business computation.

Lowering is not an Invariant, State Transition, Publication rule, or result producer.

The Lowering declaration must contain immutable Contract material only.

Every target coordinate formed by the inbound airlock must have one explicit source binding.

Equal names and equal host types do not create hidden relations.

One source coordinate must not fan out into several Contract-visible target coordinates in V1.

Several source coordinates must not be combined into one target coordinate in V1.

The target surface must be intelligible without retaining the external Input declaration or source object graph.

The user must not implement Lowering in Java or Kotlin merely because physical representation work is required.

The compiler may derive planning, completion checks, verification, diagnostics, caching, and optimization from the
established relation.

The backend may choose any physical formation strategy that preserves that established relation and its required
representation law.

The compiler must not invent missing Contract meaning from raw implementation convenience.

---

## 4. Decision

The Lowering authority path is:

```text
external presentation authority
-> explicit one-to-one source-to-Operation-parameter-Fact relation
-> established Lowering Contract
-> compiler-derived Lowering realization knowledge
-> backend value formation over the actual source values
-> complete candidate Operation-parameter material
-> applicable standing Invariant and movement judgment
-> input Fact establishment
-> ordinary user Operation invocation
```

Lowering ends external-presentation authority only after the declared relation has been realized over the actual source
values, candidate material is complete, and the required standing judgment and legal movement succeed.

Backend value formation does not own Fact meaning or establishment.

The user Operation does not perform Lowering.

---

## 5. Boundary Source and Target

The Lowering Contract is the only Contract surface allowed to state the relation between the boundary side and the
Operation-parameter Fact side.

```text
interface scope:
    one closed `facts` vocabulary
    one standing `invariants` declaration

boundary side:
    selected Input presentation
    optional same-shape canonical representative

Lowering Contract:
    immutable explicit one-to-one source-to-Operation-parameter-Fact relation

compiler/backend realization:
    compiler-derived LoweringPlan
    exact source reads and target writes
    permitted representation refinement
    candidate-material formation
    completion and declared failure paths

judgment and core-entry side:
    applicable interface-level Invariant judgment
    applicable movement judgment
    input Fact establishment
    ordinary user Operation parameter values
```

The selected Input schema remains the source schema even when Canonicalization is present because Canonicalization does
not change the Contract-visible coordinates.

When Canonicalization is selected, its representative values become the source values.

When Canonicalization is absent, the admitted Input values continue unchanged.

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

The backend may physically read source values and materialize ordinary target values.

Source type names, getters, constructors, collection implementations, object identity, runtime references, generated
helpers, and backend machinery have no factual authority.

A generated host carrier may exist because the selected Operation ABI requires one. That carrier is representation. It
is not Fact authority.

If a backend can lawfully eliminate an intermediate carrier while preserving the host-visible Operation call and every
Contract obligation, that elimination is a physical optimization only.

---

## 7. Immutable Operation-Local Relation

The Lowering Contract contains no user implementation callback and no business algorithm.

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

It contains only the exact Contract material owned by Lowering.

Every source-to-target relation is explicit.

Equal spelling does not create a relation.

Different spelling does not prevent a relation.

If a source and target require a representation refinement whose meaning is not already fixed by the declared Lowering
material and applicable Contract-owned sort law, the declaration is incomplete. A user-written converter is not an
implicit extension of the Contract.

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

## 9. Type Relation and Compiler-Derived Realization

Source and target host types may be identical.

Equal types do not authorize implicit copying and do not remove the explicit Lowering relation.

Source and target host types may also differ.

A raw type difference does not ask the user to provide an implementation.

The relation and applicable Contract-owned representation law must together determine what the difference means.

The compiler does not select meaning from the raw host-type pair.

A conversion catalog does not provide Contract authority merely because it contains a compatible function.

A library does not select itself.

A naming convention does not provide authority.

Classpath contents do not provide authority.

Backend support does not create missing Contract meaning.

When the established Lowering Contract completely determines one permitted representation refinement, the compiler may
lower that meaning to any supported backend operation, library primitive, generated helper, table, parser, copier,
constructor path, or direct machine representation that preserves it.

When the declaration does not determine the refinement, compilation must fail rather than requesting a user mapper.

The backend realization must not perform repository lookup, environmental resolution, business computation, Invariant
judgment, State movement, Publication, or undeclared capability access as part of Lowering.

---

## 10. Representation Refinement, Not Core Computation

Lowering changes representation and authority domain without deriving new cross-coordinate business meaning.

At the Contract-visible boundary, V1 forms each Operation-parameter Fact coordinate from exactly one explicitly bound
Input coordinate.

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

Compiler/backend realization may decode, parse, range-check, copy, freeze an approved bounded leaf, make an
already-declared presence distinction explicit, or select a fixed internal representation only when that exact
refinement is already determined by the established Contract meaning.

A generic text coordinate does not become a date, account identity, money, or another business concept merely because a
backend library can parse it.

The following cardinalities are prohibited in V1:

```text
one source coordinate -> multiple Contract-visible Fact coordinates
multiple source coordinates -> one Contract-visible Fact coordinate
```

The prohibition is semantic, not physical. A backend may decompose one Fact coordinate into several machine words or
pack several coordinates into one region. Those choices remain backend layout and do not create `1:N` or `N:1` Lowering
relations.

Lowering must not create a new business proposition, consult mutable current machine state, execute core computation,
produce Operation Result Material, decide whether movement is legal, evaluate an Invariant, select a Transition, grant
Publication authority, apply business Policy, infer missing material, combine coordinates, or split one coordinate into
several Contract-visible meanings.

The following are not Lowering:

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
    compiler/backend formation of the actual target value under the established Lowering Contract

core realization:
    user Operation computation over established Facts and other lawfully available immutable material

Invariant:
    judgment over one complete candidate Fact under the standing interface-level law for its exact kind

State and Transition:
    legal movement authority under their own Contract law

Publication:
    permitted outward exposure authority
```

A conforming Lowering realization must be deterministic over its declared source domain and produce either complete
candidate material or one declared Lowering refusal outcome under the owning failure law.

The generated machine supplies only the source coordinates selected by the established relation.

Budget, Capacity, Version, Governance, and other cross-cutting judgments remain under their own authority and
attribution.

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit target
coordinate of the same declared reference meaning. Resolution against another coordinate, a mutable registry, or current
core state belongs elsewhere and must not be disguised as formation.

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
the Contract-owned representation refinement required by each non-identical representation
target-coordinate completeness and uniqueness
Input source-coordinate uniqueness across bindings
finite depth, cardinality, intermediate storage, output, and work bounds
applicable schema, Version, and Governance material
declared refusal and cross-contract stop attribution
```

Every target coordinate formed by this airlock must be formed exactly once under one complete flat Lowering Contract.

Every Input coordinate may appear in at most one binding.

An Input coordinate may remain unused. That does not implicitly create a binding or require the Operation parameter Fact
surface to expose external material it does not need.

Silent target defaults, fallback constructors, same-name auto-mapping, same-type auto-copying, catalog selection,
structural guessing, package scanning, annotations, assignability, inheritance, and discovery from implementation shape
are prohibited.

The frontend must reject missing or duplicate target formation, reused source coordinates, unknown source or target
coordinates, `1:N` or `N:1` relations, structurally incompatible sorts, hidden absence, recursive host-object traversal,
unbounded collection work, environment-dependent Contract meaning, executable user material inside the relation body, a
target type absent from the enclosing interface's Fact vocabulary, or a representation refinement whose meaning is not
closed by the Contract.

There is no missing-Lowering-implementation link failure because the user does not supply a Lowering implementation.

A backend that cannot realize a semantically complete Lowering Contract with the required guarantees must reject that
backend realization. Backend inability does not change the Contract.

The accepted IDL declaration is lowered into one flat, immutable, adapter-erased Lowering Contract material.

Lowering identity includes the source-presentation schema identity, optional Canonicalization law identity, selected
Operation identity, target parameter slots and Fact-kind identities, the canonicalized explicit one-to-one binding set,
required representation-refinement law, bounds, refusal law, and relevant flow-world coordinates.

Backend helper identity, generated class identity, host object identity, allocation strategy, and machine-code shape are
not part of Lowering Contract meaning.

From ratified Lowering material, the compiler derives one immutable semantic `LoweringPlan` or equivalent realization
knowledge.

That derived material closes the exact source read set, target write set, required representation operations,
candidate-completion checks, declared refusal branches, Invariant and movement handoff, establishment requirements,
cache dependencies, and backend layout requirements legitimately implied by the Contract.

Derived material does not become another authored Lowering Contract.

---

## 12. Compiler and Backend Realization Boundary

A selected Lowering declaration creates no user implementation interface.

The user does not implement `DepositLowering`, `PlaceOrderLowering`, a generated mapper, or another Lowering callback.

Kontrakt resolves the selected Lowering Contract and derives the executable obligations required to form the actual
Operation-parameter values.

The backend realizes those obligations directly.

A backend may use an internal generated function, method, table, parser, constructor path, value builder, direct field
or array access, primitive slab, stack local, register value, or another internal representation.

Those structures are replaceable implementation.

They are not retained user-facing Contract surfaces.

They do not select Lowering meaning.

They do not own Fact authority.

They do not require runtime classpath scanning, reflection, service discovery, coordinate-name search, type-pair lookup,
or DI resolution to select a Lowering implementation.

The actual user Operation implementation is bound separately under ADR-0046 and admitted as User-System Realization
under ADR-0070.

Lowering reaches that Operation only after the required candidate and establishment judgments succeed.

The exact internal method decomposition, helper ABI, carrier materialization strategy, refusal encoding, and JVM
lowering remain compiler/backend decisions.

Any chosen realization must preserve the explicit relation and absence of hidden implementation authority.

---

## 13. Compiler-Derived Material

From the declared Lowering relation the compiler may derive:

```text
semantic LoweringPlan
source-read closure
target-write closure
representation-refinement operations
candidate-completion plan
judgment and establishment handoff
verification obligations
automatic tests
diagnostic mapping
cache dependencies
optimization opportunities
backend formation material
```

These are derived compiler material.

They do not become another authored Lowering Contract.

The compiler may choose how to execute an already-established refinement.

It may not create a refinement whose meaning is absent from the Contract.

---

## 14. Judgment and Establishment Boundary

Backend value formation does not establish Fact authority merely by producing target-shaped values.

The generated machine verifies relation coverage and candidate completion.

It then applies the applicable standing Invariant and movement judgments required for the inbound handoff.

Only after those authorities succeed may the corresponding input Fact authority be established and the ordinary user
Operation be invoked.

Successful value formation therefore does not equal successful Lowering handoff.

Candidate formation is not Fact establishment.

The generated pipeline must preserve at least these obligations:

```text
every declared target coordinate is complete exactly once
the formed material conforms to the resolved Operation parameter type and Fact kind
every target coordinate is justified by its one explicitly bound Input coordinate
every required representation refinement follows the established Lowering law
the material is immutable and no mutable external alias survives into Fact authority
presence, alternatives, relations, and ordering are explicit where the Fact surface declares them
applicable schema, Version, Governance, Budget, and Capacity judgments succeed under their own authority
every interface-level Invariant applicable to the candidate Fact kind holds
every applicable movement judgment succeeds
no back-reference to the external presentation or backend realization machinery remains
```

Only after those obligations succeed does the machine establish input Fact authority and invoke the user Operation with
the ordinary host value required by the generated Operation realization surface.

Object allocation, constructor completion, builder completion, parsing completion, or backend materialization alone does
not create Fact authority.

The backend may use temporary mutable builders, scratch buffers, ordinary libraries, offset tables, sorting workspaces,
or staged regions when those choices preserve the established Lowering law.

Such objects belong exclusively to implementation and must never be exposed as established Facts.

---

## 15. Refusal Boundary

Lowering refuses when the established Lowering law cannot form the complete candidate material required for the selected
Operation handoff from the actual admitted source values.

A runtime refusal is distinct from a definition that failed to state enough Lowering meaning.

An incomplete Contract is rejected before executable realization.

A semantically complete Contract may still define an actual-value refusal condition, such as a declared representation
that cannot be formed for the presented value under the exact Lowering law.

Lowering refusal does not mean Input refusal, Admission rejection, Canonicalization refusal, Invariant refusal, or a
Budget or Capacity result.

A target that is incomplete does not become partially established Fact material.

Lowering refusal, user Operation failure, Invariant refusal, and Publication refusal are distinct:

```text
Lowering refusal:
    no complete and establishable input Fact material can be formed under the declared Lowering law

user Operation failure:
    established input Facts exist,
    but the admitted user Operation realization does not complete with its declared result

Invariant refusal:
    complete candidate Fact material exists,
    but a standing law for that exact Fact kind does not hold

Publication refusal:
    the Operation result Fact exists,
    but the declared outward exposure is not permitted
```

The machine must preserve exact Failure and diagnostic attribution to the owning authority.

---

## 16. V1 Optimization Boundary

Kontrakt owns the Lowering Contract, derived `LoweringPlan`, exact source and target relation, completion checks,
generated judgment handoff, verification, diagnostics, test derivation, cache dependencies, and backend realization
requirements.

There is no user Lowering implementation whose call must be preserved.

The backend may therefore optimize the generated formation path directly when it preserves the established Contract.

Possible realizations include:

```text
Canonicalization-Lowering orchestration fusion
intermediate presentation elimination
dead unused source-coordinate elimination
direct source access and direct target writes
exact buffer and region sizing
single-allocation or bounded-allocation formation
primitive and finite-alternative specialization
packed relation and presence layouts
structural cache keys from the exact declared read set
specialized parsing or decoding for an established representation law
carrier construction only at the host ABI boundary
direct binding of formed values into the generated Operation invocation
AOT-generated completion, judgment, establishment, and invocation paths
```

If the generated host Operation ABI requires a carrier instance, the backend materializes that actual value before the
user Operation call.

If an intermediate carrier is not host-observable and can be removed without changing the Operation ABI or Contract
meaning, the backend may erase it.

Optimization does not permit same-name mapping, raw type-pair conversion inference, undeclared coordinate synthesis, or
business computation inside Lowering.

Lowering authority does not extend into the user core realization.

ADR-0070 separately governs analysis and optimization of the admitted user Operation realization after the legal
Operation handoff.

---

## 17. Handoff to the User Operation

Successful Lowering means:

```text
declared source-to-target relation realized over the actual source values
+ complete candidate Operation input
+ applicable standing judgment succeeded
+ applicable movement judgment succeeded
+ input Fact authority established
+ external-presentation authority ended
= legal Operation handoff
```

The compiler/backend forms the actual ordinary host values required by the Operation parameters.

For an object-shaped generated Fact carrier, this means the backend supplies an actual value of that generated type. The
user does not construct that input value merely to perform Lowering.

Conceptually:

```text
actual Input presentation value
    -> established Lowering relation
    -> backend forms actual Operation-parameter value
    -> candidate judgments
    -> Fact establishment
    -> user Operation receives that actual value
```

The Operation does not receive the Input presentation, canonical presentation object, Lowering declaration,
source-to-target relation table, `LoweringPlan`, backend helper metadata, candidate wrapper, established-Fact wrapper,
staging object, or host-language execution context.

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
    ordinary generated or declared host parameter values only

Result and Publication:
    not yet established or authorized
```

The Operation may produce its ordinary declared result candidate.

Result-side Fact establishment, result-side movement, Publication, and Output Presentation belong to later authorities.

The core must not reopen the erased Input or Lowering declaration as sources of factual meaning.

---

## 18. Open in This Section

The exact compiler-internal Lowering representation is open.

The exact backend helper decomposition and JVM ABI are open.

The exact carrier materialization and allocation strategy are open.

The exact encoding of declared Lowering refusal is open.

The exact authoring surface for a nontrivial one-to-one representation refinement that cannot be expressed by the
current relation and existing Contract-owned sort law is open.

That open syntax question does not reopen a user implementation callback. Any future form must state the missing
Contract meaning explicitly enough for Kontrakt to realize it.

Those decisions must not weaken the fixed Lowering law: one operation-local immutable relation, exact one-to-one
coordinate bindings, no runtime implementation discovery, no user-supplied Lowering implementation, no implicit
conversion authority, and a generated judgment and establishment handoff.

The complete authoring surface for additional Fact participation, Operation Result Material, result-side change
formation, Publication, and Output Presentation remains outside this ADR.

---

## 19. Consequences

Lowering becomes an explicit representation-boundary Contract instead of a mapper callback.

External and core vocabularies may remain independent because every Contract-visible relation is authored directly.

Equal host types do not create hidden copying authority.

Different host types do not force the user to supply a converter.

The Contract must instead state enough meaning for Kontrakt to determine the permitted representation refinement.

The compiler gains a closed relation from which it can derive verification, diagnostics, tests, caching,
candidate-completion checks, and backend formation.

The backend gains freedom to realize that established relation without exposing a user implementation port.

The user implements the Operation business algorithm rather than reimplementing Lowering.

The cost moves into Contract expressiveness and compiler/backend responsibility. Kontrakt must reject incomplete
Lowering meaning, support the declared representation law on the selected backend, and preserve exact refusal and
establishment boundaries.

V1 deliberately rejects `1:N`, `N:1`, same-name inference, raw type-pair converter selection, and hidden callbacks
because those mechanisms would obscure Contract meaning.

---

## 20. Migration History

This ADR was extracted mechanically from the Lowering-owned material of ADR-0048.

The extraction preserved the then-accepted Lowering Contract semantics.

The later amendment in this ADR removes the retained generated Lowering port and user-supplied Lowering realization
model. The explicit source-to-target relation, V1 cardinality law, target independence, candidate boundary, Fact
establishment handoff, and refusal ownership remain in force.

The current inbound-airlock ownership is split across ADR-0064 Input, ADR-0065 Admission, ADR-0066 Canonicalization,
this ADR for Lowering, ADR-0068 Fact, ADR-0069 Invariant, and the common selection and realization boundary in ADR-0047.
ADR-0048 remains the historical migration source rather than the current owner of those split one-dimensional laws.