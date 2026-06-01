# Findler-Style Contracts as Rejected Contract Authority

## Position

Findler-style runtime contracts are not accepted as a foundation for this contract theory.

They may be useful only as a historical and contrastive reference.

The useful observation is limited:

> ordinary type systems and ordinary interface shapes are often unable to express the full obligations that real
> software boundaries require.

That observation is accepted.

The contract model that follows from it is rejected.

The core objection is this:

> Findler-style contracts place contract authority too close to runtime objects, classes, methods, getters, wrappers,
> subtype hierarchies, and implementation bodies.

That confuses contract authority with host-language implementation machinery.

A contract must not depend on a class body, a private field, a getter, a wrapper object, a subtype hierarchy,
inheritance, override, or a method-local runtime check.

A contract fact must be admitted, lowered, normalized, and published as immutable material.

Subtyping, inheritance, class structure, and object-oriented encapsulation may remain host-language implementation
techniques.

They are not contract authority.

## What Is Retained

### 1. Type shape is not enough

Findler-style contract work is useful where it shows that ordinary type systems cannot express many practical
obligations.

For example, a type may say:

```text
List<Number>
```

But the actual obligation may be:

```text
the list length must match the number of subwindows
each value must be positive
the values must sum to 1
```

That is not merely a type shape.

It is a relation over material.

This contract theory accepts the following point:

> type shape is not full contract.

A contract may describe:

- value relations;
- cross-field relations;
- cross-method relations;
- boundary admission rules;
- state transition legality;
- policy and governance constraints;
- failure classification;
- diagnostic obligations.

### 2. Interface shape is not enough

An interface may declare methods.

But method existence is not full contract meaning.

For example, an interface may expose two methods whose results must be mutually coherent.

The method signatures alone do not express that coherence.

This contract theory accepts the following point:

> interface shape is not full contract.

An interface may be a frontend surface for contract authoring.

It must not become contract authority by itself.

### 3. Contract duplication is a real problem

Subtyping and inheritance partially address a real pressure:

> many contracts share repeated obligations, while only a small number of obligations differ.

That pressure is real.

The solution is not inheritance.

The solution is contract segmentation and composition.

Repeated obligations should be separated into explicit contract clauses.

Different obligations should remain separate.

A concrete contract surface should be formed by composing the clauses it requires.

The accepted model is:

```text
A = shared obligation clause
B = separate obligation clause
C = separate obligation clause

Use case 1 = A + B
Use case 2 = A + C
```

The rejected model is:

```text
B extends A
C extends A
```

A clause does not inherit another clause.

A clause may be composed with another clause.

If two clauses conflict, the composition is invalid.

If two clauses do not conflict, the result is simply a larger contract.

## What Is Rejected

### 1. Runtime-embedded contract checks are not contract authority

Findler-style systems often place the contract check inside or immediately around the runtime function or object being
checked.

This turns the contract into an implementation-adjacent runtime assertion.

That is rejected.

The rejected structure is:

```text
implementation body {
    contract check
    real work
}
```

or:

```text
method call
-> runtime wrapper
-> contract check
-> implementation body
```

The accepted structure is:

```text
declared contract surface
-> guard
-> lowering
-> canonical material
-> verification obligation
-> runtime enforcement projection
-> implementation body
```

A runtime check may enforce a contract.

It must not be the source of contract authority.

### 2. A wrapper object is not contract material

Wrapping a primitive value in a class in order to attach contracts is rejected.

The problem is not that values can never participate in contracts.

The problem is treating the wrapper object as the contract-bearing authority.

A primitive numeric value is material.

A wrapper object adds host-language machinery:

- object identity;
- reference semantics;
- nullable reference possibility;
- allocation;
- method dispatch;
- private fields;
- getters;
- framework interception.

This machinery is not contract meaning.

The rejected move is:

```text
primitive value
-> wrapper object
-> method contract
```

The accepted move is:

```text
raw value
-> boundary guard
-> admitted numeric material
-> operation relation contract
-> primitive/mechanical runtime representation
```

For example, the contract of square root is not a `Float` wrapper class.

The contract is the relation:

```text
input x:
    finite
    positive

output r:
    finite
    non-negative
    approximately r * r = x within declared epsilon
```

The operation relation may be contractual.

The wrapper object is not.

### 3. Value is not contract meaning

A value is not a contract.

A value is material judged by a contract.

There may be contracts that are made only of value conditions.

For example:

```text
amount >= 0
ratio >= 0
ratio <= 1
sum(percentages) == 1
```

