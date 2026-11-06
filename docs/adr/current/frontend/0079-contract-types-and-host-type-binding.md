# ADR-0079: Type Interpretation and Semantic Binding

**Status:** Draft  
**Date:** 2026-10-25  
**Revised:** 2026-11-06

## Related

- `docs/the-most-important-thing/what-contract-is.md` — §§4, 17.5–17.6
- ADR-0046 — IDL-First Interface Contract Frontend
- ADR-0047 — One-Dimensional Contract Presentations, Source Carriers, and Backend Realization, especially §§7–9
- ADR-0053 — Version Contract, especially §§3.4–3.6
- ADR-0065 — Admission Contract, especially §§5.2, 7
- ADR-0063 — Contract Establishment, especially §4.7.1
- ADR-0071 — Resolved Contract HIR, especially §§4.3–4.5
- ADR-0073 — JVM Platform-Native Surface, especially §9
- ADR-0075 — Compiler Result Ownership and Reuse
- ADR-0078 — Deterministic Numeric Value and Operation Semantics
- ADR-0080 — Characters, Strings, and Unicode
- ADR-0084 — One-Dimensional Standard-Library Semantic Acquisition, especially §3
- ADR-0085 — Compiler Type Architecture and Information Lifecycle (*Draft*)

---

## 1. Premise

### 1.1. A Contract Is a Set of Software Obligations

Kontrakt defines a Contract as a declared set of obligations that software must satisfy (`What Contract Is` §4). The
author decides which obligations belong together. Those obligations may concern a value, an act, or a relationship
between values. They may also specify the rules of a computation without prescribing the machine code that realizes it.

A type can name such a set of obligations. The name helps a compiler identify what is being discussed, but it does not
create the obligations or judge their satisfaction (`What Contract Is` §17.5). This is a way to understand types in
relation to Kontrakt's Contract theory; it does not assert that every type in every programming language represents a
complete Kontrakt Contract.

Kontrakt already provides a means to declare and establish Contracts without using a host-language class as their
authority. IDL identifies the Interface, Interaction, and Operation subjects and selects the applicable one-dimensional
Contracts. Each 1D owns the meaning of the obligations declared through its presentation. The resulting candidates pass
through HIR and the owning Establishment judgments before authoritative Contract material is available (ADR-0046;
ADR-0047 §§6–8; ADR-0063).

### 1.2. Why Kontrakt Realizes Supported 1D Obligations

The user could write code to fulfill many of the obligations expressed by IDL and 1D Contracts. Kontrakt does not take
responsibility for their realization because user implementation is inherently impossible.

It takes that responsibility because the Contracts are intended to describe their obligations in enough detail for the
compiler to determine a lawful realization. With an established Contract, the compiler can examine the obligations
together instead of asking each user to reproduce the machinery needed to satisfy them. Reaching the same degree of
correctness and optimization in separately written implementations would increasingly require users to solve problems
already entrusted to the compiler.

Kontrakt therefore aims to deliver **world-leading implementation quality** for the obligations it can realize from
established meaning. This is a product and engineering objective, not a claim that the current compiler already achieves
it. The compiler may choose a different physical implementation as long as it preserves the established obligations.
ADR-0047 §9 governs this realization boundary.

The user Operation remains the ordinary boundary for user-provided realization. Kontrakt's responsibility for its
generated contractual machinery does not transfer the Operation's user-defined work to the compiler (ADR-0047 §9).

### 1.3. The Platform Does Not Define the Contract

Kontrakt uses Java and Kotlin to carry supported 1D declarations, and the JVM is its present execution target. User
Operation realization also crosses Java/Kotlin host-compilation boundaries. These choices require Kontrakt to understand
the existing languages and the JVM, even though its Contract theory was not derived from them.

Java organizes user-defined data and behavior chiefly through classes. Its interfaces state requirements that classes
may implement, while a class may also carry construction rules and executable method bodies. Kotlin permits more
source-level forms, including top-level functions, but its types and calls still have Kotlin-defined meanings and JVM
mappings. Primitive values and language-defined operators add further cases that cannot be reduced to class method
tables.

