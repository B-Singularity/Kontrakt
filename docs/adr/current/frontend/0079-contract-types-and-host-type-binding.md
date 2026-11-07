# ADR-0079: Type Interpretation and Semantic Binding

**Status:** Draft  
**Date:** 2026-10-25

## Related

- `docs/the-most-important-thing/what-contract-is.md` — §§4, 17.5–17.6
- ADR-0046 — IDL-First Interface Contract Frontend
- ADR-0047 — One-Dimensional Contract Presentations, Source Carriers, and Backend Realization, especially §§7–9
- ADR-0053 — Version Contract, especially §§3.4–3.6
- ADR-0063 — Contract Establishment, especially §4.7.1
- ADR-0065 — Admission Contract, especially §§5.2, 7
- ADR-0071 — Resolved Contract HIR, especially §§4.3–4.5
- ADR-0073 — JVM Platform-Native Surface, especially §§7, 9
- ADR-0074 — Explicit Unsuccessful Compiler Result and Recovery (*Proposed*)
- ADR-0075 — Compiler Result Ownership and Reuse
- ADR-0078 — Deterministic Numeric Value and Operation Semantics
- ADR-0080 — Characters, Strings, and Unicode
- ADR-0084 — One-Dimensional Standard-Library Semantic Acquisition, especially §3
- ADR-0085 — Compiler Type Architecture and Information Lifecycle (*Draft*)

---

## 1. Premise

### 1.1. Host Types and Contract Meaning

Kontrakt declares and establishes Contracts independently of Java/Kotlin type or class authority (`What Contract Is`
§§4, 17.5–17.6). IDL selects Interface, Interaction, Operation, and applicable 1D subjects; each 1D owns the
obligations expressed through its permitted Java/Kotlin source carrier (ADR-0046; ADR-0047 §§6–8; ADR-0063).

Java/Kotlin type declarations, expressions, and SDK references have their own language-defined meanings. Kontrakt
must interpret those meanings faithfully to identify what the author declared, but a host type, valid source
expression, or JVM representation neither creates Contract obligations nor determines their authority (ADR-0073).
User Operation realization follows its separately governed host-compilation path (ADR-0047 §9).

## 2. Problem

A selected 1D declaration may refer to Java/Kotlin types, generic parameters, SDK declarations, and operations.
Kontrakt must resolve the **actual declarations and types intended by the authored source under the selected language
rules**, rather than infer meaning from a name, source spelling, or JVM descriptor. For an overloaded SDK call, for
example, the signature of one method does not identify the selected overload, its type substitution, or its
availability in the author's scope. Selection precedes the use of that operation's qualified meaning (ADR-0084 §3.1).

The required information includes exact declaration binding, type construction and identity, generic relations,
receiver and invocation context, source accessibility, and the applicable language/SDK environment. These elements
interact: knowing any one of them does not establish the others. Java and Kotlin may read the same SDK binary without
assigning it identical source-language type or callable meaning.

Relevant meaning must be resolved before the owning 1D publishes a complete HIR candidate; Establishment does not
reconstruct missing source information (ADR-0071 §4.3; ADR-0063 §4.7.1). The information may also matter to the
separate user Operation realization path, without requiring that path to use the 1D resolver or Contract HIR. This
ADR specifies **the required distinctions and their interpretation boundary**, not a second Contract Type System or
a complete host compiler architecture.

## 3. Decision

### 3.1. Kontrakt Resolves Its Supported Source Surface

**Ordinary 1D Contract compilation uses Kontrakt-owned Java/Kotlin source resolution.** It does not invoke `javac` or
K2 to parse, type-check, or select calls in ordinary authored 1D source. User Operation realization follows a
separate host-compilation path; it is not a 1D HIR source candidate.

The resolver identifies the actual type, declaration, or operation selected under the source language's rules. A JVM
descriptor, matching name, or matching erased type does not establish that selection (ADR-0073 §9.1). Distinct uses
require different information:

| Source use                    | Information needed to establish its meaning                                                |
|-------------------------------|--------------------------------------------------------------------------------------------|
| Generic type or invocation    | Exact generic declaration, type arguments, bounds, and applicable substitution             |
| Overloaded callable           | Complete relevant candidates, receiver, arguments, and selected declaration                |
| Kotlin extension or property  | Source-visible declaration, receiver context, and actual property/callable binding         |
| Language-defined operator     | Applicable operand types and the operator or callable actually selected                    |
| Lambda supplied to a callable | The callable's parameter/return type context and the lambda meaning needed to bind the use |

