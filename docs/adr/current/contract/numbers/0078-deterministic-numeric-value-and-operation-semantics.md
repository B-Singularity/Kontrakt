# ADR-0078: Deterministic Numeric Value and Operation Semantics

## Status

Proposed

## Date

2026-10-18

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

Numeric values contribute to Contract judgments. Their meaning must survive compiler and realization
changes.

This ADR defines shared Numeric Value and Operation semantics. It does not replace Input Presentation
Sameness (ADR-0064), Admission judgment (ADR-0065), or platform-use qualification (ADR-0073). Kontrakt
may interpret an admitted result of an external calculation without adopting the external library's
algorithm as Contract meaning.

The Numeric Realization Design implements these laws without owning their authority.

# 2. Problem

Determinism is a primary Kontrakt guarantee. Numeric results computed inside Kontrakt can contribute to
established Contract judgments and reach users through authorized interfaces. Users may rely on those
exact values as API behavior. A change in rounding, Decimal scale, or NaN bits across otherwise compatible Kontrakt
releases
can alter a user's system even when its Contract declaration remains unchanged.

Without a closed Numeric Law, incidental JVM behavior or the selected calculation algorithm could
become part of that user-facing guarantee. Freezing one implementation would prevent compatible
optimization. Replacing it with a more accurate calculation could still change an established result.

Kontrakt therefore needs Contract-level laws for Numeric Value interpretation and governed Numeric
Operation results. These laws must preserve the applicable source meaning while allowing the
realization to change. An unchanged law must retain the same legally observable numeric results
across compatible Kontrakt versions.

# 3. Decision Drivers

The governing requirements are stated in Sections 4.2, 4.3, 4.7, and 14.

# 4. Decision and Ownership

## 4.1. Shared Numeric Semantics

**Deterministic Numeric Semantics** governs the meaning of Numeric Values and Numeric Operations used in
Contract judgments. An operation may produce a nonnumeric result; Numbers defines the numeric operation's
result, not the general meaning of its result type.

Each Contract retains its own judgment and Material authority. Using a Numeric Operation does not
transfer that authority to Numbers or let a consumer redefine its result. No new 1D Contract, Numeric
authority, or user-visible numeric wrapper is introduced.

## 4.2. Determinism Requirement

The same Numeric Operation Law, legal operands, declared semantic inputs, and applicable world determine
one exact legally observable result. Neither an execution environment nor a choice of realization may
select that result implicitly.

Equality of the final Boolean judgment is insufficient if a legal observer can distinguish the Numeric
Result. Information may be discarded only when no remaining legally permitted observation requires it
under the owning law (Section 5.4).

An unchanged law must retain its result across Kontrakt releases and targets. Semantic changes follow
Section 12. Compiler-result reuse follows ADR-0075.

## 4.3. Source-Bound Operations and ADR-0073

A Java or Kotlin operation used by a Contract keeps the law of its exact resolved source callable.
Calculating it inside Kontrakt does not replace that law with ideal mathematics. Qualification uses the
applicable language or library specification, including its version when relevant.

The callable must have determined operand meaning, result format, evaluation and rounding obligations,
defined domain, and legally observable result. A recognized name or numeric return type does not
establish these obligations.

An exact result required by the source is preserved. If the source permits several results, Kontrakt may
select only a deterministic refinement compatible with every required source observation. An exact
mathematical answer is preferred when it satisfies the entire source law. A source-prescribed reference
result takes precedence over a different mathematically preferable answer.

No result law is inferred from the behavior of one JDK release. Adopting a reference procedure to resolve
an otherwise underdetermined source operation requires a separate, justified semantic decision. If a
unique lawful result cannot be established, that operation is unsupported for a governed judgment.

ADR-0073 may separately ratify a platform operation with nondeterministic permissions. Such ratification
alone does not qualify it for a deterministic Numeric judgment.

## 4.4. Kontrakt-Defined Operations and Exact Results

An operation not derived from user source follows its own complete Numeric Law. Executing it on the JVM
does not give Java or Kotlin arithmetic authority over that law.

An exact mathematical result may be defined without a finite Numeric Datum. This does not authorize an
unsupported Numeric Format (Section 4.7). Once a law has fixed an intermediate finite-format result,
later operations must consume that result even if it is not yet materialized.

