package stage.input.presentation.raw

/**
 * Closed visibility vocabulary for normalized metamodel facts.
 *
 * UNKNOWN is required because reflection / bytecode / source adapters may not expose
 * identical visibility fidelity under every backend.
 */
enum class VisibilityKind {
    PUBLIC,
    PROTECTED,
    INTERNAL,
    PRIVATE,
    UNKNOWN,
}