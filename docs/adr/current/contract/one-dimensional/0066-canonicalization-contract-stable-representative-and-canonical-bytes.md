# ADR-0066: Canonicalization Contract, Inbound Representation Control, Stable Representative, Canonical Bytes, and Explicit Omission

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization
  Boundary
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0067: Lowering Contract
- ADR-0071: Resolved Contract HIR
- ADR-0076: Canonicalization Built-In Law Catalog

---

## 1. Context

Input establishes which outside presentations may enter the Contract Machine. A legal Input can nevertheless
express the same declared meaning in different ways. If later judgments distinguish those presentations
without an explicit Contract decision, the outside producer controls meaning that the machine should own.

Canonicalization lets the author select which of those differences will cease to matter. An Interaction
selects a Canonicalization declaration through the IDL. The declaration names the direct Input coordinates
to which one exact admitted Built-In Law, or one explicitly ordered composition of such laws, will apply.
Kontrakt establishes the representative required by the selected meaning. The author selects that meaning
but cannot supply its executable authority in V1.

Canonicalization follows a successful Input Presentation and precedes the later inbound judgment that uses
its result. It cannot make refused Input legal. It also cannot change the Input coordinate shape or create
the target meaning owned by Lowering. Its absence is a legal, explicit choice.

The outbound path is different. Publication already operates on Core-authorized material, and Output
Presentation controls its outward form. Section 4.8 states why that path does not gain another
Canonicalization stage merely for symmetry.

## 2. Problem

Without an exact equivalence law, the compiler may preserve outside representation differences that later
alter lookup or authorization. An overly aggressive normalizer can instead erase distinctions the Contract
did not permit it to erase. Either mistake changes the declared machine.

A second risk arises when Admission judges one representation but Lowering or a downstream adapter uses
another. Once Canonicalization establishes a representative, later semantic use must be coherent with that
judgment. Raw evidence may remain available for its own lawful purposes, but it cannot restore a distinction
that Canonicalization erased.

A user callback or an ambient host convention cannot close a Canonicalization law. The Contract must define
the equivalence and the required representative without running application code. The law must also bound
its work on untrusted Input. Deterministic compiler serialization, although useful for realization, does not
establish semantic equivalence.

## 3. Decision Drivers

The selected law must preserve every distinction it does not explicitly erase. Its result must be
reproducible from its declared meaning and legally bound semantic bases. A noncanonical but legal Input may
succeed; illegal Input must not be repaired.

The IDL must select a declaration rather than infer a law from a host type. A selected declaration applies
only to its named coordinates. Its direct laws must already be admitted, and an explicit composition must
have independently established legality. An omitted slot or coordinate must not acquire a synthesized
identity law.

The compiler may change realization strategy, but it must preserve the full Contract result. This includes
complete refusal attribution and every semantic bound owned by the law. Authority over Budget and Capacity
remains independent.

---

## 4. Decision

### 4.1. Canonicalization Is an Inbound Representation-Control Contract

Canonicalization controls representation freedom only after Input establishes a legal Presentation. It may
replace a selected coordinate value with the exact representative of its declared meaning. A Java or Kotlin
symbol identifying a law is frontend evidence, not the law's authority.

Canonicalization cannot parse an illegal carrier into legality, derive business meaning, or create new Input
coordinates. Lowering owns shape-changing formation. Section 4.6 makes the preservation requirement precise.

### 4.2. Optional Contract

The Canonicalization slot is optional for an Interaction. Omission establishes neither a Canonicalization
Definition selection nor a Canonicalization Occurrence. Admission receives the Input-established
Presentation through the owning inbound composition when this slot is omitted.

An unnamed coordinate is also outside Canonicalization. Its value remains Input-established; no
`ExactCanonicalization` law or hidden identity application is introduced. A declaration with no selected
coordinates is invalid. The author must omit the slot instead.

Compiler-owned deterministic encoding may still exist when this slot is absent. Such encoding cannot give
the Input a canonical representative or collapse presentation distinctions.

### 4.3. Canonicalization Receives Only Legal Input Presentation

A Canonicalization Occurrence requires Input Material whose owning Input Judgment established `Presented`.
Noncanonical representation is not Input illegality. A refused Input or material not established by Input
cannot enter Canonicalization for repair.

The exact occurrence Basis and its legal entry are defined in Section 9.8. A missing prerequisite is not a
Canonicalization-owned refusal.

### 4.4. Declared Equivalence and Stable Representative

For each exact selected law `L`, `E_L` is the law-owned same-meaning relation and `C_L` is its required
representative relation on the law's successful canonicalizable domain. The law must define its supported
domain and coverage exactly; the entire legal Input domain need not be its successful domain. ADR-0076
specifies the qualification obligations, while each Exact Law Authority owns its versioned normative
meaning.

For inputs on which the stated relations apply, the common Canonicalization law requires:

```text
C_L(x) = C_L(y)  iff  E_L(x, y)
E_L(x, C_L(x))
C_L(C_L(x)) = C_L(x)
```

`E_L` must be an equivalence relation on its declared comparison domain. The representative must belong to
the same declared meaning class and be stable under reapplication. Two presentations cannot be collapsed
because a host comparator, digest, or incidental encoding considers them equal. A change in the
implementation cannot choose a different representative.

The Exact Law must make clear which distinctions remain meaningful and when its successful coverage ends.
ADR-0076 and the approved Exact Law specification own the particular operand requirement, representative
coverage, refusal semantics, and external semantic determinants. This ADR does not redefine them.

Canonicalization equivalence does not itself establish another Contract's Fact identity, authorization
judgment, or reuse eligibility. Suspicion of hostile text likewise creates no new equivalence. Other
authorities may consume an established representative only through their own declared relations.

