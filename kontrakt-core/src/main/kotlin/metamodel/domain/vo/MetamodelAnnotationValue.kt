package metamodel.domain.vo

/**
 * [Value Object] Represents raw values extracted from annotations.
 */
sealed class MetamodelAnnotationValue {
    data class Str(
        val value: String,
    ) : MetamodelAnnotationValue()

    data class IntVal(
        val value: Int,
    ) : MetamodelAnnotationValue()

    data class LongVal(
        val value: Long,
    ) : MetamodelAnnotationValue()

    data class Bool(
        val value: Boolean,
    ) : MetamodelAnnotationValue()

    data class DoubleVal(
        val valueBits: Long,
    ) : MetamodelAnnotationValue()

    data class FloatVal(
        val valueBits: Int,
    ) : MetamodelAnnotationValue()

    data class ByteVal(
        val value: Byte,
    ) : MetamodelAnnotationValue()

    data class ShortVal(
        val value: Short,
    ) : MetamodelAnnotationValue()

    data class CharVal(
        val value: Char,
    ) : MetamodelAnnotationValue()

    data class EnumVal(
        val typeName: String,
        val constantName: String,
    ) : MetamodelAnnotationValue()

    data class ClassVal(
        val className: String,
    ) : MetamodelAnnotationValue()

    data class ArrayVal(
        val elements: List<MetamodelAnnotationValue>,
    ) : MetamodelAnnotationValue()

    data class Nested(
        val typeName: String,
        val values: Map<String, MetamodelAnnotationValue>,
    ) : MetamodelAnnotationValue()

    object Null : MetamodelAnnotationValue()
}
