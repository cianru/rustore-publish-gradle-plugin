package ru.cian.rustore.publish.service.mock

interface MockServerWrapper {

    fun getBaseUrl(): String

    fun start()

    fun shutdown()
}