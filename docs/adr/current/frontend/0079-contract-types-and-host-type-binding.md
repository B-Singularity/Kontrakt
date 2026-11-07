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
§§4, 17.5–17.6). IDL selects a Contract Interface and identifies the Interactions and Operations belonging to it.
It also selects the applicable 1D subjects. Each 1D owns the obligations expressed through its permitted
Java/Kotlin source carrier (ADR-0046; ADR-0047 §§6–8; ADR-0063).

Java/Kotlin types have meanings defined by their source language. The same is true of expressions and references
to SDK declarations. Kontrakt must interpret those meanings faithfully to determine what the author actually wrote
and used. Neither a valid host expression nor its JVM representation creates Contract obligations or determines
their authority (ADR-0073). User Operation realization follows its separately governed host-compilation path (ADR-0047
§9).

## 2. Problem

A 1D source carrier selected through IDL can use Java/Kotlin types and operations. Kontrakt must interpret the
source according to its selected language rules. A matching name or JVM descriptor cannot establish what type or
operation the source actually uses.

Consider an SDK call with several overloads. Reading one method signature does not establish which overload the
source selects. The receiver and argument types can affect that choice, while the surrounding language scope can
make other declarations applicable. If the selected method is generic, its type parameters must also be interpreted
for this particular use. Only after the callable is selected can its qualified meaning be obtained under ADR-0084
§3.1.

The information needed for an accurate result depends on the use:

| Question                                   | Why it affects source interpretation                                                                                                             |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| Which type does this source use?           | The type's construction and applicable language rules determine its meaning. The same spelling can refer to a different type in another context. |
| Which declaration does a reference select? | Scope and accessibility determine what the author can refer to at that use.                                                                      |
| What type does a generic use have?         | Bounds and substitutions can change the type of a member or an expression.                                                                       |
| Which callable does an expression invoke?  | The receiver, arguments, and relevant candidates determine selection.                                                                            |
| Which language/SDK environment applies?    | Java and Kotlin can interpret the same binary differently, and a changed SDK can change the available declarations.                              |

These questions must be answered to the extent required by the owning 1D before it exposes a complete HIR candidate.
Establishment does not reconstruct missing source meaning (ADR-0071 §4.3; ADR-0063 §4.7.1). The separate user
Operation realization path may require similar language information, but is not required to use the 1D resolver or
Contract HIR. This ADR specifies the information and interpretation requirements. It does not establish a second
Contract Type System or prescribe a complete host compiler architecture.

## 3. Decision

### 3.1. Kontrakt Resolves Its Supported Source Surface

**Ordinary 1D Contract compilation uses Kontrakt-owned Java/Kotlin source resolution.** It does not invoke `javac` or
K2 to parse, type-check, or select calls in ordinary authored 1D source. User Operation realization follows a
separate host-compilation path; it is not a 1D HIR source candidate.

The resolver determines the type or operation actually used under the selected language rules. A matching
name or erased JVM type is not enough to establish the selection (ADR-0073 §9.1). What must be interpreted depends
on the permitted source use:

| Source use                    | Why additional type information is needed                                                                                                                             |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Generic type or invocation    | The use applies type arguments to a generic declaration. Applicable bounds constrain the use, and substitution can change the member or expression type.              |
| Overloaded callable           | The receiver and argument types determine which candidates are applicable. The selected callable cannot be known before the relevant candidate search is sufficient.  |
| Kotlin extension or property  | Kotlin's source rules decide whether a member or extension is available and which property or callable the use selects. The JVM representation alone is insufficient. |
| Language-defined operator     | The operand types determine whether the use denotes a built-in operation or a selected callable.                                                                      |
| Lambda supplied to a callable | The selected callable can supply the expected parameter and return types needed to interpret the lambda.                                                              |

A 1D's permitted authoring forms do not limit the supported language profile's underlying type and operation
semantics. For the same source-semantic inputs, resolved meaning must not change with worker scheduling, cache state,
or a different implementation strategy. An unsupported language/SDK profile must not be silently interpreted through
another profile. ADR-0075 governs result reuse; a change in the selected environment does not itself establish a new
Contract Version (ADR-0053).

