package planning.infrastructure.cache.adapter.outgoing.lifecycle

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Explicit transition law for adapter-level administrative lifecycle.
 *
 * This law exists to prevent drop/close coordination from degenerating into
 * scattered branch logic hidden inside the adapter implementation.
 */
internal object AdapterAdminLifecycleLaw {

    @JvmStatic
    fun canTransition(
        from: AdapterAdminState,
        to: AdapterAdminState,
    ): Boolean {
        return when (from) {
            AdapterAdminState.OPEN ->
                to == AdapterAdminState.DROP_IN_PROGRESS ||
                        to == AdapterAdminState.CLOSING

            AdapterAdminState.DROP_IN_PROGRESS ->
                to == AdapterAdminState.OPEN

            AdapterAdminState.CLOSING ->
                to == AdapterAdminState.CLOSED

            AdapterAdminState.CLOSED ->
                false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: AdapterAdminState,
        to: AdapterAdminState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal AdapterAdminState transition: $from -> $to"
            )
        }
    }

    @JvmStatic
    fun canStartDrop(
        state: AdapterAdminState,
    ): Boolean {
        return state == AdapterAdminState.OPEN
    }

    @JvmStatic
    fun canStartClose(
        state: AdapterAdminState,
    ): Boolean {
        return state == AdapterAdminState.OPEN
    }

    @JvmStatic
    fun isTerminal(
        state: AdapterAdminState,
    ): Boolean {
        return state.isTerminal
    }
}