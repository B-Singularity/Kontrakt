package adapter.reflection

import metamodel.domain.service.TypeIdentity64Deriver
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolution
import stage.canonicalization.material.TypeReference
import stage.input.diagnostics.StrictModeViolationException
import stage.input.material.ConstructorCandidateFact
import stage.input.material.ConstructorParameterFact
import stage.input.material.DeclarationOrdinal
import stage.input.material.DefaultValuePresence
import stage.input.material.MemberOrigin
import stage.input.material.NullabilityKind
import stage.input.material.PropertyFact
import stage.input.material.PropertyMutability
import stage.input.material.PropertyStorageKind
import stage.input.material.RawTypeFactsDTO
import stage.input.material.VisibilityKind
import stage.normalization.contract.MetamodelProtocolOrdering
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.jvmErasure

/**
 * Reflection implementation of RawTypeFactsProvider.
 *
 * Hexagonal role:
 *
 * - Implements the outbound raw-fact port.
 * - Contains all Kotlin/JVM reflection knowledge.
 * - Can be replaced by a future KSP adapter without changing Planning Core.
 *
 * Compiler-style role:
 *
 * - Lowers reflected KType/KClass information into raw metamodel facts.
 * - Does not perform constructor selection.
 * - Does not perform property demotion.
 * - Does not perform canonical active-member ordering.
 * - Does not participate in active-cycle detection.
 *
 * Determinism policy:
 *
 * - Reflection enumeration order is never trusted as declaration order.
 * - Declaration ordinals are emitted as DeclarationOrdinal.unavailable() unless
 *   a stable source-order reconstruction layer is introduced.
 * - Local ArrayList/HashSet instances are producer-side work buffers only.
 * - RawTypeFactsDTO.issue(...) is the deterministic boundary that performs
 *   duplicate validation, deterministic sequencing, and immutable freezing
 *   through MetamodelFactSequence.
 *
 * Reflection performance law:
 *
 * Kotlin reflection APIs such as KClass.memberProperties,
 * KClass.declaredMemberProperties, and KClass.constructors are treated as
 * expensive metadata surfaces.
 *
 * This provider therefore:
 *
 * - snapshots constructors once per resolveRawFacts call;
 * - snapshots member properties once per resolveRawFacts call;
 * - snapshots declared property names once per resolveRawFacts call;
 * - uses the declared-name set for O(1)-style origin checks;
 * - creates constructor parameter TypeReference values once and shares them
 *   between constructor-signature rendering and parameter-fact emission.
 *
 * This class deliberately does not introduce a cross-call reflection cache.
 * A higher adapter-level cache can be added later around RawTypeFactsProvider if
 * profiling proves repeated resolution of the same KClass is a bottleneck.
 *
 * Normalization boundary law:
 *
 * This provider does not call NormalizationEngine.
 * This provider does not call CanonicalTypeText.ratify(...).
 * This provider does not perform NFC checks.
 *
 * Reflection-specific type text ratification is owned by the TypeReference
 * issuance pipeline:
 *
 *     ReflectionTypeReferenceBridge
 *     -> CanonicalTypeReferenceIssuer
 *     -> TypeReference
 *
 * This provider uses [ReflectionNormalizationGuard] only as a cheap
 * adapter-local surface preflight for strings it renders directly:
 *
 * - owner type FQCN;
 * - constructor signature.
 *
 * The guard is intentionally not a Unicode normalization authority.
 *
 * Reflection limitations:
 *
 * - Unnamed constructor parameters are rejected because parameter names
 *   participate in canonical active-member identity.
 * - Java/platform nullability is emitted as UNKNOWN.
 * - Delegated properties are not inferred from JVM "$delegate" naming.
 * - JVM nested-class spelling is canonicalized by replacing '$' with '.' before
 *   adapter-local surface preflight.
 */
