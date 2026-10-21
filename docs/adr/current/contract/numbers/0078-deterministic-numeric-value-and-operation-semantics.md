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

For the same Numeric Operation Law, legal operands, declared semantic inputs, and applicable world, one exact legally
observable result must follow. An undeclared choice of execution environment cannot determine that result.

Agreement on a final Boolean judgment is insufficient when another legal observation can distinguish the underlying
Numeric Result. A distinction may be discarded only when the owning law permits it under every remaining legal
observation.

A change to Kontrakt, its execution target, or its calculation methods must not change the result of an unchanged
Numeric Law. Section 12 governs changes to the law itself. Compiler-result reuse remains subject to ADR-0075.

## 4.3. Relation to ADR-0073

ADR-0073 permits some separately ratified platform operations to execute unchanged under their platform contracts, even
when those contracts allow more than one behavior. That general permission does not make such an operation admissible as
a deterministic Numeric Operation in a governed Contract judgment.

When a user's Contract declaration relies on a Kotlin or Java operation, the resolved source operation supplies the
meaning that Kontrakt must preserve. Performing its calculation inside Kontrakt does not replace that meaning with a
Kontrakt-defined mathematical operation.

That source operation may participate in a deterministic Contract judgment only when its source law determines one
result or permits a lawful deterministic refinement under Section 4.2. A refinement must preserve the legal observations
required by ADR-0073. An operation for which this cannot be established remains unsupported for that governed use.

## 4.4. Exact Operation Meaning

An admitted Numeric Operation must determine its legal operands, definedness, and exact result. A Kontrakt-defined
operation that does not derive its meaning from a user's source operation follows its own Numeric Law. Running it on the
JVM does not make Java or Kotlin arithmetic its governing law.

An exact mathematical result can be fully defined without a finite Numeric Datum. This does not make an otherwise
unsupported Numeric Value format available under Section 4.7. If an operation has already fixed a finite-format result,
subsequent operations use that result even when it has not been physically produced.

Neither a source symbol nor a physical identifier establishes the operation's Contract authority or meaning.

## 4.5. Result Determination and Permitted Loss

An operation law fixes the result, not the algorithm used to obtain it. A Kontrakt-defined mathematical operation may
require the exact mathematical result or its correctly rounded form. This does not override the source meaning protected
by Section 4.3.

For exact real operations, `exp(a) * exp(b)` and `exp(a + b)` agree wherever the identity is defined. They do not
generally agree with a source expression that separately rounds intermediate results.

The applicable law determines the result format when one is required. It fixes each semantic rounding boundary and any
permitted information loss. Rounding through a wider intermediate format need not equal direct rounding to the target
format. A change in calculation method must preserve the required result and any intermediate result already fixed by
that law. The source law also governs required evaluation order and exceptional behavior.

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

A Numeric Value is interpretable when its domain and legally observable distinctions are fully determined. The
information needed to determine its numeric meaning must be sufficient under the applicable value law. Kontrakt cannot
infer an omitted Numeric Format, quantization parameter, or structural context from an unstated external default. Value
Interpretation may be complete without supporting any particular calculation on that value.

For structured values, the constituent numeric meanings and relevant structure must also be determined. For example,
tensor shape and logical element coordinates cannot be inferred from element count alone. A structured value may have a
logical numerical meaning independent of its physical storage. That meaning does not, by itself, replace the owning
Presentation Sameness or make every representation interchangeable. Distinctions such as duplicate sparse entries,
explicitly stored zeros, quantization parameters, and validity information remain observable whenever the owning law so
provides. Forming a new coordinate value by combining entries requires an exact operation law unless that result is
already part of the established value meaning.

A lawfully admitted value must retain its meaning without depending on later changes to an external object or hidden
execution. The law requires stable observations, not a mandatory memory copy or physical encoding. An already determined
value may be read or decoded later without reopening its meaning. Deferred access cannot obtain the established result
from mutable external state or an unperformed external calculation. Layout and storage choices remain with the
realization unless the owning value law makes a distinction observable.

Interpretation preserves every Numeric Datum distinction required by the owning law. Exact format bits are preserved
when those bits are part of that datum's meaning; unrelated storage padding is not automatically made Contract-visible.
Interpretation does not authorize normalization or precision loss. A change in Numeric Meaning requires an explicitly
legal conversion. Such a conversion produces a distinct result under its own law; it does not rewrite the admitted
input.