Neither a source symbol nor a physical identifier establishes an operation's meaning or Contract
authority.

## 4.5. Rounding and Permitted Information Loss

The applicable operation law fixes any required result format and each semantic rounding point.
Calculation methods may change, but cannot replace a required intermediate result with a differently
rounded one. Rounding via a wider format need not equal direct rounding to the target format.

For an exact real operation, `exp(a) * exp(b)` and `exp(a + b)` agree where defined.
Source operations that round intermediate results do not generally permit this substitution. Source
evaluation order and exceptional cases remain binding under Section 4.3.

A permitted error bound constrains results but does not choose one. A deterministic refinement must
determine the actual result for every admitted input, including special cases. More accurate computation
is not sufficient if it changes the required source result.

A representation's **numerical resolution** is the set of values it can distinguish. It does not
determine a measurement's accuracy or uncertainty. Domain-specific measurement meaning remains with its
owning Contract (Section 4.6).

## 4.6. Numeric Value Interpretation and Preservation

The current required Numeric Value scope is signed and unsigned integers, including BigInteger; Decimal;
and Binary16, bfloat16, Binary32, Binary64, and Binary128. This is a semantic support target, not blanket
support for external arithmetic APIs. Vector, Matrix, and Tensor are V2 targets (Section 15.1).

An interpretable Numeric Value has a determined domain and all distinctions that its law makes
observable. Missing format information cannot be supplied by an unstated external default. Value
interpretation does not authorize normalization or loss of precision. A conversion that changes meaning
must be a separate lawful operation producing a new result.

An admitted value must not change with external mutable state or an unperformed external calculation. It
may be decoded lazily if its meaning is already determined. This requires stable legal observations, not
a particular memory copy or layout. Storage choices belong to realization unless the owning law makes
them observable.

NaN is a valid floating Numeric Datum, not a Missing or Null value. Absence and validity are interpreted
under their own laws. No numeric datum is inferred from an invalid presentation. An operation that turns
such a state into a numeric result needs its own complete law.

The producer's measurement accuracy, calculation error bounds, and units are not inferred from a Numeric
Datum. Those meanings require an owning Contract declaration.

## 4.7. Value Support, Legal Use, and Executability

A value's semantic interpretation, the legality of its Input presentation, the meaning of an operation,
and the ability to execute that operation are separate determinations. In particular, understanding a
value does not authorize every operation or carrier for it.

A complete operation law can exist without an available realization. Conversely, an operation without a
closed lawful result cannot establish a governed judgment merely because an optimizer or library can
execute it. Its nonqualification is not a new Contract Failure (Section 12).

Further Numeric Formats may be introduced without changing existing laws. Rational, Complex, and Interval
values require complete meanings before support can be claimed. No global precision mode may redefine
existing source operations.

# 5. Numeric Value Domains and Sameness

## 5.1. Input Presentation Boundary

ADR-0064's accepted V1 Input Leafs include fixed-width signed and unsigned integers, Decimal, Binary32,
and Binary64. Its Decimal Presentation preserves coefficient and scale; its admitted binary Presentations
preserve exact bits.

Section 4.6 does not itself ratify additional Input Leafs or JVM carriers. The V1 support targets for
Binary16, bfloat16, and Binary128 require the independent decisions in Section 15. Input Presentation
Sameness and Canonicalization remain with their respective owners.

## 5.2. Comparison Relations

Numeric equality is not a universal Sameness law. Floating numerical equality equates signed zeros but
not NaN with itself. Raw-bit equality distinguishes those encodings. Decimal numerical equality may
equate `2.0` with `2.00`, while Decimal Presentation Sameness does
not. An approximate comparison need not be transitive and cannot establish collection uniqueness or
hashing without the equivalence properties required by the collection law.

Integer value equality, floating ordered comparison, and an explicitly defined total order likewise
remain distinct operations. Kotlin `==`, Java `==`,
`equals`, `compareTo`, and collection membership follow their resolved
operations. `any { x == value }` cannot be replaced by `contains(value)` unless their actual
comparison and membership laws coincide. Input-owned membership and key Sameness is addressed in Section
10.2.

