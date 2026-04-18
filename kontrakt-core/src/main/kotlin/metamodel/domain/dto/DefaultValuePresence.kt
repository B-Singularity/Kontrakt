package metamodel.domain.dto

/**
 * Closed default-value vocabulary for constructor-parameter facts.
 *
 * UNKNOWN is intentionally distinct from ABSENT.
 */
enum class DefaultValuePresence {
    PRESENT,
    ABSENT,
    UNKNOWN,
}