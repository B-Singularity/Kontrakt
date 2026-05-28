# Hehner as Rejected Contract Authority

## Position

Hehner is rejected as an authority for this contract theory.

Hehner may be useful only as a contrastive reference: an example of a direction this contract theory does not adopt.

## Reason 1: Mathematical rigor requires more than private notation

Mathematical rigor is not created by inventing symbols.

Mathematical rigor depends on definitions, theorems, and proofs that are accepted, checked, and validated within the
relevant mathematical community or formal discipline.

If a theory introduces its own symbolic system, the important question is not whether the symbols look formal.

The important question is:

> Has this symbolic system and theory been rigorously proven, validated, and accepted by mathematicians or by the
> relevant formal-methods community?

If not, then the notation itself does not provide mathematical rigor.

Author-specific notation is not accepted as contract authority.

A contract theory intended to apply across languages, platforms, and systems must not depend on one author's private
symbolic vocabulary.

## Reason 2: Existing mathematics should be reused where possible

The normal method of bringing mathematics into engineering is to reuse the appropriate existing mathematical domain.

Examples include:

- set theory,
- logic,
- type theory,
- automata theory,
- algebra,
- order theory,
- graph theory,
- proof theory,
- model theory,
- temporal logic.

If the goal is mathematical precision, the correct approach is to use the established definitions, symbols, theorems,
and proof methods of the relevant field.

Inventing a private notation system creates unnecessary translation cost.

It also weakens readability, because readers familiar with mathematics must first relearn the author's notation before
evaluating the actual idea.

This contract theory rejects that approach.

It should use established mathematical language where possible, and introduce new terms only when the system domain
genuinely requires them.

## Reason 3: Readability matters

A notation system that forces unnecessary memorization is a bad engineering surface.

A notation may be internally consistent, but if it blocks readers from using the mathematical knowledge they already
have, it becomes a burden.

For this contract theory, clarity is not optional.

A contract doctrine must be readable by people who already understand software, systems, mathematics, or formal
reasoning.

It must not require acceptance of a private symbolic language before the core argument can be understood.

## Reason 4: Complexity constraints are not contract meaning

This contract theory does not accept time complexity or space complexity as contract meaning.

A contract must describe stable system obligations.

A contract must not bind itself to replaceable implementation properties.

The following do not belong to contract meaning:

- algorithm choice,
- data structure choice,
- time complexity,
- space complexity,
- infrastructure strategy,
- runtime topology,
- cache strategy,
- threading model,
- storage layout.

These are implementation concerns or engineering evaluation concerns.

They may be measured.

They may be optimized.

They may be governed by runtime policy.

But they must not become the invariant meaning of the contract.

## Resource governance is different from complexity-as-contract

A system may define resource governance.

For example:

- a run must fail closed when a resolved budget is exceeded;
- an operation must remain within a declared resource envelope;
- a verification process must be bounded by policy;
- diagnostics must be size-bounded.

These are governance contracts.

But this is different from saying:

- the contract requires a specific algorithm;
- the contract requires `O(log n)`;
- the contract requires a specific memory complexity;
- the contract requires a specific data structure.

Resource governance defines allowed execution envelopes.

Algorithmic complexity describes implementation behavior.

This contract theory keeps these separate.

## System philosophy has priority over complexity constraints

A system is built around a hierarchy of values.

Different systems may prioritize different things:

- determinism,
- reproducibility,
- safety,
- auditability,
- throughput,
- latency,
- availability,
- simplicity,
- portability,
- fault containment,
- or operational predictability.

Time complexity and space complexity are important engineering concerns, but they are not always the highest concern.

A system may rationally choose an implementation with worse time complexity or worse space complexity if that
implementation better preserves the system's primary philosophy.

For example, a system may choose:

- a slower implementation because it is more deterministic;
- a larger memory footprint because it provides better auditability;
- a less asymptotically optimal structure because it is easier to verify;
- a more expensive canonicalization step because it preserves stable identity;
- a simpler bounded algorithm because it is safer under failure.

This is not a defect.

This is a trade-off made according to the system's governing philosophy.

Therefore, time complexity and space complexity must not be written into the contract as invariant obligations.

If they are written into the contract, they can incorrectly become superior to the system's actual purpose.

A contract must preserve what the system is meant to be.

It must not force every implementation to worship time and space complexity as the highest value.

## Final Judgment

Hehner is rejected as a foundation for this contract theory.

Reasons:

1. Hehner introduces author-specific notation instead of relying directly on established mathematical notation.
2. Mathematical rigor is not guaranteed by private symbolic formalism.
3. The notation creates unnecessary readability cost.
4. The theory's inclusion of time and space complexity as specification material conflicts with the separation between
   contract and implementation.
5. This contract theory defines contract as stable system obligation, not as algorithmic or complexity-bound
   implementation behavior.
6. Time complexity and space complexity are trade-off dimensions, not contract meaning.

Therefore:

> Hehner is not contract authority.

Hehner may be retained only as a rejected or contrastive reference.