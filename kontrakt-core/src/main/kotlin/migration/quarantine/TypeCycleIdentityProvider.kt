package migration.quarantine

import realization.planning.expansion.TypeCycleIdentity
import stage.canonicalization.material.representation.TypeReference

/**
 * Outbound port for active-cycle identity preflight.
 *
 * Hexagonal role:
 * - implemented by reflection, KSP, bytecode, or static-source adapters;
 * - Planning Core does not know which backend produced the identity.
 *
 * Contract:
 * - must not enumerate constructors;
 * - must not enumerate properties;
 * - must not project active members;
 * - must not order active members;
 * - must return adapter-independent canonical identity material.
 *
 * Determinism contract:
 * - for the same canonical TypeReference and the same identity algorithm id/version,
 *   resolveCycleIdentity(reference) MUST return the same TypeCycleIdentity;
 * - the result MUST NOT depend on reflection enumeration order;
 * - the result MUST NOT depend on KSP declaration iteration order;
 * - the result MUST NOT depend on object identity, wall clock, random UUID, mutable adapter cache iteration,
 *   or backend handle identity;
 * - adapter-local memoization is allowed, but cache state is not semantic authority.
 *
 * Lifecycle contract:
 * - identityAlgorithmId and identityAlgorithmVersion must be stable for the provider lifetime
 *   visible to one TypeExpansionPipeline instance;
 * - if the algorithm changes, a new provider/pipeline instance must be created through a new resolved policy boundary.
 */
interface TypeCycleIdentityProvider {
    val identityAlgorithmId: String

    val identityAlgorithmVersion: Long

    fun resolveCycleIdentity(reference: TypeReference): TypeCycleIdentity
}
