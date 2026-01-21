package discovery.domain.aggregate

import discovery.domain.aggregate.TestSpecification.TestMode
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TestSpecificationTest {

    @Test
    fun `create - successfully builds specification with valid arguments`() {
        val target = mockk<DiscoveredTestTarget>()
        val modes = setOf(TestMode.UserScenario)
        val dependencies = listOf(mockk<DependencyMetadata>())
        val seed = 12345L

        val result = TestSpecification.create(target, modes, dependencies, seed)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow()
        assertThat(spec.target).isEqualTo(target)
        assertThat(spec.modes).isEqualTo(modes)
        assertThat(spec.requiredDependencies).isEqualTo(dependencies)
        assertThat(spec.seed).isEqualTo(seed)
    }

    @Test
    fun `create - fails when modes set is empty`() {
        val target = mockk<DiscoveredTestTarget>()
        val emptyModes = emptySet<TestMode>()
        val dependencies = emptyList<DependencyMetadata>()

        val result = TestSpecification.create(target, emptyModes, dependencies)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("At least one test mode is required.")
    }

    @Test
    fun `create - allows null seed`() {
        val target = mockk<DiscoveredTestTarget>()
        val modes = setOf(TestMode.UserScenario)

        // Test Factory's default argument logic
        val result = TestSpecification.create(target, modes, emptyList())

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().seed).isNull()
    }

    @Test
    fun `create - supports ContractAuto mode`() {
        val target = mockk<DiscoveredTestTarget>()
        val modes = setOf(TestMode.ContractAuto(String::class))

        val result = TestSpecification.create(target, modes, emptyList())

        assertThat(result.isSuccess).isTrue()
        val mode = result.getOrThrow().modes.first() as TestMode.ContractAuto
        assertThat(mode.contractInterface).isEqualTo(String::class)
    }

    @Test
    fun `create - supports DataCompliance mode`() {
        val target = mockk<DiscoveredTestTarget>()
        val modes = setOf(TestMode.DataCompliance(Int::class))

        val result = TestSpecification.create(target, modes, emptyList())

        assertThat(result.isSuccess).isTrue()
        val mode = result.getOrThrow().modes.first() as TestMode.DataCompliance
        assertThat(mode.dataClass).isEqualTo(Int::class)
    }

    @Test
    fun `value object semantics - equality, hashcode and copy`() {
        val target = mockk<DiscoveredTestTarget>()
        val dep = mockk<DependencyMetadata>()
        val modes = setOf(TestMode.UserScenario)

        val spec1 = TestSpecification.create(target, modes, listOf(dep), 1L).getOrThrow()
        val spec2 = TestSpecification.create(target, modes, listOf(dep), 1L).getOrThrow()
        val spec3 = TestSpecification.create(target, modes, listOf(dep), 2L).getOrThrow()

        // 1. Equals & HashCode
        assertThat(spec1).isEqualTo(spec2)
        assertThat(spec1).isNotEqualTo(spec3)
        assertThat(spec1.hashCode()).isEqualTo(spec2.hashCode())
        assertThat(spec1.toString()).isNotBlank()
        
    }
}