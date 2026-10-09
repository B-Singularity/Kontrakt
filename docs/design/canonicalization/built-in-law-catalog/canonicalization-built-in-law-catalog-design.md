# Canonicalization Built-In Law Catalog Design and Candidate Qualification

## Status

Draft

## Date

2026-10-09

## Governing ADRs

- ADR-0076: Canonicalization Built-In Law Catalog
- ADR-0066: Canonicalization Contract
- ADR-0053: Version Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0064: Input Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0075: Compiler Result Ownership, Legal Consumption, Deterministic Realization, and Contract / Implementation
  Separation

---

# 1. Purpose

This document is the working Design surface for concrete Canonicalization Built-In Law candidates. It records the
material needed to decide which exact laws Kontrakt will actually provide.

ADR-0076 owns the Catalog architecture and the law for admission. It also fixes the Authority and Version boundaries
that this Design must preserve. This document does not replace those decisions. A candidate does not gain Contract
authority merely because it is described or implemented here.

The responsibilities are intentionally separated:

```text
ADR-0076
    Catalog architecture
    Built-In Law Authority membership law
    qualification and admission law
    identity and Version boundaries

this Design area
    candidate inventory
    candidate research
    exact-law qualification records
    deferred-profile analysis
    proposed concrete Catalog population
    API and implementation design inputs

Exact Built-In Law Authority
    owns exact versioned Contract meaning

compiler realization
    realizes admitted meaning
```

A candidate becomes a Catalog member only through an explicit admission decision under ADR-0076.
Neither a public API symbol nor implementation registration can admit it. Generated code and
verification evidence cannot grant membership either.

---

# 2. Candidate Qualification Record

Each candidate is reviewed as one proposed independently owned Exact Built-In Law Authority. Qualification is a
hard-gate
review rather than a weighted score. A candidate is not admitted because most checks look favorable. Every semantic
obligation that can change Contract-visible meaning must be closed before `ADMIT` is possible.

Security review is part of qualification, but it does not create Canonicalization meaning. It may
reveal that the proposed subject is too broad or that a distinction must remain visible. It may also
require V1 admission to be deferred. Security concerns cannot define an equivalence relation or
representative that the owning law does not support. Nor can they introduce a repair rule or new
protocol meaning.

The working record must be rich enough to answer ADR-0066 and ADR-0076 without creating a generic property bag. It also
must keep law meaning separate from evidence about one implementation.

## 2.1. Normative Source and Scope

The review first identifies who owns the proposed meaning. An external standard may own the domain rule, or Kontrakt may
propose a law that it can legitimately own as reusable built-in meaning. A library API is evidence of developer demand
or implementation practice. It is not normative semantic authority by itself.

When an external specification is relevant, the reviewer records the exact normative source needed to
understand the law. The reviewer then checks the source's present normative status. The reviewer must
determine whether a later specification supersedes it or whether errata amend it. An evolving standard
requires an explicit account of which meaning is being evaluated. Examples and implementation guidance
must not be mistaken for normative requirements. Common library behavior is evidence, not a source of
Contract authority.

A specification name is not enough when its rules leave a choice open. If that choice affects `E_L` or
`C_L`, the candidate must resolve it explicitly. The same applies when the choice changes
representative coverage or a Canonicalization-owned failure. A provider's preferred behavior cannot
complete the law.

The review must not broaden an operand merely because several implementations accept different legacy forms. Parser
behavior is evidence to examine. It does not define the union of presentations that Kontrakt must accept.

## 2.2. Exact Semantic Closure

Every candidate review must close or explicitly mark unresolved the exact law-specific content required by ADR-0076:

```text
Exact Operand Requirement
Exact Equivalence Definition
Exact Representative Definition
Exact Representative Coverage

Exact Law-Specific Failure Semantics
    when Coverage is Restricted

Exact Law-Specific Semantic Determinants
    only when additional semantic material changes
    an observation owned by this law
```

The Exact Operand Requirement is semantic rather than a JVM carrier type. It identifies the already-established Input
presentation on which the law is defined. The review also records the positive applicability relation needed by
ADR-0066: the law may govern only an Input presentation meaning that it explicitly admits. Selection applicability is
not the same question as occurrence coverage. Applicability decides whether the law may govern that presentation kind;
coverage decides whether one otherwise legal occurrence can establish the required representative.

The Exact Equivalence Definition must identify which distinctions survive and which are collapsed.
`E_L` must be an equivalence relation over the domain for which the candidate claims same meaning. A
security warning does not make two presentations equivalent. Neither host equality nor a shared hash
establishes the relation. Matching parser results or implementation shortcuts are insufficient for the
same reason.

The Exact Representative Definition is independently normative. For every successful operand, the candidate must satisfy
the common ADR-0066 law:

```text
C_L(x) = C_L(y)
    iff
E_L(x, y)

E_L(x, C_L(x))

C_L(C_L(x)) = C_L(x)
```

The law must select one representative for each declared equivalence class. That representative must
stay in the same meaning class. Repeated application must leave it unchanged. An implementation may not
choose among otherwise convenient outputs.

Representative-domain closure is mandatory. Every successful representative must remain a legal value of the exact
semantic presentation promised by the law. Canonicalization may replace the value of a selected coordinate with a
same-shape representative, but it may not change the declared coordinate structure or manufacture a different semantic
type. Shape-changing formation belongs to Lowering or another explicit owner.

The candidate must state whether coverage is total or restricted. Restricted coverage requires an exact
condition for reaching the Canonicalization-owned refusal boundary. The reviewer must also examine
how the law's equivalence relation applies to its successful canonicalizable domain. If two legal
Input presentations are declared equivalent but only one can establish a representative, the review
must explain how coverage and the owned failure remain coherent. ADR-0066 does not impose a new
universal rule that every equivalent legal Input must succeed. The relation must instead be closed
for the exact domain claimed by the law.

Input illegality and an invalid Definition have different owners. Compiler inability is also not a
Canonicalization refusal; neither is exhaustion of Budget or Capacity. If the law has several
Contract-visible negative outcomes, it must define those distinctions itself. Diagnostics cannot
reconstruct them later.

The review must identify every distinction the law can observe in its operand. Explicit absence and
finite alternatives require precise treatment. Ordering and multiplicity matter when the presentation
preserves them. The same applies to keys in a structured value. If an aggregate law merges distinct
keys or elements, it must define what happens to the collision. Host iteration order and replacement
behavior cannot decide the outcome.

A candidate must collapse declared representation freedom rather than merely validate it. A preserve-everything identity
law is not a Catalog member because omission already represents the absence of Canonicalization. A standardized value
with only one legal presentation may still need Input or Admission rules, but that fact alone does not justify a new
Canonicalization law.

The review must confirm that the proposed transformation belongs to inbound Canonicalization. Making
illegal Input legal is repair, not representative selection. Sanitizing suspicious material does not
become Canonicalization merely because it changes text. Acquiring business meaning or resolving an
external resource belongs to another owner. Generating wire bytes or signing material also ordinarily
belongs elsewhere. Exact deterministic bytes qualify here only when the proposed law owns them as its
same-domain representative.

The complete law meaning must be knowable without executing application behavior. An application
callback cannot supply a missing semantic rule. Reflection or runtime registration must not introduce
that rule indirectly. A dynamically replaceable law and mutable runtime state are equally unsuitable as
sources of authority. Executable code may realize an admitted law, but it cannot define the law's
meaning.

Ordering by itself is not Canonicalization. Sorting or storage re-layout qualifies only when the exact semantic
presentation declares the affected ordering distinction irrelevant and the law selects a representative on that basis.
If the semantic domain was already unordered, deterministic physical ordering is compiler representation preparation.

## 2.3. Semantic Determinants, External Material, and Evolution

Every semantic input that can change a law-owned observation must be explicit. Material fixed by the
exact law is Definition-determining meaning. Material supplied through a later legal relation is an
explicit Required Basis under the common Establishment architecture. Ambient provider state cannot
stand in for either.

The review must identify every ambient dependency that could change Contract-visible meaning. Locale
and timezone defaults are common examples. A registry may change independently of the law, as may the
data tables shipped with a library. Operating-system state also varies between environments: filesystem
contents and network namespaces are two concrete cases. Mutable configuration, provider defaults, and
container iteration order must be checked for the same reason. Any such material that actually
determines meaning must be bound explicitly. Otherwise the candidate remains unqualified.

For one exact law Version, the same operand and legally bound semantic material must give the same
result. Thread scheduling cannot change that result. Neither may worker placement, evaluation order,
cache state, or parallel execution. A Canonicalization-owned refusal is subject to the same determinism
requirement. The techniques used to achieve determinism do not themselves become semantic determinants.

Semantic dependencies must be acyclic. A candidate cannot discover its own meaning through recursion
that forms a semantic cycle. Fixed-point search or repeated approximation cannot substitute for a
closed law.

External material and its label are not automatically the same thing. A standard revision matters
semantically only if a distinction denoted by that revision can change an observation owned by the law.
The same test applies to registry releases and provider versions. Metadata about realization or
verification remains evidence unless an owning Contract explicitly gives it meaning.

The review must state the Version consequence of every Contract-visible change. A change to `E_L` or
`C_L` cannot silently remain under the same Exact Built-In Law Version. Changes to coverage, owned
failure, or semantic determinants have the same consequence. A dynamic `current` or `latest` lookup
cannot choose the law's meaning. Neither may a `preferred` or `nearest` fallback silently upgrade the
result. A new external release cannot mutate an already-versioned law through ambient state.

Where an external specification intentionally guarantees stability across revisions, that guarantee may support the
candidate's evolution closure. It does not remove the need to state what Kontrakt's law Version promises.

## 2.4. Finite Work and V1 Realizability

Canonicalization is on an untrusted inbound path. Every V1 candidate must have a finite and analyzable work shape for
the presentation domain it admits. The review must be able to explain how much material may need inspection and how much
representative material may be produced. Structured operands also require a finite traversal boundary.

The review must bound the work required to inspect an operand and form its representative. Output and
intermediate expansion need independent analysis. The reviewer must account for temporary work and for
the traversal depth of structured data. Aggregate cardinality and data-dependent work can also amplify
resource use. A bounded operand type is not enough if one legal value still triggers disproportionate
CPU or memory consumption.

A structured or aggregate candidate must define one closed traversal and representative relation for the whole operand.
V1 qualification does not admit an open recursive normalization language in which user-provided child laws determine
meaning at runtime.

Semantic bounds and implementation safety limits must remain separate. A bound belongs to the law when
changing it alters legal meaning or representative selection. It also belongs to the law if it changes
coverage or an owned refusal. A compiler safety limit may stop one realization. It must not silently
narrow `E_L`, select a different `C_L`, or present the stop as a Canonicalization refusal.

A semantically valid candidate may remain `DEFER` when V1 cannot yet demonstrate a safe bounded realization without
weakening the law. Qualification must expose that feasibility risk rather than hide it behind a provider limit or an
implementation-specific cutoff.

Admitting one law does not prove that every composition containing it is legal. ADR-0066 owns
composition legality and stability for the complete ordered sequence. It also owns compatibility of
required bases and rejection of semantic cycles. Resource bounds must be checked for the whole
composition, not inferred from individual admission.

## 2.5. Built-In Suitability and Authority Uniqueness

A valid representative relation is not automatically a Kontrakt Built-In Law. The candidate must
provide reusable semantic meaning that Kontrakt can legitimately make part of its vocabulary. A convention owned
by one application remains with that application. Tenant-specific and organization-specific conventions
do not acquire Built-In authority merely by being deterministic.

Repeated use in protocols is evidence that a Built-In law could be valuable. Developer demand and
common JVM APIs provide additional evidence. None of them substitutes for semantic closure, and the
number of possible public API symbols is not an admission criterion.

A new Catalog member must represent one independently owned semantic subject. Renaming an existing law
does not justify another Authority. A different package or cited standard is not sufficient either.
Shared implementation code and identical current output also do not determine ownership. If an existing
Exact Built-In Law already owns the same subject, reuse or an explicit alias may be appropriate. The
review must also ask whether an allowed ADR-0066 composition expresses the intended declaration without
introducing a convenience law.

The reverse rule is equally important. Current semantic equality does not merge Authorities that can evolve
independently under different owners. Alias relations must be explicit rather than inferred from equal outputs or shared
code.

## 2.6. Security Qualification

Security qualification applies throughout the candidate's meaning and use. It is not one final boolean.
The Kontrakt Security Architecture supplies the architecture-level constraints. The reviewer must also
read the owning standard's security considerations, including relevant errata or superseding
requirements. Known vulnerability classes and failures in deployed systems provide evidence when they
reveal a risk in the candidate.

Security evidence may show that the operand must be narrower or that a distinction has no proper owner.
It may justify `DEFER`. It may also show that the proposed subject belongs outside Canonicalization. It
cannot create an equivalence relation or representative merely because doing so appears safer.

The review covers these security questions when they are relevant to the candidate:

| Security question                   | Qualification requirement                                                                                                                                                                                                                                                                                             |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Interpretation convergence          | Determine whether parsers can disagree about the operand's meaning. Review strict parsing against permissive and legacy behavior. The law must own one interpretation rather than inherit a host parser's choice.                                                                                                     |
| Equivalence safety                  | Check whether `E_L` erases a distinction needed for identity or authorization. Routing and policy judgments may also depend on such distinctions. False equivalence and accidental collisions are admission defects, not mere implementation bugs.                                                                    |
| Judgment-use coherence              | Admission must judge the established representative relation, and later consumers must use it consistently. Raw provenance may remain for diagnostics. It must not restore a distinction that the law erased.                                                                                                         |
| Decode and transform ordering       | Identify the exact decoding layer before judging the result. An implicit extra decode or double decode is prohibited. The law must also prevent validation of one representation followed by use of another, including reinterpretation after judgment.                                                               |
| Context binding and freshness       | Bind the law Version and any Required Basis that changes the result. Protocol mode and the applicable World or occurrence may also be determinants. Registry material must not leak in through ambient state. Results must not be replayed across incompatible contexts.                                              |
| Fail-closed behavior                | Unsupported or ambiguous material cannot succeed by fallback. The same rule applies when material is corrupt, missing, or inconclusive. Failure attribution must remain with the authority that failed.                                                                                                               |
| Resource boundedness                | Hostile legal material must not cause unbounded computation or memory consumption. Traversal and output expansion need bounds, as does diagnostic amplification. Security limits cannot redefine the admitted semantic law.                                                                                           |
| Cryptographic and protocol boundary | Check whether a signature or MAC authenticates the original representation. Hash preimages and protocol transcripts may require exact bytes too. Authentication fields can require exact matching without normalization. Apply Canonicalization only when the owning protocol declares the equivalence and ordering.  |
| Sensitive input and disclosure      | When the operand may contain sensitive material, examine what raw provenance is retained and who may observe it. Diagnostics and logs must not expose that material without an authorized disclosure boundary. A correct representative alone does not establish confidentiality.                                     |
| Other security properties           | Determine whether the candidate's use requires protection beyond identical functional results. Timing behavior may matter for an authentication secret, for example. Such requirements must be traced to their owning security obligation; this review does not impose constant-time execution on every Built-In Law. |

Known vulnerability patterns are review evidence rather than new Contract law. `CWE-180` illustrates
validation performed before canonicalization. `CWE-174` addresses double decoding. HTTP request
smuggling demonstrates what can happen when parsers disagree on a message's boundaries. These cases
require careful ordering and one exact interpretation. They do not authorize Kontrakt to accept every
legacy spelling or repair a distinct value.

Generic laws and protocol-specific uses must be distinguished. A generic law may be valid but unsafe
for a field whose protocol requires exact string or byte comparison. That misuse does not change the
law's `E_L`. A protocol-specific candidate must respect the comparison and normalization rules owned by that protocol.
Authentication and signing requirements may narrow its operand or require rejection.

The security review should preserve concrete regression witnesses for distinctions likely to fail. It
should include both an equivalent pair and a closely related non-equivalent pair when the distinction
matters. Malformed or legacy syntax can expose parser disagreement. Nested encoding tests can expose an
unintended second decode. Boundary-size inputs and collision cases test different failure modes. When
authentication or signing depends on exact representation, the witness must preserve those original
bytes or characters. These cases support verification; they do not become a second semantic definition.

## 2.7. Evidence and Admission Readiness

Admission must be supported by enough independent evidence to show that the exact law is closed and checkable. The
production implementation does not need to exist first, but unresolved meaning cannot be deferred to that
implementation.

