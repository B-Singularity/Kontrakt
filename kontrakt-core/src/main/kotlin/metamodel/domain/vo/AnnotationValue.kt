package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.DiagnosticBudget
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import java.util.Collections

/**
 * Canonical metamodel representation of an annotation argument value.
 *
 * This is not:
 *
 * - a reflection AnnotationValue;
 * - a KSP value handle;
 * - a JVM constant-pool entry;
 * - a bytecode descriptor;
 * - a serialized annotation blob;
 * - or a canonical byte encoding.
 *
 * Supported annotation value families:
 *
 * - Byte
 * - Short
 * - Int
 * - Long
 * - Float, stored by raw IEEE-754 bits
 * - Double, stored by raw IEEE-754 bits
 * - Boolean
 * - Char
 * - String
 * - Class literal / KClass-like type reference
 * - Enum constant
 * - Nested annotation
 * - Array of annotation values
 *
 * Floating-point law:
 *
 * Float and Double are stored by raw bits rather than by numeric comparison.
 * This preserves deterministic equality for:
 *
 * - NaN payloads;
 * - -0.0 vs +0.0;
 * - platform-independent replay.
 *
 * String law:
 *
 * Annotation string values are bounded and protocol-guarded. This object does
 * not normalize strings and does not call Character.*. If richer Unicode policy
 * is needed for annotation strings later, introduce a dedicated
 * AnnotationStringText ratification boundary.
 *
 * Class literal law:
 *
 * The core never stores java.lang.Class, kotlin.reflect.KClass, TypeMirror, or
 * KSClass. Adapters must lower class literals to CanonicalTypeId before entering
 * the metamodel domain.
 *
 * Array law:
 *
 * Annotation arrays are deeply immutable at this boundary. The input list is
 * defensively copied and exposed as an unmodifiable list.
 *
 * Nesting law:
 *
 * Array and nested annotation values are depth-bounded to prevent deeply nested
 * structures from causing StackOverflowError in equals/hashCode/diagnostic
 * rendering.
 *
 * Diagnostic law:
 *
 * renderDiagnostic() is bounded. It is not canonical encoding and must not be
 * used as a cache key, fingerprint material, or persistence representation.
 */
sealed interface AnnotationValue {
    val kind: AnnotationValueKind

    /**
     * Maximum structural depth of this annotation value tree.
     *
     * Leaf values have depth 1.
     * Array and nested annotation values add one level over their children.
     */
    val nestingDepth: Int

    /**
     * Human-readable, bounded diagnostic rendering.
     *
     * This is not canonical encoding.
     * This is not identity material.
     * This is not a cache key.
     */
    fun renderDiagnostic(): String

