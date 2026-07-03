package realization.runtime.policy

import migration.quarantine.RuntimePolicyEpoch
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe registry for publishing immutable runtime-policy snapshots.
 *
 * Semantics:
 * - snapshots are installed only at stable policy-resolution boundaries
 * - sessions pin one snapshot at bootstrap and keep using it for their lifetime
 * - reads are lock-free
 * - writes are serialized
 * - stale epochs are rejected
 * - duplicate delivery of the exact same epoch is a benign no-op
 *
 * Normative alignment:
 * - policy resolution occurs outside the Domain Core
 * - already-running sessions must not observe mid-session policy drift
 * - resolved policy snapshots are immutable
 */
class RuntimePolicyRegistry(
    initial: RuntimePolicyEpoch,
) {
    private val ref = AtomicReference(initial)

    fun currentEpoch(): RuntimePolicyEpoch = ref.get()

    @Synchronized
    fun install(next: RuntimePolicyEpoch): PolicyInstallResult {
        val previous = ref.get()

        if (next.id < previous.id) {
            throw PlanningProtocolIntegrityException(
                "RuntimePolicyEpoch integrity violation: attempted to install stale epoch. " +
                        "current=${previous.id}, rejected=${next.id}",
            )
        }

        if (next.id == previous.id) {
            if (next == previous) {
                return PolicyInstallResult.alreadyCurrent(previous)
            }

            throw PlanningProtocolIntegrityException(
                "RuntimePolicyEpoch integrity violation: same epoch id carries a different payload. " +
                        "id=${next.id}",
            )
        }

        ref.set(next)
        return PolicyInstallResult.installed(next)
    }
}

/**
 * Result of a registry installation attempt.
 *
 * Integrity violations are surfaced as custom exceptions.
 * This result type is intentionally non-forgeable outside this file:
 * only the registry may create concrete result instances.
 */
sealed interface PolicyInstallResult {
    val epoch: RuntimePolicyEpoch
    val installed: Boolean

    companion object {
        internal fun installed(epoch: RuntimePolicyEpoch): PolicyInstallResult = InstalledImpl(epoch)

        internal fun alreadyCurrent(epoch: RuntimePolicyEpoch): PolicyInstallResult = AlreadyCurrentImpl(epoch)
    }
}

private class InstalledImpl(
    override val epoch: RuntimePolicyEpoch,
) : PolicyInstallResult {
    override val installed: Boolean = true

    override fun toString(): String = "PolicyInstallResult.Installed(epoch=${epoch.id})"
}

private class AlreadyCurrentImpl(
    override val epoch: RuntimePolicyEpoch,
) : PolicyInstallResult {
    override val installed: Boolean = false

    override fun toString(): String = "PolicyInstallResult.AlreadyCurrent(epoch=${epoch.id})"
}
