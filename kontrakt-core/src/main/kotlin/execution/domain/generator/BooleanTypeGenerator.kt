package execution.domain.generator

import discovery.api.AssertFalse
import discovery.api.AssertTrue

class BooleanTypeGenerator : TerminalGenerator {
    override fun supports(request: GenerationRequest): Boolean = request.type.classifier == Boolean::class

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> =
        buildList {
            if (request.has<AssertTrue>()) add(true)
            if (request.has<AssertFalse>()) add(false)
            if (isEmpty()) {
                add(true)
                add(false)
            }
        }

    override fun generate(
        request: GenerationRequest,
        context: GenerationContext,
    ): Any? =
        when {
            request.has<AssertTrue>() -> true
            request.has<AssertFalse>() -> false
            else -> context.seededRandom.nextBoolean()
        }

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> =
        buildList {
            if (request.has<AssertTrue>()) add(false)
            if (request.has<AssertFalse>()) add(true)
        }
}