A 1D's permitted authoring forms do not limit the supported language profile's underlying type and operation
semantics. For the same source-semantic inputs, resolved meaning must not change with worker scheduling, cache state,
or a different implementation strategy. An unsupported language/SDK profile must not be silently interpreted through
another profile. ADR-0075 governs result reuse; a change in the selected environment does not itself establish a new
Contract Version (ADR-0053).

#### 3.1.1. Complete Interpretation of Language-Defined Operations

A claimed supported Java/Kotlin profile includes the language-defined operations required to interpret its source
forms, including operations not expressed as SDK calls. The frontend must identify the actual operator, callable,
operand and result typing, and any language-defined conditions that change that selection. An operator spelling does
not by itself identify one universal operation; the same spelling may denote a language-defined operation or select
an authored/SDK callable. Interpretation may not silently replace a selected source operation with a familiar one.

The meaning of the resulting Numeric, Text, or other 1D-specific computation remains with its owning law (ADR-0078;
ADR-0080; the applicable 1D; ADR-0084 for qualified SDK meaning). This ADR does **not** redefine their
value domains, rounding, overflow, evaluation algorithms, or computation admissibility. The complete profile-specific
operation coverage and conformance catalogue can be retained for Frontend Design and Verification without becoming
an additional source of Contract law.

#### 3.1.2. Selected Language Profile and Proposed Defaults

Source interpretation uses a definite, selected language environment. For Java, the frontend must distinguish the source
language level and enabled language features from the selected JDK/API level and available SDK declarations. Module and
classpath selection can change which declarations are visible; a target JVM setting alone does not necessarily restrict
available APIs or source-language meaning. For Kotlin, the actual compiler implementation, selected language version,
API version, enabled language features, relevant semantic compiler options, and the selected stdlib/JDK/dependency
binaries must be distinguished. Kotlin module, source-set and friend relationships, metadata, and Java interoperability
settings may change source visibility, type meaning, or the applicable callable set. JVM output compatibility and
compilation-execution settings are not automatically source-semantic determinants. The compiler must retain the
determinants needed by each affected judgment; they need not form one universal profile object or physical cache key.

Where the author has not supplied a complete effective profile, Kontrakt uses supported project build configurations,
including Gradle or Maven where applicable, as the primary basis for a **reviewable default-profile draft**.
The relevant input is the actual compilation configuration after project settings and dependency selection, not
merely the build-tool version, an installed compiler, or an isolated occurrence in a build script. Missing or uncertain
settings may be proposed from the configured toolchain or environment, but must
not be silently treated as confirmed. The author or the project's declared selection policy can adopt or override the
proposal. The effective profile must be fixed before the affected source judgments run. A later environment change must
not silently replace an already selected profile or validate a Binding Result against different semantic inputs.
Build-tool dependency versions and compilation scopes supply source-interpretation evidence; they do not establish the
Contract Version or applicability governed by ADR-0053 and the owning 1D.

For the V1 ordinary 1D frontend, an arbitrary Kotlin compiler plugin's source-semantic effects are not implicitly
supported or acquired by running that plugin. If specific plugin-generated declarations, modified type relations, or
other frontend effects are explicitly supported, their precise meaning and semantic determinants must be accounted for
under the selected profile. This does not restrict independently governed plugins in the user Operation
host-compilation path. Reading build configuration and executing its scripts or plugins have different trust
implications; concrete acquisition,
trusted build integration, and isolation belong to Build Integration and Frontend Design.

Frontend Design owns the discovery procedure, supported version combinations, representation of the proposal, and
configuration workflow. This ADR does not fix a Java/Kotlin release, an SDK installation, a build-system adapter, or a
source-profile data structure.

#### 3.1.3. Java/Kotlin Language Forms and 1D Source Eligibility

The selected Java/Kotlin profile determines the lexical, syntactic, and contextual language rules needed to identify
exact type and declaration meaning. The same token shape or JVM representation need not have the same interpretation
in Java and Kotlin. In particular, Kotlin type, receiver, function, property, and expression forms must be understood
according to Kotlin rules, not Java grammar. Exact lexer/parser machinery and full grammar conformance belong to
Frontend Design.

