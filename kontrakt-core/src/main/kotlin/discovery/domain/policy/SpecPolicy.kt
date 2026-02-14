package discovery.domain.policy

/**
 * [Specification Policy]
 * Defines the Pure Domain Rules for identifying Kontrakt components.
 * Independent of any specific scanner implementation.
 */
object SpecPolicy {
    const val SCENARIO_ANNOTATION = "kontrakt.discovery.api.KontraktTest"
    const val CONTRACT_ANNOTATION = "kontrakt.discovery.api.Contract"
    const val DATA_CONTRACT_ANNOTATION = "kontrakt.discovery.api.DataContract"

    fun isScenario(hasAnnotation: (String) -> Boolean): Boolean =
        hasAnnotation(SCENARIO_ANNOTATION)

    fun isContract(hasAnnotation: (String) -> Boolean): Boolean =
        hasAnnotation(CONTRACT_ANNOTATION)

    fun isDataContract(hasAnnotation: (String) -> Boolean): Boolean =
        hasAnnotation(DATA_CONTRACT_ANNOTATION)
}