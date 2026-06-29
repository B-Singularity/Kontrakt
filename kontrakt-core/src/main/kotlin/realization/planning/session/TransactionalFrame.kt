package realization.planning.session

/**
 * Transactional snapshot for backtracking.
 *
 * Responsibility:
 * - capture ONLY rollback-relevant state
 * - restore ONLY rollback-relevant state
 *
 * It MUST NOT carry execution semantics such as:
 * - frame kind
 * - type reference
 * - member cursor value
 * - edge context
 *
 * Note:
 * - member cursor values remain session-owned primitive state
 * - only the cursor-stack pointer is captured here
 */
internal class TransactionalFrame private constructor() {
    private var savedStackPointer: Int = 0
    private var savedNodeCount: Int = 0
    private var savedIndexerUndoPtr: Int = 0
    private var savedSigPtr: Int = 0
    private var savedMemberCursorStackPointer: Int = 0

    private var savedSoftCheckpoint: Long = 0L
    private var savedPlaceholderCounter: Int = 0
    private var savedBuilderLogPos: Int = 0
    private var savedCacheLogPos: Int = 0

    fun snap(session: PlannerSession) {
        savedStackPointer = session.structures.stackPointer
        savedNodeCount = session.indexer.size
        savedIndexerUndoPtr = session.indexer.snap()
        savedSigPtr = session.indexer.currentSigPtr()
        savedMemberCursorStackPointer = session.currentMemberCursorStackPointer()

        savedSoftCheckpoint = session.currentSoftCheckpoint()
        savedPlaceholderCounter = session.currentPlaceholderCounter()
        savedBuilderLogPos = session.currentBuilderLogPos()
        savedCacheLogPos = session.currentCacheLogPos()
    }

    fun rollback(session: PlannerSession) {
        val structures = session.structures

        while (structures.stackPointer > savedStackPointer) {
            val depth = structures.stackPointer
            val poppedNodeId = structures.activeStack[--structures.stackPointer]
            structures.depthOfNodeId[poppedNodeId] = 0
            structures.clearDepthMetadata(depth)
        }

        session.performIndexerRollback(savedIndexerUndoPtr)
        session.indexer.rollbackCount(savedNodeCount, savedSigPtr)

        session.restoreCheckpointState(
            softCheckpoint = savedSoftCheckpoint,
            placeholderCounter = savedPlaceholderCounter,
            builderLogPos = savedBuilderLogPos,
            cacheLogPos = savedCacheLogPos,
            memberCursorStackPointer = savedMemberCursorStackPointer,
        )
    }

    companion object {
        @JvmStatic
        fun issue(): TransactionalFrame = TransactionalFrame()
    }
}
