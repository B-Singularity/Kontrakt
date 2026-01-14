package reporting.adapter.config

/**
 * [Configuration] Supported Report Formats
 */
enum class ReportFormat {
    /**
     * Prints execution results to the standard system console.
     */
    CONSOLE,

    /**
     * Generates a single-page HTML report (SPA) for deep analysis.
     */
    HTML,

    /**
     * Generates a machine-readable JSON report for external tools or CI integration.
     */
    JSON,
}
