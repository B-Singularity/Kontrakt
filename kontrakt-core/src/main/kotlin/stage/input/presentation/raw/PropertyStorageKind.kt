package stage.input.presentation.raw

/**
 * Closed storage vocabulary for property facts.
 *
 * This separates syntactic property presence from assignment/materialization capability.
 */
enum class PropertyStorageKind {
    BACKING_FIELD,
    LATEINIT,
    DELEGATED,
    COMPUTED,
    UNKNOWN,
}
