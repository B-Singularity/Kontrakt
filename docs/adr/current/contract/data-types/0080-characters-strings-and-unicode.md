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
- `docs/design/character-text/character-text-sdk-interpretation-and-calculation-design.md` — Working Design for
  Java/Kotlin SDK callable coverage, proposed common calculations, and conformance; not an Accepted Contract law
- `docs/design/canonicalization/built-in-law-catalog/canonicalization-built-in-law-catalog-design.md`, especially
  §8.4–§8.6 —
  research and prior specification material only for this ADR; it does not grant authority to shared Text semantics
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md` — Working Design, not an Accepted ADR
- `docs/todo/roadmap/kontrakt-v2-reference-architecture-and-v1-foundations.md` — Reference Architecture, not an Accepted
  ADR

---

## 1. Context

ADR-0078 establishes the precedent for defining shared value and operation semantics without creating a new 1D Contract
or fixing a realization. ADR-0064 already fixes the meaning and Presentation Sameness of Input `Text`. ADR-0066 and
ADR-0076 retain the authority of separately selected Exact Canonicalization Laws.

This ADR establishes the common meaning of Character and Text values and of Kontrakt-defined Text calculations wherever
they are used in the compiler. The same calculation must have the same meaning in Contract processing and in compiler
consumers. Each 1D retains its own judgment, applicability, established Material, and legal observations. A shared
calculation does not replace a 1D judgment, nor does a 1D implementation acquire permission to redefine that
calculation.

## 2. Problem

A compiler encounters characters and strings as source spelling, resolved values, operation operands, analysis evidence,
published Material, and target representations. Those are not interchangeable. Without explicit shared operation laws,
one consumer can accidentally change an observable result through Unicode-library defaults, conversion, string
comparison, compiler rewriting, or representation changes.

The existing Contract pipeline and the planned V1/V2 compiler architecture need common semantic requirements, not a
mandatory Text engine, new IR family, or physical string layout. Sections 3.1–3.3 establish the shared boundary and
value domain, the interpretation obligations, and the selected exact position, slicing, ordering, literal-search,
concatenation, finite-repetition, and ordered-literal-split laws. Section 3.4 establishes the Unicode 18.0.0
Property Observation meaning and its complete result/default-value obligations, without selecting the entire Property
catalog. Other operation selections and qualification questions remain open in Section 5. A law resolved in this
Draft is not by itself a claim of V1 execution support.

## 3. Decision

*Section 3.1 records the resolved shared boundary. Section 3.2 records the Character and Text value domain. Section
3.3 states the interpretation obligations and the selected exact scalar position, access, slice, ordering,
literal-search,
concatenation, finite-repetition, and ordered-literal-split laws. Section 3.4 records the resolved Unicode 18.0.0
Property Observation requirements; the particular Property selection, independent literal replacement, Join, and the
other candidates in Section 5 remain open. Sections 3.5–3.14 remain drafting headings. This Draft does not declare
unresolved operations or unqualified source callables supported for execution.*

### 3.1. Shared Meaning and Contract Ownership

Kontrakt defines common Character and Text value and operation semantics for the exact calculations it governs. A
Character has the meaning of a Unicode Scalar Value. A Text value is a finite, ordered sequence of those scalars. ASCII
characters therefore belong to the same Character domain. UTF encodings can represent that Text without giving it a
different meaning. This law does not require the compiler to store Text as a scalar array or to use one physical string
representation.

The common Text value preserves the identity and order of its scalars. Exact sequence equality does not perform
normalization, case folding, or collation. This agrees with the Text Presentation Sameness established by ADR-0064.
The finite bound imposed on an Input `Text` presentation remains an Input-owned condition; it is not a global maximum
length for every Text result computed by the compiler. The exact domains and boundary conditions of individual
operations
remain to be completed in Sections 3.2–3.5.

A Kontrakt-defined Character or Text operation has one exact meaning regardless of the compiler component that performs
it. Before an operation is approved, its law must identify which operands it accepts and the exact result it
produces. If another semantic input, such as a Unicode version, can change that result, the law must make that input
explicit. It must also determine the conditions under which the calculation has no successful result. The same operation
law and determining inputs must yield the same legally observable result. The execution algorithm cannot select a
result using whatever Unicode data or locale happens to be available in the environment. Defining an operation does
not require every compiler component to execute it. Nor does it imply that V1 already has a conforming realization.
Operation-specific scope and qualification remain under review in Section 5.

Unicode and the relevant encoding standards provide the semantic sources from which this ADR can define common
calculations. Existing Character and Text calculations discussed in the Canonicalization Catalog Design may be used as
research material and checked against those standards. That Design combines law specifications with
implementation-facing
material. It is not a normative basis for this ADR, and citing it cannot import its implementation choices or grant
Canonicalization authority to a shared calculation. Where a common operation is selected, this ADR must close its own
meaning against an explicitly chosen standard and any required immutable version or profile. A choice made by an
existing Canonicalization law is not automatically a choice for every new common Text operation.

ADR-0066 continues to govern Canonicalization judgments. ADR-0076 governs membership and qualification of the built-in
Canonicalization authorities. Each selected Exact Law retains its own equivalence, representative, Version, and
judgment authority. A common normalization calculation can be used without establishing a Canonicalization judgment.
Likewise, every other 1D Contract continues to own the judgments and Material established under its existing law.
ADR-0080 creates no new 1D Contract or Text judgment authority. It does not alter Establishment or introduce another
HIR law-reference protocol.

Kontrakt may interpret a Text datum lawfully produced by an external computation without adopting the producer's
operation as a Kontrakt-defined Text operation. A Java or Kotlin Source-Bound operation retains the semantics of the
exact resolved callable under ADR-0073 and the qualification model of ADR-0078. In particular, a UTF-16 Code Unit
operation does not become a Unicode Scalar operation because both are expressed using a Host `String`.

A compiler component that uses a common Text operation must preserve that operation's exact result. The component
continues to own the independent meaning of the task for which the calculation was used. For example, comparing the
spelling of two source names does not itself decide whether they resolve to the same symbol. Likewise, an exact Text
result cannot by itself determine equality of compiler products or the legal encoding of a target artifact. The relevant
IR and Compiler Product laws determine which results and original distinctions must remain available to later legal
observers. This ADR does not prescribe how long a compiler stores the information or whether it reuses a physical
representation. ADR-0071 and ADR-0075 continue to govern those boundaries.

A Text value remains finite without acquiring a global physical capacity limit. An Input size bound follows ADR-0064,
and Contract Budget and Capacity follow ADR-0051 and ADR-0052. Compiler resource controls and target-representation
limits retain their separate owners. An unsuccessful calculation cannot be published as a successful, truncated, or
silently repaired Text result. Resource exhaustion cannot create a new refusal on behalf of a 1D whose law does not
provide one.

The relevant Realization or Compiler Design chooses how to calculate and store the value. It may use an encoding buffer
or reuse a previously computed result, provided that the common meaning remains intact. These physical choices may
change between V1 and V2 without changing an unchanged Text operation law. A semantic change requires an explicit law
decision; updating a Unicode library cannot silently revise that law.

### 3.2. Character and Text Value Interpretation

A Character denotes one Unicode Scalar Value, from U+0000 through U+10FFFF excluding the surrogate code points
U+D800–U+DFFF. A Unicode Code Point is not necessarily a scalar, and a UTF-16 Code Unit or an octet is a unit of
representation rather than a Character. A legal Text is a finite sequence of these Character values. Its exact
observations preserve sequence length, scalar values, and their order. Empty Text is a legal zero-length value; U+0000,
combining scalars, and supplementary scalars remain distinguishable values. Equality never silently normalizes or folds
the sequence.

Every Unicode Scalar remains eligible for the common Character and Text domain, including noncharacters, currently
unassigned scalars, private-use scalars, default-ignorable scalars, and bidirectional controls. Their classification,
visual presentation, or security implications can affect a particular source language, Contract use, or external
boundary without changing the common value domain. A classification depending on Unicode data is subject to the
selected operation's semantic basis, not to an implicit restriction on which scalar values exist. One Character is not
a grapheme cluster: grapheme segmentation may identify a sequence containing several scalars, and that calculation
cannot silently change Character identity.

Once the meaning of a Text value is determined, its legally observable value cannot change merely because its physical
representation is produced later or an external buffer or library state changes. This requirement does not select a
materialization point, ownership mechanism, decoding strategy, or storage lifetime. Those decisions remain with the
responsible compiler boundary, provided it can preserve the exact observations the owning law requires. A partially
interpreted Host representation must not be asserted to be an established common Text value before its legality and
meaning are determined.

The Text domain has no compiler-wide physical length ceiling. A lawful operation can produce a longer finite Text than
its inputs without inheriting a particular Input presentation bound. The owning Contract and compiler resource laws
remain responsible for their distinct restrictions. Absence is not empty Text, and neither malformed encoding nor an
unsuccessful calculation supplies a Text datum by implication. This ADR does not define or assign a new Failure
judgment; ADR-0057 and the other existing owners govern the relevant attribution and outcome.

### 3.3. Exact Text Operations and Observable Results

The scope of common Character and Text calculations must satisfy three independent needs. Kontrakt first interprets
supported Java and Kotlin expressions used to author a 1D Contract. It then relates the resolved expressions to the
values and observations that the particular 1D law permits. Separately, Kontrakt's own compiler components require
Character and Text calculations even when no user-authored expression is involved. All three needs contribute to the
selection of common calculation laws. The supported authoring surface is not a new string library exposed by Kontrakt,
and no compiler component is restricted to operations that happen to appear in a 1D definition.

Java and Kotlin syntax in a 1D declaration serves as an authoring expression, not as delegated JVM execution or an
independent platform authority over the Contract. The Kontrakt-owned Contract Frontend must resolve the meaning of an
expression it supports; executing user code or accepting a JVM-computed Boolean in place of that interpretation cannot
establish the declaration's Contract meaning. A familiar method name or syntax shape is insufficient to identify the
calculation. The supported expression must have its actual source-level operand and result meaning determined, including
any evaluation behavior that can affect its lawful observation. If that meaning cannot be closed under the applicable
1D law, the expression cannot be silently accepted on the strength of a Host callable's existence. For the selected
Java and Kotlin SDK profiles, Kontrakt must account for the complete standard Character and Text callable surface,
including overloads and language-specific resolution, rather than recognizing only a sample of familiar methods. An
unrecognized callable within a claimed supported profile is a coverage deficiency, not evidence that the user's source
is unlawful. Recognizing and understanding a callable does not make its use admissible to every 1D or qualify it for
JVM execution. Third-party libraries have separately selected and extensible callable coverage; their presence does
not impose whole-library coverage or extend a 1D's Authoring Law.

Mapping an interpreted expression to Contract meaning is governed by the receiving 1D. An Admission judgment may
consume a common Text comparison without giving ADR-0080 or the Java/Kotlin spelling authority over that judgment.
The same principle applies to any other 1D. A recognized expression does not expand an otherwise restricted 1D
Authoring Law. In particular, a source `String.length` observation measured in UTF-16 Code Units cannot be silently
replaced with the number of Unicode scalars, and an unrelated collection `size` observation does not acquire Text
meaning. The result of a supported expression must preserve the source meaning Kontrakt has admitted for that
expression and the observations required by the receiving 1D.

Source-bound intermediate results need not themselves be common Text values. A Java or Kotlin operation over UTF-16
Code Units may produce a sequence containing an unpaired surrogate; Kontrakt can interpret such a source result
without falsely establishing it as a Unicode Scalar Sequence. Converting the result to common Text requires the
applicable value conditions to hold. Recognition of an authoring expression is not, by itself, ratification of its
callable for JVM execution. ADR-0073 retains its distinct platform qualification and preservation requirements where
actual platform behavior is involved, consistently with ADR-0078 §3.3.

When a compiler component performs a Kontrakt-defined operation independently of user source, the operation follows
its common law rather than acquiring the Java or Kotlin API's meaning from the chosen implementation. Scalar count
therefore observes the number of scalars, while UTF-16 Code Unit count and encoded-byte count remain different
calculations. An exact Text concatenation preserves both operand sequences in order, including the empty sequence,
without an implicit normalization or case conversion. The exact position, access, slicing, ordering, literal-search,
concatenation, finite-repetition, and ordered-literal-split laws are stated below. They use scalar units and do not
adopt collation, case folding, or Host search conventions. The remaining operation candidates and their unresolved
boundary cases stay under Section 5.3.

Compiler subsystems may use the same common calculation independently of user-authored Contract expressions. For
example, constant folding may calculate a Text result without performing an Admission judgment. Exact Text comparison
can also assist Source Resolution without deciding Symbol identity. Compiler Product equality, Cache validity,
diagnostic presentation, and JVM artifact legality remain subject to their own laws. Internal encodings and algorithms
are replaceable as long as they preserve the required result and legally observable evaluation. The compiler need not
invoke the corresponding Java/Kotlin method or materialize a Scalar array, and a faster implementation is not evidence
that an otherwise different operation is equivalent.

This ADR does not prescribe a Java/Kotlin callable mapping table. The owning Frontend and ADR-0073 platform audit
must establish their supported mappings and qualifications under existing law, while the catalog records and their
validation remain separate Design work. No new compiler pass or IR structure follows from these Text laws. Changing
that machinery cannot alter an unchanged common Text operation law. Any actual 1D judgment and the attribution of
unsuccessful calculation remain with their existing owners.

#### 3.3.1. Text-Relative Scalar Positions and Count

The scalar count of a legal Text `T`, written `|T|`, is the number of Character values in its ordered sequence. This
is an exact observation of the existing Text domain, not a newly defined equality or length bound. Scalar count is
not a UTF-16 Code Unit count, encoded-byte count, or grapheme count. The count of an empty Text is zero.

For a Text `T` of scalar count `n`, a **Scalar Index** addresses an existing element and is valid exactly when
`0 <= i < n`. A **Scalar Boundary** addresses a position before, between, or after elements and is valid exactly
when `0 <= b <= n`. Both are zero-based positions relative to the particular Text operand. A boundary at `n` is
valid, including for empty Text, but it is not an element index. Numerical equality of positions belonging to
different Text operands does not establish that the positions correspond or refer to the same occurrence.

These positions describe sequence calculations. They do not create a source coordinate, Input nominal coordinate,
HIR Semantic Reference, or identity that can be transferred from one Text value to another. Their legal meaning
does not require an integer representation, a particular storage layout, or constant-time access.

#### 3.3.2. Exact Scalar Element Access and Slice

**Scalar Element Access** is defined for a legal Text `T` and a valid Scalar Index `i`. Its successful result is
exactly `T[i]`, one Character. An index outside `0 <= i < |T|` has no successful element-access result; this is
not an empty Text, a `null` result, or a `false` match. The owning Failure law determines the attribution and
outcome of an attempted out-of-domain calculation.

**Scalar Slice** accepts a legal Text `T` and two Scalar Boundaries `s` and `e`. Its successful domain is exactly
`0 <= s <= e <= |T|`, and the result is the ordered scalar sequence `T[s:e]`, including `T[s]` through `T[e-1]`
when `s < e`. The end boundary is exclusive. When `s = e`, including `s = e = |T|`, the result is legal empty
Text. A slice over `[0, |T|)` has exactly the value of `T`. The scalar count of a successful slice is `e - s`;
for every `0 <= k < e - s`, its scalar at index `k` equals `T[s + k]`.

No scalar is split by Scalar Slice. This does not require a grapheme cluster to remain intact: a valid Scalar
Boundary may lie between a base character and a combining character. An invalid range has no successful slice
result, and it is not implicitly clamped, reversed, or repaired. The owning law retains unsuccessful-calculation
attribution. A successful slice preserves the selected scalars' exact values and relative order without establishing
source provenance or a correspondence after arbitrary Text transformations.

#### 3.3.3. Exact UTF-16 Boundary Correspondence

A legal Text has an exact well-formed UTF-16 representation. For a scalar `c`, let `w16(c)` be one Code Unit if
`c <= U+FFFF` and two Code Units otherwise. For each valid Scalar Boundary `b` in Text `T`, the corresponding
UTF-16 boundary is uniquely determined by

`B16(T, b) = sum(w16(T[i]) for 0 <= i < b)`, with `B16(T, 0) = 0`.

`B16(T, |T|)` is the exact UTF-16 Code Unit count of that representation. `B16` is strictly increasing over the
valid Scalar Boundaries. The inverse correspondence succeeds for a UTF-16 offset `u` exactly when there is a
Scalar Boundary `b` such that `B16(T, b) = u`; that boundary is then unique. An offset within the represented
Code Unit range but inside a surrogate pair is not a Scalar Boundary. An offset outside the representation's range
is likewise not a successful inverse correspondence, but for a different reason. Neither is silently rounded to an
adjacent scalar boundary.

For example, the Text `A😀B` has Scalar Boundaries `0, 1, 2, 3`, corresponding to UTF-16 boundaries
`0, 1, 3, 4`; UTF-16 offset `2` is inside the surrogate pair and has no exact inverse boundary. This calculation
uses the established scalar domain and UTF-16 encoding semantics, not a version-specific Unicode Character Property
table. It does not govern arbitrary malformed Host UTF-16 sequences or require the compiler to store such offsets.

The same principle of exact correspondence can be considered for other selected encodings, but this Decision closes
only UTF-16 boundary correspondence. The encoding-specific laws for UTF-8, UTF-32, malformed data, and byte offsets
remain open under Section 5.5. Source-bound Java/Kotlin operations keep their resolved UTF-16 behavior; in particular,
a Host substring may split a surrogate pair and yield a Host intermediate value that is not legal common Text. It may
be mapped to Scalar Slice only when both boundaries and all legally observable source behavior correspond exactly.
No new Source Location or source-operation law is established by this correspondence.

#### 3.3.4. Exact Scalar Total Ordering

Legal Text values have an exact lexicographic total order based on Unicode Scalar numerical values. Compare two
Texts at the first scalar index where their values differ; the Text with the smaller scalar value precedes the
other. If no differing scalar exists within their shared prefix, the shorter Text precedes the longer. Two Texts
compare Equal exactly when they are equal under the already established exact scalar-sequence equality. The legal
comparison result is precisely one of Less, Equal, or Greater; it is not a Host integer difference.

Consequently, empty Text precedes every nonempty Text, `"A"` precedes `"a"`, and U+E000 precedes U+1F600. A
Java UTF-16 Code Unit comparison can give a different order for the last pair; it therefore cannot silently
replace this common comparison. Neither Unicode normalization nor case conversion, locale collation, or an implied
Unicode Property profile participates in this ordering. Valid but unassigned scalars are ordered by their numeric
values as well.

The total order is an order of Text **values**, not an ordering of distinct Contract occurrences, source symbols,
or compiler products. Two occurrences may have equal Text while remaining distinct under their owning laws. Where
an owning calculation must choose between such occurrences and its result depends on that choice, the owning law
must supply a lawful and deterministic tie resolution; Text ordering invents no occurrence identity or priority.

#### 3.3.5. Exact Contiguous Scalar Matches and Deterministic Search

For legal Text operands `T` and `P`, and a valid Scalar Boundary `p` of `T`, an **Exact Contiguous Match** holds
exactly if `p + |P| <= |T|` and `slice(T, p, p + |P|)` is exactly equal to `P`. At a valid boundary where the
pattern does not fit or the selected scalars differ, the match is `false`; an invalid boundary is outside the
successful domain of the position-dependent match calculation. An empty pattern matches at every valid Scalar
Boundary, including the final boundary of `T` and boundary zero of empty Text. Matching never implicitly applies
normalization, folding, collation, or regex interpretation.

An exact search within a range accepts Scalar Boundaries `s` and `e` satisfying `0 <= s <= e <= |T|`. Its
observable **Hits** result is the finite sequence of all boundaries `p` satisfying `s <= p <= e` and
`p + |P| <= e` at which the Exact Contiguous Match holds, ordered by strictly increasing scalar boundary. Each
position occurs exactly once; overlapping occurrences are included. The end boundary belongs to the set of eligible
match positions for an empty pattern, even though the search range itself is half-open for selected scalars.
For `T = "ABABA"` and `P = "ABA"` over the entire Text, Hits is `[0, 2]`; for `"AAAA"` and `"AA"`, it is
`[0, 1, 2]`; for `"ABC"` and an empty pattern, it is `[0, 1, 2, 3]`. Searching an empty Text for an empty
pattern yields `[0]`.

A valid search with no Hits has an exact **No Match** result, distinct from an out-of-domain range. First Match
and Last Match, where requested, select respectively the lowest and highest boundary in Hits; when Hits is empty,
these selections yield No Match. They do not depend on the order in which an algorithm discovers candidates, on
parallel execution, or on cache reuse. This law specifies the order of the observable match positions, not the
physical search order or a required materialized collection. It neither selects a Java `-1` sentinel nor a Kotlin
nullable-result representation. Source-bound searches must keep their separately resolved result and position laws.

#### 3.3.6. Derived Exact Relations and Remaining Operations

Exact Prefix, Suffix, and Containment relations follow from the resolved match relation: Prefix is a match at
boundary zero; Suffix is a match starting at `|T| - |P|` when `|P| <= |T|`; Containment holds when Hits is
nonempty. Exact Region Equality follows from equality of successful Scalar Slices. These are derived meanings, not
new primitive equivalence or comparison authorities. In particular, an empty pattern is both a prefix and a suffix
of every legal Text and is contained at every valid boundary.

Exact Match and Hits alone do not determine which occurrences a transformation consumes. Sections 3.3.7–3.3.8
separately determine concatenation, finite repetition, and the selected ordered literal Split calculation. They do
not establish a universal Replace policy, resolve Regex replacement, or govern a resolved Java/Kotlin callable whose
selection and result rules differ. Join and independent literal Replacement remain under review in Section 5.3. None
of these common laws qualifies a Host callable, expands a 1D Authoring Law, or supplies a Failure judgment. A consumer
that requires ordered observations must preserve the value order or the match-position order stated here; execution
order can vary only when legally observable behavior remains unchanged.

#### 3.3.7. Exact Scalar Concatenation and Derived Finite Repetition

For legal Text operands `A` and `B`, **Exact Scalar Concatenation**, written `A || B`, is the unique Text whose first
`|A|` scalars are exactly those of `A` in order and whose following `|B|` scalars are exactly those of `B` in order.
Its scalar count is `|A| + |B|`; at each index `i` in that result, the scalar is `A[i]` when `i < |A|`, and
`B[i - |A|]` otherwise. Concatenation is defined for every pair of legal finite Text values. It neither alters a
scalar nor applies normalization, case mapping, collation, or an encoding conversion. The resulting Text is finite,
without inheriting the Input presentation bound of either operand.

Empty Text is the two-sided identity: `A || empty = empty || A = A`. Concatenation is associative as a calculation
on already determined Text values: `(A || B) || C = A || (B || C)`. It is not generally commutative. The value
laws do not authorize reordering or eliminating the evaluation of Java/Kotlin source operands, their conversions,
exceptions, or effects. Nor does associativity mean that a separately selected Unicode normalization law commutes
with concatenation; normalizing operands independently need not normalize their combined boundary.

**Exact Finite Repetition** is the derived calculation `repeat(T, n)` for a legal Text `T` and a finite nonnegative
integer `n`. `repeat(T, 0) = empty` and `repeat(T, n + 1) = repeat(T, n) || T`. Its uniquely determined result has
scalar count `n * |T|`. Consequently, `repeat(T, 1) = T`, `repeat(empty, n) = empty`, and
`repeat(T, n + m) = repeat(T, n) || repeat(T, m)` for finite nonnegative `n` and `m`. A negative repetition count
is outside this calculation's successful domain; it does not denote empty Text. Repetition introduces no new Text
equality, independent order law, or requirement for a particular algorithm.

The exact finite result and its successful domain do not impose a global Text capacity ceiling or promise unlimited
physical resources. An inability to materialize or evaluate a result within an independently owned resource limit
cannot silently shorten or otherwise change the lawful result or establish a 1D refusal not provided by that 1D.
Source-bound concatenation and repetition retain the exact resolved Java/Kotlin evaluation and result laws, including
UTF-16 intermediates that cannot be interpreted as common Text. This Section creates no Host callable qualification.

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

For valid operands and a valid result-count mode, delimiter selection and the ordered partition have exactly one
result, independent of discovery order or parallel execution. The result is an ordered semantic sequence of Text
values; it does not prescribe a JVM array, an IR representation, or a new Collection authority. Neither Unicode
normalization nor case folding is applied implicitly. An operation using Regex matches, Unicode segmentation
boundaries, callbacks, or Host-specific split and limit conventions is not automatically equivalent to this literal
calculation. Recognition of any such source callable and its admissibility remain governed by Section 3.3 and its
existing owning laws. Independent Replace consumption and result laws, including empty-match replacement policies,
remain open in Section 5.3; this Split selection does not resolve them.

### 3.4. Unicode Operations and Fixed Semantic Bases

#### 3.4.1. Unicode 18.0.0 Property Observation

For a **selected** Unicode Scalar Property, Kontrakt's common Property Observation has the exact meaning assigned to
that Property by **Unicode 18.0.0**, including its applicable normative Unicode Character Database (UCD) definitions
and derivation rules. Property identity and this immutable semantic version determine the result; whichever compiler
consumer performs the calculation must obtain the same value. A different JDK, ICU installation, host locale, or later
Unicode database must not silently change that value. This requirement does not revise the separately established
Version and applicability of an existing Canonicalization Law under ADR-0066 and ADR-0076.

Each selected Property must retain the input domain, value domain, possible values, and meaning of absent or undefined
values specified by Unicode. Binary, enumerated, catalog, numeric, string-valued, and miscellaneous Properties are not
interchangeable result types. A multiple-Script `Script_Extensions` observation denotes the specified set rather than
an arbitrarily chosen Script or a prescribed presentation order. `Numeric_Value` distinguishes an exact numeric value
from the absence of a numeric value; neither absent numeric data nor a nonnumeric Unicode data notation becomes the
number zero or an IEEE floating-point result by implication. Scalar-to-sequence mappings, contextual transformations,
and Properties defined for whole strings require their own exact input and result laws; their existence in Unicode does
not make them Scalar Property Observations automatically selected here.

A selected Scalar Property is defined for **every** legal Unicode Scalar, including scalars currently unassigned by
Unicode 18, private-use scalars, noncharacters, and default-ignorable values. Its value follows all applicable Unicode
18 definitions: explicit assignments, normative derived data, Property-specific defaults, range-dependent exceptions,
and `@missing` rules. The lack of an explicit UCD row is not an unspecified result and cannot be replaced by a
compiler-chosen default. `General_Category=Cn` does not entail that unrelated Properties are `false` or undefined;
for example, some unassigned scalar ranges have specific `Extended_Pictographic` values, and defaults for
`Bidi_Class`, `Line_Break`, and `East_Asian_Width` can vary by range. Surrogate code points are outside the common
Character domain under Section 3.2; their treatment in source-bound Code Unit operations remains distinct. A
Property's lawful `false`, `Unknown`, or absence result must not conceal an unrecognized or unselected Property request.

#### 3.4.2. Derived Classification and Selection Boundary

A common derived character classification is lawful only when its result follows exactly from the selected Unicode 18
Property definitions and their normative derivation relationships. An intermediate contributory Property, such as
`Other_Alphabetic`, is not by itself the complete derived `Alphabetic` Property. Likewise, `Script`,
`Script_Extensions`, `ID_Start`, `XID_Start`, and their related Properties may have different meanings and may not be
silently substituted. Existing Character and Text equality, indexing, and scalar validity are unchanged by these
classifications.

**Which** Unicode 18 Properties become independent common calculations remains an explicit, consumer-grounded
selection under Section 5.4. The proposed initial set of eighteen classification, whitespace, numeric, Script,
identifier, and character-state Properties is an investigation candidate, not an approved complete catalog or a claim
that all UCD Properties are available to V1. Property and algorithm consumers needed for case mapping, normalization,
segmentation, emoji, bidirectional processing, and security must be evaluated with their own exact requirements.
Supported Java/Kotlin Character/Text callables retain their actual source-level meaning and their separate SDK
coverage obligation under Section 3.3, regardless of whether the analogous Unicode Property is selected.

Property Observation establishes character facts, not the admissibility of a Contract authoring expression, an Input
value, a source identifier, or a security-sensitive spelling. Unicode identifier and security specifications can
inform their owning consumers without making this observation a new 1D Authority, Failure policy, Unicode security
judgment, or implicit Text transformation. Nothing here performs case folding, normalization, deletion of ignorable
scalars, or confusable rewriting in exact Text equality.

#### 3.4.3. Semantic Basis and Realization Separation

The common calculation's authority is its law and the fixed Unicode standard, **not** the implementation supplying
its result. The agreed implementation direction is a Kontrakt-owned Unicode library built from the official
version-pinned Unicode 18.0.0 data and the relevant normative Unicode algorithms. The source selection, data
interpretation, generation, correctness checks, library modules, data layout, and performance choices belong to
Character/Text Realization Design. UCD files alone do not replace UAX-defined algorithms, nor do they automatically
supply locale-dependent CLDR data or the behavior of a Java/Kotlin source callable. A table generated by Kontrakt,
an external reference implementation, or a target intrinsic is never a second semantic authority. This direction
requires no new 1D, HIR subject, IR family, compiler pass, global Unicode lookup service, or runtime policy engine.

### 3.5. Encoding, Decoding, and Invalid Data

### 3.6. Source-Bound Operations and Platform Qualification

### 3.7. Composition, Partiality, and Information Preservation

### 3.8. Use Through the Contract Pipeline

### 3.9. Compliance with Existing Frontend and Compiler-Product Boundaries

### 3.10. Realization, Analysis, and Optimization Obligations

### 3.11. JVM Backend and Runtime Conformance

### 3.12. Identity, Reuse, Diagnostics, and Verification

### 3.13. Version, Failure, and Resource Boundaries

### 3.14. V1 Scope, V2 Evolution, and Realization Design Ownership

## 4. Consequences

*To be completed after the decisions below are resolved. Consequences must describe actual guarantees and costs, not
repeat Section 3.*

## 5. Matters to Resolve Before Acceptance

This is a review inventory, not an approved feature list. Checked items record a resolved review disposition; unchecked
items remain open and do not imply supported functionality. Resolve each issue against ADR-0078's ownership and
qualification model. Where an existing ADR already decides the matter, refer to it rather than restating its law. A
decision belonging to another subsystem should result in a precise dependency or follow-on task, not an additional
authority in ADR-0080.

### 5.1. Define the shared semantic boundary

- [x] Establish the scope of common Text calculations under Section 3.1. Kontrakt governs the exact meaning of
  operations
  it defines, wherever the compiler uses them. Interpreting a Text value after lawful external computation does not
  qualify its producing Java or Kotlin operation for governed execution; ADR-0073 and ADR-0078 §3.3 remain applicable.
- [x] Use a finite Unicode Scalar Sequence as the common Text value meaning, consistently with ADR-0064. The bound on an
  Input `Text` presentation applies at the Input-owned boundary. Other exact calculations may produce finite results
  without inheriting that bound. Their own domains and resource conditions remain for Sections 5.2–5.5 and 5.13.
- [x] Apply common operation meanings without relocating a 1D judgment. ADR-0066 continues to own Canonicalization
  equivalence and representative judgments; a shared Unicode calculation cannot establish either judgment by itself.
- [x] Assign layer-specific information retention and legal observation to the owning IR and Compiler Product laws.
  Section 3.1 requires exact operation results, while ADR-0071 and ADR-0075 govern the meaning and legal consumption of
  products that preserve them. The concrete consumers and retention obligations remain to be audited in Sections
  5.7–5.12.
- [x] Define common Character and Text operation meaning in this ADR, without a second Catalog or new Authority. Use
  the approved Contract ADRs for their own judgments. The Canonicalization Catalog Design is available for reviewing
  existing Character and String calculations, but is not a normative source for this ADR and cannot supply authority
  merely by being referenced. Implementation catalogs and conformance machinery remain Design responsibilities.
- [x] Apply common value and operation meaning consistently wherever it is used by the compiler. Source identifiers,
  diagnostic presentation, source encoding, and target Classfile text still follow their owning language or artifact
  rules. Reusing a common calculation does not transfer those owners' authority to ADR-0080.

### 5.2. Determine the character and text value domains

- [x] Fix the meaning of a Unicode Scalar Value, including the exclusion of surrogate code points. State how the scalar
  domain differs from a Unicode Code Point, a UTF-16 Code Unit, and an octet; do not infer a conversion between them.
- [x] Decide which exact observations of a legal Text value are available to shared operations. For example, an empty
  sequence, U+0000, combining marks, and supplementary scalars remain distinct values unless a specified operation
  changes them.
- [x] Examine whether noncharacters, unassigned scalars, private-use characters, default-ignorable characters, and
  bidirectional controls remain legal scalar inputs. Their Unicode properties or security implications must not silently
  make them invalid Text.
- [x] Specify whether a character-like operation consumes one scalar or a larger unit. A visible grapheme can contain
  several scalars, while a Java `char` is one UTF-16 Code Unit; neither can be substituted merely because both are
  called a character.
- [x] Require the same legal observations once a Text value's meaning is determined, regardless of when its physical
  representation is produced. The compiler must preserve that meaning without fixing materialization timing, buffer
  ownership, or the point of physical decoding in this ADR.
- [x] Decide how result length growth interacts with a finite admitted input. NFC, case folding, and text concatenation
  can increase length; a physical buffer limit must not invent a shorter semantic result or a new law-owned refusal.
- [x] Separate empty Text as a legal value from absence, malformed encoding, and an unsuccessful calculation. This ADR
  does not decide their Failure attribution or outcomes; those remain with ADR-0057 and the applicable owning laws.
  None may be silently represented by an invented Text sentinel.

### 5.3. Close the exact basic operations

- [ ] Complete the common basic operation scope from three independent requirements: direct interpretation of supported
  Java/Kotlin 1D authoring expressions, mapping those expressions into the receiving 1D's permitted meaning, and
  Kontrakt's own compiler calculations. Account for foreseeable V2 uses without letting V1 realization difficulty
  choose the law. For each selected operation, close its operand domain, result domain, exact observations, and
  unsuccessful-result conditions without taking Failure authority. Literal Replace match consumption, empty-match
  replacement, and result formation remain independent open questions; the selected Split law does not answer them.
  A law is neither blanket approval of a Host String API family nor a declaration of V1 execution support. Catalog
  coverage and implementation details remain separate.
- [x] Define a scalar-count operation separately from a byte count, UTF-16 Code Unit count, or grapheme count. For
  example, the Java `String.length()` result for a supplementary scalar differs from the scalar count. Section 3.3.1
  closes Scalar Count by reference to the existing exact Text sequence-length observation; it does not introduce a
  second Text value law.
- [x] Determine the index unit and boundary conditions for indexing and slicing. Specify whether a slice can fail, how
  an empty slice behaves, and whether the operation can bisect a multi-unit encoded character. Sections 3.3.1–3.3.3
  close Text-relative Scalar Index/Boundary, Element Access, half-open Scalar Slice, and exact UTF-16 boundary
  correspondence without making Source Location or malformed Host string calculation a common Text value.
- [x] Define exact concatenation and its result law. Address empty operands and length growth without embedding rope,
  builder, or allocation decisions in the semantics. Section 3.3.7 closes the unique Scalar Sequence result, exact
  length, empty identity, and associativity for already determined values; it also closes finite nonnegative repetition
  as a derived calculation without deciding source operand evaluation or physical capacity.
- [x] Decide which comparison operations are needed beyond exact scalar-sequence equality. A lexicographic scalar
  ordering, locale collation, and case-folded equality are different relations and must not share an unspecified
  comparator. Section 3.3.4 selects an exact scalar-numerical lexicographic total order consistent with the existing
  equality. Locale collation and case-dependent comparisons remain separately qualified operations.
- [x] Define searching in terms of a selected sequence and index unit. For example, an `indexOf`-like operation must
  determine the result for an empty needle, absence of a match, and overlapping occurrences. Section 3.3.5 closes
  exact contiguous Scalar matching, ordered and overlapping Hits, empty-pattern boundaries, First/Last selection,
  No Match, and out-of-domain range distinctions. Source-bound `indexOf` semantics remain separately resolved.
- [ ] Review prefix, suffix, containment, repetition, split, and join individually against the three Section 3.3
  requirements. A common law must be justified by an exact calculation needed for supported source interpretation,
  lawful 1D mapping, or independent compiler work; its existence in a Host library is not sufficient. Keep callable
  coverage and the physical implementation inventory in their separate Catalog and Design work. Section 3.3.6 derives
  exact prefix, suffix, and containment from matching; Section 3.3.7 closes derived repetition; Section 3.3.8
  closes ordered literal delimiter selection, consumption, partition results, empty delimiters, and explicit result
  limits. Join and the exact SDK/1D mapping of the selected calculations remain to be reviewed, so this combined
  inventory item remains unchecked.
- [ ] Decide how byte-to-text parsing and text-to-domain formatting are divided among owners. Numeric formatting follows
  ADR-0078; temporal interpretation belongs to ADR-0082 when ratified; URI and path interpretation must not be silently
  absorbed into generic Text operations.

### 5.4. Resolve Unicode-specific operations and semantic dependencies

- [ ] Separate canonical normalization from compatibility normalization. Define any selected common NFC, NFD, NFKC,
  or NFKD calculation against its precise Unicode standard and immutable semantic basis. Preserve the existing
  Canonicalization laws' equivalence and representative choices without taking the Catalog Design as the common
  calculation's authority.
- [ ] Determine which general Unicode normalization operations are needed outside a selected Canonicalization Law.
  For each selected operation, specify the exact result and governing Unicode standard directly under this ADR.
  Reusing a calculation does not grant Canonicalization authority or adopt a Design document's implementation profile.
- [ ] Distinguish simple case mapping, full case mapping, default full case folding, Turkic-sensitive behavior, and
  locale-sensitive mapping. A case-insensitive result must not be inferred from unspecified lowercasing.
- [ ] Review contextual mappings such as the Greek final sigma and scalar-to-multiple-scalar expansions. If an operation
  depends on neighbors or locale, those determinants must be explicit before that operation is supported.
- [ ] Examine the seven Unicode 18.0.0 Canonicalization law profiles described in the Catalog Design §8.6 and verify
  their Character and Text calculations against the governing Unicode standards. Use that material to identify potential
  common operations, but do not import its normative claims or realization assumptions. Each Canonicalization law keeps
  its separate Version, `E_L`, `C_L`, and coverage authority under ADR-0066 and ADR-0076.
- [ ] Select the actual common classification Properties required by supported Source Interpretation, lawful 1D
  consumers, and independent compiler calculations. Section 3.4 already closes the Unicode 18.0.0 Property
  Observation domain, typed result, normative derived/default-value, and classification-derivation requirements;
  it does **not** approve the proposed eighteen-Property candidate set. Complete the per-Property input/result and
  consumer inventory without replacing a Java/Kotlin callable by a Unicode 18 lookup.
- [ ] Examine grapheme, word, sentence, and line segmentation as separate candidates. Their context-sensitive rules may
  require state across chunks; supporting Unicode scalars alone does not imply support for these operations.
- [ ] Treat locale collation, confusable detection, bidirectional display safety, and regular expressions as separately
  qualified candidate features. Do not make any of them default Text equality, and do not claim V1 support without a
  closed law and use case.
- [ ] Close the remaining selected Unicode operations' exact version, data, and algorithmic determinants. Section 3.4
  fixes Unicode 18.0.0 for selected common Property Observations; the corresponding decisions for later case mapping,
  normalization, segmentation, and other operations still require individual closure. The Unicode version fixed for
  an existing Canonicalization Law must not be automatically upgraded when a system library changes.
- [ ] Review Unicode stability guarantees per operation rather than globally. A compatibility claim for normalization
  must not be reused as proof that newer property tables, case mappings, segmentation results, or security data are
  unchanged.

### 5.5. Define encoding and decoding semantics without designing file I/O

- [ ] Determine which encoding and decoding operations Kontrakt needs as governed calculations. Do not equate
  source-file decoding, API transport encoding, compiler cache serialization, and a user-visible Text conversion merely
  because they all use UTF encodings.
- [ ] Examine the text-bearing artifacts that V1 already writes to disk, rather than treating persistence as a V2-only
  concern. Generated source, executable Classfiles, diagnostic records when emitted, and test or reproducer outputs can
  have different encoding requirements. The owning Artifact and Backend Designs decide their formats and publication
  procedures; ADR-0080 must only constrain any shared Text meaning they claim to preserve.
- [ ] For each selected UTF-8, UTF-16, or UTF-32 operation, define the exact mapping between octet or code-unit
  sequences and Unicode Scalar Text. Byte order and byte-order marks must be explicit where they affect the result.
- [ ] Define strict failure for malformed encodings. Include overlong UTF-8, surrogate encodings, incomplete sequences,
  invalid continuation bytes, and values beyond U+10FFFF; specify whether failure reports a position or only
  non-success.
- [ ] Decide whether replacement or repair is supported as a separate lossy operation. If it is, specify which malformed
  subsequence is replaced, the exact replacement result, and the reported error information; do not silently change
  strict decoding into repair.
- [ ] Define the relationship between an already valid Text value and its various physical encodings. Different legal
  byte encodings of the same scalar sequence must not become different Text meanings unless the consuming law explicitly
  observes bytes.
- [ ] Determine the required observations of a streaming decoder. A multi-byte scalar split at a chunk boundary must
  produce the same result and failure information as decoding the complete input, subject to the selected streaming law.
- [ ] Separate encoding validity from normalization state and from security acceptance. A valid UTF-8 sequence can
  encode Text that is not NFC and may contain confusable or bidirectional control characters.
- [ ] Keep JVM Modified UTF-8 outside the ordinary UTF-8 datum law. It belongs to Classfile representation; decide which
  semantic obligations the Backend must satisfy when emitting a corresponding constant.
- [ ] Review exact Text round-trips across a binary or textual artifact boundary when such a round-trip is required. A
  serializer must not confuse an empty value with absence, discard U+0000, or merge adjacent fields because its framing
  or escaping is ambiguous. Record the semantic obligation without selecting an on-disk codec.

### 5.6. Qualify Host operations without redefining Host language semantics

- [ ] Identify the exact Java or Kotlin callables whose Text behavior Kontrakt must understand for a governed
  calculation or a compiler transformation. Recognizing a method name or return type does not qualify the operation or
  its entire library. Account for the complete relevant standard Character/Text API of each declared supported Java
  and Kotlin SDK profile, including overload, extension, and source resolution. Separately selected third-party
  callables do not carry a whole-library coverage obligation. The Java SE 27 and Kotlin/JVM 2.4.21 Design inventories
  are investigation inputs, not conformance evidence or a declaration of executable V1 support.
- [ ] Preserve the resolved source operation's index unit, null behavior, defined domain, exception behavior, and
  evaluation requirements. Java `substring` and a Kontrakt scalar-index slice cannot be equated merely because both
  return strings. Likewise, Java Regex split, Kotlin literal or Regex split, and common ordered literal Split require
  their own exact matching, delimiter priority, zero-width, limit, and empty-result rules. Java/Kotlin replacement
  callables retain their distinct literal, Regex, and callback semantics; no universal Replace consumption order is
  imported from the common Split calculation. Unicode-defined calculations follow their explicitly selected Unicode
  standard and version, rather than acquiring a Host or generic Split/Replace policy.
- [ ] Review Java and Kotlin string concatenation separately from pure Text concatenation. Operand evaluation and
  source-defined string conversion can invoke user code; the optimizer must not erase or reorder those effects.
- [ ] Determine whether a Host operation permits multiple legally observable results because of locale, library version,
  provider data, or implementation freedom. Any deterministic refinement used inside governed processing must be
  justified as in ADR-0078 §3.3.
- [ ] Separate an already produced Host string admitted as a value from the producer's callable. The fact that an
  external library produced valid Text does not make the library executable inside Kontrakt's governed pipeline.
- [ ] Verify that an optimization involving a Source-Bound Text operation uses sufficient evidence of its exact meaning.
  When V1 Classfile acquisition or an available summary does not provide that evidence, the optimization must not assume
  it. Acquisition changes belong to Realization Design and the existing Whole-Machine research.
- [ ] Preserve ADR-0073's platform eligibility and airlock decisions. A recognized Java string operation must not become
  an exception that admits unrelated capabilities, resource access, or arbitrary call chains.

### 5.7. Check compliance with existing Frontend and HIR boundaries

- [ ] Verify that the Frontend and Diagnostic consumers preserve the distinctions they already own between source byte
  offsets, decoded positions, and literal values after escape processing. ADR-0080 does not prescribe a different
  SourceManager representation.
- [ ] Check that `.kontrakt` identifier normalization and name equality continue to follow the owning Frontend language
  rules. Rules used by another language, such as Rust identifier NFC, do not become universal Text equality or HIR
  identity laws.
- [ ] Check literal parsing, adjacent-literal combination, escape syntax, and invalid-source rejection against the
  existing `.kontrakt` grammar. This ADR must not revise source interpretation to suit a general Text operation.
- [ ] Apply ADR-0071 to any Text meaning already present in a resolved 1D Definition Candidate. The owning Frontend and
  1D vocabulary determine its candidate meaning, and the existing HIR Resolution Invariant governs its completeness.
  ADR-0080 neither adds Text-specific HIR fields nor changes the HIR handoff.
- [ ] Keep the selection and resolution of an existing Unicode Exact Law under the owning 1D and ADR-0071. A source
  label such as `normalize` cannot replace exact resolution, but ADR-0080 does not establish a second Unicode Law
  reference or binding protocol in HIR.
- [ ] Follow ADR-0071 for the relationship between resolved candidate meaning and source provenance. Changes to source
  spelling or SourceSpan do not by themselves authorize changes to HIR meaning; provenance storage and V2 reuse remain
  with their existing owners.
- [ ] Verify that a malformed source, an unsupported Text operation, and an illegal 1D definition are attributed to
  their correct compiler or Contract boundary. The Frontend must not fabricate an Admission outcome to represent its own
  inability to resolve a symbol.

### 5.8. Audit Establishment and the Contract pipeline

- [ ] Trace one Text-bearing definition from Input presentation through the selected 1D authorities. Determine exactly
  which authority requests each Text calculation and who establishes the resulting judgment; refer to ADR-0063–0068
  instead of duplicating their pipelines here.
- [ ] For Canonicalization, distinguish the Exact Law's declared equivalence and representative from the shared
  calculation that may implement them. A compiler proof that a string is already NFC does not by itself establish a
  Canonicalization Occurrence.
- [ ] For Admission, decide how a partial Text predicate can be used without invalidating its own guard or short-circuit
  behavior. Independent Admission conditions must not acquire implicit control dependencies from a compiler shortcut.
- [ ] For Lowering and Fact Establishment, inspect whether a resulting Text is interpreted from already established
  material or is formed by an exact governed operation. The same Java runtime type does not prove the same Contract
  relation.
- [ ] Check that Invariant, Policy, Governance, Publication, and Output may consume Text results only under their
  existing authorities. Their domain-specific comparisons and outward formatting must not be inferred from the generic
  Text datum.
- [ ] Determine whether a Text calculation can be physically reused across two independent 1D judgments. Reusing the
  calculation must preserve legal observations and ADR-0075 validity; it must not merge the two authority-owned
  judgments or their evidence.
- [ ] Verify how Text-specific failures are attributed when an operation is out of domain, a resource cap is reached, or
  a result is not legally usable. ADR-0057, ADR-0051, and ADR-0052 already own the relevant failure or resource
  boundaries.

### 5.9. Audit Realization IR, execution formation, and Whole-Machine analysis

- [ ] When a Text transformation relies on existing Realization Body IR, check that available evidence covers the
  required source behavior. This includes conversion, `invokedynamic` concatenation, exception paths, and user callbacks
  where relevant. Missing evidence restricts the transformation; it is not an ADR-0080 requirement to redesign the IR.
- [ ] Verify that Realization Admission and Closure Verification assess prohibited calls before an optimization could
  erase them. A subsequently unreachable forbidden call must not retroactively make an inadmissible realization legal.
- [ ] Check the legal use of any Text properties proved by Contract-Aware Analysis from established Contract Material.
  Their producer-owned scope and consumption follow ADR-0075. No new analysis product is required solely because
  ADR-0080 governs the Text operation.
- [ ] Inspect how Execution Formation distinguishes a previously established Text result from an as-yet-unperformed
  calculation. Materializing the same scalar sequence is not a license to skip a required authority judgment.
- [ ] Check transformations against the existing Contract-Aware Execution IR and its established preservation
  obligations. Legally observable Text results, failure reachability, and source evaluation effects must remain correct.
  This ADR does not require a new Execution IR field or structure.
- [ ] If existing Whole-Machine Summaries provide Text properties such as exact constants or length bounds, use them
  only within their proven scope under ADR-0075. Additional summary content and its profitability belong to
  Whole-Machine Analysis Design, not this ADR.
- [ ] Do not justify a Text transformation using a summary that lacks the necessary evidence. Whether an existing
  consumer performs selective Body Opening or conservatively skips that transformation follows the Whole-Machine
  architecture, not a new ADR-0080 procedure.
- [ ] Separate the information needed to check original realization legality from the information needed after
  optimization. A rewritten legal program does not replace the verification of the original admitted execution
  structure.

### 5.10. Decide transformation laws and validation obligations

- [ ] State the general preservation test for Text transformations. Agreement on a final Boolean value is insufficient
  when a legally observable Text result, evaluation effect, or failure condition differs.
- [ ] Examine which string constant-folding transformations are lawful. A result computed with the compiler host's
  Unicode tables is not automatically valid for an Exact Law bound to a different profile.
- [ ] Review fusion and reassociation of normalization, case folding, search, and concatenation. For example,
  normalizing two fragments separately and appending them may differ from normalizing the joined sequence because of a
  combining boundary.
- [ ] Determine which preconditions justify omission of a repeated validation or normalization calculation. A proof
  about the exact current value can permit physical reuse, but must not be treated as a new Contract judgment or a
  broader law.
- [ ] Review slicing, comparison, length checks, and search rewrites for unit mismatches. A UTF-16 offset cannot be
  substituted for a scalar index, even if it works on ASCII test data.
- [ ] If an E-graph or equality-saturation optimizer is considered, identify the exact observable meaning required
  before a Text rewrite may merge expressions into one equivalence class. Equal normalized results do not prove equal
  original scalar sequences, and a rewrite must also preserve relevant failure and evaluation behavior.
- [ ] Examine whether Text-derived E-class analysis could justify a rewrite without becoming a Contract judgment. The
  meaning of a proved precondition belongs to the relevant analysis producer; the E-graph implementation, congruence
  maintenance, and extraction policy remain optional Compiler Design choices.
- [ ] Require any Text-derived analysis consumed by a transformation to be valid for that exact use, following ADR-0075.
  The recording and invalidation mechanisms belong to Compiler Analysis Design. V1 local analysis and V2 incremental
  reuse share the same semantic obligation.
- [ ] Distinguish legality from profitability. SIMD UTF validation, string-view storage, short-string representation,
  and JDK Intrinsics are performance candidates for Design; none can authorize a semantic change.
- [ ] Define the minimum transform-validation evidence required for a supported Text rewrite. Directed fuzzing and
  translation validation can target boundary cases, while a reference judgment must remain independent enough to detect
  a shared implementation defect.

### 5.11. Audit JVM planning and runtime handoff

- [ ] Determine how an exact scalar Text result is legally represented as a Java `String` when required. Java's UTF-16
  Code Unit observations, including the possibility of unpaired surrogates in general Host strings, must not be silently
  mistaken for the Contract Text domain.
- [ ] Check how Classfile constants use Modified UTF-8, including its encoding constraints and length limit. An
  otherwise legal Text value may require a different emission strategy without becoming an invalid Contract value.
- [ ] Check that emitting and publishing V1 disk artifacts preserves their exact required bytes and does not publish a
  partially written or incorrectly encoded result as successful output. A Classfile encoding limit, disk write failure,
  and Contract Text rejection are different conditions; the Backend and Artifact owners determine their handling under
  existing failure and publication rules.
- [ ] Review Java's `StringConcatFactory` and any Kotlin-lowered string templates as target execution structures. Their
  runtime behavior must preserve the qualified source operation; Classfile shape alone need not prove a higher-level
  pure Text law.
- [ ] Determine whether the Backend relies on a JVM intrinsic, helper method, or generated code for a governed Text
  operation. The chosen path needs evidence of conformance to the exact operation law rather than an assumption based on
  one JDK build.
- [ ] Verify that stack, descriptor, and ABI representations obey JVM typing rules without creating a second Contract
  Text type system. Such target correctness belongs to Backend Design and the deferred ADR-0079 type-information work.
- [ ] Decide which Backend changes require renewed execution qualification. For example, changing an encoding helper or
  target JDK version may affect actual conformance even when the governing Text law is unchanged.

### 5.12. Audit identity, compiler products, and V2 incremental reuse

- [ ] Identify which equality is used at each Text-containing identity boundary. Semantic Text equality, Input
  Presentation Sameness, Canonicalization equivalence, source-token spelling equality, HID verification, and bytewise
  storage equality must not be conflated.
- [ ] Separate a hash match from an exact Text equality judgment. Compare collision handling for local hash tables,
  persistent HID lookup, and compiler result fingerprinting. A matching hash must not by itself merge different scalar
  sequences or substitute for the owning producer's full equality evidence.
- [ ] If an E-graph is adopted, distinguish its local hashconsing and equivalence-class identity from stable semantic
  identity or HID. Reusing an E-node address or hash bucket cannot establish a Contract equivalence, and an E-class
  merge requires a justified rewrite or congruence relation.
- [ ] Determine when physical interning or shared storage may reuse one Text payload. Reuse is legal only if all
  distinctions required by the consumers remain recoverable; a normalized key cannot silently replace a distinct
  original scalar sequence.
- [ ] Review how Compiler Canonical Encoding represents Text for fingerprinting without borrowing Canonicalization
  Contract authority. A serialized-byte representation is an implementation protocol unless an owning law specifically
  makes it meaning.
- [ ] Examine Text-bearing composite keys and their serialization boundaries. The encoding must preserve field
  boundaries and value kinds so that different records do not acquire identical byte streams through ambiguous
  concatenation. The actual byte format and collision-resolution algorithm belong to Identity and Storage Design.
- [ ] Investigate any database or ordered-index use of Text-bearing keys in V2. A physical key comparator may use byte
  ordering, a decoded value order, or a producer-defined comparison, but it must implement the owning key semantics
  consistently. Changing normalization, comparison, or encoding rules can invalidate an existing index and may require
  migration or rebuilding; do not select a database in this ADR.
- [ ] Check cached Text calculations against ADR-0075 validity requirements and the exact operation meaning. A Unicode
  profile or immutable data revision matters when it can change the legal result. The cache key and dependency
  representation remain Compiler Engine Design decisions.
- [ ] Trace Text-containing inputs and results through V1 query orchestration and possible V2 semantic-database storage.
  Determine which producer owns the queried result's equality and which dependencies make it valid. Query keys, disk
  keys, fingerprints, and Contract Definition identities may reference the same Text without becoming interchangeable
  identities.
- [ ] Test the scope of early cutoff with Text-derived results. For example, changing a literal's escape spelling may
  leave its resolved Text unchanged, while changing a scalar sequence can leave the result of an explicit NFC operation
  unchanged. A downstream result may be reused only if the equality and dependencies of that exact result are satisfied;
  source provenance or diagnostics may still change.
- [ ] Separate a semantic Text product from its Source Provenance. A revision may change escape spelling or line
  locations while the exact resolved Text result stays the same; legal reuse must still update provenance where
  required.
- [ ] Audit result publication across immutable generations. A value produced under one valid semantic basis must not be
  silently reused in another generation if its required dependencies have changed.
- [ ] Investigate whether fine-grained Text facts are worth caching. V2 may repair only affected portions or recompute
  an inexpensive operation; the ADR must not force a query per character, a particular dependency graph, or rustc-style
  Red-Green scheduling.
- [ ] Check persistent cache schema and invalidation boundaries separately from Text operation Version. A stored product
  can be corrupt or incompatible even when the governing semantic law itself has not changed.
- [ ] Review reopening a previously stored Text-bearing compiler product. Decoding, integrity checks, schema
  compatibility, generation validity, and lawful consumption are distinct checks; failure at one boundary must not be
  concealed as a valid cache hit or treated automatically as a failed 1D judgment. The store's recovery and retry policy
  belongs to its owner.
- [ ] Compare semantic-result equality with physical artifact-byte equality. Two storage representations may encode the
  same Text while an artifact producer separately requires deterministic output bytes; two identical blobs may also be
  unusable under different schema or target conditions. Determine which equality each publication or early-cutoff
  consumer actually requires.
- [ ] Distinguish clean, warm-cache, incremental, parallel, and cancelled-and-retried compilation. Given the same
  applicable law and valid inputs, each execution path must preserve the same semantic Text results and published
  Contract Material.

### 5.13. Audit diagnostics, tooling, security, and resources

- [ ] Verify that Diagnostics and IDE consumers use their own position-conversion rules. Source byte offsets, UTF-16
  positions, scalar indices, grapheme positions, and displayed columns are not interchangeable. ADR-0080 defines no new
  SourceSpan or LSP representation.
- [ ] Check preservation of offending input and error position for malformed Unicode. Diagnostics may quote or escape
  dangerous text, but their presentation must not rewrite the underlying error or source evidence.
- [ ] Examine persisted diagnostic evidence separately from rendered diagnostic messages. A renderer can escape a
  control character or replace an unprintable glyph for display, but reopening or comparing structured diagnostics must
  not depend on the rendered spelling as its sole identity or lose the original offending bytes when those bytes are
  required as evidence.
- [ ] Decide whether confusable identifiers, bidi controls, or mixed-script warnings are compilation policy,
  diagnostics, or a separately selected semantic operation. A security warning must not silently introduce a new global
  Text equality relation.
- [ ] Examine resource exhaustion during normalization, case folding, transcoding, and pattern matching. An execution
  limit must not make an exact operation return a truncated but successful result or an invented Contract rejection.
- [ ] For any proposed pattern or regex support, define the admitted pattern language before comparing engines.
  Backtracking complexity and pathological input cost cannot be delegated to a hidden arbitrary Host callable.
- [ ] Decide what an IDE or incremental editor needs from Source Provenance after changes. The ability to refresh
  location information separately from unchanged semantic Text must remain possible without prescribing an IDE cache
  implementation.
- [ ] Determine when a Text-related diagnostic result can survive V2 early cutoff while its source locations or
  rendering must be regenerated. The diagnostic producer's result validity and the provenance/rendering consumers'
  validity may differ; this review must not create a new diagnostic caching authority.
- [ ] Determine how error reports distinguish unsupported semantics, failed source qualification, nonconforming
  execution, invalid input, and resource stops. Each has a different owning boundary under existing ADRs.
- [ ] Review failure attribution across source decoding, a governed Text calculation, backend encoding, disk
  publication, and restored-cache validation. A malformed input, an unsuccessful Contract judgment, and a corrupt
  compiler artifact are not the same failure origin. Use ADR-0057, ADR-0075, and the existing compiler diagnostic
  boundaries rather than introducing an ADR-0080-wide failure type.

### 5.14. Close verification, V1 scope, and V2 extension seams

- [ ] Enumerate the official Unicode conformance material required for each selected operation. The seven already
  admitted Unicode 18.0.0 Canonicalization Laws need implementation qualification independent from the fact that their
  Exact Laws are approved.
- [ ] Design adversarial Text witnesses that distinguish scalar sequences, malformed UTF encodings, combining-boundary
  normalization, contextual case mapping, and differing index units. These must test law-preserving behavior, not merely
  common ASCII examples.
- [ ] Determine which observations are suitable for differential checks against Java, Kotlin, ICU, and independent
  reference implementations. A Host library can be an oracle only for a precisely matching supported operation and
  semantic profile.
- [ ] Include mutation, fuzzing, and transformation-directed tests that actually trigger the intended compiler
  optimization. A test that never reaches the Text rewrite does not validate that rewrite.
- [ ] If Text rewrites are explored through an E-graph, test invalid equivalence proposals as well as valid rewrites. In
  particular, include normalization across concatenation boundaries, equality of normalized output versus original Text,
  hash collisions, and rewrites that wrongly erase a required failure or Source-bound effect.
- [ ] Exercise V1 artifact encoding and disk-publication paths with round-trip and fault-injection tests. Include a text
  value that exceeds one JVM constant-pool entry, truncated serialized data, malformed stored Text, interrupted output,
  and output whose bytes differ from the applicable target format. Check the owning failure classification as well as
  the absence of false successful publication.
- [ ] Compare clean, warm, and incremental results using controlled Text changes when V2 reuse is implemented. Vary
  source spelling, exact scalar content, Unicode operation profiles, stored-product schema, and deliberately colliding
  fingerprints; verify both legitimate early cutoff and required invalidation without assuming a particular query
  scheduler or database.
- [ ] Define what V1 must close for each claimed supported Text operation: exact meaning, relevant source qualification,
  boundary integration, conformance, and a lawful executable route. An operation with a complete law but no conforming
  realization remains meaningful yet unsupported for execution. Audit the selected Java/Kotlin standard Character/Text
  SDK profiles for missing callables and distinguish unsupported but understood uses from compiler coverage gaps;
  only explicitly selected third-party callable/version profiles carry an external-library support claim.
- [ ] Identify V2 extensions that require new semantic decisions, such as an additional Unicode profile or a new
  segmentation operation. Distinguish these from implementation-only extensions such as finer incremental queries,
  alternative storage, and SIMD execution.
- [ ] Complete the corresponding Realization Design for the agreed Kontrakt-owned Unicode library using official,
  version-pinned Unicode data and normative algorithms, including input provenance, generation, conformance, release,
  and licensing evidence. String tables, primitive slabs, buffer ownership, Unicode database loading, streaming
  kernels, JDK intrinsics, and cache repair algorithms must not become normative Text meaning through this ADR.
- [ ] Decide what evidence would justify moving an item from this review inventory into the normative Decision. Where
  ADR-0064, ADR-0066, ADR-0073, ADR-0075, ADR-0076, or ADR-0078 already closes the matter, resolve it with a
  cross-reference instead of restating the law.