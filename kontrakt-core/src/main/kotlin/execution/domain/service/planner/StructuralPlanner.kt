package execution.domain.service.planner

import execution.domain.exception.StructuralPlanningException
import execution.domain.vo.plan.*
import ir.Attribute
import ir.AttributeOrigin
import ir.EdgeModel
import metamodel.domain.model.PropertySource
import metamodel.domain.vo.AnnotationDescriptor
import metamodel.domain.vo.TypeReference
import stage.input.material.TypeKind
import java.util.ArrayDeque

/**
 * [Domain Service] Structural Planner.
 *
 * Responsibilities:
 * 1. **Traversal**: DFS traversal of the type graph.
 * 2. **Cycle Detection**: Identifies recursive structures using [cycleId].
 * 3. **Attribute Merging**: Merges attributes from Use-site, Inheritance, and Declaration-site deterministically.
 */
class StructuralPlanner(
    private val typeResolver: TypeResolver,
) {
    fun plan(rootType: TypeReference): UnlinkedNode {
        val context = PlanningContext()
        return traverse(
            type = rootType,
            context = context,
            inheritedAttributes = emptyMap(),
            currentOrigin = AttributeOrigin.TYPE_DECLARATION,
            edgeKind = EdgeModel.TYPE_ARGUMENT,
            edgeName = null,
        )
    }

    private fun traverse(
        type: TypeReference,
        context: PlanningContext,
        inheritedAttributes: Map<String, Attribute>,
        currentOrigin: AttributeOrigin,
        edgeKind: EdgeModel,
        edgeName: String?,
    ): UnlinkedNode {
        // Push CycleId (Nullability Stripped)
        context.push(type.cycleId)
        var pushed = true

        try {
            // [Determinism] 1. Pre-calculate Use-Site Attributes (Last-Wins strategy)
            val useSiteAttrs = LinkedHashMap<String, Attribute>()
            type.useSiteAnnotations.forEach {
                val attr = toAttribute(it, currentOrigin)
                putLastWins(useSiteAttrs, attr.name, attr)
            }

            val effectiveForCycle = mergeAttributes(inheritedAttributes, useSiteAttrs)

            // [Safety] 2. Cycle Check
            if (context.isCycleDetected(type.cycleId)) {
                return UnlinkedCycleNode(
                    type = type,
                    attributes = effectiveForCycle,
                    edgeKind = edgeKind,
                    edgeName = edgeName,
                    targetTypeId = type.cycleId,
                    pathSnapshot = context.getPathSnapshot(),
                )
            }

            // [Contract] 3. Shallow Resolve (Must not trigger recursion)
            val descriptor =
                try {
                    typeResolver.resolve(type)
                } catch (e: Exception) {
                    throw StructuralPlanningException(type.id, "Resolution failed", e)
                }

            // [Determinism] 4. Decl-Site Attributes
            val declAttrs = LinkedHashMap<String, Attribute>()
            descriptor.annotations.forEach {
                val attr = toAttribute(it, AttributeOrigin.TYPE_DECLARATION)
                putLastWins(declAttrs, attr.name, attr)
            }

            // Merge Priority: Decl < Inherited < UseSite
            val fullEffectiveAttributes = mergeFullAttributes(declAttrs, inheritedAttributes, useSiteAttrs)

            // 5. Structural Branching
            return when (descriptor.kind) {
                TypeKind.ATOMIC -> UnlinkedAtomicNode(type, fullEffectiveAttributes)

                TypeKind.COMPOSITE -> {
                    val fields = LinkedHashMap<String, UnlinkedNode>()
                    descriptor.properties.forEach { prop ->
                        val propDeclAttrs = LinkedHashMap<String, Attribute>()
                        prop.annotations.forEach {
                            val attr = toAttribute(it, AttributeOrigin.FIELD_DECLARATION)
                            putLastWins(propDeclAttrs, attr.name, attr)
                        }

                        val childEdgeKind =
                            when (prop.source) {
                                PropertySource.CONSTRUCTOR_PARAMETER -> EdgeModel.CTOR_PARAM
                                else -> EdgeModel.FIELD
                            }

                        fields[prop.name] =
                            traverse(
                                type = prop.type,
                                context = context,
                                inheritedAttributes = propDeclAttrs,
                                currentOrigin = AttributeOrigin.FIELD_TYPE_USE,
                                edgeKind = childEdgeKind,
                                edgeName = prop.name,
                            )
                    }
                    UnlinkedCompositeNode(type, fields, fullEffectiveAttributes)
                }

                TypeKind.COLLECTION -> {
                    val elementType =
                        descriptor.elementType
                            ?: throw StructuralPlanningException(type.id, "Collection missing element type")

                    UnlinkedCollectionNode(
                        type = type,
                        elementNode =
                            traverse(
                                type = elementType,
                                context = context,
                                inheritedAttributes = emptyMap(),
                                currentOrigin = AttributeOrigin.ELEMENT_TYPE_USE,
                                edgeKind = EdgeModel.ELEMENT,
                                edgeName = null,
                            ),
                        isFixedSize = false,
                        attributes = fullEffectiveAttributes,
                    )
                }

                TypeKind.ARRAY -> {
                    val componentType =
                        descriptor.componentType
                            ?: throw StructuralPlanningException(type.id, "Array missing component type")

                    UnlinkedCollectionNode(
                        type = type,
                        elementNode =
                            traverse(
                                type = componentType,
                                context = context,
                                inheritedAttributes = emptyMap(),
                                currentOrigin = AttributeOrigin.ELEMENT_TYPE_USE,
                                edgeKind = EdgeModel.ELEMENT,
                                edgeName = null,
                            ),
                        isFixedSize = true,
                        attributes = fullEffectiveAttributes,
                    )
                }

                TypeKind.MAP -> {
                    val keyType = descriptor.keyType!!
                    val valueType = descriptor.valueType!!
                    UnlinkedMapNode(
                        type = type,
                        keyNode =
                            traverse(
                                keyType,
                                context,
                                emptyMap(),
                                AttributeOrigin.MAP_KEY_TYPE_USE,
                                EdgeModel.MAP_KEY,
                                "key",
                            ),
                        valueNode =
                            traverse(
                                valueType,
                                context,
                                emptyMap(),
                                AttributeOrigin.MAP_VALUE_TYPE_USE,
                                EdgeModel.MAP_VALUE,
                                "value",
                            ),
                        attributes = fullEffectiveAttributes,
                    )
                }

                else -> throw StructuralPlanningException(type.id, "Unsupported TypeKind: ${descriptor.kind}")
            }
        } finally {
            if (pushed) context.pop()
        }
    }

    /**
     * Helper to enforce "Last Wins" policy while preserving iteration order of the winner.
     */
    private fun <K, V> putLastWins(
        map: LinkedHashMap<K, V>,
        key: K,
        value: V,
    ) {
        if (map.containsKey(key)) map.remove(key)
        map[key] = value
    }

    private fun mergeFullAttributes(
        decl: Map<String, Attribute>,
        inherited: Map<String, Attribute>,
        useSite: Map<String, Attribute>,
    ): Map<String, Attribute> {
        val result = LinkedHashMap<String, Attribute>()
        decl.forEach { (k, v) -> putLastWins(result, k, v) }
        inherited.forEach { (k, v) -> putLastWins(result, k, v) }
        useSite.forEach { (k, v) -> putLastWins(result, k, v) }
        return result
    }

    private fun mergeAttributes(
        base: Map<String, Attribute>,
        overrides: Map<String, Attribute>,
    ): Map<String, Attribute> {
        val result = LinkedHashMap<String, Attribute>(base)
        overrides.forEach { (k, v) -> putLastWins(result, k, v) }
        return result
    }

    private fun toAttribute(
        desc: AnnotationDescriptor,
        origin: AttributeOrigin,
    ): Attribute.AnnotationAttribute = Attribute.AnnotationAttribute(desc.qualifiedName, origin, desc.values)

    private class PlanningContext {
        private val counts = HashMap<String, Int>()
        private val cycleIdStack = ArrayDeque<String>()

        fun push(cycleId: String) {
            counts[cycleId] = (counts[cycleId] ?: 0) + 1
            cycleIdStack.push(cycleId)
        }

        fun pop() {
            if (cycleIdStack.isEmpty()) throw StructuralPlanningException.IntegrityError("Stack Underflow")
            val cycleId = cycleIdStack.pop()
            val c = counts[cycleId] ?: 1
            if (c <= 1) counts.remove(cycleId) else counts[cycleId] = c - 1
        }

        fun isCycleDetected(cycleId: String) = (counts[cycleId] ?: 0) >= 2

        fun getPathSnapshot() = cycleIdStack.joinToString(" <- ")
    }
}
