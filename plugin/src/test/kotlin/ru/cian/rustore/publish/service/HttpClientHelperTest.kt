package ru.cian.rustore.publish.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import io.mockk.mockk
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.cian.rustore.publish.TimeoutsConfig
import ru.cian.rustore.publish.TrustedCertificatesConfig
import ru.cian.rustore.publish.models.response.UploadAppFileResponse
import java.io.File
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HttpClientHelperTest {

    private lateinit var server: MockWebServer
    private val httpClient = HttpClientHelper(logger = mockk(relaxed = true), timeouts = TEST_TIMEOUTS)

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should parse json body of successful response`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code":"OK","message":null}"""))

        val response = upload()

        assertThat(response.code).isEqualTo("OK")
        assertThat(response.message).isNull()
    }

    @Test
    fun `should parse json body with code field of non-2xx response`() {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody("""{"code":"ERROR","message":"Draft exists, ID = 42"}""")
        )

        val response = upload()

        assertThat(response.code).isEqualTo("ERROR")
        assertThat(response.message).isEqualTo("Draft exists, ID = 42")
    }

    @Test
    fun `should throw http exception with code url and body for plain text gateway timeout`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(504)
                .setHeader("Content-Type", "text/plain")
                .setBody("upstream request timeout")
        )

        val exception = uploadFailure()

        assertThat(exception.httpCode).isEqualTo(504)
        assertThat(exception.method).isEqualTo("POST")
        assertThat(exception.url).contains("/public/v1/application/ru.cian.app/version/1/aab")
        assertThat(exception.responseBody).isEqualTo("upstream request timeout")
        assertThat(exception.message).isNotNull().contains("HTTP 504")
        assertThat(exception.message).isNotNull().contains("'upstream request timeout'")
        assertThat(exception.message).isNotNull().contains("timeout on the Rustore gateway side")
    }

    @Test
    fun `should throw http exception for non-2xx json body without code field`() {
        server.enqueue(MockResponse().setResponseCode(502).setBody("""{"error":"bad gateway"}"""))

        val exception = uploadFailure()

        assertThat(exception.httpCode).isEqualTo(502)
        assertThat(exception.message).isNotNull().doesNotContain("timeout on the Rustore gateway side")
    }

    @Test
    fun `should truncate long html body in exception message`() {
        val html = "<html>" + "x".repeat(5000) + "</html>"
        server.enqueue(MockResponse().setResponseCode(502).setBody(html))

        val exception = uploadFailure()

        assertThat(exception.responseBody).isEqualTo(html)
        assertThat(exception.message).isNotNull().contains("truncated, total length ${html.length}")
    }

    @Test
    fun `should throw http exception for empty body`() {
        server.enqueue(MockResponse().setResponseCode(413))

        val exception = uploadFailure()

        assertThat(exception.httpCode).isEqualTo(413)
        assertThat(exception.message).isNotNull().contains("Response body: <empty>")
    }

    @Test
    fun `should throw http exception with parse cause for malformed body of successful response`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not a json"))

        val exception = uploadFailure()

        assertThat(exception.httpCode).isEqualTo(200)
        assertThat(exception.cause).isNotNull().prop(Throwable::message).isNotNull().contains("json")
    }

    @Test
    fun `should throw tls exception with hints for untrusted server certificate`() {
        startHttpsServer()

        val exception = runCatching { upload() }.exceptionOrNull()

        assertThat(exception).isNotNull().isInstanceOf(RustoreTlsException::class)
        assertThat(exception?.message).isNotNull().contains("TLS connection failed for POST")
        assertThat(exception?.message).isNotNull().contains("--useBundledRussianTrustedRootCa")
        assertThat(exception?.message).isNotNull().contains("--trustedCertificatePath")
    }

    @Test
    fun `should trust server certificate from trusted certificate file`(@TempDir tempDir: File) {
        val rootCertificate = startHttpsServer()
        val certificateFile = File(tempDir, "ca.pem").apply { writeText(rootCertificate.certificatePem()) }
        val trustedHttpClient = HttpClientHelper(
            logger = mockk(relaxed = true),
            timeouts = TEST_TIMEOUTS,
            trustedCertificates = TrustedCertificatesConfig(certificateFile = certificateFile),
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code":"OK","message":null}"""))

        val response = upload(trustedHttpClient)

        assertThat(response.code).isEqualTo("OK")
    }

    @Test
    fun `should fail by read timeout`() {
        val client = HttpClientHelper(
            logger = mockk(relaxed = true),
            timeouts = TEST_TIMEOUTS.copy(readTimeout = 1L),
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeadersDelay(3, TimeUnit.SECONDS)
                .setBody("""{"code":"OK","message":null}""")
        )

        val exception = runCatching { upload(client) }.exceptionOrNull()

        assertThat(exception).isNotNull().isInstanceOf(SocketTimeoutException::class)
    }

    private fun startHttpsServer(): HeldCertificate {
        server.shutdown()
        val rootCertificate = HeldCertificate.Builder()
            .certificateAuthority(0)
            .commonName("Test Root CA")
            .build()
        val serverCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(InetAddress.getByName("localhost").canonicalHostName)
            .signedBy(rootCertificate)
            .build()
        val serverHandshakeCertificates = HandshakeCertificates.Builder()
            .heldCertificate(serverCertificate)
            .build()
        server = MockWebServer()
        server.useHttps(serverHandshakeCertificates.sslSocketFactory(), false)
        server.start()
        return rootCertificate
    }

    private fun upload(client: HttpClientHelper = httpClient): UploadAppFileResponse = client.post<UploadAppFileResponse>(
        url = server.url("/public/v1/application/ru.cian.app/version/1/aab").toString(),
        body = "file".toRequestBody(HttpClientHelper.MEDIA_TYPE_AAB),
        headers = mapOf("Public-Token" to "secret"),
    )

    private fun uploadFailure(): RustoreHttpException {
        val exception = runCatching { upload() }.exceptionOrNull()
        assertThat(exception).isNotNull().isInstanceOf(RustoreHttpException::class)
        return exception as RustoreHttpException
    }

    private companion object {
        val TEST_TIMEOUTS = TimeoutsConfig(connectTimeout = 5L, readTimeout = 5L, writeTimeout = 5L)
    }
}
