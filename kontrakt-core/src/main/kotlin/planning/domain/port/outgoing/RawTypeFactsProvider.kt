package planning.domain.port.outgoing

import stage.canonicalization.material.TypeReference

/**
 * Outbound raw type-facts port.
 *
 * Hexagonal role:
 * - implemented by reflection, KSP, bytecode, or static-source adapters
 * - Planning Core does not know which backend produced the facts
 *
 * Accounting role:
 * - must report whether the facts came from a cache hit or actual resolution
 * - this distinction is part of order metering, not optional telemetry
 */
interface RawTypeFactsProvider {
    fun resolveRawFacts(reference: TypeReference): RawTypeFactsResolution
}
