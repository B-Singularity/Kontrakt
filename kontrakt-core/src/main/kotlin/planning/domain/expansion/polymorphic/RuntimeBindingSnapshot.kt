package planning.domain.expansion.polymorphic

import metamodel.domain.protocol.MetamodelProtocolOrdering
import metamodel.domain.vo.CanonicalTypeId
import metamodel.domain.vo.CanonicalTypeSignature
import planning.domain.exception.TypeExpansionContractViolationException
import planning.domain.expansion.sequence.ExpansionSequence

/**
 * Run-ratified immutable host runtime binding snapshot.
 *
 * RuntimeBindingSnapshotProvider must be called at run ratification only.
 * Planning/linking/execution consume this pinned value and must not re-query host
 * DI containers mid-run.
 *
 * Identity law:
 *
 * The snapshot id is owned by RuntimeBindingScopeId.
 *
 * This class treats RuntimeBindingSnapshotId as its stable identity surface and
 * does not attempt to allocate or derive snapshot ids on its own.
 *
 * Diagnostic law:
 *
 * toString() is intentionally compact. It does not recursively dump all
 * bindings.
 */
class RuntimeBindingSnapshot private constructor(
    val id: RuntimeBindingSnapshotId,
    val bindings: ExpansionSequence<ResolvedBinding>,
) {
    fun isEmpty(): Boolean = bindings.isEmpty()

    fun renderSummary(): String = "RuntimeBindingSnapshot(id=${id.renderSummary()}, bindings=${bindings.size})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingSnapshot) return false

        return id == other.id &&
                bindings == other.bindings
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + bindings.hashCode()
        return result
    }

    override fun toString(): String = renderSummary()

    companion object {
        private val BINDING_TOTAL_ORDER: Comparator<ResolvedBinding> =
            Comparator { left, right ->
                val requested =
                    TypeReferenceIdentity.compareBySignature(
                        left.requestedType,
                        right.requestedType,
                    )
                if (requested != 0) {
                    return@Comparator requested
                }

                val kind =
                    MetamodelProtocolOrdering.compareInt(
                        left = left.bindingKind.protocolOrder,
                        right = right.bindingKind.protocolOrder,
                    )
                if (kind != 0) {
                    return@Comparator kind
                }

                val selected =
                    TypeReferenceIdentity.compareBySignature(
                        left.selectedImplementation.type,
                        right.selectedImplementation.type,
                    )
                if (selected != 0) {
                    return@Comparator selected
                }

                return@Comparator MetamodelProtocolOrdering.compareUtf16CodeUnits(
                    left = left.selectedImplementation.canonicalIdentifier,
                    right = right.selectedImplementation.canonicalIdentifier,
                )
            }

        @JvmStatic
        fun issue(
            id: RuntimeBindingSnapshotId,
            bindings: Collection<ResolvedBinding>,
        ): RuntimeBindingSnapshot {
            rejectAmbiguousSameRequestAndKind(
                snapshotId = id,
                bindings = bindings,
            )

            return RuntimeBindingSnapshot(
                id = id,
                bindings =
                    ExpansionSequence.orderedStrict(
                        elements = bindings,
                        comparator = BINDING_TOTAL_ORDER,
                        duplicateMessage = { left, right ->
                            "Duplicate runtime binding: requested=${left.requestedType.signature}, " +
                                    "kind=${left.bindingKind.protocolToken}, " +
                                    "selected=${left.selectedImplementation.canonicalIdentifier}; " +
                                    "other selected=${right.selectedImplementation.canonicalIdentifier}"
                        },
                    ),
            )
        }

        private fun rejectAmbiguousSameRequestAndKind(
            snapshotId: RuntimeBindingSnapshotId,
            bindings: Collection<ResolvedBinding>,
        ) {
            val seen =
                HashMap<BindingAmbiguityKey, ConcreteImplementationReference>(
                    bindings.size.coerceAtLeast(16),
                )

            val iterator = bindings.iterator()
            while (iterator.hasNext()) {
                val binding = iterator.next()

                if (binding.bindingKind.allowsMultipleSelectedImplementations) {
                    continue
                }

                val key =
                    BindingAmbiguityKey.issue(
                        snapshotId = snapshotId,
                        binding = binding,
                    )

                val previous =
                    seen.putIfAbsent(
                        key,
                        binding.selectedImplementation,
                    )

                if (previous != null && previous != binding.selectedImplementation) {
                    throw TypeExpansionContractViolationException(
                        reason =
                            "Ambiguous runtime binding: requested=${binding.requestedType.signature}, " +
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
    private val requestedId: CanonicalTypeId,
    private val requestedSignature: CanonicalTypeSignature,
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
            return BindingAmbiguityKey(
                scopeId = snapshotId.scopeId,
                requestedId = binding.requestedType.id,
                requestedSignature = binding.requestedType.signature,
                bindingKind = binding.bindingKind,
            )
        }
    }
}
