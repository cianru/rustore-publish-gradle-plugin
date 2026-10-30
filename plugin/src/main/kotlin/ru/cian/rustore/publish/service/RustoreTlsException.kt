package ru.cian.rustore.publish.service

import ru.cian.rustore.publish.TrustedCertificatesConfig
import ru.cian.rustore.publish.utils.RUSTORE_API_BASE_URL
import javax.net.ssl.SSLException

/**
 * Thrown when TLS connection to Rustore API can't be established, e.g. the server certificate
 * is issued by Russian Trusted Root CA which is absent in the JVM truststore.
 */
internal class RustoreTlsException(
    val method: String,
    val url: String,
    trustedCertificates: TrustedCertificatesConfig,
    cause: SSLException,
) : IllegalStateException(cause) {

    override val message: String = buildString {
        append("Rustore API TLS connection failed for $method $url: ${cause.message}")
        append(
            "\nMost likely the server certificate is not trusted by JVM. " +
                "Rustore moves API to certificates issued by Russian Trusted Root CA " +
                "(Ministry of Digital Development), which is absent in the default JVM truststore. " +
                "To fix it use one of the options:"
        )
        append(
            "\n 1. Install Russian Trusted Root CA (https://www.gosuslugi.ru/crt) " +
                "into the truststore of JVM that runs Gradle."
        )
        append(
            "\n 2. Enable the certificate bundled into the plugin: " +
                "`networkSetting { useBundledRussianTrustedRootCa = true }` (CLI: `--useBundledRussianTrustedRootCa`)."
        )
        append(
            "\n 3. Set path to your CA certificate file: " +
                "`networkSetting { trustedCertificatePath = \"...\" }` (CLI: `--trustedCertificatePath`)."
        )
        append(
            "\n 4. Use the API domain with the international certificate: " +
                "`networkSetting { apiBaseUrl = \"$RUSTORE_API_BASE_URL\" }` (CLI: `--apiBaseUrl`)."
        )
        append(
            "\nCurrent settings: " +
                "useBundledRussianTrustedRootCa=${trustedCertificates.useBundledRussianTrustedRootCa}, " +
                "trustedCertificatePath=${trustedCertificates.certificateFile?.absolutePath}."
        )
    }
}
