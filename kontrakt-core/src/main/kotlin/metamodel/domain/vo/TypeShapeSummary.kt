package metamodel.domain.vo

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Pre-lowered shape summary attached to canonical type identity surfaces.
 *
 * This is intentionally lightweight.
 *
 * It does not replace ResolvedTypeShape. It only records enough structural
 * information to:
 *
 * - avoid repeated parsing of canonical type text;
 * - distinguish terminal values from object/container/polymorphic surfaces;
 * - reject impossible combinations early.
 *
 * Detailed role metadata belongs elsewhere:
 *
 * - collection element type belongs to ResolvedTypeShape / CollectionExpansionPlan;
 * - map key/value type roles belong to ResolvedTypeShape / MapExpansionPlan;
 * - array component shape belongs to ResolvedTypeShape / ArrayExpansionPlan;
 * - enum constants belong to future EnumTypeFacts / AtomicExpansionPlan.
 *
 * This class deliberately does not implement flyweight/interning or cached
 * hashCode. Type-shape interning and eviction policy belong to the later
 * planning cache/memory-governance stage.
 */
class TypeShapeSummary private constructor(
    val kind: CanonicalTypeShapeKind,
    val genericArity: Int,
    val arrayRank: Int,
    val atomicFamily: AtomicShapeFamily?,
    val schemaVersion: Int,
) {
    val isTerminalLeaf: Boolean
        get() = kind.isTerminalLeaf

    val isPolymorphicSurface: Boolean
        get() = kind.isPolymorphicSurface

    val isContainerSurface: Boolean
        get() = kind.isContainerSurface

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeSummary) return false

        return kind == other.kind &&
                genericArity == other.genericArity &&
                arrayRank == other.arrayRank &&
                atomicFamily == other.atomicFamily &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + kind.protocolOrder
        result = 31 * result + genericArity
        result = 31 * result + arrayRank
        result = 31 * result + (atomicFamily?.protocolOrder ?: 0)
        return result
    }

    override fun toString(): String {
        return buildString {
            append("TypeShapeSummary(")
            append("schemaVersion=")
            append(schemaVersion)
            append(", kind=")
            append(kind.protocolToken)
            append(", genericArity=")
            append(genericArity)
            append(", arrayRank=")
            append(arrayRank)
            append(", atomicFamily=")
            append(atomicFamily?.protocolToken)
            append(')')
        }
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        private const val MAX_GENERIC_ARITY: Int = 32
        private const val MAX_ARRAY_RANK: Int = 16

        @JvmStatic
        fun issue(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            arrayRank: Int,
            atomicFamily: AtomicShapeFamily? = null,
        ): TypeShapeSummary {
            if (genericArity < 0) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.genericArity must be >= 0: $genericArity",
                )
            }

            if (genericArity > MAX_GENERIC_ARITY) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.genericArity exceeds max=$MAX_GENERIC_ARITY: $genericArity",
                )
            }

            if (arrayRank < 0) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.arrayRank must be >= 0: $arrayRank",
                )
            }

            if (arrayRank > MAX_ARRAY_RANK) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.arrayRank exceeds max=$MAX_ARRAY_RANK: $arrayRank",
                )
            }

            validateKindCardinality(
                kind = kind,
                genericArity = genericArity,
                arrayRank = arrayRank,
                atomicFamily = atomicFamily,
            )

            return TypeShapeSummary(
                kind = kind,
                genericArity = genericArity,
                arrayRank = arrayRank,
                atomicFamily = atomicFamily,
                schemaVersion = CURRENT_SCHEMA_VERSION,
            )
        }

        private fun validateKindCardinality(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            arrayRank: Int,
            atomicFamily: AtomicShapeFamily?,
        ) {
            when (kind) {
                CanonicalTypeShapeKind.VOID,
                CanonicalTypeShapeKind.UNIT,
                CanonicalTypeShapeKind.ENUM -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireArityAndRank(kind, genericArity, 0, arrayRank, 0)
                }

                CanonicalTypeShapeKind.ATOMIC -> {
                    if (atomicFamily == null) {
                        throw TypeExpansionContractViolationException(
                            reason = "TypeShapeSummary.atomicFamily is required for ATOMIC.",
                        )
                    }
                    requireArityAndRank(kind, genericArity, 0, arrayRank, 0)
                }

                CanonicalTypeShapeKind.COMPOSITE,
                CanonicalTypeShapeKind.INTERFACE,
                CanonicalTypeShapeKind.ABSTRACT_CLASS -> {
                    requireNoAtomicFamily(kind, atomicFamily)

                    if (arrayRank != 0) {
                        throw TypeExpansionContractViolationException(
                            reason = "TypeShapeSummary.arrayRank must be 0 for kind=${kind.protocolToken}: actual=$arrayRank",
                        )
                    }
                }

                CanonicalTypeShapeKind.COLLECTION -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireArityAndRank(kind, genericArity, 1, arrayRank, 0)
                }

                CanonicalTypeShapeKind.ARRAY -> {
                    requireNoAtomicFamily(kind, atomicFamily)

                    if (genericArity != 0) {
                        throw TypeExpansionContractViolationException(
                            reason = "TypeShapeSummary.genericArity must be 0 for ARRAY itself. Component generic shape belongs to ArrayExpansionPlan: actual=$genericArity",
                        )
                    }

                    if (arrayRank <= 0) {
                        throw TypeExpansionContractViolationException(
                            reason = "TypeShapeSummary.arrayRank must be > 0 for ARRAY: actual=$arrayRank",
                        )
                    }
                }

                CanonicalTypeShapeKind.MAP -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireArityAndRank(kind, genericArity, 2, arrayRank, 0)
                }
            }
        }

        private fun requireNoAtomicFamily(
            kind: CanonicalTypeShapeKind,
            atomicFamily: AtomicShapeFamily?,
        ) {
            if (atomicFamily != null) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.atomicFamily is only valid for ATOMIC: kind=${kind.protocolToken}, atomicFamily=${atomicFamily.protocolToken}",
                )
            }
        }

        private fun requireArityAndRank(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            expectedGenericArity: Int,
            arrayRank: Int,
            expectedArrayRank: Int,
        ) {
            if (genericArity != expectedGenericArity) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.genericArity must be $expectedGenericArity for kind=${kind.protocolToken}: actual=$genericArity",
                )
            }

            if (arrayRank != expectedArrayRank) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeShapeSummary.arrayRank must be $expectedArrayRank for kind=${kind.protocolToken}: actual=$arrayRank",
                )
            }
        }
    }
}

/**
 * Atomic sub-family.
 *
 * This prevents ATOMIC from becoming an opaque bucket that hides important
 * planning semantics.
 *
 * Never use enum ordinal.
 */
enum class AtomicShapeFamily(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    PRIMITIVE(
        protocolOrder = 10,
        protocolToken = "primitive",
    ),

    STRING(
        protocolOrder = 20,
        protocolToken = "string",
    ),

    NUMBER(
        protocolOrder = 30,
        protocolToken = "number",
    ),

    BOOLEAN(
        protocolOrder = 40,
        protocolToken = "boolean",
    ),

    TEMPORAL(
        protocolOrder = 50,
        protocolToken = "temporal",
    ),

    DURATION(
        protocolOrder = 60,
        protocolToken = "duration",
    ),

    UUID(
        protocolOrder = 70,
        protocolToken = "uuid",
    ),

    DOMAIN_LEAF(
        protocolOrder = 80,
        protocolToken = "domain_leaf",
    ),
}