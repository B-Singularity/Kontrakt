package execution.domain.vo.plan

/**
 * Represents the semantic relationship between a parent node and its child in the plan.
 */
enum class EdgeKind {
    TYPE_ARGUMENT,  // Generic type argument
    FIELD,          // Class field
    CTOR_PARAM,     // Constructor parameter
    ELEMENT,        // Collection element
    MAP_KEY,        // Map key
    MAP_VALUE       // Map value
}