The host languages can calculate relationships between values. Their normal type systems, however, do not make those
relationships independent Kontrakt obligations merely because an expression is well typed. Nor do class membership,
method availability, or subtype acceptance establish the Contract authority of a 1D declaration. This is the
platform-expression limitation relevant to this ADR.

Kontrakt must therefore respect the host languages when interpreting authored expressions, without treating their models
of types or classes as its model of Contract authority. The JVM imposes a separate requirement: generated code must
satisfy the target's representation and verification rules. Those rules govern execution artifacts, not what a Contract
means (ADR-0073).

## 2. Problem

A selected 1D declaration can contain ordinary Java or Kotlin calculations. Before the owning Contract judges such a
declaration, Kontrakt must determine what the author actually expressed.

Consider a generic SDK method. Its declaration supplies a method signature, but that signature does not identify which
overload an authored call selected or which type arguments apply at that call. Conversely, identifying the overload does
not establish the method's complete calculation law. Kontrakt needs both the resolved source operation and the meaning
already qualified for that operation (ADR-0084 §3.1).

The frontend cannot leave these questions for Establishment. ADR-0071 §4.3 requires the relevant references to be
resolved before a candidate becomes visible as HIR; ADR-0063 §4.7.1 prohibits Establishment from recovering missing
candidate meaning from source or host structure.

The design problem is to obtain the exact type and call information needed for that interpretation, while avoiding two
mistakes: making Java/Kotlin type relationships a source of Contract authority, and carrying the host compiler's entire
type model into HIR simply because it was useful during resolution.

## 3. Decision

### 3.1. Kontrakt Resolves Its Supported Source Surface

**Ordinary 1D Contract compilation uses Kontrakt-owned Java/Kotlin source resolution.** It does not invoke `javac` or K2
to parse, type-check, or select calls in the authored 1D source. This decision does not change the separate
host-compilation path for user Operation realization.

For an expression within its supported language surface, the resolver must determine the source declaration or operation
actually selected under that language's rules. A JVM descriptor alone is insufficient when different source operations
share a JVM representation (ADR-0073 §9.1).

The resolver's obligations depend on the expression. The following cases illustrate distinct information it may need:

| Source case                       | Information required to interpret the use                                 |
|-----------------------------------|---------------------------------------------------------------------------|
| Generic invocation                | The applicable type arguments after source-language resolution            |
| Overloaded method                 | The declaration selected for the actual receiver and arguments            |
| Kotlin extension call             | The extension callable and receiver relationship selected by Kotlin rules |
| Implicit language conversion      | The conversion that participates in the authored calculation              |
| Lambda passed to an SDK operation | The resolved body and parameter meaning required by that operation        |

This table identifies categories of resolution, not a promise that the entire Java or Kotlin language is supported. The
exact accepted syntax and language-rule coverage remain open (§4). Within that coverage, the applicable Java or Kotlin
language rules and SDK profile follow the author's selected environment. The same source may select a different overload
under a different SDK profile. Kontrakt must interpret each selection under its own rules, not preserve a selection made
for an older profile.

For the same source-semantic inputs, Kontrakt must produce the same resolved meaning regardless of its resolver
implementation, worker schedule, or cache state. A language or SDK profile that Kontrakt cannot interpret sufficiently
must not be approximated with another profile's rules. ADR-0073 governs the platform semantic basis, and ADR-0075
governs
result validity and reuse. A change in the selected environment does not itself establish a new Contract Version; any
change to established Contract meaning follows the owning 1D and ADR-0053.

### 3.2. SDK Declarations Are Prepared Before User Compilation

Kontrakt prepares the Java/Kotlin SDK type and declaration information needed by the supported platform profiles before
ordinary user compilation. The frontend consults that prepared information instead of re-reading the installed libraries
to construct their declarations on every build.

Preparation uses the declarations of the selected, already-compiled SDK profile, including JDK classfiles and generic
signatures and Kotlin metadata. SDK sources and normative language specifications may support interpretation and
qualification; they do not replace the selected binary declarations by assumption. `javac` and K2 may be used during
preparation and conformance testing. Their results are evidence for the prepared knowledge; they are not the resolver
called during ordinary 1D compilation.

**Declaration knowledge and qualified operation meaning serve different purposes.** Declaration knowledge makes exact
source resolution possible. Qualified operation meaning supplies the behavior of the operation after it has been
selected. ADR-0084 §3 governs the latter. They may share physical storage, but a method signature must never be treated
as proof of its behavior.