A form recognized as valid Java/Kotlin source is not thereby a permitted 1D authoring form. Each 1D owns its Source
Carrier and authoring eligibility (ADR-0047 §7). Build configuration used to discover Profile settings neither
constitutes an authored 1D declaration nor authorizes additional Source Carriers.

Source meaning can be completed on demand when the omitted detail cannot affect binding or the relevant 1D
qualification. A declaration cannot be ignored merely because it lies outside the selected class or file if it can
change an applicable name, type, callable candidate, or source restriction. This is a completeness condition, not a
mandated parsing or resolution schedule.

#### 3.1.4. Selected 1D Source and the Realization Boundary

For a selected 1D, the required source interpretation covers the declarations and relations that determine its
candidate meaning and those whose presence affects authoring eligibility under the owning 1D. Neither the containing
file nor the direct-call graph alone defines that set. Kotlin file-private declarations, package imports, module
visibility, source-set relations, and relevant external declarations may participate in binding, but do not become
Contract identity or expand the 1D's authoring unit. For example, ADR-0065 §7.6 applies its initialization restriction
to the selected Admission class; this ADR does not extend that restriction to unrelated declarations.

The legality of a wider Java/Kotlin compilation and the completed meaning of an independent 1D candidate are distinct
judgments (ADR-0071 §6). The owning 1D determines its whole-declaration restrictions. Source interpretation must not
dismiss a restriction merely because the affected code is not executed by a chosen calculation.

User Operation realization remains on its own host-compilation, acquisition, MIR, and verification path. The paths
may need the same language-defined type information but need not share a physical resolver or candidate representation;
the 1D's authoring limits do not define the future reach of Java/Kotlin source information consumers.

#### 3.1.5. Source Encoding, Lexical Meaning, and Provenance

The source character stream must be decoded under an identified encoding, not an unnoticed machine default. Java's
Unicode escape translation and Kotlin's lexical rules differ; each language's token, identifier, and literal meaning
must be determined by its selected profile. An apparent identifier similarity, encoding assumption, or informal
normalization cannot replace the identifier actually declared or referenced by that language.

Source location and byte/character spans may be retained for provenance and diagnostics, but do not by themselves
establish declaration identity or Contract identity. Physical decoding, source-position representation, and diagnostic
presentation remain Frontend and Diagnostic Design concerns. ADR-0080 does not replace host lexical rules.

#### 3.1.6. Shared Declaration Knowledge and Contextual Binding

The frontend may maintain common declaration evidence obtained from supported authored source and the selected SDK.
A declaration's existence is distinct from binding a particular use to that declaration. Shared binary or declaration
evidence does not itself have one Java-and-Kotlin source type meaning. At the use site, Kontrakt resolves names in the
applicable language scope, taking account of imports, shadowing, receiver context, access rules, and the other
source-language conditions that can change the selected declaration. For Kotlin this includes file-local and module
visibility, import aliases, extension and member candidates, implicit/context receivers where the selected profile
supports them, and the source callable or property meaning distinguished from JVM members. This shared knowledge must
not become a separate source of Contract authority, and a general declaration lookup cannot substitute for the
context-dependent source judgment required by §§3.2.2–3.2.3.

Declaration knowledge and completed Binding Results may be reused across source judgments when their current semantic
inputs and validity conditions permit it. The V1 query-oriented compiler can avoid repeated discovery and resolution
through its existing result reuse, indexing, and caching mechanisms. V2 may add finer dependency tracking, selective
invalidation, early cutoff, and incremental repair without changing the meaning of a binding. In particular, the
absence of a competing declaration may be relevant when a newly selected SDK adds an overload; a previous successful
lookup alone does not prove that the old selection remains valid. ADR-0075 governs current result eligibility.

This decision separates reusable declaration evidence from a contextually resolved use. It does not require an
eagerly completed global source model, one table shared by every 1D, or a particular query boundary. Frontend Design
may choose eager or on-demand completion, physical indexes, and reuse granularity based on measured workloads. The
sharing and interpretation of type meaning remain subject to §3.1.7; ADR-0085 owns compiler type-knowledge lifecycle.
Under §3.4, only the distinctions that determine the owning 1D candidate survive as its HIR meaning.

