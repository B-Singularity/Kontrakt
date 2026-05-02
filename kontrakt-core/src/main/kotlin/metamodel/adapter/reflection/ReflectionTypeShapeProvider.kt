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
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

/**
 * Reflection implementation of TypeShapeProvider.
 *
 * Hexagonal role:
 *
 * - contains Kotlin/JVM reflection classification logic;
 * - can be replaced by KSP without Planning Core changes;
 * - does not leak reflection handles into TypeReference.
 *
 * Compiler-style role:
 *
 * - lowers a domain-issued TypeReference into a coarse expansion shape;
 * - does not inspect constructors/properties;
 * - does not perform active-member projection;
 * - does not create plan nodes;
 * - does not decide implementation selection.
 *
 * TypeReference law:
 *
 * TypeReference is a final domain-issued VO. Reflection adapters must not
 * subclass it or smuggle KType handles into it.
 *
 * Reflection KType recovery is performed through ReflectionTypeHandleRegistry,
 * which is adapter-local sidecar state.
 *
 * Determinism policy:
 *
 * - generic child references are accepted only when explicitly present;
 * - star projections are rejected;
 * - use-site variance is rejected until variance is modeled as a separate
 *   metamodel identity axis;
 * - unsupported erased container shapes fail closed;
 * - root/platform nullability is conservative:
 *     nullable -> NULLABLE;
 *     non-null with Kotlin metadata -> NON_NULL;
 *     non-null without Kotlin metadata -> UNKNOWN.
 *
 * Classification policy:
 *
 * Atomic classification is intentionally a closed reflection-adapter table.
 *
 * This provider does not currently support user-defined atomic leaf policies.
 * If that becomes necessary, introduce an explicit
 * ReflectionTypeShapeClassificationPolicy rather than letting arbitrary
 * reflection heuristics leak into the core.
 *
 * Abstract-class policy:
 *
 * Only true JVM interfaces are INTERFACE shapes.
 *
 * Abstract classes remain COMPOSITE here because they can still have constructor
 * and property structure. Downstream materialization/implementation-selection
 * phases must prevent direct instantiation of abstract classes.
 *
 * Performance policy:
 *
 * The built-in atomic whitelist is stored as a closed Set for direct membership
 * checks instead of a long chain of equality comparisons.
 *
 * Primitive-array component KType synthesis is still performed through
 * createType(...) in this version. Static KType caching is intentionally deferred
 * to the later caching/flyweight allocation phase.
 */
