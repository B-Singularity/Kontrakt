package planning.domain.expansion.polymorphic

import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.exception.TypeExpansionContractViolationException
import planning.domain.expansion.sequence.ExpansionSequence

/**
 * Run-ratified immutable host runtime binding snapshot.
 *
 * RuntimeBindingSnapshotProvider must be called at run ratification only.
 * Planning/linking/execution consume this pinned value and must not re-query host
 * DI containers mid-run.
 */
class RuntimeBindingSnapshot private constructor(
    val id: RuntimeBindingSnapshotId,
    val bindings: ExpansionSequence<ResolvedBinding>,
) {
    fun isEmpty(): Boolean {
        return bindings.isEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingSnapshot) return false

        return id == other.id && bindings == other.bindings
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + bindings.hashCode()
        return result
    }

    override fun toString(): String {
        return "RuntimeBindingSnapshot(id=$id, bindings=${bindings.size})"
    }

    companion object {
        private val BINDING_TOTAL_ORDER: Comparator<ResolvedBinding> =
            Comparator { left, right ->
                val requested = TypeReferenceIdentity.compareBySignature(
                    left.requestedType,
                    right.requestedType,
                )
                if (requested != 0) {
                    return@Comparator requested
                }

                val kind = left.bindingKind.protocolOrder.compareTo(right.bindingKind.protocolOrder)
                if (kind != 0) {
                    return@Comparator kind
                }

                val selected = TypeReferenceIdentity.compareBySignature(
                    left.selectedImplementation.type,
                    right.selectedImplementation.type,
                )
                if (selected != 0) {
                    return@Comparator selected
                }

                CanonicalTextLaw.compareCanonicalIdentifiers(
                    left.selectedImplementation.canonicalIdentifier,
                    right.selectedImplementation.canonicalIdentifier,
                )
            }

        @JvmStatic
        fun issue(
            id: RuntimeBindingSnapshotId,
            bindings: Collection<ResolvedBinding>,
        ): RuntimeBindingSnapshot {
            rejectAmbiguousSameRequestAndKind(id, bindings)

            return RuntimeBindingSnapshot(
                id = id,
                bindings = ExpansionSequence.orderedStrict(
                    elements = bindings,
                    comparator = BINDING_TOTAL_ORDER,
                    duplicateMessage = { left, right ->
                        "Duplicate runtime binding: requested=${left.requestedType.signature}, " +
                                "kind=${left.bindingKind.protocolToken}, selected=${left.selectedImplementation.canonicalIdentifier}; " +
                                "other selected=${right.selectedImplementation.canonicalIdentifier}"
                    },
                ),
            )
        }

        private fun rejectAmbiguousSameRequestAndKind(
            snapshotId: RuntimeBindingSnapshotId,
            bindings: Collection<ResolvedBinding>,
        ) {
            /*
             * This temporary HashMap is ratification-local.
             * It is not stored in canonical state, and its iteration order is not
             * observed. It is used only for O(N) ambiguity detection.
             */
            val seen = HashMap<BindingAmbiguityKey, ConcreteImplementationReference>(
                bindings.size.coerceAtLeast(16),
            )

            val iterator = bindings.iterator()
            while (iterator.hasNext()) {
                val binding = iterator.next()
                val key = BindingAmbiguityKey.issue(
                    snapshotId = snapshotId,
                    binding = binding,
                )

                val previous = seen.putIfAbsent(
                    key,
                    binding.selectedImplementation,
                )

                if (previous != null && previous != binding.selectedImplementation) {
                    throw TypeExpansionContractViolationException(
                        reason = "Ambiguous runtime binding: requested=${binding.requestedType.signature}, " +
                                "kind=${binding.bindingKind.protocolToken}, " +
                                "selectedA=${previous.canonicalIdentifier}, " +
                                "selectedB=${binding.selectedImplementation.canonicalIdentifier}",
                    )
                }
            }
        }
    }
}

/**
 * HashMap key used only during runtime binding snapshot ratification.
 *
 * It is not canonical state.
 * It is not persisted.
 * It is not iterated for deterministic output.
 */
private class BindingAmbiguityKey private constructor(
    private val scopeId: RuntimeBindingScopeId,
    private val requestedId: metamodel.domain.vo.CanonicalTypeId,
    private val requestedSignature: metamodel.domain.vo.CanonicalTypeSignature,
    private val bindingKind: BindingKind,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BindingAmbiguityKey) return false

        return scopeId == other.scopeId &&
                requestedId == other.requestedId &&
                requestedSignature == other.requestedSignature &&
                bindingKind == other.bindingKind
    }

    override fun hashCode(): Int {
        var result = scopeId.hashCode()
        result = 31 * result + requestedId.hashCode()
        result = 31 * result + requestedSignature.hashCode()
        result = 31 * result + bindingKind.hashCode()
        return result
    }

    companion object {
        fun issue(
            snapshotId: RuntimeBindingSnapshotId,
            binding: ResolvedBinding,
        ): BindingAmbiguityKey {
            TypeReferenceIdentity.requireValid(binding.requestedType)

            return BindingAmbiguityKey(
                scopeId = snapshotId.scopeId,
                requestedId = binding.requestedType.id,
                requestedSignature = binding.requestedType.signature,
                bindingKind = binding.bindingKind,
            )
        }
    }
}