# SADR-0001: Admission Judgment Integrity and Secure Compiler Realization

## Status

Proposed — Security Architecture Working Draft. Nothing in this draft changes Contract authority or asserts an
implemented security guarantee.

## Date

2026-10-11

## Related

- `docs/constitution/security/security-constitution.md`
- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0065 Admission; ADR-0064 Input; ADR-0066 Canonicalization; ADR-0048 Inbound Composition
- ADR-0063 Establishment; ADR-0071 HIR; ADR-0073 JVM Platform; ADR-0074 Compiler Result; ADR-0075 Compiler Material
- `../design/admission/admission-authoring-api-and-source-surface-working-design.md`
- `docs/verification/checklists/kontrakt-1d-hir-establishment-master-checklist.md`
- `docs/verification/checklists/kontrakt-canonicalization-adr-sadr-closure-checklist.md`
- `docs/todo/compiler-engine/kontrakt-compiler-architecture-closure-and-implementation-readiness-todo.md`
- `docs/todo/compiler-engine/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`

---

# 1. Purpose, Ownership, and Source Basis

Admission determines whether an Interaction may continue under an established Admission Definition. The user may express
the judgment in ordinary Java or Kotlin source. Before that source can contribute Contract meaning, the compiler must
determine which behavior the expression denotes and whether every observation used by that behavior is legal. Simply
compiling or executing a method that returns `Boolean` does not establish an Admission judgment.

This source boundary creates a larger attack surface than a passive declaration would. A property access may invoke
code. A library helper may read external state. A computation that is deterministic for a given value may still exhaust
resources. Even after source analysis succeeds, a later optimization or JVM realization could change which value the
judgment examines. This SADR addresses those risks across acquisition, HIR, Establishment, material consumption,
realization, verification, caching, and diagnostics.

The Security Constitution defines the security obligations shared by Kontrakt. The Contract ADRs remain authoritative
for Admission legality and semantics. In particular, ADR-0065 owns its judgment and result meanings, while the owning
Input, Canonicalization, Composition, HIR, and Establishment ADRs determine the material it may lawfully observe. This
SADR identifies trust boundaries, states which claims require evidence, and prohibits reliance on unqualified
implementations. It does not create an Admission result, redefine a Required Basis, or make an implementation work limit
into `Rejected`.

The Admission Working Design owns replaceable methods of source qualification and realization. A separate Admission
Authoring API specification will eventually enumerate public declarations and supported authoring forms. The two
documents must meet this SADR's security requirements, but neither may acquire Contract authority by describing a
convenient implementation.

The separated version of ADR-0065 still records the historical ordering in which Admission preceded Canonicalization.
The intended inbound composition now places optional Canonicalization before Admission. The exact operand, Required
Basis, Applicability, and result attribution have not yet been closed in the owning ADRs. This SADR therefore treats the
corresponding security guarantees as conditional requirements. It does not resolve the missing Contract law on its own.
The separately designated final 902-line ADR-0066 was not available for this drafting pass. The archived version and
latest handoff therefore supply provisional Canonicalization context until the final document is checked.

This draft draws on the project's Modern Security Architecture and Modern Compiler Architecture studies, as well as
external research and platform specifications. Those materials provide failure examples and engineering evidence; they
do not outrank Kontrakt's Contract authority. Section 6 records the external sources and explains where their
conclusions can and cannot be transferred. A documented vulnerability in another compiler is a reason to examine the
corresponding boundary, not evidence that Kontrakt presently contains that vulnerability.

---

# 2. Protected Property and Threat Model

## 2.1. What the architecture must protect

The central integrity property is that an established Admission result means exactly what its governing Definition says
it means. That requires the result to refer to the correct occurrence and determining operand. The observations
authorized by the Definition must be the observations actually used by its realization. A `Boolean` value with the
expected type is not adequate evidence when it was calculated from the wrong Input or under the wrong World.

Other protected integrity relationships support that claim. The compiler must preserve the distinction between HIR
candidates and Established Material. A consumer must know which Version and Binding apply to an occurrence. Analysis
summaries and verification evidence must refer to the subject they actually checked. Cached results must remain valid
for the current application, and emitted JVM artifacts must implement the judgment that the compiler accepted.

Confidentiality is a separate concern. Input values may reveal personal or operational information, and Canonicalization
may preserve or derive sensitive information about those values. Predicate source can reveal business policy. An error
message may disclose a membership test even when it prints no original value. Internal evidence, build credentials,
cached metadata, and diagnostic traces therefore need an explicit disclosure boundary. A capability to inspect a
judgment does not imply permission to disclose everything that contributed to it.

Availability also requires independent treatment. Parsing, call analysis, pattern matching, proof checking,
materialization, diagnostics, and judgment execution can consume substantial resources. The fact that an accepted
expression terminates does not establish a safe upper bound on its work. Conversely, a resource stop does not establish
that the Admission predicate was false.

A security claim must identify the exact subject and property it covers. It must also state the observations and
assumptions on which the evidence depends, identify the relevant platform and context, and explain when its validity
expires. Claims about semantic preservation, JVM structure, source closure, confidentiality, artifact provenance, and
resource containment remain distinct. An implementation must not collapse them into an undifferentiated `secure=true`
value. Deterministic output is not itself a constant-time guarantee and does not eliminate timing or cache side
channels.

## 2.2. Who or what can cause a failure

### A malicious Admission author

An author may submit Java or Kotlin source whose apparent predicate calls an effectful helper. For example, a getter may
look like a field read while opening a file. The attacker can also influence declarations and build inputs that the
configured project allows them to edit. This capability does not imply that the attacker controls the compiler binary or
the operating system.

### A malicious boundary-value supplier

An external caller may control the values entering Input without being allowed to edit an Admission Definition. Such a
caller can choose the size of collections, use unusual Unicode or numeric values, and deliberately construct worst-case
regex matches. Tests must account for these values even when the authored predicate is benign.

### A malicious or confusing dependency

An attacker may replace a dependency artifact, create two classes with the same binary name, or provide ambiguous
archive metadata. A compromised plugin may also affect the build if the actual build configuration permits it to
execute. The architecture must distinguish inert source under analysis from code that the build has already granted
execution privileges.

### A compiler or analysis defect

A wrong call summary may omit a reachable callback. An optimizer may move a guarded operation onto a previously
unreachable path. A reader may observe a partially published record. These failures can occur without a deliberate
attacker; the same protection is needed because the resulting authority confusion is identical.

### A shared or changing execution environment

If Kontrakt uses concurrent workers or shared retained products, races and cross-context reuse become relevant. If a
remote service or multi-tenant cache is later introduced, additional observers may infer information from timing or
resource contention. These service-specific threats are conditional; this draft does not assume that every such feature
exists in V1.

A verified class may also execute under a different JDK, loader, instrumented runtime, or dependency version. A
legitimate upgrade can invalidate an earlier security qualification just as effectively as malicious replacement.

## 2.3. Limits of the Admission-specific claim

An adversary with complete control of the operating system, compiler executable, hardware, or privileged native code is
outside an Admission-only containment claim unless a wider security architecture expressly supplies that protection.
Java source under inspection and a Gradle plugin already executing in the compiler process cannot be assigned the same
attacker privileges. This exclusion must appear in the claim; it must never be treated as proof of safety.

---

# 3. Architectural Trust Boundaries

The boundaries below identify where one component may rely on information produced by another. They are security
reliance points, not new Contract Authorities. Their implementations may change provided the consumer never receives
stronger meaning than the producer lawfully established.

### T01. Source and artifact acquisition

When the compiler reads source files, JARs, classfiles, or imported metadata, it must identify which bytes and member
definitions it has read. If two parsers can disagree about which class a dependency contains, the compiler must not
validate one class and later execute another. Tools that execute plugins or processors need a separate privilege
boundary; reading data and executing code are not interchangeable operations.

### T02. Java/Kotlin source refinement

The frontend may lower an authored predicate into Admission HIR only after closing every reachable operation that
affects the judgment. This includes implicit calls and exceptional behavior. A pure-looking syntax form does not justify
assuming a pure implementation. If a target cannot be resolved within the supported model, the compiler cannot rely on
an invented meaning.

### T03. Candidate-to-Establishment handoff

HIR material used for diagnostics or editor recovery must not become authoritative merely because it has a well-formed
storage representation. Establishment must receive a complete and properly sealed candidate through the prescribed
protocol. Any required Version or Binding prerequisite must be satisfied by its rightful owner.

### T04. Inbound composition and Admission entry

Input and optional Canonicalization must jointly provide exactly the complete determining presentation allowed by the
owning ADRs. A Canonicalization refusal does not authorize Admission to continue using successful fragments. The current
Application and Applicable Basis must be established rather than guessed from a matching type or physical handle. This
security condition awaits the final Contract-owned Basis law.

### T05. Admission-to-consumer handoff

A downstream consumer must receive the actual Established Admission result and its lawful attribution. It must not
reconstruct the answer from a raw host `Boolean`, reopen the original Input carrier, or observe distinctions that
Canonicalization removed from the determining operand.

### T06. Analysis evidence and optimization

A verifier's conclusion is usable only for the subject and assumptions it checked. If the optimizer changes an IR
generation or relies on an updated library profile, earlier evidence may no longer support the new result. An
inconclusive checker outcome is not a positive security finding.

### T07. Generated classfile and JVM realization

Valid JVM bytecode must satisfy the structural requirements of its target platform. That check alone cannot show that
the emitted implementation preserves the Admission meaning. The semantic and structural assurances must be established
separately, under the platform assumptions actually used.

### T08. Material publication and reuse

A cache, persistent store, frozen table, or concurrent worker may provide retained information, but it may not establish
authority by producing a matching hash. Readers must receive coherent, qualified material associated with the current
context. A record from an earlier generation does not become current because its physical slot or identifier was reused.

### T09. Outward diagnostics and observations

A diagnostic system may have enough evidence to explain why a predicate rejected a value without having permission to
disclose that value. Its output must respect the receiving audience, and it must not imply that an incomplete
explanation is exhaustive. Logs and trace exports are separate outward channels with their own disclosure requirements.

### T10. Build and release provenance

The released compiler and generated artifacts depend on actual toolchain, source, dependency, and build inputs. A
provenance or signing claim must attach to the bytes that were built and delivered. It does not by itself prove that the
source or compilation was semantically correct.

None of these boundaries may be crossed solely because two objects share a class name, source location, type, hash, or
cache key. The relevant Authority-owned relation must already exist. A missing or ambiguous relation remains unresolved
even when an implementation could choose a seemingly reasonable candidate.

---

# 4. Proposed Security Architecture Decisions

## SA-01 — The value judged by Admission must also be the value used afterward

