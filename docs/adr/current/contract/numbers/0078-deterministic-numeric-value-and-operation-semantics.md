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

A source-bound Numeric Operation is qualified by its resolved callable and applicable source specification, including
the
language and specification version wherever they affect meaning. Qualification is complete only if its legal operand
meaning, result type and format, semantic rounding points, required evaluation behavior, exceptional domain, and
complete
legally observable result are determined. An exact mathematical function and an operation with the same name in a host
library are not presumed to be the same law. A numeric return type or a recognized method name is insufficient.

If the source specification already determines the result, that result is preserved. If it permits several results,
Kontrakt may admit only an exact deterministic refinement permitted for every relevant source observation under
ADR-0073. An algorithm-independent exact mathematical result, with its legally permitted rounding and source-specific
special cases, is preferred when that choice satisfies the entire source law. Where the source law itself requires a
specified reference result, that source requirement is preserved. A newly selected reference procedure is not inferred
from one observed JDK release or implementation; using such a procedure to close an otherwise underdetermined law
requires
an explicit and independently justified semantic decision. If no legal unique result is established, the operation is
unsupported for governed judgment. Merely promising repeatability within one Kontrakt release is insufficient.

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

When a source operation admits multiple outcomes within an error bound, that bound is a constraint on possible results,
not the Numeric Result Law itself. A deterministic refinement must name one result for every legal input, including
special cases. A correctly rounded mathematical result is eligible only after the exact source operation's exceptional
rules, representation requirements, and other mandatory constraints have been satisfied. Improved numerical accuracy
alone cannot justify a changed source-bound result. The semantic result cannot depend on the selected realization,
compiler pass, target, cache state, or Kontrakt release.

## 4.6. Numeric Value Interpretation and Preservation

Kontrakt's current required Numeric Value scope includes:

- Signed and unsigned integers, including BigInteger.
- Decimal.
- Binary16, bfloat16, Binary32, Binary64, and Binary128.

These are required semantic support targets, not a promise to implement their external calculation APIs. Vector,
Matrix, Tensor, and their structured numeric variants are V2 support targets under Section 15.1, not part of the
current required Numeric Value scope.

A Numeric Value is interpretable when its domain and legally observable distinctions are fully determined. The
information needed to determine its numeric meaning must be sufficient under the applicable value law. Kontrakt cannot
infer an omitted Numeric Format or other meaning-determining information from an unstated external default. Value
Interpretation may be complete without supporting any particular calculation on that value.

A structured numeric form requires its own complete value law before it can be claimed as supported. Shape and element
count alone do not establish Tensor meaning; sparse entry presence, duplicate-coordinate interpretation, quantization
parameters, ragged coordinates, and presence or validity distinctions may require additional laws. The preliminary
mathematical model of a homogeneous indexed value does not ratify a V1 Tensor Presentation, Fact, or operation. These
V2 questions remain open under Section 15.1 and do not determine current Scalar Numeric Meaning.

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

A floating NaN is a Numeric Format value, not a universal Missing or Null marker. Missing, Null, and invalid
presentations follow their owning presence and validity laws. Numbers does not infer a numeric datum where no valid
numeric value has been established, nor convert a valid NaN into absence. A future masked or validity-bearing Tensor
must retain those owners under Section 15.1. Any operation that resolves these states into a new numeric result requires
its own complete law.

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
Interval values require their own closed meanings before being claimed as supported. Vector, Matrix, and Tensor follow
the V2 integration boundary of Section 15.1. A new format must preserve its own distinctions and must not obtain
Contract authority from the external runtime that supplies it. No global precision mode is introduced to redefine
existing source operations.

An operation not yet qualified under Sections 4.3–4.5 is not provisionally admitted on the assumption that a later
optimizer, JVM library, or realization can give it a stable answer. This is a limit on the set of governed operations,
not a refusal to interpret an otherwise legal Numeric Value and not a new Contract Failure. New operations may become
supported after their meanings are closed without changing the meaning of previously qualified operations.

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

## 5.3. Exact Binary Format and Datum Law