But even here, the value itself is not the contract.

The contract is the law that determines whether accepted material is valid.

The distinction is:

```text
value = material
contract = law over material
```

A value becomes contract material only after it is admitted, classified, normalized, and governed by a contract.

The rejected move is:

```text
value has contract meaning by itself
```

The accepted move is:

```text
value is admitted into a contract-governed material category
```

### 4. Getter is not contract fact

A getter may look like a fact projection.

But a getter is still a method.

It may hide:

- lazy computation;
- cache lookup;
- normalization;
- allocation;
- exception;
- mutation;
- I/O;
- time dependency;
- proxy dispatch;
- framework interception.

Therefore, a getter result must not be treated as contract authority.

The rejected structure is:

```text
private field
-> getter
-> contract fact
```

The accepted structure is:

```text
raw source
-> boundary guard
-> lowering
-> normalization
-> immutable fact publication
```

A contract fact is emitted.

It is not fetched.

```text
Fact is emitted, not fetched.
```

A getter may be a host-language projection surface.

It must not be the source of contract truth.

### 5. Private is not a contract boundary

`private` is an implementation hygiene mechanism.

It may reduce accidental source-level access.

It may help ordinary encapsulation.

It may protect representation from normal callers.

But it is not a semantic contract boundary.

It is not a security boundary.

It is not sufficient protection against:

- reflection;
- module opens;
- method handles;
- unsafe access;
- proxy/AOP interception;
- ORM field access;
- serialization frameworks;
- mocking frameworks;
- bytecode rewriting;
- instrumentation agents;
- same-process hostile code.

Therefore, this contract theory rejects private state as contract authority.

The accepted rule is:

```text
Private may protect implementation representation.
Private does not define contract truth.
```

Implementation may use private fields and getters internally.

A contract must not trust them.

### 6. Null is not contract meaning

`null` is not contract meaning.

`null` is unclassified absence at a boundary.

It may mean:

- not found;
- not initialized;
- unknown;
- invalid;
- unavailable;
- not applicable;
- not yet computed;
- rejected;
- failed.

These meanings are not the same.

A contract system must not preserve `null` as semantic authority.

The accepted structure is:

```text
raw null
-> boundary guard
-> rejected failure
```

or:

```text
raw null
-> boundary guard
-> explicit absence material
```

or:

```text
raw null
-> boundary guard
-> defaulted canonical value
```

After guard and lowering, canonical material should be explicit, non-null, and classified.

### 7. Primitive wrapper is host-language interop, not contract authority

Primitive wrappers exist because host languages often have two worlds:

```text
primitive values
reference objects
```

Wrappers adapt primitive values into the reference/object world.

That may be necessary for:

- generic collections;
- reflection;
- framework binding;
- serialization;
- nullable references;
- object-based APIs.

But the wrapper is not contract material.

It is host-language interop machinery.

The accepted structure is:

```text
wrapper / nullable host value
-> boundary guard
-> explicit canonical material
-> primitive/mechanical runtime representation
```

Wrappers may appear at the boundary.

They must not survive as contract authority.

### 8. Object-oriented encapsulation is not contract protection

Findler-style contracts remain too close to object-oriented encapsulation.

They often rely on the idea that an object owns private state and exposes methods that can be guarded.

This theory rejects that as the center of contract meaning.

A contract is not protected by hiding fields and exposing getters.

A contract is protected by:

- boundary guard;
- lowering;
- normalization;
- canonical material;
- immutable publication;
- verification;
- failure classification;
- diagnostics.

Object-oriented encapsulation may remain as an implementation technique.

It is not the contract doctrine.

### 9. Class is not contract authority

A class must not be treated as contract authority.

A class is an implementation carrier.

It may carry fields, methods, constructors, dispatch behavior, lifecycle hooks, framework annotations, metadata, and
private representation.

Those are implementation mechanisms.

They are not contract truth.

The rejected move is:

```text
class
= contract authority
```

or:

```text
contract is embedded in class implementation
```

That is not accepted.

Conceptually, a contract should be expressed in a unit that is not a class.

A contract should have its own unit of meaning:

```text
contract clause
contract surface
contract material
contract law
contract composition
```

A class may realize a contract.

A class may expose a frontend surface from which contract candidates are acquired.

A class may carry host-language syntax used to author immutable fact declarations.

But a class must not define contract meaning by itself.

The accepted distinction is:

```text
contract
    system obligation

class
    host-language implementation carrier

canonical contract material
    software-readable contract authority

runtime class
    possible realization of that authority
```