Users should be able to author Admission against Input-declared coordinates without changing their source when
Canonicalization is selected. The implementation must nevertheless evaluate that source against the actual presentation
that inbound Composition lawfully supplies. When a selected Canonicalization changes a value from `"kr"` to `"KR"`, the
predicate must see the latter if that is the established Representative. Lowering must not revive `"kr"` merely because
the original carrier remains available to a diagnostic tool.

This requirement protects Judgment–Use Coherence. It does not determine the exact Required Basis: the owning ADRs must
close that law before the security claim can be accepted.

## SA-02 — Inspecting Admission source must not execute it

The compiler cannot discover a method's meaning by running its constructor, getter, or helper against an arbitrary
example input. Doing so may grant network or filesystem effects before Admission has been qualified. Source refinement
must use an accountable non-executing analysis path.

Build tools that deliberately execute processors or plugins have different privileges. Their execution must be
recognized and reviewed at the build boundary instead of being presented as passive source acquisition. If refinement
cannot establish a method's meaning, calling that method at runtime as a fallback is not permitted.

## SA-03 — Source closure requires positive evidence

The absence of a reported effect is not a proof of purity. The compiler must establish the meaning of every operation
that can affect an accepted judgment. For example, a collection membership test may invoke the collection's equality
implementation. A Kotlin property may invoke a delegate. A helper may load a class whose initializer changes process
state.

Unresolved dynamic targets and unqualified external capabilities remain unsupported. Modifiers such as `val` or `final`
constrain some source-level behavior but do not independently close the reachable call graph or prove that a particular
JVM operation is safe.

## SA-04 — Permitted local mutation must not change established observations

ADR-0065 may permit fresh local accumulation inside a judgment. Its realization cannot use that permission to mutate the
Input or change an established value. A copied collection that still references mutable child objects is not necessarily
isolated. Captured state can also escape a local computation even when the local variable appears fresh.

The Design must show why permitted mutation cannot alias the determining material or leak between Applications. An
external write to the original carrier must not affect an already established operand.

## SA-05 — Termination and bounded cost require different assurances

A computation can always terminate yet require impractical time or memory. For example, two large collections can
produce a quadratic nested scan, and a permitted regex can have severe engine-dependent backtracking. The compiler
itself may also run out of resources while proving that a method is eligible.

Each relevant stage needs an explicit supported-work envelope or containment policy. When it is exhausted, the system
must report the actual stopping boundary. It must not convert a timeout into Admission `Rejected` or silently
reinterpret a Budget or Capacity Contract.

## SA-06 — An analysis success must not promote a candidate into authority

A parser can recover from an incomplete expression to show editor diagnostics. An optimizer can mark an IR fragment as
checked. A cache can return a plausible candidate record. None of those events establishes an Admission Definition.

Only the appropriate Establishment path may grant that authority, using a complete sealed candidate and the Version and
Binding prerequisites required by the owning ADRs. Shared storage does not weaken the distinction between diagnostic
data and authoritative material.

## SA-07 — An occurrence needs its exact current Application

A reusable Definition describes a judgment law; it does not make every earlier result reusable. An `Admitted`
established under one Policy World or Canonicalization selection cannot be assigned to a different Application merely
because the Definition reference matches.

Admission entry requires a complete lawful Basis and Applicable Binding. If selected Canonicalization refuses its
occurrence, successful partial assessments cannot stand in for that Basis. The exact reference and cardinality rules
remain blocked on the owning ADR decisions.

## SA-08 — Verification evidence must disclose what it actually proves

An abstract analysis may prove a property of one supported operation set, while a classfile verifier checks structural
rules for emitted bytes. Those claims are not interchangeable. The evidence used by a consumer must identify its
subject, property, assumptions, and applicable version.

A solver that returns `unknown` has not established success. Timeouts and unmodeled behavior are also inconclusive.
Where useful, a reference evaluator or independent conclusion checker should provide a way to challenge complex analyzer
results. V1 is not required to furnish a formal proof of every JVM instruction.

## SA-09 — Optimization must preserve the security-relevant behavior

Matching final Boolean values does not prove preservation if an optimization moves a dangerous computation before its
guard. The transformation must also preserve which paths are legally reached, which observations occur, and whether a
stage enters at all. An expression whose result was never established cannot be treated as an established negative
result.

A transformation across the Canonicalization/Admission boundary therefore needs evidence that it preserves refusal and
attribution as well as the permitted judgment. The Design may choose conservative optimization restrictions where a
stronger proof is not available.

## SA-10 — JVM structural verification does not prove Admission semantics

The JVM verifier checks bytecode constraints defined by the platform. It does not establish that a method implements the
user's Admission predicate or that its effects are confined. Successful classfile generation is likewise not proof that
every emitted byte sequence passes independent verification.

The backend must support both structural checks and the applicable Admission semantic-conformance checks. A class loader
alone must not be described as a hostile-code sandbox.

## SA-11 — Retained evidence must be qualified for current use

A stored success may have been correct when first produced and still be inapplicable now. For example, the Law Version
used to form a Representative might change while the Admission Definition stays the same. A cached result cannot cross
that change without the owning validity check.

The compiler must also distinguish an intact record from one that was partially written or assigned a reused physical
handle. Frozen material and HID may support reuse, but neither is a Contract Authority. A clean reference computation
must remain possible for verification when appropriate.

## SA-12 — Explaining a judgment does not authorize disclosure of its inputs

A diagnostic can help the author understand how Canonicalization affected Admission while still protecting sensitive
values. The source position of a condition may be safe to disclose to the author even when the exact Input is not safe
to send to a remote log. Hashes, lengths, and equality results may also reveal protected information.

A diagnostic must distinguish a proven contradiction from a plausible mismatch of intent. When only the final Boolean is
known, it must not invent a particular failed subcondition. The appropriate Diagnostic and Security Design owns
disclosure and retention policy.

## SA-13 — Source qualification is not a sandbox for arbitrary JVM code

Admission source closure cannot constrain an arbitrary build plugin or a native method executed outside that qualified
source. The traditional Java Security Manager is permanently disabled beginning with JDK 24, so a ClassLoader or
verified classfile cannot be assumed to provide general file/network containment.

If a deployment intends to execute hostile code, it must separately justify the chosen isolation boundary. Otherwise V1
may explicitly decline to execute untrusted build-time or runtime helpers. The SADR does not mandate an isolation
mechanism before that deployment decision exists.

## SA-14 — Artifact provenance and semantic correctness are different claims

A signed binary may be authentic yet contain a compiler defect. A semantically correct generated class may nevertheless
have been substituted after verification. The build and release system must therefore identify which source and
toolchain were used and which bytes were actually delivered.

Supply-chain controls and vulnerability response belong to the whole-machine security architecture. Admission consumers
must state which of those controls their own security claim assumes; they must not infer semantic correctness from a
signature alone.

---

# 5. Detailed Attack and Failure Cases

These cases are review hypotheses, **not findings that current Kontrakt code is exploitable**. Each case identifies how
a trust boundary could fail, which guarantee would be lost, what the architecture must make possible, and where
implementation and verification should be documented. A Contract-law dependency is marked **ADR-BLOCKED** instead of
being resolved by invention inside this SADR.

## 5.1. Acquisition, source identity and single interpretation

### TH-01. Ambiguous JAR/ZIP interpretation — T01, T10

Suppose a dependency JAR contains two entries with the same class name. One archive reader returns the first entry,
while another returns the second. Kontrakt might qualify the method in the first entry and produce a result that later
relies on the second. A nested signed JAR can create a similar disagreement if different components disagree about the
entry to which the signature applies.

Acquisition must bind the analyzed member to the exact bytes used by subsequent compilation and loading assumptions.
Ambiguous directory records, member names that escape their intended location, and disallowed nesting must be rejected
under a documented interpretation policy. Extraction must not escape its workspace, and decompression needs its own
resource limits.

Verification should feed intentionally ambiguous JARs to every parser actually used in the build path, then check
whether they agree about the selected member. A mismatch must not be resolved by choosing whichever parser appears more
permissive. ZipDiff [R04] demonstrates this type of parser disagreement; it is not a finding about the current Kontrakt
implementation. The source/artifact acquisition and supply-chain designs own the checks.

### TH-02. Classpath and binary-name shadowing — T01, T02, T07

Consider a pure helper that was analyzed in one JAR, while a second class with the same binary name is preferred by the
classloader at execution. The source is unchanged, but the verified implementation is not the one being called. A
multi-release JAR can create a related mismatch when the build and runtime select different classfile versions.

The qualification must identify the resolved binary together with the relevant loader, classpath and platform
assumptions. A different effective definition requires requalification rather than reuse of the old result. ADR-0073
owns the platform identity law; the frontend and realization designs must bind their actual readers to it.

Tests should deliberately change classpath precedence, introduce duplicate classes, and run supported JDK variants
against the same package. The test passes only if the changed binding is detected or the original security claim is
explicitly inapplicable.

### TH-03. Error-recovered source promoted into HIR — T02, T03

An IDE frontend can continue parsing after a missing expression by creating a placeholder. That is useful for error
messages, but a placeholder is not an Admission judgment. If the recovered tree is serialized into a normal HIR row and
later sealed, an incomplete method could acquire authority it never earned.

The handoff to Establishment must distinguish complete source-derived meaning from recovery state. Recovery data may
support diagnostic rendering only under its permitted lifecycle. The implementation cannot infer completeness merely
because all physical fields of a row have values.

Tests should inject incomplete expressions and unresolved helper calls before the HIR handoff. They should then attempt
to establish or reuse the result. The expected behavior is non-promotion, with diagnostics still available through their
own channel. Frontend and HIR Design own this boundary under ADR-0071.

### TH-04. Unicode, escaping and identifier disagreement — T01, T02

A source identifier can look identical to a generated name while containing a different Unicode sequence. A backslash
escape or a change in source encoding can also change which identifier the compiler resolves. If generated interface
metadata applies a different name comparison, Admission may observe a coordinate other than the one its author selected.

The frontend must resolve identifiers using the supported language's exact rules and bind them to Contract coordinates
through the owning nominal identity rules. Visual similarity cannot be used as an equality relation. Case conversion
must not silently rewrite a declared name.

Conformance fixtures should use composed and decomposed names, escaped identifiers, confusables and alternative
encodings. They should verify both source resolution and generated-interface binding. The Frontend/API Design owns these
fixtures, while the Contract ADRs retain the coordinate law.

### TH-05. Build-time code execution disguised as acquisition — T01

A tool may announce that it is merely scanning a classpath while loading classes whose static initializers execute. A
build may also run annotation processors or Gradle tasks as part of ordinary compilation. If untrusted source controls
one of those components, code can run with the build process's privileges before Admission analysis has even started.

