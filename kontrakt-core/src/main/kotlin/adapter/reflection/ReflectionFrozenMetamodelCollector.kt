package adapter.reflection

import stage.admission.diagnostics.evidence.MetamodelAdapterAssemblyException
import stage.admission.diagnostics.evidence.MetamodelException
import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageAssemblyInput
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.canonicalization.material.frozen.image.FrozenRawFactTableEntry
import stage.canonicalization.material.frozen.image.FrozenTypeCycleIdentityTableEntry
import stage.canonicalization.material.frozen.image.FrozenTypeShapeTableEntry
import stage.input.presentation.raw.ResolvedTypeShape
import stage.lowering.boundary.RawTypeFactsResolution
import stage.lowering.material.expansion.TypeCycleIdentity
import java.util.ArrayDeque

/**
 * Reflection-backed frozen metamodel collection pass.
 *
 * This collector lowers a live reflection adapter bundle into
 * FrozenMetamodelImageAssemblyInput.
 *
 * It is not:
 *
 * - a planning service;
 * - a planner session participant;
 * - a cache;
 * - an interner;
 * - a frozen ordinal authority;
 * - a frozen table assembler;
 * - a diagnostic envelope builder.
 *
 * Compiler-style role:
 *
 * ```text
 * root KType[]
 * -> ReflectionMetamodelAdapterBundle.issueRootReference(...)
 * -> TypeReference work queue
 * -> TypeShapeProvider.resolveTypeShape(...)
 * -> TypeCycleIdentityProvider.resolveCycleIdentity(...)
 * -> RawTypeFactsProvider.resolveRawFacts(...)
 * -> FrozenMetamodelImageAssemblyInput
 * ```
 *
 * Boundary split:
 *
 * - ReflectionMetamodelAdapterBundle owns backend handle sidecar lifecycle.
 * - This collector owns reachable graph collection from reflection-backed ports.
 * - FrozenMetamodelImageAssemblyInput owns caller-array snapshotting.
 * - FrozenMetamodelImageAssembler owns deterministic ordinal alignment.
 * - FrozenMetamodelImage.issue(...) owns final image integrity validation.
 *
 * Planning separation law:
 *
 * This collector is allowed to resolve raw facts eagerly.
 *
 * Reason:
 *
 * It is building a complete frozen image, not executing the planning expansion
 * protocol. ADR-0037's fact-lazy rule remains a planning traversal law:
 *
 * ```text
 * shape -> cycle identity -> active-cycle detection -> raw facts only on cycle miss
 * ```
 *
 * A frozen image must instead provide complete table coverage for every indexed
 * TypeReference that it publishes.
 *
 * Discovery order law:
 *
 * Reflection discovery order is not frozen semantic order.
 *
 * This collector may encounter types in work-queue order, but that order is only
 * transient acquisition order. FrozenMetamodelImageAssembler later aligns all
 * payloads to image-local deterministic ordinals.
 *
 * Membership law:
 *
 * The Level 1 work set deliberately uses linear structural lookup instead of
 * HashSet/HashMap.
 *
 * Reason:
 *
 * This collector must not make TypeReference.hashCode() part of publication
 * reachability semantics. The final frozen order is owned by
 * ObjectArrayFrozenTypeReferenceIndex and FrozenTypeReferenceOrder.
 *
 * This is not the final high-scale membership strategy. A later deterministic
 * interning/stable-id pass may replace the linear work set with primitive
 * membership tables.
 *
 * Collection limit law:
 *
 * Linear membership must not be unbounded.
 *
 * The collector enforces ReflectionFrozenMetamodelCollectionLimits supplied by
 * the collection input. The collector does not choose those limits.
 *
 * Limit selection belongs to acquisition/session capacity policy.
 *
 * Payload trust law:
 *
 * This collector does not deep-copy or sanitize provider-returned payloads.
 *
 * Reflection providers must return adapter-neutral frozen material. If
 * TypeReference, ResolvedTypeShape, TypeCycleIdentity, or RawTypeFactsDTO can
 * reach backend handles or mutable acquisition state, the broken boundary is
 * the provider/lowering layer, not this collector.
 *
 * Lifecycle law:
 *
 * The caller owns the ReflectionMetamodelAdapterBundle lifecycle.
 *
 * This collector does not close the bundle.
 *
 * If collection is scoped with `use { ... }`, the caller must ensure collection
 * and subsequent frozen assembly happen before close.
 *
 * Provider stability law:
 *
 * The collector snapshots cycle-identity algorithm id/version at the beginning
 * and verifies that produced TypeCycleIdentity / RawTypeFactsDTO material agrees
 * with that snapshot.
 *
 * The final cycle table and image validator repeat this check at publication.
 * The repetition is intentional: this collector catches adapter drift early,
 * while the image validator protects the published frozen image boundary.
 */
