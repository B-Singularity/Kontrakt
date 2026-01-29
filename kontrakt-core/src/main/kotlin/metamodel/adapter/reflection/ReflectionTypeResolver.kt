package metamodel.adapter.reflection

import metamodel.domain.exception.MalformedTypeException
import metamodel.domain.exception.ResolverSessionClosedException
import metamodel.domain.exception.UnsupportedSourceException
import metamodel.domain.model.ConstructorDescriptor
import metamodel.domain.model.MethodDescriptor
import metamodel.domain.model.ParameterDescriptor
import metamodel.domain.model.PropertyDescriptor
import metamodel.domain.model.TypeDescriptor
import metamodel.domain.port.outgoing.TypeResolver
import metamodel.domain.vo.TypeId
import metamodel.domain.vo.TypeReference
import metamodel.domain.vo.TypeSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVariance
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

/**
 * [Compiler Frontend] The Definitive Implementation of TypeResolver.
 *
 * ## Architecture Compliance
 * 1. **Lifecycle Enforcement:** Implements [AutoCloseable].
 * 2. **Semantic Decoupling:** Uses [TypeTaxonomy] & [TypeIdStrategy].
 * 3. **Centralized Reference Factory:** Uses [TypeReferenceFactory] to enforce global ID consistency.
 * 4. **Concrete IR Nodes:** Debuggable and stable IR implementation.
 */
