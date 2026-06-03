# Position on Meyer-Style Class-Centered Contracts

## Status

Rejected as a contract foundation.

Meyer-style Design by Contract contains one important insight: correctness should
not be debugged into a system after the fact. Contracts should be present in the
construction of software itself.

This principle is accepted.

The class-centered premise is rejected.

This document is not about any specific implementation framework. It defines a
contract-philosophical position that must remain valid even when implemented by
different tools, runtimes, languages, or platforms.

## Accepted Kernel

The following principle is acceptable:

> Software correctness should be built into the system structure, not treated as
> a late debugging outcome.

This aligns with a contract-first view of software.

Contracts should be explicit. They should be readable by software. They should
constrain execution. They should prevent invalid material from entering deeper
pipeline phases.

This principle is not the problem.

## Rejected Premise

The rejected premise is this:

> The class is the natural unit of contract.

This premise is rejected.

A class is not a contract unit. A class is a host-language implementation
artifact. At most, it is a frontend surface used to group implementation details
or attach contract declarations.

A class may implement a contract.

A class may expose syntax from which contract facts can be extracted.

A class may participate in a user-facing authoring model.

But a class is not the contract authority.

## Implementation and Contract Must Not Be Mixed

A class cannot be the authority of a contract because implementation and
contract must not be mixed.

A contract defines obligations.

An implementation attempts to satisfy those obligations.

These are different authority layers.

```text
contract
  -> defines obligations, boundaries, allowed behavior, forbidden behavior,
     invariants, state transitions, lifecycle rules, and conformance requirements

implementation
  -> materializes one possible way to satisfy those obligations
```

If a class becomes the contract authority, this direction is inverted.

```text
class
  -> defines what the contract is
```

That inversion is the core error.

The contract should judge the implementation. The implementation should not
become the judge of the contract.

A class may express contract syntax. It may implement a contract. It may be
inspected as a source-facing artifact. But it must not become the authority that
decides what the contract means.

This position identifies the **Implementation-as-Contract Anti-pattern**:

```text
Implementation-as-Contract Anti-pattern
  = treating an implementation artifact as the authority of a contract
    because it is the visible unit of code organization.
```

In class-centered systems, this often appears as:

```text
class
  -> routine
  -> precondition / postcondition
  -> class invariant
  -> object validity
```

This chain is rejected as a foundation.

The correct direction is:

```text
contract
  -> boundary operation
  -> invariant fact
  -> state transition law
  -> lifecycle rule
  -> implementation conformance
```

Once implementation and contract are mixed, the following failures follow:

- implementation shape becomes mistaken for contract meaning;
- fields and methods begin to look like obligations;
- object state begins to look like contract state;
- routine layout begins to look like behavioral law;
- inheritance begins to look like contract governance;
- class invariants begin to look like the natural form of invariant authority;
- refactoring implementation shape risks changing contract meaning;
- multiple implementations of the same contract become harder to reason about;
- contract conformance becomes confused with implementation identity.

These are symptoms of the same category error:

```text
implementation artifact != contract authority
```

Therefore, there is no need to refute each class-centered conclusion one by one.
If implementation and contract are mixed at the foundation, the model has
already crossed the wrong boundary.

Some local observations may still be useful. Preconditions, postconditions,
assertions, and invariants can still be valuable concepts. But those concepts
must be detached from class authority and re-owned by contract-level material:

```text
boundary operation
decision contract
invariant fact
state transition law
lifecycle rule
protocol law
implementation conformance rule
```

The contract-philosophical position is:

```text
Class
  = host-language implementation surface
  = possible contract authoring surface
  = possible contract implementer
  != contract authority
```

A class is a frontend artifact.

A contract is a system meaning.

Those two must not be collapsed.

## Frontend and Backend Separation

Object-oriented structures may be treated as frontend material.

```text
User-facing frontend:
  interface
  class
  method
  annotation
  DSL declaration

Canonical contract layer:
  boundary operation
  decision contract
  invariant fact
  state transition law
  lifecycle law
  protocol law

Mechanical backend:
  contract image
  static gate
  slot table
  offset table
  flag table
  deterministic execution plan
```

The object-oriented shape is erased.

The extracted contract facts are lowered into canonical contract material.

The canonical material may then be compiled into a mechanically predictable
execution substrate.

The user should not need to know the backend shape.

## What This Position Takes From Meyer

This position may take the following:

- correctness should be built into construction;
- contracts should be explicit;
- assertions are not merely debugging decorations;
- testing double-checks the result rather than creating correctness by itself;
- software should be structured so invalid states are blocked early.

This position does not take the following:

- class as the natural unit of contract;
- object instance as the natural unit of invariant authority;
- routine as the final unit of contract reasoning;
- inheritance as the primary mechanism of contract propagation;
- implementation shape as contract authority.

## Implementation Independence

A contract philosophy must not depend on the implementation that currently
embodies it.

A specific framework, runtime, compiler, library, or verification tool may
implement this philosophy, but it does not own the philosophy.

The implementation is a lowering target.

The philosophy is the source of authority.

Therefore, documents in this layer should avoid binding their claims to a
specific implementation name. They should describe contract meaning, contract
authority, boundary behavior, invariant ownership, state transition law,
lifecycle law, and implementation conformance in implementation-independent
terms.

A later implementation may compile these ideas into a particular runtime form,
but the contract meaning must remain valid without that implementation.

## Final Law

```text
A class may express a contract.
A class may implement a contract.
A class may be inspected to extract contract facts.

A class is not the contract.
A class does not own the contract.
A class is not the authority of the contract.

Implementation and contract must not be mixed.
```

Class-as-contract-authority is rejected.

The contract must exist as explicit canonical material independent of host
artifact shape.

The implementation must conform to that material.

Any theory that starts by making the class the basic unit of contract has already
mixed implementation with contract.