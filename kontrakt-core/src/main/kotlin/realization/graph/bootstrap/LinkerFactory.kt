package realization.graph.bootstrap

import realization.graph.binding.RealBindingStrategy
import realization.graph.linking.LinkerService
import statemachine.state.material.condition.IntegrityPhase
import statemachine.state.material.condition.ResolutionPhase

/**
 * Bootstrap factory for the Linker subsystem.
 * Isolates the Composition Root from the core domain logic.
 */
object LinkerFactory {
    fun createDefault(): LinkerService {
        val strategy = RealBindingStrategy()
        val resolution = ResolutionPhase(strategy)
        val integrity = IntegrityPhase()

        return LinkerService(resolution, integrity)
    }
}