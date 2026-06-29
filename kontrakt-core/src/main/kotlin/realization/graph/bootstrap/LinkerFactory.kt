package realization.graph.bootstrap

import realization.graph.linking.LinkerService
import realization.graph.binding.RealBindingStrategy
import statemachine.state.material.IntegrityPhase
import statemachine.state.material.ResolutionPhase

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