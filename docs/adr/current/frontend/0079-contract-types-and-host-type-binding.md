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

### 1.1. A Contract Is a Set of Software Obligations

Kontrakt defines a Contract as a declared set of obligations that software must satisfy (`What Contract Is` §4). The
author decides which obligations belong together. Those obligations may concern a value, an act, or a relationship
between values. They may also specify the rules of a computation without prescribing the machine code that realizes it.

A type can name such a set of obligations. The name helps a compiler identify what is being discussed, but it does not
create the obligations or judge their satisfaction (`What Contract Is` §17.5). This is a way to understand types in
relation to Kontrakt's Contract theory; it does not assert that every type in every programming language represents a
complete Kontrakt Contract.

Kontrakt already provides a means to declare and establish Contracts without using a host-language class as their
authority. IDL identifies the Interface, Interaction, and Operation subjects and selects the applicable one-dimensional
Contracts. Each 1D owns the meaning of the obligations declared through its presentation. The resulting candidates pass
through HIR and the owning Establishment judgments before authoritative Contract material is available (ADR-0046;
ADR-0047 §§6–8; ADR-0063).

### 1.2. Why Kontrakt Realizes Supported 1D Obligations

The user could write code to fulfill many of the obligations expressed by IDL and 1D Contracts. Kontrakt does not take
responsibility for their realization because user implementation is inherently impossible.

It takes that responsibility because the Contracts are intended to describe their obligations in enough detail for the
compiler to determine a lawful realization. With an established Contract, the compiler can examine the obligations
together instead of asking each user to reproduce the machinery needed to satisfy them. Reaching the same degree of
correctness and optimization in separately written implementations would increasingly require users to solve problems
already entrusted to the compiler.

Kontrakt therefore aims to deliver **world-leading implementation quality** for the obligations it can realize from
established meaning. This is a product and engineering objective, not a claim that the current compiler already achieves
it. The compiler may choose a different physical implementation as long as it preserves the established obligations.
ADR-0047 §9 governs this realization boundary.

The user Operation remains the ordinary boundary for user-provided realization. Kontrakt's responsibility for its
generated contractual machinery does not transfer the Operation's user-defined work to the compiler (ADR-0047 §9).

### 1.3. The Platform Does Not Define the Contract

Kontrakt uses Java and Kotlin to carry supported 1D declarations, and the JVM is its present execution target. User
Operation realization also crosses Java/Kotlin host-compilation boundaries. These choices require Kontrakt to understand
the existing languages and the JVM, even though its Contract theory was not derived from them.

Java organizes user-defined data and behavior chiefly through classes. Its interfaces state requirements that classes
may implement, while a class may also carry construction rules and executable method bodies. Kotlin permits more
source-level forms, including top-level functions, but its types and calls still have Kotlin-defined meanings and JVM
mappings. Primitive values and language-defined operators add further cases that cannot be reduced to class method
tables.

The host languages can calculate relationships between values. Their normal type systems, however, do not make those
relationships independent Kontrakt obligations merely because an expression is well typed. Nor do class membership,
method availability, or subtype acceptance establish the Contract authority of a 1D declaration. This is the
platform-expression limitation relevant to this ADR.

Kontrakt must therefore respect the host languages when interpreting authored expressions, without treating their models
of types or classes as its model of Contract authority. The JVM imposes a separate requirement: generated code must
satisfy the target's representation and verification rules. Those rules govern execution artifacts, not what a Contract
means (ADR-0073).

## 2. Problem

A selected 1D declaration can contain ordinary Java or Kotlin calculations. Before the owning Contract judges such a
declaration, Kontrakt must determine what the author actually expressed.

Consider a generic SDK method. Its declaration supplies a method signature, but that signature does not identify which
overload an authored call selected or which type arguments apply at that call. Conversely, identifying the overload does
not establish the method's complete calculation law. Kontrakt needs both the resolved source operation and the meaning
already qualified for that operation (ADR-0084 §3.1).

