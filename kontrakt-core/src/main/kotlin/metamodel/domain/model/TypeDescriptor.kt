package metamodel.domain.model

import metamodel.domain.vo.TypeId
import metamodel.domain.vo.TypeKind

/**
 * [Trait] Represents any element in the type graph that can hold annotations.
 * This separates the "Configuration" aspect from the "Structural" aspect.
 */
interface AnnotatedElement {
    fun hasAnnotation(fullyQualifiedClassName: String): Boolean
    fun getAnnotationAttributes(fullyQualifiedClassName: String): Map<String, Any?>?
}


/**
 * [Core Model] The Intermediate Representation (IR) of a Type.
 *
 * This graph represents the "Canonical Structure" of a type, independent of the underlying technology.
 * It acts as the **Abstract Syntax Tree (AST)** for the Generator.
 *
 * ## Architectural Contract
 * 1. **Immutable:** The graph structure must not change after creation.
 * 2. **Lazy-Capable:** Edges are defined as interfaces to allow lazy loading (Cycle-Safe).
 * 3. **Taxonomy Authority:** The sealed hierarchy is the source of truth.
 * The [kind] property is derived for fast dispatching and serialization.
 */

sealed interface TypeDescriptor : AnnotatedElement {
    // --- Identity & Classification ---
    val typeId: TypeId
    val simpleName: String
    val qualifiedName: String

    /**
     * Derived classification for fast dispatch (e.g., when statements).
     * Note: The true taxonomy is defined by the sealed TypeDescriptor hierarchy types.
     */
    val kind: TypeKind

    val isNullable: Boolean
    val isInline: Boolean

    // ========================================================================
    //  Sealed Hierarchy (The Taxonomy / AST Nodes)
    // ========================================================================

    /** 1. [Leaf Node] Atomic values (String, Int, UUID, Enum). */
    interface Value : TypeDescriptor {
        override val kind: TypeKind get() = TypeKind.VALUE
    }

    /** 2. [Collection Node] Linear containers (List, Set). */
    interface Container : TypeDescriptor {
        override val kind: TypeKind get() = TypeKind.CONTAINER
        val elementType: TypeDescriptor
    }

    /** 3. [Dictionary Node] Key-Value pairs (Map). */
    interface Map : TypeDescriptor {
        override val kind: TypeKind get() = TypeKind.MAP
        val keyType: TypeDescriptor
        val valueType: TypeDescriptor
    }

    /** 4. [Buffer Node] Fixed-size memory chunks (Array). */
    interface Array : TypeDescriptor {
        override val kind: TypeKind get() = TypeKind.ARRAY
        val componentType: TypeDescriptor
    }

    /** 5. [Graph Node] Complex objects (Class, Data Class, Interface). */
    interface Structural : TypeDescriptor {
        // Explicit mapping to the TypeKind taxonomy
        override val kind: TypeKind get() = TypeKind.STRUCTURAL

        /** Generic type arguments (e.g., Box<T> -> T). */
        val typeArguments: List<TypeDescriptor>

        // --- Edges to other nodes (Members) ---
        val constructors: List<ConstructorDescriptor>
        val properties: List<PropertyDescriptor>
        val methods: List<MethodDescriptor>
    }
}


// ========================================================================
//  Graph Edges (Members)
// ========================================================================

interface ConstructorDescriptor : AnnotatedElement {
    val parameters: List<ParameterDescriptor>
}

interface PropertyDescriptor : AnnotatedElement {
    val name: String
    val type: TypeDescriptor // Edge to another node
}

interface MethodDescriptor : AnnotatedElement {
    val name: String
    val returnType: TypeDescriptor
    val parameters: List<ParameterDescriptor>
}

interface ParameterDescriptor : AnnotatedElement {
    val name: String
    val index: Int
    val isOptional: Boolean
    val type: TypeDescriptor
}
