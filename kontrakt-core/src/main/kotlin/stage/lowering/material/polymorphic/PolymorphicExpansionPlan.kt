package stage.lowering.material.polymorphic

import stage.lowering.diagnostics.TypeExpansionContractViolationException
import stage.lowering.material.expansion.TypeCycleIdentity
import realization.planning.expansion.TypeExpansionContext

/**
 * Ratified polymorphic expansion plan for one type-expansion cycle.
 *
 * This is not:
 *
 * - an execution object;
 * - a DI container query;
 * - a reflection/KSP result;
 * - a mutable workflow builder;
 * - a cache key;
 * - or an adapter-owned selection object.
 *
 * This object is issued only after the polymorphic expansion boundary has
 * reconciled:
 *
 * - the current TypeCycleIdentity;
 * - the explicit TypeExpansionContext;
 * - implementation candidate facts or selected implementation facts;
 * - contract vacancy policy;
 * - and mode-specific readiness.
 *
 * Context law:
 *
 * Polymorphic resolution mode must be carried explicitly by TypeExpansionContext.
 * It must not be inferred from TypeReference alone.
 *
 * Since TypeExpansionContext is a sealed typed hierarchy, each concrete plan
 * accepts only the matching context subtype. The factories still assert the
 * context mode defensively so future context refactors cannot silently break the
 * plan/context relationship.
 *
 * Coherence law:
 *
 * Every plan must prove that the cycle subject and the contract/request type
 * describe the same semantic type.
 *
 * - ContractSubjectPlan:
 *     cycleIdentity.subject == implementations.contractType
 *
 * - DependencySelectionPlan:
 *     cycleIdentity.subject == selection.requestedType
 *
 * - StructuralMemberSelectionPlan:
 *     cycleIdentity.subject == selection.requestedType
 *
 * Status law:
 *
 * - ContractSubjectPlan may be READY or DEFERRED_NO_IMPLEMENTATION.
 * - DependencySelectionPlan is always READY once issued.
 * - StructuralMemberSelectionPlan is always READY once issued.
 *
 * Vacancy law:
 *
 * Empty implementation sets are legal only for contract-subject expansion when
 * ContractVacancyPolicy.WARN_AND_DEFER is active.
 *
 * If ContractVacancyPolicy.FAIL_STRICT is active, an empty implementation set
 * is rejected at issuance time.
 *
 * Diagnostic law:
 *
 * toString() returns a bounded summary. It must not dump full implementation
 * lists, binding snapshots, or seed surfaces.
 *
 * Equality/hash law:
 *
 * This object intentionally does not implement structural equals/hashCode yet.
 * Plan identity, interning, and canonical encoding are later phases. Do not make
 * this a data class, because copy() would weaken issuance discipline.
 */
sealed interface PolymorphicExpansionPlan {
    val kind: PolymorphicExpansionPlanKind
    val cycleIdentity: TypeCycleIdentity
    val context: TypeExpansionContext
    val status: PolymorphicExpansionStatus

    val isReady: Boolean
        get() = status == PolymorphicExpansionStatus.READY

    /**
     * Throws if this plan cannot legally enter a downstream materialization path.
     *
     * Deferred contract-vacancy plans are valid domain facts, but they must not
     * be treated as executable READY plans.
     */
    fun requireReady() {
        if (status != PolymorphicExpansionStatus.READY) {
            throw TypeExpansionContractViolationException(
                reason =
                    "PolymorphicExpansionPlan is not READY: " +
                            "kind=${kind.protocolToken}, status=$status, summary=${renderSummary()}",
            )
        }
    }

    /**
     * Compact diagnostic rendering.
     *
     * This is not canonical encoding and must not be used as a cache key.
     */
    fun renderSummary(): String