A Binary Numeric Datum is determined by its exact Numeric Format `F` and one logical bitstring `b` of that format's
width. The supported binary-format meanings are closed to the five named formats in Section 4.6. One common
parameterized interpretation applies to these named formats; arbitrary exponent and fraction widths are not thereby
admitted as further Numeric Formats. The corresponding parameters are:

| Format    | Total bits | Exponent bits `k` | Fraction bits `t` | Exponent bias `B` |
|-----------|-----------:|------------------:|------------------:|------------------:|
| Binary16  |         16 |                 5 |                10 |                15 |
| bfloat16  |         16 |                 8 |                 7 |               127 |
| Binary32  |         32 |                 8 |                23 |               127 |
| Binary64  |         64 |                11 |                52 |              1023 |
| Binary128 |        128 |                15 |               112 |             16383 |

From most significant to least significant bit, `b` consists of the sign bit `s`, the unsigned exponent field `e` of
width `k`, and the unsigned fraction field `f` of width `t`. Let `Emax = 2^k - 1`. Every logical bitstring of the
required width has exactly one interpretation within its specified format:

- `e = 0` and `f = 0`: signed zero, retaining `s`.
- `e = 0` and `f > 0`: subnormal finite value `(-1)^s × f × 2^(1-B-t)`.
- `0 < e < Emax`: normal finite value `(-1)^s × (2^t + f) × 2^(e-B-t)`.
- `e = Emax` and `f = 0`: signed infinity, retaining `s`.
- `e = Emax` and `f > 0`: NaN, retaining its sign and complete nonzero fraction field.

For a NaN, the most significant fraction bit `q` is the quiet/signaling discriminator. Write
`f = q × 2^(t-1) + p`, with `0 <= p < 2^(t-1)`. A NaN with `q = 1` is quiet and permits any `p`; a NaN with `q = 0`
is signaling and requires `p > 0`. When `q = 0` and `p = 0` at the reserved exponent, the datum is infinity, not NaN.
This polarity deliberately agrees with the widely used binary interchange encoding; the external specification does
not own this Kontrakt law. The sign and fraction fields do not, by themselves, give the payload a diagnostic,
identifier,
or other domain meaning. Classification does not independently establish a trap or exception result.

The interpretation retains subnormals without flush-to-zero and distinguishes both signs of zero, both signs of
infinity, and all NaN bit patterns. The exact positive finite boundaries derived from the common law are:

| Format    | Least positive normal | Least positive subnormal | Greatest finite        |
|-----------|-----------------------|--------------------------|------------------------|
| Binary16  | `2^-14`               | `2^-24`                  | `(2-2^-10) × 2^15`     |
| bfloat16  | `2^-126`              | `2^-133`                 | `(2-2^-7) × 2^127`     |
| Binary128 | `2^-16382`            | `2^-16494`               | `(2-2^-112) × 2^16383` |

The following examples are consequences of the law, not separate operation laws:

| Format    | Positive infinity                    | Quiet NaN                            | Signaling NaN                        |
|-----------|--------------------------------------|--------------------------------------|--------------------------------------|
| Binary16  | `0x7c00`                             | `0x7e00`                             | `0x7c01`                             |
| bfloat16  | `0x7f80`                             | `0x7fc0`                             | `0x7f81`                             |
| Binary128 | `0x7fff0000000000000000000000000000` | `0x7fff8000000000000000000000000000` | `0x7fff0000000000000000000000000001` |

Binary16 `0x3c00 = +1`, bfloat16 `0x3f80 = +1`, and Binary128
`0x3fff0000000000000000000000000000 = +1` follow the same interpretation.

Exact Binary Datum identity is defined by
`(F1, b1) ≡datum (F2, b2)` if and only if `F1 = F2` and `b1 = b2`. Two 16-bit values with identical bitstrings but
formats Binary16 and bfloat16 are different datums. Positive and negative zero, and NaNs that differ in sign,
quiet/signaling bit, or payload, are likewise different datums. Exact Datum identity is not numeric comparison, Input
Presentation Sameness, Fact Sameness, Contract identity, or permission to reuse a compiler result under ADR-0075.

