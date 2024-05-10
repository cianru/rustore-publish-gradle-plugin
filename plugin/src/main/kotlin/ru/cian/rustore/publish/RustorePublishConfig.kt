package ru.cian.rustore.publish

import java.io.File

internal data class InputPluginConfig(
    val credentials: Credentials,
    val deployType: DeployType,
    val artifactFormat: BuildFormat,
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
    var companyId: String,
    var clientSecret: String,
)

internal data class InputPluginCliParam(
    val deployType: DeployType? = null,
    val credentialsPath: String? = null,
    val companyId: String? = null,
    val clientSecret: String? = null,
    val mobileServicesType: MobileServicesType? = null,
    val buildFormat: BuildFormat? = null,
    val buildFile: String? = null,
    val releaseTime: String? = null,
    val releasePhasePercent: String? = null,
    val releaseNotes: String? = null,
    val apiStub: Boolean? = null,
)

internal data class ReleaseNotesConfig(
    val lang: String,
    val newFeatures: String,
)