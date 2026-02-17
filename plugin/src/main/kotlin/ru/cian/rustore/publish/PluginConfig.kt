package ru.cian.rustore.publish

import java.io.File

internal data class PluginConfig(
    val credentials: Credentials,
    val publishType: PublishType,
    val artifactFormat: BuildFormat,
    val requestTimeout: Long?,
    val mobileServicesType: MobileServicesType,
    val artifactFile: File,
    val releaseTime: String?,
    val releasePhase: ReleasePhaseConfig?,
    val releaseNotes: List<ReleaseNotesConfig>?,
    val applicationId: String,
    val seoTags: List<SeoTag>,
    val minAndroidVersion: String,
    val developerContacts: DeveloperContactsConfig,
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