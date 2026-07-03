package migration.quarantine

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Environment snapshot consumed by the runtime policy resolver.
 *
 * The Domain Core must never read these signals directly.
 * They exist only at the runtime-boundary / resolver layer.
 */
data class RuntimeEnvironment(
    val jvmVendor: String,
    val jvmVersion: String,
    val gcKind: GcKind,
    val cpuCount: Int,
    val memoryLimitBytes: Long?,
    val platformKind: PlatformKind,
) {
    init {
        if (cpuCount <= 0) {
            throw PlanningProtocolIntegrityException(
                "RuntimeEnvironment.cpuCount must be > 0: $cpuCount",
            )
        }
        if (memoryLimitBytes != null && memoryLimitBytes <= 0L) {
            throw PlanningProtocolIntegrityException(
                "RuntimeEnvironment.memoryLimitBytes must be null or > 0: $memoryLimitBytes",
            )
        }
    }
}

enum class GcKind {
    UNKNOWN,
    G1,
    ZGC,
    SHENANDOAH,
    PARALLEL,
    CMS,
}

enum class PlatformKind {
    SERVER_JVM,
    ANDROID_ART,
    OTHER,
}
