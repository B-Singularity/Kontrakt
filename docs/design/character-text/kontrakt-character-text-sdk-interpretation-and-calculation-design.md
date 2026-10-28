# Character and Text SDK Interpretation and Shared Calculation Design

## Status

**Working Design — proposed implementation and conformance design; not an Accepted Contract decision.**

## Date

2026-10-28

## Governing Law and Source Priority

1. `docs/the-most-important-thing/what-contract-is.md` and the current applicable Accepted ADRs, especially ADR-0053 (Version), ADR-0057 (Failure), ADR-0063 (Establishment), ADR-0064 (Input), ADR-0065 (Admission), ADR-0066 (Canonicalization), ADR-0067 (Lowering), ADR-0071 (Resolved Contract HIR), ADR-0073 (JVM Platform-Native), ADR-0075 (Compiler Products), ADR-0076 (Canonicalization Built-In Catalog) and ADR-0078 (Numeric).
2. ADR-0080 *Characters, Strings, and Unicode*, **Draft**. Its reviewed §3.1–§3.3 are the current draft baseline; its unresolved §3.4–§3.14 and §5 questions remain unresolved. No checklist is completed by this Design.
3. `docs/design/canonicalization/built-in-law-catalog/canonicalization-built-in-law-catalog-design.md`, including the revised V1 `Initial` Unicode **18.0.0** specification for 11 existing laws. It owns those law-specific specifications and is not a second authority for shared Text operations.
4. The project Compiler Architecture Map and the existing API/Frontend Designs, as working implementation design rather than a replacement for Contract authority.
5. Actual uploaded SDK materials and the immutable research record included in **Appendix A**, used as comparative evidence. Source implementation and runtime behavior cannot override Kontrakt Contract meaning.

The supported SDK **audit baseline** is **Java SE 27 / OpenJDK 27 source and Kotlin/JVM standard library 2.4.21**. These are investigation versions, not evidence that all their callables already compile within Kontrakt. Java `Character`'s Unicode 17 data and Kontrakt's Unicode 18 common operation basis are independent.

---

# 1. Purpose and Scope

This Design governs how the compiler inventories and understands Java/Kotlin Character and Text source operations, relates them to the shared Character/Text calculation laws being considered under ADR-0080, and proves that its supported source surface is not accidentally incomplete.

Three separate inputs determine the work: (a) complete examination of the Java and Kotlin standard Character/Text API surface, including source-language facilities; (b) actual calculation needs of Input, Admission, Canonicalization, the existing Catalog, and other 1D laws; and (c) compiler-internal calculations used independently of authored Contract expressions. Common operations are selected **after** all three are inspected. The source API alone is neither the definition of the common law nor permission to use an API in every 1D.

This document additionally records a **selective and extensible** V1 third-party library investigation, notably Commons Lang, Commons Text, Guava, and ICU4J. They do not inherit the full-coverage obligation applicable to the supported Java/Kotlin standard Character/Text surface.

This is a Design document, not a user-facing `kontrakt.text` library, a new 1D Contract, a new IR family, a special runtime verifier, or an approval of proxy-style JVM execution. It selects no mandatory String storage layout, compiler pass, scheduling policy, vectorization strategy, or cache format. It does not infer that a recognizable callable is ratified under ADR-0073.

## 1.1. Preservation of existing Contract meaning

- **Character** is a Unicode Scalar Value; **Text** is a finite ordered sequence of Scalars, including the empty sequence. Exact Text Equality is exact ordered Scalar equality, without normalization, folding, or collation. Unassigned Scalars remain possible Text constituents; a property table does not define the Text value domain.
- A Java/Kotlin `char`/`Char` is a UTF-16 Code Unit. Host `String` intermediate results may contain unpaired surrogates. Interpretation must preserve them without establishing an invalid value as Kontrakt Text or silently substituting U+FFFD.
- An Input-specific size or presentation bound does not create a global Text length bound. Resource exhaustion, capacity and target encodings retain separate owners.
- Each 1D decides which calculations its authored law permits and owns the resulting Judgment/Material; Canonicalization Laws separately own their `E_L`, `C_L`, Coverage and exact Version. The shared operation can be used without performing that Judgment.
- Exact semantic references, Symbol Identity, source spelling/paths, HID/dense ordinals, cache keys, diagnostic renderings and target artifact bytes are different things.

## 1.2. Completion criteria of this Design

This Design is ready to guide implementation when it records a traceable source surface, a proposed semantic mapping for each relevant callable, a precise reason for any ineligibility, and a test method. **It is not permitted to label a source profile fully supported** until the release gates in §12 have evidence. The investigation record establishes candidates and risks, not achieved code coverage.

# 2. Support Profiles, Scope and Evidence

## 2.1. Profiles

A Source interpretation is parameterized at least by its actual source language, language version, applicable SDK/standard-library version, compilation configuration, resolved symbol and dependency context. Java platform behavior can additionally depend on class-library Unicode data, explicit/default Locale, Charset provider, regex options and observable mutable state. These determinants must not be replaced silently by a compiler-global Unicode 18 setting.

| Profile dimension | Audit baseline | Support decision / caveat |
|---|---|---|
| Java standard language and class library | Java SE 27; uploaded OpenJDK 27 `src.zip` | Inventory basis. JDK 27 binary/JMOD, `javac 27` symbol and executable golden tests not supplied/proven |
| Kotlin source and JVM stdlib | Kotlin 2.4.21 `kotlin-stdlib` sources and JAR | Inventory basis. Kotlin Compiler 2.4.21 actual resolution/ABI tests not yet proven |
| Kontrakt common Unicode calculations | Unicode 18.0.0 | Used **only** by selected version-dependent common operations and updated Canonicalization `Initial` laws; does not change Java/Kotlin source semantics |
| Java `Character` property behavior | JDK 27 documents Unicode 17.0 data | Host interpretation profile; distinguish from Unicode 18 property queries |
| External libraries | Candidate package families, no user-supplied JARs | Selected callable/version support to be approved later, not full-library support |
| JVM target Classfile | JVM-specific format and Modified UTF-8 | Backend/Artifact ownership; never equivalent to ordinary UTF-8 |

A profile update must be represented by an SDK/source-resolution and conformance delta; it cannot retcon an approved Exact Law Version. New SDK methods should be discovered by the inventory comparison, not by waiting for a user compilation failure.

## 2.2. Uploaded evidence and reproducibility

| Input | SHA-256 | Role |
|---|---|---|
| `src.zip` | `5195cc6bff6c9074b6485b2d99e0a37c15ae2346a9dc4d6905d093b89e595059` | OpenJDK 27 source archive |
| `kotlin-stdlib-2.4.21-sources.jar` | `1e29c0668b0aa555e7b154e5da2e8094e1b755cfee25737839195bbbdad8a4a2` | Kotlin common/JVM source declarations and implementation |
| `kotlin-stdlib-2.4.21.jar` | `bd8250210584cb659847dce9cda660f8e9906e5def7fbebcea93dc6dcb6a88a3` | Kotlin compiled API surface; mapping still to verify |

The research output counts **14,914** JDK `.java` source paths, **6,103** selected Java declarations, **384** Kotlin `.kt` source paths, **4,493** selected Kotlin source declarations, **9,284** broad Kotlin static declaration candidates and **860** previously extracted Kotlin JVM method entries for a narrower selection. These are different counting universes. The Java AST extraction used JDK 21 parser tools on some JDK 27 source, and its selected source set does not cover every JDK module. The Kotlin all-source screen is heuristic. Consequently none of these numbers alone establishes callable-level completeness.

## 2.3. Evidence artifacts needed by the implementation

Use the existing audited inventories and path screens rather than discarding their negative and uncertain rows:

- `java27_declarations_audited.tsv`; `jdk27_all_source_paths_coverage.tsv`; `jdk27_java_types_and_method_names.md`.
- `kotlin2421_declarations_audited.tsv`; `kotlin2421_all_source_paths_coverage.tsv`; `kotlin2421_all_source_declaration_screen.tsv`; `kotlin2421_source_files_and_declarations.md`.
- `audit_metadata.json`, containing input checksums and the extraction limitations.

The file lists are *evidence*, not a generated source of Contract authority. A row labeled `OUTSIDE_CURATED_CHARACTER_SURFACE`, `UNEXTRACTED_ADJACENT` or similar is a pending classification, **not** proof that the source contributes no callable of interest. A final completeness claim requires reconciliation with compiler-resolved APIs, including inherited, generated and mapped declarations.

# 3. Frontend Callable Interpretation Design

## 3.1. Unit of tracking

Each relevant Java/Kotlin callable shall have a traceable **design record**. The record's key must identify source-level meaning rather than just a method name or an emitted JVM member. The following fields are the inventory/projection *design schema*; they are not new Contract Identity or HIR physical handles.

| Group | Required record fields |
|---|---|
| Provenance | archive digest, source path/line or ABI owner, Java/Kotlin language/library profile, signature evidence, review state |
| Source selection | declaration owner, callable kind (member/static/extension/operator/constructor/property), receiver, parameter and result types, generics, overload, visibility, inherited/bridge handling, defaults, vararg, named arguments, nullable/Java platform types, opt-in/deprecation |
| Value behavior | UTF-16/Scalar/Byte/Grapheme indexing, null rules, empty inputs, ordering, equality, conversion, Unicode data/version, locale, result collection kind |
| Evaluation | evaluation order, mutations/aliasing, exceptions, callbacks/dynamic `toString`, laziness/eagerness, stateful regex matcher, environmental inputs, permitted nondeterminism |
| Semantic mapping | common calculation `Cxx`/`Uxx` where exact; otherwise Host-specific `H`, effect/state `T`, domain `D`, or compiler/target `B`; mapping preconditions and preservation obligations |
| Admission/use | applicable 1D law and whether use is permitted, prohibited, or unresolved; reason linked to existing law rather than a new Text policy |
| Validation | javac/Kotlin resolved-symbol fixture, reference execution, expected result/failure, lowering and optimizer differential tests, Unicode/property version evidence, release gate status |

A single callable may have several roles: e.g. it can be **recognized** and **source-resolved**, then be ineligible for Admission because it invokes an unclosed callback. Its recognition must not be lost merely because it is not admissible.

## 3.2. Lifecycle states must not be collapsed

| State | Required claim | Not implied |
|---|---|---|
| `Inventoried` | The declared/visible surface is recorded | Valid resolution or interpretation |
| `Recognized` | Frontend knows the precise callable form | That it is selected at a given source site |
| `Resolved` | Actual Java/Kotlin source binds to this declaration with correct overload and argument conversion | 1D eligibility |
| `Interpreted` | Operand/result semantics, relevant effects and determinants are known | Pure, deterministic, or admissible |
| `Mapped` | A C/U law is shown equivalent *under stated conditions*, or H/T/D/B is retained | Ratified JVM execution |
| `Admissible` / `Ineligible` / `Undecided` | The owning 1D's existing law determines use | That the other statuses change |
| `Implemented` | The compiler can realize the selected and eligible case | That it matches the reference SDK |
| `Conformant` | Source differential and transformation tests passed for the precise profile | That a future SDK release has identical semantics |

These are **coverage evidence labels**, not an additional Contract admission/establishment pipeline. An unsupported yet otherwise valid source API shall not be mislabeled an invalid user declaration. A genuine 1D prohibition must produce a diagnostic naming that prohibition, not `unknown method`.

## 3.3. Source-dependent resolution requirements

Java: method/constructor/field selection, overload and primitive widening/boxing, varargs, generic substitution, inherited overrides/default methods, `CharSequence` dynamic receiver, null literal selection, `+` concatenation, `String.valueOf`, lambda/method references, class/library version, checked/unchecked exceptions.

Kotlin: Kotlin/Java mapped types, member-before-extension behavior, imports, overload candidates, receiver and nullable receivers, default/named arguments, inline and reified functions, generic extension generation, `CharArray`/`ByteArray` expansions, delegated JVM calls, property syntax, operator calls, string templates and function-type/lambda bodies. A Kotlin source function need not correspond to a uniquely named public JVM method.

**No source shortcut:** extracting a Java/Kotlin method name from a binary instruction or matching `String` in a type name is insufficient. The exact source expression and binding determine the interpretation. Parsing JVM bytecode later is a distinct acquisition/evidence problem and does not retroactively grant source-call authority.

## 3.4. From source result to common Character/Text

A source-level Host intermediate result may remain a UTF-16 sequence containing lone surrogates. Common Scalar Character/Text establishment is permitted only if the relevant conversion is lawful. The decoder shall not silently drop, replace, normalize, fold or trim source information. For `CharSequence` backed by mutable/external state, reading a snapshot is a distinct evaluation from assuming an immutable Text.

The common operation `ScalarCount` is not an automatic replacement for Java/Kotlin `String.length`; Java `String.split(regex)` is not Kotlin literal `split`; JDK `equalsFoldCase` is not Kontrakt Unicode 18 folding unless equivalence is actually established for that operation's domain and version.

## 3.5. Semantic mapping and value-flow rule

A resolved source expression is evaluated under its *source semantics* and admitted by its owning 1D only if those semantics and effects are legally closed. When an identical shared calculation exists, its law may be used for the corresponding part. Unsupported conversions stay explicitly unresolved. Mapping must preserve failure, result shape, operand evaluation effects, locale/profile, source positions and ordering where observable.

The implementation may use a lookup/catalog, a query-driven analysis, or another replaceable realization; this document does not require a new IR, an API interception layer, a runtime Contract engine, or an AST-based Contract Authority. No direct call to user-authored `toString` or arbitrary `Predicate` may substitute for Contract interpretation.

# 4. Shared Calculation Candidate Catalog (Design Candidates Only)

Identifiers C01–C27 and U01–U17 are **working review handles**, not approved external API names, assigned Exact Law authorities, HIR identities or supported operations. The complete list and why each was found is preserved in Appendix A §2. Every selected operation will require operand domain, result domain, exact observations and unsuccessful-result conditions to be closed in ADR-0080; this Design must not decide open §5 checklist items by implication.

## 4.1. Scalar and sequence value calculations

