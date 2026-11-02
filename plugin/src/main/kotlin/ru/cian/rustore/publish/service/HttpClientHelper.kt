package ru.cian.rustore.publish.service

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import ru.cian.rustore.publish.TimeoutsConfig
import ru.cian.rustore.publish.TrustedCertificatesConfig
import ru.cian.rustore.publish.utils.Logger
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal class HttpClientHelper constructor(
    private val logger: Logger,
    private val timeouts: TimeoutsConfig = TimeoutsConfig(),
    private val trustedCertificates: TrustedCertificatesConfig = TrustedCertificatesConfig(),
) {

    private val gson by lazy { Gson() }

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(timeouts.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(timeouts.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(timeouts.writeTimeout, TimeUnit.SECONDS)
            .apply {
                TrustManagerProvider.create(trustedCertificates)?.let { trustManager ->
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, arrayOf(trustManager), null)
                    sslSocketFactory(sslContext.socketFactory, trustManager)
                }
            }
            .build()
    }

    inline fun <reified T> get(url: String, headers: Map<String, String>? = null): T =
        execute(Request.Builder().get(), url, headers, T::class.java)

    inline fun <reified T> post(url: String, body: RequestBody, headers: Map<String, String>? = null): T =
        execute(Request.Builder().post(body), url, headers, T::class.java)

    inline fun <reified T> delete(url: String, body: RequestBody, headers: Map<String, String>? = null): T =
        execute(Request.Builder().delete(body), url, headers, T::class.java)

    inline fun <reified T> put(url: String, body: RequestBody, headers: Map<String, String>? = null): T =
        execute(Request.Builder().put(body), url, headers, T::class.java)

    /**
     * Rustore API may return a json body with `"code": "ERROR"` for non-2xx status codes
     * and the callers rely on it (e.g. draft creation when previous draft exists). So json body is parsed
     * for any status code, and [RustoreHttpException] is thrown only if the body can't be parsed.
     */
    fun <T> execute(
        requestBuilder: Request.Builder,
        url: String,
        headers: Map<String, String>?,
        responseClass: Class<T>,
    ): T {
        val request = requestBuilder
            .url(url)
            .apply { headers?.forEach { header(it.key, it.value) } }
            .build()

        return executeCall(request).use { httpResponse ->
            val responseBody = httpResponse.body?.string()
            val parseResult = runCatching { parseJson(responseBody, responseClass, httpResponse.isSuccessful) }
            parseResult.getOrNull() ?: run {
                val exception = RustoreHttpException(
                    method = request.method,
                    url = request.url.toString(),
                    httpCode = httpResponse.code,
                    httpMessage = httpResponse.message,
                    responseBody = responseBody,
                    cause = parseResult.exceptionOrNull(),
                )
                logger.e(exception)
                throw exception
            }
        }
    }

    private fun executeCall(request: Request): Response = try {
        client.newCall(request).execute()
    } catch (e: SSLHandshakeException) {
        throw tlsException(request, e)
    } catch (e: SSLPeerUnverifiedException) {
        throw tlsException(request, e)
    }

    private fun tlsException(request: Request, cause: SSLException): RustoreTlsException {
        val exception = RustoreTlsException(
            method = request.method,
            url = request.url.toString(),
            trustedCertificates = trustedCertificates,
            cause = cause,
        )
        logger.e(exception)
        return exception
    }

    private fun <T> parseJson(responseBody: String?, responseClass: Class<T>, isSuccessful: Boolean): T? {
        if (responseBody.isNullOrBlank()) return null
        val json = try {
            JsonParser.parseString(responseBody)
        } catch (e: JsonParseException) {
            throw IllegalStateException("Can't parse response body as json", e)
        }
        // Non-2xx response is treated as Rustore API response only if it has `code` field like all API models.
        if (!isSuccessful && !(json.isJsonObject && json.asJsonObject.has(RESPONSE_CODE_FIELD))) return null
        return try {
            gson.fromJson(json, responseClass)
        } catch (e: JsonParseException) {
            throw IllegalStateException("Can't parse response body as ${responseClass.simpleName}", e)
        }
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json;charset=utf-8".toMediaType()
        val MEDIA_TYPE_AAB = "application/octet-stream".toMediaType()
        private const val RESPONSE_CODE_FIELD = "code"
    }
}
