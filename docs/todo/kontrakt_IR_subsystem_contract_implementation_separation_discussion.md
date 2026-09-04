# Kontrakt IR Subsystem — Contract / Implementation Separation Discussion Summary

> Purpose: A design reference for future IR architecture work.
>
> This document does not establish new Accepted Contract law.  
> It is a structured **design criterion / architecture proposal / review memo** derived from the discussion so far.

---

# 1. Problem Statement

In conventional compiler diagrams, IR is often presented as a simple intermediate data structure:

```text
Frontend
    ↓
IR
    ↓
Optimizer
    ↓
Backend
```

In a production compiler, however, IR is not merely a temporary data structure.

Once multiple subsystems begin depending on a particular IR's:

```text
node kinds
fields
traversal model
identity model
mutation model
storage layout
```

the IR effectively becomes an internal compiler ABI.

For example:

```text
Optimizer
Verifier
Diagnostics
Analysis
Backend
Incremental Cache
```

may all become coupled to one concrete IR implementation.

At that point, even a physical representation change such as:

```text
object graph
    ↓
primitive slab
```

can become a compiler-wide rewrite.

Therefore it is dangerous to think:

> "IR is only an implementation detail, so we can replace it later."

Two fundamental problems must be prevented:

```text
1. IR semantics becoming unnecessarily coupled to physical representation.

2. Concrete implementations of adjacent IR levels communicating directly,
   causing inter-level architecture to harden into implementation topology.
```

Kontrakt should handle this more strictly than an ordinary compiler.

---

# 2. Core Conclusion

IR should be treated as a **subsystem** in its own right.

That subsystem should itself follow contract-oriented architecture and be divided into:

```text
Contract Plane
Implementation Plane
```

Each IR level is defined first by a Contract.

Its concrete data structure is only a replaceable implementation of that Contract.

Core structure:

```text
IR Level Contract
        ↑
        │ satisfies
        │
IR Implementation A
IR Implementation B
IR Implementation C
```

Movement between IR levels follows the same rule.

```text
IR-L1 Contract
        │
        ▼
L1 → L2 Lowering Contract
        │
        ▼
IR-L2 Contract
```

The concrete lowering algorithm is also replaceable:

```text
Lowering Implementation A
Lowering Implementation B
```

Each is merely a realization of the lowering Contract.

---

# 3. Most Important Architectural Rule

## 3.1 Concrete IR Implementations Must Not Communicate Directly

Bad structure:

```text
HIR Implementation
    directly knows
MIR Implementation
```

For example:

```text
HIRNode.lowerTo(MIRBuilder)
```

This causes MIR implementation details to penetrate the HIR implementation.

A better structure is:

```text
HIR Contract
        │
        ▼
HIR → MIR Lowering Contract
        │
        ▼
MIR Contract
```

A separate implementation:

```text
HIRToMIRLoweringImpl
```

consumes:

```text
HIR Contract Reader
MIR Contract Builder
```

and nothing more.

In other words:

```text
HIR Impl
    ✕
MIR Impl
```

There is no direct dependency between them.

---

## 3.2 IR Consumers Consume the IR Contract, Not Concrete Storage

The following compiler subsystems:

```text
Optimizer
Verifier
Diagnostics
Backend
Analysis
Reference
```

must not directly depend on the IR's object topology or physical storage.

The intended structure is:

```text
                IR Contract
               /    |     \
              /     |      \
             ▼      ▼       ▼
        Optimizer Verifier Analysis
```

The IR provider does not need to know which downstream consumers exist.

Consumers depend only on the published IR Contract.

This follows the same principle already used elsewhere in Kontrakt:

```text
A producer does not know concrete consumer topology.

A consumer reads only the published Contract.
```

---

# 4. What an IR Contract Must Define

An IR Contract is not merely a collection of Java or Kotlin interfaces.

It is not an OOP abstraction layer.

It is a **meaning contract**.

Each IR level should define at least:

```text
1. Semantic Vocabulary
2. Invariants
3. Equivalence Relation
4. Observable Surface
5. Identity Law
6. Construction Law
7. Publication / Mutation Law
8. Accepted Input
9. Legal Output
10. Provenance Obligation
```

---

# 5. Semantic Vocabulary

The IR Contract defines which semantic concepts exist primitively at that level.

Examples:

```text
Value
Definition
Use
Block
Control Edge
Call
Effect
Merge Relation
```

