package discovery.port.outgoing

import discovery.domain.vo.ScanIndex
import discovery.domain.vo.ScanScope

/**
 * [Outgoing Port]
 * Defines the contract for scanning the classpath to discover Kontrakt components.
 * This interface is agnostic of the underlying scanning technology (ClassGraph, KSP, etc.).
 */
interface ClasspathScanner {
    /**
     * Scans the specified scope and returns an immutable index of discovered components.
     * @throws discovery.domain.exception.RuntimeIntegrityException If the environment is corrupted.
     * @throws discovery.domain.exception.DiscoveryFailedException If user order violations are found.
     */
    fun scan(scope: ScanScope): ScanIndex
}
