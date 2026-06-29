package realization.execution.strategy

import stage.input.contract.DataContract
import kotlin.reflect.KClass

class ClassDataContractStrategy : ComplianceStrategy {
    override fun supports(kClass: KClass<*>): Boolean =
        !kClass.java.isInterface && kClass.java.isAnnotationPresent(DataContract::class.java)

    override fun decide(kClass: KClass<*>): StrategyResult = StrategyResult.Proceed
}
