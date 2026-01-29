package metamodel.domain.vo

/**
 * [Marker Interface] An opaque handle representing the raw source of a type.
 *
 * This acts as a **"Typedef for Opaque Pointer"** in compiler terms.
 * It serves as the extension point for various type systems.
 *
 * ## Implementations (Expected)
 * - **Reflection (Phase 1):** `KTypeSource(val kType: KType)` (Defined in Adapter)
 * - **Static (Phase 2):** `FqcnTypeSource(val fqcn: String)` (Defined in Domain)
 * - **KSP (Future):** `KspTypeSource(val symbol: KSClassDeclaration)` (Defined in KspAdapter)
 */
interface TypeSource