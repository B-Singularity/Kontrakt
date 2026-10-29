# Character and Text SDK Interpretation and Shared Calculation Design

## Status

**Working Design — source interpretation and calculation-mapping design; not an Accepted Contract decision.**

## Date

2026-10-29

## Scope and Ownership

This implementation-facing Design follows *What Contract Is*, applicable Accepted ADRs, and the resolved sections of
Draft ADR-0080. It cannot approve an unresolved operation. The Canonicalization Built-In Law Catalog owns each selected
law's exact equivalence, representative, coverage and version; this Design owns neither those judgments nor a new
Character/Text authority.

This document owns the inventory and precise interpretation of Java/Kotlin Character/Text source callables, their
mapping to existing or proposed common calculations, and the evidence boundaries of support claims. **Unicode Property
selection and V1/V2 compiler consumer coverage belong to
`kontrakt-unicode-property-and-calculation-coverage-design.md`.** This Design uses that selection; it does not duplicate
its 54/38/34 catalog.

The investigated baseline is **Java SE 27 / OpenJDK 27** and **Kotlin/JVM standard library 2.4.21**. These are audit
profiles, not a claim that all their callables are supported in Kontrakt. Java `Character` (Unicode 17-based) and
Kontrakt's common Unicode 18.0.0 calculations have distinct semantic profiles.

---

# 1. Purpose and Scope

This Design governs how the compiler inventories and understands Java/Kotlin Character and Text source operations,
relates them to the shared Character/Text calculation laws being considered under ADR-0080, and proves that its
supported source surface is not accidentally incomplete.

Three separate inputs determine the work: (a) complete examination of the Java and Kotlin standard Character/Text API
surface, including source-language facilities; (b) actual calculation needs of Input, Admission, Canonicalization, the
existing Catalog, and other 1D laws; and (c) compiler-internal calculations used independently of authored Contract
expressions. Common operations are selected **after** all three are inspected. The source API alone is neither the
definition of the common law nor permission to use an API in every 1D.

This document additionally records a **selective and extensible** V1 third-party library investigation, notably Commons
Lang, Commons Text, Guava, and ICU4J. They do not inherit the full-coverage obligation applicable to the supported
Java/Kotlin standard Character/Text surface.

This is a Design document, not a user-facing `kontrakt.text` library, a new 1D Contract, a new IR family, a special
runtime verifier, or an approval of proxy-style JVM execution. It selects no mandatory String storage layout, compiler
pass, scheduling policy, vectorization strategy, or cache format. It does not infer that a recognizable callable is
ratified under ADR-0073.

## 1.1. Preservation of existing Contract meaning

- **Character** is a Unicode Scalar Value; **Text** is a finite ordered sequence of Scalars, including the empty
  sequence. Exact Text Equality is exact ordered Scalar equality, without normalization, folding, or collation.
  Unassigned Scalars remain possible Text constituents; a property table does not define the Text value domain.
- A Java/Kotlin `char`/`Char` is a UTF-16 Code Unit. Host `String` intermediate results may contain unpaired surrogates.
  Interpretation must preserve them without establishing an invalid value as Kontrakt Text or silently substituting
  U+FFFD.
- An Input-specific size or presentation bound does not create a global Text length bound. Resource exhaustion, capacity
  and target encodings retain separate owners.
- Each 1D decides which calculations its authored law permits and owns the resulting Judgment/Material; Canonicalization
  Laws separately own their `E_L`, `C_L`, Coverage and exact Version. The shared operation can be used without
  performing that Judgment.
- Exact semantic references, Symbol Identity, source spelling/paths, HID/dense ordinals, cache keys, diagnostic
  renderings and target artifact bytes are different things.

## 1.2. Completion criteria of this Design

This Design is ready to guide implementation when it records a traceable source surface, a proposed semantic mapping for
each relevant callable, a precise reason for any ineligibility, and a test method. **It is not permitted to label a
source profile fully supported** until the evidence gates in §10 have been satisfied. The investigation record
establishes candidates and risks, not achieved code coverage.

# 2. Support Profiles, Scope and Evidence

## 2.1. Profiles

A Source interpretation is parameterized at least by its actual source language, language version, applicable
SDK/standard-library version, compilation configuration, resolved symbol and dependency context. Java platform behavior
can additionally depend on class-library Unicode data, explicit/default Locale, Charset provider, regex options and
observable mutable state. These determinants must not be replaced silently by a compiler-global Unicode 18 setting.

| Profile dimension                        | Audit baseline                                    | Support decision / caveat                                                                                                                               |
|------------------------------------------|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java standard language and class library | Java SE 27; uploaded OpenJDK 27 `src.zip`         | Inventory basis. JDK 27 binary/JMOD, `javac 27` symbol and executable golden tests not supplied/proven                                                  |
| Kotlin source and JVM stdlib             | Kotlin 2.4.21 `kotlin-stdlib` sources and JAR     | Inventory basis. Kotlin Compiler 2.4.21 actual resolution/ABI tests not yet proven                                                                      |
| Kontrakt common Unicode calculations     | Unicode 18.0.0                                    | Used **only** by selected version-dependent common operations and updated Canonicalization `Initial` laws; does not change Java/Kotlin source semantics |
| Java `Character` property behavior       | JDK 27 documents Unicode 17.0 data                | Host interpretation profile; distinguish from Unicode 18 property queries                                                                               |
| External libraries                       | Candidate package families, no user-supplied JARs | Selected callable/version support to be approved later, not full-library support                                                                        |
| JVM target Classfile                     | JVM-specific format and Modified UTF-8            | Backend/Artifact ownership; never equivalent to ordinary UTF-8                                                                                          |

A profile update must be represented by an SDK/source-resolution and conformance delta; it cannot retcon an approved
Exact Law Version. New SDK methods should be discovered by the inventory comparison, not by waiting for a user
compilation failure.

## 2.2. Audit evidence and its limits

