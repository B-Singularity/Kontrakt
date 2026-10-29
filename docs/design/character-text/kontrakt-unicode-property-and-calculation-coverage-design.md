# Unicode Property and Calculation Coverage Design

**Status:** Working Design — selected internal coverage; not an Accepted Contract decision or an
implementation-completion claim.

**Date:** 2026-10-29

## 1. Purpose and Decision Boundary

Kontrakt maintains its own Unicode calculation basis for Contract Frontend interpretation, lawful 1D use, compiler
analysis, verification, test synthesis, source security diagnostics, IDE/tooling, and V1/V2 compiler products. Its goal
is precise internal computation, **not** a public ICU-like Unicode SDK. The compiler may adopt LLVM-style,
official-data-based generation without treating LLVM or any host Unicode implementation as semantic authority.

This Design records **internal Unicode coverage**. ADR-0080 §3.4 selects the common observations and Case/Normalization
calculations and determines their meaning. Existing 1D Contracts retain applicability, judgment, and established
material. The separate SDK Interpretation Design owns per-callable source mapping. A future Unicode Realization Design
will own extraction, data structures, algorithms, interfaces, caching, and optimization.

The selected coverage does not make a Java/Kotlin callable eligible for 1D authoring, introduce a new HIR subject or IR
family, require one global Unicode service, or promise V1 execution support for every selected fact. Current Draft
ADR-0080 §3.4 defines the semantic basis; its remaining operation-selection questions must still be explicitly closed.

## 2. What Is Selected

| Coverage class                                  |  Count | Decision                                                                                                           |
|-------------------------------------------------|-------:|--------------------------------------------------------------------------------------------------------------------|
| Independent UCD scalar observations             | **54** | Selected as internal shared fact calculations under the Unicode 18.0.0 basis                                       |
| Independent security-profile observations       |  **2** | Selected separately under the version-matched UTS #39 profile                                                      |
| Algorithm-dependent UCD properties and mappings | **38** | Selected as internal normative material; independent observation and full-text operation selection are not implied |
| Not selected as independent observations        | **34** | No standalone query selected; contributory data may still be required by selected computations                     |

The 54 + 38 + 34 partition describes the **126 indexed non-Unihan UCD items tracked by this Design**. It is not a census
of all Unicode data: most Unihan properties, string properties, named sequences, CLDR, specialized source-script
datasets, and UTS security mappings fall outside that index. An item may be represented internally without an
independently callable query; the counts are **coverage bookkeeping**, not a public API size or performance commitment.

The four classes do not collapse into one operation type. In particular, a property on a scalar, a scalar-to-sequence
mapping, a result set of aliases, a derived test predicate, and a property or algorithm on an entire string retain
different operand/result laws.

### 2.1. Meaning of an independent observation

A selected UCD observation accepts a legal Unicode Scalar and yields exactly the result specified by Unicode 18.0.0,
including normative derivations, range-dependent defaults, and `@missing` rules. Unassigned, noncharacter, private-use,
default-ignorable and bidirectional-control scalars remain in the Character domain. No compiler-chosen fallback replaces
a defined `false`, `Unknown`, absent value, or empty result.

Preserve each result kind: Boolean, enumeration/catalog, script set, exact rational or absent numeric value, name/alias
record, and mapping result are not interchangeable. `Numeric_Value` is not a floating-point approximation.
`Script_Extensions` is a set, not an arbitrary first Script. Property observations never normalize Text, decide
identifier admissibility, deny Input, or establish a Security Judgment.

### 2.2. Algorithm-dependent material

The 38-item selection supplies official data for the Case and Normalization calculations selected by ADR-0080
§§3.4.4–3.4.5, as well as possible Segmentation and Bidi calculations. It does **not** grant independent observations
for those data items or approve the remaining whole-text algorithms. A selected algorithm must use the appropriate
Unicode 18 rules together with matching data; a property table alone does not define segmentation, normalization, bidi
display, or emoji sequence validity.

### 2.3. Source-specific behavior

