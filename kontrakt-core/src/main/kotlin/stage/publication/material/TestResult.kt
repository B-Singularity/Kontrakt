package stage.publication.material

import stage.invariant.judgment.AssertionRecord
import java.time.Duration

data class TestResult(
    val target: DiscoveredTestTarget,
    val finalStatus: TestStatus,
    val duration: Duration,
    val assertionRecords: List<AssertionRecord>,
)