The existing SDK research inventories recorded **14,914** JDK `.java` paths, **354** selected Java source files,
**6,103** extracted Java declarations, **384** Kotlin `.kt` paths, **4,493** selected Kotlin declarations, **9,284**
broad Kotlin static declaration candidates and **860** earlier Kotlin JVM method entries. These count different
populations; no count establishes the number of eligible Character/Text callables or the completeness of Source
interpretation.

The Java AST extraction used JDK 21 parser tools for some JDK 27 sources, without final JDK 27 JMOD/`javac`
reconciliation. Kotlin extraction has `expect`/`actual`, inline, generated, inherited and mapped declarations; it was
not verified against Kotlin 2.4.21 compiler Source binding and binary ABI. The underlying inventories are retained as
audit inputs, not duplicated here as document appendices.

An excluded or unextracted source row is not proof that no relevant callable exists. The implementation must reconcile
the audited scope against visible source and binary declarations and actual compiler resolution before asserting
completeness.

# 3. Frontend Callable Interpretation Design

## 3.1. Unit of tracking

Each relevant Java/Kotlin callable shall have a traceable **design record**. The record's key must identify source-level
meaning rather than just a method name or an emitted JVM member. The following fields are the inventory/projection
*design schema*; they are not new Contract Identity or HIR physical handles.

| Group            | Required record fields                                                                                                                                                                                                                                                         |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Provenance       | archive digest, source path/line or ABI owner, Java/Kotlin language/library profile, signature evidence, review state                                                                                                                                                          |
| Source selection | declaration owner, callable kind (member/static/extension/operator/constructor/property), receiver, parameter and result types, generics, overload, visibility, inherited/bridge handling, defaults, vararg, named arguments, nullable/Java platform types, opt-in/deprecation |
| Value behavior   | UTF-16/Scalar/Byte/Grapheme indexing, null rules, empty inputs, ordering, equality, conversion, Unicode data/version, locale, result collection kind                                                                                                                           |
| Evaluation       | evaluation order, mutations/aliasing, exceptions, callbacks/dynamic `toString`, laziness/eagerness, stateful regex matcher, environmental inputs, permitted nondeterminism                                                                                                     |
| Semantic mapping | common calculation `Cxx`/`Uxx` where exact; otherwise Host-specific `H`, effect/state `T`, domain `D`, or compiler/target `B`; mapping preconditions and preservation obligations                                                                                              |
| Admission/use    | applicable 1D law and whether use is permitted, prohibited, or unresolved; reason linked to existing law rather than a new Text policy                                                                                                                                         |
| Validation       | javac/Kotlin resolved-symbol fixture, reference execution, expected result/failure, lowering and optimizer differential tests, Unicode/property version evidence, release gate status                                                                                          |

A single callable may have several roles: e.g. it can be **recognized** and **source-resolved**, then be ineligible for
Admission because it invokes an unclosed callback. Its recognition must not be lost merely because it is not admissible.

## 3.2. Lifecycle states must not be collapsed

| State                                     | Required claim                                                                                    | Not implied                                       |
|-------------------------------------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------|
| `Inventoried`                             | The declared/visible surface is recorded                                                          | Valid resolution or interpretation                |
| `Recognized`                              | Frontend knows the precise callable form                                                          | That it is selected at a given source site        |
| `Resolved`                                | Actual Java/Kotlin source binds to this declaration with correct overload and argument conversion | 1D eligibility                                    |
| `Interpreted`                             | Operand/result semantics, relevant effects and determinants are known                             | Pure, deterministic, or admissible                |
| `Mapped`                                  | A C/U law is shown equivalent *under stated conditions*, or H/T/D/B is retained                   | Ratified JVM execution                            |
| `Admissible` / `Ineligible` / `Undecided` | The owning 1D's existing law determines use                                                       | That the other statuses change                    |
| `Implemented`                             | The compiler can realize the selected and eligible case                                           | That it matches the reference SDK                 |
| `Conformant`                              | Source differential and transformation tests passed for the precise profile                       | That a future SDK release has identical semantics |

These are **coverage evidence labels**, not an additional Contract admission/establishment pipeline. An unsupported yet
otherwise valid source API shall not be mislabeled an invalid user declaration. A genuine 1D prohibition must produce a
diagnostic naming that prohibition, not `unknown method`.

## 3.3. Source-dependent resolution requirements

Java: method/constructor/field selection, overload and primitive widening/boxing, varargs, generic substitution,
inherited overrides/default methods, `CharSequence` dynamic receiver, null literal selection, `+` concatenation,
`String.valueOf`, lambda/method references, class/library version, checked/unchecked exceptions.

Kotlin: Kotlin/Java mapped types, member-before-extension behavior, imports, overload candidates, receiver and nullable
receivers, default/named arguments, inline and reified functions, generic extension generation, `CharArray`/`ByteArray`
expansions, delegated JVM calls, property syntax, operator calls, string templates and function-type/lambda bodies. A
Kotlin source function need not correspond to a uniquely named public JVM method.

**No source shortcut:** extracting a Java/Kotlin method name from a binary instruction or matching `String` in a type
name is insufficient. The exact source expression and binding determine the interpretation. Parsing JVM bytecode later
is a distinct acquisition/evidence problem and does not retroactively grant source-call authority.

## 3.4. From source result to common Character/Text

A source-level Host intermediate result may remain a UTF-16 sequence containing lone surrogates. Common Scalar
Character/Text establishment is permitted only if the relevant conversion is lawful. The decoder shall not silently
drop, replace, normalize, fold or trim source information. For `CharSequence` backed by mutable/external state, reading
a snapshot is a distinct evaluation from assuming an immutable Text.

The common operation `ScalarCount` is not an automatic replacement for Java/Kotlin `String.length`; Java
`String.split(regex)` is not Kotlin literal `split`; JDK `equalsFoldCase` is not Kontrakt Unicode 18 folding unless
equivalence is actually established for that operation's domain and version.

## 3.5. Semantic mapping and value-flow rule