class ReflectionTypeShapeProvider private constructor(
    private val referenceFactory: ReflectionTypeReferenceFactory,
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        val kType = typeHandleRegistry.requireKType(reference)
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
         * dispatch law must not rely on that implementation accident.
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
         * Abstract classes remain COMPOSITE here. They may still have structural
         * facts worth projecting, but later implementation-selection and
         * materialization boundaries must prevent direct instantiation.
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
        val elementArgument =
            requireTypeArgument(
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
        val keyArgument =
            requireTypeArgument(
                ownerType = type,
                expectedArity = 2,
                argumentIndex = 0,
                shapeKind = "MAP",
            )

        val valueArgument =
            requireTypeArgument(
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
        val componentType =
            if (type.arguments.isNotEmpty()) {
                requireTypeArgument(
                    ownerType = type,
                    expectedArity = 1,
                    argumentIndex = 0,
                    shapeKind = "ARRAY",
                )
            } else {
                primitiveOrJvmArrayComponentType(
                    ownerType = type,
                    arrayClass = arrayClass,
                )
            }

        return ResolvedTypeShape.array(
            subject = subject,
            nullability = nullability,
            componentType = referenceFactory.create(componentType),
        )
    }

    private fun primitiveOrJvmArrayComponentType(
        ownerType: KType,
        arrayClass: KClass<*>,
    ): KType {
        val componentJavaClass =
            arrayClass.java.componentType
                ?: throw StrictModeViolationException(
                    "ARRAY shape has no generic argument and no JVM component type: type=$ownerType",
                )

        /*
         * This createType(...) call is known to be relatively expensive.
         *
         * Do not add a local static KType cache in this pass. Primitive/component
         * KType caching belongs to the later allocation-policy phase together
         * with canonical type interning.
         */
        return try {
            componentJavaClass.kotlin.createType(nullable = false)
        } catch (t: Throwable) {
            throw StrictModeViolationException(
                "Failed to construct array component KType: " +
                        "arrayType=$ownerType, componentClass=${componentJavaClass.name}, " +
                        "cause=${t::class.qualifiedName}",
            )
        }
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
                        "ownerType=$ownerType, actualArity=${ownerType.arguments.size}",
            )
        }

        val projection: KTypeProjection = ownerType.arguments[argumentIndex]

        val argumentType =
            projection.type
                ?: throw StrictModeViolationException(
                    "$shapeKind shape rejects star projection: " +
                            "ownerType=$ownerType, argumentIndex=$argumentIndex",
                )

        when (projection.variance) {
            KVariance.IN,
            KVariance.OUT -> {
                throw StrictModeViolationException(
                    "$shapeKind shape rejects use-site variance because variance " +
                            "is not yet represented as a separate metamodel identity axis: " +
                            "ownerType=$ownerType, argumentIndex=$argumentIndex, variance=${projection.variance}",
                )
            }

            KVariance.INVARIANT,
            null -> {
                // Accepted. Null variance with a non-null type is treated as
                // invariant for defensive reflection compatibility.
            }
        }

        return argumentType
    }

    private fun isAtomic(
        kClass: KClass<*>,
    ): Boolean {
        val javaClass = kClass.java

        if (javaClass.isPrimitive || javaClass.isEnum) {
            return true
        }

        return kClass in ATOMIC_KOTLIN_CLASSES
    }

    private fun isMap(
        kClass: KClass<*>,
    ): Boolean {
        return MAP_JAVA_CLASS.isAssignableFrom(kClass.java)
    }

    private fun isCollection(
        kClass: KClass<*>,
    ): Boolean {
        return ITERABLE_JAVA_CLASS.isAssignableFrom(kClass.java)
    }

    private fun isArray(
        kClass: KClass<*>,
    ): Boolean {
        val javaClass = kClass.java

        if (javaClass.isArray) {
            return true
        }

        return kClass in PRIMITIVE_ARRAY_KOTLIN_CLASSES
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
             * For Java/platform root types, not-marked-null is not strong enough
             * to claim NON_NULL. Use UNKNOWN so shape consumers remain
             * conservative.
             */
            NullabilityKind.UNKNOWN
        }
    }

    companion object {
        private val MAP_JAVA_CLASS: Class<*> = Map::class.java
        private val ITERABLE_JAVA_CLASS: Class<*> = Iterable::class.java

        /**
         * Closed built-in atomic type table.
         *
         * This is not a runtime cache. It is the adapter's fixed built-in
         * classification vocabulary.
         */
        private val ATOMIC_KOTLIN_CLASSES: Set<KClass<*>> =
            setOf(
                String::class,
                Boolean::class,
                Char::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
                Float::class,
                Double::class,
                BigDecimal::class,
                BigInteger::class,
                UUID::class,
                Instant::class,
                LocalDate::class,
                LocalTime::class,
                LocalDateTime::class,
                OffsetTime::class,
                OffsetDateTime::class,
                ZonedDateTime::class,
                Duration::class,
                Period::class,
            )

        /**
         * Defensive companion table for Kotlin primitive-array KClass values.
         *
         * javaClass.isArray should already catch these on the JVM. This table
         * keeps the intent explicit and protects against reflection edge cases.
         *
         * This is not an interning table.
         */
        private val PRIMITIVE_ARRAY_KOTLIN_CLASSES: Set<KClass<*>> =
            setOf(
                BooleanArray::class,
                ByteArray::class,
                CharArray::class,
                ShortArray::class,
                IntArray::class,
                LongArray::class,
                FloatArray::class,
                DoubleArray::class,
            )

        @JvmStatic
        fun issue(
            referenceFactory: ReflectionTypeReferenceFactory,
            typeHandleRegistry: ReflectionTypeHandleRegistry,
        ): ReflectionTypeShapeProvider {
            return ReflectionTypeShapeProvider(
                referenceFactory = referenceFactory,
                typeHandleRegistry = typeHandleRegistry,
            )
        }
    }
}