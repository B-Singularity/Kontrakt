package stage.input.material

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import ir.structure.DeterministicList
import java.util.Objects

/**
 * Execution intent mode.
 *
 * Determinism:
 * - Comparable is implemented so modes can be placed into deterministic structures.
 *
 * Note:
 * - Current specification enforces EXACTLY ONE mode at TestSpecification construction time.
 */
sealed interface TestMode : Comparable<TestMode> {
    data object UserScenario : TestMode {
        override fun compareTo(other: TestMode): Int = if (other is UserScenario) 0 else -1
    }

    class ContractAuto private constructor(
        val contractTypes: DeterministicList<TypeId>,
    ) : TestMode {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return contractTypes == (other as ContractAuto).contractTypes
        }

        override fun hashCode(): Int = Objects.hash(contractTypes)

        override fun toString(): String = "ContractAuto($contractTypes)"

        override fun compareTo(other: TestMode): Int {
            if (other !is ContractAuto) return 1

            val minSize = minOf(contractTypes.size, other.contractTypes.size)
            for (i in 0 until minSize) {
                val cmp = contractTypes[i].compareTo(other.contractTypes[i])
                if (cmp != 0) return cmp
            }
            return contractTypes.size - other.contractTypes.size
        }

        companion object {
            fun of(types: Collection<TypeId>): ContractAuto {
                if (types.isEmpty()) throw IrProtocolViolationException("ContractAuto requires at least one contract type.")
                val safeList = DeterministicList.of(types, IrLimits.MAX_CONTRACT_TYPES)
                return ContractAuto(safeList)
            }
        }
    }

    class DataCompliance private constructor(
        val dataContractType: TypeId,
    ) : TestMode {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return dataContractType == (other as DataCompliance).dataContractType
        }

        override fun hashCode(): Int = Objects.hash(dataContractType)

        override fun toString(): String = "DataCompliance($dataContractType)"

        override fun compareTo(other: TestMode): Int {
            if (other !is DataCompliance) return if (other is ContractAuto) -1 else 1
            return dataContractType.compareTo(other.dataContractType)
        }

        companion object {
            fun of(type: TypeId): DataCompliance = DataCompliance(type)
        }
    }
}
