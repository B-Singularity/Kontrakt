package discovery.domain.service

import discovery.api.TestDiscoverer
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import discovery.spi.ClasspathScanner
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TestDiscovererImpl(
    private val scanner: ClasspathScanner,
) : TestDiscoverer {
    private val logger = KotlinLogging.logger {}

    override suspend fun discover(
        rootPackage: String,
        contractMarker: KClass<out Annotation>,
    ): Result<List<TestSpecification>> =
        runCatching {
            logger.info { "Starting test discovery for root package: $rootPackage"}

            val implementationClasses =
                withContext(Dispatchers.IO) {
                    val contractInterfaces = scanner.findAnnotatedInterfaces(rootPackage, contractMarker)
                    contractInterfaces.flatMap { contract ->
                        scanner.findAllImplementations(rootPackage, contract)
                    }
                }.distinct()

            logger.debug { "Found ${implementationClasses.size} potential test targets." }

            implementationClasses.mapNotNull { kClass ->
                createSpecificationForClass(kClass)
                    .onFailure { error ->
                        logger.warn(error) { "Skipping class '${kClass.simpleName}'. Failed to create a TestSpecification." }
                    }.getOrNull()
            }
        }

    private fun createSpecificationForClass(kClass: KClass<*>): Result<TestSpecification> {
        val targetResult =
            DiscoveredTestTarget.create(
                kClass = kClass,
                displayName = kClass.simpleName ?: "UnnamedClass",
                fullyQualifiedName = kClass.qualifiedName ?: return Result.failure(Exception("Class must have a qualified name.")),
            )
        val target = targetResult.getOrElse { return Result.failure(it) }

        val constructor =
            kClass.primaryConstructor
                ?: return Result.failure(Exception("'${target.displayName}' must have a primary constructor."))

        val dependencies =
            constructor.parameters.map { param ->
                val dependencyResult =
                    DependencyMetadata.create(
                        name = param.name ?: return Result.failure(Exception("Parameter in '${target.displayName}' must have a name.")),
                        type =
                            param.type.classifier as? KClass<*>
                                ?: return Result.failure(Exception("Cannot determine type for parameter '${param.name}'.")),
                    )
                dependencyResult.getOrElse { return Result.failure(it) }
            }

        return TestSpecification.create(target, dependencies)
    }
}