Source interpretation uses the actual resolved language/SDK version and call semantics. JDK 27 `Character` data and
Kontrakt Unicode 18 observations are not interchangeable; `Character.isWhitespace`, Kotlin `Char.isWhitespace`, Unicode
`White_Space`, Java `equalsIgnoreCase`, and full Unicode folding can give different results. A source-compatibility
calculation is included only where it is needed for an actually supported callable and is never silently declared the
Unicode 18 common result.

## 3. Independent UCD Scalar Observations — 54 Selected

Each row establishes an **internal observation selection**, not an independent 1D Contract. The value shapes are
explanatory; the exact admissible values and absent/default semantics remain those of Unicode 18.0.0.

### 3.1. General scalar properties (14)

| Property                       | Result kind                          | Intended consumers                               |
|--------------------------------|--------------------------------------|--------------------------------------------------|
| `Name`                         | name or explicitly absent            | source escapes; diagnostics; test reporting      |
| `Name_Alias`                   | zero or more typed aliases           | source name lookup; diagnostics                  |
| `Block`                        | catalog block membership             | SDK block interpretation; character diagnostics  |
| `Age`                          | Unicode version or unassigned status | version-delta analysis; test generation          |
| `General_Category`             | enumerated category                  | character APIs; decimal folding; tests           |
| `Alphabetic`                   | binary                               | character APIs; source classification            |
| `Script`                       | catalog script value                 | identifier analysis; security diagnostics        |
| `Script_Extensions`            | set of script values                 | mixed-script analysis; segmentation dependencies |
| `White_Space`                  | binary                               | existing Unicode trim laws; test generation      |
| `Hangul_Syllable_Type`         | enumerated                           | Hangul and grapheme analysis                     |
| `Noncharacter_Code_Point`      | binary                               | precise source/encoding diagnostics              |
| `Default_Ignorable_Code_Point` | binary                               | folding dependencies; security diagnostics       |
| `Deprecated`                   | binary                               | source diagnostics; version tracking             |
| `Variation_Selector`           | binary                               | emoji/variation-sequence analysis                |

### 3.2. Case classification (5)

| Property         | Result kind | Intended consumers                          |
|------------------|-------------|---------------------------------------------|
| `Uppercase`      | binary      | source character classifiers; case analysis |
| `Lowercase`      | binary      | source character classifiers; case analysis |
| `Cased`          | binary      | context-sensitive case handling             |
| `Case_Ignorable` | binary      | context-sensitive case mapping              |
| `Soft_Dotted`    | binary      | context-sensitive case mapping              |

### 3.3. Numeric classification (4)

| Property          | Result kind                     | Intended consumers                              |
|-------------------|---------------------------------|-------------------------------------------------|
| `Numeric_Type`    | enumeration                     | digit distinction; source numeric APIs; tests   |
| `Numeric_Value`   | exact numeric value or no value | decimal-value derivation; source numeric APIs   |
| `Hex_Digit`       | binary                          | Unicode-aware hex classification and test cases |
| `ASCII_Hex_Digit` | binary                          | ASCII-only parsing and tests                    |

### 3.4. Identifier and lexical classification (8)

| Property                  | Result kind | Intended consumers                              |
|---------------------------|-------------|-------------------------------------------------|
| `ID_Start`                | binary      | Unicode identifier observation; source analysis |
| `ID_Continue`             | binary      | Unicode identifier observation; source analysis |
| `XID_Start`               | binary      | normalization-compatible identifier observation |
| `XID_Continue`            | binary      | normalization-compatible identifier observation |
| `Pattern_Syntax`          | binary      | language/pattern lexical analysis               |
| `Pattern_White_Space`     | binary      | language/pattern lexical analysis               |
| `ID_Compat_Math_Start`    | binary      | mathematical identifier profile candidates      |
| `ID_Compat_Math_Continue` | binary      | mathematical identifier profile candidates      |

### 3.5. Emoji classification (6)

