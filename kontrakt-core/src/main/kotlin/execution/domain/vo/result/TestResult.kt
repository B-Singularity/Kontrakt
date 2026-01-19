package execution.domain.vo.result

import discovery.domain.vo.DiscoveredTestTarget
import execution.domain.vo.verification.AssertionRecord
import java.time.Duration

data class TestResult(
    val target: DiscoveredTestTarget,
    val finalStatus: TestStatus,
    val duration: Duration,
    val assertionRecords: List<AssertionRecord>,
)
