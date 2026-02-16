package linking.bootstrap

import linking.adapter.strategy.RealBindingStrategy
import linking.domain.phase.IntegrityPhase
import linking.domain.phase.ResolutionPhase
import linking.domain.service.LinkerService

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