package planning.domain.expansion.seed

/**
 * Stable HID slot phase.
 *
 * Never use enum ordinal.
 *
 * protocolToken is the versioned semantic token used by canonical encoding.
 */
enum class LocalSelectorSlotPhase(
    val protocolToken: String,
) {
    SUBJECT("subject"),
    MEMBER("member"),
    ELEMENT("element"),
    COMPONENT("component"),
    KEY("key"),
    VALUE("value"),
    IMPLEMENTATION("implementation"),
    ATOMIC("atomic"),
}
