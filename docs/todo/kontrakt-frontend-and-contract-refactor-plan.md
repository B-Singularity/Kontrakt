# Kontrakt Frontend and Contract-Aligned Refactor Plan

## 0. Document Status

Status: planning document.

This document is not an ADR.

It records the execution plan for the next Kontrakt work sequence after ADR-0045 package placement has been settled. It
should live under `docs/todo/` or `docs/planning/` until the frontend authority, lowering law, and canonical contract
material model are accepted as architectural decisions.

Once those decisions are stable, they should be promoted into one or more ADRs.

Recommended split:

```text
Planning / TODO document:
    this file

Future ADRs:
    ADR-0046 Contract Frontend Authority
    ADR-0047 Lowered Contract Fact Model
    ADR-0048 Contract-Aligned Legacy Refactor Rule
```

This document exists because the current codebase still contains object-oriented, port-era, validator-era, service-era,
and framework-era shapes that do not yet match the contract theory described by `What Contract Is`.

The next refactor must not begin from package movement alone. It must begin from the meaning of the Kontrakt frontend.

---

## 1. Position

The package placement map is assumed to be settled.

The next blocking decision is the Kontrakt frontend.

Kontrakt cannot safely refactor legacy files into contract-aligned form until it knows what the user-facing contract
surface means, what survives lowering, and what becomes canonical contract material.

The immediate order is therefore:

```text
Contract Frontend v1
-> Canonical Contract Material v1
-> Lowering / Lowered Contract Fact v1
-> Minimal Judgment / Verifier Path
-> Contract-theory audit of existing files
-> Legacy quarantine breakdown
-> Planning refactor
-> Cache / interner refactor
-> Release-readiness gates
```

Planning and caching must not be resumed first. They depend on stable canonical material, stable lowering output, and
stable equality material.

---

## 2. ADR or TODO?

This plan should start as a TODO / planning document, not an ADR.

Reason:

```text
A TODO plan records work sequence.
An ADR records accepted architectural decisions.
```

The current discussion has not yet finalized:

```text
- the exact v1 frontend syntax;
- the exact authority boundary of annotations, interfaces, DSLs, or descriptors;
- the exact lowered fact taxonomy;
- the exact canonical material tables;
- the exact frontend parity law;
- the exact rule for legacy OO-shaped files.
```

Therefore this document should not be written as a decision record yet.

However, this plan should produce ADRs when the relevant decisions are ready.

Recommended promotion path:

```text
Step 1:
    Keep this file under docs/todo/ or docs/planning/.

Step 2:
    Draft Contract Frontend v1.

Step 3:
    Once accepted, promote frontend authority into an ADR.

Step 4:
    Draft Lowered Contract Fact v1.

Step 5:
    Once accepted, promote lowering and canonical material into an ADR.

Step 6:
    Draft the legacy refactor rule.

Step 7:
    Once accepted, promote the contract-aligned refactor rule into an ADR.
```

---

## 3. Guiding Rule

The core rule for the next work sequence is:

```text
Frontend meaning must be settled before legacy files are judged.
```

A legacy file cannot be correctly classified as contract, realization, adapter, diagnostic, publication, or deletion
target until Kontrakt knows what its accepted contract frontend lowers into.

The existing code often uses names such as:

```text
Contract
Validator
Executor
Publisher
Strategy
Provider
Resolver
Policy
Runtime
Planner
Factory
Port
Adapter
```

These names are not reliable contract-theory categories.

Every such file must be reclassified by its actual authority:

```text
Does it declare contract meaning?
Does it define accepted material?
Does it judge material?
Does it lower frontend surface into facts?
Does it realize already accepted material?
Does it connect outside technology?
Does it only preserve legacy port-era shape?
```

---

## 4. Phase A — Contract Frontend v1

### 4.1 Goal

Define the first Kontrakt-owned frontend that users can write or generate as contract surface.

The frontend must not allow implementation classes, getters, private fields, inheritance, subtype hierarchy, proxies,
callbacks, closures, or arbitrary runtime wrappers to become contract authority.

### 4.2 Decision to make

