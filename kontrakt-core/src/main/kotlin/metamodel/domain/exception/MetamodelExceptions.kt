package metamodel.domain.exception

import exception.KontraktException
import metamodel.domain.vo.TypeId
import metamodel.domain.vo.TypeReference

abstract class MetamodelException(
    message: String,
    cause: Throwable? = null
) : KontraktException(message, cause) {

    final override val domain: String = "METAMODEL"
    protected abstract val errorCode: String
    protected open val errorData: Map<String, Any?> = emptyMap()

    final override val details: Map<String, Any?> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        mapOf(
            "domain" to domain,
            "code" to "META.$errorCode",
            "data" to errorData
        ).also { validateJsonSafety(it) } // Safe: passing local map
    }
}

class ResolverSessionClosedException : MetamodelException("The TypeSystem session is closed.") {
    override val errorCode = "RESOLVER_CLOSED"
}

class UnsupportedReferenceException(val reference: TypeReference) : MetamodelException(
    "Unsupported TypeReference: ${reference::class.simpleName}"
) {
    override val errorCode = "UNSUPPORTED_REFERENCE"
    override val errorData = mapOf("referenceName" to reference.name)
}

class MalformedTypeException(val typeId: TypeId, details: String) : MetamodelException(
    "Failed to parse type structure for '$typeId': $details"
) {
    override val errorCode = "MALFORMED_TYPE"
    override val errorData = mapOf("typeId" to typeId.toString(), "reason" to details)
}