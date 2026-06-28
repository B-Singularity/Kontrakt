package stage.input.contract

/**
 * [Specification Validator]
 * Pure Domain Rules for structural correctness of Kontrakt components.
 * Independent of any specific scanner implementation (ClassGraph/KSP).
 *
 * Returns a [SpecViolationCode] if invalid, or null if valid.
 */
object SpecValidator {
    fun validateContract(isInterface: Boolean): SpecViolationCode? {
        if (!isInterface) {
            return SpecViolationCode.CONTRACT_MUST_BE_INTERFACE
        }
        return null
    }

    fun validateDataContract(isConcrete: Boolean): SpecViolationCode? {
        if (!isConcrete) {
            return SpecViolationCode.DATA_CONTRACT_MUST_BE_CONCRETE
        }
        return null
    }
}
