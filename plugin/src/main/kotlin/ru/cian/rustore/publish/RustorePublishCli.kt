package ru.cian.rustore.publish

internal data class RustorePublishCli(
    val publishType: PublishType? = null,
    val credentialsPath: String? = null,
    val keyId: String? = null,
    val clientSecret: String? = null,
    val requestTimeout: String? = null,
    val mobileServicesType: MobileServicesType? = null,
    val buildFormat: BuildFormat? = null,
    val buildFile: String? = null,
    val releasePhasePercent: String? = null,
    val releaseNotes: String? = null,
    val apiStub: Boolean? = null,
    val seoTagIds: List<Int>? = null,
    val minAndroidVersion: String? = null,
    val appType: AppTypes? = null,
)
