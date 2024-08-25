package ru.cian.rustore.publish.service.mock

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import ru.cian.rustore.publish.utils.Logger
import java.util.concurrent.TimeUnit

private const val DELAY_REQUEST_BODY_SECONDS = 2L

@SuppressWarnings("MaxLineLength", "MagicNumber")
class MockServerWrapperImpl(
    val logger: Logger,
    val applicationId: String,
    val requestFormArgument: String,
): MockServerWrapper {

    private lateinit var mockWebServer: MockWebServer

    override fun getBaseUrl(): String {
        return mockWebServer.url("/").toString()
    }

    override fun start() {
        logger.v(":: start mock server")
        val versionId = 123456789
        val priorityUpdate = 5

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            @SuppressWarnings("ReturnCount")
            override fun dispatch(request: RecordedRequest): MockResponse {
                when {
                    request.path!!.contains("/public/auth/") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                              "code": "OK",
                              "message": "mock_message_get_token",
                              "body": {
                                "jwe": "mock_jwe"
                              }
                            }""".trimMargin())
                    request.path!!.contains("/public/v1/application/$applicationId/version") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                              "code": "OK",
                              "message": "mock_message_create_draft",
                              "body": $versionId
                            }""".trimIndent())
                    request.path!!.contains("/public/v1/application/$applicationId/version/$versionId/$requestFormArgument") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                              "code": "OK",
                              "message": "mock_message_upload_file" 
                            }""".trimIndent())
                    request.path!!.contains("/public/v1/application/$applicationId/version/$versionId/commit?priorityUpdate=$priorityUpdate") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                              "code": "OK",
                              "message": "mock_message_submit_publication"
                            }""".trimIndent())
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.dispatcher = dispatcher
    }

    override fun shutdown() {
        logger.v(":: shutdown mock server")
        mockWebServer.shutdown()
    }
}