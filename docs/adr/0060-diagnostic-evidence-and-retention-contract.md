# ADR-0060: Diagnostic Evidence and Retention Contract

## Status

Proposed

## Date

2026-08-23

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
- ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis

---

## 1. Context

Kontrakt already requires the Contract Machine to state its authoritative results explicitly. ADR-0057 applies that rule
to Failure and deliberately removes diagnostic material from Failure identity. Failure states what required machine
meaning was not satisfied. Diagnostic material may explain that result, but it must not become a second authority that
decides what the Failure meant.

The same separation is required outside Failure. A successful judgment may need explanation, a State-Machine refusal may
need supporting material, and a Realization Failure may need evidence from the physical execution boundary. These cases
do not share one result kind, but they all create a need to relate explanation material to an exact diagnosable
occurrence without transferring authority to the diagnostic system.

Kontrakt has three machine axes that matter here: Contract, State Machine, and Realization. Diagnostic Evidence may
relate to any of them, but this ADR defines only the Contract meaning of Diagnostic Evidence and Retention. It does not
define how the Kontrakt compiler diagnoses source programs or how a generated backend captures JVM, operating-system,
hardware, profiler, tracing, or crash material. Those implementation responsibilities are separated into ADR-0061 and
ADR-0062.

A rich Contract creates an unusual opportunity. The machine already knows the exact authority, applicable world,
selected Version, canonical material, and judgment that produced a result. Diagnostic Evidence therefore does not need
to reconstruct Contract meaning from logs or stack traces. It can be bound directly to the semantic occurrence that it
explains.

That strength also creates cost. Declared evidence may require values to be frozen at the judgment boundary and retained
after the source result is established. The Contract must say what is guaranteed without turning optional observability
into mandatory machine work. Required evidence and optional diagnostic depth therefore have to remain distinct.

## 2. Scope and Authority

ADR-0060 owns the meaning of a Diagnostic Evidence definition, the occurrence established from that definition, the
relation between evidence and the source occurrence, and the meaning of an explicit Retention guarantee. It also owns
the user-facing Contract distinction between required evidence and optional diagnostic depth.

The Kontrakt compiler may use the Contract definitions in this ADR while checking a program, but compiler errors and
warnings are tool diagnostics rather than occurrences of the user's Diagnostic Evidence Contract. ADR-0061 owns that
tool architecture.

A generated Contract Machine may realize required evidence with backend-specific storage and may correlate the same
occurrence with richer operational diagnostics. Those mechanisms remain realization choices unless this ADR explicitly
makes a material part of Contract meaning. ADR-0062 owns that architecture.

## 3. Decision Drivers

Diagnostic Evidence must explain an already-established machine result rather than create a competing result. The source
that judged the original obligation remains authoritative even if diagnostic capture later fails or no human-readable
explanation is available.

An evidence occurrence needs exact provenance. Material is not Contract Evidence merely because it was observed near a
Failure in time or appears in the same runtime stack. The evidence definition must resolve to an exact diagnosable
source, and the occurrence must remain related to the exact source occurrence it explains.

Contract-required evidence is closed. If a definition requires a complete shape, a partial capture cannot silently
become a weaker valid occurrence. Optional operational material may be incomplete, but that incompleteness cannot be
used to satisfy a stronger Contract Evidence obligation.

Material that describes a value at the judgment boundary must come from that boundary. Reading mutable state later is
reconstruction. Later investigation may still be useful, but a reconstructed value cannot be presented as the frozen
observation used by the original judgment.

Diagnostic Evidence must not steal semantic coordinates from the source authority. Failure `applicable context` remains
Failure meaning where ADR-0057 requires it. Evidence may refer to that established material without redefining it.

The Contract remains backend-neutral. A `Throwable`, stack frame, native address, profiler sample, operating-system
trace, hardware error record, or telemetry packet can be useful realization evidence without becoming required Contract
vocabulary.

A diagnostic depth choice may reduce optional observation cost. It cannot change Contract validity, erase an established
Failure, or turn declared evidence into sampled best-effort data.

## 4. Diagnostic Characteristic Inventory for Deliberation

This section is an inventory for ADR deliberation. It does not adopt every item below as an independent Contract
coordinate. The purpose is to preserve distinctions that recur across software systems and general engineering before
this ADR decides what the Diagnostic Contract actually owns.

The inventory intentionally keeps related characteristics separate. Later work may represent several items under one
canonical structure, but that compression must occur only after their semantic differences have been examined.

The ownership tags classify where each characteristic is useful without deleting or rewriting the characteristic itself.
A tag is not a final adoption decision, and more than one tag may apply.

| Tag   | Meaning                                                                                                                                                                                                              |
|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CT`  | The characteristic can remain meaningful at Contract Theory level without depending on the Kontrakt compiler, a source-file representation, or one backend.                                                          |
| `CT→` | The engineering principle is useful to Contract Theory, but its concrete software or physical-engineering form must be translated into Kontrakt's explicit, shallow Contract semantics rather than copied directly.  |
| `C`   | The characteristic directly informs the Kontrakt compiler diagnostic subsystem in ADR-0061.                                                                                                                          |
| `R`   | The characteristic directly informs generated Contract-Machine or runtime diagnostic realization in ADR-0062.                                                                                                        |
| `EXT` | The characteristic touches Diagnostic but its authoritative meaning belongs to another Contract or engineering authority such as Failure, Publication/Output, recovery, security, maintenance, or lifecycle control. |

`CT→` is intentionally distinct from `CT`. For example, a compiler may need a deep provenance chain through source,
resolution, optimization, and lowering, while the Contract Theory translation only needs the exact semantic relation
among the diagnosable subject, owning authority, judgment, and applicable Contract material. The engineering principle
is preserved, but the implementation topology is not imported into the Contract.

### 4.1. Identity, Subject, and Ownership

| ID     | Characteristic          | Ownership    | Meaning                                                                                                  | Difference from nearby characteristics                                                                      |
|--------|-------------------------|--------------|----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `D001` | **Diagnostic Identity** | `CT / C / R` | Stable identity of the diagnostic definition independent of rendered wording.                            | Different from an occurrence identity: one definition can produce many occurrences.                         |
| `D002` | **Occurrence Identity** | `CT / C / R` | Identity of one concrete diagnostic occurrence.                                                          | Does not identify the diagnostic kind; it distinguishes repeated instances of the same definition.          |
| `D003` | **Owning Authority**    | `CT`         | Authority responsible for the judgment or observation that the diagnostic explains.                      | Different from the subject: authority answers who may judge, while subject answers what is being diagnosed. |
| `D004` | **Subject**             | `CT`         | Exact Contract, State-Machine authority, Realization boundary, component, or occurrence being diagnosed. | A subject can be diagnosed by an authority without being that authority itself.                             |

### 4.2. Classification, Severity, Criticality, and Attention

| ID     | Characteristic     | Ownership       | Meaning                                                               | Difference from nearby characteristics                                                                                 |
|--------|--------------------|-----------------|-----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `D005` | **Severity**       | `CT→ / C / R`   | Diagnostic handling strength such as error, warning, or remark.       | Not the same as physical or business criticality, and not necessarily the order in which a human should inspect items. |
| `D006` | **Criticality**    | `CT→ / R / EXT` | Importance of the underlying condition or consequence.                | Criticality describes the condition being diagnosed; severity describes the diagnostic treatment.                      |
| `D007` | **Classification** | `C / R`         | Stable grouping by diagnostic kind or requirement class.              | Classification organizes rules; it does not by itself tell an operator which item is most urgent.                      |
| `D008` | **Priority**       | `C / R / EXT`   | Ordering of human or system attention among simultaneous diagnostics. | Priority is operational attention, while classification and criticality remain descriptive properties.                 |

### 4.3. Source, Semantic, and Transformation Provenance

| ID     | Characteristic                | Ownership | Meaning                                                                                                | Difference from nearby characteristics                                                                                           |
|--------|-------------------------------|-----------|--------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| `D009` | **Primary Source Location**   | `C`       | Main user-visible source coordinate associated with the diagnostic.                                    | It is one presentation anchor, not the whole provenance chain.                                                                   |
| `D010` | **Related Source Locations**  | `C`       | Additional declarations or spans needed to understand a relation or conflict.                          | Unlike the primary location, these are supporting anchors and may be many.                                                       |
| `D011` | **Source Provenance**         | `C`       | Relation back to the declarations, includes, expansions, or source artifacts from which material came. | Broader than a line/column location; provenance can cross expansion or composition boundaries.                                   |
| `D012` | **Semantic Provenance**       | `CT→ / C` | Relation from source declaration through resolution to canonical semantic material and judgment.       | Unlike source provenance, it explains meaning transformation rather than textual origin.                                         |
| `D013` | **Transformation Provenance** | `C`       | Relation preserved across optimization, lowering, specialization, or backend transforms.               | Unlike semantic provenance to canonical meaning, this follows later representation changes and can be invalidated by transforms. |

### 4.4. Judgment and Failure Relations

| ID     | Characteristic        | Ownership  | Meaning                                                    | Difference from nearby characteristics                                                           |
|--------|-----------------------|------------|------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `D014` | **Judgment Relation** | `CT`       | Exact authoritative judgment that the diagnostic explains. | A diagnostic may relate to a successful judgment, so this cannot be reduced to Failure relation. |
| `D015` | **Failure Relation**  | `CT / EXT` | Relation to an established Failure when one exists.        | Failure remains its own semantic result; the diagnostic does not establish or replace it.        |

### 4.5. Diagnostic Basis and Evidence Relations

| ID     | Characteristic           | Ownership     | Meaning                                                                                         | Difference from nearby characteristics                                                                              |
|--------|--------------------------|---------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `D016` | **Diagnostic Basis**     | `CT`          | The material and rule basis on which the diagnostic account rests.                              | Broader than one observed value: it can include required meaning, observations, references, and relations.          |
| `D017` | **Required Meaning**     | `CT`          | What the governing authority required to hold.                                                  | Different from a baseline measurement: this is semantic obligation, not merely a comparison reference.              |
| `D018` | **Established Material** | `CT`          | Semantic material already established by an authority.                                          | Different from observed material, which can be realization-side measurement without semantic authority.             |
| `D019` | **Observed Material**    | `CT→ / R`     | Material actually measured or observed during realization or diagnosis.                         | Observation can be uncertain or incomplete and does not automatically become authoritative meaning.                 |
| `D020` | **Missing Material**     | `CT`          | Required material for which no satisfying established or observed material exists.              | Absence differs from conflict: nothing satisfies the requirement rather than multiple materials being incompatible. |
| `D021` | **Conflicting Material** | `CT`          | Materials that cannot jointly satisfy the applicable rules.                                     | Conflict differs from simple expected/found mismatch because both sides may individually exist yet be incompatible. |
| `D022` | **Mismatch Relation**    | `CT`          | Explicit difference between required and found material.                                        | A mismatch need not imply causality or a multi-party conflict.                                                      |
| `D023` | **Causal Relation**      | `CT→ / C / R` | Claim that one condition, event, or material contributed causally to another.                   | Temporal order and logical inconsistency alone do not prove causality.                                              |
| `D024` | **Constraint Relation**  | `CT`          | Logical or Contract relation showing why a set of materials cannot satisfy the governing rules. | Unlike causal relation, this can explain invalidity without claiming a physical cause.                              |

### 4.6. Causal Semantics from Dependability Engineering

| ID     | Characteristic                                | Ownership   | Meaning                                                                            | Difference from nearby characteristics                                                                      |
|--------|-----------------------------------------------|-------------|------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `D025` | **Failure Mode / Cause / Effect Separation**  | `CT→ / EXT` | Keeps how something failed, why it failed, and what resulted as separate meanings. | These are intentionally distinct in FMEA/FMECA and must not be collapsed into one diagnostic “cause” field. |
| `D026` | **Initiating Event / Consequence Separation** | `CT→ / EXT` | Separates the event that starts a scenario from downstream outcomes.               | An initiating event is not itself the full causal explanation or final consequence.                         |

### 4.7. Applicability, Assumptions, and Operating Conditions

| ID     | Characteristic                      | Ownership     | Meaning                                                                               | Difference from nearby characteristics                                                                                     |
|--------|-------------------------------------|---------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `D027` | **Assumptions**                     | `CT→ / R`     | Explicit premises under which a diagnostic analysis is valid.                         | Assumptions are not observed facts; if they fail, the diagnostic conclusion may no longer apply.                           |
| `D028` | **Applicability**                   | `CT`          | Conditions under which a diagnostic definition or evidence relation is valid.         | Broader than operating condition because applicability can include Version, Policy World, scope, or diagnostic capability. |
| `D029` | **Operating Condition**             | `CT→ / R`     | Actual mode, load, environment, or state under which evidence was obtained.           | An operating condition is contextual material, not necessarily the comparison baseline.                                    |
| `D030` | **Influence Quantity / Confounder** | `CT→ / R`     | Quantity or condition that affects observation without being the target of diagnosis. | Unlike the subject or required meaning, it changes interpretation of evidence indirectly.                                  |
| `D032` | **Reference / Baseline**            | `CT→ / C / R` | Reference against which an observation or condition is interpreted.                   | Baseline is a comparison basis; it must not be confused with the actual operating condition or a Contract obligation.      |

### 4.8. Measurement, Observation Interpretation, and Evidence Trustworthiness

| ID     | Characteristic                          | Ownership     | Meaning                                                                                                                                                                      | Difference from nearby characteristics                                                                                                                                                                                                                                                                                                                                                         |
|--------|-----------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D031` | **Measurement Model**                   | `R`           | Realization-side model relating observed inputs to a measured quantity, estimated condition, or other interpreted realization material.                                      | A Measurement Model belongs to observation of realization. It is not the mapping from IDL to canonical Contract material and does not mediate authoritative Contract or State-Machine judgment.                                                                                                                                                                                                |
| `D185` | **Observation Interpretation Boundary** | `CT→ / R`     | Preserves the distinction between observed realization material, the interpretation applied to that material, and the diagnostic statement supported by that interpretation. | `D018 Established Material` and `D019 Observed Material` distinguish kinds of material. This characteristic additionally preserves the interpretive relation between observation and diagnostic statement. `D166 Cross-Layer Semantic Alignment` relates evidence across implementation layers; it does not make the interpretation itself an authoritative Contract or State-Machine meaning. |
| `D033` | **Calibration State**                   | `R`           | Whether the measuring system remains valid against its calibration conditions.                                                                                               | Calibration state is one support for trustworthiness, not the diagnostic conclusion itself.                                                                                                                                                                                                                                                                                                    |
| `D034` | **Measurement Traceability**            | `CT→ / R`     | Traceable chain from a measurement result back to an identified reference.                                                                                                   | Traceability does not guarantee low uncertainty or absence of mistakes.                                                                                                                                                                                                                                                                                                                        |
| `D035` | **Uncertainty**                         | `CT→ / R`     | Explicit uncertainty associated with observed or measured material.                                                                                                          | Uncertainty is not the same as invalidity; valid evidence may still have non-zero uncertainty.                                                                                                                                                                                                                                                                                                 |
| `D036` | **Precision**                           | `CT→ / C / R` | Resolution or spread with which a value, location, or time is represented.                                                                                                   | Precision does not imply correctness, validity, or traceability.                                                                                                                                                                                                                                                                                                                               |
| `D037` | **Validity**                            | `CT`          | Whether a field or evidence item is actually valid for interpretation.                                                                                                       | Validity answers whether it may be used; quality and uncertainty describe how strongly it should be trusted.                                                                                                                                                                                                                                                                                   |
| `D038` | **Quality**                             | `CT→ / R`     | Status of the evidence source or value, such as good, uncertain, or bad.                                                                                                     | Quality is broader than binary validity and can carry source-health information.                                                                                                                                                                                                                                                                                                               |
| `D039` | **Diagnostic Confidence**               | `CT→ / C / R` | Confidence in an attribution or diagnostic conclusion.                                                                                                                       | Must not be inferred directly from evidence quality; good evidence can still support multiple hypotheses.                                                                                                                                                                                                                                                                                      |