    class ByteValue private constructor(
        val value: Byte,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.BYTE
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is ByteValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.toInt()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Byte,
            ): ByteValue {
                return ByteValue(value)
            }
        }
    }

    class ShortValue private constructor(
        val value: Short,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.SHORT
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is ShortValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.toInt()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Short,
            ): ShortValue {
                return ShortValue(value)
            }
        }
    }

    class IntValue private constructor(
        val value: Int,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.INT
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is IntValue && value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Int,
            ): IntValue {
                return IntValue(value)
            }
        }
    }

    class LongValue private constructor(
        val value: Long,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.LONG
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is LongValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Long,
            ): LongValue {
                return LongValue(value)
            }
        }
    }

    /**
     * Float annotation value stored by raw IEEE-754 bits.
     *
     * Do not compare by numeric equality. Bit-level identity is intentional.
     */
    class FloatValue private constructor(
        val valueBits: Int,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.FLOAT
        override val nestingDepth: Int = 1

        val value: Float
            get() = Float.fromBits(valueBits)

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is FloatValue && valueBits == other.valueBits
        }

        override fun hashCode(): Int {
            return valueBits
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Float,
            ): FloatValue {
                return FloatValue(value.toRawBits())
            }

            @JvmStatic
            fun issueBits(
                valueBits: Int,
            ): FloatValue {
                return FloatValue(valueBits)
            }
        }
    }

    /**
     * Double annotation value stored by raw IEEE-754 bits.
     *
     * Do not compare by numeric equality. Bit-level identity is intentional.
     */
    class DoubleValue private constructor(
        val valueBits: Long,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.DOUBLE
        override val nestingDepth: Int = 1

        val value: Double
            get() = Double.fromBits(valueBits)

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is DoubleValue && valueBits == other.valueBits
        }

        override fun hashCode(): Int {
            return valueBits.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Double,
            ): DoubleValue {
                return DoubleValue(value.toRawBits())
            }

            @JvmStatic
            fun issueBits(
                valueBits: Long,
            ): DoubleValue {
                return DoubleValue(valueBits)
            }
        }
    }

    class BooleanValue private constructor(
        val value: Boolean,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.BOOLEAN
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is BooleanValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Boolean,
            ): BooleanValue {
                return BooleanValue(value)
            }
        }
    }

    class CharValue private constructor(
        val value: Char,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.CHAR
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is CharValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.code
        }

        companion object {
            @JvmStatic
            fun issue(
                value: Char,
            ): CharValue {
                MetamodelProtocolTextGuards.requireProtocolChar(
                    field = "AnnotationValue.CharValue.value",
                    value = value,
                )

                return CharValue(value)
            }
        }
    }

    class StringValue private constructor(
        val value: String,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.STRING
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is StringValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                value: String,
                allowEmpty: Boolean = true,
            ): StringValue {
                MetamodelProtocolTextGuards.requireBoundedProtocolText(
                    field = "AnnotationValue.StringValue.value",
                    value = value,
                    maxChars = AnnotationValue.MAX_ANNOTATION_STRING_CHARS,
                    allowEmpty = allowEmpty,
                )

                return StringValue(value)
            }
        }
    }

    /**
     * Class literal / KClass-like annotation value.
     *
     * The core does not store java.lang.Class, KClass, TypeMirror, or KSClass.
     * Adapters must lower class literals to CanonicalTypeId before entering the
     * metamodel domain.
     */
    class ClassLiteralValue private constructor(
        val referencedType: CanonicalTypeId,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.CLASS_LITERAL
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is ClassLiteralValue &&
                    referencedType == other.referencedType
        }

        override fun hashCode(): Int {
            return referencedType.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                referencedType: CanonicalTypeId,
            ): ClassLiteralValue {
                requireClassLiteralTarget(referencedType)
                return ClassLiteralValue(referencedType)
            }

            private fun requireClassLiteralTarget(
                referencedType: CanonicalTypeId,
            ) {
                if (referencedType.shapeSummary.kind == CanonicalTypeShapeKind.VOID) {
                    throw MetamodelFactContractViolationException(
                        "AnnotationValue.ClassLiteralValue must not reference VOID.",
                    )
                }
            }
        }
    }

    /**
     * Enum constant annotation value.
     *
     * enumType is canonical metamodel identity, not java.lang.Enum or a runtime
     * class handle.
     */
    class EnumConstantValue private constructor(
        val enumType: CanonicalTypeId,
        val constantName: String,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.ENUM_CONSTANT
        override val nestingDepth: Int = 1

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is EnumConstantValue &&
                    enumType == other.enumType &&
                    constantName == other.constantName
        }

        override fun hashCode(): Int {
            var result = enumType.hashCode()
            result = 31 * result + constantName.hashCode()
            return result
        }

        companion object {
            @JvmStatic
            fun issue(
                enumType: CanonicalTypeId,
                constantName: String,
            ): EnumConstantValue {
                if (enumType.shapeSummary.kind != CanonicalTypeShapeKind.ENUM) {
                    throw MetamodelFactContractViolationException(
                        "AnnotationValue.EnumConstantValue requires enum CanonicalTypeId: " +
                                "actualKind=${enumType.shapeSummary.kind.protocolToken}",
                    )
                }

                MetamodelProtocolTextGuards.requireAsciiIdentifierToken(
                    field = "AnnotationValue.EnumConstantValue.constantName",
                    value = constantName,
                    maxChars = AnnotationValue.MAX_ENUM_CONSTANT_NAME_CHARS,
                )

                return EnumConstantValue(
                    enumType = enumType,
                    constantName = constantName,
                )
            }
        }
    }

    /**
     * Nested annotation value.
     *
     * AnnotationDescriptor must expose annotationValueNestingDepth so this value
     * can enforce bounded recursive structure without inspecting descriptor
     * internals.
     */
    class AnnotationLiteralValue private constructor(
        val descriptor: AnnotationDescriptor,
        override val nestingDepth: Int,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.ANNOTATION_LITERAL

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is AnnotationLiteralValue &&
                    descriptor == other.descriptor
        }

        override fun hashCode(): Int {
            return descriptor.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                descriptor: AnnotationDescriptor,
            ): AnnotationLiteralValue {
                val depth = 1 + descriptor.annotationValueNestingDepth

                requireAnnotationValueNestingDepthWithinLimit(
                    field = "AnnotationValue.AnnotationLiteralValue.nestingDepth",
                    depth = depth,
                )

                return AnnotationLiteralValue(
                    descriptor = descriptor,
                    nestingDepth = depth,
                )
            }
        }
    }

    /**
     * Array annotation value.
     *
     * JVM annotations allow arrays of annotation values. This VO deliberately
     * stores a deeply immutable list and validates bounded size.
     *
     * Mixed element kinds are not rejected here. If homogeneous arrays are
     * required for a specific annotation member, enforce that at
     * AnnotationValueEntry / AnnotationDescriptor validation.
     */
    class ArrayValue private constructor(
        private val elementsStorage: List<AnnotationValue>,
        override val nestingDepth: Int,
    ) : AnnotationValue {
        override val kind: AnnotationValueKind = AnnotationValueKind.ARRAY

        val size: Int
            get() = elementsStorage.size

        val elements: List<AnnotationValue>
            get() = elementsStorage

        operator fun get(
            index: Int,
        ): AnnotationValue {
            return elementsStorage[index]
        }

        override fun renderDiagnostic(): String {
            return AnnotationValueDiagnostics.render(this)
        }

        override fun toString(): String {
            return renderDiagnostic()
        }

        override fun equals(other: Any?): Boolean {
            return other is ArrayValue &&
                    elementsStorage == other.elementsStorage
        }

        override fun hashCode(): Int {
            return elementsStorage.hashCode()
        }

        companion object {
            @JvmStatic
            fun issue(
                elements: List<AnnotationValue>,
                allowEmpty: Boolean = true,
            ): ArrayValue {
                if (!allowEmpty && elements.isEmpty()) {
                    throw MetamodelFactContractViolationException(
                        "AnnotationValue.ArrayValue.elements must not be empty.",
                    )
                }

                if (elements.size > AnnotationValue.MAX_ARRAY_ELEMENTS) {
                    throw MetamodelFactContractViolationException(
                        "AnnotationValue.ArrayValue.elements exceeds protocol cap=${AnnotationValue.MAX_ARRAY_ELEMENTS}.",
                    )
                }

                var maxChildDepth = 0
                val copy = ArrayList<AnnotationValue>(elements.size)

                for (index in elements.indices) {
                    val element = elements[index]

                    if (element.nestingDepth > maxChildDepth) {
                        maxChildDepth = element.nestingDepth
                    }

                    copy.add(element)
                }

                val depth = 1 + maxChildDepth

                requireAnnotationValueNestingDepthWithinLimit(
                    field = "AnnotationValue.ArrayValue.nestingDepth",
                    depth = depth,
                )

                return ArrayValue(
                    elementsStorage = Collections.unmodifiableList(copy),
                    nestingDepth = depth,
                )
            }
        }
    }

    companion object {
        const val MAX_ANNOTATION_STRING_CHARS: Int = 1_024
        const val MAX_ENUM_CONSTANT_NAME_CHARS: Int = 128
        const val MAX_ARRAY_ELEMENTS: Int = 256

        /**
         * Prevents deeply recursive annotation structures from causing
         * StackOverflowError in equals/hashCode/diagnostic rendering.
         */
        const val MAX_NESTING_DEPTH: Int = 32

        /**
         * Hard cap for any single diagnostic rendering.
         *
         * This prevents nested arrays/annotations from expanding exponentially in
         * logs or exception messages.
         */
        const val MAX_RENDERED_DIAGNOSTIC_CHARS: Int = 2_048

        internal const val MAX_ARRAY_DIAGNOSTIC_ELEMENTS: Int = 16
        internal const val MAX_DIAGNOSTIC_STRING_SAMPLE_CHARS: Int = 96

        @JvmStatic
        fun byte(
            value: Byte,
        ): ByteValue = ByteValue.issue(value)

        @JvmStatic
        fun short(
            value: Short,
        ): ShortValue = ShortValue.issue(value)

        @JvmStatic
        fun int(
            value: Int,
        ): IntValue = IntValue.issue(value)

        @JvmStatic
        fun long(
            value: Long,
        ): LongValue = LongValue.issue(value)

        @JvmStatic
        fun float(
            value: Float,
        ): FloatValue = FloatValue.issue(value)

        @JvmStatic
        fun floatBits(
            valueBits: Int,
        ): FloatValue = FloatValue.issueBits(valueBits)

        @JvmStatic
        fun double(
            value: Double,
        ): DoubleValue = DoubleValue.issue(value)

        @JvmStatic
        fun doubleBits(
            valueBits: Long,
        ): DoubleValue = DoubleValue.issueBits(valueBits)

        @JvmStatic
        fun boolean(
            value: Boolean,
        ): BooleanValue = BooleanValue.issue(value)

        @JvmStatic
        fun char(
            value: Char,
        ): CharValue = CharValue.issue(value)

        @JvmStatic
        fun string(
            value: String,
            allowEmpty: Boolean = true,
        ): StringValue = StringValue.issue(
            value = value,
            allowEmpty = allowEmpty,
        )

        @JvmStatic
        fun classLiteral(
            referencedType: CanonicalTypeId,
        ): ClassLiteralValue = ClassLiteralValue.issue(
            referencedType = referencedType,
        )

        @JvmStatic
        fun enumConstant(
            enumType: CanonicalTypeId,
            constantName: String,
        ): EnumConstantValue = EnumConstantValue.issue(
            enumType = enumType,
            constantName = constantName,
        )

        @JvmStatic
        fun annotation(
            descriptor: AnnotationDescriptor,
        ): AnnotationLiteralValue = AnnotationLiteralValue.issue(
            descriptor = descriptor,
        )

        @JvmStatic
        fun array(
            elements: List<AnnotationValue>,
            allowEmpty: Boolean = true,
        ): ArrayValue = ArrayValue.issue(
            elements = elements,
            allowEmpty = allowEmpty,
        )

        internal fun diagnosticStringSample(
            value: String,
        ): String {
            if (value.length <= MAX_DIAGNOSTIC_STRING_SAMPLE_CHARS) {
                return value
            }

            return value.substring(0, MAX_DIAGNOSTIC_STRING_SAMPLE_CHARS) + "...<truncated>"
        }
    }
}

