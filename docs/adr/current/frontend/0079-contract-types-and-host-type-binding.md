# ADR-0079: Type Interpretation and Semantic Binding

**Status:** Accepted

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

Each 1D restricts which source forms the author may use; that restriction does not change the language meaning of a
form that is both permitted and within declared support. For such a use, Kontrakt must apply the relevant Java/Kotlin
type and operation rules in full, even when those rules involve forms that the 1D does not itself permit authors to
write. Unsupported uses and language-invalid uses remain distinct (§3.5). For the same source-semantic inputs,
resolved meaning must not change with worker scheduling, cache state, or implementation strategy. An unsupported
language/SDK profile cannot be silently interpreted through another profile. ADR-0075 governs result reuse; a
changed environment does not itself establish a new Contract Version (ADR-0053).

#### 3.1.1. Complete Interpretation of Language-Defined Operations

A supported Java/Kotlin profile must be interpreted according to the operations defined by that language, even
when an operation is not an SDK call. For a permitted expression, the frontend must determine which operation was
actually selected and what types that operation accepts and produces. A condition that changes operation selection
must be considered before interpretation is complete.

The spelling of an expression does not necessarily identify the operation it uses. A familiar operator can denote
a language-defined built-in operation or a selected callable, depending on the language and operand types.

For example, Kotlin can interpret `a[i]` through a selected `get` callable, while `a(x)` may use `invoke`. A
property access can also resolve to a source property whose JVM representation is a method. Other permitted language
conventions and built-in operations are resolved under their own rules, not by guessing from surface syntax. The
actual operation and its types must be determined; an apparently similar operation cannot replace it.

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
The effective platform API selection, module path, and classpath determine which declarations the source may use.
A JVM target setting alone does not select the source grammar or API surface. Options such as `javac --release`,
however, jointly constrain the source language, target, and platform APIs; their combined effects must be respected.
An API view selected for an earlier release cannot be replaced by declarations from the currently installed JDK.

For **Kotlin**, the compiler implementation and selected language version determine the language rules in force.
The API version can limit library API use even when declarations are present. Relevant compiler options may change
source-semantic behavior. The selected stdlib, JDK, dependencies, language built-ins, and in-scope source declarations
contribute the evidence and rules needed for interpretation. Module and source-set membership, including applicable
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
silently replace it, and a Binding Result cannot be validated against different semantic inputs. Build configuration
supplies evidence for source interpretation. Selected dependencies and compilation scope may change the declarations
and type rules that apply. They do not establish Contract Version or applicability, which remain governed by
ADR-0053 and the owning 1D.

Ordinary 1D source interpretation does not implicitly execute or accept arbitrary compiler plugins, annotation
processors, or generated-source transformations. When such an effect is explicitly supported and belongs to the
selected compilation environment, its generated or altered declarations and type relations must be accounted for
before affected bindings are complete. A generated declaration cannot be presumed to exist merely because a build
script names a processor. This restriction does not govern the separate user Operation host-compilation path.
Reading build configuration differs from executing scripts, processors, or plugins; Build Integration and Frontend
Design own acquisition methods and trust and isolation measures.

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
Carriers and forms it permits (ADR-0047 §7). Those authoring restrictions govern eligibility, while the selected
language rules govern the meaning of a permitted and supported use (§3.1). Build configuration used to identify a
profile is not an authored 1D declaration and cannot authorize an additional Source Carrier.

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

The same type name can refer to different types when an import or a nearer declaration changes which name is
visible at the use. The source language also decides whether the referenced declaration is accessible there. For
Kotlin, file-local and module visibility must be respected, and an import alias must resolve to the type it actually
names. The fact that a declaration was found elsewhere does not make it a valid type for this source use.

A member's type can depend on the receiver through which it is used. An explicit receiver supplies a type for
member selection and generic substitution; a call without an explicit receiver can instead depend on the implicit
receivers available at that location. Kotlin further distinguishes member and extension candidates, and its
receiver-selection rules can be affected by language-defined restrictions such as `@DslMarker`. If the selected
language rules admit context parameters or context receivers for the permitted use, their typing must be considered
as well. These conditions determine which source member or callable is selected. A Kotlin property retains its
source meaning even if its JVM representation uses methods.