### 4.9. Explicit Unknown and Abstention

| ID     | Characteristic       | Ownership | Meaning                                                                                 | Difference from nearby characteristics                                              |
|--------|----------------------|-----------|-----------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `D040` | **Abstention**       | `CT`      | Explicit refusal to make an attribution when available evidence is ambiguous.           | Stronger than “low confidence”: abstention avoids inventing a cause at all.         |
| `D065` | **Graceful Unknown** | `CT`      | Preserves unknown, unavailable, or unsupported values without fabricating replacements. | Broader than attribution abstention because any evidence coordinate can be unknown. |

### 4.10. Diagnostic Capability

| ID     | Characteristic                 | Ownership     | Meaning                                                                                               | Difference from nearby characteristics                                                     |
|--------|--------------------------------|---------------|-------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| `D041` | **Detectability**              | `CT→ / R`     | Whether a particular failure or condition is observable by the diagnostic mechanism.                  | A condition can be detectable without the mechanism covering all conditions of that class. |
| `D042` | **Diagnostic Coverage**        | `CT→ / R`     | Portion or domain of failures for which the mechanism can provide the intended diagnostic capability. | Coverage is aggregate scope, not per-occurrence confidence or isolation precision.         |
| `D043` | **Isolation / Discrimination** | `CT→ / C / R` | Ability to distinguish which candidate source or failure is responsible.                              | Detection can succeed while isolation remains only subsystem-level.                        |
| `D044` | **Resolution / Specificity**   | `CT→ / C / R` | Granularity at which isolation or explanation can identify the subject.                               | Specificity refines isolation depth; it does not measure overall coverage.                 |
| `D045` | **False Positive Resistance**  | `CT→ / C / R` | Resistance to declaring a condition that is not present.                                              | Different from missed detection; both must be considered independently.                    |
| `D046` | **False Negative Awareness**   | `CT→ / C / R` | Recognition that real conditions may remain undetected.                                               | This is not the same as low confidence in a detected occurrence.                           |
| `D047` | **Latent Failure Awareness**   | `CT→ / R`     | Recognition that a failure can exist before evidence or indication is available.                      | Prevents equating absence of diagnostic evidence with absence of Failure.                  |

### 4.11. Attribution under Multiple Causes or Hypotheses

| ID     | Characteristic             | Ownership     | Meaning                                                                                 | Difference from nearby characteristics                                                                     |
|--------|----------------------------|---------------|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `D048` | **Common-Cause Awareness** | `CT→ / C / R` | Recognizes that multiple symptoms may share one cause.                                  | Different from aggregation: occurrences remain distinct even if investigation identifies a common cause.   |
| `D049` | **Alternative Hypotheses** | `CT`          | Keeps multiple plausible causes separate while evidence is insufficient to isolate one. | Avoids converting a candidate explanation into authoritative fact.                                         |
| `D050` | **Evidence Convergence**   | `CT→ / C / R` | Allows attribution to become more specific as additional evidence arrives.              | Convergence must not rewrite already-established source semantics; it only refines the diagnostic account. |

### 4.12. Diagnostic Temporality

| ID     | Characteristic                           | Ownership     | Meaning                                                                    | Difference from nearby characteristics                                                               |
|--------|------------------------------------------|---------------|----------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `D051` | **Occurrence Time**                      | `CT→ / R`     | Time at which the underlying event or judgment occurred.                   | Not the same as when a collector observed or stored it.                                              |
| `D052` | **Observation Time**                     | `R`           | Time at which a diagnostic observer first saw the event or material.       | May be later than occurrence time and can use a different clock.                                     |
| `D053` | **Capture Time**                         | `CT→ / R`     | Time at which evidence was frozen or copied into a diagnostic record.      | Capture may be triggered after detection, so it must not silently substitute for occurrence time.    |
| `D054` | **Report Time**                          | `R`           | Time at which the diagnostic became available to its consumer.             | Different from capture; transport or buffering can delay reporting.                                  |
| `D055` | **Timeliness / Latency**                 | `CT→ / R`     | Whether diagnostic availability is early enough for its intended decision. | Retention asks how long evidence remains; timeliness asks how quickly it becomes usable.             |
| `D056` | **Ordering**                             | `CT→ / C / R` | Order among diagnostic occurrences or evidence records.                    | Order alone does not establish causality.                                                            |
| `D057` | **Time Synchronization**                 | `R`           | Ability to compare times across different producers or components.         | Distinct from ordering inside one source; synchronization is needed for cross-source reconstruction. |
| `D059` | **Causal Ordering vs Temporal Ordering** | `CT`          | Explicit separation of “happened before” from “caused”.                    | Temporal precedence is evidence, not proof of causal responsibility.                                 |

### 4.13. Correlation

| ID     | Characteristic  | Ownership     | Meaning                                                                                               | Difference from nearby characteristics                                                  |
|--------|-----------------|---------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `D058` | **Correlation** | `CT→ / C / R` | Binds related diagnostic records to the same run, interaction, trace, incident, or source occurrence. | Correlation states shared context; it does not by itself imply cause or temporal order. |

### 4.14. Evidence Completeness and Loss

| ID     | Characteristic   | Ownership     | Meaning                                                                         | Difference from nearby characteristics                                                                              |
|--------|------------------|---------------|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `D060` | **Completeness** | `CT`          | Whether all material required by the applicable evidence obligation is present. | Evidence can be valid yet incomplete; completeness is independent of truth of individual fields.                    |
| `D061` | **Partiality**   | `CT`          | Explicit state that only part of the expected diagnostic material is available. | Partiality is an incompleteness condition, not a universal replacement for a stricter Contract Evidence occurrence. |
| `D062` | **Loss**         | `CT→ / C / R` | Known loss of events or evidence during capture, transport, or collection.      | Loss differs from deliberate truncation and from unknown absence.                                                   |
| `D063` | **Overflow**     | `C / R`       | Loss caused specifically by bounded buffer or recorder capacity.                | Overflow is one reason for loss and should not be generalized to all missing evidence.                              |
| `D064` | **Truncation**   | `CT→ / C / R` | Intentional or representation-limited removal of part of the material.          | Unlike accidental loss, truncation is known policy or format behavior and should be disclosed.                      |

### 4.15. Historical and Temporal Fidelity

| ID     | Characteristic                        | Ownership | Meaning                                                                                       | Difference from nearby characteristics                                                          |
|--------|---------------------------------------|-----------|-----------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `D066` | **Freshness**                         | `CT→ / R` | How current an observation is relative to the state being discussed.                          | Freshness does not establish that the value represents the original occurrence.                 |
| `D067` | **Frozen Historical Evidence**        | `CT`      | Material captured and frozen at the occurrence or defined diagnostic boundary.                | Different from later reconstruction; it preserves the state that existed at capture time.       |
| `D068` | **Reconstructed Evidence**            | `CT`      | Material inferred later from retained traces, logs, or current state.                         | Useful for investigation but must not be presented as occurrence-time frozen evidence.          |
| `D069` | **Current State vs Historical State** | `CT`      | Explicit distinction between what is true now and what was true for the diagnosed occurrence. | A current query can contradict historical evidence without rewriting the past occurrence.       |
| `D070` | **Historical Fidelity**               | `CT`      | Degree to which retained evidence accurately preserves the diagnosed occurrence.              | Broader than freshness: it covers capture timing, reconstruction, and preservation across time. |

### 4.16. Evidence Integrity and Authenticity

| ID     | Characteristic                               | Ownership     | Meaning                                                                                                         | Difference from nearby characteristics                                                              |
|--------|----------------------------------------------|---------------|-----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `D071` | **Evidence Integrity**                       | `CT`          | Evidence is not silently altered, mixed with another occurrence, or semantically corrupted after establishment. | Integrity concerns preservation after creation, not whether the claimed producer was genuine.       |
| `D072` | **Evidence Authenticity / Origin Integrity** | `CT→ / C / R` | Evidence originates from the source or authority it claims.                                                     | Authenticity is source trust; content can remain unmodified yet still come from the wrong producer. |

### 4.17. Explanation and Semantic Projection