### 4.4.1. Finite Ordered Composition of Exact Laws

One selected coordinate may name a finite, flat, explicitly ordered composition of admitted Exact Laws. The
declaration `compose(A, B, C)` is illustrative only; the Contract meaning is the ordered relation `A -> B ->
C`. Reordering its members changes the declared relation even if some observed outputs are equal.

A composition is one coordinate binding, not several bindings and not a new Catalog member. Its constituent
laws retain their independent Authorities. The composition must establish a complete lawful meaning under
its separately owning Composition law before Canonicalization Definition Judgment relies on it.

The full composition must satisfy Section 4.4. Each constituent must accept the presentation established by
its predecessor. The final representative must remain stable under reapplication, and refusal must be
deterministic. All required semantic bases must be mutually lawful. The whole computation must be bounded
and free of semantic dependency cycles. Pairwise host type compatibility is insufficient. A semantic cycle
is rejected, not evaluated by fixed-point iteration.

A later constituent cannot observe a distinction removed by an earlier constituent unless the complete
composition law explicitly makes that dependency lawful. A constituent is reached only after its required
predecessor succeeds. On refusal, later dependent constituents do not enter, as Section 9.9 specifies.

The compiler may neither change the authored sequence nor weaken a constituent's exact meaning. A proven
redundant member remains part of the declared meaning even if its physical work can be omitted without
changing the Contract result. A curated combined Built-In Law is a different exact law and is not inferred
from an apparently equivalent composition.

### 4.5. Judgment–Use Coherence

After a whole Canonicalization Occurrence succeeds, later inbound judgments must observe its final
representative for every selected coordinate. Unselected coordinates retain the exact Input-established
Presentation. The owning inbound composition produces the complete boundary presentation from those two
sources.

```text
Input Presented
  -> Canonicalization, when selected
  -> complete boundary presentation
  -> Admission
  -> Lowering
```

If the slot is omitted, the Input-established Presentation reaches Admission without a Canonicalization
result. A `Refused` occurrence cannot supply a partially canonicalized presentation, even though it
preserves legally established coordinate assessments under Section 9.9.

Admission, Lowering, and downstream adapters cannot use raw retained evidence to recover a distinction the
selected law erased. They cannot apply an undeclared second normalization and claim it is the same result.
Raw Input may remain accessible for separately authorized provenance or diagnostics. ADR-0048 owns the
complete inbound wiring; this ADR establishes only the Canonicalization-side requirement.

### 4.6. Same-Shape Representative

Canonicalization leaves the declared Input coordinate structure unchanged. It changes only a selected
coordinate's representation within the presentation domain admitted by its direct or composed law. It cannot
create or rename a coordinate, map to a different target domain, or turn an unselected coordinate into a
selected one.

### 4.7. Canonical Bytes

Exact canonical bytes are Contract Material only when the selected law explicitly owns a normative byte
relation. That law must fix the byte schema and ordering needed to interpret the output. It must also
declare any
meaning-changing bound. Changing those normative rules changes the law.

A representative law can be complete without canonical bytes. Deterministic compiler serialization is not a
substitute for them. A composed binding does not inherit a new byte protocol merely because a constituent
owns canonical bytes. Any composition-owned exact byte relation must be explicitly established by its owning
law.

### 4.8. Inbound and Outbound Boundaries Are Not Stage-Symmetric

Publication begins with Core-authorized meaning. Output Presentation controls the outward structure and does
not require an inbound-style Canonicalization stage for symmetry. If an outward protocol requires
deterministic encoding, its own authority defines that requirement.

Material leaving one Contract Machine becomes outside material upon entering another. The receiving machine
applies its own Input Contract and any selected Canonicalization.

---

## 5. V1 Canonicalization Authoring Contract

The IDL selects at most one Canonicalization declaration in the exact Interaction selection context. It does
not select individual Built-In Laws. The selected declaration names direct Input coordinates and associates
each with one exact law or one explicit legal composition. The host-language declaration is evidence for
resolution, not executable authority. Inheriting a host marker
cannot select this Contract role.

The public Kotlin or Java syntax belongs to Canonicalization Design. A source API may express `name -> LawA`
or `code -> compose(LawA, LawB)`, but only the exact resolved law relationship is normative.

### 5.1. Built-In Law Catalog Boundary

ADR-0076 owns Catalog membership and qualification. Each admitted Exact Built-In Law independently owns its
semantic Authority and exact Version under ADR-0053. A law reference resolves to that Authority's complete
versioned Definition; the Catalog does not supply a hidden current Version or acquire ownership of the
member's meaning.

A curated combined law remains one independently admitted law. A user-declared composition does not create
another Built-In Law Authority or become a Catalog member. Kontrakt may share realization work between them
without merging their Contract meanings. The omission rule in Section 4.2 eliminates the need for a generic
built-in identity law.

### 5.2. Exact Law References

Every direct law and every composition constituent must resolve to one exact admitted semantic reference. A
host name or structurally similar type cannot establish the reference. Neither can an application-defined
replacement or runtime registration. A source alias is legal only insofar as it resolves to the exact
authorized meaning rather than supplying a
different one. A requested Law Version cannot silently fall back to another Version.

An admitted law may govern a scalar or another complete ratified presentation domain for one Input
coordinate. Host generic structure does not create a recursive Canonicalization tree or authorize a law
outside its declared operand domain.

### 5.3. Coordinate Binding and Composition

Every declared coordinate selection is nominal and direct. It owns one binding whose meaning is either one
Exact Law or one finite explicit ordered composition. Two independently declared bindings for the same
coordinate are not a composition and violate the Canonicalization Definition law in Section 9.6.

