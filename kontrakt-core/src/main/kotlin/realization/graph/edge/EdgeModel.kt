package realization.graph.edge

import realization.graph.IrLimits
import realization.graph.IrProtocolViolationException
import realization.graph.structure.DeterministicMap
import realization.identity.CanonicalIdentifier

/**
 * Edge kind within a canonical plan graph.
 */
enum class EdgeKind { TYPE_ARGUMENT, FIELD, CTOR_PARAM, ELEMENT, MAP_KEY, MAP_VALUE }

/**
 * Where an attribute originates from, for traceability and order stability.
 */
enum class AttributeOrigin {
    TYPE_DECLARATION,
    FIELD_DECLARATION,
    FIELD_TYPE_USE,
    MAP_KEY_TYPE_USE,
    MAP_VALUE_TYPE_USE,
    ELEMENT_TYPE_USE,
}

/**
 * Base attribute type in the plan order.
 *
 * Attributes are modeled as order objects (not annotations) to avoid reflection leakage
 * and to enable deterministic serialization and caching.
 */
sealed class Attribute {
    abstract val name: CanonicalIdentifier
    abstract val origin: AttributeOrigin
}

/**
 * Represents an annotation-like attribute with deterministic key/value pairs.
 *
 * Invariants:
 * - values must be normalized into DeterministicMap
 * - each value must respect max length constraints
 */
class AnnotationAttribute(
    override val name: CanonicalIdentifier,
    override val origin: AttributeOrigin,
    values: Map<CanonicalIdentifier, String>,
) : Attribute() {
    val values: DeterministicMap<CanonicalIdentifier, String>

    init {
        this.values =
            DeterministicMap.of(values, IrLimits.MAX_ATTRIBUTE_VALUES) {
                if (it.length > IrLimits.MAX_METADATA_VALUE_LENGTH) {
                    throw IrProtocolViolationException("Attribute value too long.")
                }
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationAttribute) return false
        return name == other.name && origin == other.origin && values == other.values
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + values.hashCode()
        return result
    }

    override fun toString(): String = "AnnotationAttribute(name=$name, origin=$origin, values=$values)"
}
