package planning.infrastructure.cache

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.runtime.lifecycle.BuilderHandleState
import planning.domain.runtime.lifecycle.L2LifecycleLaw
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle

/**
 * Object identity shell for one builder-handle episode in the Planning L2 runtime.
 *
 * ## Why this class exists
 *
 * A miss-path build authority must not remain an implicit callback capability or
 * a loosely tracked future. It must be an explicit lifecycle host with:
 *
 * - exactly-once convergence,
 * - explicit OPEN / COMMITTED / ABORTED meaning,
 * - and supervisory force-abort support.
 *
 * This class provides that builder-handle identity shell.
 *
 * ## Architectural position
 *
 * This class owns builder-handle convergence only.
 *
 * It does **not** own:
 *
 * - shared-slot terminal truth,
 * - waiter terminal truth,
 * - commit-right arbitration,
 * - or region lifecycle.
 *
 * Build execution and publication authority are intentionally separate.
 * This class tracks only builder-handle convergence for one issued handle episode.
 *
 * ## Ownership
 *
 * This class owns:
 *
 * - exactly-once commit / abort authority,
 * - supervisory convergence bookkeeping,
 * - immutable deadline material,
 * - immutable generation material,
 * - and operational linkage for slot-local handle registry scans.
 *
 * ## Non-ownership
 *
 * This class does not decide:
 *
 * - who wins authoritative publication,
 * - whether the shared slot is still publishable,
 * - or whether the region remains open for mutable lifecycle work.
 *
 * Those decisions belong elsewhere.
 *
 * ## Identity and reclamation model
 *
 * Like [WaiterCell], this class is an object identity shell.
 * It is intentionally not immediately pooled or rebound.
 *
 * The runtime currently prefers identity shells for builder episodes because supervisory
 * callbacks, delayed observation, and grace-aware reclamation are easier to make correct
 * when a handle episode retains stable object identity.
 *
 * ## Concurrency contract
 *
 * Builder-handle truth is carried only by [handleWord].
 * Everything else in this class is support material.
 *
 * Current conceptual packing:
 *
 * - bits `0..1` : [BuilderHandleState] code
 * - remaining bits reserved
 *
 * The reserved space exists so the runtime can evolve operational flags later without
 * redefining the conceptual authority surface.
 */