The independent coordinate declaration order gives no semantic priority. The explicit order within a
composition does. Neither source iteration nor a runtime condition may choose, augment, or reorder the law
sequence. Section 4.4.1 governs composition legality.

### 5.4. Selective Coordinate Coverage

A successfully established declaration must select at least one coordinate. Every selected name must match
exactly one direct Contract-visible coordinate in the current Input context, and its law must positively
admit that coordinate's presentation meaning. An unresolved or incompatible current mapping is a declaration
or applicability error at its owning boundary. Section 9.2 distinguishes it from a fully resolved
declaration that the Definition Judgment refuses.

Coordinates not selected remain outside Canonicalization. Adding an unselected Input coordinate does not
change the Canonicalization-owned declaration meaning. By contrast, removing or renaming a selected
coordinate invalidates its current mapping. A type change also invalidates the mapping when the selected law
no longer admits the presentation domain. Kontrakt must not remap by position, type resemblance, source
order, or an inferred replacement law.

A new Input Definition requires exact current mapping verification even when the Canonicalization
declaration is unchanged. That verification belongs to the current Binding and inbound Composition
relationship, not automatically to Canonicalization Definition Identity. Sections 9.1, 9.2, and 9.12 define
the boundary.

### 5.5. No Application-Defined Canonicalization Law in V1

V1 does not accept an application-supplied canonicalizer as Contract authority. A callback may depend on
undeclared state, perform unbounded work, or erase distinctions that later judgments need. Selecting
admitted Exact Laws, including through explicit composition, does not grant the application the right to
redefine those laws.

A future custom-law facility needs a separate Contract and security decision. It must define precisely when
two operands mean the same thing and which representative follows. It must also determine refusal and bound
the required work. Any external semantic Basis that can change the result requires Contract-owned versioning.
The review must address aggregate collisions without treating executable code as the specification. A custom
equivalence can change lookup or deduplication, and its security consequences for later judgments must be
explained to users before the Law is exposed.
It cannot be introduced by relaxing a V1 type check or allowing a callback as a composition member.

### 5.6. Built-In Law Specification

Each Exact Built-In Law must satisfy ADR-0076 and its owning normative specification. That specification
determines which presentations it accepts and which differences it erases. It also determines when a
representative can be established and when the Law refuses. If absence or aggregate collisions are possible
in that presentation, their meaning must be resolved rather than left to a host container. The Law also owns
any meaning-determining external basis and any normative canonical-byte protocol.

Conformance vectors are versioned with the owning Law. An implementation verification suite may evolve
independently of that normative meaning under Section 10. User documentation explains the Law but cannot
serve as an alternative Authority.

V1 does not provide a separate user-authored canonical output shape. Representative changes remain within
the admitted Input coordinate domain; a distinct target presentation is Lowering's responsibility.

---

## 6. Canonicalization Law and Composition Eligibility

### 6.1. Exact Built-In Law Eligibility

A Law may be selected only after ADR-0076 admits its complete exact meaning. Selection requires a positive
declaration that the Law can govern the selected Input presentation. Unsupported pairings do not receive a
runtime fallback. Applicability to a presentation kind is distinct from successful coverage of a particular
legal input value; the latter may result in an Occurrence Refusal.

The Law must fix every semantic determinant or declare a Required Basis under ADR-0063 when a determinant
must be supplied for a later Judgment. It cannot obtain missing meaning from the host environment or
application behavior, including through reflection or mutable providers. For a legal absence or aggregate
presentation, the Exact Law must determine the result rather than borrowing host behavior.

A Law may refuse an Input-established presentation outside its own successful coverage. It cannot repair
Input illegality, produce business meaning, or change the Input shape. Section 4.4 governs equivalence;
Section 7 governs finite work.

### 6.2. Composition Eligibility

The separately owning Composition law must establish the full ordered relation before the Canonicalization
Definition Judgment can depend on it. Every constituent must have an exact admissible Law reference. The
final composition must satisfy Section 4.4 and be legal at every reached intermediate presentation. Every
reached intermediate representative must satisfy its constituent Law's same-shape requirement; a legal final
shape cannot excuse an intermediate domain-changing step.

Legality must establish the final representative relation and its repeated-application stability. It must
also establish how each constituent's refusal and Required Basis affect the complete composition. The whole
ordered composition must have finite bounds, even when each constituent is individually bounded. Semantic
dependency cycles are illegal.

If a later constituent needs a distinction erased by its predecessor, legality must be resolved by the
complete Composition law; type compatibility or matching sample outputs are not proof. A proven invalid
composition is rejected by its own Authority. An inability to establish legality is not proof that the
composition law is false, and it cannot be converted into a Canonicalization Occurrence Refusal.

The compiler cannot amend the declared sequence to make it legal. A legal but redundant constituent remains
part of the Definition's declared meaning; the diagnostic treatment of such redundancy is separately owned.

---

## 7. Finite-Work Law

An admitted Canonicalization Law must bound the work it can require on untrusted Input. The bound must
account for traversal and representative expansion in the presentation domains it admits. Structured domains
require a finite legal traversal boundary; general application-defined recursion or imperative iteration is
not V1 Canonicalization.

Composition is bounded as a whole. One constituent can expand data supplied to the next, so individual
bounds do not suffice. The Composition law must close the effect of the declared sequence, including its
intermediate growth and final output.

A **semantic bound** belongs to Contract meaning when crossing it changes whether the law applies or
succeeds, or changes the result it must establish. A compiler resource limit does not change any of these
meanings. Budget and Capacity retain their independent authorities when their own conditions stop work.

Any realization must preserve the complete Contract outcome. An implementation digest cannot establish
equivalence without the Exact Law's required semantic check.

---

