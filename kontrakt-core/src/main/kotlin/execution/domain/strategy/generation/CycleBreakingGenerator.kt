package execution.domain.strategy.generation

import execution.domain.exception.RuntimeInstantiationException
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.generation.TruncationRecord
import execution.domain.vo.generation.TruncationRule
import execution.domain.vo.plan.UnlinkedCycleNode
import execution.port.outgoing.RuntimeInstantiator
import execution.port.outgoing.RuntimeTypeHandle
import ir.Attribute
import metamodel.domain.model.TypeDescriptor
import stage.diagnostic.evidence.TracePhase
import stage.input.material.TypeKind

class CycleBreakingGenerator(
    private val descriptor: TypeDescriptor,
    private val attributes: Map<String, Attribute>,
    private val diagnosticInfo: UnlinkedCycleNode,
    private val runtimeHandle: RuntimeTypeHandle,
    private val instantiator: RuntimeInstantiator,
) : Generator<Any?> {
    override fun generate(context: GenerationContext): Any? {
        val rule = decideRule()

        context.traceSink.emit(
            TruncationRecord(
                path = diagnosticInfo.pathSnapshot,
                edgeKind = diagnosticInfo.edgeKind,
                edgeName = diagnosticInfo.edgeName,
                typeId = diagnosticInfo.targetTypeId,
                rule = rule,
                suppressedConstraints = attributes.keys.toList(),
                timestamp = context.clock.millis(), // Injected
                phase = TracePhase.DESIGN, // BDD Phase: Given/Design
            ),
        )

        return when (rule) {
            TruncationRule.NULL -> null
            TruncationRule.EMPTY_COLLECTION -> instantiator.createEmptyCollection(runtimeHandle)
            TruncationRule.EMPTY_MAP -> instantiator.createEmptyMap(runtimeHandle)
            TruncationRule.EMPTY_ARRAY -> instantiator.createEmptyArray(runtimeHandle)
            TruncationRule.DIAGNOSTIC_STUB -> {
                instantiator.createDiagnosticStub(runtimeHandle, "Access denied to cycle stub: ${descriptor.name}")
            }

            TruncationRule.FAIL_FAST -> throw RuntimeInstantiationException(
                "Cycle detected on non-nullable concrete type: ${descriptor.name}. " +
                        "Kontrakt cannot safely instantiate this cycle. Consider making the field nullable or breaking the cycle in your test setup.",
            )
        }
    }

    private fun decideRule(): TruncationRule {
        if (descriptor.isNullable) return TruncationRule.NULL
        return when (descriptor.kind) {
            TypeKind.COLLECTION -> TruncationRule.EMPTY_COLLECTION
            TypeKind.ARRAY -> TruncationRule.EMPTY_ARRAY
            TypeKind.MAP -> TruncationRule.EMPTY_MAP
            TypeKind.INTERFACE -> TruncationRule.DIAGNOSTIC_STUB
            else -> TruncationRule.FAIL_FAST
        }
    }
}
