package reporting.adapter.config

import java.nio.file.Path

/**
 * [Configuration] Reporting Directives
 *
 * Encapsulates all configuration required by the Reporting Context to generate artifacts.
 * This is decoupled from [UserControlOptions] to keep the domain clean.
 */
data class ReportingDirectives(
    /**
     * The root directory where reports should be generated.
     * e.g., "build/reports/kontrakt"
     */
    val baseReportDir: Path,

    /**
     * Whether to include verbose details (like success logs) in the report.
     */
    val verbose: Boolean,

    /**
     * Whether to keep historical reports (timestamped) or overwrite the latest one.
     */
    val archiveMode: Boolean,

    /**
     * The maximum number of stack trace elements to include in error logs.
     */
    val stackTraceLimit: Int,

    /**
     * The set of formats enabled for this run.
     */
    val formats: Set<ReportFormat>
)