A resolved source expression is evaluated under its *source semantics* and admitted by its owning 1D only if those
semantics and effects are legally closed. When an identical shared calculation exists, its law may be used for the
corresponding part. Unsupported conversions stay explicitly unresolved. Mapping must preserve failure, result shape,
operand evaluation effects, locale/profile, source positions and ordering where observable.

The implementation may use a lookup/catalog, a query-driven analysis, or another replaceable realization; this document
does not require a new IR, an API interception layer, a runtime Contract engine, or an AST-based Contract Authority. No
direct call to user-authored `toString` or arbitrary `Predicate` may substitute for Contract interpretation.

# 4. Shared Calculation Mapping Catalog

C01–C27 and U01–U17 are **review handles**, not external API names or HIR identities. Some common calculations are
already selected in ADR-0080 §§3.3–3.4; the others remain candidates. These tables track Source mapping needs without
creating another operation law. Unicode Property inventory and compiler consumer coverage remain in the companion
Coverage Design.

## 4.1. Scalar and sequence value calculations

| ID  | Candidate                               | Required closure / typical SDK demand                                                                     | Boundaries                                               |
|-----|-----------------------------------------|-----------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| C01 | Scalar validity and code point → Scalar | Distinguish range-valid code point from surrogate/non-scalar; `Character.isValidCodePoint` counterexample | Do not narrow valid Text by `isDefined`                  |
| C02 | Exact Scalar Count                      | Count every Scalar, including unassigned/combining; result finite                                         | Not UTF-16/byte/grapheme count                           |
| C03 | Scalar element access                   | Position and valid index domain; unsuccessful out-of-range                                                | Source `charAt` is code-unit                             |
| C04 | Scalar slice                            | Start/end, end-exclusive, empty boundaries, failures                                                      | Kotlin `IntRange` and Host `substring` differ            |
| C05 | Text–Text concatenate                   | Exact ordered value, identity for empty operands, no implicit normalization                               | `String + Object` can invoke user code                   |
| C06 | Repeat                                  | Count zero/negative, finite construction, length consequences                                             | Resource limit is not Input rejection                    |
| C07 | Exact equality                          | Reuse already established ordered Scalar equality                                                         | No collation or case folding                             |
| C08 | Scalar lexical ordering                 | Code-point order and prefix relation if selected                                                          | Java UTF-16 `compareTo` may differ                       |
| C09 | Literal search                          | Empty needle, first/last, from-position, overlap, success/failure, units                                  | Distinct from Regex Matcher                              |
| C10 | Prefix/suffix/containment               | Derive from equality/search where exact                                                                   | May provide convenience projection without new primitive |
| C11 | Region equality                         | Two source ranges, units, length, failure                                                                 | JDK `regionMatches(ignoreCase)` Host rules               |
| C12 | Literal replace                         | Left-to-right, overlap, empty target, replacement, exact result                                           | Regex replacement grammar separate                       |
| C13 | Literal delimiter split                 | Delimiter precedence, empty tokens, limit, result collection, failure                                     | Java Regex `split` != Kotlin literal `split`             |
| C14 | Text-only join                          | Separator, sequence cardinality, empty/nil distinction                                                    | `toString`, `null` and callback not pure join            |
| C15 | Finite exact-set trim                   | Declared membership set, leading/trailing/both, empty and all-trimmed                                     | JDK trim/strip, Kotlin and Catalog W5/W6/W18 differ      |
| C16 | Whitespace runs collapse                | Exact set, replacement, empty/edge behavior                                                               | Catalog-specific folding retains authority               |
| C17 | Pad/truncate/fixed-size construction    | Position units, pad value, truncation, range, shape                                                       | Display width not Scalar Count                           |
| C18 | Line-separator recognition/split        | CR/LF/CRLF, final empty line, Unicode separators when applicable                                          | Not UAX #14 line-break opportunities                     |

## 4.2. Encodings and narrowly bounded textual codecs

| ID  | Candidate                             | Closure to investigate                                                   | Boundary                                    |
|-----|---------------------------------------|--------------------------------------------------------------------------|---------------------------------------------|
| C19 | UTF-16 code-unit length/index/slice   | Host-accurate code-unit sequences, including ill-formed intermediates    | Never redefine Scalar Text                  |
| C20 | Strict UTF-16 ↔ Scalar conversion     | Surrogate validity and unsuccessful input position                       | Replacement decoding separate               |
| C21 | Strict UTF-8 encode/decode            | Overlong forms, invalid bytes, truncation, surrogate encoding, >U+10FFFF | BOM/framing/offset separately explicit      |
| C22 | Strict UTF-16 encode/decode           | Byte order, BOM, malformed pair handling                                 | Host Java String may be ill-formed          |
| C23 | Strict UTF-32 encode/decode           | Endianness, illegal scalar, BOM if supported                             | Scope not automatically V1                  |
| C24 | Encoded lengths and offsets           | Exact length/offset units, mapping across code units/bytes/scalars       | Artifact framing and Buffer layout distinct |
| C25 | Explicit-variant Hex encode/decode    | Hex digits, case, prefixes, odd nibble and parsing failures              | UUID grammar still domain-owned             |
| C26 | Explicit-variant Base64 encode/decode | Basic/URL/MIME, padding, invalid characters, chunk framing               | Byte codec, not Unicode normalization       |
| C27 | Explicit-grammar escape/unescape      | Java literal/Text Block, JSON, XML, HTML, URL as *different* grammars    | Do not create a universal escape function   |

## 4.3. Unicode 18.0.0 calculation mappings

