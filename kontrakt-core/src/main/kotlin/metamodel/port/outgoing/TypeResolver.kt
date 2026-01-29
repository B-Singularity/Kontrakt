package metamodel.domain.port.outgoing

import metamodel.domain.model.TypeDescriptor
import metamodel.domain.vo.TypeReference

/**
 * [Secondary Port] The Abstract Interface for Type Resolution.
 *
 * In Hexagonal Architecture, this is an **Outgoing Port**.
 * The Domain Core (`Metamodel`) defines this interface to request type information,
 * unaware of whether it comes from:
 * - JVM Reflection (Runtime)
 * - KSP (Compile-time)
 * - Bytecode Analysis (ASM)
 *
 * @see metamodel.adapter.reflection.ReflectionTypeResolver (The Adapter)
 */
interface TypeResolver {

    /**
     * Resolves a [TypeReference] (Pointer) into a concrete [TypeDescriptor] (Graph Node).
     *
     * @param reference The opaque handle pointing to a type source (e.g., KType, KSClassDeclaration).
     * @return The canonical Domain IR (Intermediate Representation) of the type.
     * @throws metamodel.domain.exception.MetamodelException If resolution fails or the source is invalid.
     */
    fun resolve(reference: TypeReference): TypeDescriptor
}