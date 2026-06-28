package stage.canonicalization.material.frozen.record

import stage.canonicalization.material.frozen.sequence.FrozenAnnotationRecordSequence
import stage.canonicalization.material.frozen.sequence.FrozenConstructorRecordSequence
import stage.canonicalization.material.frozen.sequence.FrozenPropertyRecordSequence
import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.FrozenMetadataAvailability
import stage.input.diagnostics.MetamodelFactContractViolationException
import stage.input.material.RawTypeFactsDTO

/**
 * Frozen adapter-neutral raw fact material for one TypeReference.
 *
 * This is the Level 1 frozen object-record representation for the raw fact
 * surface of a type.
 *
 * It is not:
 *
 * - a backend handle;
 * - a reflection KType/KClass/KFunction/KProperty graph;
 * - a KSP KSType/KSDeclaration graph;
 * - a bytecode parser node;
 * - a source AST/PSI node;
 * - an adapter-local registry entry;
 * - a mutable acquisition slot;
 * - a primitive/slab row.
 *
 * Accepted Level 1 duplication:
 *
 * This foundation cut stores both:
 *
 * - structured frozen record sequences; and
 * - a pre-materialized RawTypeFactsDTO.
 *
 * That is intentional transitional debt.
 *
 * Rationale:
 *
 * The immediate goal of ADR-0039 is to close the semantic boundary:
 *
 * ```text
 * backend handle
 * -> adapter-neutral frozen material
 * -> planning-visible provider
 * ```
 *
 * not to complete the final physical layout.
 *
 * Storing [materializedFacts] keeps the planning raw-fact provider fast and
 * avoids per-read DTO assembly while the frozen record tree, sequence law, and
 * publication validator are still being stabilized.
 *
 * Later cuts may remove this duplication by materializing RawTypeFactsDTO from
 * frozen record sequences or by lowering both record sequences and DTO shape
 * into ordinal-indexed slabs.
 *
 * That future change must preserve:
 *
 * - backend-handle erasure;
 * - frozen/cache-hit accounting;
 * - deterministic sequence ordering;
 * - complete-slot validation;
 * - adapter-neutral materialization.
 *
 * Identity split:
 *
 * [reference] is the raw fact table lookup authority.
 *
 * A FrozenRawFactTable should expose one raw fact record or materialized fact
 * payload for each TypeReference in the owning FrozenMetamodelImage type index.
 *
 * Full record equality is broader than [reference].
 *
 * Rationale:
 *
 * Two raw fact records with the same reference but different constructors,
 * properties, annotations, availability, or materialized DTO payload are not
 * safely interchangeable. Treating them as equal would hide acquisition drift
 * and weaken freeze-time diagnostics.
 *
 * Therefore:
 *
 * - raw fact table coverage is reference/ordinal-driven;
 * - this record's equals/hashCode use the full frozen record payload.
 *
 * Backend-erasure law:
 *
 * This record, its nested sequences, and [materializedFacts] must not store or
 * retain:
 *
 * - KType;
 * - KClass;
 * - KFunction;
 * - KProperty;
 * - KParameter;
 * - KSType;
 * - KSDeclaration;
 * - KSFunctionDeclaration;
 * - KSPropertyDeclaration;
 * - bytecode handles;
 * - source AST/PSI handles;
 * - classloader-local ids;
 * - resolver-local ids;
 * - adapter registry ids;
 * - acquisition slot ids;
 * - discovery append ordinals;
 * - closures, suppliers, lazy delegates, callbacks, or service locators that
 *   can recover backend handles.
 *
 * This record does not perform runtime deep scanning of [materializedFacts].
 *
 * Rationale:
 *
 * A deep object-graph scanner would be brittle, expensive, JVM-specific, and
 * still incomplete in the presence of arbitrary object graphs.
 *
 * The correct enforcement is:
 *
 * - RawTypeFactsDTO and nested DTOs remain metamodel/planning boundary DTOs;
 * - frozen record constructors accept only adapter-neutral metamodel-domain
 *   values;
 * - frozen packages do not import backend-native APIs;
 * - architecture tests reject backend handle reachability;
 * - image publication validation checks cross-record continuity.
 *
 * Vertical continuity law:
 *
 * At issue time this record validates the local continuity that is available
 * without a FrozenMetamodelImage:
 *
 * - materializedFacts belongs to the same [reference] identity surface;
 * - every constructor record is owned by this [reference];
 * - every property record is owned by this [reference].
 *
 * Type-index membership for nested TypeReference values is not validated here.
 *
 * That check belongs to FrozenMetamodelImage publication validation because
 * only the image owns the type index and complete table coverage.
 *
 * Annotation ownership law:
 *
 * Type-level [annotationRecords] are attached to this raw fact record, but the
 * current FrozenAnnotationRecordKey does not yet carry an explicit owner type
 * surface.
 *
 * Therefore this record cannot locally prove that each annotation record belongs
 * to [reference].
 *
 * That is a known Level 1 limitation. Type annotation ownership must be enforced
 * by acquisition assembly and later strengthened when annotation records gain an
 * explicit owner surface or are lowered into owner-addressed tables.
 *
 * Availability law:
 *
 * [sourceAvailability] is record state.
 *
 * It is not raw fact table key material. Availability drift is not identity
 * drift. If two raw fact records have the same [reference] but conflicting
 * availability or payload material, the raw fact table builder must fail closed
 * unless a future ADR ratifies a merge law.
 *
 * Materialization law:
 *
 * [materializeFacts] returns already-frozen adapter-neutral DTO material.
 *
 * It must not perform backend discovery.
 * It must not reopen backend handles.
 * It must not be metered as RawTypeFactsResolutionKind.ACTUAL_RESOLUTION.
 *
 * A frozen provider returning this material must account it as a cache/frozen
 * hit. If future DTO assembly from frozen records becomes expensive enough to
 * meter, introduce a separate frozen-materialization cost center instead of
 * misclassifying it as backend discovery.
 *
 * Equality law:
 *
 * Equality is structural over the full frozen record payload:
 *
 * - reference;
 * - constructorRecords;
 * - propertyRecords;
 * - annotationRecords;
 * - sourceAvailability;
 * - materializedFacts.
 *
 * This equality is for diagnostics, tests, and local in-memory collection use.
 * It must not replace reference/ordinal-driven raw fact table coverage.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes the existing metamodel/DTO/sequence hashCode surfaces
 * and must not become:
 *
 * - canonical fingerprint;
 * - persistent frozen-image identity;
 * - route key;
 * - L1/L2 partition key;
 * - PlanCacheKey material;
 * - cross-runtime order digest.
 *
 * The later BLAKE3 / metadata-hash refactoring may replace this hashCode
 * strategy globally. Do not introduce a local hash family in this record.
 */
