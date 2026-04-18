package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.exception.MetamodelFactOwnershipMismatchException
import metamodel.domain.structure.MetamodelFactSequence
import metamodel.domain.vo.DeclarationOrdinal

/**
 * Raw normalized constructor candidate fact.
 *
 * This DTO represents a constructor candidate before Core-owned semantic selection.
 *
 * It must not encode:
 * - selected constructor result
 * - capability-demotion result
 * - projection result
 * - traversal order
 *
 * The parameter collection is a MetamodelFactSequence, not a generic List.
 * It is compact-index validated and deterministically ordered by parameterIndex.
 */
class ConstructorCandidateFact private constructor(
    val ownerTypeFqcn: String,
    val constructorSignature: String,
    val constructorSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val visibility: VisibilityKind,
    val origin: MemberOrigin,
    val parameters: MetamodelFactSequence<ConstructorParameterFact>
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            constructorSignature: String,
            constructorSignatureNormalizationVersion: Long,
            declarationOrdinal: DeclarationOrdinal,
            visibility: VisibilityKind,
            origin: MemberOrigin,
            parameters: Collection<ConstructorParameterFact>
        ): ConstructorCandidateFact {
            validateCanonicalComponent("ConstructorCandidateFact.ownerTypeFqcn", ownerTypeFqcn)
            validateCanonicalComponent("ConstructorCandidateFact.constructorSignature", constructorSignature)

            if (constructorSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorCandidateFact",
                    reason = "constructorSignatureNormalizationVersion must be >= 0: " +
                            constructorSignatureNormalizationVersion
                )
            }

            /*
             * Validate ownership before deterministic sequencing.
             *
             * If an adapter emits a parameter owned by another type, the error is
             * fact-boundary corruption and should be reported before any ordering
             * or compact-index validation work is performed.
             */
            validateParameterOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                parameters = parameters
            )

            val orderedParameters = MetamodelFactSequence.compactIndexedBy(
                owner = ownerTypeFqcn,
                factKind = "ConstructorParameterFact",
                indexName = "parameterIndex",
                elements = parameters,
                indexOf = { fact -> fact.parameterIndex }
            )

            return ConstructorCandidateFact(
                ownerTypeFqcn = ownerTypeFqcn,
                constructorSignature = constructorSignature,
                constructorSignatureNormalizationVersion = constructorSignatureNormalizationVersion,
                declarationOrdinal = declarationOrdinal,
                visibility = visibility,
                origin = origin,
                parameters = orderedParameters
            )
        }

        private fun validateParameterOwnership(
            ownerTypeFqcn: String,
            parameters: Collection<ConstructorParameterFact>
        ) {
            val iterator = parameters.iterator()
            while (iterator.hasNext()) {
                val parameter = iterator.next()

                if (parameter.ownerTypeFqcn != ownerTypeFqcn) {
                    throw MetamodelFactOwnershipMismatchException(
                        expectedOwner = ownerTypeFqcn,
                        actualOwner = parameter.ownerTypeFqcn,
                        factKind = "ConstructorParameterFact"
                    )
                }
            }
        }
    }
}