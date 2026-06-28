package metamodel.domain.port.outgoing

import metamodel.domain.vo.TypeReference
import stage.input.material.RawTypeFactsDTO

/**
 * [Secondary Port] Resolves raw metamodel facts for a domain-issued TypeReference.
 *
 * Hexagonal role:
 *
 * This is an outgoing port owned by the metamodel domain.
 *
 * The metamodel domain defines the shape of the information it needs, while
 * adapters decide how to obtain it from a concrete backend:
 *
 * - JVM reflection;
 * - KSP;
 * - bytecode analysis;
 * - static source analysis;
 * - generated metadata.
 *
 * This port is intentionally narrow.
 *
 * It returns RawTypeFactsDTO, not a broad TypeDescriptor. Shape classification,
 * constructor selection, property eligibility, active-member projection, and
 * planning decisions are separate responsibilities.
 *
 * TypeReference law:
 *
 * TypeReference is not an opaque adapter handle.
 *
 * TypeReference is a final domain-issued value object that already passed:
 *
 * - canonical type text ratification;
 * - type shape ratification;
 * - cycle-key issuance;
 * - signature issuance;
 * - identity-coherence proof verification.
 *
 * Adapters must not implement, subclass, or manually assemble TypeReference.
 *
 * If an adapter needs backend-native handles such as KType, KSClassDeclaration,
 * or ASM nodes, it must keep those in adapter-local sidecar state. Those handles
 * must not leak through this port.
 *
 * Raw-fact law:
 *
 * The returned RawTypeFactsDTO contains only raw normalized facts:
 *
 * - raw constructor candidates;
 * - raw constructor parameters;
 * - raw property facts;
 * - lowered type identity metadata;
 * - normalization/version metadata.
 *
 * It must not contain:
 *
 * - selected constructors;
 * - active-member sets;
 * - demotion results;
 * - traversal order;
 * - runtime binding decisions;
 * - cache/interner key material;
 * - adapter-native handles.
 *
 * Determinism law:
 *
 * Adapter enumeration order is not trusted.
 *
 * RawTypeFactsDTO.issue(...) is responsible for deterministic sequencing,
 * duplicate detection, ownership checks, and resource caps at the raw-fact
 * boundary.
 *
 * Failure law:
 *
 * Implementations should fail closed when backend metadata is insufficient for
 * deterministic metamodel construction.
 *
 * Examples:
 *
 * - unnamed constructor parameters;
 * - star projections;
 * - unsupported use-site variance;
 * - unnormalized backend names;
 * - incoherent owner/member facts.
 */
interface RawTypeFactsResolver {
    /**
     * Resolves raw normalized facts for a domain-issued type reference.
     *
     * @param reference domain-issued type identity. This is not a reflection or
     * KSP handle.
     *
     * @return raw normalized metamodel facts for the referenced type.
     *
     * @throws stage.input.diagnostics.MetamodelException if the adapter cannot
     * produce deterministic raw facts.
     */
    fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsDTO
}