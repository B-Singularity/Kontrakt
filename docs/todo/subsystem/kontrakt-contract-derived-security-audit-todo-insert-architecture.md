## Optional Contract-Derived Security Audit

Kontrakt should provide an **opt-in Security Audit subsystem**, invoked separately in the same manner as contract-driven
test generation. The audit examines whether **individually legal Contract declarations can produce security-relevant
risks when their meanings are connected across the pipeline**. Its first capability is declaration-derived analysis, not
a general vulnerability scanner or a second mandatory verifier.

The audit must not require a new security Contract or a separately authored threat model to run. It uses the
distinctions, relationships, and disclosure rules that users have already declared. When the declarations do not
establish whether a distinction is security-sensitive, the audit may identify a concrete risk but must not claim that an
exploit or authorization violation has been proven.

### Architectural position

The audit is an independent **read-only compiler analysis consumer**. It observes existing semantic products through
their lawful producer-owned protocols and does not participate in Contract Establishment or normal machine execution.

```text
Declared Contract definitions
             |
             v
      Resolved Contract HIR
             |
             v
  Authority-Owned Establishment
             |
             v
    Canonical Contract World ------------------------+
             |                                       |
             | normal compiler consumers            |
             v                                       |
  Realization / Execution Formation                  |
             |                                       |
             v                                       |
      Generated JVM Product                          |
                                                     |
Resolved HIR projections ----------------------------+
                                                     |
CRI relation observations, where applicable ---------+
                                                     |
                                                     v
                                 Producer-Owned Semantic Protocols
                                                     |
                                                     v
                                  ADR-0075 Legal Consumption
                                                     |
                                             opt-in  v
                                 Contract-Derived Security Audit
                                   | relation-path examination
                                   | distinction-loss examination
                                   | disclosure and scope examination
                                                     |
                                                     v
                                  Audit Findings + Coverage Result
                                                     |
                                                     v
                                       Compiler Diagnostics / Report
```

The ordinary compiler path continues independently of the audit. The diagram describes legal information flow, not a
required physical pass order, a new material container, or a second Contract graph. CRI may supply qualified relation
observations when its proposed boundary is available; the audit must not depend on CRI becoming an independent source of
Contract authority.

Mandatory Contract, IR, and security-boundary verification remain on the ordinary compiler path. The optional audit
looks for **risks in what was declared**, including combinations that are legal under individual Contract laws but may
be dangerous for the user's intended domain.

### Declared pipeline under review

The first audit should follow declared relationships across the inbound and outward Contract boundaries:

```text
Input Presentation
      |
      v
Optional Canonicalization -- selected coordinates only
      |                       unselected coordinates retain Input meaning
      v
Admission of the complete Boundary Presentation
      |
      v
Lowering --> complete Fact Candidate
                        |
                        v
             Fact / applicable Invariant judgments
                        |
                        v
                 Fact Establishment
                        |
                        v
                 User Operation
                        |
                        v
           Result Candidate / owned judgments
                        |
                        v
                 Core Exit Material
                        |
                        v
                Publication --> Output
```

This is a **semantic analysis path**, not a claim that every Contract has the same Required Basis or that all runtime
judgments are statically available. The audit must follow exact, declared source-to-target bindings and the lawful
observation of their owning producers. It must not reconstruct missing relations from names, object layout, generated
classes, or query dependencies.

### Initial analysis families

- **Canonicalization and downstream Fact distinctions.** Identify a selected equivalence law that can merge different
  Input presentations when its representative is used by a declared Lowering relation to form Fact material. Explain
  whether the collapsed distinction can affect a Fact coordinate or whole-Fact sameness; do not treat one matching
  coordinate as proof that two complete Facts are identical.
- **Judgment–use coherence.** Inspect declared Input, Canonicalization, Admission, and Lowering relations for
  disagreements about which complete Boundary Presentation is judged and subsequently used. If the discrepancy violates
  an existing mandatory law, it remains a compiler error rather than an optional finding.
- **Required Basis, identity, and scope assumptions.** Look for declaration-supported risks in how source occurrences,
  Versions, Contract Worlds, applicability contexts, and Governance scopes are related. A legally invalid binding or
  missing completeness proof belongs to ordinary Establishment/Verification; the audit reports only additional risks or
  gaps supported by the declared meanings.
