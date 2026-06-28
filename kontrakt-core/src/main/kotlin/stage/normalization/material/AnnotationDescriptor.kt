package stage.normalization.material

import governance.budget.DiagnosticBudget
import stage.canonicalization.material.CanonicalTypeId
import stage.canonicalization.material.TypeShapeRatificationFingerprint
import stage.input.diagnostics.MetamodelFactContractViolationException
import stage.input.material.ArrayComponentShapeHint
import stage.input.material.TypeShapeSummary
import stage.normalization.contract.MetamodelProtocolOrdering

/**
 * Canonical descriptor for one annotation instance.
 *
 * Descriptor law:
 *
 * An annotation descriptor is the pair:
 *
 *     AnnotationQualifiedName + AnnotationValueMap
 *
 * Ordering law:
 *
 * AnnotationDescriptor has deterministic structural order order:
 *
 * 1. qualifiedName order;
 * 2. argument map structural order;
 * 3. annotation value structural order.
 *
 * Do not sort descriptors by hashCode alone. Hashes may collide and would break
 * Comparable consistency.
 *
 * Diagnostic law:
 *
 * toString() is intentionally compact.
 * renderDiagnostic() provides bounded human-readable details.
 *
 * Nesting law:
 *
 * annotationValueNestingDepth exposes the maximum nested AnnotationValue depth
 * contained in this descriptor. AnnotationValue.AnnotationLiteralValue uses this
 * value to enforce recursive annotation depth limits without inspecting this
 * descriptor's internals.
 */
class AnnotationDescriptor private constructor(
    val qualifiedName: AnnotationQualifiedName,
    val values: AnnotationValueMap,
    val annotationValueNestingDepth: Int,
) : Comparable<AnnotationDescriptor> {
    override fun compareTo(other: AnnotationDescriptor): Int =
        AnnotationDescriptorOrder.compare(
            left = this,
            right = other,
        )

    fun renderSummary(): String = "AnnotationDescriptor(name=$qualifiedName, arguments=${values.size})"

    fun renderDiagnostic(): String {
        val budget =
            DiagnosticBudget(
                remaining = MAX_RENDERED_DIAGNOSTIC_CHARS,
            )
        val builder = StringBuilder()

        budget.append(builder, "AnnotationDescriptor(")
        budget.append(builder, "name=")
        budget.append(builder, qualifiedName.value)
        budget.append(builder, ",values=")
        budget.append(builder, values.renderDiagnostic())
        budget.append(builder, ")")

        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationDescriptor) return false

        return qualifiedName == other.qualifiedName &&
                values == other.values
    }

    override fun hashCode(): Int {
        var result = qualifiedName.hashCode()
        result = 31 * result + values.hashCode()
        return result
    }

    override fun toString(): String = renderSummary()

    companion object {
        private const val MAX_RENDERED_DIAGNOSTIC_CHARS: Int = 2_048

        @JvmStatic
        fun issue(
            qualifiedName: AnnotationQualifiedName,
            values: AnnotationValueMap = AnnotationValueMap.empty(),
        ): AnnotationDescriptor {
            val annotationValueNestingDepth = computeAnnotationValueNestingDepth(values)

            requireAnnotationValueNestingDepthWithinLimit(
                depth = annotationValueNestingDepth,
            )

            return AnnotationDescriptor(
                qualifiedName = qualifiedName,
                values = values,
                annotationValueNestingDepth = annotationValueNestingDepth,
            )
        }

        private fun computeAnnotationValueNestingDepth(values: AnnotationValueMap): Int {
            var maxDepth = 0
            var index = 0

            while (index < values.size) {
                val depth = values[index].value.nestingDepth

                if (depth > maxDepth) {
                    maxDepth = depth
                }

                index += 1
            }

            return maxDepth
        }

        private fun requireAnnotationValueNestingDepthWithinLimit(depth: Int) {
            if (depth < 0) {
                throw MetamodelFactContractViolationException(
                    "AnnotationDescriptor.annotationValueNestingDepth must be >= 0: $depth",
                )
            }

            if (depth > AnnotationValue.MAX_NESTING_DEPTH) {
                throw MetamodelFactContractViolationException(
                    "AnnotationDescriptor.annotationValueNestingDepth exceeds order cap=" +
                            "${AnnotationValue.MAX_NESTING_DEPTH}: $depth",
                )
            }
        }
    }
}

/**
 * Protocol-defined ordering for annotation descriptors.
 *
 * This comparator is structural and deterministic.
 *
 * It does not use:
 *
 * - reflection order;
 * - source order;
 * - hash-only order;
 * - locale-sensitive text comparison;
 * - planning-domain text law.
 */
