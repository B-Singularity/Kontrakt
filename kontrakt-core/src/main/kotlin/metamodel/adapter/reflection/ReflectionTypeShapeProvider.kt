package metamodel.adapter.reflection

import metamodel.domain.dto.NullabilityKind
import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.vo.TypeReference
import planning.domain.port.outgoing.TypeShapeProvider
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
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Reflection implementation of TypeShapeProvider.
 *
 * Hexagonal role:
 * - contains Kotlin/JVM reflection classification logic
 * - can be replaced by KSP without Planning Core changes
 *
 * Compiler-style role:
 * - lowers a TypeReference into a coarse expansion shape
 * - does not inspect constructors/properties
 * - does not perform active-member projection
 * - does not create plan nodes
 *
 * Determinism policy:
 * - generic child references are accepted only when explicitly present
 * - star projections are rejected
 * - unsupported erased container shapes fail closed
 * - root/platform nullability is conservative: nullable -> NULLABLE, otherwise
 *   NON_NULL only when the declaring root has Kotlin metadata, else UNKNOWN
 */
class ReflectionTypeShapeProvider private constructor(
    private val referenceFactory: ReflectionTypeReferenceFactory,
) : TypeShapeProvider {

    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        val kType = ReflectionTypeReferenceAccess.requireKType(reference)
        val kClass = kType.jvmErasure
        val nullability = mapShapeNullability(kType)

        if (isAtomic(kClass)) {
            return ResolvedTypeShape.atomic(
                subject = reference,
                nullability = nullability,
            )
        }

        /*
         * Check MAP before COLLECTION.
         *
         * Current JVM collections do not make Map implement Iterable, but the
         * dispatch law should not rely on that accident.
         */
        if (isMap(kClass)) {
            return resolveMapShape(
                subject = reference,
                type = kType,
                nullability = nullability,
            )
        }

        if (isCollection(kClass)) {
            return resolveCollectionShape(
                subject = reference,
                type = kType,
                nullability = nullability,
            )
        }

        if (isArray(kClass)) {
            return resolveArrayShape(
                subject = reference,
                type = kType,
                arrayClass = kClass,
                nullability = nullability,
            )
        }

        /*
         * Only true JVM interfaces are INTERFACE shapes.
         *
         * Abstract classes remain COMPOSITE for now because they may still have
         * constructor and property structure that should go through the normal
         * active-member pipeline.
         */
        if (isInterface(kClass)) {
            return ResolvedTypeShape.interfaceShape(
                subject = reference,
                nullability = nullability,
            )
        }

        return ResolvedTypeShape.composite(
            subject = reference,
            nullability = nullability,
        )
    }

    private fun resolveCollectionShape(
        subject: TypeReference,
        type: KType,
        nullability: NullabilityKind,
    ): ResolvedTypeShape {
        val elementArgument = requireTypeArgument(
            ownerType = type,
            expectedArity = 1,
            argumentIndex = 0,
            shapeKind = "COLLECTION",
        )

        return ResolvedTypeShape.collection(
            subject = subject,
            nullability = nullability,
            elementType = referenceFactory.create(elementArgument),
        )
    }

    private fun resolveMapShape(
        subject: TypeReference,
        type: KType,
        nullability: NullabilityKind,
    ): ResolvedTypeShape {
        val keyArgument = requireTypeArgument(
            ownerType = type,
            expectedArity = 2,
            argumentIndex = 0,
            shapeKind = "MAP",
        )

        val valueArgument = requireTypeArgument(
            ownerType = type,
            expectedArity = 2,
            argumentIndex = 1,
            shapeKind = "MAP",
        )

        return ResolvedTypeShape.map(
            subject = subject,
            nullability = nullability,
            keyType = referenceFactory.create(keyArgument),
            valueType = referenceFactory.create(valueArgument),
        )
    }

    private fun resolveArrayShape(
        subject: TypeReference,
        type: KType,
        arrayClass: KClass<*>,
        nullability: NullabilityKind,
    ): ResolvedTypeShape {
        val componentType = if (type.arguments.isNotEmpty()) {
            requireTypeArgument(
                ownerType = type,
                expectedArity = 1,
                argumentIndex = 0,
                shapeKind = "ARRAY",
            )
        } else {
            val componentJavaClass = arrayClass.java.componentType
                ?: throw StrictModeViolationException(
                    "ARRAY shape has no generic argument and no JVM component type: type=$type"
                )

            try {
                componentJavaClass.kotlin.createType(nullable = false)
            } catch (t: Throwable) {
                throw StrictModeViolationException(
                    "Failed to construct primitive-array component KType: " +
                            "arrayType=$type, componentClass=${componentJavaClass.name}, cause=${t::class.qualifiedName}"
                )
            }
        }

        return ResolvedTypeShape.array(
            subject = subject,
            nullability = nullability,
            componentType = referenceFactory.create(componentType),
        )
    }

    private fun requireTypeArgument(
        ownerType: KType,
        expectedArity: Int,
        argumentIndex: Int,
        shapeKind: String,
    ): KType {
        if (ownerType.arguments.size != expectedArity) {
            throw StrictModeViolationException(
                "$shapeKind shape requires exactly $expectedArity type argument(s): " +
                        "ownerType=$ownerType, actualArity=${ownerType.arguments.size}"
            )
        }

        val projection: KTypeProjection = ownerType.arguments[argumentIndex]

        return projection.type
            ?: throw StrictModeViolationException(
                "$shapeKind shape rejects star projection: ownerType=$ownerType, argumentIndex=$argumentIndex"
            )
    }

    private fun isAtomic(
        kClass: KClass<*>,
    ): Boolean {
        val javaClass = kClass.java

        if (javaClass.isPrimitive || javaClass.isEnum) {
            return true
        }

        return kClass == String::class ||
                kClass == Boolean::class ||
                kClass == Char::class ||
                kClass == Byte::class ||
                kClass == Short::class ||
                kClass == Int::class ||
                kClass == Long::class ||
                kClass == Float::class ||
                kClass == Double::class ||
                kClass == BigDecimal::class ||
                kClass == BigInteger::class ||
                kClass == UUID::class ||
                kClass == Instant::class ||
                kClass == LocalDate::class ||
                kClass == LocalTime::class ||
                kClass == LocalDateTime::class ||
                kClass == OffsetTime::class ||
                kClass == OffsetDateTime::class ||
                kClass == ZonedDateTime::class ||
                kClass == Duration::class ||
                kClass == Period::class
    }

    private fun isMap(
        kClass: KClass<*>,
    ): Boolean {
        return Map::class.java.isAssignableFrom(kClass.java)
    }

    private fun isCollection(
        kClass: KClass<*>,
    ): Boolean {
        return Iterable::class.java.isAssignableFrom(kClass.java)
    }

    private fun isArray(
        kClass: KClass<*>,
    ): Boolean {
        return kClass.java.isArray ||
                kClass == BooleanArray::class ||
                kClass == ByteArray::class ||
                kClass == CharArray::class ||
                kClass == ShortArray::class ||
                kClass == IntArray::class ||
                kClass == LongArray::class ||
                kClass == FloatArray::class ||
                kClass == DoubleArray::class
    }

    private fun isInterface(
        kClass: KClass<*>,
    ): Boolean {
        return kClass.java.isInterface
    }

    private fun mapShapeNullability(
        type: KType,
    ): NullabilityKind {
        if (type.isMarkedNullable) {
            return NullabilityKind.NULLABLE
        }

        val rootClass = type.jvmErasure.java
        val hasKotlinMetadata = rootClass.getAnnotation(Metadata::class.java) != null

        return if (hasKotlinMetadata) {
            NullabilityKind.NON_NULL
        } else {
            /*
             * For Java/platform root types, not-marked-null is not strong enough to
             * claim NON_NULL. Use UNKNOWN so shape consumers remain conservative.
             */
            NullabilityKind.UNKNOWN
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            referenceFactory: ReflectionTypeReferenceFactory,
        ): ReflectionTypeShapeProvider {
            return ReflectionTypeShapeProvider(
                referenceFactory = referenceFactory,
            )
        }
    }
}