The frontend cannot leave these questions for Establishment. ADR-0071 §4.3 requires the relevant references to be
resolved before a candidate becomes visible as HIR; ADR-0063 §4.7.1 prohibits Establishment from recovering missing
candidate meaning from source or host structure.

The design problem is to obtain the exact type and call information needed for that interpretation, while avoiding two
mistakes: making Java/Kotlin type relationships a source of Contract authority, and carrying the host compiler's entire
type model into HIR simply because it was useful during resolution. Java and Kotlin may consume evidence from the same
SDK binary without assigning that evidence the same source-language type or callable meaning.

## 3. Decision

### 3.1. Kontrakt Resolves Its Supported Source Surface

**Ordinary 1D Contract compilation uses Kontrakt-owned Java/Kotlin source resolution.** It does not invoke `javac` or K2
to parse, type-check, or select calls in the authored 1D source. This decision does not change the separate
host-compilation path for user Operation realization.

For an expression within its supported language surface, the resolver must determine the source declaration or operation
actually selected under that language's rules. A JVM descriptor alone is insufficient when different source operations
share a JVM representation (ADR-0073 §9.1).

The resolver's obligations depend on the expression. The following cases illustrate distinct information it may need:

| Source case                       | Information required to interpret the use                                 |
|-----------------------------------|---------------------------------------------------------------------------|
| Generic invocation                | The applicable type arguments after source-language resolution            |
| Overloaded method                 | The declaration selected for the actual receiver and arguments            |
| Kotlin extension call             | The extension callable and receiver relationship selected by Kotlin rules |
| Implicit language conversion      | The conversion that participates in the authored calculation              |
| Lambda passed to an SDK operation | The resolved body and parameter meaning required by that operation        |

The supported 1D authoring forms remain bounded by their owning laws. That limit does not permit Kontrakt to omit or
approximate the basic operations of a Java or Kotlin language profile it claims to support. The applicable language
rules and SDK follow the author's selected environment. The same source may select a different overload under a
different SDK profile. Kontrakt must interpret each selection under its actual rules, not preserve a selection made for
an older profile.

For the same source-semantic inputs, Kontrakt must produce the same resolved meaning regardless of its resolver
implementation, worker schedule, or cache state. A language or SDK profile that Kontrakt cannot interpret sufficiently
must not be approximated with another profile's rules. ADR-0073 governs the platform semantic basis, and ADR-0075
governs
result validity and reuse. A change in the selected environment does not itself establish a new Contract Version; any
change to established Contract meaning follows the owning 1D and ADR-0053.

#### 3.1.1. Complete Interpretation of Language-Defined Operations

A supported Java or Kotlin profile includes the meaning of its basic language operations. Kontrakt must interpret them
completely, including operations expressed without an ordinary SDK method call. It may not declare a language profile
supported while leaving one of its basic operations without the interpretation required by that profile. This obligation
is independent of whether a particular 1D permits the operation in its authored calculation.

The source language determines how an operation is interpreted. Some operations have language-defined calculation laws;
others select an SDK or an authored callable; some combine a language-defined evaluation rule with a call. An operator
symbol does not by itself identify either a callable or one uniform calculation. The resolver must preserve the actual
operand types, conversions, evaluation order, conditional execution, partiality, and call selection whenever they affect
the authored meaning. It must not turn a conditional calculation into unconditional execution or replace a selected
callable with a familiar built-in operation.

Numeric and Text calculation meaning remains governed by ADR-0078, ADR-0080, and the applicable platform law in
ADR-0073. The owning 1D determines whether the resulting calculation is lawful for its Contract. ADR-0079 governs the
accurate interpretation and connection of that calculation; a host operator or type does not create Contract authority.