| ID     | Characteristic                | Ownership     | Meaning                                                                                                    | Difference from nearby characteristics                                                                         |
|--------|-------------------------------|---------------|------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `D073` | **Explanation**               | `CT`          | Human- or tool-oriented account derived from evidence and semantic relations.                              | Explanation is not itself evidence and may summarize or omit internal details.                                 |
| `D074` | **User-Language Projection**  | `CT→ / C`     | Explains the problem in the vocabulary of the declared Contract rather than compiler implementation terms. | Unlike generic readability, this requires semantic mapping back to user concepts.                              |
| `D075` | **Semantic Compression**      | `C`           | Reduces large internal analysis state to the small set of meanings needed to understand the issue.         | Compression must preserve the relevant cause and must not become evidence loss in the underlying record.       |
| `D076` | **Root-Cause Focus**          | `CT→ / C / R` | Prioritizes the most explanatory cause over cascaded secondary diagnostics.                                | Does not merge separate Failures; it is an explanation and attention rule.                                     |
| `D077` | **First-Divergence Evidence** | `C / R`       | Uses the earliest trustworthy divergence between comparable executions as a diagnostic pivot.              | A technique, not a universal Diagnostic coordinate; it is evidence-selection strategy where comparison exists. |
| `D078` | **Mismatch Clarity**          | `CT / C`      | Makes required and actual material and their exact difference explicit.                                    | More specific than readability: the semantic mismatch itself must be visible.                                  |

### 4.18. Actionability, Decision Support, and Correction Guidance

| ID     | Characteristic            | Ownership     | Meaning                                                                                                      | Difference from nearby characteristics                                                                                                |
|--------|---------------------------|---------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `D079` | **Actionability**         | `CT→ / C / R` | Diagnostic explanation gives the user enough direction to decide what to inspect or change next.             | Actionability does not mean the diagnostic may prescribe recovery or rewrite Contract authority.                                      |
| `D080` | **Decision Sufficiency**  | `CT`          | Information is sufficient for the intended diagnostic decision.                                              | Different from completeness: a complete raw record can still be poor decision support, while a minimal explanation can be sufficient. |
| `D081` | **Guidance**              | `CT→ / C / R` | Non-authoritative suggestions for correcting or investigating the issue.                                     | Guidance is advice, not evidence and not a Contract result.                                                                           |
| `D082` | **Suggestion Confidence** | `C / R`       | States how strongly a suggested correction can be trusted.                                                   | Different from diagnostic confidence: the diagnosis can be certain while the preferred edit remains ambiguous.                        |
| `D083` | **Automatic-Fix Safety**  | `C`           | Whether an edit can be applied mechanically without guessing user intent or changing authority unexpectedly. | A valid suggestion is not automatically safe for machine application.                                                                 |

### 4.19. Representation, Vocabulary, and Interoperability

| ID     | Characteristic                                                | Ownership     | Meaning                                                                               | Difference from nearby characteristics                                                                        |
|--------|---------------------------------------------------------------|---------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `D084` | **Human Readability**                                         | `C / R`       | Presentation can be understood efficiently by a human.                                | Readability is a rendering property and does not define diagnostic identity or evidence.                      |
| `D085` | **Machine Readability**                                       | `C / R`       | Structured tools can consume diagnostic meaning without parsing prose.                | Machine readability does not require exposing internal compiler representation.                               |
| `D086` | **Representation Independence**                               | `CT`          | Diagnostic meaning remains stable across text, IDE, JSON, SARIF, or other renderings. | Renderer choice must not become semantic identity.                                                            |
| `D087` | **Data Processing / Communication / Presentation Separation** | `CT→ / C / R` | Diagnostic computation, transport, and display remain separate responsibilities.      | Prevents transport or UI formats from becoming the diagnostic authority.                                      |
| `D088` | **Localization Independence**                                 | `C`           | Translated wording does not change diagnostic identity or machine meaning.            | Localization is a presentation transformation, not a semantic version.                                        |
| `D089` | **Common Vocabulary**                                         | `CT→ / C / R` | Producer and consumer use stable terms for diagnostic meanings.                       | A common vocabulary is semantic interoperability, not merely a shared wire format.                            |
| `D090` | **Interoperability**                                          | `CT→ / C / R` | Diagnostic meaning can cross compatible tools or systems without semantic loss.       | Broader than machine readability because different producers and consumers may use different implementations. |

### 4.20. Diagnostic Acquisition and Richness

| ID     | Characteristic                          | Ownership    | Meaning                                                                                                   | Difference from nearby characteristics                                                                                   |
|--------|-----------------------------------------|--------------|-----------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `D091` | **Diagnostic Depth**                    | `CT / C / R` | Configured richness of optional diagnostic acquisition.                                                   | Depth must not weaken the guaranteed evidence core.                                                                      |
| `D092` | **Required vs Optional Evidence**       | `CT`         | Separates evidence required by Contract from optional enrichment.                                         | Optional material may be reduced for cost; required material cannot silently become best effort.                         |
| `D093` | **Selective Capture**                   | `C / R`      | Captures only relevant evidence rather than all possible material continuously.                           | Selection is an acquisition strategy; it cannot redefine diagnostic meaning.                                             |
| `D094` | **Triggerability**                      | `CT→ / R`    | Deep capture can begin on Failure, anomaly, threshold, or explicit investigation request.                 | A trigger controls acquisition timing, not whether the underlying Failure exists.                                        |
| `D095` | **Threshold**                           | `C / R`      | Suppresses capture below a configured significance or duration threshold.                                 | Threshold is not severity; a severe class can still have different acquisition thresholds.                               |
| `D096` | **Throttling**                          | `C / R`      | Limits repeated acquisition or reporting rate.                                                            | Throttling controls volume but must not erase required occurrence semantics.                                             |
| `D097` | **Sampling**                            | `C / R`      | Records only a subset of eligible optional observations.                                                  | Sampling can reduce coverage and therefore must not satisfy mandatory evidence unless the Contract explicitly allows it. |
| `D098` | **Differential Observability**          | `C / R`      | Focuses deep observation on behavior that differs from a reference or peer group.                         | A runtime optimization strategy rather than a universal semantic coordinate.                                             |
| `D099` | **Streaming Summarization / Sketching** | `C / R`      | Maintains compact summaries instead of retaining all raw telemetry.                                       | A summary is not equivalent to the raw evidence it replaced unless the Contract defines that meaning.                    |
| `D100` | **Full-Stack Extensibility**            | `R`          | Allows evidence from multiple software and hardware layers to correlate through one diagnostic framework. | Extensibility must preserve authority boundaries between Contract evidence and backend operational evidence.             |

### 4.21. Diagnostic Cost and Non-Intrusiveness

