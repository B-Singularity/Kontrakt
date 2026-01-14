package execution.domain.vo.trace

/**
 * [ADR-016] Audit Structure
 * Categorizes the test lifecycle into fixed BDD phases.
 *
 * **Design Decision:**
 * Added [RESULT] to strictly define the 'Termination Phase' of a test.
 * This allows the logging system to detect 'Incomplete Tests' (Crashes).
 */
enum class TracePhase {
    /** [GIVEN] Strategy selection, dependency assembly. */
    DESIGN,

    /** [WHEN] Invocation of target method. */
    EXECUTION,

    /** [THEN] Contract validation. */
    VERIFICATION,

    /** [END] Final verdict and teardown. */
    RESULT
}