## 5.3. Exact Binary Format and Datum Law

An exact Binary Numeric Datum is `(F, b)`: one admitted Numeric Format and one logical
bitstring of its width. Only the following named formats are included. Their common interpretation does
not admit arbitrary exponent or fraction sizes.

| Format    | Total bits | Exponent bits `k` | Fraction bits `t` | Exponent bias `B` |
|-----------|-----------:|------------------:|------------------:|------------------:|
| Binary16  |         16 |                 5 |                10 |                15 |
| bfloat16  |         16 |                 8 |                 7 |               127 |
| Binary32  |         32 |                 8 |                23 |               127 |
| Binary64  |         64 |                11 |                52 |              1023 |
| Binary128 |        128 |                15 |               112 |             16383 |

The bitstring consists of sign `s`, unsigned exponent `e`, and
unsigned fraction `f`, in that order from most to least significant bit. With
`Emax = 2^k - 1`, every bitstring has one interpretation:

- `e = 0, f = 0`: signed zero, retaining `s`.
- `e = 0, f > 0`: subnormal `(-1)^s × f × 2^(1-B-t)`.
- `0 < e < Emax`: normal `(-1)^s × (2^t + f) × 2^(e-B-t)`.
- `e = Emax, f = 0`: signed infinity.
- `e = Emax, f > 0`: NaN, retaining `s` and the complete fraction field.

For NaN, write `f = q × 2^(t-1) + p`, where `q` is the leading fraction bit and
`0 <= p < 2^(t-1)`. `q = 1` denotes quiet NaN. `q = 0, p > 0` denotes
signaling NaN. `q = 0, p = 0` with `e = Emax` denotes infinity instead. The
selected polarity agrees with the common binary interchange encoding but is fixed by this law, not
amended automatically by an external specification.

Subnormals do not flush to zero. All signed-zero, signed-infinity, and NaN sign/payload distinctions
remain part of the datum. The NaN payload has no implied diagnostic or domain meaning. Classification
alone establishes no trap or exception outcome.

Two datums are identical exactly when `F1 = F2` and `b1 = b2`. Thus
Binary16 and bfloat16 with the same bits remain different. Datum identity does not establish Input or
Fact Sameness, Contract identity, or reuse permission under ADR-0075.

The bits prescribe neither byte order nor storage. The payload occupies the existing format bits, not an
extra field. A primitive integer carrier may retain signaling NaN bits while a floating conversion may
quiet them. No carrier is presumed lossless or prohibited solely from its format name; each path must
satisfy ADR-0073. Physical padding and alignment are Design choices.

## 5.4. Legal Observation and Distinction Lifetime

An exact datum's distinctions may be observed only by an operation or Contract consumer entitled to
observe them. For instance, a lawful raw-bit operation distinguishes NaN payloads, while
`isNaN` can return the same Boolean. An operation that forms a new result follows its
own law; it does not automatically propagate the source representation (Sections 4.3–4.5 and 8).

Under the basic arithmetic profile of Section 8.3, adding `1.0f` to
`Binary32(0x7fc00001)` can produce the newly determined NaN `Binary32(0x7fc00000)`. The original
operand remains `0x7fc00001` for an authorized raw-bit observation. Consequently,
`x + 0.0f -> x` is not an unconditional lawful rewrite: NaN results and signed zero can differ.
Rewrites require equivalence for the complete admitted domain and legal observations.

An original distinction can cease to constrain later realization only after its legally possible
observations are discharged or the owning law establishes a new result without it. One execution that
does not observe the distinction is insufficient. Storage reclamation does not change an established
datum. ADR-0073 governs observation discharge; ADR-0075 governs result validity and reuse. Raw-bit access
does not itself authorize outward disclosure of NaN payloads.

# 6. Fixed-Width Integer Semantics

## 6.1. Width, Signedness, and Promotion

Each operation uses its exact operand and result widths, signedness, and source promotion rule. Java may
promote a narrow integer to `int`; Kotlin may resolve a different overload. The
frontend must resolve the operation rather than infer it from a storage lane.

