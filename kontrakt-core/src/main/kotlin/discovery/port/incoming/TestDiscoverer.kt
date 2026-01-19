package discovery.port.incoming

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveryPolicy
import kotlin.reflect.KClass

interface TestDiscoverer {
    /**
     * Performs a comprehensive scan to discover and generate [discovery.domain.aggregate.TestSpecification]s.
     *
     * The discovery process includes:
     * 1. **Contract Automation:** Finds interfaces marked with [contractMarker] and generates specifications for all their implementations.
     * 2. **Manual Scenarios:** Finds classes annotated with `@KontraktTest` and generates specifications for them.
     * 3. **Dependency Resolution:** Automatically scans for concrete implementations of interface/abstract dependencies to configure `Real` mocking strategies.
     *
     * This is a `suspend` function as classpath scanning is a blocking I/O operation. (See ADR-003)
     *
     * @param policy The policy defining discovery rules (scope, filters, etc.). [Modified]
     * @param contractMarker The annotation class used to identify a `Contract` interface.
     * @return A [Result] containing a list of all discovered [discovery.domain.aggregate.TestSpecification]s, or an exception if the scan fails.
     */
    suspend fun discover(
        policy: DiscoveryPolicy,
        contractMarker: KClass<out Annotation>,
    ): Result<List<TestSpecification>>
}