| ID | Candidate | Required closure / typical SDK demand | Boundaries |
|---|---|---|---|
| C01 | Scalar validity and code point → Scalar | Distinguish range-valid code point from surrogate/non-scalar; `Character.isValidCodePoint` counterexample | Do not narrow valid Text by `isDefined` |
| C02 | Exact Scalar Count | Count every Scalar, including unassigned/combining; result finite | Not UTF-16/byte/grapheme count |
| C03 | Scalar element access | Position and valid index domain; unsuccessful out-of-range | Source `charAt` is code-unit |
| C04 | Scalar slice | Start/end, end-exclusive, empty boundaries, failures | Kotlin `IntRange` and Host `substring` differ |
| C05 | Text–Text concatenate | Exact ordered value, identity for empty operands, no implicit normalization | `String + Object` can invoke user code |
| C06 | Repeat | Count zero/negative, finite construction, length consequences | Resource limit is not Input rejection |
| C07 | Exact equality | Reuse already established ordered Scalar equality | No collation or case folding |
| C08 | Scalar lexical ordering | Code-point order and prefix relation if selected | Java UTF-16 `compareTo` may differ |
| C09 | Literal search | Empty needle, first/last, from-position, overlap, success/failure, units | Distinct from Regex Matcher |
| C10 | Prefix/suffix/containment | Derive from equality/search where exact | May provide convenience projection without new primitive |
| C11 | Region equality | Two source ranges, units, length, failure | JDK `regionMatches(ignoreCase)` Host rules |
| C12 | Literal replace | Left-to-right, overlap, empty target, replacement, exact result | Regex replacement grammar separate |
| C13 | Literal delimiter split | Delimiter precedence, empty tokens, limit, result collection, failure | Java Regex `split` != Kotlin literal `split` |
| C14 | Text-only join | Separator, sequence cardinality, empty/nil distinction | `toString`, `null` and callback not pure join |
| C15 | Finite exact-set trim | Declared membership set, leading/trailing/both, empty and all-trimmed | JDK trim/strip, Kotlin and Catalog W5/W6/W18 differ |
| C16 | Whitespace runs collapse | Exact set, replacement, empty/edge behavior | Catalog-specific folding retains authority |
| C17 | Pad/truncate/fixed-size construction | Position units, pad value, truncation, range, shape | Display width not Scalar Count |
| C18 | Line-separator recognition/split | CR/LF/CRLF, final empty line, Unicode separators when applicable | Not UAX #14 line-break opportunities |

## 4.2. Encodings and narrowly bounded textual codecs

| ID | Candidate | Closure to investigate | Boundary |
|---|---|---|---|
| C19 | UTF-16 code-unit length/index/slice | Host-accurate code-unit sequences, including ill-formed intermediates | Never redefine Scalar Text |
| C20 | Strict UTF-16 ↔ Scalar conversion | Surrogate validity and unsuccessful input position | Replacement decoding separate |
| C21 | Strict UTF-8 encode/decode | Overlong forms, invalid bytes, truncation, surrogate encoding, >U+10FFFF | BOM/framing/offset separately explicit |
| C22 | Strict UTF-16 encode/decode | Byte order, BOM, malformed pair handling | Host Java String may be ill-formed |
| C23 | Strict UTF-32 encode/decode | Endianness, illegal scalar, BOM if supported | Scope not automatically V1 |
| C24 | Encoded lengths and offsets | Exact length/offset units, mapping across code units/bytes/scalars | Artifact framing and Buffer layout distinct |
| C25 | Explicit-variant Hex encode/decode | Hex digits, case, prefixes, odd nibble and parsing failures | UUID grammar still domain-owned |
| C26 | Explicit-variant Base64 encode/decode | Basic/URL/MIME, padding, invalid characters, chunk framing | Byte codec, not Unicode normalization |
| C27 | Explicit-grammar escape/unescape | Java literal/Text Block, JSON, XML, HTML, URL as *different* grammars | Do not create a universal escape function |

## 4.3. Unicode 18.0.0 data/algorithm-dependent candidates

| ID | Candidate | Required semantic determinants | Boundary |
|---|---|---|---|
| U01 | General_Category and derived Property | Exact UCD 18 tables and property type/result | `isDefined` is version-specific Host behavior |
| U02 | Script and Script_Extensions | Property and mapping policy | Identifier policy remains Frontend |
| U03 | White_Space / Pattern_White_Space | Exact chosen property and immutable version | JDK `strip` is not equivalent |
| U04 | Nd and Decimal_Digit_Value | Final release UCD, digit ranges/values | General NumericValue differs |
| U05 | Simple case mapping | Single-scalar mapping, version, applicable characters | Java `char`/`int` overloads differ |
| U06 | Full/conditional/locale case mapping | One-to-many, context and locale determinants | Kotlin/Java default locale differs |
| U07 | Default Full Case Fold | Exact Full mapping and profile | Not JDK `equalsIgnoreCase` |
| U08 | NFC/NFD/NFKC/NFKD | UAX #15, Unicode 18 data, domain and normalization form | No implicit Text equality |
| U09 | Normalization check/quick check | Form, version, accuracy of Yes/Maybe/No | Check not always normalization result |
| U10 | NFC / NFKC Case Fold | Distinct full composition, NFKC_CF/ignorable semantics | Existing Catalog Law judgment stays separate |
| U11 | ID/XID Start/Continue | Exact UCD properties | Language identifier admissibility separate |
| U12 | Emoji/Variation/Default_Ignorable etc. | Precisely selected properties and value type | Whole UCD not automatically V1 source API |
| U13 | Extended Grapheme boundaries | UAX #29 version, returned boundaries/units | Grapheme != Scalar; display width separate |
| U14 | Word/Sentence boundaries | UAX #29 rules, version/profile | Deferred until real consumer scope confirmed |
| U15 | Line break opportunities | UAX #14, line-break classes, rule/version | Distinct from CRLF line parsing |
| U16 | Confusable Skeleton material | UTS #39 version and exact mappings | Security Judgment not a Text result |
| U17 | BiDi property/algorithm material | UAX #9 profile, required observations | Rendering/visual order and security policy separate |

### 4.4. Selection rule

An operation is selected only if its exact semantics is independently reusable by at least one demonstrated 1D or compiler calculation, or is necessary to interpret a supported source callable without loss; mere presence in a standard library does not require creating a new Contract operation. `C` candidates may be derived from other `C` laws if this preserves their entire visible result. `U` candidates need exact versioned data and applicable stability claims. If source semantics are not equivalent, a Host interpretation route **remains distinct** even if it reuses a calculation internally.

# 5. Fourteen Investigation and Implementation Bundles

Bundles are **work and coverage partitions**; they are not 14 independent new Contract authorities or compiler passes. Each bundle must have callable provenance, semantic distinctions, shared candidate mapping, consumer mapping and differential tests. The original detailed investigation, exact SDK method families and counterexamples remain in Appendix A §3.

| Bundle | Concrete source/API scope | Shared candidates | Requirements retained outside common Text law | Review exit evidence |
|---|---|---|---|---|
| **0 — Callable completeness** | Java/Kotlin declarations, member/extension/default/inline, Java mapped APIs, source `+` and string templates, `StringConcatFactory` | All as indexed | Language resolution; ADR-0073 eligibility | Coverage inventory + javac/Kotlin symbol fixtures |
| **1 — Value domain** | `Character`, `Char`, `CharSequence`, surrogate/codepoint, nullable | C01/C20 | Host ill-formed UTF-16 and aliasing | Scalar vs code-unit counterexample tests |
| **2 — Sequence operations** | Length, charAt/get, substring/slice, concat/repeat, range | C02–C06/C19 | Index units, throws, user object effects | Boundary/golden range matrix |
| **3 — Compare/search** | `equals`, `compareTo`, `indexOf`, `regionMatches`, ignoreCase, fold case | C07–C11/U07 | JDK version, UTF-16 ordering, locale/Host ignoreCase | Empty/overlap/non-BMP/case tests |
| **4 — Transform** | replace, literal/regex split, join, trim, pad, indent/lines/translateEscapes | C12–C18 | Regex, callback, delimiter precedence, whitespace variants | Java–Kotlin differential corpus |
| **5 — Properties** | `Character`, `Char`, Script, blocks, digits, emojis, identifier props | U01–U04/U11–U12 | JDK 17 UCD, Frontend identifier permission | Unicode 17/18 property boundary tests |
| **6 — Case behavior** | char/string mappings, ignoreCase, foldCase, Locale | U05–U07/U10 | Default Locale and context; source-specific behavior | One-to-many and Turkish/Greek tests |
| **7 — Normalization/Catalog** | Normalizer, canonical matching, 11 existing Canonicalization laws | C15/U03–U04/U07–U10 | Each Law's `E_L`/`C_L`/Version | UCD 18 conformance + Catalog invariants |
| **8 — Regex/segment** | Pattern, Matcher, Kotlin Regex, MatchResult, splits/replacement, BreakIterator | C09/U13–U15 | Regex grammar, matcher state, work bound, captures | Compiler/SDK regex fixtures, work proof |
| **9 — Encoding** | Charset, String `getBytes`/`encodedLength`, Decoder/Encoder, Kotlin ByteArray | C20–C24 | Provider/malformed policies, I/O/target | Strict vs replacement + boundary corpus |
| **10 — Formatting/codecs** | HexFormat, Base64, String.format, Formatter, Scanner, Message/List Format, domain parsing | C25–C27 | Numeric/Temporal/URI/XML, `toString`, locale | Grammar/formatter side-effect tests |
| **11 — Mutable/functional** | StringBuilder/Buffer, CharBuffer, Kotlin map/filter/fold, streams/sequences | Reuse selected C/U | State, alias, callbacks, evaluation, collection law | Effect/evaluation-order tests |
| **12 — Compiler consumers** | Lexer, literal, source offset, diagnostics, identity/cache, Classfile, I/O | C02–C05/C09/C18/C20–C24/U01–U03/U13 | Symbol, SourceManager, Artifact, Target | Round trip, source positions, target constraints |
| **13 — Third party** | Commons Lang/Text, Guava, ICU4J selected APIs | Existing C/U reuse | Version pin, native eligibility, effects, dependency graph | Real JAR symbol + reference fixtures |

## 5.1. Bundle 0 — no ‘unknown SDK API’ as a substitute for analysis

A supported Java/Kotlin standard Character/Text API should be recognized and resolved even if a particular authored use is disallowed. The inventory must traverse inherited declarations, `CharSequence`, Kotlin generated/inline extensions, null receivers, `CharArray` and `ByteArray` codecs, Java operator concatenation, Kotlin template conversion and other non-method-call syntax. Every source expression is associated with the actually selected overload; a method name or bytecode signature alone is not enough.

## 5.2. Bundles 1–4 — value, sequence and transform accuracy

Maintain the source's index and result domains. Java/Kotlin `String.length` counts UTF-16 units; `"😀"` is two units and one Scalar. Java `substring(0,1)` on that value produces a Host String with a lone surrogate, not common Text. Java `"a,".split(",")` and Kotlin `"a,".split(',')` produce different list shapes. Java `trim`, Java `strip`, Kotlin `trim` and Unicode `White_Space` derive from distinct predicates. Exact sequences and conditional variations must not be collapsed under one convenient operation name.

Search must distinguish empty needles, overlapping matches, first/last, from positions and out-of-bounds. Split must explicitly model delimiter order, zero-width matches and trailing empties. For `String.join` and Kotlin `joinToString(transform)`, object conversion/callbacks must be accounted for rather than erased by pure Text Join.

## 5.3. Bundles 5–7 — versioned Unicode without Host substitution

Unicode 18 common property/normalization/fold computations are separate from Java 27 `Character` Unicode 17 and from mutable/default Locale observations. `Character.getNumericValue` is not the same as `Nd` Decimal Value. A normalization stability guarantee for previously assigned scalars is not a general proof of equality for all Scalars or for segmentation, newer Properties and Case Mapping.

The 11 Catalog `Initial` laws are independent Canonicalization law selections: leading/trailing/boundary Unicode trim (3), decimal-digit fold (1), four normalization forms (4), default full fold and NFC/NFKC case fold (3). Their representative calculations may reuse selected shared operations, but the Catalog's exact law identity, Version, Coverage, `E_L` and `C_L` remain outside this Design's authority.

## 5.4. Bundle 8 — regex meaning and finite work

Java `Pattern`/`Matcher` has flags, named groups, zero-width matching, capture, matching state, regions, transparent/anchoring bounds, replacements and laziness. Kotlin `Regex` on JVM typically uses Java regex machinery but exposes different result/limit/collection semantics. UAX #29 grapheme segmentation, Java regex `\\X`, Java UTF-16 positions, line breaking and display clipping are not the same result law.

Recognizing arbitrary regex source is mandatory for interpretation coverage; **admitting** a regex in a 1D that requires provably bounded calculation is conditional on the existing Admission §7.7 work law. No unsupported regex may be executed as a delegated JVM policy engine.

## 5.5. Bundles 9–10 — encodings, codecs, formatting and domains

Strict UTF conversion and replacement/ignore/repair are distinct. Charset provider and error action are observable Host determinants. Java 27 `String.encodedLength(Charset)` includes its encoder's behavior and is not strict UTF-8 byte count. UTF-8/UTF-16/UTF-32, BOM and byte order, buffer offsets and JVM Modified UTF-8 must be identified separately. Hex and Base64 have variant-specific grammars; a generic ‘decode’ cannot hide MIME, URL-safe or padding behavior. Source escapes, JSON, XML, HTML and URI percent encoding are different grammars.

`Formatter`, `Scanner`, `MessageFormat`, `ListFormat` and numeric/temporal/URI conversions can call user methods or consume locale/environment-dependent material. The recognition of an SDK API is not approval to execute these effects within a pure Text calculation.

## 5.6. Bundle 11 — temporary state is not Text mutation

Builder operations (`append`, `setLength`, `reverse`, range edits), aliasing and escape, Kotlin `buildString`, generated `map`/`filter`/`fold`, eager lists and lazy sequences/streams must preserve evaluation order, captures and effects. An Admission-local finite binder does not make an Input Text mutable. A collection operation cannot be represented by arbitrary higher-order Contract structure merely because the source API takes a lambda.

## 5.7. Bundle 12 — internal compiler consumer coverage

The compiler may use shared calculations independently of authored source: decoding source files, escape and literal validation, identifier spelling inspection, exact constant folding, offset mapping, line/column diagnostics, grapheme-safe clipping, deterministic serialization and optimizer checks. Host implementation selection cannot redefine these calculations. Symbol/Path/HID/Cache/Artifact equality remains separately governed, as does JVM Classfile Modified UTF-8 and its target constraints.

## 5.8. Bundle 13 — selective external support

The initial review families are Commons Lang `StringUtils`/`Strings.CS`/`Strings.CI`/`RegExUtils`; Commons Text `StringEscapeUtils` and strictly closed replacements; Guava `CharMatcher`/`Splitter`/`Joiner`/`Strings`/`Ascii`/`CaseFormat`; ICU4J `UCharacter`/`Normalizer2`/`UnicodeSet`/`BreakIterator` selectively. Do not automatically admit `StringSubstitutor` arbitrary lookups, ICU Collator/Transliterator, arbitrary Java Pattern or any dependency-transitive API. No external JAR was supplied, so callable-level approval remains pending.

# 6. Implementation Data Projection and Traceability

## 6.1. Catalog records as an implementation seam

Use a source-versioned audit table and generated (or equivalent) semantic mapping evidence rather than hand-maintained method-name switches. An implementation may divide records into files, queries or another storage layout. The following *conceptual* fields make the requirement testable:

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

These values describe a **Design inventory**, not a new HIR authority, a version-binding substitute or an instruction that every runtime execution be intercepted. Frontend/HIR can project only semantically needed information through its already governed protocol.

## 6.2. Concrete mapping examples