A critical distinction must be maintained between semantic concepts and representation artifacts.

Possible semantic vocabulary:

```text
Call
Branch
Value
Effect
```

Possible implementation artifacts:

```text
PhiNode
ParentPointer
UseList
InstructionLinkedList
```

If the Contract unnecessarily fixes the latter, implementation replaceability is reduced.

---

# 6. Invariants

The Contract defines properties guaranteed to hold once material has reached that IR level.

For example:

```text
all references are exact
```

At an SSA-like level:

```text
each value has exactly one defining occurrence

every use references an established definition

all executable control transfer is explicit

every block successor targets an established block

every value crossing a control-flow merge has an explicit incoming relation
```

Consumers are allowed to depend on these invariants.

This is a desirable dependency.

The dependency should be explicit and strong.

---

# 7. Equivalence Relation

The most important criterion for distinguishing IR levels is the **equivalence relation**.

The key question is:

> At this level, which two representations may be treated as the same meaning?

For example:

```text
for
```

and:

```text
iterator + loop
```

may be distinct in source syntax but equivalent at a lower semantic level.

Similarly:

```text
Foo
self::Foo
```

may differ in authored spelling but resolve to the same exact declaration.

Once resolution has established the exact declaration, that spelling distinction may no longer be semantically relevant downstream.

Therefore an IR level is defined more accurately by:

> which distinctions still matter semantically, and which distinctions may now be collapsed

than by:

> which fields are stored.

Abstractly:

```text
IR_L = representations / equivalence_L
```

This is a useful mental model.

---

# 8. Observable Surface

The IR Contract should define what consumers are allowed to observe.

For example:

```text
consumer may observe:
    operation meaning
    operands
    control-flow relation
    exact semantic reference
    provenance relation
```

Consumers must not derive meaning from:

```text
JVM object identity
physical table offset
arena address
allocation topology
hash bucket
row number
```

Without this boundary, physical implementation gradually acquires semantic authority.

---

# 9. Identity Law

Semantic identity and physical identity must be separated.

Examples:

```text
semantic IR identity
    ≠
physical row number

semantic identity
    ≠
JVM object identity

semantic identity
    ≠
hash value

semantic identity
    ≠
worker allocation order
```

HIDs, ordinals, and table indices may be lookup or storage accelerators.

They must not define meaning.

---

# 10. Mutation / Publication Law

If every consumer can mutate the IR directly, the representation hardens rapidly.

A dangerous structure is:

```text
node.children.add(...)
node.parent = ...
node.type = ...
```

being callable from arbitrary compiler code.

Instead, the IR Contract may define laws such as:

```text
published IR is immutable
```

or:

```text
mutation is allowed only through a sanctioned rewrite transaction
```

A concrete implementation may internally use:

```text
mutable construction
    ↓
verify
    ↓
seal
    ↓
publish
```

The physical mechanism remains replaceable.

---

# 11. The Real Criterion for Creating IR Levels

IR levels should not primarily be separated for optimization convenience.

They should be separated where **semantic distinctions change**.

Use the following questions when deciding whether a new IR level is justified.

## Question 1

Does the primitive semantic vocabulary change?

Example:

```text
source symbol
    ↓
exact semantic reference
```

If yes, this is a strong boundary candidate.

## Question 2

Can two representations previously treated as distinct now be treated as equivalent?

If yes, this is a lowering boundary candidate.

## Question 3

Does a new invariant become permanently established?

Example:

```text
all references resolved
```

If yes, this is a semantic checkpoint candidate.

## Question 4

Can some information from the previous level now be permanently forgotten?

Example:

```text
original syntactic sugar
```

If yes, this is a lowering candidate.

## Question 5

Does a consumer need to reinterpret the previous representation?

If yes, the current level's Contract is probably insufficient.

## Question 6

Is the change only a physical storage/layout change?

Example:

```text
Kotlin objects
    ↓
primitive slab
```

If so, it is not a new IR level.

## Question 7

Was only a new property proven?

Example:

```text
IR
+
Verified certificate
```

If so, an overlay is more appropriate than a new IR level.

---

# 12. Optimization Usually Does Not Create a New IR Level

Consider:

```text
Execution IR generation N
    ↓ equivalent transform
Execution IR generation N+1
```

Optimization should produce an equivalent realization at the same semantic level.

Therefore:

```text
Optimized IR
```

does not necessarily mean a new semantic IR level.

