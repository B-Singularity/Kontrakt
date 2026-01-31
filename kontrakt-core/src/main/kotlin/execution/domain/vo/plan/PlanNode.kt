package execution.domain.vo.plan

import metamodel.domain.vo.TypeReference

/**
 * Represents a constraint or metadata associated with a plan node.
 * This encodes constraints directly into the IR, replacing legacy runtime validators.
 */
interface Attribute

/**
 * Represents an annotation extracted from the source code (e.g., @StringLength, @NotNull).
 */
data class AnnotationAttribute(
    val name: String,
    val values: Map<String, Any?>
) : Attribute

/**
 * [IR Core]
 * The common interface for all nodes in the generation plan.
 */
sealed interface PlanNode {
    val type: TypeReference

    /**
     * The set of constraints and metadata derived from the source type.
     * Used by the Linker to select the appropriate Generator.
     */
    val attributes: Set<Attribute>
}