# Kontrakt Verifier Candidate Implementation Plan

## 0. Status

This document is not the current Kontrakt architecture.

It is a candidate verifier implementation plan derived from the current Kontrakt direction:

```text
contract authority
-> canonical material
-> deterministic lowering
-> frozen publication
-> explicit lifecycle state
-> bounded physical substrate
-> diagnosable failure
```

The authoritative architecture remains the ADR/design-document model around acquisition, lowering, deterministic
identity, physical substrate, memory envelope, freezing, publication, lifecycle state, and diagnostic material.

The verifier design below should be treated as an implementation candidate, not as a replacement for the existing
architecture.

The important correction is this:

```text
Contract Image
Reference VM
Generated Static Gate
```

are not already accepted Kontrakt architectural primitives.

They are candidate implementation mechanisms for a future verifier.

The Kontrakt-native wording is:

```text
Frozen Canonical Contract Material
Verifier / Judgment Projection
Generated Enforcement Projection
Explicit Runtime Lifecycle Gate
Evidence / Diagnostic Material
```

---

## 1. Position

Kontrakt must not verify contracts by trusting:

```text
objects
classes
getters
private fields
subtype hierarchies
inheritance
runtime proxies
higher-order wrappers
recursive control flow
implementation bodies
```

The verifier must be built around Kontrakt-owned material.

The core direction is:

```text
Contract Frontend Surface
-> Acquisition
-> Lowering
-> Canonical Contract Material
-> Frozen / Sealed Contract Material
-> Verifier Projection
-> Runtime Enforcement Projection
-> Evidence / Diagnostic Material
```

This document may use the shorter term:

```text
Frozen Contract Material Image
```

to mean the physically frozen, canonical, verifier-readable material form.

That term is an implementation convenience.

It must not be understood as a new source of contract authority separate from Kontrakt's canonical material model.

---

## 2. Core Rule

```text
Implementation may compute.
Kontrakt decides whether the result may exist as accepted material.
```

A contract is not proven by finding an `if` statement inside implementation code.

A contract such as:

```text
requires x > 1
```

is enforced by a Kontrakt-owned boundary gate.

The implementation must only receive material that has already passed that gate.

The verifier does not ask:

```text
Does the implementation contain the condition x > 1?
```

The verifier asks:

```text
Can any value that violates x > 1 reach the implementation through the Kontrakt-controlled path?
```

If the answer is no, the boundary contract is enforced for that path.

---

## 3. Non-Goals

The verifier must not become a Findler-style runtime monitor.

The first implementation must not try to do the following:

```text
1. Prove arbitrary implementation code correct.
2. Analyze arbitrary JVM bytecode to discover whether the implementation contains contract checks.
3. Use runtime proxies as the main enforcement model.
4. Use higher-order functions as contract material.
5. Use recursion as core machine control.
6. Use class hierarchy as contract structure.
7. Use Kotlin value classes as hot-path authority.
8. Use object graphs as canonical contract representation.
9. Start with SIMD, ASM generation, or Disruptor as the baseline.
10. Treat generated JVM classes as contract authority.
11. Treat CI-only verification as sufficient for live boundary input.
12. Treat runtime fail-closed as process shutdown.
```

These may appear as substrate, tooling, or later optimization targets.

They must not define contract authority.

---

## 4. Kontrakt-Aligned Verifier Architecture

### 4.1 Native lifecycle

The verifier should align with the existing Kontrakt lifecycle:

```text
Frontend Contract Surface
    interface / DSL / annotation / document-derived syntax

        ↓

Acquisition
    collect contract candidates from host-language or external frontend surfaces

        ↓

Lowering
    preserve only contract-authoritative facts
    reject or ignore non-authoritative host artifacts
    remove frontend syntax authority
    eliminate class-shaped authoring form after fact extraction
    refuse closure, proxy, inheritance, subtype hierarchy, getter body, private field, and implementation body as contract material

        ↓

Canonical Contract Material
    LoweredContractFact
    contract clause
    boundary fact
    invariant fact
    operation fact
    transition fact
    policy fact
    diagnostic fact

        ↓

Frozen / Sealed Contract Material
    deterministic identity
    stable ordering
    immutable publication material
    memory envelope checked
    acquisition-order independence preserved

        ↓

Verifier Projection
    predicate judgment
    transition judgment
    policy judgment
    lowering-preservation judgment
    publication judgment

        ↓

Runtime Enforcement Projection
    generated boundary gate
    generated operation gate
    transition table
    policy ledger
    publication gate

        ↓

Evidence / Diagnostic Material
    compact primitive evidence
    cold diagnostic rendering
```

