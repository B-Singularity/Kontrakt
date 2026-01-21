package discovery.domain.vo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DiscoveryPolicyTest {

    @Test
    fun `create - uses default scope when not provided`() {
        val policy = DiscoveryPolicy()

        assertThat(policy.scope).isEqualTo(ScanScope.All)
    }

    @Test
    fun `create - uses provided scope`() {
        val customScope = ScanScope.Packages(setOf("com.example"))

        val policy = DiscoveryPolicy(scope = customScope)

        assertThat(policy.scope).isEqualTo(customScope)
    }
}