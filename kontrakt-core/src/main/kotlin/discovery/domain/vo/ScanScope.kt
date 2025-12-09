package discovery.domain.vo

sealed interface ScanScope {
    data object All : ScanScope

    data class Packages(
        val packageNames: List<String>,
    ) : ScanScope

    data class Classes(
        val classNames: List<String>,
    ) : ScanScope
}