| ID     | Characteristic                        | Ownership     | Meaning                                                                                          | Difference from nearby characteristics                                                                                                                    |
|--------|---------------------------------------|---------------|--------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D101` | **Deduplication**                     | `C / R`       | Shares repeated immutable context or evidence structures instead of copying them per occurrence. | Physical sharing must not merge occurrence identity.                                                                                                      |
| `D102` | **Lazy Materialization**              | `C / R`       | Defers expensive explanation, dump, or provenance expansion until it is needed.                  | The underlying required relation must still be available; laziness cannot invent evidence later.                                                          |
| `D103` | **Cost Boundedness**                  | `CT→ / C / R` | Diagnostic work has explicit limits so it cannot dominate compiler or machine resources.         | Cost limits are realization constraints and may not silently weaken mandatory evidence.                                                                   |
| `D104` | **Non-Intrusiveness**                 | `CT→ / C / R` | Observation should avoid materially perturbing the behavior being diagnosed.                     | A performance specialization of non-interference: the diagnostic can be semantically harmless yet still distort timing enough to invalidate observations. |
| `D147` | **Guarantee Before Cost**             | `CT`          | Required diagnostic guarantees are checked before optional cost optimization.                    | Cost models may choose enrichment but may not legalize dropping required evidence.                                                                        |
| `D148` | **Optional Enrichment Profitability** | `C / R`       | Expensive optional evidence is acquired only when expected diagnostic value justifies its cost.  | Applies only after required guarantees are satisfied.                                                                                                     |

### 4.22. Human Attention and Alarm Management

| ID     | Characteristic                       | Ownership     | Meaning                                                                          | Difference from nearby characteristics                                                       |
|--------|--------------------------------------|---------------|----------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| `D105` | **Attention Boundedness**            | `CT→ / C / R` | Diagnostic output must not overwhelm the human attention available to act on it. | Different from compute cost; a cheap warning can still be harmful if it floods the user.     |
| `D106` | **Alert Aggregation**                | `C / R / EXT` | Groups related alerts for presentation while preserving underlying occurrences.  | Presentation aggregation must not become Failure aggregation or destroy occurrence identity. |
| `D107` | **Alarm Rationalization**            | `C / R / EXT` | Requires a reason that a diagnostic deserves active human attention.             | Rationalization concerns alert policy, not whether the diagnostic evidence is true.          |
| `D108` | **Nuisance / Chattering Resistance** | `C / R / EXT` | Controls repetitive, unstable, or low-value indications that erode trust.        | Distinct from false positives: a true diagnostic can still chatter excessively.              |

### 4.23. Determinism, Reproduction, and Incomplete Execution

| ID     | Characteristic                      | Ownership     | Meaning                                                                                                         | Difference from nearby characteristics                                                                                    |
|--------|-------------------------------------|---------------|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `D109` | **Determinism**                     | `CT / C / R`  | Equivalent semantic input produces stable diagnostic meaning and deterministic ordering where order is defined. | Does not require every backend timestamp or operational sample to be bit-identical.                                       |
| `D110` | **Reproducibility**                 | `CT→ / C / R` | Investigators can recreate the relevant diagnostic situation from sufficient material.                          | Reproducibility is an ability; reproducer material is the concrete input needed to achieve it.                            |
| `D111` | **Reproducer Material**             | `C / R`       | Exact source, configuration, Version, seed, IR, or other material needed to reproduce an issue.                 | It is evidence selected for replay, not the same as the diagnostic explanation.                                           |
| `D112` | **Replayability**                   | `C / R`       | Retained diagnostic/event material can be reprocessed through an analysis flow.                                 | Replay is stronger than simple retention because the format and semantics must still support re-execution or re-analysis. |
| `D113` | **Incomplete-Execution Visibility** | `CT / C / R`  | Explains which processing remained unfinished without inventing semantic results for stages never reached.      | Preserves ADR-0057’s rule that unreached processing is not `Skipped` or `NotEvaluated` result meaning.                    |

### 4.24. Diagnostic Assurance

| ID     | Characteristic                    | Ownership     | Meaning                                                                                                | Difference from nearby characteristics                                                                       |
|--------|-----------------------------------|---------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `D114` | **Auditability**                  | `CT / C / R`  | A later reviewer can trace why a diagnostic was produced and which material supported it.              | Auditability needs provenance and retention but is not identical to either.                                  |
| `D115` | **Verifiability**                 | `CT / C / R`  | Diagnostic evidence or derived explanation can be checked against its definitions and sources.         | Verification asks whether the diagnostic is correct; auditability asks whether its history can be inspected. |
| `D116` | **Diagnostic Mechanism Health**   | `CT→ / C / R` | The mechanism performing diagnosis has observable health and can itself fail.                          | Prevents diagnostic machinery from being treated as infallible authority.                                    |
| `D117` | **Self-Test / Proof-Testability** | `C / R`       | Diagnostic machinery can be deliberately exercised to confirm that detection and reporting still work. | Different from runtime health observation because it requires active verification capability.                |
| `D118` | **Graceful Degradation**          | `CT / C / R`  | Loss of diagnostic capability does not rewrite the original machine result and degrades explicitly.    | A degraded diagnostic may become less informative without changing the source Failure.                       |
| `D149` | **Diagnostic Test Coverage**      | `C`           | Diagnostic definitions and rendering paths are protected by systematic tests.                          | Testing the diagnostic system is different from diagnostic coverage of the user system.                      |

### 4.25. Top-Level Diagnostic Boundaries

| ID     | Characteristic                                   | Ownership  | Meaning                                                                                     | Difference from nearby characteristics                                                                      |
|--------|--------------------------------------------------|------------|---------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `D119` | **Non-Authority**                                | `CT`       | Diagnostic does not establish the source judgment or Failure it explains.                   | The source authority remains sovereign even when diagnostic evidence is missing.                            |
| `D120` | **Non-Interference**                             | `CT`       | Diagnostic activity must not change Contract meaning or machine semantic outcome.           | Broader than non-intrusiveness: semantic interference is forbidden even when performance overhead is small. |
| `D121` | **Orthogonality to Recovery**                    | `CT / EXT` | Diagnosis and recovery remain separate authorities.                                         | A diagnostic may recommend recovery but does not own the recovery action.                                   |
| `D122` | **Detection / Diagnosis Separation**             | `CT / EXT` | Recognizes condition detection as different from explaining or isolating what is wrong.     | A detector can raise an anomaly without sufficient evidence for diagnosis.                                  |
| `D123` | **Diagnosis / Prognosis Separation**             | `CT / EXT` | Separates explanation of present/past condition from prediction of future degradation.      | Prediction must not be presented as evidence that a current Failure already exists.                         |
| `D124` | **Diagnosis / Alert Separation**                 | `CT / EXT` | Separates diagnostic meaning from whether and how it is actively announced.                 | An internal diagnostic can exist without a user alert; alert priority does not redefine diagnosis.          |
| `D125` | **Diagnostic / Maintenance Decision Separation** | `CT / EXT` | Separates diagnosis from the later operational decision to repair, replace, or reconfigure. | Action policy can use diagnostics without becoming part of diagnostic evidence.                             |

### 4.26. Lifecycle Validity and Operational Feedback

| ID     | Characteristic                        | Ownership           | Meaning                                                                                                        | Difference from nearby characteristics                                                                             |
|--------|---------------------------------------|---------------------|----------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `D126` | **Lifecycle Consistency**             | `CT / C / R`        | Diagnostic definitions and evidence relations remain valid across the lifecycle of the governed system.        | A once-valid diagnostic cannot be assumed valid after contract, configuration, or implementation evolution.        |
| `D127` | **Management of Change**              | `CT→ / C / R / EXT` | Changes that can invalidate diagnostic assumptions or mappings are explicitly reviewed.                        | A change process is distinct from Version identity; it governs how validity is reassessed.                         |
| `D128` | **In-Service Evidence Feedback**      | `R / EXT`           | Operational evidence can inform later assurance or diagnostic design.                                          | Feedback must not retroactively rewrite historical Contract meaning.                                               |
| `D129` | **Unintended-Behavior Investigation** | `C / R / EXT`       | Diagnostic design leaves room to investigate behavior not captured by the originally expected failure catalog. | Different from changing Contract semantics at runtime; it belongs to engineering investigation and later revision. |

### 4.27. Availability, Retention, Persistence, and Archival

| ID     | Characteristic         | Ownership       | Meaning                                                                                             | Difference from nearby characteristics                                                 |
|--------|------------------------|-----------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `D130` | **Availability**       | `CT`            | Whether established evidence can currently be retrieved or inspected.                               | Semantic existence can remain true while current availability is false.                |
| `D131` | **Retention**          | `CT`            | Guaranteed period or lifecycle boundary for keeping established evidence available.                 | Retention is an availability guarantee, not proof of persistence across all failures.  |
| `D132` | **Eviction Semantics** | `CT→ / R`       | Rules governing when retained physical material may be removed.                                     | Eviction is a storage action; expiry is the end of a semantic availability obligation. |
| `D133` | **Persistence**        | `CT / R`        | Whether evidence survives process, runtime, or stronger restart boundaries.                         | Persistence is orthogonal to how long retention lasts inside a live process.           |
| `D134` | **Archival**           | `CT→ / R / EXT` | Transfer to a separate long-term record intended for later investigation.                           | Archive is not the same storage tier or access semantics as active retention.          |
| `D135` | **Retention Fidelity** | `CT`            | Retained representation preserves the evidence meaning, identity, ordering, and applicable context. | Keeping bytes is insufficient if later decoding changes their semantic interpretation. |

### 4.28. Confidentiality, Access, and Disclosure

| ID     | Characteristic         | Ownership   | Meaning                                                                                     | Difference from nearby characteristics                                                                           |
|--------|------------------------|-------------|---------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `D136` | **Confidentiality**    | `CT→ / EXT` | Diagnostic material may contain sensitive internal or user data that must remain protected. | Confidentiality limits who may learn material; it is not the same as whether the material exists.                |
| `D137` | **Access Control**     | `R / EXT`   | Controls which actors may request, acquire, or read diagnostic evidence.                    | Access control is operational authorization and does not by itself grant outward Contract authority.             |
| `D138` | **Disclosure Control** | `CT / EXT`  | Controls which internal diagnostic material may cross an external boundary.                 | In Kontrakt this must remain subordinate to Publication and Output rather than an ad hoc diagnostic export path. |

### 4.29. Stability, Portability, and Evolution

| ID     | Characteristic                                | Ownership     | Meaning                                                                                                | Difference from nearby characteristics                                                             |
|--------|-----------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `D139` | **Backend Independence**                      | `CT`          | Contract-level diagnostic meaning does not depend on JVM, OS, hardware, or one logging representation. | Backend-specific evidence can enrich the occurrence without defining its canonical meaning.        |
| `D140` | **Schema / Representation Evolution**         | `CT→ / C / R` | Machine-readable diagnostic representations can evolve without silently changing established meanings. | Schema versioning concerns representation compatibility, not Contract Version semantics by itself. |
| `D141` | **Evidence Portability**                      | `CT→ / C / R` | Evidence can move across compatible tools or backends without losing the meaning needed for diagnosis. | Portability is stronger than serializability because semantic interpretation must survive.         |
| `D150` | **Semantic Stability of Diagnostic Identity** | `CT / C / R`  | The same diagnostic concept remains identifiable across wording, localization, or renderer changes.    | Identity stability does not require source locations or occurrence context to remain unchanged.    |

### 4.30. Sufficiency, Relevance, and Decision Traceability

| ID     | Characteristic                            | Ownership | Meaning                                                                                                                  | Difference from nearby characteristics                                                                                                |
|--------|-------------------------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `D142` | **Diagnostic Sufficiency**                | `CT`      | The guaranteed diagnostic account contains enough material to understand the intended judgment or condition.             | Sufficiency is purpose-relative and does not require maximal raw-data completeness.                                                   |
| `D143` | **Minimality / Relevance**                | `CT`      | Material that does not contribute to the intended diagnosis is not made mandatory.                                       | Minimality complements sufficiency; it is not license to omit required evidence.                                                      |
| `D144` | **Evidence-to-Decision Traceability**     | `CT`      | A diagnostic decision or conclusion can be traced back to the evidence and rule that supported it.                       | Different from source provenance: this traces justification rather than origin alone.                                                 |
| `D145` | **Conformity under Uncertainty**          | `CT→ / R` | When observations are uncertain, pass/fail or attribution claims do not imply more certainty than the evidence supports. | Particularly important for Realization evidence; Contract judgments must define how uncertain input participates rather than hide it. |
| `D146` | **Diagnostic Profile / Capture Contract** | `CT / R`  | Explicit user choice about optional diagnostic depth or capture intent.                                                  | A profile configures enrichment; it must not redefine source semantics or weaken required evidence.                                   |

### 4.31. Design-Time Diagnosability and Novel Conditions

| ID     | Characteristic                           | Ownership | Meaning                                                                                                                                                                          | Difference from nearby characteristics                                                                                                                                                      |
|--------|------------------------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D151` | **Design-Time Diagnosability**           | `CT→ / R` | Diagnostic architecture is designed so that the states and Failures that matter can actually be observed or distinguished with available observation points and instrumentation. | `D041 Detectability` asks whether an already-given mechanism can detect a condition. Design-time diagnosability asks whether the system was made diagnosable before that mechanism is used. |
| `D152` | **Adaptive Evidence Acquisition**        | `CT→ / R` | Evidence acquisition can change its target, depth, location, or resolution when an anomaly or new diagnostic hypothesis justifies further observation.                           | `D094 Triggerability` starts deeper capture. Adaptive acquisition changes what is observed after the trigger rather than merely turning a fixed recorder on.                                |
| `D153` | **Known-Fault / Novel-Fault Separation** | `CT→ / R` | Distinguishes a condition that matches a known diagnostic class from one that is observably outside the known fault model.                                                       | `D040 Abstention` means evidence is insufficient to make a supported claim. Novel-fault recognition can support the narrower claim that the observed condition does not fit known classes.  |
| `D154` | **Operating-Envelope Shift Detection**   | `CT→ / R` | Detects that the current operating or environmental domain has moved outside the region for which a diagnostic method, model, or evidence relation was established.              | `D028 Applicability` defines where a diagnostic is valid. Envelope-shift detection determines that the current realization has crossed that applicability boundary.                         |

### 4.32. Confidence, Uncertainty, and Model Adequacy

| ID     | Characteristic                                 | Ownership | Meaning                                                                                                                                                                                     | Difference from nearby characteristics                                                                                                                                                                             |
|--------|------------------------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D155` | **Confidence Calibration**                     | `CT→ / R` | A stated diagnostic confidence should correspond to the diagnostic mechanism's demonstrated likelihood of being correct within the conditions where that confidence is used.                | `D039 Diagnostic Confidence` records how strongly a diagnosis is asserted. Calibration asks whether that stated strength is itself trustworthy.                                                                    |
| `D156` | **Uncertainty Decomposition**                  | `CT→ / R` | Keeps materially different sources of uncertainty distinguishable when they affect interpretation or action.                                                                                | `D035 Uncertainty` records uncertainty as a property. Decomposition distinguishes observation noise, model uncertainty, incomplete knowledge, or other sources when treating them as one value would hide meaning. |
| `D157` | **Model Adequacy / Model-Mismatch Visibility** | `CT→ / R` | Diagnostic use of a model does not hide evidence that the model itself is no longer adequate for the observed system or operating domain.                                                   | `D031 Measurement Model` identifies how observation is interpreted. Model adequacy asks whether that interpretation model still fits the situation being diagnosed.                                                |
| `D158` | **Physics / Domain Consistency**               | `CT→ / R` | A derived diagnostic conclusion can be checked against applicable physical, engineering, or domain constraints rather than being trusted solely because an inference mechanism produced it. | `D024 Constraint Relation` explains incompatibility among governed semantic materials. This characteristic checks the diagnostic inference itself against independently applicable domain constraints.             |

### 4.33. Evidence Diversity, Consistency, Fusion, and Refinement

| ID     | Characteristic                         | Ownership | Meaning                                                                                                                                                                                | Difference from nearby characteristics                                                                                                                                                                              |
|--------|----------------------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D159` | **Evidence-Source Diversity**          | `CT→ / R` | Diagnostic assurance can use materially independent evidence sources so that one observation mechanism or failure mode does not silently dominate the whole diagnosis.                 | `D048 Common-Cause Awareness` reasons about one cause behind multiple symptoms. Evidence-source diversity instead concerns avoiding a common weakness in the evidence-producing mechanisms themselves.              |
| `D160` | **Evidence Consistency Checking**      | `CT→ / R` | Evidence obtained from different observations or models is checked for semantic compatibility when those sources are used together.                                                    | `D021 Conflicting Material` concerns material that cannot satisfy governing rules. Evidence consistency concerns disagreement among the diagnostic inputs themselves before a combined diagnostic claim is trusted. |
| `D161` | **Explicit Evidence-Fusion Semantics** | `CT→ / R` | When multiple evidence sources contribute to one attribution, the rule by which they are combined is explicit enough that the resulting claim does not appear stronger than its basis. | `D058 Correlation` only says records belong to the same context. Fusion explains how several correlated records become one diagnostic conclusion.                                                                   |
| `D162` | **Monotonic Evidence Refinement**      | `CT→ / R` | Additional evidence refines or narrows a diagnostic account according to defined relations rather than causing unsupported oscillation between incompatible claims.                    | `D050 Evidence Convergence` describes increasing specificity as evidence arrives. This characteristic makes the allowed direction of that refinement an explicit property of the diagnostic reasoning process.      |

### 4.34. Diagnostic Context Windows and Semantic Observation