## 8. Canonicalizable-Domain Condition

The Canonicalization domain begins with Input-established `Presented` material. Legal Input need not already
be canonical. An entered Canonicalization Occurrence succeeds when every selected coordinate establishes the
final representative required by its exact direct or composed Law. It refuses when a legally reached
selected assessment establishes a Law-owned negative condition. Sections 9.8–9.10 specify legal entry,
complete assessment, and attribution.

An unsuccessful prerequisite does not create a Canonicalization Occurrence Refusal. This includes an Input
that was not Presented, an unresolved HIR reference, and a Basis that has not been established.
Composition's own rejection likewise retains its owner. A compiler or resource failure also retains its own
owner. A fully resolved but invalid Canonicalization declaration is instead subject to the Definition
Judgment in Section 9.6.

A generated implementation that returns a result contrary to the selected Law has a Kontrakt conformance
defect. An application-supplied post-check cannot revise a successfully established representative or become
a second authority.

---

## 9. Definition-Time Processing, HIR, and Establishment Boundary

The compiler resolves the IDL-selected declaration into complete pre-authority HIR meaning before Contract
Establishment. The HIR lifecycle follows ADR-0071. The separately owned Composition prerequisite must be
legal before a dependent Canonicalization Definition Judgment enters. ADR-0063 then governs authoritative
Definition and Occurrence Establishment.

A complete but contractually invalid declaration may reach the Definition Judgment and be refused. An
unresolved symbol or incomplete HIR cannot. The following sections define the Canonicalization-specific
information at each boundary; they do not prescribe physical passes or data structures.

### 9.1. Complete Resolved Canonicalization Definition Candidate Meaning

One fully resolved Canonicalization declaration yields one Definition Candidate before authority is
established. It must preserve every declared direct-coordinate selection occurrence and the exact Law
meaning associated with it. The Candidate can faithfully represent an empty selection or a duplicate
selection so that Canonicalization's own Definition Judgment may refuse it. It is not an Established
Definition.

For each declared selection, the Candidate exposes the nominal direct-coordinate requirement and its exact
supported Input presentation requirement. It also exposes either one admitted Exact Law reference or one
explicit ordered relation of admitted Law references whose legality belongs to Composition. Independently
owned normative Law meaning is observed through its exact reference rather than copied as new
Canonicalization authority.

The declaration's selected-surface observation is complete even when its membership is empty or contains
repeated coordinates. Independent selections have no semantic ordering. A legal Established Definition, by
contrast, has a nonempty selection set containing exactly one binding for each coordinate. A composition's
internal order remains part of its declared meaning under Section 4.4.1.

A Candidate preserves any Definition-determining semantic requirement and any Law-owned Required Basis
Requirement that its exact meaning needs. It does not turn an actual Input value or occurrence-time Basis
Binding into Definition meaning. Fixed external semantic material belongs to the Exact Law's versioned
meaning when that Law so declares; ambient provider state never substitutes for it.

The **current Input Definition** is not part of the Canonicalization-owned declaration meaning merely
because it supplies the presently selected operand. The selected nominal requirement must still be matched
to an exact legal Input coordinate in the current context under Section 9.2. Changes confined to unselected
Input coordinates do not change the Canonicalization-owned declaration meaning, but they may require fresh
compatibility or downstream judgments. ADR-0053 and ADR-0063 determine whether an authoritative identity can
be reused.

The Candidate must be complete without consulting host artifacts or an implementation at Judgment time. Its
complete HIR Projection includes its exact Candidate Reference and all of its owned meaning. Narrower
observations may be provided only with producer-declared completeness under Section 9.4.

### 9.2. Exact Candidate and Input Coordinate Reference Boundary

The following reference domains must remain distinct:

| Semantic reference                               | What it designates                                                                                   |
|--------------------------------------------------|------------------------------------------------------------------------------------------------------|
| Canonicalization Definition Candidate Reference  | One completely resolved pre-authority Canonicalization declaration                                   |
| Canonicalization IDL Binding Candidate Reference | One exact IDL selection occurrence and its target Definition Candidate                               |
| Declared Input-coordinate selection              | One nominal requirement in the declaration; its multiplicity is preserved before Definition Judgment |
| Resolved Input Coordinate Reference              | One direct coordinate in one exact current Input Definition Candidate, as defined by ADR-0064        |
| Exact Built-In Law Reference                     | One exact independently owned Law Authority and Versioned Meaning                                    |

Candidate identity follows ADR-0071's semantic reference law. Source ordinals and host symbols cannot create
it. ADR-0063 separately establishes authoritative Definition References; a Candidate Reference is never
converted into one by a cast or matching payload.

The current mapping establishes which exact direct Input coordinate satisfies each declared nominal
requirement. Equal names or types in distinct Input Definitions do not make the references interchangeable.
The mapping does not make the entire current Input Definition a determinant of Canonicalization Definition
meaning.

If a selected coordinate no longer resolves exactly or no longer admits the selected Law, its current
resolution or applicability fails. The diagnostic must identify the affected Canonicalization declaration
and coordinate. The compiler may not silently choose another coordinate or reuse a mapping from an older
Input Definition. An unresolved current mapping is not an entered Canonicalization Definition Refusal, and
neither is a valid Definition's later Occurrence Refusal.

A duplicate selected coordinate whose references are otherwise fully resolved is different. The Candidate
retains both declared selection occurrences for Canonicalization Definition Judgment under Section 9.6.
Neither a source-order tie-break nor implicit Composition can repair it.

A direct binding has one Exact Law reference. An explicit composition retains its complete ordered reference
relation without creating another user-selected 1D Definition or a new Catalog Law. Meaning equality,
reference equality, and physical storage equality remain distinct under ADR-0063 and ADR-0071. Shared
physical backing cannot weaken any of these reference boundaries.

