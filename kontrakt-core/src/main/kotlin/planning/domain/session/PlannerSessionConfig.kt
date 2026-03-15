package planning.domain.session

import planning.domain.exception.PlanningProtocolException
import planning.domain.exception.PlanningProtocolIntegrityException
import kotlin.math.ln

/**
 * Constitutional session configuration + capacity law.
 *
 * This is a protocol-governed runtime configuration object, not a tuning bag.
 *
 * Key properties:
 * - plain class + private constructor + factory issuance
 * - deterministic reverse-calculation of dense capacities from maxPlannerBytes
 * - version tuple carried explicitly for cache-key stability and protocol traceability
 *
 * Notes:
 * - reverse-calc remains SSOT
 * - numeric limits are policy defaults
 * - environment-aware auto-sizing may be layered above this config later
 */
class PlannerSessionConfig private constructor(
    // Version tuple
    val normalizationSpecVersion: Long,
    val edgeOrderingVersion: Long,
    val capabilityProfileVersion: Long,
    val workAccountingVersion: Long,
    val entropyVersion: Long,
    val entropySeed: Long,

    // Runtime limits
    val maxPlannerBytes: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,

    // DoS / protocol limits
    val maxSignatureLen: Int,
    val signatureMemoryRatio: Double,

    // Derived caps
    val maxNodeIdCap: Int,
    val indexerTableCap: Int,
    val undoLogCap: Int,
    val maxSemanticDepth: Int,
    val maxSignatureBytes: Int,
) {

    companion object {

        /**
         * The minimum overhead reserved for the Session object itself,
         * basic stack space, and JVM alignment padding.
         */
        private const val FIXED_SESSION_HEADROOM_BYTES = 16_384L // 16KB

        /**
         * Canonical issuance path.
         *
         * Performs strict validation and deterministic capacity reverse-calculation.
         */
        @JvmStatic
        fun issue(
            normalizationSpecVersion: Long = 1L,
            edgeOrderingVersion: Long = 1L,
            capabilityProfileVersion: Long = 1L,
            workAccountingVersion: Long = 1L,
            entropyVersion: Long = 1L,
            entropySeed: Long = 0L,
            maxPlannerBytes: Long = 10L * 1024L * 1024L,
            maxPhysicalSteps: Int = 1_000_000,
            maxSemanticWorkUnits: Int = 100_000,
            maxSignatureLen: Int = 8192,
            signatureMemoryRatio: Double = 0.2,
        ): PlannerSessionConfig {
            validateInputs(
                normalizationSpecVersion = normalizationSpecVersion,
                edgeOrderingVersion = edgeOrderingVersion,
                capabilityProfileVersion = capabilityProfileVersion,
                workAccountingVersion = workAccountingVersion,
                entropyVersion = entropyVersion,
                maxPlannerBytes = maxPlannerBytes,
                maxPhysicalSteps = maxPhysicalSteps,
                maxSemanticWorkUnits = maxSemanticWorkUnits,
                maxSignatureLen = maxSignatureLen,
                signatureMemoryRatio = signatureMemoryRatio,
            )

            val maxSignatureBytes = (maxPlannerBytes * signatureMemoryRatio).toInt()
            val structBytesAvailable = maxPlannerBytes - maxSignatureBytes - FIXED_SESSION_HEADROOM_BYTES

            var low = 1
            var high = 2_000_000
            var bestNodeCap = 0
            var bestTableCap = 0
            var bestUndoCap = 0
            var bestDepth = 0

            while (low <= high) {
                val midNodeCap = (low + high) / 2
                val targetDepth = (midNodeCap / 10).coerceIn(256, 4096)
                val tableCap = nextPowerOfTwo((midNodeCap * 2).coerceAtLeast(1024))
                val undoCap = targetDepth * 8

                val tableBytes = tableCap.toLong() * 16L
                val nodeBytes = midNodeCap.toLong() * 24L

                val logDepth = (ln(targetDepth.toDouble()) / ln(2.0)).toInt() + 2
                val flatSize = targetDepth * logDepth
                val rmqBytes = (targetDepth * 12L) + (flatSize.toLong() * 12L)

                val undoBytes = undoCap.toLong() * 24L
                val stackBytes = targetDepth.toLong() * 4L

                val total = tableBytes + nodeBytes + rmqBytes + undoBytes + stackBytes

                if (total <= structBytesAvailable) {
                    bestNodeCap = midNodeCap
                    bestTableCap = tableCap
                    bestUndoCap = undoCap
                    bestDepth = targetDepth
                    low = midNodeCap + 1
                } else {
                    high = midNodeCap - 1
                }
            }

            if (bestNodeCap == 0) {
                throw PlanningProtocolException(
                    "Insufficient memory for minimal planner session."
                )
            }

            return PlannerSessionConfig(
                normalizationSpecVersion = normalizationSpecVersion,
                edgeOrderingVersion = edgeOrderingVersion,
                capabilityProfileVersion = capabilityProfileVersion,
                workAccountingVersion = workAccountingVersion,
                entropyVersion = entropyVersion,
                entropySeed = entropySeed,
                maxPlannerBytes = maxPlannerBytes,
                maxPhysicalSteps = maxPhysicalSteps,
                maxSemanticWorkUnits = maxSemanticWorkUnits,
                maxSignatureLen = maxSignatureLen,
                signatureMemoryRatio = signatureMemoryRatio,
                maxNodeIdCap = bestNodeCap,
                indexerTableCap = bestTableCap,
                undoLogCap = bestUndoCap,
                maxSemanticDepth = bestDepth,
                maxSignatureBytes = maxSignatureBytes,
            )
        }

        private fun validateInputs(
            normalizationSpecVersion: Long,
            edgeOrderingVersion: Long,
            capabilityProfileVersion: Long,
            workAccountingVersion: Long,
            entropyVersion: Long,
            maxPlannerBytes: Long,
            maxPhysicalSteps: Int,
            maxSemanticWorkUnits: Int,
            maxSignatureLen: Int,
            signatureMemoryRatio: Double,
        ) {
            if (normalizationSpecVersion < 0L) {
                throw PlanningProtocolIntegrityException("normalizationSpecVersion must be >= 0")
            }
            if (edgeOrderingVersion < 0L) {
                throw PlanningProtocolIntegrityException("edgeOrderingVersion must be >= 0")
            }
            if (capabilityProfileVersion < 0L) {
                throw PlanningProtocolIntegrityException("capabilityProfileVersion must be >= 0")
            }
            if (workAccountingVersion < 0L) {
                throw PlanningProtocolIntegrityException("workAccountingVersion must be >= 0")
            }
            if (entropyVersion < 0L) {
                throw PlanningProtocolIntegrityException("entropyVersion must be >= 0")
            }
            if (maxPlannerBytes <= 0L) {
                throw PlanningProtocolIntegrityException("maxPlannerBytes must be > 0")
            }
            if (maxPhysicalSteps <= 0) {
                throw PlanningProtocolIntegrityException("maxPhysicalSteps must be > 0")
            }
            if (maxSemanticWorkUnits <= 0) {
                throw PlanningProtocolIntegrityException("maxSemanticWorkUnits must be > 0")
            }
            if (maxSignatureLen <= 0) {
                throw PlanningProtocolIntegrityException("maxSignatureLen must be > 0")
            }
            if (signatureMemoryRatio <= 0.0 || signatureMemoryRatio >= 1.0) {
                throw PlanningProtocolIntegrityException(
                    "signatureMemoryRatio must be in (0, 1)"
                )
            }
        }

        private fun nextPowerOfTwo(v: Int): Int {
            var n = v - 1
            n = n or (n ushr 1)
            n = n or (n ushr 2)
            n = n or (n ushr 4)
            n = n or (n ushr 8)
            n = n or (n ushr 16)
            return if (n < 0) 1 else n + 1
        }
    }
}