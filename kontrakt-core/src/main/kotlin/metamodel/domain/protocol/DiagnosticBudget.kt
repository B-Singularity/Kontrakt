package metamodel.domain.protocol

/**
 * Small bounded string rendering helper for metamodel diagnostics.
 *
 * This is not:
 *
 * - canonical encoding;
 * - fingerprint material;
 * - persistence format;
 * - or a logging framework.
 *
 * It only prevents diagnostic rendering from allocating unbounded strings when
 * deeply nested or wide metamodel structures are printed.
 *
 * Law:
 *
 * - all callers share the same truncation behavior;
 * - rendering stops once the budget is exhausted;
 * - truncation marker is deterministic;
 * - this class must not throw merely because rendering exceeds the budget.
 */
class DiagnosticBudget(
    private var remaining: Int,
) {
    init {
        if (remaining < 0) {
            remaining = 0
        }
    }

    fun hasRemaining(): Boolean {
        return remaining > 0
    }

    fun append(
        builder: StringBuilder,
        value: String,
    ) {
        if (remaining <= 0) return

        if (value.length <= remaining) {
            builder.append(value)
            remaining -= value.length
            return
        }

        if (remaining > TRUNCATION_MARKER.length) {
            val take = remaining - TRUNCATION_MARKER.length
            builder.append(value.substring(0, take))
            builder.append(TRUNCATION_MARKER)
            remaining = 0
            return
        }

        builder.append(value.substring(0, remaining))
        remaining = 0
    }

    companion object {
        const val TRUNCATION_MARKER: String = "...<truncated>"
    }
}