The build must inventory each component that can execute code and define its trust level. The non-executing Admission
acquisition route must not use effects from those executions as semantic input. If the project chooses to support
untrusted build plugins, it needs a separately justified isolation or privilege policy.

A safe regression fixture can try to create a marker file or contact a blocked local endpoint from a test processor. The
result must make it clear whether execution was disallowed or occurred in an expressly authorized build stage. Build and
Frontend Security Design own the mechanism.

### TH-06. Malformed metadata or dual decoder meaning — T01, T03, T08

Imagine that a persisted HIR record contains a duplicate field. One decoder uses the first value while another uses the
last. If that field encodes a Version or evidence status, the consumers may disagree about what was checked. Integer
overflow in a declared length can create a related error even when the input buffer is small.

Every trusted re-entry path needs one versioned data interpretation. The Design must specify what to do with duplicate
or unknown fields and ensure that length checks cannot wrap around. It must not use host deserialization hooks that
execute code to reconstruct an untrusted record.

Tests should mutate serialized records and compare producer and consumer interpretations. An ambiguous record must
remain unqualified instead of acquiring the meaning of whichever parser happens to accept it. Material and Protocol
Serialization Design own the encoding and decoder.

## 5.2. Reachable Java/Kotlin semantics and hidden capabilities

### TH-07. Getter, overloaded operator and hidden side effect — T02

The expression `input.region` appears to read a value, but a custom getter or delegated property may execute user code.
A comparison may likewise invoke an overloaded operator whose implementation reads a global flag. If the frontend treats
the surface syntax as a pure observation, a hidden capability becomes a determinant of Admission.

A source form may be admitted only when all relevant targets and their behavior are covered by the supported semantics.
The frontend must distinguish an Input-declared coordinate observation from an arbitrary host getter. The existence of a
Kotlin property does not establish that correspondence.

Tests should provide custom getters that perform harmless observable effects, delegated properties with changing values,
and `equals` implementations that call other helpers. Those cases must be rejected unless their complete meaning is
explicitly supported. Admission Source Qualification and Frontend Verification Design own the checks.

### TH-08. Reflection, MethodHandle and dynamic call-target gaps — T02

A call graph may conclude that a predicate is pure because its visible calls appear harmless. An unexpected reflective
target or a method handle can still invoke effectful code. Dynamic class generation and proxy dispatch can alter the
target after a static analyzer has completed its work.

Source qualification cannot rely on a list of known bad reflection methods. For each admitted dynamic form, the compiler
needs a defensible account of its possible targets and the meaning of each relevant target. If it cannot establish that
account, the expression remains unsupported. Executing the original callback to discover its target is not a safe
fallback.

Negative tests should introduce late-bound targets and classes whose method bodies differ from the analyzed target. The
verifier should expose the missing closure instead of yielding success. ADR-0073 and Verifier Design own the
platform-specific treatment. Java reflection research [R18] illustrates why target closure deserves an explicit
assumption.

### TH-09. Static initializer, environment and nondeterministic default — T02

A Kotlin `object` may run initialization code on first access. An otherwise familiar library method may use the current
default locale or timezone. The same Admission source can therefore behave differently after process configuration
changes without any Input value changing.

The compiler must not assume that a method is deterministic because it belongs to a standard library. A supported
semantic profile must explain the relevant defaults, initialization behavior and external observations. If ambient state
would determine the judgment and the Contract does not authorize that observation, the source cannot be qualified by
silently fixing an arbitrary default.

Conformance tests should run the same legal Input under different locales, timezones and initialization orders. They
should show either invariant supported meaning or an explicit unsupported case. The Source Catalog and Platform
Qualification Design own these profiles.

### TH-10. Callback hidden inside collection or library operation — T02

A concise collection expression may execute substantial hidden code. Calling `contains` on a host collection can invoke
user-defined equality. Sorting may call a comparator with mutable captured state. Iterating a map may expose a host
order that the Input `Association` meaning never promised.

Qualification must account for the callbacks and observations that the selected operation can reach. The implementation
cannot treat Kotlin sequence pipelines as pure simply because the library call has a familiar name. In particular,
membership meaning must not be replaced by accidental iteration order.

Tests should use comparators that mutate state and collections whose traversal order changes between constructions. The
result must either follow the established Input meaning or reject the unsupported host behavior. Admission Source and
Collection Catalog Design owns these tests.

### TH-11. Mutable alias, escape and concurrent carrier change — T02, T04

An outer collection may be immutable while each contained object remains mutable. A local accumulator can also retain an
alias to Input through a captured lambda. If another thread changes a referenced element after Input presentation, an
Admission implementation that reopens the carrier may observe a value that was never established.

Permitted local mutation must remain local to the judgment. The alias and escape analysis must show that the
implementation cannot modify or re-read mutable backing objects in a way that changes the lawful operand. Copying only
the outer collection is not sufficient evidence.

Tests should mutate nested objects concurrently after establishment and exercise closures that escape their local scope.
Admission must still observe only the established presentation. Input Material and Admission Alias Analysis Design own
the realization.

### TH-12. Exception laundering into `Rejected` — T02, T05

Suppose a helper divides by zero and the emitted method catches an exception before returning `false`. The authoring
source might contain that branch, but an Admission `Rejected` can be established only if the complete behavior is
supported by ADR-0065's judgment law. A verifier failure or interrupted runtime computation is never a legal negative
result merely because Java has a Boolean return type.

The implementation must preserve the guard and exceptional-domain semantics of supported source. It must distinguish a
legal, completed judgment from a failed compiler operation or physical execution.

Tests should cover index bounds, guarded division and catch blocks. They must record whether Admission entered and
completed, rather than asserting only that the host method returned `false`. The Admission Verifier and Backend Designs
own these checks under the owning result law.

### TH-13. Unjustified termination and recursion closure — T02

A loop that appears finite may change its own bound. A helper may also recurse through an indirect call, or wait until
external state changes. The absence of a literal `while(true)` does not establish that the judgment completes.

V1 may support only constructs whose completion can be justified under the chosen source semantics. It must not claim a
general termination decision procedure. Where the compiler cannot prove the supported progress obligation, the construct
remains unsupported rather than being executed speculatively.

Tests should use mutable loop bounds, indirect recursion and loops whose progress depends on an external callback. The
Frontend Termination Analysis Design must show how the supported subset is distinguished from these cases.

### TH-14. Numeric, Unicode or equality drift — T02, T06, T07

A comparison can change meaning when the compiler narrows an integer or reorders floating-point operations. For example,
NaN comparisons do not obey ordinary total ordering, and positive and negative zero may be distinguishable under some
operations. Unicode character counts and decimal scale can likewise expose distinctions that an apparently harmless
optimization removes.

The supported operation profile must define which distinctions the Admission source can observe. An optimizer must
preserve those distinctions under the selected JVM semantics instead of using generic mathematical identities.

Tests should compare reference and optimized results at overflow boundaries, floating exceptional values and
representative Unicode strings. ADR-0073 owns the platform operation profiles; the Reference Evaluator and Optimizer
Designs own the checks.

## 5.3. Algorithmic complexity, denial of service and compiler availability

### TH-15. ReDoS in accepted regex patterns — T02, T05

A predicate can use a regex that matches normally on typical strings but takes enormous time on a crafted near-match.
Its logical result may be perfectly well-defined and terminating. That does not make the selected matcher safe for every
admitted input length.

The Regex Catalog must qualify the actual features supported by the selected engine. A backreference or capture may
change both cost and observable behavior. A non-backtracking replacement is permissible only if it preserves the
admitted syntax, captures and Unicode semantics, or if the supported feature subset is explicitly narrower.

Tests should use adversarial patterns together with worst-case inputs for the actual engine and platform profile. ReDoS
reviews [R05] and regex complexity research [R16] support the need to distinguish logical semantics from engine cost; a
September 2026 detector comparison [R06] remains preliminary. Matcher and Resource Design own work limits and stop
behavior.

### TH-16. Finite Cartesian-product amplification — T02, T05

Two individually bounded collections can still produce quadratic work when a predicate checks every member of one
against every member of the other. Three nested traversals can amplify the cost further. A proof that each loop
terminates does not bound the combined work at a practical level.

Admission execution needs a supported input-size and work policy wherever the chosen source forms can exhibit such
growth. Exceeding that envelope must be attributed to the appropriate resource boundary, not treated as a false
predicate. Contract Budget and Capacity laws remain separately owned.

Measurements should increase each collection size independently and then together. They should confirm that a declared
work stop occurs without creating `Rejected`. Execution and Resource Design own the measurement and stopping mechanism.

### TH-17. Parser, CFG, call graph, solver or summary explosion — T01, T02, T06

An adversarial source file can contain a very deep expression, and a call graph can have so many potential edges that
its summaries become impractically large. A solver may return `unknown` after exploring a difficult path condition.
These are attacks on compilation itself, even if no generated Admission program ever runs.

The compiler must bound supported analysis work or provide containment that produces a truthful unsuccessful result.
Allocation arithmetic and nested graph traversal must not overflow or exhaust a process stack merely because the source
is hostile. Returning a plausible partial analysis as complete is prohibited.

Tests should separately vary expression depth, graph breadth and solver difficulty. They should include cancellation and
memory-pressure paths, and verify that incomplete evidence is never promoted into Establishment. Compiler Engine and
Verifier Resource Design own the implementation.

### TH-18. Archive expansion bombs and nested resources — T01

A compressed dependency can be small on disk yet expand to an enormous size. Nested archives can multiply the effect.
Source acquisition that checks only the outer file's byte count may therefore exhaust memory or storage before semantic
analysis begins.

The acquisition design must bound the amount of decompressed material it will inspect. It also needs a policy for nested
archives and extraction destinations, because a path that escapes the intended workspace creates a separate integrity
risk.

Fixtures should include high compression ratios and bounded archive nesting while remaining small enough to test safely.
The observed stop must be attributed to acquisition rather than to Admission `Rejected`. Artifact Acquisition Security
Design owns the policy.

### TH-19. Diagnostic and evidence amplification — T06, T09

Malformed source can trigger the same diagnostic from many locations. A proof analyzer might also generate a very large
trace, or provenance relationships could form a chain too costly to render. A diagnostic system can therefore exhaust
resources even when the compiler correctly refuses the underlying source.

The Design must bound diagnostic generation and outward emission independently of the Contract judgment. When details
are intentionally truncated, the message must say so. Truncation cannot silently turn incomplete analysis into a
confident explanation.

Tests should induce repeated errors and large but bounded evidence graphs, then verify both resource handling and
truthful output. Diagnostic and Compiler Resource Design own the concrete limits.

## 5.4. Establishment, Composition, actual operand and Authority confusion

### TH-20. Raw/canonicalized value confusion — T04, T05

