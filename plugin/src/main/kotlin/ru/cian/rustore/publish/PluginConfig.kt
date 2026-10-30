package ru.cian.rustore.publish

import ru.cian.rustore.publish.utils.DEFAULT_CONNECT_TIMEOUT_SECONDS
import ru.cian.rustore.publish.utils.DEFAULT_READ_TIMEOUT_SECONDS
import ru.cian.rustore.publish.utils.DEFAULT_WRITE_TIMEOUT_SECONDS
import ru.cian.rustore.publish.utils.RUSTORE_API_BASE_URL
import java.io.File

internal data class PluginConfig(
    val credentials: Credentials,
    val publishType: PublishType,
    val artifactFormat: BuildFormat,
    val mobileServicesType: MobileServicesType,
    val artifactFile: File,
    val releasePhase: ReleasePhaseConfig?,
    val releaseNotes: List<ReleaseNotesConfig>?,
    val applicationId: String,
    val seoTagIds: List<Int>,
    val minAndroidVersion: String,
    val developerContacts: DeveloperContactsConfig,
    val appType: AppTypes?,
    val apiBaseUrl: String = RUSTORE_API_BASE_URL,
    val trustedCertificates: TrustedCertificatesConfig = TrustedCertificatesConfig(),
    val timeouts: TimeoutsConfig = TimeoutsConfig(),
)

internal data class TimeoutsConfig(
    val connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT_SECONDS,
    val readTimeout: Long = DEFAULT_READ_TIMEOUT_SECONDS,
    val writeTimeout: Long = DEFAULT_WRITE_TIMEOUT_SECONDS,
)

internal data class TrustedCertificatesConfig(
    val useBundledRussianTrustedRootCa: Boolean = false,
    val certificateFile: File? = null,
)

internal data class ReleasePhaseConfig(
    val percent: Double,
)

internal data class Credentials(
    var keyId: String,
    var clientSecret: String,
)

internal data class ReleaseNotesConfig(
    val lang: String,
    val newFeatures: String,
)

internal data class DeveloperContactsConfig(
    val email: String,
    val website: String? = null,
    val vkCommunity: String? = null,
)