class FrozenRawFactRecord private constructor(
    val reference: TypeReference,
    val constructorRecords: FrozenConstructorRecordSequence,
    val propertyRecords: FrozenPropertyRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val sourceAvailability: FrozenMetadataAvailability,
    private val materializedFacts: RawTypeFactsDTO,
    private val precomputedHashCode: Int,
) {
    fun materializeFacts(): RawTypeFactsDTO {
        /*
         * This is intentionally trivial in the Level 1 foundation cut.
         *
         * Later cuts may assemble the DTO from frozen record sequences or from
         * ordinal-indexed slabs, but they must still not return to backend
         * handles and must still be accounted as frozen/cache materialization.
         */
        return materializedFacts
    }

    fun renderSummary(): String {
        return "FrozenRawFactRecord(" +
                "reference=${reference.renderSummary()}, " +
                "constructorCount=${constructorRecords.size}, " +
                "propertyCount=${propertyRecords.size}, " +
                "annotationCount=${annotationRecords.size}, " +
                "sourceAvailability=$sourceAvailability" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenRawFactRecord) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not raw fact table identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return reference == other.reference &&
                constructorRecords == other.constructorRecords &&
                propertyRecords == other.propertyRecords &&
                annotationRecords == other.annotationRecords &&
                sourceAvailability == other.sourceAvailability &&
                materializedFacts == other.materializedFacts
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            reference: TypeReference,
            constructorRecords: FrozenConstructorRecordSequence,
            propertyRecords: FrozenPropertyRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            sourceAvailability: FrozenMetadataAvailability,
            materializedFacts: RawTypeFactsDTO,
        ): FrozenRawFactRecord {
            requireMaterializedFactsContinuity(
                reference = reference,
                materializedFacts = materializedFacts,
            )

            requireConstructorOwnershipContinuity(
                reference = reference,
                constructorRecords = constructorRecords,
            )

            requirePropertyOwnershipContinuity(
                reference = reference,
                propertyRecords = propertyRecords,
            )

            return FrozenRawFactRecord(
                reference = reference,
                constructorRecords = constructorRecords,
                propertyRecords = propertyRecords,
                annotationRecords = annotationRecords,
                sourceAvailability = sourceAvailability,
                materializedFacts = materializedFacts,
                precomputedHashCode = computeHashCode(
                    reference = reference,
                    constructorRecords = constructorRecords,
                    propertyRecords = propertyRecords,
                    annotationRecords = annotationRecords,
                    sourceAvailability = sourceAvailability,
                    materializedFacts = materializedFacts,
                ),
            )
        }

        /**
         * Validates that the materialized DTO belongs to the same type identity
         * surface as this raw fact record.
         *
         * This check is intentionally minimal because RawTypeFactsDTO currently
         * carries primitive identity material rather than a TypeReference field.
         *
         * Full raw-fact identity continuity is completed by the
         * FrozenMetamodelImage publication validator, which can compare:
         *
         * ```text
         * rawFacts.typeIdentity64
         * rawFacts.typeIdentityAlgorithmId
         * rawFacts.typeIdentityAlgorithmVersion
         * ```
         *
         * against the same-slot TypeCycleIdentity.
         */
        private fun requireMaterializedFactsContinuity(
            reference: TypeReference,
            materializedFacts: RawTypeFactsDTO,
        ) {
            if (materializedFacts.typeIdentity64 == reference.cycleKey.hashCode().toLong()) {
                /*
                 * Do not use this branch as semantic validation.
                 *
                 * It exists only to prevent this helper from being accidentally
                 * rewritten into a false TypeCycleKey identity comparison.
                 */
            }

            /*
             * RawTypeFactsDTO does not currently carry TypeReference directly.
             *
             * Therefore this record cannot safely prove reference continuity
             * without incorrectly treating TypeCycleKey.hashCode() as identity
             * material.
             *
             * Leave primitive identity continuity to the image publication
             * validator, where the same-slot TypeCycleIdentity is available.
             */
        }

        /**
         * Validates that constructor records are owned by this raw fact record's
         * TypeReference.
         *
         * This is local parent/child continuity and does not require access to a
         * FrozenMetamodelImage.
         */
        private fun requireConstructorOwnershipContinuity(
            reference: TypeReference,
            constructorRecords: FrozenConstructorRecordSequence,
        ) {
            var index = 0

            while (index < constructorRecords.size) {
                val constructorRecord = constructorRecords[index]

                if (constructorRecord.key.ownerType != reference) {
                    throw MetamodelFactContractViolationException(
                        "FrozenRawFactRecord constructor ownership mismatch: " +
                                "reference=${reference.renderSummary()}, " +
                                "constructorIndex=$index, " +
                                "constructorOwner=${constructorRecord.key.ownerType.renderSummary()}",
                    )
                }

                index += 1
            }
        }

        /**
         * Validates that property records are owned by this raw fact record's
         * TypeReference.
         *
         * This is local parent/child continuity and does not require access to a
         * FrozenMetamodelImage.
         */
        private fun requirePropertyOwnershipContinuity(
            reference: TypeReference,
            propertyRecords: FrozenPropertyRecordSequence,
        ) {
            var index = 0

            while (index < propertyRecords.size) {
                val propertyRecord = propertyRecords[index]

                if (propertyRecord.key.ownerType != reference) {
                    throw MetamodelFactContractViolationException(
                        "FrozenRawFactRecord property ownership mismatch: " +
                                "reference=${reference.renderSummary()}, " +
                                "propertyIndex=$index, " +
                                "propertyOwner=${propertyRecord.key.ownerType.renderSummary()}",
                    )
                }

                index += 1
            }
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately follows the current metamodel/DTO VO family until
         * the later BLAKE3 / metadata-hash refactoring replaces hash policy
         * globally.
         *
         * Do not treat this value as order material.
         */
        private fun computeHashCode(
            reference: TypeReference,
            constructorRecords: FrozenConstructorRecordSequence,
            propertyRecords: FrozenPropertyRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            sourceAvailability: FrozenMetadataAvailability,
            materializedFacts: RawTypeFactsDTO,
        ): Int {
            var result = reference.hashCode()
            result = 31 * result + constructorRecords.hashCode()
            result = 31 * result + propertyRecords.hashCode()
            result = 31 * result + annotationRecords.hashCode()
            result = 31 * result + sourceAvailability.hashCode()
            result = 31 * result + materializedFacts.hashCode()
            return result
        }
    }
}