#### 3.2.1. Prepared Lookup and Required Completion

V1's working direction is hybrid preparation. Kontrakt prepares a profile-bound lookup basis for the SDK declarations
relevant to its supported 1D source surface. Detailed declaration information may be completed when a source use needs
it; preparing the basis does not require eagerly expanding every SDK declaration. The resulting interpretation must
still account for every declaration that can affect the supported use. Finding one plausible method is not sufficient
when another applicable overload may exist.

Completion is measured against the actual source judgment, not against whether an individual classfile was parsed. If a
needed declaration or relation cannot be resolved sufficiently, the frontend cannot claim a successful source
interpretation (§3.5). This does not decide the full candidate-discovery rules for Java or Kotlin (§4), nor does it fix
an index, materialization policy, query boundary, or storage layout. Frontend Design owns those implementation choices;
ADR-0085 owns the lifecycle of compiler type information.

#### 3.2.2. Source Availability in the Permitted 1D Surface

A declaration present in an SDK binary is not necessarily available to the authored source. Preparation supplies the
relevant declaration facts; Kontrakt's resolver judges their use under the selected language rules and the actual source
context. Accessibility cannot be settled by one global availability flag attached to a method. Java access rules can
depend on the accessing location and receiver. Kotlin source visibility also differs from JVM binary visibility.
ADR-0073 §9.1 governs the distinction between source operations and JVM implementation methods.

The required coverage is the source surface permitted by the owning 1D, not every callable form available in Java or
Kotlin. For example, ADR-0065 §5.2 defines a flat Admission declaration: inheritance and virtual specialization do
not supply its judgment meaning. The Admission Authoring API Working Design reviews private helpers in that selected
class without approving same-name helper overloads or arbitrary external calls. Sealed authoring APIs may constrain
which implementations users can provide, but sealing does not itself decide member accessibility or create Contract
authority. For a supported private helper
or SDK call, the resolver must still apply the exact source access rules. Prohibited or unsupported forms do not become
lawful merely because a binary declaration can be found.

The frontend may prepare reusable accessibility facts, then make context-dependent decisions when interpreting the
use. It must not turn a declaration's binary presence into source permission. The exact access-rule coverage for each
supported Java/Kotlin source form remains open (§4); the preparation and lookup implementation remains Design.

### 3.3. Resolved Calls Use Qualified Meaning

After resolving the source operation under §3.1, the frontend uses the applicable qualified meaning provided under
ADR-0084. It must account for the actual type substitution and call-site conditions before composing that meaning into
the authored calculation.

A reusable generic interpretation can serve different type instantiations when its qualification covers those uses. The
compiler need not create a separate handwritten implementation for every instantiation. It may not reuse a meaning when
a difference in the selected operation changes observable behavior.

The host type identifies the source operation; it does not prove every fact relevant to its execution. For example, a
collection's static interface type does not by itself prove which stateful implementation will answer a virtual call.
When that distinction affects interpretation, the frontend must obtain sufficient meaning under ADR-0084 and the
applicable platform law. It cannot resolve the uncertainty by assuming a convenient implementation.

The owning 1D law remains responsible for its value and operation domain. Numeric calculation laws belong to ADR-0078,
and Character/Text laws belong to ADR-0080. This ADR supplies the source interpretation needed to reach those laws; it
does not rewrite them.

### 3.4. Forming the HIR Candidate

The frontend must finish the calculation meaning required by the owning 1D before publishing a Resolved Contract HIR
candidate. Establishment then receives that candidate through the protocol defined in ADR-0071, rather than receiving an
unresolved Java/Kotlin expression.

Only distinctions that determine the candidate's meaning need to survive source resolution as candidate meaning. An
intermediate inference constraint, for example, may be discarded once its result has been applied. A type-related
distinction that changes the applicable calculation cannot be discarded before the owning 1D can judge it.

This rule does not prescribe a universal type field in HIR or require HIR to store a copy of the SDK declaration
database. ADR-0071 §4.4 leaves each 1D responsible for its own candidate vocabulary. The exact type-related observations
needed by each 1D remain to be checked (§4).