Usually it means:

```text
same IR Contract
different generation
```

---

# 13. Verification Usually Does Not Create a New IR Level

A useful model is:

```text
Published Realization Knowledge
    +
Verification Overlay
```

Verification may add the property:

```text
this realization is legal
```

without changing the underlying semantic vocabulary.

Therefore:

```text
Verified Realization
```

may be better represented as:

```text
same realization material
+
verification result / certificate
```

instead of a full second IR.

---

# 14. Lowering Itself Must Be a Contract

In ordinary compiler architecture, the arrow:

```text
L1
 ↓
L2
```

is often treated as an implementation function.

In a contract-oriented architecture, **the arrow itself is a Contract**.

A Lowering Contract should define:

```text
Accepted input invariant

Produced output invariant

Meaning-preservation relation

Which distinctions may be erased

Which information must remain

Failure / refusal conditions

Provenance preservation obligation

Determinism

Completion condition
```

The concrete lowering implementation should be a mechanically replaceable realization of this Contract.

---

# 15. Implementation Should Perform as Little Semantic Judgment as Possible

The intended structure is:

```text
semantic decision
    Contract

mechanical realization
    Implementation
```

Example:

L1 Contract:

```text
StructuredConditional
    condition
    thenRegion
    elseRegion
```

L2 Contract:

```text
Block
Branch
Merge
Value
```

L1 → L2 Lowering Contract:

```text
evaluate condition

transfer control to exactly one legal successor

preserve both region result relations

make merge value correspondence explicit
```

The concrete lowering implementation must not ask:

```text
What does this structured conditional mean?
```

That meaning has already been fixed by the Contract.

The implementation only realizes the declared movement into L2.

---

# 16. Example: Phi Nodes vs Block Arguments

Suppose the semantic requirement is only:

```text
incoming value correspondence at control-flow merge must be explicit
```

Then multiple implementations are possible.

V1:

```text
PhiNode
```

V2:

```text
Block Argument
```

Another implementation:

```text
parallel edge-transfer table
```

If the Contract says:

```text
there must be a PhiNode
```

then the implementation has been unnecessarily frozen.

A better Contract is:

```text
Every control-flow merge shall make incoming value correspondence
explicit and unambiguous.
```

The physical realization remains replaceable.

---

# 17. Formal View of IR Contract and Implementation

An IR level can be viewed abstractly as:

```text
L = (V, I, E, O)
```

where:

```text
V = semantic vocabulary
I = invariants
E = equivalence relation
O = observable surface
```

A concrete representation `R` must satisfy:

```text
R satisfies L
```

Similarly, a lowering relation can be represented as:

```text
T(L1 → L2)
```

and a concrete lowering implementation `t` must satisfy:

```text
t satisfies T(L1 → L2)
```

The architecture therefore becomes:

```text
L1 Contract
   ↑
R1-A
R1-B

T12 Contract
   ↑
T12-A
T12-B

L2 Contract
   ↑
R2-A
R2-B
```

This is the actual basis for replaceability.

---

# 18. IR Subsystem Structure

```text
┌─────────────────────────────────────────────┐
│                 IR SUBSYSTEM                │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │          CONTRACT PLANE             │    │
│  │                                     │    │
│  │  L1 Contract                        │    │
│  │       │                             │    │
│  │  L1→L2 Lowering Contract            │    │
│  │       │                             │    │
│  │  L2 Contract                        │    │
│  │       │                             │    │
│  │  L2→L3 Lowering Contract            │    │
│  │       │                             │    │
│  │  L3 Contract                        │    │
│  └─────────────────────────────────────┘    │
│                                             │
│  ───────────────────────────────────────    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │       IMPLEMENTATION PLANE          │    │
│  │                                     │    │
│  │  L1 Impl A / B                      │    │
│  │  Lowering 1→2 Impl A / B            │    │
│  │  L2 Impl A / B                      │    │
│  │  Lowering 2→3 Impl A / B            │    │
│  │  L3 Impl A / B                      │    │
│  │                                     │    │
│  │  storage / cache / slab / builder   │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

Other compiler subsystems connect to the Contract Plane:

```text
Optimizer ───────┐
Verifier ────────┤
Diagnostics ─────┼──→ IR Contract
Analysis ────────┤
Backend ─────────┤
Reference ───────┘
```

---

# 19. IR Subsystem Does Not Imply One Giant `ir/` Package

Treating IR as an architectural subsystem does not mean all IR-related code should live under:

```text
ir/
    everything