Modifiers and annotations participate in source typing only to the extent that the selected language assigns them
that effect. For example, an annotation that changes Kotlin's candidate selection cannot be ignored when that
selection determines a type. An ordinary annotation does not, merely by being present, establish a new type relation
or guarantee that the annotated callable is pure or safe under a Contract. Qualification of the selected operation
remains governed by ADR-0084 and the owning 1D.

The selected language can provide source members that were not explicitly written as methods. Kotlin data classes,
for example, provide `componentN` and `copy` members under Kotlin's rules. Java records provide component accessors,
including accessors that the author may explicitly implement. The type of a source use must reflect the member the
language actually provides. Conversely, a bridge or other JVM implementation helper is not an independent source
callable merely because it appears in a classfile. A source member's type does not establish its runtime behavior or
its admissibility under a 1D.

A general declaration lookup is therefore insufficient to establish the result of a source use. The relevant
judgment must satisfy the contextual availability and candidate-selection requirements in §§3.2.2–3.2.3.

Completed Binding Results and declaration evidence may be reused only when the current source judgment still
supports them (ADR-0075). For example, adding an overload can invalidate a previous call selection even though the
originally selected callable still exists. An earlier successful lookup does not establish current candidate
sufficiency (§3.2.3). Reuse and incremental implementation belong to Frontend Design.

This decision does not require a global source model or a shared physical type table. Frontend Design determines
when knowledge is completed and how it is indexed or reused. ADR-0085 owns the lifecycle of compiler type knowledge.
Neither implementation choice changes the language-specific type meaning governed by §3.1.7. HIR retains only the
resolved distinctions needed by its owning 1D (§3.4).

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
Similarly, matching erasure does not make a raw type and a parameterized type interchangeable. Java arrays also
have language-defined type relationships and special members, including `clone` with an array return type; treating
them as ordinary `Object` members would lose source meaning. An enclosing generic type can affect the meaning of a
nested member type and its inherited members.

Kotlin nullability affects which type relations and member uses are valid. A flexible type, including a platform
type obtained through Java interoperability, has the bounds supplied by Kotlin's language rules. It must not be
replaced with either its non-null or nullable bound merely to simplify the judgment. Nor may its comparisons be
forced into a universally transitive relation that Kotlin's flexible type rules do not establish. Kotlin's
definitely non-null type forms also matter where a generic use requires them. When recognized annotations on Java
declarations affect the types visible to Kotlin, interpretation must respect the annotation's actual type-use
location and the selected language rules. Source typing does not, by itself, guarantee that an external value will be
non-null at runtime.

A Kotlin type alias denotes the type to which the alias resolves; it does not create a new independent source type.
The applied type arguments still matter when the alias refers to a generic type. A Kotlin value class, by contrast,
has its own source type even where the JVM represents a value through the underlying type. The source distinction
must not be discarded because the host representation looks similar.

For example, an alias for `String` is usable as `String`. A value class whose underlying property is a `String`
is not thereby interchangeable with `String` in Kotlin source.

Function types must likewise retain their language-defined meaning. Kotlin permits function types whose calls
require a receiver. A suspending function type also has type rules that differ from those of an ordinary function.
A Java functional interface and a Kotlin function type are not made identical by matching parameter and return types.
Their source meanings cannot be inferred merely from similar JVM method signatures. Where the selected language
permits SAM conversion, it must be interpreted in the actual target context; the conversion does not make the two
declared types identical. Resolving a function type does not make a higher-order source form admissible under a 1D
that prohibits it.

A callable reference is not necessarily interpreted as an ordinary call. Its referenced callable and resulting
function or property-reference type depend on the reference form and its applicable target context. A bound member
reference can already supply its receiver, whereas an unbound reference can leave the receiver as part of the
reference's function type. For Java, the language's exact and inexact method-reference rules can also affect whether
an argument contributes to candidate applicability. Kontrakt must use the applicable reference-selection rules,
not assume that the same spelling selects the same declaration in a direct invocation.

Some expressions require type relationships that do not appear explicitly in the authored source. When an
expression combines alternatives with different types, its result can depend on a language-defined common bound.
When several type constraints must hold, an intersection or another constrained type may be needed to determine
which members remain usable. Kotlin's `Nothing` can affect the type of an expression that has a branch which does
not return a value. A literal can also require contextual typing before its final type is known.

Kontrakt must preserve the effects of those relationships until the relevant source judgment is complete. It must
not substitute a convenient wider type for a distinction that changes the result. This obligation neither adds
new authoring forms nor requires provisional types to become part of the final HIR candidate (§3.4).

