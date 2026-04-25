package planning.domain.expansion

import metamodel.domain.vo.TypeReference

/**
 * Preflight result produced before raw facts are resolved.
 *
 * This is the ADR-0037 split point.
 *
 * It carries:
 * - the subject type,
 * - the cycle identity used by PlannerSession.enterOrDetectCycle(...),
 * - and only the cheap child-type information needed for non-composite dispatch.
 *
 * It must not carry RawTypeFactsDTO.
 */
sealed interface TypeExpansionPreflightDecision {

    val subject: TypeReference

    val cycleIdentity: TypeCycleIdentity

    class AtomicPreflight private constructor(
        override val subject: TypeReference,
        override val cycleIdentity: TypeCycleIdentity,
    ) : TypeExpansionPreflightDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                cycleIdentity: TypeCycleIdentity,
            ): AtomicPreflight {
                return AtomicPreflight(
                    subject = subject,
                    cycleIdentity = cycleIdentity,
                )
            }
        }
    }

    class CompositePreflight private constructor(
        override val subject: TypeReference,
        override val cycleIdentity: TypeCycleIdentity,
    ) : TypeExpansionPreflightDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                cycleIdentity: TypeCycleIdentity,
            ): CompositePreflight {
                return CompositePreflight(
                    subject = subject,
                    cycleIdentity = cycleIdentity,
                )
            }
        }
    }

    class CollectionPreflight private constructor(
        override val subject: TypeReference,
        override val cycleIdentity: TypeCycleIdentity,
        val elementType: TypeReference,
    ) : TypeExpansionPreflightDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                cycleIdentity: TypeCycleIdentity,
                elementType: TypeReference,
            ): CollectionPreflight {
                return CollectionPreflight(
                    subject = subject,
                    cycleIdentity = cycleIdentity,
                    elementType = elementType,
                )
            }
        }
    }

    class ArrayPreflight private constructor(
        override val subject: TypeReference,
        override val cycleIdentity: TypeCycleIdentity,
        val componentType: TypeReference,
    ) : TypeExpansionPreflightDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                cycleIdentity: TypeCycleIdentity,
                componentType: TypeReference,
            ): ArrayPreflight {
                return ArrayPreflight(
                    subject = subject,
                    cycleIdentity = cycleIdentity,
                    componentType = componentType,
                )
            }
        }
    }

    class MapPreflight private constructor(
        override val subject: TypeReference,
        override val cycleIdentity: TypeCycleIdentity,
        val keyType: TypeReference,
        val valueType: TypeReference,
    ) : TypeExpansionPreflightDecision {
        companion object {
            @JvmStatic
            fun issue(
                subject: TypeReference,
                cycleIdentity: TypeCycleIdentity,
                keyType: TypeReference,
                valueType: TypeReference,
            ): MapPreflight {
                return MapPreflight(
                    subject = subject,
                    cycleIdentity = cycleIdentity,
                    keyType = keyType,
                    valueType = valueType,
                )
            }
        }
    }
}