Wrapping addition, subtraction, and multiplication at width `w` produce bits modulo
`2^w`. Bitvector arithmetic is not exact mathematical integer arithmetic. A modular
identity may be used only when the required result and observations are preserved. It cannot silently
replace checked arithmetic or alter a signed comparison.

## 6.2. Division and Remainder

Division and remainder retain the source law's quotient direction and remainder sign. Truncating, floor,
ceiling, and Euclidean laws are distinct when their domains or results differ. Each qualified law fixes
both quotient and remainder.

Java fixed-width signed division of the minimum signed value by `-1` has its defined
wrapped result. A mathematical quotient rewrite must preserve that case. Division by zero in a throwing
source operation is outside its domain; guards and exceptional-result handling follow Section 10.1.

## 6.3. Bitwise and Shift

Bitwise operations, shifts, rotates, population counts, leading/trailing zero counts, and bit reversal
follow their exact bitvector laws. A shift's source-defined distance masking, extension, narrow
promotion, and result width are binding. A width-sized Java shift distance is not automatically invalid.

`BigInteger` shift semantics are not substituted for fixed-width shifts merely because the
mathematical expressions resemble one another. Negative shift distances and arithmetic right shifts
follow the exact resolved operation.

## 6.4. Checked Operations and Conversion

Checked arithmetic includes source-defined checked negation, absolute value, increment, and decrement, as
well as checked addition, subtraction, and multiplication. These operations and exact narrowing are
partial when they can throw on overflow. Their definedness is observable, so an optimizer may not
eliminate a checked intermediate unless its guard and legal behavior are preserved.

Sign extension, zero extension, truncation, saturation, and rounding are different conversions. Bit
reinterpretation is not numeric conversion. The exact source law governs each; a backend operation with
poison or undefined behavior cannot replace a defined source conversion outside the backend's safe domain
without a guard or equivalent realization.

# 7. Arbitrary-Precision Integer Semantics

A qualified `BigInteger`-like arithmetic operation produces the exact mathematical integer
required by its source law, within its legal domain. Supporting BigInteger values does not admit the
library's methods as a group (Section 4.7).

Division, truncating remainder, and nonnegative modulus are distinct. A modulus operation may require a
positive modulus. Ordinary powers may exclude negative exponents; modular exponentiation may admit them
when the modular inverse exists. The same exact-source rule covers modular inverse, greatest common
divisor, shifts, and exact conversion bounds. Division by zero remains partial. Mathematical equality
does not license substitution across different source domains.

Signed bitwise operations use their own infinite sign-extension semantics. They are not widened
fixed-width bitvectors. A negative shift distance reverses direction only where the exact source law says
so.

A probabilistic primality test, ambient random generator, or algorithm-dependent classification is not
qualified just because it returns a Boolean or integer. A distinct deterministic primality law would
require its own complete result and source compatibility under Section 4.3.

No multiplication, division, or modular algorithm is mandated. Resource exhaustion cannot change the
mathematical result or become an ordinary `false` or Admission `Rejected`.
Its ownership follows Section 12.

# 8. Binary32 and Binary64 Semantics

## 8.1. Representation and Value Operations

An admitted Binary32 or Binary64 Input preserves its entire legal bit pattern. Representational uses
cannot collapse signed zero or canonicalize an original NaN. Source conversions and newly produced
numeric results instead obey their qualified operations (Sections 4.6 and 5.4).

## 8.2. Basic Arithmetic and Rounding

The default Java-compatible Binary32/64 basic-arithmetic profile rounds finite results to nearest, ties
to even, at the required result precision. It preserves gradual underflow and the sign of a rounded zero.
Overflow and underflow outcomes follow the exact result law, as do other exceptional numeric conditions.
Source arithmetic does not gain extra IEEE flags or traps without that law.

Wider intermediates, flush-to-zero, reassociation, reciprocal replacement, or FMA contraction are
permitted only when they preserve the source result and its required rounding boundaries (Section 4.5).
Fused multiply-add rounds its exact product-plus-sum once and is distinct from two separately rounded
operations. Java floating `%` does not use the IEEE remainder quotient rule.

Floating addition is not generally associative. Aggregation order and lawful reductions are governed by
Section 10.2.