Assume Input presents the region `"kr"` and Canonicalization establishes `"KR"`. A user writes `input.region == "KR"`,
and the Admission Definition is meant to observe the established Representative. If its implementation reads the old
carrier, the result may incorrectly be `Rejected`. A more serious case occurs when Admission approves `"KR"` but
Lowering uses the original `"kr"` for a protected operation.

The security rule is that the same legally determining presentation must be judged and consumed. It applies whether
Canonicalization is omitted, selects one coordinate or selects all eligible coordinates. The detailed operand and Basis
semantics remain **ADR-BLOCKED** on ADR-0065, ADR-0066 and ADR-0048.

Tests must combine selected and unselected coordinates within one Input and observe both Admission and Lowering.
Composition and Execution Design own the implementation after the Contract law closes.

### TH-21. Partial Canonicalization refusal bypass — T04

A Canonicalization occurrence can fail even though an internal assessment of one coordinate succeeded. A faulty consumer
might take the successful value, combine it with untouched Input fields and run Admission as though the whole occurrence
had succeeded.

The established outcome of the whole Canonicalization occurrence controls whether the next stage can enter. Partial
assessment data is not an alternative source of continuation authority. Where Canonicalization is legitimately omitted,
that omission must follow the owning Composition law rather than be represented as an invented successful occurrence.

Tests should create mixed success/refusal assessments and assert that Admission does not enter. They must not report
this case as Admission `Rejected`. Composition and Material Design own the implementation, subject to ADR-0066.

### TH-22. Current World, selection and version replay — T04, T08

An Admission Definition can be shared across several Interactions. Suppose one use selects a Canonicalization Law while
another does not. A cache that indexes only the Admission Definition may return `Admitted` from the first Application
for the second, even though the actual determining presentation differs.

Occurrence results are qualified by the current Application and its determinants. A changed World, Version, policy or
selected Law may invalidate a result without changing the Definition identity. The cache must consult that validity
relation instead of treating a matching ID as authorization.

Tests should reuse the same Definition in deliberately different bindings and inject a previously successful result. The
incorrect transfer must be rejected or requalified. Establishment, Material and Reuse Design own this boundary.

### TH-23. Ambiguous or fabricated Required Basis — T04

Two Input occurrences can have the same values and Host type while representing different semantic occurrences. A
resolver that chooses the newest occurrence by timestamp may pair Admission with the wrong Input. A similar problem
arises when two Canonicalization candidates match the same string key.

Required Basis resolution must obey the exact reference domain and cardinality established by the owning ADRs. Ambiguity
cannot be resolved by choosing the first, nearest or most recently cached record. Because the canonicalized operand law
is not yet closed, this case is **ADR-BLOCKED** on ADR-0065, ADR-0063 and ADR-0048.

Tests should create deliberately ambiguous candidates and assert that no arbitrary basis is selected.
Composition/Material Design owns the final resolution mechanism.

### TH-24. Double occurrence from retry, race or cache hit — T04, T05, T08

Two workers may evaluate the same semantic Application at once. If they both publish a result independently, consumers
could see two competing Established Occurrences. An interrupted worker may also leave a partially written record that a
retry mistakes for a completed result.

The implementation must preserve the owning occurrence and publication law regardless of scheduling. Duplicate physical
computation is not authority to create a second occurrence. Recovery may discard incomplete work but cannot rewrite
established history.

Tests should control worker interleavings and inject cancellation between writing and publication. They should also
reuse physical slots deliberately to expose stale-handle assumptions. Establishment and Compiler Engine Concurrency
Design own this behavior.

### TH-25. Candidate, analyzer or diagnostics authority laundering — T03, T04

A typed reference that points into HIR storage is not the same thing as an Established Definition reference, even if the
two happen to share an integer representation. Treating a verifier pass flag or diagnostic snapshot as Established
Material would elevate analysis evidence into Contract authority.

Every consumer must require the producer's correct reference domain and lifecycle-qualified projection. Incomplete or
speculative data cannot be made authoritative by copying it into a frozen table. The Design must preserve this
distinction even when physical arrays are shared for performance.

Tests should exchange reference domains and attempt to read recovery snapshots as established results. All such attempts
must fail qualification without changing existing material. HIR, Establishment and Protocol Design own the mechanism.

## 5.5. Verification, optimization, classfiles and runtime

### TH-26. Unsound effect or call-target summary — T02, T06

An effect summary may state that a helper reads no external state even though the helper invokes a callback that reads a
system property. A verifier that trusts that summary can approve a predicate whose actual meaning is not closed.

Each reusable summary must identify the exact implementation and environment for which it was established. Its relevant
effect and exception claims must have defensible support, and changes in dependencies must trigger the appropriate
requalification. A summary that cannot account for reachable behavior cannot establish a legal Admission source.

Tests should deliberately omit an effect from a test summary and compare the conclusion with the concrete behavior of
the helper. Source Analysis and Verifier Design own the defense, using the semantic profiles governed by ADR-0073.

### TH-27. Proof or certificate applies to another subject — T06, T08

A proof can be correct for one IR generation and inapplicable to another. If a cache attaches the proof to a
superficially similar transformed method, the compiler may claim an optimization is safe without checking the
transformation that actually occurred.

The relying stage must verify that the evidence identifies the right subject, the property proved and the assumptions
under which the checker ran. A solver timeout, an unrecognized certificate or a stale library profile must remain
inconclusive. Stored evidence does not confer authority merely by being authentic.

Tests should swap certificates between methods and change a dependency after evidence is produced. Verification and
Reuse Design own the current-qualification policy.

### TH-28. Optimization removes a security-critical guard — T06, T07

Imagine a guard that prevents division by zero. An optimizer may conclude that the guarded branch is unreachable under a
faulty range assumption and move the division before the guard. The final Boolean might still match on ordinary inputs,
while a formerly safe input now triggers an exception.

Security-preserving optimization must account for reachability, exceptional behavior and stage entry as well as the
final result. Moving a predicate across the Canonicalization boundary also requires preserving the meaning of refusal
and non-entry. A Boolean-only test oracle is therefore insufficient.

The implementation should test guard-motion and stage-fusion counterexamples against a reference route. Targeted IR
mutation and scoped translation validation are candidate techniques, as illustrated by [R01–R03] and [R22]. Optimizer
and Verification Design own the method.

### TH-29. Short-circuit, exception and floating-point reorder — T06, T07

Short-circuit evaluation avoids executing later expressions when an earlier condition already determines the result. A
parallel reduction can evaluate those later expressions anyway. If one is a guarded partial operation, this changes the
supported semantics. Floating-point reassociation can also change the result for NaN or signed zero.

The optimizer must justify each reordering for the actual operations involved. An algebraic transformation that is safe
over total mathematical integers cannot be applied automatically to JVM floating point, regex callbacks or exceptional
source.

Tests should force the early condition to decide the result while a later operation would otherwise fail. They should
also compare ordered and reordered operations on edge-case values. Optimizer and Backend Design own the transformation
rules.

### TH-30. JVM linking, JIT or instrumentation drift — T07

A compiled class can pass structural checks but execute with different behavior after a runtime agent instruments it.
Dynamic class replacement or a changed library can also change the method ultimately invoked. These events can
invalidate semantic evidence without changing the source file that was originally verified.

The Admission assurance must state its supported JDK and runtime profile. It must separately address or exclude
instrumentation, JNI, native libraries and dynamic class replacement where the implementation cannot verify their
effects. It cannot promise to control unmodeled JIT defects.

Tests should execute the same supported corpus across the claimed platform matrix and compare its behavior with a
reference route. JVM Platform and Deployment Security Design own this qualification.

### TH-31. Classfile parser/producer disagreement — T01, T07

A classfile reader can accept an attribute that a later verifier rejects. Invalid constant-pool indices or stack-map
frames may cause another parser to fail during lazy decoding rather than at the initial read. A backend may produce
bytes that seem plausible but are not legal for the target JVM.

Generation must be followed by structural verification appropriate to the emitted target. Semantic preservation is a
different assurance and must be checked independently. The Design also needs overflow-safe classfile length handling and
deterministic treatment of malformed metadata.

Tests should mutate attributes, stack maps and constant-pool references before and after transformation. Oracle's
Class-File API and the JVMS [R07, R09] describe the platform boundary. JVM Frontend and Backend Design own the concrete
checks.

### TH-32. Abstract verifier accepts behavior concrete target rejects — T06, T07

An abstract range analysis may conclude that a value stays within permitted bounds while the concrete JVM program
reaches an unmodeled path. The verifier's approval then becomes a false statement about what execution can do.

Kontrakt needs a checkable relationship between the abstract facts it uses and the concrete behavior it claims to
protect. A proof for a simplified model does not automatically cover host callbacks, exceptional paths or all runtime
library variants. When a relevant behavior is unmodeled, the result must remain inconclusive.

Tests should construct concrete counterexamples for the supported abstract operations. eBPF verifier research [R10–R12]
supplies a useful failure pattern, although its machine and privilege model differ from JVM Admission. Verifier Design
owns Kontrakt's own proof obligations.

## 5.6. Storage, publication, confidentiality and supply chain

### TH-33. Poisoned or stale cache/product evidence — T08

A stored Admission result can contain a valid hash and still refer to the wrong World. A cached proof can also be
corrupted or paired with a different compiler profile. Simply checking that a cache entry exists does not qualify it for
the current application.

Every reused product needs integrity and context checks appropriate to its actual role. A corrupt, incomplete or
foreign-domain entry must not become Established Material. The system must make stale and revoked knowledge
distinguishable from current knowledge.

Tests should inject bit corruption, cross-World replay, incorrect Versions and interrupted writes into retained
products. Material, Reuse and Compiler Engine Security Design own the implementation.

### TH-34. Partially published or ABA-reused material — T08

Suppose a worker publishes a reference before all of its producer-owned fields are visible. Another worker can read a
mixture of old and new values and construct a result no Authority ever established. A reused array slot may create a
similar error when a stale handle appears to identify the current generation.

The physical storage strategy must provide coherent publication and exact reference qualification. This SADR does not
prescribe VarHandle operations, barriers or a locking layout. Those choices belong to Design, but their outcome must
satisfy the Established lifecycle.

Tests should pause writers at publication boundaries, cancel them, and reuse slots while readers continue. A reader must
never accept an incomplete or mismatched record. Establishment, Material and Concurrency Design own the strategy.

### TH-35. Diagnostic leakage and adaptive predicate oracle — T09

Detailed diagnostics can tell a developer why Canonicalization caused Admission to reject a value. The same details can
become a secret-disclosure oracle if an unauthorized caller may submit many inputs and compare messages. Even counts,
timing and whether a cache hit occurred can reveal information about a protected predicate.

