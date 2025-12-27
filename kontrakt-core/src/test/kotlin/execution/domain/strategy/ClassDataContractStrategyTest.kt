package execution.domain.strategy

import discovery.api.Contract
import discovery.api.DataContract

class ClassDataContractStrategyTest : ComplianceStrategyTest() {
    override val strategy = ClassDataContractStrategy()

    @DataContract
    data class ValidData(
        val id: Int,
    )

    @DataContract
    class ValidNormalClass

    @DataContract
    interface InvalidInterface

    class PlainClass

    @Contract
    class ContractButNoData

    override fun provideSupportTestCases(): List<SupportTestCase> =
        listOf(
            SupportTestCase(ValidData::class, true),
            SupportTestCase(ValidNormalClass::class, true),
            SupportTestCase(InvalidInterface::class, false),
            SupportTestCase(PlainClass::class, false),
            SupportTestCase(ContractButNoData::class, false),
        )

    override fun provideDecideTestCases(): List<DecideTestCase> =
        listOf(
            DecideTestCase(ValidData::class, StrategyResult.Proceed),
            DecideTestCase(ValidNormalClass::class, StrategyResult.Proceed),
        )
}