#### 3.1.7. Language-Specific Type Meaning and Interoperability

Java and Kotlin Source Type Meaning is interpreted independently under each language's selected profile, declaration
scope, and type rules. A common SDK binary, class name, erased type, or JVM descriptor does not give Java and Kotlin a
single source type identity, type relation, or callable meaning. Java generics, raw and captured types, and conversions
must be distinguished from Kotlin nullability, flexible/platform types, declaration- and use-site variance, type
projections, function types, value classes, and other Kotlin-specific distinctions wherever they affect supported source
meaning. Exact declaration identity and source type identity must be determined under each language's own rules. Type
identity, type equivalence, subtyping, assignability, conversion, type-argument containment, capture, and inference
are distinct judgments; success of one does not establish another. In particular, Kotlin flexible/platform type
relations and separate capture variables must not be converted into a universal, transitive type identity. Java
raw/parameterized types or captures must not be equated by matching erasure, spelling, or bounds. These are
information-preservation requirements, not new type relations defined by Kontrakt.

The selected SDK's binary and metadata evidence, including classfiles, signatures, annotations, and Kotlin metadata,
may be acquired, decoded, and reused as common declaration evidence under B-1 (§3.1.6). This does not authorize a
language-neutral semantic type view. When Java source uses a Kotlin-produced declaration, the Java interpretation must
reflect what is available under Java source and the qualified JVM/ABI evidence. When Kotlin source uses a Java-produced
declaration, Kotlin's applicable interoperability and metadata rules determine the Kotlin view, including any
nullability and collection mapping. These directions need not yield symmetrical or lossless mappings. Contradictory or
incomplete evidence cannot be silently normalized into one convenient type meaning (§3.5).

Interop interpretation is a result of the consuming language's source judgment. A result may be cached and reused only
under the semantic determinants and validity conditions that actually support it (ADR-0075). This ADR neither requires
a shared semantic Type Identity nor a globally authoritative interop-relation graph; maintaining one could propagate an
outdated, language-specific inference to unrelated consumers. The same evidence may nonetheless be read once and
language-specific interpretations reused without rereading SDK archives. This is a semantic separation, not a mandate
for separate binary readers, type-table implementations, or compiler processes; those choices and type-knowledge
lifecycle belong to Frontend Design and ADR-0085.

### 3.2. SDK Declarations Are Prepared Before User Compilation

Kontrakt prepares the Java/Kotlin SDK type and declaration information needed by the supported platform profiles before
ordinary user compilation. The frontend consults that prepared information instead of re-reading the installed libraries
to construct their declarations on every build.

Preparation reads declaration evidence from the compiled binaries selected for the author's SDK and language
profile, including JDK classfiles, generic signatures, annotations, and Kotlin metadata. It does not acquire declaration
meaning by invoking SDK methods. The selected archive members and binary mappings must correspond to that profile; a
file name or SDK version label alone does not establish that correspondence. Commonly decoded evidence retains its
original distinctions; Kotlin source-visible declarations need not correspond one-to-one to Java source-visible or JVM
implementation members (§3.1.7). SDK sources and normative language specifications may support interpretation and
qualification, but they do not replace the selected binary declarations by assumption.
`javac` and K2 may be used during preparation and conformance testing. Their results are evidence for the prepared
knowledge, not the resolver called during ordinary 1D compilation.

**Declaration knowledge and qualified operation meaning serve different purposes.** Declaration knowledge makes exact
source resolution possible. Qualified operation meaning supplies the behavior of the operation after it has been
selected. ADR-0084 §3 governs the latter. They may share physical storage, but a method signature must never be treated
as proof of its behavior.

#### 3.2.1. Prepared Lookup and Required Completion

V1's working direction is hybrid preparation. Kontrakt prepares a profile-bound lookup basis for the SDK declarations
relevant to its supported 1D source surface. Detailed declaration information may be completed when a source use needs
it; preparing the basis does not require eagerly expanding every SDK declaration. The resulting interpretation must
still account for every declaration that can affect the supported use. Finding one plausible method is not sufficient
when another applicable overload may exist.

Completion is measured against the actual source judgment, not against whether an individual classfile was parsed.
The frontend cannot claim a successful interpretation without the required declarations and relations (§3.5). This
section does not decide the detailed Java/Kotlin search algorithms, indexes, completion granularity, query boundaries,
or storage layout. Frontend Design owns those choices; ADR-0085 owns the lifecycle of compiler type information.

