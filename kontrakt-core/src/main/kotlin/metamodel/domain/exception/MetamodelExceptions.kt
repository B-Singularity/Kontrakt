package metamodel.domain.exception

import exception.KontraktException
import metamodel.domain.vo.TypeId
import metamodel.domain.vo.TypeSource

/**
 * [Domain Root] Base exception for the Metamodel bounded context.
 * It inherits from the framework's shared kernel exception [KontraktException].
 */
sealed class MetamodelException(message: String, cause: Throwable? = null) : KontraktException(message, cause)

/**
 * [Invariant Violation]
 * Thrown when accessing a closed Resolver.
 * This enforces the lifecycle policy of the Metamodel domain.
 */
class ResolverSessionClosedException : MetamodelException(
    "The TypeResolver session is closed. Do not reuse the resolver across analysis sessions."
)

/**
 * [Policy Violation]
 * Thrown when the Resolver receives a TypeSource that violates the adapter's contract.
 */
class UnsupportedSourceException(source: TypeSource) : MetamodelException(
    "Unsupported TypeSource: ${source::class.simpleName}. This resolver does not support this source type."
)

/**
 * [Validation Failure]
 * Thrown when the structure of the type is invalid according to Domain Rules.
 */
class MalformedTypeException(
    val typeId: TypeId,
    details: String
) : MetamodelException("Failed to parse type '$typeId': $details")