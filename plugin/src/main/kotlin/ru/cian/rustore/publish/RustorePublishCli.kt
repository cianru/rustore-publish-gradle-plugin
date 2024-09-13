package ru.cian.rustore.publish

internal data class RustorePublishCli(
    val deployType: DeployType? = null,
    val credentialsPath: String? = null,
    val keyId: String? = null,
    val clientSecret: String? = null,
    val requestTimeout: String? = null,
    val mobileServicesType: MobileServicesType? = null,
    val buildFormat: BuildFormat? = null,
    val buildFile: String? = null,
    val releaseTime: String? = null,
    val releasePhasePercent: String? = null,
    val releaseNotes: String? = null,
    val apiStub: Boolean? = null,
)
