package execution.adapter.config

import discovery.domain.vo.DiscoveryPolicy
import discovery.domain.vo.ScanScope
import execution.domain.vo.config.AuditDepth
import execution.domain.vo.config.AuditPolicy
import execution.domain.vo.config.DeterminismPolicy
import execution.domain.vo.config.ExecutionPolicy
import execution.domain.vo.config.LogRetention
import execution.domain.vo.config.ResourcePolicy
import reporting.adapter.config.ReportingDirectives
import java.nio.file.Paths

/**
 * Maps the raw [UserControlOptions] to the structured domain configuration object [ExecutionPolicy].
 *
 * This mapping logic applies the **"Matrix of Control"** to resolve:
 * 1. Log Retention based on verbosity and archive mode.
 * 2. Audit Depth based on the trace flag.
 * 3. Resource constraints based on the [ExecutionTier] ([ADR-024]).
 */
fun UserControlOptions.toExecutionPolicy(): ExecutionPolicy {
    // 1. Resolve Log Retention (Quantity)
    // --archive or --verbose implies keeping ALL logs for post-mortem analysis.
    // --quiet implies keeping NO logs to reduce CI log volume.
    // Default is keeping logs only ON_FAILURE.
    val retention = when {
        this.archiveMode || this.isVerbose -> LogRetention.ALWAYS
        this.verbosity == UserControlOptions.Verbosity.QUIET -> LogRetention.NONE
        else -> LogRetention.ON_FAILURE
    }

    // 2. Resolve Audit Depth (Quality)
    // --trace enables "Explainable" mode (full BDD history + Design Decisions).
    // Default is "Simple" mode (Result only).
    val depth = if (this.traceMode) {
        AuditDepth.EXPLAINABLE
    } else {
        AuditDepth.SIMPLE
    }

    // Resource limits are internal constraints (User cannot customize low-level details).
    // Defaults to 5 seconds.
    val resourcePolicy = ResourcePolicy(
        timeoutMs = 5000L
    )

    return ExecutionPolicy(
        determinism = DeterminismPolicy(seed = this.seed),
        auditing = AuditPolicy(retention = retention, depth = depth),
        resources = resourcePolicy,
    )
}

/**
 * Maps [UserControlOptions] to [DiscoveryPolicy].
 * Resolves the [ScanScope] based on provided test patterns or package constraints.
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
 * Maps [UserControlOptions] to [ReportingDirectives].
 *
 * Translates user intentions into concrete reporting infrastructure instructions.
 * Crucially, it respects the [UserControlOptions.reportFormats] allowing for explicit
 * enabling/disabling of specific reporters (Console, HTML, JSON).
 */
fun UserControlOptions.toReportingDirectives(): ReportingDirectives =
    ReportingDirectives(
        baseReportDir = Paths.get("build", "reports", "kontrakt"),
        verbose = this.isVerbose,
        archiveMode = this.archiveMode,
        stackTraceLimit = this.stackTraceLimit,
        formats = this.reportFormats // Directly mapped from the Source of Truth
    )