### 9.3. Canonicalization IDL Binding Candidate

Under ADR-0071 Section 4.13, an Interaction selects Canonicalization at its exact IDL role coordinate within
the applicable Interface and Policy World. The Binding Candidate records that selecting context and refers
to exactly one Canonicalization Definition Candidate. Distinct legal contexts may select the same target
without copying or merging its Definition.

Two competing targets for one exact selection cannot be settled by declaration order. Omission creates no
Canonicalization authority or application Material. A declaration's coordinate-to-Law bindings are its own
Definition meaning, not independent IDL role selections.

The Binding Candidate does not decide current Input compatibility or actual Occurrence Basis. Those
relations belong to their existing owners under Sections 9.2 and 9.8. A Definition Judgment does not consume
the IDL selection merely because that selection led to the Definition's use.

### 9.4. Resolved Canonicalization HIR Candidate Protocol

ADR-0071 Section 8.9 supplies the HIR Protocol. Canonicalization must provide complete Definition Candidate
and IDL Binding Candidate Projections, with narrower producer-owned observations where a consumer needs
them.

The **Declared Selection Surface Projection** exposes the full declared membership before Definition
Judgment. It includes an empty declaration or repeated nominal selections where present. It must not
substitute the entire current Input coordinate surface.

The **Selected Law Relation Projection** gives the exact Law relation for a declared selection. It exposes
one Exact Law reference or the complete explicit ordered composition reference, without duplicating the
authority of its constituents.

The **Definition Requirement Projection** exposes the requirements that the owning Definition has declared.
Exact Law-owned semantics remain obtainable through the Law's own reference and legal projection. Actual
occurrence values and Basis Bindings do not become HIR Definition determinants.

The **IDL Binding Target Projection** exposes the exact selection context, role, and target Definition
Candidate Reference. It does not copy the target's meaning or assert that its current Input mapping is
valid.

A projection's equality covers precisely its declared complete observation. A narrower or unavailable
observation cannot establish that an omitted selection is absent. In particular, comparing unchanged Law
references cannot hide a newly declared duplicate selection. ADR-0071 governs compatible Protocol evolution
and coherence. The logical Protocol does not prescribe its
physical realization.

### 9.5. HIR Seal and Establishment Handoff

The HIR Seal defined by ADR-0071 checks whether the resolved pre-authority meaning is complete and coherent.
Exact required references must be resolved. Visible HIR cannot contain incomplete or dangling required
meaning. Recovery placeholders are therefore prohibited. The same applies to a required Protocol extension
that cannot be interpreted legally, or a view assembled from incompatible Generations.

Seal does not establish Canonicalization's own Definition law. A completely resolved empty declaration or
duplicate selection can therefore be Visible HIR and later receive Canonicalization Definition Refusal. The
compiler must not delete its offending meaning merely to make Seal succeed.

If frontend resolution or exact current Input-coordinate matching fails, that stage owns its unsuccessful
result. An HIR Seal rejection exists only if its Judgment legally entered. Neither unsuccessful result
becomes a Canonicalization Definition Refusal for a Judgment that never entered. User-facing Diagnostics may
explain the Canonicalization declaration without taking ownership of the underlying result.

Establishment observes Candidate meaning through its legal HIR Protocol. Separately Established
prerequisites retain their own Authority. Physical fusion or early checks do not merge the legal entry
conditions, and a retained earlier HIR Generation cannot certify a different current Input Binding.

### 9.6. Canonicalization Definition Establishment Judgment and Result

An owning Canonicalization Definition Judgment may enter only after the complete Candidate is legally
Visible and every authoritative prerequisite that the Definition actually requires has been satisfied. It
does not require an actual Input Occurrence or import the current IDL selection as a Definition determinant.
An Exact Law's genuinely Definition-time Required Basis remains a prerequisite under ADR-0063.

An entered Judgment establishes exactly one of these outcomes:

- **Established Canonicalization Definition:** The complete declaration satisfies Canonicalization's
  Definition law. Its selection set is nonempty, with exactly one lawful binding for each coordinate.
- **Canonicalization Definition Refusal:** The fully resolved declaration violates that law. An empty
  selection or duplicate independent coordinate binding is such a violation.

For example, two independent declarations `name -> A` and `name -> B` are invalid even when both laws are
individually admitted. One declaration `name -> compose(A, B)` is a single binding; the separate Composition
law must establish its legality first.

The successful Judgment establishes the Canonicalization-owned Definition meaning and its exact
authoritative reference under Sections 9.7 and 10. It does not ratify constituent Built-In Laws again, redo
Composition Legality, or apply the Definition to Input values.

A failed prerequisite prevents legal entry and cannot generate an additional Canonicalization Definition
Refusal. This includes failed HIR formation, failed Seal, and independently unsuccessful Composition. Actual
current Input mapping errors retain their correct ownership under Section 9.2.

No partial Definition becomes Established. Discovery order cannot choose one of two competing bindings or
change Result ownership. ADR-0057 owns downstream Failure attribution; common compiler failure and recovery
boundaries belong to ADR-0074.

### 9.7. Established Canonicalization Definition Material and Exact Reference

Under ADR-0063 Section 4.3.1, a successful Definition Judgment establishes one complete Canonicalization
Definition and its exact `EstablishedCanonicalizationDefinitionRef`. The Material records the exact
declaring Authority and lawfully bound Version. It preserves the full selected-coordinate relation and the
direct semantic obligations that the Canonicalization Definition owns. A selected Exact Law or separately
legal Composition is referenced without duplicating its independent Authority.