Frontend Design shall maintain a versioned, language-specific operation catalogue that exhaustively accounts for the
basic operations of each supported Java/Kotlin profile. It must cover their interpretation and conformance, including
defined evaluation and failure conditions. The catalogue is implementation and verification work; it does not define a
second operation law or allow support gaps that contradict this section.

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
including Gradle or Maven where applicable, as the primary basis for a **reviewable default-profile draft**. The
relevant input is the actual compilation configuration after applicable
project settings and dependency selection, not merely the build-tool version, an installed compiler, or a text
occurrence
in a build script. Missing or uncertain settings may be proposed from the configured toolchain or environment, but must
not be silently treated as confirmed. The author or the project's declared selection policy can adopt or override the
proposal. The effective profile must be fixed before the affected source judgments run. A later environment change must
not silently replace an already selected profile or validate a Binding Result against different semantic inputs.
Build-tool dependency versions and compilation scopes supply source-interpretation evidence; they do not establish the
Contract Version or applicability governed by ADR-0053 and the owning 1D.

For the V1 ordinary 1D frontend, an arbitrary Kotlin compiler plugin's source-semantic effects are not implicitly
supported or acquired by running that plugin. If specific plugin-generated declarations, modified type relations, or
other frontend effects are explicitly supported, their precise meaning and semantic determinants must be accounted for
under the selected profile. This does not restrict independently governed plugins in the user Operation host-compilation
path.
Reading build configuration and executing its scripts or plugins have different trust implications; concrete
acquisition,
trusted build integration, and isolation belong to Build Integration and Frontend Design.

Frontend Design owns the discovery procedure, supported version combinations, representation of the proposal, and
configuration workflow. This ADR does not fix a Java/Kotlin release, an SDK installation, a build-system adapter, or a
source-profile data structure.

#### 3.1.3. Java/Kotlin Language Forms and 1D Source Eligibility

For each Java or Kotlin language profile Kontrakt declares supported, the frontend must accurately recognize its
lexical and syntactic forms under that language's rules, including context-dependent distinctions. Java and Kotlin have
separate lexical, syntactic, and contextual rules; the presence of a similar token or JVM representation does not equate
their source forms. Kotlin's language-version-dependent declarations, function and property forms, receiver syntax, and
expression conventions must not be interpreted through Java grammar. The exhaustive grammar and operation conformance
catalogues, as well as lexer/parser machinery, belong to Frontend Design.

A declaration or expression may be valid in its host language yet unavailable as an authored form in a particular 1D.
The owning 1D determines authoring eligibility; the frontend's ability to recognize a form does not grant Contract
meaning or expand a 1D Source Carrier. In particular, reading Gradle Kotlin DSL for profile discovery does not select a
Kotlin Script as a 1D carrier. Any additional Script carrier is a later decision for the owning 1D laws, not this ADR.

Accurate source interpretation does not require eager construction of a complete semantic model for every declaration.
The compiler may defer detailed completion where doing so cannot change the interpretation or legality required by the
current source judgment. A skipped region cannot be assumed irrelevant when it changes name lookup, scope, an
applicable initializer restriction, or another determinant of the selected source meaning. Recognition does not excuse
omission of basic language-operation semantics under §3.1.1. Physical parsing and completion remain Frontend Design.

#### 3.1.4. Selected 1D Source and the Realization Boundary

Java/Kotlin 1D source is a representation through which an author declares Contract obligations; the owning 1D, not
the source carrier, defines those obligations. For a selected 1D subject, Source Interpretation must complete the
language meaning of the declarations and calculations that determine its candidate. This includes related source
material whose meaning affects binding or 1D eligibility even if it is not directly executed in the selected judgment.
For example, ADR-0065 §7.6 requires the selected Admission class as a whole to satisfy its initialization restriction.
For Kotlin, the necessary source meaning may depend on declarations, imports, accessibility, or initialization beyond
the containing file or directly called functions. File, package, module, and source-set boundaries are language and
compilation-context facts, not automatic 1D definition boundaries or Contract identity. The owning 1D still determines
the extent of whole-declaration authoring restrictions.

The legality of a Java compilation unit and the semantic completeness of an independently selected 1D candidate are
distinct judgments. A completed candidate does not prove that an entire source file or build is legal; conversely, an
unrelated source error does not by itself redefine that candidate's resolved meaning. The relevant frontend and owning
1D laws determine the required source scope, while ADR-0071 determines candidate visibility and independent failure
impact. ADR-0065 §7.5 already distinguishes unreachable judgment paths from source forms forbidden by the language
or authoring law. The remaining interpretation of whole-source restrictions in Admission Working Design must be
checked against that Accepted law rather than silently settled here.

