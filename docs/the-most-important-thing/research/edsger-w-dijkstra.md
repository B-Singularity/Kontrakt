# Dijkstra, State Space, and the Misapplied Physics Model

## Position

Dijkstra's state-space model is not accepted here as the definition of software machine state.

It may be useful as a reasoning model over program variables.

But it should not be mistaken for the ontology of a software machine.

The core objection is simple:

> Physics describes a world that already has state.  
> Software receives input and performs mappings.  
> These are not the same thing.

## Physics: State Describes the World

In physics, quantities such as position, momentum, field values, and energy distribution can be called state because
they describe the configuration of the physical world.

A physical system occupies a configuration.

The equations of physics are closer to constraints over lawful evolution than to active software machines.

They do not consume an input payload and produce an output object.

They describe what kinds of evolution are possible for a world that already has physical state.

```text
physics:
    state    = world configuration
    equation = constraint over lawful evolution
```

## Software: Input Is Domain Material

A software input is not a physical world configuration.

It is not a particle with position and momentum.

It does not move by itself.

It is domain material consumed by a machine.

An output is codomain material produced by a machine.

Neither input nor output is automatically machine state.

```text
software:
    input  = domain material
    output = codomain material
    state  = machine-owned transition condition
```

The mistake is to treat the domain of possible input values as if it were a physical state space.

A domain is only the set of values a machine may receive.

It is not the machine's state.

This mistake becomes destructive when software state is built as a Cartesian product of value domains:

```text
A × B × C × ...
```

Each added dimension multiplies the number of possible points.

That does not produce a better machine-state model.

It produces combinatorial value-space expansion.

A machine whose state is defined as the Cartesian product of all possible variable values cannot remain a
well-controlled machine.

Its state vocabulary is no longer explicit, small, named, or transition-governing.

This contradicts the idea of a good machine.

A good machine should reduce control to explicit states and legal transitions.

It should not inflate state into the full product of every possible data combination.

Therefore, treating Cartesian value space as machine state is not merely a harmless abstraction.

It makes the machine harder to control, harder to reason about, harder to recover, and harder to diagnose.

## Machine State Is Not Valuation Space

Machine state is not the set of all possible variable valuations.

Machine state is the condition owned by the machine that determines what the machine may legally do next.

A value becomes state only when the machine owns it, preserves it across a transition boundary, and uses it to determine
future transition legality.

A raw input value is not state.

A payload is not state.

A function argument is not state.

A temporary variable is not state.

A value becomes state only when it is accepted into the machine's own transition structure.

## Software as Composed Transitions

A software system is not one physical state-space equation.

It is better understood as a composition of mappings and transitions.

The simplest view is:

```text
input -> output
```

But a real software machine may carry internal state:

```text
(machine_state₀, input₁) -> (machine_state₁, output₁)
(machine_state₁, input₂) -> (machine_state₂, output₂)
(machine_state₂, input₃) -> (machine_state₃, output₃)
```

The inputs are consumed by transitions.

The state belongs to the machine performing those transitions.

## Internal State as Reflection, Not Raw Input

A software machine naturally has internal state.

But internal state is not the same thing as raw input.

Internal state is a machine-owned reflection of prior accepted transitions.

It records what the machine has accepted, rejected, normalized, sealed, committed, suspended, or failed.

A value becomes part of state only after the machine has interpreted it and incorporated it into its own transition
structure.

## Accepted Distinction

This contract theory adopts the following distinction:

```text
Physics:
    state    = physical world configuration
    equation = constraint over lawful evolution

Software:
    input   = domain material
    output  = codomain material
    machine = active mapping / transition structure
    state   = machine-owned condition governing legal transitions
```

## Final Judgment

Dijkstra's state-space model may be useful for program reasoning.

But it is not accepted as the state doctrine of a software machine.

State is not the Cartesian product of possible input dimensions or variable values.

Input belongs to the domain.

Output belongs to the codomain.

State belongs to the machine.