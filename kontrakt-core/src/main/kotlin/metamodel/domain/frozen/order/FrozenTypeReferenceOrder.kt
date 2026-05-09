package metamodel.domain.frozen.order

import metamodel.domain.protocol.MetamodelProtocolOrdering
import metamodel.domain.vo.ArrayComponentShapeHint
import metamodel.domain.vo.TypeReference
import metamodel.domain.vo.TypeShapeSummary

/**
 * Deterministic ordering authority for TypeReference inside frozen metamodel
 * structures.
 *
 * This comparator is a frozen-image construction tool.
 *
 * It is not:
 *
 * - semantic equality;
 * - canonical byte encoding;
 * - route64 material;
 * - PlanCacheKey material;
 * - persistent digest material;
 * - backend acquisition order.
 *
 * Why this exists:
 *
 * Frozen sequences and frozen indexes must assign deterministic local order
 * without consulting:
 *
 * - reflection enumeration order;
 * - KSP enumeration order;
 * - bytecode declaration order;
 * - source parser order;
 * - HashMap / HashSet iteration order;
 * - JVM object identity;
 * - classloader identity;
 * - locale-dependent collation.
 *
 * TypeReference already owns semantic equality. This comparator provides a
 * strict deterministic order for frozen local layout only.
 *
 * Sorted-set prohibition:
 *
 * This comparator must not be used as a duplicate-coalescing TreeSet/SortedSet
 * authority.
 *
 * Frozen indexes and sequences must use:
 *
 * ```text
 * array defensive copy
 * -> array sort
 * -> adjacent comparator-equality scan
 * -> equals(...) confirmation or fail-closed rejection
 * ```
 *
 * not:
 *
 * ```text
 * TreeSet.add(...)
 * ```
 *
 * Comparator equality law:
 *
 * Comparator equality means only:
 *
 * ```text
 * same ordering material
 * ```
 *
 * It does not mean:
 *
 * ```text
 * safe to merge
 * safe to drop one side
 * same full semantic payload
 * ```
 *
 * If this comparator returns 0 for two distinct TypeReference instances and
 * TypeReference.equals(...) does not agree, the caller must fail closed.
 *
 * Input determinism law:
 *
 * This comparator does not normalize polluted input.
 *
 * It assumes TypeReference was already issued by the canonical metamodel
 * TypeReference issuance path. If adapter-specific text, backend-local ids,
 * object identities, or registry ids have already entered TypeReference
 * material, ordering will remain deterministic for that polluted input but the
 * resulting frozen images may diverge across adapters.
 *
 * Performance law:
 *
 * The implementation deliberately uses direct local-variable branches rather
 * than lambda-based comparison chaining.
 *
 * Reason:
 *
 * This comparator is used by frozen indexes and record sequences. Even though
 * Kotlin inline lambdas can often be optimized away, explicit branches keep the
 * bytecode/JIT surface simpler and avoid stressing JVM inlining thresholds in
 * large image-publication sorts.
 *
 * Hash law:
 *
 * This comparator must not use hashCode() as ordering material.
 *
 * hashCode may be a cheap equality negative filter elsewhere, but it cannot
 * define canonical order. Hash ordering would be collision-sensitive and would
 * leak transitional hash policy into frozen sequence layout.
 */
