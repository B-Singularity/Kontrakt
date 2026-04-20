package metamodel.adapter.reflection

import metamodel.domain.exception.UnsupportedMetamodelSourceException
import metamodel.domain.vo.TypeReference
import kotlin.reflect.KType

/**
 * Adapter-local access bridge for reflection-backed TypeReference values.
 *
 * Domain code must not depend on this.
 */
internal object ReflectionTypeReferenceAccess {

    fun requireKType(
        reference: TypeReference,
    ): KType {
        val backed = reference as? ReflectionBackedTypeReference
            ?: throw UnsupportedMetamodelSourceException(
                "Reflection adapter received a non-reflection TypeReference: " +
                        "referenceClass=${reference::class.qualifiedName}, id=${reference.id}"
            )

        return backed.kType
    }
}