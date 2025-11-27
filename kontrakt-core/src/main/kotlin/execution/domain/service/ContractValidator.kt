package execution.domain.service

import discovery.api.Positive
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation

class ContractValidator {

    fun validate(element: KAnnotatedElement, value: Any?)  {

        if (value == null) {
            if (element) {}
        }
    }
}