| ID  | Candidate                              | Required semantic determinants                                                       | Boundary                                                    |
|-----|----------------------------------------|--------------------------------------------------------------------------------------|-------------------------------------------------------------|
| U01 | General_Category and derived Property  | Exact UCD 18 tables and property type/result                                         | `isDefined` is version-specific Host behavior               |
| U02 | Script and Script_Extensions           | Property and mapping policy                                                          | Identifier policy remains Frontend                          |
| U03 | White_Space / Pattern_White_Space      | Exact chosen property and immutable version                                          | JDK `strip` is not equivalent                               |
| U04 | Nd and Decimal_Digit_Value             | Final release UCD, digit ranges/values                                               | General NumericValue differs                                |
| U05 | Simple Case Mapping — selected         | Lower/Upper/Title: Scalar → Scalar, Unicode 18                                       | Host `char`/`int` overloads differ                          |
| U06 | Full Case Mapping — selected           | Default Lower/Upper/Text Titlecase; explicit Unicode `SpecialCasing` locale profiles | Host Locale, version and evaluation remain distinct         |
| U07 | Folding — selected                     | Default/Turkic × Simple/Full; `F18` is default full                                  | Not JDK `equalsIgnoreCase` or an ambient-Locale comparison  |
| U08 | NFC/NFD/NFKC/NFKD — selected           | Exact Unicode 18 Text transformations                                                | No implicit Text equality or Canonicalization judgment      |
| U09 | Derived `isNormalized` — selected      | `N(x) = x` under Exact Equality                                                      | Quick Check is internal data only; no independent result    |
| U10 | `Q18`/`H18` — selected                 | Exact existing NFC/NFKC Case Fold representatives                                    | R5 is internal; D146 not selected; Catalog retains judgment |
| U11 | ID/XID Start/Continue                  | Exact UCD properties                                                                 | Language identifier admissibility separate                  |
| U12 | Emoji/Variation/Default_Ignorable etc. | Precisely selected properties and value type                                         | Whole UCD not automatically V1 source API                   |
| U13 | Extended Grapheme boundaries           | UAX #29 version, returned boundaries/units                                           | Grapheme != Scalar; display width separate                  |
| U14 | Word/Sentence boundaries               | UAX #29 rules, version/profile                                                       | Deferred until real consumer scope confirmed                |
| U15 | Line break opportunities               | UAX #14, line-break classes, rule/version                                            | Distinct from CRLF line parsing                             |
| U16 | Confusable Skeleton material           | UTS #39 version and exact mappings                                                   | Security Judgment not a Text result                         |
| U17 | BiDi property/algorithm material       | UAX #9 profile, required observations                                                | Rendering/visual order and security policy separate         |

### 4.4. Selection rule

ADR-0080 §§3.3–3.4 determine the selected operations. Unselected entries here remain candidates until their owner
determines exact operands and results. A Java/Kotlin API name cannot select a common operation by itself. Source meaning
remains separate when it differs in Unicode version, Locale, units, or effects. Property selection and the
dependent-data inventory remain in the companion Coverage Design.

# 5. Fourteen Investigation and Implementation Bundles

Bundles are **work and coverage partitions**; they are not 14 independent new Contract authorities or compiler passes.
Each bundle must have callable provenance, semantic distinctions, shared candidate mapping, consumer mapping and
differential tests. Concrete SDK families and non-equivalence examples are summarized below.

| Bundle                        | Concrete source/API scope                                                                                                           | Shared candidates                   | Requirements retained outside common Text law              | Review exit evidence                              |
|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|------------------------------------------------------------|---------------------------------------------------|
| **0 — Callable completeness** | Java/Kotlin declarations, member/extension/default/inline, Java mapped APIs, source `+` and string templates, `StringConcatFactory` | All as indexed                      | Language resolution; ADR-0073 eligibility                  | Coverage inventory + javac/Kotlin symbol fixtures |
| **1 — Value domain**          | `Character`, `Char`, `CharSequence`, surrogate/codepoint, nullable                                                                  | C01/C20                             | Host ill-formed UTF-16 and aliasing                        | Scalar vs code-unit counterexample tests          |
| **2 — Sequence operations**   | Length, charAt/get, substring/slice, concat/repeat, range                                                                           | C02–C06/C19                         | Index units, throws, user object effects                   | Boundary/golden range matrix                      |
| **3 — Compare/search**        | `equals`, `compareTo`, `indexOf`, `regionMatches`, ignoreCase, fold case                                                            | C07–C11/U07                         | JDK version, UTF-16 ordering, locale/Host ignoreCase       | Empty/overlap/non-BMP/case tests                  |
| **4 — Transform**             | replace, literal/regex split, join, trim, pad, indent/lines/translateEscapes                                                        | C12–C18                             | Regex, callback, delimiter precedence, whitespace variants | Java–Kotlin differential corpus                   |
| **5 — Properties**            | `Character`, `Char`, Script, blocks, digits, emojis, identifier props                                                               | U01–U04/U11–U12                     | JDK 17 UCD, Frontend identifier permission                 | Unicode 17/18 property boundary tests             |
| **6 — Case behavior**         | char/string mappings, ignoreCase, foldCase, Locale                                                                                  | U05–U07/U10                         | Default Locale and context; source-specific behavior       | One-to-many and Turkish/Greek tests               |
| **7 — Normalization/Catalog** | Normalizer, canonical matching, 11 existing Canonicalization laws                                                                   | C15/U03–U04/U07–U10                 | Each Law's `E_L`/`C_L`/Version                             | UCD 18 conformance + Catalog invariants           |
| **8 — Regex/segment**         | Pattern, Matcher, Kotlin Regex, MatchResult, splits/replacement, BreakIterator                                                      | C09/U13–U15                         | Regex grammar, matcher state, work bound, captures         | Compiler/SDK regex fixtures, work proof           |
| **9 — Encoding**              | Charset, String `getBytes`/`encodedLength`, Decoder/Encoder, Kotlin ByteArray                                                       | C20–C24                             | Provider/malformed policies, I/O/target                    | Strict vs replacement + boundary corpus           |
| **10 — Formatting/codecs**    | HexFormat, Base64, String.format, Formatter, Scanner, Message/List Format, domain parsing                                           | C25–C27                             | Numeric/Temporal/URI/XML, `toString`, locale               | Grammar/formatter side-effect tests               |
| **11 — Mutable/functional**   | StringBuilder/Buffer, CharBuffer, Kotlin map/filter/fold, streams/sequences                                                         | Reuse selected C/U                  | State, alias, callbacks, evaluation, collection law        | Effect/evaluation-order tests                     |
| **12 — Compiler consumers**   | Lexer, literal, source offset, diagnostics, identity/cache, Classfile, I/O                                                          | C02–C05/C09/C18/C20–C24/U01–U03/U13 | Symbol, SourceManager, Artifact, Target                    | Round trip, source positions, target constraints  |
| **13 — Third party**          | Commons Lang/Text, Guava, ICU4J selected APIs                                                                                       | Existing C/U reuse                  | Version pin, native eligibility, effects, dependency graph | Real JAR symbol + reference fixtures              |

