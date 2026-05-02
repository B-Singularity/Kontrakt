package metamodel.domain.dto

/**
 * Closed nullability vocabulary for normalized metamodel facts.
 *
 * UNKNOWN is first-class.
 * It must not be collapsed into nullable or non-nullable by adapter convenience.
 */
enum class NullabilityKind {
    NON_NULL,
    NULLABLE,
    UNKNOWN,
}