A floating NaN is a Numeric Format value, not a universal Missing or Null marker. Missing, Null, invalid slots, and
masks follow the owning Presentation's presence and validity law. Numbers does not infer a numeric datum for an absent
or invalid element, nor convert a valid NaN into absence. Any operation that resolves these states into a new numeric
result requires its own complete law.

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
while Decimal Presentation Sameness does not. Approximate comparison is not presumed transitive and does not establish a
Sameness relation. A relation used for membership uniqueness or hash indexing must have the equivalence properties
required by the owning collection law; an arbitrary numeric predicate is not substituted for it.

The meaning of Kotlin `==`, Java `==`, `equals`, `compareTo`, and a Collection membership operation follows the exact
resolved operation and the applicable value/collection law. Syntax alone cannot authorize a host equality implementation
or allow an optimizer to replace `any { x == value }` with `contains(value)`.

# 6. Fixed-Width Integer Semantics

## 6.1. Width, Signedness, and Promotion

Each supported fixed-width operation has one exact operand and result width, signedness interpretation, and
source-language promotion or conversion rule. A narrower Java integer expression may be promoted to `int`; a Kotlin
overload may produce a different type. The frontend must resolve the actual expression before numeric refinement.
Neither an operand's source spelling nor its storage lane alone decides the result type. An exact mathematical integer
operation and a fixed-width bitvector operation have distinct meanings; neither inherits the other's overflow or
equivalence laws.

For wrapping addition, subtraction, and multiplication at width `w`, the output bits are the result modulo `2^w`. Signed
and unsigned interpretation of those bits remains operation-specific. Modular identities may be used only where the full
operation law, including its fixed-width results and required observations, is preserved. A mathematical integer
identity does not automatically establish an equivalent signed comparison or checked operation. Reassociation and
algebraic simplification are permitted only under that exact operation law; a checked operation is not rewritten as a
wrapping operation without establishing the required guard.

## 6.2. Division, Remainder, and Exceptional Bounds

Integer division and remainder must retain the exact source law for quotient direction, remainder sign, division by
zero, and the minimum signed value divided by negative one. In Java fixed-width signed division, the latter has its
defined wrapped result; it must not be turned into an unrelated mathematical overflow failure. Truncating, floor,
ceiling, and Euclidean quotient and remainder laws are distinct wherever their results or domains differ. Each admitted
law determines both quotient and remainder under its required sign and range conditions. A mathematical integer quotient
identity may be applied to a fixed-width result only when its overflow and exceptional cases also agree.

Division by zero remains outside the legal domain of a throwing source operation. A governed judgment may use it only
where its domain is established on every reachable path or where an explicitly supported non-exceptional result law
provides the partiality. A host exception is not Admission `Rejected` or another invented Contract outcome.

## 6.3. Bitwise and Shift

AND, OR, XOR, complement, left shift, arithmetic right shift, logical right shift, rotate, population count,
leading/trailing zero count, bit reversal, and related fixed-width operations have exact bitvector meaning.
Shift-distance masking, result width, bit extension, and narrow-type promotion follow the resolved source operation. A
width-sized Java shift distance is not automatically an illegal distance.

Fixed-width operations are not replaced by `BigInteger` shifts without proving that sign extension, truncation, and
distance semantics agree. A fixed-width shift's masked distance and width-bounded result are not the same law as an
arbitrary-precision shift. Negative shift distances and arithmetic right shifts retain the exact source operation's
meaning.

## 6.4. Checked and Exact Conversion Operations

Checked addition, subtraction, multiplication, negation, increment, decrement, absolute value, and exact narrowing are
separate partial operation laws when they can throw on overflow. They may be used only in an established non-exceptional
domain. Ordinary wrapping arithmetic and checked arithmetic do not share one operation meaning. An algebraic
transformation cannot remove a checked intermediate operation unless its original definedness and legally observable
behavior are preserved.

Numeric conversions distinguish sign extension, zero extension, truncation, narrowing, saturation, floating rounding,
and rejection of inexact results. Interpreting a bit pattern with a different signedness is distinct from converting a
mathematical integer into another numeric domain. Equal storage bits do not by themselves make these operations
equivalent. The exact Kotlin/Java source conversion is preserved. A backend conversion with poison or undefined behavior
outside its domain must not replace a defined saturating or wrapping source conversion without an appropriate guard or
equivalent implementation.

# 7. Arbitrary-Precision Integer Semantics

