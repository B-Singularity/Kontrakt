# ADR-0084: One-Dimensional Standard-Library Semantic Acquisition and Interpretation Boundary

## Status

Accepted

## Date

2026-10-31

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend
- ADR-0047: One-Dimensional Contract Presentations and Source Carrier Law, especially §7–8
- ADR-0063: Contract Establishment and Applicability
- ADR-0065: Admission Contract, especially §6.3–6.5 and §7
- ADR-0071: Resolved Contract HIR
- ADR-0073: JVM Platform-Native Surface, especially §7, §9 and §13–15
- ADR-0075: Compiler-Produced Material and Reuse
- ADR-0080: Characters, Strings, and Unicode (*Draft*)
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md` (*Working Design*)
- `docs/design/character-text/kontrakt-character-text-sdk-interpretation-and-calculation-design.md` (*Working Design*)
- `docs/todo/realization/kontrakt-contract-aware-whole-machine-compilation-research-todo.md` (*Research TODO*)

---

## 1. Context

Java and Kotlin are the authoring languages for selected one-dimensional Contracts. Their declarations remain source
carriers rather than Contract authorities (ADR-0047 §7). But authoring must also be practical. Requiring users to avoid
ordinary Java and Kotlin standard-library operations would make familiar rules needlessly difficult to express. Kontrakt
therefore allows supported standard-library operations in 1D authoring.

That convenience requires Kontrakt to interpret what the author actually wrote. A library call must retain the meaning
of the selected operation under the applicable Java or Kotlin profile. For example, an Admission expression that uses
Java `String.compareTo` cannot be interpreted using a different ordering chosen by Kontrakt. The frontend must obtain
enough information about the operation to represent its exact calculation in the Contract candidate.

The existing pipeline already determines what happens after that meaning is formed. The frontend produces Resolved
Contract HIR, and the owning 1D Contract judges the candidate during Establishment. The unresolved issue is **how the
frontend obtains the standard-library calculation knowledge it needs at compilation time**: by looking up meaning
prepared and maintained within Kontrakt, or by deriving that meaning from the host compiler and library on demand. This
ADR settles that acquisition choice without changing the established judgment process.

## 2. Problem

Java and Kotlin compilers can resolve source calls, but a resolved method reference alone does not describe all behavior
needed by a one-dimensional judgment. The required knowledge may include comparison rules, callback invocation,
ordering, exceptional behavior, and dependence on the runtime environment.

Reading a library body is not by itself a proof of those properties. Executing the method with sample values gives
results for those values, not the meaning of the operation over its applicable domain.

If Kontrakt treats incomplete external information as complete, it can establish a judgment that its generated execution
does not preserve. Conversely, requiring every supported library implementation to be reinterpreted during every user
compilation creates avoidable work and ties candidate formation to toolchain internals.

## 3. Decision

**Kontrakt will use prequalified, Kontrakt-owned standard-library semantic knowledge as the normal source of
one-dimensional computation interpretation.**

For a selected and resolved Java or Kotlin operation, the frontend obtains its previously qualified meaning from
compiler-owned knowledge associated with the applicable source and platform profile. It uses that meaning to form the
required Kontrakt IR candidate. The owning 1D Contract then decides whether that complete candidate is lawful under its
existing rules.

The installed JDK, Kotlin standard library, `javac`, and K2 may supply evidence when this knowledge is prepared,
audited, or checked. Their on-demand output is not a substitute for a qualified interpretation during an ordinary
Contract compilation.

The catalog, query, table, or other storage used to retain this knowledge is a replaceable compiler implementation. Its
contents do not create a new Contract authority. ADR-0073 §9 already governs platform ratification and the **Semantic
Operation Profile**; this ADR decides how sufficiently qualified operation meaning is made available to 1D candidate
formation without reinterpreting the platform at every use.

### 3.1. Exact operation and sufficient meaning

Interpretation begins with the operation actually selected by the Java or Kotlin source language, not its short name or
JVM descriptor alone. Source-level overloads, receivers, language mappings, and relevant type substitutions must be
accounted for. ADR-0073 §9.1–9.5 governs the platform surface and its ratification.

A qualified interpretation must contain enough meaning for the particular 1D computation to be resolved and checked. It
must preserve every distinction that can affect that judgment. A general platform summary is sufficient only when it
supplies those facts. It cannot stand in for an unavailable calculation law.

For example, applying `compareTo` inside a collection judgment requires the source comparison law. A sort additionally
needs the semantics of the selected sort operation and its comparator. When the comparator is an authored lambda,
Kontrakt must resolve that closed body as part of the judgment. A known standard-library method does not make an unknown
callback lawful.

The same library operation may be used in several 1D roles. Its platform meaning is not rewritten separately for each
role. Its eligibility is decided for the actual use by the owning 1D law, including Admission §7 where applicable.

### 3.2. Normal compilation path

```text
selected Java/Kotlin 1D source
    -> resolve exact source operation
    -> obtain qualified platform meaning for its profile
    -> compose the authored computation in Kontrakt IR
    -> complete compiler-side semantic qualification
    -> publish a valid Resolved Contract HIR candidate
    -> owning 1D Establishment judgment
