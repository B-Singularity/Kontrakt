package stage.canonicalization.material.frozen.sequence

import stage.canonicalization.material.frozen.record.FrozenConstructorParameterRecord

/**
 * Deterministic constructor-parameter sequence.
 *
 * Parameters must be compactly indexed as 0..N-1 after backend-neutral lowering.
 */
interface FrozenConstructorParameterRecordSequence :
    FrozenDeterministicSequence<FrozenConstructorParameterRecord>