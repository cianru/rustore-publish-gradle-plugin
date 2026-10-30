package ru.cian.rustore.publish.service

/**
 * Thrown when Rustore API returns a response that can't be handled: non-2xx status code without
 * a valid json body (e.g. `504 upstream request timeout` from Rustore gateway) or a malformed json body.
 */
internal class RustoreHttpException(
    val method: String,
    val url: String,
    val httpCode: Int,
    val httpMessage: String,
    val responseBody: String?,
    cause: Throwable? = null,
) : IllegalStateException(cause) {

    override val message: String = buildString {
        append("Rustore API request failed: HTTP $httpCode")
        if (httpMessage.isNotBlank()) append(" $httpMessage")
        append(" for $method $url.")
        append(" Response body: ")
        append(responseBody.toBodySnippet())
        if (httpCode in GATEWAY_TIMEOUT_CODES) {
            append(
                "\nIt looks like a timeout on the Rustore gateway side, it doesn't depend on plugin params. " +
                    "Try to repeat the publication later."
            )
        }
    }

    companion object {

        private const val MAX_BODY_LENGTH = 1000
        private val GATEWAY_TIMEOUT_CODES = setOf(408, 504)

        private fun String?.toBodySnippet(): String = when {
            isNullOrBlank() -> "<empty>"
            length > MAX_BODY_LENGTH -> "'${take(MAX_BODY_LENGTH)}...' (truncated, total length $length)"
            else -> "'$this'"
        }
    }
}
