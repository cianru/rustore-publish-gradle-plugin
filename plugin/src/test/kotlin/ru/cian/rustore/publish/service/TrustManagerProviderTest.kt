package ru.cian.rustore.publish.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.cian.rustore.publish.TrustedCertificatesConfig
import ru.cian.rustore.publish.utils.hasException
import java.io.File

class TrustManagerProviderTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should load bundled russian trusted root ca`() {
        val certificates = TrustManagerProvider.loadBundledRussianTrustedRootCa()

        assertThat(certificates).hasSize(1)
        val certificate = certificates.single()
        assertThat(certificate.subjectX500Principal.name).contains("CN=Russian Trusted Root CA")
        assertThat(TrustManagerProvider.sha256Fingerprint(certificate)).isEqualTo(RUSSIAN_TRUSTED_ROOT_CA_SHA256)
        assertThat(TrustManagerProvider.BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_SHA256).isEqualTo(RUSSIAN_TRUSTED_ROOT_CA_SHA256)
    }

    @Test
    fun `should fail on bundled certificate fingerprint mismatch`() {
        val certificates = TrustManagerProvider.loadBundledRussianTrustedRootCa()
        val wrongSha256 = RUSSIAN_TRUSTED_ROOT_CA_SHA256.replaceFirst("D2", "00")

        assertThat { TrustManagerProvider.checkFingerprint(certificates, wrongSha256) }
            .hasException(IllegalStateException::class)
    }

    @Test
    fun `should fail on unexpected extra certificate in bundled resource`() {
        val certificate = TrustManagerProvider.loadBundledRussianTrustedRootCa().single()

        assertThat {
            TrustManagerProvider.checkFingerprint(listOf(certificate, certificate), RUSSIAN_TRUSTED_ROOT_CA_SHA256)
        }.hasException(IllegalStateException::class)
    }

    @Test
    fun `should return null trust manager without additional certificates`() {
        assertThat(TrustManagerProvider.create(TrustedCertificatesConfig())).isNull()
    }

    @Test
    fun `should trust default and bundled certificates`() {
        val trustManager = TrustManagerProvider.create(TrustedCertificatesConfig(useBundledRussianTrustedRootCa = true))

        val issuers = trustManager?.acceptedIssuers?.map { it.subjectX500Principal.name }
        assertThat(issuers).isNotNull().contains(
            TrustManagerProvider.loadBundledRussianTrustedRootCa().single().subjectX500Principal.name
        )
        assertThat(issuers!!.size).isGreaterThan(1)
    }

    @Test
    fun `should load certificate from file`() {
        val file = File(tempDir, "ca.pem")
        javaClass.getResourceAsStream(TrustManagerProvider.BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_PATH)!!
            .use { input -> file.outputStream().use { input.copyTo(it) } }

        val trustManager = TrustManagerProvider.create(TrustedCertificatesConfig(certificateFile = file))

        assertThat(trustManager?.acceptedIssuers?.map { it.subjectX500Principal.name })
            .isNotNull().contains(TrustManagerProvider.loadCertificates(file).single().subjectX500Principal.name)
    }

    @Test
    fun `should throw readable error for invalid certificate file`() {
        val file = File(tempDir, "invalid.pem").apply { writeText("not a certificate") }

        assertThat { TrustManagerProvider.loadCertificates(file) }.hasException(IllegalArgumentException::class)
    }

    private companion object {
        const val RUSSIAN_TRUSTED_ROOT_CA_SHA256 =
            "D2:6D:2D:02:31:B7:C3:9F:92:CC:73:85:12:BA:54:10:35:19:E4:40:5D:68:B5:BD:70:3E:97:88:CA:8E:CF:31"
    }
}