For the independently declared Canonicalization Definition model, one exact declaring Authority and its
lawfully bound Version identify one complete Definition. No further Authority-local Definition coordinate is
needed. ADR-0053 determines the applicable Version claim and binding; this decision does not transfer the
Version authority of Kontrakt-provided Exact Built-In Laws to the user declaration. Those Law Authorities
remain independently versioned under ADR-0076.

Two declarations that use equal selected Laws do not become the same authoritative Definition when their
Authorities differ. An IDL selection does not change the established Definition's Identity. The current
Input Definition does not either. HIR Candidate References and host or physical identifiers remain outside
the authoritative identity formula. A Candidate Reference cannot be cast into an Established Definition
Reference.

The Definition may establish a future Occurrence's Required Basis Requirement without establishing an actual
Basis Binding. No Input Occurrence is created by Definition Establishment. Only complete authoritative
Definition meaning becomes visible through the Canonical Contract World, as specified by ADR-0063 Section

10.

### 9.8. Canonicalization Occurrence Unit, Required Basis, and Applicability

One Canonicalization Occurrence is one legally entered application of an exact Established Canonicalization
Definition to one exact Input Occurrence whose Input outcome is `Presented`. It covers the whole
selected-coordinate declaration rather than creating separate Canonicalization Occurrences for coordinates
or composition constituents.

The applicable Interaction selection and inbound Composition identify the semantic application. For one such
lawful application relationship and one exact Presented Input Occurrence, there is one Canonicalization
Occurrence. A fresh legal Interaction entry that establishes a distinct Presented Input Occurrence may
establish another Canonicalization Occurrence even if all values are equal. Multiple distinct semantic
applications remain distinct only where their owning application relationship actually permits them; running
an evaluator again never creates a new Occurrence.

An entered application has its own exact `EstablishedCanonicalizationOccurrenceRef` under ADR-0063 Section
6.5. This reference designates a semantic application. The compiler cannot derive it from time, a host
invocation, a value digest, or its own evaluation count.

The Occurrence Judgment requires **exactly one** Presented Input Occurrence as determining Basis. The owning
inbound Composition relation specifies which one. Its legal ADR-0063 Basis Binding and Applicability must be
established before the dependent Judgment enters. Finding an apparently matching Input is insufficient if
the legally applicable Basis remains ambiguous or incomplete.

An Exact Law may declare another Required Basis for an actual Judgment. Ambient state cannot become an
implicit Basis. A clock or provider may participate only if the applicable
Exact Law declares a legal Basis relation. A determinant that the
Exact Law fixes at Definition time is not an Occurrence Basis. A missing or Inapplicable required Basis
prevents the Judgment that needs it from entering; it cannot fabricate `Refused` or an invented successful
representative.

For ordered Composition, later constituents are reachable only after their predecessors succeed. The
separately owning Composition law determines the legal entry and additional Basis requirements of each
reached step. An unreached step cannot generate a Refusal or a required-result claim. These rules must not
suppress an independently reachable coordinate assessment or allow an arbitrary missing Context to override
a lawfully determined result. Constituent judgments do not become child Canonicalization Occurrences.

### 9.9. Complete Occurrence Judgment and Coordinate Assessment Law

After legal entry, the whole Canonicalization Occurrence Judgment assesses every selected coordinate that
can legally be reached. It establishes one overall outcome:

- **`Canonicalized`:** Every selected coordinate has established its required final representative.
- **`Refused`:** At least one legally reached selected assessment has established a Canonicalization-owned
  refusal condition.

The full result includes every independently reachable selected-coordinate assessment. Finding one refusal
does not permit Kontrakt to omit another reachable assessment. Successful assessments remain established
with their exact final representatives even when the whole Occurrence is `Refused`. Each refusal identifies
the selected coordinate and its exact refusing Law meaning. Discovery order and scheduling do not determine
result membership.

```text
one Canonicalization Occurrence
    whole outcome: Refused
    name: successful final representative
    region: refused by exact Law B
    code: refused by exact Law C
```

An ordered composition records the authoritative result of its reached constituents. If a reached
constituent refuses, later dependent steps do not enter. Their non-entry is distinguishable from refusal. A
successful intermediate constituent does not automatically establish a final coordinate representative;
constituent ownership remains with its own Law and the separately owning Composition law.

A selected Input coordinate may legally be absent. Its Exact Law decides whether that absence is a supported
operand and what representative or refusal follows. Absence is not an implicit no-op. If the Law cannot
govern the coordinate's declared presentation domain, that incompatibility must be resolved before
occurrence entry. ADR-0064 continues to distinguish legal absence from a null-like value, an empty
aggregate, and a missing required Input field.

Only `Canonicalized` supplies a complete selected-coordinate representative surface for inbound composition.
A `Refused` whole Occurrence can establish successful coordinate relations for lawful diagnostic or
verification use, but no consumer may splice those relations into a partially successful Admission operand.

### 9.10. Established Canonicalization Occurrence Material and Failure Attribution

ADR-0063 Sections 4.4.1 and 7.6 govern the Material established for one entered application. The Material
identifies the exact Occurrence and the Definition it applies. It identifies which Presented Input
Occurrence determined this application. Any additional Applicable Basis is included only when the entered
Judgment genuinely requires it. The exact Applicable Context is also preserved when the owning applicability
law uses it to determine the application, as required by ADR-0063 Section 7.6. The Material includes the
single whole outcome and every lawfully reached coordinate assessment. Each successful final representative
and every Law-owned refusal attribution is retained as a direct relation when established.

The refusal attribution names the exact coordinate and the exact Law-owned negative condition. Where
Composition is involved, it identifies the constituent that was legally reached and refused. Unreached steps
do not acquire fabricated negative results. Incidental temporary values or evaluation traces are not
authoritative Material.

