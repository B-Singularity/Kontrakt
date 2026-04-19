package planning.domain.projection

import java.util.AbstractList

/**
 * Frozen ordered traversal input for one node-expansion episode.
 *
 * This is the only member collection that traversal frames may consume.
 *
 * It is already:
 * - projected,
 * - uniqueness-verified,
 * - canonically ordered,
 * - immutable.
 */
class OrderedActiveMembers private constructor(
    private val snapshot: Array<Any?>,
) : AbstractList<ProjectedActiveMember>(), RandomAccess {

    override val size: Int
        get() = snapshot.size

    override fun get(index: Int): ProjectedActiveMember {
        @Suppress("UNCHECKED_CAST")
        return snapshot[index] as ProjectedActiveMember
    }

    companion object {
        internal fun issueFromOrderedMembers(
            members: List<ProjectedActiveMember>,
        ): OrderedActiveMembers {
            val snapshot = arrayOfNulls<Any?>(members.size)

            var i = 0
            while (i < members.size) {
                snapshot[i] = members[i]
                i++
            }

            return OrderedActiveMembers(snapshot)
        }
    }
}