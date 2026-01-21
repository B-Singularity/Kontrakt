package discovery.domain.vo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DiscoveredTestTargetTest {

    // -- Factory Method: create --

    @Test
    fun `create - succeeds with valid arguments`() {
        val kClass = String::class
        val displayName = "StringTarget"
        val fqn = "java.lang.String"

        val result = DiscoveredTestTarget.create(kClass, displayName, fqn)

        assertThat(result.isSuccess).isTrue()
        val target = result.getOrThrow()
        assertThat(target.kClass).isEqualTo(kClass)
        assertThat(target.displayName).isEqualTo(displayName)
        assertThat(target.fullyQualifiedName).isEqualTo(fqn)
    }

    @Test
    fun `create - fails when displayName is empty`() {
        val result = DiscoveredTestTarget.create(String::class, "", "pkg.ClassName")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Display name cannot be blank")
    }

    @Test
    fun `create - fails when displayName is blank`() {
        val result = DiscoveredTestTarget.create(String::class, "   ", "pkg.ClassName")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Display name cannot be blank")
    }

    @Test
    fun `create - fails when fullyQualifiedName is empty`() {
        val result = DiscoveredTestTarget.create(String::class, "ValidName", "")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Fully qualified name cannot be blank")
    }

    @Test
    fun `create - fails when fullyQualifiedName is blank`() {
        val result = DiscoveredTestTarget.create(String::class, "ValidName", "   ")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Fully qualified name cannot be blank")
    }

    // -- Value Object Semantics --

    @Test
    fun `vo - verify equality based on content`() {
        val kClass = String::class
        val t1 = DiscoveredTestTarget.create(kClass, "A", "pkg.A").getOrThrow()
        val t2 = DiscoveredTestTarget.create(kClass, "A", "pkg.A").getOrThrow()
        val t3 = DiscoveredTestTarget.create(kClass, "B", "pkg.B").getOrThrow()

        assertThat(t1).isEqualTo(t2)
        assertThat(t1).isNotEqualTo(t3)
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode())
    }
}