internal object ReflectionFrozenMetamodelCollector {
    @JvmStatic
    fun collect(
        bundle: ReflectionMetamodelAdapterBundle,
        input: ReflectionFrozenMetamodelCollectionInput,
    ): FrozenMetamodelImageAssemblyInput {
        return try {
            collectVerified(
                bundle = bundle,
                input = input,
            )
        } catch (e: MetamodelAdapterAssemblyException) {
            throw e
        } catch (e: MetamodelException) {
            throw e
        } catch (e: RuntimeException) {
            throw MetamodelAdapterAssemblyException(
                "Reflection frozen metamodel collection failed. " +
                        "blame=reflection-frozen-collector, " +
                        "imageId=${input.imageId.renderSummary()}, " +
                        "schemaVersion=${input.schemaVersion.renderSummary()}, " +
                        "rootTypeCount=${input.rootTypeCount}, " +
                        "maxDiscoveredTypeReferences=${input.limits.maxDiscoveredTypeReferences}, " +
                        "cause=${e::class.qualifiedName}: ${e.message}",
            )
        }
    }

    private fun collectVerified(
        bundle: ReflectionMetamodelAdapterBundle,
        input: ReflectionFrozenMetamodelCollectionInput,
    ): FrozenMetamodelImageAssemblyInput {
        val rootTypes =
            input.copyRootTypes()

        val identityAlgorithmIdSnapshot =
            bundle.typeCycleIdentityProvider.identityAlgorithmId

        val identityAlgorithmVersionSnapshot =
            bundle.typeCycleIdentityProvider.identityAlgorithmVersion

        requireIdentityProviderMetadata(
            imageId = input.imageId,
            identityAlgorithmId = identityAlgorithmIdSnapshot,
            identityAlgorithmVersion = identityAlgorithmVersionSnapshot,
        )

        val workSet =
            TypeReferenceWorkSet.issue(
                imageId = input.imageId,
                maxDiscoveredTypeReferences = input.limits.maxDiscoveredTypeReferences,
                initialCapacity = initialEntryCapacity(
                    input = input,
                ),
            )

        var rootIndex = 0

        while (rootIndex < rootTypes.size) {
            val rootReference =
                bundle.issueRootReference(
                    type = rootTypes[rootIndex],
                )

            workSet.enqueueIfNew(
                reference = rootReference,
            )

            rootIndex += 1
        }

        val initialEntryCapacity =
            initialEntryCapacity(
                input = input,
            )

        val shapeEntries =
            ArrayList<FrozenTypeShapeTableEntry>(
                initialEntryCapacity,
            )

        val cycleIdentityEntries =
            ArrayList<FrozenTypeCycleIdentityTableEntry>(
                initialEntryCapacity,
            )

        val rawFactEntries =
            ArrayList<FrozenRawFactTableEntry>(
                initialEntryCapacity,
            )

        while (workSet.hasPending()) {
            val reference =
                workSet.removeFirstPending()

            val shape =
                bundle.typeShapeProvider.resolveTypeShape(
                    reference = reference,
                )

            shapeEntries.add(
                FrozenTypeShapeTableEntry.issue(
                    reference = reference,
                    shape = shape,
                ),
            )

            enqueueShapeChildren(
                workSet = workSet,
                shape = shape,
            )

            val cycleIdentity =
                bundle.typeCycleIdentityProvider.resolveCycleIdentity(
                    reference = reference,
                )

            requireCycleIdentityMatchesSnapshot(
                imageId = input.imageId,
                reference = reference,
                identityAlgorithmIdSnapshot = identityAlgorithmIdSnapshot,
                identityAlgorithmVersionSnapshot = identityAlgorithmVersionSnapshot,
                cycleIdentity = cycleIdentity,
            )

            cycleIdentityEntries.add(
                FrozenTypeCycleIdentityTableEntry.issue(
                    reference = reference,
                    identity = cycleIdentity,
                ),
            )

            val rawFactsResolution =
                bundle.rawTypeFactsProvider.resolveRawFacts(
                    reference = reference,
                )

            requireRawFactsMatchSnapshot(
                imageId = input.imageId,
                reference = reference,
                identityAlgorithmIdSnapshot = identityAlgorithmIdSnapshot,
                identityAlgorithmVersionSnapshot = identityAlgorithmVersionSnapshot,
                rawFactsResolution = rawFactsResolution,
            )

            rawFactEntries.add(
                FrozenRawFactTableEntry.issue(
                    reference = reference,
                    facts = rawFactsResolution.facts,
                ),
            )

            enqueueRawFactChildren(
                workSet = workSet,
                rawFactsResolution = rawFactsResolution,
            )
        }

        return FrozenMetamodelImageAssemblyInput.issue(
            imageId = input.imageId,
            schemaVersion = input.schemaVersion,
            typeReferences = workSet.copyDiscoveredReferences(),
            shapeEntries = toShapeEntryArray(
                entries = shapeEntries,
            ),
            cycleIdentityAlgorithmId = identityAlgorithmIdSnapshot,
            cycleIdentityAlgorithmVersion = identityAlgorithmVersionSnapshot,
            cycleIdentityEntries = toCycleIdentityEntryArray(
                entries = cycleIdentityEntries,
            ),
            rawFactEntries = toRawFactEntryArray(
                entries = rawFactEntries,
            ),
        )
    }

