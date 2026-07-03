package adapter.reflection

import stage.admission.diagnostics.evidence.MetamodelAdapterStateViolationException
import stage.canonicalization.material.representation.TypeReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KType

/**
 * Adapter-local sidecar registry for reflection-native type handles.
 *
 * Architectural role:
 *
 * ```text
 * Reflection adapter:
 *   KType
 *   -> rendered identity material
 *   -> CanonicalTypeReferenceIssuer
 *   -> TypeReference
 *   -> ReflectionTypeHandleRegistry.bindOrVerify(TypeReference, KType)
 *
 * Later reflection adapter ports:
 *   TypeReference
 *   -> ReflectionTypeHandleRegistry.requireKType(TypeReference)
 *   -> KType
 * ```
 *
 * This registry exists because TypeReference must remain pure metamodel-domain
 * material. TypeReference must not retain Kotlin reflection handles.
 *
 * Boundary law:
 *
 * - domain code must not depend on this registry;
 * - planning core must not depend on this registry;
 * - TypeReference must not contain KType / KClass / JVM descriptors;
 * - this registry is not canonical state;
 * - this registry is not persisted;
 * - this registry is not a cache-key authority;
 * - this registry is not a TypeReference interner.
 *
 * Current-cut ownership law:
 *
 * The authoritative adapter-local binding is:
 *
 * ```text
 * TypeReference -> KType
 * ```
 *
 * This cut deliberately does not maintain a reverse KType -> TypeReference map.
 * A reverse index would require either a lock, a two-map transaction, or an
 * explicit binding-cell state machine. That hardening belongs to the later
 * adapter-local cache/interner phase, not to the current identity-boundary
 * refactor.
 *
 * Lock-avoidance law:
 *
 * This implementation intentionally avoids synchronized blocks. It uses a
 * single ConcurrentHashMap and AtomicBoolean lifecycle flag.
 *
 * This aligns with the wider planning/runtime style: use single-authority
 * atomic publication where possible; introduce explicit locks only when the
 * architecture truly requires them.
 */
interface ReflectionTypeHandleRegistry {
    /**
     * Compatibility wrapper.
     *
     * New code should call [bindOrVerify] to observe whether the operation
     * inserted a new binding or verified an existing one.
     */
    fun bind(
        reference: TypeReference,
        kType: KType,
    ) {
        bindOrVerify(
            reference = reference,
            kType = kType,
        )
    }

    /**
     * Bind or verify an existing adapter-local handle binding.
     *
     * Required behavior:
     *
     * - no existing binding:
     *     insert and return [ReflectionTypeHandleBindingDecision.INSERTED];
     * - same reference already bound to the same KType:
     *     return [ReflectionTypeHandleBindingDecision.ALREADY_BOUND];
     * - same reference bound to a different KType:
     *     fail closed.
     *
     * This method does not attempt reverse KType uniqueness validation in this
     * cut. Reverse validation is a later registry-hardening concern.
     */
    fun bindOrVerify(
        reference: TypeReference,
        kType: KType,
    ): ReflectionTypeHandleBindingDecision

    /**
     * Resolve the adapter-native KType for a domain-issued TypeReference.
     *
     * This method must be used only inside the reflection adapter boundary.
     */
    fun requireKType(
        reference: TypeReference,
    ): KType

    /**
     * Clear all adapter-local sidecar bindings while keeping the registry open.
     *
     * This is not semantic rollback.
     * This is not canonical cache eviction.
     * This is not TypeReference invalidation.
     */
    fun clear()

    /**
     * Close this registry permanently.
     *
     * After close, bind, lookup, and clear operations fail closed.
     */
    fun close()

    fun isClosed(): Boolean

    companion object {
        @JvmStatic
        fun issue(): ReflectionTypeHandleRegistry {
            return DefaultReflectionTypeHandleRegistry()
        }
    }
}