### 4.2 Candidate implementation names

The following names are implementation candidates:

```text
Frozen Contract Material Image
    candidate physical representation of frozen canonical contract material

Reference Judgment Machine
    candidate deterministic semantic oracle for predicate/transition/policy judgment

Generated Static Gate
    candidate optimized enforcement projection generated from frozen material

Predicate IR
    candidate closed instruction vocabulary for simple guard/postcondition predicates
```

These terms must remain subordinate to the Kontrakt canonical material model.

---

## 5. Verification Layers

Kontrakt should split verification into separate layers.

### 5.1 Contract material verification

This verifies that the contract material itself is well-formed.

Examples:

```text
all referenced fields exist
all referenced states exist
all referenced failures exist
all transition source and target states exist
all policy units are declared
all diagnostic templates are known
all contract clauses are named deterministically
```

This does not require implementation objects.

### 5.2 Composition and consistency verification

This verifies that composed contract clauses do not conflict.

Examples:

```text
Clause A: amount >= 0
Clause B: amount must be absent
=> invalid composition

Boundary emits CreateOrderCommand
Operation requires PayOrderCommand
=> disconnected contract path

State Cancelled is terminal
Transition Cancelled -> Paid exists
=> invalid transition table
```

This is contract-level verification.

### 5.3 Lowering preservation verification

This verifies that frontend meaning survives lowering.

Questions:

```text
Was semantic identity preserved?
Was ordering law preserved?
Was failure vocabulary preserved?
Was diagnostic evidence preserved?
Was policy budget preserved?
Were host artifacts erased?
Is the lowered representation deterministic?
Is acquisition-order independence preserved?
```

Host artifacts to erase:

```text
class
getter
private field
wrapper
proxy
closure
subtype hierarchy
inheritance
higher-order function
```

### 5.4 Frozen publication verification

This verifies that canonical material can be safely frozen and published.

Questions:

```text
Is the material immutable after freeze?
Is the identity stable?
Is the ordering deterministic?
Is the memory envelope respected?
Can published consumers observe only fully sealed material?
Are partial publications impossible?
```

### 5.5 Enforcement projection verification

This verifies that generated runtime gates/tables match the canonical material.

Examples:

```text
Reference judgment says x <= 1 is rejected.
Generated boundary gate must also reject x <= 1.

Reference judgment says Created + Pay -> Paid.
Generated transition table must also return Paid.

Reference policy says budget exhaustion produces ResourceExhausted.
Generated policy ledger must also produce ResourceExhausted.
```

### 5.6 Contract realization conformance verification

This verifies whether a concrete implementation realizes the contract obligations declared by a contract surface, such
as an interface contract or operation contract.

The implementation class is not the owner of the contract.

The implementation class is not contract authority.

The contract authority remains in canonical contract material.

The implementation is only a realization candidate.

Kontrakt verifies the candidate through Kontrakt-controlled gates.

It does not inspect implementation internals as contract authority.

It does not treat implementation methods, class bodies, private fields, getter bodies, inheritance, or override as the
source of contract meaning.

The verification question is not:

```text
Does this implementation class define the contract?
```

The verification question is:

```text
Given the interface / operation contract material,
does this implementation realization satisfy the declared obligations through the Kontrakt-controlled path?
```

Flow:

```text
interface / operation contract material
-> admitted input material
-> precondition gate
-> implementation invocation
-> output material
-> postcondition gate
-> invariant gate
-> publication gate
-> conformance evidence
```

The implementation may compute.

Kontrakt decides whether the computed output satisfies the interface / operation contract and may be published.

A passing implementation is not a new contract authority.

It is only evidence that the implementation conforms to the contract material for the verified paths.

---

## 6. Runtime Versus CI/Test

Kontrakt should not be CI-only.

Kontrakt should not be Spring-style always-on proxy monitoring either.

The correct split is:

```text
CI / build / test:
    heavy verification

runtime:
    minimal generated enforcement kernel
```

### 6.1 CI / build verification

Run in CI or build-time:

```text
contract material well-formedness
composition consistency
lowering preservation
frozen material validation
memory envelope validation
generated gate differential verification
state transition coverage
policy exhaustion checks
implementation conformance witnesses
```

### 6.2 Test mode

Run in tests:

```text
generated witness cases
boundary negative cases
postcondition violation cases
illegal transition cases
policy exhaustion cases
diagnostic rendering checks
```

### 6.3 Runtime enforcement

Keep runtime enforcement small:

```text
boundary gate
operation precondition gate
essential postcondition gate
transition table gate
policy ledger gate
publication invariant gate
compact evidence append
```

Do not use runtime proxy chains.

Do not use reflection-driven annotation lookup in the hot path.

Do not use higher-order contract wrappers.

### 6.4 Runtime fail-closed scope

Fail-closed must not mean process shutdown.

Fail-closed means:

```text
invalid material cannot be admitted
invalid transition cannot occur
invalid output cannot be published
exhausted execution cannot continue
```

The default failure scope is the smallest safe boundary:

```text
material
request
operation
transition
publication
route / contract image quarantine
```

Process-level fail-stop is reserved only for engine integrity faults:

```text
contract image digest mismatch
generated gate mismatch
transition table corruption
invalid table offset
unknown opcode in trusted frozen material
material arena corruption
evidence system corruption that prevents safe diagnosis
```

---

## 7. Frozen Contract Material Image

The Frozen Contract Material Image is a candidate physical representation of canonical material.

It should be table-oriented.

It must not be an object graph.

It must not be a class hierarchy.

It is a frozen, contract-owned material substrate.

### 7.1 Candidate tables

```text
SchemaTable
    material schema id
    slot ranges
    primitive kind information

SlotTable
    slot id
    slot kind
    offset
    normalization rule id

PredicateProgramTable
    program id
    opcode range
    operand range
    failure id

BoundaryPlanTable
    boundary id
    input schema id
    output schema id
    extractor plan id
    guard program ids
    lowering plan id

OperationPlanTable
    operation id
    input schema id
    output schema id
    precondition program ids
    postcondition program ids
    implementation binding id

TransitionTable
    state id
    event id
    next state id
    rejection failure id

PolicyTable
    policy id
    budget kind
    initial budget
    debit rule id
    exceeded failure id

FailureTable
    failure id
    category
    severity
    diagnostic template id

DiagnosticTable
    stable diagnostic id
    evidence field layout
    cold rendering metadata
```

### 7.2 Physical representation target

Cold compiler code may use structured objects.

Hot runtime must not depend on object graphs.

Target runtime form:

```text
int[]
long[]
byte[]
short[]
offset tables
range tables
bitsets
```

Design rule:

```text
cold compiler may use objects
hot runtime must use primitive frozen material
```

---

## 8. Predicate IR as Candidate Judgment Form

Predicate IR is a candidate closed representation for simple contract predicates.

It should be small, deterministic, and non-extensible by arbitrary host functions.

Example source:

```text
requires x > 1
ensures result > x
```

Lowered predicate form:

```text
PredicateProgram XGreaterThanOne:
    LOAD_I32 slot(x)
    CONST_I32 1
    GT_I32
    REQUIRE_TRUE failure(X_MUST_BE_GREATER_THAN_1)
```

Physical form:

```text
opcodes:
    [LOAD_I32, CONST_I32, GT_I32, REQUIRE_TRUE]

operands:
    [slot_x, 1, 0, failure_id]
```

### 8.1 Initial opcode set

Keep v1 deliberately small.

```text
LOAD_I32
LOAD_I64
LOAD_BOOL
LOAD_FLAG
CONST_I32
CONST_I64
CONST_BOOL

EQ_I32
NE_I32
GT_I32
GE_I32
LT_I32
LE_I32

EQ_I64
NE_I64
GT_I64
GE_I64
LT_I64
LE_I64

AND_BOOL
OR_BOOL
NOT_BOOL

IS_PRESENT
IS_ABSENT

REQUIRE_TRUE
REQUIRE_FALSE
```

Do not support:

```text
arbitrary host functions
closure predicates
recursive predicates
higher-order predicate logic
runtime callbacks
proxy-mediated checks
```

If a predicate cannot be lowered into the closed vocabulary, it is not accepted into canonical material.

---

## 9. Reference Judgment Machine

The Reference Judgment Machine is a candidate semantic oracle.

