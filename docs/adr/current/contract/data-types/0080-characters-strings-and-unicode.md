# ADR-0080: Characters, Strings, and Unicode

**Status:** Draft

**Date:** 2026-10-25

## Related

The following documents remain authoritative within their own scope. References to a Design or Research document do not
turn its proposals into Accepted Contract law.

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0053: Version Contract
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, especially §7.9.1 and §7.9.5
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary
- ADR-0073: JVM Platform-Native Contract Ratification
- ADR-0075: Compiler Result Ownership and Legal Consumption
- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0078: Deterministic Numeric Value and Operation Semantics
- `docs/design/character-text/kontrakt-character-text-sdk-interpretation-and-calculation-design-en.md` — Working Design
  for Java/Kotlin callable interpretation.
- `docs/design/character-text/kontrakt-unicode-property-and-calculation-coverage-design.md` — Working Design for the
  selected Unicode coverage inventory and compiler consumers.
- `docs/design/canonicalization/built-in-law-catalog/canonicalization-built-in-law-catalog-design.md`, especially
  §8.5–§8.6 — existing admitted Canonicalization laws. Their `E_L`, `C_L`, Version, and Coverage remain with their
  owners.
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md` — Working Design, not an Accepted ADR
- `docs/todo/roadmap/kontrakt-v2-reference-architecture-and-v1-foundations.md` — Reference Architecture, not an Accepted
  ADR

---

## 1. Context

ADR-0078 establishes how common value and operation semantics can be defined without creating a 1D Contract or selecting
an implementation. ADR-0064 already defines Input `Text` Presentation Sameness. ADR-0066 and ADR-0076 retain the
authority of the selected Canonicalization Laws.

This ADR defines the meaning of common Character and Text calculations. Their meaning is the same wherever the compiler
uses them. Each 1D still determines whether and how to use a calculation in its own judgment.

## 2. Problem

The same text can appear as source spelling, a resolved value, or a JVM representation. These are not necessarily
interchangeable. Without exact operation laws, different compiler consumers can obtain different results from the same
purported calculation.

Kontrakt needs shared meaning without prescribing a Text engine. Sections 3.1–3.6 define the values and operations.
Sections 3.7–3.14 explain how they are composed and preserved across existing compiler boundaries. Selecting a semantic
law does not certify its V1 implementation.

## 3. Decision

### 3.1. Shared Meaning and Contract Ownership

Kontrakt defines a Character as one Unicode Scalar Value and Text as a finite sequence of Characters. A common operation
is defined by its accepted operands, successful result, and conditions for unsuccessful calculation. Where a Unicode
version or another profile determines the result, it is part of that operation's meaning. The same law and determining
inputs must give the same legal result, regardless of the compiler component or algorithm.

**Exact Text equality** compares the original Scalar sequence. It neither normalizes nor folds text. This agrees with
Input Presentation Sameness under ADR-0064. The finite Input presentation bound is not a global limit on other Text
calculations. Details of the value domain are in §3.2; the selected calculations begin in §3.3.

**Authority remains with each 1D.** A shared normalization result is not a Canonicalization judgment: the selected Law
still owns its `E_L`, `C_L`, Version, and established Material under ADR-0066 and ADR-0076. Other judgments retain their
existing owners under §3.8. The Catalog Design can inform this ADR, but its implementation choices and draft
descriptions cannot establish common-law meaning or transfer 1D authority.

**Source meaning is separate from common meaning.** Kontrakt may interpret a Text value produced elsewhere without
authorizing its producing operation inside a Contract. A Java/Kotlin callable retains its resolved Source meaning under
ADR-0073 and the qualification principles of ADR-0078. A Host `String` operation measured in UTF-16 Code Units does not
become a Scalar operation because it returns a string. §3.3 defines the interpretation boundary.

Compiler products may retain or reuse an exact Text result, but the result does not settle Symbol Identity or Compiler
Product equality. Their own laws govern legal observation and reuse under ADR-0071 and ADR-0075; see §3.12.

The common Text law imposes no physical storage format. Resource limits follow ADR-0051 and ADR-0052; unsuccessful
calculation and failure attribution follow ADR-0057. Implementations may change without changing the selected meaning. A
newer Unicode library is not permission to change a version-pinned result.

### 3.2. Character and Text Value Interpretation

A **Character** is a Unicode Scalar Value in U+0000–U+10FFFF, excluding U+D800–U+DFFF. A Unicode Code Point can be a
surrogate, so it is not necessarily a Character. A UTF-16 Code Unit and an octet are representation units, not
Characters.

**Text** is a finite ordered sequence of Characters. Empty Text is legal. Its Scalar values and their positions are
preserved exactly. U+0000 is a normal Character; a supplementary Scalar is still one Character. Combining Scalars do not
become one Character merely because they display as a single Grapheme. Segmentation is defined separately in §3.4.6.

Every Scalar is legal Text, including those without an assigned Unicode character name. Noncharacters and private-use
Scalars are not excluded. Neither default-ignorable characters nor bidirectional controls are removed by the value law.
Classification and source-language restrictions are separate decisions, governed by the relevant law.

A determined Text value cannot later change because its backing buffer or library state changes. This is a semantic
requirement, not a rule about when to materialize or copy it. A malformed Host representation is not common Text until a
lawful conversion succeeds (§3.5).

The Text domain has no global length ceiling. Empty Text is not absence. Malformed encoding and unsuccessful evaluation
do not implicitly yield Text values; their outcomes follow the applicable Failure law.

### 3.3. Exact Text Operations and Observable Results

Common Text calculations serve compiler analyses as well as 1D definitions. They do not constitute a new Java/Kotlin
string library.

For a supported Java/Kotlin authoring expression, the Contract Frontend must resolve the **actual callable** before
interpreting its meaning. This includes overload resolution and evaluation behavior that can affect a legal observation.
Kontrakt must not discover a Contract Definition by running user code or accepting the Boolean returned by an external
method. The receiving 1D determines whether that resolved calculation is lawful (§3.7).

For each declared supported Java/Kotlin SDK profile, the interpretation inventory must cover the full relevant standard
Character/Text callable surface. An unrecognized standard callable is a coverage deficiency, not proof that the
expression is unlawful. Third-party coverage may be selected separately. Recognition alone neither admits a callable
into every 1D nor qualifies its execution under ADR-0073. Actual callable qualification belongs to the SDK
Interpretation Design and the owning Frontend.

A source-bound intermediate value is not automatically common Text. For example, a Host substring can produce an
unpaired surrogate. The compiler can preserve that source meaning, but its conversion to common Text must satisfy §3.2
and §3.5. Likewise, a Java `String.length` result counts UTF-16 Code Units, not the Scalars counted by §3.3.1.

When the compiler itself selects a common Text calculation, its result follows the law below rather than whichever Host
API implements it. Operations that happen to return equal values are not interchangeable unless their required Source
observations are also preserved under §3.7.

#### 3.3.1. Text-Relative Scalar Positions and Count

The Scalar Count `|T|` is the number of Characters in Text `T`. Empty Text has count zero. UTF-16 Code Unit count and
Grapheme count are separate observations.

For `n = |T|`, both positions are zero-based and relative to that particular Text:

| Position                | Valid domain  | Meaning                                                    |
|-------------------------|---------------|------------------------------------------------------------|
| **Scalar Index** `i`    | `0 <= i < n`  | Addresses an existing Character.                           |
| **Scalar Boundary** `b` | `0 <= b <= n` | Addresses a position before, between, or after Characters. |

Boundary `n` is valid even for empty Text; it is not an element index. Equal numerical positions on different Text
values do not establish correspondence. These positions are not Source Coordinates, Input Nominal Coordinates, or HIR
Semantic References. Their meaning does not depend on physical indexing performance.

#### 3.3.2. Exact Scalar Element Access and Slice

**Scalar Element Access** returns `T[i]` for a legal Text and a valid Scalar Index `i`. Otherwise it has no successful
result.

**Scalar Slice** takes Scalar Boundaries `s` and `e`, with the exact successful domain `0 <= s <= e <= |T|`. Its result
`T[s:e]` contains the Scalars from `s` up to, but not including, `e`, in their original order. Its Scalar Count is
`e - s`, and its element `k` is `T[s + k]` for `0 <= k < e - s`.

A slice with `s = e` is empty Text, including at the final boundary. The complete slice `T[0:|T|]` equals `T`. Invalid
ranges have no successful result; they are not silently clamped or repaired.

A Scalar Slice never splits a Scalar, but it may split a Grapheme Cluster. It preserves selected Text values, not source
provenance or correspondence after another Text transformation. Failure attribution follows ADR-0057.

#### 3.3.3. Exact UTF-16 Boundary Correspondence

A legal Text has an exact well-formed UTF-16 representation. For each Scalar `c`, let `w16(c)` be one Code Unit when
`c <= U+FFFF` and two otherwise. The UTF-16 Code Unit offset corresponding to Scalar Boundary `b` is

`B16(T, b) = sum(w16(T[i]) for 0 <= i < b)`, with `B16(T, 0) = 0`.

At the final Scalar Boundary, `B16(T, |T|)` is the UTF-16 Code Unit count. The mapping is strictly increasing. Its
inverse is defined exactly for Code Unit offsets equal to some `B16(T, b)`. An offset within a surrogate pair has no
Scalar Boundary correspondence and must not be rounded. An offset outside the Text's UTF-16 range is likewise invalid.

For `A😀B`, Scalar Boundaries `0, 1, 2, 3` map to UTF-16 offsets `0, 1, 3, 4`. Offset `2` splits the surrogate pair.

This correspondence does not apply to malformed Host UTF-16. A source-bound substring may split a surrogate pair; it is
equivalent to Scalar Slice only if its boundaries and legally observable behavior agree. §3.5 defines the other encoding
correspondences.

#### 3.3.4. Exact Scalar Total Ordering

Legal Text values are ordered lexicographically by the numeric value of their Scalars. At the first differing index, the
smaller Scalar determines the earlier Text. When one Text is a prefix of the other, the shorter comes first. The
comparison result is exactly `Less`, `Equal`, or `Greater`; `Equal` holds precisely when the Scalar sequences are equal.

For example, U+E000 precedes U+1F600 by Scalar value. Java UTF-16 Code Unit comparison need not agree, so it cannot
replace this order. No normalization or Collation is implicit.

This orders Text **values**, not Contract occurrences or compiler symbols. Equal Text values may belong to distinct
occurrences. If selecting one of them affects a judgment, the owning law must determine the tie rather than borrowing an
invented priority from Text ordering.

#### 3.3.5. Exact Contiguous Scalar Matches and Deterministic Search

For legal Text `T`, pattern `P`, and valid Scalar Boundary `p`, an **Exact Contiguous Match** holds exactly when
`p + |P| <= |T|` and `T[p:p + |P|] = P`. A nonmatching valid boundary yields `false`; an invalid boundary is outside the
calculation's domain. The empty pattern matches at every valid boundary, including the final one.

An exact search uses a range `0 <= s <= e <= |T|`. Its **Hits** are all boundaries `p` in ascending order satisfying
`s <= p <= e` and `p + |P| <= e` where the exact match holds. Hits include overlapping matches, with each starting
boundary appearing once. For `T = "AAAA"`, `P = "AA"`, Hits is `[0, 1, 2]`. For `T = "ABC"` and empty `P`, Hits is
`[0, 1, 2, 3]`; searching empty Text for empty `P` yields `[0]`.

A valid search with no Hits returns **No Match**, not an invalid-range result. **First Match** and **Last Match** select
the smallest and largest Hits respectively, or No Match when Hits is empty. The selected order follows Scalar positions,
regardless of the search algorithm. A Source callable may use a different index unit or absence representation (§3.3).

#### 3.3.6. Derived Exact Relations

Exact Prefix, Suffix, and Containment relations follow from the resolved match relation: Prefix is a match at
boundary zero; Suffix is a match starting at `|T| - |P|` when `|P| <= |T|`; Containment holds when Hits is
nonempty. Exact Region Equality follows from equality of successful Scalar Slices. These are derived meanings, not
new primitive equivalence or comparison authorities. In particular, an empty pattern is both a prefix and a suffix
of every legal Text and is contained at every valid boundary.

Exact Hits do not prescribe match consumption. Split and Replace select it separately in §§3.3.8–3.3.9.

#### 3.3.7. Exact Scalar Concatenation and Derived Finite Repetition

For legal Text `A` and `B`, **Exact Scalar Concatenation** `A || B` contains all Scalars of `A` followed by all Scalars
of `B`. Its count is `|A| + |B|`. The result at index `i` is `A[i]` for `i < |A|`, otherwise `B[i - |A|]`.

Empty Text is the identity. Concatenation is associative but generally not commutative:

`A || empty = empty || A = A` and `(A || B) || C = A || (B || C)`.

**Exact Finite Repetition** derives from Concatenation. For finite integer `n >= 0`, `repeat(T, 0) = empty` and
`repeat(T, n + 1) = repeat(T, n) || T`. Its result has count `n * |T|`. Thus `repeat(T, 1) = T`,
`repeat(empty, n) = empty`, and `repeat(T, n + m) = repeat(T, n) || repeat(T, m)`. A negative count is outside its
successful domain.

These value identities do not justify skipping Source evaluation (§3.7). They also do not make normalization
distributive across Concatenation (§3.4.5). Physical capacity limits do not alter the defined Text result (§3.1).

#### 3.3.8. Deterministic Ordered Literal Delimiter Split

An **Exact Ordered Literal Split** takes a legal Text `T`, a finite ordered sequence of legal Text delimiters
`D = <d0, ..., d(m-1)>`, and an explicit result-count mode. It compares delimiters by the already established
Exact Contiguous Scalar Match. The delimiter sequence is an ordered semantic operand: its order is significant,
including when delimiters have equal values or one is a prefix of another. This is a selected common Scalar Text
calculation, not an interpretation of a Java Regex or a blanket rule for Kotlin or Java `split` calls.

**Delimiter selection.** Starting at the next eligible Scalar Boundary, select the lowest boundary at which any
delimiter matches. If several delimiters match there, select the delimiter with the lowest ordinal in `D`. Thus
the rule is earliest position first and declared delimiter order second, not longest delimiter first. The selected
delimiter is identified by its exact matched Text and ordinal; there is no unspecified tie resolution. An empty
delimiter matches at every Scalar Boundary by Section 3.3.5, and competes at that boundary by the same declared
ordinal rule. An empty delimiter sequence yields no delimiter matches.

**Non-overlapping consumption and finite progress.** A selected nonempty delimiter beginning at `p` and having
scalar count `k > 0` consumes exactly `[p, p + k)` of the original Text. The next eligible search begins at
`p + k`, so already consumed scalar regions are not selected again. A selected empty delimiter consumes no scalar;
its boundary is selected at most once, and any subsequent search begins at the next Scalar Boundary. A selection
at the final boundary ends the search if its delimiter is empty. These rules determine a finite ordered sequence
of selected matches even when some delimiters are empty. The choice of a physical search direction or algorithm
cannot change that sequence.

**Partition result.** For selections in their determined order, the result is the finite ordered sequence of the
exact Scalar Slices between consumed delimiter regions, followed by the remaining suffix. The delimiters themselves
are omitted from this selected calculation's result, while the exact values, order, and multiplicity of all
unconsumed original scalars are preserved. An empty segment is a valid Text result, including at the beginning,
between adjacent delimiters, or at the end. The base calculation retains leading, interior, and trailing empty
segments; it does not silently discard them. With no selected delimiter the result is the one-element sequence
containing `T`, even when `T` is empty. When only the empty delimiter is selected for `T = "ABC"` with no result
limit, the result is `["", "A", "B", "C", ""]`; for empty `T`, it is `["", ""]`. For `T = "a--b"`, delimiters
`["-", "--"]` produce `["a", "", "b"]`, whereas `["--", "-"]` produce `["a", "b"]`.

**Explicit result-count modes.** `Unlimited` permits selection of all matches under the preceding rules.
`AtMost(k)` is defined for a positive finite integer `k` and produces no more than `k` Text segments: select at
most `k - 1` delimiters, then take the entire remaining original suffix as the last segment. In particular,
`AtMost(1)` yields `[T]`. `AtMost(0)` and negative bounds are outside the successful domain of this selected
mode; a Host API that uses zero or negative limits differently keeps its own meaning. The base calculation
retains trailing empty segments under both modes. Any variant that removes trailing empties, includes delimiters,
or uses another count convention requires its own explicit result law; none is an implicit default here.

For valid operands, the ordered partition is unique regardless of how the delimiters are found. Regex and
source-specific Split operations have different selection rules (§3.6). Literal Replace uses its own consumption rule
(§3.3.9).

#### 3.3.9. Exact Literal Replace

**Exact Literal Replace** takes legal Scalar Text `T`, a literal pattern `P`, a replacement Text `R`, and one of two
modes: `First` or `All`. It uses the exact match relation in §3.3.5. Matches are selected from the original `T`;
inserted replacement Text is never searched again.

For nonempty `P`, select the earliest eligible match and consume its interval `[p, p + |P|)`. In `All`, resume at
`p + |P|` and repeat until no match remains. In `First`, stop after the first selection. For empty `P`, `First`
selects boundary `0`, while `All` selects every Scalar Boundary from `0` through `|T|` exactly once. Empty matches
consume no Scalar. Both modes therefore produce a finite sequence of non-overlapping selections.

Form the result by concatenating each unconsumed region of the original Text with one copy of `R` at every selected
match, followed by the remaining suffix. With no selection, the result is `T`. Original Scalars outside consumed
intervals retain their values and order. All legal Text operands have a unique result.

| `T`     | `P`   | `R` | Mode    | Result  |
|---------|-------|-----|---------|---------|
| `aaaa`  | `aa`  | `X` | `All`   | `XX`    |
| `ababa` | `aba` | `X` | `All`   | `Xba`   |
| `ab`    | empty | `X` | `All`   | `XaXbX` |
| `ab`    | empty | `X` | `First` | `Xab`   |

The rule is for exact literal Scalar matching. Regex replacement follows §3.6.2; Java/Kotlin replacements retain
their resolved Source meaning under §3.3.

#### 3.3.10. Derived Text Construction and Edge Operations

The following calculations are derived from the existing Count, Slice, Match, Concatenation, and Repetition laws.
They introduce no separate primitive Text operation:

| Calculation           | Exact common result                                                                                                                                                                              |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Join**              | For a finite ordered sequence of Text values, concatenate its members with a specified Text separator between adjacent members. An empty sequence yields empty Text; empty members are retained. |
| **Replace Range**     | For `0 <= s <= e <=                                                                                                                                                                              |T|`, the result is `T[0:s] || R || T[e:|T|]`. An invalid range has no successful result. |
| **Edge Trim**         | For a specified Scalar membership condition, remove the maximal matching prefix, suffix, or both, and return the remaining Slice. An all-matching input yields empty Text.                       |
| **Run Collapse**      | Replace each maximal nonempty run meeting a specified Scalar membership condition with the specified replacement Text. Trimming either end, when required, is a separate selected step.          |
| **Scalar Padding**    | For target count `n >= 0` and pad Scalar `c`, insert `max(0, n -                                                                                                                                 |T|)` copies of `c` before or after `T`, as specified. No original Scalar is removed. |
| **Scalar Truncation** | For nonnegative maximum count `n`, retain `T[0:min(n,                                                                                                                                            |T|)]`. Other selection directions require an explicit rule. |

The membership condition in Edge Trim and Run Collapse is an explicit determinant, not a default whitespace class.
Options such as Join limits or prefix and suffix decoration are separate, explicit calculations. The existing
Canonicalization Trim and WHATWG Whitespace Collapse Laws retain their own representatives and judgments; no common
calculation selects one of those Laws by implication.

A derived result may replace a Source callable only where their resolved meanings agree (§§3.3 and 3.7).

#### 3.3.11. CR, LF, and CRLF Logical Line Boundaries

The **Logical Line Terminator** calculation observes the original Scalar Text and returns the ordered, disjoint
half-open intervals of its line terminators. Each U+000D CR followed immediately by U+000A LF forms one interval
of length two. Every remaining CR or LF forms an interval of length one. No other Scalar is a terminator in this
calculation. An empty Text has no terminator intervals.

For `T = "A\r\nB\n"`, the terminator intervals are `[1, 3)` and `[4, 5)`. The intervals determine the
intervening content regions by Scalar Slice. They do not by themselves select whether a Source API returns an
empty final line, includes a terminator, or omits empty results. Such behavior follows the resolved callable.

This calculation neither changes line endings nor determines UAX #14 line-break opportunities. The LF and CRLF
Canonicalization Laws retain their representatives under Catalog §8.11.

#### 3.3.12. Parsing and Formatting Boundary

Parsing Text into another value domain and formatting such a value as Text do not form one generic Text primitive.
Their grammar and conversion results follow the relevant value-domain meaning. Numeric values follow ADR-0078;
temporal meaning remains subject to ADR-0082. URI and Path interpretation do not arise from Scalar Text alone.
Encoding and decoding bytes follow §3.5.

A supported Java/Kotlin parsing or formatting callable is still interpreted under §3.3, including its evaluation
and unsuccessful outcomes. Deriving its final Text from common operations does not justify removing an observable
Source calculation.

### 3.4. Unicode Operations and Fixed Semantic Bases

#### 3.4.1. Unicode 18.0.0 Property Observation

A selected common Unicode Scalar Property has the meaning specified by **Unicode 18.0.0** and its normative UCD
derivation rules. The selected Property and fixed Unicode version determine its result. A different Host Unicode
database cannot change that result.

Each Property retains its defined result type and its own treatment of absent data. In particular:

| Property situation                  | Required interpretation                                                                                           |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `Script_Extensions`                 | Preserve the specified set of Scripts; do not select one member.                                                  |
| `Numeric_Value`                     | Distinguish an exact numeric value from the absence of one. Absence is not zero or an IEEE floating-point result. |
| String or context-dependent mapping | Use its selected Text operation, not a Scalar Property observation.                                               |
| An unrecognized Property request    | Do not represent it as a legitimate `false`, `Unknown`, or absent result.                                         |

Every selected Scalar Property is defined for **every** legal Scalar, including those unassigned in Unicode 18. Values
follow official defaults and derivation rules even when no explicit UCD data row exists. For example,
`General_Category=Cn` does not imply `Extended_Pictographic=false`. `Bidi_Class`, `Line_Break`, and `East_Asian_Width`
can have range-dependent defaults. The compiler must implement the actual `@missing` and Property-specific rules instead
of inventing a fallback.

Surrogate Code Points are outside the common Character domain (§3.2). The applicable Source law still governs Host Code
Unit operations.

#### 3.4.2. Property Coverage and Derived Classification

The Unicode Coverage Design records the selected inventory:

| Category                              | Count | Role                                            |
|---------------------------------------|------:|-------------------------------------------------|
| UCD Scalar Property observations      |    54 | Independent common observations                 |
| UTS #39 security-profile observations |     2 | Independent security facts                      |
| Algorithm-dependent data              |    38 | Inputs to selected calculations                 |
| Excluded independent observations     |    34 | Not selected as independent common observations |

The two UTS #39 observations are `Identifier_Status` and `Identifier_Type`, using the security profile corresponding to
Unicode 18.0.0. `Identifier_Type` preserves every applicable type. Neither observation grants identifier permission.
Confusable mappings are inputs to §3.4.8, not additional independent Properties.

Contributory Properties help derive official values but cannot replace their defined observations. Decimal-digit
interpretation for `Nd` Scalars is a separate derived calculation, not another Property or Java
`Character.getNumericValue`. Its callable-specific conditions remain for the SDK Interpretation Design. Scalar legality
and source identifier permission follow their own laws.

#### 3.4.3. Semantic Basis and Realization Separation

The agreed realization direction is a **Kontrakt-owned Unicode library** derived from official, version-pinned Unicode
18.0.0 data and normative algorithms. Data-generation correctness, table layout, and execution methods belong to
Character/Text Realization Design. UCD files alone do not implement the UAX algorithms, supply CLDR tailoring, or define
Java/Kotlin callable behavior.

An implementation table or Host intrinsic may realize a selected calculation. It does not become another semantic
authority. The choice of a library creates no new Contract judgment or runtime policy engine.

#### 3.4.4. Selected Case Mapping and Case Folding

The following operations use **Unicode 18.0.0**:

| Calculation                            | Operand and exact result                                                                                                |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| Simple Lowercase, Uppercase, Titlecase | Each maps one Scalar to one Scalar using the matching Unicode simple mapping; an unmapped Scalar stays unchanged.       |
| Default Full Lowercase, Uppercase      | Each maps Text using Unicode's complete default mappings. Context rules and one-to-many results apply.                  |
| Default Full Titlecase                 | Maps Text using Unicode's titlecase rules and Default Word Boundaries (§3.4.6). It is not scalar-by-scalar uppercasing. |
| Explicit-locale Full Case Mapping      | Applies only the selected locale rules in Unicode 18 `SpecialCasing.txt`. Arbitrary CLDR tailoring is not selected.     |
| Simple Case Fold                       | Maps Scalar to Scalar using the explicit **Default** or **Turkic** profile.                                             |
| Full Case Fold                         | Maps Text to Text using the explicit **Default** or **Turkic** profile, including one-to-many mappings.                 |

Default calculations never read ambient Locale. Turkic Folding is distinct from Default Folding. Full Case Mapping
applies Unicode context conditions, including final Sigma. Every legal operand has an exact result even when mapping
expands the Text. A Source callable using different rules follows its resolved Source meaning (§3.3).

`F18(x)` denotes Unicode 18 R4 Default Full Case Folding, as used by the existing Canonicalization Law. Folding-based
equality and ordering derive from applying the selected Fold and then the exact relations in §3.3. This ADR selects no
independent case-insensitive comparison.

#### 3.4.5. Selected Normalization and Caseless Calculations

**NFC, NFD, NFKC, and NFKD** are selected Unicode 18 Text-to-Text calculations under UAX #15. They apply the required
decomposition and composition algorithms, including Hangul rules. Each succeeds for every finite Scalar Text. Expansion
does not make its result invalid.

The seven existing Canonicalization Laws in Catalog §8.6 use the following exact representative calculations:

| Existing representative                | Common calculation     | Existing Law's equivalence                            |
|----------------------------------------|------------------------|-------------------------------------------------------|
| `NFC18(x)`                             | NFC                    | Canonical equivalence by equal `NFD18` results        |
| `NFD18(x)`                             | NFD                    | The same equivalence, with a different representative |
| `NFKC18(x)`                            | NFKC                   | Compatibility equivalence by equal `NFKD18` results   |
| `NFKD18(x)`                            | NFKD                   | The same equivalence, with a different representative |
| `F18(x)`                               | Default Full Case Fold | Unicode D144                                          |
| `Q18(x) = NFC18(F18(NFD18(x)))`        | NFC Case Fold          | Unicode D145                                          |
| `H18(x) = toNFKC_Casefold18(NFD18(x))` | NFKC Case Fold         | Unicode D147                                          |

`toNFKC_Casefold18` denotes Unicode R5: version-fixed `NFKC_CF` mappings followed by NFC. It is needed to define `H18`
but is **not** selected as a separate common operation. Unicode D146 Compatibility Caseless Matching is not selected.
Neither a different normalization order nor ordinary lowercasing may replace the exact formulas above.

Each existing Canonicalization Law retains its authority and its selected `E_L`, `C_L`, and Version under ADR-0066 and
ADR-0076 (§3.8). A matching representative value is not itself a Canonicalization judgment.

For each form `N`, the derived Boolean `isNormalized_N(x)` holds exactly when `N(x) = x` by Scalar Equality. Unicode
Normalization Quick Check Properties are internal algorithm data; no independent String Quick Check observation is
selected. An implementation may use Quick Check only while preserving this exact Boolean.

Normalization cannot generally be distributed across Concatenation. Unicode stability guarantees apply only to their
stated scope. Later Property or Segmentation versions are not automatically equivalent to Unicode 18. **Stream-Safe Text
Processing** is not selected and must not replace these results.

#### 3.4.6. Selected Text Segmentation

Unicode 18.0.0 UAX #29 **Default Extended Grapheme Boundaries** and **Default Word Boundaries** are selected common
calculations. Each returns an ordered set of Scalar Boundaries for a finite Text of length `n`. The result includes `0`
and `n`, without duplicates. Empty Text has the sole boundary `0`. Segments and their counts derive from these
boundaries using §3.3.

A Grapheme may contain several Characters. Default Word Boundaries are not a natural-language word parser. Both
algorithms use Unicode's default rules without ambient Locale. Neither changes the original Text value.

UAX #29 Sentence Boundaries and UAX #14 Line Break Opportunities remain **internal normative calculations**, not
independently selected operations. Line-break opportunities do not define rendered lines. CR/LF terminators have their
own calculation (§3.3.11).

#### 3.4.7. Emoji Sequence Analysis

The Unicode Coverage Design selects six Scalar Emoji Properties. Whole-Text sequences follow Unicode 18 UTS #51. The
following are **internal analysis facts**, not independent common operations:

| Fact                          | Meaning                                                                         |
|-------------------------------|---------------------------------------------------------------------------------|
| Emoji sequence classification | Whether the Text satisfies a specified UTS #51 sequence form, and which form    |
| RGI membership                | Membership in the version-fixed recommended-for-general-interchange set         |
| `RGI_Emoji_Qualification`     | The applicable qualification status, or no applicable status outside its domain |

A valid Emoji sequence need not be RGI; a Grapheme Cluster need not be an Emoji sequence. These facts do not change Text
or choose a glyph representation.

#### 3.4.8. Bidirectional and Identifier-Security Analysis

Unicode 18 UAX #9 **Bidirectional Analysis** is selected as an internal normative calculation. It supports explicit LTR
and RTL paragraph direction as well as first-strong direction. Its embedding levels and visual order do not modify the
original Scalar sequence. Source token order remains intact; the Source display rules and font layout are separate.

The following Unicode 18 UTS #39 calculations are also internal:

| Calculation         | Exact scope                                                                                                                        |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------|
| Confusable analysis | `internalSkeleton`, ordinary LTR `skeleton`, and direction-aware `bidiSkeleton`; direction can be explicit LTR/RTL or first-strong |
| Script analysis     | Resolved Script Set and mixed-script classification                                                                                |
| Restriction level   | Identifier restriction-level classification                                                                                        |

**These calculations do not impose automatic warnings or rejection.** A Confusable relation alone changes neither Text
Equality nor Symbol Identity. Source illegality follows the Source language's actual rules.

### 3.5. Encoding, Decoding, and Invalid Data

#### 3.5.1. Selected UTF Conversions

The common calculations select Unicode **UTF-8, UTF-16, and UTF-32** forms. Each encodes legal Scalar Text as an ordered
sequence of Code Units. A UTF-8 Code Unit is an octet; UTF-16 and UTF-32 Code Units have 16 and 32 bits. Converting
these units to bytes follows the chosen scheme (§3.5.2).

Encoding succeeds for every finite legal Text, including U+0000 and unassigned Scalars. An unpaired Host UTF-16
surrogate is not common Text and needs a separately qualified conversion. These Unicode forms do not redefine
Java/Kotlin Charset operations.

#### 3.5.2. Byte Order and Byte-Order Signatures

The selected byte schemes differ as follows:

| Scheme                        | Selected interpretation                                                                                                  |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| UTF-8                         | Encoding emits no BOM. Initial `EF BB BF` decodes as U+FEFF unless the consuming protocol chooses signature handling.    |
| UTF-16BE / UTF-16LE           | Fixed byte order. Initial encoded U+FEFF remains content.                                                                |
| UTF-32BE / UTF-32LE           | Fixed byte order. Initial encoded U+FEFF remains content.                                                                |
| BOM-sensitive UTF-16 / UTF-32 | Initial valid BOM selects byte order and is consumed. Without BOM or an overriding protocol rule, default to big-endian. |

The selected encoding profile must also fix whether to emit a signature. Other protocols may select different BOM
handling or a different default byte order; those choices must be explicit. Signature consumption is not Text trimming,
and fixed-endian decoding is not interchangeable with signature-sensitive decoding.

#### 3.5.3. Strict Failure and Lossy Decoding

**Strict decoding** produces one Scalar Text for well-formed input. Malformed input has no successful Text result.
Failure identifies the start of the first malformed subsequence using the selected input unit (octet or Code Unit). An
incomplete valid prefix fails as truncated only after the end of input is known.

| Form   | Ill-formed examples                                                              |
|--------|----------------------------------------------------------------------------------|
| UTF-8  | Overlong encoding, invalid continuation, encoded surrogate, value above U+10FFFF |
| UTF-16 | Unpaired surrogate                                                               |
| UTF-32 | Code Unit outside the Scalar domain                                              |

**Replacement decoding** is a different lossy calculation. It replaces each Unicode maximal subpart of an ill-formed
subsequence with one U+FFFD and preserves valid intervening input. At final input end, UTF-8 `E2 82` yields one U+FFFD;
`E2 28 A1` yields U+FFFD, `(`, U+FFFD. Strict decoding must never silently fall back to Replacement. Replacement does
not recover or authenticate the original bytes.

A Java/Kotlin Charset callable follows its own effective Charset and error policy. Its reporting, replacement, or
ignoring of bad input is not automatically the Unicode maximal-subpart calculation. The Source profile also determines
any observable failure length and buffer-consumption state. An unmappable character under a non-UTF Charset is distinct
from malformed encoded input.

#### 3.5.4. Partial Input, Positions, and Round-Trips

The result of completed decoding is independent of input chunk boundaries for the same selected profile. A valid partial
prefix is not a truncation error before final input end. A Source streaming API may expose intermediate state; that
state follows its own resolved meaning (§3.3).

For valid Text and a fixed byte scheme, each Scalar Boundary maps to the Byte offset after encoding the preceding
Scalars. The selected signature contributes to the offset. An offset inside a multi-byte Scalar has no inverse Scalar
Boundary. The corresponding UTF-16 positions follow §3.3.3.

Lossy decoding cannot guarantee one-to-one Source provenance. An original U+FFFD and a replacement U+FFFD may be equal
Text even though their origins differ. Consumers needing the original bytes retain them separately (§3.13).

For matching lossless profiles, `Decode(Encode(T)) = T`. Reverse Byte identity requires a profile that preserves all
original bytes, including signature treatment. A consumed BOM or a replacement operation does not promise such recovery.

#### 3.5.5. Source, Artifact, and Other Semantic Boundaries

A Java/Kotlin conversion retains its resolved Charset profile, including provider-dependent behavior where applicable.
Selection of the common UTF calculations does not exclude source-defined non-UTF Charsets. Equivalence to a common
calculation must be established, not inferred from the method name.

Source decoding and target artifact encoding obey their own format laws. JVM Classfile Modified UTF-8 is distinct from
ordinary UTF-8; its constant-pool limit is addressed in §3.11. The validity of encoded text does not determine
normalization or Contract admissibility.

### 3.6. Source-Bound Operations and Platform Qualification

#### 3.6.1. Regex Pattern Meaning

A resolved Java/Kotlin Regex uses the **actual Source callable's meaning**, not a restricted Pattern language invented
by Kontrakt. The resolved profile determines construction and matching. Admission use remains subject to ADR-0065 §7.7.

| Aspect          | Meaning to preserve                                                        |
|-----------------|----------------------------------------------------------------------------|
| Pattern         | Grammar, flags, Unicode rules, and selected options                        |
| Match selection | Alternation priority, quantifier behavior, assertions, and atomic behavior |
| Captures        | Participation, updates during repetition, and final group values           |
| Result          | The match selected by the operation and its bounds                         |

The set of accepted strings alone cannot determine which match and captures the Source observes. The meaning does not
require a specific matching engine.

#### 3.6.2. Matcher State and Derived Operations

A stateful Matcher operation determines both its return value and any state later calls can observe. The Source API
fixes how search advances and what its region and reset operations mean. The compiler may express this as transitions
without retaining a physical Matcher object.

| Operation            | Meaning to preserve                                               |
|----------------------|-------------------------------------------------------------------|
| Regex Split          | Match consumption, zero-width progress, limit, and empty segments |
| Regex Replace        | Match selection, group expansion, and output formation            |
| Callback replacement | Call order, returned values, and legal observable effects         |
| Match iteration      | Search progression and evaluation timing                          |

None of these results follows merely from a Boolean match. They also cannot be replaced unconditionally by common
Literal Split or Replace (§3.3). Java and Kotlin may differ.

Some APIs expose engine-sensitive observations, such as `hitEnd()`. Matching final results is then insufficient to prove
equivalence. A replacement must preserve the source-profile guarantee for that observation; if it cannot, the
platform-specific operation remains qualified under ADR-0073. This does not grant callback execution any Contract
authority (§3.7).

#### 3.6.3. Regex Inputs and Completion

Pattern construction and matching are separate computations. Invalid Pattern syntax is not No Match. A computed Pattern
must satisfy the receiving 1D's source-closure and completion laws. For Admission, ADR-0065 §§7.3–7.7 require
finite-work qualification, not an automatic restriction to linear-time engines. Resource termination is not an Admission
rejection.

Regex positions and character classes use the resolved Source's coordinate units and Unicode profile. A UTF-16 match
offset maps to a Scalar Boundary only where §3.3.3 permits it. Common Unicode 18 calculations may replace Source
calculations only when their exact results and required observations agree.

#### 3.6.4. Collation Meaning

Source Collation follows its **effective rule profile**, not merely the named Locale. The profile must fix its rules,
strength, and decomposition settings. Provider data is also a determinant when it affects the result. Comparison yields
`Less`, `Equal`, or `Greater`. Unicode UCA or CLDR may supply the actual rules when applicable; Unicode 18 Default UCA
does not replace an arbitrary Java/Kotlin `Collator`.

| Observed call                  | Preservation requirement               |
|--------------------------------|----------------------------------------|
| `Collator.compare`             | Comparison under the effective profile |
| `CollationKey.compareTo`       | Source-defined key comparison relation |
| `CollationKey.getSourceString` | Original source string                 |
| `CollationKey.toByteArray`     | Exact returned bytes when observed     |

The key's internal representation is replaceable unless its bytes are observed. Collation equality does not establish
Exact Scalar Equality.

#### 3.6.5. Realization Equivalence and Deferred Placement

An alternative Regex or Collation realization must preserve the observations of the resolved Source use, including its
failures and evaluation effects (§3.7). An unproven Host engine or Provider cannot silently define a common result. The
stage that holds the resolved meaning remains an architecture decision, not a new Text law.

### 3.7. Composition, Partiality, and Information Preservation

#### 3.7.1. Closed Computation Composition

A Java/Kotlin invocation contributes its **resolved computation**, not independent Contract authority. In Admission, the
selected flat Definition contains the judgment. Its Helpers and permitted local state are interpreted under ADR-0065
§the relevant Design and 7–8. A call on an external object or an unresolved Callback cannot provide missing judgment
meaning at runtime. Normal User Realization callbacks instead follow their actual Source meaning under ADR-0073.

Composition preserves the result and the conditions under which each computation is reached. A partial operation must
remain defined on every path reaching it. Source exceptions cannot become Contract rejection. ADR-0065 §7.5 owns the
Admission completion requirement.

#### 3.7.2. Semantic Dependency and Deterministic Independence

Preserve the dependencies that determine a lawful result. Two computations proven independent in their actual context
may execute in either order. Their results must still be combined in the specified order when that combination is
order-sensitive.

Independence alone does not permit a computation to be executed on a previously unreachable path, omitted, repeated, or
have its result permuted. Each transformation must preserve the required observations. In particular, a failure in one
independent Contract computation cannot erase another required judgment under ADR-0057; the single Admission judgment
itself follows ADR-0065.

The same applicable semantic basis must produce the same legally required result regardless of scheduling (ADR-0075
§22). Platform-permitted variation follows ADR-0073 rather than an invented global execution order.

### 3.8. Use Through the Contract Pipeline

A common Text result may inform a judgment only as permitted by its owning 1D. It cannot make that judgment on the 1D's
behalf.

| Contract use              | Governing boundary                                                                                                                                           |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Input                     | Presentation and Sameness follow ADR-0064.                                                                                                                   |
| Admission                 | The closed Definition determines Continuation under ADR-0065 (§3.7).                                                                                         |
| Canonicalization          | The selected Law retains `E_L`, `C_L`, Version, and its judgment under ADR-0066 and ADR-0076. Calculating a representative does not establish an Occurrence. |
| Lowering, Fact, Invariant | Declared relations and Fact standing follow ADR-0067–0069.                                                                                                   |
| Policy and Governance     | Their existing decisions govern whether they may consume the Text result.                                                                                    |
| Publication and Output    | Existing outward authority governs presentation. Formatting does not create a fact or permission to expose one (ADR-0058–0059).                              |

A shared computation may be reused without merging the judgments or their evidence. Applicable Basis and legal
consumption remain governed by ADR-0063 and ADR-0075.

### 3.9. Compliance with Existing Frontend and Compiler-Product Boundaries

The actual Source grammar decides which identifiers and literals are valid and how escapes are decoded. Decoded Text is
not the same as its source spelling or provenance. In particular, Unicode normalization is not an implicit `.kontrakt`
identifier rule. The Frontend must verify its concrete grammar and callable coverage rather than treating a later Text
calculation as a repair.

ADR-0071 governs the meaning of a resolved 1D Definition Candidate. Exact Law selection stays with that 1D; equal Text
results do not prove equal source spellings or candidates. The storage stage and handoff protocol for resolved Source
calculations are not selected by this ADR.

### 3.10. Realization, Analysis, and Optimization Obligations

A Realization must be legal **before** an optimization removes any of its calls. A legal optimized result cannot
retroactively approve a prohibited original call. Analysis evidence may justify a transformation only in its
producer-defined scope under ADR-0075. Without that evidence, the affected transformation cannot be assumed legal.

Each Text rewrite must preserve the resolved meaning and the dependencies in §3.7. For example, normalization is not
generally distributive across concatenation. Equal normalized results also do not prove equal original Scalar Text. A
transformation involving source effects or index conversions needs its own equivalence basis.

The compiler may choose its analysis and optimization machinery freely within these obligations. This ADR does not
select an execution IR or a rewrite engine.

### 3.11. JVM Backend and Runtime Conformance

Every legal Scalar Text has a well-formed UTF-16 representation. A general JVM `String` may contain an unpaired
surrogate, so conversion back to Scalar Text is partial (§§3.2 and 3.5). Source callables keep their actual UTF-16
observations under ADR-0073.

A Classfile `CONSTANT_Utf8_info` entry uses **Modified UTF-8** and may contain at most **65,535 encoded bytes**. That is
an artifact restriction, not a Text value limit. The Backend must choose a legal representation or fail at its own
boundary. Emitted Classfiles must satisfy JVM typing and artifact completion rules.

A target intrinsic or generated helper may realize a governed Text operation only when its result conforms to that
operation. Backend implementation and runtime conformance remain for verification.

### 3.12. Identity and Compiler-Product Reuse

Text Equality (§3.2) is not a universal identity relation. The comparison must match the result whose reuse is being
considered.

| Comparison                            | Meaning                                                             |
|---------------------------------------|---------------------------------------------------------------------|
| Scalar Text Equality                  | Exact original Scalar sequence                                      |
| Input Sameness                        | The relation selected by ADR-0064                                   |
| Canonicalization equivalence          | The selected Law's `E_L`                                            |
| Source provenance                     | Original spelling and location, even when decoded values match      |
| Compiler Product or artifact equality | The producing boundary's own result law under ADR-0071 and ADR-0075 |

A matching hash or shared storage address cannot prove these equalities. Reuse also requires the current determining
Basis, including the exact Unicode or Source profile. For example, `"\u0041"` and `"A"` can decode to equal Text while
their provenance differs. Equal NFC results can also hide different original Text.

Early cutoff may preserve a product whose **own** equality and validity still hold; it does not preserve every dependent
product automatically. The database representation and incremental scheduling strategy remain V2 Design matters.

### 3.13. Version, Failure, and Resource Boundaries

Each selected calculation uses the version or profile that determines its result (§§3.4–3.6). The applicable Contract
and compiler laws own failure attribution under ADR-0051, ADR-0052, ADR-0057, and ADR-0075. A resource stop is neither a
truncated successful Text nor an invented Admission rejection.

Diagnostic evidence must preserve the original information needed to explain a failure. Escaping a bidi control or
malformed byte sequence for display does not replace the underlying evidence. Source offsets must be converted using the
appropriate coordinate law (§§3.3.3 and 3.5.4); displayed columns are a separate concern.

ADR-0060–0062 remain Proposed and do not establish a diagnostic rendering policy here. The security calculations of
§3.4.8 do not require an automatic warning.

### 3.14. V1 Scope, V2 Evolution, and Realization Design Ownership

A selected Text law defines meaning. It does not establish support for each Java/Kotlin callable or its lawful 1D use.
Executable realization and conformance require their own evidence. The declared SDK profile and supported execution path
must therefore be verified before V1 support is claimed.

V2 may change calculation and reuse mechanisms without changing an unchanged law. Adding another Unicode profile or
changing the operation result requires a semantic decision. The compiler stage that stores a Source calculation remains
deliberately undecided.

## 4. Consequences

- The compiler can share exact Text calculations without creating another 1D judgment. Each Contract continues to own
  its decision (§§3.1 and 3.8).
- Java/Kotlin calls require accurate Source interpretation. A standard method name alone cannot qualify its use (§§3.3
  and 3.6).
- Independent calculations may be rescheduled, but a rewrite must preserve their meaning and lawful observations (§§3.7
  and 3.10).
- JVM output and compiler reuse obey their own representation and validity laws (§§3.11–3.12).
- Semantic selection does not establish V1 conformance. Integration and executable verification follow their owning
  Designs (§3.14).

