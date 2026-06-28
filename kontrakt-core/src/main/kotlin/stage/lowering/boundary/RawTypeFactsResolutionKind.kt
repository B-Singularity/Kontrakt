package stage.lowering.boundary

/**
 * Distinguishes the accounting semantics of raw type-fact retrieval.
 *
 * CACHE_HIT:
 * - provider returned already-ratified raw facts from a memoized/cache surface
 * - physical-only metering
 *
 * ACTUAL_RESOLUTION:
 * - provider/backend performed actual fact discovery/reconciliation
 * - semantic-also metering
 */
enum class RawTypeFactsResolutionKind {
    CACHE_HIT,
    ACTUAL_RESOLUTION,
}