object FrozenTypeReferenceOrder : Comparator<TypeReference> {
    override fun compare(
        left: TypeReference,
        right: TypeReference,
    ): Int {
        if (left === right) {
            return 0
        }

        var comparison =
            compareString(
                left = left.id.value,
                right = right.id.value,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareShapeSummary(
                left = left.id.shapeSummary,
                right = right.id.shapeSummary,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.id.classifierId,
                right = right.id.classifierId,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.id.classifierVersion,
                right = right.id.classifierVersion,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.id.ratificationFingerprint.algorithmId,
                right = right.id.ratificationFingerprint.algorithmId,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.id.ratificationFingerprint.algorithmVersion,
                right = right.id.ratificationFingerprint.algorithmVersion,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.id.ratificationFingerprint.valueEncoding.protocolOrder,
                right = right.id.ratificationFingerprint.valueEncoding.protocolOrder,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.id.ratificationFingerprint.value,
                right = right.id.ratificationFingerprint.value,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.cycleKey.value,
                right = right.cycleKey.value,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareString(
                left = left.signature.value,
                right = right.signature.value,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.signature.schemaVersion,
                right = right.signature.schemaVersion,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.useSiteAnnotations.size,
                right = right.useSiteAnnotations.size,
            )

        if (comparison != 0) {
            return comparison
        }

        var annotationIndex = 0

        while (annotationIndex < left.useSiteAnnotations.size) {
            comparison =
                left.useSiteAnnotations[annotationIndex].compareTo(
                    right.useSiteAnnotations[annotationIndex],
                )

            if (comparison != 0) {
                return comparison
            }

            annotationIndex += 1
        }

        return compareInt(
            left = left.typeNestingDepth,
            right = right.typeNestingDepth,
        )
    }

    private fun compareShapeSummary(
        left: TypeShapeSummary,
        right: TypeShapeSummary,
    ): Int {
        if (left === right) {
            return 0
        }

        var comparison =
            compareInt(
                left = left.schemaVersion,
                right = right.schemaVersion,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.kind.protocolOrder,
                right = right.kind.protocolOrder,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.genericArity,
                right = right.genericArity,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareInt(
                left = left.arrayRank,
                right = right.arrayRank,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareNullableProtocolOrder(
                left = left.atomicFamily?.protocolOrder,
                right = right.atomicFamily?.protocolOrder,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareArrayComponentHint(
                left = left.arrayComponentHint,
                right = right.arrayComponentHint,
            )

        if (comparison != 0) {
            return comparison
        }

        return compareInt(
            left = left.expansionSurface.protocolOrder,
            right = right.expansionSurface.protocolOrder,
        )
    }

    private fun compareArrayComponentHint(
        left: ArrayComponentShapeHint?,
        right: ArrayComponentShapeHint?,
    ): Int {
        if (left === right) {
            return 0
        }

        if (left == null) {
            return -1
        }

        if (right == null) {
            return 1
        }

        var comparison =
            compareBoolean(
                left = left.hasGenericComponent,
                right = right.hasGenericComponent,
            )

        if (comparison != 0) {
            return comparison
        }

        comparison =
            compareNullableProtocolOrder(
                left = left.componentGenericArityHint,
                right = right.componentGenericArityHint,
            )

        if (comparison != 0) {
            return comparison
        }

        return compareNullableProtocolOrder(
            left = left.componentShapeKindHint?.protocolOrder,
            right = right.componentShapeKindHint?.protocolOrder,
        )
    }

    /**
     * Compares nullable protocol-order values without sentinel substitution.
     *
     * Do not encode null as -1, Int.MIN_VALUE, or any other numeric sentinel.
     *
     * Reason:
     *
     * Sentinel substitution can collide with future protocol values if a domain
     * later ratifies that numeric value. Null ordering is therefore expressed
     * structurally:
     *
     * ```text
     * null < non-null
     * non-null values compare by MetamodelProtocolOrdering.compareInt(...)
     * ```
     */
    private fun compareNullableProtocolOrder(
        left: Int?,
        right: Int?,
    ): Int {
        if (left == null && right == null) {
            return 0
        }

        if (left == null) {
            return -1
        }

        if (right == null) {
            return 1
        }

        return compareInt(
            left = left,
            right = right,
        )
    }

    private fun compareString(
        left: String,
        right: String,
    ): Int {
        if (left === right) {
            return 0
        }

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

    private fun compareBoolean(
        left: Boolean,
        right: Boolean,
    ): Int {
        return MetamodelProtocolOrdering.compareBoolean(
            left = left,
            right = right,
        )
    }
}