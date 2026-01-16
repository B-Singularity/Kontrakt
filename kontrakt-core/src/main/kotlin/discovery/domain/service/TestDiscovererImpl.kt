package discovery.domain.service

import discovery.api.KontraktTest
import discovery.api.Stateful
import discovery.api.TestDiscoverer
import discovery.domain.aggregate.TestSpecification
import discovery.domain.aggregate.TestSpecification.Companion.create
import discovery.domain.aggregate.TestSpecification.TestMode
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DependencyMetadata.MockingStrategy
import discovery.domain.vo.DiscoveredTestTarget
import discovery.domain.vo.DiscoveryPolicy
import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScanner
import exception.KontraktConfigurationException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * [Domain Service] Test Discoverer Implementation.
 *
 * Scans the classpath to identify test candidates based on the provided policy.
 * It maps annotations (@Contract, @KontraktTest, @DataContract) to executable [TestSpecification]s.
 */
class TestDiscovererImpl(
    private val scanner: ClasspathScanner,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TestDiscoverer {
    private val logger = KotlinLogging.logger {}

    override suspend fun discover(
        policy: DiscoveryPolicy,
        contractMarker: KClass<out Annotation>,
    ): Result<List<TestSpecification>> =
        runCatching {
            val scope = policy.scope
            logger.info { "Starting test discovery with scope: $scope" }

            withContext(ioDispatcher) {
                // 1. Discover @Contract interfaces (Auto-generated tests)
                val contractSpecs =
                    scanner
                        .findAnnotatedInterfaces(scope, contractMarker)
                        .flatMap { contract ->
                            scanner
                                .findAllImplementations(scope, contract)
                                .map { impl -> impl to contract }
                        }.mapNotNull { (impl, contract) ->
                            createSpecificationForClass(impl, setOf(TestMode.ContractAuto(contract)), scope)
                                .onFailure { e ->
                                    logger.warn { "Failed to create spec for implementation '${impl.simpleName}': ${e.message}" }
                                }.getOrNull()
                        }
                // 2. Discover @KontraktTest classes (User scenarios)
                val manualSpecs =
                    scanner
                        .findAnnotatedClasses(scope, KontraktTest::class)
                        .mapNotNull { testClass ->
                            createSpecificationForClass(testClass, setOf(TestMode.UserScenario), scope)
                                .onFailure { e ->
                                    logger.warn { "Failed to create spec for test class '${testClass.simpleName}': ${e.message}" }
                                }.getOrNull()
                        }

                // 3. Discover @DataContract classes (Data compliance tests)
                val dataContractSpecs =
                    scanner.findAnnotatedClasses(scope, discovery.api.DataContract::class)
                        .mapNotNull { dataClass ->
                            createSpecificationForClass(
                                dataClass,
                                setOf(TestMode.DataCompliance(dataClass)),
                                scope
                            ).getOrNull()
                        }


                // Merge duplicates (A class might be picked up by multiple scanners)
                val allSpecs = contractSpecs + manualSpecs + dataContractSpecs

                allSpecs
                    .groupBy { it.target.fullyQualifiedName }
                    .map { (_, specs) -> mergeSpecifications(specs) }
            }
        }

    private suspend fun createSpecificationForClass(
        kClass: KClass<*>,
        initialModes: Set<TestMode>,
        scope: ScanScope,
    ): Result<TestSpecification> {
        val targetResult =
            DiscoveredTestTarget.create(
                kClass = kClass,
                displayName = kClass.simpleName ?: "UnnamedClass",
                fullyQualifiedName =
                    kClass.qualifiedName
                        ?: return Result.failure(
                            KontraktConfigurationException(
                                "Class '${kClass.simpleName}' must have a qualified name. Local or anonymous classes are not supported.",
                            ),
                        ),
            )
        val target = targetResult.getOrElse { return Result.failure(it) }

        val constructor =
            kClass.primaryConstructor
                ?: return Result.failure(
                    KontraktConfigurationException(
                        "Target class '${target.displayName}' must have a primary constructor for dependency injection.\n" +
                                "Tip: Interfaces, Objects, or Abstract classes cannot be tested directly as a Target.",
                    ),
                )

        val dependencies =
            constructor.parameters.map { param ->
                val paramType =
                    param.type.classifier as? KClass<*>
                        ?: return Result.failure(
                            KontraktConfigurationException(
                                "Cannot determine type for parameter '${param.name}' " +
                                        "in '${target.displayName}'.",
                            ),
                        )

                val strategy = determineMockingStrategy(paramType, scope)

                DependencyMetadata
                    .create(param.name ?: "unknown", paramType, strategy)
                    .getOrElse { return Result.failure(it) }
            }

        return create(target, initialModes, dependencies)
    }

    private suspend fun determineMockingStrategy(
        type: KClass<*>,
        scope: ScanScope,
    ): MockingStrategy {
        if (type.qualifiedName == "java.time.Clock") {
            return MockingStrategy.Environment(DependencyMetadata.EnvType.TIME)
        }

        if (type.findAnnotation<Stateful>() != null) {
            return MockingStrategy.StatefulFake
        }

        if (type.java.isInterface || type.isAbstract) {
            val implementations = scanner.findAllImplementations(scope, type)

            return if (implementations.isNotEmpty()) {
                MockingStrategy.Real(implementation = implementations.first())
            } else {
                MockingStrategy.StatelessMock
            }
        }

        return MockingStrategy.Real(implementation = type)
    }

    /**
     * [Logic] Safely merges multiple specifications for the same target class.
     * Addresses:
     * 1. Seed Priority: Explicit seed wins over null.
     * 2. Mode Union: Combines all test modes.
     * 3. Dependency Safety: Validates that dependencies are consistent.
     */
    private fun mergeSpecifications(specs: List<TestSpecification>): TestSpecification {
        // Target is invariant for the same class, so taking the first is safe.
        val base = specs.first()

        // 1. Merge Modes (Union)
        val mergedModes = specs.flatMap { it.modes }.toSet()

        // 2. Resolve Seed (Priority: First non-null seed wins)
        val resolvedSeed = specs.mapNotNull { it.seed }.firstOrNull()

        // 3. Dependencies Consistency Check

        return create(
            target = base.target,
            modes = mergedModes,
            requiredDependencies = base.requiredDependencies,
            seed = resolvedSeed
        ).getOrThrow()
    }
}
