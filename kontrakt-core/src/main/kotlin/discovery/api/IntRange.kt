package discovery.api

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LongRange(val min: Long, val max: Long) {
}