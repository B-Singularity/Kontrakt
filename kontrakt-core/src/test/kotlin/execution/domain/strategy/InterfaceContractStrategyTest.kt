package execution.domain.strategy

import discovery.api.Contract

class InterfaceContractStrategyTest : ComplianceStrategyTest() {

    override val strategy = InterfaceContractStrategy()

    @Contract(verifyConstructors = true)
    interface ContractTrueInterface
    class ImplTrue : ContractTrueInterface

    @Contract(verifyConstructors = false)
    interface ContractFalseInterface
    class ImplFalse : ContractFalseInterface

    @Contract(verifyConstructors = true)
    class DirectTrue

    @Contract(verifyConstructors = false)
    class DirectFalse

    class NoContract
    interface JustInterface

    override fun provideSupportTestCases(): List<SupportTestCase> = listOf(
        SupportTestCase(ImplTrue::class, true),
        SupportTestCase(ImplFalse::class, true),
        SupportTestCase(DirectTrue::class, true),
        SupportTestCase(DirectFalse::class, true),
        SupportTestCase(ContractTrueInterface::class, false),
        SupportTestCase(NoContract::class, false),
        SupportTestCase(JustInterface::class, false)
    )

    override fun provideDecideTestCases(): List<DecideTestCase> = listOf(
        DecideTestCase(ImplTrue::class, StrategyResult.Proceed),
        DecideTestCase(DirectTrue::class, StrategyResult.Proceed),
        DecideTestCase(ImplFalse::class, StrategyResult.Skip),
        DecideTestCase(DirectFalse::class, StrategyResult.Skip),
        DecideTestCase(NoContract::class, StrategyResult.Skip)
    )
}