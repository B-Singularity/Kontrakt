package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.record.FrozenPropertyRecord

/**
 * Deterministic property record sequence.
 *
 * Property order must be derived from backend-neutral property identity, not
 * from reflection/KSP/backend enumeration order.
 */
interface FrozenPropertyRecordSequence :
    FrozenDeterministicSequence<FrozenPropertyRecord>