private object AnnotationDescriptorOrder {
    fun compare(
        left: AnnotationDescriptor,
        right: AnnotationDescriptor,
    ): Int {
        val nameCompare = left.qualifiedName.compareTo(right.qualifiedName)
        if (nameCompare != 0) {
            return nameCompare
        }

        return compareValueMaps(
            left = left.values,
            right = right.values,
        )
    }

    private fun compareValueMaps(
        left: AnnotationValueMap,
        right: AnnotationValueMap,
    ): Int {
        val leftSize = left.size
        val rightSize = right.size
        val minSize = if (leftSize < rightSize) leftSize else rightSize

        var index = 0
        while (index < minSize) {
            val entryCompare =
                compareEntries(
                    left = left[index],
                    right = right[index],
                )

            if (entryCompare != 0) {
                return entryCompare
            }

            index += 1
        }

        return MetamodelProtocolOrdering.compareInt(leftSize, rightSize)
    }

    private fun compareEntries(
        left: AnnotationValueEntry,
        right: AnnotationValueEntry,
    ): Int {
        val nameCompare = left.name.compareTo(right.name)
        if (nameCompare != 0) {
            return nameCompare
        }

        return AnnotationValueOrder.compare(
            left = left.value,
            right = right.value,
        )
    }
}

/**
 * Structural deterministic ordering for annotation values.
 *
 * This is not canonical encoding.
 * This is only a stable in-memory order order for descriptors.
 */
private object AnnotationValueOrder {
    fun compare(
        left: AnnotationValue,
        right: AnnotationValue,
    ): Int {
        val kindCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.kind.protocolOrder,
                right = right.kind.protocolOrder,
            )
        if (kindCompare != 0) {
            return kindCompare
        }