ADR-0057 owns Failure cardinality. One whole `Refused` Canonicalization Judgment with several coordinate
refusal relations does not automatically create several Failures. If another independently authorized
Judgment establishes a separate negative meaning, it retains its own Failure. Neither Failure count nor
authority follows the number of diagnostic messages or physical result entries.

Only the legally entered Canonicalization Judgment can establish its own Occurrence Refusal. Input refusal
and other unsuccessful prerequisites keep their own owners; implementation unavailability and resource
interruption also cannot establish that refusal. The complete result cannot be published if its required
assessment membership has not been established. Once established, the exact Occurrence's direct relations
remain immutable even if a later law Version or compiler Generation changes.

A Law-owned canonical-byte relation enters the Occurrence Material only if the Law actually establishes it
under Section 4.7. Physical encoding and constituent byte protocols do not automatically become
whole-composition canonical bytes.

### 9.11. Established Canonicalization Semantic Protocol and Consumer Law

Canonicalization uses the read-only Established Semantic Protocol defined by ADR-0063 Section 11. Its
authoritative typed reference domains are `EstablishedCanonicalizationDefinitionRef` and
`EstablishedCanonicalizationOccurrenceRef`. Input and Exact Law references keep their own Authorities. No
separate Coordinate Occurrence Reference or new Material family is required.

The complete **Definition Projection** exposes the exact authoritative Definition and its directly
established meaning. The complete **Occurrence Projection** exposes the exact application, its legally
determined Basis and Applicable Context, where established, together with its whole result and all directly
owned relations. ADR-0063 also provides Direct Established Relation observations.

Narrower producer-owned projections may expose the selected-coordinate Law relation or the complete
Occurrence outcome. The **Coordinate Assessment Projection** covers every legally reachable assessment,
including successes in a refused whole Occurrence. The **Refusal Attribution Projection** exposes every
established refusal and its exact owning Law. The **Canonical Bytes Projection** exists only for byte
relations established by an Exact Law.

The complete **Final Representative Projection** is available only when the whole Occurrence is
`Canonicalized`. For `Refused`, narrow assessments may expose successful coordinate representatives to
legally authorized consumers. That observation cannot become a complete Final Representative Projection.
An unavailable projection is not an empty successful result.

Inbound Composition combines a successful complete selected-coordinate surface with unselected
Input-established coordinates. Diagnostics and Verification may observe the exact evidence that their own
legal consumption rules permit. None of these consumers may rejudge the producer-owned result or acquire a
raw Input interpretation that defeats Section 4.5. Completeness of Established Material does not imply
unrestricted external disclosure of its contents.

Projection equality and availability follow the producing Authority's declared complete observation.
Semantic absence differs from unavailable backing or compiler observation failure. Physical sharing does not
make equal projected values identical References. ADR-0063 Sections 11.5–11.11 govern compatibility and
coherent observation; this ADR prescribes no per-projection storage or lookup structure.

### 9.12. Reuse, Current Validity, and Coherent Observation

A retained Definition remains semantically valid only if its exact Authority and Version Binding and its
complete Definition-determining meaning remain valid. The complete selected set must be checked, not just
the previously known Law references; newly added selections and illegal duplicates cannot be ignored.

Current Input compatibility is separate. Every selected nominal coordinate must still resolve to its exact
direct coordinate in the current Input Definition and satisfy the Law's presentation requirement. Neither an
old matching name nor an old successful Binding establishes compatibility with the current Input Definition.
A valid Definition does not automatically validate a subsequent Basis Binding or Judgment.

A new legal semantic application establishes its own Occurrence even if the values or representatives match
an earlier application. Repeating physical work on the same application creates no new Occurrence. Reusing a
computation never supplies a missing authoritative Judgment.

A complete assessment or refusal projection can remain valid only while its full observable membership
remains valid. Matching old refusal entries is insufficient when a formerly successful coordinate could now
refuse. A legal observation cannot mix incompatible HIR or Established Material Generations. ADR-0063 and
ADR-0071 own these coherence laws.

A compiler cannot report a Canonicalization Contract result when its supporting execution or observation
failed. The owner of unavailable backing or damaged cache data remains the compiler or realization boundary.
No cache hit, fingerprint, or saved product establishes Contract success or Refusal without the current
required validity. Incremental repair and physical scheduling are Compiler Design, not Canonicalization
semantics.

---

## 10. Identity and Conformance

The authoritative identity law in ADR-0053 and ADR-0063 applies to Canonicalization. Section 9.7 fixes the
Definition's Cardinality without deriving Identity from its source class, selected Input values, or a
physical representation. An Exact Built-In Law retains its separately owned Authority and Version under
ADR-0076.

Changing any Definition-determining requirement changes the declaration's meaning. In particular, a new
selected coordinate or a change to the Exact Law or composition order is a semantic revision. Such a change
must follow the owning
Authority's Version revision law. Two different Authorities or Versions do not merge merely because their
current representatives are equal. A change confined to an unselected Input coordinate does not by itself
change the Canonicalization-owned Definition; current compatibility must still be judged under Section 9.12.

Every admitted Exact Law requires versioned normative conformance vectors and an independent implementation
verification suite under ADR-0076. Changes to normative vectors must be classified under the owning Law's
ratification and correction law. A change to normative Law meaning requires a lawful new Version. Correcting
a mistaken test is not automatically a semantic change, but a changed normative representative cannot be
disguised as a test-only correction. Adversarial verification and performance coverage may improve without
revising immutable Contract meaning.

## 11. Determinism Law

For the same exact Law `L`, legally admitted operand `x`, and applicable semantic Basis, any two lawful
realizations must establish the same Canonicalization-owned result:

```text
Outcome(R1, L, x) = Outcome(R2, L, x)
```

The result is the exact final representative or the Law-owned refusal. For an ordered composition, the
requirement covers the complete declared sequence and its exact refusal attribution. For a whole Occurrence,
it covers every legally reachable coordinate assessment and the complete overall result under Section 9.9.
Physical first-failure timing cannot change the authoritative relations.

The realization may fuse or specialize work only while preserving the complete meaning, including any
Contract-owned observable State. Source and host defaults cannot choose a different representative. A
resource interruption retains its own result owner and does not redefine Canonicalization. The compiler may change
execution strategy without rewriting the
Definition's ordered composition.

A computation can move across the Canonicalization boundary only if that move preserves the selected law's
declared equivalence classes. In particular, the moved computation must not distinguish two presentations
that the law treats as equivalent. Otherwise the transformation changes the declared machine. Logical
Judgment order remains authoritative even when physical work is reordered.

Determinism of Contract results does not by itself prescribe timing or allocation patterns. Their security
or resource constraints belong to the authorities that declare them.

## 12. Refusal Boundary

Section 9.6 defines Definition Refusal and Sections 8–9.10 define Occurrence Refusal. Only an entered
Judgment may establish either negative result. The complete set of legally reached successful assessments
and refusal attributions belongs to the exact Occurrence; one discovered refusal does not hide the others.

An unsuccessful prerequisite cannot be reclassified as Canonicalization refusal. Stops owned by Budget or
Capacity remain independent, as do compiler failures. An unreached ordered constituent establishes no
invented refusal, and a refused whole Occurrence supplies no partially successful Admission presentation.

ADR-0057 governs distinct Failure judgments and their attribution. A diagnostic explanation cannot replace
the exact Contract-owned negative relations. Omission of Canonicalization establishes no Canonicalization
refusal surface.

## 13. Relationship to Admission and Lowering

Section 4.5 fixes the Canonicalization-side consumer requirement. Inbound Composition may deliver the full
representative-bearing presentation to Admission only after the Canonicalization Occurrence succeeds.
Admission then judges that boundary meaning. Lowering must consume the same meaning rather than
reconstructing an earlier Input representation.

Canonicalization establishes neither Fact meaning nor Operation parameter targets. Lowering owns the
explicit source-to-target formation. ADR-0048 owns whole-airlock composition, while the Admission and
Lowering ADRs will be aligned through their own subsequent revisions. Their unreviewed earlier sequencing
does not redefine Canonicalization here.

## 14. Open in This Section

The V1 public API belongs to Canonicalization Design. This ADR does not prescribe Java or Kotlin symbols,
source helpers, or carrier layouts. ADR-0076 and the Exact Law specifications own Built-In Catalog
membership and the individual Law meanings. A future application-defined law requires a new Contract and
security decision under Section 5.5.

The separate Composition authority owns its detailed legality judgment and each constituent's step-level
Basis and reachability. Legal but redundant compositions retain their declared meaning; Diagnostic Design
determines whether to warn and what severity to assign. These are not unspecified Canonicalization results.

ADR-0063 owns the common Established Material and Protocol laws. ADR-0053 owns Version Binding. ADR-0071
owns HIR lifecycle. Section 9 specializes these laws sufficiently for Canonicalization without creating a
new identity or storage system. Compiler Design owns physical realization and incremental execution;
Security SADR owns its distinct threat-model and defense verification. None of these artifacts may override
the Contract requirements established here.

## 15. Consequences

An Interaction may remove declared representation freedom from legal Input before later inbound judgment.
The choice is explicit and selective. V1 sacrifices arbitrary user canonicalizers in exchange for laws whose
outcomes can be verified without executing application-defined authority.

A lawful implementation can change how it computes the result without changing what the Contract
establishes. A refused whole Occurrence preserves every legally reached assessment and refusal relation, but
no later inbound judgment can use a partial canonicalized presentation. Canonical bytes remain normative
only for laws that explicitly own them. Outbound Publication and Output retain the asymmetric boundary
described in Section 4.8.

## 16. Migration History

This ADR was extracted from ADR-0048. Extraction did not itself change the accepted Canonicalization
semantics.

The subsequent inbound-boundary review located Canonicalization after Input and before Admission. It made
legal Input a prerequisite, prohibited revival of erased distinctions, and kept Output asymmetrical. The V1
authoring review then required explicit IDL selection of a Canonicalization declaration rather than direct
Law selection. Sparse coordinate bindings replaced complete-coordinate coverage; an omitted coordinate no
longer receives an identity law.

The Catalog-boundary review assigned Catalog membership and qualification to ADR-0076 while preserving each
Exact Law Authority's independent normative meaning. A later composition review admitted finite explicit
ordered compositions of those laws, without granting user code executable authority or creating a recursive
hierarchy. Public host-language API details moved to Design.

The HIR and Definition Judgment review specialized ADR-0071's Candidate and Seal boundaries and established
an explicit `Canonicalization Definition Refusal` for a fully resolved illegal declaration. Frontend or HIR
failures do not become that Refusal. The Occurrence review applied ADR-0063 to one whole
Definition-to-Presented-Input application. It required
complete coordinate assessments and specialized Established Material and its producer-owned Protocol.
ADR-0057
retains Failure ownership.

The final closure review applied the existing Authority/Version identity law to a single complete Definition
per declaring Authority and lawfully bound Version. It tied each Occurrence to the exact lawful Interaction
selection and Presented Input application without creating runtime-derived identity. It also aligned the
selecting role with Interaction rather than Operation. These refinements do not transfer exact Built-In Law
Version ownership to the declaration or prescribe physical realization.