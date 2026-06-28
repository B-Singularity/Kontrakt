package stage.input.contract

/**
 * [Specification Policy]
 * Defines the Pure Domain Rules for identifying Kontrakt components.
 * Independent of any specific scanner implementation.
 */
object SpecPolicy {
    const val SCENARIO_ANNOTATION = "kontrakt.stage.input.contract.KontraktTest"
    const val CONTRACT_ANNOTATION = "kontrakt.stage.input.contract.Contract"
    const val DATA_CONTRACT_ANNOTATION = "kontrakt.stage.input.contract.DataContract"

    fun isScenario(hasAnnotation: (String) -> Boolean): Boolean = hasAnnotation(SCENARIO_ANNOTATION)

    fun isContract(hasAnnotation: (String) -> Boolean): Boolean = hasAnnotation(CONTRACT_ANNOTATION)

    fun isDataContract(hasAnnotation: (String) -> Boolean): Boolean = hasAnnotation(DATA_CONTRACT_ANNOTATION)
}
