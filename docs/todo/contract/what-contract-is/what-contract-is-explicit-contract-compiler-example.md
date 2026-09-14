# Explicit Contract as Compiler Knowledge

## Position

A rich Contract does not require a rich runtime representation.

When Contract meaning is explicit and separate from implementation, the compiler can know more before execution.

That knowledge may reduce runtime machinery.

```text
explicit Contract
    ↓
more compile-time knowledge
    ↓
less runtime uncertainty
    ↓
more specialization
```

## Why Separation Matters

When meaning is carried by classes, inheritance, virtual dispatch, runtime objects, or framework behavior, the compiler
must reconstruct that meaning from implementation.

This makes replacement harder.

It also makes analysis and optimization more conservative.

When Contract authority exists independently, the implementation shape is free to change.

> Meaning is explicit, therefore representation is replaceable.

## Compiler Example

A compiler may already know that:

```text
one Policy World is fixed
one Governance binding is applicable
one Version is selected
some State paths are unreachable
one Operation realization is exact
some judgments are already established
```

The backend may use that knowledge to remove physical work.

Possible results include:

```text
branch pruning
generic-path specialization
exact binding
devirtualization
redundant judgment removal
lookup removal
wrapper elimination
type specialization
reduced runtime-facing type inference
dense tables or primitive slabs
stage fusion
```

The Contract does not request these optimizations.

It establishes meaning.

The compiler derives legal optimization knowledge from that meaning.

## Representation Freedom

A semantic relation such as:

```text
Operation
    → Facts
    → Invariants
    → Policy
```

does not require an object graph with the same shape.

The backend may realize the same meaning as compact tables, dense ordinals, primitive slabs, or specialized bytecode.

The representation may change.

The Contract meaning must not.

## Core Point

```text
implicit meaning
    → implementation carries authority
    → compiler must infer more
    → runtime stays more generic

explicit rich Contract
    → authority is already established
    → compiler knows more
    → runtime machinery may become smaller
```

A rich Contract can therefore improve replaceability, analyzability, and optimization at the same time.