| Source callable/expression | Required interpretation | Possible shared calculation | Why it cannot be mapped blindly |
|---|---|---|---|
| Java `s.length()` / Kotlin `s.length` | UTF-16 code-unit count | C19 | C02 ScalarCount gives a different result for supplementary scalars |
| Java `s.codePointCount(a,b)` | Host UTF-16 region counting with ill-formed handling | C19/C20 under strict preconditions | May count isolated surrogate code units |
| Java/Kotlin `s.substring(...)` | UTF-16 positions and language-specific overload/range | C04 only with validated correspondence | May produce lone-surrogate result |
| Java `String.split(regex)` | Pattern flags, default limit, trailing-empty policy | C13 only in constrained literal-equivalent cases | General Java Regex and split semantics |
| Kotlin `s.split(',')` | Literal delimiter, Kotlin trailing-empty rule | C13 with exact policy | Delimiter order and limit must be retained |
| Java `s.strip()` | JDK `Character.isWhitespace` | C15 only with exact Host-set equivalence | Not Unicode 18 `White_Space` |
| Kotlin `s.lowercase()` | Locale.ROOT, Kotlin/JDK behavior | U06 only on proven equivalent profile/domain | Not Java default-locale mapping |
| Java `s.equalsFoldCase(t)` | JDK 27 Full Case Fold comparison | U07 only if equivalent for exact version/domain | JDK 17 UCD vs U18 not silently erased |
| Java `String.encodedLength(cs)` | Charset encoder and replacement rules | C24 under proven same encoder semantics | Not strict UTF-8 byte count |
| Kotlin `Regex.replace(transform)` | Lambda calls and matching results | C12 only for a proven fixed literal fragment | Evaluation/effects and state are essential |
| `builder.append(obj)` | Mutable builder + object-to-string conversion | C05 only after lawful snapshot/pure conversion | User method and alias may be observable |
| Java/Kotlin `+` with Object | Source evaluation and conversion | C05 only for known Text operands | Compiler lowering recipe not source semantics |

## 6.3. No completeness by inference

Do not auto-mark a family supported because one overload passes tests or its name exists in the extraction. Each exposed, supported overload must be tested with applicable receiver types, generic inference and default arguments; a family-level exception must be recorded explicitly. Deprecated public API remains in the inventory until its actual source profile excludes it; deprecation alone is not a reason to pretend it does not exist.

# 7. Third-Party API Qualification and Evolution

## 7.1. V1 selection mechanism

For each selected external library, pin **Maven coordinates + exact version** first, inventory the actual API and inherited/overloaded forms, record runtime dependencies and Java/Kotlin binding, then determine a closed subset of Callables and source use contexts. Commons/Guava/ICU4J candidates in Appendix A are priorities for investigation, **not release commitments**. No user-uploaded external JAR was available to substantiate a full method inventory.

A new library version must have a declaration delta and semantic regression review. A newly supported callable may reuse C/U calculations, while a new operation law needs separate ADR review. Do not change existing shared operation meanings just to match a third-party convention.

## 7.2. Dynamic and domain-sensitive surfaces

`StringSubstitutor` with arbitrary lookups, Guava `Joiner` with custom object conversions, Collation/MessageFormat locale behavior, UnicodeSets driven by arbitrary patterns, arbitrary regex engines and client callbacks have additional state/grammar/effect boundaries. Recognition is not automatic execution eligibility. Where an exact, finite, pure subset is provable, support can be added by an explicit callable qualification without inventing proxy-style runtime enforcement.

# 8. Compiler Product, Optimizer and Target Preservation

## 8.1. Value preservation

Frontend source parsing may preserve literal spelling, escape provenance, source encoding and exact scalar value as **different observations**. HIR resolved semantic references remain separate from source location or in-memory storage identity. Establishment receives only the lawful determinants the owning law requires. Generated APIs remain downstream projections rather than foundations of common Text meaning.

## 8.2. Transformation constraints

A constant fold or rewrite is allowed only if it preserves all legally observable source results and effects, including exceptional behavior, locale/encoding determinant, match state and order. In particular:

- `NFC(a + b)` is not generally equal to `NFC(a) + NFC(b)`.
- Case-insensitive comparison is not generally full case folding, and JDK Unicode 17 results are not guaranteed to equal Unicode 18 results.
- A concatenation involving an Object may invoke user `toString`; it is not a pure Text concatenation without proof.
- Moving `Regex.replace(transform)` can reorder callbacks or change their invocation count.
- Replacing Java `String.length()` with Scalar Count is unsound; UTF-16 offsets must not become scalar offsets.
- UTF-8 Byte Count and JVM Constant-Pool Modified UTF-8 encoded length are separate observations.

## 8.3. Performance remains replaceable

Representations, caching, deduplication, normalization quick checks, SIMD/intrinsics and streaming are allowed implementation choices only after equivalence and resource behavior are proved for the exact selected law. No such implementation is mandated or made a source of semantic authority. Compiler resource exhaustion must not be laundered into a 1D Admission rejection.

# 9. Diagnostics and Failure Distinctions

The compiler must distinguish at least the following classes and identify the actual owner/source context rather than emitting an undifferentiated ‘unsupported string function’ error.

| Diagnostic class | Meaning | Owner / treatment |
|---|---|---|
| Invalid Java/Kotlin source | Resolution cannot find a legal callable according to the source language/profile | Frontend language analysis |
| Valid SDK source, missing coverage | API belongs to the claimed supported profile but the compiler inventory/interpreter lacks it | Compiler defect/unsupported implementation scope, **not** a 1D user violation |
| Known source, ineligible authored use | Exact semantics are recognized but owning 1D prohibits the effect, basis, state, callback or work | Diagnostic grounded in existing 1D law |
| SDK version/profile mismatch | Callable is absent or changed for the configured actual SDK profile | Profile mismatch with explicit version |
| Common-value conversion unsuccessful | A Host sequence is not legal Scalar Text, or strict encoding fails | Preserve precise conversion/failure conditions, ADR-0057 ownership |
| Exact-calculation not selected | A C/U candidate has not been approved or its law is not closed | Design/support gap, not a made-up Contract restriction |
| Resource/target limitation | Work budget, artifact size, Classfile encoding or compiler capacity | Responsible budget/target owner, not spurious Input rejection |
| Platform ratification absent | Recognized native callable not legally qualified for execution | ADR-0073 qualification outcome |

An error message should present the selected source symbol/profile and failed condition when known, with source spans and escaping that do not split surrogate pairs or hide control characters. Avoid claiming whether the application UI can render a grapheme faithfully.

# 10. Conformance Strategy — Evidence Rather Than Counts

## 10.1. Independent verification gates

| Gate | Required input | Pass condition | Blocking failure |
|---|---|---|---|
| G0 — Inventory closure | Exact SDK/JAR/source profile and ABI/JMOD | Every relevant callable reconciled to resolved symbol or an explicit reviewed exclusion | Missing inherited/inline/generated/overload forms |
| G1 — Language resolution | javac 27 and Kotlin 2.4.21 compiler fixtures | Same declaration/argument evaluation/overload chosen as reference compiler | Name-based or JVM-signature-only resolution |
| G2 — Host meaning | Executable golden fixtures on matching runtime | Results, exceptions, effects, null, locale/charset/Unicode/regex-state as documented | Unexamined behavior declared pure |
| G3 — Shared mapping | C/U law proof or H/T/D/B mapping | Equivalence constraints and non-equivalence clearly recorded | Host 17 property treated as U18 by default |
| G4 — 1D legality | ADR-0064/65/66/73 and other owning laws | Permit/deny reason equals actual owner law | Recognition conflated with admissibility |
| G5 — Lowering/optimization | Differential compiler, generated code and native reference | Results and legally visible effects preserved | Constant-fold/optimizer changes source meaning |
| G6 — Release regression | Static inventory deltas + golden vectors + diagnostics | Supported profile has no unaccounted eligible API path | Silent missing SDK API discovered by end users |

Failure of one gate must not be disguised by successful tests in another. A `javac` compile proving source validity does not prove Kontrakt's lawful 1D use, and an ICU property test does not prove JDK `Character` behavior.

## 10.2. Boundary fixtures that are mandatory to cover

| Family | Golden and negative cases |
|---|---|
| Scalar/UTF-16 | supplementary scalar (`"😀"`), isolated high/low surrogate, `isValidCodePoint(0xD800)`, char/int overloads |
| Index/range | zero-length, end-exclusive, negative and overflow, Kotlin `IntRange`, invalid slice and zero repeat |
| Search/split | empty needle, overlapping hit, first/last/from index, Java default regex split vs Kotlin literal split, prefix collision in Kotlin delimiter list, zero-width regex |
| Whitespace/line | U+2003, U+00A0, CR/LF/CRLF, trailing delimiter and empty final line, JDK trim vs strip vs Kotlin trim |
| Unicode/case | `ß` against `ss`, Turkish `I`, Greek contextual sigma, Java 17 property versus Unicode 18 new assignments, `Nd` vs numeric values |
| Normalization | decomposed/composed forms, cross-boundary NFC(concat), UCD 18 official conformance, idempotence |
| Regex | `CANON_EQ`, Unicode flags, `\\X`, zero-width progress, Matcher.region/state, captures, Kotlin transform callback effects, bounded work |
| Mutation/functional | Builder reverse surrogate handling, setLength NUL, captured state, lazy/eager collection, Stream/Sequence behavior |
| Encoding | malformed UTF-8/UTF-16, strict vs replacement, BOM/endian, Java `encodedLength` with lone surrogate, Classfile Modified UTF-8 |
| Codecs/format | Hex odd digit, Base64 variant padding/MIME/URL, Java/JSON/XML escapes, `String.format("%s", object)` `toString` effects |
| Identity/diagnostics | `String.intern` vs Symbol/HID equality, escaped controls, grapheme-safe clipping, source byte/UTF-16/scalar coordinates |

The full case-by-case research matrix, including additional SDK call forms, is preserved verbatim in Appendix A §5.1. The fixtures need exact expected outputs and reference compiler/runtime version; this document does not invent unexecuted expected results.

## 10.3. Standard SDK completeness claim

For the selected Java/Kotlin source profile, an API family is **complete** only after every exposed supported callable and overload has a resolution fixture, semantic classification and eligibility disposition. The overall standard Character/Text coverage is complete only after unresolved rows are zero **in the audited scope** and the audited scope is itself reconciled with official source+binary+compiler symbol data. A selected V1 third-party family can instead declare an intentionally partial and versioned callable allowlist.

# 11. Implementation Plan and Dependencies

| Work package | Owner's design concern | Input → output | Blocker / done criterion |
|---|---|---|---|
| P0: Scope reconciliation | SDK inventory/ABI audit | Source paths + JDK 27 JMOD/javac + Kotlin compiler/stdlib → callable coverage table | Missing JDK binary and official compiler symbol evidence |
| P1: Semantic classification | Frontend Source Interpretation | Each resolved callable → H/T/D/B or equivalent C/U with preconditions | No unclassified supported standard Text API |
| P2: Common operation selection | ADR-0080 decision process | C01–C27, U01–U17 evidence → closed exact operation law | Each selected operand/result/failure and version defined; **requires ADR approval** |
| P3: 1D consumer mapping | Input/Admission/Canonicalization/others | Applicable authoring expression + calculation → existing 1D judgment | Existing Authority untouched; no opaque JVM execution |
| P4: Frontend/IR use | Existing HIR protocol and compiler flow | Source semantics → needed resolved representations and provenance | No new IR stage or HIR identity assumption |
| P5: Reference implementation/tests | Compiler realization and QA | Fixed SDK/Unicode fixtures → differential results | Error/effect/locale/surrogate/regex tests pass |
| P6: External adapter expansion | Versioned third-party Callable Catalog | Exact JAR + source/ABI → selected supported overloads | Per-library allowlist and tests; no transitive blanket promise |
| P7: Release and upgrade | Quality gate | New SDK/Unicode/library version → inventory delta + test rerun | No silent semantic profile upgrade |

**V1:** Target the supported Java/Kotlin standard Character/Text profile in full recognition/interpretation, subject to demonstrated source resolution and existing 1D eligibility; select external library calls deliberately. This is a **design target**, not a claim that existing V1 code already meets it.

**V2 and later:** New Java/Kotlin SDK releases, expanded third-party support, new native calculation profiles, and improved analysis/reuse may be added without changing approved common operation results. Any genuine common semantic extension or change returns to the owning ADR. No V2 dependency or incremental scheduler is mandated here.

# 12. Traceability to ADR-0080 Without Pretending Acceptance

| ADR-0080 draft area | Evidence/design supplied here | What remains to be decided in ADR |
|---|---|---|
| §3.3 / §5.3 | SDK/1D/compiler demand separation; C01–C18 and result distinctions | Which exact operations are Contract laws and their closed result/failure conditions |
| §5.4 | Unicode U01–U17, UCD 18 profile; JDK 17 separate | Which property/fold/segment operations are selected and exact version/algorithm law |
| §5.5 | C19–C27, strict/lossy/streaming/target distinctions | Exact codec/encoding scope, invalid input results, offset units |
| §5.6 | SourceCallableRecord fields, full Java/Kotlin core standard API recognition target | Legal supported authoring surface and Source interpretation closure |
| §5.7–§5.8 | No new HIR identity/IR stage or Establishment authority | Particular projection/preservation evidence in existing compiler flow |
| §5.9–§5.11 | Effects/alias/regex/Locale/target distinctions and differential validation | Target- and execution-specific legal guarantees, optimizer proof obligations |
| §5.12–§5.13 | Identity/path/HID/cache and diagnostics separate from Text law | Detailed diagnostic and resource law integration |
| §5.14 | G0–G6 evidence gates, V1 external subset and future profile changes | Supported release scope and final approval |

No ADR checkmark is changed. No Source API signature is newly declared part of a Contract solely by the presence of a candidate in this Design.

# 13. Open Items, Risk Register and Research Boundaries

| Risk | Evidence today | Necessary closure |
|---|---|---|
| JDK parser/ABI mismatch | Extracted JDK 27 source using tools from JDK 21 in part; no JDK 27 JMOD verified | Cross-check source with exact JDK 27 JMOD and `javac 27` |
| Kotlin source/JVM mismatch | `expect/actual`, inline, generated functions and mapping; no Kotlin Compiler 2.4.21 symbol fixture run | Run Kotlin compiler source binding and compiled stdlib ABI checks |
| Inventory scope false negatives | Selected 354-ish Java source study and static screens do not prove all exposed SDK Text API; many Kotlin adjacent files | Explicit `all-source` exclusion review, inherited visibility, module boundaries |
| Exact external library support | No third-party JARs supplied | Exact Maven version, sources, ABI, golden corpus per selected Callable |
| Unicode profile leakage | Java 27 Unicode 17, Kontrakt Unicode 18 | Version-specific reference tests and separate mapping |
| Work/effect leakage | Matcher, lambdas, builders, formatters, IO | Existing 1D and ADR-0073 finite/effect eligibility evidence |
| Loss of semantic distinctions | UTF-16 vs Scalar; source path/identity vs Text value; MUTF-8 | End-to-end differential and target tests |
| Approved vs proposed conflation | ADR-0080 §3.4–§3.14 open, 44 C/U items are candidates | Explicit ADR closure per operation, no automatic checklists |