#### 3.2.2. Source Availability in the Permitted 1D Surface

A declaration present in SDK binary evidence is not necessarily accessible to the authored source. Availability
must be judged in the selected Java/Kotlin context, including package/module boundaries, receiver constraints,
visibility, and permitted authoring forms; no global "available" flag on a binary method is sufficient. Kotlin source
visibility and callable/property forms can differ from their JVM representations (ADR-0073 §9.1).

Each 1D determines which authoring forms are lawful; the ability to resolve an otherwise prohibited source form
does not make it eligible. The required source access rules must still be applied precisely wherever a selected use
is lawful. Prepared accessibility facts can be reused, but cannot replace context-specific source binding.

#### 3.2.3. Complete Source Candidate Selection

Within the source forms permitted by an owning 1D, Kontrakt must discover the declarations relevant to the selected
Java or Kotlin call under that language's name, scope, receiver, access, and candidate-selection rules. The actual
selected callable is determined from those declarations, not from the inventory of operations for which Kontrakt already
has qualified SDK meaning. An unqualified overload or a permitted authored helper must not disappear from selection
merely because another operation has an available interpretation.

Candidate search may proceed in stages and may stop when the applicable language rules establish that no unexamined
candidate can change the outcome, including ambiguity. Finding the first applicable declaration is not such a proof.
A rejected provisional candidate is not necessarily a failed source-resolution judgment. If the relevant candidate set
cannot be established sufficiently, the resolver cannot select a successful call (§3.5).

A new overload, member, extension, or other declaration may change selection under a newly chosen profile. Reuse must
therefore establish the current adequacy of the candidate search, including any absence of competing declarations on
which the outcome depends. ADR-0075 owns the reusable result's validity; Frontend Design owns the search, indexing, and
incremental strategy. Java and Kotlin candidate-selection rules remain language-specific.

### 3.3. Resolved Calls Use Qualified Meaning

The actual source operation is selected under §§3.1–3.2 **before** any qualified calculation meaning is attached.
For an SDK call, ADR-0084 governs whether qualified meaning covers that exact operation and use. Generic substitution,
receiver context, and other call-site distinctions must be determined where they affect that attachment; having an SDK
signature or a JVM-callable target does not prove the calculation's meaning. Qualified generic meaning may be reused
for different type instantiations only when its qualification covers the particular selected uses. A static receiver
type does not by itself establish a dynamically dispatched implementation when that distinction matters to the
qualified interpretation (ADR-0073; ADR-0084).

Qualified meaning must not influence which overload the language selects. If adequate qualified meaning is unavailable
for a selected operation, the compiler may not silently choose a different overload or assume a convenient runtime
implementation. The content of Numeric, Text, or other contract computation is governed by its existing owning ADR.

### 3.4. Forming the HIR Candidate

The owning 1D's Resolved Contract HIR candidate must contain the exact, completed meaning required by that 1D before
it becomes available to Establishment (ADR-0071; ADR-0063). Its candidate vocabulary is owned by the 1D: this ADR
does not require HIR to retain a generic host Type Model, SDK declaration database, solver state, or Java/Kotlin
interoperability graph. Resolved type, declaration, and callable distinctions must be preserved **when they determine
that candidate's meaning**; intermediate information may be discarded once its relevant effect is resolved.

Establishment applies the owning Contract law to the resolved candidate. Neither a host type nor an HIR transport
reference acquires Contract authority by being used for source interpretation.

### 3.5. SDK Binary Evidence and Unsuccessful Source Interpretation

#### 3.5.1. Supported SDK Interpretation and Evidence Adequacy

Kontrakt must correctly interpret normal, conforming SDK binaries within the Java/Kotlin profiles it declares
supported, for the source meanings required by its permitted 1D authoring surface. An omitted declaration, incorrect
generic relation, mistaken Kotlin mapping, or incomplete candidate set in that supported scope is a compiler correctness
defect. It does not become a normal unsupported-source outcome merely because the current resolver or prepared SDK
knowledge failed to recover the correct meaning. Detection of such a defect still forbids a false successful result.
A source form outside a particular 1D's permitted authoring surface is a separate restriction under that 1D law.

