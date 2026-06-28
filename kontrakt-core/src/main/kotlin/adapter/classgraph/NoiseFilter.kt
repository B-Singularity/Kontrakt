package adapter.classgraph

import io.github.classgraph.ClassInfo

/**
 * [Noise Filtration Adapter]
 * Filters out compiler artifacts, synthetic classes, and non-target candidates.
 * Depends on 'ClassGraph' types, so it belongs in the Adapter layer.
 *
 * ## Heuristics
 * - Blocks synthetic/anonymous classes via flags.
 * - Blocks local classes via JVM naming patterns (`$1`, `$2`...).
 */
internal object NoiseFilter {
    // JVM Compiler Artifact Pattern:
    // Captures anonymous inner classes ($1) and local classes ($1Local).
    // User defined classes cannot usually match this pattern in source code.
    private val LOCAL_OR_ANON_PATTERN = Regex("""\$\d+""")

    fun isFundamentalNoise(info: ClassInfo): Boolean {
        // 1. Basic Flags
        if (info.isSynthetic) return true
        if (info.isAnonymousInnerClass) return true

        val name = info.name

        // 2. Naming Pattern (Local/Anonymous Class fallback)
        // Solves the missing 'isLocalClass' API issue by checking for "$<digits>"
        if (LOCAL_OR_ANON_PATTERN.containsMatchIn(name)) return true

        // 3. Kotlin/Compiler Specific Artifacts
        if (name.endsWith("\$DefaultImpls")) return true
        if (name.contains("\$WhenMappings")) return true

        // [Platform] Filter Lambda artifacts safely.
        if (name.contains("\$\$Lambda") || name.contains("\$Lambda\$")) return true

        return false
    }

    fun isHeuristicNoise(info: ClassInfo): Boolean {
        val name = info.name
        if (name.endsWith("\$Companion")) return true
        return false
    }
}