        return when (left) {
            is AnnotationValue.ByteValue -> {
                MetamodelProtocolOrdering.compareInt(
                    left = left.value.toInt(),
                    right = (right as AnnotationValue.ByteValue).value.toInt(),
                )
            }

            is AnnotationValue.ShortValue -> {
                MetamodelProtocolOrdering.compareInt(
                    left = left.value.toInt(),
                    right = (right as AnnotationValue.ShortValue).value.toInt(),
                )
            }

            is AnnotationValue.IntValue -> {
                MetamodelProtocolOrdering.compareInt(
                    left = left.value,
                    right = (right as AnnotationValue.IntValue).value,
                )
            }

            is AnnotationValue.LongValue -> {
                MetamodelProtocolOrdering.compareLong(
                    left = left.value,
                    right = (right as AnnotationValue.LongValue).value,
                )
            }

            is AnnotationValue.FloatValue -> {
                MetamodelProtocolOrdering.compareInt(
                    left = left.valueBits,
                    right = (right as AnnotationValue.FloatValue).valueBits,
                )
            }

            is AnnotationValue.DoubleValue -> {
                MetamodelProtocolOrdering.compareLong(
                    left = left.valueBits,
                    right = (right as AnnotationValue.DoubleValue).valueBits,
                )
            }

            is AnnotationValue.BooleanValue -> {
                MetamodelProtocolOrdering.compareBoolean(
                    left = left.value,
                    right = (right as AnnotationValue.BooleanValue).value,
                )
            }

            is AnnotationValue.CharValue -> {
                MetamodelProtocolOrdering.compareInt(
                    left = left.value.code,
                    right = (right as AnnotationValue.CharValue).value.code,
                )
            }

            is AnnotationValue.StringValue -> {
                MetamodelProtocolOrdering.compareUtf16CodeUnits(
                    left = left.value,
                    right = (right as AnnotationValue.StringValue).value,
                )
            }

            is AnnotationValue.ClassLiteralValue -> {
                compareCanonicalTypeIds(
                    left = left.referencedType,
                    right = (right as AnnotationValue.ClassLiteralValue).referencedType,
                )
            }

            is AnnotationValue.EnumConstantValue -> {
                compareEnumConstants(
                    left = left,
                    right = right as AnnotationValue.EnumConstantValue,
                )
            }

            is AnnotationValue.AnnotationLiteralValue -> {
                left.descriptor.compareTo(
                    (right as AnnotationValue.AnnotationLiteralValue).descriptor,
                )
            }

            is AnnotationValue.ArrayValue -> {
                compareArrays(
                    left = left,
                    right = right as AnnotationValue.ArrayValue,
                )
            }
        }
    }

    private fun compareEnumConstants(
        left: AnnotationValue.EnumConstantValue,
        right: AnnotationValue.EnumConstantValue,
    ): Int {
        val typeCompare =
            compareCanonicalTypeIds(
                left = left.enumType,
                right = right.enumType,
            )
        if (typeCompare != 0) {
            return typeCompare
        }

        return MetamodelProtocolOrdering.compareUtf16CodeUnits(
            left = left.constantName,
            right = right.constantName,
        )
    }

    private fun compareArrays(
        left: AnnotationValue.ArrayValue,
        right: AnnotationValue.ArrayValue,
    ): Int {
        val leftSize = left.size
        val rightSize = right.size
        val minSize = if (leftSize < rightSize) leftSize else rightSize

        var index = 0
        while (index < minSize) {
            val elementCompare =
                compare(
                    left = left[index],
                    right = right[index],
                )

            if (elementCompare != 0) {
                return elementCompare
            }

            index += 1
        }

        return MetamodelProtocolOrdering.compareInt(leftSize, rightSize)
    }

    private fun compareCanonicalTypeIds(
        left: CanonicalTypeId,
        right: CanonicalTypeId,
    ): Int {
        val textCompare =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.value,
                right = right.value,
            )
        if (textCompare != 0) {
            return textCompare
        }

        val shapeCompare =
            compareTypeShapeSummaries(
                left = left.shapeSummary,
                right = right.shapeSummary,
            )
        if (shapeCompare != 0) {
            return shapeCompare
        }

        val classifierIdCompare =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.classifierId,
                right = right.classifierId,
            )
        if (classifierIdCompare != 0) {
            return classifierIdCompare
        }

        val classifierVersionCompare =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.classifierVersion,
                right = right.classifierVersion,
            )
        if (classifierVersionCompare != 0) {
            return classifierVersionCompare
        }

        return compareRatificationFingerprints(
            left = left.ratificationFingerprint,
            right = right.ratificationFingerprint,
        )
    }

    private fun compareTypeShapeSummaries(
        left: TypeShapeSummary,
        right: TypeShapeSummary,
    ): Int {
        val kindCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.kind.protocolOrder,
                right = right.kind.protocolOrder,
            )
        if (kindCompare != 0) return kindCompare

        val genericArityCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.genericArity,
                right = right.genericArity,
            )
        if (genericArityCompare != 0) return genericArityCompare

        val arrayRankCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.arrayRank,
                right = right.arrayRank,
            )
        if (arrayRankCompare != 0) return arrayRankCompare

        val atomicFamilyCompare =
            MetamodelProtocolOrdering.compareNullableProtocolOrder(
                left = left.atomicFamily?.protocolOrder,
                right = right.atomicFamily?.protocolOrder,
            )
        if (atomicFamilyCompare != 0) return atomicFamilyCompare

        val componentHintCompare =
            compareArrayComponentHints(
                left = left.arrayComponentHint,
                right = right.arrayComponentHint,
            )
        if (componentHintCompare != 0) return componentHintCompare

        val expansionSurfaceCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.expansionSurface.protocolOrder,
                right = right.expansionSurface.protocolOrder,
            )
        if (expansionSurfaceCompare != 0) return expansionSurfaceCompare

        return MetamodelProtocolOrdering.compareInt(
            left = left.schemaVersion,
            right = right.schemaVersion,
        )
    }

    private fun compareArrayComponentHints(
        left: ArrayComponentShapeHint?,
        right: ArrayComponentShapeHint?,
    ): Int {
        if (left == null && right == null) return 0
        if (left == null) return -1
        if (right == null) return 1

        val genericPresenceCompare =
            MetamodelProtocolOrdering.compareBoolean(
                left = left.hasGenericComponent,
                right = right.hasGenericComponent,
            )
        if (genericPresenceCompare != 0) return genericPresenceCompare

        val genericArityCompare =
            MetamodelProtocolOrdering.compareNullableInt(
                left = left.componentGenericArityHint,
                right = right.componentGenericArityHint,
            )
        if (genericArityCompare != 0) return genericArityCompare

        return MetamodelProtocolOrdering.compareNullableProtocolOrder(
            left = left.componentShapeKindHint?.protocolOrder,
            right = right.componentShapeKindHint?.protocolOrder,
        )
    }

    private fun compareRatificationFingerprints(
        left: TypeShapeRatificationFingerprint,
        right: TypeShapeRatificationFingerprint,
    ): Int {
        val algorithmCompare =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.algorithmId,
                right = right.algorithmId,
            )
        if (algorithmCompare != 0) return algorithmCompare

        val algorithmVersionCompare =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.algorithmVersion,
                right = right.algorithmVersion,
            )
        if (algorithmVersionCompare != 0) return algorithmVersionCompare

        val encodingCompare =
            MetamodelProtocolOrdering.compareInt(
                left = left.valueEncoding.protocolOrder,
                right = right.valueEncoding.protocolOrder,
            )
        if (encodingCompare != 0) return encodingCompare

        return MetamodelProtocolOrdering.compareUtf16CodeUnits(
            left = left.value,
            right = right.value,
        )
    }
}
