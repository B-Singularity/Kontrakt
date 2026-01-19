package discovery.domain.vo

/**
 * [Domain Value Object] Scan Scope
 *
 * Defines the boundaries for the test discovery process.
 * This sealed interface ensures that the scope is mutually exclusive and type-safe.
 */
sealed interface ScanScope {
    /**
     * Scans the entire classpath for valid contracts.
     */
    data object All : ScanScope

    /**
     * Restricts the scan to specific packages (including sub-packages).
     *
     * @property packageNames A set of package names to scan. Uses [Set] to prevent duplicate entries.
     */
    data class Packages(
        val packageNames: Set<String>,
    ) : ScanScope

    /**
     * Restricts the scan to specific fully qualified class names.
     *
     * @property classNames A set of class names to scan. Uses [Set] to prevent duplicate entries.
     */
    data class Classes(
        val classNames: Set<String>,
    ) : ScanScope
}
