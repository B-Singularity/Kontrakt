package stage.input.presentation.raw

/**
 * Deterministic rank table for raw metamodel fact ordering.
 *
 * This object is the single local authority for enum-to-int ordering used by
 * raw fact DTO sequencing and raw duplicate-key rendering.
 *
 * Keep this separate from planning semantic ordering.
 * These ranks only stabilize raw fact boundary sequences.
 */
internal object MetamodelFactRanks {
    fun visibilityRank(value: VisibilityKind): Int =
        when (value) {
            VisibilityKind.PUBLIC -> 0
            VisibilityKind.PROTECTED -> 1
            VisibilityKind.INTERNAL -> 2
            VisibilityKind.PRIVATE -> 3
            VisibilityKind.UNKNOWN -> 4
        }

    fun nullableVisibilityRank(value: VisibilityKind?): Int =
        if (value == null) {
            -1
        } else {
            visibilityRank(value)
        }

    fun originRank(value: MemberOrigin): Int =
        when (value) {
            MemberOrigin.DECLARED -> 0
            MemberOrigin.INHERITED -> 1
            MemberOrigin.SYNTHETIC -> 2
            MemberOrigin.ADAPTER_INFERRED -> 3
            MemberOrigin.UNKNOWN -> 4
        }

    fun nullabilityRank(value: NullabilityKind): Int =
        when (value) {
            NullabilityKind.NON_NULL -> 0
            NullabilityKind.NULLABLE -> 1
            NullabilityKind.UNKNOWN -> 2
        }

    fun mutabilityRank(value: PropertyMutability): Int =
        when (value) {
            PropertyMutability.READ_ONLY -> 0
            PropertyMutability.MUTABLE -> 1
            PropertyMutability.UNKNOWN -> 2
        }

    fun storageKindRank(value: PropertyStorageKind): Int =
        when (value) {
            PropertyStorageKind.BACKING_FIELD -> 0
            PropertyStorageKind.LATEINIT -> 1
            PropertyStorageKind.DELEGATED -> 2
            PropertyStorageKind.COMPUTED -> 3
            PropertyStorageKind.UNKNOWN -> 4
        }
}
