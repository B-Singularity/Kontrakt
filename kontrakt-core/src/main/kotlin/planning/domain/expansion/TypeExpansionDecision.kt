package planning.domain.expansion

import metamodel.domain.vo.TypeReference

/**
 * Closed expansion decision produced by TypeExpansionPipeline.
 *
 * This is not a plan node.
 * It is a compiler-style dispatch result that tells StructuralPlannerCore which
 * expansion frame to create next.
 *
 * Interface expansion is intentionally not represented yet.
 * Until implementation-resolution policy exists, interface shape is fail-closed
 * inside TypeExpansionPipeline.
 */
sealed interface TypeExpansionDecision {

    val subject: TypeReference

    class AtomicExpansion private constructor(
        override val subject: TypeReference,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
            ): AtomicExpansion {
                return AtomicExpansion(subject)
            }
        }
    }

    class CompositeExpansion private constructor(
        override val subject: TypeReference,
        val plan: CompositeExpansionPlan,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                plan: CompositeExpansionPlan,
            ): CompositeExpansion {
                return CompositeExpansion(
                    subject = subject,
                    plan = plan,
                )
            }
        }
    }

    class CollectionExpansion private constructor(
        override val subject: TypeReference,
        val elementType: TypeReference,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                elementType: TypeReference,
            ): CollectionExpansion {
                return CollectionExpansion(
                    subject = subject,
                    elementType = elementType,
                )
            }
        }
    }

    class ArrayExpansion private constructor(
        override val subject: TypeReference,
        val componentType: TypeReference,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                componentType: TypeReference,
            ): ArrayExpansion {
                return ArrayExpansion(
                    subject = subject,
                    componentType = componentType,
                )
            }
        }
    }

    class MapExpansion private constructor(
        override val subject: TypeReference,
        val keyType: TypeReference,
        val valueType: TypeReference,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                keyType: TypeReference,
                valueType: TypeReference,
            ): MapExpansion {
                return MapExpansion(
                    subject = subject,
                    keyType = keyType,
                    valueType = valueType,
                )
            }
        }
    }
}