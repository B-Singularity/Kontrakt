package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

/**
 * Lightweight shape summary attached to canonical type identity surfaces.
 *
 * This is not ResolvedTypeShape.
 *
 * This object records enough shape information to:
 *
 * - prevent repeated string parsing;
 * - distinguish terminal / structural / polymorphic / container surfaces;
 * - distinguish sealed surfaces from open polymorphic surfaces;
 * - bind interface/abstract/sealed surfaces to polymorphic expansion;
 * - preserve coarse component hints for generic arrays;
 * - reject impossible shape combinations early.
 *
 * Detailed child references and concrete expansion plans belong to:
 *
 * - ResolvedTypeShape;
 * - PolymorphicExpansionPlan;
 * - CollectionExpansionPlan;
 * - ArrayExpansionPlan;
 * - MapExpansionPlan.
 *
 * Hint law:
 *
 * Array component hints are non-authoritative. They exist only to help the next
 * stage decide whether heavy component resolution is required.
 *
 * Any optimized path using arrayComponentHint MUST call
 * requireArrayComponentConsistency(...) after resolving the actual component
 * shape and before committing ArrayExpansionPlan / ResolvedTypeShape.
 *
 * Generic arity cap:
 *
 * MAX_GENERIC_ARITY is a protocol cap, not an accidental implementation limit.
 * Types with generic arity greater than this value are intentionally rejected by
 * this metamodel boundary. Raising the cap requires protocol amendment and
 * golden-vector updates.
 *
 * Raw type law:
 *
 * Raw generic use-sites are not repaired by this VO.
 *
 * A reflection/KSP adapter that observes a raw generic type must handle it
 * before issuing TypeShapeSummary by one of the explicit adapter/lowering
 * strategies:
 *
 * - reject as non-canonical raw type;
 * - lower to an explicit star-projection representation if the active policy
 *   allows star projection;
 * - defer through a dedicated unresolved-generic surface in a later protocol.
 *
 * The core must never silently convert raw generic arity into wildcard material.
 *
 * Allocation note:
 *
 * This VO intentionally does not implement static flyweight caching, hash
 * caching, bit packing, or interning. Those are separate governance/performance
 * concerns and will be handled later by canonical encoding / metamodel
 * interning / L2 interning layers. Mixing them into this semantic VO would blur
 * the DDD boundary.
 *
 * Persistence note:
 *
 * issue(...) creates a summary under the current schema only. Persisted or
 * imported summary material must be restored through a separate artifact
 * verifier / migration boundary, never by silently passing an incoming schema
 * version into this issuer.
 */
