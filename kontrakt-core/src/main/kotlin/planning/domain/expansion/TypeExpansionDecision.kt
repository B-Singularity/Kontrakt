package planning.domain.expansion

import metamodel.domain.vo.TypeReference

/**
 * Closed expansion decision produced by TypeExpansionPipeline.
 *
 * This is not a plan node.
 * It is a compiler-style dispatch result that tells StructuralPlannerCore which
 * expansion frame to create next.
 */
sealed interface TypeExpansionDecision {

    val subject: TypeReference

    /**
     * Leaf-like type. No active-member traversal should be performed.
     */
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

    /**
     * Composite object type. Traversal must consume orderedMembers from
     * CompositeExpansionPlan.
     */
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

    /**
     * Iterable-like container. The next frame should handle element expansion.
     */
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

    /**
     * Array-like container. The next frame should handle component expansion.
     */
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

    /**
     * Map-like container. The next frame should handle key/value expansion.
     */
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

    /**
     * Interface/protocol-like type.
     *
     * This is separated from Composite so implementation-resolution policy can
     * be introduced explicitly instead of accidentally treating interfaces as
     * ordinary objects.
     */
    class InterfaceExpansion private constructor(
        override val subject: TypeReference,
    ) : TypeExpansionDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
            ): InterfaceExpansion {
                return InterfaceExpansion(subject)
            }
        }
    }
}