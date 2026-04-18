package metamodel.domain.dto

/**
 * Closed mutability vocabulary for property facts.
 *
 * UNKNOWN must not be treated as mutable or read-only implicitly.
 */
enum class PropertyMutability {
    READ_ONLY,
    MUTABLE,
    UNKNOWN,
}