- **Invariant and Fact coverage.** Check whether a declared downstream invariant or other judgment is being relied upon
  for a distinction that an earlier selected transformation has removed. Do not invent an invariant or infer the user's
  intended protection requirement when no such requirement is declared.
- **Publication, Output, and Failure observations.** Compare declared outward projections and observable failure
  distinctions with any relevant protection or separation meaning that is actually declared. A projection is not a
  confidentiality violation merely because it exposes data; the audit must identify the declared basis for treating that
  disclosure as risky.

For example:

```text
Input.username
    |
    +-- ASCII lowercase Canonicalization
    |              "Admin" / "admin" -> "admin"
    v
Admission --> Lowering: username -> AccountFact.username
                                      |
                                      v
                          Fact candidate / sameness
```

The audit can establish that the selected law removes case distinctions and that Lowering carries the representative
into the named Fact coordinate. It can therefore warn about **potential distinction loss affecting Fact formation**. It
cannot infer from the names `username` or `AccountFact` that the coordinate is an authentication principal, or claim an
account takeover without a declared basis. If the complete Fact includes an independently distinguishing coordinate, the
warning must not assert whole-Fact identity collision.

### Findings and assurance boundary

Each finding should identify the owning declarations, the exact relation path, the relevant Contract law or projection,
a small counterexample when justified, and the difference between known evidence and missing security intent. The result
must distinguish:

```text
Mandatory violation       -> normal compiler rejection / verification
Supported security risk   -> optional audit warning with declared evidence
Undetermined significance -> review note identifying the missing meaning
Incomplete / unsupported  -> explicit coverage limitation
```

The first category is not a new optional failure mode: a proven violation of an Accepted law must not become acceptable
when the audit is disabled. An audit result is **compiler-owned analysis material**, not Established Fact, Contract
Failure, or permission to publish otherwise private information.

A clean audit means only that **no finding was produced within the stated coverage and assumptions**. It is not proof
that the system is secure. Diagnostics must not expose private Fact values or producer-private evidence just to make a
warning more detailed.

### Required subsystem properties

- Audit on/off must not change Contract meaning, Establishment, normal compiler rejection, the generated JVM product, or
  machine behavior. Only the separate audit result and its reporting may differ.
- The audit consumes existing HIR, Established Material, and narrowly exposed relation projections. It must not create a
  parallel authoritative World, change source bindings, or assign new Contract meaning.
- Audit results require exact source attribution and producer-owned validity conditions. Reuse must not turn stale
  declarations, changed source domains, or obsolete relation evidence into current findings.
- For the same declared semantic basis, audit rules, and tool version, equivalent findings must not depend on traversal
  order, worker scheduling, or cache hits.
- Analysis, counterexample generation, and diagnostic output must have explicit resource bounds. Exhaustion or
  cancellation reports incomplete coverage rather than success.
- V1 may use existing frozen/compiler-result and query infrastructure; V2 may use incremental analysis. Neither choice
  should be required by this TODO or become Contract authority.

### Work to close

- [ ] Identify the exact declaration-derived observations the audit may consume from HIR, Establishment, and the
  applicable producer-owned protocols. Review CRI integration without duplicating ADR-0075 mediation.
- [ ] Define the first relation-path analyses and the conditions under which a cross-stage distinction is relevant to
  Fact formation, later judgment, or outward projection.
- [ ] Separate mandatory compiler errors from optional risk warnings, undetermined significance, and incomplete audit
  coverage. Specify the minimum evidence for each classification.
- [ ] Define audit-result ownership, input determinants, validity, provenance, and deterministic reporting within the
  existing compiler-product and diagnostic architecture.
- [ ] Set bounded execution and output limits; decide what can be summarized without losing the evidence needed to
  explain a finding.
- [ ] Test intentional and unintentional Canonicalization-to-Fact mappings, both selected and unselected coordinates,
  distinct full-Fact identities, and lawful versus unsafe outward projections.
- [ ] Verify that disabled, enabled, clean, reused, and parallel audit runs leave normal compilation unchanged and
  produce equivalent audit findings within the same stated coverage.

The initial audit is limited to **security risks supported by declared Contract meaning and declared pipeline
relations**. It does not analyze arbitrary user-code effects, undisclosed external authorization policies, JVM escape
paths, exploitability, or timing side channels. Those require separate explicit analysis inputs or other security
subsystems and must not be claimed as covered by this feature.