#### 3.1.1. Complete Interpretation of Language-Defined Operations

A supported Java/Kotlin profile must be interpreted according to the operations defined by that language, even
when an operation is not an SDK call. For a permitted expression, the frontend must determine which operation was
actually selected and what types that operation accepts and produces. A condition that changes operation selection
must be considered before interpretation is complete.

A familiar operator spelling is not enough. Depending on the language and its operand types, the spelling can denote
a built-in operation or a callable defined by authored source or an SDK. Kontrakt cannot replace the selected source
operation with one that appears similar.

This section establishes which operation the source uses, not the calculation law for that operation. Numeric
calculations remain under ADR-0078, Text calculations under ADR-0080, and other 1D-specific calculations under their
owning laws. ADR-0084 owns qualified SDK operation meaning. In particular, this ADR does not redefine value domains,
rounding or overflow behavior. It also does not define evaluation algorithms or decide whether an individual
computation is admissible under a 1D. Detailed operation-coverage and conformance catalogues belong to Frontend
Design and Verification, not to an additional source of Contract law.

#### 3.1.2. Selected Language Profile and Proposed Defaults

Source interpretation must use a definite language environment. The required environment is the one that
actually determines the meaning of the selected source. It cannot be inferred from a target JVM setting or from an
installed compiler alone.

For **Java**, the source language level and enabled features determine the language rules available to the author.
The selected JDK/API level determines which platform declarations may be used. The module and classpath selection
can further change which declarations are visible. These decisions must be distinguished because a JVM target
setting does not, by itself, select the source grammar or limit the available APIs.

For **Kotlin**, the compiler implementation and selected language version determine the language rules in force.
The API version can limit use of library APIs even when those declarations occur in the selected binaries. Relevant
compiler options may change source-semantic behavior. The selected stdlib, JDK, and dependency binaries establish
the declaration evidence available for interpretation. Module and source-set membership, including applicable
friend relationships, can affect visibility. Kotlin metadata and Java interoperability settings may also affect
the type and callable meaning seen by Kotlin source.

JVM output compatibility and settings governing how compilation executes are not automatically determinants of
source meaning. A particular source judgment needs the effective inputs that actually affect it; this ADR does not
require one universal profile object or physical cache key.

If the author has not provided a complete effective profile, Kontrakt uses supported project build configurations,
such as Gradle or Maven, as the primary basis for a **reviewable default-profile draft**. The draft must reflect the
actual compilation configuration after project settings and dependency selection. A build-tool version, the mere
presence of an installed compiler, or one isolated setting in a build script is not sufficient evidence. When a
setting remains uncertain, Kontrakt may propose it from the configured toolchain or environment, but may not treat
that proposal as confirmed. The author or the project's declared selection policy can adopt or override it.

The effective profile must be fixed before any affected source judgment runs. A later environment change cannot
silently replace that profile, and a Binding Result cannot be validated against semantic inputs different from those
that supported its interpretation. Build configuration supplies source-interpretation evidence. In particular,
dependency versions and the
compilation scope can affect which declarations and type rules apply. That evidence does not establish the Contract
Version or applicability owned by ADR-0053 and the relevant 1D.

The V1 ordinary 1D frontend does not implicitly support arbitrary Kotlin compiler plugins by executing them. If a
particular plugin effect is explicitly supported, the selected profile must account for the declarations it generates
and any type relations it changes. Other supported source-semantic effects must likewise have their precise meanings
and applicable determinants established. This
restriction does not govern plugins used by the separate user Operation host-compilation path. Reading build
configuration and executing scripts or plugins have different trust implications. Build Integration and Frontend
Design own acquisition methods and the required trust and isolation measures.

Frontend Design also owns how the profile is discovered and presented for confirmation, which version combinations
are supported, and how the configuration workflow operates. This ADR chooses no specific Java/Kotlin release, SDK
installation, build-system adapter, or source-profile data structure.