User Operation Realization follows its separate host-compilation and realization-acquisition path. Its classfiles and
other qualified evidence are processed toward MIR under the realization architecture, not through the Contract-side
Resolved HIR as if they were 1D source candidates. Neither path is required to use the other's physical parser, source
coverage unit, or failure-recovery mechanism. Where both paths interpret the same supported language fact, their
conclusions must still respect the same applicable language semantics. Current 1D authoring restrictions bound which
calculations a 1D may submit; they do not redefine the basic type and operation semantics of a declared supported
language profile or require future source consumers to inherit those authoring restrictions.

#### 3.1.5. Source Encoding, Lexical Meaning, and Provenance

The source character stream used for a judgment must be decoded under a determined encoding rather than an unnoticed
machine default. For Java, the selected language rules then govern Unicode escape translation, token boundaries,
identifiers, literals, and grammar interpretation in their defined order. Kotlin has its own lexical and literal rules,
including quoted identifiers and expressions embedded in string templates; Java's Unicode escape translation must not
be applied to Kotlin source. An encoding choice or identifier transformation must not silently replace either language's
authored meaning. The proposed-default mechanism of §3.1.2 may suggest an encoding, but the effective input
interpretation must be fixed before resolution.

Source provenance records where material was acquired for diagnostic and verification purposes; a path, byte offset,
or character span is not by itself the semantic identity of a resolved declaration or Contract candidate. Display
warnings about visually confusable identifiers may be useful, but they cannot make identifiers equal under rules other
than those of the selected source language. Physical decoding, source-position storage, and diagnostic presentation
remain Frontend and Diagnostic Design responsibilities. ADR-0080's Character/Text Contract meaning does not replace
Java or Kotlin lexical, `Char`, or `String` semantics.

#### 3.1.6. Shared Declaration Knowledge and Contextual Binding

The frontend may maintain common knowledge of declarations obtained from supported authored source and the selected SDK.
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
meaning. Exact type identity, subtyping, conversion, inference, and operation selection follow their respective
language laws; their detailed conformance scope remains subject to §4.

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

A declaration present in an SDK binary is not necessarily available to the authored source. Preparation supplies the
relevant declaration facts; Kontrakt's resolver judges their use under the selected language rules and the actual source
context. Accessibility cannot be settled by one global availability flag attached to a method. Java access rules can
depend on the accessing location and receiver. Kotlin source visibility also differs from JVM binary visibility.
ADR-0073 §9.1 governs the distinction between source operations and JVM implementation methods.

The required coverage is the source surface permitted by the owning 1D, not every callable form available in Java or
Kotlin. For example, ADR-0065 §5.2 defines a flat Admission declaration: inheritance and virtual specialization do
not supply its judgment meaning. The Admission Authoring API Working Design reviews private helpers in that selected
class without approving same-name helper overloads or arbitrary external calls. Sealed authoring APIs may constrain
which implementations users can provide, but sealing does not itself decide member accessibility or create Contract
authority. For a supported private helper or SDK call, the resolver must still apply the exact source access rules.
Prohibited or unsupported forms do not become lawful merely because a binary declaration can be found.

The frontend may prepare reusable accessibility facts, then make context-dependent decisions when interpreting the
use. It must not turn a declaration's binary presence into source permission. The exact access-rule coverage for each
supported Java/Kotlin source form remains open (§4); the preparation and lookup implementation remains Design.

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
incremental strategy. The detailed Java and Kotlin selection rules remain distinct and are reviewed under §4.

### 3.3. Resolved Calls Use Qualified Meaning

After resolving the source operation under §3.1, the frontend uses the applicable qualified meaning provided under
ADR-0084. It must account for the actual type substitution and call-site conditions before composing that meaning into
the authored calculation.

A reusable generic interpretation can serve different type instantiations when its qualification covers those uses. The
compiler need not create a separate handwritten implementation for every instantiation. It may not reuse a meaning when
a difference in the selected operation changes observable behavior.