| Property                | Result kind | Intended consumers                       |
|-------------------------|-------------|------------------------------------------|
| `Emoji`                 | binary      | host API interpretation; generated tests |
| `Emoji_Presentation`    | binary      | emoji presentation diagnostics           |
| `Emoji_Modifier`        | binary      | emoji modifier classification            |
| `Emoji_Modifier_Base`   | binary      | emoji modifier contexts                  |
| `Emoji_Component`       | binary      | emoji sequence analysis                  |
| `Extended_Pictographic` | binary      | grapheme rules; emoji analysis           |

### 3.6. Bidirectional classification (3)

| Property        | Result kind | Intended consumers                   |
|-----------------|-------------|--------------------------------------|
| `Bidi_Class`    | enumeration | source interpretation; bidi analysis |
| `Bidi_Control`  | binary      | source-security diagnostics          |
| `Bidi_Mirrored` | binary      | host APIs; bidi diagnostics          |

### 3.7. Other scalar classification and diagnostics (14)

| Property               | Result kind | Intended consumers                                    |
|------------------------|-------------|-------------------------------------------------------|
| `Math`                 | binary      | host character APIs; classification                   |
| `Quotation_Mark`       | binary      | source and diagnostic classification                  |
| `Dash`                 | binary      | punctuation diagnostics; test data                    |
| `Sentence_Terminal`    | binary      | sentence analysis; test data                          |
| `Terminal_Punctuation` | binary      | sentence/text classification                          |
| `Diacritic`            | binary      | character analysis; tests                             |
| `Extender`             | binary      | character analysis; tests                             |
| `Ideographic`          | binary      | host APIs; source classification                      |
| `Unified_Ideograph`    | binary      | CJK character analysis                                |
| `Grapheme_Base`        | binary      | grapheme-related analysis                             |
| `Grapheme_Extend`      | binary      | grapheme-related analysis                             |
| `Regional_Indicator`   | binary      | grapheme and flag sequence analysis                   |
| `Join_Control`         | binary      | identifier context diagnostics                        |
| `East_Asian_Width`     | enumeration | console/IDE presentation inputs; not terminal columns |

### 3.8. Derived decimal-digit calculation

**Select** an exact derived decimal-digit observation for scalar values classified as `General_Category=Nd`, yielding
the exact digit in 0–9 and an explicitly non-successful/absent outcome for other scalars as determined by its own final
law. This is a reusable computation derived from the selected UCD properties, **not** a 55th independent UCD Property
and not Java `Character.getNumericValue`. It is required by the existing Unicode Decimal-Digit Fold selection; its final
operand/result law remains in ADR-0080.

## 4. Algorithm-Dependent UCD Material — 38 Selected

### 4.1. Case mapping and folding (13)

| Property or mapping        | Internal calculation dependency            |
|----------------------------|--------------------------------------------|
| `Lowercase_Mapping`        | Default full lowercase mapping; may expand |
| `Uppercase_Mapping`        | Default full uppercase mapping; may expand |
| `Titlecase_Mapping`        | Default full titlecase mapping             |
| `Case_Folding`             | R4 default and explicit Turkic fold data   |
| `Simple_Lowercase_Mapping` | Single-Scalar lowercase result             |
| `Simple_Uppercase_Mapping` | Single-Scalar uppercase result             |
| `Simple_Titlecase_Mapping` | Single-Scalar titlecase result             |
| `Simple_Case_Folding`      | Single-Scalar fold result                  |
| `Changes_When_Lowercased`  | Derived change fact for lowercasing        |
| `Changes_When_Uppercased`  | Derived change fact for uppercasing        |
| `Changes_When_Titlecased`  | Derived change fact for titlecasing        |
| `Changes_When_Casefolded`  | Derived change fact for folding            |
| `Changes_When_Casemapped`  | Derived change fact across case mappings   |

### 4.2. Normalization (12)

