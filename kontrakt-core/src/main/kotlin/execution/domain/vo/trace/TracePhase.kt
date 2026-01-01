package execution.domain.vo.trace

/**
 * [ADR-016] Audit Structure
 * Categorizes the test lifecycle into fixed BDD phases.
 * Note: Descriptions and display logic are delegated to the Presentation Layer.
 */
enum class TracePhase {
    /** [GIVEN] Strategy selection, dependency assembly. */
    DESIGN,

    /** [WHEN] Invocation of target method. */
    EXECUTION,

    /** [THEN] Contract validation. */
    VERIFICATION
}