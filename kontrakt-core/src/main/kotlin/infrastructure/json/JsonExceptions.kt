package infrastructure.json

/**
 * Base exception for JSON infrastructure errors.
 */
open class JsonException(
    message: String,
) : RuntimeException(message)

/**
 * Thrown when serialization fails (e.g. Circular Reference in Encoder).
 */
class JsonEncodingException(
    message: String,
) : JsonException(message)

/**
 * Thrown when validation fails (e.g. Invalid Type in Validator).
 */
class JsonValidationException(
    message: String,
) : JsonException(message)