| ID     | Characteristic                     | Ownership     | Meaning                                                                                                                                                                                                       | Difference from nearby characteristics                                                                                                                                                                          |
|--------|------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D163` | **Diagnostic Context Window**      | `CT→ / C / R` | A diagnostic may require a bounded sequence of material before, at, or after an occurrence when a single snapshot cannot explain the condition.                                                               | `D067 Frozen Historical Evidence` preserves material at a defined point. A context window preserves an explicitly bounded interval or sequence around that point.                                               |
| `D164` | **Qualified Negative Evidence**    | `CT→ / C / R` | Absence of an observation is used as evidence only when the observation scope, capability, and relevant time window make that absence meaningful.                                                             | `D020 Missing Material` states semantic absence of required material. Negative evidence is an observational claim and is invalid when the mechanism could simply have failed to observe the material.           |
| `D165` | **Semantic Event Construction**    | `C / R`       | Raw log, trace, metric, or low-level records can be assembled into diagnostic events that correspond to meaningful system actions or conditions before higher-level reasoning uses them.                      | `D075 Semantic Compression` reduces diagnostic material for explanation. Semantic event construction occurs earlier and changes raw observations into analyzable semantic units.                                |
| `D166` | **Cross-Layer Semantic Alignment** | `CT→ / C / R` | Evidence originating in lower software, operating-system, virtual-machine, firmware, or hardware layers is related to the higher-level operation, resource, or Contract meaning that it can actually support. | `D074 User-Language Projection` controls how the final explanation is expressed. Cross-layer alignment is needed during analysis so that low-level events are not attributed to the wrong higher-level meaning. |

### 4.35. Observation Bias, Precision Escalation, and Runtime Observability

| ID     | Characteristic                      | Ownership     | Meaning                                                                                                                                                                                   | Difference from nearby characteristics                                                                                                                                  |
|--------|-------------------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D167` | **Observation-Method Bias**         | `CT→ / C / R` | The observation technique itself can systematically distort which behavior appears important, and that bias must not be mistaken for properties of the diagnosed system.                  | `D035 Uncertainty` covers lack of exact knowledge. Bias can remain directional and repeatable even when individual measurements appear precise.                         |
| `D168` | **Precision Escalation**            | `C / R`       | A cheap or sampled observation can be replaced or supplemented by more precise instrumentation for the narrowed subject when the original evidence cannot support the required diagnosis. | `D093 Selective Capture` chooses where to collect. Precision escalation specifically changes the quality or exactness of observation after an insufficiency is found.   |
| `D169` | **Observability Quality Vector**    | `CT→ / R`     | Diagnostic observability is treated as several independent qualities such as coverage, timeliness, granularity, flexibility, and cost rather than one scalar richness level.              | `D091 Diagnostic Depth` describes an overall enrichment choice. A quality vector preserves trade-offs that cannot be represented correctly by one level.                |
| `D170` | **Diagnostic Data-Plane Isolation** | `R`           | Expensive diagnostic processing, aggregation, or storage can be separated from the critical execution path whose behavior is being diagnosed.                                             | `D103 Cost Boundedness` states the cost requirement. Data-plane isolation is one runtime architecture for meeting that requirement without changing diagnostic meaning. |

### 4.36. Comparative Diagnosis, Semantic Boundaries, and Oracles

| ID     | Characteristic                                    | Ownership     | Meaning                                                                                                                                                                            | Difference from nearby characteristics                                                                                                                                                                      |
|--------|---------------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D171` | **Semantic-Stable Observation Boundary**          | `C / R`       | Comparison and observation use boundaries whose meaning remains stable enough across implementation changes to support reliable diagnostic comparison.                             | `D013 Transformation Provenance` follows material through transformations. A semantic-stable boundary chooses where comparison remains meaningful without requiring every internal representation to match. |
| `D172` | **Comparative / Differential Diagnosis**          | `C / R`       | Diagnosis can compare a failing execution, configuration, or component with a suitable normal or peer case and reason from the differences.                                        | `D032 Reference / Baseline` identifies a comparison basis. Differential diagnosis uses the relation between two comparable executions or populations as an active diagnostic method.                        |
| `D173` | **Diagnostic Oracle Strength**                    | `CT→ / C / R` | A diagnostic conclusion may be no stronger than the correctness or reference oracle against which its evidence is interpreted.                                                     | `D032 Reference / Baseline` identifies what is compared. Oracle strength asks what kinds of correctness claims that reference is actually capable of supporting.                                            |
| `D174` | **Correctness / Quality-Degradation Separation**  | `C`           | Compiler diagnostics distinguish semantic incorrectness from valid output whose optimization quality, performance, security surface, or other implementation quality has degraded. | A Contract Failure states unsatisfied required meaning. Quality degradation can deserve a compiler remark or investigation even when semantic correctness remains intact.                                   |
| `D175` | **Semantic Checker / Symptom Monitor Separation** | `CT→ / C / R` | Diagnostics distinguish checks of domain or Contract semantics from generic symptoms such as timeout, resource spikes, exceptions, or low-level error signals.                     | `D122 Detection / Diagnosis Separation` separates noticing a condition from explaining it. This characteristic distinguishes what kind of meaning a detector is capable of checking in the first place.     |

### 4.37. Transformation Correctness, Scoped Completeness, and Validation

| ID     | Characteristic                                     | Ownership     | Meaning                                                                                                                                                                                                      | Difference from nearby characteristics                                                                                                                                                                                  |
|--------|----------------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D176` | **Diagnostic-Metadata Transformation Conformance** | `C`           | Diagnostic and debug metadata that survives optimization or lowering is verified to remain truthful for the transformed program, and is updated or invalidated when it no longer conforms.                   | `D013 Transformation Provenance` preserves links across transformation. Conformance asks whether those preserved links are still semantically correct.                                                                  |
| `D177` | **Scoped Completeness**                            | `CT→ / C / R` | A diagnostic may make a completeness guarantee for an explicitly bounded subject even when the whole system is not completely observable.                                                                    | `D060 Completeness` asks whether required material is present. Scoped completeness additionally states the exact domain over which that completeness claim is valid.                                                    |
| `D178` | **Fault-Injection Validation**                     | `C / R`       | Diagnostic detection, isolation, and evidence paths are validated by deliberately introducing representative faults or failures rather than testing only nominal execution.                                  | `D117 Self-Test / Proof-Testability` checks that the mechanism can exercise itself. Fault injection evaluates its behavior against a population of externally introduced failure conditions.                            |
| `D179` | **Reproducer Environment Fidelity**                | `C / R`       | A reproducer preserves enough relevant environment, configuration, scheduling, state, or resource conditions to make a repeated diagnostic investigation semantically comparable to the original occurrence. | `D110 Reproducibility` is the general ability to reproduce. `D111 Reproducer Material` identifies needed artifacts. Environment fidelity asks whether the recreated surrounding conditions are sufficiently equivalent. |

### 4.38. Trigger Semantics, Heterogeneous Evidence, and Diagnostic Self-Management

| ID     | Characteristic                               | Ownership     | Meaning                                                                                                                                                                   | Difference from nearby characteristics                                                                                                                                                                        |
|--------|----------------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `D180` | **Trigger / Cause Separation**               | `CT→ / C / R` | The event that caused diagnostic collection or investigation to begin is not automatically treated as the cause of the diagnosed condition.                               | `D059 Causal Ordering vs Temporal Ordering` rejects causal inference from sequence alone. Trigger/cause separation applies that rule specifically to diagnostic activation.                                   |
| `D181` | **Heterogeneous Evidence Type Preservation** | `CT→ / R`     | Logs, metrics, traces, semantic records, and hardware evidence can be correlated without erasing the different evidential meanings and limitations of their source types. | `D087 Data Processing / Communication / Presentation Separation` separates pipeline responsibilities. This characteristic preserves evidence-type distinctions inside one diagnostic analysis.                |
| `D182` | **End-to-End Evidence-Metadata Continuity**  | `CT→ / C / R` | Provenance, integrity, validity, or related evidence metadata remains connected to the evidence as it crosses diagnostic layers and transformations.                      | `D071 Evidence Integrity` protects the material itself. Metadata continuity protects the information needed to interpret whether that material remains trustworthy after crossing boundaries.                 |
| `D183` | **Diagnostic Decision Self-Trace**           | `C`           | The diagnostic subsystem can explain why it emitted, suppressed, grouped, escalated, or downgraded a tool diagnostic.                                                     | `D114 Auditability` traces a diagnostic back to its supporting material. Self-trace additionally exposes the diagnostic subsystem's own selection and presentation decisions.                                 |
| `D184` | **Evidence-Value-Aware Diagnostic Costing**  | `C / R`       | Expensive diagnostic enrichment is prioritized by expected explanatory value, relevance, or diagnostic utility rather than applied uniformly.                             | `D103 Cost Boundedness` limits total burden and `D148 Optional Enrichment Profitability` decides whether enrichment pays off. This characteristic also ranks where limited diagnostic budget should be spent. |

---

## 5. Diagnostic Contract Obligations Extracted from the Inventory

The inventory above is deliberately broader than this Contract. It records characteristics, engineering distinctions,
and implementation techniques that are useful during design. They do not become Contract coordinates merely because a
compiler, operating system, monitoring system, or engineered system benefits from them.

This section extracts the obligations that remain when implementation form is removed. These obligations are normative
for the Diagnostic Contract. The detailed decisions that follow refine them without requiring every inventory item to
become part of canonical Diagnostic meaning.

### 5.1. Diagnostic Does Not Own the Meaning It Explains

A Diagnostic must not become the authority that establishes the judgment, Failure, State-Machine decision, or
Realization result that it explains. The owning authority establishes that meaning first. Diagnostic material may
describe, support, or investigate it without replacing that authority.

The absence, loss, or failure of Diagnostic material therefore cannot erase or rewrite an already-established source
result. A Diagnostic subsystem may itself fail, but that failure is distinct from the result it was attempting to
explain.

### 5.2. Diagnostic Must Preserve Its Exact Subject and Semantic Origin

A Diagnostic must remain related to the exact subject and occurrence that it explains. It must also preserve enough
provenance to identify where the diagnostic meaning originated.

This obligation does not require a source file, line number, stack frame, or any particular frontend representation.
Those are possible realizations of provenance. Contract meaning requires the semantic relation, not one tool-specific
coordinate system.

### 5.3. Diagnostic Must Preserve the Basis of Its Account

A Diagnostic must preserve the material that justifies its diagnostic account. Where the account depends on a required
meaning, established material, observed material, missing material, conflicting material, or another declared relation,
those roles must remain distinguishable enough to explain the result correctly.

A Diagnostic must not reduce an explainable judgment to an opaque code when the applicable Diagnostic Contract requires
the basis needed to understand that judgment.

### 5.4. Different Evidential, Interpretive, and Causal Roles Must Remain Distinct

Diagnostic material with different semantic roles must not be silently collapsed into one undifferentiated value.
Established Contract or State-Machine material is not the same as observed realization material. When a diagnostic
account interprets observed realization material, that interpretation remains distinct from both the observation and the
diagnostic statement it supports. A cause is not a Failure mode, an effect, or a consequence. Temporal precedence is not
causality. A current observation is not historical evidence merely because it concerns the same subject.

This distinction does not introduce a Measurement Model into Contract or State-Machine authority. Contract and
State-Machine judgments use their established semantic material directly. A realization-side Measurement Model or other
interpretation mechanism may support Diagnostic material, but its concrete form remains a realization concern.

The Contract does not require every engineering distinction to become a universal field. It requires those distinctions
to remain intact whenever collapsing them would change what the Diagnostic claims.

### 5.5. Diagnostic Claims Must Remain within Their Applicability

A Diagnostic must not generalize evidence beyond the conditions under which that evidence is meaningful. Applicable
Version, Policy World, State, operating condition, assumption, reference, measurement condition, or other relevant
context must remain related to the diagnostic account when interpretation depends on it.

Where the source authority already owns applicable context, the Diagnostic refers to that meaning rather than redefining
or duplicating it as a competing authority.

### 5.6. Diagnostic Must Not Claim More Certainty than Its Basis Supports

A Diagnostic must not present uncertain, approximate, invalid, unavailable, or weakly attributable material as stronger
knowledge than its source supports. Known uncertainty and limits of attribution must remain visible in the diagnostic
meaning when they affect interpretation.

When the available material cannot support a conclusion, the Diagnostic may remain explicitly unknown or abstain from
attribution. It must not manufacture a definite cause to complete a preferred presentation shape.

### 5.7. Diagnostic Availability Must Not Imply Universal Coverage

