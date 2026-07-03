package migration.quarantine

import stage.input.presentation.raw.ScanIndex
import stage.input.presentation.raw.ScanScope

/**
 * [Outgoing Port]
 * Defines the contract for scanning the classpath to discover Kontrakt components.
 * This interface is agnostic of the underlying scanning technology (ClassGraph, KSP, etc.).
 */
interface ClasspathScanner {
    /**
     * Scans the specified scope and returns an immutable index of discovered components.
     * @throws stage.admission.diagnostics.evidence.RuntimeIntegrityException If the environment is corrupted.
     * @throws stage.admission.diagnostics.evidence.DiscoveryFailedException If user order violations are found.
     */
    fun scan(scope: ScanScope): ScanIndex
}