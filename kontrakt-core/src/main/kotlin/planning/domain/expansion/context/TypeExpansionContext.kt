package planning.domain.expansion.context

import planning.domain.expansion.polymorphic.ContractVacancyPolicy
import planning.domain.expansion.polymorphic.PolymorphicResolutionMode
import planning.domain.expansion.polymorphic.RuntimeBindingSnapshotId
import planning.domain.expansion.seed.DeterministicSeedSurfaceId

/**
 * Explicit type-expansion context.
 *
 * This is a sealed hierarchy to make illegal states unrepresentable.
 *
 * The same TypeReference may appear as:
 * - a behavioral contract subject;
 * - a constructor dependency;
 * - or a structural member.
 *
 * Therefore polymorphic mode must be carried by request/frame context.
 * It must not be inferred from TypeReference alone.
 *
 * The context carries lightweight ids, not heavyweight run-ratified snapshots.
 * The actual RuntimeBindingSnapshot and DeterministicSeedSurface are owned by
 * the run/session boundary.
 *
 * This value deliberately exposes typed fields only.
 * It does not provide canonicalComponentAt(...) string rendering. Canonical byte
 * encoding must be handled later by CanonicalSignatureProvider using tagged /
 * length-prefixed encoding.
 */
sealed interface TypeExpansionContext {
    val mode: PolymorphicResolutionMode
    val runtimeBindingSnapshotId: RuntimeBindingSnapshotId
    val deterministicSeedSurfaceId: DeterministicSeedSurfaceId

    class ContractSubject private constructor(
        override val runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
        override val deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
        val contractVacancyPolicy: ContractVacancyPolicy,
    ) : TypeExpansionContext {
        override val mode: PolymorphicResolutionMode
            get() = PolymorphicResolutionMode.CONTRACT_SUBJECT

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ContractSubject) return false

            return runtimeBindingSnapshotId == other.runtimeBindingSnapshotId &&
                    deterministicSeedSurfaceId == other.deterministicSeedSurfaceId &&
                    contractVacancyPolicy == other.contractVacancyPolicy
        }

        override fun hashCode(): Int {
            var result = runtimeBindingSnapshotId.hashCode()
            result = 31 * result + deterministicSeedSurfaceId.hashCode()
            result = 31 * result + contractVacancyPolicy.protocolToken.hashCode()
            return result
        }

        override fun toString(): String {
            return "TypeExpansionContext.ContractSubject(binding=$runtimeBindingSnapshotId, seed=$deterministicSeedSurfaceId, vacancy=${contractVacancyPolicy.protocolToken})"
        }

        companion object {
            @JvmStatic
            fun issue(
                runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
                deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
                contractVacancyPolicy: ContractVacancyPolicy,
            ): ContractSubject {
                return ContractSubject(
                    runtimeBindingSnapshotId = runtimeBindingSnapshotId,
                    deterministicSeedSurfaceId = deterministicSeedSurfaceId,
                    contractVacancyPolicy = contractVacancyPolicy,
                )
            }
        }
    }

    class DependencySite private constructor(
        override val runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
        override val deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
    ) : TypeExpansionContext {
        override val mode: PolymorphicResolutionMode
            get() = PolymorphicResolutionMode.DEPENDENCY_SITE

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DependencySite) return false

            return runtimeBindingSnapshotId == other.runtimeBindingSnapshotId &&
                    deterministicSeedSurfaceId == other.deterministicSeedSurfaceId
        }

        override fun hashCode(): Int {
            var result = runtimeBindingSnapshotId.hashCode()
            result = 31 * result + deterministicSeedSurfaceId.hashCode()
            return result
        }

        override fun toString(): String {
            return "TypeExpansionContext.DependencySite(binding=$runtimeBindingSnapshotId, seed=$deterministicSeedSurfaceId)"
        }

        companion object {
            @JvmStatic
            fun issue(
                runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
                deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
            ): DependencySite {
                return DependencySite(
                    runtimeBindingSnapshotId = runtimeBindingSnapshotId,
                    deterministicSeedSurfaceId = deterministicSeedSurfaceId,
                )
            }
        }
    }

    class StructuralMember private constructor(
        override val runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
        override val deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
    ) : TypeExpansionContext {
        override val mode: PolymorphicResolutionMode
            get() = PolymorphicResolutionMode.STRUCTURAL_MEMBER

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StructuralMember) return false

            return runtimeBindingSnapshotId == other.runtimeBindingSnapshotId &&
                    deterministicSeedSurfaceId == other.deterministicSeedSurfaceId
        }

        override fun hashCode(): Int {
            var result = runtimeBindingSnapshotId.hashCode()
            result = 31 * result + deterministicSeedSurfaceId.hashCode()
            return result
        }

        override fun toString(): String {
            return "TypeExpansionContext.StructuralMember(binding=$runtimeBindingSnapshotId, seed=$deterministicSeedSurfaceId)"
        }

        companion object {
            @JvmStatic
            fun issue(
                runtimeBindingSnapshotId: RuntimeBindingSnapshotId,
                deterministicSeedSurfaceId: DeterministicSeedSurfaceId,
            ): StructuralMember {
                return StructuralMember(
                    runtimeBindingSnapshotId = runtimeBindingSnapshotId,
                    deterministicSeedSurfaceId = deterministicSeedSurfaceId,
                )
            }
        }
    }
}