The interpretation must use the particular type judgment required by the source language, not treat every
successful type check as the same relation:

| Judgment                             | Question it answers                                                                                                                                          |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Type identity or equivalence         | Whether two type uses are the same or equivalent under the applicable language rule. This does not follow merely from a common erasure.                      |
| Subtyping or assignability           | Whether a value of one type can be used where another is required. These judgments are not interchangeable with type identity.                               |
| Conversion                           | Whether a specified language conversion makes the use legal. A permitted conversion does not make the source and target types identical.                     |
| Type-argument containment or capture | How a generic argument is constrained and interpreted for this use. Matching bounds do not make separate captures identical.                                 |
| Inference                            | What type information is determined from the available constraints. The inferred result must still satisfy the separate relation required by the source use. |

A type conversion must be interpreted under the rule for its actual source use. Java, for example, does not
apply exactly the same permitted conversions in assignment and in each phase of invocation applicability. Kotlin
does not inherit Java's implicit numeric widening merely because the JVM has related numeric representations.
The original expression type and the type at which a converted use is accepted must therefore not be conflated.
A Java constant expression or constant variable can affect whether narrowing is allowed in an assignment, even
when an otherwise similar nonconstant expression has the same static type. A compound assignment can also use
language-defined conversion rules distinct from an ordinary assignment. These facts must be interpreted where they
change the actual source judgment. For example, `final int n = 10; byte b = n;` can be legal in Java where the
same assignment from a nonconstant `int` is not. A source-level conversion does not prove that a cast or unboxing
will succeed at runtime. Calculation and failure meaning remain with their owning laws.

When a source declaration omits a type, the language rules for that form determine the inferred declaration type.
Java local `var` uses the appropriate initializer typing and projection rules. Kotlin property inference and
function-return inference do not follow one interchangeable rule for every declaration form. In particular, a type
made available by a smart cast at one use is not automatically the inferred declared type of another variable.
If the language cannot determine the required type, Kontrakt must not invent a wider type and report success.

A permitted expression may have a type refined by its control-flow context. Kotlin applies smart casts only where
the relevant flow conditions and stability requirements hold. Language-recognized Kotlin function contracts can
supply flow or calls-in-place facts relevant to such judgments; their effects must be interpreted under Kotlin's
actual rules. These host-language contracts are typing evidence, not Kontrakt Contract authority or permission to
invoke the function in a 1D. Java pattern variables have their own flow-sensitive scope and typing rules. A
condition that refines one use must not silently rewrite a declaration's type or justify the same refinement at
an unrelated use.

The surrounding expected or target type can constrain an expression's interpretation. For example, a lambda may
receive its parameter types from a candidate callable, while an invocation's type arguments and resulting type can
depend on the context in which the invocation occurs. Candidate selection and contextual inference must interact
only as the selected language specifies. Kontrakt cannot universally fix every argument type before considering
candidates, nor choose a method merely because its result type matches an expected result.

For example, Kotlin can allow a lambda's return type to distinguish candidates under its
`@OverloadResolutionByLambdaReturnType` rules. This does not authorize the selection of arbitrary calls by their
return types. An expected type is a constraint on interpretation, not automatically the expression's final type.

The types referred to by all these judgments follow the consuming language's rules. How the frontend assigns or
stores internal identifiers is outside this ADR.

Declaration evidence from selected binaries or in-scope source may be shared across languages (§3.1.6). Shared
evidence does not produce one language-neutral semantic type. Kotlin built-in and Java-mapped type rules are also
part of Kotlin source meaning; they cannot be inferred solely from JVM descriptors or metadata.

When Kotlin source uses a Java declaration, whether supplied by source or selected binary, Kotlin's interoperability
rules determine the type it sees. A Java type can become flexible in Kotlin, including distinctions concerning
nullability or collection mutability. Recognized type-use annotations may affect only a particular part of a generic
type; their effect must not be applied indiscriminately to the enclosing type. A JVM `void` result does not thereby
become a Kotlin source type named `void`.