## 8.3. NaN Result Closure

For qualified addition, subtraction, multiplication, division, remainder, and fused multiply-add under
the default Java-compatible Binary32/64 profile, an operation whose law requires NaN produces quiet NaN
`0x7fc00000` (Binary32) or `0x7ff8000000000000` (Binary64), provided its source law
permits that result. This applies whether the NaN arose from NaN operands or other exceptional
arithmetic.

This is a newly produced result, not Input normalization. NaN inputs do not always imply NaN output: an
operation with an expressly determined non-NaN result retains it. If the source cannot permit the fixed
NaN result, another lawful exact result must be determined or the operation remains unsupported.

The fixed NaN rule does not extend to other floating callables. Their source-specific NaN and signed-zero
behavior is qualified under Sections 4.3 and 8.4–8.5. Signaling NaN bits remain protected in an original
Presentation; a carrier that may quiet them is not presumed lossless (Section 5.3).

## 8.4. Classification, Comparison, and Raw Bits

Floating classification, comparison, sign operations, and bit conversions have their own exact laws.
Primitive numerical equality differs from boxed Java equality and raw-bit equality (Section 5.2).

Canonicalizing `doubleToLongBits` is not `doubleToRawLongBits`; `longBitsToDouble` does
not guarantee signaling NaN preservation through later floating use. Raw-bit access requires a source
representation and realization that preserve the claimed bits.

In particular, `Math.copySign` and `StrictMath.copySign` are not interchangeable for a NaN
sign operand. The exact callable governs it. Minimum and maximum operations can select NaN, a non-NaN
operand, or a signed zero according to their own source laws. Their unspecified NaN result bits require
lawful deterministic refinement wherever those bits are observable (Section 4.3).

## 8.5. Mathematical Functions

Each elementary or transcendental function must have one complete reproducible result law for its
admitted domain. Correct mathematical rounding may provide that result when compatible with the source.
An error tolerance alone cannot select among multiple permitted results (Sections 4.3–4.5).

Java `Math`, `StrictMath`, and Kotlin callables are qualified separately by
exact overload and applicable specification. A `StrictMath` operation that requires a
specified fdlibm result must preserve it even if another calculation is more accurate. A
`Math` operation allowing approximations must preserve its specified accuracy,
monotonicity, and special cases when Kontrakt selects a deterministic refinement. A historical
implementation's output is not an automatic reference law.

An unqualified function cannot contribute to governed judgment. An external Adapter may still supply an
already calculated value through a legal Input path (Section 11).

# 9. Decimal Semantics

## 9.1. Value and Result

A Decimal Presentation has an exact coefficient and scale; its numerical value is
`coefficient × 10^(-scale)`. Its Presentation Sameness is specified by ADR-0064, distinct from numeric
cohort equality (Section 5.2).

A Decimal-producing operation determines the full coefficient/scale result. A source-bound preferred
scale must be retained even when a differently scaled result has the same numerical value. A distinct
conversion may form a new datum. Exact mathematics does not create an intermediate Decimal rounding point
unless the operation law requires one.

## 9.2. Exact and Rounded Operations

Decimal arithmetic is qualified by exact source overload, including division, powers, scale changes, and
square root. Division requiring a finite Decimal may be partial when the exact quotient has no admissible
finite representation. A scale change that forbids rounding is likewise partial if it cannot represent
the exact value. Ideal mathematical division follows Section 4.4 instead.

Rounded operations preserve the source's precision, scale, and rounding law. Java
`RoundingMode` cases `UP`, `DOWN`,
`CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`,
`HALF_EVEN`, and `UNNECESSARY` retain their distinct meanings.
`UNNECESSARY` is legal only when no rounding is needed; otherwise the source operation retains
its exceptional behavior (Section 10.1). A `MathContext` supplies its declared precision and
mode; no process-wide default context is inferred.

Context-free and context-bearing overloads are distinct. `BigDecimal.divide(BigDecimal)` cannot silently acquire
rounding. Source-prescribed preferred scales and representation-specific results remain part of the
complete Decimal Datum. An error bound that permits multiple outputs is insufficient under Section 4.5.

