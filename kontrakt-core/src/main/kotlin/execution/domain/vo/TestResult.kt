package execution.domain.vo

import discovery.domain.vo.DiscoveredTestTarget
import execution.domain.TestStatus
import java.time.Duration

data class TestResult(
    val target: DiscoveredTestTarget,
    val finalStatus: TestStatus,
    val duration: Duration,
    val assertionRecords: List<AssertionRecord>,
)