These are logical bits, not a prescribed byte order, host carrier, storage layout, or extra physical payload. A
meaning-preserving physical representation may use fixed-width primitive lanes, with padding or cache-line alignment
under Design ownership; the NaN payload is already within the datum's declared bit width. An integer bit carrier may
retain a signaling NaN pattern without performing floating arithmetic. By contrast, an actual floating-value carrier
or conversion may quiet it. Each proposed path must preserve the observations required of that path under ADR-0073;
no blanket carrier prohibition or blanket losslessness follows from the format name.

## 5.4. Legal Numeric Observations and Distinction Lifetime

A distinction in an exact Binary Datum is not a general authorization for every operation or Contract consumer to
observe it. An authorized raw-bit operation may distinguish two NaN payloads; a qualified `isNaN` operation may return
the same Boolean for both. A representation-preserving use must preserve all distinctions required by its applicable
law. Classification, numeric equality, ordered comparison, sign manipulation, bit reinterpretation, format conversion,
and newly produced arithmetic results instead follow their individually qualified operation laws under Sections 4.3–4.5
and 8. Raw-bit observation does not automatically authorize external disclosure of a payload or reinterpretation of it
as Diagnostic meaning.

Value preservation does not impose universal NaN payload propagation. Under Section 8.3's applicable basic-arithmetic
profile, `Binary32(0x7fc00001) + 1.0f` may be required to yield the newly formed canonical NaN
`Binary32(0x7fc00000)`, while a legal raw-bit observation of the original operand still yields `0x7fc00001`.
Consequently, the unconditional rewrite `x + 0.0f -> x` is not valid for that profile over all Binary32 inputs: NaN
payloads may differ, and signed zero can also distinguish the results. Such a rewrite is legal only on a domain and
under observations for which the complete applicable operation law establishes equivalence. The requirement can limit
optimization; it does not mandate a single calculation algorithm or permanent storage of every original bit pattern.

An original distinction may cease to constrain later realization only when its owning observation obligations have
been discharged or an owning law has established a new result that does not retain that distinction. The absence of an
observation in one current execution is not sufficient if a legally possible later consumer still requires it. Replacing
or reclaiming physical storage never itself changes the established datum. Observation discharge remains governed by
ADR-0073, and compiler-result validity and reuse remain governed by ADR-0075.

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

For each BigInteger-like callable selected for governed use, the exact source operation determines its input
restrictions
and complete result, including the sign convention of quotient and remainder, positivity of a modulus, existence of a
modular inverse, exponent restrictions, and exact conversion bounds. The mathematical equality of two formulations does
not permit substituting one callable for another when their valid domains or exceptional behavior differ. These laws do
not require one multiplication, division, or modular arithmetic algorithm.

A deterministic Boolean or integer result is not inferred from a method's return type. Operations defined by
probabilistic evidence, ambient randomness, or algorithm-dependent classification are not qualified merely because they
are methods of a supported numeric type. If a distinct exact operation is established, its source-law compatibility
and independently closed result must be demonstrated before use.

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

For floating operations outside the fixed basic arithmetic profile of Section 8.3, qualification is per exact callable.
Unary negation, absolute value, sign and sign-copy operations, minimum and maximum, and other operations must retain
their distinct source laws for NaN operands, signed zero, and any representational observation. An input NaN does not
uniformly entail a NaN result, and a newly produced NaN does not uniformly inherit its operand's sign or payload.
Section 8.3's fixed NaN bits do not supply a default result for these operations.

For example, Java `Math.copySign` and `StrictMath.copySign` cannot be treated as interchangeable when the sign operand
is
NaN. The resolved callable's own permitted NaN-sign rule must be honored. Likewise, a minimum or maximum that returns
NaN, a non-NaN operand, or a selected signed zero cannot be reduced to one universal floating comparison law. A source
law that leaves NaN result bits unspecified requires an explicitly lawful deterministic result refinement wherever those
bits can be legally observed. Otherwise that use remains unsupported. None of these refinements normalizes an already
admitted Input datum.

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

