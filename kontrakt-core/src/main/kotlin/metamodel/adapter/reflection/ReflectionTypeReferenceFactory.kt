package metamodel.adapter.reflection

import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.port.outgoing.NormalizationEngine
import metamodel.domain.vo.OrderedUseSiteAnnotations
import metamodel.domain.vo.TypeReference
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

/**
 * Reflection adapter factory for TypeReference.
 *
 * This class does not implement TypeReference and does not create a reflection-
 * backed TypeReference subclass.
 *
 * TypeReference is a final domain-issued VO. This factory renders reflection
 * KType material, asks a domain issuer to create the TypeReference, and stores
 * the adapter-only KType handle in ReflectionTypeHandleRegistry.
 */
class ReflectionTypeReferenceFactory private constructor(
    private val normalizationGuard: ReflectionNormalizationGuard,
    private val typeReferenceIssuer: ReflectionTypeReferenceIssuer,
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
    val typeSignatureNormalizationVersion: Long,
) {
    fun create(
        type: KType,
    ): TypeReference {
        val rendered = renderTypeIdentity(type)

        normalizationGuard.requireNormalizedComponent(
            field = "TypeReference.id",
            value = rendered.id,
        )

        if (rendered.cycleId != rendered.id) {
            normalizationGuard.requireNormalizedComponent(
                field = "TypeReference.cycleId",
                value = rendered.cycleId,
            )
        }

        /*
         * In this reflection adapter, signature is the same rendered surface as
         * id. Do not re-run normalization for the same String.
         */
        val signature = rendered.id

        val reference =
            typeReferenceIssuer.issue(
                idText = rendered.id,
                cycleText = rendered.cycleId,
                signatureText = signature,
                useSiteAnnotations = OrderedUseSiteAnnotations.empty(),
                typeNestingDepth = rendered.typeNestingDepth,
            )

        typeHandleRegistry.bind(
            reference = reference,
            kType = type,
        )

        return reference
    }

    private fun renderTypeIdentity(
        type: KType,
    ): RenderedTypeIdentity {
        val state = RenderState()

        appendTypeIdentity(
            type = type,
            state = state,
            depth = 0,
        )

        return RenderedTypeIdentity(
            id = state.idBuilder.toString(),
            cycleId = state.cycleIdBuilder.toString(),
            typeNestingDepth = state.maxObservedDepth + 1,
        )
    }

    private fun appendTypeIdentity(
        type: KType,
        state: RenderState,
        depth: Int,
    ) {
        requireDepthWithinLimit(
            type = type,
            depth = depth,
            phase = "rendering",
        )

        if (depth > state.maxObservedDepth) {
            state.maxObservedDepth = depth
        }

        val classifier =
            type.classifier as? KClass<*>
                ?: throw StrictModeViolationException(
                    "Reflection TypeReference factory only supports concrete KClass-backed KType values: $type",
                )

        appendClassName(
            state = state,
            kClass = classifier,
        )

        if (type.arguments.isNotEmpty()) {
            state.idBuilder.append('<')
            state.cycleIdBuilder.append('<')

            var index = 0
            while (index < type.arguments.size) {
                if (index > 0) {
                    state.idBuilder.append(',')
                    state.cycleIdBuilder.append(',')
                }

                appendTypeArgumentIdentity(
                    state = state,
                    argument = type.arguments[index],
                    ownerType = type,
                    argumentIndex = index,
                    depth = depth + 1,
                )

                index += 1
            }

            state.idBuilder.append('>')
            state.cycleIdBuilder.append('>')
        }

        if (type.isMarkedNullable) {
            state.idBuilder.append('?')
        }
    }

    private fun appendClassName(
        state: RenderState,
        kClass: KClass<*>,
    ) {
        val rawName = kClass.qualifiedName ?: kClass.java.name

        var index = 0
        while (index < rawName.length) {
            val rendered =
                if (rawName[index] == '$') {
                    '.'
                } else {
                    rawName[index]
                }

            state.idBuilder.append(rendered)
            state.cycleIdBuilder.append(rendered)

            index += 1
        }
    }

    private fun appendTypeArgumentIdentity(
        state: RenderState,
        argument: KTypeProjection,
        ownerType: KType,
        argumentIndex: Int,
        depth: Int,
    ) {
        val argumentType =
            argument.type
                ?: throw StrictModeViolationException(
                    "Strict mode rejects star projection: ownerType=$ownerType, argumentIndex=$argumentIndex",
                )

        when (argument.variance) {
            KVariance.IN,
            KVariance.OUT -> {
                throw StrictModeViolationException(
                    "Strict mode rejects use-site variance in reflected type because variance " +
                            "is not yet represented as a separate metamodel identity axis: " +
                            "ownerType=$ownerType, argumentIndex=$argumentIndex, variance=${argument.variance}",
                )
            }

            KVariance.INVARIANT,
            null -> {
                // Accepted.
            }
        }

        appendTypeIdentity(
            type = argumentType,
            state = state,
            depth = depth,
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
                        "maxDepth=$MAX_GENERIC_RENDER_DEPTH, type=$type",
            )
        }
    }

    companion object {
        private const val MAX_GENERIC_RENDER_DEPTH: Int = 32

        @JvmStatic
        fun issue(
            normalizationEngine: NormalizationEngine,
            typeReferenceIssuer: ReflectionTypeReferenceIssuer,
            typeHandleRegistry: ReflectionTypeHandleRegistry,
            typeSignatureNormalizationVersion: Long,
        ): ReflectionTypeReferenceFactory {
            if (typeSignatureNormalizationVersion < 0L) {
                throw StrictModeViolationException(
                    "ReflectionTypeReferenceFactory.typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion,
                )
            }

            return ReflectionTypeReferenceFactory(
                normalizationGuard = ReflectionNormalizationGuard.issue(normalizationEngine),
                typeReferenceIssuer = typeReferenceIssuer,
                typeHandleRegistry = typeHandleRegistry,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
        }
    }
}

private class RenderState {
    val idBuilder: StringBuilder = StringBuilder()
    val cycleIdBuilder: StringBuilder = StringBuilder()
    var maxObservedDepth: Int = 0
}

private class RenderedTypeIdentity(
    val id: String,
    val cycleId: String,
    val typeNestingDepth: Int,
)