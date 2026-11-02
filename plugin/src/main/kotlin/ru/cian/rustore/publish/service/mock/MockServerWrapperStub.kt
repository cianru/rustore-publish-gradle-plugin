package ru.cian.rustore.publish.service.mock

class MockServerWrapperStub(
    private val baseUrl: String,
) : MockServerWrapper {

    override fun getBaseUrl(): String {
        return baseUrl
    }

    override fun start() {
        // nothing;
    }

    override fun shutdown() {
        // nothing;
    }
}