The existence of Diagnostic machinery must not imply that every possible Failure or condition is detectable or
diagnosable. A Diagnostic guarantee extends only across the domain for which the applicable Contract can actually
establish the promised material.

In particular, strong diagnostic coverage over Contract judgments does not imply equivalent coverage over user
realization, operating-system behavior, hardware behavior, or other boundaries that Kontrakt does not fully own.

### 5.8. Detection and Attribution Strength Must Remain Distinct

Knowing that a problem or abnormal condition exists is not the same as identifying its exact source. A Diagnostic must
not claim finer isolation or specificity than its evidence supports.

When several causes remain plausible, the Diagnostic may preserve alternatives, a common-cause relation, or an
unresolved attribution. It must not convert diagnostic convenience into false semantic precision.

### 5.9. Known Incompleteness Must Remain Visible

The presence of some Diagnostic Evidence does not imply that the evidence is complete. Known loss, partial capture,
overflow, truncation, unavailable material, or other incompleteness must not be represented as a complete account when
that distinction matters to interpretation.

A stronger declared evidence obligation cannot be satisfied by silently weakening it to whatever material happened to
remain available.

### 5.10. Diagnostic Must Preserve Temporal Fidelity

A Diagnostic must preserve the temporal meaning of its material. Occurrence, observation, capture, and later
availability may happen at different times. A value reconstructed or read later must not be represented as though it
were frozen at the original occurrence.

Current state, occurrence-time evidence, and later reconstruction may all be useful, but the Diagnostic must preserve
their different temporal status.

### 5.11. Diagnostic Material Must Preserve Integrity and Origin

Diagnostic material must not be silently altered, mixed with another occurrence, or detached from its claimed origin in
a way that changes its meaning. Retention, transport, rendering, and backend conversion must preserve the identity and
semantic relation required to interpret the material.

This obligation defines semantic integrity. It does not require one storage format, cryptographic mechanism, or
transport protocol.

### 5.12. Evidence, Explanation, and Guidance Are Different Responsibilities

Evidence states diagnostic material. Explanation turns that material into an account that a consumer can understand.
Guidance suggests what the consumer may do next. These responsibilities must not be collapsed.

Explanation may derive from Evidence but cannot invent new authoritative facts. Guidance may recommend a correction but
cannot present that recommendation as though the Contract had already established it as the only valid action.

### 5.13. Diagnostic Explanation Must Be Expressible in the Subject's Meaning Vocabulary

A Diagnostic must remain explainable in the vocabulary of the meaning it diagnoses rather than requiring the consumer to
understand the diagnostic implementation's internal representation.

For Kontrakt Contract material, that means explanation can refer to declared Contract subjects, authorities, worlds,
States, Failures, Publications, Outputs, and other established machine meanings. Compiler ordinals, internal nodes,
lowered gates, stack frames, and backend object identities may support an engineering view without becoming the required
user explanation.

### 5.14. Required Diagnostic Material Must Be Sufficient and Relevant

A required Diagnostic account must contain enough material to understand the subject and the applicable diagnostic
judgment. At the same time, the Contract must not make unrelated material mandatory merely because it may occasionally
be useful during investigation.

Diagnostic richness is therefore not measured by raw volume. The obligation is sufficient relevant material, not maximal
capture.

### 5.15. Required Diagnostic Material and Optional Enrichment Must Remain Distinct

Material required by the Diagnostic Contract and additional investigative material are different obligations. Optional
depth may add broader history, backend observations, traces, snapshots, or other enrichment, but it cannot redefine the
guaranteed diagnostic core.

A configuration that lowers optional diagnostic depth may reduce cost. It cannot remove material that the applicable
Contract declares required.

### 5.16. Diagnostic Cost Control Must Not Weaken Required Guarantees

Diagnostic work may be bounded, selective, delayed, deduplicated, sampled, or otherwise optimized where the applicable
obligation permits it. Cost control cannot silently convert required evidence into best-effort evidence.

The Contract defines the guarantee before an implementation chooses how cheaply to realize it. Concrete capture
thresholds, sampling algorithms, buffering, lazy materialization, and profitability decisions belong to the compiler or
generated-system architecture.

### 5.17. Diagnostic Must Not Interfere with Source Semantics

The presence, absence, configured depth, capture strategy, or internal failure of Diagnostic machinery must not change
the meaning of the Contract judgment or State-Machine result it observes. Diagnostic work must not create a second
execution semantics for the machine merely to make investigation easier.

Where observation has unavoidable realization cost or perturbation, that limitation belongs to the Diagnostic capability
and realization guarantee rather than being hidden as though observation were free.

### 5.18. Diagnostic Mechanism Failure Is a Separate Condition

A Diagnostic mechanism can fail, lose material, become unavailable, or provide weaker evidence than intended. Such a
condition must remain distinguishable from the original judgment or Failure being diagnosed.

Diagnostic self-checking, regression testing, proof testing, and mechanism health are realization and assurance
techniques. Their Contract consequence is that a broken Diagnostic path cannot masquerade as evidence that the diagnosed
condition did not occur.

### 5.19. Diagnosis Is Distinct from Detection, Prognosis, Alert, and Recovery

This ADR does not make Diagnostic the authority for every activity surrounding a problem. Detection of an abnormal
condition, explanation of an established result, prediction of future state, decision to alert a human, recovery action,
and maintenance action are different responsibilities.

A later ADR may connect those responsibilities, but Diagnostic must not acquire their authority merely because
diagnostic information is useful to them.

### 5.20. Diagnostic Meaning Is Independent of Representation and Transport

Human text, IDE annotations, structured records, machine protocols, localization, storage formats, and external
transports may represent the same Diagnostic meaning. None of those representations becomes the Contract merely by
carrying it.

A representation may expose more or less optional material according to its purpose, but it must not silently change the
identity or required meaning of the Diagnostic occurrence it represents.

### 5.21. Diagnostic Existence Does Not Grant Disclosure Authority

Internal Diagnostic Evidence may exist without authority to expose it outside the Contract Machine. Availability to a
diagnostic subsystem, operator, debugger, or backend does not itself authorize Publication or Output.

Any outward exposure remains subject to the existing Publication and Output contracts. Diagnostic does not create a
parallel export boundary.

### 5.22. Establishment, Availability, Retention, Persistence, and Archival Are Distinct

A Diagnostic occurrence may have been established even when its material is no longer available. Retention controls how
long established material remains available. Persistence concerns survival across a stronger lifecycle boundary such as
restart. Archival is a separate long-term preservation responsibility.

Deletion, expiry, or eviction therefore does not rewrite the historical semantic fact that the Diagnostic occurrence was
established, and long storage does not make retained material more authoritative than it originally was.

### 5.23. Diagnostic Meaning Must Remain Stable across Version and Representation Change

A Diagnostic definition and occurrence must remain interpretable against the Contract definition and Version to which
they apply. A renderer, backend, storage schema, or diagnostic implementation may evolve without silently changing an
already-established Diagnostic meaning.

Where a Contract change changes the meaning or applicability of a Diagnostic definition, that change must be handled as
Contract evolution rather than hidden behind a stable message string or implementation identifier.

### 5.24. Required Diagnostic Meaning Must Be Deterministic and Auditable

For the same authoritative occurrence and the same applicable Contract material, required Diagnostic meaning must not
depend on nondeterministic renderer order, thread scheduling, incidental object identity, or whichever diagnostic
consumer happens to observe it first.

The relation from subject and basis to the required diagnostic account must remain inspectable enough to verify why that
account belongs to the occurrence. Reproducer files, replay tools, test harnesses, and proof artifacts may strengthen
this property, but no particular mechanism is part of the Contract definition.

---

## 6. Contract Decision — Diagnostic Evidence

### 6.1. Diagnostic Evidence Is a Contract over Explanation Material

Diagnostic Evidence is Contract meaning about the material that a machine must be able to establish as evidence for an
exact diagnosable occurrence.

It does not repeat the judgment. It states which supporting material belongs to the diagnostic account of that judgment
when the evidence obligation applies.

```text
exact machine judgment
    establishes Result or Failure
        ↓
Diagnostic Evidence definition
    identifies allowed supporting material
        ↓
Diagnostic Evidence occurrence
    freezes the material for this exact occurrence
```

The evidence occurrence is authoritative only about its own diagnostic statement. It can authoritatively state that a
declared value was the value captured from an exact source for this occurrence. It cannot reinterpret the original
judgment or replace the authority that established it.

This distinction is important. Calling evidence entirely non-authoritative would make declared Contract Evidence no
stronger than a log. Giving it authority over the source judgment would recreate the Failure problem. Diagnostic
Evidence therefore has a narrow authority: the truth of the declared diagnostic material and its relation to the exact
source occurrence.

### 6.2. Evidence Is Not Failure Meaning

ADR-0057 remains authoritative over Failure.

A Failure already states its source, failed meaning, applicable context, and boundary according to that ADR. Diagnostic
Evidence may refer to those coordinates when they are relevant to investigation. It does not move them out of Failure or
make Failure depend on a retained diagnostic record.

Consider a stock obligation that establishes `RequiredStockUnavailable`. If requested quantity is part of the Failure's
applicable context because the Failure cannot be interpreted without it, that value remains Failure meaning. If an
additional observed inventory sample is useful only to explain how the source judged the obligation, that sample may be
Diagnostic Evidence instead. The ownership is decided by whether the material is required to state the Failure itself,
not by whether an operator would like to see it.

The diagnostic layer therefore cannot be used to shrink Failure into a code plus an evidence bag. The same rule applies
to successful Results and State-Machine judgments. Material essential to their semantic meaning stays with the owning
Contract.

### 6.3. Evidence Is Not Limited to Failure

Diagnostic Evidence may be declared for an established judgment that is useful to explain even when no Failure exists.

A compiler or operator may need to know why a particular policy branch was selected, why a transition was accepted, or
which canonical values caused an invariant to pass. This does not require every judgment to emit evidence. It only means
that the Diagnostic Evidence Contract is not defined as a Failure attachment.

This avoids an artificial distinction where the machine is explainable only when it fails. It also allows verification,
audit, and controlled explanation of successful machine behavior without inventing success-flavored Failure objects.

V1 may support a narrower set of diagnosable sources than the theory permits. That limitation belongs to frontend and
backend capability, not to the definition of Diagnostic Evidence.

### 6.4. Definition and Occurrence Are Different

A Diagnostic Evidence definition is static Contract material. It identifies the diagnosable source and the material that
an occurrence must establish.

A Diagnostic Evidence occurrence belongs to one concrete machine occurrence. It contains or references the frozen values
established for that occurrence and is tied to the exact source result it explains.

```text
Diagnostic Evidence Definition
    source = exact canonical judgment authority
    material = closed declared selection

Diagnostic Evidence Occurrence
    definition identity
    source-result occurrence identity
    frozen selected material
```

The definition must not depend on runtime object identity. The occurrence must not depend on the address of an evidence
buffer or the sequence number chosen by a storage backend.

Frontend shorthand may eventually allow concise selection, but canonical lowering must resolve that shorthand to exact
sources before it becomes authority. No runtime wildcard may decide which judgment an evidence record belongs to.

### 6.5. Evidence Material Comes from Declared Sources

A Diagnostic Evidence definition may select only material that the exact source can establish or expose to diagnostic
lowering.

That material may already exist as a result coordinate, a Failure context coordinate, a canonical judgment input, or a
declared observation produced while the source judges its obligation. The evidence layer cannot call arbitrary user code
to manufacture a value after the fact. It cannot inspect a private field, walk an object graph, invoke a callback, or
infer a value from a stack frame and then claim Contract authority for it.

The owning source therefore needs a finite diagnostic surface. This does not require every internal intermediate value
to become a public Contract coordinate. It requires the compiler to know which material may legally be selected when a
Diagnostic Evidence declaration asks for it.

A future frontend may offer broad authoring conveniences. Canonical material still contains the exact source and exact
selected coordinates. The machine never performs dynamic discovery of diagnostic fields.

### 6.6. Evidence Selection Is Closed

A Diagnostic Evidence definition declares a closed set of evidence coordinates.

Runtime processing cannot append an undeclared debug field because it looks useful. A backend cannot silently add a
stack trace to the Contract occurrence. An adapter cannot inject environment variables or raw payload bytes into the
same Contract Evidence object.