The project must decide which frontend is authoritative in v1.

Recommended v1 position:

```text
Primary authority frontend:
    Kontrakt-owned descriptor / DSL

Secondary acquisition surfaces:
    Kotlin interface metadata
    annotations
    generated indexes
    external document-derived syntax

Rejected as authority:
    class body
    getter body
    private field
    inheritance
    subtype hierarchy
    proxy
    runtime wrapper
    lambda / closure
    arbitrary implementation body
```

The secondary acquisition surfaces may provide candidates. They must not remain authority after lowering.

### 4.3 Minimal v1 frontend scope

The v1 frontend should support only the minimum required to prove the path:

```text
contract
operation
input schema
output schema
requires
ensures
failure id
diagnostic id
```

Do not include the following in v1:

```text
full state-machine syntax
full policy/governance syntax
external plugin system
arbitrary Kotlin expressions
arbitrary JVM method references
recursive predicates
higher-order predicates
host-language lambdas as contract material
```

### 4.4 Deliverables

```text
docs/design/contract-frontend-v1.md
examples/contracts/calculate.kontrakt
frontend grammar or descriptor schema
frontend rejection rules
frontend authority note
```

### 4.5 Exit criteria

```text
- A minimal contract can be authored without using implementation classes as authority.
- The frontend can express one operation with input, output, precondition, postcondition, failure, and diagnostic ids.
- Unsupported host-language authority is rejected or ignored before canonical material exists.
- Annotation/interface acquisition is clearly marked as secondary, not authoritative.
```

---

## 5. Phase B — Lowered Contract Fact v1

### 5.1 Goal

Define what survives from the frontend after lowering.

The lowering step removes frontend syntax authority and produces Kontrakt-owned facts.

### 5.2 Candidate fact taxonomy

Initial v1 facts:

```text
ContractFact
OperationFact
InputSchemaFact
OutputSchemaFact
SlotFact
PredicateFact
FailureFact
DiagnosticFact
BoundaryFact
PublicationFact
ImplementationBindingCandidateFact
```

These are candidate names. They must be reviewed against `What Contract Is` before becoming final.

### 5.3 Lowering law

The lowering law must answer:

```text
Which frontend elements survive?
Which frontend elements are evidence only?
Which host-language artifacts are erased?
Which names become stable ids?
Which failures are declared?
Which diagnostics are declared?
Which unsupported constructs are rejected?
```

Host artifacts that must not survive as authority:

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
implementation body
```

### 5.4 Deliverables

```text
docs/design/lowered-contract-fact-v1.md
LoweredContractFact model
frontend-to-fact lowering tests
unsupported-frontend rejection tests
Calculate lowered fact golden vector
```

### 5.5 Exit criteria

```text
- Calculate frontend lowers into stable facts.
- Fact ordering is deterministic.
- Fact ids are stable under repeated run.
- Host implementation artifacts do not survive as contract authority.
- Unsupported constructs fail closed with diagnostic evidence.
```

---

## 6. Phase C — Canonical Contract Material v1

### 6.1 Goal

Convert lowered facts into canonical material that can be judged and later frozen.

This phase must remain smaller than the final frozen physical image design. It only needs enough structure to support
the first verifier path.

### 6.2 Candidate canonical material

```text
ContractId
OperationId
SchemaId
SlotId
PredicateProgramId
FailureId
DiagnosticId
BoundaryPlanId
OperationPlanId
PublicationPlanId
```

### 6.3 Candidate tables

```text
SchemaTable
SlotTable
PredicateProgramTable
BoundaryPlanTable
OperationPlanTable
FailureTable
DiagnosticTable
PublicationPlanTable
```

### 6.4 Deliverables

```text
docs/design/canonical-contract-material-v1.md
canonical material builder
stable ordering law
stable id assignment law
Calculate canonical material golden vector
```

### 6.5 Exit criteria

```text
- Canonical material is deterministic.
- Repeated lowering produces identical canonical material.
- Shuffled acquisition order does not change semantic order.
- Canonical material does not depend on object graph identity.
- Canonical material can be consumed by the predicate judgment path.
```

---

## 7. Phase D — Predicate IR and Reference Judgment MVP

### 7.1 Goal

Build the smallest verifier kernel that can judge a contract predicate without trusting implementation code.

The reference judgment path is not the final hot path. It is the semantic oracle.

### 7.2 Initial predicate vocabulary

```text
LOAD_I32
LOAD_I64
LOAD_BOOL
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