A supported `BigInteger`-like arithmetic operation has an exact mathematical integer result where its domain is defined.
A comparison may instead return a Boolean. Supporting the value does not admit its library's entire operation set under
Section 4.7.

Division and remainder follow their own quotient law. A nonnegative modulus result is not interchangeable with a
truncating remainder. An ordinary power may reject negative exponents while modular exponentiation may permit them when
the required inverse exists. A `mod` operation may require a positive modulus. Modular inverse, greatest common divisor,
shifts, and conversions each retain the applicable source law. Division by zero and exact conversion outside its target
range retain their source-defined partiality. An invalid operand does not create a new Contract Failure.

Signed arbitrary-precision bitwise operations follow their explicitly defined infinite sign-extension behavior. They are
not widened versions of fixed-width bitvector operations by default. A negative shift distance reverses the shift
direction only where the applicable source operation expressly defines that behavior.

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
subnormal values, overflow, underflow, remainder, and exceptional numeric conditions. Its finite nonexceptional results
follow the exact mathematical operation with the rounding required by that law; cases involving NaN, infinity, or other
exceptional numeric conditions retain their operation-specific results. The default Java-compatible Binary32/64
arithmetic profile uses round-to-nearest, ties-to-even at the required result precision. It preserves gradual underflow,
including subnormal results and the required sign of a rounded zero. No implicit wider intermediate precision,
flush-to-zero mode, reassociation, reciprocal substitution, or FMA contraction is permitted unless the exact result law
remains intact. Ordinary Java/Kotlin arithmetic does not acquire IEEE exception flags or traps as additional observable
results without an explicit law admitting those observations.

A fused multiply-add is distinct from separately rounded multiplication followed by addition. Where fused multiply-add
is admitted, it rounds the exact product-plus-sum once under its applicable result law. Replacing separate operations
with it requires preservation of the original intermediate and final observations. Java floating `%` and IEEE remainder
have different quotient-selection laws and are not interchangeable. Floating addition is not generally associative, even
when all operations are pure. An exact real-number identity alone does not authorize a binary floating rewrite that
changes NaN results, signed zero, or required rounding. A reduction over a fixed ordered Sequence preserves the legally
selected evaluation order unless an exact equivalence has been established. Unordered collection iteration cannot
silently select an arithmetic accumulation order that changes the result.

## 8.3. NaN Result Closure

Input NaN bit preservation and the NaN result of an arithmetic operation are distinct. Under the default Java-compatible
Binary32/64 profile, a supported addition, subtraction, multiplication, division, remainder, or fused multiply-add whose
operation law requires a NaN result produces quiet NaN `0x7fc00000` for Binary32 or `0x7ff8000000000000` for Binary64,
provided the applicable source law permits that exact result. This choice applies whether the NaN result arises from
non-NaN operands or from one or more NaN operands. Their NaN signs and payloads do not determine the arithmetic result.

The fixed result is an operation-result law, not Input canonicalization or a change to an already admitted datum. An
operation that observes raw bits, preserves or transforms a representation, or has a more specific source result retains
its own exact law. A NaN operand does not require a NaN result when the applicable operation expressly determines a
non-NaN result. A source operation that cannot admit this fixed NaN result requires another lawful exact deterministic
result law or remains unsupported for governed use.

Other floating operations, including unary negation, magnitude and sign operations, minimum and maximum, and
mathematical functions, require their own complete law for NaN operands and results. This includes cases where a NaN
operand changes a non-NaN result. Where the applicable source law permits multiple behaviors, only an expressly selected
lawful deterministic refinement satisfies Section 4.2. The fixed arithmetic NaN rule does not extend to these operations
without such qualification.

Signaling NaN bit patterns retain every distinction required by their admitted Presentation. A carrier that may quiet
such a pattern cannot be presumed to preserve it. Their participation in an admitted arithmetic operation follows that
operation's result law.

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

A Decimal Presentation retains its exact coefficient and scale. Its numerical value is `coefficient × 10^(-scale)`.
Numerical cohort equality may equate `2.0` with `2.00`, but Presentation Sameness does not, as specified in Section 5.2.

A Decimal Operation that produces a Decimal Datum must determine its coefficient and scale. When the operation is
source-bound, its preferred scale and rounding follow Section 4.3. An exact calculation whose law requires no
intermediate Decimal result is not assigned one merely because its operands are Decimal.

An established Decimal Datum cannot have its required scale changed on the ground that its numerical value is unchanged.
A separately defined conversion may form a new result under its own law.