This law serves two purposes. It makes the evidence guarantee finite, and it limits accidental retention of sensitive
material. Rich implementation diagnostics may carry more information in their own records, but they remain a different
plane.

The closed shape also lets the compiler reason about cost before realization. A required evidence coordinate has a known
sort and source relation. That can participate in allocation, retention, publication, and backend-capability planning
without inspecting runtime logger configuration.

### 6.7. Evidence Preserves Exact Names and Meanings

Diagnostic Evidence does not rename source coordinates or derive new factual values through arbitrary expressions.

When evidence selects an existing Contract coordinate, its canonical meaning is the source coordinate's meaning. A
renderer may display a friendly label later. A backend may encode the value differently. Neither changes the evidence
identity.

A derived explanation such as `remaining = allowance - consumed` is not automatically Diagnostic Evidence merely because
the operands are evidence. If the machine needs `remaining` as authoritative evidence, that meaning must already exist
at an owning authority that can establish it. Otherwise the subtraction belongs to an explanation or presentation tool.

This follows the same discipline as Output strict projection: a later boundary may present established meaning but may
not quietly create new factual authority.

### 6.8. Evidence Is Frozen to the Occurrence It Explains

Material that claims to describe a judgment occurrence is frozen to that occurrence.

If a value can change after the judgment, reading it later is not equivalent. The diagnostic subsystem may perform later
analysis, but it must not label a later observation as the earlier source value.

```text
judgment uses value = 4
        ↓
required evidence captures value = 4
        ↓
state later changes to value = 7

later value = 7
    cannot rewrite the evidence occurrence
```

This is the diagnostic counterpart of ADR-0057's frozen Failure context. The two freezes serve different authorities.
Failure context freezes the material needed to interpret Failure. Diagnostic Evidence freezes the declared supporting
material for the evidence occurrence.

If the same physical value is already frozen in Failure or Result material, the diagnostic definition should reference
that material rather than create a second semantic copy. A realization may physically duplicate bytes for storage, but
that duplication does not create another coordinate identity.

### 6.9. Occurrence Time and Observation Time Are Not Automatically the Same

Diagnostic systems frequently observe an event after the event occurred. Hardware may report a corrected error later. A
trace collector may timestamp receipt after the producer timestamp. A buffered runtime event may be drained by another
thread. Those times are useful, but the Contract must not silently treat one as the other.

Diagnostic Evidence therefore does not gain an implicit wall-clock coordinate. If event time is Contract meaning, it
must come from an existing declared time authority. Backend observation or collection time may be attached as
operational metadata without becoming that Contract time.

Where both are available, tooling may preserve both so investigators can reason about latency and ordering. Their
presence is a diagnostic capability, not a universal identity rule.

### 6.10. Provenance Is Part of the Evidence Relation

An evidence occurrence must retain enough canonical provenance to identify the exact source definition and source result
occurrence it explains.

The provenance is semantic, not a JVM stack. It survives backend replacement because it refers to Contract authorities,
Version-sensitive canonical definitions, selected World where applicable, and stable occurrence correlation owned by
Kontrakt rather than by a particular logging facility.

Source provenance does not imply that every diagnostic record repeats all of the source's context bytes. A compact
identity may resolve to immutable canonical material already known by the machine. The logical relation is required; the
physical encoding remains a backend decision.

This distinction allows V1 to use compact tables and V2 to use content-addressed or persistent identity without changing
what the evidence means.

### 6.11. Contract Evidence Is All-or-Nothing with Respect to Its Declared Shape

A Diagnostic Evidence occurrence is established only when its required coordinates have been established according to
the definition.

The Contract does not invent `PartialEvidence`, `TruncatedEvidence`, or `MaybeEvidence` as universal semantic states. If
the definition explicitly contains optional or absent coordinates through existing Contract absence law, that is part of
the closed shape. An unexpected capture failure is different.

This rule prevents a storage or tracing failure from silently weakening the Contract. A backend-specific dump may still
state that some frames were unavailable or some events were lost. That is useful operational evidence, but it is not a
complete occurrence of a stricter Contract Evidence definition.

### 6.12. Failure to Establish Evidence Does Not Rewrite the Source Result

A source Result or Failure remains established even if a later diagnostic obligation cannot be completed.

If the Diagnostic Evidence Contract itself has an applicable required obligation and the machine retains enough
authority to judge that obligation, inability to establish the required evidence may establish a separate Failure owned
by that diagnostic obligation. It does not replace, merge with, or retroactively invalidate the original source result.

```text
source Failure A is established
        ↓
Diagnostic Evidence obligation for A is evaluated
        ↓
evidence cannot be established
        ↓
possible Failure of the evidence obligation

Failure A remains Failure A
```

If abrupt realization loss prevents the evidence obligation from being judged at all, the machine must not invent a
secondary Failure merely to explain the missing diagnostic material. ADR-0057's rule about unknowable results still
applies.

### 6.13. Later Unreached Processing Remains Execution Evidence

ADR-0057 already rejects semantic outcomes such as `Skipped`, `Blocked`, or `NotEvaluated` for processing that was never
reached because an earlier Failure made it unreachable.

Diagnostic tooling may still explain the dependency relation. It may state that a particular source Failure prevented a
later dependent boundary from being entered, or show the last reachable machine boundary. That statement belongs to
execution evidence or an explanation graph. It does not establish a synthetic result for the unexecuted Contract.

The distinction is especially important for compiler and Whole-Machine diagnostics. A causal path can explain why a node
has no result without pretending that the node ran.

### 6.14. Explanation and Remediation Are Not Evidence

A diagnostic can contain more than evidence without giving every part the same authority.

A human explanation may summarize several evidence coordinates. A note may add context from another declaration. A help
message may suggest a change. A compiler fix may propose an edit. Those are tool products derived from the diagnostic
record.

The underlying Evidence remains the material that was actually established. A suggestion is not evidence that the
suggested edit is correct. An automatic fix therefore needs its own compiler-side applicability and validation rules.
The Contract Diagnostic Evidence model does not acquire a `hint` or `fix-it` semantic field merely because compiler UIs
need those concepts.

### 6.15. Diagnostic Evidence Is Selective, Not Universal

Kontrakt does not require every declared judgment to retain a full diagnostic record.

Some judgments may already be sufficiently explained by their Result or Failure material. Others may be so frequent that
retaining additional evidence would dominate runtime cost. A machine designer declares Diagnostic Evidence where
accountability requires more than the source result already provides.

The compiler may still generate its own source diagnostics or optional runtime instrumentation for undeclared cases.
Those facilities cannot be promoted to Contract guarantee after deployment by configuration alone.

Selective declaration keeps diagnostic cost visible and makes retained sensitive material reviewable before execution.
It also creates a clean V2 path for richer capture without turning V1 into an always-on flight recorder.

---

## 7. Contract Decision — Retention

### 7.1. Retention Governs Availability, Not Semantic Existence

Retention is separate from Diagnostic Evidence because it answers a different question.

Diagnostic Evidence states what evidence occurrence was established. Retention states the availability that the machine
must preserve for an established occurrence after that establishment.

```text
Evidence occurrence established
        ↓
Retention obligation applies
        ↓
evidence remains available within the declared boundary
```

Expiry, reclamation, or external archival behavior cannot rewrite the original evidence occurrence. Conversely, bytes
remaining in a cache or file after the guaranteed boundary do not extend the Contract.

This separates historical semantic truth from storage lifetime.

### 7.2. Retention Does Not Create Evidence

A Retention declaration cannot cause an evidence occurrence to exist when the corresponding Diagnostic Evidence was not
established.

It also cannot strengthen a partial operational trace into Contract Evidence. Retention operates only on material whose
own authority has already been established.

The compiler must therefore resolve Retention against an exact retainable source. A storage implementation cannot define
a new retention subject merely because it can write arbitrary objects.

### 7.3. Retention Guarantees Must Be Realizable Before They Are Promised

A Contract retention guarantee is stronger than best-effort logging.

If a target cannot provide the requested availability under the required boundary, the backend must reject the
realization or require a different declared Contract. It cannot silently downgrade `must remain available` to `normally
stays in a ring buffer`.

This is the same fail-closed capability boundary used elsewhere in Kontrakt. The frontend declares semantic intent. The
backend states what it can realize. Capability matching decides whether the combination is valid.

### 7.4. Retention Duration and Storage Capacity Are Different Responsibilities

Retention describes how long or through which Contract lifecycle boundary evidence must remain available. The amount of
physical storage required to honor that promise belongs to resource planning and Capacity.

A declaration such as `retain this evidence through boundary X` does not become `keep the newest 1000 records` simply
because the backend uses a ring buffer. A ring buffer of insufficient size would fail to realize the retention guarantee
under the expected admission bounds.

Likewise, a count limit is not automatically a retention semantic. If the product later needs a Contract that promises
`the latest N occurrences`, that is a separate meaning that should be designed explicitly rather than inferred from
storage implementation.

### 7.5. Expiry and Eviction Must Not Be Confused

Expiry is the end of a declared availability obligation.

Eviction is a physical act used to reclaim storage. A backend may evict an occurrence after its retention obligation has
ended. Evicting it earlier violates the guarantee unless another retained representation still satisfies the same
availability obligation.

Priority-based displacement, oldest-record overwrite, compression, external archival, or tiered storage are realization
strategies. They can be used only when their worst-case behavior still satisfies the declared Contract.

This distinction matters because mature diagnostic systems often use bounded storage and overwrite policies. Those
mechanisms are suitable for optional operational evidence. They are not sufficient by themselves to prove Contract
retention.

### 7.6. Retention Does Not Imply Persistence Across Restart

Retention across a logical boundary and durability across physical failure are not synonyms.

A V1 backend may be able to retain evidence during an Interaction or another supported in-process lifetime without being
able to guarantee survival across process restart, host reboot, disk loss, or machine loss. Those stronger guarantees
require explicit lifecycle and capability semantics.

This ADR therefore does not infer persistence from words such as `store`, `record`, or `retain`. If later ADRs add
restart-stable or failure-domain-stable retention, the backend must state the persistence mechanism and capability
separately.

### 7.7. Retention Storage Is Not Publication

Evidence can be retained internally without being authorized for an outside consumer.

A database row, diagnostic file, telemetry buffer, or remote archival system is not automatically a Publication
boundary. If retained Contract Evidence is to become an outward claim, ADR-0058 and ADR-0059 still apply.

The eventual path is conceptually:

```text
retained Diagnostic Evidence
        ↓
Publication authorization for that evidence source
        ↓
Output strict projection
        ↓
outside
```

ADR-0058 does not yet define the final Diagnostic Evidence publication source grammar. Until that refinement exists,
retention provides internal availability only. A debug endpoint must not bypass Publication by exposing a storage record
directly.

### 7.8. Retention and Confidentiality Are Related but Not the Same Contract

Retained diagnostics can be more sensitive than ordinary Output because they may contain fault-time values that were
never intended for external use.

The safest first rule is minimization: do not declare evidence that the Contract does not need. Retention cannot solve
confidentiality by itself, and this ADR does not introduce a generic redaction transform that would conflict with strict
projection law.

A backend may encrypt storage or restrict operator access as an implementation control. Future Contract work may add a
separate confidentiality authority if machine-level guarantees are required. Those controls do not alter the meaning of
the retained evidence.

### 7.9. V1 Retention Must Stay within Closed Lifecycle Semantics

The current Scope and Lifecycle model is not yet complete enough to promise every possible retention boundary.

V1 therefore should support only retention forms whose beginning and end are already explicit in the Contract Machine or
can be added without inventing process-specific semantics. Unsupported stronger requests fail during realization rather
than being interpreted through JVM process lifetime.

The canonical Retention model must still be designed so V2 can add stronger lifecycle and persistence capabilities
without changing existing evidence identity. Physical storage location, queue index, filename, and object address cannot
be part of Retention identity.

---

## 8. User Diagnostic API and Frontend

### 8.1. The User Declares Accountability, Not Logging Behavior

The user-facing Diagnostic API exists to declare which machine occurrences require additional accountable material.

It is not a logging DSL. The user does not choose a logger class, message template, sink, stack depth, serialization
format, or callback. Those are realization and presentation choices.