Successful source interpretation requires the actual declarations and relations needed under the selected language
rules, including the complete relevant candidate set (§§3.2.1–3.2.3). Decoding a classfile or metadata record is not
itself proof of that source meaning. The evidence used must refer coherently to the binaries selected for the current
profile, including any applicable source-to-JVM mappings and the consuming language's interoperability interpretation.
It must not combine declarations or source-type results from different binaries, language profiles, or interpretation
contexts merely because their names or erased JVM representations agree.

Normal absence of optional data is not corruption: a non-generic Java declaration may have no generic `Signature`, and
Java declarations do not require Kotlin metadata. Conversely, a required declaration, signature, mapping, or binary
that cannot actually be obtained or interpreted must not be treated as a nonexistent source candidate. An unusable
item may be irrelevant to a particular judgment only when its irrelevance is established under the language rules;
not having read or discovered it does not establish that fact. This includes declarations that could affect overload
selection. The success of an unrelated judgment and the eligibility of earlier compiler products remain subject to
ADR-0071 and ADR-0075, rather than a separate SDK-wide failure-containment law in this ADR.

#### 3.5.2. Unsuccessful Conditions and Their Ownership

The responsible source judgment must distinguish failures to obtain the information needed to identify a type,
declaration, or operation from actual rejection under the selected source rules:

| Condition                                          | Required distinction                                                                                                          |
|----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| SDK evidence unavailable                           | A required binary or declaration cannot be read; this does not prove that the declaration is absent.                          |
| Malformed, incompatible, or contradictory evidence | Classfile, signature, metadata, or source-to-binary mapping cannot support the selected interpretation.                       |
| Invalid source use                                 | The selected language and context reject a reference or invocation, including inaccessibility or ambiguity.                   |
| Selected operation lacks qualified meaning         | Source binding succeeded, but ADR-0084 does not qualify the operation for the required use.                                   |
| Defective interpretation of supported inputs       | The compiler's failure or incorrect inference within declared support is a correctness defect, not lawful unsupported source. |

A provisional failed lookup or rejected overload candidate does not establish that the owning judgment failed. Where
required meaning is unresolved, the compiler cannot publish a completed HIR candidate. An unsuccessful compiler
source judgment is not a refusal by an unentered 1D Establishment judgment (ADR-0063 §4.7.1; ADR-0071 §6). General
execution cancellation, resource exhaustion, result protocols, and diagnostic presentation retain their existing
compiler-result ownership; this ADR does not define a separate failure taxonomy.

#### 3.5.3. Lawful Response to Unusable Evidence

Required SDK evidence may be reacquired or interpreted by another qualified means **for the same selected source
basis**, provided the exact declaration and necessary relationships are established. SDK source files, another
reader's output, or host-compiler evidence do not automatically prove correspondence to the selected binary.
Kontrakt must not infer the missing type or callable from erasure, suppress competing candidates, replace the SDK
version, or execute arbitrary SDK/authored code to manufacture interpretation success (ADR-0084 §§3.4–3.5).

Reuse of an earlier product remains conditional on ADR-0075's validity rules. Missing or conflicting evidence cannot
be consumed as a completed result, and a later successful attempt does not retroactively make a failed source
judgment successful.

#### 3.5.4. Verification and Implementation Boundary

This ADR fixes correctness, evidence adequacy, unsuccessful conditions, and lawful response. It does not prescribe when
checks run or which physical component performs them. Verifier subsystems, responsible producers, and compiler managers
may divide verification and coordination work. Eager or deferred validation, reuse, parallelism, recovery mechanisms,
and scheduling can be chosen and changed through profiling and implementation, but a required condition cannot be
regarded as satisfied when a completed source result is consumed or valid HIR candidate meaning is exposed.

Normal supported-profile interpretation must be covered by compiler conformance and QA work; error handling does not
replace that product obligation. Binary readers, archive selection, resource safeguards, physical type storage, and
validation algorithms belong to Frontend, Verification, and Compiler Design or ADR-0085 as applicable. ADR-0075 governs
compiler-product integrity, trust, and reuse. Failure-result representation, output, and diagnostic rendering belong
to their owning Failure, Compiler Result, and Diagnostic documents, not this ADR; a compiler interpretation failure is
not an ADR-0057 Contract Machine Failure.