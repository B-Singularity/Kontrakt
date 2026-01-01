package common.config

/**
 * [ADR-016] User Control Surface
 * An immutable configuration object containing execution and reporting control options
 * input by the user via CLI or configuration files.
 * This object serves as the Single Source of Truth within the execution context.
 */
data class UserControlOptions(
    /**
     * [--trace]
     * Flag to enable Synthetic BDD Audit mode.
     * If true, detailed events for Design, Execution, and Verification phases will be collected.
     * This incurs a slight performance penalty but provides full transparency.
     */
    val traceMode: Boolean = false,

    /**
     * [--tests "pattern"]
     * Filter patterns to execute only specific classes or methods.
     * Supports wildcards (e.g., "com.shop.*Order*", "MyTest").
     * If empty, a full scan is performed.
     */
    val testPatterns: Set<String> = emptySet(),

    /**
     * [--package "name"]
     * Limits the scanning scope to a specific package to support fast feedback loops.
     * If null, scanning starts from the root package.
     */
    val packageScope: String? = null,

    /**
     * [--archive]
     * [ADR-015] Report retention policy.
     * If true, the report is saved as a timestamped file (e.g., `history/report-{time}.html`).
     * If false, `index.html` is overwritten (Default).
     */
    val archiveMode: Boolean = false,

    /**
     * [--verbose / --quiet]
     * Controls the verbosity level of the console output.
     */
    val verbosity: Verbosity = Verbosity.NORMAL,

    /**
     * [--seed 12345]
     * Fixes the random seed for Deterministic Reproduction.
     * If null, a new random seed is generated for each run.
     */
    val seed: Long? = null
) {
    enum class Verbosity {
        QUIET,   // Outputs only failure summaries.
        NORMAL,  // Outputs smart summaries and failure details (Default).
        VERBOSE  // Outputs full details including successful logs.
    }

    companion object {
        val DEFAULT = UserControlOptions()
    }
}
