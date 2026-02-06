package execution.domain.vo.generation

/**
 * [Policy] Strategies for breaking recursive cycles.
 */
enum class TruncationRule {
    NULL,               // Return null (if nullable)
    EMPTY_COLLECTION,   // Return empty list/set
    EMPTY_MAP,          // Return empty map
    EMPTY_ARRAY,        // Return empty array
    DIAGNOSTIC_STUB,    // Return a proxy that throws on access (for Interfaces)
    FAIL_FAST           // Throw exception immediately (Cycle detected on non-nullable concrete type)
}