Evidence must be chosen to test the candidate's actual claims. The normative specification establishes
the expected meaning; official conformance data may test it. Independently derived reference vectors
and separate implementations can expose interpretation differences. Malformed inputs and adversarial
cases test rejection behavior. Version-crossing cases test evolution, while resource-bound analysis
tests V1 feasibility. A simple law need not use every method. Its record must nevertheless contain
evidence capable of falsifying the important claims.

Conformance evidence and security evidence answer different questions. Passing a standard's
conformance suite does not resolve an applicable parser differential or security risk. The review
must evaluate both when the candidate has such a risk.

Normative examples and conformance vectors that define or verify an admitted law must be tied to the exact law Version.
A provider's current behavior is not a substitute for that versioned evidence.

The review keeps semantic qualification distinct from realization evidence:

```text
semantic qualification
    decides what the law means

pre-admission evidence
    demonstrates that the proposed meaning is closed,
    independently checkable, and safe enough for V1 admission

post-admission realization evidence
    demonstrates that one supported realization preserves
    the already-admitted meaning
```

A realization constraint cannot weaken the law. An inconvenient provider or backend does not justify
choosing a different representative. Compiler artifacts have a separate status. A cache key and
fingerprint are not Contract meaning, and neither is a dense handle. A result reuse rule gains semantic
significance only when an owning Contract makes that distinction observable.

The final disposition follows Section 8. Before a formal `ADMIT`, the complete versioned normative
law specification must close the mandatory semantic gates. Any material security contradiction must
also be resolved, and V1 must have sufficient finite-work confidence. The candidate must justify
Built-In suitability, satisfy Authority uniqueness, and have adequate pre-admission evidence.
A favorable batch recommendation does not establish Catalog membership. `DEFER` preserves a plausible
law when an admission question remains materially unresolved. `REJECT` applies when the proposed
subject falls structurally outside the Canonicalization or Built-In boundary, or when its exact
relation cannot satisfy the governing law. A rejected broad subject may be researched later as a
narrower, semantically different candidate.

---

# 3. Cross-Candidate Review Traps

The following checks apply during candidate research.

A broad standard or library name is not a complete law. Every choice that can change the representative or an owned
failure must be explicit. A tie-break rule is one example. The treatment of unknown members is another.

A representative must remain inside the law's exact semantic presentation domain. If representative formation can leave
that domain, the candidate must close the problem explicitly. It can narrow the operand domain or use Restricted
Coverage with exact failure semantics. Otherwise it must prove domain closure.

Ambient state must not finish the law. For example, a `latest` registry or the host locale cannot select Contract
meaning. Provider defaults and implementation state are subject to the same rule.

Current semantic equality does not merge independently owned Authorities. The reverse is also true: a different public
name does not justify a new Authority. A different source spelling or implementation routine is likewise insufficient.

A domain-specific name does not create a new Authority when it removes exactly the same distinction as an existing law.
A protocol field that only requires the existing ASCII case representative can reuse that law. Host names and HTTP
header names are examples that must first pass this Authority-uniqueness check before a domain-specific law is created.

The opposite mistake is treating every standardized identifier as a Canonicalization candidate. Some identifiers already
have one legal representation and mainly need validity checking. In that case Input or Admission is the relevant owner,
not a new Built-In Canonicalization Authority.

Validation or ordering alone is not Canonicalization. A physical storage change is not Canonicalization either. The
candidate must satisfy the representative-selection law owned by ADR-0066.

Candidate names in this document are descriptive working names only. They are not Authority or Version identities, and
they do not establish public API spelling. Package structure and compiler coordinates remain separate design work.

Security concern does not itself make two presentations equivalent. A suspicious spelling may remain semantically
distinct and be rejected later by another authority. Canonicalization may erase the distinction only when the selected
exact law owns that equivalence.

Parser differential is evidence to resolve, not permission to accept every parser's language. When strict and liberal
implementations disagree, the candidate must identify the one exact operand and interpretation it owns. Hidden second
decoding and post-judgment reinterpretation are not compatibility features.

A protocol that requires exact byte or string comparison does not automatically inherit a generic
Canonicalization law. A signing input may give the original bytes independent meaning. Authentication
redirects and routing keys can likewise carry context-bound distinctions. Those values remain under
their owning protocols unless the protocol declares the relevant equivalence.

Raw source material may remain available as separately owned provenance or diagnostic evidence. Retention does not let
a later consumer restore semantic authority to a distinction that the selected law already erased.

ADR-0066 may use several admitted Exact Built-In Laws inside one finite ordered Canonicalization composition. Candidate
qualification here still judges each Exact Built-In Law independently. Composition does not create a new Catalog member
or merge the constituent Authorities. ADR-0066 remains the owner of composition legality.

---

# 4. Current Candidate Inventory

The initial inventory contained 75 candidates. All 75 have now received a proposed review disposition in
Section 8. No candidate remains unreviewed. The results are not formal Catalog admission decisions.

New unreviewed candidates may be recorded here before qualification. They leave this section when
an `ADMIT (proposed)`, `DEFER`, or `REJECT` result is recorded in Section 8. The domain grouping is a
review aid; it does not establish shared Authority or Version ownership.

## 4.1. Demand and Standard Signals

The inventory does not use one evidence source for every domain. General text candidates are often motivated by repeated
developer practice. Protocol candidates are stronger when the owning standard already defines equivalence or a preferred
representation.

| Candidate area            | Representative evidence                    | Catalog implication                                                                         |
|---------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------|
| Boundary trim and case    | Kotlin, Java, ICU                          | Repeated use justifies review, but the host operation does not define the law               |
| Unicode normalization     | Unicode normalization forms                | The external semantic profile is already precise enough to ground exact review              |
| Encoded text              | RFC 4648                                   | Canonical encoded text must remain separate from bytes-to-text encoding                     |
| Numeric text              | Decimal and scientific notation practice   | Grouping, radix, scale markers, and exponent spelling need exact self-contained grammars    |
| Bit representation        | Two's-complement and fixed-width bit forms | Numeric value, byte sequence, and fixed-width bit-vector meaning must remain distinct       |
| POSIX IPv4 text           | POSIX network-address syntax               | Legacy multi-radix and abbreviated forms need an explicit profile before normalization      |
| HTTP-date                 | RFC 9110                                   | Multiple accepted date spellings have one required generated form                           |
| HTTP and CoAP URIs        | RFC 9110 and RFC 7252                      | Scheme-specific standards already define important normal-form choices                      |
| `tel:` URI                | RFC 3966                                   | Visual separators and parameter ordering have protocol-defined comparison rules             |
| Network prefixes          | RFC 9911                                   | Clearing non-prefix bits gives an exact representative problem                              |
| International identifiers | DOI and ISO-backed identifier profiles     | Identifier equivalence must be separated from validation and registry ownership             |
| Retail identifiers        | GS1 Digital Link and GTIN rules            | Shorter GTIN forms can map to one 14-digit representation under an exact profile            |
| Cloud-native quantity     | Kubernetes Quantity                        | The API datatype already distinguishes legal non-canonical text from canonical output       |
| Software supply chain     | ECMA-427 Package URL                       | Core syntax and type-specific normalization must not be collapsed into one hidden rule      |
| Security textual encoding | RFC 7468                                   | Parser tolerance and generator form are distinct and can expose a representative problem    |
| Schema meaning            | Avro Parsing Canonical Form                | Domain semantics, not raw JSON spelling, determine which schema distinctions are irrelevant |

A standard name is still not enough. The qualification record must identify the exact semantic subject that Kontrakt
would own. It must also identify which parts remain owned by the external protocol or registry.

## 4.2. Review Priority

Candidate discovery and candidate admission are separate activities. Discovery should cover different domains before the
Catalog is narrowed. Admission should start with candidates whose exact meaning can be closed without importing large
amounts of ambient or mutable state.

The current qualification order is:

```text
small self-contained text and encoded-text laws
    ↓
Unicode laws with explicit semantic Basis
    ↓
compact standards-backed protocol representatives
    ↓
numeric, radix, bit, and temporal representatives
    ↓
international and financial identifiers
    ↓
registry-dependent or type-dependent identifiers
    ↓
structured domain values and aggregate semantics
    ↓
resource-sensitive and cross-authority stress cases
```

This order no longer assumes that text utilities are the center of the Catalog. A compact protocol law can be reviewed
earlier than a familiar string transformation when the protocol already closes the semantic relation more precisely.

## 4.3. Domain Coverage Discovery

Candidate research should continue to sweep real application domains rather than stop after the first useful set of text
operations. The purpose of this table is coverage control. It does not admit every listed subject.

| Domain                       | Current coverage direction                                                                          |
|------------------------------|-----------------------------------------------------------------------------------------------------|
| Text and Unicode             | Existing trim, case, normalization, whitespace, and line-ending candidates                          |
| Numeric and numeric text     | Decimal value, grouped decimal, rate text, scientific notation, and binary float lexical candidates |
| Radix and bit representation | Radix integer, fixed-width bit-vector, two's-complement byte, and POSIX mode candidates             |
| Encoded text                 | Base16, Base32, Base64, and Base64url candidates                                                    |
| Web and HTTP                 | Percent encoding, HTTP-date, media type, qvalue, and HTTP(S) URI candidates                         |
| Network and IoT              | IPv6, POSIX IPv4 text, network-prefix, EUI-48 / MAC-48, and CoAP URI candidates                     |
| International identifiers    | BCP 47, UUID, DOI, IBAN, and GTIN candidates                                                        |
| Temporal standards           | HTTP-date and XML Schema date/time candidates; RFC 3339/9557 remain separate research               |
| Software supply chain        | Package URL candidate; other package and artifact identifiers remain under review                   |
| Cloud-native                 | Kubernetes Quantity candidate                                                                       |
| Geographic                   | `geo:` URI candidate; CRS text remains a separate boundary problem                                  |
| Security / PKI text          | RFC 7468 candidate; signing and wire canonicalization remain separate owners                        |
| Schema and structured data   | Avro candidate; RDF canonicalization remains a stress case                                          |
| Scientific / healthcare      | UCUM quantity remains a deferred aggregate research case                                            |
| Financial / business data    | IBAN is a candidate; currency-symbol interpretation remains outside generic Canonicalization        |

A domain is not complete merely because one candidate exists in it. The sweep exists to expose missing semantic shapes
and to prevent the Catalog from becoming a renamed collection of string utility methods.

---

# 5. General Candidate Working Notes

The laws in this section address common text or numeric representation choices. Presence here does not mean
ratification. Each candidate must pass ADR-0076 qualification as one exact Built-In Law Authority. If it does not, the
review must say why. A duplicate meaning or an over-broad subject are two common reasons.

Text candidates consume the `Text` presentation already established by ADR-0064, which is a sequence of Unicode scalar
values rather than arbitrary JVM UTF-16 code units. Host material outside that legal Input presentation never enters
Canonicalization.

The working labels below are not public API names.

## 5.1. Boundary Whitespace Trim Family

The ASCII and Unicode trim families were reviewed in Section 8 with proposed `ADMIT` outcomes for
their boundary, leading, and trailing forms. The research below records the meaning that must still
be closed in each normative law specification. None of these descriptions fixes a public API name.

General-purpose APIs expose all three trimming behaviors, so each has an observable use case. The
review must not merge them simply because one scanner can implement every behavior.

The ASCII family cannot inherit a host predicate by name. Java `trim()` is one example of behavior that must not define
the Contract. Qualification must choose the exact finite ASCII code-point set. In particular, "ASCII whitespace" and
"every code point at or below U+0020" are not synonymous.

The Unicode family is intended to use an exact Unicode whitespace property under an explicit Unicode semantic Basis.
Qualification must decide whether the candidate is defined by the Unicode `White_Space` property or by another exact
Unicode set. The property choice and any Version-sensitive membership are law meaning.

For every family member, interior characters remain unchanged. A start-only law does not remove trailing whitespace; an
end-only law does not remove leading whitespace; a both-boundary law removes both. These distinct representative
functions are separate semantic subjects unless a later Authority-uniqueness review proves that one should be expressed
only as a composition or API alias.

Empty input and all-whitespace input need explicit representative treatment. For the obvious trim relation the expected
representative is the empty Text value, but that is not considered closed until the exact operand and whitespace-set
laws
are fixed.

## 5.2. ASCII Case Conversion and Case Fold

The ASCII lowercase and uppercase candidates have proposed `ADMIT` outcomes in Section 8.
The ASCII case-fold proposal was `REJECT`ed as an independent Built-In Authority because it
duplicates the reviewed lowercase law. These labels are not public API names.

The lowercase candidate maps ASCII `A` through `Z` to `a` through `z` and preserves every other Unicode scalar value.
The uppercase candidate applies the inverse case-direction mapping to ASCII letters and likewise preserves every other
scalar value.