This Design does not claim the SDK source-extraction phase has proved a guarantee against every end-user compilation failure. It specifies the audit and release gates needed to make such a guarantee defensible, and preserves the raw researched candidates without treating them as accepted implementation behavior.

# 14. Reference Sources

- The OpenJDK 27 archive `src.zip` and Kotlin/JVM 2.4.21 `kotlin-stdlib-2.4.21[-sources].jar` uploaded for this work, with hashes in §2.2.
- Official Java SE 27 API: https://docs.oracle.com/en/java/javase/27/docs/api/
- Kotlin standard-library API: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/ ; https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.io.encoding/ ; Kotlin Java interop: https://kotlinlang.org/docs/java-interop.html
- Unicode 18: https://www.unicode.org/versions/Unicode18.0.0/ ; UAX #15, #29, #31, #14 and UCD via https://www.unicode.org/reports/tr44/
- Candidate libraries: https://commons.apache.org/proper/commons-lang/ ; https://commons.apache.org/proper/commons-text/ ; https://github.com/google/guava/wiki/StringsExplained ; https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/
- Project: relevant Accepted ADRs named at the beginning, ADR-0080 Draft, updated Unicode 18 Canonicalization Catalog, Compiler Architecture Map.

---

# Appendix A. Complete Research Record (Unabridged, Non-Normative)

This Appendix reproduces **the full prior 516-line research report without editorial deletion**. It preserves every researched SDK group, all 44 candidate IDs, examples, counterexamples, audit limitations and source references used to write this Design. Its investigation recommendations are **not** approved Contract law and do not override the numbered Design sections or Accepted ADRs. Research counts and any unresolved differences retain the evidential limits stated in their original context.

# Kontrakt ADR-0080 — JDK 27 · Kotlin/JVM 2.4.21 전체 Character/Text 기능 조사와 계산 계약 묶음 선정안

- 조사 기준일: **2026-10-28**.
- 상태: **조사 보고서 / 설계 제안**. ADR-0080의 승인된 Decision을 변경하지 않았으며 체크리스트를 완료로 변경하지 않았다.
- **실물 입력:** 사용자가 업로드한 OpenJDK 27 `src.zip`, `kotlin-stdlib-2.4.21-sources.jar`, `kotlin-stdlib-2.4.21.jar`.
- **프로젝트 근거:** ADR-0080 §3.1–§3.3 및 §5.3–§5.14, ADR-0064, 0065, 0066, 0073, 0076, 0078, Unicode 18로 갱신한 Canonicalization Built-In Law Catalog Design.
- 이 보고서는 **API 전체 기능의 조사와 필요한 계약법의 범위 선정**이다. SDK 메서드 전체가 현재 1D에서 실행 가능하다거나 모든 오버로드가 이미 컴파일되도록 구현됐다는 뜻은 아니다.

## 0. 결론 — 계산법보다 먼저 충족해야 할 해석 의무

Kontrakt가 Java 및 Kotlin을 계약 작성 언어로 지원한다면, **지원하는 언어 및 SDK 버전에서 합법적으로 선택되는 문자 관련 Callable을 누락 없이 인식하고, 정확한 소스 의미와 부적격 사유를 구별할 수 있어야 한다**. 이 의무는 공통 Character/Text 계산 법칙의 수보다 크다. Java·Kotlin의 모든 API를 새로운 독립 계약 연산으로 승격하는 것은 불필요하고 위험하다.

이번 실물 조사의 결과는 네 층으로 분리된다.

1. **SDK Interpretation 표면**: Java/Kotlin 언어 구문, 타입 매핑, 선언·오버로드·확장·상속·제네릭·기본 인수·인라인·클로저의 정확한 해석. 사용자 코드의 적법한 Source가 컴파일러에 알려지지 않은 API라는 이유로 잘못 실패하면 Coverage 결함이다.
2. **Source-bound 계산 및 효과**: UTF-16, JDK Unicode 17 Property, Kotlin/JVM 특유의 Locale·정규식·상태·예외·콜백 등 원래 호출의 의미. 공통 연산과 같다는 증거가 없는 호출은 무리하게 합치지 않는다.
3. **Kontrakt 공통 계산 법칙**: 여러 1D/컴파일러 컴포넌트에서 동일하게 사용할 정확하고 독립적인 Scalar/Text 계산. Unicode 데이터 기반 법칙은 **Unicode 18.0.0**의 규범적 버전을 갖되, 데이터 비의존 연산까지 억지로 버전에 묶지 않는다.
4. **판단을 소유하는 소비자**: Input, Admission, Canonicalization, Fact/Invariant, Numeric, Temporal, Symbol/Path, Compiler Product가 어떤 계산을 자기 Judgment에 사용할지 결정한다. 새로운 Text Law로 그 권한을 이전하지 않는다.

**실무적 결론:** 아래 14개 묶음의 기능 범위를 모두 검토하고, 기본 연산·Unicode·Host 어댑터·도메인 판단을 한 번에 구분한 뒤 ADR-0080 §5.3~§5.14로 반영하는 것이 맞다. 일부만 먼저 닫고 누락된 API를 발견할 때마다 기본법을 고치는 방식은 피해야 한다.

### 0.1 SDK 실물 추출·Coverage 수치의 의미

| 관찰면 | 이번에 실제 확인한 자료 | 완전성의 의미 |
|---|---:|---|
| JDK 27 `src.zip`의 `.java` 소스 경로 | **14,914개** | ZIP 경로 **전수 기록**. 모두가 외부 공개 API인 것은 아님 |
| Java AST 분석 확장 대상 | **354개 소스 파일** | `java.lang` 최상위, `java.text`, `java.util.regex`, `java.nio.charset`, `java.io`, `java.util.stream` 및 특수 관련 API를 포함 |
| 고유 Java 추출 선언 | **6,103개** | 생성자·메서드·필드·타입 혼합, 공개·보호 선언. 각 선언의 소스/줄/그룹 기록 |
| Kotlin 2.4.21 소스 JAR | **384개 `.kt` 파일** | 파일 경로 전수 기록 |
| Kotlin의 해당 소스 원장에서 선언이 추출된 파일 | **58개** | `kotlin.text`, `kotlin.io.encoding`, Kotlin 기본 타입, 관련 생성 코드·컬렉션·I/O |
| Kotlin 관련 소스 선언 | **4,493개** | Source 형태의 공개 선언 후보. 원장에는 생성된 배열/컬렉션 선언과 `expect/actual` 중복 포함 |
| 이전 JVM Kotlin 공개 바이너리 추출 | **860개** | 기존 선별 관련 JVM 클래스에서 추출. JVM 문법/이름은 Kotlin Source Callable과 일대일 대응하지 않음 |

기존 보고서의 Java 3,186개·Kotlin 1,672개는 **이전 선별 범위**의 수치다. 이번 6,103개·4,493개는 확장된 정적 원장이지, 그 숫자만으로 Java/Kotlin의 모든 문자 관련 Source Callable을 의미까지 완전 검증했다는 증거가 아니다. Java AST는 현재 컨테이너의 JDK 21 파서를 이용했으므로 JDK 27 소스의 일부 Preview 문법에서 진단을 기록했다. Kotlin 2.4.21 컴파일러 자체의 Symbol Resolution 검증이나 JDK 27 `jmod` 바이너리 교차검증도 아직 수행되지 않았다. **이 미완료 증거를 완료로 둔갑시키지 않는다.**

### 0.2 본문에서 사용하는 분류

| 기호 | 뜻 | 법적 함의 |
|---|---|---|
| **C** | 버전 비의존 또는 정확히 결정되는 공통 Character/Text 계산법 후보 | 선택 후 ADR-0080에서 독립 의미·피연산자·결과·비성공을 확립 |
| **U** | 고정 Unicode 데이터/규칙에 의존하는 공통 계산법 후보 | Unicode 18.0.0 등 정확한 의미적 기준 필요 |
| **H** | Java/Kotlin 정확한 Source-bound 의미 | Host API 전체를 공통 계산으로 1:1 승격하지 않음 |
| **T** | 상태·Callback·외부 환경·예외·자원·순서가 관여 | 해석은 필수, 1D 적격성은 별도 판정 |
| **D** | 별도 1D/도메인의 최종 Judgment·해석 | 공통 계산은 보조만 함 |
| **B** | Compiler Product 또는 JVM Target의 표현 책임 | Contract Text의 값 동등성과 구별 |

## 1. Java/Kotlin 표준 기능의 실제 해석 범위 — 누락 방지 마스터 맵

**Java SE 27 실제 표면.** `java.lang.String`, `Character`, `CharSequence`, `StringBuilder`, `StringBuffer`, `Appendable`, `StringConcatFactory` 및 Source `+`; `java.util.regex.Pattern`, `Matcher`, `MatchResult`와 관련 예외; `java.text.Normalizer`, `BreakIterator`, `Bidi`, `Collator`, `RuleBasedCollator`, `CharacterIterator`, `StringCharacterIterator`, `AttributedString`; `java.nio.charset.Charset`, `StandardCharsets`, Encoder/Decoder/CoderResult/ErrorAction; `CharBuffer`·`ByteBuffer`; `java.util.Formatter`, `Formattable`, `StringJoiner`, `Scanner`, `StringTokenizer`, `HexFormat`, `Base64`, `Locale`, `ResourceBundle`, `Properties`, `stream.Collectors.joining`; `java.io.Reader/Writer`, `StringReader/StringWriter`, Line I/O; `java.text.Format`·`MessageFormat`·`ListFormat`·Number/Date Formatting; XML/URI 관련 인접 모듈은 **도메인 경계**로 분류한다. 이름이 같은 메서드라도 선언 클래스와 오버로드를 고유하게 식별한다.

**Kotlin/JVM 2.4.21 실제 표면.** Kotlin 기본 `Char`, `String`, `CharSequence`, `StringBuilder`의 Kotlin/Java 타입 매핑; `kotlin.text`의 멤버·Extension·nullable receiver·반복·검색·비교·치환·Split/Trim·Lines·Regex·Format·Char 분류/숫자 변환·Hex/UTF 함수; `kotlin.io.encoding.Base64`; `CharArray`/`ByteArray`/`UByteArray`, `String`/`CharSequence` 및 Sequence·Iterable의 함수형/Generated 확장; `kotlin.io`의 Reader/Writer/파일 처리; Kotlin의 `operator +`, 문자열 Template, `toString`, 기본 인수, Lambda 및 인라인 호출, JDK API 직접 호출.

다음처럼 **메서드명만 보고 합치면 실제 오류를 낳는 경계**가 존재한다.

| 경계 | Java/JDK | Kotlin/JVM | 공통 계약과의 관계 |
|---|---|---|---|
| Character | `char` UTF-16 Code Unit; `Character` API의 `int` Code Point 오버로드 | `Char` UTF-16 Code Unit; JVM Java 호출 매핑 | Kontrakt Character는 Unicode Scalar. Surrogate에 대한 값/실패 구별 필수 |
| Text length | `String.length()` UTF-16 단위 | `String.length` UTF-16 단위 | 공통 `ScalarCount`와 별개 |
| 일부 `codePoint*` | 고립 Surrogate도 `int`로 반환 가능 | Java API 직접 호출 가능 | Code Point 결과가 반드시 Scalar인 것은 아님 |
| Split | `String.split(String)`의 정규식 및 기본 limit 0 | `split(vararg Char/String)`은 literal delimiter, `split(Regex)` 별도 | 구문/중첩/우선순위/후행 빈 결과가 다름 |
| Whitespace | `trim`: 코드 단위 `<= U+0020`, `strip`: `Character.isWhitespace` | `Char.isWhitespace`: JVM 구현은 `isWhitespace` 또는 `isSpaceChar` | Unicode 18 `White_Space`와 동치 아님 |
| Case | `toLowerCase()` 기본 Locale; `equalsIgnoreCase`와 `equalsFoldCase` 구별 | `lowercase()` 기본 `Locale.ROOT`; `ignoreCase` 별도 | Host 데이터 버전과 Unicode 18을 분리 |
| Regex | Pattern과 상태ful Matcher | Kotlin Regex, JVM에서는 Pattern/Matcher 사용하지만 래퍼의 결과·limit/옵션 별도 | 독립 Pattern 의미/Work Law 필요 |
| String construction | `StringBuilder`, mutable `CharSequence`, `String.valueOf(Object)` | Kotlin Builder 및 `buildString`, Lambda 변환 | 순수 Text 계산으로 사용자 효과를 지우면 안 됨 |
| Encoding | `getBytes`, `CharsetDecoder`, `encodedLength(Charset)` | `toByteArray`, `decodeToString`, Base64/Hex | Strict/Lossy, Charset Provider, 오버로드 구별 |

**결정:** Source Resolution은 (언어, 버전, 선언 소유자, 오버로드, receiver, 인자 변환, 평가 순서, 기본값, 인라인/콜백, 결과 타입)을 확정한다. 그다음 그 호출을 C/U/H/T/D/B로 매핑해야 한다. 단순 메서드 이름 문자열 비교나 JVM 메서드명 검색은 부적합하다.

## 2. 공통 계산 계약 후보 전체 표

아래 표는 **공통법에 올릴 실제 후보**를 정리한 것이다. `필수`는 V1에서 당장 구현됐다는 뜻이 아니라, Source와 1D/Compiler 계산의 수요를 종합하면 **법칙의 의미를 결정해야 한다는 우선 제안**이다. Java/Kotlin Callable의 해석 Coverage는 이 표 밖에서도 반드시 유지된다.