## 5.1. Bundle 0 — no ‘unknown SDK API’ as a substitute for analysis

A supported Java/Kotlin standard Character/Text API should be recognized and resolved even if a particular authored use
is disallowed. The inventory must traverse inherited declarations, `CharSequence`, Kotlin generated/inline extensions,
null receivers, `CharArray` and `ByteArray` codecs, Java operator concatenation, Kotlin template conversion and other
non-method-call syntax. Every source expression is associated with the actually selected overload; a method name or
bytecode signature alone is not enough.

## 5.2. Bundles 1–4 — value, sequence and transform accuracy

Maintain the source's index and result domains. Java/Kotlin `String.length` counts UTF-16 units; `"😀"` is two units and
one Scalar. Java `substring(0,1)` on that value produces a Host String with a lone surrogate, not common Text. Java
`"a,".split(",")` and Kotlin `"a,".split(',')` produce different list shapes. Java `trim`, Java `strip`, Kotlin `trim`
and Unicode `White_Space` derive from distinct predicates. Exact sequences and conditional variations must not be
collapsed under one convenient operation name.

Search must distinguish empty needles, overlapping matches, first/last, from positions and out-of-bounds. Split must
explicitly model delimiter order, zero-width matches and trailing empties. For `String.join` and Kotlin
`joinToString(transform)`, object conversion/callbacks must be accounted for rather than erased by pure Text Join.

## 5.3. Bundles 5–7 — versioned Unicode without Host substitution

Unicode 18 common property/normalization/fold computations are separate from Java 27 `Character` Unicode 17 and from
mutable/default Locale observations. `Character.getNumericValue` is not the same as `Nd` Decimal Value. A normalization
stability guarantee for previously assigned scalars is not a general proof of equality for all Scalars or for
segmentation, newer Properties and Case Mapping.

The 11 Unicode `Initial` Canonicalization Laws remain distinct (Catalog §§8.5–8.6). ADR-0080 §3.4.5 maps the seven
Case/Normalization representatives to the exact shared calculations. Each Law retains its own `E_L`, `C_L`, Version, and
Coverage. Java/Kotlin APIs map to these calculations only when their Source profiles agree.

## 5.4. Bundle 8 — regex meaning and finite work

Java `Pattern`/`Matcher` has flags, named groups, zero-width matching, capture, matching state, regions,
transparent/anchoring bounds, replacements and laziness. Kotlin `Regex` on JVM typically uses Java regex machinery but
exposes different result/limit/collection semantics. UAX #29 grapheme segmentation, Java regex `\\X`, Java UTF-16
positions, line breaking and display clipping are not the same result law.

Recognizing arbitrary regex source is mandatory for interpretation coverage; **admitting** a regex in a 1D that requires
provably bounded calculation is conditional on the existing Admission §7.7 work law. No unsupported regex may be
executed as a delegated JVM policy engine.

## 5.5. Bundles 9–10 — encodings, codecs, formatting and domains

Strict UTF conversion and replacement/ignore/repair are distinct. Charset provider and error action are observable Host
determinants. Java 27 `String.encodedLength(Charset)` includes its encoder's behavior and is not strict UTF-8 byte
count. UTF-8/UTF-16/UTF-32, BOM and byte order, buffer offsets and JVM Modified UTF-8 must be identified separately. Hex
and Base64 have variant-specific grammars; a generic ‘decode’ cannot hide MIME, URL-safe or padding behavior. Source
escapes, JSON, XML, HTML and URI percent encoding are different grammars.

`Formatter`, `Scanner`, `MessageFormat`, `ListFormat` and numeric/temporal/URI conversions can call user methods or
consume locale/environment-dependent material. The recognition of an SDK API is not approval to execute these effects
within a pure Text calculation.

## 5.6. Bundle 11 — temporary state is not Text mutation

Builder operations (`append`, `setLength`, `reverse`, range edits), aliasing and escape, Kotlin `buildString`, generated
`map`/`filter`/`fold`, eager lists and lazy sequences/streams must preserve evaluation order, captures and effects. An
Admission-local finite binder does not make an Input Text mutable. A collection operation cannot be represented by
arbitrary higher-order Contract structure merely because the source API takes a lambda.

## 5.7. Bundle 12 — internal compiler consumer coverage

The compiler may use shared calculations independently of authored source: decoding source files, escape and literal
validation, identifier spelling inspection, exact constant folding, offset mapping, line/column diagnostics,
grapheme-safe clipping, deterministic serialization and optimizer checks. Host implementation selection cannot redefine
these calculations. Symbol/Path/HID/Cache/Artifact equality remains separately governed, as does JVM Classfile Modified
UTF-8 and its target constraints.

## 5.8. Bundle 13 — selective external support

The initial review families are Commons Lang `StringUtils`/`Strings.CS`/`Strings.CI`/`RegExUtils`; Commons Text
`StringEscapeUtils` and strictly closed replacements; Guava `CharMatcher`/`Splitter`/`Joiner`/`Strings`/`Ascii`/
`CaseFormat`; ICU4J `UCharacter`/`Normalizer2`/`UnicodeSet`/`BreakIterator` selectively. Do not automatically admit
`StringSubstitutor` arbitrary lookups, ICU Collator/Transliterator, arbitrary Java Pattern or any dependency-transitive
API. No external JAR was supplied, so callable-level approval remains pending.