When Java source uses a Kotlin declaration, Java's rules apply to the declarations exposed to Java in the selected
compilation environment. For an already compiled dependency, the selected Java-visible binary is the evidence. If
a supported source relationship must be resolved before that binary exists, equivalent Java-visible declaration
meaning must be established from valid source or other profile-grounded evidence; the original Kotlin declaration
cannot simply be treated as a Java callable. A Kotlin property may be exposed through accessors, and Kotlin default
arguments do not automatically authorize abbreviated Java calls. JVM-facing names and members can differ from the
Kotlin source, and metadata can help interpret that correspondence without replacing the actual Java-visible
meaning. Interoperability need not be symmetric or lossless. Contradictory or incomplete required evidence cannot
be combined into a convenient type result (§3.5).

An interop interpretation belongs to the consuming language's source judgment. Reusing it requires the same relevant
semantic inputs and current validity under ADR-0075. Kontrakt is not required to establish a shared semantic Type
Identity or an authoritative global interop-relation graph. Such a global relation could propagate one language's
outdated inference to an unrelated use. Reuse of common binary evidence and language-specific interpretations is
nevertheless permitted. Whether they share readers, type tables, or processes is a Frontend Design and ADR-0085
implementation decision.

### 3.2. Declaration Evidence and SDK Preparation

Source binding uses the declarations that the selected language environment makes relevant to the particular use.
These may come from selected SDK APIs, project dependencies, in-scope authored source, or explicitly supported
generated declarations. The scope of source binding is not limited to a prepared SDK inventory. Conversely, the
presence of a declaration in that inventory does not establish its availability at a particular source use.

The SDK declaration knowledge required by a supported source use must be available before its interpretation is
considered complete. Preparation and reuse of that knowledge may reduce repeated work but do not define its meaning. For
selected compiled declarations, JVM
descriptors alone may omit generic meaning; signatures, type-use annotations, Kotlin metadata, and the actual
selected archive member may be needed. An effective Java release may instead supply its platform API declarations
through release-specific symbol evidence rather than the installed JDK's executable classfiles. Language built-ins
and in-scope source declarations have their own legitimate evidence. In every case, the evidence must correspond to
the actual selected semantic environment; a file name or SDK version label alone is not enough. Declaration meaning
is not obtained by executing SDK methods.

Binary evidence may be shared, but Kotlin source declarations need not correspond one-to-one with Java-visible or
JVM implementation members (§3.1.7). SDK source, language specifications, and compiler output can assist
interpretation or qualification, but cannot override the actual declaration meaning for the selected environment.
`javac` and K2 may provide preparation and conformance evidence; ordinary authored 1D source resolution remains
Kontrakt-owned (§3.1).

**Declaration knowledge and qualified operation meaning answer different questions.** Declaration meaning permits
source operation selection. Qualified meaning establishes that operation's behavior for the particular use under
ADR-0084 §3. Shared physical storage would not make a method signature proof of its calculation meaning.

#### 3.2.1. Prepared Lookup and Required Completion

A prepared declaration lookup may supply profile-bound SDK evidence, while detailed meaning may be completed when
an actual source use requires it. The sufficiency of the result is measured against that use and its complete
language-defined declaration environment, not against the preparation inventory or whether a particular classfile
was parsed. Finding one plausible overload is insufficient if another eligible candidate may affect the result.

This requirement does not select eager or hybrid preparation, a query boundary, an index, or a search algorithm.
Frontend Design determines these choices and the handling of source and generated declarations. Physical type
storage and knowledge lifecycle remain within Frontend Design and ADR-0085; §3.2.3 owns candidate sufficiency.

#### 3.2.2. Source Availability in the Permitted 1D Surface

A declaration established from SDK or source evidence is not necessarily accessible to the authored use. Package or
module boundaries may make it unavailable. The receiver and the language's visibility rules can also change whether
the source may use it. Source availability therefore requires a judgment in the selected Java/Kotlin context; a
single global "available" flag on a binary method is insufficient. Kotlin's source visibility and its callable or
property form can differ from their JVM representations (ADR-0073 §9.1).

Source access rules must be applied to each permitted use. Prepared accessibility facts can be reused, but cannot
replace context-specific source binding. The separate 1D authoring restrictions remain governed by §3.1.3.

#### 3.2.3. Complete Source Candidate Selection

Within the source forms permitted by the owning 1D, Kontrakt must determine the candidates for a Java or Kotlin
call under that language's rules. The judgment depends on the selected profile's declaration environment and the
use-specific name context, imports, access rules, receivers, arguments, and applicable target-type constraints.
The language-derived member set, including relevant inherited and synthesized members, as well as applicable top-level
or extension declarations, must be considered when they can affect this use. Candidate discovery is sufficient only
when no unexamined declaration in that environment could change the selected callable or the judgment of ambiguity.
Finding one applicable callable is not proof of sufficiency.

