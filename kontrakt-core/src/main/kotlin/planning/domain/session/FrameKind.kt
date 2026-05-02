package planning.domain.session

/**
 * Explicit frame kinds for the iterative DFS machine.
 *
 * Native method recursion is constitutionally forbidden.
 */
enum class FrameKind {
    PLAN_NODE,
    ITERATE_MEMBERS,
    EXPAND_EDGE,
    ALLOCATE,
}
