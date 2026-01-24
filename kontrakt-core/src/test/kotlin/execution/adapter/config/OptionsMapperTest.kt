package execution.adapter.config

import discovery.domain.vo.ScanScope
import execution.domain.vo.config.AuditDepth
import execution.domain.vo.config.LogRetention
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reporting.adapter.config.ReportFormat
import java.nio.file.Paths

class OptionsMapperTest {

    private val keysToClear = listOf(
        "kontrakt.trace",
        "kontrakt.archive",
        "kontrakt.seed",
        "kontrakt.reports.html",
        "kontrakt.reports.json",
        "kontrakt.reports.console"
    )

    @BeforeEach
    @AfterEach
    fun clearProperties() {
        keysToClear.forEach { System.clearProperty(it) }
    }

    @Test
    fun `fromSystemProperties should use default values when properties are missing`() {
        // Given: No system properties set (cleared in Setup)

        // When
        val options = UserControlOptions.fromSystemProperties()

        // Then
        assertThat(options.traceMode).isFalse()
        assertThat(options.archiveMode).isFalse()
        assertThat(options.seed).isNull()

        // Default Report Formats (Opt-out strategy: Enabled by default)
        assertThat(options.reportFormats).containsExactlyInAnyOrder(
            ReportFormat.HTML,
            ReportFormat.JSON,
            ReportFormat.CONSOLE
        )
    }

    @Test
    fun `fromSystemProperties should parse boolean flags and seed correctly`() {
        // Given
        System.setProperty("kontrakt.trace", "true")
        System.setProperty("kontrakt.archive", "true")
        System.setProperty("kontrakt.seed", "9999")

        // When
        val options = UserControlOptions.fromSystemProperties()

        // Then
        assertThat(options.traceMode).isTrue()
        assertThat(options.archiveMode).isTrue()
        assertThat(options.seed).isEqualTo(9999L)
    }

    @Test
    fun `fromSystemProperties should handle invalid seed gracefully`() {
        // Given: Seed is not a number
        System.setProperty("kontrakt.seed", "not-a-number")

        // When
        val options = UserControlOptions.fromSystemProperties()

        // Then
        assertThat(options.seed).isNull() // Should return null safely
    }

    @Test
    fun `fromSystemProperties should disable reports when explicitly set to false`() {
        // Given: All reports explicitly disabled
        System.setProperty("kontrakt.reports.html", "false")
        System.setProperty("kontrakt.reports.json", "false")
        System.setProperty("kontrakt.reports.console", "false")

        // When
        val options = UserControlOptions.fromSystemProperties()

        // Then
        assertThat(options.reportFormats).isEmpty()
    }

    @Test
    fun `fromSystemProperties should enable reports when set to true or explicit value`() {
        // Given
        System.setProperty("kontrakt.reports.html", "true")

        // "false"가 아니면 무조건 켜지는지 확인 (예: 오타나 임의의 문자열)
        System.setProperty("kontrakt.reports.json", "yes")

        System.setProperty("kontrakt.reports.console", "false")

        // When
        val options = UserControlOptions.fromSystemProperties()

        // Then
        assertThat(options.reportFormats).contains(ReportFormat.HTML, ReportFormat.JSON)
        assertThat(options.reportFormats).doesNotContain(ReportFormat.CONSOLE)
    }


    // =================================================================================================================
    // toExecutionPolicy() Tests
    // =================================================================================================================

