package execution.domain.strategy

import discovery.api.DataContract
import kotlin.reflect.KClass

class ClassDataContractStrategy : ComplianceStrategy {
    override fun supports(kClass: KClass<*>): Boolean {
        return !kClass.java.isInterface && kClass.java.isAnnotationPresent(DataContract::class.java)
    }

    override fun decide(kClass: KClass<*>): StrategyResult {
        return StrategyResult.Proceed
    }
}