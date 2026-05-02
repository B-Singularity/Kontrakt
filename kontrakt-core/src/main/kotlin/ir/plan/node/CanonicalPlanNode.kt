package ir.plan.node

import ir.IrLimits
import ir.identity.CanonicalIdentifier
import ir.identity.CanonicalSignature
import ir.plan.edge.Attribute
import ir.structure.DeterministicMap

/**
 * Canonical plan node protocol.
 *
 * Guarantees:
 * - typeSignature is a canonical byte signature
 * - attributes are deterministic and immutable
 *
 * Note:
 * - No raw collections are allowed in the protocol surface.
 */
sealed interface CanonicalPlanNode {
    val typeSignature: CanonicalSignature
    val attributes: DeterministicMap<CanonicalIdentifier, Attribute>
}

/**
 * Leaf node in the canonical plan graph.
 */
class CanonicalAtomicNode(
    override val typeSignature: CanonicalSignature,
    attributes: Map<CanonicalIdentifier, Attribute>,
) : CanonicalPlanNode {
    override val attributes: DeterministicMap<CanonicalIdentifier, Attribute> =
        DeterministicMap.of(attributes, IrLimits.MAX_NODE_ATTRIBUTES)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalAtomicNode) return false
        return typeSignature == other.typeSignature && attributes == other.attributes
    }

    override fun hashCode(): Int = 31 * typeSignature.hashCode() + attributes.hashCode()

    override fun toString(): String = "CanonicalAtomicNode(sig=$typeSignature, attrs=$attributes)"
}

/**
 * Composite node in the canonical plan graph, containing deterministic named fields.
 */
class CanonicalCompositeNode(
    override val typeSignature: CanonicalSignature,
    attributes: Map<CanonicalIdentifier, Attribute>,
    fields: Map<CanonicalIdentifier, CanonicalPlanNode>,
) : CanonicalPlanNode {
    override val attributes: DeterministicMap<CanonicalIdentifier, Attribute> =
        DeterministicMap.of(attributes, IrLimits.MAX_NODE_ATTRIBUTES)

    val fields: DeterministicMap<CanonicalIdentifier, CanonicalPlanNode> =
        DeterministicMap.of(fields, IrLimits.MAX_NODE_FIELDS)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalCompositeNode) return false
        return typeSignature == other.typeSignature &&
            attributes == other.attributes &&
            fields == other.fields
    }

    override fun hashCode(): Int {
        var result = typeSignature.hashCode()
        result = 31 * result + attributes.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun toString(): String = "CanonicalCompositeNode(sig=$typeSignature, attrs=$attributes, fields=$fields)"
}