### 7.3 Deliverables

```text
Predicate IR model
Predicate IR encoder
Reference predicate interpreter
failure-code return model
minimal evidence handle
predicate golden vectors
```

### 7.4 Exit criteria

```text
- x > 1 can be judged from primitive slots.
- result > x can be judged from primitive slots.
- invalid predicate programs fail closed.
- reference judgment emits deterministic failure ids.
- minimal evidence is recorded without hot-path diagnostic string construction.
```

---

## 8. Phase E — Boundary, Operation, and Publication MVP

### 8.1 Goal

Create the first controlled path from raw input to published material.

The implementation may compute, but Kontrakt decides whether input may reach it and whether output may be published.

### 8.2 MVP flow

```text
raw input
-> boundary extraction
-> precondition gate
-> admitted material
-> implementation invocation
-> output material
-> postcondition gate
-> publication judgment
-> published claim or failure evidence
```

### 8.3 Required behavior

For the Calculate example:

```text
x = 1:
    boundary reject
    implementation not called

x = 2, result = 4:
    accepted and published

x = 2, result = 1:
    postcondition violation
    output not published
```

### 8.4 Deliverables

```text
Boundary gate MVP
Operation invocation adapter MVP
Postcondition gate MVP
Publication judgment MVP
minimal evidence output
Calculate end-to-end test
```

### 8.5 Exit criteria

```text
- Invalid input cannot reach implementation through the Kontrakt-controlled path.
- Invalid output cannot be published.
- Implementation class is not treated as contract authority.
- Publication is a judgment, not a reporting side effect.
```

---

## 9. Phase F — What Contract Is File Audit

### 9.1 Goal

Audit existing files against the contract theory after the frontend and lowering meaning are stable.

This is the point where object-oriented and port-era files are reviewed one by one.

### 9.2 Audit record format

Every reviewed file should receive a short classification record:

```text
File:
Current role:
Legacy smell:
Contract-side candidate:
Realization-side candidate:
Adapter-side candidate:
Diagnostic/publication candidate:
Must split:
Must rename:
Can delete:
Quarantine bucket:
Decision:
```

### 9.3 Classification rule

```text
Contract-side:
    declares meaning, obligation, material, judgment, failure, diagnostic evidence, publication rule, policy rule, or state/transition rule.

Realization-side:
    computes over already accepted material or implements a deterministic projection of contract material.

Adapter-side:
    connects Kontrakt to outside technology.

Quarantine:
    mixes contract, realization, adapter, service, port, validator, publisher, strategy, provider, or framework lifecycle authority.

Delete:
    preserves an obsolete architecture shape with no remaining contract-theory role.
```

### 9.4 First audit buckets

Review in this order:

```text
1. frontend and input contract files
2. lowered fact and canonical material candidates
3. validation / verification / compliance legacy
4. publication / reporting legacy
5. provider port legacy
6. policy / governance legacy
7. execution surface legacy
8. cache infrastructure legacy
9. planning files
10. cache / interner files
```

### 9.5 Deliverables

```text
docs/todo/legacy-file-audit.md
docs/todo/quarantine-breakdown.md
per-bucket refactor checklist
first batch of accepted renames/splits/deletions
```

### 9.6 Exit criteria

```text
- Every quarantine file has an intended next action.
- No file is moved out of quarantine without a contract-theory classification.
- No contract package contains planner/runtime/framework-specific authority by accident.
- No realization package recreates contract-axis names such as governance or publication as implementation authority.
```

---

## 10. Phase G — Legacy Quarantine Breakdown

### 10.1 Goal

Break quarantine buckets only after the frontend, lowering, canonical material, and first verifier path are stable.

### 10.2 Current quarantine buckets

Expected buckets:

