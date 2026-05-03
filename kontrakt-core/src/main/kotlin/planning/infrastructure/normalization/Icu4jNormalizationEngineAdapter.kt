package planning.infrastructure.normalization

import com.ibm.icu.text.Normalizer2
import com.ibm.icu.util.VersionInfo
import metamodel.domain.port.outgoing.NormalizationEngine
import planning.domain.exception.EnvironmentIntegrityException

/**
 * Outgoing Adapter: ICU4J-based NFC predicate engine.
 *
 * Why ICU4J:
 * - JDK normalization behavior can drift with JDK/vendor/Unicode updates.
 * - ICU is versioned as a library artifact, so we can pin it precisely.
 *
 * Fail-Closed pinning:
 * - We validate the runtime ICU version matches the expected pinned version.
 * - If it does not match, we throw immediately (environment violation).
 */
class Icu4jNormalizationEngineAdapter(
    private val expectedIcuVersionPrefix: String,
) : NormalizationEngine {
    override val engineId: String = "icu4j"

    /**
     * ICU version string as reported by ICU4J at runtime.
     *
     * Note: ICU may render as "78.2" or "78.2.0.0" depending on API; we pin by prefix.
     */
    override val engineVersion: String = VersionInfo.ICU_VERSION.toString()

    private val nfc: Normalizer2 = Normalizer2.getNFCInstance()

    init {
        if (!engineVersion.startsWith(expectedIcuVersionPrefix)) {
            throw EnvironmentIntegrityException(
                "ICU4J version mismatch. Expected prefix=$expectedIcuVersionPrefix, actual=$engineVersion",
            )
        }
    }

    override fun isNfc(input: String): Boolean = nfc.isNormalized(input)
}
