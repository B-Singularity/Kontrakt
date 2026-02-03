package execution.domain.vo.plan

import metamodel.domain.vo.TypeReference

/**
 * [IR Core]
 * The common interface for all nodes in the generation plan.
 * Acts as the backbone for both the "Request" (Unlinked) and the "Instruction" (Executable).
 */
sealed interface PlanNode {
    val type: TypeReference

    /**
     * [Audit Metadata] ("The Receipt")
     * A human-readable set of constraints and metadata applied to this node.
     * e.g., "@Size(min=1)", "@Email", "@Past"
     *
     * ### Design Choice: Why String?
     * - **Audit Traceability:** Primary purpose is logging and reporting ("Why was this value generated?").
     * - **Serialization:** Strings are universally serializable (JSON/HTML) without custom adapters.
     * - **Decoupling:** Prevents the VM from depending on complex Metamodel/Annotation structures.
     */
    val attributes: Set<String>
}