    private fun initialEntryCapacity(
        input: ReflectionFrozenMetamodelCollectionInput,
    ): Int {
        val rootBased =
            if (input.rootTypeCount < DEFAULT_INITIAL_ENTRY_CAPACITY) {
                DEFAULT_INITIAL_ENTRY_CAPACITY
            } else {
                input.rootTypeCount
            }

        return if (rootBased > input.limits.maxDiscoveredTypeReferences) {
            input.limits.maxDiscoveredTypeReferences
        } else {
            rootBased
        }
    }

    private fun enqueueShapeChildren(
        workSet: TypeReferenceWorkSet,
        shape: ResolvedTypeShape,
    ) {
        /*
         * Subject continuity is validated later by FrozenMetamodelImage.issue(...).
         *
         * This collector uses child references only as reachability edges.
         */
        enqueueIfPresent(
            workSet = workSet,
            reference = shape.elementType,
        )

        enqueueIfPresent(
            workSet = workSet,
            reference = shape.keyType,
        )

        enqueueIfPresent(
            workSet = workSet,
            reference = shape.valueType,
        )

        enqueueIfPresent(
            workSet = workSet,
            reference = shape.componentType,
        )
    }

    private fun enqueueRawFactChildren(
        workSet: TypeReferenceWorkSet,
        rawFactsResolution: RawTypeFactsResolution,
    ) {
        val rawFacts =
            rawFactsResolution.facts

        var constructorIndex = 0

        while (constructorIndex < rawFacts.constructors.size) {
            val constructor =
                rawFacts.constructors[constructorIndex]

            var parameterIndex = 0

            while (parameterIndex < constructor.parameters.size) {
                val parameter =
                    constructor.parameters[parameterIndex]

                workSet.enqueueIfNew(
                    reference = parameter.typeReference,
                )

                parameterIndex += 1
            }

            constructorIndex += 1
        }

        var propertyIndex = 0

        while (propertyIndex < rawFacts.properties.size) {
            val property =
                rawFacts.properties[propertyIndex]

            workSet.enqueueIfNew(
                reference = property.typeReference,
            )

            propertyIndex += 1
        }
    }

    private fun enqueueIfPresent(
        workSet: TypeReferenceWorkSet,
        reference: TypeReference?,
    ) {
        if (reference == null) {
            return
        }

        workSet.enqueueIfNew(
            reference = reference,
        )
    }

    private fun requireIdentityProviderMetadata(
        imageId: FrozenMetamodelImageId,
        identityAlgorithmId: String,
        identityAlgorithmVersion: Long,
    ) {
        if (identityAlgorithmId.isBlank()) {
            throw MetamodelAdapterAssemblyException(
                "Reflection frozen metamodel collection rejected blank cycle identity algorithm id: " +
                        "imageId=${imageId.renderSummary()}",
            )
        }

        if (identityAlgorithmVersion < 0L) {
            throw MetamodelAdapterAssemblyException(
                "Reflection frozen metamodel collection rejected negative cycle identity algorithm version: " +
                        "imageId=${imageId.renderSummary()}, " +
                        "identityAlgorithmId=$identityAlgorithmId, " +
                        "identityAlgorithmVersion=$identityAlgorithmVersion",
            )
        }
    }

    private fun requireCycleIdentityMatchesSnapshot(
        imageId: FrozenMetamodelImageId,
        reference: TypeReference,
        identityAlgorithmIdSnapshot: String,
        identityAlgorithmVersionSnapshot: Long,
        cycleIdentity: TypeCycleIdentity,
    ) {
        if (cycleIdentity.subject != reference) {
            throw MetamodelAdapterAssemblyException(
                "Reflection frozen metamodel collection detected cycle identity subject mismatch: " +
                        "imageId=${imageId.renderSummary()}, " +
                        "expected=${reference.renderSummary()}, " +
                        "actual=${cycleIdentity.subject.renderSummary()}",
            )
        }

        if (
            cycleIdentity.identityAlgorithmId == identityAlgorithmIdSnapshot &&
            cycleIdentity.identityAlgorithmVersion == identityAlgorithmVersionSnapshot
        ) {
            return
        }

        throw MetamodelAdapterAssemblyException(
            "Reflection frozen metamodel collection detected cycle identity algorithm drift: " +
                    "imageId=${imageId.renderSummary()}, " +
                    "reference=${reference.renderSummary()}, " +
                    "expected=$identityAlgorithmIdSnapshot@$identityAlgorithmVersionSnapshot, " +
                    "actual=${cycleIdentity.identityAlgorithmId}@${cycleIdentity.identityAlgorithmVersion}",
        )
    }