/**
 * Result of adapter-local handle binding.
 *
 * This is operational adapter state, not domain semantic output.
 *
 * Enum values are JVM singleton instances and do not allocate per call.
 */
enum class ReflectionTypeHandleBindingDecision {
    INSERTED,
    ALREADY_BOUND,
}

private class DefaultReflectionTypeHandleRegistry : ReflectionTypeHandleRegistry {
    private val closed: AtomicBoolean =
        AtomicBoolean(false)

    /*
     * Single authoritative adapter-local binding table.
     *
     * This is intentionally not a primitive hot-path table yet.
     * This is intentionally not a reverse-indexed bijection table yet.
     *
     * Current responsibility:
     *
     *     TypeReference -> KType
     *
     * The table exists only so reflection-backed providers can recover the
     * backend-native handle for a domain-issued TypeReference.
     */
    private val handlesByReference: ConcurrentHashMap<TypeReference, KType> =
        ConcurrentHashMap()

    override fun bindOrVerify(
        reference: TypeReference,
        kType: KType,
    ): ReflectionTypeHandleBindingDecision {
        requireOpen(
            operation = "bindOrVerify",
        )

        val previous =
            handlesByReference.putIfAbsent(
                reference,
                kType,
            )

        if (previous != null) {
            if (previous == kType) {
                /*
                 * A concurrent close can happen after putIfAbsent/read.
                 * If it already published before we return, fail closed.
                 *
                 * If close happens after this check, this operation is
                 * considered linearized before close.
                 */
                requireOpen(
                    operation = "bindOrVerify",
                )

                return ReflectionTypeHandleBindingDecision.ALREADY_BOUND
            }

            throw MetamodelAdapterStateViolationException(
                "Reflection TypeReference handle collision. " +
                        "The same TypeReference is already bound to a different KType: " +
                        "reference=${reference.renderSummary()}, " +
                        "previousKType=$previous, " +
                        "actualKType=$kType",
            )
        }

        /*
         * Close race handling:
         *
         * If close is published after putIfAbsent but before this post-check,
         * remove the freshly inserted binding and fail closed.
         *
         * This avoids leaving a binding inserted after a close publication.
         */
        if (closed.get()) {
            handlesByReference.remove(
                reference,
                kType,
            )

            throw closedException(
                operation = "bindOrVerify",
            )
        }

        return ReflectionTypeHandleBindingDecision.INSERTED
    }

    override fun requireKType(
        reference: TypeReference,
    ): KType {
        requireOpen(
            operation = "requireKType",
        )

        val kType = handlesByReference[reference]

        if (kType != null) {
            /*
             * Same linearization rule as bind:
             *
             * If close is already visible before returning, fail closed.
             * If close happens after this check, the lookup is considered
             * linearized before close.
             */
            requireOpen(
                operation = "requireKType",
            )

            return kType
        }

        if (closed.get()) {
            throw closedException(
                operation = "requireKType",
            )
        }

        throw MetamodelAdapterStateViolationException(
            "Reflection adapter cannot resolve KType for TypeReference. " +
                    "The reference was not issued through this reflection adapter registry: " +
                    reference.renderSummary(),
        )
    }

    override fun clear() {
        requireOpen(
            operation = "clear",
        )

        handlesByReference.clear()

        /*
         * If close races with clear, closed state wins. A post-check prevents
         * callers from treating a racing clear as a successful live operation.
         */
        requireOpen(
            operation = "clear",
        )
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            handlesByReference.clear()
        }
    }

    override fun isClosed(): Boolean {
        return closed.get()
    }

    private fun requireOpen(
        operation: String,
    ) {
        if (closed.get()) {
            throw closedException(
                operation = operation,
            )
        }
    }

    private fun closedException(
        operation: String,
    ): MetamodelAdapterStateViolationException {
        return MetamodelAdapterStateViolationException(
            "ReflectionTypeHandleRegistry is closed: operation=$operation",
        )
    }
}