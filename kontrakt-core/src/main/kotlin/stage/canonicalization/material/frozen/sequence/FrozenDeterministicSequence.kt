package stage.canonicalization.material.frozen.sequence

/**
 * Immutable deterministic sequence surface for frozen records.
 *
 * This is not a raw List wrapper in the semantic sense.
 *
 * Required law:
 * - ordering authority is explicit;
 * - adapter enumeration order is non-authoritative;
 * - ordering is a strict total order;
 * - duplicate semantic keys fail closed;
 * - local ordinals are compact and assigned after deterministic ordering;
 * - no backend-native declaration object participates in equality or ordering.
 *
 * The first implementation may use an immutable List internally. That internal
 * storage does not weaken the sequence law.
 */
interface FrozenDeterministicSequence<TRecord> {
    val size: Int

    operator fun get(
        index: Int,
    ): TRecord
}