    /**
     * Plan for a contract subject expansion.
     *
     * A contract subject is the root polymorphic surface currently being
     * expanded. It carries a deterministic implementation set rather than one
     * selected implementation.
     *
     * Empty implementation set handling:
     *
     * - WARN_AND_DEFER -> DEFERRED_NO_IMPLEMENTATION
     * - FAIL_STRICT -> issuance failure
     */
    class ContractSubjectPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.ContractSubject,
        val implementations: PolymorphicImplementationCandidates,
        override val status: PolymorphicExpansionStatus,
    ) : PolymorphicExpansionPlan {
        override val kind: PolymorphicExpansionPlanKind
            get() = PolymorphicExpansionPlanKind.CONTRACT_SUBJECT

        override fun renderSummary(): String =
            "ContractSubjectPlan(" +
                    "subject=${cycleIdentity.subject.signature}, " +
                    "implementations=${implementations.size}, " +
                    "status=$status, " +
                    "mode=${context.mode.protocolToken}" +
                    ")"

        override fun toString(): String = renderSummary()

        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.ContractSubject,
                implementations: PolymorphicImplementationCandidates,
            ): ContractSubjectPlan {
                requireContextMode(
                    actual = context.mode,
                    expected = PolymorphicResolutionMode.CONTRACT_SUBJECT,
                    owner = "ContractSubjectPlan",
                )

                TypeReferenceIdentity.requireSameSemanticType(
                    left = cycleIdentity.subject,
                    right = implementations.contractType,
                    reason = "ContractSubjectPlan subject must match implementation set contract type",
                )

                val status =
                    deriveContractSubjectStatus(
                        context = context,
                        implementations = implementations,
                    )

                return ContractSubjectPlan(
                    cycleIdentity = cycleIdentity,
                    context = context,
                    implementations = implementations,
                    status = status,
                )
            }

            private fun deriveContractSubjectStatus(
                context: TypeExpansionContext.ContractSubject,
                implementations: PolymorphicImplementationCandidates,
            ): PolymorphicExpansionStatus {
                if (!implementations.isEmpty()) {
                    return PolymorphicExpansionStatus.READY
                }

                return when (context.contractVacancyPolicy) {
                    ContractVacancyPolicy.WARN_AND_DEFER -> {
                        PolymorphicExpansionStatus.DEFERRED_NO_IMPLEMENTATION
                    }

                    ContractVacancyPolicy.FAIL_STRICT -> {
                        throw TypeExpansionContractViolationException(
                            reason = "Contract subject has no implementation and FAIL_STRICT is active.",
                        )
                    }
                }
            }
        }
    }

    /**
     * Plan for selecting one implementation at a dependency site.
     *
     * A dependency site represents a required type that appears as a dependency
     * of another expansion subject.
     *
     * This plan is issued only after an ImplementationSelection has already been
     * ratified, so the plan is always READY.
     */
    class DependencySelectionPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.DependencySite,
        val selection: ImplementationSelection,
    ) : PolymorphicExpansionPlan {
        override val kind: PolymorphicExpansionPlanKind
            get() = PolymorphicExpansionPlanKind.DEPENDENCY_SELECTION

        override val status: PolymorphicExpansionStatus
            get() = PolymorphicExpansionStatus.READY

        override fun renderSummary(): String =
            "DependencySelectionPlan(" +
                    "subject=${cycleIdentity.subject.signature}, " +
                    "selected=${selection.selectedImplementation.canonicalIdentifier}, " +
                    "bindingKind=${selection.bindingKind.protocolToken}, " +
                    "selectionMode=${selection.selectionMode.protocolToken}, " +
                    "status=$status, " +
                    "mode=${context.mode.protocolToken}" +
                    ")"

        override fun toString(): String = renderSummary()

        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.DependencySite,
                selection: ImplementationSelection,
            ): DependencySelectionPlan {
                requireContextMode(
                    actual = context.mode,
                    expected = PolymorphicResolutionMode.DEPENDENCY_SITE,
                    owner = "DependencySelectionPlan",
                )

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

    /**
     * Plan for selecting one implementation at a structural member site.
     *
     * A structural member site represents a type encountered while expanding the
     * structure of another type, for example a projected field/member/property.
     *
     * This class remains separate from DependencySelectionPlan even though the
     * current issuance law is similar. Dependency sites and structural members
     * may diverge later in member ordering, visibility, field materialization,
     * proxy/decorator, or synthetic edge policy.
     */
    class StructuralMemberSelectionPlan private constructor(
        override val cycleIdentity: TypeCycleIdentity,
        override val context: TypeExpansionContext.StructuralMember,
        val selection: ImplementationSelection,
    ) : PolymorphicExpansionPlan {
        override val kind: PolymorphicExpansionPlanKind
            get() = PolymorphicExpansionPlanKind.STRUCTURAL_MEMBER_SELECTION

        override val status: PolymorphicExpansionStatus
            get() = PolymorphicExpansionStatus.READY

        override fun renderSummary(): String =
            "StructuralMemberSelectionPlan(" +
                    "subject=${cycleIdentity.subject.signature}, " +
                    "selected=${selection.selectedImplementation.canonicalIdentifier}, " +
                    "bindingKind=${selection.bindingKind.protocolToken}, " +
                    "selectionMode=${selection.selectionMode.protocolToken}, " +
                    "status=$status, " +
                    "mode=${context.mode.protocolToken}" +
                    ")"

        override fun toString(): String = renderSummary()

        companion object {
            @JvmStatic
            fun issue(
                cycleIdentity: TypeCycleIdentity,
                context: TypeExpansionContext.StructuralMember,
                selection: ImplementationSelection,
            ): StructuralMemberSelectionPlan {
                requireContextMode(
                    actual = context.mode,
                    expected = PolymorphicResolutionMode.STRUCTURAL_MEMBER,
                    owner = "StructuralMemberSelectionPlan",
                )

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

    companion object {
        private fun requireContextMode(
            actual: PolymorphicResolutionMode,
            expected: PolymorphicResolutionMode,
            owner: String,
        ) {
            if (actual != expected) {
                throw TypeExpansionContractViolationException(
                    reason =
                        "$owner context mode mismatch: " +
                                "expected=${expected.protocolToken}, actual=${actual.protocolToken}",
                )
            }
        }
    }
}