| 번호 | 계산 의미 후보 | 구분 | 수요 | 권고 |
|---|---|---|---|---|
| C01 | Unicode Scalar 유효성 검사, Code Point→Scalar 전환 | C | 모든 Host/Contract 값 경계 | 필수 |
| C02 | Scalar Sequence의 정확한 Count | C | Input 길이, Admission, 내부 IR | 필수 |
| C03 | Scalar Index/Element Access | C | 분류·정확한 순회 | 필수 |
| C04 | Scalar Slice, End-exclusive Range | C | 부분값·조합·검색 | 필수 |
| C05 | Scalar Concatenate | C | Source 계산·정규화 합성 | 필수 |
| C06 | Scalar Repeat | C | JVM/Kotlin repeat·고정 조합 | 필수 후보 |
| C07 | Scalar Exact Equality | C | 기존 ADR-0064/0080 값법 | **이미 확립된 의미 재사용** |
| C08 | Scalar Lexicographic Order | C | 순서·선택·분석 | 필수 후보 |
| C09 | Scalar Literal Search, first/last/from-position | C | `indexOf`, containment, 구간 | 필수 |
| C10 | Exact Prefix/Suffix/Containment | C-파생 | Condition/Admission | C09/C07에서 파생 가능 |
| C11 | Scalar Region Equality, 비교 범위 | C-파생 | regionMatches, 부분 비교 | 검토 |
| C12 | Exact Literal Replace (비중첩·왼쪽부터 등 결정) | C | Canonicalization/Source | 필수 |
| C13 | Literal Delimiter Split (결과 경계/빈값 정책 명시) | C | Kotlin/Java 일부 API | 필수 |
| C14 | Text Join with **Text-only** inputs | C | 집합·문자열 결합 | 필수 후보 |
| C15 | Exact Predicate-Set Trim/Strip | C | W5/W6/W18 카탈로그 | 필수 |
| C16 | ASCII/Unicode Whitespace Run Collapse | C-파생 | Catalog, NormalizeSpace | 검토 |
| C17 | Pad/Truncate/Fixed-size Construction | C-파생 | SDK/API, 진단 | 검토 |
| C18 | Exact line-separator scan and split | C | Source/Formatting/Diagnostics | 필수 후보 |
| C19 | UTF-16 Code Unit length/index/slice | H/C 보조 | Java/Kotlin Host semantics | **일반 Scalar law와 명확히 분리** |
| C20 | UTF-16↔Scalar Strict Validation/Conversion | C | String/Char 경계 | 필수 |
| C21 | UTF-8 Strict Encode/Decode | C | Source/IR/Artifact | 필수 |
| C22 | UTF-16 Strict Encode/Decode | C | Host 연결 | 필수 |
| C23 | UTF-32 Strict Encode/Decode | C | Unicode 데이터/외부 포맷 | 후순위 후보 |
| C24 | Encoded byte/code-unit count, offset correspondence | C/B | Source/Backend length | 필수 범위 선정 |
| C25 | Hex Encode/Decode with explicit variant | C | Java/Kotlin 2.4 실제 SDK | 필수 후보 |
| C26 | Base64 Encode/Decode with explicit variant | C | JVM/Kotlin 실제 SDK | 필수 후보 |
| C27 | Fixed grammar Escape/Unescape | C 또는 D | Literal, Java/JSON 등 | **문법별로 구별** |
| U01 | Unicode 18 General_Category 및 파생 Property | U | Character, Lexer, Admission | 필수 |
| U02 | Unicode 18 Script/Script_Extensions | U | Identifier, Catalog | 필수 후보 |
| U03 | Unicode 18 White_Space / Pattern_White_Space 등 | U | Strip, Pattern, Catalog | 필수 |
| U04 | Unicode 18 Nd/Decimal Digit Value | U | Catalog Decimal Fold | 필수 |
| U05 | Unicode 18 Simple Case Mapping | U | Char/Object 비교 | 필수 후보 |
| U06 | Unicode 18 Full Case Mapping, 조건부/Locale 별도 | U | Text 변환 | 필수 후보 |
| U07 | Unicode 18 Default Full Case Folding | U | Catalog Case Fold | 필수 |
| U08 | Unicode 18 NFC/NFD/NFKC/NFKD | U | Catalog 4 Law | 필수 |
| U09 | Unicode 18 Normalize Check/Quick Check | U | 적합성·최적화 | 필수 후보 |
| U10 | Unicode 18 NFC Case Fold / NFKC Case Fold | U-합성 | Catalog 2 Law | 필수 |
| U11 | Unicode 18 XID_Start/XID_Continue, ID* | U | Frontend material | 후보 (정책은 D) |
| U12 | Unicode 18 Emoji/Variation/Default_Ignorable 등 | U | Character/Regex/진단 | 수요별 범위 결정 |
| U13 | UAX #29 Extended Grapheme Boundaries | U | Regex/Diagnostics | 검토 우선 |
| U14 | UAX #29 Word/Sentence Boundaries | U | Regex/진단 | 후순위, 명확한 수요 조건 |
| U15 | UAX #14 Line Break Opportunities | U | 화면 배치 | 후순위 |
| U16 | UTS #39 Confusable Skeleton 재료 | U/D | Identifier Security | 정책과 구별, 후순위 |
| U17 | BiDi Property/Algorithm Material | U/D | Diagnostics | 필요한 최소 결과 먼저 |

**직접 공통화하지 않을 것:** Java/Kotlin 전체 Regex Engine, Java의 모든 Charset Provider, Locale Collation, Format의 동적 사용자 객체 변환, Builder 상태, 사용자 Lambda 실행, Object.toString, JVM Interning, Symbol Identity, Path Resolution, Canonicalization `E_L/C_L`, Admission Accept/Reject, Numeric/Temporal/URI 도메인 Parsing. 공통 계산이 재사용되더라도 판단권은 각 소유자에게 남는다.

## 3. 묶음별 상세 조사·최종 제안

### 묶음 0 — SDK Callable 해석의 완전성: 모든 묶음에 적용하는 선행 의무

**실제 인터페이스:** JDK 27 `String`, `Character`, `Pattern`, `Charset`, `java.text`, Kotlin 2.4.21 `kotlin.text`의 공개 선언뿐 아니라 Source Language의 `+`, 인덱싱, Template, Extension, `get`, 호출의 Default Argument, lambda/inline, Java Interop, 중복/상속/bridge를 포함한다.

**누락이 발생하는 구체적 이유:** JDK `StringBuilder`의 일부 메서드는 `AbstractStringBuilder`에서 상속되거나 오버라이드되고, Kotlin/JVM은 Java `String` 멤버를 타입 매핑을 통해 노출한다. `source.jar`에 이름이 보인다는 이유만으로 실제 Kotlin Source에서 호출 가능한 선언이 아니며, 반대로 Kotlin inline extension은 대응되는 별도 JVM Method가 없는 경우도 있다. Java Source의 `a + b`는 일반 `String.concat` 호출이 아니고, Java의 `StringConcatFactory` 같은 Target 메커니즘으로 컴파일될 수 있다.

**해석 책임:**

- `language`, `languageVersion`, `sdkVersion`, `jvmTarget`, module/classpath, overload owner, extension receiver, visibility, deprecated/hidden/opt-in을 식별한다.
- 모든 인자의 평가 순서·Null 처리·타입 변환·클로저의 도달성·effect를 보존한다.
- Unicode 데이터, Charset/Locale, 정규식 Flag, mutable state, 객체 변환을 의미적 입력으로 취급하는지 판단한다.
- **API를 이해함(Recognized)**, **Source 선택이 유효함(Resolved)**, **1D가 허용함(Admissible)**, **구현됨(Implemented)**, **적합성 테스트됨(Conformant)**을 구별한다.
- 1D에서 불허되는 정확한 Source 호출을 `unknown method`처럼 오인하지 않는다. 반대로 정상적인 Source 메서드를 컴파일러가 몰라 거부하는 경우에는 Coverage 결함으로 분류한다.

**공통 계산으로 승격:** 이 묶음 자체에서 새 Character Law를 만들지 않는다. 나머지 묶음의 C/U Operation과 H/T/D 관계를 결정하는 **전수 감사 의무**다.

**다루어야 할 오버로드 형태:** `String.valueOf`의 8종 이상의 입력 변형, `String.join(CharSequence, Iterable)`/vararg, `String.format` 정적/Locale 인수, Kotlin `split` delimiter/Regex/Pattern 오버로드, Kotlin `String?.isNullOrBlank`, Nullable Extension, Kotlin/JVM의 `CharArray.concatToString` 등. **정확한 목록은 부속 TSV에서 소스·서명 단위 확인**한다.

### 묶음 1 — 값의 Domain: Scalar, Code Point, UTF-16, CharSequence와 Nullable

**SDK 근거:** `Character.isValidCodePoint`, `isSurrogate`, `isHighSurrogate`, `isLowSurrogate`, `isSurrogatePair`, `toCodePoint`, `toChars`, `charCount`, `codePointAt/Before/Count`, `offsetByCodePoints`, `String.charAt`, `CharSequence.charAt`; Kotlin `Char.code`, `isSurrogate`, `CharSequence`와 Java mapped `String`, `CharArray`, `StringBuilder`.

**결정해야 할 정확한 차이:**

- Kontrakt Character = **Unicode Scalar**. Java/Kotlin `char/Char` = UTF-16 단위. `Character.isValidCodePoint(0xD800)`은 범위 판정의 의미상 `true`가 될 수 있지만 Kontrakt Character는 될 수 없다.
- UTF-16에 고립 Surrogate가 있는 Host `String`은 Source 값으로 존재 가능하다. 이를 손실 없이 구간 추출·검색·비교할 수 있어도 Contract Text 값으로 확립할 수 있다는 뜻은 아니다.
- Empty Text와 `null`, Unassigned Scalar, Noncharacter, Private Use, Default Ignorable, Grapheme Cluster, UTF-8/UTF-16 바이트·단위는 각각 다른 차원이다.
- `String`의 불변성과 Mutable `CharSequence`의 관찰 시점·Alias·Escape는 다르다. Snapshot의 적법한 경우를 따로 판단한다.

**공통법:** C01/C20. Source-bound UTF-16 중간값 H. `null`은 Contract Text의 Empty로 치환 금지. **Unassigned Scalar를 Unicode 데이터 버전이 바뀌었다고 법적 Text에서 배제하지 않는다.**

**반례:** `"😀".length() == 2`, Scalar Count는 1. `"😀".substring(0, 1)`은 고립 Surrogate를 보유한 Java String이다. 이 값을 Unicode Scalar Text로 조용히 `U+FFFD`로 바꾸면 계약의 값 의미가 손상된다.

### 묶음 2 — 길이, 인덱싱, 구간, 결합과 반복

**Java 실물:** `String.length`, `charAt`, `codePointAt`, `codePointCount`, `offsetByCodePoints`, `substring`, `subSequence`, `getChars`, `concat`, `repeat`, `StringBuilder.appendCodePoint` 및 인덱스 메서드. Java 27 `String.encodedLength(Charset)`는 묶음 9의 Byte Length와 연결된다.

**Kotlin 실물:** `length`, `get(Int)`, `substring(start,end)`와 `IntRange`, `slice(IntRange|Iterable<Int>)`, `subSequence`, `drop`, `take`, `takeLast`, `dropLast`, `chunked`, `windowed`, `repeat`, `plus`, `StringBuilder` 관련 확장.

**정확한 계산 조건:**

1. `count`, `index`, `slice`에서 단위가 Scalar·UTF-16·Byte·Grapheme 중 무엇인지 반드시 명시.
2. End-exclusive 구간과 Kotlin `IntRange`의 포함 끝점, 빈 Range, 음수 인덱스, 범위 초과, 출발점이 끝보다 큰 경우의 결과/예외 구분.
3. JDK의 `codePointCount`는 Invalid Surrogate를 각각 코드포인트 비슷한 단위로 세는 구간 동작이 가능하다. Scalar Count로 치환 금지.
4. Repetition의 0회·음수·길이 Overflow·메모리 부족을 구분. Resource Exhaustion이 임의의 1D Rejection을 낳을 수 없음.
5. `String + Object`, `StringBuilder.append(Object)`는 `toString()`·평가 효과를 수반할 수 있으므로 **Text-Text 순수 Concat**만 C로 직접 닫는다.

**공통법:** C02–C06, C19. `substring`의 Host 변종은 H로 유지. `StringBuilder`와 Lambda는 T. 1D가 허용한 유한 계산만 결과로 이용.

### 묶음 3 — Equality, Comparison, Region, Search, Prefix/Suffix

**Java 실물:** `String.equals`, `contentEquals`, `compareTo`, `regionMatches`, `equalsIgnoreCase`, `compareToIgnoreCase`, **`equalsFoldCase`/`compareToFoldCase`/`UNICODE_CASEFOLD_ORDER`**, `indexOf`(char/int/String/fromIndex/begin-end 변종), `lastIndexOf`, `startsWith`, `endsWith`, `contains`. Java `char`/`int` 검색은 Supplementary Code Point와 Surrogate 처리 법칙을 구별한다.

**Kotlin 실물:** `equals(ignoreCase)`, `compareTo(ignoreCase)`, `contentEquals`, `regionMatches`, `contains`, `indexOf`, `lastIndexOf`, `indexOfAny`, `lastIndexOfAny`, `findAnyOf`, `findLastAnyOf`, `startsWith`, `endsWith`, `commonPrefixWith`, `commonSuffixWith`, `String?.equals`, nullable receiver.

**정확한 계산 조건:**

- Exact Equality는 ADR-0080 §3.1–§3.2의 Scalar Sequence 값 동등성을 유지한다. UTF-16 단위 순서는 Scalar 순서와 Supplementary Code Point 부근에서 다를 수 있다.
- Literal Search의 빈 Needle, 첫/마지막 일치, 시작 위치가 음수/길이 초과, 중첩, 발견 실패 결과, Index Unit을 완전히 정한다.
- Java `equalsIgnoreCase`의 단일문자 식 비교와 `equalsFoldCase`의 **1:M Unicode Full Fold**를 동일시하면 안 된다 (`ß` 등).
- JDK 27 `Character` 데이터 17.0 vs Kontrakt U18 및 Locale Collation에 따른 결과 차이를 남긴다.
- `CharSequence`가 Mutable이거나 Custom Implementation이면 `contentEquals`를 순수 Text 비교로 치환할 수 있는지 추가 조건이 필요하다.

**공통법:** C07–C11, U07과 일부 연계. Source의 Host IgnoreCase/CaseFold는 H로 정확한 버전 의미를 보존. `Collator`는 D/T.

### 묶음 4 — Replace, Split, Join, Trim, Pad, Lines, Indent, Translate

**Java 실물:** `String.replace(char,char)`, `replace(CharSequence,CharSequence)`, `replaceAll`, `replaceFirst`, `split`, `splitWithDelimiters`, `join`, `trim`, `strip/Leading/Trailing`, `lines`, `indent`, `stripIndent`, `translateEscapes`, `StringJoiner`, `StringTokenizer`.

**Kotlin 실물:** `replace/replaceFirst/replaceRange/replaceAfter/replaceBefore/replaceIndent`, `removePrefix/Suffix/Surrounding`, `split(Char|String|Regex|Pattern)`, `splitToSequence`, `joinToString`, `trim/trimStart/trimEnd`, `trimIndent/Margin`, `padStart/End`, `lines`, `lineSequence`, `prependIndent`, `replaceIndentByMargin`, `trim(predicate)`.

**의미를 달리하는 핵심 오버로드:**

