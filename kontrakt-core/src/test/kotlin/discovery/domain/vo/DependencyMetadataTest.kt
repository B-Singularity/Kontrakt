package discovery.domain.vo

import discovery.domain.vo.DependencyMetadata.EnvType
import discovery.domain.vo.DependencyMetadata.MockingStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DependencyMetadataTest {

    // -- Factory Method: create --

    @Test
    fun `create - succeeds with valid arguments`() {
        val name = "repository"
        val type = String::class
        val strategy = MockingStrategy.StatelessMock

        val result = DependencyMetadata.create(name, type, strategy)

        assertThat(result.isSuccess).isTrue()
        val metadata = result.getOrThrow()
        assertThat(metadata.name).isEqualTo(name)
        assertThat(metadata.type).isEqualTo(type)
        assertThat(metadata.strategy).isEqualTo(strategy)
    }

    @Test
    fun `create - fails when name is empty`() {
        val result = DependencyMetadata.create("", String::class, MockingStrategy.StatelessMock)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Name cannot be blank")
    }

    @Test
    fun `create - fails when name contains only whitespace`() {
        val result = DependencyMetadata.create("   ", String::class, MockingStrategy.StatelessMock)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Name cannot be blank")
    }

    // -- Mocking Strategy Variants --

    @Test
    fun `strategy - supports StatelessMock`() {
        val result = DependencyMetadata.create("dep", Int::class, MockingStrategy.StatelessMock)

        assertThat(result.getOrThrow().strategy)
            .isEqualTo(MockingStrategy.StatelessMock)
    }

    @Test
    fun `strategy - supports StatefulFake`() {
        val result = DependencyMetadata.create("dep", Int::class, MockingStrategy.StatefulFake)

        assertThat(result.getOrThrow().strategy)
            .isEqualTo(MockingStrategy.StatefulFake)
    }

    @Test
    fun `strategy - supports Environment with specific EnvType`() {
        val envStrategy = MockingStrategy.Environment(EnvType.TIME)

        val result = DependencyMetadata.create("clock", Long::class, envStrategy)

        val strategy = result.getOrThrow().strategy as MockingStrategy.Environment
        assertThat(strategy.type).isEqualTo(EnvType.TIME)
    }

    @Test
    fun `strategy - supports Real with implementation class`() {
        val realStrategy = MockingStrategy.Real(implementation = ArrayList::class)

        val result = DependencyMetadata.create("list", List::class, realStrategy)

        val strategy = result.getOrThrow().strategy as MockingStrategy.Real
        assertThat(strategy.implementation).isEqualTo(ArrayList::class)
    }

    // -- Value Object Semantics --

    @Test
    fun `vo - verify equality based on content`() {
        val strategy = MockingStrategy.StatelessMock
        val meta1 = DependencyMetadata.create("repo", String::class, strategy).getOrThrow()
        val meta2 = DependencyMetadata.create("repo", String::class, strategy).getOrThrow()
        val meta3 = DependencyMetadata.create("service", String::class, strategy).getOrThrow()

        // Content-based equality check
        assertThat(meta1).isEqualTo(meta2)
        assertThat(meta1).isNotEqualTo(meta3)
        assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode())
    }
}