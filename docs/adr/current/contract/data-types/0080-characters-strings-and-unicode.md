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
mandatory Text engine, new IR family, or physical string layout. Sections 3.1–3.3 establish the shared boundary, value
domain, and obligations for interpreting and selecting basic operations. Section 5 still contains unresolved exact
operation laws and qualification questions; none is approved for execution merely by appearing in this Draft.

## 3. Decision

*Section 3.1 records the shared boundary resolved in the first review group. Section 3.2 records the agreed Character
and Text value domain, and Section 3.3 states the contract-level obligations for interpreting and selecting basic
operations. The precise operation scope and unresolved result conditions remain under Section 5.3; Sections 3.4–3.14
remain drafting headings. This Draft does not declare those unresolved operations supported.*

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
1D law, the expression cannot be silently accepted on the strength of a Host callable's existence.

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
without an implicit normalization or case conversion. Indexing, slicing, comparison, and search likewise need their
own precisely determined units, applicable domains, and results. Exact sequence equality does not confer an ordering;
if scalar lexicographic order or exact contiguous-subsequence search is selected, its relation must be defined without
silently adopting collation, case folding, or a Host search convention. Section 5.3 retains the decisions about the
particular operations and boundary cases that are not yet closed.

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

### 3.4. Unicode Operations and Fixed Semantic Bases

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
  unsuccessful-result conditions without taking Failure authority. A law is neither blanket approval of a Host String
  API family nor a declaration of V1 execution support. Catalog coverage and implementation details remain separate.
- [ ] Define a scalar-count operation separately from a byte count, UTF-16 Code Unit count, or grapheme count. For
  example, the Java `String.length()` result for a supplementary scalar differs from the scalar count.
- [ ] Determine the index unit and boundary conditions for indexing and slicing. Specify whether a slice can fail, how
  an empty slice behaves, and whether the operation can bisect a multi-unit encoded character.
- [ ] Define exact concatenation and its result law. Address empty operands and length growth without embedding rope,
  builder, or allocation decisions in the semantics.
- [ ] Decide which comparison operations are needed beyond exact scalar-sequence equality. A lexicographic scalar
  ordering, locale collation, and case-folded equality are different relations and must not share an unspecified
  comparator.
- [ ] Define searching in terms of a selected sequence and index unit. For example, an `indexOf`-like operation must
  determine the result for an empty needle, absence of a match, and overlapping occurrences.
- [ ] Review prefix, suffix, containment, repetition, split, and join individually against the three Section 3.3
  requirements. A common law must be justified by an exact calculation needed for supported source interpretation,
  lawful 1D mapping, or independent compiler work; its existence in a Host library is not sufficient. Keep callable
  coverage and the physical implementation inventory in their separate Catalog and Design work.
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
- [ ] Examine the seven Unicode 17.0.0 Canonicalization law profiles described in the Catalog Design §8.6 and verify
  their Character and Text calculations against the governing Unicode standards. Use that material to identify potential
  common operations, but do not import its normative claims or realization assumptions. Each Canonicalization law keeps
  its separate Version, `E_L`, `C_L`, and coverage authority under ADR-0066 and ADR-0076.
- [ ] Determine whether classification operations such as whitespace, decimal digit, identifier property, or general
  category are required. If supported, specify their exact Unicode property and version rather than relying on the
  current JDK's `Character` tables.
- [ ] Examine grapheme, word, sentence, and line segmentation as separate candidates. Their context-sensitive rules may
  require state across chunks; supporting Unicode scalars alone does not imply support for these operations.
- [ ] Treat locale collation, confusable detection, bidirectional display safety, and regular expressions as separately
  qualified candidate features. Do not make any of them default Text equality, and do not claim V1 support without a
  closed law and use case.
- [ ] Identify which Unicode version or immutable data profile determines each selected operation. The Unicode version
  fixed for an existing Canonicalization Law must not be automatically upgraded when a system library changes.
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
  its entire library.
- [ ] Preserve the resolved source operation's index unit, null behavior, defined domain, exception behavior, and
  evaluation requirements. Java `substring` and a Kontrakt scalar-index slice cannot be equated merely because both
  return strings.
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
  admitted Unicode 17.0.0 Canonicalization Laws need implementation qualification independent from the fact that their
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
  realization remains meaningful yet unsupported for execution.
- [ ] Identify V2 extensions that require new semantic decisions, such as an additional Unicode profile or a new
  segmentation operation. Distinguish these from implementation-only extensions such as finer incremental queries,
  alternative storage, and SIMD execution.
- [ ] Assign concrete physical choices to the appropriate Design documents. String tables, primitive slabs, buffer
  ownership, Unicode database loading, streaming kernels, JDK intrinsics, and cache repair algorithms must not become
  normative Text meaning through this ADR.
- [ ] Decide what evidence would justify moving an item from this review inventory into the normative Decision. Where
  ADR-0064, ADR-0066, ADR-0073, ADR-0075, ADR-0076, or ADR-0078 already closes the matter, resolve it with a
  cross-reference instead of restating the law.