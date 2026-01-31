package execution.domain.service.planner

import execution.domain.exception.StructuralPlanningException
import execution.domain.vo.plan.*
import metamodel.domain.port.outgoing.TypeResolver
import metamodel.domain.vo.TypeKind
import metamodel.domain.vo.TypeReference
import java.util.ArrayDeque

/**
 * Analyzes the type structure and produces an [UnlinkedNode] tree.
 */
class StructuralPlanner(
    private val typeResolver: TypeResolver
) {

    fun plan(rootType: TypeReference): UnlinkedNode {
        val context = PlanningContext()
        return traverse(rootType, context, emptySet())
    }

    /**
     * Traverses the type graph and builds the structural plan.
     *
     * **Attribute Scope Rule:**
     * Attributes passed to this method apply *strictly* to the current node being created.
     * They do NOT implicitly propagate to children nodes.
     * For example, @NotNull on a parent field does not imply @NotNull on its children's fields.
     */
    private fun traverse(
        type: TypeReference,
        context: PlanningContext,
        attributes: Set<Attribute>
    ): UnlinkedNode {

        // 1. Cycle Detection
        if (context.hasAncestor(type)) {
            return UnlinkedReferenceNode(
                type = type,
                recursionDepth = context.getDepth(type),
                attributes = attributes
            )
        }

        context.push(type)

        try {
            val descriptor = typeResolver.resolve(type)

            return when (descriptor.kind) {
                TypeKind.ATOMIC -> UnlinkedAtomicNode(type, attributes)

                TypeKind.COLLECTION -> {
                    val elementType = descriptor.elementType
                        ?: throw StructuralPlanningException(type, "Collection missing element type")

                    // Note: Attributes on the collection (e.g., @Size) belong to the collection node.
                    // Elements start with an empty set of attributes unless defined by the element type itself.
                    UnlinkedCollectionNode(type, traverse(elementType, context, emptySet()), attributes)
                }

                TypeKind.INTERFACE, TypeKind.ABSTRACT -> {
                    UnlinkedInterfaceNode(type, attributes)
                }

                TypeKind.COMPOSITE -> {
                    val fields = descriptor.fields.associate { field ->
                        // Extract annotations from the field definition and convert to Attributes.
                        val fieldAttributes = field.annotations.map {
                            AnnotationAttribute(it.name, it.values)
                        }.toSet()

                        field.name to traverse(field.type, context, fieldAttributes)
                    }
                    UnlinkedCompositeNode(type, fields, attributes)
                }

                else -> UnlinkedAtomicNode(type, attributes)
            }

        } catch (e: Exception) {
            if (e is execution.domain.exception.ExecutionException) throw e
            throw StructuralPlanningException(type, "Unexpected error during traversal", e)
        } finally {
            context.pop()
        }
    }

    private class PlanningContext {
        private val stack = ArrayDeque<TypeReference>()
        fun push(type: TypeReference) = stack.push(type)
        fun pop() = stack.pop()
        fun hasAncestor(type: TypeReference) = stack.contains(type)
        fun getDepth(type: TypeReference) = stack.indexOf(type)
    }
}