package realization.planning.projection

import stage.lowering.diagnostics.AmbiguousActiveMemberOrderingException
import stage.lowering.diagnostics.DuplicateActiveMemberKeyException

/**
 * Core-owned active-member orderer.
 *
 * Responsibilities:
 * - canonical active-member uniqueness verification
 * - entropy-target uniqueness verification
 * - canonical ordering ratification
 * - OrderedActiveMembers freeze
 *
 * Non-responsibilities:
 * - raw fact discovery
 * - constructor selection
 * - property demotion
 * - traversal execution
 * - interner key issuance
 */
class ActiveMemberOrderer private constructor() {
    fun order(projection: ActiveMemberProjectionResult): OrderedActiveMembers {
        val members = ArrayList<ProjectedActiveMember>(projection.members.size)
        projection.members.copyTo(members)

        verifyCanonicalKeyUniqueness(
            ownerTypeFqcn = projection.ownerTypeFqcn,
            members = members,
        )

        verifyEntropyTargetUniqueness(
            ownerTypeFqcn = projection.ownerTypeFqcn,
            members = members,
        )

        members.sortWith(MEMBER_COMPARATOR)

        assertNoOrderingTiesAfterCanonicalUniqueness(
            ownerTypeFqcn = projection.ownerTypeFqcn,
            members = members,
        )

        return OrderedActiveMembers.issueFromOrderedMembers(members)
    }

    private fun verifyCanonicalKeyUniqueness(
        ownerTypeFqcn: String,
        members: List<ProjectedActiveMember>,
    ) {
        verifyUniqueKeys(
            ownerTypeFqcn = ownerTypeFqcn,
            keyKind = "CanonicalActiveMemberKey",
            members = members,
            keyOf = { member -> CanonicalActiveMemberKey.issue(member) },
            keyComparator = CanonicalActiveMemberKey.comparator(),
            keyToString = { key -> key.render() },
        )
    }

    private fun verifyEntropyTargetUniqueness(
        ownerTypeFqcn: String,
        members: List<ProjectedActiveMember>,
    ) {
        verifyUniqueKeys(
            ownerTypeFqcn = ownerTypeFqcn,
            keyKind = "EntropyTargetKey",
            members = members,
            keyOf = { member -> EntropyTargetKey.issue(member) },
            keyComparator = EntropyTargetKey.comparator(),
            keyToString = { key -> key.render() },
        )
    }

    private fun <K : Any> verifyUniqueKeys(
        ownerTypeFqcn: String,
        keyKind: String,
        members: List<ProjectedActiveMember>,
        keyOf: (ProjectedActiveMember) -> K,
        keyComparator: Comparator<in K>,
        keyToString: (K) -> String,
    ) {
        if (members.size < 2) {
            return
        }

        val sorted = ArrayList<ProjectedActiveMember>(members.size)

        var i = 0
        while (i < members.size) {
            sorted.add(members[i])
            i++
        }

        sorted.sortWith(
            Comparator { left, right ->
                keyComparator.compare(keyOf(left), keyOf(right))
            },
        )

        var cursor = 1
        while (cursor < sorted.size) {
            val leftKey = keyOf(sorted[cursor - 1])
            val rightKey = keyOf(sorted[cursor])

            if (keyComparator.compare(leftKey, rightKey) == 0) {
                throw DuplicateActiveMemberKeyException(
                    ownerTypeFqcn = ownerTypeFqcn,
                    keyKind = keyKind,
                    duplicateKey = keyToString(leftKey),
                )
            }

            cursor++
        }
    }

    /**
     * This assertion is redundant under the current key/comparator model because
     * CanonicalActiveMemberKey uniqueness has already been verified and
     * MEMBER_COMPARATOR delegates to that key.
     *
     * It is intentionally retained as an executable invariant guard.
     *
     * If a future comparator change silently weakens canonical ordering strictness,
     * this method fails closed instead of allowing traversal over an ambiguous order.
     */
    private fun assertNoOrderingTiesAfterCanonicalUniqueness(
        ownerTypeFqcn: String,
        members: List<ProjectedActiveMember>,
    ) {
        var i = 1
        while (i < members.size) {
            val left = members[i - 1]
            val right = members[i]

            if (MEMBER_COMPARATOR.compare(left, right) == 0) {
                throw AmbiguousActiveMemberOrderingException(
                    ownerTypeFqcn = ownerTypeFqcn,
                    reason = "Canonical member comparator produced a tie after uniqueness verification.",
                )
            }

            i++
        }
    }

    companion object {
        private val MEMBER_COMPARATOR: Comparator<ProjectedActiveMember> =
            Comparator { left, right ->
                CanonicalActiveMemberKey.comparator().compare(
                    CanonicalActiveMemberKey.issue(left),
                    CanonicalActiveMemberKey.issue(right),
                )
            }

        @JvmStatic
        fun issue(): ActiveMemberOrderer = ActiveMemberOrderer()
    }
}
