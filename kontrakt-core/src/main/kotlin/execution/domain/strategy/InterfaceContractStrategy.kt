package execution.domain.strategy

import discovery.api.Contract
import kotlin.reflect.KClass

class InterfaceContractStrategy : ComplianceStrategy {
    override fun supports(kClass: KClass<*>): Boolean = !kClass.java.isInterface && findContractAnnotation(kClass) != null

    override fun decide(kClass: KClass<*>): StrategyResult {
        val annotation = findContractAnnotation(kClass) ?: return StrategyResult.Skip

        return if (annotation.verifyConstructors) {
            StrategyResult.Proceed
        } else {
            StrategyResult.Skip
        }
    }

    private fun findContractAnnotation(kClass: KClass<*>): Contract? {
        if (kClass.java.isAnnotationPresent(Contract::class.java)) {
            return kClass.java.getAnnotation(Contract::class.java)
        }
        return kClass.java.interfaces
            .firstOrNull { it.isAnnotationPresent(Contract::class.java) }
            ?.getAnnotation(Contract::class.java)
    }
}