/**
 * Stable annotation value kind vocabulary.
 *
 * Do not use enum ordinal. protocolOrder is the only stable ordering surface.
 */
enum class AnnotationValueKind(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    BYTE(10, "byte"),
    SHORT(20, "short"),
    INT(30, "int"),
    LONG(40, "long"),
    FLOAT(50, "float"),
    DOUBLE(60, "double"),
    BOOLEAN(70, "boolean"),
    CHAR(80, "char"),
    STRING(90, "string"),
    CLASS_LITERAL(100, "class_literal"),
    ENUM_CONSTANT(110, "enum_constant"),
    ANNOTATION_LITERAL(120, "annotation_literal"),
    ARRAY(130, "array"),
}

private fun requireAnnotationValueNestingDepthWithinLimit(
    field: String,
    depth: Int,
) {
    if (depth <= 0) {
        throw MetamodelFactContractViolationException(
            "$field must be > 0: $depth",
        )
    }

    if (depth > AnnotationValue.MAX_NESTING_DEPTH) {
        throw MetamodelFactContractViolationException(
            "$field exceeds protocol cap=${AnnotationValue.MAX_NESTING_DEPTH}: $depth",
        )
    }
}

/**
 * Bounded diagnostic renderer for AnnotationValue.
 *
 * This prevents nested arrays/annotations from expanding unboundedly in logs or
 * exception messages.
 */
