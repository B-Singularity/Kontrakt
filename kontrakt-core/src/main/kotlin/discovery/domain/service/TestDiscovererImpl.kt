package discovery.domain.service

import discovery.api.KontraktTest
import discovery.api.Stateful
import discovery.api.TestDiscoverer
import discovery.domain.aggregate.TestSpecification
import discovery.domain.aggregate.TestSpecification.TestMode
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DependencyMetadata.MockingStrategy
import discovery.domain.vo.DiscoveredTestTarget
import discovery.domain.vo.ScanScope
import discovery.spi.ClasspathScanner
import exception.KontraktConfigurationException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class TestDiscovererImpl(
    private val scanner: ClasspathScanner,
) : TestDiscoverer {
    private val logger = KotlinLogging.logger {}

    override suspend fun discover(
        scope: ScanScope,
        contractMarker: KClass<out Annotation>,
    ): Result<List<TestSpecification>> =
        runCatching {
            logger.info { "Starting test discovery with scope: $scope" }

            withContext(Dispatchers.IO) {
                val contractSpecs =
                    scanner
                        .findAnnotatedInterfaces(scope, contractMarker)
                        .flatMap { contract ->
                            scanner
                                .findAllImplementations(scope, contract)
                                .map { impl -> impl to contract }
                        }.mapNotNull { (impl, contract) ->
                            createSpecificationForClass(impl, setOf(TestMode.ContractAuto(contract)))
                                .onFailure { e ->
                                    logger.warn { "Failed to create spec for implementation '${impl.simpleName}': ${e.message}" }
                                }.getOrNull()
                        }
                val manualSpecs =
                    scanner
                        .findAnnotatedClasses(scope, KontraktTest::class)
                        .mapNotNull { testClass ->
                            createSpecificationForClass(testClass, setOf(TestMode.UserScenario))
                                .onFailure { e ->
                                    logger.warn { "Failed to create spec for test class '${testClass.simpleName}': ${e.message}" }
                                }.getOrNull()
                        }

                (contractSpecs + manualSpecs)
                    .groupBy { it.target.fullyQualifiedName }
                    .map { (_, specs) ->
                        if (specs.size == 1) {
                            specs.first()
                        } else {
                            val base = specs.first()
                            val mergedModes = specs.flatMap { it.modes }.toSet()

                            TestSpecification.create(base.target, mergedModes, base.requiredDependencies).getOrThrow()
                        }
                    }
            }
        }

    private fun createSpecificationForClass(
        kClass: KClass<*>,
        initialModes: Set<TestMode>,
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

                val strategy = determineMockingStrategy(paramType)

                DependencyMetadata
                    .create(param.name ?: "unknown", paramType, strategy)
                    .getOrElse { return Result.failure(it) }
            }

        return TestSpecification.create(target, initialModes, dependencies)
    }

    private fun determineMockingStrategy(type: KClass<*>): MockingStrategy {
        if (type.qualifiedName == "java.time.Clock") {
            return MockingStrategy.Environment(DependencyMetadata.EnvType.TIME)
        }

        if (type.findAnnotation<Stateful>() != null) {
            return MockingStrategy.StatefulFake
        }

        return MockingStrategy.Real
    }
}