Establishment does not upgrade a host type to Contract authority. It judges the resolved candidate under ADR-0063 and
produces the corresponding established meaning where the owning law permits. The exact information relationship between
particular HIR candidates and their Established Material remains part of the open cross-ADR review (§4).

### 3.5. Failure to Obtain Complete Meaning

If the source operation or its required qualified meaning cannot be resolved, the frontend cannot publish that
calculation as a complete HIR candidate. This is a compiler-side inability to form the candidate, not an established
violation of the owning Contract. A call forbidden by the selected source language's access rules and an inability to
verify those rules are distinct compiler-side failures. Neither establishes a 1D Contract refusal. ADR-0084 §3.4 governs
the judgment boundary; the diagnostic representation remains outside this ADR.

The compiler must not compensate for missing meaning by executing arbitrary authored code, accepting a valid JVM call as
sufficient evidence, or asking a user-supplied implementation to define what the Contract should mean (ADR-0047 §§7, 9;
ADR-0084 §§3.4–3.5).

## 4. Remaining Work

This Draft leaves the following decisions open. Listing them does not approve any proposed mechanism.

| Area                | Decision still required                                                                                        | Owner or boundary                            |
|---------------------|----------------------------------------------------------------------------------------------------------------|----------------------------------------------|
| Source coverage     | Exact supported Java/Kotlin expressions, declaration forms, and callbacks                                      | ADR-0079                                     |
| SDK profiles        | Supported version combinations; exact preparation coverage and conformance to the selected profile             | ADR-0079 / Design                            |
| SDK lookup          | Concrete candidate-discovery and completion rules under the selected hybrid preparation direction (§3.2.1)     | ADR-0079 / Frontend Design                   |
| Source availability | Exact access rules needed by the permitted 1D authoring surfaces, including context-dependent uses (§3.2.2)    | ADR-0079 / owning 1D / Frontend Design       |
| Type resolution     | Required rules and completion conditions for names, generic inference, overloads, conversions, and nullability | ADR-0079                                     |
| Resolution output   | Exact semantic result used to connect a selected source call to qualified SDK meaning                          | ADR-0079 ↔ ADR-0084                          |
| Semantic reuse      | When generic qualified meaning may be specialized without changing the selected operation's law                | ADR-0079 ↔ ADR-0084                          |
| HIR observations    | Which resolved type distinctions are determinants of each 1D candidate                                         | ADR-0079 ↔ ADR-0071 / owning 1D              |
| Establishment       | What is preserved as candidate meaning and what the owning judgment establishes as new authoritative material  | ADR-0063 / owning 1D; reviewed from ADR-0079 |
| Incomplete coverage | Precise compiler-side unsuccessful results for unsupported or unresolved forms                                 | ADR-0074 (*Proposed*) / ADR-0084             |
| Conformance         | How Java/Kotlin resolution and SDK interpretations are checked against supported language and library profiles | Design / Verification                        |
| Later consumers     | Type information needed outside the HIR candidate, including Realization and JVM backend use                   | ADR-0085 (*Draft*)                           |

Parser structure, inference algorithms, storage layouts, query indexes, and caches remain Design matters. ADR-0075
governs compiler-result validity and reuse. ADR-0073 §9.6 governs when execution may preserve a qualified platform call
rather than replacing its algorithm.

## 5. Consequences

Kontrakt must maintain conformance for the subset of Java/Kotlin resolution that it claims to support. A change in the
author's selected SDK may select a different operation from unchanged source; a change in Kontrakt's resolver
implementation must not change the correct meaning for the same semantic inputs. Release testing must cover the
source-language and SDK profiles claimed by the compiler; the detailed procedure remains open in §4.

Hybrid preparation removes repeated SDK-wide declaration discovery from ordinary user compilation without demanding
that every SDK detail be expanded in advance. Detailed completion and source-context access checks still have a cost.
Their representation, reuse, and performance policy remain Design matters under ADR-0075 and ADR-0085.

Users cannot assume that an expression accepted by `javac` or K2 is accepted as 1D Contract source. Kontrakt must
document the supported subset and report its own inability to interpret an expression before that expression is
submitted as a complete Contract candidate.

This document remains **Draft**. The open semantic coverage and information-preservation decisions in §4 must be
reviewed before acceptance.