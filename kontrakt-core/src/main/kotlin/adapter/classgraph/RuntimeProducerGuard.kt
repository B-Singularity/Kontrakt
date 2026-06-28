package adapter.classgraph

import discovery.domain.exception.RuntimeIntegrityException
import discovery.port.outgoing.ClasspathScanner
import java.util.ServiceConfigurationError
import java.util.ServiceLoader

/**
 * [Runtime Integrity Guard]
 * Enforces the "Single Producer" policy.
 *
 * ## Strict Identity Policy
 * - **Distinct by Class Identity**: We check actual loaded `Class` objects.
 * - **Shadowing Check**: If multiple classes have the same FQCN but different Class Loaders/Identities,
 * it is considered a **Shadowing Conflict** and will cause a failure.
 */
internal object RuntimeProducerGuard {
    fun ensureSingleProvider() {
        try {
            val apiLoader = ClasspathScanner::class.java.classLoader
            val threadLoader = Thread.currentThread().contextClassLoader

            // 1. Load Provider Classes (Identity Check)
            val apiClasses = loadProviderClasses(apiLoader)
            val threadClasses =
                if (threadLoader != null && threadLoader !== apiLoader) {
                    loadProviderClasses(threadLoader)
                } else {
                    emptyList()
                }

            // 2. Unify & Validate
            val allClasses = (apiClasses + threadClasses).distinct() // Distinct by Object Identity

            // 3. Shadowing Detection
            // Group by Name to find conflicts (Same Name, Different Class Object)
            val groupedByName = allClasses.groupBy { it.name }

            val shadowingConflicts = groupedByName.filter { it.value.size > 1 }
            if (shadowingConflicts.isNotEmpty()) {
                // [Determinism Fix] Sort keys to ensure consistent error message order
                val conflictDetails = shadowingConflicts.keys.sorted().joinToString(", ")

                throw RuntimeIntegrityException(
                    "Environment Integrity Violation: Provider Shadowing detected. " +
                            "Multiple distinct classes found for the same provider name(s): [$conflictDetails]. " +
                            "Check your classpath for duplicate artifacts.",
                )
            }

            // 4. Single Producer Check
            val distinctCount = allClasses.size
            if (distinctCount != 1) {
                // [Determinism] Ensure the list is sorted
                val names = allClasses.map { it.name }.sorted().joinToString(", ")
                val status = if (distinctCount == 0) "Missing Provider" else "Multiple Providers"
                val foundList = if (distinctCount > 0) " (Found: $names)" else ""

                throw RuntimeIntegrityException(
                    "Environment Integrity Violation: $status$foundList (Must have exactly one 'kontrakt-discovery-*' artifact)",
                )
            }
        } catch (e: ServiceConfigurationError) {
            throw RuntimeIntegrityException("ServiceLoader failure during provider inspection.", e)
        } catch (e: LinkageError) {
            throw RuntimeIntegrityException("Provider linkage failure.", e)
        }
    }

    private fun loadProviderClasses(loader: ClassLoader): List<Class<out ClasspathScanner>> =
        ServiceLoader
            .load(ClasspathScanner::class.java, loader)
            .stream()
            .map { it.type() }
            .toList()
}