| Property or mapping            | Internal calculation dependency                                |
|--------------------------------|----------------------------------------------------------------|
| `Canonical_Combining_Class`    | Canonical ordering during normalization                        |
| `Decomposition_Mapping`        | Canonical and compatibility decomposition                      |
| `Decomposition_Type`           | Distinguishes decomposition type                               |
| `Composition_Exclusion`        | Excludes defined compositions                                  |
| `Full_Composition_Exclusion`   | Complete composition-exclusion predicate                       |
| `NFC_Quick_Check`              | NFC implementation data only                                   |
| `NFD_Quick_Check`              | NFD implementation data only                                   |
| `NFKC_Quick_Check`             | NFKC implementation data only                                  |
| `NFKD_Quick_Check`             | NFKD implementation data only                                  |
| `NFKC_Casefold`                | Unicode R5 NFKC_CF mapping used by H18                         |
| `NFKC_Simple_Casefold`         | Additional internal mapping data; no common transform selected |
| `Changes_When_NFKC_Casefolded` | Derived change fact; not a standalone operation                |

### 4.3. Text segmentation (7)

| Property or mapping         | Internal calculation dependency                                        |
|-----------------------------|------------------------------------------------------------------------|
| `Line_Break`                | Versioned text boundary computation; relevant rule context is required |
| `Grapheme_Cluster_Break`    | Versioned text boundary computation; relevant rule context is required |
| `Word_Break`                | Versioned text boundary computation; relevant rule context is required |
| `Sentence_Break`            | Versioned text boundary computation; relevant rule context is required |
| `Indic_Conjunct_Break`      | Versioned text boundary computation; relevant rule context is required |
| `Indic_Positional_Category` | Versioned text boundary computation; relevant rule context is required |
| `Indic_Syllabic_Category`   | Versioned text boundary computation; relevant rule context is required |

### 4.4. Bidirectional mapping (3)

| Property or mapping        | Internal calculation dependency                                            |
|----------------------------|----------------------------------------------------------------------------|
| `Bidi_Mirroring_Glyph`     | Paired brackets or mirroring relation for bidi computation and diagnostics |
| `Bidi_Paired_Bracket`      | Paired brackets or mirroring relation for bidi computation and diagnostics |
| `Bidi_Paired_Bracket_Type` | Paired brackets or mirroring relation for bidi computation and diagnostics |

### 4.5. Joining and contextual shaping (3)

| Property or mapping            | Internal calculation dependency                    |
|--------------------------------|----------------------------------------------------|
| `Joining_Type`                 | Contextual joining or sequence-property derivation |
| `Joining_Group`                | Contextual joining or sequence-property derivation |
| `Prepended_Concatenation_Mark` | Contextual joining or sequence-property derivation |

For the selected Case and Normalization calculations, see ADR-0080 §§3.4.4–3.4.5. The four Quick Check Properties remain
**internal data**, not independent Scalar observations or String Quick Check operations. Other segmentation, full bidi,
and emoji-sequence operations await their own selection.

### 4.6. Selected Case and Normalization calculations

The following **whole-Text results** are selected under ADR-0080 §§3.4.4–3.4.5. They are not additional UCD Scalar
Properties and are not independent Canonicalization judgments.

| Calculation family           | Selected result                                                                 | Principal consumers                                                  |
|------------------------------|---------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Simple Case Mapping          | Lowercase, Uppercase, Titlecase (Scalar → Scalar)                               | Java/Kotlin Character interpretation; constant analysis              |
| Default Full Case Mapping    | Lowercase, Uppercase, Titlecase (Text → Text, contextual where specified)       | Source interpretation; Text analysis                                 |
| Explicit-locale Full Mapping | Only Unicode 18 `SpecialCasing.txt` locale conditions, with an explicit profile | Supported Host callable interpretation; conditional Text conversion  |
| Simple/Full Folding          | Default and Turkic profiles; exact Scalar or Text result                        | Source comparison interpretation; Contract and compiler calculations |
| Normalization                | NFC, NFD, NFKC, NFKD                                                            | Existing Canonicalization representatives; compiler analysis         |
| Default Full Case Fold       | `F18`                                                                           | Existing Default Full Case Fold representative                       |
| Canonical Caseless           | `Q18`                                                                           | Existing NFC Case Fold representative                                |
| Identifier Caseless          | `H18`                                                                           | Existing NFKC Case Fold representative                               |
| Exact normalized-state test  | `isNormalized` derived from transformation and Exact Equality                   | Source interpretation; diagnostics and analysis                      |