It is not the final hot path.

It exists so generated gates and optimized backends can be checked against a deterministic reference.

### 9.1 Responsibilities

```text
1. Execute Predicate IR deterministically.
2. Return accept/reject/violation decisions.
3. Produce minimal evidence.
4. Support exhaustive, boundary, property-style, and differential tests.
5. Define the semantics of generated enforcement projections.
```

### 9.2 Return model

Avoid object-heavy return in the evaluator.

A simple status code is enough.

```text
0 = ACCEPTED
positive value = failure id
negative value = internal engine fault
```

Cold diagnostics can render this into structured reports.

### 9.3 Reference evaluator sketch

```kotlin
fun evalPredicate(
    opcodes: IntArray,
    operands: LongArray,
    intSlots: IntArray,
    longSlots: LongArray,
    flags: IntArray,
    base: Int,
): Int {
    val stack = LongArray(32)
    var sp = 0
    var pc = 0

    while (pc < opcodes.size) {
        when (opcodes[pc]) {
            LOAD_I32 -> {
                val slot = operands[pc].toInt()
                stack[sp++] = intSlots[base + slot].toLong()
            }

            CONST_I32 -> {
                stack[sp++] = operands[pc]
            }

            GT_I32 -> {
                val rhs = stack[--sp].toInt()
                val lhs = stack[--sp].toInt()
                stack[sp++] = if (lhs > rhs) 1 else 0
            }

            REQUIRE_TRUE -> {
                val ok = stack[--sp] != 0L
                if (!ok) return operands[pc].toInt()
            }

            else -> return INTERNAL_UNKNOWN_OPCODE
        }

        pc++
    }

    return ACCEPTED
}
```

The interpreter is a correctness anchor.

It is not the final performance story.

---

## 10. Generated Static Gates

Generated Static Gates are candidate optimized enforcement projections.

They should be generated from the same canonical material / Predicate IR that the Reference Judgment Machine executes.

### 10.1 Example

Source contract:

```text
requires x > 1
```

Generated gate:

```kotlin
// GENERATED CODE - DO NOT MODIFY
object CalculateInputGate {
    const val ACCEPTED: Int = 0
    const val FAILURE_X_MUST_BE_GREATER_THAN_1: Int = 1

    @JvmStatic
    fun admit(rawX: Int, intSlots: IntArray, base: Int): Int {
        if (rawX <= 1) {
            return FAILURE_X_MUST_BE_GREATER_THAN_1
        }

        intSlots[base] = rawX
        return ACCEPTED
    }
}
```

This generated JVM class is not contract authority.

It is a JVM packaging substrate for a generated enforcement projection.

Authority remains in:

```text
Frozen Canonical Contract Material
Predicate IR
Lowering Law
Guard Plan
```

### 10.2 Differential verification

Every generated gate must be checked against the Reference Judgment Machine.

```text
For each generated gate:
    run reference judgment on witness input
    run generated gate on same witness input
    compare decision
    compare failure id
```

For simple numeric predicates, generate boundary witnesses.

Example for `x > 1`:

```text
x = Int.MIN_VALUE
x = -1
x = 0
x = 1
x = 2
x = Int.MAX_VALUE
```

Expected:

```text
x <= 1 -> reject
x > 1  -> accept
```

Generated gates are accepted only if they match the reference judgment.

---

## 11. Contract Type Implementation Mapping

### 11.1 Invariant Contract

Invariant Contract is implemented as predicate programs attached to lifecycle points.

Trigger points:

```text
material issue
boundary admission
lowering completion
state transition completion
publication
```

Rule:

```text
A material row cannot be published unless all required publication invariants have passed.
```

### 11.2 Boundary Contract

Boundary Contract is implemented as:

```text
extractor
+ null classifier
+ normalizer
+ guard program
+ material issuer
```

Flow:

```text
Raw DTO / raw fields
-> extract primitive slots
-> null / absence classification
-> normalization
-> pre-admission guard
-> accepted material row
-> admitted lane
```

Rule:

```text
The implementation never receives raw boundary material through the Kontrakt-controlled path.
```

### 11.3 Interface / Operation Contract

Operation Contract is implemented as:

```text
precondition gate
+ implementation invocation
+ postcondition gate
+ invariant recheck
+ publication decision
```

Failure classification:

