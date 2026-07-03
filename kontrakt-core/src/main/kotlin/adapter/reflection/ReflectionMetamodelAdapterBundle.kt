package adapter.reflection

import migration.quarantine.RawTypeFactsProvider
import migration.quarantine.RawTypeFactsResolution
import migration.quarantine.TypeCycleIdentityProvider
import migration.quarantine.TypeShapeProvider
import realization.planning.expansion.TypeCycleIdentity
import stage.admission.diagnostics.evidence.MetamodelAdapterStateViolationException
import stage.canonicalization.material.representation.TypeReference
import stage.input.presentation.raw.ResolvedTypeShape
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KType

/**
 * Reflection-backed metamodel adapter bundle.
 *
 * This object is an infrastructure composition result.
 *
 * It is not:
 *
 * - a domain service;
 * - a planning service;
 * - a cache;
 * - an interner;
 * - a policy resolver;
 * - a semantic lifecycle authority.
 *
 * Hexagonal boundary:
 *
 * Planning code should consume only the exposed outbound ports:
 *
 * - [typeShapeProvider]
 * - [typeCycleIdentityProvider]
 * - [rawTypeFactsProvider]
 *
 * Planning code must not depend on reflection-native implementation details
 * such as KType, ReflectionTypeHandleRegistry, or ReflectionTypeReferenceBridge.
 *
 * Reflection entrypoint:
 *
 * The only reflection-specific public operation is [issueRootReference].
 * It is intended for infrastructure/discovery wiring that starts from a KType.
 * Once a TypeReference exists, planning must continue through ports.
 *
 * Lifecycle law:
 *
 * This bundle implements [AutoCloseable] so callers can scope reflection
 * sidecar state with:
 *
 * ```kotlin
 * reflectionBundle.use { bundle ->
 *     ...
 * }
 * ```
 *
 * Closing the bundle releases adapter-local KType sidecar bindings.
 *
 * Close is:
 *
 * - idempotent;
 * - operational only;
 * - not semantic rollback;
 * - not cache eviction;
 * - not canonical identity invalidation.
 *
 * Closed-state law:
 *
 * After close, every bundle entrypoint and exposed guarded provider fails
 * closed with [MetamodelAdapterStateViolationException].
 *
 * This prevents closed registry failures from appearing as fragmented provider
 * implementation details.
 */
class ReflectionMetamodelAdapterBundle private constructor(
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
    private val typeReferenceBridge: ReflectionTypeReferenceBridge,
    typeShapeProviderDelegate: TypeShapeProvider,
    typeCycleIdentityProviderDelegate: TypeCycleIdentityProvider,
    rawTypeFactsProviderDelegate: RawTypeFactsProvider,
) : AutoCloseable {
    private val closed: AtomicBoolean =
        AtomicBoolean(false)

    val typeShapeProvider: TypeShapeProvider =
        GuardedTypeShapeProvider(
            owner = this,
            delegate = typeShapeProviderDelegate,
        )

    val typeCycleIdentityProvider: TypeCycleIdentityProvider =
        GuardedTypeCycleIdentityProvider(
            owner = this,
            delegate = typeCycleIdentityProviderDelegate,
        )

    val rawTypeFactsProvider: RawTypeFactsProvider =
        GuardedRawTypeFactsProvider(
            owner = this,
            delegate = rawTypeFactsProviderDelegate,
        )

    /**
     * Issue the initial TypeReference for a reflection-discovered root type.
     *
     * This method is intentionally reflection-specific and should be used only
     * by infrastructure/discovery code.
     *
     * The returned TypeReference is domain-issued by CanonicalTypeReferenceIssuer
     * through ReflectionTypeReferenceBridge. This bundle does not issue
     * TypeReference directly.
     */
    fun issueRootReference(
        type: KType,
    ): TypeReference {
        requireOpen(
            operation = "issueRootReference",
        )

        val reference =
            typeReferenceBridge.issueReference(type)

        /*
         * If close is observed before returning, fail closed.
         *
         * If close happens after this check, this operation is considered
         * linearized before close.
         */
        requireOpen(
            operation = "issueRootReference",
        )

        return reference
    }

    fun isClosed(): Boolean {
        return closed.get()
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            typeHandleRegistry.close()
        }
    }

    private fun requireOpen(
        operation: String,
    ) {
        if (closed.get()) {
            throw MetamodelAdapterStateViolationException(
                "ReflectionMetamodelAdapterBundle is closed: operation=$operation",
            )
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            typeHandleRegistry: ReflectionTypeHandleRegistry,
            typeReferenceBridge: ReflectionTypeReferenceBridge,
            typeShapeProvider: TypeShapeProvider,
            typeCycleIdentityProvider: TypeCycleIdentityProvider,
            rawTypeFactsProvider: RawTypeFactsProvider,
        ): ReflectionMetamodelAdapterBundle {
            return ReflectionMetamodelAdapterBundle(
                typeHandleRegistry = typeHandleRegistry,
                typeReferenceBridge = typeReferenceBridge,
                typeShapeProviderDelegate = typeShapeProvider,
                typeCycleIdentityProviderDelegate = typeCycleIdentityProvider,
                rawTypeFactsProviderDelegate = rawTypeFactsProvider,
            )
        }
    }

    private class GuardedTypeShapeProvider(
        private val owner: ReflectionMetamodelAdapterBundle,
        private val delegate: TypeShapeProvider,
    ) : TypeShapeProvider {
        override fun resolveTypeShape(
            reference: TypeReference,
        ): ResolvedTypeShape {
            owner.requireOpen(
                operation = "TypeShapeProvider.resolveTypeShape",
            )

            val result =
                delegate.resolveTypeShape(reference)

            owner.requireOpen(
                operation = "TypeShapeProvider.resolveTypeShape",
            )

            return result
        }
    }

    private class GuardedTypeCycleIdentityProvider(
        private val owner: ReflectionMetamodelAdapterBundle,
        private val delegate: TypeCycleIdentityProvider,
    ) : TypeCycleIdentityProvider {
        override val identityAlgorithmId: String
            get() {
                owner.requireOpen(
                    operation = "TypeCycleIdentityProvider.identityAlgorithmId",
                )
                return delegate.identityAlgorithmId
            }

        override val identityAlgorithmVersion: Long
            get() {
                owner.requireOpen(
                    operation = "TypeCycleIdentityProvider.identityAlgorithmVersion",
                )
                return delegate.identityAlgorithmVersion
            }

        override fun resolveCycleIdentity(
            reference: TypeReference,
        ): TypeCycleIdentity {
            owner.requireOpen(
                operation = "TypeCycleIdentityProvider.resolveCycleIdentity",
            )

            val result =
                delegate.resolveCycleIdentity(reference)

            owner.requireOpen(
                operation = "TypeCycleIdentityProvider.resolveCycleIdentity",
            )

            return result
        }
    }

    private class GuardedRawTypeFactsProvider(
        private val owner: ReflectionMetamodelAdapterBundle,
        private val delegate: RawTypeFactsProvider,
    ) : RawTypeFactsProvider {
        override fun resolveRawFacts(
            reference: TypeReference,
        ): RawTypeFactsResolution {
            owner.requireOpen(
                operation = "RawTypeFactsProvider.resolveRawFacts",
            )

            val result =
                delegate.resolveRawFacts(reference)

            owner.requireOpen(
                operation = "RawTypeFactsProvider.resolveRawFacts",
            )

            return result
        }
    }
}