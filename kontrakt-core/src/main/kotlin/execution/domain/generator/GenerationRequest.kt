package execution.domain.generator

import kotlin.reflect.KParameter
import kotlin.reflect.KType

data class GenerationRequest(
    val type: KType,
    val annotations: List<Annotation> = emptyList(),
    val name: String
) {

    inline fun <reified T : Annotation> has(): Boolean {
        return annotations.any { it is T }
    }

    inline fun <reified T : Annotation> find(): T? {
        return annotations.filterIsInstance<T>().firstOrNull()
    }

    companion object {
        private const val ENTRY_POINT_NAME = "[ENTRY_POINT]"
        private const val ANONYMOUS_PARAM = "[ARG]"
        private const val ELEMENT_Item = "[ELEMENT]"


        fun from(param: KParameter): GenerationRequest {
            return GenerationRequest(
                type = param.type,
                annotations = param.annotations,
                name = param.name ?: ANONYMOUS_PARAM
            )
        }

        fun from(
            type: KType,
            name: String = ELEMENT_Item,
            annotations: List<Annotation> = emptyList()
        ): GenerationRequest {
            return GenerationRequest(
                type = type,
                annotations = annotations,
                name = name
            )
        }

        fun entryPoint(type: KType): GenerationRequest {
            return GenerationRequest(
                type = type,
                annotations = emptyList(),
                name = ENTRY_POINT_NAME
            )
        }
    }
}