`BigDecimal.pow(int)` and `BigDecimal.pow(int, MathContext)` have different result laws. The latter's documented
error allowance does not select one output; it needs a lawful deterministic refinement or remains
unsupported. `BigDecimal.sqrt(MathContext)` has its own preferred-scale, precision, rounding, and exceptional
conditions. A current implementation result is not silently promoted into any of these laws.

## 9.3. Formation, Conversion, and Text Projection

Decimal formation from a string, an integer, a coefficient/scale pair, a binary value, or a
floating-to-text conversion follows the actual source operation. `new BigDecimal(double)` and
`BigDecimal.valueOf(double)` are different and must not be conflated by constant folding.

Exact and lossy integer conversions remain distinct. `toString`, `toPlainString`,
and `toEngineeringString` may produce different deterministic Text. Formatting and parsing do not
redefine Input Presentation Sameness.

# 10. Cross-Operation Composition and Partiality

## 10.1. Guard and Evaluation Order

A partial operation can be used only when its legal domain is established on every path that reaches it,
or when a supported non-exceptional result law represents its partiality. Source guards and short-circuit
evaluation must remain effective after transformation.

Independent Admission conditions do not inherit source evaluation order from the class-wide judgment and
cannot implicitly guard one another. Exceptions and arithmetic traps are not Admission outcomes. Compiler
or resource refusals also remain with their own authorities (Section 12).

Source qualification and proven-unreachable path elimination follow ADR-0065 §7.5. Removing an
unreachable lawful path does not legalize forbidden source.

## 10.2. Collections and Reductions

Collection element predicates follow their selected numeric comparison. `Membership.contains` and
`Association.containsKey` instead use Input-owned Presentation Sameness. Whole-Collection equality also
preserves any declared constituent and order distinctions. A compiler may exchange these relations only
after establishing equivalence on the actual domains (Section 5.2).

Aggregation over a Sequence preserves its semantic order. An unordered Membership gains no order from
physical iteration. Reassociation or parallel reduction requires equivalence under the exact numeric
result and guard laws. An independently defined exact reduction may establish a different accumulation
law without changing ordinary source arithmetic (Section 4.5).

## 10.3. Source Legality and Optimization

Source eligibility follows the owning authoring law. A proven unreachable path may avoid lowering, but
forbidden or invalid source is not legalized by dead-code elimination (ADR-0065 §7.5).

# 11. Numeric Qualification and Platform Audit

Only operations qualified under Sections 4.3–4.7 and their format-specific laws may contribute to
governed judgments. A complete Numeric Law without a verified executable realization remains meaningful
but is not executable support (Section 14).

ADR-0073 decides whether a platform or external call may execute inside the governed boundary. A lawful
external calculation may instead supply an already computed value through an approved Adapter or Input
path. Neither a numeric return type, runtime reflection, nor dynamic provider discovery authorizes an
otherwise prohibited call.

Source qualification knowledge and its reuse remain compiler products under ADR-0075, not Numeric or
Contract authority.

# 12. Establishment, Identity, Version, and Failure Boundaries

Numbers creates neither a separate Numeric Definition nor a Numeric Version authority (Section 4.1).
ADR-0063 governs Required Basis and applicability; ADR-0053 governs Contract Version binding. An external
specification becomes Required Basis only when an owning Contract requires it.

A change that alters the Numeric Result of an unchanged law is a semantic change. It cannot be hidden by
a Kontrakt release or a different implementation. Existing Contract Version and Establishment rules
govern its binding.

External standards are evidence for defining and verifying closed Numeric Laws, not automatic amendments
to them. A source-bound operation must nevertheless satisfy the exact applicable source specification (Section 4.3). An
external format needs an unambiguous authorized mapping; a changed external
specification requires renewed compatibility judgment.

An unsupported or out-of-domain operation does not establish Admission `Rejected`. ADR-0057
owns Failure; ADR-0051 and ADR-0052 own Budget and Capacity stops. Compiler safety and realization
exhaustion likewise cannot fabricate a Contract outcome.

# 13. Realization Design Ownership

The Numeric Realization Design may choose algorithms and storage under Sections 4.2 and 4.5.
Compiler-result validity and reuse follow ADR-0075. Its decisions do not change Numeric Meaning.

