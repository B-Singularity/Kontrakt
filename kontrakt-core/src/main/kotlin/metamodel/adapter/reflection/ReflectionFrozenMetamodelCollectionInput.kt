package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelAdapterAssemblyException
import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.image.FrozenMetamodelImageSchemaVersion
import kotlin.reflect.KType

/**
 * Limits for reflection-backed frozen metamodel collection.
 *
 * This object is policy input, not policy authority.
 *
 * The collector enforces these limits, but it does not decide them.
 *
 * The values should be resolved by the acquisition/session capacity policy
 * before collection begins.
 *
 * Capacity boundary law:
 *
 * The reflection frozen collector currently uses structural linear membership
 * for TypeReference reachability in order to avoid making TypeReference.hashCode
 * part of frozen publication semantics.
 *
 * Therefore an unbounded graph would create an algorithmic DoS risk.
 *
 * This limit closes that boundary without introducing HashSet/HashMap or
 * backend-registry membership into the frozen collection pass.
 */
internal class ReflectionFrozenMetamodelCollectionLimits private constructor(
    val maxDiscoveredTypeReferences: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxDiscoveredTypeReferences: Int,
        ): ReflectionFrozenMetamodelCollectionLimits {
            if (maxDiscoveredTypeReferences > 0) {
                return ReflectionFrozenMetamodelCollectionLimits(
                    maxDiscoveredTypeReferences = maxDiscoveredTypeReferences,
                )
            }

            throw MetamodelAdapterAssemblyException(
                "Reflection frozen metamodel collection rejected non-positive discovered TypeReference limit: " +
                        "maxDiscoveredTypeReferences=$maxDiscoveredTypeReferences",
            )
        }
    }
}

/**
 * Command object for reflection frozen metamodel collection.
 *
 * This object is reflection-adapter collection input, not frozen image semantic
 * material and not planning-visible material.
 *
 * It groups:
 *
 * - one image id;
 * - one frozen image schema version;
 * - one root KType set;
 * - one externally resolved collection limit set.
 *
 * Snapshot law:
 *
 * JVM arrays are mutable.
 *
 * The issue(...) factory snapshots the caller-owned root KType array.
 *
 * The stored array is private and is never returned directly. Consumers must
 * request a fresh copy through copyRootTypes().
 *
 * KType payload law:
 *
 * This object snapshots the root array, not KType object graphs.
 *
 * KType handle lifecycle belongs to ReflectionMetamodelAdapterBundle and its
 * ReflectionTypeHandleRegistry.
 *
 * The caller must keep the bundle open while this input is consumed by
 * ReflectionFrozenMetamodelCollector.
 *
 * Limit law:
 *
 * This input requires explicit collection limits.
 *
 * Do not hide a default limit inside this input object. A hidden default would
 * turn a policy decision into an adapter implementation detail.
 *
 * Buffer release law:
 *
 * After this input is issued, callers should release or clear their mutable
 * root-type staging buffers as soon as possible.
 */
internal class ReflectionFrozenMetamodelCollectionInput private constructor(
    val imageId: FrozenMetamodelImageId,
    val schemaVersion: FrozenMetamodelImageSchemaVersion,
    val limits: ReflectionFrozenMetamodelCollectionLimits,
    private val rootTypesSnapshot: Array<KType>,
) {
    val rootTypeCount: Int
        get() = rootTypesSnapshot.size

    fun copyRootTypes(): Array<KType> {
        return rootTypesSnapshot.copyOf()
    }

    override fun toString(): String {
        return "ReflectionFrozenMetamodelCollectionInput(" +
                "imageId=${imageId.renderSummary()}, " +
                "schemaVersion=${schemaVersion.renderSummary()}, " +
                "rootTypeCount=$rootTypeCount, " +
                "maxDiscoveredTypeReferences=${limits.maxDiscoveredTypeReferences}" +
                ")"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            rootTypes: Array<KType>,
            limits: ReflectionFrozenMetamodelCollectionLimits,
        ): ReflectionFrozenMetamodelCollectionInput {
            val rootTypesSnapshot =
                rootTypes.copyOf()

            if (rootTypesSnapshot.isEmpty()) {
                throw MetamodelAdapterAssemblyException(
                    "Reflection frozen metamodel collection requires at least one root KType: " +
                            "imageId=${imageId.renderSummary()}, " +
                            "schemaVersion=${schemaVersion.renderSummary()}",
                )
            }

            if (rootTypesSnapshot.size > limits.maxDiscoveredTypeReferences) {
                throw MetamodelAdapterAssemblyException(
                    "Reflection frozen metamodel collection root count exceeds discovered TypeReference limit: " +
                            "imageId=${imageId.renderSummary()}, " +
                            "schemaVersion=${schemaVersion.renderSummary()}, " +
                            "rootTypeCount=${rootTypesSnapshot.size}, " +
                            "maxDiscoveredTypeReferences=${limits.maxDiscoveredTypeReferences}",
                )
            }

            return ReflectionFrozenMetamodelCollectionInput(
                imageId = imageId,
                schemaVersion = schemaVersion,
                limits = limits,
                rootTypesSnapshot = rootTypesSnapshot,
            )
        }
    }
}