## 9.2. Exact and Rounded Operations

Addition, subtraction, multiplication, comparison, division, integral division, remainder, powers, scale conversion,
square root, and conversions must be qualified individually, including each source overload. Exact division requiring a
finite Decimal result is partial if the quotient has no admissible representation or another domain condition fails.
Other exact mathematical division follows Section 4.4. Scale-changing operations without rounding are partial if an
exact result cannot be represented in the required result scale.

A rounded Decimal operation must fix its rounding rule and any precision or scale constraints required by its result
law. The closed Java `RoundingMode` cases `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN`, and
`UNNECESSARY` retain their exact distinct meanings. `UNNECESSARY` is legal only where no rounding is needed. A
`MathContext` contributes its exact precision and rounding case; default process-wide context is not inferred.

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

Numbers establishes neither a separate Numeric Definition nor a Numeric Version Authority. Contract Required Basis and
applicability remain governed by ADR-0063. Contract Version Identity and binding remain governed by ADR-0053. An
external numeric specification becomes Required Basis only where the owning Contract law requires it.

A Contract Version must not silently acquire different Numeric Meaning when Kontrakt changes. The Numeric Law used to
interpret that established meaning must remain equivalent. A change that alters a required Numeric Result is a change of
meaning, not a compatible realization update. Its version and applicability are governed by ADR-0053 and ADR-0063, not
by an implicit Kontrakt release number.

An unsupported operation or an operation outside its domain does not establish Admission `Rejected`. Failure and
resource refusal retain their respective owners under ADR-0057, ADR-0051, and ADR-0052.

# 13. Realization Design Ownership

Realization does not determine Numeric Meaning. Its freedom to change calculation methods is bounded by Sections 4.2 and
4.5. Compiler-result reuse remains governed by ADR-0075. Resource limits remain under their existing Contract
authorities.

# 14. Conformance and Execution Eligibility

An operation may be declared executable only when its realization is shown to preserve the complete Numeric Law. A
numerical tolerance does not establish conformance when the law requires one exact result. The means of verification do
not define that result.

# 15. Integration and Supersession Boundaries

This ADR supplies shared Numeric Meaning without superseding other Contracts. The authority split is defined in Section
4.1. Existing Input support remains limited as stated in Section 5.1. Platform admissibility follows Section 11.

ADR-0064 requires a separate compatibility decision before Binary16, bfloat16, Binary128, or new structured Numeric
Presentations may enter its V1 Input surface. ADR-0073 must independently ratify each proposed carrier and executable
use. Those Accepted documents are not amended by this Proposed ADR.

# 16. Decisions Still Required Before Acceptance

The following decisions remain open; none is made Accepted by this draft:

1. Complete the separate NaN operand and result laws for floating operations outside Section 8.3's fixed basic
   arithmetic result rule, and qualify each applicable source operation under Section 4.3.
2. Complete the deterministic result laws for supported `Math`, `StrictMath`, and Kotlin mathematical operations.
3. Complete the exact BigInteger and Decimal operation laws, including exceptional domains and result representation.
4. Resolve ADR-0065 §7.5 against Section 10.3 without silently modifying that Accepted ADR.
5. Define the semantic details of Binary16, bfloat16, and Binary128, then determine their legal Input and Platform
   boundaries under ADR-0064 and ADR-0073.
6. Complete format-specific Structured Numeric Value laws under the preservation rule in Section 4.6. Tensor structure,
   quantization, missing values, and legal observations require explicit decisions by their respective owners.
7. Determine which operations have an algorithm-independent unique result. Any exception relying on a reference
   procedure requires separate approval consistent with Section 4.3.
8. Decide how explicitly supplied measurement resolution or uncertainty participates in existing Input and Fact
   meanings, without introducing a new Numbers authority.

# 17. Rejected Directions

The following alternatives are excluded by the controlling laws rather than repeated here:

- A new Numeric 1D Contract or runtime Numeric authority: Section 4.1.
- Treating external library algorithms as Numeric Contract authority: Sections 4.6 and 11.
- One mandatory representation, universal precision mode, or implicit normalization: Sections 4.5–4.7.
- Treating host syntax or approximate tolerance as a complete operation law: Sections 4.3–4.4.
- Replacing Input Sameness with one numeric equality rule: Section 5.
- Allowing an optimization or cache hit to establish Numeric Meaning: Sections 4.1 and 4.2.
- Treating resource or computation failure as Admission `Rejected`: Section 12.