#### 3.1.3. Java/Kotlin Language Forms and 1D Source Eligibility

The selected language profile governs how a Java or Kotlin source form is interpreted. Similar syntax or JVM
representations can have different source meanings. For example, a Kotlin property or function type must be
interpreted by Kotlin's rules, rather than translated into a Java field or method merely because of its JVM
representation. The relevant lexical and syntactic rules must be respected insofar as they determine the type or
declaration actually used. Frontend Design owns the lexer, parser, and full grammar-conformance machinery.

Correctly recognizing a language form does not make it a lawful 1D authoring form. Each 1D determines which Source
Carriers and forms it permits (ADR-0047 §7). A build configuration used to identify a language profile is not an
authored 1D declaration and cannot authorize an additional Source Carrier.

Source interpretation may complete details on demand if those details cannot affect the current binding or the
owning 1D's source qualification. A declaration outside the selected file or class still matters when its presence
can change the selected type or callable, or the source restrictions that apply. This is a requirement for complete
interpretation, not a required parsing or resolution schedule.

#### 3.1.4. Selected 1D Source and the Realization Boundary

For a selected 1D, interpretation must cover the source information that determines its candidate meaning. It
must also cover information needed to apply the source restrictions that the owning 1D has already defined. Neither
the selected file nor the direct-call graph necessarily contains all that information. A relevant external
declaration may change a type or callable binding without becoming part of the authored 1D Contract.

In Kotlin, for example, an import can affect which declaration is selected, while file-private and module
visibility can determine whether that declaration is accessible. Source-set relations may also matter. These
language facts are inputs to interpretation; they do not create Contract identity or enlarge the 1D's authoring
unit. Conversely, ADR-0065 §7.6 applies its initialization restriction to the selected Admission class, not to
unrelated external declarations.

The owning 1D determines which parts of its selected declaration must satisfy whole-declaration restrictions.
Source interpretation cannot ignore a restricted part solely because the chosen calculation would not execute it.
The legality of a wider Java/Kotlin compilation and the completeness of an independent 1D candidate remain distinct
judgments (ADR-0071 §6).

User Operation realization follows its separate host-compilation, acquisition, MIR, and verification path. Both
paths may need the same language-defined type information, but they need not share a resolver or candidate
representation. A 1D's authoring restrictions do not determine what language information a future realization
consumer may need.

#### 3.1.5. Source Encoding, Lexical Meaning, and Provenance

A source reference cannot be bound correctly unless the character stream has been decoded under a known encoding.
An unnoticed machine default is not a reliable source of its meaning. Java performs Unicode escape translation under
its own lexical rules, which differ from Kotlin's rules. As a result, the frontend must use the identifier actually
declared or referenced by the selected language, rather than one obtained by visual resemblance or informal
normalization. The same principle applies when lexical interpretation affects a token or literal needed for source
type resolution.

Source positions may be kept to explain where a declaration or use came from. Those positions do not by themselves
define that declaration or a Contract. Frontend and Diagnostic Design own physical decoding, span representation,
and diagnostic rendering. ADR-0080 governs Text meaning; it does not replace the host language's lexical rules.

#### 3.1.6. Shared Declaration Knowledge and Contextual Binding

The frontend may reuse declaration evidence obtained from the selected source and SDK. Knowing that a declaration
exists is not the same as knowing whether a particular use selects it. A successful use must be resolved according
to the applicable source language and the context of that use. Evidence shared between Java and Kotlin does not
thereby acquire one common source-type meaning.

A type name can be affected by an import or a nearer declaration that shadows another name. A member access can
depend on the receiver's type and on whether the member is accessible in that context. Kotlin adds rules that
cannot be reconstructed from JVM members alone. Its file-local and module visibility rules affect which uses are
lawful. Import aliases can change the name through which a declaration is referenced. Member and extension
candidates must be considered according to Kotlin's selection rules. Where a selected profile supports implicit
or context receivers, their applicable typing also contributes to binding. A Kotlin property must retain its
source meaning even if the corresponding JVM representation uses methods.