## 5.9. Less-obvious SDK and source-language surfaces

The primary inventory must not silently lose text-producing or text-sensitive operations that appear outside the obvious
`String` and `kotlin.text` declarations. Source resolution should explicitly consider the following families, then
classify the result using the existing C/U/H/T/D/B meanings rather than expanding common Text law by method name.

| Surface                                                                  | Exact distinction to preserve                                                                                             |
|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| Java `String.valueOf(Object)`, source `+` and Kotlin string templates    | User `toString`, null conversion, evaluation order, and effect; compiler `StringConcatFactory` is only a target mechanism |
| Java `String.intern`                                                     | String-pool identity is not Exact Text Equality, Symbol Identity, or compiler-product identity                            |
| Java `Matcher.results` and `Pattern.splitAsStream`                       | Lazy match progression, state, zero-width behavior, and evaluation timing                                                 |
| Java regex `\X` and `\b{g}`                                              | Grapheme behavior within host regex rules, not a generic UAX #29 operation                                                |
| Java `String.lines`, `indent`, `stripIndent`, `translateEscapes`         | Separate line-separator, empty-line, whitespace and Java-escape semantics                                                 |
| Java `ListFormat`, `Formatter`, `Scanner`, `Formattable`                 | Locale, user conversions, external state and domain-specific formatting                                                   |
| Java `String.encodedLength(Charset)`                                     | Actual charset encoder and malformed-input policy, not strict UTF-8 length                                                |
| Kotlin nullable-receiver extensions                                      | `String?.isNullOrBlank` and null-specific result semantics                                                                |
| Kotlin generated `_Strings`, `_Arrays`, `_Sequences` extensions          | Source-visible inline/iterator behavior may not appear as a distinct public JVM member                                    |
| Kotlin `Regex.replace(transform)`                                        | Callback invocation count, order, captures and effects                                                                    |
| Kotlin `buildString`/`appendLine` and `CharArray`/`ByteArray` expansions | Temporary mutable state, conversion boundaries and aliasing                                                               |
| Kotlin `HexFormat`, `Base64`, `Path.readText`/`writeText`                | Codec variants and I/O/path ownership outside common Text judgment                                                        |
| UUID, numeric, temporal and URI parsing                                  | Reuse shared scanning where exact, but preserve the responsible domain grammar and failure law                            |

The compiler must treat a recognizable but ineligible callable as **known and ineligible**, not unknown; a callable
omitted by an advertised supported source profile is a coverage defect, not evidence of a user-authored Contract
violation.

# 6. Implementation Data Projection and Traceability

## 6.1. Catalog records as an implementation seam

Use a source-versioned audit table and generated (or equivalent) semantic mapping evidence rather than hand-maintained
method-name switches. An implementation may divide records into files, queries or another storage layout. The following
*conceptual* fields make the requirement testable:

```text
SourceCallableRecord
  sourceProfile      = (language, languageVersion, stdlibOrJdkVersion, target)
  binding            = (qualifiedOwner, sourceDeclaration, callableKind,
                        receiver, parameterTypes, returnType, typeSubstitutions,
                        overload, defaults, visibility, imports)
  semanticProfile    = (valueUnits, nullAndEmpty, exceptions, orderedEffects,
                        unicodeData, locale, charset, regexFlags, mutableState)
  interpretation     = Recognized | Resolved | Interpreted | ...
  mapping            = CommonCalculation(id, preconditions)
                     | HostExact(profile) | StatefulEffects
                     | DomainOwned(owner) | TargetOwned(owner) | Unresolved(reason)
  eligibilityEvidence = (owning1D, applicableLaw, judgmentOrUnresolvedReason)
  tests               = (bindingFixture, referenceFixtures, differentialFixtures,
                         expectedFailures, evidenceStatus)
```

These values describe a **Design inventory**, not a new HIR authority, a version-binding substitute or an instruction
that every runtime execution be intercepted. Frontend/HIR can project only semantically needed information through its
already governed protocol.

## 6.2. Concrete mapping examples

| Source callable/expression            | Required interpretation                               | Possible shared calculation                      | Why it cannot be mapped blindly                                    |
|---------------------------------------|-------------------------------------------------------|--------------------------------------------------|--------------------------------------------------------------------|
| Java `s.length()` / Kotlin `s.length` | UTF-16 code-unit count                                | C19                                              | C02 ScalarCount gives a different result for supplementary scalars |
| Java `s.codePointCount(a,b)`          | Host UTF-16 region counting with ill-formed handling  | C19/C20 under strict preconditions               | May count isolated surrogate code units                            |
| Java/Kotlin `s.substring(...)`        | UTF-16 positions and language-specific overload/range | C04 only with validated correspondence           | May produce lone-surrogate result                                  |
| Java `String.split(regex)`            | Pattern flags, default limit, trailing-empty policy   | C13 only in constrained literal-equivalent cases | General Java Regex and split semantics                             |
| Kotlin `s.split(',')`                 | Literal delimiter, Kotlin trailing-empty rule         | C13 with exact policy                            | Delimiter order and limit must be retained                         |
| Java `s.strip()`                      | JDK `Character.isWhitespace`                          | C15 only with exact Host-set equivalence         | Not Unicode 18 `White_Space`                                       |
| Kotlin `s.lowercase()`                | Locale.ROOT, Kotlin/JDK behavior                      | U06 only on proven equivalent profile/domain     | Not Java default-locale mapping                                    |
| Java `s.equalsFoldCase(t)`            | JDK 27 Full Case Fold comparison                      | U07 only if equivalent for exact version/domain  | JDK 17 UCD vs U18 not silently erased                              |
| Java `String.encodedLength(cs)`       | Charset encoder and replacement rules                 | C24 under proven same encoder semantics          | Not strict UTF-8 byte count                                        |
| Kotlin `Regex.replace(transform)`     | Lambda calls and matching results                     | C12 only for a proven fixed literal fragment     | Evaluation/effects and state are essential                         |
| `builder.append(obj)`                 | Mutable builder + object-to-string conversion         | C05 only after lawful snapshot/pure conversion   | User method and alias may be observable                            |
| Java/Kotlin `+` with Object           | Source evaluation and conversion                      | C05 only for known Text operands                 | Compiler lowering recipe not source semantics                      |