# 14. Conformance and Execution Eligibility

An operation may execute in governed processing only when its exact Numeric Law is qualified and the
selected realization is shown to preserve that law over its admitted domain. Conformance covers the
required result and every legally observable distinction. It also covers definedness and required
evaluation behavior under Sections 4.3–5.4. Agreement on selected inputs or within a tolerance does not
establish conformance to an exact result law.

Kontrakt may rely on an independently ratified platform guarantee when the admitted operation executes
unchanged under ADR-0073. It need not verify the internal implementation of a conforming JVM. If
Kontrakt selects one deterministic refinement from several source-permitted results, the selected
execution must produce that result. A substituted calculation or compiler transformation must satisfy
its applicable preservation obligations. Verification methods remain Compiler Design responsibilities
and do not define Numeric Meaning.

Execution eligibility must remain valid for the applicable source specification and realization
conditions. Changes that can affect conformance require renewed qualification. Compiler-result reuse
follows ADR-0075. When conformance cannot be established, that execution path is not eligible for
governed use. This does not invalidate the Numeric Law or establish Admission `Rejected`. No verification
result grants Contract authority.

# 15. Integration and Supersession Boundaries

ADR-0078 does not supersede any owning Contract (Section 4.1). The current Input scope is fixed by
ADR-0064 (Section 5.1). Binary16, bfloat16, and Binary128 are V1 exact-bit Scalar Input Leaf support
targets, but ADR-0064 must ratify their Presentation laws and ADR-0073 must qualify their carriers and
executable uses. This Proposed ADR does neither.

## 15.1. V2 Vector, Matrix, and Tensor Support

Vector, Matrix, and Tensor are V2 Structured Numeric Value support targets across Kontrakt's pipeline.
Their earlier mathematical analysis remains Design/Research, not V1 ratification. A homogeneous indexed
value has a determined shape, an element Numeric Format, and an exact datum at each coordinate. This
model alone is not complete Tensor support.

A V2 extension must define the additional meaning of each claimed supported form, including heterogeneous
elements when admitted. Sparse forms must resolve the observation of explicit zeros, duplicates, missing
coordinates, and entry order. They must also fix any operation that combines entries, unless the owning
value law itself determines the resulting numeric value. Ragged coordinates require a closed coordinate
law. Quantization requires its parameters and coordinate-dependent interpretation. Masked and labeled
forms must retain their respective presence and domain distinctions. Coordinate-wise numerical equality
does not alone establish an owning Presentation's Sameness.

Support must extend from Interaction/Input through Admission and Canonicalization to Lowering and Fact
Establishment. It must also cover Invariant, Operation/result judgment, and Output/Publication. Each
owning Contract must close the meaning it uses or establishes. Input retains presentation and presence
law. Canonicalization and Lowering own their transformations and required relations. Fact retains
established meaning and Sameness; Output/Publication governs projection. Policy and Governance
obligations remain with their owners. Budget and Capacity retain resource authority. Failure and
Diagnostics retain their own authorities. Platform execution remains separately qualified. No new Tensor
Contract authority is introduced. Tensor operations still need separate complete result laws under
Section 4.7.

A V2 realization may use primitive slabs, compressed storage, or device representations without making
them Numeric Meaning. An external Tensor object or buffer is not automatically a lawful Input or Fact. V1
may consume lawfully admitted scalar or aggregate results of external Tensor calculations without
claiming verification of the external calculation. The V2 extension must preserve established V1 Scalar
Numeric Laws. Supporting one Tensor form does not imply support for every framework or operation.

# 16. Decisions Still Required Before Acceptance

The shared operation-qualification law is closed by Sections 4–11. New callables require individual
qualification; universal coverage of Java, Kotlin, BigInteger, or Decimal APIs is not claimed.

The remaining V1 integration work is the independent Input and carrier ratification described in Section

15. Section 5.3 already closes the three formats' Numeric Meanings.

The V2 work in Section 15.1 is not an acceptance condition for the current Scalar Numeric Law.

# 17. Rejected Directions

Rejected alternatives are governed by Sections 4.1–4.7, 5.2, and 12. This section establishes no
additional restrictions.