A general declaration lookup is therefore insufficient to establish the result of a source use. The relevant
judgment must satisfy the contextual availability and candidate-selection requirements in §§3.2.2–3.2.3. Shared
declaration evidence does not become Contract authority.

Completed Binding Results and declaration evidence may be reused when their current semantic inputs still support
them (ADR-0075). V1 may use existing query-oriented reuse, indexing, and caching to avoid repeating work. V2 may refine
dependency tracking and selective invalidation. It may also avoid unnecessary downstream work
through early cutoff or repair only affected results. Neither version may change
binding meaning for the sake of reuse. For example, adding an overload to a newly selected SDK can invalidate an
earlier call selection even when the previously selected method still exists. Reuse must account for that possibility
under §3.2.3; an earlier successful lookup is not proof of current validity.

This decision does not require a complete global source model, one shared table for all 1Ds, or a specific query
boundary. Frontend Design may choose eager or on-demand completion and the physical indexing and reuse granularity
based on measured workloads. Type-meaning interpretation remains governed by §3.1.7, while ADR-0085 owns the
lifecycle of compiler type knowledge. HIR retains only the resolved distinctions needed by its owning 1D (§3.4).

#### 3.1.7. Language-Specific Type Meaning and Interoperability

Java and Kotlin must each be interpreted under the type rules of the selected source-language profile. Even if both
languages read the same SDK binary, a common class name or JVM descriptor does not imply the same source type or
callable meaning. Kontrakt must preserve the distinctions that actually affect a permitted 1D source use, rather
than infer a type from its spelling or erased JVM representation.

For a **generic declaration**, the type parameters and their applicable bounds constrain how the generic type or
call may be used. A use can require more than one bound to hold. Substitution then determines the types seen at that
particular use.

For example, suppose `Base<T>` declares a method returning `T` and `Derived<U>` extends `Base<List<U>>`. Through
`Derived<U>`, the inherited method has return type `List<U>`. If the source uses `Derived<String>`, the returned
type is `List<String>`. Retaining only the original method declaration or its erased return type would lose the type
seen by the source author.

Java and Kotlin apply different rules to several common generic situations:

| Source situation                 | Information required for the actual type judgment                                                                                                                                                  |
|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java raw generic use             | The use must be recognized as raw rather than parameterized. Erasure and any applicable unchecked conversion are then interpreted under Java rules.                                                |
| Java wildcard use                | The relevant upper or lower bound constrains how the parameterized type can be used. Wildcard capture must be considered where Java's type rules require it.                                       |
| Kotlin declaration-site variance | A type parameter's `out` or `in` declaration changes the applicable type relations. Its argument cannot be compared as though every generic parameter were invariant.                              |
| Kotlin use-site projection       | A projection changes which type can be read or supplied at a particular use. That effect must be applied when interpreting member types.                                                           |
| Kotlin star projection           | The readable and writable positions follow the declared variance and applicable upper bounds. `*` must not be interpreted automatically as one unrestricted `Any?` argument or as a Java raw type. |

A Java capture cannot be replaced by a convenient concrete type simply because its bounds are known. Separately
captured wildcards must remain distinguishable where the language's type judgment depends on that distinction.
Similarly, matching erasure does not make a raw type and a parameterized type interchangeable. These requirements
concern accurate Java interpretation; they do not grant any generic source form permission under a 1D Contract.

Kotlin also requires distinctions that Java's erased representation cannot supply. Nullability changes the type
relationships available to source expressions. A flexible or platform type cannot be forced into one universally
transitive type relation merely to simplify comparison. Function types and value classes have source meanings that
may differ from their JVM representations. Their relevant distinctions must survive interpretation when they change
a type, member, or callable selected by a permitted source use.

The interpretation must use the particular type judgment required by the source language, not treat every
successful type check as the same relation:

| Judgment                             | Question it answers                                                                                                                                          |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Type identity or equivalence         | Whether two type uses are the same or equivalent under the applicable language rule. This does not follow merely from a common erasure.                      |
| Subtyping or assignability           | Whether a value of one type can be used where another is required. These judgments are not interchangeable with type identity.                               |
| Conversion                           | Whether a specified language conversion makes the use legal. A permitted conversion does not make the source and target types identical.                     |
| Type-argument containment or capture | How a generic argument is constrained and interpreted for this use. Matching bounds do not make separate captures identical.                                 |
| Inference                            | What type information is determined from the available constraints. The inferred result must still satisfy the separate relation required by the source use. |

The exact types and declarations referred to by these judgments follow the consuming language's rules. How the
frontend assigns or stores internal identifiers is outside this ADR. These distinctions preserve existing language
meaning; they do not create new Kontrakt type relations.

Evidence from selected SDK binaries may be decoded once and reused for both languages under §3.1.6. The evidence
can include classfiles, generic signatures, annotations, and Kotlin metadata. Shared decoding does not produce a
language-neutral semantic type. When Java source uses a Kotlin-produced declaration, the Java view is determined by
the Java source rules and the qualified JVM/ABI evidence. When Kotlin source uses a Java-produced declaration, Kotlin
applies its interoperability rules to the available evidence. The latter can change the apparent nullability or
collection mapping. These directions need not be symmetric or lossless. Contradictory or incomplete evidence cannot
be silently combined into a convenient type result (§3.5).

An interop interpretation belongs to the consuming language's source judgment. Reusing it requires the same relevant
semantic inputs and current validity under ADR-0075. Kontrakt is not required to establish a shared semantic Type
Identity or an authoritative global interop-relation graph. Such a global relation could propagate one language's
outdated inference to an unrelated use. Reuse of common binary evidence and language-specific interpretations is
nevertheless permitted. Whether they share readers, type tables, or processes is a Frontend Design and ADR-0085
implementation decision.

### 3.2. SDK Declarations Are Prepared Before User Compilation

Kontrakt prepares the SDK declaration information needed by supported Java/Kotlin profiles before ordinary user
compilation. The frontend uses that prepared knowledge rather than rebuilding all needed SDK declarations from the
installed libraries on every build.

The preparation basis is the compiled binary selected by the effective language/SDK profile. Generic signatures and
type annotations can change how Java source sees a method; Kotlin metadata can change how Kotlin source interprets
a declaration. The selected archive member and source-to-binary mapping must therefore correspond to the actual
profile. A file name or SDK version label is not sufficient evidence. Interpretation does not obtain declaration
meaning by executing SDK methods.

Common binary evidence may be decoded once, but Kotlin source declarations need not correspond one-to-one with
Java-visible or JVM implementation members (§3.1.7). SDK source and language specifications can assist
interpretation or qualification; neither automatically replaces the selected binary declarations. `javac` and K2
may serve as evidence during preparation and conformance work, but they are not invoked as the ordinary 1D source
resolver.

**Declaration knowledge and qualified operation meaning answer different questions.** The first lets Kontrakt
select the source operation. The second establishes the behavior qualified for that selected operation under
ADR-0084 §3. Physical storage may be shared, but a method signature alone never proves calculation meaning.

#### 3.2.1. Prepared Lookup and Required Completion

V1's working direction is hybrid preparation. Kontrakt prepares a profile-bound lookup basis for the SDK declarations
relevant to its supported 1D source surface. Detailed declaration information may be completed when a source use needs
it; preparing the basis does not require eagerly expanding every SDK declaration. The resulting interpretation must
still account for every declaration that can affect the supported use. Finding one plausible method is not sufficient
when another applicable overload may exist.

Completion is measured against the actual source judgment, not against whether an individual classfile was parsed.
The frontend cannot claim a successful interpretation without the required declarations and relations (§3.5). This
section does not select a Java/Kotlin search algorithm. Frontend Design determines how to index declarations,
when to complete their details, and where query boundaries should be placed. Physical storage layout and the
lifecycle of compiler type information remain outside this decision and within the applicable Frontend Design and
ADR-0085 responsibilities.