```

That would risk mixing ownership again.

A more natural organization is to place representations under their owning subsystem.

For example:

```text
semantic/
    representation/

realization/
    ir/

execution/
    ir/

backend/
    jvm/
        ir/
```

Therefore:

> IR is architecturally a subsystem, while each semantic or realization subsystem may own its own representation Contract and implementation.

---

# 20. JVM IR Must Also Separate Contract and Implementation

JVM IR clearly belongs to the implementation plane.

Even there, however, separate:

```text
JVM IR Contract
        ↑
        │ satisfies
        │
JVM IR Implementation
```

The JVM IR Contract may define:

```text
JVM execution vocabulary

legal invocation forms

stack/local relation

exception-region semantics

classfile-relevant type relation

legal control transfer
```

Concrete implementations may include:

```text
ASM Tree

custom primitive tables

streaming bytecode plan

direct classfile builder
```

V1 may use ASM-backed structures.

V2 may replace them with a custom compact JVM IR.

Upper layers should not need to change if both satisfy the same JVM IR Contract.

---

# 21. Execution IR May Have Multiple Implementations

Example:

## Reference Implementation

```text
simple immutable object graph
```

Goals:

```text
clarity
debuggability
independent checking
```

## Production Implementation

```text
dense SSA tables
primitive slabs
compact indices
```

Goals:

```text
throughput
memory locality
low allocation
```

If both satisfy the same Execution IR Contract, they can also support strong differential testing.

---

# 22. Candidate Top-Level Law for the Contract Plane

The strongest summary from the discussion is:

```text
Each IR level is defined first by an explicit contract of meaning,
invariants, observable relations, and accepted transitions.

Concrete representations do not communicate with one another directly.

Inter-level movement occurs only through declared lowering contracts.

Physical storage, traversal, indexing, construction, caching, and
lowering algorithms are replaceable realizations of those contracts
and do not acquire semantic authority.
```

In shorter form:

> Each IR level is first defined by a Contract of meaning, invariants, observable relations, and legal transitions.  
> Concrete IR implementations do not communicate directly.  
> Movement between levels occurs only through declared Lowering Contracts.  
> Storage, traversal, indexing, builders, caches, and lowering algorithms are replaceable realizations and do not acquire semantic authority.

---

# 23. Self-Similarity with Kontrakt's Existing Principles

Kontrakt already applies principles such as:

```text
authority separated from realization

consumer depends on published Contract

implementation replaceable behind Contract

no hidden authority in object topology

no implicit semantic inference from physical shape
```

The same laws should apply recursively to the compiler's IR subsystem.

Therefore:

```text
Kontrakt
    = contract-oriented compiler
```

and also:

```text
Kontrakt compiler architecture
    = internally contract-oriented architecture
```

---

# 24. Intentionally Limit What Becomes Frozen

Making every aspect of an IR fully replaceable may not be realistic.

A successful production compiler often benefits from a stable central IR semantic contract.

The realistic goal is:

```text
Stable:
    IR semantic contract

Replaceable:
    physical representation
    storage
    indexing
    builder
    traversal
    cache
    lowering implementation
```

Two important laws follow:

```text
Physical IR representation is not the IR semantics.

IR semantics is not Contract semantics.
```

---

# 25. Three-Layer Model

```text
Contract Meaning
        ↓ realized through
Logical IR Contract
        ↓ encoded by
Physical IR Implementation
```

Implementation flows downward.

Authority must not flow upward.

Therefore:

```text
Physical IR Implementation
    must not become
Logical IR semantic authority

Logical IR
    must not become
Contract meaning authority
```

---

# 26. ADR / Design / Implementation Separation

IR architecture should be documented at three distinct levels.

## ADR

Decide semantic contracts only.

Examples:

```text
all control transfer is explicit

all references are exact

merge value relation is explicit

no unresolved semantic reference remains

published representation is immutable to consumers
```

## Design

Decide logical schema and access model.

Examples:

```text
ValueRef
BlockRef
OperationView
RewriteTransaction
```

## Implementation

Decide physical representation.

Examples:

```text
Kotlin object graph

primitive arrays

arena

slab

ASM MethodNode

custom bytecode table
```

This prevents physical representation from becoming an architectural law.

---

# 27. IR-Level Decision Checklist

When proposing a new IR level:

```text
[ ] Does the semantic vocabulary actually differ from the previous level?