The host type identifies the source operation; it does not prove every fact relevant to its execution. For example, a
collection's static interface type does not by itself prove which stateful implementation will answer a virtual call.
When that distinction affects interpretation, the frontend must obtain sufficient meaning under ADR-0084 and the
applicable platform law. It cannot resolve the uncertainty by assuming a convenient implementation.

The owning 1D law remains responsible for its value and operation domain. Numeric calculation laws belong to ADR-0078,
and Character/Text laws belong to ADR-0080. This ADR supplies the source interpretation needed to reach those laws; it
does not rewrite them.

### 3.4. Forming the HIR Candidate

The frontend must finish the calculation meaning required by the owning 1D before publishing a Resolved Contract HIR
candidate. Establishment then receives that candidate through the protocol defined in ADR-0071, rather than receiving an
unresolved Java/Kotlin expression.

Only distinctions that determine the candidate's meaning need to survive source resolution as candidate meaning. An
intermediate inference constraint, for example, may be discarded once its result has been applied. A type-related
distinction that changes the applicable calculation cannot be discarded before the owning 1D can judge it.

This rule does not prescribe a universal type field in HIR or require HIR to store a copy of the SDK declaration
database, Java/Kotlin source type graphs, or cross-language interoperability relations. ADR-0071 §4.4 leaves each 1D
responsible for its own candidate vocabulary. The exact type-related observations needed by each 1D remain to be
checked (§4).

Establishment does not upgrade a host type to Contract authority. It judges the resolved candidate under ADR-0063 and
produces the corresponding established meaning where the owning law permits. The exact information relationship between
particular HIR candidates and their Established Material remains part of the open cross-ADR review (§4).

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

The owning declaration or source judgment must distinguish the following conditions insofar as they determine its
result. These are interpretation obligations, not a compiler-wide failure taxonomy or an output format.

| Condition                                 | Required distinction                                                                                                                                                                                                                                             |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Binary acquisition or reading failure     | The selected SDK binary, archive entry, or required declaration data cannot be obtained or read. This does not prove that the source declaration is absent.                                                                                                      |
| Malformed or incompatible binary evidence | Required classfile, generic signature, metadata, or source-to-binary mapping is malformed, contradictory, or outside the actually supported format. An unsupported profile is not assumed to have another profile's meaning.                                     |
| Invalid authored source                   | The selected Java/Kotlin rules reject the source use, including an inaccessible or ambiguous call. This is not an SDK-reading failure.                                                                                                                           |
| Qualified SDK meaning unavailable         | The source operation has been selected, but its calculation meaning is not qualified for this use under ADR-0084. Its declared qualification scope is distinct from basic language-operation coverage (§3.1.1).                                                  |
| Supported-input interpretation defect     | A normal input within Kontrakt's declared support is interpreted incorrectly or cannot be interpreted due to a defect in the compiler, prepared knowledge, or its validation. It is not reclassified as malformed user source or an acceptable SDK coverage gap. |
| Incomplete compiler execution             | A required judgment cannot finish because of exhausted resources, cancellation, or another execution limitation. This is not evidence that the selected SDK binary is malformed.                                                                                 |

A low-level reader error, an unsuccessful provisional lookup, or a rejected overload candidate does not automatically
establish the result of the owning source judgment. The producer responsible for a requirement judges whether that
requirement has been met; Verifier and Manager work may contribute evidence without becoming a substitute semantic
authority. This respects the common result-ownership direction in ADR-0074 (*Proposed*) without adopting its unapproved
result protocol here.

If source interpretation does not succeed, Kontrakt cannot publish a completed HIR candidate on that basis. Such an
unsuccessful compiler judgment is not a refusal by the owning 1D Contract. A later Establishment judgment that was never
entered does not acquire a separate fabricated failure (ADR-0063 §4.7.1; ADR-0071 §6).

#### 3.5.3. Lawful Response to Unusable Evidence