#### 3.2.2. Source Availability in the Permitted 1D Surface

A declaration present in SDK binary evidence is not necessarily accessible to the authored source. Package or
module boundaries may make it unavailable. The receiver and the language's visibility rules can also change whether
the source may use it. Source availability therefore requires a judgment in the selected Java/Kotlin context; a
single global "available" flag on a binary method is insufficient. Kotlin's source visibility and its callable or
property form can differ from their JVM representations (ADR-0073 §9.1).

Each 1D determines which authoring forms are lawful; the ability to resolve an otherwise prohibited source form
does not make it eligible. The required source access rules must still be applied precisely wherever a selected use
is lawful. Prepared accessibility facts can be reused, but cannot replace context-specific source binding.

#### 3.2.3. Complete Source Candidate Selection

Within the source forms permitted by the owning 1D, Kontrakt must determine which declarations are candidates
for a Java or Kotlin call. The source language's rules decide what the call name denotes in its scope. The receiver
and access conditions may change which members can participate. The actual callable is selected from the relevant
candidate set under that language's selection rules.

That set cannot be limited to operations for which Kontrakt already has qualified SDK meaning. An unqualified
overload or a permitted authored helper still participates in source selection when the language rules make it
relevant. Otherwise an unrelated available interpretation could incorrectly determine the call.

Candidate search may proceed in stages and may stop when the applicable language rules establish that no unexamined
candidate can change the outcome, including ambiguity. Finding the first applicable declaration is not such a proof.
A rejected provisional candidate is not necessarily a failed source-resolution judgment. If the relevant candidate set
cannot be established sufficiently, the resolver cannot select a successful call (§3.5).

A new overload, member, extension, or other declaration may change selection under a newly chosen profile. Reuse must
therefore establish the current adequacy of the candidate search, including any absence of competing declarations on
which the outcome depends. ADR-0075 owns the reusable result's validity; Frontend Design owns the search, indexing, and
incremental strategy. Java and Kotlin candidate-selection rules remain language-specific.

### 3.3. Resolved Calls Use Qualified Meaning

A source operation is first selected according to the language rules and the information requirements in
§§3.1–3.2. Only then may Kontrakt attach its qualified calculation meaning. ADR-0084 determines whether that
qualified meaning applies to the exact SDK operation and use.

For a generic SDK call, the effective argument and return types can depend on the actual type substitution. The
receiver context may also affect the applicable qualification. A signature or JVM-callable target alone cannot
prove the operation's calculation meaning. Qualified generic meaning may be reused across type instantiations only
when the qualification covers each selected use. Where dynamic dispatch matters to the qualification, the static
receiver type alone does not establish the implementation that will be invoked (ADR-0073; ADR-0084).

Qualified meaning cannot change which overload the language selected. If that operation lacks adequate qualified
meaning, Kontrakt may neither choose a different overload nor assume a convenient runtime implementation. The
Numeric, Text, or other Contract calculation remains governed by the owning ADR.

### 3.4. Forming the HIR Candidate

Before a 1D candidate can be made available to Establishment, it must contain the completed source meaning needed
by that owning 1D (ADR-0071; ADR-0063). The candidate does not become a general-purpose Java/Kotlin type model.
For example, if a permitted generic call determines a 1D expression's result type, the candidate must retain the
resulting meaning needed by that 1D. It need not retain every provisional generic constraint or every rejected
call candidate that the frontend considered.

The 1D owns its candidate vocabulary. This ADR does not require HIR to retain a host Type Model, the SDK declaration
database, solver state, or an interoperability graph. A resolved type or callable distinction must be preserved
when it changes the candidate's meaning; information that no longer affects that meaning can remain within the
frontend.

Establishment applies the owning Contract law to this resolved candidate. A host type or HIR transport reference
does not gain Contract authority from its role in source interpretation.

### 3.5. SDK Binary Evidence and Unsuccessful Source Interpretation

#### 3.5.1. Supported SDK Interpretation and Evidence Adequacy

