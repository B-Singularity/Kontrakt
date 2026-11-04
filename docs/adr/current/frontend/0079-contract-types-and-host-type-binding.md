# ADR-0079: Type Interpretation and Semantic Binding

**Status:** Draft

**Date:** 2026-10-25

## Related

- `docs/the-most-important-thing/what-contract-is.md`, especially §17.5
- ADR-0047: One-Dimensional Contract Presentations and Source Carrier Law
- ADR-0063: Contract Establishment and Applicability
- ADR-0071: Resolved Contract HIR
- ADR-0073: JVM Platform-Native Surface and Preservation
- ADR-0075: Compiler Result Ownership and Reuse
- ADR-0078: Deterministic Numeric Value and Operation Semantics
- ADR-0080: Characters, Strings, and Unicode
- ADR-0084: One-Dimensional Standard-Library Semantic Acquisition and Interpretation Boundary
- ADR-0085: Compiler Type Architecture and Information Lifecycle (*Draft*)

---

## 1. Context

Supported one-dimensional Contracts may be authored using Java or Kotlin source carriers. Authors use ordinary language
and standard-library expressions to state calculations that Kontrakt must interpret before the owning Contract can judge
them. The source carrier supplies evidence; it does not acquire Contract authority (ADR-0047 §7–8).

A source type helps determine which declaration an expression denotes and how that declaration applies at the actual
call site. It does not, by itself, specify the complete behavior of a library operation. ADR-0084 already requires
Kontrakt to obtain that behavior from prequalified standard-library semantic knowledge. The Numeric and Character/Text
laws remain with ADR-0078 and ADR-0080; other data domains retain their own ownership.

This ADR concerns how Kontrakt interprets the types and calls in supported Java/Kotlin 1D source and uses those results
to select and apply qualified operation meaning. The later use and lifetime of type information throughout the compiler
belong to ADR-0085.

## 2. Problem

A prepared library declaration can describe a generic method without determining which overload a particular source
expression selects or which type arguments apply to that invocation. Conversely, resolving a method signature does not
establish the calculation's behavior, including any relevant evaluation or failure conditions.

Kontrakt needs both kinds of information to form an exact Contract candidate. Reanalyzing the installed JDK or Kotlin
standard library during each user compilation would repeat work already suitable for preparation. Requiring `javac` or
K2 to resolve ordinary 1D source would also make that compilation path depend on host-compiler analysis that Kontrakt
intends to perform itself.

The frontend therefore needs a defined separation between prepared SDK declaration knowledge, resolution of authored
source, and application of qualified SDK meaning.

## 3. Decision

### 3.1. Kontrakt-Owned Source Resolution

**Kontrakt resolves the supported Java and Kotlin 1D source surface itself during ordinary Contract compilation.** This
path does not invoke `javac` or K2 to parse, type-check, or resolve the authored 1D source.

The result must identify the actual source declaration or operation selected under the applicable language rules. When
the interpretation depends on an inferred type, a receiver, an argument binding, or a language-defined conversion, the
frontend must resolve that distinction rather than infer the operation from its name or JVM descriptor alone.

The supported source-language surface and the exact resolution obligations remain to be specified. This decision does
not change the separate host-compilation path for user Operation Realization.

### 3.2. Prepared SDK Type and Declaration Knowledge

Kontrakt prepares the type and declaration knowledge needed for its supported Java/Kotlin SDK profiles before ordinary
user compilation. The 1D frontend consumes that prepared knowledge instead of scanning or recompiling the standard
libraries on every use.

JDK classfiles and generic signatures, Kotlin metadata, and SDK source may contribute to preparation. Their exact
extraction and packaging mechanisms remain implementation decisions. Compiler tools, including `javac` and K2, may still
be used to prepare, audit, or test this knowledge outside the ordinary 1D compilation path.

**SDK declaration knowledge and qualified SDK operation meaning are distinct.** The former supports type and call
resolution. The latter is acquired under ADR-0084 and supplies the calculation meaning that a resolved call requires.
They may share physical infrastructure without being treated as the same semantic product.

### 3.3. Binding Resolved Calls to Qualified Meaning

Once the frontend has resolved an authored operation, it obtains the corresponding qualified meaning under ADR-0084. It
applies the type substitutions and call-site information needed to interpret that operation in its actual use.

A qualified generic meaning may serve multiple type instantiations where its applicability has been established. This
decision does not require a separate handwritten calculation implementation or stored meaning for every callable and
type combination. It also does not allow operations with different observable behavior to be merged merely because their
signatures are similar.

The owning data-domain law determines the meaning of its common values and calculations. Type interpretation supplies
the resolved source distinctions needed to connect the authored operation to those laws. If the available knowledge is
insufficient, the frontend cannot claim a complete interpretation; the unsuccessful result follows ADR-0084 §3.4.

### 3.4. Resolved Contract Candidate

The frontend composes the authored calculation from its resolved source operations and applicable qualified meanings. It
provides the candidate meaning required by ADR-0071 so that the owning 1D Contract can judge it without reopening the
authored source or reconstructing SDK behavior.

Type interpretation does not establish a Contract judgment. The owning 1D and Establishment retain that responsibility
under ADR-0063. The HIR need not retain the frontend's entire type model or SDK declaration store merely because those
materials were used during resolution.

## 4. Remaining Work

The following matters are not decided by this draft:

- The supported Java/Kotlin 1D expression surface and SDK profiles.
- The required coverage and representation of prepared Java/Kotlin type and declaration knowledge.
- The exact language-resolution rules Kontrakt must implement for names, types, generics, overloads, conversions, and
  authored callbacks.
- The logical result supplied by source type/call resolution and its connection to qualified SDK knowledge.
- The conditions for generic semantic reuse, domain binding, and composition of authored calculations.
- The type-related information required by each 1D HIR candidate and the corresponding unsuccessful-result cases.
- The conformance tests, preparation workflow, and compilation-cost targets for the independent frontend.

Parser design, inference algorithms, storage layouts, query indexes, and caches belong to Design. Type representation
and consumption in downstream compiler subsystems belong to ADR-0085. These open items do not become approved laws by
appearing in this list.

## 5. Consequences

Kontrakt takes responsibility for implementing and maintaining the language-resolution behavior it supports for 1D
authoring. That work can be prepared and tested independently of a user's build, while normal compilation performs only
the source-dependent resolution and semantic application it needs.

Users can continue to express supported Contract calculations in Java/Kotlin without introducing a separate
host-compiler analysis step for 1D source. This draft does not claim that all Java/Kotlin expressions are supported,
that the resolver is implemented, or that prepared SDK meaning is complete for every library operation.

The existing Contract, SDK qualification, HIR, and realization boundaries remain governed by their owning ADRs. This ADR
stays **Draft** until the remaining semantic obligations have been reviewed and decided.