```text
precondition failure
    = caller/input rejection

postcondition failure
    = implementation conformance violation

invariant failure after implementation
    = implementation produced invalid material

engine failure
    = Kontrakt internal fault
```

Rule:

```text
Kontrakt does not inspect whether the implementation checked the precondition internally.
Kontrakt ensures invalid input cannot reach the implementation through the controlled gate.
```

### 11.4 State Machine Contract

State Machine Contract is implemented as a transition table.

Table form:

```text
stateId x eventId -> nextStateId or failureId
```

Rule:

```text
Implementation may request a transition.
Kontrakt decides whether the transition is legal.
```

The implementation does not own state transition authority.

The transition table does.

### 11.5 Lowering Contract

Lowering Contract is implemented as a lowering plan plus preservation checks.

Flow:

```text
frontend surface
-> source slot
-> normalization operation
-> canonical slot
-> stable id
-> SoA offset
```

Rule:

```text
Lowering is valid only if semantic obligations survive while host-language artifacts are erased.
```

### 11.6 Policy / Governance Contract

Policy Contract is implemented as a budget ledger.

Budget dimensions may include:

```text
semantic work units
physical steps
memory bytes
arena capacity
diagnostic budget
transition count
timeout envelope
```

Rule:

```text
A policy violation is not undefined behavior.
It is a declared failure transition.
```

---

## 12. Evidence Implementation

Evidence should not begin as one global shared cursor.

Initial structure:

```text
per-worker evidence segment
single-writer append
primitive arrays
deterministic merge at publication boundary
```

Segment layout candidate:

```text
evidenceKind[]
contractId[]
materialId[]
decisionCode[]
failureId[]
slotA[]
slotB[]
sequence[]
```

Rule:

```text
Hot path records compact primitive evidence.
Cold path renders human diagnostics.
```

Avoid initially:

```text
global AtomicInteger cursor
global synchronized log
object-per-evidence entry
string diagnostics in hot path
```

Ring-buffer protocols may be studied later.

The first design should use per-worker append-only segments.

---

## 13. Pipeline Instead of Type-State Wrappers

Do not rely on Kotlin value classes or phantom types as hot-path authority.

Conceptually, the distinction between raw and admitted material is valid.

But it should be enforced by Kontrakt-owned pipeline topology and state tables, not by JVM wrapper types.

Rejected hot-path authority:

```kotlin
@JvmInline
value class RawMaterialId(val id: Int)

@JvmInline
value class VerifiedMaterialId(val id: Int)
```

Reason:

```text
value class boxing can occur through nullable, generic, interface, reflection, collection, and framework boundaries.
```

Accepted model:

```text
RawInputLane
-> BoundaryGate
-> AdmittedMaterialLane
-> WorkerStage
```

Or:

```text
materialState[materialId] = RAW / ADMITTED / LOWERED / FROZEN / PUBLISHED
admittedByBoundary[materialId] = boundaryId
contractEpoch[materialId] = epoch
```

Best hot-path form:

```text
worker consumes only admitted lane
raw lane is physically unreachable from worker stage
```

---

## 14. Recursion and Higher-Order Functions

Kontrakt core must not use recursion as machine authority.

Kontrakt core must not use higher-order functions as contract authority.

The replacement is explicit machine-owned control.

```text
recursive call chain
-> explicit stack / explicit pipeline

closure / callback / higher-order value
-> explicit stage / operation id / predicate id
```

Rule:

```text
No closure enters canonical contract material.
No recursive control enters canonical contract material.
No proxy-wrapped function enters runtime authority.
```

If host syntax uses lambdas for authoring convenience, that syntax must be eliminated before canonical material is
formed.

The surviving representation must be:

```text
OperationId
PredicateId
ProjectionId
OrderingLawId
TransitionId
PolicyRuleId
```

not a function value.

---

## 15. Optimization Position

### 15.1 Reference judgment machine

Do not remove it early.

Use it as reference semantics.

```text
Reference Judgment Machine = semantic oracle
Generated Static Gate = optimized realization
```

### 15.2 AOT generated gates

Accept as hot-path target.

Start with generated Kotlin static functions.

Consider bytecode generation later.

```text
Phase 1: reference interpreter
Phase 2: generated Kotlin source
Phase 3: generated bytecode
```

### 15.3 SIMD

Accept as later backend.

Only meaningful for batch validation over SoA material.

