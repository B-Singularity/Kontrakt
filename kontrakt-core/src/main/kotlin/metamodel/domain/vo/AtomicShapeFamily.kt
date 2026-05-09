package metamodel.domain.vo

/**
 * Mutually-exclusive atomic sub-family used by TypeShapeSummary.
 *
 * This is a metamodel summary vocabulary, not the full atomic expansion model.
 *
 * It exists to answer:
 *
 *     "What kind of terminal atomic leaf is this canonical type?"
 *
 * It does not answer:
 *
 *     "How should the value be generated?"
 *     "Which deterministic seed material should be consumed?"
 *     "Which exact equality material should be emitted?"
 *
 * Those belong later to:
 *
 * - planning/domain/expansion/atomic/AtomicKind
 * - AtomicValueStrategy
 * - AtomicEqualityMaterial
 * - AtomicExpansionPlan
 *
 * Classifier safety:
 *
 * AtomicShapeFamily is only as correct as the classifier that issued it.
 * Reflection/KSP adapters must be covered by golden-vector tests that assert
 * stable classification for temporal, UUID, number, boolean, string, enum,
 * and domain-leaf surfaces.
 *
 * Misclassifying TEMPORAL_INSTANT as INTEGRAL_NUMBER is a order violation
 * because it can bypass seed-governed deterministic materialization later.
 *
 * Never use enum ordinal. protocolOrder is the only stable ordering surface.
 */
enum class AtomicShapeFamily(
    val protocolOrder: Int,
    val protocolToken: String,
    val atomicSurface: AtomicLeafSurface,
) {
    BOOLEAN_SCALAR(
        protocolOrder = 10,
        protocolToken = "boolean_scalar",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    CHARACTER_SCALAR(
        protocolOrder = 20,
        protocolToken = "character_scalar",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    INTEGRAL_NUMBER(
        protocolOrder = 30,
        protocolToken = "integral_number",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    FLOATING_NUMBER(
        protocolOrder = 40,
        protocolToken = "floating_number",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    DECIMAL_NUMBER(
        protocolOrder = 50,
        protocolToken = "decimal_number",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    STRING_TEXT(
        protocolOrder = 60,
        protocolToken = "string_text",
        atomicSurface = AtomicLeafSurface.TEXT_VALUE,
    ),

    TEMPORAL_INSTANT(
        protocolOrder = 70,
        protocolToken = "temporal_instant",
        atomicSurface = AtomicLeafSurface.DETERMINISTIC_MATERIAL_VALUE,
    ),

    TEMPORAL_LOCAL(
        protocolOrder = 80,
        protocolToken = "temporal_local",
        atomicSurface = AtomicLeafSurface.DETERMINISTIC_MATERIAL_VALUE,
    ),

    DURATION_SCALAR(
        protocolOrder = 90,
        protocolToken = "duration_scalar",
        atomicSurface = AtomicLeafSurface.SCALAR_VALUE,
    ),

    UUID_SCALAR(
        protocolOrder = 100,
        protocolToken = "uuid_scalar",
        atomicSurface = AtomicLeafSurface.DETERMINISTIC_MATERIAL_VALUE,
    ),

    DOMAIN_LEAF(
        protocolOrder = 110,
        protocolToken = "domain_leaf",
        atomicSurface = AtomicLeafSurface.DOMAIN_LEAF_VALUE,
    ),

    OPAQUE_LEAF(
        protocolOrder = 120,
        protocolToken = "opaque_leaf",
        atomicSurface = AtomicLeafSurface.OPAQUE_VALUE,
    ),
    ;

    /**
     * True when the later atomic expansion stage must derive value material from
     * deterministic seed state rather than host time/random/runtime state.
     *
     * This is only a summary-level signal. The actual seed consumption order
     * belongs to AtomicExpansionPlan.
     */
    val requiresSeedGovernedMaterialization: Boolean
        get() = atomicSurface == AtomicLeafSurface.DETERMINISTIC_MATERIAL_VALUE

    /**
     * True when this value must not be structurally projected into members.
     */
    val isOpaqueToStructuralProjection: Boolean
        get() = atomicSurface == AtomicLeafSurface.OPAQUE_VALUE
}

/**
 * Coarse semantic surface for atomic leaves.
 *
 * This prevents TypeShapeSummary validation from depending on the detailed
 * meaning of every AtomicShapeFamily enum entry.
 */
enum class AtomicLeafSurface(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    SCALAR_VALUE(
        protocolOrder = 10,
        protocolToken = "scalar_value",
    ),

    TEXT_VALUE(
        protocolOrder = 20,
        protocolToken = "text_value",
    ),

    DETERMINISTIC_MATERIAL_VALUE(
        protocolOrder = 30,
        protocolToken = "deterministic_material_value",
    ),

    DOMAIN_LEAF_VALUE(
        protocolOrder = 40,
        protocolToken = "domain_leaf_value",
    ),

    OPAQUE_VALUE(
        protocolOrder = 50,
        protocolToken = "opaque_value",
    ),
}