When required SDK binary evidence is unavailable or unusable, Kontrakt may obtain it again, use another qualified way
to interpret the same selected binary, or supply other evidence adequate to establish the same source meaning. Such a
response is lawful only if the selected language/SDK basis, exact source declaration, and necessary relations remain
established. SDK source text, a different reader, or host-compiler output does not by itself prove correspondence to
the selected compiled binary. Kontrakt must not guess from erasure, suppress a competing overload, substitute a
different SDK version, or select another operation merely to obtain a successful result.

An unsuccessful attempt does not become successful retrospectively because a later qualified attempt succeeds. An
earlier completed result may be reused only if ADR-0075 establishes its current eligibility; failed replacement or
unavailable new evidence neither validates nor automatically invalidates that earlier result. Failure propagation,
independent work, coherent observation, and cache validity follow ADR-0071 and ADR-0075, not a new SDK-specific
propagation policy. No affected judgment may consume missing, inconsistent, or unqualified evidence as if it were a
completed result.

Interpretation does not recover missing meaning by executing arbitrary authored or SDK code, treating a JVM-callable
method as proof of source eligibility, or delegating Contract meaning to user realization (ADR-0047 §§7, 9;
ADR-0084 §§3.4–3.5).

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

## 4. Remaining Work

The preceding sections settle the meaning obligations for the selected source profiles, hybrid SDK preparation, legal
candidate selection, and declaration-evidence failure. This Draft still has open questions. The table does not approve
any unchosen mechanism or change an Accepted 1D law.

| Area                                | Decision or work still required                                                                                                                                                           | Owner or boundary                                         |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| Language conformance                | Exact Java/Kotlin source-type and operation rules for names, generics, overloads, conversions, receivers, and nullability; no correctness gaps deferred to V2                             | ADR-0079; language-specific normative rules               |
| Type distinctions and relationships | Complete Java/Kotlin type-meaning inventory, identity, subtyping, inference, capture, projection, conversions, and their required basic-operation observations; B-2.2 onward remains open | ADR-0079; detailed conformance in Frontend Design; §3.1.7 |
| Java/Kotlin interoperation          | Direction-specific source type/callable interpretation, metadata/annotation disagreements, platform and flexible types, ABI mappings; preserve B-2.1 independence                         | ADR-0079 / ADR-0073 / Frontend Verification; §3.1.7       |
| Source-profile proposal             | Build-system-derived effective compilation inputs and reviewable defaults, source-semantic settings, qualified plugin coverage, trust boundary, and profile conformance                   | Frontend / Build Integration Design; §3.1.2               |
| Language forms and source intake    | Java/Kotlin lexical and grammar conformance, deterministic decoding, and separation of host syntax recognition from 1D authoring eligibility                                              | Frontend Design / owning 1D; §§3.1.3, 3.1.5               |
| New 1D source carriers              | Revisit Kotlin Script after scripting maturity is reassessed, if the owning 1D later proposes it; no support decision in this ADR                                                         | Owning 1D / ADR-0047; §3.1.3                              |
| Selected source legality            | Exact source scope required by each 1D; Admission whole-class initialization and remaining Working Design consistency review under ADR-0065 §§7.5–7.6                                     | Owning 1D / Frontend Design; §3.1.4                       |
| Declaration and binding reuse       | Shared declaration evidence, contextual Binding Results, language-specific type meaning, and measured reuse without fixing the V2 incremental engine                                      | Frontend Design / ADR-0085; §§3.1.6–3.1.7, §§3.2.1–3.2.3  |
| Operation catalogue                 | Exhaustive, version-specific inventory and conformance tests for basic language operations; no optional gaps within a supported profile                                                   | Frontend Design / Verification; obligation in §3.1.1      |
| 1D authoring surfaces               | The precise Java/Kotlin declaration and expression forms permitted by each 1D, separately from complete basic-operation interpretation                                                    | Owning 1D / Frontend Design                               |
| SDK profiles                        | Supported version combinations, binary-selection rules, and conformance of prepared declaration knowledge                                                                                 | Frontend Design / Verification; ADR-0073 semantic basis   |
| SDK lookup                          | Concrete indexes, completion granularity, candidate search, negative dependencies, and lawful early-stop implementation                                                                   | Frontend Design; semantic requirements in §§3.2.1–3.2.3   |
| Source availability                 | Conformance coverage for context-dependent access rules in permitted 1D authoring forms                                                                                                   | Frontend Design / Verification; §3.2.2                    |
| Binary acquisition and verification | Concrete classfile/archive/metadata readers, binary-evidence checks, lawful recovery, resource safeguards, and profiling-guided timing                                                    | Verification / Frontend / Compiler Design; §3.5 law       |
| Qualified binding                   | Exact semantic conditions for applying prepared SDK calculation meaning to a resolved source use                                                                                          | ADR-0079 ↔ ADR-0084                                       |
| Semantic reuse                      | When generic qualified meaning may be specialized without changing the selected source operation's law                                                                                    | ADR-0079 ↔ ADR-0084                                       |
| HIR observations                    | Which resolved type distinctions determine the meaning of each 1D candidate                                                                                                               | ADR-0079 ↔ ADR-0071 / owning 1D                           |
| Establishment                       | What candidate distinctions must remain available for the owning Establishment judgment                                                                                                   | ADR-0063 / owning 1D; reviewed from ADR-0079              |
| Compiler result                     | Shared unsuccessful-result ownership, observation, and recovery protocol; not diagnostic rendering in this ADR                                                                            | ADR-0074 (*Proposed*) and related compiler-result design  |
| Later consumers                     | Type information needed outside 1D HIR, including Realization and JVM backend use                                                                                                         | ADR-0085 (*Draft*)                                        |