- Java `"a,".split(",")` → 후행 빈 문자열을 제거하는 기본 정규식 분할. Kotlin `"a,".split(',')` → 끝의 빈 문자열을 보존. Java `Pattern.split`/`splitWithDelimiters`와 Kotlin `Regex.split`의 Limit 및 제로 길이 Match도 따로 분석.
- Kotlin literal split에서 여러 구분자가 같은 위치에서 일치하면 구분자 인수 순서가 결과를 결정할 수 있다.
- Java `trim` (`<= U+0020`), Java `strip` (`Character.isWhitespace`), Kotlin `trim` (JVM Char.isWhitespace/SpaceChar), Unicode `White_Space`는 **네 종류의 다른 기준**이다.
- Java `String.lines`는 JDK 규정의 CR/LF/CRLF 처리이며 UAX #14의 Line Break Opportunities와 다르다. 마지막 빈 줄·빈 입력의 결과도 정해야 한다.
- `String.translateEscapes`는 Java Text Block escape 규칙이며 JSON/HTML/URI escape와 다르다.
- `joinToString`의 `transform`, Java `join`의 `toString()` 변환 등은 콜백·객체 평가 의미가 관여한다.

**공통법:** C12–C18 중 순수한 문자 계산만 C로 승격. Regex Replace는 묶음 8; Context Formatting은 묶음 10; Kotlin predicate Trim은 T; Catalog의 W5/W6/W18은 묶음 7과 연결.

### 묶음 5 — Unicode Character Properties, Digits, Scripts, Emoji, Identifier

**Java 실물:** `Character.getType`, `getDirectionality`, `getNumericValue`, `digit`, `forDigit`, `isDigit`, `isLetter`, `isAlphabetic`, `isWhitespace`, `isSpaceChar`, `isJavaIdentifierStart/Part`, `isUnicodeIdentifierStart/Part`, `isIdentifierIgnorable`, `UnicodeBlock/Script`, `getName`, `codePointOf`, `isEmoji`, `isEmojiPresentation`, `isEmojiModifier`, `isEmojiModifierBase`, `isEmojiComponent`, `isExtendedPictographic`, code-point vs char 오버로드.

**Kotlin 실물:** `Char.isDigit`, `isLetter`, `isLetterOrDigit`, `isWhitespace`, `isUpperCase/LowerCase/TitleCase`, `category`, `directionality`, `digitToInt/OrNull`, `isSurrogate`, JVM `Character` 의존 구현, `CharCategory`, `CharDirectionality`.

**정확한 법칙:**

- Unicode 18의 `General_Category`, `White_Space`, `Pattern_White_Space`, `Script/Script_Extensions`, `Nd`, `Decimal_Digit_Value`, `XID_Start/Continue`, `Default_Ignorable_Code_Point`, `Emoji` 관련 Property는 필요한 정확한 UCD 데이터로 고정한다.
- Java/JDK 27 Character의 문서상 **Unicode 17.0** 기반 결과를 U18 Property Query로 대체하지 않는다. 그 호출을 해석하고, 가능한 경우 버전 17 호환 계산으로 매핑해야 한다.
- Java `Character.getNumericValue`는 `Nd` Decimal Digit Value만 반환하는 함수가 아니다. 다른 숫자 문자 및 비정수/미정 결과를 구별한다.
- `Character.isValidCodePoint`는 Scalar Validity가 아니다. `Character.toUpperCase(char)`와 `toUpperCase(int)`의 반환 및 Supplementary 처리를 구분한다.
- UAX #31의 ID/XID Property와 Java `isJavaIdentifierStart/Part`, Kotlin Lexer 식별자 허용 법칙을 동일시하면 안 된다.
- Unicode Property는 데이터 계산일 뿐, 소스에 해당 문자를 허용할지·입력으로 승인할지는 Frontend/Input/Admission의 판단이다.

**공통법:** U01–U04, U11–U12. 표준 데이터 U18은 정확한 Semantic Profile로 잠근다. Host JDK 17 Property 호환은 **별도 H** 계산. Identifier Permission은 D.

### 묶음 6 — Case Mapping, Full Folding, IgnoreCase, Locale

**Java 실물:** `Character.toUpperCase/toLowerCase/toTitleCase`(char/int), `String.toUpperCase/LowerCase`(Locale 또는 default), `equalsIgnoreCase/compareToIgnoreCase`, JDK 26~의 `equalsFoldCase/compareToFoldCase/UNICODE_CASEFOLD_ORDER`.

**Kotlin 실물:** `Char.lowercase()/uppercase()/titlecase()`(Text 반환 가능), `lowercaseChar/uppercaseChar/titlecaseChar`, `String.lowercase()/uppercase()`(Locale.ROOT와 Locale 인수), `ignoreCase` 비교·검색·치환.

**정확한 법칙:**

- Simple Mapping과 Full Mapping은 결과 길이가 다를 수 있다. U+00DF `ß`와 Context-sensitive Greek Sigma를 검토한다.
- Locale.ROOT·명시 Locale·시스템 기본 Locale을 서로 다른 입력으로 취급한다. Java default `toLowerCase()`를 Kotlin default `lowercase()`로 치환하면 터키어 `I` 등에서 값이 바뀔 수 있다.
- Case Folding은 Case Mapping과 다르며 `NFKC_Casefold`도 별도 변환이다. JVM `equalsFoldCase`의 데이터 버전과 U18 기준은 달라질 수 있다.
- Kotlin `Char.uppercase()`는 문자 하나를 Text로 확대할 수 있지만 `uppercaseChar()`는 Character 결과다. Kotlin `ignoreCase=true`도 문자열 전체 Full Fold와 동일하다고 가정 금지.

**공통법:** U05/U06/U07/U10. Locale-dependent Host 호출은 H/T, 사용 가능한 값 결정이 닫히면 명시적 Locale 매개 계산은 별도 후보. JDK 17 동작을 U18로 자동 정규화 금지.

### 묶음 7 — Normalization 및 기존 Canonicalization Catalog의 합성법

**Java 실물:** `java.text.Normalizer.normalize/isNormalized`, Java `Pattern.CANON_EQ`, `String.equalsFoldCase` 연계. Kotlin stdlib는 Normalization 전체를 동일한 독립 API로 제공하지 않으므로 Java Interop/선택 라이브러리 경로를 별도 해석한다.

**기존 Catalog 실제 수요:** Unicode 18 기준 `Initial` 11개 중 (1) Whitespace Leading/Trailing/Boundary Trim 3개, (2) Unicode Decimal Digit Fold 1개, (3) NFC/NFD/NFKC/NFKD 4개, (4) Default Full Case Fold, NFC Case Fold, NFKC Case Fold 3개. 각각의 `E_L`, `C_L`, Coverage 및 Exact Version은 ADR-0066/0076에 남는다.

**정확한 계산과 판단 분리:**

- U18 NFC/NFD/NFKC/NFKD와 Normalize Check는 공통 계산으로 정의 가능하다. Normalization은 Unicode 18 UAX #15에 고정하고, 데이터 버전·입력 도메인·비성공을 명시한다.
- `NFC(a + b)`와 `NFC(a) + NFC(b)`는 일반적으로 같지 않다. 경계에 조합할 수 있는 Scalar가 있으면 `Concat`과 `NFC`의 순서를 바꾼 최적화가 틀릴 수 있다.
- NFKC Case Fold는 `Default Full Fold`와 같지 않으며 `NFKC_CF` 등 규범적 데이터·Default Ignorable 처리 의미가 필요하다.
- Canonicalization Law는 공통 계산의 출력을 사용해도 해당 Law가 선택한 동등성·대표값 판정이라는 사실이 변하지 않는다.

**공통법:** C15/U03/U04/U07–U10. Catalog의 11개 Law 자체를 일반 Text API로 복사하지 않으며, §8.4~§8.13의 ASCII·형식·도메인 Law들도 실제 계산 부품만 재사용.

### 묶음 8 — Java/Kotlin Regex, Matcher, Segmentation, Capture/Boundary

**Java 실물:** `Pattern.compile`, Flag (`UNIX_LINES`, `CASE_INSENSITIVE`, `COMMENTS`, `MULTILINE`, `LITERAL`, `DOTALL`, `UNICODE_CASE`, `CANON_EQ`, `UNICODE_CHARACTER_CLASS`), Character Classes, Unicode `\p`, `\X`, `\b{g}`, Anchors, Lookaround, Group/Backreference, Named Groups, Replacement `$n/${name}`, `Matcher.matches/lookingAt/find`, `group`, `start/end`, `region`, Anchoring/Transparent Bounds, `hitEnd/requireEnd`, `appendReplacement/Tail`, `results`, Split/Stream.

**Kotlin 실물:** `Regex`, `RegexOption`, `MatchResult`, `MatchGroup`, `MatchNamedGroupCollection`, `matches`, `containsMatchIn`, `matchAt`, `matchesAt`, `find/findAll`, `replace/replaceFirst`, `replace(transform)`, `split/splitToSequence`, `matchEntire`, `groups`/range/destructured, JVM Pattern 변환.

**핵심 위험:**

- Java `Matcher`는 상태ful이며 현재 Match/Region/Find 순서가 결과를 변경한다. Kotlin `Regex`는 그 엔진을 쓰더라도 API의 List/Sequence 구성, Limit, `RegexOption` 매핑, Match Result가 다르다.
- Java Regex의 정규식 문법에는 UTF-16 인덱스, Grapheme `\X`, Unicode Categories, Zero-width Match, Lookbehind, Backreference, replacement-template 의미가 얽힌다.
- 모든 Java Pattern을 임의 입력으로 실행할 수 있다는 선언은 Admission §7.7의 **지원되는 Work Law**를 충족하지 않는다. Backtracking worst-case가 예측 불가능할 수 있다. **무조건 금지가 아니라 정확한 유한 의미/예산·작업법으로 입증되는 subset**을 별도 규격으로 결정한다.
- `CANON_EQ`/Unicode case class는 정확한 JDK Profile의 의미를 보존. Unicode 18 공통 Property/Normalization과 자동 동일시하지 않는다.
- UAX #29 Grapheme/Word/Sentence Boundaries, UAX #14 Line Break는 서로 다른 계산. Regex Match Boundary와 Diagnostic 표시 경계를 혼합하지 않는다.

**공통법:** Literal Search(C09)와 U13(명확히 필요한 Grapheme Boundaries)을 독립적으로 정의한다. Regex Engine 및 Source Pattern API 전체는 **H/T + 선택된 Pattern Work Law**로 유지한다. Capture와 Match Iterator의 결과 구조는 IR·Collection/1D 영역과 연결.

### 묶음 9 — UTF, Charset, Encoder/Decoder, BOM, Byte Length, Offset

**Java 실물:** `String.getBytes` 다양한 변형, 생성자 `String(byte[], Charset/charsetName/off/len)` 계열, `String.encodedLength(Charset)`(JDK 27), `Charset.forName/defaultCharset/availableCharsets`, `CharsetDecoder/Encoder`의 `onMalformedInput`, `onUnmappableCharacter`, `replaceWith`, Stateful `decode/encode`, `CoderResult`, `StandardCharsets`, `CharBuffer/ByteBuffer`, `Reader/Writer`와 IO Encoding.

**Kotlin 실물:** `String.toByteArray`, `ByteArray.decodeToString`, `String.encodeToByteArray`, `Charsets`, `kotlin.text` 변환, `kotlin.io` Reader/Writer, `kotlin.io.encoding.Base64`(묶음 10).

**정확한 법칙:**

1. Strict Decode와 malformed/unmappable replacement, `REPORT`/`REPLACE`/`IGNORE`는 서로 다른 계산이다. 손상된 바이트를 `U+FFFD`로 바꿨다는 사실을 원래 텍스트 값과 혼동하면 안 된다.
2. UTF-8, UTF-16, UTF-32의 BOM/endian, UTF-16 Surrogate 입력 적격성, Byte Index와 Scalar/UTF-16 인덱스 간 대응을 명시한다.
3. Charset의 Default·Provider·Aliases·Stateful Encoding은 선택된 Runtime 환경 입력에 의존할 수 있다. 모든 Charset을 공통 순수 UTF 규격으로 승격하지 않는다.
4. `String.encodedLength(Charset)`는 해당 Charset Encoder의 기본 오류 처리까지 반영하는 Host 의미가 있으며 **Strict UTF-8 Byte Count가 아니다**.
5. JVM Classfile Constant Pool의 Modified UTF-8 및 65,535 Byte 제한, 다른 Target 파일/Artifact의 Encoding은 **B**로 분리한다.

**공통법:** C20–C24, 필요 시 Strict UTF-32. Charset Host API는 H/T. 인코딩·Source Offset은 SourceManager/Backend의 소유권을 변경하지 않는다.

### 묶음 10 — Hex, Base64, Escape, Formatting, Numeric/Temporal/URI Text

**Java 실물:** `HexFormat`(prefix/suffix/delimiter, upper/lower, ByteBuffer, parse/format), `Base64`(Basic/URL/MIME, Padding/line separator, Stream), `String.format/formatted/valueOf`, `Formatter`, `Formattable`, `StringJoiner`, `Scanner`, `StringTokenizer`, `MessageFormat`, `ListFormat`, `NumberFormat/DecimalFormat`, `DateFormat`, `java.net.URI/URL` 및 XML 관련.

**Kotlin 실물:** `HexFormat`, `hexToByteArray`, `toHexString`, `kotlin.io.encoding.Base64`의 variant, `String.format`, `toIntOrNull`·`toBigDecimal` 등 문자→도메인 변환, `toString` Numeric Formatting.

**정확한 법칙:**

- Hex: 각 Byte의 두 Hex Digit, Uppercase, 공백/구분자, prefix/suffix, 구문 오류, 홀수 Nibble 등. Java/Kotlin API의 서로 다른 형식 옵션은 H로 보존.
- Base64: Basic/URL-safe/MIME, Padding 허용/금지, 잘못된 문자, CRLF, Trailing Bytes, Byte String vs Text 결과를 구분.
- Escape: Java Source Escape, Java Text Block, JSON, XML, HTML, URL percent encoding은 상호 대체 불가. 특정 Grammar를 채택한 경우에만 정확한 C 또는 D.
- Formatter: `%s` 등의 사용자 `toString()`·`Formattable.formatTo`, `%t` 날짜, Locale·숫자 Formatting은 단순 Text Template이 아니다. Source API를 이해하되 모든 임의의 포맷터 실행을 1D에 허용하지 않는다.
- Numeric/Temporal/URI/XML의 Grammar, Error, Domain Value는 각각 ADR-0078/0082 등 해당 소유자가 결정. 공통 Character Scan과 결과의 Judgment를 구별한다.

**공통법:** C25–C27을 한정된 결정 가능한 규격으로 검토. 모든 Formatter/Scanner/MessageFormat을 새 Text Law로 올리지 않는다. XML 및 URL은 도메인 Candidate로 명시적으로 분리.

### 묶음 11 — Mutable Builder, Buffer, Stream, Iterable, Callback

**Java 실물:** `StringBuilder`, `StringBuffer`, `AbstractStringBuilder`, `Appendable`, `CharBuffer`, `Buffer.position/limit/mark`, `StringJoiner`, `IntStream` from `String.chars/codePoints`, `Collectors.joining`, `Stream.map/filter/reduce`, Writer/Reader mutable context.

