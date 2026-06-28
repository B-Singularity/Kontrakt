package stage.canonicalization.diagnostics.provenance

/**
 * Backend family that produced the frozen image.
 *
 * This is diagnostic provenance, not semantic identity.
 */
enum class MetamodelSourceAdapterKind {
    REFLECTION,
    KSP,
    BYTECODE,
    SOURCE,
    GENERATED,
}