package planning.domain.runtime.orchestration

/**
 * Immutable restart descriptor for one planning-run suspension site.
 *
 * A resume point:
 * - MUST be immutable,
 * - MUST identify its site family,
 * - MUST identify its payload schema version,
 * - MUST NOT retain PlannerSession,
 * - MUST NOT retain worker-local primitive arrays/slabs,
 * - MUST NOT retain mutable frame-stack state as suspended execution state.
 *
 * Concrete resume-point payloads are expected to be introduced incrementally
 * as planner-core suspension sites are lifted into explicit restartable sites.
 */
interface PlanningResumePoint {
    val siteId: PlanningResumeSiteId
    val schemaVersion: Int
}