    @Test
    fun `toExecutionPolicy should map retention to ALWAYS when archiveMode is enabled`() {
        val options = UserControlOptions(
            archiveMode = true,
            verbosity = UserControlOptions.Verbosity.NORMAL
        )

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.retention).isEqualTo(LogRetention.ALWAYS)
    }

    @Test
    fun `toExecutionPolicy should map retention to ALWAYS when verbosity is VERBOSE`() {
        val options = UserControlOptions(
            archiveMode = false,
            verbosity = UserControlOptions.Verbosity.VERBOSE
        )

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.retention).isEqualTo(LogRetention.ALWAYS)
    }

    @Test
    fun `toExecutionPolicy should map retention to ALWAYS when both archiveMode and VERBOSE are enabled`() {
        val options = UserControlOptions(
            archiveMode = true,
            verbosity = UserControlOptions.Verbosity.VERBOSE
        )

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.retention).isEqualTo(LogRetention.ALWAYS)
    }

    @Test
    fun `toExecutionPolicy should map retention to NONE when verbosity is QUIET`() {
        // archiveMode must be false to reach this branch
        val options = UserControlOptions(
            archiveMode = false,
            verbosity = UserControlOptions.Verbosity.QUIET
        )

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.retention).isEqualTo(LogRetention.NONE)
    }

    @Test
    fun `toExecutionPolicy should map retention to ON_FAILURE by default`() {
        val options = UserControlOptions(
            archiveMode = false,
            verbosity = UserControlOptions.Verbosity.NORMAL
        )

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.retention).isEqualTo(LogRetention.ON_FAILURE)
    }

    @Test
    fun `toExecutionPolicy should map depth to EXPLAINABLE when traceMode is enabled`() {
        val options = UserControlOptions(traceMode = true)

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.depth).isEqualTo(AuditDepth.EXPLAINABLE)
    }

    @Test
    fun `toExecutionPolicy should map depth to SIMPLE when traceMode is disabled`() {
        val options = UserControlOptions(traceMode = false)

        val policy = options.toExecutionPolicy()

        assertThat(policy.auditing.depth).isEqualTo(AuditDepth.SIMPLE)
    }

    @Test
    fun `toExecutionPolicy should map seed and use default timeout`() {
        val expectedSeed = 12345L
        val options = UserControlOptions(seed = expectedSeed)

        val policy = options.toExecutionPolicy()

        assertThat(policy.determinism.seed).isEqualTo(expectedSeed)
        assertThat(policy.resources.timeoutMs).isEqualTo(5000L)
    }

    // =================================================================================================================
    // toDiscoveryPolicy() Tests
    // =================================================================================================================

    @Test
    fun `toDiscoveryPolicy should map scope to Classes when testPatterns are present`() {
        val patterns = setOf("com.example.MyTest")
        val options = UserControlOptions(
            testPatterns = patterns,
            packageScope = "com.ignored" // Patterns take precedence
        )

        val policy = options.toDiscoveryPolicy()

        assertThat(policy.scope).isInstanceOf(ScanScope.Classes::class.java)
        assertThat((policy.scope as ScanScope.Classes).classNames).isEqualTo(patterns)
    }

    @Test
    fun `toDiscoveryPolicy should map scope to Packages when packageScope is provided and testPatterns are empty`() {
        val pkg = "com.example"
        val options = UserControlOptions(
            testPatterns = emptySet(),
            packageScope = pkg
        )

        val policy = options.toDiscoveryPolicy()

        assertThat(policy.scope).isInstanceOf(ScanScope.Packages::class.java)
        assertThat((policy.scope as ScanScope.Packages).packageNames).containsExactly(pkg)
    }

    @Test
    fun `toDiscoveryPolicy should map scope to All when neither patterns nor packageScope are provided`() {
        val options = UserControlOptions(
            testPatterns = emptySet(),
            packageScope = null
        )

        val policy = options.toDiscoveryPolicy()

        assertThat(policy.scope).isEqualTo(ScanScope.All)
    }

    @Test
    fun `toDiscoveryPolicy should map scope to All when packageScope is blank`() {
        val options = UserControlOptions(
            testPatterns = emptySet(),
            packageScope = "   "
        )

        val policy = options.toDiscoveryPolicy()

        assertThat(policy.scope).isEqualTo(ScanScope.All)
    }

    // =================================================================================================================
    // toReportingDirectives() Tests
    // =================================================================================================================

    @Test
    fun `toReportingDirectives should map all configuration fields correctly`() {
        val options = UserControlOptions(
            verbosity = UserControlOptions.Verbosity.VERBOSE,
            archiveMode = true,
            stackTraceLimit = 50
        )

        val directives = options.toReportingDirectives()

        assertThat(directives.baseReportDir).isEqualTo(Paths.get("build", "reports", "kontrakt"))
        assertThat(directives.verbose).isTrue()
        assertThat(directives.archiveMode).isTrue()
        assertThat(directives.stackTraceLimit).isEqualTo(50)
        assertThat(directives.formats).containsExactlyInAnyOrder(
            ReportFormat.CONSOLE, ReportFormat.HTML, ReportFormat.JSON
        )
    }

    @Test
    fun `toReportingDirectives should set verbose to false when verbosity is not VERBOSE`() {
        val options = UserControlOptions(
            verbosity = UserControlOptions.Verbosity.NORMAL
        )

        val directives = options.toReportingDirectives()

        assertThat(directives.verbose).isFalse()
    }
}