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

### 3.9. Frontend and Compiler-Product Consumption

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
- [ ] Determine whether any internal Text value can be interpreted lazily. Required legal observations must be fixed
  before use, and lazy decoding must not depend on a mutable external buffer or on later library state.
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
- [ ] Decide where evidence about exact Source-Bound operations is acquired when V1 starts from Host Classfiles. A
  compiler summary may be used only if it preserves enough original meaning; the acquisition strategy itself belongs to
  Realization Design and the existing Whole-Machine research.
- [ ] Preserve ADR-0073's platform eligibility and airlock decisions. A recognized Java string operation must not become
  an exception that admits unrelated capabilities, resource access, or arbitrary call chains.

### 5.7. Audit the Contract Frontend and HIR handoff

- [ ] Verify that the SourceManager distinguishes byte offsets, decoded text positions, and the values obtained after
  source-language escape processing. Those differences may be necessary for diagnostics even when two literals denote
  the same Text value.
- [ ] Determine who defines identifier normalization and name equality for `.kontrakt` source. Rust's identifier NFC
  rule is useful comparative evidence but cannot be imported as universal Text equality or a new HIR identity law.
- [ ] Review literal parsing, adjacent-literal combination, escape syntax, and invalid-source rejection under the actual
  `.kontrakt` grammar. The common Text law must not silently rewrite the language's source interpretation.
- [ ] Check that resolved HIR has enough information to distinguish a literal Text value from a declared Text operation
  and its exact semantic basis. Do not prescribe a second HIR or a universal Text property record without evidence that
  existing producer protocols are insufficient.
- [ ] Decide how an existing Unicode Exact Law reference is made resolvable at the HIR boundary. A textual label such as
  `normalize` is not evidence of the selected profile or its Version.
- [ ] Identify whether original source spelling or SourceSpan is a separate provenance product when its meaning is no
  longer needed by HIR. A V2 provenance-only change should not necessarily invalidate unchanged resolved Text meaning.
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

- [ ] Determine which Text-bearing Host operations the existing Realization Body IR can represent without loss of
  relevant source meaning. Check conversion, invokedynamic concatenation, exception paths, and possible user callbacks
  in addition to the resulting string value.
- [ ] Verify that Realization Admission and Closure Verification assess prohibited calls before an optimization could
  erase them. A subsequently unreachable forbidden call must not retroactively make an inadmissible realization legal.
- [ ] Identify the Text facts that Contract-Aware Analysis can prove from established Contract Material. The producer
  must expose their scope and lawful consumption under ADR-0075; the optimizer must not infer unsupported facts from the
  mere presence of a string.
- [ ] Inspect how Execution Formation distinguishes a previously established Text result from an as-yet-unperformed
  calculation. Materializing the same scalar sequence is not a license to skip a required authority judgment.
- [ ] Determine what exact Text observations the Contract-Aware Execution IR must preserve. Include failure
  reachability, source evaluation effects, and legally observable intermediate results when they affect transformation
  legality.
- [ ] Review whether Whole-Machine Summaries can profitably carry exact constant Text, length bounds, prefix
  information, or qualified normalization properties. These are candidate compiler-analysis products, not additional
  Contract Facts.
- [ ] Determine when a summary is insufficient and selective Body Opening is needed. V1 need not implement every
  analysis, but must not claim a transformation based on a summary that lacks the required proof.
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
- [ ] Decide how a pass records the exact legal scope of a Text-derived analysis and when later rewrites invalidate it.
  A V1 local analysis and a V2 incremental repair may have different lifetimes but the same correctness obligation.
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
- [ ] Determine when physical interning or shared storage may reuse one Text payload. Reuse is legal only if all
  distinctions required by the consumers remain recoverable; a normalized key cannot silently replace a distinct
  original scalar sequence.
- [ ] Review how Compiler Canonical Encoding represents Text for fingerprinting without borrowing Canonicalization
  Contract authority. A serialized-byte representation is an implementation protocol unless an owning law specifically
  makes it meaning.
- [ ] Determine which exact semantic dependencies must accompany cached Text calculations. Unicode operation profile,
  actual source specification, and any relevant immutable table version matter only when they can change the
  calculation's legal result.
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
- [ ] Distinguish clean, warm-cache, incremental, parallel, and cancelled-and-retried compilation. Given the same
  applicable law and valid inputs, each execution path must preserve the same semantic Text results and published
  Contract Material.

### 5.13. Audit diagnostics, tooling, security, and resources

- [ ] Determine which position units a diagnostic may report and who performs conversion. Source byte offsets, UTF-16
  positions for LSP clients, scalar indices, grapheme positions, and displayed columns can differ; a consumer must not
  silently reuse one number as another.
- [ ] Check preservation of offending input and error position for malformed Unicode. Diagnostics may quote or escape
  dangerous text, but their presentation must not rewrite the underlying error or source evidence.
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
- [ ] Determine how error reports distinguish unsupported semantics, failed source qualification, nonconforming
  execution, invalid input, and resource stops. Each has a different owning boundary under existing ADRs.

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