class TypeShapeSummary private constructor(
    val kind: CanonicalTypeShapeKind,
    val genericArity: Int,
    val arrayRank: Int,
    val atomicFamily: AtomicShapeFamily?,
    val arrayComponentHint: ArrayComponentShapeHint?,
    val expansionSurface: CanonicalExpansionSurface,
    val schemaVersion: Int,
) {
    val hasGenericComponent: Boolean
        get() = arrayComponentHint?.hasGenericComponent ?: false

    val componentGenericArityHint: Int?
        get() = arrayComponentHint?.componentGenericArityHint

    val componentShapeKindHint: CanonicalTypeShapeKind?
        get() = arrayComponentHint?.componentShapeKindHint

    /**
     * Conservative signal that a heavier component-resolution pass is required.
     *
     * This value is not permission to skip ArrayExpansionPlan construction.
     */
    val requiresComponentShapeResolution: Boolean
        get() =
            kind == CanonicalTypeShapeKind.ARRAY &&
                (arrayComponentHint == null || arrayComponentHint.hasGenericComponent)

    /**
     * Required consistency assertion for array expansion.
     *
     * ArrayExpansionPlan / ResolvedTypeShape creation code should call this
     * before committing an array expansion result if it received this summary.
     *
     * This method intentionally lives on TypeShapeSummary, not only on
     * ArrayComponentShapeHint, so callers cannot accidentally bypass the fact
     * that hint verification is meaningful only for ARRAY summaries.
     */
    fun requireArrayComponentConsistency(
        actualComponentKind: CanonicalTypeShapeKind,
        actualComponentGenericArity: Int,
    ) {
        if (kind != CanonicalTypeShapeKind.ARRAY) {
            throw MetamodelFactContractViolationException(
                "Array component consistency can only be checked for ARRAY summaries: " +
                    "kind=${kind.protocolToken}",
            )
        }

        requireGenericArityWithinCap(
            field = "actualComponentGenericArity",
            value = actualComponentGenericArity,
        )

        if (actualComponentKind == CanonicalTypeShapeKind.VOID) {
            throw MetamodelFactContractViolationException(
                "Array component actual kind must not be VOID.",
            )
        }

        if (actualComponentKind == CanonicalTypeShapeKind.ARRAY) {
            throw MetamodelFactContractViolationException(
                "Nested array actual kind must be represented by TypeShapeSummary.arrayRank, " +
                    "not by ARRAY component kind.",
            )
        }

        arrayComponentHint?.requireMatchesResolvedComponent(
            actualComponentKind = actualComponentKind,
            actualComponentGenericArity = actualComponentGenericArity,
        )
    }

    /**
     * Optional guard for plan constructors.
     *
     * This makes the intended call site explicit:
     *
     *     summary.requireArrayExpansionPlanConsistency(plan.componentKind, plan.componentArity)
     *
     * The current method is deliberately small. Later ArrayExpansionPlan can wrap
     * this with richer checks when container protocol types are introduced.
     */
    fun requireArrayExpansionPlanConsistency(
        componentKind: CanonicalTypeShapeKind,
        componentGenericArity: Int,
    ) {
        requireArrayComponentConsistency(
            actualComponentKind = componentKind,
            actualComponentGenericArity = componentGenericArity,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeSummary) return false

        return kind == other.kind &&
            genericArity == other.genericArity &&
            arrayRank == other.arrayRank &&
            atomicFamily == other.atomicFamily &&
            arrayComponentHint == other.arrayComponentHint &&
            expansionSurface == other.expansionSurface &&
            schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + kind.protocolOrder
        result = 31 * result + genericArity
        result = 31 * result + arrayRank
        result = 31 * result + (atomicFamily?.protocolOrder ?: 0)
        result = 31 * result + (arrayComponentHint?.hashCode() ?: 0)
        result = 31 * result + expansionSurface.protocolOrder
        return result
    }

    override fun toString(): String =
        buildString {
            append("TypeShapeSummary(")
            append("schemaVersion=")
            append(schemaVersion)
            append(", kind=")
            append(kind.protocolToken)
            append(", expansionSurface=")
            append(expansionSurface.protocolToken)
            append(", genericArity=")
            append(genericArity)
            append(", arrayRank=")
            append(arrayRank)
            append(", atomicFamily=")
            append(atomicFamily?.protocolToken)
            append(", arrayComponentHint=")
            append(arrayComponentHint)
            append(')')
        }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 3

        /**
         * Protocol cap for canonical metamodel summaries.
         *
         * This is deliberately strict. Kontrakt rejects pathological or
         * machine-generated type surfaces above this arity until a future
         * protocol amendment explicitly raises the cap.
         */
        const val MAX_GENERIC_ARITY: Int = 64

        /**
         * Protocol cap for array rank.
         */
        const val MAX_ARRAY_RANK: Int = 16

        @JvmStatic
        fun issue(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            arrayRank: Int,
            atomicFamily: AtomicShapeFamily? = null,
            arrayComponentHint: ArrayComponentShapeHint? = null,
        ): TypeShapeSummary {
            requireGenericArityWithinCap(
                field = "genericArity",
                value = genericArity,
            )
            requireArrayRank(arrayRank)

            val expansionSurface = kind.expansionSurface

            validateKindContract(
                kind = kind,
                genericArity = genericArity,
                arrayRank = arrayRank,
                atomicFamily = atomicFamily,
                arrayComponentHint = arrayComponentHint,
            )

            return TypeShapeSummary(
                kind = kind,
                genericArity = genericArity,
                arrayRank = arrayRank,
                atomicFamily = atomicFamily,
                arrayComponentHint = arrayComponentHint,
                expansionSurface = expansionSurface,
                schemaVersion = CURRENT_SCHEMA_VERSION,
            )
        }

        internal fun requireGenericArityWithinCap(
            field: String,
            value: Int,
        ) {
            if (value < 0) {
                throw MetamodelFactContractViolationException(
                    "$field must be >= 0: $value",
                )
            }

            if (value > MAX_GENERIC_ARITY) {
                throw MetamodelFactContractViolationException(
                    "$field exceeds protocol cap=$MAX_GENERIC_ARITY: $value. " +
                        "This type surface is rejected by the current metamodel protocol.",
                )
            }
        }

        private fun requireArrayRank(arrayRank: Int) {
            if (arrayRank < 0) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.arrayRank must be >= 0: $arrayRank",
                )
            }

            if (arrayRank > MAX_ARRAY_RANK) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.arrayRank exceeds protocol cap=$MAX_ARRAY_RANK: $arrayRank",
                )
            }
        }

        private fun validateKindContract(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            arrayRank: Int,
            atomicFamily: AtomicShapeFamily?,
            arrayComponentHint: ArrayComponentShapeHint?,
        ) {
            when (kind) {
                CanonicalTypeShapeKind.VOID,
                CanonicalTypeShapeKind.UNIT,
                CanonicalTypeShapeKind.ENUM,
                -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireExactGenericArity(kind, genericArity, 0)
                    requireExactArrayRank(kind, arrayRank, 0)
                    requireNoArrayComponentHint(kind, arrayComponentHint)
                }

                CanonicalTypeShapeKind.ATOMIC -> {
                    if (atomicFamily == null) {
                        throw MetamodelFactContractViolationException(
                            "TypeShapeSummary.atomicFamily is required for ATOMIC.",
                        )
                    }

                    requireExactGenericArity(kind, genericArity, 0)
                    requireExactArrayRank(kind, arrayRank, 0)
                    requireNoArrayComponentHint(kind, arrayComponentHint)
                }

                CanonicalTypeShapeKind.COMPOSITE,
                CanonicalTypeShapeKind.INTERFACE,
                CanonicalTypeShapeKind.SEALED_INTERFACE,
                CanonicalTypeShapeKind.ABSTRACT_CLASS,
                CanonicalTypeShapeKind.SEALED_CLASS,
                -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireMinimumGenericArity(
                        kind = kind,
                        actual = genericArity,
                        minimum = kind.minimumGenericArity,
                    )
                    requireExactArrayRank(kind, arrayRank, 0)
                    requireNoArrayComponentHint(kind, arrayComponentHint)
                }

                CanonicalTypeShapeKind.COLLECTION,
                CanonicalTypeShapeKind.MAP,
                -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireMinimumGenericArity(
                        kind = kind,
                        actual = genericArity,
                        minimum = kind.minimumGenericArity,
                    )
                    requireExactArrayRank(kind, arrayRank, 0)
                    requireNoArrayComponentHint(kind, arrayComponentHint)
                }

                CanonicalTypeShapeKind.ARRAY -> {
                    requireNoAtomicFamily(kind, atomicFamily)
                    requireExactGenericArity(kind, genericArity, 0)

                    if (arrayRank <= 0) {
                        throw MetamodelFactContractViolationException(
                            "TypeShapeSummary.arrayRank must be > 0 for ARRAY: actual=$arrayRank",
                        )
                    }

                    arrayComponentHint?.validateForArray()
                }
            }
        }

        private fun requireNoAtomicFamily(
            kind: CanonicalTypeShapeKind,
            atomicFamily: AtomicShapeFamily?,
        ) {
            if (atomicFamily != null) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.atomicFamily is only valid for ATOMIC: " +
                        "kind=${kind.protocolToken}, atomicFamily=${atomicFamily.protocolToken}",
                )
            }
        }

        private fun requireNoArrayComponentHint(
            kind: CanonicalTypeShapeKind,
            arrayComponentHint: ArrayComponentShapeHint?,
        ) {
            if (arrayComponentHint != null) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.arrayComponentHint is only valid for ARRAY: " +
                        "kind=${kind.protocolToken}, arrayComponentHint=$arrayComponentHint",
                )
            }
        }

        private fun requireExactGenericArity(
            kind: CanonicalTypeShapeKind,
            actual: Int,
            expected: Int,
        ) {
            if (actual != expected) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.genericArity must be $expected for kind=${kind.protocolToken}: actual=$actual",
                )
            }
        }

        private fun requireMinimumGenericArity(
            kind: CanonicalTypeShapeKind,
            actual: Int,
            minimum: Int,
        ) {
            if (actual < minimum) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.genericArity must be >= $minimum for kind=${kind.protocolToken}: actual=$actual",
                )
            }
        }

        private fun requireExactArrayRank(
            kind: CanonicalTypeShapeKind,
            actual: Int,
            expected: Int,
        ) {
            if (actual != expected) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeSummary.arrayRank must be $expected for kind=${kind.protocolToken}: actual=$actual",
                )
            }
        }
    }
}
