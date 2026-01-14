package discovery.domain.vo

/**
 * [Domain Value Object] Discovery Policy
 *
 * Encapsulates all rules regarding *which* tests should be discovered and selected for execution.
 * This policy is exclusively used during the "Discovery Phase" before the execution engine starts.
 */
data class DiscoveryPolicy(
    val scope: ScanScope = ScanScope.All
)