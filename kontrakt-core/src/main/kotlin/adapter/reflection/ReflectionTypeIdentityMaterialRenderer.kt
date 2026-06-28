package adapter.reflection

import metamodel.domain.vo.CanonicalTypeShapeKind
import stage.input.material.TypeNestingDepthLaw
import stage.input.material.TypeShapeSummary
import stage.input.diagnostics.StrictModeViolationException
import stage.input.material.ArrayComponentShapeHint
import stage.input.material.AtomicShapeFamily
import stage.input.material.OrderedUseSiteAnnotations
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

/**
 * Reflection adapter-local renderer for type identity material.
 *
 * This class is deliberately an adapter renderer, not a canonical authority.
 *
 * It may observe Kotlin/JVM reflection material, but it must stop at producing
 * deterministic candidate material. The metamodel domain service owns all
 * canonical TypeReference issuance.
 *
 * Authority split:
 *
 * ```text
 * Reflection adapter:
 *   KType / KClass -> ReflectionTypeIdentityMaterial
 *
 * Metamodel domain:
 *   ReflectionTypeIdentityMaterial
 *   -> CanonicalTypeText.ratify(...)
 *   -> CanonicalTypeId
 *   -> TypeCycleKey
 *   -> CanonicalTypeSignature
 *   -> TypeReference
 * ```
 *
 * This renderer must not:
 *
 * - call TypeReference.issue(...);
 * - call CanonicalTypeText.ratify(...);
 * - issue CanonicalTypeId / TypeCycleKey / CanonicalTypeSignature;
 * - retain backend handles in emitted material;
 * - expose JVM internal names as canonical candidate text.
 */
