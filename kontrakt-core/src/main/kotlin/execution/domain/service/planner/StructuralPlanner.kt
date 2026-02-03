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
        attributes: Set<String>
    ): UnlinkedNode {

        // 1. Cycle Detection (재귀 방지)
        // 스택을 검사하여 순환이 감지되면 구조 생성을 멈추고 ReferenceNode(토큰)를 반환합니다.
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
                // [Atomic] 더 이상 쪼갤 수 없는 단위 (String, Int, Enum 등)
                TypeKind.ATOMIC -> UnlinkedAtomicNode(type, attributes)

                // [Collection] 요소 타입을 찾아 하위 구조 생성
                TypeKind.COLLECTION -> {
                    val elementType = descriptor.elementType
                        ?: throw StructuralPlanningException(type, "Collection missing element type")

                    // 주의: @Size 같은 속성은 컬렉션 자체에 붙고, 내부 요소는 속성 없이 시작함
                    UnlinkedCollectionNode(
                        type = type,
                        elementNode = traverse(elementType, context, emptySet()),
                        attributes = attributes
                    )
                }

                // [Interface/Abstract] Linker가 나중에 구현체를 찾도록 위임
                TypeKind.INTERFACE, TypeKind.ABSTRACT -> {
                    UnlinkedInterfaceNode(type, attributes)
                }

                // [Composite] 필드를 순회하며 구조 확장
                TypeKind.COMPOSITE -> {
                    val fields = descriptor.fields.associate { field ->
                        // 필드에 붙은 어노테이션을 추출하여 자식 노드의 Attribute로 변환
                        val fieldAttributes = field.annotations.map {
                            AnnotationAttribute(it.name, it.values)
                        }.toSet()

                        field.name to traverse(field.type, context, fieldAttributes)
                    }
                    UnlinkedCompositeNode(type, fields, attributes)
                }

                // Fallback
                else -> UnlinkedAtomicNode(type, attributes)
            }

        } catch (e: Exception) {
            if (e is execution.domain.exception.ExecutionException) throw e
            throw StructuralPlanningException(type, "Unexpected error during traversal", e)
        } finally {
            context.pop()
        }
    }

    // 순환 참조 감지를 위한 Context
    private class PlanningContext {
        private val stack = ArrayDeque<TypeReference>()
        fun push(type: TypeReference) = stack.push(type)
        fun pop() = stack.pop()
        fun hasAncestor(type: TypeReference) = stack.contains(type)
        fun getDepth(type: TypeReference) = stack.indexOf(type)
    }
}