Unicode R5 is an internal dependency of `H18`. Unicode D146 is not separately selected. Quick Check is algorithm
material only. Stream-Safe Text is not selected. Caseless comparison and ordering, where needed, derive from the
selected transformations and exact comparison laws rather than new primitives.

## 5. Security-Profile Observations and String Calculations

| Selection               | Result                                                                                | Boundary                                                                         |
|-------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| `Identifier_Status`     | UTS #39-defined identifier status                                                     | Internal security fact, not automatic source or Contract rejection               |
| `Identifier_Type`       | UTS #39-defined set/classification of identifier types                                | Preserve multiplicity; no invented single-label conversion                       |
| Confusable mapping data | Selected as internal normative **algorithm material**, not a third scalar observation | Whole-string skeleton and mixed-script analysis require separate exact decisions |

Source-security diagnostics and Contract-Derived Security Audit may consume overlapping facts, but remain different
judgments. Neither may infer semantic identity from visual similarity; Latin and Cyrillic lookalikes remain different
Scalars. Original source spelling and safely escaped presentation must remain distinguishable from exact resolved
identity.

## 6. Not Selected for Independent Observation — 34 Items

### 6.1. Specialist character information (9)

`Logical_Order_Exception` `Modifier_Combining_Mark` `Vertical_Orientation` `Radical` `IDS_Unary_Operator`
`IDS_Binary_Operator` `IDS_Trinary_Operator` `Unicode_Radical_Stroke` `Equivalent_Unified_Ideograph`.

Specialist rendering, ideographic decomposition, historical writing-system data and layout details do not currently have
a demonstrated independent observation consumer. They can be added when a concrete source or compiler computation needs
them.

### 6.2. Egyptian hieroglyphic information (7)

`kEH_HG` `kEH_IFAO` `kEH_JSesh` `kEH_Cat` `kEH_Desc` `kEH_NoMirror` `kEH_NoRotate`.

The Egyptian-specific dataset is outside the general compiler Character/Text coverage and is not silently included
merely because it appears in the UCD index.

### 6.3. Obsolete or deprecated properties (9)

`FC_NFKC_Closure` `Expands_On_NFC` `Expands_On_NFD` `Expands_On_NFKC` `Expands_On_NFKD` `Hyphen` `Grapheme_Link`
`Unicode_1_Name` `ISO_Comment`.

Obsolete/deprecated properties are not selected as new common observations. A source SDK compatibility profile may still
require an exact historical behavior where such a callable genuinely exists.

### 6.4. Contributory data (9)

`Other_Alphabetic` `Other_Default_Ignorable_Code_Point` `Other_Grapheme_Extend` `Other_ID_Start` `Other_ID_Continue`
`Other_Lowercase` `Other_Math` `Other_Uppercase` `Jamo_Short_Name`.

Contributory data is **not discarded**: it is used where the official derivation of a selected property requires it. In
particular, `Other_Alphabetic` must not substitute for the complete `Alphabetic` result, and `Jamo_Short_Name` can
contribute to correct naming calculations.

The exclusion concerns independent observation only, not the legality of scalar values and not a prohibition on
internally consuming official data. The selection intentionally does not promise complete Unihan dictionary/reading
data, CLDR collation, general-purpose locale formatting, UnicodeSet pattern engines, font shaping, or every specialist
Unicode database.

## 7. Consumers Across V1 and V2

