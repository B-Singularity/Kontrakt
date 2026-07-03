package stage.canonicalization.material.frozen.sequence

import stage.canonicalization.material.frozen.records.FrozenPropertyRecord

/**
 * Deterministic property records sequence.
 *
 * Property order must be derived from backend-neutral property identity, not
 * from reflection/KSP/backend enumeration order.
 */
interface FrozenPropertyRecordSequence :
    FrozenDeterministicSequence<FrozenPropertyRecord>