```text
migration/quarantine/provider_port_legacy
migration/quarantine/execution_surface_legacy
migration/quarantine/policy_governance_legacy
migration/quarantine/validation_verification_legacy
migration/quarantine/publication_reporting_legacy
```

Additional bucket may be introduced if needed:

```text
migration/quarantine/cache_infrastructure_legacy
```

### 10.3 Breakdown rule

A quarantine file may leave quarantine only when it can be classified as exactly one of:

```text
contract-side file
realization-side file
adapter-side file
diagnostic/publication file
test support file
deleted obsolete file
```

A file that still carries multiple roles must be split before leaving quarantine.

---

## 11. Phase H — Planning Refactor

### 11.1 Goal

Refactor planning after canonical material is stable.

Planning must consume Kontrakt-owned material. It must not consume legacy DTOs, port-era provider shapes, object graphs,
or framework lifecycle surfaces as authority.

### 11.2 Planning input

```text
canonical contract material
schema table
slot table
predicate program ids
boundary plan
operation plan
failure ids
diagnostic ids
implementation binding candidate material
```

### 11.3 Planning output

```text
operation execution plan
boundary gate plan
postcondition gate plan
publication gate plan
implementation invocation plan
evidence plan
```

### 11.4 Exit criteria

```text
- Planning is deterministic without cache.
- Planning does not rely on acquisition order.
- Planning does not use legacy provider ports as authority.
- Planning output is stable under repeated runs.
- Planning output feeds the verifier/publication path.
```

---

## 12. Phase I — Cache and Interner Refactor

### 12.1 Goal

Implement cache and interner only after planning material and equality material are stable.

### 12.2 Required decisions

```text
PlanCacheKey semantic equality material
route key vs equality key
exact equality check
hit / miss / refusal taxonomy
stable intern id law
cache-disabled equivalence rule
collision handling rule
```

### 12.3 Exit criteria

```text
- Cache enabled and cache disabled produce the same semantic result.
- Interner state cannot change contract meaning.
- Intern id assignment is deterministic within declared scope.
- Cache hit requires exact equality, not hash equality alone.
- Parallel completion order cannot change published identity.
```

---

## 13. Phase J — Release Readiness Gates

### 13.1 Goal

Close only the gates that are required for the next public milestone.

Do not pull all post-v1 work into the frontend/verifier MVP.

### 13.2 Required before claiming a stable foundation

```text
package boundary architecture tests
contract lowering boundary documentation
canonical material golden vectors
predicate IR golden vectors
reference judgment tests
boundary negative tests
postcondition violation tests
publication refusal tests
repeated-run determinism tests
shuffled input determinism tests
report stability tests
known limitations document
```

### 13.3 Required before release candidate or stable release

```text
metadata identity golden vectors
planning cache / interner equivalence tests
diagnostic budget and sanitization tests
malformed binary fuzzing
ClassLoader isolation tests
multi-threaded lane isolation tests
build daemon leak tests
resource profile calibration
benchmark and profiling notes
license and dependency hygiene
reproducible CI evidence
```

---

## 14. Immediate Next Actions

Execute in this exact order:

```text
1. Write docs/design/contract-frontend-v1.md.
2. Write the Calculate frontend example.
3. Write docs/design/lowered-contract-fact-v1.md.
4. Define the minimal fact taxonomy.
5. Define predicate subset and rejection rules.
6. Build the first frontend-to-fact lowering golden vector.
7. Build Predicate IR and reference judgment MVP.
8. Build Calculate boundary / operation / publication path.
9. Start the What Contract Is file audit.
10. Break quarantine only after each file has a contract-theory classification.
```

Do not resume planning cache or interner implementation until frontend, lowering, canonical material, and no-cache
planning input are stable.

---

## 15. Final Rule

The next Kontrakt milestone is not package movement.

The next milestone is:

```text
A user-facing contract frontend can lower into Kontrakt-owned canonical material, and Kontrakt can judge a minimal operation without trusting implementation classes as contract authority.
```

Only after that milestone exists can the old object-oriented files be reliably rewritten according to
`What Contract Is`.