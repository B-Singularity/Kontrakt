package metamodel.domain.vo

/**
 * [Marker Annotation] Indicates that the marked API is provisional
 * and assumes best-effort resolution (e.g., Static Analysis, KSP).
 */
@RequiresOptIn(message = "This API is provisional and assumes best-effort resolution.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class ProvisionalApi

/**
 * [Value Object] An opaque carrier used to transport external type information into the Domain.
 *
 * This class mirrors the **"Token/Symbol Handle"** concept in compiler architecture.
 */
sealed interface TypeReference {
    val typeId: TypeId
    val source: TypeSource

    /**
     * [Phase 1 Primary] A reference based on JVM Runtime information.
     * The Adapter determines the implementation of [source] (typically wrapping KType).
     */
    data class Runtime(
        override val source: TypeSource,
        override val typeId: TypeId
    ) : TypeReference

    /**
     * [Phase 2 Provisional] A reference based on pure Name (String).
     * Used when the class is not loaded, or for simple name-based matching.
     */
    @ProvisionalApi
    data class Static(
        val fullyQualifiedClassName: String,
        override val typeId: TypeId
    ) : TypeReference {
        init {
            require(fullyQualifiedClassName.isNotBlank()) { "fullyQualifiedClassName cannot be blank" }
        }

        // Internal implementation to satisfy TypeSource, exposed via interface
        override val source: TypeSource = FqcnSource(fullyQualifiedClassName)

        private data class FqcnSource(val fqcn: String) : TypeSource
    }

}