package discovery.adapter

import discovery.domain.exception.DiscoveryFailedException
import discovery.domain.exception.DiscoveryViolation
import discovery.domain.exception.DiscoveryViolation.ViolationKind
import discovery.domain.exception.RuntimeIntegrityException
import discovery.domain.policy.SpecViolationCode
import java.util.Collections

/**
 * [Error Taxonomy & Reporting]
 * Handles error classification, message mapping, and violation creation.
 */
internal class DiscoveryTaxonomy {
    private val violations = ArrayList<DiscoveryViolation>()

    fun reportUserError(
        className: String,
        sourceFile: String?,
        context: String,
        kind: ViolationKind,
        message: String,
    ) {
        val loc = sourceFile?.let { "$it:1" }
        val dto =
            DiscoveryViolation(
                className = className,
                sourceLocation = loc,
                context = context,
                kind = kind,
                message = message,
            )
        violations.add(dto)
    }

    fun reportSpecViolation(
        className: String,
        sourceFile: String?,
        code: SpecViolationCode,
    ) {
        // [Message Mapping] Adapter controls the UX/wording, Domain controls the Rules.
        val message =
            when (code) {
                SpecViolationCode.CONTRACT_MUST_BE_INTERFACE ->
                    "@Contract must be placed on an Interface."

                SpecViolationCode.DATA_CONTRACT_MUST_BE_CONCRETE ->
                    "@DataContract must be placed on a concrete class (not interface/abstract/enum)."
            }
        reportUserError(className, sourceFile, "Annotation Misuse", ViolationKind.PROTOCOL_VIOLATION, message)
    }

    fun throwIfFailed() {
        if (violations.isNotEmpty()) {
            Collections.sort(violations) // Total Order
            throw DiscoveryFailedException(ArrayList(violations))
        }
    }

    fun wrapInfrastructureError(
        phase: String,
        target: String?,
        cause: Throwable,
    ): Nothing {
        val targetInfo = target?.let { " (target=$it)" } ?: ""
        throw RuntimeIntegrityException(
            "Classpath linkage failure or scanner error during $phase$targetInfo.",
            cause,
        )
    }

    fun wrapInternalBug(
        name: String,
        cause: Throwable,
    ): Nothing =
        throw RuntimeIntegrityException(
            "Internal order violation during discovery (bug). Invalid Class Name: '$name'",
            cause,
        )
}
