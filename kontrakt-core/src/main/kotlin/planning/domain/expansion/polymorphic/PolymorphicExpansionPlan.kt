package planning.domain.expansion.polymorphic

import planning.domain.exception.TypeExpansionContractViolationException
import planning.domain.expansion.TypeCycleIdentity
import planning.domain.expansion.context.TypeExpansionContext

sealed interface PolymorphicExpansionPlan {
    val cycleIdentity: TypeCycleIdentity
    val context: TypeExpansionContext
    val status: PolymorphicExpansionStatus

    class ContractSubjectPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.ContractSubject,
        val implementations: PolymorphicImplementationSet,
        override val status: PolymorphicExpansionStatus,
    ) : PolymorphicExpansionPlan {
        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.ContractSubject,
                implementations: PolymorphicImplementationSet,
            ): ContractSubjectPlan {
                TypeReferenceIdentity.requireSameSemanticType(
                    left = cycleIdentity.subject,
                    right = implementations.contractType,
                    reason = "ContractSubjectPlan subject must match implementation set contract type",
                )

                val status = if (implementations.isEmpty()) {
                    when (context.contractVacancyPolicy) {
                        ContractVacancyPolicy.WARN_AND_DEFER -> PolymorphicExpansionStatus.DEFERRED_NO_IMPLEMENTATION
                        ContractVacancyPolicy.FAIL_STRICT -> throw TypeExpansionContractViolationException(
                            reason = "Contract subject has no implementation and FAIL_STRICT is active.",
                        )
                    }
                } else {
                    PolymorphicExpansionStatus.READY
                }

                return ContractSubjectPlan(
                    cycleIdentity = cycleIdentity,
                    context = context,
                    implementations = implementations,
                    status = status,
                )
            }
        }
    }

    class DependencySelectionPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.DependencySite,
        val selection: ImplementationSelection,
    ) : PolymorphicExpansionPlan {
        override val status: PolymorphicExpansionStatus
            get() = PolymorphicExpansionStatus.READY

        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.DependencySite,
                selection: ImplementationSelection,
            ): DependencySelectionPlan {
                TypeReferenceIdentity.requireSameSemanticType(
                    left = cycleIdentity.subject,
                    right = selection.requestedType,
                    reason = "DependencySelectionPlan subject must match selected request type",
                )

                return DependencySelectionPlan(
                    cycleIdentity = cycleIdentity,
                    context = context,
                    selection = selection,
                )
            }
        }
    }

    class StructuralMemberSelectionPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.StructuralMember,
        val selection: ImplementationSelection,
    ) : PolymorphicExpansionPlan {
        override val status: PolymorphicExpansionStatus
            get() = PolymorphicExpansionStatus.READY

        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.StructuralMember,
                selection: ImplementationSelection,
            ): StructuralMemberSelectionPlan {
                TypeReferenceIdentity.requireSameSemanticType(
                    left = cycleIdentity.subject,
                    right = selection.requestedType,
                    reason = "StructuralMemberSelectionPlan subject must match selected request type",
                )

                return StructuralMemberSelectionPlan(
                    cycleIdentity = cycleIdentity,
                    context = context,
                    selection = selection,
                )
            }
        }
    }
}