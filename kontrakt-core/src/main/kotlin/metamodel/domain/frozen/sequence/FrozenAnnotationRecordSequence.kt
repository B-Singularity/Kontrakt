package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.record.FrozenAnnotationRecord

/**
 * Deterministic annotation record sequence.
 *
 * Annotation order must be derived from canonical annotation identity and
 * payload ordering.
 */
interface FrozenAnnotationRecordSequence :
    FrozenDeterministicSequence<FrozenAnnotationRecord>