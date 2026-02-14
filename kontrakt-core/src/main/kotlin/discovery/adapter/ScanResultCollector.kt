package discovery.adapter

import discovery.adapter.policy.NoiseFilter
import discovery.domain.policy.SpecPolicy
import discovery.domain.policy.SpecValidator
import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import kontrakt.ir.TypeId
import kontrakt.ir.exception.IrProtocolViolationException
import java.util.TreeSet

/**
 * [Collector & Assembler]
 * Iterates over scan results, applies filters, and assembles the raw components.
 * * Note: Final Determinism (Sorting/Freezing) is delegated to [discovery.domain.vo.ScanIndex.of].
 * This class focuses on "Finding" and "Validating".
 */
internal class ScanResultCollector(
    private val scanResult: ScanResult,
    private val taxonomy: DiscoveryTaxonomy
) {
    // Context for Error Reporting
    var currentPhase = "INIT"
    var currentTarget: String? = null

    fun collectScenarios(): List<TypeId> {
        currentPhase = "SCENARIO_SCAN"

        val candidates = safeSort(
            scanResult.getClassesWithAnnotation(SpecPolicy.SCENARIO_ANNOTATION),
            "SCENARIO_CANDIDATES"
        )

        val scenarioSet = TreeSet<TypeId>()

        for (info in candidates) {
            currentTarget = info.name
            if (NoiseFilter.isFundamentalNoise(info)) continue

            scenarioSet.add(validateAndCreate(info))
        }

        return ArrayList(scenarioSet)
    }

    fun collectContracts(): Map<TypeId, List<TypeId>> {
        currentPhase = "CONTRACT_SCAN"
        val contractMap = HashMap<TypeId, List<TypeId>>()

        // Fetch all annotated types (ClassGraph mixes classes/interfaces here)
        val candidates = safeSort(
            scanResult.getClassesWithAnnotation(SpecPolicy.CONTRACT_ANNOTATION),
            "CONTRACT_CANDIDATES"
        )

        for (info in candidates) {
            currentTarget = info.name

            // [Fix 2] Filter Noise FIRST.
            // Synthetic classes with annotations should be ignored, not validated for misuse.
            if (NoiseFilter.isFundamentalNoise(info)) continue

            // 1. Domain Validation (Misuse Check)
            val violation = SpecValidator.validateContract(isInterface = info.isInterface)

            if (violation != null) {
                taxonomy.reportSpecViolation(info.name, info.sourceFile, violation)
            } else {
                // 2. Valid Interface -> Process
                val contractId = validateAndCreate(info)

                // [Fix 1] Exception-Safe Phase Restoration
                val prevPhase = currentPhase
                currentPhase = "IMPL_SCAN_FOR_${info.name}"
                try {
                    val impls = collectImplementations(info.name)
                    if (impls.isNotEmpty()) {
                        contractMap[contractId] = impls
                    }
                } finally {
                    currentPhase = prevPhase // Always restore phase
                }
            }
        }

        return contractMap
    }

    fun collectDataContracts() {
        currentPhase = "DATA_CONTRACT_SCAN"
        val candidates = safeSort(
            scanResult.getClassesWithAnnotation(SpecPolicy.DATA_CONTRACT_ANNOTATION),
            "DATA_CONTRACT_CANDIDATES"
        )

        for (info in candidates) {
            currentTarget = info.name
            if (NoiseFilter.isFundamentalNoise(info)) continue

            val isConcrete = !info.isInterface && !info.isAbstract && !info.isEnum
            val violation = SpecValidator.validateDataContract(isConcrete = isConcrete)
            if (violation != null) {
                taxonomy.reportSpecViolation(info.name, info.sourceFile, violation)
            }

            validateAndCreate(info)
        }
    }

    private fun collectImplementations(contractName: String): List<TypeId> {
        val candidates = safeSort(
            scanResult.getClassesImplementing(contractName),
            "IMPL_CANDIDATES_FOR_$contractName"
        )

        val implSet = TreeSet<TypeId>()

        for (impl in candidates) {
            currentTarget = impl.name
            if (NoiseFilter.isFundamentalNoise(impl)) continue
            if (NoiseFilter.isHeuristicNoise(impl)) continue
            if (impl.isInterface || impl.isAbstract) continue

            implSet.add(validateAndCreate(impl))
        }

        return ArrayList(implSet)
    }

    private fun validateAndCreate(info: ClassInfo): TypeId {
        val name = info.name
        return try {
            TypeId.of(name)
        } catch (e: IrProtocolViolationException) {
            taxonomy.wrapInternalBug(name, e)
        }
    }

    private fun safeSort(collection: Collection<ClassInfo>, phaseContext: String): List<ClassInfo> {
        val prevPhase = currentPhase
        currentPhase = "SORTING_$phaseContext"
        try {
            return collection.sortedBy { it.name }
        } catch (e: LinkageError) {
            taxonomy.wrapInfrastructureError("SORTING_$phaseContext", null, e)
        } finally {
            currentPhase = prevPhase
        }
    }
}