class ReflectionRawTypeFactsProvider private constructor(
    private val typeReferenceBridge: ReflectionTypeReferenceBridge,
    private val normalizationGuard: ReflectionNormalizationGuard,
    private val typeIdentity64Deriver: TypeIdentity64Deriver,
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        val kType = typeHandleRegistry.requireKType(reference)
        val kClass = kType.jvmErasure
        val ownerTypeFqcn = renderOwnerTypeFqcn(kClass)
        val ownerHasKotlinMetadata = hasKotlinMetadata(kClass)

        /*
         * Reflection surface snapshot.
         *
         * These calls are intentionally centralized here so lower functions do
         * not accidentally re-query expensive kotlin-reflect metadata surfaces.
         */
        val constructorSurface = kClass.constructors
        val propertySurface = kClass.memberProperties
        val declaredPropertyNames = declaredPropertyNameSet(kClass)

        val constructors =
            resolveConstructors(
                ownerTypeFqcn = ownerTypeFqcn,
                constructors = constructorSurface,
                ownerHasKotlinMetadata = ownerHasKotlinMetadata,
            )

        val properties =
            resolveProperties(
                ownerTypeFqcn = ownerTypeFqcn,
                ownerClass = kClass,
                properties = propertySurface,
                declaredPropertyNames = declaredPropertyNames,
                ownerHasKotlinMetadata = ownerHasKotlinMetadata,
            )

        return RawTypeFactsResolution.actualResolution(
            facts =
                RawTypeFactsDTO.issue(
                    typeIdentity64 = typeIdentity64Deriver.deriveIdentity64(reference),
                    typeIdentityAlgorithmId = typeIdentity64Deriver.identityAlgorithmId,
                    typeIdentityAlgorithmVersion = typeIdentity64Deriver.identityAlgorithmVersion,
                    ownerTypeFqcn = ownerTypeFqcn,
                    normalizationVersion = typeReferenceBridge.typeSignatureNormalizationVersion,
                    constructors = constructors,
                    properties = properties,
                ),
        )
    }

    private fun resolveConstructors(
        ownerTypeFqcn: String,
        constructors: Collection<KFunction<Any>>,
        ownerHasKotlinMetadata: Boolean,
    ): Collection<ConstructorCandidateFact> {
        /*
         * Local producer buffer only.
         *
         * RawTypeFactsDTO.issue(...) owns final deterministic ordering and
         * duplicate validation for constructor candidates.
         */
        val result = ArrayList<ConstructorCandidateFact>(constructors.size)

        val iterator = constructors.iterator()
        while (iterator.hasNext()) {
            val constructor = iterator.next()
            val parameters =
                projectValueParametersInDeterministicIndexOrder(
                    ownerTypeFqcn = ownerTypeFqcn,
                    constructor = constructor,
                    ownerHasKotlinMetadata = ownerHasKotlinMetadata,
                )

            result.add(
                ConstructorCandidateFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    constructorSignature =
                        renderConstructorSignature(
                            ownerTypeFqcn = ownerTypeFqcn,
                            valueParameters = parameters,
                        ),
                    constructorSignatureNormalizationVersion =
                        typeReferenceBridge.typeSignatureNormalizationVersion,
                    declarationOrdinal = DeclarationOrdinal.unavailable(),
                    visibility = mapVisibility(constructor.visibility),
                    origin = MemberOrigin.DECLARED,
                    parameters =
                        resolveConstructorParameters(
                            ownerTypeFqcn = ownerTypeFqcn,
                            valueParameters = parameters,
                        ),
                ),
            )
        }

        return result
    }

    private fun projectValueParametersInDeterministicIndexOrder(
        ownerTypeFqcn: String,
        constructor: KFunction<Any>,
        ownerHasKotlinMetadata: Boolean,
    ): List<ConstructorParameterProjection> {
        /*
         * Constructor signature rendering and ConstructorParameterFact emission
         * must use the exact same VALUE-parameter ordering rule and the exact same
         * TypeReference instances.
         *
         * This projection prevents renderConstructorSignature(...) and
         * resolveConstructorParameters(...) from calling
         * typeReferenceBridge.issueReference(...) independently for the same
         * KParameter.type.
         *
         * Ordering law:
         *
         * - Kotlin reflection enumeration order is not trusted.
         * - JVM / platform library sort stability is not a semantic authority.
         * - VALUE parameters are ordered by the order-defined integer ordering
         *   of KParameter.index.
         * - Duplicate VALUE parameter indexes fail closed.
         * - Non-compact VALUE parameter index ranges fail closed.
         *
         * Why this remains adapter-local for now:
         *
         * This method is not publishing canonical ordering by itself. It builds an
         * adapter-local projection so constructor signature rendering and parameter
         * fact emission share the same TypeReference instances. Final immutable
         * sequencing and duplicate validation still belong to RawTypeFactsDTO.issue(...).
         *
         * A later metamodel hardening pass may promote this projection into a
         * domain-side deterministic sequence VO. Do not introduce that abstraction
         * in this bridge refactor cut.
         */
        val rawParameters = constructor.parameters

        /*
         * Capacity uses the upper bound from the reflection surface.
         *
         * Some entries may be INSTANCE / EXTENSION_RECEIVER in non-constructor
         * KFunction surfaces, but this provider handles constructors and filters to
         * VALUE parameters. The upper bound still prevents avoidable ArrayList
         * resizing without a separate pre-count pass.
         */
        val orderedValueParameters = ArrayList<KParameter>(rawParameters.size)

        val parameterIterator = rawParameters.iterator()
        while (parameterIterator.hasNext()) {
            val parameter = parameterIterator.next()

            if (parameter.kind == KParameter.Kind.VALUE) {
                insertValueParameterByProtocolIndex(
                    ownerTypeFqcn = ownerTypeFqcn,
                    constructor = constructor,
                    orderedValueParameters = orderedValueParameters,
                    candidate = parameter,
                )
            }
        }

        requireCompactValueParameterIndexes(
            ownerTypeFqcn = ownerTypeFqcn,
            constructor = constructor,
            orderedValueParameters = orderedValueParameters,
        )

        val projections =
            ArrayList<ConstructorParameterProjection>(orderedValueParameters.size)

        var localIndex = 0
        while (localIndex < orderedValueParameters.size) {
            val parameter = orderedValueParameters[localIndex]
            val parameterName =
                parameter.name
                    ?: throw StrictModeViolationException(
                        "Reflection adapter refuses unnamed constructor parameter because parameter name " +
                                "participates in canonical active-member identity: " +
                                "ownerType=$ownerTypeFqcn, " +
                                "parameterIndex=$localIndex, " +
                                "kParameterIndex=${parameter.index}",
                    )

            projections.add(
                ConstructorParameterProjection(
                    parameter = parameter,
                    localIndex = localIndex,
                    name = parameterName,
                    typeReference = typeReferenceBridge.issueReference(parameter.type),
                    nullability =
                        mapNullability(
                            isMarkedNullable = parameter.type.isMarkedNullable,
                            declarationHasKotlinMetadata = ownerHasKotlinMetadata,
                        ),
                    defaultValuePresence = mapDefaultPresence(parameter),
                ),
            )

            localIndex += 1
        }

        return projections
    }

    private fun resolveConstructorParameters(
        ownerTypeFqcn: String,
        valueParameters: List<ConstructorParameterProjection>,
    ): Collection<ConstructorParameterFact> {
        val result =
            ArrayList<ConstructorParameterFact>(valueParameters.size)

        var localIndex = 0
        while (localIndex < valueParameters.size) {
            val parameter = valueParameters[localIndex]

            result.add(
                ConstructorParameterFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    name = parameter.name,
                    typeReference = parameter.typeReference,
                    parameterIndex = parameter.localIndex,
                    nullability = parameter.nullability,
                    defaultValuePresence = parameter.defaultValuePresence,
                    typeSignatureNormalizationVersion =
                        typeReferenceBridge.typeSignatureNormalizationVersion,
                ),
            )

            localIndex += 1
        }

        return result
    }


    private fun insertValueParameterByProtocolIndex(
        ownerTypeFqcn: String,
        constructor: KFunction<Any>,
        orderedValueParameters: MutableList<KParameter>,
        candidate: KParameter,
    ) {
        /*
         * Explicit order insertion order.
         *
         * Do not use:
         *
         * - java.lang.Integer.compare(...);
         * - compareBy(...);
         * - List.sortWith(...);
         * - platform sort stability as semantic authority.
         *
         * KParameter.index is accepted only as raw reflection material that is
         * re-validated under our own order law.
         */
        var insertAt = 0

        while (insertAt < orderedValueParameters.size) {
            val current = orderedValueParameters[insertAt]

            val comparison =
                MetamodelProtocolOrdering.compareInt(
                    left = candidate.index,
                    right = current.index,
                )

            if (comparison == 0) {
                throw StrictModeViolationException(
                    "Reflection adapter observed duplicate VALUE constructor parameter index. " +
                            "Constructor parameter ordering is not a strict total order: " +
                            "ownerType=$ownerTypeFqcn, " +
                            "constructor=$constructor, " +
                            "duplicateIndex=${candidate.index}, " +
                            "leftName=${current.name}, " +
                            "rightName=${candidate.name}",
                )
            }

            if (comparison < 0) {
                break
            }

            insertAt += 1
        }

        orderedValueParameters.add(
            insertAt,
            candidate,
        )
    }

    private fun requireCompactValueParameterIndexes(
        ownerTypeFqcn: String,
        constructor: KFunction<Any>,
        orderedValueParameters: List<KParameter>,
    ) {
        /*
         * Compactness law:
         *
         * After filtering to VALUE parameters and ordering by KParameter.index, the
         * reflected index surface must be exactly:
         *
         *     0, 1, 2, ..., N - 1
         *
         * We do not use KParameter.index as a domain ordinal directly. We accept it
         * only if it can be validated as a compact adapter-local ordering witness.
         *
         * If Kotlin/JVM reflection ever exposes gaps or shifted indexes for
         * constructor VALUE parameters, the adapter fails closed rather than
         * leaking a platform-specific ordinal law into metamodel facts.
         */
        var expectedIndex = 0

        while (expectedIndex < orderedValueParameters.size) {
            val parameter = orderedValueParameters[expectedIndex]

            val comparison =
                MetamodelProtocolOrdering.compareInt(
                    left = parameter.index,
                    right = expectedIndex,
                )

            if (comparison != 0) {
                throw StrictModeViolationException(
                    "Reflection adapter observed non-compact VALUE constructor parameter indexes. " +
                            "Constructor parameter ordering cannot be lowered into compact metamodel parameter indexes: " +
                            "ownerType=$ownerTypeFqcn, " +
                            "constructor=$constructor, " +
                            "expectedIndex=$expectedIndex, " +
                            "actualKParameterIndex=${parameter.index}, " +
                            "parameterName=${parameter.name}",
                )
            }

            expectedIndex += 1
        }
    }

    private fun resolveProperties(
        ownerTypeFqcn: String,
        ownerClass: KClass<*>,
        properties: Collection<KProperty1<out Any, *>>,
        declaredPropertyNames: Set<String>,
        ownerHasKotlinMetadata: Boolean,
    ): Collection<PropertyFact> {
        /*
         * Local producer buffer only.
         *
         * RawTypeFactsDTO.issue(...) owns final deterministic ordering and
         * duplicate validation for property facts.
         */
        val result = ArrayList<PropertyFact>(properties.size)

        val iterator = properties.iterator()
        while (iterator.hasNext()) {
            val property = iterator.next()
            val propertyTypeReference =
                typeReferenceBridge.issueReference(property.returnType)

            val propertyOrigin =
                mapPropertyOrigin(
                    property = property,
                    ownerClass = ownerClass,
                    declaredPropertyNames = declaredPropertyNames,
                )

            val propertyDeclarationHasKotlinMetadata =
                propertyDeclarationHasKotlinMetadata(
                    property = property,
                    fallbackOwnerHasKotlinMetadata = ownerHasKotlinMetadata,
                )

            result.add(
                PropertyFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    name = property.name,
                    typeReference = propertyTypeReference,
                    declarationOrdinal = DeclarationOrdinal.unavailable(),
                    nullability =
                        mapNullability(
                            isMarkedNullable = property.returnType.isMarkedNullable,
                            declarationHasKotlinMetadata =
                                propertyDeclarationHasKotlinMetadata,
                        ),
                    declaredVisibility = mapVisibility(property.visibility),
                    setterVisibility = mapSetterVisibility(property),
                    origin = propertyOrigin,
                    mutability = mapMutability(property),
                    storageKind = inferStorageKind(property),
                    typeSignatureNormalizationVersion =
                        typeReferenceBridge.typeSignatureNormalizationVersion,
                ),
            )
        }

        return result
    }

    private fun renderConstructorSignature(
        ownerTypeFqcn: String,
        valueParameters: List<ConstructorParameterProjection>,
    ): String {
        val builder = StringBuilder()
        builder.append(ownerTypeFqcn)
        builder.append('(')

        var index = 0
        while (index < valueParameters.size) {
            if (index > 0) {
                builder.append(',')
            }

            builder.append(valueParameters[index].typeReference.signature)
            index += 1
        }

        builder.append(')')

        val signature = builder.toString()

        normalizationGuard.requireReflectionComponentSurface(
            field = "ConstructorCandidateFact.constructorSignature",
            value = signature,
        )

        return signature
    }

    private fun renderOwnerTypeFqcn(
        kClass: KClass<*>,
    ): String {
        /*
         * Use Kotlin qualified name only.
         *
         * Do not fall back to java.name and do not replace '$' with '.'.
         *
         * java.name is JVM binary-name material and may contain '$' for nested,
         * generated, obfuscated, or otherwise backend-specific classes.
         *
         * Blindly lowering '$' to '.' can create identity collisions between
         * distinct bytecode-level classes. Raw facts must use the same safe
         * reflection classifier-name surface as TypeReference rendering.
         */
        val ownerTypeFqcn =
            kClass.qualifiedName
                ?: throw StrictModeViolationException(
                    "Reflection raw-facts provider rejects anonymous/local/non-qualified owner class: " +
                            "javaName=${kClass.java.name}",
                )

        normalizationGuard.requireReflectionClassifierNameSurface(
            field = "RawTypeFactsDTO.ownerTypeFqcn",
            value = ownerTypeFqcn,
        )

        return ownerTypeFqcn
    }

    private fun mapVisibility(
        visibility: KVisibility?,
    ): VisibilityKind {
        return when (visibility) {
            KVisibility.PUBLIC -> VisibilityKind.PUBLIC
            KVisibility.PROTECTED -> VisibilityKind.PROTECTED
            KVisibility.INTERNAL -> VisibilityKind.INTERNAL
            KVisibility.PRIVATE -> VisibilityKind.PRIVATE
            null -> VisibilityKind.UNKNOWN
        }
    }

    private fun mapSetterVisibility(
        property: KProperty1<out Any, *>,
    ): VisibilityKind? {
        val mutable =
            property as? KMutableProperty1<out Any, *>
                ?: return null

        return mapVisibility(mutable.setter.visibility)
    }

    private fun mapMutability(
        property: KProperty1<out Any, *>,
    ): PropertyMutability {
        return if (property is KMutableProperty1<out Any, *>) {
            PropertyMutability.MUTABLE
        } else {
            PropertyMutability.READ_ONLY
        }
    }

    private fun inferStorageKind(
        property: KProperty1<out Any, *>,
    ): PropertyStorageKind {
        if (property.isLateinit) {
            return PropertyStorageKind.LATEINIT
        }

        val javaField = property.javaField

        if (javaField == null) {
            return PropertyStorageKind.COMPUTED
        }

        /*
         * Do not infer DELEGATED from "$delegate".
         * That is Kotlin/JVM compiler implementation detail, not a stable
         * metamodel fact order.
         *
         * Positive backing-field recognition is limited to the stable case where
         * the Java field name equals the property name.
         */
        return if (javaField.name == property.name) {
            PropertyStorageKind.BACKING_FIELD
        } else {
            PropertyStorageKind.UNKNOWN
        }
    }

    private fun mapNullability(
        isMarkedNullable: Boolean,
        declarationHasKotlinMetadata: Boolean,
    ): NullabilityKind {
        if (isMarkedNullable) {
            return NullabilityKind.NULLABLE
        }

        return if (declarationHasKotlinMetadata) {
            NullabilityKind.NON_NULL
        } else {
            /*
             * Java/platform declarations often appear as isMarkedNullable=false
             * even when the actual nullability contract is unknown.
             */
            NullabilityKind.UNKNOWN
        }
    }

    private fun mapDefaultPresence(
        parameter: KParameter,
    ): DefaultValuePresence {
        return if (parameter.isOptional) {
            DefaultValuePresence.PRESENT
        } else {
            DefaultValuePresence.ABSENT
        }
    }

    private fun mapPropertyOrigin(
        property: KProperty1<out Any, *>,
        ownerClass: KClass<*>,
        declaredPropertyNames: Set<String>,
    ): MemberOrigin {
        if (property.name in declaredPropertyNames) {
            return MemberOrigin.DECLARED
        }

        val declaringClass =
            property.javaField?.declaringClass
                ?: property.javaGetter?.declaringClass

        if (declaringClass != null && declaringClass != ownerClass.java) {
            return MemberOrigin.INHERITED
        }

        /*
         * Reflection could not reliably attribute origin.
         * This is safer than incorrectly collapsing to DECLARED.
         */
        return MemberOrigin.UNKNOWN
    }

    private fun declaredPropertyNameSet(
        ownerClass: KClass<*>,
    ): Set<String> {
        /*
         * This set is a local membership index only.
         *
         * It is not canonical state.
         * It is not persisted.
         * It is not iterated for deterministic output.
         *
         * The iteration order of declaredMemberProperties is therefore not part
         * of the metamodel order.
         */
        val declared = ownerClass.declaredMemberProperties
        val names = HashSet<String>(declared.size.coerceAtLeast(16))

        val iterator = declared.iterator()
        while (iterator.hasNext()) {
            names.add(iterator.next().name)
        }

        return names
    }

    private fun propertyDeclarationHasKotlinMetadata(
        property: KProperty1<out Any, *>,
        fallbackOwnerHasKotlinMetadata: Boolean,
    ): Boolean {
        val declaringClass =
            property.javaField?.declaringClass
                ?: property.javaGetter?.declaringClass
                ?: return fallbackOwnerHasKotlinMetadata

        return declaringClass.getAnnotation(Metadata::class.java) != null
    }

    private fun hasKotlinMetadata(
        kClass: KClass<*>,
    ): Boolean {
        return kClass.java.getAnnotation(Metadata::class.java) != null
    }

    companion object {
        @JvmStatic
        fun issue(
            typeReferenceBridge: ReflectionTypeReferenceBridge,
            typeIdentity64Deriver: TypeIdentity64Deriver,
            typeHandleRegistry: ReflectionTypeHandleRegistry,
        ): ReflectionRawTypeFactsProvider {
            return ReflectionRawTypeFactsProvider(
                typeReferenceBridge = typeReferenceBridge,
                normalizationGuard = ReflectionNormalizationGuard.issue(),
                typeIdentity64Deriver = typeIdentity64Deriver,
                typeHandleRegistry = typeHandleRegistry,
            )
        }
    }
}

/**
 * Adapter-local projection of one constructor VALUE parameter.
 *
 * This object exists only inside ReflectionRawTypeFactsProvider.
 *
 * It prevents the constructor signature and constructor parameter facts from
 * independently creating TypeReference instances for the same KParameter.type.
 *
 * It is not a domain VO.
 * It is not stored in RawTypeFactsDTO.
 * It is not canonical state.
 */
private class ConstructorParameterProjection(
    val parameter: KParameter,
    val localIndex: Int,
    val name: String,
    val typeReference: TypeReference,
    val nullability: NullabilityKind,
    val defaultValuePresence: DefaultValuePresence,
)