## 6.3. No completeness by inference

Do not auto-mark a family supported because one overload passes tests or its name exists in the extraction. Each
exposed, supported overload must be tested with applicable receiver types, generic inference and default arguments; a
family-level exception must be recorded explicitly. Deprecated public API remains in the inventory until its actual
source profile excludes it; deprecation alone is not a reason to pretend it does not exist.

# 7. Third-Party API Qualification and Evolution

## 7.1. V1 selection mechanism

For each selected external library, pin **Maven coordinates + exact version** first, inventory the actual API and
inherited/overloaded forms, record runtime dependencies and Java/Kotlin binding, then determine a closed subset of
Callables and source use contexts. Commons/Guava/ICU4J candidates in §5.8 are priorities for investigation, **not
release commitments**. No user-uploaded external JAR was available to substantiate a full method inventory.

A new library version must have a declaration delta and semantic regression review. A newly supported callable may reuse
C/U calculations, while a new operation law needs separate ADR review. Do not change existing shared operation meanings
just to match a third-party convention.

## 7.2. Dynamic and domain-sensitive surfaces

`StringSubstitutor` with arbitrary lookups, Guava `Joiner` with custom object conversions, Collation/MessageFormat
locale behavior, UnicodeSets driven by arbitrary patterns, arbitrary regex engines and client callbacks have additional
state/grammar/effect boundaries. Recognition is not automatic execution eligibility. Where an exact, finite, pure subset
is provable, support can be added by an explicit callable qualification without inventing proxy-style runtime
enforcement.

# 8. Compiler Product, Optimizer and Target Preservation

## 8.1. Value preservation

Frontend source parsing may preserve literal spelling, escape provenance, source encoding and exact scalar value as
**different observations**. HIR resolved semantic references remain separate from source location or in-memory storage
identity. Establishment receives only the lawful determinants the owning law requires. Generated APIs remain downstream
projections rather than foundations of common Text meaning.

## 8.2. Transformation constraints

A constant fold or rewrite is allowed only if it preserves all legally observable source results and effects, including
exceptional behavior, locale/encoding determinant, match state and order. In particular:

- `NFC(a + b)` is not generally equal to `NFC(a) + NFC(b)`.
- Case-insensitive comparison is not generally full case folding, and JDK Unicode 17 results are not guaranteed to equal
  Unicode 18 results.
- A concatenation involving an Object may invoke user `toString`; it is not a pure Text concatenation without proof.
- Moving `Regex.replace(transform)` can reorder callbacks or change their invocation count.
- Replacing Java `String.length()` with Scalar Count is unsound; UTF-16 offsets must not become scalar offsets.
- UTF-8 Byte Count and JVM Constant-Pool Modified UTF-8 encoded length are separate observations.

# 9. Diagnostics and Failure Distinctions

The compiler must distinguish at least the following classes and identify the actual owner/source context rather than
emitting an undifferentiated ‘unsupported string function’ error.

| Diagnostic class                      | Meaning                                                                                           | Owner / treatment                                                             |
|---------------------------------------|---------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| Invalid Java/Kotlin source            | Resolution cannot find a legal callable according to the source language/profile                  | Frontend language analysis                                                    |
| Valid SDK source, missing coverage    | API belongs to the claimed supported profile but the compiler inventory/interpreter lacks it      | Compiler defect/unsupported implementation scope, **not** a 1D user violation |
| Known source, ineligible authored use | Exact semantics are recognized but owning 1D prohibits the effect, basis, state, callback or work | Diagnostic grounded in existing 1D law                                        |
| SDK version/profile mismatch          | Callable is absent or changed for the configured actual SDK profile                               | Profile mismatch with explicit version                                        |
| Common-value conversion unsuccessful  | A Host sequence is not legal Scalar Text, or strict encoding fails                                | Preserve precise conversion/failure conditions, ADR-0057 ownership            |
| Exact-calculation not selected        | A C/U candidate has not been approved or its law is not closed                                    | Design/support gap, not a made-up Contract restriction                        |
| Resource/target limitation            | Work budget, artifact size, Classfile encoding or compiler capacity                               | Responsible budget/target owner, not spurious Input rejection                 |
| Platform ratification absent          | Recognized native callable not legally qualified for execution                                    | ADR-0073 qualification outcome                                                |

An error message should present the selected source symbol/profile and failed condition when known, with source spans
and escaping that do not split surrogate pairs or hide control characters. Avoid claiming whether the application UI can
render a grapheme faithfully.

# 10. Conformance Strategy — Evidence Rather Than Counts

## 10.1. Independent verification gates

| Gate                       | Required input                                             | Pass condition                                                                          | Blocking failure                                  |
|----------------------------|------------------------------------------------------------|-----------------------------------------------------------------------------------------|---------------------------------------------------|
| G0 — Inventory closure     | Exact SDK/JAR/source profile and ABI/JMOD                  | Every relevant callable reconciled to resolved symbol or an explicit reviewed exclusion | Missing inherited/inline/generated/overload forms |
| G1 — Language resolution   | javac 27 and Kotlin 2.4.21 compiler fixtures               | Same declaration/argument evaluation/overload chosen as reference compiler              | Name-based or JVM-signature-only resolution       |
| G2 — Host meaning          | Executable golden fixtures on matching runtime             | Results, exceptions, effects, null, locale/charset/Unicode/regex-state as documented    | Unexamined behavior declared pure                 |
| G3 — Shared mapping        | C/U law proof or H/T/D/B mapping                           | Equivalence constraints and non-equivalence clearly recorded                            | Host 17 property treated as U18 by default        |
| G4 — 1D legality           | ADR-0064/65/66/73 and other owning laws                    | Permit/deny reason equals actual owner law                                              | Recognition conflated with admissibility          |
| G5 — Lowering/optimization | Differential compiler, generated code and native reference | Results and legally visible effects preserved                                           | Constant-fold/optimizer changes source meaning    |
| G6 — Release regression    | Static inventory deltas + golden vectors + diagnostics     | Supported profile has no unaccounted eligible API path                                  | Silent missing SDK API discovered by end users    |