private object AnnotationValueDiagnostics {
    fun render(
        value: AnnotationValue,
    ): String {
        val budget = DiagnosticBudget(
            remaining = AnnotationValue.MAX_RENDERED_DIAGNOSTIC_CHARS,
        )
        val builder = StringBuilder()

        appendValue(
            value = value,
            builder = builder,
            budget = budget,
        )

        return builder.toString()
    }

    private fun appendValue(
        value: AnnotationValue,
        builder: StringBuilder,
        budget: DiagnosticBudget,
    ) {
        if (!budget.hasRemaining()) return

        when (value) {
            is AnnotationValue.ByteValue -> {
                budget.append(builder, value.value.toString())
            }

            is AnnotationValue.ShortValue -> {
                budget.append(builder, value.value.toString())
            }

            is AnnotationValue.IntValue -> {
                budget.append(builder, value.value.toString())
            }

            is AnnotationValue.LongValue -> {
                budget.append(builder, value.value.toString())
            }

            is AnnotationValue.FloatValue -> {
                budget.append(builder, "Float(bits=")
                budget.append(builder, value.valueBits.toString())
                budget.append(builder, ")")
            }

            is AnnotationValue.DoubleValue -> {
                budget.append(builder, "Double(bits=")
                budget.append(builder, value.valueBits.toString())
                budget.append(builder, ")")
            }

            is AnnotationValue.BooleanValue -> {
                budget.append(builder, value.value.toString())
            }

            is AnnotationValue.CharValue -> {
                budget.append(builder, "'")
                budget.append(builder, value.value.toString())
                budget.append(builder, "'")
            }

            is AnnotationValue.StringValue -> {
                budget.append(builder, "\"")
                budget.append(
                    builder = builder,
                    value = AnnotationValue.diagnosticStringSample(value.value),
                )
                budget.append(builder, "\"")
            }

            is AnnotationValue.ClassLiteralValue -> {
                budget.append(builder, value.referencedType.value)
                budget.append(builder, "::class")
            }

            is AnnotationValue.EnumConstantValue -> {
                budget.append(builder, value.enumType.value)
                budget.append(builder, ".")
                budget.append(builder, value.constantName)
            }

            is AnnotationValue.AnnotationLiteralValue -> {
                /*
                 * AnnotationDescriptor should expose a bounded diagnostic
                 * renderer later. Until that file is refactored, keep this path
                 * short and bounded instead of delegating to an unbounded
                 * descriptor.toString().
                 */
                budget.append(builder, "@annotation(depth=")
                budget.append(builder, value.nestingDepth.toString())
                budget.append(builder, ")")
            }

            is AnnotationValue.ArrayValue -> {
                appendArray(
                    value = value,
                    builder = builder,
                    budget = budget,
                )
            }
        }
    }

    private fun appendArray(
        value: AnnotationValue.ArrayValue,
        builder: StringBuilder,
        budget: DiagnosticBudget,
    ) {
        budget.append(builder, "[")

        val limit = minOf(
            value.size,
            AnnotationValue.MAX_ARRAY_DIAGNOSTIC_ELEMENTS,
        )
        var index = 0

        while (index < limit && budget.hasRemaining()) {
            if (index > 0) {
                budget.append(builder, ",")
            }

            appendValue(
                value = value[index],
                builder = builder,
                budget = budget,
            )

            index += 1
        }

        if (value.size > limit) {
            if (limit > 0) {
                budget.append(builder, ",")
            }

            budget.append(builder, "...<truncated>")
        }

        budget.append(builder, "]")
    }
}