The compiler may select different verification and storage strategies as profiling and measured workloads change.
ADR-0075 governs result validity and reuse. ADR-0073 §9.6 governs eligible preservation of a qualified platform call
in execution. Neither changes Source Interpretation's success and failure conditions.

## 5. Consequences

Kontrakt must maintain conformance for its selected Java/Kotlin language profiles. Basic language operations cannot be
omitted from a claimed supported profile; the operation catalogue in Design supplies the exhaustive implementation and
conformance inventory. The owning 1D may still restrict which authored expressions it accepts. A change in the
author's selected SDK may select a different operation from unchanged source; a change in Kontrakt's resolver
implementation must not change the correct meaning for the same semantic inputs.

Hybrid preparation avoids repeating SDK-wide declaration discovery during ordinary user compilation without requiring
every declaration to be expanded in advance. Source-dependent completion, candidate search, and required verification
still have a cost. Profiling may guide when and how the compiler performs that work. For normal binaries within a
declared supported profile, inaccurate or missing interpretation is a compiler defect, not a permissible reduction of
language support. Actual binary acquisition, decoding, format, or execution failures cannot be hidden by caching,
deferred checks, or guessed source meaning.

An expression accepted by `javac` or K2 need not be permitted by a particular 1D authoring law. That distinction does
not excuse a gap in the basic language operations of a supported Java/Kotlin profile. Incomplete source interpretation
remains outside valid HIR; its unsuccessful meaning belongs to its owning compiler judgment rather than a later 1D
refusal. The impact on independent compiler products and the eligibility of previous results follow ADR-0071 and
ADR-0075. Failure output and diagnostic presentation remain outside this ADR.

The selected source profile may begin as an automatically generated proposal based primarily on supported build-system
compilation settings, but its effective interpretation basis must be fixed before use. Java and Kotlin language-form
recognition is separate from each 1D's permission to author a particular form; build scripts used for discovery do not
become Contract Source Carriers. The selected candidate requires complete contextual source meaning; this does not
merge Contract-side source interpretation with the host-compilation and MIR acquisition of user realization.

Common SDK and declaration evidence and contextual Binding Results may be reused to reduce frontend work. Java and
Kotlin Source Type Meaning remains language-specific; using the same binary does not create a common type identity or a
mandatory cross-language semantic graph. Physical indexes, language-specific caches, lazy completion, and future
incremental techniques remain replaceable; they must preserve the meaning of current source bindings and the legal HIR
observations under ADR-0071 and ADR-0075.

This document remains **Draft**. The open semantic coverage and information-preservation decisions in §4 must be
reviewed before acceptance.