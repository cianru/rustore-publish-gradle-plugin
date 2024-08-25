package ru.cian.rustore.publish.service.mock

import ru.cian.rustore.publish.service.RustoreServiceImpl

class MockServerWrapperStub : MockServerWrapper {

    override fun getBaseUrl(): String {
        return RustoreServiceImpl.DOMAIN_URL
    }

    override fun start() {
        // nothing;
    }

    override fun shutdown() {
        // nothing;
    }
}