The [WHATWG Infra Standard](https://infra.spec.whatwg.org/#ascii-lowercase) defines ASCII lowercase and uppercase
as separate operations. It also defines ASCII case-insensitive matching by equality after ASCII lowercasing. For these
candidates, the equivalence relation can be stated without invoking a conversion procedure: two `Text` values are
equivalent exactly when corresponding Unicode scalars are identical or differ only as one of the 26 ASCII upper/lower
letter pairs. The scalar sequences must have the same length. All non-ASCII scalars remain distinct unless already
identical.

Both candidates use that same equivalence relation. The lowercase law selects the ASCII lowercase member of each pair,
while the uppercase law selects the uppercase member. Each therefore chooses a different exact representative for a
mixed-case equivalence class. For example, `"AbC"` has representative `"abc"` under the former law and `"ABC"`
under the latter. Neither maps Unicode-only case variants such as `İ` or `ß`. Each mapping preserves the scalar count,
returns a legal `Text`, is idempotent, and has Total Representative Coverage. No additional Unicode data, host locale,
or runtime registry determines the outcome, and no legal `Text` causes a Canonicalization-owned refusal.

The common equivalence does not automatically merge the candidates into one Authority. Their representative
obligations are different and cannot be substituted for one another. This supports retaining two proposed independent
law subjects, subject to ADR-0076's final Authority-uniqueness and normative-specification review; separate API names
alone would not justify that result. The relation is appropriate only where ASCII letter case is declared irrelevant.
For example, a protocol's case-sensitive command or an identifier requiring exact spelling must not inherit it merely
because another protocol uses case-insensitive names. Downstream judgments must use the established representative,
not an alternate Unicode or locale-sensitive case conversion.

The ASCII case-fold proposal declares ASCII letter case irrelevant and selects lowercase ASCII as
its representative. Section 8 records that this duplicates the exact meaning proposed for ASCII
lowercase. It therefore does not justify a separate Catalog Authority. An explicit API alias may
still project that Authority if it is formally admitted.

None of the ASCII candidates owns locale behavior or Unicode case data.

## 5.3. Unicode Default Case Conversion and Full Case Folding

The Unicode default lowercase and uppercase candidates have `REJECT` review outcomes as independent
Canonicalization Built-In Laws in Section 8. Default full case folding retains an `ADMIT (proposed)` review outcome.
These names do not determine public API spelling.

The lowercase and uppercase candidates exist because invariant case conversion is a ubiquitous developer operation.
Their meaning cannot come from ambient host behavior. Java's default locale is one example. The host's current Unicode
tables are another.

Unicode Default Case Conversion has standard-defined full mappings and context-sensitive rules. A precise realization
would also require the Unicode semantic data that determines the result, rather than the host's ambient tables.
Locale-specific conversion is separate; Turkish casing is a representative example.

The reviewed operations are useful case conversions, but neither is the representative of Unicode Default Caseless
Matching. Lowercase can leave caseless-equivalent strings distinct, such as `ß` and `ss`. Uppercase can merge strings
that Default Caseless Matching distinguishes, such as ASCII `i` and U+0131 DOTLESS I. Equal conversion output could
be used to define a mathematical equivalence relation, but it would not by itself justify an independently owned
same-meaning relation for the general `Text` domain. That semantic ownership gap is the reason for `REJECT`, not an
assertion that these conversions are nondeterministic or unusable outside Canonicalization. A narrower, independently
justified semantic subject would require a new candidate review.

The case-fold candidate is not lowercasing. It targets Unicode Default Full Case Folding for locale-independent caseless
matching, including multi-code-point foldings where the Unicode profile requires them. Qualification must distinguish
Default Full Case Folding from Simple Case Folding and Turkic-specific folding. Those are not interchangeable profiles.

Unicode case data is meaning-determining when it changes the exact representative. Provider name or ICU/JDK version is
not itself the semantic Basis.

## 5.4. Unicode Normalization Forms

The four normalization-form candidates are Unicode NFC, Unicode NFD, Unicode NFKC, and Unicode NFKD.
These are descriptive review names, not public API spellings.

### NFC

The candidate selects Unicode Normalization Form C as the representative for canonical equivalence. Compatibility
distinctions remain observable.

NFC has the strongest general-text demand signal among the four normalization forms. Qualification must nevertheless
close the exact Unicode Basis dependency rather than inherit host Unicode tables. Unicode normalization stability may
preserve part of the law across later versions. Unassigned code points still require explicit treatment.

Canonical bytes are not part of this candidate law.

### NFD

NFD uses the same canonical-equivalence relation as NFC and selects the canonical decomposed representative.

The law remains useful for internal text-processing domains even though it is less common as an application-facing
default. Qualification must close the same Basis and evolution questions as NFC and justify its independent Built-In
suitability.

### NFKC

NFKC applies compatibility decomposition followed by canonical composition. Selecting it therefore declares
compatibility distinctions irrelevant in addition to canonical distinctions.

NFKC is not an implementation substitute for NFC. It has a stronger equivalence relation and is especially relevant to
restricted domains such as identifiers. Qualification must close its exact Unicode Basis and operand domain. Version
evolution must then be checked against that meaning.

### NFKD

NFKD erases the same compatibility distinctions as NFKC and selects the compatibility-decomposed representative.

Qualification must close the same Basis and evolution questions and justify why the decomposed compatibility
representative deserves an independent Built-In Authority rather than remaining a specialized internal-processing
profile.

## 5.5. Unicode NFC Case Fold

This candidate is intended to collapse Unicode canonical-equivalent and default-caseless distinctions under one
independently specified exact law. Section 8 records an `ADMIT (proposed)` review outcome, not formal Catalog admission.

Unicode D145 defines Canonical Caseless Matching using canonical decomposition and Default Full Case Folding. The
working equivalence compares the NFD forms obtained after folding canonically decomposed inputs. The proposed NFC
representative applies NFC after folding the canonically decomposed input. This must not be shortened to an
unspecified "NFC plus case folding" operation: the order and the Unicode semantic Basis are part of the exact meaning.
Locale-sensitive lowercasing is not part of this law.

ADR-0066 permits finite ordered composition of separately admitted laws, but it also permits an independently admitted
curated combined Built-In Law. A standardized, repeatedly useful canonical-caseless relation can justify one
independently selectable and versioned Law Authority even where a lawful composition could produce the same meaning.
The candidate is not admitted merely as a convenience alias; its exact relation and representative must be owned by
that Authority rather than inherited from runtime composition.

Formal admission still requires an exact Unicode Basis, treatment of unassigned code points, and verification that
representative equality matches the declared equivalence. Repeated-application stability, same-domain closure, and
finite-work bounds must also be established for the complete law, not assumed from its constituent operations.

## 5.6. Unicode NFKC Case Fold

This candidate targets Unicode `NFKC_Casefold` semantics for identifier-like text. It is a defined semantic operation,
not an arbitrary composition chosen by an implementation. In particular, ordinary lowercasing cannot stand in for it.
Section 8 records an `ADMIT (proposed)` review outcome, not formal Catalog admission.

Unicode R5 defines `toNFKC_Casefold`, while D147 defines Identifier Caseless Matching using that operation after
canonical decomposition. The working equivalence compares `toNFKC_Casefold(NFD(x))` results. The proposed
representative is that same exact Unicode-defined form. The preliminary NFD step must not be omitted when the law
claims D147 equivalence. The exact Unicode data and profile that determine these results must be fixed by the Law
Version, not inherited from a host library.

The Unicode profile combines compatibility normalization with case folding. It also defines how default-ignorable
material is treated. This provides useful, independently standardized identifier-caseless meaning that a generic
composition of NFKC and Full Case Folding must not silently claim to reproduce. ICU support is implementation and
demand evidence, not authority to change the Unicode profile.

The proposed operand remains already-established `Text`; this law does not establish that a Text value is a valid or
secure identifier. Its use in an identifier domain must be selected explicitly. A legality rule that needs to observe
an original distinction erased by this law must already be established at an authorized earlier boundary; otherwise
the law must not be selected for that input. Admission receives the representative and cannot restore erased Input
distinctions. In particular, compatibility characters and default-ignorable code points can collapse to the same
representative as otherwise distinct input. Signing, authorization, and protocol validation must not silently import
this equivalence.

Formal admission still requires proof that the proposed representative is idempotent and belongs to the same legal
presentation domain, complete representative coverage, a pinned Unicode semantic Basis, and finite-work and security
evidence for the exact law. These obligations cannot be discharged by the availability of a provider API.

## 5.7. WHATWG ASCII Whitespace Collapse

Whitespace collapse is common in application text processing. The earlier Unicode-wide whitespace-collapse proposal
remains historical research; it is no longer an active candidate in Section 8. Its scope could erase distinctions among
Unicode separators without a sufficiently justified general Built-In meaning. The replacement candidate follows the
WHATWG Infra Standard's `strip and collapse ASCII whitespace` algorithm. Five code points belong to **one** proposed
Built-In Law, not five independent Authorities.

Normative source: WHATWG Infra, §4.7, [`ASCII whitespace`](https://infra.spec.whatwg.org/#ascii-whitespace) and
[`strip and collapse ASCII whitespace`](https://infra.spec.whatwg.org/#strip-and-collapse-ascii-whitespace).

The operand is an already-established Input `Text`, interpreted as Unicode scalar values under ADR-0064. The exact set
`W` is fixed to the following five code points:

```text
U+0009  TAB
U+000A  LINE FEED
U+000C  FORM FEED
U+000D  CARRIAGE RETURN
U+0020  SPACE
```

`E_L` relates two Text values exactly when the ordered sequences of their nonempty maximal runs of scalars outside `W`
are identical. This declares the kind and length of each intervening `W` run irrelevant, including runs at either
boundary. It does not collapse any distinction between scalars outside `W`. In particular, removing a `W` run between
two nonempty runs would change the relation: `"AB"` is not equivalent to `"A B"`.

`C_L` joins the non-`W` runs with exactly one U+0020 SPACE between adjacent runs. All leading and trailing `W` scalars
are removed. An empty operand, or one containing only `W`, has the empty `Text` representative. This fixes the
representative without consulting a host whitespace predicate. It gives one representative per equivalence class;
`C_L` is idempotent and remains in the same `Text` domain.

Representative Coverage is Total for legal established Input `Text`. The law does not own a refusal for any such
operand. A single finite scan suffices, and the representative cannot contain more scalar values than the operand.
The fixed literal set and algorithm determine the proposed Law Version. A later change to the WHATWG Living Standard
cannot silently change that Version's meaning. Host locale, JDK or ICU Unicode data, and mutable external registries
are not semantic determinants.

For example, `"  A\t B\r\nC  "` becomes `"A B C"`. The escape notation in this example denotes the respective
control characters. U+000B VERTICAL TAB, U+00A0 NO-BREAK SPACE, and U+2028 LINE SEPARATOR remain untouched.
The separate ASCII boundary-trim candidates currently propose a six-character set that includes U+000B; those laws
must not be substituted for this exact five-character relation.

This law intentionally erases line and control-character distinctions. It is unsuitable wherever such characters
separate fields, records, or security-relevant protocol elements. WHATWG's generic ASCII whitespace set must not be
mistaken for XML, JSON, or HTTP whitespace grammar. Canonicalization cannot repair illegal Input or replace protocol
validation. When selected, Admission judges the established representative, and Lowering consumes that same meaning.
Raw provenance may support diagnostics but cannot reintroduce an erased distinction as a later semantic determinant.

The existing boundary-trim candidates do not collapse interior whitespace. The standardized algorithm supplies a
reusable reason to examine an independent Built-In. Nevertheless, ADR-0066 composition and Authority uniqueness must
still be checked before formal Catalog admission. Section 8 records `ADMIT (proposed)` only; a complete Exact Law
specification, independent evidence, and an explicit membership decision remain necessary.

## 5.8. LF Line Ending

The candidate is motivated by cross-platform text tooling and source-control systems that normalize repository text to
LF. That ecosystem behavior is demand evidence, not the law definition.

The current working relation treats CRLF as a line-ending spelling. It also admits standalone CR and
LF. LF is the representative. Other Unicode line separators remain unchanged unless the law explicitly
admits them; U+2028 and U+2029 are examples.

Qualification must decide the exact source separator set rather than inherit host newline behavior. Git is evidence of
demand, not semantic authority. The law does not alter ordinary text around line endings. In particular, it does not
trim
or collapse content.

## 5.9. Unicode Decimal Digit Fold

Search and text-normalization infrastructure commonly folds native decimal digits into ASCII digits. The narrow
candidate should target Unicode decimal digits only, not every character with a numeric value.

The working representative maps a Unicode scalar with General_Category `Nd` and decimal value `0` through `9` to ASCII
`0` through `9`, preserving every other scalar value.

Qualification must close the exact Unicode property and value mapping. It must also settle Version dependence and prove
that the representative stays in the promised domain. Other numeric characters remain outside this candidate; Roman
numerals and vulgar fractions are two examples.

## 5.10. Unicode CJK Width Fold

Width folding is used by search systems to remove selected full-width / half-width presentation distinctions. The
candidate must not be defined as "whatever Elasticsearch `cjk_width` or ICU currently does."

Section 8 records `REJECT` for this broad independent Built-In Law. Unicode width properties and compatibility
mappings do not by themselves define one narrow CJK width-fold equivalence and representative. The proposal leaves
open which full-width ASCII and half-width Katakana distinctions are removed and how multi-scalar cases involving
combining or voiced marks are handled. Applying NFC to resolve those cases could also remove unrelated canonical
distinctions. An implementation's current behavior cannot settle that Contract meaning.

The rejection does not claim that width conversion is inherently nondeterministic. Exact mappings and combining rules
could be implemented deterministically under an explicit Unicode semantic Basis. Narrower full-width ASCII and
half-width Katakana profiles may be researched as separate candidates, each with an independently justified exact
relation, representative, and security review. Such future work must remain distinct from full NFKC compatibility
normalization and does not reopen the rejected broad subject by implication.

## 5.11. Unicode Diacritic Fold

Accent and diacritic folding is common in search and application libraries. There is still no safe generic meaning
behind the phrase "strip accents."

Apache Commons and Lucene show that developers need diacritic-related transformations, but their
behavior does not define one shared law. Some implementations decompose text before removing marks.
Others transliterate instead. Broad ICU search folding erases additional distinctions and therefore
cannot silently define this candidate.

Unicode UTR #30 Character Foldings was withdrawn before a final published version. It therefore cannot be cited as an
ambient normative authority that silently completes this candidate. Neither the Unicode `Diacritic` property nor a
general combining-mark category establishes that removing those characters preserves meaning. The distinction may be
linguistically or security-significant in the original Text domain.

Section 8 records `REJECT` for the generic independent Built-In proposal. It cannot claim one standards-defined
equivalence and representative merely from the prevalence of accent-insensitive search or host folding functions.
Deterministic mark removal would not resolve this semantic ownership defect.

A future narrowly scoped law would need either:

```text
one current normative mapping source that exactly owns the desired relation
or
one independently justified Kontrakt-owned finite/versioned mapping with reviewable semantic meaning
```

Such a law would require separate operand, representative, and security qualification. This possibility does not
change the rejection of the present generic proposal.

## 5.12. Decimal Numeric Value

The presentation domain is a finite base-10 decimal represented by an integer coefficient and a decimal scale. Scale
differences are irrelevant when they denote the same exact decimal number.

For non-zero values, the working representative removes every trailing base-10 zero from the coefficient and adjusts the
scale without changing the exact value. Zero has one representative with coefficient zero and scale zero.

```text
2.00
    → the same representative as 2

600.0
    → coefficient 6 with scale -2

0.00
    → coefficient 0 with scale 0
```

Java `BigDecimal.stripTrailingZeros` and Python `Decimal.normalize` provide strong demand evidence for this relation.
Those host APIs do not define the Contract.

The Java `BigDecimal` cohort and stripping behavior are documented by the
[Java Platform API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigDecimal.html).

Section 8 records `ADMIT (proposed)` for the exact numeric-cohort relation, with Restricted Coverage as the proposed
solution to representative-domain closure. The equivalence relates two legal Decimal presentations when their exact
values `coefficient × 10^(-scale)` are equal. For each nonzero value, the proposed representative removes all trailing
base-10 coefficient zeroes and adjusts the scale exactly. For zero, it selects `(coefficient=0, scale=0)`.

The law establishes that representative only when the fully reduced coefficient and scale are both legal under the
same established Decimal domain and its closed bounds. Otherwise it owns an explicit Canonicalization refusal. It does
not stop reduction early, clamp the scale, round, or substitute a convenient carrier value. Numerically equivalent
operands within one exact Decimal domain have the same mathematical reduced form, so their coverage judgment is the
same. Java `BigDecimal` scale overflow is implementation evidence, not a source of Contract failure semantics. The
formal law must pin the bound and refusal relation, including cases where the zero representative is outside the
applicable scale domain; this positive review does not ratify those details.

This profile is distinct from business scale rules. A fixed monetary scale is one example of meaning that belongs
elsewhere.

## 5.13. Binary32 Canonical NaN

The presentation domain is IEEE 754 binary32 only when raw NaN representation remains Contract-visible at the Input
boundary. The candidate maps every NaN bit pattern to one quiet-NaN representative while preserving every non-NaN bit
pattern, including signed zero.

Section 8 records `ADMIT (proposed)`. The proposed representative is `0x7fc00000`, consistent with the canonical NaN
bits used by Java `Float.floatToIntBits`. Two binary32 presentations are equivalent when both bit patterns denote NaN,
or, for non-NaN patterns, only when their complete 32 bits match. Every NaN payload, sign, and signaling/quiet
distinction
is deliberately collapsed; signed zero and all non-NaN bits remain distinct. The representative is idempotent, stays
inside the binary32 domain, and has Total Coverage for admitted exact binary32 bits.

Raw-bit acquisition must precede the law. A lossless 32-bit carrier can realize the fixed exponent/fraction mask test
without executing floating-point arithmetic. Passing signaling NaN through a JVM `Float` path before Input
Establishment cannot be assumed lossless. This is a platform-carrier qualification and verification obligation under
ADR-0073, not permission to weaken ADR-0064 Input sameness or the proposed Law. A domain that needs NaN payload or
signaling distinctions for diagnostics, integrity, or control must not select this collapse. Formal admission still
requires evidence that each supported Input path preserves the admitted bit distinctions before Canonicalization.

## 5.14. Binary64 Canonical NaN

The binary64 candidate follows the same shape as binary32. Every NaN representation maps to one quiet NaN while every
non-NaN bit pattern remains unchanged.

Section 8 records `ADMIT (proposed)`. The proposed representative is `0x7ff8000000000000`, consistent with Java
`Double.doubleToLongBits`. All binary64 NaN bit patterns belong to one equivalence class; every non-NaN pattern is
only equivalent to the identical 64-bit pattern. The law preserves both signed zeroes, infinities, and finite-value
bits exactly. It is Total, idempotent, and uses fixed masks rather than host floating-point equality or arithmetic.

The realization must preserve the complete binary64 bit datum at Input acquisition, for example through a lossless
64-bit carrier. A JVM `Double` carrier cannot be presumed to retain every signaling NaN representation. The
independent binary64 Law does not inherit the binary32 representative or its bit-width semantics. NaN payload and
signaling-state loss is intentional only after the selected Canonicalization establishes its representative. Formal
admission still requires supported-path preservation and security evidence for that boundary.

## 5.15. ASCII Grouped-Decimal Text

This candidate targets one self-contained ASCII grammar. It uses `,` for grouping and `.` for the
decimal separator. The operand requirement must specify valid grouping positions. An ambient locale
cannot change that grammar. Host settings and external numbering profiles have no authority over it.

The representative removes only valid grouping separators. It does not silently add the stronger distinctions owned by
fixed-point or scientific-notation candidates.

```text
1,234,567.89
    -> 1234567.89
```

Qualification must decide the exact sign and fractional grammar. It must also review whether this subject deserves an
independent Authority or should be expressed through composition with another admitted decimal-text law.

## 5.16. Decimal-Comma Grouped Text

This candidate addresses an exact decimal-comma grammar without using a locale profile. The operand grammar itself must
fix `,` as the decimal separator and `.` as the grouping separator. Other separators or culturally inferred variants are
not accepted by implication.

The original working representative changes the decimal separator to `.`, as illustrated below:

```text
12.345,67
    -> 12345.67
```

Section 8 rejects this proposed independent Law. In its source grammar, `.` denotes grouping and `,` denotes the
decimal point. The output therefore cannot in general be interpreted under the same grammar as the input without
changing numeric meaning. For example, `1,234` means one and 234 thousandths under the proposed input grammar, while
its suggested representative `1.234` denotes one thousand two hundred thirty-four in that grammar. The problem is
same-domain semantic closure, not an inability to deterministically rewrite punctuation. Neither an implicit locale
switch nor a subsequent parser is allowed to decide which interpretation is authoritative.

A narrower grouping-removal profile might preserve the comma decimal separator, as in `12.345,67` -> `12345,67`.
That is a different exact representative and requires its own admission review; it is not admitted by this rejection.
This candidate must not become a general international-number parser. A different grouping grammar requires a separate
exact review rather than ambient locale selection.

## 5.17. Percentage / Per-Mille Rate Text

This candidate treats percent and per-mille text as presentations of an exact rate. The symbols carry scale semantics;
they are not decoration to be stripped mechanically.

```text
85.5%
855‰
0.855
    -> 0.855
```

Qualification must define the legal decimal grammar and use exact arithmetic. It must not round through binary floating
point. If the plain decimal form is admitted into the same operand domain, its relation to the existing decimal-text
laws
must also pass Authority-uniqueness review.

## 5.18. Scientific-Notation Text

Scientific notation allows several spellings of the same exact decimal value. The law must decide how
to write the exponent marker and whether to retain a positive exponent sign. It must also handle
leading zeros in the exponent and redundant zeros in the significand. Each decision must lead to one
exact representative.

The current direction keeps the operand inside scientific notation and chooses one scientific representative. It does
not make ordinary fixed-point text equivalent merely because both spellings denote the same decimal value.

```text
1.23e+04
    -> 1.23E4
```

A broader law that equates fixed-point and exponent notation would have a different equivalence relation. It must be
qualified separately rather than hidden behind a formatter choice.

## 5.19. Binary32 Decimal Lexical Form

This candidate concerns decimal Text that denotes an IEEE 754 binary32 value. Two legal spellings are equivalent only
when the law's exact conversion relation produces the same binary32 value.

Section 8 records `ADMIT (proposed)` for the exact
[W3C XML Schema 1.1](https://www.w3.org/TR/xmlschema11-2/) `float` lexical/canonical profile. The already-established
`Text` operand is interpreted under the selected `floatRep` grammar and the specifically chosen
`floatLexicalMap`; equivalence means identity of the resulting binary32 value, not equality of exact decimal rationals.
The representative is the result of the chosen `floatCanonicalMap` for that value. Both maps must be frozen as the
Exact Law Version's meaning, since XML Schema permits other conforming mappings that need not emit identical Text.

This profile distinguishes positive and negative zero by value identity and represents NaN by its one canonical lexical
value. The grammar and maps must close the treatment of `INF`, `-INF`, exponent forms, rounding, and tie-breaking.
Within the exact legal lexical profile the representative remains `Text` and is intended to be stable on reapplication;
other `Text` cannot be silently repaired or accepted by a permissive host parser. The formal law must pin occurrence
coverage and owned refusal where the selected Input `Text` lies outside its exact operand profile. The resulting
binary32
value is an interpretation used to define Text equivalence, not a Lowering result or a new Input presentation.

The law cannot use the current JVM floating-point formatter as semantic authority. Exact rational/integer-based
rounding or another independently verified implementation must produce the specified binary32 result, without
intermediate binary64 double-rounding. Very long numerals and extreme exponents require bounded-work evidence; any
safety limit must not silently change the selected rounding or representative.

## 5.20. Binary64 Decimal Lexical Form

The binary64 candidate has the same semantic shape as binary32 but a different value space and precision boundary. That
difference prevents one host formatting routine from implicitly defining both laws.

Section 8 records `ADMIT (proposed)` for the separate W3C XML Schema 1.1 `double` lexical/canonical profile. Legal
`Text` under the selected `doubleRep` grammar is interpreted by the specifically chosen `doubleLexicalMap`; operands
are equivalent when they have the same binary64 value identity. Their one Text representative is selected by the
corresponding `doubleCanonicalMap`. The Law Version must fix both exact maps rather than inherit the current JVM or
an interchangeable IEEE 754 formatter.

Value identity preserves the distinction between positive and negative zero; NaN has one lexical identity within this
Text law. The grammar and mapping specify infinities and exact rounding independently of binary32. The proposed
representative remains in the same lexical Text domain. The formal law must close coverage or refusal outside the
exact grammar, prove reapplication stability, and verify finite work for adversarially long numerals and exponents.
Its equivalence does not assert that two different exact decimal rationals are equal: they may round to the same
binary64 value. A downstream consumer must not reinterpret the established representative under another floating-point
or exact-decimal profile.

## 5.21. Radix Integer Text Family

Three working candidates cover mathematical integers written in hexadecimal, binary, or octal Text.
They do not encode an octet sequence, so they are distinct from Base16 and other encoded-byte laws. The
operand grammar must decide when leading zeros can be ignored. It must also define prefixes and signs,
including negative zero. Hexadecimal letter case needs its own exact rule.

Qualification must decide whether the three candidates are independent Authorities or one exact semantic family with
separate authoring projections. A host parser's auto-radix rules cannot define the operand.

The representative must not accidentally import fixed-width integer meaning. In an integer law, redundant leading zero
digits can be irrelevant. In a bit-vector law, the same digits may carry width.

## 5.22. Fixed-Width Bit-Vector Text Family

The hexadecimal and binary candidates represent an exact-width bit sequence. Width is part of the semantic subject.
Canonicalization may remove allowed separators or normalize hexadecimal case, but it must preserve every bit and the
established width.

```text
0000_0000_1010_1111
    -> 0000000010101111
```

This family is not an integer formatter. `0000000010101111` cannot become `10101111` when the operand is a 16-bit
vector.
Qualification must close the exact separator and prefix grammar for each textual form and review Authority uniqueness
against Base16.

## 5.23. Minimal Two's-Complement Signed-Integer Bytes

This candidate operates on a signed integer presented as a big-endian two's-complement octet sequence. Equivalent
operands decode to the same mathematical signed integer. The representative uses the shortest octet sequence that
preserves that integer and its sign.

Redundant sign-extension octets may disappear. A leading zero that is required to keep a positive value positive may
not.

```text
00 7F
    -> 7F

00 80
    -> 00 80
```

Qualification must ensure that the Input presentation preserves the octet sequence as semantic material. This law does
not reinterpret arbitrary memory or protocol buffers.

## 5.24. POSIX File-Mode Octal Text

A POSIX-style numeric file mode is a bitmask presentation, not merely an octal spelling of an unrelated mathematical
integer. The candidate therefore remains domain-specific even if its implementation can share radix parsing machinery.

The exact operand profile must define which mode bits and numeric widths are admitted. Redundant leading zero digits may
be canonicalized only where the selected profile treats them as presentation detail. Symbolic mode expressions such as
`u+rwx` are not part of this law because they can describe operations rather than one already-established mode value.

---

# 6. Structured, Protocol, and Domain Candidate Working Notes

These candidates have a closed textual or structured presentation that is usually defined by a protocol or domain
standard. A candidate may depend on parsing rules, but parsing does not itself become Canonicalization authority. The
Input side must first establish the legal semantic presentation on which the representative law operates.

When a candidate changes structured fields together, it must still preserve the presentation shape promised by
ADR-0066. A standard that produces signing bytes or a different wire representation belongs to another owner even when
it uses the word canonicalization.

## 6.1. Base16 Lowercase Text

The operand must already be legal Base16 textual presentation under an exact profile. The candidate does not encode
bytes
into hexadecimal text.

The intended equivalence declares hexadecimal letter case irrelevant and selects lowercase ASCII hex digits as the
representative while preserving the represented bit string.

Qualification must close the exact Base16 grammar. It must decide whether the `0x` prefix is legal and
whether separators are permitted. Odd digit counts need an explicit rule too. RFC 4648 Base16 has no
padding. A broader host parser cannot silently expand the operand domain.

## 6.2. Base16 Uppercase Text

This candidate has the same legal encoded-text boundary as the lowercase profile but selects uppercase hexadecimal
letters.

Authority uniqueness must be reviewed explicitly. Lowercase and uppercase select different representatives and therefore
cannot be aliases merely because they preserve the same decoded bytes.

## 6.3. Base64 RFC 4648 Canonical Text

Section 8 records `REJECT` for this proposed independent Built-In
Law. [RFC 4648](https://www.rfc-editor.org/rfc/rfc4648.html) defines the Base64 alphabet, padding requirements, and
canonical zero pad bits. Under the strict standard profile, each octet sequence already has one legal encoded
representation. The corresponding Input grammar can recognize that representation without creating a second
Canonicalization Authority that preserves everything.

Some decoders accept ignored line breaks, non-alphabet characters, excess padding, or nonzero unused pad bits. Such
acceptance does not establish a common equivalence law for Kontrakt. RFC 4648 explicitly distinguishes strict encoding
requirements from permissions that a referring specification may grant. Treating every decodable spelling as equivalent
would import parser tolerance into Contract meaning and could erase differences relevant to signatures, covert channels,
or validation.

The rejected subject is generic RFC 4648 Base64 canonicalization, not Base64 processing in every protocol. A separately
specified domain such as MIME line-wrapped Base64 may supply its own exact accepted presentations and representative.
Its scope would require new qualification instead of silently widening this candidate.

## 6.4. Base64url RFC 4648 Canonical Text

Section 8 records `REJECT` for this generic independent Built-In
Law. [RFC 4648](https://www.rfc-editor.org/rfc/rfc4648.html) defines the URL-safe alphabet but leaves omission of
padding to the referring specification. Neither a universal padded representative nor a universal unpadded
representative follows from the Base64url name alone.

The strict padded and strict unpadded profiles each have a single canonical spelling for an octet sequence when their
grammar and zero pad-bit requirements are fixed. A law that equates `YQ==` with `YQ` would add a cross-profile
equivalence not granted by every consumer. This is particularly unsafe where the encoded text itself participates in a
signature or another byte-exact protocol rule; JWS, for example, specifies unpadded Base64url for its own use.

Input or the relevant protocol Authority must enforce the selected alphabet and padding rule. A future protocol-specific
law may be qualified when that protocol explicitly admits more than one representation of the same meaning. Decoder
permissiveness cannot supply the missing semantic authority.

## 6.5. Base32 RFC 4648 Canonical Text

The operand must already be legal Base32 text under one exact RFC 4648 profile. The candidate does not decode arbitrary
text and then re-encode it under implementation defaults.

RFC 4648 defines a Base32 alphabet and canonical pad-bit requirements. Qualification must close the padding policy and
the accepted grammar. Case-insensitive behavior from a permissive decoder cannot silently widen the operand.

## 6.6. Base32hex RFC 4648 Canonical Text

Base32hex uses a different alphabet from ordinary Base32. It therefore needs its own exact semantic review rather than
an
implementation flag on one ambiguous candidate.

The same canonical pad-bit and padding questions apply. Shared codec code does not make the two alphabets one Authority.

## 6.7. Decimal Integer Text Canonical Form

Developers frequently remove redundant leading zeros or normalize signs when handling decimal integer text. That demand
does not justify a vague "number string normalize" law.

The candidate must define one exact textual grammar and representative relation. Qualification must explicitly decide:

```text
optional or required sign
treatment of leading plus
leading-zero equivalence
negative zero
empty or sign-only text
ASCII digits only or broader Unicode digits
maximum length / semantic bounds where Contract-visible
```

Parsing text into a numeric Fact is not Canonicalization. This candidate is legal only if the operand remains Text and
the output is one same-shape representative of the declared textual equivalence class.

## 6.8. Fixed-Point Decimal Text Canonical Form

This candidate covers decimal quantities that arrive as Text and remain Text after Canonicalization. It is distinct from
the structured Decimal Numeric Value candidate in Section 5.12.

The working scope is fixed-point notation rather than exponent notation. Qualification must decide
whether redundant integer leading zeros are equivalent. It must separately decide whether trailing
zeros in the fraction are irrelevant. Legal spellings such as `.5` and `12.` need exact treatment. The
representative must also account for an explicit plus sign and negative zero.

```text
12.50
    -> 12.5

.5
    -> 0.5
```

These examples illustrate the candidate direction only. They do not establish the final law before the exact grammar is
closed.

Business scale remains outside this candidate. If `12.50` carries a Contract-visible scale distinct from `12.5`, the two
texts are not equivalent under this law.

## 6.9. RFC 3986 Percent-Encoding Syntax Representative

Section 8 records `ADMIT (proposed)` for the narrow syntax representative described
by [RFC 3986, Sections 2.1 and 6.2.2](https://www.rfc-editor.org/rfc/rfc3986.html). The operand must be an
already-established `Text` presentation whose exact URI-component context and percent-triplet grammar are fixed. This
Law does not accept an arbitrary whole URI and infer its component boundaries.

Two legal component presentations are equivalent only when they differ by the hexadecimal letter case of a percent
triplet or by the choice between an unreserved ASCII character and its percent-encoded spelling. The representative uses
uppercase hexadecimal letters in retained triplets and direct spelling for encoded unreserved characters. Reserved
characters stay encoded when supplied as encoded octets.

```text
%2f
    -> %2F

%7e
    -> ~

%25
    -> %25
```

The representative remains in the same exact component-presentation domain and must be stable when the Law is applied
again. Valid percent triplets and the allowed direct character repertoire must be defined by the operand grammar. Other
established `Text` lies outside successful coverage and must not be repaired by a host URI decoder. The exact Refusal
and Coverage relation belongs in the versioned Law specification.

A selected component Law cannot become a second URI parser. In particular, decoding an encoded slash, applying this Law
before component parsing, or decoding again after Admission can change path boundaries. Encoded unreserved dots may also
matter to a later dot-segment interpretation. The application of this Law therefore requires a legally fixed component
boundary and downstream judgment-use coherence. Path normalization, routing, signature bytes, and scheme-specific
interpretation remain with their own Authorities. No ambient URI library may decide the transformation order.

## 6.10. IPv6 RFC 5952 Text

Section 8 records `ADMIT (proposed)` for an
exact [RFC 5952 Section 4](https://www.rfc-editor.org/rfc/rfc5952.html#section-4) all-hexadecimal profile. The operand
is an already-established legal IPv6 address Text without a zone identifier. Two operands are equivalent exactly when
they denote the same 128-bit IPv6 address. The representative uses lowercase hexadecimal digits, removes leading zeros
in each field, compresses the longest eligible zero run, and selects the leftmost run on a tie. It never compresses a
single zero field merely for brevity.

IPv4-embedded spellings are legal inputs when admitted by the exact IPv6 operand grammar, but the representative is
always the RFC 5952 Section 4 hexadecimal form. Section 5 recommends mixed IPv4 notation for certain address classes;
this narrower all-hexadecimal choice is an explicit Kontrakt profile, not a claim that every RFC 5952 recommendation
mandates all-hexadecimal output. A deployment-dependent prefix list must not influence it. The grammar and the Section 4
tie rules are fixed by the Law, not a host address formatter.

This Law does not establish a DNS name, an interface scope, or equality of network endpoints. Text outside the legal
address grammar has exact non-repairing Coverage and refusal treatment. Canonical formation must remain in the same
textual domain and be idempotent.

## 6.11. BCP 47 Registry-Independent Casing

This candidate is narrower than full BCP 47 canonicalization. RFC 5646 states that case distinctions do not carry
language-tag meaning and describes a conventional casing form that can be reproduced without consulting the IANA
Language Subtag Registry.

The working representative follows that registry-independent convention. Most subtags are lowercase. Syntactically
identified two-letter and four-letter subtags use the conventional uppercase and titlecase forms where RFC 5646
specifies
that treatment. Locale-sensitive host casing must not be used.

This law would change presentation case only. It would not apply registry `Preferred-Value` mappings or other
registry-dependent canonicalization.

## 6.12. BCP 47 Registry-Dependent Canonical Representative

Section 8 records `ADMIT (proposed)` for the registry-dependent language-tag canonicalization described
by [RFC 5646 Section 4.5](https://www.rfc-editor.org/rfc/rfc5646.html#section-4.5). The operand is an
already-established well-formed BCP 47 language-tag Text admitted by one precisely defined Registry profile. The Law
applies RFC 5646 Preferred-Value and applicable canonical subtag rules, including the RFC-defined ordering of extension
singletons, and selects one exact casing and replacement sequence. The registry-independent casing Law in Section 6.11
remains narrower.

The [IANA Language Subtag Registry](https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry)
snapshot used to determine meaning must be immutable and identified by the Exact Law Version and a verifiable content
identity. Kontrakt must not read the current IANA Registry, an OS locale database, or a newer ICU dataset during
canonicalization. A new snapshot that changes the mapping requires a new semantic Version; it does not silently redefine
the existing Law.

The proposed profile does not invent normalization inside registered extension subtags, reorder their contents without
authority, or reinterpret private-use values. Optional transformations not required by the selected RFC 5646
canonicalization profile, including unconditional Suppress-Script deletion or arbitrary variant reordering, are not
silently added. Exact grammar, Preferred-Value replacement order, legality after replacement, same-domain closure, and
finite work remain normative admission checks.

## 6.13. UUID Lowercase Text

The candidate domain is one exact standard textual UUID form. Hexadecimal letter case is declared irrelevant and
lowercase text is the representative.

Qualification must confirm the accepted grammar and delimiter positions. It must separately decide whether wrappers or
URN forms belong to the operand. Alternative textual forms cannot be accepted by accident. The law changes neither UUID
bits nor version or variant meaning.

A structured UUID value that no longer contains textual case does not need this law.

## 6.14. RFC 9911 MAC-48 Lowercase Text

This candidate is narrower than a generic MAC-address normalizer. RFC 9911 defines a 48-bit IEEE 802 MAC text profile as
six hexadecimal octets separated by colons and uses lowercase hexadecimal characters for its canonical representation.

Qualification must decide whether Kontrakt adopts that exact textual profile as the operand. Other address lengths and
separator conventions are not silently included.

## 6.15. IPv4 Network Prefix

The candidate operand is legal IPv4 prefix text with an exact prefix length. Two operands are equivalent when they
denote
the same prefix bits at the same prefix length.

The working representative clears every address bit outside the prefix. RFC 9911 uses this canonical form for its IPv4
prefix type.

```text
192.0.2.1/24
    -> 192.0.2.0/24
```

Qualification must keep strict dotted-decimal syntax separate from legacy IPv4 parser behavior. Prefix-length legality
belongs to the operand profile rather than to a backend parser.

## 6.16. IPv6 Network Prefix

Section 8 records `ADMIT (proposed)` for the [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) IPv6 network-prefix
representative. The operand is an already-established legal IPv6-prefix Text with a prefix length from 0 through 128.
Two operands are equivalent only when their prefix lengths and the address bits covered by that length are equal. The
representative clears every non-prefix bit and formats the remaining 128-bit address using the Section 6.10 RFC 5952
Section 4 all-hexadecimal profile.

```text
2001:db8::1/64
    -> 2001:db8::/64
```

The prefix length is retained. A shorter and a longer prefix are not equivalent merely because their displayed address
portions agree. Embedded IPv4 presentation is interpreted only through the precise IPv6 operand grammar, and no ambient
prefix policy or host formatting rule selects the result. This Law's normative meaning includes its relation to the
admitted IPv6 textual profile; shared realization code does not establish Authority ownership.

## 6.17. EUI-48 / MAC-48 Multi-Format Text

Section 8 records `REJECT` for the proposed generic multi-format Authority. This candidate is broader than the
separately proposed [RFC 9911](https://www.rfc-editor.org/rfc/rfc9911.html) lowercase colon-separated MAC-48 Text Law in
Section 6.14. Several forms are widely used, including hyphen-separated octets and vendor-specific dotted notation, but
their prevalence does not establish one universal standard input grammar and interpretation rule for Kontrakt.

Accepting all such formats under a single Law without fixing their octet interpretation, any relevant bit-order
convention, and the supported source standard would make its equivalence depend on an inferred origin. It would also
overlap the existing RFC 9911 Authority without proving a distinct and safely bounded need. This is not a claim that
conversions between individually specified formats are inherently nondeterministic.

A new narrowly named candidate may explicitly translate a designated IEEE EUI-48 textual profile, or a separately
specified dotted form, into the RFC 9911 representation after its grammar and octet-order semantics are established.
That proposal requires a separate Catalog decision; the present broad multi-format Law is not admitted.

## 6.18. HTTP-Date Text

Section 8 records `ADMIT (proposed)` for
the [RFC 9110 Section 5.6.7](https://www.rfc-editor.org/rfc/rfc9110.html#section-5.6.7) HTTP-date profile. The standard
recognizes IMF-fixdate, obsolete RFC 850 date, and obsolete asctime date forms for recipients, but requires an
IMF-fixdate form when a sender generates a date. These admitted spellings can represent one HTTP date without giving
Kontrakt a generic timestamp-normalization Authority.

The proposed equivalence relates exact legal spellings that denote the same UTC calendar date and time under the
required interpretation context. The representative is that date's IMF-fixdate Text. The obsolete RFC 850 spelling has a
two-digit year whose interpretation depends on the reference time specified by RFC 9110. That time must therefore be an
explicitly resolved and bound UTC Required Basis. It cannot be the compiler's clock, a mutable runtime default, or the
moment at which a cached result happens to be reused.

```text
Sun, 06 Nov 1994 08:49:37 GMT
Sunday, 06-Nov-94 08:49:37 GMT
Sun Nov  6 08:49:37 1994
    -> Sun, 06 Nov 1994 08:49:37 GMT
```

The normative specification must fix calendar legality, weekday consistency, leap-second treatment, the exact RFC 850
century rule, representable year range, and failure when a legal Input `Text` is not a canonicalizable HTTP-date. These
conditions must be decided under the selected Law rather than delegated to the current JVM date parser. HTTP-date
Canonicalization does not establish freshness, expiry, or authorization. Those judgments belong to their own Contracts.

## 6.19. HTTP Media Type Text

Section 8 records `ADMIT (proposed)` for a restricted HTTP media-type *syntax* representative based
on [RFC 9110 Section 8.3.1](https://www.rfc-editor.org/rfc/rfc9110.html#section-8.3.1)
and [RFC 6838 Section 4.3](https://www.rfc-editor.org/rfc/rfc6838.html#section-4.3). It does not claim to canonicalize
every semantic equivalence of a registered media type. The already-established `Text` operand must be parsed under one
exact HTTP media-type grammar, with one type/subtype and a finite set of named parameters.

The common equivalence treats ASCII letter case in type, subtype, and parameter names as irrelevant. RFC 6838 gives no
meaning to parameter order and prohibits repeated parameter names. Within the selected HTTP quoted-string grammar,
equivalent quoted and token spellings of an unchanged parameter value may share a representative. The Law preserves
every parameter value's exact content and case rather than assuming the value's registered comparison semantics.

The representative lowercases type, subtype, and parameter names; orders parameters by a fixed ASCII name order; emits
an unquoted token only when the value is a legal token; and otherwise uses one exactly specified quoted-string and
escape spelling. For example, the following are equivalent under the proposed common syntax relation:

```text
Application/Example;Z=ABC;A="xyz"
    -> application/example;a=xyz;z=ABC
```

No parameter registration or ambient IANA registry lookup may silently extend this equivalence. In particular,
`charset=UTF-8` is not converted to `charset=utf-8` merely because that particular parameter may have a case-insensitive
value. Duplicate names after ASCII case normalization, invalid escapes, malformed parameter syntax, or unsupported
extensions must have exact non-repairing coverage and refusal rules. Host map iteration order cannot select the output.
The Law remains a same-domain `Text` representative and does not establish media-type-specific processing meaning.

## 6.20. HTTP Qvalue Text

HTTP qvalues have a small numeric domain and several legal lexical spellings for the same value. This makes them a
useful
narrow numeric-text candidate.

Qualification must first close the exact RFC grammar and precision bound. It must then choose one representative
spelling
for each legal value. The standard constrains the value and syntax but does not by itself justify an arbitrary host
floating-point formatter.

The operand remains qvalue Text. Parsing the value into an unrelated numeric Fact is outside this law.

## 6.21. HTTP (S) URI Normal Form

Section 8 records `ADMIT (proposed)` for a restricted HTTP (S) scheme-based URI representative
using [RFC 9110 Section 4.2.3](https://www.rfc-editor.org/rfc/rfc9110.html#section-4.2.3) and the
applicable [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986.html) syntax rules. This is not an assertion that URI
normalization proves two origin resources are the same, and it does not admit generic whole-URI canonicalization for
every scheme.

The proposed V1 operand is an already-established absolute `http` or `https` URI `Text` under a strict ASCII-host
profile. Userinfo, internationalized host interpretation requiring IDNA, and IP-literal profiles whose canonical
spelling has not been closed are outside this operand. DNS resolution, origin-specific query rules, and platform
URL-parser recovery do not establish legal alternate presentations. HTTP and HTTPS remain distinct schemes.

Within that domain the Law declares only the selected standards-defined presentation differences equivalent. Its
representative lowercases the scheme and ASCII host, omits the matching default port (`80` for HTTP or `443` for HTTPS),
applies the exact RFC 3986 percent-encoding and dot-segment rules in an expressly fixed order, and preserves query
contents and ordering rather than treating them as an unordered map. The ordinary empty-path representative is `/` only
where the applicable HTTP request-target context permits that equivalence.

```text
http://EXAMPLE.com:80/%7Euser
    -> http://example.com/~user
```

RFC 9110 identifies an OPTIONS request-target exception relevant to empty-path handling. A `Text` value alone cannot
supply that request context. The positive applicability requirement must therefore exclude contexts where the proposed
normalization is not valid or demand the appropriate explicit context binding. A later consumer may not re-interpret the
representative under another URI parser or decode it a second time. Signed URI material, routing identity, encoded
separators, and path traversal are security-sensitive; the selected Law is not a substitute for their owning validation
and authorization rules.

The exact normative specification must settle the ASCII-host grammar, URI component boundaries, operation order,
percent-encoding and path corner cases, legal occurrence coverage, and the identity of any context-dependent Required
Basis. Host syntax not supported by the selected profile must be refused rather than guessed. A more permissive URL
library or a future standards revision cannot silently broaden this Law's Version.

## 6.22. CoAP URI Normal Form

Section 8 records `ADMIT (proposed)` for a restricted `coap` / `coaps` URI normal-form Law grounded
in [RFC 7252 Section 6.3](https://www.rfc-editor.org/rfc/rfc7252.html#section-6.3)
and [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986.html). The operand is an already-established whole URI Text whose
scheme, authority, path, and query are parsed under one exact CoAP URI profile. The relation is the selected standard
scheme-specific normalization, not proof that two requests ultimately reach the same resource.

The representative lowercases the scheme and permitted ASCII host, removes the scheme's default port (`5683` for `coap`
and `5684` for `coaps`), applies only the RFC-authorized percent-encoding and path rules, and uses `/` for an eligible
empty path. It does not equate `coap` with `coaps`, sort application-defined query items, decode a reserved path
separator, or allow a later consumer to decode the representative again. The rules for IPv4 and IPv6 literals must be
explicit, including the chosen IPv6 textual profile.

V1 qualification selects an exact ASCII-host and permitted-IP-literal domain; unsupported internationalized names,
userinfo, zone identifiers, and ambiguous URI spellings lie outside successful Coverage. Parsing precedes
component-sensitive transformation, and the order of percent-encoding and dot-segment treatment is fixed by the Law
rather than a host URL library. Any reused implementation shared with the HTTP URI Law must still preserve the
independent CoAP Authority and its own scheme defaults.

## 6.23. Generic URN Lexical Representative

Section 8 records `ADMIT (proposed)` for the restricted **RFC 8141 assigned-name-only** Text
representative. [RFC 8141 Section 3.1](https://www.rfc-editor.org/rfc/rfc8141.html#section-3.1) defines generic
URN-equivalence for the assigned name. The operand is a legal `urn:` assigned-name Text without `r-component`,
`q-component`, or `f-component`; full URNs carrying those components are outside this Law's successful Coverage.

Its equivalence disregards only scheme and Namespace Identifier ASCII letter case and percent-triplet hexadecimal letter
case in the Namespace-Specific String. The representative uses `urn:`, a lowercase NID, and uppercase hexadecimal
letters within retained NSS percent triplets. It must not decode percent-encoded octets, infer namespace-specific
identity, or reinterpret the assigned name through generic RFC 3986 unreserved-decoding rules.

RFC 8141 ignores optional components for the generic URN-equivalence comparison while permitting them to affect
resolution requests. Discarding such components from a full input URN could change operational meaning. Restricting the
operand to assigned names avoids this problem without claiming the whole-URN identity or resolution semantics.
Namespace-specific stronger relations require independently qualified Laws.

## 6.24. RFC 3966 `tel:` URI Representative

RFC 3966 gives a much better Contract subject than generic user-entered telephone-number normalization. Visual
separators
do not affect URI comparison, and parameters are compared independently of their source order. The standard also gives a
preferred parameter ordering that can guide one representative.

A local number is not complete without its `phone-context`. That material is part of the URI meaning and cannot be
supplied from an ambient default region.

Qualification must keep global and local numbers distinct. It must also preserve the standard's parameter-specific
comparison rules instead of applying one blanket text fold to every parameter value.

## 6.25. XML Schema Date/Time Canonical Lexical Family

XML Schema Datatypes explicitly separates a value space from its lexical space and defines canonical lexical mappings.
That architecture is close to the representative problem that this Catalog is qualifying.

The current working family contains three candidates: `date`, `time`, and `dateTime`. They require
independent admission when their semantic subjects or Version histories differ. Shared implementation
code does not merge them.

The exact XSD profile determines timezone normalization and fractional-second spelling. Qualification must preserve the
profile's own timezone semantics rather than importing RFC 3339 or RFC 9557 meaning. Similar-looking timestamp strings
can
belong to different equivalence relations under those standards.

## 6.26. DOI Textual Representative

DOI names have an unusual but useful comparison rule. Basic Latin `A` through `Z` compare case-insensitively, while
non-ASCII case differences are not collapsed by that rule. DOI comparison also does not perform generic Unicode
normalization.

That makes DOI a stronger domain candidate than applying Unicode case folding to an identifier. Qualification must
choose
one exact representative for the Basic Latin case-insensitive relation and preserve every distinction outside it.

The DOI grammar and any URI or proxy presentation remain separate questions. A DOI-name law must not silently absorb the
syntax of a resolver URL.

## 6.27. IBAN Electronic Text

Section 8 records `ADMIT (proposed)` for the paper-to-electronic spacing distinction
under [ISO 13616-1](https://www.iso.org/standard/81089.html). The operand is an already-established Text in the exact
electronic IBAN presentation or its specified paper grouping with `U+0020 SPACE`. The equivalence removes only
authorized printed grouping spaces while preserving all account characters exactly. Its representative is the
corresponding unspaced electronic Text.

```text
DE89 3704 0044 0532 0130 00
    -> DE89370400440532013000
```

The Law does not uppercase the BBAN or apply case-insensitive Unicode folding. Country-specific BBAN rules may make
letter case meaningful. Nor does it silently remove arbitrary Unicode whitespace, repair check digits, or infer country
formats from an ambient registry. Country-specific length and BBAN legality, MOD-97 check-digit judgment, and account
validity remain separate Input / Admission concerns. The precise paper-group grammar and supported domain must be fixed
so no invalid spacing is repaired by canonicalization.

## 6.28. GTIN 14-Digit Representative

GS1 Digital Link syntax uses a 14-digit representation for GTIN values. Shorter forms are padded with
leading zeroes under that profile. This applies to GTIN-8, GTIN-12, and GTIN-13.

This is not a generic integer-leading-zero law. The zeros are part of a domain representation rule for a GS1 identifier.
Qualification must preserve that domain meaning and keep check-digit validity outside Canonicalization.

A later GS1 Digital Link URI candidate may consume this representative without turning the GTIN Authority into a URI
law.

## 6.29. Package URL Canonical Representative

Section 8 records `ADMIT (proposed)` only for an
**[ECMA-427](https://ecma-international.org/publications-and-standards/standards/ecma-427/) core PURL syntax**
representative. The operand is a legal Package URL Text in an exact core grammar. The Law owns only representation
distinctions for which the ECMA-427 core specification supplies common rules. Package-type-specific identity and
normalization are not silently included.

The canonical core form uses the `pkg:` scheme and a lowercase package type, removes only the redundant slashes that
ECMA-427 declares insignificant, applies component-specific percent encoding, and chooses one exact order for distinct
qualifier keys where the standard allows reordering. Version, namespace, name, qualifier values, and subpath remain
subject to the core component grammar; their ecosystem-specific case sensitivity or meaning cannot be guessed. Duplicate
or invalid qualifier keys, malformed percent encodings, and unsupported type-dependent forms require exact Coverage and
refusal.

A Maven, npm, or PyPI rule may create additional equivalence not established by the core syntax. Such a rule needs its
own qualified Law or explicit versioned semantic material. Canonicalizing the core PURL Text does not establish that two
package versions are the same dependency or may be treated as the same vulnerability target. The normative specification
must verify same-domain closure and idempotence, including the exact percent-encoding and qualifier-order algorithm.

## 6.30. Kubernetes Quantity Representative

Section 8 records `ADMIT (proposed)` for a **Kubernetes Quantity canonical Text** Law tied to one immutable Kubernetes
Quantity semantic profile. The basis is
the [Kubernetes Quantity API documentation](https://kubernetes.io/docs/reference/kubernetes-api/definitions/quantity-resource/)
and a specific verifiable `k8s.io/apimachinery/pkg/api/resource` implementation baseline. Kubernetes parses DecimalSI,
BinarySI, and DecimalExponent suffix families, remembers the selected family, applies its specified precision and range
rules, and emits its own canonical format. Kontrakt must reproduce those semantics rather than replace them with a new
exact-value DecimalSI-only rule.

```text
1.5
    -> 1500m

1.5Gi
    -> 1536Mi
```

A numeric comparison is not the Law's equivalence: `1Ki` and `1024` can compare as the same numeric quantity while their
Kubernetes canonical Text remains different because the source suffix families are retained. The proposed `E_L` relates
exactly those legal input spellings that produce the same Text under the fixed Kubernetes parse-and-canonicalize
profile. `C_L` is that profile's actual canonical Text. The exact Law must establish that the result is a legal
same-domain operand and a fixed point under repeated application; neither property may be assumed merely because a Go
`String()` method exists.

Kubernetes can round finer inputs, including the documented `0.1m` to `1m`, and can cap values outside its supported
range. Such behavior is part of the selected Kubernetes semantics and must not be silently replaced by mathematical
exact-value equivalence or a Kontrakt-only refusal rule. The exact normative specification must reconcile documentation
and the chosen implementation baseline for suffixes, precision, rounding, signs, boundary values, zero, and
serialization. Input Text must be acquired losslessly; the Kubernetes-defined interpretation is then explicit inside
this Law. Host floating-point arithmetic is not its semantic authority.

The Law Version identifies the immutable semantic profile and its source/test-vector provenance. Kubernetes releases are
compatibility evidence, not necessarily distinct Law Versions: different releases may be supported under one Version
when their relevant Quantity behavior is proven equivalent. A changed parser or canonical representative requires a new
semantic Version and must not silently alter an existing Authority. Developers should not have to select a cluster
release when several verified releases share that profile. This Quantity Law does not establish general Kubernetes
resource-policy validity or numeric-comparison semantics.

## 6.31. `geo:` URI Representative

Section 8 records `ADMIT (proposed)` for an [RFC 5870](https://www.rfc-editor.org/rfc/rfc5870.html) WGS-84 core `geo:`
URI Text profile. The operand is a legal URI with WGS-84 coordinates and only the core parameters admitted by the
profile. Two operands are equivalent under the RFC's geographic URI comparison rules, including equal exact decimal
coordinate values, the longitude equivalence at the antimeridian, longitude irrelevance at the poles, and the
RFC-authorized omitted/explicit default CRS distinction.

Kontrakt selects one deterministic representative within each such class: minimal exact decimal coordinate spelling,
longitude `180` at the antimeridian, longitude `0` at the poles, and omission of an explicit default `crs=wgs84`. The
existence and exact value of altitude are preserved. The existence and exact value of uncertainty `u` are also
preserved: absent altitude is not altitude zero, and an absent `u` parameter is not `u=0`.

The V1 operand excludes unqualified extension parameters and other CRS-specific equivalence rules, which RFC 5870 does
not globally determine. The Law must specify exact latitude/longitude bounds, negative zero, numeric lexical grammar,
component order, refusal, and idempotence. It operates on exact decimal text meaning, not Binary32/64 approximations,
host geolocation, or lookup of a mutable geodetic registry.

## 6.32. RFC 7468 Textual-Encoding Representative

Section 8 records `ADMIT (proposed)` for a **single [RFC 7468](https://www.rfc-editor.org/rfc/rfc7468.html)
textual-encoding instance**. Its operand is one legal established encapsulation Text with an explicit BEGIN/END Label
and Base64-encoded octets. The equivalence relates only permitted parser spellings carrying exactly the same Label and
octet sequence. The representative selects one exact strict-generator presentation with Base64 lines of 64 characters
except the final line, no extraneous whitespace, and a fixed LF line ending.

The admitted parser spellings are narrower than every input tolerated by a permissive PEM reader. BEGIN and END Labels
must match, label case and content are preserved, Base64 alphabet, padding, and zero pad bits are validated, and only
explicitly enumerated line-layout or whitespace variants are canonicalized. A mismatch, inserted nonalphabet characters,
or unsupported framing is refused rather than repaired. The exact output newline/trailing-newline rule must be fixed so
the canonical output is again a legal operand and applying the Law twice is stable.

A file containing several encapsulations is a different subject. The Law does not reorder such instances or normalize
ASN.1, certificates, keys, signatures, or their validation results. RFC 7468 supplies a generator form and bounded
parser latitude that are independent of the rejected generic Base64 textual law.

## 6.33. Avro Parsing Canonical Form

Section 8 records `ADMIT (proposed)` for the
versioned [Apache Avro Parsing Canonical Form](https://avro.apache.org/docs/current/specification/#parsing-canonical-form-for-schemas)
Law. The operand and result are both Text presentations of legal Avro schemas under one frozen Avro specification
profile. The equivalence is equality of the Parsing Canonical Form Text, and the representative is the exact result of
the Avro-defined PRIMITIVES, FULLNAMES, STRIP, ORDER, STRINGS, INTEGERS, and WHITESPACE transformations in their
specified order.

This is not general JSON Canonicalization. The Parsing Canonical Form intentionally discards properties that do not
affect Avro parsing, but they may still affect schema evolution, defaults, logical-type interpretation, or application
policy. Therefore this Law asserts parsing equivalence only. It does not establish full schema substitutability,
validation of application constraints, or equality of arbitrary JSON documents.

The normative Law must fix the Avro specification version, complete schema grammar, name/namespace resolution, exact
escaping and integer rules, idempotent same-domain output, and bounded handling of deeply nested schemas. Named schema
references require explicit finite resolution, not host object identity or unbounded recursive traversal. Avro
fingerprints may accelerate lookup, but the exact canonical content remains the final equality witness; hash equality
alone is not authoritative.

## 6.34. POSIX IPv4 Numbers-and-Dots Text

This candidate covers the historical POSIX IPv4 numbers-and-dots syntax as one explicit operand profile. That profile
can
admit one- through four-part forms and radix forms that a strict dotted-decimal IPv4 grammar intentionally rejects.

Equivalent operands denote the same 32-bit IPv4 address under that exact POSIX interpretation. The working
representative is the ordinary four-octet decimal dotted form.

```text
127.1
0x7f.1
0177.0.0.1
    -> 127.0.0.1
```

This law must be selected explicitly. It does not authorize a strict IPv4 Input to begin accepting legacy syntax. It
also cannot delegate interpretation to an OS resolver or a host parser whose accepted forms may differ.

WHATWG URL-host IPv4 parsing remains a different semantic domain. Similar visible input does not make the two parsing
relations one Authority.

---

# 7. Deferred and Explicit Boundary Working Notes

The following profiles are plausible or commonly requested transformations that are not part of the current
admission-ready set. This section also records familiar requests that belong to a different authority unless a narrower
semantic subject is defined. Moving a deferred profile into the built-in Catalog requires explicit qualification and a
Catalog admission decision under ADR-0076. A new ADR is required only if that work changes the Catalog architecture or
qualification law.

## 7.1. Unicode Stabilized Normalization

Unicode Stabilized Strings reject code points that are unassigned in the selected Unicode version so that a successful
normalization result remains stable across version evolution.

Stabilized NFC and NFKC remain deferred until their exact refusal relation and semantic-Basis requirements are closed at
the Catalog level. ADR-0066 owns how those closed requirements are represented and established downstream.

## 7.2. Locale-Sensitive Case Conversion

Locale-sensitive case conversion is common in user-facing text but cannot inherit an ambient locale. The JVM process
locale is one example of state that must not become hidden Contract meaning.

A future law must identify the exact locale/profile as Contract-visible semantic material and prove that the resulting
mapping still satisfies Canonicalization's representative and idempotence laws. It remains deferred from the basic
locale-independent surface.

## 7.3. Turkic Case Folding

Unicode defines Turkic-specific case-fold behavior distinct from Default Case Folding. It is not silently selected by
language environment or locale.

A future profile must make that distinction explicit and independently justify Built-In suitability. It remains separate
from the Unicode default full case-fold candidate.

## 7.4. Broad Search Folding

Lucene ICU folding and similar search pipelines intentionally erase several kinds of distinction at once. Case and
accent are two examples. Width and digit distinctions may also be erased.

That breadth is useful for search but is too large to import as a default Canonicalization law merely because a SOTA
search library provides it. Narrow laws are reviewed separately above. A future broad search-fold Authority must first
define its exact relation and representative. It must then close its Unicode Basis and security consequences.

Folding U+00A0 NO-BREAK SPACE into U+0020 SPACE belongs to this review unless a narrower law is independently justified.
The two code points have different line-breaking semantics, so visual similarity alone cannot establish equivalence.

## 7.5. Signed-Zero Collapse

A binary floating-point law may declare positive and negative zero equivalent, but IEEE 754 behavior can later observe
the sign. V1 therefore defers this law until those semantic consequences are reviewed explicitly.

## 7.6. Whole-URI Canonicalization

RFC 3986 defines generic syntax normalization, but whole-URI equivalence can become scheme-specific. V1 therefore admits
no generic whole-URI canonicalization law.

Section 6.9 isolates percent-encoding syntax because that relation can be reviewed without importing the rest of URI
meaning. Sections 6.21 and 6.22 review HTTP and CoAP only because those schemes provide their own normalization rules.
Dot-segment removal remains outside the generic component candidate because path interpretation creates a different
boundary.

## 7.7. IDNA and PRECIS Profiles

IDNA and PRECIS combine representative formation with legality rules. A future Kontrakt profile must split those
responsibilities according to Kontrakt authority rather than import an external pipeline as one opaque law.
Representative formation may belong to Canonicalization while Input or Admission owns the corresponding legality rule.

## 7.8. Instant-Preserving Temporal Profiles

A temporal law may declare two offset date-time presentations equivalent when they denote the same instant and select a
UTC representative. Such a law is valid only when the original civil-time context is explicitly irrelevant.

V1 therefore admits no generic date-time canonicalization law.

## 7.9. Strict IPv4 Text and Non-POSIX Legacy Forms

A strict four-octet dotted-decimal IPv4 presentation can already be representationally singular when Input rejects
redundant or legacy spellings. In that case Canonicalization has no additional representative to select.

Section 6.34 now isolates the POSIX numbers-and-dots relation as an explicit candidate. That decision does not make
other
legacy parser behavior part of the same law. A parser that accepts additional spellings needs its own exact semantic
review before those spellings can enter a Canonicalization operand domain.

WHATWG URL-host IPv4 parsing remains a separate semantic domain because it deliberately preserves its own legacy IPv4
interpretation rules. If Kontrakt later supports that behavior, it must be reviewed as a URL-host profile rather than as
generic IPv4 text.

## 7.10. Order-Insensitive Finite Aggregates

Sorting and duplicate removal are common in application-defined collections, but the generic operation
is not yet a Built-In Law candidate. Tags are one example. Identifier collections and permission-like
lists raise the same question. The Contract must first establish whether order and multiplicity are
distinctions Canonicalization may erase.

If a future aggregate law needs an element order to choose one representative, that order must be Contract-defined. A
host comparator cannot supply it implicitly. Text ordering is particularly sensitive because UTF-16 code-unit order is
not automatically the same relation as Unicode scalar-value order.

## 7.11. Email Address Normalization

Generic email normalization remains deferred. SMTP mailbox local-parts cannot be assumed case-insensitive, while the
domain portion follows different comparison rules. Internationalized domains add a separate IDNA boundary.

A single lowercasing or Unicode-folding law therefore cannot safely define mailbox equivalence. A future candidate must
start from a narrower semantic subject.

## 7.12. User-Entered Telephone Numbers

User-entered telephone numbers remain deferred because national notation can require numbering-plan context before it
has
one global interpretation. A hidden default region cannot become a Canonicalization determinant.

Section 6.24 treats RFC 3966 `tel:` URI as a different semantic subject because the protocol carries the context
required
for its own comparison rules. That candidate does not justify a generic phone-number normalizer.

## 7.13. RFC 3339 Lexical Normalization

A narrow RFC 3339 textual profile may be useful even when the original offset is preserved. Candidate work can examine
permitted lexical variation without converting every timestamp to UTC.

The offset distinction must remain exact. RFC 3339 `-00:00` means that the local offset is unknown and is not equivalent
to `Z` or `+00:00`. Any future representative must preserve that distinction.

RFC 9557 adds named time-zone and additional suffix semantics on top of RFC 3339-style timestamps. Those fields create a
separate candidate problem and must not be folded into a generic instant-only law.

## 7.14. Canonical JSON and JCS

JSON Canonicalization Scheme work does not belong to this inbound Built-In Law Catalog merely because it uses the word
canonicalization. JCS defines deterministic JSON serialization and requires parsed JSON string data to be preserved
rather than Unicode-normalized.

That authority belongs to the protocol or serialization layer that owns the representation. Kontrakt Canonicalization
must not absorb JCS as an inbound text law.

## 7.15. HTML Character References

HTML character-reference resolution is not a generic Text Canonicalization law. In raw HTML source, replacing a
character
reference with the literal character can change parsing because characters such as `<` participate in markup syntax.

After an HTML parser has established character data, the source spelling distinction has already been consumed by that
parser. The correct owner is therefore HTML interpretation at the Input boundary, not a later law that rewrites
arbitrary
Text.

## 7.16. Currency Symbols and Currency Codes

A currency symbol does not identify one currency without additional context. The symbol `$` is the obvious example. A
locale or market convention can change what it means, so a generic symbol-to-ISO-code law would have an undeclared
semantic determinant.

An alphabetic currency code that only needs the existing ASCII case representative should reuse that law if the operand
profile permits it. The Catalog must not create a currency-specific Authority merely to rename an existing case rule.

## 7.17. Time-Zone Identifier Aliases

Time-zone databases contain aliases, but the meaning of a preferred identifier is not as simple as selecting the newest
IANA spelling. CLDR maintains stable canonical identifiers for its own purposes, and those choices can differ from a
current TZDB preference.

A future candidate must therefore name the authority that owns the representative. It must also make the relevant data
version explicit when that version can change the result.

## 7.18. SIP and SIPS URI Profiles

SIP URI comparison has domain-specific rules that cannot be inferred from generic URI normalization.
Case handling depends on the relevant component. Parameters and headers have their own comparison
semantics. Percent-encoding must also follow the SIP rules.

A future candidate should begin from the exact RFC comparison relation. The work remains deferred until the
representative
and the relationship to generic URI constituent laws are closed.

## 7.19. GS1 Digital Link Canonical URI

GS1 Digital Link defines a canonical URI profile, not merely a GTIN text format. It requires HTTPS and
selects a canonical host. It uses the current 14-digit GTIN representation and restricts which query
material remains. This is stronger than the GTIN candidate in Section 6.28.

The full URI law remains deferred because it combines several GS1 identifier rules with URI-level semantics. The
candidate must first prove that these rules form one legitimate Built-In Authority rather than a protocol-owned output
projection or a composition of narrower laws.

## 7.20. UCUM Quantity Canonicalization

UCUM gives units exact semantics and defines relationships to canonical units. That makes it a strong aggregate research
case for a pair such as `(value, unit)`.

A unit cannot be changed without changing the numeric value consistently. Qualification must therefore close conversion
precision and exact arithmetic before any representative is admitted. A text-only unit fold would not preserve the
quantity meaning.

## 7.21. RDF Dataset Canonicalization

RDFC-1.0 is a W3C canonicalization algorithm for RDF datasets. It is useful as a stress case because graph isomorphism
and
blank-node assignment require substantially more work than ordinary text normalization.

The standard produces a canonical serialization of a dataset and discusses denial-of-service risk for difficult inputs.
Kontrakt must first decide whether that output remains inside the Canonicalization presentation boundary or belongs to a
serialization/signing owner. The resource problem must remain separate from semantic equivalence.

## 7.22. LDAP and X.509 String Preparation

LDAP string matching and X.509 name comparison can require preparation before comparison. Their rules
may normalize Unicode or alter case. Some rules also treat insignificant spaces specially. Legality
checks are part of those protocols but are not automatically Canonicalization.

A future candidate must split representative formation from prohibited-input handling before admission. The profile must
not become a shortcut for importing an entire security-sensitive matching pipeline as one opaque law.

## 7.23. SPDX License Expressions

SPDX defines exact syntax and operator meaning for license expressions, but it does not provide one universal canonical
ordering or parenthesization for every semantically equivalent expression.

The current subject therefore remains deferred. Kontrakt should not invent a broad Boolean-algebra normal form merely
because applications would find one convenient.

## 7.24. Signing and Wire Canonicalization

Many important standards use canonicalization to produce signing or wire material. Their existence is evidence for
strict
representation control, but it does not make them inbound Canonicalization laws.

DER and XML canonicalization are examples of standards that control serialization. JWK Thumbprint
instead constructs a hash preimage from selected key material. The relevant serialization, signing, or
identity-derivation authority owns those outputs. A separate same-shape inbound law would need
independent justification.

## 7.25. Generic Filesystem Paths

Generic path normalization remains outside the Catalog. Lexically removing `.` or `..` is not
equivalent to resolving a filesystem path. Symbolic links can change the resolved object. Mount points
and platform rules can change it as well.

A future path candidate needs a much narrower semantic subject. Ambient filesystem state cannot become hidden
Canonicalization meaning.

## 7.26. Generic SQL and Database Identifiers

Database identifier comparison depends on the database and its configured semantics. Case folding and collation can also
depend on deployment configuration.

A generic SQL identifier law would therefore hide ambient semantic state. A database-specific profile could be reviewed
later if its equivalence and representative are closed independently of one running database instance.

## 7.27. Single-Shape International Identifiers

Some international identifiers primarily need validation rather than normalization because their legal representation is
already singular. A fixed-width identifier with no alternate legal spelling does not need a Canonicalization law merely
because it is widely used.

LEI is a useful example of this boundary. Similar identifier families should first prove that multiple legal
representations of the same meaning actually exist before entering the candidate inventory.

## 7.28. Charset Labels

Charset names and aliases are a real interoperability problem, but there is more than one relevant semantic universe.
The
IANA charset registry and the WHATWG Encoding model do not expose exactly the same alias policy.

A future candidate must choose one authority and Version basis before mapping aliases to a representative name. Until
then, a generic "charset label normalize" law would hide an external semantic choice.

## 7.29. Fixed-Width Integer Byte Order

Big-endian and little-endian byte sequences can represent the same fixed-width integer when the byte order is already
known. The raw octets alone do not reveal that order, so a generic byte-order Canonicalization law would require hidden
context.

This remains a stress case rather than a current candidate. Qualification must first decide whether byte order is part
of
Input presentation meaning and whether conversion to an integer belongs to Lowering instead of Canonicalization.

## 7.30. Finite Bit Sets and CPU-Affinity Text

Finite sets of bit positions can have several textual presentations. A dense binary mask and a
hexadecimal mask are common. An index list may instead enumerate the set bits or compress consecutive
indexes into ranges. CPU-affinity tooling demonstrates that these forms occur in real system input.

There is no universal representative across those forms. Sorting a list does not by itself establish
Canonicalization meaning. Compressing ranges or choosing a mask format has the same problem. CPU affinity remains a
useful stress case. An admitted law needs an exact domain profile. Its
representative must be owned by a relevant standard or independently justified by Kontrakt.

## 7.31. Bit Numbering and Bit Order

Bit numbering cannot be inferred from a byte sequence. APIs and protocols differ on whether bit zero is associated with
the least-significant or most-significant position of an octet.

A Built-In law must not select one convention from ambient platform behavior. This subject remains outside the current
candidate set until an exact domain presentation makes the numbering convention explicit. In many cases that convention
belongs to Input interpretation or Lowering rather than Canonicalization.

## 7.32. IPv6 Scoped Address and Zone Identifier

An IPv6 zone identifier can depend on host-local network state. An interface name and numeric interface
index may refer to the same zone only on a particular node. The active network namespace can change
that relationship. The running machine therefore cannot decide the law's equivalence relation.

The RFC 5952 candidate remains limited to the IPv6 address text itself. Scoped-address canonicalization is deferred
unless a future semantic subject supplies a stable explicit zone identity without ambient interface lookup.

## 7.33. Other Researched Domain Cases

The following subjects were investigated during candidate discovery. They are recorded here so that absence from the
current candidate table is deliberate rather than accidental.

| Subject                                       | Current disposition                         | Reason                                                                                                                                |
|-----------------------------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| ISBN and ISSN textual profiles                | Further research                            | Display punctuation and namespace-specific rules must be separated from identifier validity before one representative is proposed     |
| LDAP GeneralizedTime                          | Deferred                                    | RFC 4517 defines equality by the same UTC instant but explicitly does not define canonical LDAP encodings                             |
| OAuth scope sets                              | Aggregate stress case                       | Scope order is not semantic, but the protocol does not define one canonical sort order                                                |
| HTTP Structured Fields                        | Owner review                                | The standard mainly serializes an already parsed abstract field value, so protocol serialization may own the result                   |
| OCI digest text                               | Authority-uniqueness review                 | Much of the visible normalization may already be covered by exact digest grammar plus an existing Base16 law                          |
| ORCID presentation                            | Further research                            | The relationship between bare identifier text, grouped display text, and the recommended HTTPS URI needs one exact operand definition |
| CPE 2.3 names                                 | Owner review                                | The standard separates a semantic name model from string bindings, which may make binding a projection rather than Canonicalization   |
| Protobuf deterministic serialization          | Excluded as evidence of a different problem | Protobuf explicitly warns that deterministic serialization is not a canonical byte representation                                     |
| Binary32 / Binary64 hexadecimal lexical forms | Further research                            | Exact hexadecimal float text is viable, but V1 demand is lower than the decimal lexical candidates                                    |
| Delimited hexadecimal octet sequences         | Authority-uniqueness review                 | Separator removal may already be expressible by a narrower Base16 profile rather than a new Authority                                 |
| CIDR prefix versus dotted netmask             | Boundary / owner review                     | Prefix length is well-defined, but dotted-netmask syntax is not one universal canonical network-prefix presentation                   |
| IP socket-endpoint text                       | Authority-uniqueness review                 | Address and port syntax depend on the enclosing protocol; one universal endpoint textual Authority is not yet justified               |
| DNS name presentation                         | Boundary / composition review               | ASCII case may reuse an existing law, while a trailing dot can distinguish absolute from context-dependent relative interpretation    |
| Service name versus numeric port              | Outside generic Canonicalization            | Registry lookup or protocol defaulting is not a stable equivalence relation between two endpoint presentations                        |
| CRS WKT                                       | Deferred                                    | The standard defines the representation language but does not provide one universal canonical writer                                  |

---

# 8. Admission Decision Record

Candidate review records a proposed disposition before any historical Catalog membership decision.
The review uses the following outcomes:

```text
ADMIT (proposed)
    the candidate appears ready for formal admission, subject to
    completion and approval of its exact normative law specification

DEFER
    candidate may be valid but one or more admission questions remain unresolved

REJECT
    candidate does not satisfy the Canonicalization or Built-In Catalog boundary
    under the proposed semantic subject
```

A proposed `ADMIT` is not Catalog admission. Formal `ADMIT` requires a complete versioned normative
law specification and an explicit Catalog decision under ADR-0076. Specification approval and
Catalog admission may be recorded together if neither precedes the other.

The disposition must state the semantic reason. Implementation availability alone does not turn a semantically valid law
into a different law, and implementation registration does not turn a candidate into a member.

Formal `ADMIT` is a hard-gate result rather than a score. The Section 2 qualification record and the
exact normative law specification must be closed. Relevant security review and pre-admission evidence
must also be complete. A candidate remains `DEFER` while a material question about its semantics or
evolution is unresolved. The same applies to security, finite work, V1 feasibility, or required
evidence. `REJECT` means that the proposed semantic subject fails the governing boundary. The lack of
a convenient implementation alone does not justify rejection.

The formal `ADMIT` record identifies the independently owned Law Authority and the exact law
specification approved for admission. Once admitted, ADR-0076 treats Built-In Law Authority Membership
as historical and monotonic. A later lifecycle or Governance rule may restrict future use without
erasing or reassigning that membership.

A new exact Version under an already-admitted Authority does not create another Catalog member.

The review tables below use the same domain names as the candidate inventory. A candidate leaves
Section 4 when its review disposition is recorded here. This movement does not create Catalog
membership. Working notes in Sections 5–7 remain research evidence rather than current status
records; the tables below own the recorded review disposition.

## 8.1. ADMIT (Proposed)

A proposed `ADMIT` is a favorable review outcome, not a ratified Exact Built-In Law. Each entry
still requires its complete normative law specification and an explicit Catalog admission
under ADR-0076.

### Text and Unicode

| Review area           | Law working name                              | Review outcome   |
|-----------------------|-----------------------------------------------|------------------|
| Boundary text         | ASCII boundary whitespace trim                | ADMIT (proposed) |
| Boundary text         | ASCII leading whitespace trim                 | ADMIT (proposed) |
| Boundary text         | ASCII trailing whitespace trim                | ADMIT (proposed) |
| Boundary text         | Unicode boundary whitespace trim              | ADMIT (proposed) |
| Boundary text         | Unicode leading whitespace trim               | ADMIT (proposed) |
| Boundary text         | Unicode trailing whitespace trim              | ADMIT (proposed) |
| Case                  | ASCII lowercase representative                | ADMIT (proposed) |
| Case                  | ASCII uppercase representative                | ADMIT (proposed) |
| Case                  | Unicode default full case-fold representative | ADMIT (proposed) |
| Unicode normalization | Unicode NFC case-fold profile                 | ADMIT (proposed) |
| Unicode normalization | Unicode NFKC case-fold profile                | ADMIT (proposed) |
| Unicode normalization | Unicode NFC normalization                     | ADMIT (proposed) |
| Unicode normalization | Unicode NFD normalization                     | ADMIT (proposed) |
| Unicode normalization | Unicode NFKC normalization                    | ADMIT (proposed) |
| Unicode normalization | Unicode NFKD normalization                    | ADMIT (proposed) |
| Text representation   | WHATWG ASCII whitespace collapse              | ADMIT (proposed) |
| Text representation   | LF line-ending normalization                  | ADMIT (proposed) |
| Text representation   | Unicode decimal-digit fold                    | ADMIT (proposed) |

The ASCII trim reviews propose the six-character set from U+0009 through U+000D and U+0020,
not Java `trim()` behavior. The Unicode trim reviews propose the Unicode `White_Space` property
under a fixed Unicode semantic version. These selections must be closed in each exact normative
law specification before formal admission. Leading, trailing, and both-boundary forms remain
separate proposed Authorities pending that approval.

The ASCII case reviews use the 26 ASCII upper/lower letter pairs as one exact case-insensitive equivalence relation,
consistent with WHATWG Infra's ASCII case-insensitive matching. Their representatives differ: lowercase selects the
lowercase spelling, and uppercase selects the uppercase spelling. Both preserve non-ASCII scalars, require no Unicode
version or ambient locale, and have Total Coverage with length-preserving, idempotent representatives. The different
representatives justify reviewing two independent Law Authorities rather than treating uppercase as an alias for
lowercase. Final Authority uniqueness and exact-law approval remain necessary. Case-sensitive protocol fields and
identifier semantics must not acquire this equivalence without their own applicable contract declaration.

The full case-fold review proposes Unicode Default Full Case Folding rather than lowercasing.
The normalization reviews propose the four distinct Unicode normalization forms. Every
Unicode-dependent law requires its exact semantic Version before formal admission.

The NFC case-fold review proposes Unicode D145 Canonical Caseless Matching with one NFC representative. An explicit
composition may express similar meaning, but a frequently used standardized combination can justify a separately
versioned curated Built-In Law under ADR-0066. The NFKC case-fold review proposes Unicode D147 Identifier Caseless
Matching using `toNFKC_Casefold` after NFD. That law also removes distinctions involving compatibility characters and
default-ignorable code points. Neither proposed law validates identifier syntax or makes every identifier use safe.
Their precise Unicode Basis, representative stability, and security obligations remain subject to normative approval.

The WHATWG whitespace-collapse review proposes one law over exactly U+0009, U+000A, U+000C, U+000D, and U+0020.
It removes boundary runs and collapses interior runs to one U+0020. This is not the earlier Unicode-wide whitespace
proposal. The selected equivalence can erase protocol delimiters, so its use cannot substitute for protocol validation.

The LF review proposes CRLF and standalone CR as alternate spellings of LF. Other Unicode
line separators are preserved. The decimal-digit review proposes folding only Unicode `Nd`
characters to their matching ASCII digits under fixed Unicode data.

### Numeric and Numeric Text

| Review area  | Law working name                                   | Review outcome   |
|--------------|----------------------------------------------------|------------------|
| Numeric text | Decimal integer textual representative             | ADMIT (proposed) |
| Numeric text | Fixed-point decimal textual representative         | ADMIT (proposed) |
| Numeric text | ASCII grouped-decimal textual representative       | ADMIT (proposed) |
| Numeric text | Percentage / per-mille rate textual representative | ADMIT (proposed) |
| Numeric text | Scientific-notation textual representative         | ADMIT (proposed) |
| Numeric      | Decimal numeric-value representative               | ADMIT (proposed) |
| Numeric      | Binary32 canonical NaN                             | ADMIT (proposed) |
| Numeric      | Binary64 canonical NaN                             | ADMIT (proposed) |
| Numeric text | Binary32 decimal lexical representative            | ADMIT (proposed) |
| Numeric text | Binary64 decimal lexical representative            | ADMIT (proposed) |

The integer and fixed-point reviews propose exact ASCII numeric grammars. Their representatives
preserve the exact numeric value while the operand remains Text. The grouped-decimal review
removes valid ASCII grouping commas only. It does not remove fractional trailing zeros.

The rate review proposes an exact fractional value for `%` and `‰`, represented as ordinary
fixed-point Text. The scientific-notation review preserves scientific notation without
expanding large exponents into fixed-point output. These are review proposals; their complete
grammars and coverage rules still require normative approval before Catalog admission.

The Decimal numeric-value review proposes exact numeric-cohort equivalence over the already-established Decimal Leaf.
It selects the fully reduced coefficient/scale pair, with `(0,0)` for zero, and uses Restricted Coverage with an
explicit Canonicalization refusal if that representative lies outside the same closed Decimal domain. No rounding or
silent scale clipping is admitted. The Binary32 and Binary64 NaN reviews collapse only their respective NaN bit
patterns to `0x7fc00000` and `0x7ff8000000000000`; non-NaN bits, including signed zero, remain exact. Each supported
Input acquisition route must preserve the complete admitted bit datum before the selected Law runs.

The two decimal lexical reviews select the distinct W3C XML Schema 1.1 `floatLexicalMap` / `floatCanonicalMap` and
`doubleLexicalMap` / `doubleCanonicalMap` algorithm pairs as their proposed exact Text relations and representatives.
They retain signed-zero identity and one `NaN` text identity under those profiles. The Law Versions must specify the
exact grammar, rounding, representative coverage, and bounded realization, not borrow JVM formatter behavior. All five
reviews remain proposals until their exact normative specifications and admission evidence are approved.

### Radix and Bit Representation

| Review area    | Law working name                                            | Review outcome   |
|----------------|-------------------------------------------------------------|------------------|
| Radix integer  | Hexadecimal integer textual representative                  | ADMIT (proposed) |
| Radix integer  | Binary integer textual representative                       | ADMIT (proposed) |
| Radix integer  | Octal integer textual representative                        | ADMIT (proposed) |
| Bit vector     | Fixed-width hexadecimal bit-vector textual representative   | ADMIT (proposed) |
| Bit vector     | Fixed-width binary bit-vector textual representative        | ADMIT (proposed) |
| Binary integer | Minimal two's-complement signed-integer byte representative | ADMIT (proposed) |
| System bitmask | POSIX file-mode octal textual representative                | ADMIT (proposed) |

The radix integer reviews propose exact ASCII grammars without radix prefixes. Their representatives
remove redundant leading zeros and normalize the sign. The hexadecimal representative uses lowercase
digits. These laws preserve mathematical integer value, not bit-vector width.

The bit-vector reviews preserve every bit and the width of the represented sequence. They remove only
permitted separators; the hexadecimal form also selects lowercase digits. The signed-integer Bytes review
proposes the shortest nonempty big-endian two's-complement sequence. The POSIX mode review proposes a
12-bit mask with a four-digit octal representative. These choices require approval in the exact normative
law specifications before admission.

### Encoded Text

| Review area  | Law working name                                    | Review outcome   |
|--------------|-----------------------------------------------------|------------------|
| Encoded text | Base16 lowercase textual representative             | ADMIT (proposed) |
| Encoded text | Base16 uppercase textual representative             | ADMIT (proposed) |
| Encoded text | Base32 RFC 4648 canonical textual representative    | ADMIT (proposed) |
| Encoded text | Base32hex RFC 4648 canonical textual representative | ADMIT (proposed) |

The Base16 reviews preserve the octet sequence and select lowercase or uppercase digits respectively.
They propose an even number of hexadecimal digits without prefixes or separators. The Base32 and
Base32hex reviews propose uppercase representatives for permitted case variants. Their operand grammars
must enforce RFC 4648 padding and zero pad bits. Decoder permissiveness cannot expand those grammars.

### Web and HTTP

| Review area   | Law working name                                | Review outcome   |
|---------------|-------------------------------------------------|------------------|
| URI component | RFC 3986 percent-encoding syntax representative | ADMIT (proposed) |
| HTTP / Time   | HTTP-date textual representative                | ADMIT (proposed) |
| HTTP          | HTTP media-type textual representative          | ADMIT (proposed) |
| HTTP          | HTTP qvalue textual representative              | ADMIT (proposed) |
| URI / HTTP    | HTTP(S) URI normal-form representative          | ADMIT (proposed) |

The percent-encoding review limits equivalence to hexadecimal case in triplets and direct presentation of RFC 3986
unreserved characters. It requires an explicit URI-component operand and excludes reserved-character decoding and
implicit later reinterpretation. The HTTP-date review selects RFC 9110's IMF-fixdate representative with an explicit
UTC Required Basis for RFC 850 two-digit-year interpretation.

The media-type review is a restricted common-syntax profile. It canonicalizes ASCII-insensitive names, parameter order,
and legally equivalent token/quoted presentations without folding registration-specific parameter values. The HTTP (S)
URI review proposes a strict ASCII-host, context-limited scheme-based representative, not generalized resource identity;
userinfo, unclosed host profiles, implicit OPTIONS request-target rules, and origin-specific query equivalence remain
outside it. Each Law still requires exact grammar, coverage, and security closure before formal admission.

The qvalue review proposes exact RFC 9110 syntax and a minimal decimal representative for the same thousandth-based
value. Out-of-grammar Text is not repaired.

### Network and IoT

| Review area           | Law working name                                   | Review outcome   |
|-----------------------|----------------------------------------------------|------------------|
| Protocol / Network    | POSIX IPv4 numbers-and-dots textual representative | ADMIT (proposed) |
| Protocol / Network    | RFC 9911 MAC-48 lowercase textual representative   | ADMIT (proposed) |
| Protocol / Network    | IPv4 network-prefix representative                 | ADMIT (proposed) |
| Protocol / Identifier | IPv6 RFC 5952 all-hex textual representative       | ADMIT (proposed) |
| Protocol / Network    | IPv6 network-prefix representative                 | ADMIT (proposed) |
| URI / IoT             | CoAP URI normal-form representative                | ADMIT (proposed) |

The POSIX IPv4 review maps its expressly permitted legacy spellings to four decimal octets. The MAC-48 review selects
RFC 9911 lowercase colon notation. The IPv4 prefix review clears host bits without changing the prefix length.

The IPv6 address review selects the RFC 5952 Section 4 all-hexadecimal representative for the same 128-bit address,
including legal IPv4-embedded inputs without selecting a deployment-specific mixed form. The IPv6 prefix review clears
non-prefix bits and retains the exact prefix length. CoAP uses its own RFC 7252 scheme defaults and an explicitly
restricted host and URI-component interpretation, independent of the HTTP (S) URI Authority.

### Identifiers and Internationalization

| Review area              | Law working name                                   | Review outcome   |
|--------------------------|----------------------------------------------------|------------------|
| Protocol / Identifier    | BCP 47 registry-independent casing representative  | ADMIT (proposed) |
| Protocol / Identifier    | BCP 47 registry-dependent canonical representative | ADMIT (proposed) |
| URI / Identifier         | Generic URN assigned-name textual representative   | ADMIT (proposed) |
| Protocol / Identifier    | UUID RFC 9562 lowercase textual representative     | ADMIT (proposed) |
| URI / Telephony          | RFC 3966 `tel:` URI representative                 | ADMIT (proposed) |
| International identifier | DOI textual representative                         | ADMIT (proposed) |

BCP 47 casing is registry-independent. UUID and DOI reviews preserve their identifier meaning while selecting lowercase
ASCII spelling. The `tel:` review follows RFC 3966 comparison rules; its complete parameter treatment still requires
normative closure.

The registry-dependent BCP 47 review uses one immutable IANA Language Subtag Registry snapshot per Exact Law Version
and does not normalize extension internals without their own authority. The RFC 8141 URN review concerns assigned-name
Text only; optional resolver, query, and fragment components are outside its operand.

### Temporal Standards

| Review area           | Law working name                                       | Review outcome   |
|-----------------------|--------------------------------------------------------|------------------|
| Temporal / XML Schema | XML Schema `date` canonical lexical representative     | ADMIT (proposed) |
| Temporal / XML Schema | XML Schema `time` canonical lexical representative     | ADMIT (proposed) |
| Temporal / XML Schema | XML Schema `dateTime` canonical lexical representative | ADMIT (proposed) |

The three reviews propose XML Schema 1.1 canonical lexical mappings. Their equivalence follows datatype value identity,
not an unconditional conversion to one UTC instant. The absence of a timezone remains meaningful.

### Financial and Retail Identifiers

| Review area          | Law working name                       | Review outcome   |
|----------------------|----------------------------------------|------------------|
| Retail identifier    | GTIN 14-digit representative           | ADMIT (proposed) |
| Financial identifier | IBAN electronic textual representative | ADMIT (proposed) |

The GTIN review proposes a 14-digit zero-padded Text representative. It does not repair check digits or decide whether
an identifier has been allocated.

The IBAN review removes only correctly positioned paper-format U+0020 grouping spaces to select electronic Text.
It preserves account-character case and does not perform BBAN registry, check-digit, or account-validity judgment.

### Software Supply Chain

| Review area           | Law working name                         | Review outcome   |
|-----------------------|------------------------------------------|------------------|
| Software supply chain | ECMA-427 core PURL syntax representative | ADMIT (proposed) |

The PURL review owns only ECMA-427's common syntactic equivalences. Package-type-specific name, namespace, and
version normalization requires separately qualified meaning and cannot be inferred from a current package registry.

### Cloud-Native

| Review area          | Law working name                                     | Review outcome   |
|----------------------|------------------------------------------------------|------------------|
| Cloud-native numeric | Kubernetes Quantity canonical textual representative | ADMIT (proposed) |

The Kubernetes Quantity review selects the versioned, source-verified Kubernetes parse-and-canonicalize semantics,
including suffix-family retention and specified rounding. It does not replace the Kubernetes canonical output with
Kontrakt's own exact-value DecimalSI encoding. Kubernetes release compatibility is verified against the frozen semantic
profile; a different release number alone does not require a distinct Law Version.

### Geographic

| Review area    | Law working name                               | Review outcome   |
|----------------|------------------------------------------------|------------------|
| Geographic URI | RFC 5870 WGS-84 core `geo:` URI representative | ADMIT (proposed) |

The geographic review adopts RFC 5870 coordinate equivalence and fixes a representative for poles, the antimeridian,
and the default CRS while preserving optional altitude and uncertainty distinctions. Extensions are outside V1 coverage.

### Security / PKI Text

| Review area               | Law working name                                | Review outcome   |
|---------------------------|-------------------------------------------------|------------------|
| Security textual encoding | RFC 7468 single-instance textual representative | ADMIT (proposed) |

The RFC 7468 review represents one Label and byte payload in a fixed strict-generator text layout. It does not accept
arbitrary permissive-PEM repairs, alter the decoded security object, or order multiple encapsulations.

### Schema and Structured Data

| Review area   | Law working name            | Review outcome   |
|---------------|-----------------------------|------------------|
| Schema / Data | Avro Parsing Canonical Form | ADMIT (proposed) |

The Avro review uses the precise Parsing Canonical Form in one frozen Avro specification version. It is not an
assertion of whole-schema equivalence and does not replace exact canonical-content comparison with a hash alone.

## 8.2. DEFER

No candidates in the initial 75-candidate inventory remain `DEFER` after qualification review. This does not remove
`DEFER` as a possible disposition for a future candidate whose required semantic or security evidence remains open.

## 8.3. REJECT

Rejection concerns the proposed independent law, not necessarily every possible authoring alias.

### Network and IoT

| Review area        | Law working name                                    | Review outcome | Reason for rejection                                                                                                                                                        |
|--------------------|-----------------------------------------------------|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Protocol / Network | EUI-48 / MAC-48 multi-format textual representative | REJECT         | The broad candidate has no one fixed standard for all accepted source forms and octet interpretations, and its independent Authority boundary against RFC 9911 is unproved. |

This rejection leaves the standards-backed RFC 9911 MAC-48 Law intact. Explicitly scoped additional source-format
conversions may be reviewed independently; common vendor usage does not by itself establish a universal equivalence.

### Text and Unicode

| Review area | Law working name                         | Review outcome | Reason for rejection                                                                                                                                                    |
|-------------|------------------------------------------|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case        | ASCII case-fold representative           | REJECT         | Its exact ASCII equivalence relation and lowercase representative duplicate the proposed ASCII lowercase law. An alias may reuse that Authority.                        |
| Case        | Unicode default lowercase representative | REJECT         | Default lowercase is case conversion, not a representative of Unicode Default Caseless Matching. No separate general-text equivalence is justified.                     |
| Case        | Unicode default uppercase representative | REJECT         | Default uppercase can erase distinctions retained by Unicode Default Caseless Matching. An independent general-text relation is unjustified.                            |
| Text        | Unicode CJK width fold                   | REJECT         | The broad profile has no one exact width-equivalence rule; multi-scalar composition and unrelated NFC effects remain unresolved. Narrower laws need separate review.    |
| Text        | Unicode diacritic fold                   | REJECT         | No general Unicode accent-removal equivalence exists. Broad removal can erase language- or security-significant distinctions. A narrower profile needs separate review. |

The Unicode default lowercase and uppercase operations are standardized and useful outside this admission decision.
Rejecting these proposed Built-In Authorities does not reject their algorithms or prevent a future narrowly scoped law.
The Catalog does not derive Contract equivalence merely from equality of conversion output. The Unicode Default Full
Case Folding candidate remains a separate proposed law for its explicitly defined caseless-matching purpose.

The CJK width-fold rejection concerns the unresolved broad subject, not the possibility of separately qualified
full-width ASCII or half-width Katakana laws. The diacritic-fold rejection likewise concerns the proposed generic
same-meaning relation, not accent-sensitive search or narrower standards-backed comparisons. Neither rejection means
that these transformations are inherently nondeterministic; a fixed implementation cannot replace missing Contract
meaning or justify unsafe equivalence.

### Numeric and Numeric Text

| Review area  | Law working name                             | Review outcome | Reason for rejection                                                                                                                                                                                    |
|--------------|----------------------------------------------|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Numeric text | Decimal-comma grouped textual representative | REJECT         | Changing a comma decimal separator to `.` creates a Text that can denote a different number under the same grouping/decimal grammar. The proposed same-domain representative does not preserve meaning. |

This rejection applies to the original comma-to-dot representative, not to all decimal-comma processing. A separately
qualified Law could remove only valid grouping periods while retaining the comma decimal separator. That narrower
relation must receive its own exact grammar, equivalence, security review, and Catalog disposition before use.

### Encoded Text

| Review area  | Law working name                                    | Review outcome | Reason for rejection                                                                                                                                                                                     |
|--------------|-----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Encoded text | Base64 RFC 4648 canonical textual representative    | REJECT         | The strict RFC 4648 grammar already has one representation per octet sequence. Adding permissive decoder spellings lacks one generic normative equivalence and may erase security-relevant distinctions. |
| Encoded text | Base64url RFC 4648 canonical textual representative | REJECT         | Padding choices are owned by referring protocols. Strict individual profiles already have singular representations, while generic padded/unpadded equivalence is not universally authorized.             |

These rejections concern the proposed generic independent Canonicalization Authorities, not Base64 codecs or
protocol-specific text profiles. A future MIME or other standards-backed profile must independently define the legal
operand spellings and canonical representative. Neither RFC 4648 decoder latitude nor a provider's fallback rule can
supply that meaning. In particular, signature-bearing Base64url text must retain the spelling mandated by its protocol.

---

# 9. API and Compiler Design Follow-On

Concrete API design begins only from admitted or deliberately prototyped candidate meaning. Follow-on Design has four
main concerns:

```text
public authoring spelling and generated projection
exact Authority and Version reference projection
compiler lookup and physical representation
optimized realization and reuse
```

None of these artifacts owns Catalog membership or exact law meaning.

Source aliases must resolve to the same exact Authority rather than mint duplicate Catalog members. Similarity cannot
infer aliasing. Equal structure or shared implementation code, for example, is insufficient.

Compiler realization may physically fuse adjacent lookup and evaluation work when profitable. That optimization must
still preserve the logical Authority and Version boundary. Contract-visible observations must remain recoverable.

---

# 10. Qualification Evidence and Verification Follow-On

Admission must not precede the evidence needed to show that the proposed law is semantically closed and independently
checkable. Security-relevant representation ambiguity must also be examined before admission. When V1 feasibility is in
doubt, qualification evidence must expose that risk rather than hide it.

That evidence should cover the questions that can invalidate admission:

```text
normative and conformance evidence
adversarial or differential evidence where interpretation can diverge
representative-domain closure
resource-bound evidence when the law can amplify or traverse input substantially
```

Pre-admission evidence must expose missing semantics before a historical Catalog membership decision is made. It should
also expose security or V1 feasibility risk. This evidence does not replace the exact semantic definition. If the law is
valid but V1 realization risk remains too high, the candidate may remain `DEFER` without weakening its meaning.

After admission, implementation verification can become more realization-specific:

```text
property and differential tests
hostile-input regression tests
backend preservation checks
performance evidence
recompute-equivalence checks when results are reused
```

These artifacts can reject an implementation or show that a realization is impractical for the intended support
surface. They cannot redefine the admitted law. In particular, they cannot change `E_L` or `C_L`.

Performance engineering belongs to Design or Verification unless a Contract independently makes a distinction visible.
SIMD or caching, for example, do not become Built-In Law meaning merely because an implementation uses them.

---

# 11. Working Sequence

The concrete Catalog work proceeds in this order. Candidate research includes a domain sweep before detailed
qualification so that one familiar ecosystem does not dominate the final population.

```text
candidate research
    ↓
exact semantic qualification
    ↓
pre-admission evidence review
    ↓
Built-In suitability review
    ↓
Authority uniqueness review
    ↓
proposed disposition from candidate review
    ↓
complete and approve exact normative law specification
    ↓
explicit Catalog admission decision (for ADMIT)
    ↓
public API projection design
    ↓
compiler realization
    ↓
post-admission realization verification
```

A `DEFER` or `REJECT` review ends with its recorded reason. Only a proposed `ADMIT` proceeds to
exact normative specification and a possible formal Catalog admission decision.

Pre-admission evidence does not require the production implementation to exist first. It must prevent membership from
relying on unresolved semantics or ambient provider behavior. It must also expose representation ambiguity before an
implementation assumption can harden into Contract meaning.

Concrete qualification is reviewed in small fixed batches so that each candidate receives equivalent
scrutiny. Sources must support the exact meaning. Security risks and verification evidence must also be
reviewed. The current batch size is five candidates. A proposed `ADMIT` from a batch does not enter
the Catalog until its normative specification and formal admission decision are complete. Batch size
changes only the workflow, not the admission law. The first batch may calibrate the record format,
but later domains must not receive weaker gates.

Research may expose a missing common Catalog rule. The work returns to ADR-0076 only when that common law must change. A
change to membership semantics or Version ownership is one example. Candidate-specific detail stays in this Design.

Ordinary candidate-specific meaning does not reopen ADR-0076.