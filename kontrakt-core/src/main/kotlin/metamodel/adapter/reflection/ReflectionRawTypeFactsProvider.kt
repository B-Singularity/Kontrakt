package metamodel.adapter.reflection

import metamodel.domain.dto.ConstructorCandidateFact
import metamodel.domain.dto.ConstructorParameterFact
import metamodel.domain.dto.DefaultValuePresence
import metamodel.domain.dto.MemberOrigin
import metamodel.domain.dto.NullabilityKind
import metamodel.domain.dto.PropertyFact
import metamodel.domain.dto.PropertyMutability
import metamodel.domain.dto.PropertyStorageKind
import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.dto.VisibilityKind
import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.service.TypeIdentity64Deriver
import metamodel.domain.vo.DeclarationOrdinal
import metamodel.domain.vo.TypeReference
import metamodel.port.outgoing.NormalizationEngine
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolution
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
 * - Implements the outbound raw-fact port.
 * - Contains all Kotlin/JVM reflection knowledge.
 * - Can be replaced by a future KSP adapter without changing Planning Core.
 *
 * Compiler-style role:
 * - Lowers reflected KType/KClass information into normalized raw facts.
 * - Does not perform constructor selection.
 * - Does not perform property demotion.
 * - Does not perform canonical active-member ordering.
 *
 * Determinism policy:
 * - Reflection enumeration order is never trusted as declaration order.
 * - Declaration ordinals are emitted as DeclarationOrdinal.Unavailable unless a
 *   stable source-order reconstruction layer is introduced.
 * - Local ArrayList instances are only producer buffers.
 * - RawTypeFactsDTO.issue(...) is the deterministic boundary that performs
 *   duplicate validation, deterministic sequencing, and immutable freezing
 *   through MetamodelFactSequence.
 *
 * Reflection limitations:
 * - Unnamed constructor parameters are rejected because parameter names
 *   participate in canonical active-member identity.
 * - Java/platform nullability is emitted as UNKNOWN.
 * - Delegated properties are not inferred from JVM "$delegate" naming.
 */
