package planning.domain.session

import planning.domain.exception.PlanningProtocolIntegrityException
import stage.input.material.MemberFact
import java.util.Arrays

/**
 * Primitive open-addressing tracker for hot-path uniqueness checks.
 *
 * This avoids boxed Long keys and generic-map churn in the traversal loop.
 */
internal class PrimitiveMemberTracker private constructor(
    capacity: Int,
) {
    private val cap: Int = nextPowerOfTwo(capacity.coerceAtLeast(16))
    private val mask: Int = cap - 1

    private val used = BooleanArray(cap)
    private val keys = LongArray(cap)
    private val values = arrayOfNulls<MemberFact>(cap)

    fun reset() {
        Arrays.fill(used, false)
        Arrays.fill(values, null)
    }

    fun findCollision(key: Long): MemberFact? {
        var idx = startIndex(key)
        var probes = 0

        while (used[idx]) {
            if (keys[idx] == key) {
                return values[idx]
            }
            idx = (idx + 1) and mask
            probes++
            if (probes > cap) {
                throw PlanningProtocolIntegrityException(
                    "PrimitiveMemberTracker probe overflow.",
                )
            }
        }
        return null
    }

    fun mark(
        key: Long,
        fact: MemberFact,
    ) {
        var idx = startIndex(key)
        var probes = 0

        while (used[idx]) {
            if (keys[idx] == key) {
                values[idx] = fact
                return
            }
            idx = (idx + 1) and mask
            probes++
            if (probes > cap) {
                throw PlanningProtocolIntegrityException(
                    "PrimitiveMemberTracker probe overflow.",
                )
            }
        }

        used[idx] = true
        keys[idx] = key
        values[idx] = fact
    }

    private fun startIndex(key: Long): Int {
        var h = key
        h = h xor (h ushr 33)
        h *= -0xae502812aa7333L
        h = h xor (h ushr 33)
        h *= -0x3b314601e57a13adL
        h = h xor (h ushr 33)
        return h.toInt() and mask
    }

    private fun nextPowerOfTwo(v: Int): Int {
        var n = v - 1
        n = n or (n ushr 1)
        n = n or (n ushr 2)
        n = n or (n ushr 4)
        n = n or (n ushr 8)
        n = n or (n ushr 16)
        return n + 1
    }

    companion object {
        @JvmStatic
        fun issue(capacity: Int): PrimitiveMemberTracker {
            if (capacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PrimitiveMemberTracker.capacity must be positive: $capacity",
                )
            }
            return PrimitiveMemberTracker(capacity)
        }
    }
}
