package metamodel.domain.vo

/**
 * [Value Object] Type Reference (Strictly Pure)
 *
 * Represents a static reference to a type in the source code.
 * This interface is completely agnostic of runtime technologies (No JVM classes).
 */
interface TypeReference {
    /**
     * The canonical identity (e.g., "com.example.User<java.lang.String>?").
     */
    val id: String

    /**
     * The identity used for cycle detection (ADR-027).
     *
     * ## Contract
     * 1. **Nullability Stripped**: "String?" and "String" share the same cycleId.
     * 2. **Generics Preserved**: "List<String>" != "List<Int>".
     * 3. **Normalized**: Inner classes must use '.' instead of '$'.
     */
    val cycleId: String

    val signature: String

    /**
     * Annotations present at the usage site.
     * The creating Adapter MUST guarantee deterministic ordering (e.g., sorted by name).
     */
    val useSiteAnnotations: List<AnnotationDescriptor>
}

data class AnnotationDescriptor(
    val qualifiedName: String,
    // Using Any? for simplicity in this snippet, ideally MetamodelAnnotationValue
    val values: Map<String, MetamodelAnnotationValue>
)