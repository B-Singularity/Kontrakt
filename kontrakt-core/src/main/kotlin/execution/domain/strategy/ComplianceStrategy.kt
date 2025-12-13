package execution.domain.strategy

import kotlin.reflect.KClass

sealed class StrategyResult {
    object Proceed : StrategyResult()
    object Skip : StrategyResult()
    data class Violation(val message: String) : StrategyResult()
}

interface ComplianceStrategy {
    fun supports(kClass: KClass<*>): Boolean
    fun decide(kClass: KClass<*>): StrategyResult
}