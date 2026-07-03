package realization.runtime.support

import stage.canonicalization.material.representation.TypeReference

/**
 * [Port] Resolves an opaque handle for runtime operations.
 */
interface RuntimeTypeResolver {
    fun resolveHandle(type: TypeReference): RuntimeTypeHandle
}

/**
 * [Port] Abstracts low-level instantiation logic (Reflection/KSP).
 */
interface RuntimeInstantiator {
    fun createEmptyCollection(handle: RuntimeTypeHandle): Any

    fun createEmptyMap(handle: RuntimeTypeHandle): Any

    fun createEmptyArray(handle: RuntimeTypeHandle): Any

    fun createDiagnosticStub(
        handle: RuntimeTypeHandle,
        failureMessage: String,
    ): Any
}

interface RuntimeTypeHandle