Each candidate must be assessed using the argument-to-parameter correspondence that its language actually permits.
In Kotlin, named arguments can identify parameters independently of their declaration order, and default arguments
can affect whether a candidate applies. In Java, the applicability of a variable-arity method depends on the
language-defined invocation phases. The chosen language also determines how spread arguments and function-valued
arguments participate. Argument mapping is part of deciding whether a candidate applies; it cannot be supplied as an
unexamined assumption after selection.

A candidate can supply the target type needed to interpret an argument. In such cases, candidate applicability and
argument typing must be considered together as the language requires. Generic invocation inference can also have
distinct obligations when checking whether a candidate applies and when establishing the final invocation type.
The final type must reflect every constraint that actually participates in its determination, including constraints
from an allowed lambda body where the language makes them relevant. Neither a provisional inference result nor a
convenient wider type can replace the resolved invocation type.

The language's candidate priority and most-specific selection rules must be applied before a call is considered
resolved. Language-defined modifier or annotation effects on that selection remain part of the judgment (§3.1.6).
Java's invocation phases and Kotlin's prioritized candidate sets need not select the same result from apparently
similar declarations. If more than one candidate remains without a language-defined unique selection,
the call is ambiguous. An unqualified SDK overload or a permitted authored helper must still participate whenever
the language makes it relevant; qualified meaning cannot be used to preselect a convenient overload (§3.3).

Candidate search may proceed in stages. It may stop once the language rules establish that further candidates cannot
alter selection or ambiguity, but an unsuccessful provisional candidate does not by itself fail the whole source use.
If the relevant candidates or their required typing cannot be established sufficiently, no successful call can be
selected (§3.5).

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
prove the operation's calculation meaning.

A qualified meaning can cover a particular generic instantiation or a range of instantiations, provided that its
conditions actually cover the selected use. Whether that meaning is stored or reused per instantiation or in a
parameterized form is not decided here. Where dynamic dispatch matters to qualification, the static receiver type
alone does not establish the implementation that will be invoked (ADR-0073; ADR-0084).

Qualified meaning cannot change which overload the language selected. If that operation lacks adequate qualified
meaning, Kontrakt may neither choose a different overload nor assume a convenient runtime implementation. The
Numeric, Text, or other Contract calculation remains governed by the owning ADR.

### 3.4. Forming the HIR Candidate

Before a 1D candidate can be made available to Establishment, it must contain the completed source meaning needed
by that owning 1D (ADR-0071; ADR-0063). The candidate does not become a general-purpose Java/Kotlin type model.
For example, if a permitted generic call determines a 1D expression's result type, the candidate must retain the
resulting meaning needed by that 1D. It need not retain every provisional generic constraint or every rejected
call candidate that the frontend considered.

An expression's result type does not, by itself, determine its 1D meaning. For example, when Admission permits
both `amount > 0` and `amount >= 0`, their Boolean result types do not erase the difference between the judgments.
If a selected operation or source-language conversion contributes to candidate meaning, the resolved distinction
must survive in the candidate. A host callable signature cannot replace the qualified calculation meaning that
this 1D requires (ADR-0084). An exact reference to separately owned meaning is sufficient only when the owning
1D law already permits that relation and the required meaning is resolved. Neither source binding nor SDK
qualification may be deferred to Establishment.

The 1D owns its candidate vocabulary. This ADR does not require HIR to retain a host Type Model, the SDK declaration
database, solver state, or an interoperability graph. Information that no longer affects the candidate's meaning
can remain within the frontend.

Establishment applies the owning Contract law to this resolved candidate. A host type or HIR transport reference
does not gain Contract authority from its role in source interpretation.

### 3.5. Source Evidence and Unsuccessful Source Interpretation

#### 3.5.1. Supported Source Interpretation and Evidence Adequacy

Within declared support, Kontrakt must correctly interpret permitted Java/Kotlin source uses and the required
conforming SDK, project, and language evidence. A missed declaration can change which callable is selected; a wrong
generic relation or Kotlin mapping can change the type seen by the author. An incomplete candidate set can likewise
produce a wrong call selection. Declared support must distinguish the effective profile and source-language uses it
covers, so unsupported use can be identified independently of a resolver's failure on an otherwise supported use.
The detailed supported-feature and version inventory belongs to Frontend Design.