    private fun requireRawFactsMatchSnapshot(
        imageId: FrozenMetamodelImageId,
        reference: TypeReference,
        identityAlgorithmIdSnapshot: String,
        identityAlgorithmVersionSnapshot: Long,
        rawFactsResolution: RawTypeFactsResolution,
    ) {
        val rawFacts =
            rawFactsResolution.facts

        if (
            rawFacts.typeIdentityAlgorithmId == identityAlgorithmIdSnapshot &&
            rawFacts.typeIdentityAlgorithmVersion == identityAlgorithmVersionSnapshot
        ) {
            return
        }

        throw MetamodelAdapterAssemblyException(
            "Reflection frozen metamodel collection detected raw fact algorithm drift: " +
                    "imageId=${imageId.renderSummary()}, " +
                    "reference=${reference.renderSummary()}, " +
                    "expected=$identityAlgorithmIdSnapshot@$identityAlgorithmVersionSnapshot, " +
                    "actual=${rawFacts.typeIdentityAlgorithmId}@${rawFacts.typeIdentityAlgorithmVersion}",
        )
    }

    private fun toShapeEntryArray(
        entries: ArrayList<FrozenTypeShapeTableEntry>,
    ): Array<FrozenTypeShapeTableEntry> {
        return Array(entries.size) { index ->
            entries[index]
        }
    }

    private fun toCycleIdentityEntryArray(
        entries: ArrayList<FrozenTypeCycleIdentityTableEntry>,
    ): Array<FrozenTypeCycleIdentityTableEntry> {
        return Array(entries.size) { index ->
            entries[index]
        }
    }

    private fun toRawFactEntryArray(
        entries: ArrayList<FrozenRawFactTableEntry>,
    ): Array<FrozenRawFactTableEntry> {
        return Array(entries.size) { index ->
            entries[index]
        }
    }

    /**
     * Transient deterministic work set for reflection freeze collection.
     *
     * This is not frozen image identity material.
     *
     * It deliberately avoids HashSet/HashMap so reachability membership does not
     * depend on transitional TypeReference.hashCode policy.
     *
     * Final order remains non-authoritative. FrozenMetamodelImageAssembler and
     * ObjectArrayFrozenTypeReferenceIndex assign deterministic image-local
     * ordinals later.
     */
    private class TypeReferenceWorkSet private constructor(
        private val imageId: FrozenMetamodelImageId,
        private val maxDiscoveredTypeReferences: Int,
        private val discovered: ArrayList<TypeReference>,
        private val pending: ArrayDeque<TypeReference>,
    ) {
        fun enqueueIfNew(
            reference: TypeReference,
        ): Boolean {
            if (contains(reference)) {
                return false
            }

            if (discovered.size >= maxDiscoveredTypeReferences) {
                throw MetamodelAdapterAssemblyException(
                    "Reflection frozen metamodel collection exceeded discovered TypeReference limit: " +
                            "imageId=${imageId.renderSummary()}, " +
                            "maxDiscoveredTypeReferences=$maxDiscoveredTypeReferences, " +
                            "currentDiscoveredTypeReferences=${discovered.size}, " +
                            "candidate=${reference.renderSummary()}",
                )
            }

            discovered.add(reference)
            pending.addLast(reference)
            return true
        }

        fun hasPending(): Boolean {
            return !pending.isEmpty()
        }

        fun removeFirstPending(): TypeReference {
            return pending.removeFirst()
        }

        fun copyDiscoveredReferences(): Array<TypeReference> {
            return Array(discovered.size) { index ->
                discovered[index]
            }
        }

        private fun contains(
            reference: TypeReference,
        ): Boolean {
            var index = 0

            while (index < discovered.size) {
                if (discovered[index] == reference) {
                    return true
                }

                index += 1
            }

            return false
        }

        companion object {
            @JvmStatic
            fun issue(
                imageId: FrozenMetamodelImageId,
                maxDiscoveredTypeReferences: Int,
                initialCapacity: Int,
            ): TypeReferenceWorkSet {
                return TypeReferenceWorkSet(
                    imageId = imageId,
                    maxDiscoveredTypeReferences = maxDiscoveredTypeReferences,
                    discovered = ArrayList(initialCapacity),
                    pending = ArrayDeque(),
                )
            }
        }
    }

    private const val DEFAULT_INITIAL_ENTRY_CAPACITY: Int = 16
}