package metamodel.domain.frozen.record

import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.frozen.sequence.FrozenAnnotationRecordSequence

/**
 * Frozen property record.
 *
 * This is the Level 1 frozen object-record representation for one property
 * candidate after backend-neutral lowering.
 *
 * It is not:
 *
 * - a reflection KProperty;
 * - a reflection KCallable;
 * - a KSP KSPropertyDeclaration;
 * - a bytecode field node;
 * - a bytecode method node;
 * - a source AST/PSI property declaration;
 * - an adapter-local slot;
 * - a discovery append ordinal;
 * - a primitive/slab row.
 *
 * Identity split:
 *
 * [key] is the primary property identity axis inside property sequences.
 *
 * Sequence duplicate detection must be key-driven:
 *
 * ```text
 * same FrozenPropertyRecordKey
 * -> duplicate property identity
 * -> reject or fail closed unless a future ADR ratifies a merge law
 * ```
 *
 * Full record equality is broader than key equality.
 *
 * Rationale:
 *
 * Two property records with the same property key but different annotation
 * records, declaration availability, or source availability are not safely
 * interchangeable. Treating them as equal would hide acquisition drift and
 * weaken freeze-time diagnostics.
 *
 * Therefore:
 *
 * - property sequence builders use [key] as the duplicate-detection authority;
 * - this record's equals/hashCode use the full frozen record payload.
 *
 * Annotation ownership law:
 *
 * Property-level [annotationRecords] are attached to this property record, but
 * the current FrozenAnnotationRecordKey does not yet carry an explicit owner
 * property key.
 *
 * Therefore this record cannot locally prove that every nested annotation
 * record belongs to this property.
 *
 * That is a known Level 1 limitation. Property annotation ownership must be
 * enforced by the acquisition path and later strengthened when annotation
 * record keys gain an explicit owner surface or when annotation records are
 * lowered into owner-addressed tables.
 *
 * Availability law:
 *
 * [declarationAvailability] and [sourceAvailability] are record state.
 *
 * They are not property key material.
 *
 * Availability drift is not identity drift. If two records have the same
 * semantic key but conflicting availability payload, the property sequence
 * builder must fail closed unless a future ADR ratifies an availability merge
 * law.
 *
 * Backend-erasure law:
 *
 * This record and its nested sequences must not store:
 *
 * - backend property handles;
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
 * - annotationRecords;
 * - declarationAvailability;
 * - sourceAvailability.
 *
 * This equality is for diagnostics, tests, and local in-memory collection use.
 * It must not replace key-driven duplicate detection in property sequences.
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
class FrozenPropertyRecord private constructor(
    val key: FrozenPropertyRecordKey,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenPropertyRecord(" +
                "key=${key.renderSummary()}, " +
                "annotationCount=${annotationRecords.size}, " +
                "declarationAvailability=$declarationAvailability, " +
                "sourceAvailability=$sourceAvailability" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenPropertyRecord) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not property identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return key == other.key &&
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
            key: FrozenPropertyRecordKey,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): FrozenPropertyRecord {
            return FrozenPropertyRecord(
                key = key,
                annotationRecords = annotationRecords,
                declarationAvailability = declarationAvailability,
                sourceAvailability = sourceAvailability,
                precomputedHashCode = computeHashCode(
                    key = key,
                    annotationRecords = annotationRecords,
                    declarationAvailability = declarationAvailability,
                    sourceAvailability = sourceAvailability,
                ),
            )
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
            key: FrozenPropertyRecordKey,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): Int {
            var result = key.hashCode()
            result = 31 * result + annotationRecords.hashCode()
            result = 31 * result + declarationAvailability.hashCode()
            result = 31 * result + sourceAvailability.hashCode()
            return result
        }
    }
}