package metamodel.adapter.reflection

import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.vo.AnnotationDescriptor
import metamodel.domain.vo.TypeReference
import metamodel.port.outgoing.NormalizationEngine
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

/**
 * Reflection adapter factory for TypeReference.
 *
 * Adapter role:
 * - Convert Kotlin reflection KType into a pure metamodel TypeReference.
 * - Preserve a reflection-only KType handle behind an internal adapter interface.
 * - Validate normalized output through the injected Kontrakt NormalizationEngine.
 *
 * Non-responsibilities:
 * - raw fact extraction
 * - constructor/property projection
 * - active-member ordering
 * - nodeIdentity64 derivation
 *
 * This class is replaceable by a future KSP TypeReference factory.
 */
class ReflectionTypeReferenceFactory private constructor(
    private val normalizationGuard: ReflectionNormalizationGuard,
    val typeSignatureNormalizationVersion: Long,
) {
    fun create(
        type: KType,
    ): TypeReference {
        rejectUnsupportedTypeShape(
            type = type,
            depth = 0,
        )

        val id = renderType(
            type = type,
            includeNullability = true,
            depth = 0,
        )

        val cycleId = renderType(
            type = type,
            includeNullability = false,
            depth = 0,
        )

        val signature = id

        normalizationGuard.requireNormalizedComponent(
            field = "TypeReference.id",
            value = id,
        )
        normalizationGuard.requireNormalizedComponent(
            field = "TypeReference.cycleId",
            value = cycleId,
        )
        normalizationGuard.requireNormalizedComponent(
            field = "TypeReference.signature",
            value = signature,
        )

        return ReflectionBackedTypeReferenceImpl(
            kType = type,
            id = id,
            cycleId = cycleId,
            signature = signature,
            useSiteAnnotations = emptyList(),
        )
    }

    /**
     * Intentionally no create(KClass<*>) overload.
     *
     * KClass.starProjectedType can introduce List<*>-style shapes and bypass the
     * strict no-star-projection rule. Callers must supply an explicit KType.
     */
    private fun rejectUnsupportedTypeShape(
        type: KType,
        depth: Int,
    ) {
        requireDepthWithinLimit(
            type = type,
            depth = depth,
            phase = "validation",
        )

        val classifier = type.classifier

        if (classifier !is KClass<*>) {
            throw StrictModeViolationException(
                "Reflection TypeReference factory only supports concrete KClass-backed KType values: $type"
            )
        }

        var i = 0
        while (i < type.arguments.size) {
            val argument = type.arguments[i]
            val argumentType = argument.type

            if (argumentType == null) {
                throw StrictModeViolationException(
                    "Strict mode rejects star projections in reflected type: type=$type, argumentIndex=$i"
                )
            }

            rejectUnsupportedTypeShape(
                type = argumentType,
                depth = depth + 1,
            )

            i++
        }
    }

    private fun renderType(
        type: KType,
        includeNullability: Boolean,
        depth: Int,
    ): String {
        requireDepthWithinLimit(
            type = type,
            depth = depth,
            phase = "rendering",
        )

        val classifier = type.classifier as? KClass<*>
            ?: throw StrictModeViolationException(
                "Cannot render non-KClass reflected type: $type"
            )

        val builder = StringBuilder()
        appendClassName(builder, classifier)

        if (type.arguments.isNotEmpty()) {
            builder.append('<')

            var i = 0
            while (i < type.arguments.size) {
                if (i > 0) {
                    builder.append(',')
                }

                appendTypeArgument(
                    builder = builder,
                    argument = type.arguments[i],
                    includeNullability = includeNullability,
                    ownerType = type,
                    argumentIndex = i,
                    depth = depth + 1,
                )

                i++
            }

            builder.append('>')
        }

        if (includeNullability && type.isMarkedNullable) {
            builder.append('?')
        }

        return builder.toString()
    }

    private fun appendClassName(
        builder: StringBuilder,
        kClass: KClass<*>,
    ) {
        /*
         * This is adapter-specific spelling canonicalization only.
         * It is not Unicode normalization.
         * Unicode normalization is enforced later by ReflectionNormalizationGuard.
         */
        val rawName = kClass.qualifiedName ?: kClass.java.name
        builder.append(rawName.replace('$', '.'))
    }

    private fun appendTypeArgument(
        builder: StringBuilder,
        argument: KTypeProjection,
        includeNullability: Boolean,
        ownerType: KType,
        argumentIndex: Int,
        depth: Int,
    ) {
        val argumentType = argument.type
            ?: throw StrictModeViolationException(
                "Strict mode rejects star projection: ownerType=$ownerType, argumentIndex=$argumentIndex"
            )

        when (argument.variance) {
            KVariance.IN -> builder.append("in ")
            KVariance.OUT -> builder.append("out ")
            KVariance.INVARIANT,
            null -> Unit
        }

        builder.append(
            renderType(
                type = argumentType,
                includeNullability = includeNullability,
                depth = depth,
            ),
        )
    }

    private fun requireDepthWithinLimit(
        type: KType,
        depth: Int,
        phase: String,
    ) {
        if (depth > MAX_GENERIC_RENDER_DEPTH) {
            throw StrictModeViolationException(
                "Reflected type nesting exceeds deterministic $phase depth limit: " +
                        "maxDepth=$MAX_GENERIC_RENDER_DEPTH, type=$type"
            )
        }
    }

    companion object {
        private const val MAX_GENERIC_RENDER_DEPTH: Int = 32

        @JvmStatic
        fun issue(
            normalizationEngine: NormalizationEngine,
            typeSignatureNormalizationVersion: Long,
        ): ReflectionTypeReferenceFactory {
            if (typeSignatureNormalizationVersion < 0L) {
                throw StrictModeViolationException(
                    "ReflectionTypeReferenceFactory.typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion
                )
            }

            return ReflectionTypeReferenceFactory(
                normalizationGuard = ReflectionNormalizationGuard.issue(normalizationEngine),
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
        }
    }
}

/**
 * Adapter-internal bridge.
 *
 * Domain code must only see TypeReference.
 * ReflectionRawTypeFactsProvider may downcast to this interface because both
 * producer and consumer live in the same reflection adapter.
 */
internal interface ReflectionBackedTypeReference : TypeReference {
    val kType: KType
}

/**
 * Reflection-backed TypeReference implementation.
 *
 * Not a data class.
 * No copy-style reconstruction.
 */
private class ReflectionBackedTypeReferenceImpl(
    override val kType: KType,
    override val id: String,
    override val cycleId: String,
    override val signature: String,
    override val useSiteAnnotations: List<AnnotationDescriptor>,
) : ReflectionBackedTypeReference