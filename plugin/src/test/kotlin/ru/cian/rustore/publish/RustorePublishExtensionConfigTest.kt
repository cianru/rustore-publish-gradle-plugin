package ru.cian.rustore.publish

import assertk.assertThat
import assertk.assertions.isEqualTo
import groovy.lang.Binding
import groovy.lang.GroovyShell
import org.junit.jupiter.api.Test

class RustorePublishExtensionConfigTest {

    @Test
    fun `should configure network setting by groovy closure`() {
        val config = RustorePublishExtensionConfig("release")

        GroovyShell(Binding(mapOf("config" to config))).evaluate(
            """
            config.with {
                networkSetting {
                    apiBaseUrl = "https://public-api-m.rustore.ru"
                    useBundledRussianTrustedRootCa = true
                    trustedCertificatePath = "/tmp/ca.pem"
                }
            }
            """.trimIndent()
        )

        assertThat(config.networkSetting.apiBaseUrl).isEqualTo("https://public-api-m.rustore.ru")
        assertThat(config.networkSetting.useBundledRussianTrustedRootCa).isEqualTo(true)
        assertThat(config.networkSetting.trustedCertificatePath).isEqualTo("/tmp/ca.pem")
    }
}
