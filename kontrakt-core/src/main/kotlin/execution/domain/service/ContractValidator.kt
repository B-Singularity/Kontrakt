package execution.domain.service

import discovery.api.NotNull
import discovery.api.Null
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation

class ContractValidator {

    fun validate(element: KAnnotatedElement, value: Any?) {

        if (value == null) {
            if (element.has<NotNull>()) {
                throw ContractViolationException("NotNull violation: value is null")
            }
            return
        }

        if (element.has<Null>()) {
            throw ContractViolationException("Null violation: value must be null but got '$value'")
        }
    }


    private inline fun <reified T : Annotation> KAnnotatedElement.find(): T? = findAnnotation<T>()
    private inline fun <reified T : Annotation> KAnnotatedElement.has(): Boolean = findAnnotation<T>() != null

    class ContractViolationException(message: String) : RuntimeException(message)
}