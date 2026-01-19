package execution.domain.vo.context.generation

import kotlin.reflect.KParameter
import kotlin.reflect.KType

data class GenerationRequest(
    val type: KType,
    val annotations: List<Annotation> = emptyList(),
    val name: String,
) {
    inline fun <reified T : Annotation> has(): Boolean = annotations.any { it is T }

    inline fun <reified T : Annotation> find(): T? = annotations.filterIsInstance<T>().firstOrNull()

    companion object {
        private const val ENTRY_POINT_NAME = "[ENTRY_POINT]"
        private const val ANONYMOUS_PARAM = "[ARG]"
        private const val ELEMENT_ITEM = "[ELEMENT]"

        fun from(param: KParameter): GenerationRequest =
            GenerationRequest(
                type = param.type,
                annotations = param.annotations,
                name = param.name ?: ANONYMOUS_PARAM,
            )

        fun from(
            type: KType,
            name: String = ELEMENT_ITEM,
            annotations: List<Annotation> = emptyList(),
        ): GenerationRequest =
            GenerationRequest(
                type = type,
                annotations = annotations,
                name = name,
            )

        fun entryPoint(type: KType): GenerationRequest =
            GenerationRequest(
                type = type,
                annotations = emptyList(),
                name = ENTRY_POINT_NAME,
            )
    }
}