class ReflectionTypeIdentityMaterialRenderer private constructor(
    private val normalizationGuard: ReflectionNormalizationGuard,
    val typeSignatureNormalizationVersion: Long,
) {
    fun render(
        type: KType,
    ): ReflectionTypeIdentityMaterial {
        val state = RenderState()

        appendTypeIdentity(
            type = type,
            state = state,
            zeroBasedDepth = 0,
        )

        val idText = state.idBuilder.toString()

        /*
         * cycleText differs from idText only by nullability markers.
         *
         * Do not maintain a second builder during recursive rendering. That
         * doubles append traffic and makes the hot renderer path noisier.
         */
        val cycleText =
            if (state.sawNullableMarker) {
                stripNullabilityMarkers(idText)
            } else {
                idText
            }

        return ReflectionTypeIdentityMaterial.issue(
            idText = idText,
            cycleText = cycleText,
            signatureText = idText,
            shapeSummary = observeShape(type),
            useSiteAnnotations = OrderedUseSiteAnnotations.empty(),
            typeNestingDepth = state.maxObservedZeroBasedDepth + 1,
        )
    }

    private fun appendTypeIdentity(
        type: KType,
        state: RenderState,
        zeroBasedDepth: Int,
    ) {
        requireDepthWithinDomainLaw(
            zeroBasedDepth = zeroBasedDepth,
            type = type,
        )

        if (zeroBasedDepth > state.maxObservedZeroBasedDepth) {
            state.maxObservedZeroBasedDepth = zeroBasedDepth
        }

        val classifier = requireKClassBackedType(type)

        appendClassName(
            state = state,
            kClass = classifier,
            sourceType = type,
        )

        appendTypeArguments(
            state = state,
            type = type,
            zeroBasedDepth = zeroBasedDepth,
        )

        if (type.isMarkedNullable) {
            state.idBuilder.append(NULLABILITY_MARKER)
            state.sawNullableMarker = true
        }
    }

    private fun appendClassName(
        state: RenderState,
        kClass: KClass<*>,
        sourceType: KType,
    ) {
        val qualifiedName =
            kClass.qualifiedName
                ?: throw StrictModeViolationException(
                    "Reflection renderer rejects anonymous/local/non-qualified classes: " +
                            "type=$sourceType, javaName=${kClass.java.name}",
                )

        /*
         * Do not lower JVM binary names here.
         *
         * KClass.qualifiedName is already the Kotlin-level qualified name when it is
         * available. If the value still contains JVM-internal spelling such as '$',
         * '/', ';', '[' or ']', the reflection surface is not safe identity material
         * for this adapter and must fail closed.
         *
         * In particular, do not replace '$' with '.'. That can collapse:
         *
         * - a generated or obfuscated class whose binary/simple name contains '$';
         * - a true nested class whose JVM binary name contains '$';
         *
         * into the same candidate canonical type text.
         */
        normalizationGuard.requireReflectionClassifierNameSurface(
            field = "ReflectionTypeIdentityMaterial.classifierName",
            value = qualifiedName,
        )

        state.idBuilder.append(qualifiedName)
    }

    private fun appendTypeArguments(
        state: RenderState,
        type: KType,
        zeroBasedDepth: Int,
    ) {
        val arguments = type.arguments
        if (arguments.isEmpty()) {
            return
        }

        state.idBuilder.append('<')

        var index = 0
        while (index < arguments.size) {
            if (index > 0) {
                state.idBuilder.append(',')
            }

            appendTypeArgumentIdentity(
                state = state,
                argument = arguments[index],
                ownerType = type,
                argumentIndex = index,
                zeroBasedDepth = zeroBasedDepth + 1,
            )

            index += 1
        }

        state.idBuilder.append('>')
    }

    private fun appendTypeArgumentIdentity(
        state: RenderState,
        argument: KTypeProjection,
        ownerType: KType,
        argumentIndex: Int,
        zeroBasedDepth: Int,
    ) {
        val argumentType =
            argument.type
                ?: throw StrictModeViolationException(
                    "Strict mode rejects star projection: " +
                            "ownerType=$ownerType, argumentIndex=$argumentIndex",
                )

        when (argument.variance) {
            KVariance.IN,
            KVariance.OUT,
                -> {
                throw StrictModeViolationException(
                    "Strict mode rejects use-site variance because variance is not yet represented " +
                            "as a separate metamodel identity axis: ownerType=$ownerType, " +
                            "argumentIndex=$argumentIndex, variance=${argument.variance}",
                )
            }

            KVariance.INVARIANT,
            null,
                -> {
                // Accepted.
            }
        }

        appendTypeIdentity(
            type = argumentType,
            state = state,
            zeroBasedDepth = zeroBasedDepth,
        )
    }


    /**
     * Shape observation keeps KType as the source value and only lowers to JVM
     * class material where reflection capability requires it.
     *
     * This prevents the renderer from becoming an early type-erasure stage.
     */
    private fun observeShape(
        type: KType,
    ): TypeShapeSummary {
        val kClass = requireKClassBackedType(type)

        atomicFamily(kClass)?.let { family ->
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.ATOMIC,
                genericArity = 0,
                arrayRank = 0,
                atomicFamily = family,
            )
        }

        /*
         * JVM erasure is consulted only after KType-level handling has preserved
         * the argument surface for shape summaries that need it.
         */
        val javaClass = kClass.java

        if (javaClass == Void.TYPE || javaClass == Void::class.java) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.VOID,
                genericArity = 0,
                arrayRank = 0,
            )
        }

        if (kClass == Unit::class) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.UNIT,
                genericArity = 0,
                arrayRank = 0,
            )
        }

        if (javaClass.isEnum) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.ENUM,
                genericArity = 0,
                arrayRank = 0,
            )
        }

        if (javaClass.isArray) {
            val arrayObservation = observeArray(javaClass)

            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.ARRAY,
                genericArity = type.arguments.size,
                arrayRank = arrayObservation.rank,
                arrayComponentHint = arrayObservation.componentHint,
            )
        }

        if (Map::class.java.isAssignableFrom(javaClass)) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.MAP,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        if (Iterable::class.java.isAssignableFrom(javaClass)) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.COLLECTION,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        if (kClass.isSealed && javaClass.isInterface) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.SEALED_INTERFACE,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        if (javaClass.isInterface) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.INTERFACE,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        if (kClass.isSealed) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.SEALED_CLASS,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        if (kClass.isAbstract) {
            return TypeShapeSummary.issue(
                kind = CanonicalTypeShapeKind.ABSTRACT_CLASS,
                genericArity = type.arguments.size,
                arrayRank = 0,
            )
        }

        return TypeShapeSummary.issue(
            kind = CanonicalTypeShapeKind.COMPOSITE,
            genericArity = type.arguments.size,
            arrayRank = 0,
        )
    }

    private fun observeArray(
        javaClass: Class<*>,
    ): ArrayObservation {
        var rank = 0
        var current: Class<*>? = javaClass

        while (current != null && current.isArray) {
            rank += 1
            current = current.componentType
        }

        if (current == null) {
            throw StrictModeViolationException(
                "Reflection array observation reached null component class: " +
                        "arrayClass=${javaClass.name}",
            )
        }

        val componentHint =
            ArrayComponentShapeHint.issue(
                hasGenericComponent = false,
                componentGenericArityHint = 0,
                componentShapeKindHint = componentShapeKindHint(current),
            )

        return ArrayObservation(
            rank = rank,
            componentHint = componentHint,
        )
    }

    private fun componentShapeKindHint(
        component: Class<*>,
    ): CanonicalTypeShapeKind? {
        if (component == Void.TYPE || component == Void::class.java) {
            return CanonicalTypeShapeKind.VOID
        }

        if (component.kotlin == Unit::class) {
            return CanonicalTypeShapeKind.UNIT
        }

        if (component.isEnum) {
            return CanonicalTypeShapeKind.ENUM
        }

        if (component.isPrimitive) {
            return CanonicalTypeShapeKind.ATOMIC
        }

        return null
    }

    private fun atomicFamily(
        kClass: KClass<*>,
    ): AtomicShapeFamily? {
        return when (kClass) {
            Boolean::class -> AtomicShapeFamily.BOOLEAN_SCALAR
            Char::class -> AtomicShapeFamily.CHARACTER_SCALAR

            Byte::class,
            Short::class,
            Int::class,
            Long::class,
                -> AtomicShapeFamily.INTEGRAL_NUMBER

            Float::class,
            Double::class,
                -> AtomicShapeFamily.FLOATING_NUMBER

            BigDecimal::class,
            BigInteger::class,
                -> AtomicShapeFamily.DECIMAL_NUMBER

            String::class -> AtomicShapeFamily.STRING_TEXT

            Instant::class -> AtomicShapeFamily.TEMPORAL_INSTANT

            LocalDate::class,
            LocalTime::class,
            LocalDateTime::class,
            OffsetTime::class,
            OffsetDateTime::class,
            ZonedDateTime::class,
                -> AtomicShapeFamily.TEMPORAL_LOCAL

            Duration::class,
            Period::class,
                -> AtomicShapeFamily.DURATION_SCALAR

            UUID::class -> AtomicShapeFamily.UUID_SCALAR

            else -> null
        }
    }

    private fun requireKClassBackedType(
        type: KType,
    ): KClass<*> {
        return type.classifier as? KClass<*>
            ?: throw StrictModeViolationException(
                "Reflection renderer only supports KClass-backed KType values: $type",
            )
    }

    private fun requireDepthWithinDomainLaw(
        zeroBasedDepth: Int,
        type: KType,
    ) {
        val semanticDepth = zeroBasedDepth + 1

        TypeNestingDepthLaw.requireWithinLimit(
            field = "ReflectionTypeIdentityMaterialRenderer.typeNestingDepth",
            depth = semanticDepth,
        )

        if (semanticDepth <= 0) {
            throw StrictModeViolationException(
                "Reflected type nesting produced invalid semantic depth: " +
                        "depth=$semanticDepth, type=$type",
            )
        }
    }

    private fun requireSafeReflectionClassName(
        field: String,
        value: String,
        sourceType: KType,
    ) {
        if (value.isEmpty()) {
            throw StrictModeViolationException(
                "$field must not be empty: sourceType=$sourceType",
            )
        }

        var index = 0
        while (index < value.length) {
            when (val ch = value[index]) {
                '\u0000',
                '|',
                '/',
                ';',
                '[',
                ']',
                '<',
                '>',
                '\n',
                '\r',
                '\t',
                    -> {
                    throw StrictModeViolationException(
                        "$field contains forbidden JVM/order/control material: " +
                                "char=${ch.code}, index=$index, value=$value, sourceType=$sourceType",
                    )
                }
            }

            index += 1
        }

        /*
         * Keep the existing adapter guard as the final adapter-local surface
         * check. This preserves one guard authority while moving obvious
         * backend-pollution rejection before recursive construction completes.
         */
        normalizationGuard.requireReflectionComponentSurface(
            field = field,
            value = value,
        )
    }

    private fun stripNullabilityMarkers(
        idText: String,
    ): String {
        val stripped = StringBuilder(idText.length)

        var index = 0
        while (index < idText.length) {
            val ch = idText[index]
            if (ch != NULLABILITY_MARKER) {
                stripped.append(ch)
            }
            index += 1
        }

        return stripped.toString()
    }

    companion object {
        private const val NULLABILITY_MARKER: Char = '?'

        @JvmStatic
        fun issue(
            typeSignatureNormalizationVersion: Long,
        ): ReflectionTypeIdentityMaterialRenderer {
            if (typeSignatureNormalizationVersion < 0L) {
                throw StrictModeViolationException(
                    "ReflectionTypeIdentityMaterialRenderer.typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion,
                )
            }

            return ReflectionTypeIdentityMaterialRenderer(
                normalizationGuard = ReflectionNormalizationGuard.issue(),
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
        }
    }
}