class ReflectionTypeResolver(
    private val taxonomy: TypeTaxonomy = DefaultTypeTaxonomy,
    private val typeIdStrategy: TypeIdStrategy = DefaultTypeIdStrategy
) : TypeResolver, AutoCloseable {

    // [Symbol Table]
    private val cache = ConcurrentHashMap<TypeId, TypeDescriptor>()
    private val closed = AtomicBoolean(false)

    // [Internal Components]
    private val annotationResolver = ReflectionAnnotationResolver()

    // [Centralized Factory] Binds the ID Strategy to Reference Creation logic.
    private val referenceFactory = TypeReferenceFactory { kType ->
        TypeReference.Runtime(
            source = KTypeSource(kType),
            typeId = typeIdStrategy.generate(kType)
        )
    }

    // [Sub-Resolver] Injected with the factory
    private val memberResolver = ReflectionMemberResolver(this, referenceFactory, annotationResolver)

    override fun resolve(reference: TypeReference): TypeDescriptor {
        ensureOpen()

        cache[reference.typeId]?.let { return it }

        val kType = (reference.source as? KTypeSource)?.kType
            ?: throw UnsupportedSourceException(reference.source)

        return cache.computeIfAbsent(reference.typeId) { _ ->
            parseType(kType, reference.typeId)
        }
    }

    private fun parseType(kType: KType, typeId: TypeId): TypeDescriptor {
        val kClass = kType.jvmErasure

        return when {
            taxonomy.isValue(kClass) -> ValueDescriptorImpl(kType, typeId, annotationResolver)
            kClass.java.isArray -> createArrayDescriptor(kType, typeId)
            kClass.isSubclassOf(Map::class) -> createMapDescriptor(kType, typeId)
            kClass.isSubclassOf(Iterable::class) -> createContainerDescriptor(kType, typeId)
            else -> StructuralDescriptorImpl(kType, typeId, this, memberResolver, referenceFactory, annotationResolver)
        }
    }

    private fun createContainerDescriptor(kType: KType, typeId: TypeId): TypeDescriptor {
        if (kType.arguments.isEmpty()) throw MalformedTypeException(typeId, "Container must have a generic argument.")
        return ContainerDescriptorImpl(kType, typeId, this, referenceFactory, annotationResolver)
    }

    private fun createMapDescriptor(kType: KType, typeId: TypeId): TypeDescriptor {
        if (kType.arguments.size < 2) throw MalformedTypeException(
            typeId,
            "Map must have Key and Value type arguments."
        )
        return MapDescriptorImpl(kType, typeId, this, referenceFactory, annotationResolver)
    }

    private fun createArrayDescriptor(kType: KType, typeId: TypeId): TypeDescriptor {
        val kClass = kType.jvmErasure
        return if (kClass.java.componentType.isPrimitive) {
            PrimitiveArrayDescriptorImpl(kType, typeId, this, referenceFactory, annotationResolver)
        } else {
            if (kType.arguments.isEmpty()) throw MalformedTypeException(
                typeId,
                "Object Array must have a component type argument."
            )
            // Object Arrays do not use annotations in this adapter implementation
            ObjectArrayDescriptorImpl(kType, typeId, this, referenceFactory)
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            cache.clear()
        }
    }

    private fun ensureOpen() {
        if (closed.get()) throw ResolverSessionClosedException()
    }

    // ========================================================================
    //  Concrete IR Nodes
    // ========================================================================

    private data class ValueDescriptorImpl(
        @Transient private val kType: KType,
        override val typeId: TypeId,
        private val annotations: ReflectionAnnotationResolver
    ) : TypeDescriptor.Value {
        private val kClass = kType.jvmErasure
        override val simpleName: String = kClass.simpleName ?: "Unknown"
        override val qualifiedName: String = kClass.qualifiedName ?: "Unknown"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = kClass.isValue

        override fun hasAnnotation(fullyQualifiedClassName: String) =
            annotations.hasAnnotation(kClass, fullyQualifiedClassName)

        override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
            annotations.getAttributes(kClass, fullyQualifiedClassName)

        override fun toString(): String = "Value($simpleName)"
    }

    private class ContainerDescriptorImpl(
        kType: KType,
        override val typeId: TypeId,
        private val resolver: TypeResolver,
        private val refs: TypeReferenceFactory,
        private val annotations: ReflectionAnnotationResolver
    ) : TypeDescriptor.Container {
        private val kClass = kType.jvmErasure
        override val simpleName: String = kClass.simpleName ?: "List"
        override val qualifiedName: String = kClass.qualifiedName ?: "kotlin.collections.List"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = kClass.isValue

        override val elementType: TypeDescriptor by lazy {
            val arg = kType.arguments.first().type!!
            resolver.resolve(refs.create(arg))
        }

        override fun hasAnnotation(fullyQualifiedClassName: String) =
            annotations.hasAnnotation(kClass, fullyQualifiedClassName)

        override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
            annotations.getAttributes(kClass, fullyQualifiedClassName)

        override fun toString(): String = "Container($simpleName)"
    }

    private class MapDescriptorImpl(
        kType: KType,
        override val typeId: TypeId,
        private val resolver: TypeResolver,
        private val refs: TypeReferenceFactory,
        private val annotations: ReflectionAnnotationResolver
    ) : TypeDescriptor.Map {
        private val kClass = kType.jvmErasure
        override val simpleName: String = kClass.simpleName ?: "Map"
        override val qualifiedName: String = kClass.qualifiedName ?: "kotlin.collections.Map"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = kClass.isValue

        override val keyType: TypeDescriptor by lazy { resolver.resolve(refs.create(kType.arguments[0].type!!)) }
        override val valueType: TypeDescriptor by lazy { resolver.resolve(refs.create(kType.arguments[1].type!!)) }

        override fun hasAnnotation(fullyQualifiedClassName: String) =
            annotations.hasAnnotation(kClass, fullyQualifiedClassName)

        override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
            annotations.getAttributes(kClass, fullyQualifiedClassName)

        override fun toString(): String = "Map($simpleName)"
    }

    private class ObjectArrayDescriptorImpl(
        kType: KType,
        override val typeId: TypeId,
        private val resolver: TypeResolver,
        private val refs: TypeReferenceFactory
        // Removed unused 'annotations' parameter
    ) : TypeDescriptor.Array {
        override val simpleName: String = "Array"
        override val qualifiedName: String = "kotlin.Array"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = false

        override val componentType: TypeDescriptor by lazy {
            val arg = kType.arguments.first().type!!
            resolver.resolve(refs.create(arg))
        }

        override fun hasAnnotation(fullyQualifiedClassName: String) = false
        override fun getAnnotationAttributes(fullyQualifiedClassName: String) = null
        override fun toString(): String = "Array<${componentType.simpleName}>"
    }

    private class PrimitiveArrayDescriptorImpl(
        kType: KType,
        override val typeId: TypeId,
        private val resolver: TypeResolver,
        private val refs: TypeReferenceFactory,
        private val annotations: ReflectionAnnotationResolver
    ) : TypeDescriptor.Array {
        private val kClass = kType.jvmErasure
        override val simpleName: String = kClass.simpleName ?: "Array"
        override val qualifiedName: String = kClass.qualifiedName ?: "Unknown"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = false

        override val componentType: TypeDescriptor by lazy {
            val componentKClass = kClass.java.componentType.kotlin
            resolver.resolve(refs.create(componentKClass.starProjectedType))
        }

        override fun hasAnnotation(fullyQualifiedClassName: String) =
            annotations.hasAnnotation(kClass, fullyQualifiedClassName)

        override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
            annotations.getAttributes(kClass, fullyQualifiedClassName)

        override fun toString(): String = "PrimitiveArray($simpleName)"
    }

    private class StructuralDescriptorImpl(
        kType: KType,
        override val typeId: TypeId,
        private val resolver: TypeResolver,
        private val members: ReflectionMemberResolver,
        private val refs: TypeReferenceFactory,
        private val annotations: ReflectionAnnotationResolver
    ) : TypeDescriptor.Structural {
        private val kClass = kType.jvmErasure
        override val simpleName: String = kClass.simpleName ?: "Unknown"
        override val qualifiedName: String = kClass.qualifiedName ?: "Unknown"
        override val isNullable: Boolean = kType.isMarkedNullable
        override val isInline: Boolean = kClass.isValue

        override val typeArguments: List<TypeDescriptor> by lazy {
            kType.arguments.mapNotNull { it.type }.map { resolver.resolve(refs.create(it)) }
        }

        override val properties: List<PropertyDescriptor> by lazy { members.resolveProperties(kClass) }
        override val constructors: List<ConstructorDescriptor> by lazy { members.resolveConstructors(kClass) }
        override val methods: List<MethodDescriptor> = emptyList()

        override fun hasAnnotation(fullyQualifiedClassName: String) =
            annotations.hasAnnotation(kClass, fullyQualifiedClassName)

        override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
            annotations.getAttributes(kClass, fullyQualifiedClassName)

        override fun toString(): String = "Structural($simpleName)"
    }
}