```

This is a logical responsibility flow, not a required sequence of physical passes or separate stored IRs.

The HIR contains the meaning required by its existing candidate protocol. It does not become a copy of JDK source,
Kotlin FIR, a library bytecode graph, or the internal catalog. ADR-0071 governs its completeness and visibility.

### 3.3. Evidence and qualification

A library interpretation must be qualified against an identified semantic basis before it can support candidate
formation. Its evidence must be sufficient to establish the claimed behavior for the supported domain, not merely a
collection of passing examples.

The qualification work may inspect normative specifications, source, bytecode, compiler output, and actual reference
executions. It may use verified summaries or translations. These are methods for producing and checking compiler
knowledge; none automatically makes a method eligible in a 1D Contract.

At the point of use, Kontrakt verifies that the interpretation applies to the resolved operation, the current semantic
basis, and the operands or callbacks being composed. A model valid only under a precondition cannot be used when that
precondition has not been established. Version-sensitive behavior cannot silently inherit a result from a different
profile.

The actual evidence format and conformance procedures belong to Design. ADR-0073 governs ratification and platform-basis
compatibility; ADR-0075 governs knowledge validity and reuse.

### 3.4. Incomplete meaning and judgment outcomes

A recognized standard-library call is not automatically admissible. If its meaning cannot be resolved sufficiently, the
frontend must not publish a complete HIR candidate on that basis.

An unresolved or unsupported interpretation is a compiler-side unsuccessful result. It is not evidence of a Contract-law
violation. A complete candidate that violates its owning 1D law follows that Contract's refusal rules. ADR-0065 §6.5
supplies the corresponding distinction for Admission.

Kontrakt must not infer a successful calculation from a familiar method name, a platform namespace, a valid JVM call, or
a favorable test result. It must not use an unqualified external execution as a fallback for missing meaning.

### 3.5. Execution is a separate decision

This ADR governs the acquisition of meaning before Establishment. It does not require Kontrakt to reimplement each
library algorithm in generated code.

After a lawful judgment has been established, the realization may preserve an eligible platform call under ADR-0073 §9.6
or use a Kontrakt-owned transformation whose meaning is proven equivalent. The chosen realization must preserve the
established judgment under its applicable platform basis. Platform-basis drift follows ADR-0073 §7.4.

An actual Admission occurrence may still return `Admitted` or `Rejected` depending on its input. Definition
Establishment fixes the judgment law, not the outcome for inputs that have not yet arrived (ADR-0065 §6.5).

## 4. V1 Scope and Evolution

V1's default interpretation inventory is limited to the standard libraries supplied by its supported Java and Kotlin
platform profiles. It does not promise that every operation in those libraries is eligible in every 1D Contract. The
required recognition coverage and actual admissibility remain different questions.

Third-party dependencies are not part of this V1 default. Their future support requires an explicit support decision and
suitable qualification. This does not amend ADR-0073's existing platform boundary or create a general ban on external
technology.

The interpretation knowledge may be prepared before a user's build and reused during compilation. V1 may use generated
tables, compact records, or queries. V2 may use persistent or incremental repair without changing the source of
interpretation or the ownership of Contract judgment. ADR-0075 governs those reuse boundaries.

## 5. Boundaries Not Decided Here

This decision does **not** select a Java/Kotlin source parser. Kontrakt-owned parsing and host-compiler-assisted name or
call resolution remain acquisition alternatives. Either may identify the exact operation that is subsequently
interpreted using the qualified knowledge selected by this ADR. The `.kontrakt` IDL parser remains governed by ADR-0046.

This ADR also does not choose a physical catalog schema, a separate IR level, the method for proving library models, the
final JVM implementation of 1D calculations, or the Operation Realization acquisition path. The Architecture Map and the
applicable Design documents retain those implementation questions.

Character/Text interpretation details remain in their owning documents. In particular, ADR-0080 is still Draft; this ADR
does not approve its proposed Character or Unicode laws.

## 6. Alternatives Considered

| Approach                                                                              | Decision reason                                                                                                                                                |
|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Derive library meaning from the installed compiler or library during each compilation | Not the normal path. Call resolution, library-body access, and sample execution do not by themselves supply the complete properties a 1D judgment may require. |
| Maintain qualified semantic knowledge before user compilation                         | **Selected.** The compiler can check applicability and form IR from an explicit, reusable meaning basis.                                                       |
| Execute the authored 1D code to discover its meaning                                  | Rejected by existing Admission law (ADR-0065 §7.6). Runtime execution is not a replacement for Definition interpretation.                                      |

An external analysis may still be used to build, refresh, or challenge a qualified interpretation. If it discovers a
mismatch, the affected knowledge must be invalidated and requalified before use; the mismatch must not be hidden by
selecting whichever result makes the user source pass.

## 7. Consequences

Kontrakt takes responsibility for preparing and maintaining the standard-library knowledge needed by its declared 1D
support. This increases release-time qualification work but avoids treating live toolchain analysis as the source of a
Contract candidate's calculation law.

Support claims must be tied to the exact operations and semantic bases actually qualified. When knowledge is
insufficient, compilation cannot advance by guessing. The benefit is a stable interpretation boundary: Java/Kotlin
authoring remains natural while HIR, Establishment, and later realization keep their existing responsibilities.

This ADR may become Accepted only after its scope has been checked against ADR-0073's ratification model, ADR-0065's
closed-computation rules, and the current Character/Text Design. Acceptance does not certify that V1's library inventory
or conformance tests have already been implemented.