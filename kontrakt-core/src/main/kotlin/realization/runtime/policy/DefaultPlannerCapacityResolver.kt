package realization.runtime.policy

import migration.quarantine.PlannerCapacityResolver
import migration.quarantine.ResolvedPlannerSessionCaps
import migration.quarantine.ResolvedSessionBudget
import realization.runtime.policy.interning.ResolvedSizingCalibration
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.math.max

/**
 * Default deterministic implementation of PlannerCapacityResolver.
 *
 * This implementation follows the "primitive byte ledger" style:
 * - all arithmetic is performed in Long
 * - impossible layouts fail closed
 * - signature slab is reserved separately
 * - structural bytes are derived deterministically from already-resolved inputs
 */
class DefaultPlannerCapacityResolver private constructor(
    private val calibration: ResolvedSizingCalibration,
) : PlannerCapacityResolver {
    override fun resolve(budget: ResolvedSessionBudget): ResolvedPlannerSessionCaps {
        validateCalibration(calibration)

        val reservedSignatureBytes = reserveSignatureBytes(budget, calibration)
        if (reservedSignatureBytes < budget.maxSignatureLen) {
            throw PlanningProtocolIntegrityException(
                "Reserved signature bytes must be >= maxSignatureLen: reserved=$reservedSignatureBytes, maxSignatureLen=${budget.maxSignatureLen}",
            )
        }

        val structBudget =
            safeSubtract(
                safeSubtract(
                    budget.maxPlannerBytesPerWorker,
                    reservedSignatureBytes.toLong(),
                    "plannerBytes - signatureBytes",
                ),
                budget.fixedHeadroomBytes,
                "remainingStructBudget",
            )

        if (structBudget <= 0L) {
            throw PlanningProtocolIntegrityException(
                "Resolved structural budget must be > 0 after signature/headroom reservation: $structBudget",
            )
        }

        verifyMinimalLayoutFits(structBudget)

        var low = 1
        var high = 2_000_000
        var best: ResolvedPlannerSessionCaps? = null

        while (low <= high) {
            val nodeCap = low + ((high - low) ushr 1)
            val tableCap = nextPowerOfTwoChecked(max(8, safeMultiply(nodeCap, 2, "nodeCap * 2")))
            val desiredDepth = desiredDepth(nodeCap, calibration)

            val feasibleDepth =
                capDepthByBudget(
                    nodeCap = nodeCap,
                    tableCap = tableCap,
                    desiredDepth = desiredDepth,
                    structBudget = structBudget,
                )

            if (feasibleDepth <= 0) {
                high = nodeCap - 1
                continue
            }

            val undoCap = safeMultiply(depthCap = feasibleDepth, perDepth = calibration.undoRecordsPerDepth)
            val totalStruct =
                totalStructBytes(
                    nodeCap = nodeCap,
                    depthCap = feasibleDepth,
                    tableCap = tableCap,
                    undoCap = undoCap,
                )

            if (totalStruct <= structBudget) {
                best =
                    ResolvedPlannerSessionCaps(
                        maxNodeIdCap = nodeCap,
                        maxDepthCap = feasibleDepth,
                        indexerTableCap = tableCap,
                        undoLogCap = undoCap,
                        maxSignatureBytes = reservedSignatureBytes,
                        structBudgetBytes = structBudget,
                    )
                low = nodeCap + 1
            } else {
                high = nodeCap - 1
            }
        }

        return best ?: throw PlanningProtocolIntegrityException(
            "Minimal valid planner layout does not fit within resolved structural budget.",
        )
    }

    companion object {
        @JvmStatic
        fun issue(): DefaultPlannerCapacityResolver =
            DefaultPlannerCapacityResolver(
                calibration = ResolvedSizingCalibration(),
            )

        /**
         * Internal-only issuance path for tests or controlled runtime bootstrap.
         */
        internal fun issue(calibration: ResolvedSizingCalibration): DefaultPlannerCapacityResolver =
            DefaultPlannerCapacityResolver(calibration)
    }

    private fun validateCalibration(calibration: ResolvedSizingCalibration) {
        if (calibration.signatureReserveRatio <= 0.0 || calibration.signatureReserveRatio >= 1.0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSizingCalibration.signatureReserveRatio must be in (0, 1): ${calibration.signatureReserveRatio}",
            )
        }
        if (calibration.preferredDepthDivisor <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSizingCalibration.preferredDepthDivisor must be > 0: ${calibration.preferredDepthDivisor}",
            )
        }
        if (calibration.undoRecordsPerDepth <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSizingCalibration.undoRecordsPerDepth must be > 0: ${calibration.undoRecordsPerDepth}",
            )
        }
    }

    private fun reserveSignatureBytes(
        budget: ResolvedSessionBudget,
        calibration: ResolvedSizingCalibration,
    ): Int {
        val candidate = (budget.maxPlannerBytesPerWorker.toDouble() * calibration.signatureReserveRatio).toLong()
        val reserved = max(candidate, budget.maxSignatureLen.toLong())
        if (reserved > Int.MAX_VALUE.toLong()) {
            throw PlanningProtocolIntegrityException(
                "Reserved signature bytes exceed Int.MAX_VALUE: $reserved",
            )
        }
        return reserved.toInt()
    }

    private fun desiredDepth(
        nodeCap: Int,
        calibration: ResolvedSizingCalibration,
    ): Int {
        val raw = max(16, nodeCap / calibration.preferredDepthDivisor)
        return minOf(raw, Int.MAX_VALUE - 2)
    }

    private fun capDepthByBudget(
        nodeCap: Int,
        tableCap: Int,
        desiredDepth: Int,
        structBudget: Long,
    ): Int {
        var low = 1
        var high = desiredDepth
        var best = 0

        while (low <= high) {
            val depth = low + ((high - low) ushr 1)
            val undoCap = safeMultiply(depthCap = depth, perDepth = calibration.undoRecordsPerDepth)
            val total =
                totalStructBytes(
                    nodeCap = nodeCap,
                    depthCap = depth,
                    tableCap = tableCap,
                    undoCap = undoCap,
                )

            if (total <= structBudget) {
                best = depth
                low = depth + 1
            } else {
                high = depth - 1
            }
        }

        return best
    }

    private fun totalStructBytes(
        nodeCap: Int,
        depthCap: Int,
        tableCap: Int,
        undoCap: Int,
    ): Long {
        val denseNodeBytesPerNode = 12L

        val nodeIdIndexerTableBytes = safeMultiplyLong(tableCap.toLong(), 16L, "tableCap * 16")
        val nodeIdIndexerDenseBytes =
            safeMultiplyLong(nodeCap.toLong(), denseNodeBytesPerNode, "nodeCap * denseNodeBytes")

        val activeStackBytes = safeMultiplyLong(depthCap.toLong(), 4L, "depthCap * 4")
        val depthOfNodeIdBytes = safeMultiplyLong(nodeCap.toLong(), 4L, "nodeCap * 4")

        val incomingEdgeRankAtDepthBytes = safeMultiplyLong(depthCap.toLong(), 8L, "depthCap * 8")
        val floorLog2Bytes = safeMultiplyLong(depthCap.toLong(), 4L, "depthCap * 4")

        val logDepth = floorLog2(depthCap) + 1
        val flatMinEdgeRankUpBytes =
            safeMultiplyLong(
                safeMultiplyLong(depthCap.toLong(), logDepth.toLong(), "depthCap * logDepth"),
                8L,
                "flatMinEdgeRankUp",
            )
        val flatArgminUpBytes =
            safeMultiplyLong(
                safeMultiplyLong(depthCap.toLong(), logDepth.toLong(), "depthCap * logDepth"),
                4L,
                "flatArgminUp",
            )

        val undoLogBytes = safeMultiplyLong(undoCap.toLong(), 24L, "undoCap * 24")

        return safeAddAll(
            nodeIdIndexerTableBytes,
            nodeIdIndexerDenseBytes,
            activeStackBytes,
            depthOfNodeIdBytes,
            incomingEdgeRankAtDepthBytes,
            floorLog2Bytes,
            flatMinEdgeRankUpBytes,
            flatArgminUpBytes,
            undoLogBytes,
        )
    }

    private fun verifyMinimalLayoutFits(structBudget: Long) {
        val minimalNodeCap = 1
        val minimalDepthCap = 1
        val minimalTableCap = 8
        val minimalUndoCap = calibration.undoRecordsPerDepth

        val minimal =
            totalStructBytes(
                nodeCap = minimalNodeCap,
                depthCap = minimalDepthCap,
                tableCap = minimalTableCap,
                undoCap = minimalUndoCap,
            )

        if (minimal > structBudget) {
            throw PlanningProtocolIntegrityException(
                "Minimal valid planner layout does not fit: required=$minimal, available=$structBudget",
            )
        }
    }

    private fun floorLog2(v: Int): Int {
        if (v <= 0) {
            throw PlanningProtocolIntegrityException("floorLog2 input must be > 0: $v")
        }
        return 31 - Integer.numberOfLeadingZeros(v)
    }

    private fun nextPowerOfTwoChecked(v: Int): Int {
        if (v <= 0) {
            throw PlanningProtocolIntegrityException("nextPowerOfTwo input must be > 0: $v")
        }
        if (v > (1 shl 30)) {
            throw PlanningProtocolIntegrityException(
                "nextPowerOfTwo would overflow or produce invalid capacity: $v",
            )
        }
        var n = v - 1
        n = n or (n ushr 1)
        n = n or (n ushr 2)
        n = n or (n ushr 4)
        n = n or (n ushr 8)
        n = n or (n ushr 16)
        return n + 1
    }

    private fun safeMultiply(
        a: Int,
        b: Int,
        label: String,
    ): Int {
        val result = a.toLong() * b.toLong()
        if (result > Int.MAX_VALUE.toLong() || result < Int.MIN_VALUE.toLong()) {
            throw PlanningProtocolIntegrityException("Int overflow while computing $label: $a * $b")
        }
        return result.toInt()
    }

    private fun safeMultiply(
        depthCap: Int,
        perDepth: Int,
    ): Int = safeMultiply(depthCap, perDepth, "depthCap * perDepth")

    private fun safeMultiplyLong(
        a: Long,
        b: Long,
        label: String,
    ): Long {
        if (a == 0L || b == 0L) return 0L
        val result = a * b
        if (result / b != a) {
            throw PlanningProtocolIntegrityException("Long overflow while computing $label: $a * $b")
        }
        return result
    }

    private fun safeSubtract(
        a: Long,
        b: Long,
        label: String,
    ): Long {
        val result = a - b
        if ((b > 0 && result > a) || (b < 0 && result < a)) {
            throw PlanningProtocolIntegrityException("Long overflow while computing $label: $a - $b")
        }
        return result
    }

    private fun safeAddAll(vararg values: Long): Long {
        var sum = 0L
        for (value in values) {
            val next = sum + value
            if ((value > 0 && next < sum) || (value < 0 && next > sum)) {
                throw PlanningProtocolIntegrityException("Long overflow while summing primitive byte ledger.")
            }
            sum = next
        }
        return sum
    }
}