// ========================================================================
//  Separated Concerns: Policies & Strategies
// ========================================================================

fun interface TypeIdStrategy {
    fun generate(kType: KType): TypeId
}

object DefaultTypeIdStrategy : TypeIdStrategy {
    override fun generate(kType: KType): TypeId {
        val sb = StringBuilder()
        buildString(kType, sb)
        return TypeId(sb.toString())
    }

    private fun buildString(kType: KType, sb: StringBuilder) {
        val kClass = kType.jvmErasure
        sb.append(kClass.qualifiedName ?: kClass.java.name)
        if (kType.arguments.isNotEmpty()) {
            sb.append("<")
            kType.arguments.forEachIndexed { index, arg ->
                if (index > 0) sb.append(", ")
                when (arg.variance) {
                    KVariance.IN -> sb.append("in ")
                    KVariance.OUT -> sb.append("out ")
                    KVariance.INVARIANT -> {} // Explicitly ignored
                    null -> {} // Explicitly ignored
                }
                if (arg.type == null) sb.append("*") else buildString(arg.type!!, sb)
            }
            sb.append(">")
        }
        if (kType.isMarkedNullable) sb.append("?")
    }
}

fun interface TypeTaxonomy {
    fun isValue(kClass: KClass<*>): Boolean
}

object DefaultTypeTaxonomy : TypeTaxonomy {
    override fun isValue(kClass: KClass<*>): Boolean {
        return kClass == String::class ||
                kClass == Int::class || kClass == Long::class ||
                kClass == Double::class || kClass == Float::class ||
                kClass == Boolean::class || kClass == java.util.UUID::class ||
                kClass.java.isEnum
    }
}

