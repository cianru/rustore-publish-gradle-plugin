package ru.cian.rustore.publish

import java.io.File

internal data class PluginConfig(
    val credentials: Credentials,
    val deployType: DeployType,
    val artifactFormat: BuildFormat,
    val requestTimeout: Long?,
    val mobileServicesType: MobileServicesType,
    val artifactFile: File,
    val releaseTime: String?,
    val releasePhase: ReleasePhaseConfig?,
    val releaseNotes: List<ReleaseNotesConfig>?,
    val applicationId: String,
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