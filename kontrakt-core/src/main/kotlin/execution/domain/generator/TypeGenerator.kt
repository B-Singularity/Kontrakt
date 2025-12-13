package execution.domain.generator

import kotlin.reflect.KParameter

interface TypeGenerator {

    fun supports(param: KParameter): Boolean

    // Smart Fuzz
    fun generate(param: KParameter): Any?

    fun generateValidBoundaries(param: KParameter): List<Any?>

    fun generateInvalid(param: KParameter): List<Any?>
}