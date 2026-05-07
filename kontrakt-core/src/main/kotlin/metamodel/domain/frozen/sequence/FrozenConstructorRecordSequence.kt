package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.record.FrozenConstructorRecord

/**
 * Deterministic constructor record sequence.
 *
 * Concrete builders must reject duplicate constructor keys and must not preserve
 * reflection/KSP constructor enumeration order as semantic order.
 */
interface FrozenConstructorRecordSequence :
    FrozenDeterministicSequence<FrozenConstructorRecord>