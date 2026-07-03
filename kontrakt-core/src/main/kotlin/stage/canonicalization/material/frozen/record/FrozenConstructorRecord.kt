package stage.canonicalization.material.frozen.record

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.material.frozen.FrozenMetadataAvailability
import stage.canonicalization.material.frozen.sequence.FrozenAnnotationRecordSequence
import stage.canonicalization.material.frozen.sequence.FrozenConstructorParameterRecordSequence

/**
 * Frozen constructor record.
 *
 * This is the Level 1 frozen object-record representation for one constructor
 * candidate after backend-neutral lowering.
 *
 * It is not:
 *
 * - a reflection KFunction;
 * - a reflection KParameter container;
 * - a KSP KSFunctionDeclaration;
 * - a bytecode method node;
 * - a source AST/PSI constructor declaration;
 * - an adapter-local slot;
 * - a discovery append ordinal;
 * - a primitive/slab row.
 *
 * Identity split:
 *
 * [key] is the primary constructor identity axis inside constructor sequences.
 *
 * Sequence duplicate detection must be key-driven:
 *
 * ```text
 * same FrozenConstructorRecordKey
 * -> duplicate constructor identity
 * -> reject or fail closed unless a future ADR ratifies a merge law
 * ```
 *
 * Full record equality is broader than key equality.
 *
 * Rationale:
 *
 * Two records with the same constructor key but different parameter records,
 * annotation records, declaration availability, or source availability are not
 * safely interchangeable. Treating them as equal would hide acquisition drift
 * and weaken freeze-time diagnostics.
 *
 * Therefore:
 *
 * - sequence builders use [key] as the duplicate-detection authority;
 * - this record's equals/hashCode use the full frozen record payload.
 *
 * Child ownership law:
 *
 * [parameterRecords] must contain only parameters owned by this constructor
 * key.
 *
 * For every parameter record:
 *
 * ```text
 * parameterRecord.key.ownerConstructorKey == key
 * ```
 *
 * This record validates that continuity at issue time because the parent key is
 * available locally and the check does not require a FrozenMetamodelImage.
 *
 * Annotation ownership law:
 *
 * Constructor-level [annotationRecords] are attached to this constructor
 * record, but the current FrozenAnnotationRecordKey does not yet carry an
 * explicit owner-constructor key.
 *
 * Therefore this record cannot prove constructor-annotation ownership locally.
 *
 * That is a known Level 1 limitation. Constructor annotation ownership must be
 * enforced by the acquisition path and later strengthened when annotation
 * record keys gain an explicit owner surface or when annotation records are
 * lowered into owner-addressed tables.
 *
 * Availability law:
 *
 * [declarationAvailability] and [sourceAvailability] are record state.
 *
 * They are not constructor key material.
 *
 * Availability drift is not identity drift. If two records have the same
 * semantic key but conflicting availability payload, the constructor sequence
 * builder must fail closed unless a future ADR ratifies an availability merge
 * law.
 *
 * Backend-erasure law:
 *
 * This record and its nested sequences must not store:
 *
 * - backend constructor handles;
 * - reflection objects;
 * - KSP declarations;
 * - bytecode handles;
 * - source AST/PSI handles;
 * - adapter registry ids;
 * - discovery append ordinals;
 * - closures, suppliers, or lazy delegates that can recover backend handles.
 *
 * The record does not perform runtime "is lowered" self-attestation checks.
 * Erasure is enforced through:
 *
 * - metamodel-domain value-object boundaries;
 * - closed frozen sequence construction protocols;
 * - image publication validation;
 * - architecture tests that reject backend handle reachability.
 *
 * Equality law:
 *
 * Equality is structural over the full frozen record payload:
 *
 * - key;
 * - parameterRecords;
 * - annotationRecords;
 * - declarationAvailability;
 * - sourceAvailability.
 *
 * This equality is for diagnostics, tests, and local in-memory collection use.
 * It must not replace key-driven duplicate detection in constructor sequences.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes the existing metamodel VO/sequence hashCode surfaces
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
class FrozenConstructorRecord private constructor(
    val key: FrozenConstructorRecordKey,
    val parameterRecords: FrozenConstructorParameterRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenConstructorRecord(" +
                "key=${key.renderSummary()}, " +
                "parameterCount=${parameterRecords.size}, " +
                "annotationCount=${annotationRecords.size}, " +
                "declarationAvailability=$declarationAvailability, " +
                "sourceAvailability=$sourceAvailability" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorRecord) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not constructor identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return key == other.key &&
                parameterRecords == other.parameterRecords &&
                annotationRecords == other.annotationRecords &&
                declarationAvailability == other.declarationAvailability &&
                sourceAvailability == other.sourceAvailability
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
            key: FrozenConstructorRecordKey,
            parameterRecords: FrozenConstructorParameterRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): FrozenConstructorRecord {
            requireParameterOwnershipContinuity(
                key = key,
                parameterRecords = parameterRecords,
            )

            return FrozenConstructorRecord(
                key = key,
                parameterRecords = parameterRecords,
                annotationRecords = annotationRecords,
                declarationAvailability = declarationAvailability,
                sourceAvailability = sourceAvailability,
                precomputedHashCode = computeHashCode(
                    key = key,
                    parameterRecords = parameterRecords,
                    annotationRecords = annotationRecords,
                    declarationAvailability = declarationAvailability,
                    sourceAvailability = sourceAvailability,
                ),
            )
        }

        /**
         * Validates that every nested parameter record points back to this
         * constructor key.
         *
         * This is the strongest ownership check this record can perform locally.
         *
         * It does not validate:
         *
         * - parameter-index compactness;
         * - parameter sequence ordering;
         * - duplicate parameter keys;
         * - parameter type membership in a FrozenMetamodelImage.
         *
         * Those are sequence-builder and image-publication responsibilities.
         */
        private fun requireParameterOwnershipContinuity(
            key: FrozenConstructorRecordKey,
            parameterRecords: FrozenConstructorParameterRecordSequence,
        ) {
            var index = 0

            while (index < parameterRecords.size) {
                val parameterRecord = parameterRecords[index]

                if (parameterRecord.key.ownerConstructorKey != key) {
                    throw MetamodelFactContractViolationException(
                        "FrozenConstructorRecord parameter ownership mismatch: " +
                                "constructorKey=${key.renderSummary()}, " +
                                "parameterIndex=$index, " +
                                "parameterOwner=${parameterRecord.key.ownerConstructorKey.renderSummary()}",
                    )
                }

                index += 1
            }
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately follows the current metamodel VO family until the
         * later BLAKE3 / metadata-hash refactoring replaces hash policy
         * globally.
         *
         * Do not treat this value as order material.
         */
        private fun computeHashCode(
            key: FrozenConstructorRecordKey,
            parameterRecords: FrozenConstructorParameterRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): Int {
            var result = key.hashCode()
            result = 31 * result + parameterRecords.hashCode()
            result = 31 * result + annotationRecords.hashCode()
            result = 31 * result + declarationAvailability.hashCode()
            result = 31 * result + sourceAvailability.hashCode()
            return result
        }
    }
}