A class has responsibility.

It does not have authority.

### 10. Class-based fact declarations are tolerated only as host-language surfaces

Most host languages do not provide a native contract unit.

Many of them use `.class`, class metadata, interface declarations, records, annotations, or object members as their
basic units.

That reality is acknowledged.

On such platforms, immutable fact contracts may have to be authored through class-shaped or interface-shaped syntax.

That is tolerated only as a frontend surface.

The accepted use is:

```text
class/interface/record surface
-> immutable fact declaration candidates
-> guard
-> lowering
-> canonical contract material
```

The rejected use is:

```text
class body
-> implementation code
-> algorithm
-> contract authority
```

If a host class is used as a contract authoring surface, it must express immutable facts only.

It must not contain implementation algorithms as contract meaning.

It must not turn methods, private fields, getter bodies, inheritance, or override into contract authority.

The class-shaped surface must be erased during lowering.

The resulting authority must be backend-erased, immutable, explicit, and contract-owned.

The accepted rule is:

```text
Host classes may be used to write immutable fact surfaces.
Host classes must not become contract authority.
```

### 11. Subtyping is not needed as contract doctrine

Subtyping is not contract preservation.

More strongly, subtyping is not needed as a contract doctrine.

A subtype relation may provide host-language convenience:

```text
callable compatibility
generic collection compatibility
framework integration
polymorphic dispatch
maintenance convenience
```

But none of these are contract authority.

Subtyping exists mainly because object-oriented languages wanted to place different implementations behind one apparent
type surface.

That is a host-language maintenance technique.

It is not a contract-theoretic necessity.

The rejected move is:

```text
B is a subtype of A
therefore B preserves the contract of A
```

But even the weaker move is rejected:

```text
B is a subtype of A
therefore subtyping is the right way to express contract variation
```

That also does not follow.

A contract does not need a subtype hierarchy in order to express shared obligations or different obligations.

Repeated obligations should be separated into explicit contract clauses.

Different obligations should remain separate.

A concrete contract surface should be formed by composition:

```text
A + B
A + C
```

not by subtype hierarchy:

```text
B extends A
C extends A
```

Subtyping also carries mechanical cost when it becomes runtime authority.

It encourages:

```text
virtual dispatch
interface dispatch
megamorphic call sites
heterogeneous object collections
object pointer chasing
branch prediction failure
cache locality loss
```

These costs are not merely optimization details.

They are symptoms of the same conceptual error:

```text
contract meaning is being routed through object identity and runtime type hierarchy
```

This contract theory rejects that path.

Subtyping may be tolerated only as a host-language frontend artifact.

It may appear at the boundary because the host language already uses subtype relations, generics, framework proxies, and
class metadata.

But it must be erased before canonical contract authority is formed.

The accepted move is:

```text
host subtype surface
-> acquisition
-> role classification
-> contract clause extraction
-> contract composition
-> canonical contract material
-> primitive/mechanical runtime representation
```

The rejected move is:

```text
host subtype hierarchy
-> contract authority
```

The final rule is:

```text
Subtyping is not contract preservation.
Subtyping is not contract variation.
Subtyping is not contract reuse.
Subtyping is host-language machinery.

Contracts are segmented and composed.
They are not subtyped.
```

### 12. Inheritance is not contract inheritance

Inheritance is not contract inheritance.

Inheritance is primarily an implementation reuse mechanism.

It may reuse fields, methods, initialization order, protected hooks, and internal representation.

That does not make it semantic contract inheritance.

The rejected move is:

```text
Child extends Parent
therefore Child inherits Parent's contract meaning
```

This contract theory rejects that inference.

Inheritance mixes several unrelated concerns:

- code reuse;
- field reuse;
- method reuse;
- subtype relation;
- dynamic dispatch;
- state inheritance;
- behavior inheritance;
- substitutability expectation.

A contract system must not infer semantic validity from that mixture.

Implementation reuse is not semantic obligation.

### 13. Override is not contract refinement

Override is an implementation mechanism.

It replaces or specializes behavior in a subtype.

Since contract meaning must not depend on implementation bodies, override cannot be the basic mechanism for contract
refinement.

The rejected move is:

```text
override method
therefore refine inherited contract
```

That is not accepted.

If a contract must be changed or extended, the change must be expressed as contract composition.

It must not be hidden inside an override.

Contract meaning has no override.

Contract meaning has:

- independent clauses;
- composition;
- conflict detection;
- obligation preservation;
- canonical lowering.

### 14. Contract duplication must not be solved by inheritance

Inheritance tries to remove duplicated obligations by creating hierarchy.

This contract theory rejects hierarchy as the solution.

Repeated obligations must be separated into explicit contract clauses.

Different obligations must remain separate.

A contract surface is produced by composition, not inheritance.

There is no parent contract that gives birth to a child contract.

There are only clauses that can be composed.

Example:

```text
CanonicalIdentityClause
VersionCoherenceClause
PersistenceEligibilityClause
PublicVisibilityClause
```

A repository save surface may require:

```text
CanonicalIdentityClause
+ VersionCoherenceClause
+ PersistenceEligibilityClause
```

An API response surface may require:

```text
CanonicalIdentityClause
+ PublicVisibilityClause
```

Neither surface inherits from the other.

Neither clause overrides another.

They are composed.

If the composed clauses conflict, the composition is rejected.

If they do not conflict, the contract simply has more obligations.

### 15. Inheritance damages mechanical clarity

Inheritance and subtyping also damage runtime mechanical clarity when they become hot-path authority.

They encourage:

- virtual dispatch;
- interface dispatch;
- megamorphic call sites;
- heterogeneous object collections;
- reference chasing;
- object header dependency;
- hidden state layout;
- fragile base-class coupling.

This is not merely an optimization concern.

It affects contract authority because the meaning of a contract becomes entangled with dynamic dispatch, object
identity, and implementation hierarchy.

Host-language hierarchy may be tolerated at cold frontend boundaries.

It must not become canonical contract material or hot-path contract authority.

The accepted move is:

```text
host subtype / inherited object
-> acquisition
-> role classification
-> canonical contract material
-> primitive/mechanical runtime representation
```

The rejected move is:

```text
host subtype hierarchy
-> contract authority
```

## Accepted Replacement Model

The accepted model is:

```text
Raw host material
    DTO
    object
    class surface
    class body
    wrapper
    nullable value
    getter surface
    framework entity
    proxy object
    subtype instance
    inherited implementation

        ↓

Boundary Contract
    admission
    rejection
    null classification
    wrapper erasure
    direction check
    version check

        ↓

Role Classification
    fact
    projection
    boundary
    transition
    operation
    policy
    diagnostic

        ↓

Contract Segmentation
    independent contract clauses
    repeated obligations factored out
    no inheritance
    no override

        ↓

Contract Composition
    required clauses selected
    conflicts detected
    obligations preserved

        ↓

Lowering Contract
    backend erasure
    normalization
    canonical naming
    identity assignment
    semantic preservation
    host-object removal
    class-shape removal
    hierarchy removal

        ↓

Canonical Contract Material
    explicit
    immutable
    non-null
    backend-erased
    syntax-independent
    contract-owned
    hierarchy-free
    class-authority-free

        ↓

Mechanical Runtime Representation
    primitive ids
    flat tables
    arrays
    offsets
    counters
    bitsets
    bounded diagnostics
```

The contract authority begins only after guard, role classification, segmentation, composition, lowering, normalization,
and canonical publication.

## Final Judgment

Findler-style contracts are retained only as a limited historical reference.

Accepted:

1. type systems are often too weak to express real software obligations;
2. interface shape is not full contract;
3. relational obligations between values, methods, and boundaries matter;
4. contract duplication is a real problem;
5. class-shaped syntax may be tolerated as a host-language frontend for immutable fact declarations.

Rejected:

1. runtime-embedded contract checks as contract authority;
2. primitive wrappers as contract material;
3. values as contract meaning;
4. getters as contract facts;
5. private fields as contract boundaries;
6. null as semantic contract material;
7. object-oriented encapsulation as contract protection;
8. method-local checks as the center of contract;
9. classes as contract authority;
10. implementation bodies as contract subjects;
11. algorithms inside classes as contract meaning;
12. subtyping as contract preservation;
13. inheritance as contract inheritance;
14. override as contract refinement;
15. hierarchy as the solution to duplicated obligations.

The final judgment is:

> Findler shows that ordinary type/interface shape is insufficient.  
> Findler does not provide the contract authority model accepted here.

This contract theory keeps the need for richer obligations.

It rejects the placement of contract authority inside classes, wrappers, getters, private fields, object methods,
runtime assertion surfaces, subtype hierarchies, and inherited implementations.

Contracts are not classes.

Contracts are not inherited.

Contracts are segmented, composed, lowered, and published as canonical material.