package planning.domain.session

/**
 * [Internal Primitive] Transactional snapshot for backtracking.
 *
 * Snapshot scope (fixed list):
 * 1) stackPointer
 * 2) nodeId watermark (indexer.size)
 * 3) indexer undo pointer
 * 4) signature slab pointer
 *
 * Invariant:
 * - Budget counters MUST be monotonic and MUST NOT be rolled back.
 */
class TransactionalFrame {
    private var savedStackPointer: Int = 0
    private var savedNodeCount: Int = 0
    private var savedIndexerUndoPtr: Int = 0
    private var savedSigPtr: Int = 0

    fun snap(session: PlannerSession) {
        this.savedStackPointer = session.structures.stackPointer
        this.savedNodeCount = session.indexer.size
        this.savedIndexerUndoPtr = session.indexer.snap()
        this.savedSigPtr = session.indexer.currentSigPtr()
    }

    fun rollback(session: PlannerSession) {
        // 1) Restore stack pointer and clear GreyMap entries for popped nodes.
        val structures = session.structures
        while (structures.stackPointer > this.savedStackPointer) {
            val poppedNodeId = structures.activeStack[--structures.stackPointer]
            structures.depthOfNodeId[poppedNodeId] = 0
        }

        // 2) Restore sparse table via undo log replay (metered).
        session.performIndexerRollback(this.savedIndexerUndoPtr)

        // 3) Restore dense watermarks (logical free).
        session.indexer.rollbackCount(this.savedNodeCount, this.savedSigPtr)
    }
}