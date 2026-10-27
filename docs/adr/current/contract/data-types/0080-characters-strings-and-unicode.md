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
- `docs/design/canonicalization/built-in-law-catalog/canonicalization-built-in-law-catalog-design.md`, especially §8.6
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md` — Working Design, not an Accepted ADR
- `docs/todo/roadmap/kontrakt-v2-reference-architecture-and-v1-foundations.md` — Reference Architecture, not an Accepted
  ADR

---

## 1. Context

ADR-0078 establishes the precedent for defining shared value and operation semantics without creating a new 1D Contract
or fixing a realization. ADR-0064 already fixes the meaning and Presentation Sameness of Input `Text`. ADR-0066 and
ADR-0076 retain the authority of separately selected Exact Canonicalization Laws.

This ADR will address the shared Character and Text meaning needed by lawful Contract processing and its compiler
consumers. Each 1D retains its own judgment, applicability, established Material, and legal observations. Its use of a
shared Text operation must nevertheless preserve that operation's meaning.

## 2. Problem

A compiler encounters characters and strings as source spelling, resolved values, operation operands, analysis evidence,
published Material, and target representations. Those are not interchangeable. Without explicit shared operation laws,
one consumer can accidentally change an observable result through Unicode-library defaults, conversion, string
comparison, compiler rewriting, or representation changes.

The existing Contract pipeline and the planned V1/V2 compiler architecture need common semantic requirements, not a
mandatory Text engine, new IR family, or physical string layout. The items in Section 5 remain open for review. This
outline does not itself ratify an operation or implementation.

## 3. Decision

*Decision text reserved. The following headings are a drafting structure, not adopted laws.*

### 3.1. Shared Meaning and Contract Ownership

### 3.2. Character and Text Value Interpretation

### 3.3. Exact Text Operations and Observable Results

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

This is a review inventory, not an approved feature list. Resolve each issue against ADR-0078's ownership and
qualification model. Where an existing ADR already decides the matter, refer to it rather than restating its law. A
decision belonging to another subsystem should result in a precise dependency or follow-on task, not an additional
authority in ADR-0080.

### 5.1. Define the shared semantic boundary

- [ ] Determine exactly which Text computations Kontrakt itself governs. Separate a value interpreted after lawful
  external computation from an operation executed inside a governed judgment, following ADR-0078 §3.3. Do not make every
  Java or Kotlin string API a Kontrakt operation.
- [ ] Determine whether the common Text datum must use the same bounded Unicode Scalar Sequence domain as ADR-0064 Input
  `Text`, or whether some internal exact operations can have a distinct finite result domain. If they can, specify the
  boundary at which an Input-owned size limit becomes applicable.
- [ ] Specify how shared operation laws constrain 1D implementations without taking over their judgments. For example,
  Canonicalization chooses its equivalence and representative under ADR-0066; this ADR can constrain the exact Unicode
  calculation that a chosen law uses.
- [ ] Identify every place where a shared operation's result is legally observed after another Contract has acted.
  Determine which distinction must survive until the owning consumer no longer needs it, using ADR-0078 §4.3 as the
  model.
- [ ] Decide whether this ADR needs a common operation-law reference at all. Prefer a reference to already ratified
  exact meaning over a second catalog that duplicates the seven Unicode Canonicalization Law authorities in the Accepted
  Catalog Design §8.6.
- [ ] Keep the scope of source identifiers, diagnostic wording, source encodings, and target Classfile text separate
  from the meaning of a Contract Text datum. Determine which common rules they may reuse without acquiring Contract
  authority.

### 5.2. Determine the character and text value domains

- [ ] Fix the meaning of a Unicode Scalar Value, including the exclusion of surrogate code points. State how the scalar
  domain differs from a Unicode Code Point, a UTF-16 Code Unit, and an octet; do not infer a conversion between them.
- [ ] Decide which exact observations of a legal Text value are available to shared operations. For example, an empty
  sequence, U+0000, combining marks, and supplementary scalars remain distinct values unless a specified operation
  changes them.
- [ ] Examine whether noncharacters, unassigned scalars, private-use characters, default-ignorable characters, and
  bidirectional controls remain legal scalar inputs. Their Unicode properties or security implications must not silently
  make them invalid Text.
- [ ] Specify whether a character-like operation consumes one scalar or a larger unit. A visible grapheme can contain
  several scalars, while a Java `char` is one UTF-16 Code Unit; neither can be substituted merely because both are
  called a character.
- [ ] Require a Text value to have the same legal observations regardless of when its physical representation is
  materialized. Lazy decoding and buffer ownership are realization choices. They cannot make the result depend on
  mutable external content or a later library state.
- [ ] Decide how result length growth interacts with a finite admitted input. NFC, case folding, and text concatenation
  can increase length; a physical buffer limit must not invent a shorter semantic result or a new law-owned refusal.
- [ ] Separate Text absence from empty Text, an invalid encoding, and a failed operation. The relevant owner must decide
  absence or refusal; none may be encoded as a silently chosen Text sentinel.

### 5.3. Close the exact basic operations

- [ ] Establish the smallest V1 operation set actually required by the Contract pipeline and compiler. For each selected
  operation, record the legal operand domain, result domain, exact observation, and any failure condition instead of
  approving a broad String API family.
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
- [ ] Review prefix, suffix, containment, repetition, split, and join individually. Include an operation only if the
  compiler or Contract pipeline needs its exact semantics; broad availability in a Host library is not sufficient.
- [ ] Decide how byte-to-text parsing and text-to-domain formatting are divided among owners. Numeric formatting follows
  ADR-0078; temporal interpretation belongs to ADR-0082 when ratified; URI and path interpretation must not be silently
  absorbed into generic Text operations.

### 5.4. Resolve Unicode-specific operations and semantic dependencies

- [ ] Separate canonical normalization from compatibility normalization. Establish which exact forms are used, while
  referring to the existing NFC, NFD, NFKC, and NFKD Exact Law specifications rather than reauthorizing their
  equivalence or representative choices.
- [ ] Determine whether any general Unicode normalization operation is needed outside a selected Canonicalization Law.
  If yes, specify its exact result meaning and how it references an approved semantic basis without granting new
  Canonicalization authority.
- [ ] Distinguish simple case mapping, full case mapping, default full case folding, Turkic-sensitive behavior, and
  locale-sensitive mapping. A case-insensitive result must not be inferred from unspecified lowercasing.
- [ ] Review contextual mappings such as the Greek final sigma and scalar-to-multiple-scalar expansions. If an operation
  depends on neighbors or locale, those determinants must be explicit before that operation is supported.
- [ ] Audit the seven Unicode 17.0.0 Exact Laws admitted in the Canonicalization Catalog Design §8.6. Identify reusable
  calculation primitives, but retain each law's separate Version, `E_L`, `C_L`, and coverage authority.
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