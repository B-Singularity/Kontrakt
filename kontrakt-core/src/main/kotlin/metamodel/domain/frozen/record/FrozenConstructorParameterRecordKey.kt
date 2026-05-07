package metamodel.domain.frozen.record

/**
 * Backend-neutral constructor-parameter identity key.
 *
 * TODO:
 * Replace parameterName String with CanonicalParameterName once the VO is
 * ratified.
 */
class FrozenConstructorParameterRecordKey private constructor(
    val ownerConstructorKey: FrozenConstructorRecordKey,
    val parameterIndex: Int,
    val parameterName: String,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerConstructorKey: FrozenConstructorRecordKey,
            parameterIndex: Int,
            parameterName: String,
        ): FrozenConstructorParameterRecordKey {
            return FrozenConstructorParameterRecordKey(
                ownerConstructorKey = ownerConstructorKey,
                parameterIndex = parameterIndex,
                parameterName = parameterName,
            )
        }
    }
}