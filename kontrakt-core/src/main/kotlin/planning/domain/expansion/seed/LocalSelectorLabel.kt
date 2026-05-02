package planning.domain.expansion.seed

/**
 * Stable HID selector label.
 *
 * Never use enum ordinal.
 *
 * protocolToken is the versioned semantic token used by canonical encoding.
 */
enum class LocalSelectorLabel(
    val protocolToken: String,
) {
    ACTIVE_MEMBER("active_member"),
    COLLECTION_ELEMENT("collection_element"),
    ARRAY_COMPONENT("array_component"),
    MAP_KEY("map_key"),
    MAP_VALUE("map_value"),
    POLYMORPHIC_IMPLEMENTATION("polymorphic_implementation"),
    ATOMIC_VALUE("atomic_value"),
}