[ ] Does the equivalence relation change?

[ ] Does a new invariant become established?

[ ] Is there a distinction that may now be permanently erased?

[ ] Can consumers operate only on the new Contract without reinterpreting the previous level?

[ ] Are we accidentally calling a storage/layout change a new IR level?

[ ] Are we turning a verification property or analysis overlay into a new IR?

[ ] Are we turning an optimization generation into a new semantic IR level?
```

---

# 28. IR Contract Checklist

For each IR Contract:

```text
[ ] Meaning

[ ] Semantic vocabulary

[ ] Invariants

[ ] Equivalence relation

[ ] Observable surface

[ ] Identity law

[ ] Provenance relation

[ ] Construction law

[ ] Publication / mutation law

[ ] Consumer obligations

[ ] Forbidden inference from physical representation
```

---

# 29. Lowering Contract Checklist

For each transition between levels:

```text
[ ] accepted source invariant

[ ] required target invariant

[ ] preserved meaning relation

[ ] distinctions intentionally erased

[ ] information that must survive

[ ] provenance preservation

[ ] failure / refusal conditions

[ ] determinism

[ ] completion condition

[ ] target construction only through target Contract

[ ] direct dependency between source implementation and target implementation forbidden
```

---

# 30. Concrete Implementation Review Checklist

For each concrete IR implementation:

```text
[ ] Does semantic identity remain independent of object/row/index/hash identity?

[ ] Do consumers avoid direct access to internal fields/layout?

[ ] Is mutation restricted to an explicit owner?

[ ] Does the source IR implementation avoid direct knowledge of the target IR implementation?

[ ] Does the lowering implementation avoid inventing new semantic law?

[ ] Does an optimized hot-path view remain non-authoritative?

[ ] Do cache artifacts remain non-authoritative?

[ ] Can the physical implementation be replaced without rewriting IR Contract consumers?
```

---

# 31. Recommended Design Order

Do not begin with IR implementation classes.

Recommended sequence:

```text
1. Identify the semantic levels that are actually necessary.

2. Define the equivalence relation for each level.

3. Define the invariants for each level.

4. Define the observable surface.

5. Define the Lowering Contract between levels.

6. Restrict consumer dependencies to the Contract surface.

7. Only then choose the V1 physical implementation.

8. Implement an independent IR Contract verifier.

9. Implement an implementation-specific structural verifier.

10. Assume a V2 alternate representation and review replaceability.
```

---

# 32. Final Summary

The core result of this discussion is:

```text
IR is not merely a data structure.

IR is a compiler subsystem.

The IR subsystem itself must strictly separate
the Contract Plane from the Implementation Plane.

Each IR level is defined by a Contract of:
meaning,
invariants,
equivalence,
and observable surface,
not by a concrete node shape.

Concrete IR implementations do not communicate directly.

Movement between levels occurs only through Lowering Contracts.

A lowering implementation is not the semantic decision owner.
It mechanically realizes an already-declared source/target Contract relation.

Optimizer, verifier, diagnostics, backend, and analysis
consume IR Contracts rather than concrete IR implementations.

Physical storage, object graphs, slabs, tables, builders, caches,
and traversal strategies are replaceable realizations.

Semantic identity remains separate from physical identity.

Verification overlays and optimization generations should not be
promoted into new semantic IR levels without a genuine change in meaning.

JVM IR belongs to the implementation plane, but even JVM IR should
separate JVM IR Contract from JVM IR Implementation.

IR semantic contracts may intentionally stabilize,
while their physical representations remain replaceable.
```

The most compressed architecture law is:

> **IR meaning is owned by the Contract; IR physical shape is owned by the Implementation.  
> Concrete IR implementations do not communicate directly, and all movement between IR levels occurs through explicit IR and Lowering Contracts.  
> An implementation only realizes meaning already established by the Contract and never acquires semantic authority from its physical representation.**

---

# 33. Status

This document is a discussion summary and design proposal.

The following remain to be decided during actual IR architecture design:

```text
- the actual number of IR levels
- semantic vocabulary of each level
- equivalence relation of each level
- invariant set of each level
- detailed Lowering Contracts
- mutation / publication model
- consumer access surface
- V1 physical representations
- independent IR Contract verifiers
- JVM IR Contract
- V2 alternate representations
- incremental / persistent storage model
```
