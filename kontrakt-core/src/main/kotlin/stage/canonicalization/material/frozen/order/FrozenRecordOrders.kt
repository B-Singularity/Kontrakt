package stage.canonicalization.material.frozen.order

import stage.canonicalization.contract.representative.MetamodelProtocolOrdering
import stage.canonicalization.material.frozen.records.FrozenAnnotationRecord
import stage.canonicalization.material.frozen.records.FrozenAnnotationRecordKey
import stage.canonicalization.material.frozen.records.FrozenConstructorParameterRecord
import stage.canonicalization.material.frozen.records.FrozenConstructorRecord
import stage.canonicalization.material.frozen.records.FrozenConstructorRecordKey
import stage.canonicalization.material.frozen.records.FrozenPropertyRecord
import stage.canonicalization.material.frozen.records.FrozenPropertyRecordKey

/**
 * Deterministic ordering authorities for Level 1 frozen records sequences.
 *
 * These comparators exist only at the frozen sequence publication boundary.
 *
 * They are designed for:
 *
 * ```text
 * defensive copy
 * -> array sort
 * -> adjacent duplicate/conflict scan
 * -> immutable object-array sequence
 * ```
 *
 * They must not be used with:
 *
 * - TreeSet;
 * - SortedSet;
 * - TreeMap as a duplicate-coalescing structure;
 * - any data structure where comparator equality silently drops a later records.
 *
 * Reason:
 *
 * Record comparators intentionally order by frozen records key, not by full
 * records payload. If two records have the same key but different payload, the
 * correct behavior is fail-closed conflict detection, not silent replacement or
 * coalescing.
 *
 * Ordering law:
 *
 * Comparator equality means:
 *
 * ```text
 * same frozen semantic key
 * ```
 *
 * It does not mean:
 *
 * ```text
 * safe to merge
 * safe to drop one side
 * same full records payload
 * ```
 *
 * Full payload equality remains the responsibility of each records's equals(...)
 * implementation and the sequence builder's conflict policy.
 *
 * Input determinism law:
 *
 * These comparators assume that all key fields have already been lowered and
 * validated at issue(...) boundaries.
 *
 * A comparator cannot repair polluted input. For example, if two adapters emit
 * different constructorSignature or canonicalPayloadKey text for the same
 * semantic declaration, the ordering will remain deterministic but the frozen
 * images will differ. That must be prevented by:
 *
 * - CanonicalTypeReference issuance;
 * - key issue(...) guards;
 * - canonical signature/text policies;
 * - adapter-neutral lowering laws;
 * - future CanonicalConstructorSignature / CanonicalPropertyName /
 *   FrozenAnnotationPayloadKey value objects.
 *
 * Performance law:
 *
 * These comparators deliberately avoid hashCode as ordering material.
 *
 * hashCode may be useful as an equality negative filter, but it cannot define a
 * strict canonical order. Hash ordering would be collision-sensitive and would
 * leak hash-policy changes into frozen sequence layout.
 *
 * The direct branch style is intentional. It avoids allocation, avoids lambda
 * call surfaces, and keeps comparison control flow explicit for the JVM JIT.
 */
object FrozenAnnotationRecordOrder : Comparator<FrozenAnnotationRecord> {
    override fun compare(
        left: FrozenAnnotationRecord,
        right: FrozenAnnotationRecord,
    ): Int {
        return FrozenAnnotationRecordKeyOrder.compare(
            left = left.key,
            right = right.key,
        )
    }
}

object FrozenConstructorParameterRecordOrder : Comparator<FrozenConstructorParameterRecord> {
    override fun compare(
        left: FrozenConstructorParameterRecord,
        right: FrozenConstructorParameterRecord,
    ): Int {
        var comparison =
            FrozenConstructorRecordKeyOrder.compare(
                left = left.key.ownerConstructorKey,
                right = right.key.ownerConstructorKey,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.key.parameterIndex,
                right = right.key.parameterIndex,
            )

        return comparison
    }
}

object FrozenConstructorRecordOrder : Comparator<FrozenConstructorRecord> {
    override fun compare(
        left: FrozenConstructorRecord,
        right: FrozenConstructorRecord,
    ): Int {
        return FrozenConstructorRecordKeyOrder.compare(
            left = left.key,
            right = right.key,
        )
    }
}

object FrozenPropertyRecordOrder : Comparator<FrozenPropertyRecord> {
    override fun compare(
        left: FrozenPropertyRecord,
        right: FrozenPropertyRecord,
    ): Int {
        return FrozenPropertyRecordKeyOrder.compare(
            left = left.key,
            right = right.key,
        )
    }
}

object FrozenAnnotationRecordKeyOrder : Comparator<FrozenAnnotationRecordKey> {
    override fun compare(
        left: FrozenAnnotationRecordKey,
        right: FrozenAnnotationRecordKey,
    ): Int {
        var comparison =
            FrozenTypeReferenceOrder.compare(
                left = left.annotationType,
                right = right.annotationType,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            left.annotationQualifiedName.compareTo(
                right.annotationQualifiedName,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.useSiteTarget,
                right = right.useSiteTarget,
            )

        if (comparison != 0) {
            return comparison
        }

        return compareString(
            left = left.canonicalPayloadKey,
            right = right.canonicalPayloadKey,
        )
    }
}

object FrozenConstructorRecordKeyOrder : Comparator<FrozenConstructorRecordKey> {
    override fun compare(
        left: FrozenConstructorRecordKey,
        right: FrozenConstructorRecordKey,
    ): Int {
        var comparison =
            FrozenTypeReferenceOrder.compare(
                left = left.ownerType,
                right = right.ownerType,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.constructorSignature,
                right = right.constructorSignature,
            )

        if (comparison != 0) {
            return comparison
        }

        return compareString(
            left = left.parameterShapeSignature,
            right = right.parameterShapeSignature,
        )
    }
}

object FrozenPropertyRecordKeyOrder : Comparator<FrozenPropertyRecordKey> {
    override fun compare(
        left: FrozenPropertyRecordKey,
        right: FrozenPropertyRecordKey,
    ): Int {
        var comparison =
            FrozenTypeReferenceOrder.compare(
                left = left.ownerType,
                right = right.ownerType,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.propertyName,
                right = right.propertyName,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            FrozenTypeReferenceOrder.compare(
                left = left.propertyType,
                right = right.propertyType,
            )

        if (comparison != 0) {
            return comparison
        }

        return compareInt(
            left = left.visibilityRank,
            right = right.visibilityRank,
        )
    }
}

private fun compareString(
    left: String,
    right: String,
): Int {
    return MetamodelProtocolOrdering.compareUtf16CodeUnits(
        left = left,
        right = right,
    )
}

private fun compareInt(
    left: Int,
    right: Int,
): Int {
    return MetamodelProtocolOrdering.compareInt(
        left = left,
        right = right,
    )
}