Diagnostic output therefore depends on audience and purpose. A local developer view may reveal information that a
remotely accessible service must redact or omit. Retained traces and crash reports need their own disclosure policy
rather than inheriting authorization from the interactive diagnostic.

Tests should submit paired inputs that differ only in a protected fact and compare outward channels. The Diagnostic
Security Design owns the redaction, rate and output controls.

### TH-36. Compromised library, CI, signing or release workflow — T01, T10

A compromised annotation processor can modify generated code after source review. A stolen signing credential may
authenticate an artifact that the project never intended to release. These problems arise outside the Admission
predicate but can invalidate any claim made about the delivered compiler.

The whole-machine build and release process must identify its actual dependencies and code-executing steps. A signature
supports authenticity under its key assumptions; it does not prove that the source is benign or that compilation
preserved semantics. Supply-chain practice should follow an explicit trust policy informed by SLSA and NIST [R13–R15].

Tests should substitute a dependency or build artifact in a controlled pipeline and verify that the expected provenance
policy detects the change. Whole-machine Source, Build and Release Security owns these checks.

### TH-37. Rollback and revoked evidence resurrection — T08, T10

An artifact may have a valid historical signature while being unsafe for the current runtime. For example, a compiler
release may revoke a semantic profile after discovering a verification defect. A cache that continues trusting proofs
qualified by the old profile can reintroduce the very condition the fix was meant to remove.

Validity must include applicable revocation and migration state, not just stored authenticity. The exact invalidation
relation must follow the actual relying claim and its determinants. This may require refusing to reuse old products even
if their content fingerprint is unchanged.

Tests should restore previous generations after a simulated profile retirement and confirm that current consumers do not
accept revoked evidence. Release, Reuse and Platform Security Design own the policy.

### TH-38. Shared-service denial or side-channel leakage — T08, T09

A future shared compiler service could let one tenant submit a very expensive predicate while other tenants wait for the
same analysis workers. Timing or cache contention might also reveal that another tenant compiled a particular source
pattern. These risks require a shared deployment; a local V1 tool need not claim that it already provides tenant
isolation.

If a shared service is introduced, its security architecture must qualify tenant identities and resource boundaries. It
must also state what a tenant can infer through timing and shared cache behavior. Fairness and confidentiality are
distinct properties that require separate evidence.

Tests should simulate concurrent tenants under controlled workloads and observe both resource isolation and outward
information leakage. Whole-machine Shared-Service Security Design owns this conditional V2-facing work.

### TH-39. Confused security profile or governance scope — T04, T06, T08

An Admission proof may be valid under one Policy World while a consumer requests an outcome under another. If the cache
indexes only a common enum value or shares a namespace across scopes, the consumer can mistakenly accept the proof as
current.

Security qualification must bind the evidence to the exact relevant scope without creating a new Contract Definition
identity. The compiler cannot replace an established governance or applicability relation with an internal
security-profile label.

Tests should produce identical-looking payloads in deliberately different scopes and then attempt to exchange their
evidence. Compiler Security Qualification and Governance/Protocol Design own the guard against cross-scope reliance.

### TH-40. Information preserved in forbidden derivative form — T08, T09

Removing raw Input from a diagnostic is not enough if the diagnostic still prints a hash that identifies one of a few
possible values. A result count may reveal membership, and repeated timing observations may expose whether two values
fell into the same canonical equivalence class.

Confidentiality policy must cover derived information as well as the original bytes. The relevant test is what an
authorized or unauthorized observer can infer from the complete outward channel. This does not imply a universal V1
constant-time or formal noninterference guarantee.

Tests should vary only the protected portion of an input and compare what each declared audience can observe. Diagnostic
and Information-Flow Security Design own the analysis.

---

# 6. External State-of-the-Art and Research Assessment

## 6.1. Compiler optimization correctness and security

Alive2 checks refinements of LLVM IR using symbolic reasoning and SMT [R01]. It provides an example of making the
claimed semantics of an optimization explicit. Its published limitations include transformations it does not cover, so
its success cannot be generalized to arbitrary JVM methods. Kontrakt may learn from that approach without adopting
LLVM's semantic model as its own authority.

Optimuzz, presented at PLDI 2025, combines optimization-directed fuzzing with translation validation to uncover
incorrect compiler transformations [R02]. EuroSec 2026 research investigates security bugs introduced by compilation
using IR mutation and coverage-guided fuzzing [R03]. These approaches motivate testing the precise transformations that
Kontrakt actually performs. A guard moved above its qualifying condition deserves a targeted test, even if an ordinary
happy-path corpus never reaches the exceptional case.

Silent Bugs Matter, published at USENIX Security 2023, illustrates that compiler-introduced defects can have security
consequences not captured by a simplistic correctness check [R22]. For Admission, a comparison of final Boolean results
is not a sufficient oracle when stage entry or a protected observation changes. Reference judgments and
transformation-specific evidence must cover the property the consumer relies upon. Fuzzing supplies counterexamples and
coverage information; it does not prove universal equivalence.

## 6.2. Parser differentials and archive security

My ZIP isn't your ZIP, published at USENIX Security 2025, tested multiple ZIP parsers and found cases in which they
assigned different meanings to the same archive [R04]. Some of its practical examples involve signed nested JARs. This
result is relevant because Kontrakt may read a Java dependency during acquisition and later rely on the platform to load
a class from that dependency.

The transferable lesson is that the actual bytes and archive member selected during analysis must remain the subject of
subsequent claims. The implementation should test the specific parser and loader combination it uses. Rejecting
ambiguous archives addresses this interpretation problem, but it does not protect against a malicious dependency whose
bytes are unambiguous, or against code executed by a build plugin.

## 6.3. Regex denial of service and modern engines

An AsiaCCS 2025 review examines ReDoS detection and defense across practical regex engines [R05]. Its findings do not
support a universal guarantee based only on pattern syntax. ICALP 2026 research examines complexity for richer
expression forms involving backreferences [R16]. A September 2026 arXiv study compares ReDoS detection tools and reports
disagreement among them; it remains preliminary evidence rather than a peer-reviewed result [R06].

The Admission Regex Catalog must therefore qualify the actual matcher it intends to use. If a non-backtracking engine
cannot implement a supported capture feature with the same semantics, replacing the matcher changes the accepted
judgment. The Design may instead restrict its supported feature subset or apply an explicit work envelope. The SADR
requires that such a choice be supported by both semantic conformance and adversarial cost tests.

## 6.4. Java Platform and classfile guarantees

Oracle's Java Class-File API documentation distinguishes classfile parsing, building, transformation, and explicit
verification [R07]. It also describes lazy model behavior that can defer some parsing failures. A backend that
successfully constructs a classfile has therefore not established every structural property of that file. The JVM
Specification defines the separate verification rules applied by the platform [R09].

For Kontrakt this creates two distinct proof obligations. The emitted file must be structurally legal for the declared
target, and its behavior must preserve the Admission meaning. Neither obligation implies the other. A valid classfile
may implement the wrong predicate.

Oracle also documents that the Java Security Manager is permanently disabled from JDK 24 onward [R08]. This matters when
considering execution of untrusted helpers: successful class loading and bytecode verification do not provide
general-purpose isolation of filesystem or network effects. Any stronger containment claim needs an actual
deployment-level mechanism.

## 6.5. Verifiers and abstraction soundness

CAV 2023 research on the eBPF verifier studies the soundness of range analysis and derives concrete counterexamples to
faulty abstract conclusions [R10]. An IEEE S&P 2025 survey examines remaining memory-safety challenges in the eBPF
verifier ecosystem [R11]. An eBPF Foundation report from 2026 discusses security hardening following findings that
include verifier or runtime failures [R12].

The useful analogy is the danger of treating an abstract-analysis pass as a guarantee about concrete execution when the
abstraction omits relevant behavior. Admission should require a checkable relation between its source summaries and the
JVM behavior it relies upon. The analogy has limits: eBPF executes under a different privilege system and instruction
model. Kontrakt must derive its own obligations rather than copy eBPF-specific restrictions.

## 6.6. Source-flow analysis and differential oracles

CodeQL documents path queries that connect security-relevant sources with sinks [R17]. Such a path can help explain why
an Admission expression might reach an external capability. It is not, by itself, a complete proof that all legal and
illegal observations have been identified. Research on Java reflection explains why dynamically selected call targets
can challenge static reachability models [R18].

JITfuzz provides a JVM-oriented example of coverage-guided testing against Just-in-Time compilers [R19]. Research
published in Computers & Security in 2025 studies differential-test oracle quality for JavaScript JIT compilers,
including the problem of nondeterministic test environments [R20]. Its empirical target is not the JVM, but its warning
applies whenever the reference and actual execution can observe different ambient state.

Kontrakt should keep reference-vs-realization tests explicit about the environment they control. A test is weaker when
both implementations rely on the same erroneous semantic profile. A diagnostic path or a fuzzing corpus is useful
evidence only for the property it actually exercised.

## 6.7. Software supply-chain security and continuous verification

SLSA 1.2 describes how provenance can strengthen confidence about source and build processes [R13]. NIST SSDF 1.1
provides process guidance for secure software development, while its proposed 1.2 revision was an initial public draft
at the time of this document [R14, R15]. These sources help frame the compiler's build and release obligations, but they
do not establish that its Admission semantics are correct.

ClusterFuzzLite documents a way to run fuzzing as part of continuous integration, including JVM-oriented projects [R21].
A practical Kontrakt workflow can preserve reproducing inputs from malformed-JAR, source-closure and optimizer tests as
regression fixtures. This improves detection after changes, but finite coverage remains different from an exhaustive
safety argument.

## 6.8. Basic security failure classes that still apply

Unusual compiler failure modes do not replace ordinary security engineering. The following risks matter where the
corresponding feature or deployment surface actually exists. Their presence here is not a claim that Kontrakt is a web
service, uses native memory, or currently exposes each attack surface.

### Unauthorized access to source and diagnostic evidence

A remotely accessible compiler endpoint might reveal Admission predicates or retained Input evidence to a caller who did
not author them. The deployment must authenticate the requester and authorize the particular disclosure. Admission does
not itself authenticate users. A local developer build may operate under a different authorization model and should not
claim remote-service protections that it has not implemented.

### Filesystem traversal and replacement races

Archive extraction or diagnostic export can be tricked into writing outside the intended workspace if it accepts `../`
paths or follows attacker-controlled symlinks. A file may also be replaced after a check but before its bytes are used.
Acquisition and filesystem Security Design must constrain the resolved path and bind use to the validated object. Tests
must include symlink replacement and traversal attempts.

### Injection into external execution boundaries