```text
v1: scalar static gate
v2: batch gate
v3: vectorized gate
```

### 15.4 Ring-buffer evidence transport

Do not start with it.

Start with per-worker evidence segments.

Consider ring-buffer protocols only if evidence transport becomes a bottleneck.

---

## 16. Implementation Phases

### Phase 1: Canonical Material and Predicate IR

Build:

```text
contract frontend parser or descriptor reader
canonical name resolver
stable id assignment
schema table
slot table
predicate IR
failure table
```

Deliverable:

```text
Contract source can become canonical verifier-readable material.
```

### Phase 2: Reference Judgment Machine

Build:

```text
predicate interpreter
material cursor over primitive slots
failure code return
minimal evidence capture
```

Deliverable:

```text
x > 1 and result > x can be evaluated without objects, proxies, classes, getters, or subtype hierarchy as authority.
```

### Phase 3: Boundary and Operation Gates

Build:

```text
boundary extractor
guard runner
material issuer
operation pre-gate
implementation invocation adapter
operation post-gate
publication decision
```

Deliverable:

```text
Invalid input cannot reach implementation through the controlled path.
Invalid output cannot be published.
```

### Phase 4: State Machine and Policy Kernel

Build:

```text
transition table
illegal transition failure
policy budget ledger
resource exceeded transition
```

Deliverable:

```text
State changes and resource use are judged by Kontrakt-owned tables.
```

### Phase 5: Evidence Segment

Build:

```text
per-worker primitive evidence buffer
evidence merge
cold diagnostic renderer
```

Deliverable:

```text
Every accept, reject, transition, and failure has compact evidence.
```

### Phase 6: Generated Static Gates

Build:

```text
Kotlin source generator
reference judgment vs generated gate differential tests
generated boundary gates
generated operation postcondition gates
```

Deliverable:

```text
Hot path can bypass opcode dispatch while preserving reference semantics.
```

### Phase 7: Advanced Backends

Only after the model is stable.

Study:

```text
ASM bytecode backend
JVM Vector API backend
batch validation
ring-buffer evidence transport
```

Deliverable:

```text
Performance backends remain subordinate to frozen canonical contract material semantics.
```

---

## 17. Minimal MVP Example

Contract:

```text
contract Calculate {
    input CalculateInput {
        x: i32
    }

    requires XGreaterThanOne {
        x > 1
    }

    output CalculateOutput {
        x: i32
        result: i32
    }

    ensures ResultGreaterThanInput {
        result > x
    }
}
```

Expected compiled artifacts:

```text
Schema:
    CalculateInput
        slot 0: x i32

    CalculateOutput
        slot 0: x i32
        slot 1: result i32

Predicate:
    XGreaterThanOne
        LOAD_I32 0
        CONST_I32 1
        GT_I32
        REQUIRE_TRUE X_MUST_BE_GREATER_THAN_1

    ResultGreaterThanInput
        LOAD_I32 1
        LOAD_I32 0
        GT_I32
        REQUIRE_TRUE RESULT_MUST_BE_GREATER_THAN_INPUT

Boundary:
    CalculateInputBoundary
        input raw x
        emits CalculateInput material
        guard XGreaterThanOne

Operation:
    Calculate
        pre XGreaterThanOne
        post ResultGreaterThanInput
```

Expected runtime behavior:

```text
x = 1
    -> boundary reject
    -> implementation not called

x = 2, result = 4
    -> accepted

x = 2, result = 1
    -> postcondition violation
    -> output not published
```

---

## 18. Final Architecture Statement

Kontrakt verifier should be implemented as a compiler-backed judgment system over frozen canonical contract material.

It should not be implemented as a proxy monitor.

It should not verify contracts by inspecting whether implementation classes contain certain checks.

It should enforce contracts by controlling the only valid path from raw input to published material.

The final rule is:

```text
Contract authority lives in canonical contract material.
Judgment is performed by Kontrakt-owned gates.
Implementation computes inside admitted boundaries.
Publication is controlled by Kontrakt.
Evidence proves the path.
```

Short form:

```text
No proxy.
No class authority.
No object graph.
No higher-order runtime contract.
No recursive hidden control.
No process-level fail-closed for ordinary contract rejection.

Canonical Material.
Frozen Material.
Reference Judgment.
Generated Gate.
Explicit Pipeline.
Primitive Evidence.
```