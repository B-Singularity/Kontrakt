package execution.adapter

import discovery.domain.vo.DiscoveryPolicy
import discovery.domain.vo.ScanScope
import execution.domain.vo.AuditDepth
import execution.domain.vo.AuditPolicy
import execution.domain.vo.DeterminismPolicy
import execution.domain.vo.ExecutionPolicy
import execution.domain.vo.LogRetention
import execution.domain.vo.ResourcePolicy
import reporting.adapter.config.ReportFormat
import reporting.adapter.config.ReportingDirectives
import java.nio.file.Paths

/**
 * Maps the raw UserControlOptions to the structured ExecutionPolicy.
 * This applies the "Matrix of Control" logic to resolve Retention and Depth.
 */
fun UserControlOptions.toExecutionPolicy(): ExecutionPolicy {
    // 1. Resolve Log Retention (Quantity)
    // --archive or --verbose implies keeping ALL logs.
    // --quiet implies keeping NO logs.
    // Default is keeping logs only ON_FAILURE.
    val retention = when {
        this.archiveMode || this.isVerbose -> LogRetention.ALWAYS
        this.verbosity == UserControlOptions.Verbosity.QUIET -> LogRetention.NONE
        else -> LogRetention.ON_FAILURE
    }

    // 2. Resolve Audit Depth (Quality)
    // --trace enables "Explainable" mode (full BDD history).
    // Default is "Simple" mode (Result only).
    val depth = if (this.traceMode) {
        AuditDepth.EXPLAINABLE
    } else {
        AuditDepth.SIMPLE
    }

    return ExecutionPolicy(
        determinism = DeterminismPolicy(seed = this.seed),
        auditing = AuditPolicy(retention = retention, depth = depth),
        resources = ResourcePolicy(timeoutMs = 5000L)
    )
}

/**
 * Maps UserControlOptions to DiscoveryPolicy.
 * Resolves the ScanScope based on provided patterns or package names.
 */
fun UserControlOptions.toDiscoveryPolicy(): DiscoveryPolicy {
    val scope = when {
        this.testPatterns.isNotEmpty() -> ScanScope.Classes(classNames = this.testPatterns)
        !this.packageScope.isNullOrBlank() -> ScanScope.Packages(packageNames = setOf(this.packageScope))
        else -> ScanScope.All
    }
    return DiscoveryPolicy(scope)
}

/**
 * Maps UserControlOptions to ReportingDirectives.
 */
fun UserControlOptions.toReportingDirectives(): ReportingDirectives {
    return ReportingDirectives(
        baseReportDir = Paths.get("build", "reports", "kontrakt"),
        verbose = this.verbosity == UserControlOptions.Verbosity.VERBOSE,
        archiveMode = this.archiveMode,
        stackTraceLimit = this.stackTraceLimit,
        formats = setOf(ReportFormat.CONSOLE, ReportFormat.HTML, ReportFormat.JSON)
    )
}