An incorrect interpretation within declared support is a compiler correctness defect. Missing resolver capabilities
or incomplete prepared knowledge cannot retroactively turn a supported source use into an unsupported one. Such a
defect never licenses a false successful result. A form forbidden by an owning 1D is instead restricted under that
1D's law, even when it is valid in the selected Java/Kotlin language.

Success requires the relevant declarations and relations in the actual source judgment (§3.2.3). Decoding one
classfile or metadata record does not prove that a particular use selects its declaration. For a compiled dependency,
evidence must match the binary selected by the current profile; when source or release-specific symbol evidence is
required instead, it must represent that same selected environment. Where source-to-binary correspondence matters,
it must be established rather than presumed. The consuming language's interoperability rules apply in each case.
Matching names or erasures do not justify combining results from distinct profiles or use contexts.

The normal absence of optional binary data is not corruption. For example, a non-generic Java declaration may
have no generic `Signature`, and Java declarations are not required to carry Kotlin metadata.

Conversely, if required declaration evidence cannot be obtained, its absence from prepared knowledge does not prove
that the declaration is absent. The same applies when a required generic signature, source projection, or
source-to-binary correspondence cannot be interpreted. Evidence can be disregarded only if the language rules
establish that it cannot affect the current judgment; unread or undiscovered competing candidates are not thereby
irrelevant.

The success of an unrelated judgment and the eligibility of earlier compiler products remain governed by ADR-0071
and ADR-0075. This ADR does not create a separate SDK-wide failure-containment law.

#### 3.5.2. Unsuccessful Conditions and Their Ownership

A source type judgment succeeds only when its required declarations, type relations, and use-specific constraints
are resolved. Incomplete inference cannot be made successful by choosing a wider type. Equally, a type relation or
candidate whose applicability remains unresolved cannot be treated as false or inapplicable. This applies when a
calculation cannot complete because of recursion, a computation budget, cancellation, or resource exhaustion. Such
an interruption does not decide the source-language relationship; its handling remains under the compiler-result
and resource policies.

The following conditions must remain distinguishable, without prescribing one diagnostic or result schema:

| Condition                                  | Required distinction                                                                                                                               |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| Unsupported language use                   | The use falls outside previously declared support for the effective profile; this is not proof that the source is invalid under Java/Kotlin.       |
| Invalid source use                         | The selected language rejects the resolved use, including inaccessibility or ambiguity, after the applicable judgment is sufficiently established. |
| Required declaration evidence unavailable  | An essential source, symbol, or binary declaration cannot be obtained or read; its absence is not established.                                     |
| Contradictory or unusable evidence         | Required signatures, metadata, or source-to-declaration correspondence cannot support the selected meaning.                                        |
| Required type judgment unfinished          | A necessary inference, relation, or candidate applicability has not been determined; this is neither success nor a negative language judgment.     |
| Selected operation lacks qualified meaning | Source binding may have succeeded, but ADR-0084 has not qualified the computation for that use.                                                    |

A provisional rejected overload or failed lookup does not by itself fail the whole source judgment. Nor does a
failure of a supported resolver retroactively make the use unsupported; such a failure is a compiler correctness
problem (§3.5.1). A form may be language-valid yet prohibited by its owning 1D, which is a separate source-eligibility
judgment. No unresolved compiler judgment may form a completed HIR candidate or be reported as a refusal by an
unentered 1D Establishment judgment (ADR-0063 §4.7.1; ADR-0071 §6). General unsuccessful-result and diagnostic
representation remains under its owning Compiler Result and Diagnostic laws.

#### 3.5.3. Lawful Response to Unusable Evidence

Required declaration evidence may be reacquired or interpreted by another qualified means **for the same selected
source basis**, provided that the necessary declarations and relationships are actually established. SDK source or
another reader's output does not automatically correspond to the selected compiled binary, release-specific API
view, or source compilation scope. Host-compiler evidence likewise must match the actual basis in question.

Kontrakt cannot repair missing type information by guessing from JVM erasure. It cannot make an overload selectable
by ignoring a competing candidate or choose another SDK/API profile to obtain a more convenient declaration.
Executing arbitrary SDK or authored code to manufacture a source interpretation is also prohibited (ADR-0084
§§3.4–3.5).

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