internal class BuilderHandleCell private constructor(
    supervisoryDeadlineNanos: Long,
    generation: Long,
) {
    /**
     * Sole builder-handle authority field.
     *
     * Every transition to `COMMITTED` or `ABORTED` must race through this word.
     */
    @Volatile
    private var handleWord: Int = encodeInitialWord()

    /**
     * Intrusive linkage for slot-local builder-handle registry traversal.
     *
     * This linkage is operational only and must never be treated as semantic authority.
     */
    @Volatile
    var next: BuilderHandleCell? = null

    /**
     * Immutable monotonic supervisory deadline.
     *
     * OPEN is supervision-bound by contract. Therefore a handle episode cannot be issued
     * without a meaningful deadline.
     */
    private val supervisoryDeadlineNanos: Long = supervisoryDeadlineNanos

    /**
     * Immutable generation tag for stale-reference defense and future reclamation logic.
     */
    private val generation: Long = generation

    /**
     * Reads the current builder-handle top-level state with acquire semantics.
     */
    fun readStateAcquire(): BuilderHandleState {
        val word = HANDLE_WORD_HANDLE.getAcquire(this) as Int
        return decodeState(word)
    }

    /**
     * Returns true if this builder-handle episode already converged to a terminal state.
     */
    fun isTerminalAcquire(): Boolean = readStateAcquire().isTerminal

    /**
     * Returns the immutable supervisory deadline.
     */
    fun readSupervisoryDeadline(): Long = supervisoryDeadlineNanos

    /**
     * Returns the immutable generation tag.
     */
    fun readGeneration(): Long = generation

    /**
     * Returns true if the handle is still OPEN and the given monotonic time has reached
     * or passed the supervisory deadline.
     *
     * This method does not itself perform terminalization.
     * It only answers whether supervisory force-convergence is now permitted.
     */
    fun isOverdueAcquire(nowNanos: Long): Boolean {
        if (nowNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "BuilderHandleCell.isOverdueAcquire requires monotonic nanos >= 0: $nowNanos",
            )
        }

        val state = readStateAcquire()
        if (state != BuilderHandleState.OPEN) {
            return false
        }

        return nowNanos >= supervisoryDeadlineNanos
    }

    /**
     * Attempts normal builder commit convergence.
     *
     * ## Enforcement model
     *
     * [L2LifecycleLaw.requireTransition] asserts that `OPEN -> COMMITTED`
     * is a legal edge in the abstract lifecycle law.
     *
     * The dynamic current-state truth remains the CAS on [handleWord].
     *
     * Returns `true` only if this caller won the terminalization race.
     */
    fun tryCommit(): Boolean {
        L2LifecycleLaw.requireTransition(
            BuilderHandleState.OPEN,
            BuilderHandleState.COMMITTED,
        )

        while (true) {
            val observed = HANDLE_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != BuilderHandleState.OPEN) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = BuilderHandleState.COMMITTED,
                )

            if (HANDLE_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    /**
     * Attempts normal builder abort convergence.
     *
     * Returns `true` only if this caller won the terminalization race.
     */
    fun tryAbort(): Boolean {
        L2LifecycleLaw.requireTransition(
            BuilderHandleState.OPEN,
            BuilderHandleState.ABORTED,
        )

        while (true) {
            val observed = HANDLE_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != BuilderHandleState.OPEN) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = BuilderHandleState.ABORTED,
                )

            if (HANDLE_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    /**
     * Attempts supervisory force-abort for an overdue OPEN handle.
     *
     * ## Why this exists
     *
     * The builder lifecycle must not depend on indefinite cooperative progress.
     * If an issued OPEN handle becomes abandoned or overdue, the runtime must still
     * have a lawful convergence path.
     *
     * This method provides that path.
     *
     * ## Semantics
     *
     * - first checks whether supervisory abort is currently allowed,
     * - then races through the same authority field as ordinary convergence,
     * - and returns `true` only if the supervisor won.
     *
     * A failed CAS after overdue detection is legal and simply means another contender
     * converged the handle first.
     */
    fun tryForceAbortIfOverdue(nowNanos: Long): Boolean {
        if (!isOverdueAcquire(nowNanos)) {
            return false
        }

        L2LifecycleLaw.requireTransition(
            BuilderHandleState.OPEN,
            BuilderHandleState.ABORTED,
        )

        while (true) {
            val observed = HANDLE_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != BuilderHandleState.OPEN) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = BuilderHandleState.ABORTED,
                )

            if (HANDLE_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    companion object {
        private const val STATE_MASK: Int = 0b0011

        private val LOOKUP = MethodHandles.lookup()

        private val HANDLE_WORD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                BuilderHandleCell::class.java,
                "handleWord",
                Int::class.javaPrimitiveType,
            )

        /**
         * Factory-issued construction only.
         *
         * This keeps builder-handle issuance explicit and guarantees that no handle episode
         * exists without valid deadline / generation material.
         */
        @JvmStatic
        fun issue(
            supervisoryDeadlineNanos: Long,
            generation: Long,
        ): BuilderHandleCell {
            if (supervisoryDeadlineNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "BuilderHandleCell.supervisoryDeadlineNanos must be > 0: $supervisoryDeadlineNanos",
                )
            }
            if (generation < 0L) {
                throw PlanningProtocolIntegrityException(
                    "BuilderHandleCell.generation must be >= 0: $generation",
                )
            }

            return BuilderHandleCell(
                supervisoryDeadlineNanos = supervisoryDeadlineNanos,
                generation = generation,
            )
        }

        private fun encodeInitialWord(): Int = BuilderHandleState.OPEN.code

        private fun decodeState(word: Int): BuilderHandleState = BuilderHandleState.fromCode(word and STATE_MASK)

        private fun encodeStatePreservingFlags(
            currentWord: Int,
            newState: BuilderHandleState,
        ): Int {
            val flags = currentWord and STATE_MASK.inv()
            return flags or newState.code
        }
    }
}
