package discovery.api

import discovery.domain.aggregate.TestSpecification
import kotlin.reflect.KClass

interface TestDiscoverer {
    /**
     * Scans a root package for `Contracts` and creates a `TestSpecification` for each implementation found.
     *
     * This is a `suspend` function as classpath scanning is a blocking I/O operation. (See ADR-003)
     *
     * @param rootPackage The root package to begin the scan from (e.g., "com.example").
     * @param contractMarker The annotation class used to identify a `Contract` interface.
     * @return A `Result` containing a list of all discovered `TestSpecification`s, or an exception if the scan fails.
     */
    suspend fun discover(
        rootPackage: String,
        contractMarker: KClass<out Annotation>,
    ): Result<List<TestSpecification>>
}