Failure of one gate must not be disguised by successful tests in another. A `javac` compile proving source validity does
not prove Kontrakt's lawful 1D use, and an ICU property test does not prove JDK `Character` behavior.

## 10.2. Boundary fixtures that are mandatory to cover

| Family               | Golden and negative cases                                                                                                                                           |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scalar/UTF-16        | supplementary scalar (`"😀"`), isolated high/low surrogate, `isValidCodePoint(0xD800)`, char/int overloads                                                          |
| Index/range          | zero-length, end-exclusive, negative and overflow, Kotlin `IntRange`, invalid slice and zero repeat                                                                 |
| Search/split         | empty needle, overlapping hit, first/last/from index, Java default regex split vs Kotlin literal split, prefix collision in Kotlin delimiter list, zero-width regex |
| Whitespace/line      | U+2003, U+00A0, CR/LF/CRLF, trailing delimiter and empty final line, JDK trim vs strip vs Kotlin trim                                                               |
| Unicode/case         | `ß` against `ss`, Turkish `I`, Greek contextual sigma, Java 17 property versus Unicode 18 new assignments, `Nd` vs numeric values                                   |
| Normalization        | decomposed/composed forms, cross-boundary NFC(concat), UCD 18 official conformance, idempotence                                                                     |
| Regex                | `CANON_EQ`, Unicode flags, `\\X`, zero-width progress, Matcher.region/state, captures, Kotlin transform callback effects, bounded work                              |
| Mutation/functional  | Builder reverse surrogate handling, setLength NUL, captured state, lazy/eager collection, Stream/Sequence behavior                                                  |
| Encoding             | malformed UTF-8/UTF-16, strict vs replacement, BOM/endian, Java `encodedLength` with lone surrogate, Classfile Modified UTF-8                                       |
| Codecs/format        | Hex odd digit, Base64 variant padding/MIME/URL, Java/JSON/XML escapes, `String.format("%s", object)` `toString` effects                                             |
| Identity/diagnostics | `String.intern` vs Symbol/HID equality, escaped controls, grapheme-safe clipping, source byte/UTF-16/scalar coordinates                                             |

The cases below are review minima rather than an exhaustive per-overload fixture inventory. The fixtures need exact
expected outputs and reference compiler/runtime version; this document does not invent unexecuted expected results.

## 10.3. Standard SDK completeness claim

For the selected Java/Kotlin source profile, an API family is **complete** only after every exposed supported callable
and overload has a resolution fixture, semantic classification and eligibility disposition. The overall standard
Character/Text coverage is complete only after unresolved rows are zero **in the audited scope** and the audited scope
is itself reconciled with official source+binary+compiler symbol data. A selected V1 third-party family can instead
declare an intentionally partial and versioned callable allowlist.

## 10.4. Unclosed evidence must not be represented as support

JDK 27 binary/JMOD and `javac` binding reconciliation, Kotlin 2.4.21 compiler/ABI resolution, inherited and generated
API coverage, actual source-specific golden behavior, selected external JAR versions, and 1D eligibility remain open
until individually demonstrated. Unresolved common C/U operations likewise do not become supported because a catalog row
exists. These are later conformance and release tasks, not reasons to rewrite the semantic law now.

# 11. Implementation Plan and Dependencies

| Work package                             | Owner's design concern                  | Input → output                                                                      | Blocker / done criterion                                                            |
|------------------------------------------|-----------------------------------------|-------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| P0: Scope reconciliation                 | SDK inventory/ABI audit                 | Source paths + JDK 27 JMOD/javac + Kotlin compiler/stdlib → callable coverage table | Missing JDK binary and official compiler symbol evidence                            |
| P1: Semantic classification              | Frontend Source Interpretation          | Each resolved callable → H/T/D/B or equivalent C/U with preconditions               | No unclassified supported standard Text API                                         |
| P2: Remaining common-operation selection | ADR-0080 decision process               | Unselected C/U candidates → closed result laws                                      | Case/Normalization already selected in §3.4; remaining candidates require decisions |
| P3: 1D consumer mapping                  | Input/Admission/Canonicalization/others | Applicable authoring expression + calculation → existing 1D judgment                | Existing Authority untouched; no opaque JVM execution                               |
| P4: Frontend/IR use                      | Existing HIR protocol and compiler flow | Source semantics → needed resolved representations and provenance                   | No new IR stage or HIR identity assumption                                          |
| P5: Reference implementation/tests       | Compiler realization and QA             | Fixed SDK/Unicode fixtures → differential results                                   | Error/effect/locale/surrogate/regex tests pass                                      |
| P6: External adapter expansion           | Versioned third-party Callable Catalog  | Exact JAR + source/ABI → selected supported overloads                               | Per-library allowlist and tests; no transitive blanket promise                      |
| P7: Release and upgrade                  | Quality gate                            | New SDK/Unicode/library version → inventory delta + test rerun                      | No silent semantic profile upgrade                                                  |

**V1:** Target the supported Java/Kotlin standard Character/Text profile in full recognition/interpretation, subject to
demonstrated source resolution and existing 1D eligibility; select external library calls deliberately. This is a
**design target**, not a claim that existing V1 code already meets it.

**V2 and later:** New Java/Kotlin SDK releases, expanded third-party support, new native calculation profiles, and
improved analysis/reuse may be added without changing approved common operation results. Any genuine common semantic
extension or change returns to the owning ADR. No V2 dependency or incremental scheduler is mandated here.