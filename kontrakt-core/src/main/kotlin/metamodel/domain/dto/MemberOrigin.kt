package metamodel.domain.dto

/**
 * Closed origin vocabulary for normalized metamodel facts.
 *
 * Existing TypeFactsDTO already had DECLARED / INHERITED / SYNTHETIC.
 * This version extends the vocabulary for source-reconciliation and drift reporting.
 */
enum class MemberOrigin {
    DECLARED,
    INHERITED,
    SYNTHETIC,

    /**
     * Adapter reconstructed the fact from incomplete backend evidence.
     */
    ADAPTER_INFERRED,

    /**
     * Adapter could not determine origin deterministically.
     */
    UNKNOWN,
}
