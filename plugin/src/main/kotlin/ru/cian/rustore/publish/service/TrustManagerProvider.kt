package ru.cian.rustore.publish.service

import ru.cian.rustore.publish.TrustedCertificatesConfig
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Builds [X509TrustManager] that trusts JVM default CAs (including `javax.net.ssl.trustStore` if it's set)
 * and additional CA certificates from [TrustedCertificatesConfig].
 * The additional certificates are applied only to the plugin http client, JVM global settings are not changed.
 */
internal object TrustManagerProvider {

    internal const val BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_PATH = "/certificates/russian_trusted_root_ca.pem"

    /**
     * Expected fingerprint of the bundled certificate, protects against silent replacement of the resource file.
     * Source: https://www.gosuslugi.ru/crt
     */
    internal const val BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_SHA256 =
        "D2:6D:2D:02:31:B7:C3:9F:92:CC:73:85:12:BA:54:10:35:19:E4:40:5D:68:B5:BD:70:3E:97:88:CA:8E:CF:31"

    /**
     * @return `null` if no additional certificates are configured and JVM default trust manager should be used.
     */
    fun create(config: TrustedCertificatesConfig): X509TrustManager? {
        val additionalCertificates = buildList {
            if (config.useBundledRussianTrustedRootCa) addAll(loadBundledRussianTrustedRootCa())
            config.certificateFile?.let { addAll(loadCertificates(it)) }
        }
        if (additionalCertificates.isEmpty()) return null

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        (defaultTrustManager().acceptedIssuers.asList() + additionalCertificates).forEachIndexed { index, cert ->
            keyStore.setCertificateEntry("ca-$index", cert)
        }
        return trustManager(keyStore)
    }

    internal fun loadBundledRussianTrustedRootCa(): List<X509Certificate> {
        val stream = TrustManagerProvider::class.java.getResourceAsStream(BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_PATH)
            ?: error("Bundled certificate `$BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_PATH` is not found in plugin resources")
        val certificates = stream.use { parseCertificates(it) }
        checkFingerprint(certificates, BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_SHA256)
        return certificates
    }

    internal fun checkFingerprint(certificates: List<X509Certificate>, expectedSha256: String) {
        val actual = certificates.map { sha256Fingerprint(it) }
        check(actual == listOf(expectedSha256)) {
            "Bundled certificate `$BUNDLED_RUSSIAN_TRUSTED_ROOT_CA_PATH` failed integrity check: " +
                "expected SHA-256 fingerprint $expectedSha256, actual $actual"
        }
    }

    internal fun sha256Fingerprint(certificate: X509Certificate): String =
        MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
            .joinToString(":") { "%02X".format(it) }

    /**
     * Supports PEM and DER encoded files, PEM file may contain several certificates.
     */
    internal fun loadCertificates(file: File): List<X509Certificate> {
        val certificates = try {
            file.inputStream().use { parseCertificates(it) }
        } catch (e: CertificateException) {
            throw IllegalArgumentException(
                "Can't parse certificate file `${file.absolutePath}`. Expected X.509 certificate in PEM or DER format.",
                e,
            )
        }
        require(certificates.isNotEmpty()) {
            "Certificate file `${file.absolutePath}` doesn't contain any X.509 certificate."
        }
        return certificates
    }

    private fun parseCertificates(stream: InputStream): List<X509Certificate> =
        CertificateFactory.getInstance("X.509")
            .generateCertificates(stream)
            .filterIsInstance<X509Certificate>()

    private fun defaultTrustManager(): X509TrustManager = trustManager(null)

    private fun trustManager(keyStore: KeyStore?): X509TrustManager {
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(keyStore)
        return factory.trustManagers.filterIsInstance<X509TrustManager>().first()
    }
}
