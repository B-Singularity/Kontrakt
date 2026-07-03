package stage.input.presentation.raw

/**
 * [Input Scope]
 * Defines the boundaries for the discovery process.
 */
sealed class ScanScope {
    data class Packages(
        val packageNames: List<String>,
    ) : ScanScope()

    data class Classes(
        val classNames: List<String>,
    ) : ScanScope()
}
