package discovery.api

/**
 * Marks an interface as a "Contract" to be discovered and tested by the Kontrakt framework.
 *
 * The framework will scan for all implementations of any interface marked with this annotation.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Contract(
    val verifyConstructors: Boolean = false
)