For mathematical functions, qualification begins with the exact source overload and its versioned language or library
law. Java `Math`, `StrictMath`, and Kotlin callables are audited separately. Where a Java `StrictMath` callable requires
a
specified fdlibm result, a different correctly rounded mathematical result is not substituted merely because it is
more accurate. Where a Java `Math` callable permits multiple approximations, its stated accuracy, monotonicity, special
cases, and other observations constrain any selected deterministic refinement. The same method spelling or
mathematical name does not imply the same result law in Kotlin or Java.

A correctly rounded result may close a mathematically defined operation only when the result format, rounding,
exceptional inputs, and source requirements make it one legal answer. The exact result of an ideal real function does
not
by itself specify the complete result of a floating library callable. A tolerance or quality-of-implementation bound
without a unique selection is insufficient; a specific historical library output is not automatically a normative
reference result. An operation without a complete legal result law remains unsupported for governed use, independently
of whether an external adapter may lawfully supply an already computed value.

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

For each supported Decimal operation, the result law determines the full Decimal Datum, not only its numerical value.
Where the source law prescribes a preferred scale, a precision-dependent representation, or a special result for an
exact
value, those requirements are retained. Inexact rounding is permitted only where the exact overload admits it;
`UNNECESSARY`
does not become a request for the nearest result when its exactness condition fails.

`BigDecimal.pow(int)` and `BigDecimal.pow(int, MathContext)` do not share a general result law. The latter's documented
error allowance and potential future algorithm changes do not, by themselves, choose one Numeric Result. The operation
must receive a lawful exact deterministic refinement covering its result coefficient and scale and its exceptional
domain, or remain unsupported for governed use. A current implementation's observed output is not silently frozen as
the source law. `BigDecimal.sqrt(MathContext)` is likewise qualified with its own exact rounding, preferred scale,
precision, and exceptional conditions rather than being grouped with `pow` or a binary host `sqrt`.

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

The domain of a partial Numeric Operation is part of that operation's meaning, not a license for a new Admission
outcome.
A source guard may establish that the operation is reached only on its legal inputs. A transformation cannot remove the
guard, change the order of required observations, or replace a checked operation with a total one unless the original
source law and its legally observable behavior are preserved. Whether an unreachable but otherwise unsupported source
form is admissible remains governed by the unresolved ADR-0065 Section 7.5 conflict in Section 10.3; Numeric partiality
does not settle that source-legality question.

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

An operation selected for governed use must be distinguished from an operation whose numeric family is only understood.
When the complete Numeric Result Law is not established for the exact callable, it remains unqualified. When its meaning
is complete but the available realization has not demonstrated conformance, the meaning remains valid but executable
support is not claimed. In neither case may a library's current behavior, an optimizer's rewrite, or a cache hit provide
missing semantic qualification. Qualified source operations and separately defined mathematical operations remain
distinct even if their current results happen to agree.

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

External numeric standards and their revisions are evidence for defining and verifying Kontrakt's own closed value
laws, not automatic amendments to them. This independence does not waive a qualified source-bound operation's
compatibility obligations under Sections 4.3–4.4: the exact applicable source specification still constrains its
result and legal observations. An external format can be consumed only through an unambiguous, lawfully established
mapping to the required Kontrakt meaning; a changed external specification requires a fresh compatibility judgment.

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

ADR-0064 requires a separate compatibility decision before Binary16, bfloat16, or Binary128 Presentations may enter
its V1 Input surface. ADR-0073 must independently ratify each proposed carrier and executable use. A V2 Tensor
Presentation also requires its own owning-Contract decisions under Section 15.1. Those Accepted documents are not
amended by this Proposed ADR.

## 15.1. V2 Vector, Matrix, and Tensor Support