**Kotlin 실물:** Builder 확장 `appendLine`, `appendRange`, `append`, `deleteRange`, `set`, `buildString`; String·CharSequence·CharArray의 `map/filter/mapNotNull/fold/reduce/groupBy/chunked/windowed`, Sequence/Iterable/Iterator/Array 변환 및 generated inline extension.

**정확한 법칙:**

- Builder의 Alias, 상태 전이, local mutation, snapshot, escape, `reverse`의 Surrogate 처리, `setLength`로 생기는 NUL Code Unit을 보존한다.
- Input은 immutable; Admission §7.4는 **닫힌 유한 Binder에서만** 임시 가변 축적을 허용할 수 있다. Builder가 존재한다는 사실로 Input Mutation을 허용하지 않는다.
- Lambda의 Closed Body, Invocation Cardinality, 순서, Capture, 예외, Effect가 닫히지 않으면 단순 유한 순수 Text 계산으로 처리할 수 없다.
- Lazy Sequence와 Eager List, Java parallel Stream, Kotlin Sequence의 평가 순서·재평가 가능성은 서로 다르다. 계약적 의미만 확정하고 버퍼·알고리즘은 구현 선택으로 남긴다.
- `String.chars()`는 UTF-16 단위 stream, `codePoints()`는 Code Point stream이다. 둘 다 임의로 Unicode Scalar Collection으로 승격하지 않는다.

**공통법:** 변환 결과 중 정확하고 닫힌 C만 공유. 기존 Collection Law, Binder, Host effects T는 유지. Lambda 자체를 공통 Character/Text Predicate 객체로 승격하지 않는다.

### 묶음 12 — Compiler 내부, Diagnostic, Locale/I/O/Target Artifact

**SDK 및 구현 관찰:** JDK `java.io.*`, `Reader/Writer`, `Scanner`, `ResourceBundle`, `Locale`, `BreakIterator`, `Collator`, `Bidi`, java.desktop AWT/font text 객체; `StringConcatFactory`, `java.lang.classfile` 일부 Target API; Kotlin 파일·Console·Reader/Writer 및 Kotlin Source Compiler의 템플릿/문자 escape.

**Kontrakt의 독립 수요:** Lexing/Unicode Escape Decode, Literal `String/Char` 형성, Source Byte→UTF-16/Scalar Line/Column 대응, Identifier Spelling 검사, Literal Constant Folding, Diagnostic Escape/Truncate/Grapheme Safe Slicing, Path Display·Filename, Deterministic Serialized Bytes·HID/Cache Input, Java/Kotlin Backend/ConstantPool Classfile MUTF-8, Verification/Optimization의 결과 동등성.

**소유자/함정:**

- Source Identifier Equality vs Text Equality vs Symbol Identity vs OS Path Lookup vs HID/Cache Product Equality는 다르다.
- Source `+`에서 객체 변환이 미치는 영향과 JVM `invokedynamic StringConcatFactory` recipe는 서로 다른 계층이다. Backend가 Java 평가 효과를 지우면 안 된다.
- Line/Column, Display Width, Grapheme Clipping, BiDi 순서, Font/Layout와 Terminal Width는 동일한 계약이 아니다.
- Diagnostic Renderer가 UTF-16 단위로 `substring`을 자르면 고립 Surrogate를 만들 수 있다. 표시 계층의 안전성 문제이지 Input Text의 의미를 바꿀 사유가 아니다.
- Source Reader와 Default Charset·Locale·FileSystem/ResourceBundle은 환경 의존 가능. JVM Classfile MUTF-8은 일반 UTF-8과 다르다.

**공통법:** C02–C05/C09/C18/C20–C24, U01–U03, U13의 필요한 계산 재사용. Symbol/Path/HID/Artifact/Target Law는 D/B, 효과·환경은 T.

### 묶음 13 — V1 외부 라이브러리 선별 및 확장

이번 업로드에는 서드파티 JAR이 없다. 따라서 아래 기능은 **공식 API 자료에 근거한 후보**이지 실물 버전별 전수 Coverage를 검증한 지원 확정 목록이 아니다. **외부 라이브러리는 표준 SDK와 달리 V1 일부 Callable을 선별**한다.

| 우선 | 라이브러리·표면 | 선별할 실제 기능 | 공통화 시 주의 |
|---|---|---|---|
| V1 우선 | Apache Commons Lang `StringUtils`, `Strings.CS/CI`, `RegExUtils` | Null-safe blank/empty, trim/strip, substring, compare, search, padding, split/join, replace, distance/difference | `null` 반환, ASCII/Whitespace, Deprecated→신 API, `equalsIgnoreCase` 정확한 기준 |
| V1 우선 | Apache Commons Text `StringEscapeUtils` | Java/JSON/XML/HTML escape/unescape | 서로 다른 Grammar와 잘못된 escape 실패/복구 조건 |
| V1 우선 | Guava `CharMatcher`/`Splitter`/`Joiner`/`Strings`/`Ascii`/`CaseFormat` | UTF-16 char Matcher, literal/pattern Split, limit, omitEmpty, trimResults, join null 정책, camel-hyphen 변환 | Guava Matcher는 Unicode Scalar Predicate가 아니라 주로 Java `char` 기반; 객체 `toString` 효과 주의 |
| V1 우선 | ICU4J `UCharacter`/`Normalizer2`/`UnicodeSet`/`BreakIterator`의 제한된 값 계산 | Unicode 18 Property/Normalize/CaseFold/Boundary | **ICU 내부 계산 엔진**과 **사용자 ICU4J API**는 다른 범위; 버전별 데이터 차이 |
| 나중 | ICU4J Collator/Transliterator 등 | Locale Collation, 복잡한 변환 | 명시 Locale/룰·상태/버전·비용 규격 없이는 V1 일반 Pure Text로 승격하지 않음 |
| 나중 | RE2/J 또는 기타 Regex | 안정적인 정규식 엔진에 대응 가능한 subset | Java Regex 전체와 호환되지 않을 수 있음. 새 라이브러리·정규식 grammar 적격성은 별도 증명 |

**외부 라이브러리 지원의 추가 조건:** 정확한 Maven artifact/version, 공개 Callable·오버로드, 실제 의존선, Java/Kotlin Binding, 비교·Null·효과·Regex·Locale/Unicode Profile, 구현 reference tests, V1 허용/진단. 서드파티 기능이 추가되더라도 기존 공통 계산 법칙의 의미를 바꾸지 않는다.

### 3.13.1 외부 라이브러리 지원을 무작정 확장하지 않을 이유

- **Commons Lang 3**는 구 API `StringUtils`와 이후 권장되는 `Strings.CS`/`Strings.CI` 및 `RegExUtils`가 병존한다. Deprecated 메서드를 발견했다고 버리면 기존 사용자 Source를 해석하지 못하고, 모두 별도 계산법으로 승격하면 중복이 생긴다. V1에 채택된 *해당 버전의 Callable*은 모두 해석하되 본질적으로 같은 계산은 공유한다.
- **Commons Text**에서 `StringEscapeUtils`는 고정 Escape Grammar의 계산 후보가 되지만, `StringSubstitutor`의 다양한 Lookup/치환은 Dictionary, 외부 자원·상태 및 순환 재귀·기본값을 요구할 수 있다. 자동적으로 일반 Text Pure Operation으로 승인하면 안 된다. `StringTokenizer`, `TextStringBuilder`, `WordUtils`, `similarity` 패키지는 유용하지만 V1 전체 지원의 근거가 아직 부족하다.
- **Guava Splitter**는 `on(char)`, `on(String)`, `on(Pattern)`, `on(CharMatcher)`, `fixedLength`가 서로 다른 해석 경로이고 `omitEmptyStrings`, `trimResults`, `limit`, `withKeyValueSeparator`가 출력 구조와 Domain을 바꾼다. `Joiner.useForNull`/`skipNulls`, `MapJoiner`, `CharMatcher`의 UTF-16 Unit 의미, `CaseFormat`의 ASCII 전제를 독립적으로 검토해야 한다.
- **ICU4J 79.1**은 Unicode 18 데이터 기반 공통 계산을 구현할 수 있는 후보지만 ICU Collation/Transliteration/MessageFormat/Locale 자료를 전부 Kontrakt Unicode 18 공통법으로 승인한 것은 아니다. 사용자 작성 Java/Kotlin이 `Normalizer2` 등을 직접 호출할 때의 API 해석과 컴파일러 내부에서 라이브러리를 쓰는 것은 별개다.

공식 근거: https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html ; https://javadoc.io/static/org.apache.commons/commons-text/1.14.0/org/apache/commons/text/package-summary.html ; https://github.com/google/guava/wiki/StringsExplained ; https://unicode-org.github.io/icu/download/ . **외부 JAR을 제공받지 않은 상태**이므로 메서드·오버로드별 실물 지원 여부를 승인했다고 표시하지 않는다.

### 3.13.2 기본 SDK에서 흔히 빠지는 API·언어 구문 검출표

| 누락 위험 | 왜 SDK 이름 탐색만으로 발견하기 어려운가 | 처리 위치 |
|---|---|---|
| `String.valueOf(Object)` 및 `String + Any` | JVM `toString`/Null 변환과 Kotlin Template·언어 연산자 의미 | 묶음 0/2/10/12 |
| `StringConcatFactory` | Java Source의 concat가 SDK 메서드 호출이 아닐 수 있음 | 묶음 0/12 |
| `String.intern` | String Pool Identity가 Text Value Equality와 다름 | 묶음 3/12 |
| `String.transform(Function)` | 임의 Lambda에 의존하므로 순수 Text 변환이 아님 | 묶음 0/11 |
| `Matcher.results` / `Pattern.splitAsStream` | Lazy 또는 Stream 연산과 Match State의 조합 | 묶음 8/11 |
| `Pattern`의 `\X`와 `\b{g}` | UTF-16 Regex에 Grapheme 의미가 섞임 | 묶음 8 |
| `String.lines` / `indent` / `stripIndent` | 줄의 종결·공백·최종 빈줄이 일반 Split과 다름 | 묶음 4/12 |
| `java.text.ListFormat` | Locale을 이용한 자연어 목록 포맷이며 일반 Join과 다름 | 묶음 10/12 |
| `String.encodedLength(Charset)` | JDK 27 추가 API. Encoder의 Replacement 경계 | 묶음 9 |
| Kotlin `String?.isNullOrBlank` | Nullable receiver 확장 함수 | 묶음 0/4 |
| Kotlin `Regex.replace(transform)` | Lambda/MatchResult·Evaluation/Callback | 묶음 8/11 |
| Kotlin `buildString`/`appendLine` | Java Builder만 읽으면 Kotlin Extension 누락 | 묶음 0/11 |
| Kotlin `String.split(Pattern)` | Java Pattern Wrapper이고 Java Pattern.split과 반환·limit 다름 | 묶음 0/8 |
| Kotlin generated `_Strings.kt`, `_Arrays.kt`, `_Sequences.kt` | 수작업한 `kotlin.text` 파일 외부에 inline·iterator API 존재 | 묶음 0/11 |
| Kotlin `HexFormat`, `Base64` | 기본 문자열 파일 밖의 Codec API | 묶음 9/10 |
| `java.text.Collator`, `java.awt.font.TextLayout` | Text Sorting과 Glyph/Layout의 의미 구분 필요 | 묶음 12 |
| Standard `java.nio.charset.Charset` Provider | 기본 Charset의 실행 환경·Stateful Decoder | 묶음 9/12 |
| Java character property의 `char`와 `int` | 같은 함수명에 Unicode Supplementary 처리 차이 | 묶음 1/5 |


## 4. API 기능군별 최종 분류와 법칙 소유자

| 기능군 | SDK 해석 의무 | 공통법(C/U) | 최종 소유자 / 불허 조건 |
|---|---|---|---|
| `char`/`Char`·Surrogate | Java/Kotlin **모든 관련 오버로드** | Scalar 유효성·UTF-16 전환 | Source H, Contract Text 형성은 관련 Law |
| Length·Count·Slice | 모든 오버로드·Range/Index 단위 | Scalar/Host-unit 계산 분리 | Input 범위는 Input, Compiler resource는 별도 |
| Compare/Search | literal/region/ignoreCase/Full Fold·Null | Exact Scalar Search/Order, Fold U18 | Host IgnoreCase는 H, Collation D |
| Replace/Split/Join | literal vs Regex, delimiter priority, trailing empties, callback | Literal Replace/Split/Join | Regex Pattern Law, Object Conversion T |
| Trim/Whitespace | JDK `trim/strip`, Kotlin `trim`, predicate trim | Exact-set Trim, Unicode Properties | Catalog의 정확한 E_L/C_L는 1D |
| Case | Simple/Full/Locale/default, JDK 27 Fold API | U18 Mapping/Fold | JDK17·Locale Host 의미 H |
| Normalize | Java Normalizer/Pattern.CANON_EQ | NFC etc U18 | Canonicalization E_L/C_L 독립 |
| Regex/Boundary | Pattern/Matcher/Kotlin Regex 전부 | Literal Search·UAX#29 최소 계산 | Matching Grammar/Work Law 별도; Callback/State T |
| Charset/UTF | Encoding 옵션·Provider·Malformed | Strict UTF·Byte Count·Mapping | IO/Target B, Lossy Host H |
| Hex/Base64 | JDK/Kotlin 표준 변형 | 정확한 variant별 Codec 후보 | File/Network Domain 별도 |
| Formatting | Formatter, `toString`, Format/Scan | 구성된 Text-only 연산 | Numeric/Temporal/Locale/동적 객체 T/D |
| Builder/Functional | Builder와 array/sequence/lambda | 순수 파생 결과만 | Admission finite binder, state/effects T |
| Source/Diagnostics/IR | Escape, Source Coordinates, Presentation, Cache | 계산 재사용 | Identity/Compiler Product/Backend B |
| 외부 라이브러리 | V1 선별 Callable | 기존 계산 재사용 | Version, Profile, 1D legality 독립 |

## 5. 사용자에게 오류를 발생시키지 않기 위한 검증 규격

표준 SDK의 메서드를 원장에 적었다는 사실과, 실제 사용자의 Kotlin/Java 소스 코드를 올바르게 컴파일할 수 있다는 사실은 다르다. **컴파일러 오류 예방은 '지원'을 여러 단계로 검증해야 가능**하다.

| 확인 단계 | 반드시 남길 증거 | 실패 시 의미 |
|---|---|---|
| **V0 Source Inventory** | SDK Source/ABI/Module/Visibility/버전 및 각 오버로드 위치 | 공개 API 누락·중복·상속 오류 |
| **V1 Language Binding** | 동일 Source의 javac 27 / Kotlin Compiler 2.4.21 resolved symbols | Member/Extension/기본 인수/Type inference 오해 |
| **V2 Semantic Interpretation** | 정확한 피연산자·결과·Exception/Effect/Locale/Version/Mutation/Callback | Source 의미 오해 |
| **V3 Common Law Mapping** | C/U 의미를 사용할 때 실제 Host와 동등한 조건; 아니면 H 보존 | 무리한 자동 변환 |
| **V4 1D Admissibility** | Input·Admission 등 원래 법칙의 지원 가능/불허 사유 | Contract Policy와 Coverage 혼동 |
| **V5 Realization / Optimizer** | Constant Folding·IR lowering·JVM backend 의미 보존 | 합법적인 Source 변형 오류 |
| **V6 Conformance and Diagnostics** | 독립 SDK reference, 경계값·실패·회귀 테스트, 정확한 사용자 진단 | 정상 입력을 'unknown'으로 거부하거나 잘못 승인 |

