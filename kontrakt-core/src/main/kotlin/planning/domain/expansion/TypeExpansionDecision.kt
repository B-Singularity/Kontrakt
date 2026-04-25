package planning.domain.expansion

import metamodel.domain.vo.TypeReference

/**
 * Full expansion decision produced after active-cycle detection reports cycle miss.
 *
 * This sealed vocabulary must remain complete even if StructuralPlannerCore only
 * consumes CompositeExpansion in the current implementation phase.
 *
 * Reason:
 * - the domain decision surface should not shrink merely because execution frames
 *   for atomic/container/map paths are not implemented yet;
 * - future frames must attach to a stable closed vocabulary;
 * - deleting non-composite decisions would make TypeExpansionPipeline less lawful
 *   than the documented expansion model.
 */
sealed interface TypeExpansionDecision {

    val subject: TypeReference

    /**
     * Leaf-like type.
     *
     * No active-member traversal is required.
     * The current core may fail closed until a leaf/generator frame exists.
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
     * Composite object type with frozen projected traversal plan.
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
     * Collection-like container.
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
     * Array-like container.
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
     * Map-like container.
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
}