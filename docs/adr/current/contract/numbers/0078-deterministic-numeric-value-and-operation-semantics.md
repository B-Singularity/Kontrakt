# ADR-0078: Deterministic Numeric Value and Operation Semantics

## Status

Proposed

## Date

2026-10-18

## Proposed Location

`docs/adr/current/contract/numbers/0078-deterministic-numeric-value-and-operation-semantics.md`

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend
- ADR-0047: One-Dimensional Contract Presentations and Pipeline Selection
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary
- ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0075: Compiler Result Ownership and Legal Consumption
- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0077: Contract Relation Infrastructure
- `docs/design/api/canonicalization-authoring-api-design.md`
- `docs/design/admission/admission-authoring-api-and-source-surface-working-design.md`
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`

---

# 1. Context

Numeric values participate in Contract judgments throughout Kontrakt. Their meaning must remain stable across compiler
and realization boundaries.

ADR-0064 owns Input Presentation Sameness. ADR-0065 owns Admission judgments. ADR-0073 controls legal platform use. This
ADR supplies the shared meaning of Numeric Values and Numeric Operations without taking over those authorities.

Kontrakt must be able to interpret a lawfully supplied numeric result without owning the external library that produced
it. Its subsequent governed numeric calculations follow the same Numbers law as other Contract consumers.

The Numeric Realization Design implements these laws. Its algorithms and physical representations have no independent
Contract authority.

# 2. Problem

A numerical result can depend on an unstated choice of overflow, rounding, evaluation order, or representation. Two
implementations can therefore produce different Contract judgments from the same declared meaning.

An external numeric result creates a separate risk. Kontrakt may misinterpret its format or lose a distinction while
admitting it. Conversely, attempting to interpret the external library's entire algorithm would make Contract meaning
dependent on that library.

Freezing a particular numeric algorithm would also prevent compatible optimization. Yet replacing an algorithm must not
change an established result merely because the replacement is more accurate.

The contract must therefore distinguish value interpretation, governed operation meaning, and replaceable realization.

# 3. Decision Drivers

1. A lawfully supplied Numeric Value must retain every distinction required by its applicable Presentation and operation
   laws.
2. An operation used in a governed judgment must produce one legally observable result under the same semantic inputs.
3. Supporting a Numeric Value does not imply support for every operation on it.
4. External calculation and Kontrakt-governed calculation must not be confused.
5. A compatible realization may change its implementation but not its Numeric Meaning.

# 4. Decision and Ownership

## 4.1. Shared Numeric Semantics

**Deterministic Numeric Semantics** defines the meaning of Numeric Values and Numeric Operations used by Contract
judgments. A Numeric Operation may return a number, Boolean, Text, or structured result. Numbers determines the numeric
operation's result, not the general meaning of its result type.

Numbers does not establish Contract judgments or Material. Input, Admission, Canonicalization, Lowering, Fact, and
Establishment retain their authority. Their use of a Numeric Operation does not permit them to redefine that operation's
meaning.

This ADR does not introduce a new 1D Contract, Numeric authority, or user-visible numeric wrapper.

## 4.2. Determinism Requirement

For the same exact numeric operation law, legal operands, declared semantic inputs, and applicable world, the operation
must yield one exact legally observable result. A result that depends on processor model, JIT decision, library
provider, thread schedule, locale, platform default, or unrecorded implementation version is not a closed deterministic
operation.

A fixed Boolean judgment alone is not sufficient evidence of deterministic numeric behavior. A result may later be
observed through bits, scale, order, text projection, or a different Contract. An implementation may disregard a
distinction only when the owning law has proved that distinction irrelevant to every legal observation of the
transformed result.

Compiler results formed through clean computation, cache reuse, restoration, optimization, or parallel compilation must
agree under the same legal semantic basis. An optimization does not narrow or change the owning numeric law. A Kontrakt
release, numerical-kernel revision, or target change is not permission to select a different result for that unchanged
law. If an improvement requires different legally observable numeric results, it is a change of meaning that must be
handled under Section 12, not a transparent implementation upgrade.

## 4.3. Relation to ADR-0073

ADR-0073 permits some separately ratified platform operations to execute unchanged under their platform contracts, even
when those contracts allow more than one behavior. That general permission does not make such an operation admissible as
a deterministic Numeric Operation in a governed Contract judgment.

An exact Kotlin or Java operation is admitted into the deterministic numeric surface only when its applicable semantics,
or an explicitly legal deterministic refinement of those semantics, satisfies Section 4.2. When Kontrakt substitutes a
numeric realization for an accepted platform operation, ADR-0073's Preservation Law remains binding. The substitute must
preserve every relevant source-semantic observation and may choose a deterministic result only inside the platform's
permitted behavior.

If the platform promises an exact result, Kontrakt cannot replace it with a different result merely because the new
result is more accurate or easier to reproduce. If the platform permits multiple results but no legal deterministic
refinement can be established, that operation is unsupported for the deterministic governed use. It is not silently
treated as a different numeric operation.

## 4.4. Exact Operation Meaning

An admitted Numeric Operation must have a complete semantic definition. It identifies the operands, applicable source
operation, result domain, definedness, and exact result. The definition includes every distinction that affects a legal
observation.

This meaning is not a physical profile record or HIR schema. It is not identified by a source symbol, runtime object,
machine instruction, external implementation, or cache key. Section 4.7 defines when an operation is sufficiently
complete to be supported.

## 4.5. Result Determination and Permitted Loss

An operation law fixes the result rather than the algorithm that calculates it. Where the mathematical operation and
required rounding determine one result, that result is preferred over a reference algorithm's historical output. Section
4.3 still requires source-semantic preservation.

The operation law also determines where numeric information may be lost. A realization may use exact or wider
intermediate calculations only if it preserves the required result and intermediate rounding. Required evaluation order
and exceptional behavior must also be preserved. Ordered arithmetic and a separately defined exact aggregate may
therefore have different accumulation laws.

A representation's **numerical resolution** describes the values it can distinguish. This does not establish a
measurement's accuracy or uncertainty. Measurement resolution may affect a judgment only when supplied through its
owning Contract boundary. Numbers does not infer it from the representation.

## 4.6. Numeric Value Interpretation and Preservation

Kontrakt's required Numeric Value scope includes:

- Signed and unsigned integers, including BigInteger.
- Decimal.
- Binary16, bfloat16, Binary32, Binary64, and Binary128.
- Vector, Matrix, and Tensor values.

These are required semantic support targets, not a promise to implement their external calculation APIs.

A Numeric Value is interpretable when its domain and legally observable distinctions are fully determined. For
structured values, the constituent numeric meanings and relevant structure must also be determined. For example, tensor
shape and logical element coordinates cannot be inferred from element count alone.

A lawfully admitted value must retain its meaning without depending on later changes to an external object or hidden
execution. The law requires stable observations, not a mandatory memory copy or physical encoding. Layout and storage
choices remain with the realization unless the owning value law makes a distinction observable.

Interpretation does not authorize normalization or precision loss. A change in Numeric Meaning requires an explicitly
legal conversion. Such a conversion produces a distinct result under its own law; it does not rewrite the admitted
input.

The external producer's measurement accuracy and algorithmic error bounds are not inferred from the result. Units and
other domain meaning remain with the Contract that supplies them. Section 4.1 governs authority over those judgments.

## 4.7. Value Support and Operation Qualification

**Value Interpretation** is complete when the legally observable meaning of the supplied value is closed. **Operation
Meaning** is complete when the operation defines its valid inputs, exact results, and exceptional behavior. Either may
be complete without the other.

The following are distinct determinations:

- Whether a Numeric Value is understood under the Numbers law.
- Whether its presentation and use are legal under the owning Contract.
- Whether a particular Numeric Operation has a complete result law.
- Whether a current realization can execute that law.

A Numeric family does not authorize every operation or carrier. An operation whose meaning is incomplete cannot be used
to establish a governed judgment. A known operation that is outside the current realization's capabilities does not
thereby lose its semantic definition. The different refusal and failure authorities remain governed by Section 12.

New Numeric Formats and structured meanings may be introduced without changing existing laws. Rational, Complex, and
Interval values require their own closed meanings before being claimed as supported. A new format must preserve its own
distinctions and must not obtain Contract authority from the external runtime that supplies it. No global precision mode
is introduced to redefine existing source operations.

# 5. Numeric Value Domains and Sameness

## 5.1. Existing Input Domains Remain Intact

ADR-0064 owns the accepted Input Leaf laws. Its current V1 baseline includes fixed-width signed and unsigned integers,
Decimal, Binary32, and Binary64. Decimal preserves coefficient and scale. Binary32/64 preserve the admitted interchange
bits.

Section 4.6 expands the required **numeric semantic scope**. It does not automatically extend ADR-0064's legal Input
presentations or ADR-0073's host-carrier ratification. Those boundaries require their own explicit decisions.

Input Presentation Sameness is not replaced by numeric equality or an internal representation choice. Conversion or
Canonicalization must be separately authorized.

## 5.2. Comparison Relations Are Operation-Specific

A numeric comparison must declare which relation it computes. Integer value equality, floating numerical equality,
floating ordered comparison, an explicitly defined total ordering, Decimal numerical cohort comparison, Decimal
coefficient/scale equality, raw-bit equality, and Collection Presentation Sameness are not interchangeable.

IEEE-style numerical equality does not equate NaN to itself and equates positive and negative zero. Input Binary
Presentation Sameness compares the complete admitted bits. Decimal numerical comparison can equate `2.0` and `2.00`,
while Decimal Presentation Sameness does not. A relation used for membership uniqueness or hash indexing must have the
equivalence properties required by the owning collection law; an arbitrary numeric predicate is not substituted for it.

The meaning of Kotlin `==`, Java `==`, `equals`, `compareTo`, and a Collection membership operation follows the exact
resolved operation and the applicable value/collection law. Syntax alone cannot authorize a host equality implementation
or allow an optimizer to replace `any { x == value }` with `contains(value)`.

# 6. Fixed-Width Integer Semantics

## 6.1. Width, Signedness, and Promotion

Each supported fixed-width operation has one exact operand and result width, signedness interpretation, and
source-language promotion or conversion rule. A narrower Java integer expression may be promoted to `int`; a Kotlin
overload may produce a different type. The frontend must resolve the actual expression before numeric refinement.
Neither an operand's source spelling nor its storage lane alone decides the result type.

For wrapping addition, subtraction, and multiplication at width `w`, the output bits are the result modulo `2^w`. Signed
and unsigned interpretation of those bits remains operation-specific. Reassociation and algebraic simplification are
permitted only under that exact operation law; a checked operation is not rewritten as a wrapping operation without
establishing the required guard.

## 6.2. Division, Remainder, and Exceptional Bounds

Integer division and remainder must retain the exact source law for quotient direction, remainder sign, division by
zero, and the minimum signed value divided by negative one. In Java fixed-width signed division, the latter has its
defined wrapped result; it must not be turned into an unrelated mathematical overflow failure. Floor division and floor
modulus are distinct operations from truncating division and remainder.

Division by zero remains outside the legal domain of a throwing source operation. A governed judgment may use it only
where its domain is established on every reachable path or where an explicitly supported non-exceptional result law
provides the partiality. A host exception is not Admission `Rejected` or another invented Contract outcome.

## 6.3. Bitwise and Shift

AND, OR, XOR, complement, left shift, arithmetic right shift, logical right shift, rotate, population count,
leading/trailing zero count, bit reversal, and related fixed-width operations have exact bitvector meaning.
Shift-distance masking, result width, bit extension, and narrow-type promotion follow the resolved source operation. A
width-sized Java shift distance is not automatically an illegal distance.

Fixed-width operations are not replaced by `BigInteger` shifts without proving that sign extension, truncation, and
distance semantics agree.

## 6.4. Checked and Exact Conversion Operations

Checked addition, subtraction, multiplication, negation, increment, decrement, absolute value, and exact narrowing are
separate partial operation laws when they can throw on overflow. They may be used only in an established non-exceptional
domain. Ordinary wrapping arithmetic and checked arithmetic do not share one operation meaning.

Numeric conversions distinguish sign extension, zero extension, truncation, narrowing, saturation, floating rounding,
and rejection of inexact results. The exact Kotlin/Java source conversion is preserved. A backend conversion with poison
or undefined behavior outside its domain must not replace a defined saturating or wrapping source conversion without an
appropriate guard or equivalent implementation.

# 7. Arbitrary-Precision Integer Semantics

A supported `BigInteger`-like operation denotes a finite mathematical integer result under its exact operation law.
Addition, subtraction, multiplication, comparison, division, remainder, modulus, powers, signed bit operations, shifts,
and conversions are independently qualified. A negative exponent, zero divisor, nonpositive modulus, missing modular
inverse, or exact conversion outside its target range is handled according to that operation's definedness, not by
inventing a Contract failure.

Signed arbitrary-precision bitwise operations follow their explicitly defined infinite sign-extension behavior. They are
not widened versions of fixed-width bitvector operations by default.

A library's probabilistic primality test, ambient random generator, or unspecified algorithm-dependent result is not
admitted merely because its arguments and return value are integers. A deterministic primality operation would require
an independently closed exact law and verification. BigInteger as a value type does not admit the library's entire
method set.

The mathematical result and the physical cost of constructing it are separate. Resource or implementation limits cannot
silently change the arithmetic result. Budget, Capacity, compiler safety, and realization ownership govern resource
exhaustion; such exhaustion does not become an ordinary Boolean `false` or Admission `Rejected`.

# 8. Binary32 and Binary64 Semantics

## 8.1. Representation and Value Operations

The admitted Binary32 and Binary64 Input presentations preserve their complete permitted 32-bit or 64-bit data. Reading,
transporting, caching, or projecting an already-established datum must not silently canonicalize NaN, collapse signed
zero, discard payload bits, or round again. Any direct host carrier that cannot preserve an admitted distinction must be
classified accordingly under ADR-0073; Kontrakt cannot recover original bits after a lossy boundary conversion.

A value operation can produce a new binary result and is governed by its own exact Numeric Operation law. Input-bit
preservation is not an assertion that arithmetic always propagates the input NaN payload or that all bit-pattern
constructors are lossless on every JVM path.

## 8.2. Basic Arithmetic and Rounding

Supported basic binary arithmetic must fix its result format, rounding rule, treatment of signed zero, infinities,
subnormal values, overflow, underflow, remainder, and exceptional numeric conditions. The default Java-compatible
Binary32/64 arithmetic profile uses round-to-nearest, ties-to-even at the required result precision. No implicit wider
intermediate precision, flush-to-zero mode, reassociation, reciprocal substitution, or FMA contraction is permitted
unless the exact result law remains intact.

A fused multiply-add is distinct from separately rounded multiplication followed by addition. Floating addition is not
generally associative, even when all operations are pure. A reduction over a fixed ordered Sequence preserves the
legally selected evaluation order unless an exact equivalence has been established. Unordered collection iteration
cannot silently select an arithmetic accumulation order that changes the result.

## 8.3. NaN Result Closure

Input NaN bit preservation and NaN generation by arithmetic must be specified separately. Every supported binary
operation whose result can be NaN must determine an exact legally observable result, including which NaN encoding is
produced or which operand bits are retained, quieted, or transformed. Platform-dependent NaN payload propagation is not
admitted as a closed deterministic operation.

**Proposed V1 default for newly generated arithmetic NaN:** where the exact applicable source operation permits this
refinement, newly produced Binary32 NaN is quiet NaN `0x7fc00000` and newly produced Binary64 NaN is quiet NaN
`0x7ff8000000000000`. This is a proposed operation-result rule, not a rule for Input canonicalization, raw-bit identity
operations, or unconditional rewriting of existing NaNs. Operations that copy raw bits, change sign bits, reinterpret
representations, or promise a more specific behavior must retain their own exact rule. If the fixed-result policy would
violate a source operation's guarantee, that operation requires a different lawful exact profile or is unsupported.

This default must be confirmed by per-operation preservation and conformance evidence before this ADR can be Accepted.
The exact behavior of signaling NaNs and round trips through Java/Kotlin floating values must be specified without
assuming that the JVM preserves every signaling representation. Simply choosing a fixed NaN constant in a compiler pass
does not prove legal preservation.

## 8.4. Classification, Comparison, and Raw Bits

`isNaN`, `isInfinite`, `isFinite`, primitive comparisons, explicit total comparison functions, canonicalizing bit
conversions, raw-bit observations, and raw-bit reinterpretations are distinct operations. Primitive numerical equality,
boxed Java equality, a total comparison, and raw-bit equality do not share one equality law.

Raw-bit access may be admitted only where the source representation and actual realization preserve the bits that the
operation claims to observe. Canonicalizing `doubleToLongBits` is not interchangeable with `doubleToRawLongBits`.
`longBitsToDouble` is not assumed to preserve every signaling NaN bit pattern through a subsequent floating-value use.

## 8.5. Math Functions and Platform Refinement

Each elementary or transcendental function requires one exact, reproducible result law over its whole admitted domain,
including special values and rounding. A maximum-error or correctly-rounded tolerance by itself does not choose one
unique result when several bit patterns remain allowed. The function therefore needs a uniquely specified result or
another ratified exact deterministic refinement consistent with the source contract. An implementation-independent exact
result, including correctly rounded output where legally applicable, is preferred over freezing a particular
approximation algorithm. A specified reference algorithm may determine the result only where that alternative is
explicitly approved and preserves the source contract; it is not the default substitute for an unresolved result law.

`Math` and `StrictMath` are audited separately, by exact callable and versioned specification. An exact `StrictMath`
algorithm may be a suitable basis for some functions; its name alone does not prove that every overload has the required
bitwise result. More mathematically accurate output from another library is not automatically a legal replacement for a
source operation with a different specified result. Platform functions that cannot meet this section are not admitted
into deterministic Contract judgments until a conforming exact profile exists.

# 9. Decimal Semantics

## 9.1. Value, Presentation, and Arithmetic Result

A Decimal presentation retains an exact coefficient and an exact scale. Its numerical value is
`coefficient × 10^(-scale)`. Numerical cohort equality may ignore differences in scale for comparison, but Presentation
Sameness does not. Arithmetic results must preserve their own exact coefficient/scale and rounding laws, including the
source operation's preferred scale and tie behavior.

An implementation may use compact or wide representation, but it cannot normalize trailing zeros or change scale merely
because the numerical value is unchanged. Explicit scale-changing operations are legal derived calculations when their
exact result law is supported.

## 9.2. Exact and Rounded Operations

Addition, subtraction, multiplication, comparison, division, integral division, remainder, powers, scale conversion,
square root, and conversions must be qualified individually, including each source overload. Exact division is partial
where the quotient has no admissible finite decimal expansion or its other domain conditions fail. Scale-changing
operations without rounding are partial if an exact result cannot be represented in the required result scale.

A rounded Decimal operation must fix precision, result scale where applicable, and an explicit rounding rule. The closed
Java `RoundingMode` cases `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN`, and `UNNECESSARY`
retain their exact distinct meanings. `UNNECESSARY` is legal only where no rounding is needed. A `MathContext`
contributes its exact precision and rounding case; default process-wide context is not inferred.

Context-bearing and context-free overloads are not interchangeable. The result of `BigDecimal.divide(BigDecimal)` is not
silently replaced with a rounded quotient. An algorithm with an error bound, including a host `sqrt` implementation, is
insufficient for a deterministic profile if that bound permits multiple different results and the profile does not
otherwise fix which one is selected.

## 9.3. Formation, Conversion, and Text Projection

Decimal formation from a source string, integer, coefficient/scale pair, binary floating value, or a floating-to-text
round trip follows its exact operation. In particular `new BigDecimal(double)` and `BigDecimal.valueOf(double)` are
distinct. Their results must not be conflated in the frontend or by constant folding.

Exact and lossy integer conversions are separate. `toString`, `toPlainString`, and `toEngineeringString` may have
distinct, deterministic textual meanings. Text formatting and parsing do not retroactively redefine Input Decimal
Presentation Sameness.

# 10. Cross-Operation Composition and Partiality

## 10.1. Guard and Evaluation Order

Every admitted operation is valid only on its exact domain. Source short-circuit behavior, conditionals, early returns,
and guarding comparisons must be preserved where they protect partial numeric calculations. Two independent Admission
conditions combined by the class-wide judgment law do not acquire a source evaluation order, and one condition cannot
automatically serve as the other's guard.

An exception, arithmetic trap, compiler resource refusal, and a legitimate Contract `Rejected` are different outcomes.
Numeric exception handling cannot be used to fabricate a second Admission result type.

## 10.2. Collections, Equality, and Reductions

A numeric predicate over Collection elements uses the exact element comparison law. A `Membership.contains` or
`Association.containsKey` operation uses the owning Input Presentation Sameness. Whole-Collection equality retains the
collection's declared constituent and observable-order distinctions. No compiler transformation may exchange these
relations without proving they are equivalent for the actual operand domains.

Numeric aggregation over a Sequence preserves the applicable order. A Membership without observable order does not
acquire one from its iterator. Floating reassociation requires proof of the same legal result and guard behavior. A
separately defined exact or reproducible reduction may use a different accumulation law under Section 4.5; it does not
redefine ordinary ordered source arithmetic.

## 10.3. Source Legality and Optimization

A source declaration is qualified under its applicable authoring and operation laws; reachability-based dead-code
elimination is a later compiler optimization and cannot create permission for an operation whose source form is
forbidden under the applicable whole-source qualification rule.

**Consistency requirement:** ADR-0065 §7.5 in the current file permits removal of a proven unreachable branch containing
unsupported syntax. That wording conflicts with the subsequently agreed whole-source legality-before-optimization rule.
This proposed clause does not silently override Accepted ADR-0065. The two documents must be reconciled explicitly
before acceptance. The implementation must not choose whichever interpretation is convenient.

# 11. Numeric Qualification and Platform Audit

A Numeric Operation used in governed processing must be resolved to its exact source meaning where applicable. For
Kotlin and Java, this includes the exact overload and relevant language version. Qualification must establish the result
law and legal execution domain under Sections 4.3, 4.4, and 4.7. An unresolved or unverified operation cannot contribute
to a governed judgment.

ADR-0073 controls whether a platform or external call may execute within the governed boundary. A permitted external
calculation may instead deliver a result through an approved Adapter or Input path. Under Section 4.6, Kontrakt
interprets the admitted value without taking ownership of the library's prior computation. A numeric return type does
not legalize a prohibited call or bypass the boundary for new external facts.

Compiler qualification and its reuse are compiler products under ADR-0075. They do not establish Numeric Meaning.
Reflection or dynamic provider discovery cannot grant permission.

# 12. Establishment, Identity, Version, and Failure Boundaries

Numbers does not establish a separate Numeric Definition or Occurrence. Existing Contract judgments retain their
Required Basis and applicability laws under ADR-0063. Version Claim and binding remain governed by ADR-0053. An external
numeric specification becomes a required semantic basis only when the owning law so requires.

Changing an operation's result-determining law changes its meaning. It cannot be disguised as an optimization or cache
migration. A compatible new implementation must preserve the unchanged law's legally observable results. Greater
numerical accuracy alone does not excuse a different result. ADR-0053 governs intentional meaning changes; Numbers
introduces no independent version authority or obligation to retain unsafe historical executables.

An unsupported operation, a value outside an operation's domain, a prohibited platform call, and resource exhaustion are
distinct from a legal Admission result. Their respective owners determine the outcome. Numbers cannot establish Failure,
`Rejected`, or Diagnostic Material on their behalf.

# 13. Realization Design Ownership

The Numeric Realization Design owns calculation algorithms and physical representations. It also owns platform fast
paths and implementation-specific resource protection. These may change without changing the established Numeric Meaning
under Sections 4–12.

Compiler optimization and cache reuse remain subject to their existing result and validity laws. Budget, Capacity, and
security authorities continue to govern resource limits. No implementation mechanism can establish a new Numeric
outcome.

# 14. Conformance and Execution Eligibility

An operation may be advertised as executable only when the realization has sufficient evidence that its legal
observations match the approved Numeric Meaning. When the law fixes one result, a numerical tolerance is not sufficient.

Verification methods and test cases belong to Numeric Realization Design. They do not define the operation's legal
result.

# 15. Integration and Supersession Boundaries

This ADR supplies shared Numeric Meaning without superseding other Contracts. The authority split is defined in Section
4.1. Existing Input support remains limited as stated in Section 5.1. Platform admissibility follows Section 11.

ADR-0064 requires a separate compatibility decision before Binary16, bfloat16, Binary128, or new structured Numeric
Presentations may enter its V1 Input surface. ADR-0073 must independently ratify each proposed carrier and executable
use. Those Accepted documents are not amended by this Proposed ADR.

# 16. Decisions Still Required Before Acceptance

The following decisions remain open; none is made Accepted by this draft:

1. Define each supported floating operation's exact NaN result, including the proposed default in Section 8.3.
2. Complete the deterministic result laws for supported `Math`, `StrictMath`, and Kotlin mathematical operations.
3. Complete the exact BigInteger and Decimal operation laws, including exceptional domains and result representation.
4. Resolve ADR-0065 §7.5 against Section 10.3 without silently modifying that Accepted ADR.
5. Define the semantic details of Binary16, bfloat16, and Binary128, then determine their legal Input and Platform
   boundaries under ADR-0064 and ADR-0073.
6. Complete the Structured Numeric Value laws. Tensor structure, quantization, missing values, and legal observations
   require explicit decisions by their respective owners.
7. Determine which operations have an algorithm-independent unique result. Any exception relying on a reference
   procedure requires separate approval consistent with Section 4.3.
8. Decide how explicitly supplied measurement resolution or uncertainty participates in existing Input and Fact
   meanings, without introducing a new Numbers authority.
9. Complete the Numeric Realization Design under Sections 13 and 14. The implementation plan is not Contract meaning.

# 17. Rejected Directions

The following alternatives are excluded by the controlling laws rather than repeated here:

- A new Numeric 1D Contract or runtime Numeric authority: Section 4.1.
- Treating external library algorithms as Numeric Contract authority: Sections 4.6 and 11.
- One mandatory representation, universal precision mode, or implicit normalization: Sections 4.5–4.7.
- Treating host syntax or approximate tolerance as a complete operation law: Sections 4.3–4.4.
- Replacing Input Sameness with one numeric equality rule: Section 5.
- Allowing an optimization or cache hit to establish Numeric Meaning: Sections 4.2 and 13.
- Treating resource or computation failure as Admission `Rejected`: Section 12.

# 18. Consequences and Follow-On Work

Contract consumers share the Numeric Law while retaining their own judgments. The compiler may choose different
realizations, but it must preserve the same legal result.

Next work is to close the remaining Numeric Value and Operation details in Section 16. The corresponding Realization
Design and the necessary ADR-0064, ADR-0065, and ADR-0073 consistency changes follow separately. This Proposed ADR
changes none of those Accepted documents.