A source filename or metadata field can become dangerous if an implementation concatenates it into a shell command or
other external language. The safe Admission design does not execute source merely to learn its meaning. If separate
build tooling invokes a command, database, or network service, its Design must define arguments and capabilities without
treating untrusted text as executable syntax.

### Unsafe deserialization and memory access

Persisted HIR and verifier evidence must be decoded as qualified data rather than used to instantiate attacker-selected
executable host objects. A primitive storage implementation also needs bounds and lifetime checks. An incorrect slab
offset or integer overflow could corrupt another record even though the high-level Contract meaning is immutable.
Material and native-boundary Design own these implementation checks.

### Resource exhaustion and error handling

A regex can occupy a matcher for excessive time, while a malformed classfile can exhaust a parser. Error paths can then
leak internal data or misattribute a stop. Security Design needs finite-resource behavior and controlled reporting for
each actual stage. The owning Contract law still determines whether any Admission judgment was established.

### Weak defaults and downgraded assurance

An unsupported Java method must not silently fall back to direct execution because a verifier was disabled. If the
product offers a lower-assurance mode, its scope must be explicit, and a stronger security claim must not be reused in
that mode. Design should test failure paths and configuration changes, not only intended settings.

### Cryptography, release provenance and incident response

Hashes are useful for integrity and reuse, but a hash alone does not establish Contract authority. If signed artifacts,
attestations, remote caches or encrypted retained evidence are used, whole-machine Security Design must specify the keys
and trust assumptions. Security incidents also require a way to identify affected versions and invalidate
no-longer-trusted evidence. The exact implementation belongs to the build and release boundary.

### Auditable attribution with least authority

The compiler must retain enough authorized provenance to explain which current Application a security-relevant result
belongs to. That does not justify indefinite retention of every raw Input. A frontend parser normally needs source
bytes, not permission to execute them. A Diagnostic producer can often explain a source location and a judgment
relationship without receiving all values. A cache reader should not gain the ability to mutate Established Material.

The practical controls depend on deployment. An IDE, a local build and a future shared service need different
authorization and disclosure assumptions. The SADR must state which assumptions actually apply before claiming isolation
or confidentiality.

## 6.9. Research sources and limits of transfer

The sources below were examined for the 2026-10-11 draft. An official platform specification, a peer-reviewed finding, a
project manual, and a preprint do not carry equal evidentiary weight. Each entry records what it supports, not a
technology that Kontrakt is obliged to adopt.