Vector, Matrix, and Tensor are assigned to V2 as an explicit cross-pipeline Structured Numeric Value extension. Their
mathematical investigation is retained as V2 Design/Research, not ratified here as current supported value semantics.
A base homogeneous indexed value can be described by a determined shape, one element Numeric Format, and one exact
element datum at each logical coordinate; that description alone does not constitute complete support for Tensor
systems.
Sparse, ragged, quantized, heterogeneous, masked, or labeled forms need separate closed meanings wherever they are
claimed as supported. A supported sparse form must determine whether duplicate entries, explicit zeros, omitted
coordinates, and entry order are observable; combining entries to form a new numeric result requires an exact operation
law unless that result is already completely determined by the owning structured value law. Quantization parameters,
coordinate-dependent meanings, and validity must likewise be explicit wherever the supported form requires them. The
logical meaning need not prescribe physical layout, and equality of coordinate-wise numbers alone does not determine
an owning Presentation's Sameness. V2 support does not imply universal coverage of every external Tensor framework or
operation.

For every V2 Tensor form that Kontrakt declares supported, the extension must be coherent across the existing
Interaction/Input, Admission, Canonicalization, Lowering, Fact, Invariant, Establishment, Operation/result judgment, and
Output/Publication boundaries. The owning Contracts must determine the relevant presentation and presence laws,
structural validity, Canonicalization authority, required relations and lowering, established Fact meaning and Sameness,
legal observations, and projection of results. Governance, Policy, Budget, Capacity, Failure, Diagnostics, and Platform
qualification apply where the supported use requires them. Tensor does not become a new Contract authority, and each
1D Contract continues to own only its existing judgment. Qualification of Tensor operations is separate from
interpretation of Tensor values, and each supported operation requires a complete result law under Section 4.7.

V2 may choose data-oriented primitive slabs, compressed layouts, device representations, and cache-line-aware storage
without putting those physical decisions into the Numeric Law. A Tensor object or external buffer is not thereby a
legal Input, Fact, or Core authority. V1 may lawfully consume separately admitted scalar or closed aggregate results of
external Tensor calculations under existing boundaries without claiming that the external Tensor calculation itself
was verified. V2 integration must preserve the established V1 Scalar Numeric Laws and Contract ownership.

# 16. Decisions Still Required Before Acceptance

The operation-law qualification decision is closed by Sections 4.3–4.5, 4.7, and 6–11. A callable with a complete exact
source result or an established lawful deterministic refinement may be qualified. The remaining callables are not
implicitly supported. In particular, Section 8.3's NaN result law is not extended to other floating callables, and a
library's documented error bound is not promoted to a unique result. Extending the set of qualified callables requires a
complete operation-specific law; their algorithms and physical representations remain Design work. This closes the
shared Numeric Operation decision without claiming universal coverage of Java, Kotlin, BigInteger, or Decimal APIs.

The following independent decisions remain open; none is made Accepted by this draft:

1. Resolve ADR-0065 §7.5 against Section 10.3 without silently modifying that Accepted ADR.
2. Determine which Binary16, bfloat16, and Binary128 Input Presentations and platform uses are independently ratified
   under ADR-0064 and ADR-0073. Section 5.3 closes their Numeric Format interpretations but does not ratify those uses.
3. Decide how explicitly supplied measurement resolution or uncertainty participates in existing Input and Fact
   meanings, without introducing a new Numbers authority.

Vector, Matrix, Tensor, and their structured variants are V2 support work under Section 15.1, not conditions for
accepting the current Scalar Numeric Law. Their V2 format-specific meaning, owning-Contract integration, operation
qualification, and realization still require separate decisions before those capabilities may be claimed as supported.

# 17. Rejected Directions

The following alternatives are excluded by the controlling laws rather than repeated here:

- A new Numeric 1D Contract or runtime Numeric authority: Section 4.1.
- Treating external library algorithms as Numeric Contract authority: Sections 4.6 and 11.
- One mandatory representation, universal precision mode, or implicit normalization: Sections 4.5–4.7 and 5.3.
- Treating host syntax or approximate tolerance as a complete operation law: Sections 4.3–4.4.
- Replacing Input Sameness with one numeric equality rule: Section 5.
- Allowing an optimization or cache hit to establish Numeric Meaning: Sections 4.1 and 4.2.
- Treating resource or computation failure as Admission `Rejected`: Section 12.