Kontrakt must correctly interpret normal, conforming SDK binaries within its declared supported Java/Kotlin
profiles whenever permitted 1D source uses require that interpretation. A missing declaration can change which
callable is selected. An incorrect generic relation can change the type seen by the author. An incorrect Kotlin
mapping can change the result even when the underlying JVM binary is the same. If relevant callable candidates
were not fully accounted for, a selected call may likewise be wrong.

These failures are compiler correctness defects within declared support. A limitation of the current resolver or
prepared SDK knowledge cannot turn them into lawful unsupported-source outcomes. Detecting such a defect still
forbids a false successful result. By contrast, a source form that an owning 1D does not permit is restricted under
that 1D's law, irrespective of whether the language itself supports it.

Successful source interpretation requires the declarations that actually determine the type or call under the
selected language rules. If another declaration could alter overload selection, the candidate search is not yet
complete (§§3.2.1–3.2.3). Decoding a classfile or metadata record is not proof that a particular source use selects
that declaration.

The interpretation must use evidence from the binaries selected by the current profile. Where a source declaration
maps to a JVM declaration, that correspondence must be established for the actual selected binary. The consuming
language's interoperability rules must also be applied. Matching names or erased JVM representations do not justify
combining evidence or source-type results from different binaries, profiles, or use contexts.

The normal absence of optional binary data is not corruption. For example, a non-generic Java declaration may
have no generic `Signature`, and Java declarations are not required to carry Kotlin metadata.

Conversely, if the binary or declaration required for a source judgment cannot be obtained, its absence from prepared
knowledge does not show that the source candidate is absent. The same applies when a required generic signature or
source-to-binary mapping cannot be interpreted. An unusable item can be disregarded only when the applicable language
rules establish that it cannot affect the current judgment. This includes declarations that could affect overload
selection; failing to read or discover one does not establish its irrelevance.

The success of an unrelated judgment and the eligibility of earlier compiler products remain governed by ADR-0071
and ADR-0075. This ADR does not create a separate SDK-wide failure-containment law.

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
basis**, provided that the necessary declarations and relationships are actually established. SDK source files or
another reader's output do not automatically correspond to the selected binary. The same applies to host-compiler
evidence: it must be shown to refer to the binary and source basis in question.

Kontrakt cannot repair missing type information by guessing from JVM erasure. It cannot make an overload selectable
by ignoring a competing candidate, or change the SDK version to obtain a more convenient declaration. Executing
arbitrary SDK or authored code to manufacture a source interpretation is also prohibited (ADR-0084 §§3.4–3.5).

Reuse of an earlier product remains conditional on ADR-0075's validity rules. Missing or conflicting evidence cannot
be consumed as a completed result, and a later successful attempt does not retroactively make a failed source
judgment successful.

#### 3.5.4. Verification and Implementation Boundary

This ADR determines what constitutes a correct and sufficiently evidenced source interpretation. It also fixes how
an unsuccessful judgment must be distinguished from a successful one. It does not prescribe which component performs
the necessary checks or when they run. Verification and coordination responsibilities may be divided among the
responsible producers, verifier subsystems, and compiler managers.

Implementations may choose eager or deferred validation. They may also change reuse, parallel execution, recovery,
and scheduling strategies after profiling. None of those choices can excuse a missing condition when a completed
source result is consumed or a valid HIR candidate becomes visible.

Correct interpretation of normal inputs within a supported profile remains a compiler-product obligation. Error
handling cannot replace the conformance and QA work needed for that obligation. Frontend and Compiler Design own
binary reading and archive selection, as well as resource safeguards and validation algorithms where applicable.
ADR-0085 owns physical type storage and the lifecycle of compiler type knowledge. ADR-0075 governs compiler-product
integrity, trust, and reuse.

The owning Failure, Compiler Result, and Diagnostic documents determine how unsuccessful compiler results are
represented and explained. This ADR does not define their output format or diagnostic rendering. In particular, a
compiler source-interpretation failure is not an ADR-0057 Contract Machine Failure.