// ========================================================================
//  Internal Contracts (File Level)
// ========================================================================

/**
 * Encapsulates the rule for creating TypeReferences.
 */
private fun interface TypeReferenceFactory {
    fun create(kType: KType): TypeReference
}

// ========================================================================
//  Member Resolution
// ========================================================================

private class ReflectionMemberResolver(
    private val resolver: TypeResolver,
    private val refs: TypeReferenceFactory,
    private val annotations: ReflectionAnnotationResolver
) {
    fun resolveProperties(kClass: KClass<*>): List<PropertyDescriptor> {
        return kClass.declaredMemberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .map { PropertyDescriptorImpl(it, resolver, refs, annotations) }
    }

    fun resolveConstructors(kClass: KClass<*>): List<ConstructorDescriptor> {
        val primary = kClass.primaryConstructor ?: return emptyList()
        return listOf(ConstructorDescriptorImpl(primary, resolver, refs, annotations))
    }
}

// [Concrete IR] Property
private class PropertyDescriptorImpl(
    private val prop: KProperty1<out Any, *>,
    private val resolver: TypeResolver,
    private val refs: TypeReferenceFactory,
    private val annotations: ReflectionAnnotationResolver
) : PropertyDescriptor {
    override val name: String = prop.name
    override val type: TypeDescriptor by lazy { resolver.resolve(refs.create(prop.returnType)) }
    override fun hasAnnotation(fullyQualifiedClassName: String) =
        annotations.hasAnnotation(prop, fullyQualifiedClassName)

    override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
        annotations.getAttributes(prop, fullyQualifiedClassName)

    override fun toString(): String = "Property($name)"
}

// [Concrete IR] Constructor
private class ConstructorDescriptorImpl(
    private val func: KFunction<*>,
    private val resolver: TypeResolver,
    private val refs: TypeReferenceFactory,
    private val annotations: ReflectionAnnotationResolver
) : ConstructorDescriptor {
    override val parameters: List<ParameterDescriptor> by lazy {
        func.parameters.map { ParameterDescriptorImpl(it, resolver, refs, annotations) }
    }

    override fun hasAnnotation(fullyQualifiedClassName: String) =
        annotations.hasAnnotation(func, fullyQualifiedClassName)

    override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
        annotations.getAttributes(func, fullyQualifiedClassName)

    override fun toString(): String = "Constructor(params=${func.parameters.size})"
}

// [Concrete IR] Parameter
private class ParameterDescriptorImpl(
    private val param: KParameter,
    private val resolver: TypeResolver,
    private val refs: TypeReferenceFactory,
    private val annotations: ReflectionAnnotationResolver
) : ParameterDescriptor {
    override val name: String = param.name ?: "arg${param.index}"
    override val index: Int = param.index
    override val isOptional: Boolean = param.isOptional
    override val type: TypeDescriptor by lazy { resolver.resolve(refs.create(param.type)) }
    override fun hasAnnotation(fullyQualifiedClassName: String) =
        annotations.hasAnnotation(param, fullyQualifiedClassName)

    override fun getAnnotationAttributes(fullyQualifiedClassName: String) =
        annotations.getAttributes(param, fullyQualifiedClassName)

    override fun toString(): String = "Parameter($name)"
}

// ========================================================================
//  Annotation Resolution
// ========================================================================

private class ReflectionAnnotationResolver {
    fun hasAnnotation(element: KAnnotatedElement, fullyQualifiedClassName: String): Boolean {
        return element.annotations.any { it.annotationClass.qualifiedName == fullyQualifiedClassName }
    }

    fun getAttributes(element: KAnnotatedElement, fullyQualifiedClassName: String): Map<String, Any?>? {
        if (!hasAnnotation(element, fullyQualifiedClassName)) return null
        throw UnsupportedOperationException(
            "Attribute extraction for annotation '$fullyQualifiedClassName' is not yet implemented in Phase 2 reflection adapter."
        )
    }
}

// ========================================================================
//  Adapters
// ========================================================================

data class KTypeSource(val kType: KType) : TypeSource