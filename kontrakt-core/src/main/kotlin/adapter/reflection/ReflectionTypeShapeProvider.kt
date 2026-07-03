package adapter.reflection

import stage.admission.diagnostics.evidence.StrictModeViolationException
import stage.canonicalization.material.representation.TypeReference
import stage.input.presentation.raw.NullabilityKind
import stage.input.presentation.raw.ResolvedTypeShape
import stage.lowering.boundary.TypeShapeProvider
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
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * Reflection implementation of [TypeShapeProvider].
 *
 * Hexagonal role:
 *
 * - contains Kotlin/JVM reflection classification logic;
 * - can be replaced by KSP / bytecode / source-backed adapters without
 *   Planning Core changes;
 * - does not leak reflection handles into TypeReference or planning DTOs.
 *
 * Compiler-style role:
 *
 * - lowers a domain-issued [TypeReference] into one immediate coarse expansion
 *   shape;
 * - does not inspect constructors/properties;
 * - does not perform active-member projection;
 * - does not perform active-member ordering;
 * - does not create plan nodes;
 * - does not decide implementation selection;
 * - does not recursively resolve child shapes.
 *
 * TypeReference law:
 *
 * [TypeReference] is a final domain-issued VO. Reflection adapters must not
 * subclass it, synthesize it directly, or smuggle KType handles into it.
 *
 * Reflection KType recovery is performed through [ReflectionTypeHandleRegistry],
 * which is adapter-local sidecar state.
 *
 * Issuance boundary:
 *
 * This provider may need child TypeReference values for collection, map, and
 * array shapes. It obtains them through [ReflectionTypeReferenceBridge].
 *
 * The bridge delegates to the metamodel-domain [realization.identity.CanonicalTypeReferenceIssuer].
 * Therefore this provider remains a shape-classification adapter, not a
 * TypeReference issuance authority.
 *
 * Determinism policy:
 *
 * - generic child references are accepted only when explicitly present;
 * - star projections are rejected;
 * - use-site variance is rejected until variance is modeled as a separate
 *   metamodel identity axis;
 * - unsupported erased container shapes fail closed;
 * - non-primitive erased JVM array components are not reconstructed through
 *   Kotlin reflection;
 * - root/platform nullability is conservative:
 *
 * ```text
 * nullable                   -> NULLABLE
 * non-null + Kotlin metadata -> NON_NULL
 * non-null + no metadata     -> UNKNOWN
 * ```
 *
 * Classification policy:
 *
 * Atomic classification is intentionally a closed reflection-adapter table.
 * This table is not a cache and not an interner. It is the adapter's fixed
 * built-in vocabulary for leaf-like scalar shapes.
 *
 * If user-defined atomic leaf policies become necessary, introduce an explicit
 * ReflectionTypeShapeClassificationPolicy rather than letting arbitrary
 * reflection heuristics leak into the core.
 *
 * Abstract-class policy:
 *
 * Only true JVM interfaces are INTERFACE shapes in this provider.
 *
 * Abstract classes remain COMPOSITE here because they can still carry
 * constructor/property structure. Downstream materialization and polymorphic
 * implementation-selection phases must prevent direct instantiation when the
 * type is not materializable.
 *
 * Current-cut performance policy:
 *
 * - Keep the original closed classification tables.
 * - Do not introduce caching/interner/pooling here.
 * - Avoid repeated jvmErasure / Metadata lookup inside one provider call.
 * - Remove createType(...) from primitive-array component recovery.
 */