A conceptual declaration has two semantic parts: an exact diagnosable source and a closed selection of material owned by
that source. Retention is declared separately because an evidence shape and an availability promise are different
Contracts.

The following is an illustrative semantic form, not final `.kontrakt` syntax:

```text
Diagnostic Evidence:
    source = InventoryAdmission.RequiredStock
    material = requestedQuantity, observedAvailableQuantity

Retention:
    evidence = InventoryAdmission.RequiredStock
    boundary = declared supported lifecycle boundary
```

The frontend may later provide more ergonomic authoring. Resolution must lower it to the same exact canonical relation.

### 8.2. Diagnostic Source Selection Resolves Before Runtime

A Diagnostic Evidence declaration cannot wait until runtime to decide which authority it refers to.

If the frontend eventually allows a broad selector for author convenience, the compiler expands it during definition
processing into exact diagnosable sources according to a closed rule. The canonical representation contains those exact
sources. Declaration order and runtime registration do not affect the result.

This avoids a diagnostic version of dynamic AOP matching where a new class or implementation method silently starts
producing Contract Evidence because it happens to match a pattern.

### 8.3. The Frontend Selects Existing Diagnostic Material

The user may select only material the owning source declares as available for diagnostic use.

A source can expose a diagnostic coordinate without exposing its whole internal representation. The compiler must reject
a request to inspect hidden user implementation state or to run an arbitrary expression after the judgment.

This creates a useful boundary for Contract authors. The author can make an important actual value diagnosable without
turning every internal computation into a Fact or Output coordinate.

The exact declaration mechanism for source-owned diagnostic coordinates remains a frontend design detail to be closed
after the semantic model is accepted. The canonical rule is already fixed: the source relationship is explicit and
finite.

### 8.4. No Executable Diagnostic Callback Enters Contract Meaning

The frontend must reject executable diagnostic callbacks as Contract material.

A declaration such as `onFailure { inspectObjectGraph() }` would make evidence depend on user implementation behavior
and runtime object shape. It would also make cost and determinism impossible to reason about before execution.

Generated code may call backend-owned capture primitives chosen by the compiler. That is realization of a static
evidence obligation, not user-supplied diagnostic control flow.

### 8.5. Human Message Text Is Not Contract Identity

The Contract declaration should not require a human error message to serve as semantic identity.

Documentation may eventually attach explanatory text for tooling, but text can change for clarity without creating a new
Contract definition. Stable identity comes from canonical source and evidence material, not punctuation or English
wording.

This leaves room for multiple renderers and later localization while preserving one Contract meaning.

### 8.6. Severity Is Not a Universal Diagnostic Contract Coordinate

Many engineering systems classify events by severity, but those scales mean different things in different authorities. A
compiler warning severity, a spacecraft event severity, a hardware correctable-error class, and an application business
failure are not one taxonomy.

ADR-0060 therefore does not add a universal Contract `severity`. If a particular Contract domain later needs severity as
machine meaning, that domain can own it. Compiler diagnostics and backend operational events may use their own severity
models without leaking them into Diagnostic Evidence.

### 8.7. Policy Worlds May Select Different Diagnostic Contracts

Diagnostic Evidence remains a Contract and therefore participates in existing Policy composition rather than inventing a
runtime enable flag as semantic authority.

If one Policy World requires richer Contract Evidence than another, the difference must be visible in the selected world
before the governed scope begins. An operator cannot disable required Contract Evidence by mutating a logger setting
inside an active boundary.

Backend operational tracing may still be enabled dynamically because it is not Contract authority. The two controls must
not share one switch whose meaning changes depending on which consumer reads it.

### 8.8. Versioning Applies to Evidence Definitions

Changing the diagnosable source or closed evidence material changes the Diagnostic Evidence Contract definition.

A later compiler cannot interpret an old retained occurrence through a new definition merely because the display name is
the same. The occurrence remains bound to the canonical definition applicable when it was established.

This is important for long-lived retention. Storage schemas may evolve, but semantic decoding must preserve the Version
relation rather than applying the latest frontend declaration retrospectively.

### 8.9. Publication and Output Remain Explicit

A user may want internal evidence for engineering and a smaller external error response for service consumers. Those are
not contradictory requirements.

Diagnostic Evidence first exists inside the machine. Publication later determines which evidence material, if any, may
receive outward authority. Output then projects the exact outward shape. A backend cannot expose all retained evidence
through an administrative endpoint and call that equivalent to Publication.

This preserves least disclosure without adding a diagnostic-specific transformation language.

### 8.10. Generated Host APIs Are Artifacts

If Kontrakt generates Kotlin or Java accessors for retained evidence, those accessors are host artifacts. They do not
become the Contract definition.

The same applies to generated numeric IDs, table offsets, event classes, or storage handles. Canonical Contract identity
must survive if V2 changes the generated API or another backend uses a different representation.

### 8.11. Diagnostic Depth Is Configurable above the Guaranteed Evidence Core

A user needs control over how much diagnostic work the generated system performs, but one control cannot mean both
Contract guarantee and operational verbosity.

If richer evidence is part of accountable machine behavior, the user declares that material through Diagnostic Evidence
and, where applicable, selects it through the existing Policy World. The compiler resolves and lowers that obligation
before execution. A runtime setting cannot weaken it after the governed boundary begins.

Additional investigation is different. The user may choose an operational diagnostic profile that enables deeper JVM,
operating-system, hardware, trace, reproducer, or recent-history capture without changing the Contract. The exact API
and profile names remain open, but the semantic split is fixed: required evidence is a Contract choice; optional
diagnostic depth is an implementation choice.

A profile should describe meaningful capture behavior rather than expose a numeric verbosity scale whose cost and
semantics change between backends. One backend may obtain a stack cheaply while another cannot provide one at all. The
common configuration therefore names the diagnostic intent and lets backend capability resolution determine which
optional enrichments can realize it. Backend-specific overrides can exist outside Contract identity.

The lowest supported operational profile still preserves stable correlation to the authoritative occurrence. Higher
profiles may retain more context or activate targeted observation. None may replace the declared evidence shape with a
weaker partial occurrence.

---

## 9. Interaction with Existing Contracts

### 9.1. Failure

Failure remains the authoritative unsuccessful machine result. Diagnostic Evidence cannot redefine its source, failed
meaning, applicable context, or boundary.

A diagnostic failure is separate from the original Failure when an evidence obligation itself cannot be satisfied.
Unreachable later processing remains unexecuted rather than receiving synthetic failure-like statuses.

### 9.2. Publication

Diagnostic material remains internal unless Publication explicitly grants outward authority to the applicable evidence
source.

Internal availability, retention, operator access, or debugger visibility does not imply Publication.

### 9.3. Output

Once Diagnostic Evidence is publication-authorized, Output can expose only a strict projection of that authorized
material according to ADR-0059.

Output does not rename, redact, derive, or format new diagnostic facts. Consumer-specific transformation remains outside
the Core unless later Contract work explicitly adds another authority.

### 9.4. Policy and Governance

Policy may select different Diagnostic Evidence and Retention Contracts in different Policy Worlds because it already
selects combinations of one-dimensional Contracts.

Governance selects the applicable world. It does not dynamically edit an evidence definition inside a governed scope.
Operational tracing controls remain implementation controls unless represented through existing Contract selection.

### 9.5. Version

Evidence definition identity is Version-sensitive like other one-dimensional Contracts.

Retained occurrences decode against the definition that was applicable when they were established. A new compiler or
runtime cannot reinterpret historical evidence through a later Version.

### 9.6. Budget and Capacity

Required evidence capture consumes resources. Where Kontrakt owns the relevant resource region, Budget and Capacity
planning account for that realization cost.

Budget does not decide what evidence means. Capacity does not decide how long evidence must remain. They constrain the
physical realization that must satisfy those Contracts.

Optional operational diagnostics may have separate deployment budgets. Their exhaustion cannot silently remove required
Contract Evidence.

### 9.7. Whole Machine

Cross-Core diagnostic correlation may help reconstruct a system-level incident, but it does not create a hidden shared
Core or new causal Contract.

Each Core preserves its own evidence authority and outward boundary. External traces can be composed for investigation
without becoming Whole-Machine semantic communication.

---

## 10. V1 Boundary

V1 must represent Diagnostic Evidence definitions as explicit Contract material and keep definition identity separate
from runtime occurrence. It may support only the diagnosable sources that the current canonical IR and backend can
realize deterministically. A smaller V1 source set is acceptable; a hidden logging fallback is not.

The first frontend does not need the final convenience syntax. It must still resolve evidence definitions to exact
authorities, preserve the distinction between required evidence and optional diagnostic depth, and leave generated host
APIs outside Contract authority.

V1 Retention remains limited to lifecycle semantics that are already explicit. Stronger persistence guarantees are not
inferred from JVM process lifetime, file storage, or an operational collector.

## 11. Verification Requirements

### 11.1. Definition Verification

A Diagnostic Evidence definition must resolve to an exact source, a closed diagnosable material set, and the applicable
Version and Policy World. Hidden implementation state or executable callbacks cannot complete the definition.

Retention is checked only after its evidence source is valid. An unsupported lifecycle boundary or backend capability
does not imply a weaker guarantee.

### 11.2. Occurrence Conformance

An implementation that claims to establish Contract Evidence must be checkable against the canonical definition. The
exact physical representation may vary, but the decoded occurrence must refer to the exact source occurrence and contain
the complete declared material.

## 12. Open Decisions

### 12.1. Exact `.kontrakt` Diagnostic Evidence Syntax

The semantic model is decided before frontend convenience. The final spelling must not introduce executable diagnostic
callbacks or make human message text part of Contract identity.

### 12.2. Final V1 Diagnosable Source Set

Diagnostic Evidence is not limited to Failure in the theory. V1 may expose only source categories whose evidence can be
resolved and lowered deterministically with the current IR.

### 12.3. Source-Owned Additional Diagnostic Observation Syntax

Any additional diagnosable observation must remain finite, declarative, and owned by its source authority.

### 12.4. Retention Lifecycle Vocabulary

The exact lifecycle vocabulary waits for the remaining Scope and Lifecycle work. JVM process lifetime is not inferred as
a Contract scope.

### 12.5. Persistence across Restart and Stronger Failure Domains

Persistence across process restart, machine restart, power loss, host loss, or distributed failure requires explicit
lifecycle and backend capability semantics.

### 12.6. Diagnostic Evidence Publication Selector Refinement

Diagnostic Evidence cannot become outward authority merely because it is retained or internally available. The exact
Publication selector extension remains to be designed against ADR-0058.

### 12.7. Generic Severity, Confidence, Coverage, and Partial-Evidence Taxonomies

No universal Contract taxonomy is introduced merely because particular compilers, safety systems, hardware formats, or
observability systems use such concepts.

### 12.8. Generic Redaction or Diagnostic Transformation Authority

Strict projection and source minimization remain the current Contract tools. A stronger transformation or
confidentiality authority is not defined here.

## 13. Consequences

### Positive

Kontrakt gains an accountable diagnostic model without turning diagnostics into a second judgment system. Failure and
other machine results keep their existing authority, while declared evidence can be trustworthy Contract material rather
than best-effort logging.

The explicit Contract pipeline gives Diagnostic Evidence a stronger source relation than ordinary post-hoc
observability. Evidence can refer to the exact semantic occurrence it explains instead of reconstructing authority from
runtime shape.

A user can require a guaranteed evidence core without making every rich diagnostic facility part of Contract meaning.
Policy, Version, Publication, Output, Budget, and Capacity keep their existing responsibilities.

Retention becomes an availability guarantee rather than accidental storage behavior. Expiry or eviction cannot rewrite
the semantic history of the source occurrence.

### Negative

A real evidence guarantee can require memory, capture work, and retention capacity that a best-effort logger would
avoid. Some backend targets may have to reject stronger contracts instead of silently degrading them.

The distinction between Failure meaning, applicable context, Diagnostic Evidence, optional operational observation, and
Retention creates more semantic boundaries that the frontend and verifier must preserve correctly.

### Neutral

This ADR does not require every judgment to emit Diagnostic Evidence.

It does not require every retained occurrence to be published outward.

It does not define Kontrakt compiler diagnostics or generated-system operational diagnostic mechanisms.