| ID  | Source and date                                                                                                                                                                                                           | Relevant support and caution                                                                                                                                        |
|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| R01 | [Alive2 — official repository and tooling](https://github.com/AliveToolkit/alive2)                                                                                                                                        | Symbolic/refinement checking of LLVM IR transformations; explicitly limited coverage, including interprocedural limitations. Not proof of JVM or Admission meaning. |
| R02 | [Kwon et al., *Optimization-Directed Compiler Fuzzing for Continuous Translation Validation*, PLDI 2025](https://doi.org/10.1145/3729275)                                                                                 | Combines optimization-directed grey-box fuzzing and translation validation; useful testing strategy, not an automatic guarantee for Kontrakt.                       |
| R03 | [*Detecting Compiler-Introduced Security Bugs via IR Mutation and Coverage-Guided Fuzzing*, EuroSec 2026](https://doi.org/10.1145/3803525.3804981)                                                                        | Optimization-related security defects and directed IR fuzzing; transfer as a test hypothesis rather than a verified Kontrakt defense.                               |
| R04 | [You et al., *My ZIP isn't your ZIP: Identifying and Exploiting Semantic Gaps Between ZIP Parsers*, USENIX Security 2025](https://www.usenix.org/conference/usenixsecurity25/presentation/you)                            | Differential parser inconsistencies, including practical nested-JAR/signature cases; does not imply any exact Kontrakt bug.                                         |
| R05 | [*SoK: A Literature and Engineering Review of Regular Expression Denial of Service*, AsiaCCS 2025](https://doi.org/10.1145/3708821.3733912)                                                                               | Modern engine behavior changes ReDoS risk; mitigations are heterogeneous and sometimes incomplete.                                                                  |
| R06 | [Mahassadi et al., *An Empirical Analysis of ReDoS Vulnerabilities and ReDoS Detection Tools*, arXiv 2609.10294, September 2026](https://arxiv.org/abs/2609.10294)                                                        | **Preprint, not peer-reviewed at this drafting time**; reports disagreements between detection tools.                                                               |
| R07 | [Oracle Java SE 26, `java.lang.classfile` package documentation](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/lang/classfile/package-summary.html)                                                   | Official classfile parsing/generation/transformation semantics; separate explicit verification and caveats for lazily parsed models.                                |
| R08 | [Oracle JDK 24, *The Security Manager Is Permanently Disabled*](https://docs.oracle.com/en/java/javase/24/security/security-manager-is-permanently-disabled.html)                                                         | Official documentation that legacy JVM Security Manager protection cannot be enabled as of JDK 24.                                                                  |
| R09 | [Java Virtual Machine Specification SE 25, §4.10 Verification](https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-4.html)                                                                                           | JVM structural/type verification, not an Admission semantic equivalence proof.                                                                                      |
| R10 | [*Verifying the Verifier: eBPF Range Analysis Verification*, CAV 2023](https://link.springer.com/chapter/10.1007/978-3-031-37709-9_12)                                                                                    | Tests abstract verifier soundness and generates concrete counterexamples. eBPF architecture differs from JVM.                                                       |
| R11 | [*SoK: Challenges and Paths Toward Memory Safety for eBPF*, IEEE S&P 2025](https://doi.org/10.1109/SP61157.2025.00134)                                                                                                    | Highlights practical verifier/runtime safety limitations; analogy for proof scope, not imported machine law.                                                        |
| R12 | [eBPF Foundation, *Strengthening eBPF Security: Progress on Audit and Runtime Hardening*, 2026](https://ebpf.foundation/strengthening-ebpf-security-progress-on-audit-and-runtime-hardening/)                             | Reports verified research/audit findings involving bypasses, races and DoS in a different execution system.                                                         |
| R13 | [SLSA 1.2 — official supply-chain specification](https://slsa.dev/spec/v1.2/)                                                                                                                                             | Build/source provenance tracks and assurance framing; not source/Contract correctness proof.                                                                        |
| R14 | [NIST SP 800-218, *SSDF Version 1.1*, 2022 final](https://csrc.nist.gov/pubs/sp/800/218/final)                                                                                                                            | Secure software development and vulnerability response practices.                                                                                                   |
| R15 | [NIST SP 800-218 Rev.1, *SSDF Version 1.2*, December 2025 Initial Public Draft](https://csrc.nist.gov/pubs/sp/800/218/r1/ipd)                                                                                             | **Draft** updated guidance, not a superseding final standard.                                                                                                       |
| R16 | [*On the Complexity of the Matching Problem of Regular Expressions with Backreferences*, ICALP 2026](https://doi.org/10.4230/LIPIcs.ICALP.2026.135)                                                                       | Complexity results for richer regex feature sets; does not show the worst case for an unqualified actual engine.                                                    |
| R17 | [GitHub CodeQL — Creating Path Queries, official documentation](https://codeql.github.com/docs/writing-codeql-queries/creating-path-queries/)                                                                             | Traceable source-to-sink data flow models; path evidence is not complete purity/termination proof.                                                                  |
| R18 | [*Challenges for Static Analysis of Java Reflection: Literature Review and Empirical Study*, research record](https://research.tue.nl/en/publications/challenges-for-static-analysis-of-java-reflection-literature-revi/) | Real Java reflection frequently challenges sound static target resolution; older foundational work.                                                                 |
| R19 | [*JITfuzz: Coverage-guided Fuzzing for JVM Just-in-Time Compilers*, IEEE 2023](https://ieeexplore.ieee.org/document/10172743/)                                                                                            | JVM-specific JIT fuzzing precedent, not proof against all platform/compiler drift.                                                                                  |
| R20 | [*Fuzzing JavaScript JIT Compilers with a High-quality Differential Test Oracle*, *Computers & Security* 2025](https://doi.org/10.1016/j.cose.2025.104660)                                                                | Examines nondeterministic test oracles and false positives; actual experiments concern JavaScript, not JVM.                                                         |
| R21 | [Google ClusterFuzzLite — official documentation](https://google.github.io/clusterfuzzlite/)                                                                                                                              | CI-focused fuzzing, including JVM language support; coverage is a measurable tool property, not blanket assurance.                                                  |
| R22 | [*Silent Bugs Matter: A Study of Compiler-Introduced Security Bugs*, USENIX Security 2023](https://www.usenix.org/conference/usenixsecurity23/presentation/xu-jianhao)                                                    | Highlights security consequences beyond simplistic compiler-output-equivalence expectations.                                                                        |

---

# 7. Assurance Architecture and Evaluation Obligations

## 7.1. A qualified source capability is an assurance subject

A V1 capability cannot be qualified at the level of a label such as “Kotlin strings.” A source construct must be
specified precisely enough that a compiler reviewer can determine what behavior it permits. For example, qualifying a
string comparison requires knowing which overload is selected and which Unicode and comparison semantics it uses. If the
implementation can invoke a user-defined helper, that helper's relevant behavior must also be covered.

For each supported construct, the Working Design should identify the language and platform profile that defines its
meaning. It must explain which Input observations are allowed and which other operations are reachable. It must also
account for exceptional behavior and for any permitted local mutation. When the construct involves loops or pattern
matching, the qualification needs a defensible completion argument and an explicit treatment of resource cost. Tests
that support acceptance should include cases that demonstrate where the supported boundary ends.

These qualifications are compiler assurance products. They do not automatically become new Admission Definition
determinants. Their relationship to Contract Material remains governed by the owning ADRs. The Working Design should
distinguish implemented capabilities from those that are conditional, unsupported, or still awaiting evidence. Any stage
relying on a qualification must also be able to determine whether a changed dependency makes it stale.

An unreachable-code finding is useful only if the compiler can justify unreachability under the selected source
semantics. A helper cannot be excluded merely because the current tests never call it. Inlining and source formatting
may change physical representation, but they must not change any legal Admission observation.

## 7.2. Keep semantic, structural and security evidence separate

A reference Admission evaluator can provide a meaning against which an optimized realization is compared. The JVM
verifier can check structural constraints of a generated classfile. Source analysis can justify why a particular getter
or helper is permitted. Fuzzing can expose cases in which two implementation paths disagree. Each contributes useful
evidence, but none establishes all the properties addressed by this SADR.

The compiler should make shared assumptions visible. If both the reference evaluator and optimized backend call the same
faulty helper, their agreement does not independently validate the helper. If two checks decode the same damaged
Material through the same parser, the comparison may reproduce the same defect. Reviewers need to know which parts of a
claim are checked independently and which are trusted inputs.

Counterexamples that invalidate a summary or transform should become reproducible regression tests. V1 does not have to
prove arbitrary JVM bytecode formally. It does have to avoid presenting limited checks as an unrestricted security
guarantee.

## 7.3. Security-specific conformance tests

### The inbound operand and its binding

Use the same Admission source against three configurations: no Canonicalization, Canonicalization of one declared
coordinate, and Canonicalization of all relevant coordinates. For the partially selected case, make neighboring fields
distinguishable so that an accidental use of the raw carrier is visible. A separate test must produce a Canonicalization
refusal after some internal coordinate assessments succeed. Admission must not enter in that last case.

Next, retain an earlier `Admitted` result while changing the World or applicable Version. A different Policy or
Canonicalization Law should also be tested. The result may remain associated with its original Application, but it must
not become the current Application's result merely because the Definition or HID matches.

### Hidden execution and value semantics

Create a getter whose body records an effect, a delegate with an observable callback, and a collection whose comparator
consults mutable state. The frontend must either justify the complete behavior under a supported semantic profile or
refuse the source. An annotation processor that performs the same effect belongs to the build execution boundary and
should be tested there instead.

For accepted numeric and text operations, compare the reference and realized judgments near integer overflow and
narrowing boundaries. Floating-point tests should include NaN and signed zero. Text cases should distinguish visually
similar Unicode sequences and default-locale behavior where relevant. The required observation is preservation of the
selected legal source meaning, not agreement on a hand-picked ordinary value.

### Collections, regexes and finite work

Construct collections with duplicate members and varying host iteration orders. Include immutable outer containers with
mutable nested objects to test that Admission never reopens a changing carrier. For regexes, use adversarial
pattern/input pairs that challenge the actual matcher. Captures and backreferences should be tested only to the extent
that the supported feature set admits them.

Resource tests must also grow nested collection loops independently of regex work. When a supported envelope is
exceeded, the result must be an appropriately attributed stop rather than an invented `Rejected`.

### Analysis and compiler availability

Provide deeply nested source expressions and classfiles containing large but bounded constant pools. Independently
increase CFG fanout and call-graph breadth to reveal algorithms with unacceptable growth. If a solver reports `unknown`
or a resource limit interrupts analysis, the compiler must not publish complete evidence from that attempt.

### Transformation and platform conformance

Compare a guarded expression before and after an optimization that would otherwise move its partial operation. Test that
short-circuit evaluation does not unexpectedly reach a failing helper. Compare floating-point reduction cases under the
exact supported platform profile. Stage-fusion tests must inspect Admission non-entry and failure attribution as well as
Boolean output.

Corrupt classfile attributes and stack-map frames to test structural verification. Then vary the runtime loader or a
supported dependency version to check whether semantic qualification remains applicable. These are separate tests: a
structurally valid classfile can still implement the wrong Admission predicate.

### Publication, diagnostics and release

Interrupt material publication and retry with a second worker. A reader must not accept a partially published record or
a result from another Reference Domain. Replay old proof records under a new World and restore an invalidated generation
to test current qualification.

Diagnostics should be tested for each intended audience. A local developer may be entitled to see a Representative that
a shared log must not show. Repeated input probes must also be considered because counts or timing can reveal
information without printing a value.

Finally, replace a dependency after review and attempt to use an earlier signed release. These tests validate the
declared build and release trust assumptions; they do not prove the Admission judgment itself.

For every negative test, record the first boundary that prevents continuation. The test report must explain whether any
earlier Material was lawfully established and which owner reports the unsuccessful outcome. Checking that a returned
Boolean is `false` cannot distinguish unsupported source from Canonicalization `Refused`, Admission `Rejected`, a
compiler failure, or a resource stop.

## 7.4. Security claim evidence and validity

Evidence must identify what it is evidence about. An effect summary may apply to one resolved helper implementation but
not to a different library version. A reference judgment may depend on the exact compiler and JVM semantic profile. An
occurrence-level result also depends on its current World and Applicable Binding. The relying stage must have enough
information to recognize a changed determinant and requalify the claim.

Evidence need not reside in a universal certificate object. It may be represented compactly or retained only while
needed, provided its exact subject and validity can be established. This SADR does not mandate a central evidence ledger
or a particular table layout.

The system must not conflate a valid result with an inconclusive one. An unsupported operation never gained a
qualification; a stale result was qualified for a different context; a corrupt record cannot be trusted to say what it
means; an incomplete computation has not finished its proof obligations; and a revoked qualification may no longer be
used even if its bytes remain intact. Those distinctions matter when a consumer decides whether to proceed.

A timeout is not a failed Admission predicate. After a crash, recovery can discard incomplete work and begin again, but
it cannot publish a result from a computation that never satisfied its required Establishment path or revise an already
established occurrence.

## 7.5. Confidentiality and information-flow tests

Every channel that releases diagnostic information needs an intended audience. A developer running a local compiler may
be authorized to inspect a value transformation. A remote caller checking a policy result may not be authorized to see
either the original value or its Representative. A log collector has a different purpose again. The disclosure policy
must be applied at the actual outward boundary rather than inferred from a `debug` label.

Tests should change only one protected property of the Input while keeping the public circumstances the same. They can
then compare the outputs visible to an observer who lacks access to that property. If the outputs differ, the Design
must explain why that information is permitted or limit the channel. Equality indicators, lengths and timing can reveal
information even when the raw string is absent.

This method provides concrete evidence about specified channels. It is not a general formal noninterference proof, and
V1 should not claim that all timing or cache side channels have been eliminated.

## 7.6. Availability metrics and stopping evidence

A benchmark must make clear what the adversary can grow. One test may increase authored source size; another may grow
the number of reachable helpers. A regex stress test should vary the input length separately from the pattern length.
These measurements are not interchangeable, because different stages can have different complexity behavior.

The Design should specify the supported workload and the point where it will stop exceeding work. Each stop needs an
owner and a truthful result classification. A wall-clock timeout can protect a process but does not establish that the
underlying algorithm has a bounded asymptotic cost. Process isolation, quotas and alternative matchers remain
implementation choices, and any such choice must preserve the admitted Contract meaning within its declared support
envelope.

---

# 8. Proposed V1 Security Baseline and V2 Seams

## 8.1. Proposed V1 Admission security gates

### V1-01 — Source acquisition does not use execution as discovery

The compiler must not invoke arbitrary user methods to determine their Admission meaning. If a build stage actually
executes a compiler plugin or processor, it must be identified as such and assessed according to its real privileges. A
fallback from failed static qualification to direct method invocation does not satisfy this gate.

### V1-02 — Each supported source form has a closed meaning

The frontend must account for the operations a supported source form can reach. It must establish which values may be
observed and which effects are possible before completing its HIR candidate. A recovered parse tree or an unresolved
helper cannot be treated as a sealed definition merely because the compiler can display a partial result.

### V1-03 — Admission uses the complete lawful inbound operand

Once the owning ADRs close the Basis rule, the compiler must implement it consistently with Canonicalization omitted,
partially selected, and fully selected. If a selected Canonicalization occurrence refuses, Admission remains unentered.
This gate remains blocked until the Contract-owned meaning is resolved.

### V1-04 — Each unsuccessful outcome keeps its owner

A source form that is unsupported does not produce Admission `Rejected`. The same is true of a compiler cancellation,
resource stop, or inconclusive verification result. The implementation must report the actual boundary and preserve
earlier established results according to their governing laws.

### V1-05 — Resource work has an accountable envelope

The compiler and execution path must have a documented treatment of resource-heavy supported operations. This includes
source analysis and pattern matching as well as the generation of diagnostic evidence. The Design must state what
happens when the supported envelope is exceeded without redefining Contract Budget or Capacity.

### V1-06 — Accepted operations are checked at their semantic boundaries

The Source Catalog must include difficult values relevant to each supported operation. Numeric tests need overflow and
exceptional floating-point cases. Text and collection tests need the precise comparisons and ordering behavior the Input
law supports. A broad library category cannot pass this gate without qualifying its actual supported forms.

### V1-07 — A reference route can challenge optimized execution

For the supported semantics, the implementation must provide a defensible way to compare intended Admission judgment
with emitted execution. The evidence must cover the context and observations relevant to each transform. Tests should
not rely only on typical successful values or a Boolean equality check that ignores failure attribution.

### V1-08 — Classfile structure and Admission preservation are checked separately

Generated bytes must be checked against the structural constraints of their declared JVM target. A separate conformance
argument must address whether they implement the established Admission meaning. Passing one of these checks does not
waive the other.

### V1-09 — Reuse does not bypass current qualification

Frozen data, HID-based reuse, cache hits, early cutoff and parallel evaluation may save work. They cannot make an
obsolete occurrence result current. The implementation must reject or requalify records whose context is wrong, whose
publication is incomplete, or whose integrity cannot be trusted.

### V1-10 — Diagnostics disclose only authorized evidence

The diagnostic system should be able to explain a mismatch between the authored Input-facing condition and the actual
Representative. It must not invent an explanation that its evidence does not support. Separate channels must honor their
disclosure policies and cap output where needed.

### V1-11 — Platform and execution assumptions are explicit

The supported JDK and classpath profiles must be identifiable. If native code, instrumentation or dynamically replaced
classes fall outside the assurance, the claim must say so. Compiling an Admission expression does not certify arbitrary
hostile Java execution as sandboxed.

### V1-12 — Negative and fault-injection tests are mandatory for acceptance

Acceptance must rely on tests that attempt to falsify the claimed boundaries. This includes malformed inputs to the
compiler, wrong call summaries, unsafe optimization candidates, interrupted publication and misleading diagnostics.
Every confirmed defect should become a reproducible regression case with a clear expected outcome.

These gates are proposed requirements, not implemented guarantees. Before acceptance, the owning Designs and
Verification/QA work must specify the supported scope and supply executable evidence for each gate.

## 8.2. Preserve V2 options without committing V1 to V2 mechanisms

V2 may retain compiler products in a persistent store or distribute them across machines. Such a system must know
whether the remote record belongs to the same qualified subject and whether its evidence was revoked. If V2 introduces a
different incremental repair scheduler, the scheduler cannot make an old Admission result authoritative simply because
it finds an unchanged dependency fingerprint.

Remote verification introduces a related issue. A certificate needs to identify the claim it checked and the trusted
checker assumptions. A shared service adds tenant identity and disclosure requirements, because an attacker may learn
from another tenant's cached work or deny them resources. These boundaries must be assessed when the corresponding
features are actually introduced.

None of those future mechanisms may become a determinant of Admission Contract meaning by default. The architecture must
not require rustc red-green scheduling, a particular Merkle representation, JVM object identity or a global
security-pass bit. A clean recomputation path and the owner of each security claim must remain identifiable even after
new reuse methods are added.

## 8.3. Which boundaries deserve early review

These priorities are not CVSS ratings and do not assert that a vulnerability is exploitable in the current code. They
express the cost of discovering a faulty trust boundary late in implementation.

### P0 — Meaning or authority could be established incorrectly

Non-executing source acquisition must be decided before the frontend relies on arbitrary Java/Kotlin helpers. Otherwise
meaning discovery may itself run hostile code. The exact inbound operand and Required Basis must also close before
downstream realization is designed, because using a different value after Admission approval defeats the purpose of the
judgment.

The HIR-to-Establishment gate is equally fundamental. It must not promote a recovered candidate or incomplete analysis
to authority. Supported call targets and their effects need a closed model, or the compiler cannot truthfully certify
the predicate. Finally, verification and optimization must define the scope of their preservation claims before a
transform is permitted to remove a guard. These are P0 because later patches cannot reliably repair a compiler that has
already encoded the wrong meaning.

### P1 — A correct semantic design may still be unsafe in operation

Regex and nested collection calculations can exhaust resources despite being finite. Classfile and classpath readers can
disagree about which implementation is being used. Retained products can be stale or incompletely published, while
diagnostic explanations can disclose sensitive information. The build system may also execute more code than the
source-acquisition model assumes. Each risk requires an explicit Design owner and a negative test before V1 security
acceptance.

### P2/V2 — Shared deployment adds new observers and capabilities

Multi-tenant side channels and remote-cache certificates require an actual shared-service threat model. They are not
automatic V1 obligations for a local compiler, but the V1 representation and validity laws must not prevent those
boundaries from being secured later.

---

# 9. Ownership Routing: ADR, SADR and Design

## 9.1. Contract-owned decisions

ADR-0065 owns the meaning of Admission and the observations required by an Admission Definition. ADR-0064 and ADR-0066
own the Input and Canonicalization meanings. ADR-0048 owns their composition. Those authorities must determine which
complete Presentation can be judged and how that Presentation is related to the current Admission occurrence. This SADR
can demonstrate an attack caused by mixing original and canonicalized values, but it cannot settle the Required Basis by
adding its own reference type.

ADR-0063 owns Establishment, ADR-0071 owns HIR boundaries, and ADR-0075 governs the corresponding Compiler Material
architecture. The security requirement is that candidates, analysis evidence and Established Material remain
distinguishable. Failure ownership also remains governed by ADR-0057, ADR-0065 and the applicable compiler-result
decisions in ADR-0074. A resource failure cannot acquire the meaning of `Rejected` through a security-policy shortcut.

ADR-0073 governs the supported JVM platform meanings. Admission must state which operations its authoring source
permits, but it should not reproduce the JVM specification or define new platform semantics in its own SADR.

## 9.2. Implementation-owned decisions

The Admission Working Design should explain how the frontend identifies supported Java/Kotlin source and produces
qualified candidates. It should also record exact library profiles, callable and effect analysis, and the approach to
resource containment. The future Admission Authoring API document will enumerate user-visible forms. This SADR requires
complete qualification and concrete negative tests without choosing the API syntax.

Compiler and Verification Design own the reference evaluator, execution IR, analysis summaries, optimizer and backend
realization. Platform/Backend Design owns classfile emission and loading assumptions. Compiler Engine and Material
Design own reuse, persistent representations and incremental scheduling. These Designs must satisfy the assurance
properties in this SADR but may choose replaceable algorithms.

Diagnostic ADRs and their Security Design own presentation, permitted disclosure and retention. Whole-machine Build and
Release Security owns compiler provenance, CI trust, dependency verification, signing and vulnerability response. The
Admission-specific claim must say when it relies on those shared protections without duplicating their policy.

## 9.3. How an issue moves between the three documents

If analysis of an attack shows that Admission cannot identify its lawful determining operand, the defect belongs to the
owning Contract ADR. If the operand law is complete but the JVM backend may read a stale field, the remedy belongs to
implementation Design and its verification tests. If a chosen implementation permits secret leakage or authority
confusion, this SADR records the trust boundary and assurance requirement.

The Design should contain actionable implementation decisions rather than a copy of every attack scenario. The SADR
should retain the reason a protection is required and the conditions under which it may be claimed. The ADR must not be
filled with implementation defenses in order to compensate for a missing security architecture.

---

# 10. Unresolved Decisions and Acceptance Evidence

The items below remain open. Each question has a concrete owner and a form of evidence that would make a proposed answer
reviewable. An attractive design choice is not yet an accepted decision.

## O-01. What is the exact Admission operand after optional Canonicalization?

ADR-0065 must settle the Required Basis with ADR-0066, ADR-0048 and ADR-0063. Reviewers need a lawful trace for the
omitted, partially selected and fully selected cases. They also need to see that a Canonicalization refusal prevents
Admission entry and that each Producer Protocol supplies the exact needed observation.

## O-02. Which Java/Kotlin source forms are supported in V1?

The Admission Working Design and later API Catalog must describe actual supported operations and overloads, with
ADR-0073 supplying their platform meaning. Each accepted form needs examples that work and examples that should be
refused. A library Version or overload change must cause appropriate requalification.

## O-03. Which build components are allowed to execute?

Build and Frontend Security Design must inventory plugins, processors and other executing components actually used. The
default privilege policy must be explicit. A safe canary test should show whether a component can create a side effect
before source qualification.

## O-04. Which regex features can be supported safely?

Regex and Admission Source Design must identify the matcher whose semantics will be used. Tests must challenge its cost
with adversarial inputs and compare captures or other observable behavior if a substitute engine is considered. The
supported feature set cannot be chosen from performance alone.

## O-05. Which resource envelopes apply to each stage?

Compiler Resource Design must specify how parsing, analysis, execution and diagnostics handle exhausting their supported
work. Cancellation tests must show which stage stopped and that the stop did not manufacture a Contract result. Budget
and Capacity remain governed by their own laws.

## O-06. What independent checking is feasible for V1?

Verifier and Optimizer Design must describe the intended reference model and where assumptions are shared. Mutation and
differential tests should attempt to invalidate real optimization rules. Reviewers must know which properties are
checked and which remain assumptions.

## O-07. Which JVM environments are covered?

ADR-0073, Backend and Deployment Design must specify a supported JDK and classfile matrix. They must identify how
loaders and runtime dependencies affect qualification. If agents or native code are outside the claim, that exclusion
must be explicit and tested where feasible.

## O-08. When is retained knowledge still current?

ADR-0075 and Reuse Design must connect retained evidence to its live determinants. Fault-injection tests should restore
corrupted, rolled-back or cross-World records. They should also interrupt publication to verify that incomplete Material
remains unusable.

## O-09. What may diagnostics disclose?

Diagnostic Security Design must specify disclosure for each actual output channel. Paired-input and repeated-probe tests
should show whether original Input, Representative values or derived facts can leak to an unauthorized observer. The
answer may differ for a local authoring tool and a future remote service.

## O-10. Is untrusted or shared execution part of the deployment?

Whole-machine Security Architecture must decide whether remote compilation, shared caches or hostile build-code
execution are supported. If they are, the Design must justify the relevant isolation and authorization properties. If
they are not, the product must avoid claiming protections it never needed or provided.

## O-11. How does revocation affect earlier evidence?

Release, Platform and Reuse Design must decide what happens after a trusted compiler or semantic profile is withdrawn. A
test should attempt to use earlier signed artifacts and cached proofs after revocation. Current consumers must follow
the declared validity and migration law.

## O-12. Which requirements belong to a whole-machine SADR?

Security architecture review must route concerns that affect all Contracts to their proper owner. For example,
dependency signing and remote-worker isolation should not be redefined independently for Admission. The review is
complete only when the receiving document owns the requirement and no Contract meaning has been duplicated.

These questions should be addressed alongside the Admission ADR and Working Design. Start with the actual operand and
Basis, then examine source closure and resource use. HIR/Establishment reliance should be checked before optimizer/JVM
preservation. Reuse, diagnostics and release assumptions then require their own review. Open items must remain
explicitly unresolved until their supporting evidence exists.

---

# 11. Closure and Review Procedure

Reviewers should examine each proposed guarantee from the position of an adversary who can control the relevant boundary
input. They should identify the protected material and ask what a legitimate consumer is entitled to observe. A
plausible attack should then be traced to the first boundary that must prevent it. The available evidence and any
unmodeled assumption must remain visible throughout that trace.

Not every scenario requires the same test, but a serious claim should be challenged where applicable by unsupported
source, a changed current context, an interrupted evaluation, resource exhaustion and incorrect downstream
interpretation. This is especially important when the final Boolean happens to be correct despite a broken authority or
confidentiality relationship.

The SADR can be accepted only after the V1 security obligations have actual owners and reviewable evidence. A security
argument must not be broader than its platform or source model, and it cannot silently settle a Contract meaning that
belongs in an ADR. An extensive list of potential vulnerabilities is a useful review input; it is not proof that the
current compiler resists those attacks.

## 11.1. Current closure status

At this draft stage, the requirements SA-01 through SA-14 are proposed and still require a cross-check against the
Security Constitution. The HIR and Establishment authority boundary is also proposed: its typed-reference and recovery
behavior must be audited against ADR-0071, ADR-0063 and ADR-0075.

The exact operand and Required Basis are **blocked on ADR work**. This includes their compatibility with Input-facing
authoring and the selected Canonicalization result. No implementation decision in this document may be represented as
resolving that blockage.

Source capability qualification remains **open**. The Admission Working Design must name the supported forms and show
why each accepted one is safe to refine. The build security work must independently identify any processors or plugins
that actually execute during acquisition. Resource treatment is also open until the Design defines what parsing, regex
matching, analysis and diagnostic generation will do at their supported limits.

Reference evaluation, optimizer conformance and JVM verification remain **open** pending a chosen assurance scope and
executable comparison tests. Reuse validity still needs fault injection against old Worlds and interrupted publication.
Diagnostics confidentiality needs audience-specific controls and leakage tests. Platform qualification needs an explicit
supported runtime matrix. Build and release security needs an inventory of actual dependencies and toolchain trust.

V2 facilities involving distributed repair, remote evidence reuse or multi-tenant operation are **deferred with
replaceable seams**, not accepted as implemented. Their security assumptions must be reviewed if those facilities are
enabled. Finally, negative tests for TH-01 through TH-40 remain **open** until reproducible fixtures and an owning
verification path exist.

The threat cases in Section 5 are review hypotheses and not confirmed vulnerabilities in the current Kontrakt
implementation. This document is suitable for architecture discussion, but it is not ready to be accepted as evidence
that the V1 compiler is secure. Each guarantee must be linked to its actual owner and tested within its declared
supported scope.