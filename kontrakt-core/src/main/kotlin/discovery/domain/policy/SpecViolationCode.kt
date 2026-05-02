package discovery.domain.policy

/**
 * [Specification Violation Code]
 * Enumerates pure domain rule violations.
 * Independent of specific scanners or reporting mechanisms.
 */
enum class SpecViolationCode {
    CONTRACT_MUST_BE_INTERFACE,
    DATA_CONTRACT_MUST_BE_CONCRETE,
}
