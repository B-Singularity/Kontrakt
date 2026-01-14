package execution.domain.vo

/**
 * [Domain Aggregate] Execution Policy
 *
 * Defines the immutable rules for *how* the discovered tests should be executed.
 * The configuration is structured into logical groups to separate concerns.
 */
data class ExecutionPolicy(
    /**
     * [Control] Reproducibility settings (e.g., Random Seed).
     */
    val determinism: DeterminismPolicy = DeterminismPolicy.DEFAULT,

    /**
     * [Observability] Logging and Auditing settings (Retention, Depth).
     */
    val auditing: AuditPolicy = AuditPolicy.DEFAULT,

    /**
     * [Infrastructure] Resource limits and concurrency settings.
     */
    val resources: ResourcePolicy = ResourcePolicy.DEFAULT
) {
    companion object {
        val DEFAULT = ExecutionPolicy()
    }
}

/**
 * Controls the randomness and reproducibility of the test execution.
 */
data class DeterminismPolicy(
    /**
     * The seed for random number generation.
     * If null, the engine generates a new random seed for every run.
     */
    val seed: Long? = null
) {
    companion object {
        val DEFAULT = DeterminismPolicy()
    }
}

/**
 * Controls the verbosity and retention of audit logs.
 */
data class AuditPolicy(
    /**
     * Determines which log files to keep on disk.
     */
    val retention: LogRetention = LogRetention.ON_FAILURE,

    /**
     * Determines the level of detail written to the log files.
     */
    val depth: AuditDepth = AuditDepth.SIMPLE
) {
    companion object {
        val DEFAULT = AuditPolicy()
    }
}

/**
 * Controls system resource usage during execution.
 */
data class ResourcePolicy(
    /**
     * The maximum time (in milliseconds) allowed for a single test scenario.
     */
    val timeoutMs: Long = 5000L,

    /**
     * The number of parallel workers. 1 implies sequential execution.
     */
    val parallelism: Int = 1
) {
    companion object {
        val DEFAULT = ResourcePolicy()
    }
}