class ReflectionTypeShapeProvider private constructor(
    private val typeReferenceBridge: ReflectionTypeReferenceBridge,
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        val kType = typeHandleRegistry.requireKType(reference)
        val kClass = kType.jvmErasure

        /*
         * Observe class metadata once for this provider call.
         *
         * This is not a cache. It only avoids repeating the same reflection
         * lookup inside a single resolveTypeShape invocation.
         */
        val javaClass = kClass.java
        val hasKotlinMetadata =
            javaClass.getAnnotation(Metadata::class.java) != null

        val nullability =
            mapShapeNullability(
                type = kType,
                hasKotlinMetadata = hasKotlinMetadata,
            )

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
            elementType = typeReferenceBridge.issueReference(elementArgument),
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
            keyType = typeReferenceBridge.issueReference(keyArgument),
            valueType = typeReferenceBridge.issueReference(valueArgument),
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
            componentType = typeReferenceBridge.issueReference(componentType),
        )
    }

    /**
     * Recover component KType only for deterministic primitive-array cases.
     *
     * Kotlin Array<T> should normally expose T as a KType argument and therefore
     * not reach this branch.
     *
     * Primitive arrays such as IntArray do not expose Array<T>-style component
     * arguments. Those are handled through typeOf<T>(), which is stable and does
     * not synthesize a new arbitrary KType through reflection.
     *
     * Non-primitive erased JVM arrays are rejected. Reconstructing their
     * component as KType via createType(...) would recover type information from
     * erased Class material and can become environment/classloader dependent.
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun primitiveOrJvmArrayComponentType(
        ownerType: KType,
        arrayClass: KClass<*>,
    ): KType {
        val componentJavaClass =
            arrayClass.java.componentType
                ?: throw StrictModeViolationException(
                    "ARRAY shape has no generic argument and no JVM component type: " +
                            "type=$ownerType",
                )

        if (!componentJavaClass.isPrimitive) {
            throw StrictModeViolationException(
                "Non-primitive JVM array component type is erased and cannot be " +
                        "deterministically reconstructed as KType without explicit " +
                        "Kotlin type argument material: " +
                        "ownerType=$ownerType, " +
                        "arrayClass=${arrayClass.qualifiedName ?: arrayClass.java.name}, " +
                        "componentClass=${componentJavaClass.name}",
            )
        }

        return when (componentJavaClass) {
            java.lang.Boolean.TYPE -> typeOf<Boolean>()
            java.lang.Byte.TYPE -> typeOf<Byte>()
            Character.TYPE -> typeOf<Char>()
            java.lang.Short.TYPE -> typeOf<Short>()
            Integer.TYPE -> typeOf<Int>()
            java.lang.Long.TYPE -> typeOf<Long>()
            java.lang.Float.TYPE -> typeOf<Float>()
            java.lang.Double.TYPE -> typeOf<Double>()

            else -> {
                throw StrictModeViolationException(
                    "Unsupported primitive array component class: " +
                            "ownerType=$ownerType, " +
                            "arrayClass=${arrayClass.qualifiedName ?: arrayClass.java.name}, " +
                            "componentClass=${componentJavaClass.name}",
                )
            }
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
            KVariance.OUT,
                -> {
                throw StrictModeViolationException(
                    "$shapeKind shape rejects use-site variance because variance " +
                            "is not yet represented as a separate metamodel identity axis: " +
                            "ownerType=$ownerType, argumentIndex=$argumentIndex, " +
                            "variance=${projection.variance}",
                )
            }

            KVariance.INVARIANT,
            null,
                -> {
                /*
                 * Accepted. Null variance with a non-null type is treated as
                 * invariant for defensive reflection compatibility.
                 */
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
        hasKotlinMetadata: Boolean,
    ): NullabilityKind {
        if (type.isMarkedNullable) {
            return NullabilityKind.NULLABLE
        }

        return if (hasKotlinMetadata) {
            NullabilityKind.NON_NULL
        } else {
            /*
             * For Java/platform root types, not-marked-null is not strong
             * enough to claim NON_NULL. Use UNKNOWN so shape consumers remain
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
         * This is not a runtime cache.
         * This is not a TypeReference interner.
         * This is not a policy memoization surface.
         *
         * It is the reflection adapter's fixed built-in classification
         * vocabulary for known scalar/leaf-like Kotlin/JVM types.
         *
         * Keep this table explicit until a separate
         * ReflectionTypeShapeClassificationPolicy is introduced.
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
         * keeps the reflection-adapter intent explicit and protects against
         * Kotlin/JVM reflection edge cases.
         *
         * This is not an interning table.
         * This is not a cache.
         * This is not a primitive slab.
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
            typeReferenceBridge: ReflectionTypeReferenceBridge,
            typeHandleRegistry: ReflectionTypeHandleRegistry,
        ): ReflectionTypeShapeProvider {
            return ReflectionTypeShapeProvider(
                typeReferenceBridge = typeReferenceBridge,
                typeHandleRegistry = typeHandleRegistry,
            )
        }
    }
}