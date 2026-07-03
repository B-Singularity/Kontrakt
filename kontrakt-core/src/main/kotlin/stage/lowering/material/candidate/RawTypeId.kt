package stage.lowering.material.candidate

import realization.graph.IrLimits
import realization.graph.IrProtocolViolationException

/**
 * Raw type identifier for payload nodes.
 *
 * Unlike Spec TypeId, this may represent type tokens at a different boundary.
 * Still enforces:
 * - hygiene (no whitespace/control)
 * - max length
 */
@JvmInline
value class RawTypeId private constructor(
    val value: String,
) {
    override fun toString(): String = value

    companion object {
        fun of(value: String?): RawTypeId {
            if (value.isNullOrBlank()) throw IrProtocolViolationException("RawTypeId blank.")
            if (value.length > IrLimits.MAX_TYPE_ID_LENGTH) throw IrProtocolViolationException("RawTypeId too long.")
            if (value.any { it.isWhitespace() || it.isISOControl() }) throw IrProtocolViolationException("Hygiene failed.")
            return RawTypeId(value)
        }
    }
}