class ReflectionRawTypeFactsProvider private constructor(
    private val referenceFactory: ReflectionTypeReferenceFactory,
    private val normalizationGuard: ReflectionNormalizationGuard,
    private val typeIdentity64Deriver: TypeIdentity64Deriver,
) : RawTypeFactsProvider {

    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        val kType = ReflectionTypeReferenceAccess.requireKType(reference)
        val kClass = kType.jvmErasure
        val ownerTypeFqcn = renderOwnerTypeFqcn(kClass)
        val ownerHasKotlinMetadata = hasKotlinMetadata(kClass)

        val constructors = resolveConstructors(
            ownerTypeFqcn = ownerTypeFqcn,
            kClass = kClass,
            ownerHasKotlinMetadata = ownerHasKotlinMetadata,
        )

        val properties = resolveProperties(
            ownerTypeFqcn = ownerTypeFqcn,
            kClass = kClass,
            ownerHasKotlinMetadata = ownerHasKotlinMetadata,
        )

        return RawTypeFactsResolution.actualResolution(
            facts = RawTypeFactsDTO.issue(
                typeIdentity64 = typeIdentity64Deriver.deriveIdentity64(reference),
                typeIdentityAlgorithmId = typeIdentity64Deriver.identityAlgorithmId,
                typeIdentityAlgorithmVersion = typeIdentity64Deriver.identityAlgorithmVersion,
                ownerTypeFqcn = ownerTypeFqcn,
                normalizationVersion = referenceFactory.typeSignatureNormalizationVersion,
                constructors = constructors,
                properties = properties,
            ),
        )
    }

    private fun resolveConstructors(
        ownerTypeFqcn: String,
        kClass: KClass<*>,
        ownerHasKotlinMetadata: Boolean,
    ): Collection<ConstructorCandidateFact> {
        /*
         * Local producer buffer only.
         * RawTypeFactsDTO.issue(...) owns final deterministic ordering and duplicate
         * validation for constructor candidates.
         */
        val result = ArrayList<ConstructorCandidateFact>()

        val iterator = kClass.constructors.iterator()
        while (iterator.hasNext()) {
            val constructor = iterator.next()
            val valueParameters = valueParametersInDeterministicIndexOrder(constructor)

            result.add(
                ConstructorCandidateFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    constructorSignature = renderConstructorSignature(
                        ownerTypeFqcn = ownerTypeFqcn,
                        valueParameters = valueParameters,
                    ),
                    constructorSignatureNormalizationVersion = referenceFactory.typeSignatureNormalizationVersion,
                    declarationOrdinal = DeclarationOrdinal.unavailable(),
                    visibility = mapVisibility(constructor.visibility),
                    origin = MemberOrigin.DECLARED,
                    parameters = resolveConstructorParameters(
                        ownerTypeFqcn = ownerTypeFqcn,
                        valueParameters = valueParameters,
                        ownerHasKotlinMetadata = ownerHasKotlinMetadata,
                    ),
                ),
            )
        }

        return result
    }

    private fun valueParametersInDeterministicIndexOrder(
        constructor: KFunction<*>,
    ): List<KParameter> {
        /*
         * Constructor signature rendering and ConstructorParameterFact emission must
         * use the exact same VALUE-parameter ordering rule.
         *
         * We intentionally sort by KParameter.index here because constructor
         * signatures require parameter order before RawTypeFactsDTO can freeze the
         * outer constructor collection.
         */
        val valueParameters = ArrayList<KParameter>()

        val parameterIterator = constructor.parameters.iterator()
        while (parameterIterator.hasNext()) {
            val parameter = parameterIterator.next()

            if (parameter.kind == KParameter.Kind.VALUE) {
                valueParameters.add(parameter)
            }
        }

        valueParameters.sortWith(
            Comparator { left, right ->
                java.lang.Integer.compare(left.index, right.index)
            },
        )

        return valueParameters
    }

    private fun resolveConstructorParameters(
        ownerTypeFqcn: String,
        valueParameters: List<KParameter>,
        ownerHasKotlinMetadata: Boolean,
    ): Collection<ConstructorParameterFact> {
        val result = ArrayList<ConstructorParameterFact>(valueParameters.size)

        var localIndex = 0
        while (localIndex < valueParameters.size) {
            val parameter = valueParameters[localIndex]
            val parameterName = parameter.name
                ?: throw StrictModeViolationException(
                    "Reflection adapter refuses unnamed constructor parameter because parameter name " +
                            "participates in canonical active-member identity: " +
                            "ownerType=$ownerTypeFqcn, parameterIndex=$localIndex"
                )

            val parameterTypeReference = referenceFactory.create(parameter.type)

            result.add(
                ConstructorParameterFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    name = parameterName,
                    typeReference = parameterTypeReference,
                    parameterIndex = localIndex,
                    nullability = mapNullability(
                        isMarkedNullable = parameter.type.isMarkedNullable,
                        declarationHasKotlinMetadata = ownerHasKotlinMetadata,
                    ),
                    defaultValuePresence = mapDefaultPresence(parameter),
                    typeSignatureNormalizationVersion = referenceFactory.typeSignatureNormalizationVersion,
                ),
            )

            localIndex++
        }

        return result
    }

    private fun resolveProperties(
        ownerTypeFqcn: String,
        kClass: KClass<*>,
        ownerHasKotlinMetadata: Boolean,
    ): Collection<PropertyFact> {
        /*
         * Local producer buffer only.
         * RawTypeFactsDTO.issue(...) owns final deterministic ordering and duplicate
         * validation for property facts.
         */
        val result = ArrayList<PropertyFact>()

        val iterator = kClass.memberProperties.iterator()
        while (iterator.hasNext()) {
            val property = iterator.next()
            val propertyTypeReference = referenceFactory.create(property.returnType)
            val propertyOrigin = mapPropertyOrigin(
                property = property,
                ownerClass = kClass,
            )
            val propertyDeclarationHasKotlinMetadata = propertyDeclarationHasKotlinMetadata(
                property = property,
                fallbackOwnerHasKotlinMetadata = ownerHasKotlinMetadata,
            )

            result.add(
                PropertyFact.issue(
                    ownerTypeFqcn = ownerTypeFqcn,
                    name = property.name,
                    typeReference = propertyTypeReference,
                    declarationOrdinal = DeclarationOrdinal.unavailable(),
                    nullability = mapNullability(
                        isMarkedNullable = property.returnType.isMarkedNullable,
                        declarationHasKotlinMetadata = propertyDeclarationHasKotlinMetadata,
                    ),
                    declaredVisibility = mapVisibility(property.visibility),
                    setterVisibility = mapSetterVisibility(property),
                    origin = propertyOrigin,
                    mutability = mapMutability(property),
                    storageKind = inferStorageKind(property),
                    typeSignatureNormalizationVersion = referenceFactory.typeSignatureNormalizationVersion,
                ),
            )
        }

        return result
    }

    private fun renderConstructorSignature(
        ownerTypeFqcn: String,
        valueParameters: List<KParameter>,
    ): String {
        val builder = StringBuilder()
        builder.append(ownerTypeFqcn)
        builder.append('(')

        var i = 0
        while (i < valueParameters.size) {
            if (i > 0) {
                builder.append(',')
            }

            val reference = referenceFactory.create(valueParameters[i].type)
            builder.append(reference.signature)
            i++
        }

        builder.append(')')

        val signature = builder.toString()

        normalizationGuard.requireNormalizedComponent(
            field = "ConstructorCandidateFact.constructorSignature",
            value = signature,
        )

        return signature
    }

    private fun renderOwnerTypeFqcn(
        kClass: KClass<*>,
    ): String {
        /*
         * This is JVM spelling canonicalization only.
         * Unicode normalization is not performed here. The NormalizationGuard
         * enforces the NFC-REJECT boundary.
         */
        val rawName = kClass.qualifiedName ?: kClass.java.name
        val ownerTypeFqcn = rawName.replace('$', '.')

        normalizationGuard.requireNormalizedComponent(
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
        property: KProperty1<*, *>,
    ): VisibilityKind? {
        val mutable = property as? KMutableProperty1<*, *>
            ?: return null

        return mapVisibility(mutable.setter.visibility)
    }

    private fun mapMutability(
        property: KProperty1<*, *>,
    ): PropertyMutability {
        return if (property is KMutableProperty1<*, *>) {
            PropertyMutability.MUTABLE
        } else {
            PropertyMutability.READ_ONLY
        }
    }

    private fun inferStorageKind(
        property: KProperty1<*, *>,
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
         * metamodel fact protocol.
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
        property: KProperty1<*, *>,
        ownerClass: KClass<*>,
    ): MemberOrigin {
        if (isDeclaredProperty(
                ownerClass = ownerClass,
                property = property,
            )
        ) {
            return MemberOrigin.DECLARED
        }

        val declaringClass = property.javaField?.declaringClass
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

    private fun isDeclaredProperty(
        ownerClass: KClass<*>,
        property: KProperty1<*, *>,
    ): Boolean {
        /*
         * Do not create a HashSet/LinkedHashSet just for membership.
         * We only ask a deterministic yes/no question.
         * The iteration order of declaredMemberProperties is not used as semantic order.
         */
        val iterator = ownerClass.declaredMemberProperties.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == property.name) {
                return true
            }
        }

        return false
    }

    private fun propertyDeclarationHasKotlinMetadata(
        property: KProperty1<*, *>,
        fallbackOwnerHasKotlinMetadata: Boolean,
    ): Boolean {
        val declaringClass = property.javaField?.declaringClass
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
            referenceFactory: ReflectionTypeReferenceFactory,
            normalizationEngine: NormalizationEngine,
            typeIdentity64Deriver: TypeIdentity64Deriver,
        ): ReflectionRawTypeFactsProvider {
            return ReflectionRawTypeFactsProvider(
                referenceFactory = referenceFactory,
                normalizationGuard = ReflectionNormalizationGuard.issue(normalizationEngine),
                typeIdentity64Deriver = typeIdentity64Deriver,
            )
        }
    }
}