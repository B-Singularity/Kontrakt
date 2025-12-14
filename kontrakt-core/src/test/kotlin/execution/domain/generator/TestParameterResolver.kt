package execution.domain.generator

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

object TestParameterResolver {

    fun getParam(func: KFunction<*>, paramName: String = ""): KParameter {
        val params = func.parameters
        return if (paramName.isBlank()) {
            params.last()
        } else {
            params.find { it.name == paramName }
                ?: throw IllegalArgumentException("Parameter '$paramName' not found in function ${func.name}")
        }
    }
}