/**
 * Adapter-rendered candidate material.
 *
 * This is not canonical identity.
 * This is not TypeReference.
 * This is not a domain-issued canonical VO.
 *
 * The metamodel domain must still ratify all text fields before issuing
 * CanonicalTypeId, TypeCycleKey, CanonicalTypeSignature, or TypeReference.
 */
class ReflectionTypeIdentityMaterial private constructor(
    val idText: String,
    val cycleText: String,
    val signatureText: String,
    val shapeSummary: TypeShapeSummary,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val typeNestingDepth: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            idText: String,
            cycleText: String,
            signatureText: String,
            shapeSummary: TypeShapeSummary,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            typeNestingDepth: Int,
        ): ReflectionTypeIdentityMaterial {
            return ReflectionTypeIdentityMaterial(
                idText = idText,
                cycleText = cycleText,
                signatureText = signatureText,
                shapeSummary = shapeSummary,
                useSiteAnnotations = useSiteAnnotations,
                typeNestingDepth = typeNestingDepth,
            )
        }
    }
}

private class RenderState {
    val idBuilder: StringBuilder = StringBuilder()
    var sawNullableMarker: Boolean = false
    var maxObservedZeroBasedDepth: Int = 0
}

private class ArrayObservation(
    val rank: Int,
    val componentHint: ArrayComponentShapeHint,
)