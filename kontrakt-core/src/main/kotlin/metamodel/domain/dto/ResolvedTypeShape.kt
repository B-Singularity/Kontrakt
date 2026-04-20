package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.vo.TypeKind
import metamodel.domain.vo.TypeReference

/**
 * Resolved type-shape descriptor.
 *
 * This replaces the legacy broad TypeDescriptor for Planning Core dispatch.
 *
 * This DTO answers one question only:
 *
 * "Which expansion strategy should the planner use for this TypeReference?"
 *
 * It must not contain:
 * - constructor facts,
 * - property facts,
 * - projected active members,
 * - ordered traversal members,
 * - generator-specific payload material,
 * - source/adapter handles.
 *
 * DDD role:
 * - metamodel domain DTO for type-shape facts
 *
 * Hexagonal role:
 * - returned by TypeShapeProvider, implemented by reflection/KSP/bytecode adapters
 *
 * Compiler-style role:
 * - pre-dispatch shape classification before entering a specific expansion frame
 */
class ResolvedTypeShape private constructor(
    val subject: TypeReference,
    val kind: TypeKind,
    val nullability: NullabilityKind,
    val elementType: TypeReference?,
    val keyType: TypeReference?,
    val valueType: TypeReference?,
    val componentType: TypeReference?,
) {
    companion object {
        @JvmStatic
        fun atomic(
            subject: TypeReference,
            nullability: NullabilityKind,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.ATOMIC,
                nullability = nullability,
                elementType = null,
                keyType = null,
                valueType = null,
                componentType = null,
            )
        }

        @JvmStatic
        fun composite(
            subject: TypeReference,
            nullability: NullabilityKind,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.COMPOSITE,
                nullability = nullability,
                elementType = null,
                keyType = null,
                valueType = null,
                componentType = null,
            )
        }

        @JvmStatic
        fun interfaceShape(
            subject: TypeReference,
            nullability: NullabilityKind,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.INTERFACE,
                nullability = nullability,
                elementType = null,
                keyType = null,
                valueType = null,
                componentType = null,
            )
        }

        @JvmStatic
        fun collection(
            subject: TypeReference,
            nullability: NullabilityKind,
            elementType: TypeReference,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.COLLECTION,
                nullability = nullability,
                elementType = elementType,
                keyType = null,
                valueType = null,
                componentType = null,
            )
        }

        @JvmStatic
        fun array(
            subject: TypeReference,
            nullability: NullabilityKind,
            componentType: TypeReference,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.ARRAY,
                nullability = nullability,
                elementType = null,
                keyType = null,
                valueType = null,
                componentType = componentType,
            )
        }

        @JvmStatic
        fun map(
            subject: TypeReference,
            nullability: NullabilityKind,
            keyType: TypeReference,
            valueType: TypeReference,
        ): ResolvedTypeShape {
            return issue(
                subject = subject,
                kind = TypeKind.MAP,
                nullability = nullability,
                elementType = null,
                keyType = keyType,
                valueType = valueType,
                componentType = null,
            )
        }

        @JvmStatic
        fun issue(
            subject: TypeReference,
            kind: TypeKind,
            nullability: NullabilityKind,
            elementType: TypeReference?,
            keyType: TypeReference?,
            valueType: TypeReference?,
            componentType: TypeReference?,
        ): ResolvedTypeShape {
            validateCanonicalComponent("ResolvedTypeShape.subject.id", subject.id)
            validateCanonicalComponent("ResolvedTypeShape.subject.cycleId", subject.cycleId)
            validateCanonicalComponent("ResolvedTypeShape.subject.signature", subject.signature)

            validateShapeCardinality(
                owner = subject.id,
                kind = kind,
                elementType = elementType,
                keyType = keyType,
                valueType = valueType,
                componentType = componentType,
            )

            return ResolvedTypeShape(
                subject = subject,
                kind = kind,
                nullability = nullability,
                elementType = elementType,
                keyType = keyType,
                valueType = valueType,
                componentType = componentType,
            )
        }

        private fun validateShapeCardinality(
            owner: String,
            kind: TypeKind,
            elementType: TypeReference?,
            keyType: TypeReference?,
            valueType: TypeReference?,
            componentType: TypeReference?,
        ) {
            when (kind) {
                TypeKind.ATOMIC,
                TypeKind.COMPOSITE,
                TypeKind.INTERFACE -> {
                    requireNoContainerTypes(
                        owner = owner,
                        factKind = "ResolvedTypeShape",
                        kind = kind,
                        elementType = elementType,
                        keyType = keyType,
                        valueType = valueType,
                        componentType = componentType,
                    )
                }

                TypeKind.COLLECTION -> {
                    if (elementType == null || keyType != null || valueType != null || componentType != null) {
                        throw InvalidTypeFactShapeException(
                            owner = owner,
                            factKind = "ResolvedTypeShape",
                            reason = "COLLECTION shape requires exactly elementType and no key/value/component type.",
                        )
                    }
                }

                TypeKind.ARRAY -> {
                    if (componentType == null || elementType != null || keyType != null || valueType != null) {
                        throw InvalidTypeFactShapeException(
                            owner = owner,
                            factKind = "ResolvedTypeShape",
                            reason = "ARRAY shape requires exactly componentType and no element/key/value type.",
                        )
                    }
                }

                TypeKind.MAP -> {
                    if (keyType == null || valueType == null || elementType != null || componentType != null) {
                        throw InvalidTypeFactShapeException(
                            owner = owner,
                            factKind = "ResolvedTypeShape",
                            reason = "MAP shape requires exactly keyType and valueType and no element/component type.",
                        )
                    }
                }
            }
        }

        private fun requireNoContainerTypes(
            owner: String,
            factKind: String,
            kind: TypeKind,
            elementType: TypeReference?,
            keyType: TypeReference?,
            valueType: TypeReference?,
            componentType: TypeReference?,
        ) {
            if (elementType != null || keyType != null || valueType != null || componentType != null) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = factKind,
                    reason = "$kind shape must not carry container child type references.",
                )
            }
        }
    }
}