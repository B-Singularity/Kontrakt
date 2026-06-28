package stage.canonicalization.material.frozen.table

/**
 * Typed table identifier for frozen image diagnostics.
 *
 * Do not pass arbitrary table-name strings across frozen image integrity
 * boundaries. String table names make diagnostics fragile and allow spelling
 * drift.
 */
enum class FrozenMetamodelImageTableId {
    TYPE_INDEX,
    SHAPE_TABLE,
    CYCLE_IDENTITY_TABLE,
    RAW_FACT_TABLE,
    CONSTRUCTOR_RECORD_SEQUENCE,
    CONSTRUCTOR_PARAMETER_SEQUENCE,
    PROPERTY_RECORD_SEQUENCE,
    ANNOTATION_RECORD_SEQUENCE,
}