### 5.1 최소한 반드시 테스트할 반례 집합

아래 표는 테스트 후보이자 **Source Interpreter의 규범적 구분점**이다. 정확한 Expected 값이 확정되기 전에는 아무 문자열이든 자르는 임시 구현으로 채워서는 안 된다.

| 반례 | 관찰 차이 및 검증 방향 |
|---|---|
| `"😀".length()` vs Scalar Count | UTF-16=2 / Scalar=1 |
| `"😀".substring(0,1)` | 고립 High Surrogate를 Host String에서 유지하는가 |
| `Character.isValidCodePoint(0xD800)` | Valid Code Point vs Invalid Scalar |
| `Character.codePointAt("\uD800",0)` | 고립 Surrogate 결과의 비-Scalar 취급 |
| `"a,".split(",")` Java / Kotlin `split(',')` | 후행 빈 원소와 Regex/Literal 차이 |
| `"abc".split("")` | Zero-width Match, 시작/끝 빈 항목·limit 차이 |
| Kotlin `split("ab", "a")` 구분자 순서 반대 | 같은 위치에서 Prefix 중첩될 때 우선순위 |
| `"\u2003".trim()` Java vs `strip()` vs Kotlin `trim()` | 세 가지 공백 Predicate 차이 |
| `"\u00A0"` trim/strip | NO-BREAK SPACE 등 `White_Space` 분류 차이 |
| `"i".toUpperCase(Locale.forLanguageTag("tr"))` | 명시 Locale vs 기본 Locale/ROOT |
| `"ß".equalsIgnoreCase("ss")` vs `equalsFoldCase` | Simple IgnoreCase와 Full Case Fold의 차이 |
| Kotlin `Char.uppercase()` vs `uppercaseChar()` | One-to-many mapping 가능성과 반환 도메인 |
| `NFC("e"+"\u0301")` vs `NFC("e")+NFC("\u0301")` | 정규화/결합 교환 불가 |
| `StringBuilder.reverse()`와 Surrogate Pair | UTF-16 Builder reverse 실제 Host semantics |
| `StringBuilder.setLength(oldLen+1)` | 삽입되는 NUL Code Unit 및 mutable 상태 |
| `String.chars()` vs `codePoints()` | 단위/결과 개수·Surrogate 처리 |
| `Pattern`의 `CANON_EQ`, Unicode flags | U18 common normalization과 같다고 자동 주장 금지 |
| Regex Zero-width find 반복·`Matcher.region` | Match State, Bounds, Termination |
| `Regex.replace(transform)` | Lambda 평가·호출 순서와 Effects |
| Malformed UTF-8 `decodeToString(throwOnInvalidSequence=...)` | Strict vs Replacement |
| JDK `String.encodedLength(Charset)` + 고립 Surrogate | Charset 오류/대체 처리와 Strict Byte Count 구별 |
| Java/Kotlin Hex Parsing에서 홀수 Digits | 각 오버로드의 실패/수용 조건 |
| Base64 MIME vs URL variant, Padding 없음 | 종류별 해석 차이 |
| `String.format("%s", customObject)` | User `toString` 평가·효과 |
| Builder/Stream/Sequence의 mutable capture | Invocation law 및 Admission §7.4 적격성 |
| JVM ConstantPool `CONSTANT_Utf8_info` | Modified UTF-8와 일반 UTF-8 및 표현 한계 |
| Unicode 18에서 새로 할당된 Scalar의 `Character.isDefined` | Java 27의 Unicode 17 vs 공통 U18 Property 결과 |
| `String intern()` / Symbol Resolution / HID | String Equality, Symbol Identity, Compiler Product Equality를 구별 |

### 5.2 증거 원장 구성

이 보고서와 함께 생성한 ZIP에 다음을 포함했다.

| 파일 | 기록 목적 |
|---|---|
| `java27_declarations_audited.tsv` | JDK 소스에서 추출한 6,103개 고유 타입·필드·생성자·메서드 선언의 파일/줄/그룹/검증 상태 |
| `kotlin2421_declarations_audited.tsv` | Kotlin/JVM Source에서 추출한 4,493개 선언의 파일/줄/수신자/서명/주석/그룹 |
| `jdk27_all_source_paths_coverage.tsv` | JDK 소스 ZIP의 **14,914개 파일 경로** 전체와 선정/비선정 상태 |
| `kotlin2421_all_source_paths_coverage.tsv` | Kotlin `.kt` 파일 384개 경로 전체와 선정/비선정 상태 |
| `jdk27_java_types_and_method_names.md` | JDK 추출 공개 타입별 메서드명 및 오버로드 개수 인덱스 |
| `kotlin2421_source_files_and_declarations.md` | Kotlin 소스 파일별 추출 이름 인덱스 |
| `audit_metadata.json` | 입력 SHA-256, 결과 개수, Coverage 한계 |
| `kotlin2421_all_source_declaration_screen.tsv` | Kotlin 384개 전체 파일의 9,284개 정적 선언 후보 스크리닝. Java/Kotlin Source Binding 입증 전의 참고 원장 |

**해석:** `OUTSIDE_CURATED_CHARACTER_SURFACE`, `NOT_SELECTED_CORE_ADJACENT`와 같은 파일 상태는 해당 소스에 Character/Text 관련 공개 API가 절대 존재하지 않는다는 증명이 아니다. 실제로 `JDK`에는 매우 넓은 도메인 API가 `String`을 매개변수로 사용한다. 이번 목표는 Java의 모든 `String`을 인자로 받는 API를 무작정 Text 법칙으로 올리는 것이 아니라 **기본 문자 처리 라이브러리와 그것을 해석할 때 필요한 정확한 언어·외부 경계**를 닫는 것이다.

### 5.3 Kotlin 384개 전체 소스 파일 추가 재검색

표준 문자 패키지와 생성 확장 함수만 추출하는 원장의 누락 가능성을 줄이기 위해 **업로드된 Kotlin 소스 JAR의 `.kt` 384개 파일 전부**를 재검색했다. 이때 정적 패턴으로 포착된 선언 후보는 **9,284건 / 354개 파일**이다. 원래의 4,493개 선별 원장과 달리 이 숫자에는 일반 컬렉션, 표준 언어 타입, Coroutine/Time/UUID, `toString()` 등 문자 계산과 관계없는 선언이 대량 포함돼 있다. 형식적 AST가 아니므로 그 숫자를 곧장 정확한 외부 API 수로 사용하지 않는다.

추가 검토에서 별도 경계로 확인한 주요 위치는 다음과 같다.

| 추가 발견 위치 | 구체적 기능 | 판단 |
|---|---|---|
| `jvmMain/jdk7/kotlin/io/path/PathReadWrite.kt` | `Path.readText`, `writeText`, `appendText`, `forEachLine`, `readLines` | Character/Charset 계산을 사용하지만 **Path·파일 I/O** Authority 및 Environment는 별도 |
| `commonMain/kotlin/uuid/Uuid.kt` | `Uuid.parse`, `parseHex`, `toHexString`, `toHexDashString` | 공통 Hex 계산을 재사용하되 **UUID Grammar**는 도메인 소유 |
| `commonMain/kotlin/time/Duration.kt`, `Instant.kt` | `parse`, `parseOrNull`, `toString`, ISO 표현 | **시간 도메인(ADR-0082)** 소유. 일반 Text Parser로 승격 금지 |
| `jvmMain/kotlin/util/BigDecimals.kt` | `toBigDecimal` 및 관련 Numeric 변환 | **Numeric Domain(ADR-0078)** 소유 |
| `commonMain/generated/_OneToManyTitlecaseMappings.kt` | One-to-many Titlecase 계산용 생성 데이터 | 호출 가능한 API 원장과 **내부 구현 자료**를 구별 |
| `commonMain/generated/_Comparisons.kt`, `_Ranges.kt` | 비교·Range와 `Char` 관련 Generated API | Text Operation이 아니어도 **Source Binding / 인덱스 계산**에서 의미 필요 |
| `commonMain/kotlin/Any.kt`, `jvmMain/kotlin/Any.kt` | `toString` | `String + Any`와 문자열 Template의 객체 변환 경계 |
| `jvmMain/kotlin/util/Exceptions.kt` | `stackTraceToString` | Error/Diagnostic 출력, 사용자 상태·정보 관찰과 분리 |

이 전수 재검색의 선언 후보 전체는 `kotlin2421_all_source_declaration_screen.tsv`에 소스 경로·줄·서명·정적 분류를 남겼다. 특정 형식의 패턴 탐색은 공개 API의 완전성을 증명하지 못하므로, **정식 Kotlin 2.4.21 컴파일러의 심벌 해석과 실제 ABI 대조는 별개의 Conformance Gate**로 유지한다.


## 6. ADR-0080으로 옮길 수 있는 확정적 결론과 남겨둘 구현 과제

**프로젝트 법칙에서 이미 확정된 점:** Character=Unicode Scalar, Text=정확한 Scalar Sequence, Empty·Unassigned Scalar·Noncharacter 허용, exact equality는 정규화/Casefold를 하지 않음, Java/Kotlin Source 의미가 공통 계약의 법적 의미를 덮어쓰지 않음, 각 1D와 Establishment·Canonicalization·Compiler Product의 Authority 유지, 전역 Text 길이 상한 금지.

**이번 조사의 설계 제안:** (1) 전체 표준 SDK Callable 인식과 1D 사용 가능성을 분리한다. (2) 공통법 후보 C01–C27, U01–U17을 검토 대상으로 채택한다. (3) Unicode 18 데이터 기반 연산은 독립 Version/Reference로, Java 27 `Character`의 Unicode 17 의미는 Host 전용 해석으로 남긴다. (4) Regex·mutable/callback·Locale/Charset·Formatting은 정확한 Source 의미를 보존하면서 적법성이 입증되는 경우만 사용한다. (5) 외부 라이브러리는 V1 선별 지원하고 추가 버전과 Callable은 별도 Coverage로 확장한다.

**아직 완료됐다고 말해서는 안 되는 기술 검증:** JDK 27 JMOD 공개 API와 AST 추출의 1:1 비교, 실제 javac 27과 Kotlin 2.4.21의 Symbol Resolution 대량 테스트, 각 오버로드의 Golden Output 실행 검증, 모든 외부 JAR의 실물 선언 수집, 선택된 1D별 Authoring 합법성 실증, 모든 공통 계산 후보의 승인 및 구현. 이 작업은 문서에 기능을 나열했다는 사실만으로 자동 해결되지 않는다. **SDK 전체 지원을 선언하는 품질 게이트는 이 증거들이 통과한 뒤에만 충족한다.**

## 7. 권고하는 실제 결정 문서의 배치

| ADR-0080 영역 | 이 조사에서 공급할 결정 자료 | 책임 경계 |
|---|---|---|
| §3.3/§5.6 | Java 27·Kotlin 2.4.21 표준 Text API의 완전한 Source 해석 의무, 지원 Profile별 Coverage | API 목록·오버로드 데이터는 Callable/Frontend Catalog Design |
| §5.3 | C01–C18: 값·Count·Slice·Concat·Search·Literal Transform의 기본 계산과 경계 | Java/Kotlin Source 특유 차이는 H |
| §5.4 | U01–U17: Unicode 18 Property, Case Mapping/Folding, Normalization, Segmentation의 수요 및 독립 범위 | Canonicalization 11 Law의 E_L/C_L는 기존 소유자 |
| §5.5 | C19–C27: UTF/Byte/Offset, Hex/Base64 및 Escape의 범위 | Target/Artifact, Charset Provider, Formatting의 별도 주체 |
| §5.7–§5.9 | Operation Result의 유한성, State/Callback/Regex Work Law, Admission finite binder와 구현 자원 | No proxy-style runtime checks, No new Judgment Authority |
| §5.10–§5.13 | Source/IR/Optimizer/Diagnostics/Cache/Backend의 독립 수요 | Semantic refs·HID·Source Path 분리 |
| §5.14 | SDK 선언/해석/적격성/구현/적합성의 다단계 검증, 외부 라이브러리 추가 | 매 release 버전 차분, Golden Regression |

**추천:** 지금의 14개 묶음은 기능 조사 단위로 사용하되, 실제 ADR의 Contract Decision에는 **공통 계산 의미**만 넣고 방대한 Java/Kotlin 표준 Callable·오버로드 및 플랫폼 Profile은 별도의 **지원/검증 Catalog**에서 관리한다. Kotlin/Java Source 호환성을 위해 표준 메서드 수백 개를 ADR에 옮겨 적는 대신, 그 전체를 해석해 위 계산과 정확히 대응한다는 검증 의무를 둔다. 기존 결정을 지우거나 체크리스트를 임의로 체크할 근거는 아직 없다.

## 8. 외부 규범과 출처

- Java SE 27 — 업로드한 JDK 27 `src.zip`의 각 `java.base/...` 소스 및 원장 파일·행번호. 공식 Java API: https://docs.oracle.com/en/java/javase/27/docs/api/
- Kotlin/JVM 2.4.21 — 업로드한 `kotlin-stdlib-2.4.21-sources.jar`와 Binary JAR의 해당 함수/타입. 표준 API: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/ ; https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.io.encoding/ ; Java interop: https://kotlinlang.org/docs/java-interop.html
- Unicode 18.0.0 — 정식 규범의 코어·UCD·UAX: https://www.unicode.org/versions/Unicode18.0.0/ ; https://www.unicode.org/reports/tr44/ ; https://www.unicode.org/reports/tr15/ ; https://www.unicode.org/reports/tr29/ ; https://www.unicode.org/reports/tr31/ ; https://www.unicode.org/reports/tr14/ . 각 공통 U Law는 고정된 18.0.0판 규범 및 데이터에 따름.
- Java Regex/Charset: https://docs.oracle.com/en/java/javase/27/docs/api/java.base/java/util/regex/Pattern.html ; https://docs.oracle.com/en/java/javase/27/docs/api/java.base/java/nio/charset/CharsetDecoder.html
- Apache Commons Lang: https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html ; Commons Text: https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringEscapeUtils.html
- Guava: https://github.com/google/guava/wiki/StringsExplained ; ICU4J: https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/
- Kontrakt 법칙: Project ZIP `docs/adr/current/contract/...`, Unicode 18 개정 Catalog Design. Draft ADR-0080 최신 644줄 본문은 이번 업로드에 없고 기존 ZIP은 650줄이므로 **실제 ADR을 덮어쓰지 않음**.
