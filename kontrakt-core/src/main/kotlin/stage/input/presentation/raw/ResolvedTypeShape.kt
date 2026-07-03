package stage.input.presentation.raw

import stage.admission.diagnostics.evidence.InvalidTypeFactShapeException
import stage.canonicalization.material.representation.TypeReference

/**
 * Resolved type-shape descriptor.
 *
 * This replaces the legacy broad TypeDescriptor for planning-core dispatch.
 *
 * This DTO answers one question only:
 *
 *     "Which expansion strategy should the planner use for this TypeReference?"
 *
 * This is not:
 *
 * - a constructor-fact container;
 * - a property-fact container;
 * - a projected active-member set;
 * - an ordered traversal-member view;
 * - a generator-specific payload object;
 * - a reflection/KSP/source handle;
 * - a cache key;
 * - or a canonical encoding.
 *
 * DDD role:
 *
 * ResolvedTypeShape is a metamodel-domain DTO for shape facts.
 *
 * Hexagonal role:
 *
 * It is returned by TypeShapeProvider implementations. Reflection, KSP, bytecode,
 * or source adapters may classify shape, but Planning Core consumes only this
 * closed DTO.
 *
 * Compiler-style role:
 *
 * This is pre-dispatch classification before entering a concrete expansion
 * frame.
 *
 * Trust-boundary law:
 *
 * subject and child type references are final domain-issued TypeReference VOs.
 *
 * This DTO must not revalidate:
 *
 * - subject.id;
 * - subject.signature;
 * - subject.cycleKey;
 * - child TypeReference identities.
 *
 * Their integrity is already enforced by:
 *
 * - CanonicalTypeId;
 * - TypeCycleKey;
 * - CanonicalTypeSignature;
 * - TypeIdentityCoherenceProof;
 * - TypeReference.issue(...).
 *
 * Revalidating those surfaces here would duplicate work and risk drifting from
 * TypeReference's own issuance law.
 *
 * Shape law:
 *
 * The declared DTO kind must agree with subject.shapeSummary.kind.
 *
 * This prevents a classifier from emitting:
 *
 * - subject whose TypeReference says ATOMIC;
 * - ResolvedTypeShape whose kind says COLLECTION.
 *
 * Cardinality law:
 *
 * Each shape kind has exactly one valid child-reference layout:
 *
 * - ATOMIC / COMPOSITE / INTERFACE:
 *     no child type references;
 *
 * - COLLECTION:
 *     exactly elementType;
 *
 * - ARRAY:
 *     exactly componentType;
 *
 * - MAP:
 *     exactly keyType and valueType.
 *
 * This fail-closed boundary allows downstream expansion frames to avoid
 * defensive null/cast guessing.
 *
 * Recursion law:
 *
 * Child references are TypeReference values. Their nesting-depth limits are
 * already enforced by TypeReference.issue(...). This DTO does not add another
 * recursive walk.
 *
 * Diagnostic law:
 *
 * toString() returns a compact summary and must not recursively dump child
 * TypeReference internals.
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
    fun renderSummary(): String {
        return "ResolvedTypeShape(" +
                "subject=${subject.id.value}, " +
                "kind=$kind, " +
                "nullability=$nullability, " +
                "element=${elementType?.id?.value ?: "<none>"}, " +
                "key=${keyType?.id?.value ?: "<none>"}, " +
                "value=${valueType?.id?.value ?: "<none>"}, " +
                "component=${componentType?.id?.value ?: "<none>"}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

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
            requireSubjectKindCoherence(
                subject = subject,
                kind = kind,
            )

            validateShapeCardinality(
                owner = subject.id.value,
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

        /**
         * Defensive classifier coherence check.
         *
         * TypeReference already carries a TypeShapeSummary. TypeShapeProvider must
         * not classify the same subject into a different shape kind.
         *
         * This comparison intentionally uses enum names because TypeKind and the
         * shape-summary kind may be represented by different enum types during the
         * current refactoring window.
         *
         * If the project later unifies those enums, replace this with direct
         * enum equality.
         */
        private fun requireSubjectKindCoherence(
            subject: TypeReference,
            kind: TypeKind,
        ) {
            val subjectKindName = subject.shapeSummary.kind.name
            val resolvedKindName = kind.name

            if (subjectKindName != resolvedKindName) {
                throw InvalidTypeFactShapeException(
                    owner = subject.id.value,
                    factKind = "ResolvedTypeShape",
                    reason = "Resolved shape kind must match TypeReference.shapeSummary.kind: " +
                            "subjectKind=$subjectKindName, resolvedKind=$resolvedKindName",
                )
            }
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
                TypeKind.INTERFACE,
                    -> {
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
                    if (
                        elementType == null ||
                        keyType != null ||
                        valueType != null ||
                        componentType != null
                    ) {
                        throw InvalidTypeFactShapeException(
                            owner = owner,
                            factKind = "ResolvedTypeShape",
                            reason = "COLLECTION shape requires exactly elementType and no key/value/component type.",
                        )
                    }
                }

                TypeKind.ARRAY -> {
                    if (
                        componentType == null ||
                        elementType != null ||
                        keyType != null ||
                        valueType != null
                    ) {
                        throw InvalidTypeFactShapeException(
                            owner = owner,
                            factKind = "ResolvedTypeShape",
                            reason = "ARRAY shape requires exactly componentType and no element/key/value type.",
                        )
                    }
                }

                TypeKind.MAP -> {
                    if (
                        keyType == null ||
                        valueType == null ||
                        elementType != null ||
                        componentType != null
                    ) {
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
            if (
                elementType != null ||
                keyType != null ||
                valueType != null ||
                componentType != null
            ) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = factKind,
                    reason = "$kind shape must not carry container child type references.",
                )
            }
        }
    }
}