| Consumer                                          | Direct Unicode calculation                                                                         | What remains outside property selection                                                   |
|---------------------------------------------------|----------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| Contract Frontend / IDL                           | Identifier properties, escape/name data, decoding, exact source boundaries                         | Actual lexical grammar, source binding and diagnostic ownership                           |
| Java/Kotlin authoring interpretation              | Character classification, digits, case, normalization, regex classes and Unicode-sensitive APIs    | Resolved host callable, evaluation/effects, actual SDK Unicode profile, 1D eligibility    |
| Input / Canonicalization / Admission and other 1D | Whitespace, decimal digit, case and normalization facts where selected                             | Each 1D owns judgment, equivalence, representative, applicability and material            |
| Verifier / reference calculation                  | Exact selected observations, full-text transformations and negative examples                       | Verifier eligibility and legality under existing compiler laws                            |
| Realization / optimizer                           | Exact results for constant propagation and lawful rewrites                                         | Source effects, exceptions and whole-machine preservation                                 |
| Test synthesis / PBT / fuzzing                    | Category-driven boundary generation, absent/default property cases, fold/normalize counterexamples | Test objectives, expected judgment and generated-test product ownership                   |
| Source security diagnostics                       | Bidi, script sets, identifier status/type, confusable material                                     | Diagnostic policy, source/identity safety and security-audit judgment                     |
| Console / reports / IDE / LSP                     | Name, alias, grapheme boundaries, emoji, bidi, East Asian Width                                    | Renderer width, UTF-16/byte positions, output escaping, presentation-specific policy      |
| V2 semantic queries / incremental compiler        | Results of actual selected calculations as semantic dependencies                                   | Scheduling, cache keys, invalidation, change-frontier and product identity                |
| Persistent stores / filesystem artifacts          | Text/encoding and name/representation constraints where actually used                              | DB key equality, filesystem case/normalization, artifact publication, serialized identity |
| JVM backend / generated APIs                      | UTF-16, classfile Modified UTF-8 and constant materialization as needed                            | Target constraints and host operation qualification, not Character value restriction      |

A subsystem that merely stores or reuses a Unicode-derived product is usually an **indirect** consumer: persistence or
cache invalidation does not justify an additional scalar Property. Conversely test synthesis, security warnings and
diagnostic presentation may directly consume properties even when the authoring API does not.

## 8. V1/V2 Coverage and Evolution

The **selected semantic coverage** spans both V1 and the anticipated V2 system. A V1 feature is executable only when its
exact calculation is closed, its applicable source/1D use is qualified, and the relevant execution route exists. The
catalog is not a promise that every selected observation, Unicode algorithm, third-party API or SDK overload is
V1-enabled.

| Scope                    | Commitment                                                                                                                                                 |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| V1 semantic foundation   | Preserve ADR-0080 Character/Text and selected Unicode 18 observation results; support exact data required by actually enabled 1D laws and source callables |
| V1 compiler products     | Keep semantic Text distinct from source spelling, UTF-16 intermediate, symbol identity, diagnostic rendering, target bytes and result ownership            |
| V2 expansion             | Let test, security, IDE, query, incremental and persistent-storage products consume the same fixed results without redefining the underlying operation     |
| Future Unicode revisions | A new standard version cannot silently revise an existing law; update selected profiles explicitly and preserve dependent product validity                 |
| Future consumer          | Add observation or algorithm only on stated need and separate decision; do not infer all future runtime or SDK APIs are supported                          |

Exact Text Equality remains scalar-sequence equality. NFC equality, casefold equality, confusable relations, display
similarity, symbolic identity, filesystem name equality and database collation are separate relations; neither this
catalog nor V2 reuse fuses them.

## 9. Follow-On Decisions and Ownership

| Remaining decision                                                             | Owner                                                                                         |
|--------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Property result semantics and derived decimal-digit details                    | ADR-0080 §3.4; the derived decimal-digit result still requires its own complete operation law |
| Case mapping, folding, and normalization                                       | Selected by ADR-0080 §§3.4.4–3.4.5; this Design records their consumers                       |
| Segmentation, emoji sequences, full bidi, and confusable skeleton              | Remain separate ADR-0080 decisions or with their existing domain owner                        |
| Actual Java/Kotlin Callable mapping and supported source contexts              | Character/Text SDK Interpretation Design and owning 1D                                        |
| UCD acquisition, data generation, internal structures and computation strategy | Later Unicode Realization Design                                                              |
| Query granularity, persistent keys, dependency/repair policy, publication      | Compiler Product and V2 Engine Designs                                                        |
| Verification/conformance execution and performance measurement                 | Later implementation and release qualification, not this semantic-selection decision          |

This Design keeps the catalog as one maintained selection list. Other documents may consume it, but must not silently
maintain a second conflicting Property selection or turn a calculation result into Contract authority.