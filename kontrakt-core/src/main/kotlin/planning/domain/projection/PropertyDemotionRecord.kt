package planning.domain.projection

import metamodel.domain.dto.PropertyFact

/**
 * Structured deterministic property demotion evidence.
 *
 * This evidence is diagnostic/protocol material.
 * It must be deterministically ordered before rendering/truncation by any later
 * diagnostic pipeline.
 */
class PropertyDemotionRecord private constructor(
    val ownerTypeFqcn: String,
    val propertyName: String,
    val reason: PropertyDemotionReason,
    val property: PropertyFact,
) {
    companion object {
        @JvmStatic
        fun issue(
            property: PropertyFact,
            reason: PropertyDemotionReason,
        ): PropertyDemotionRecord =
            PropertyDemotionRecord(
                ownerTypeFqcn = property.ownerTypeFqcn,
                propertyName = property.name,
                reason = reason,
                property = property,
            )
    }
}
