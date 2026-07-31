package ru.cian.rustore.publish

import groovy.lang.Closure
import org.gradle.api.Project

open class RustorePublishExtension(
    project: Project
) {

    val instances = project.container(RustorePublishExtensionConfig::class.java) { name ->
        RustorePublishExtensionConfig(name)
    }

    companion object {

        const val MAIN_EXTENSION_NAME = "rustorePublish"
    }
}

/**
 * For required property use GradleProperty class instance.
 * For example:
 *  var param by GradleProperty(project, String::class.java)
 */
class RustorePublishExtensionConfig(
    val name: String,
) : java.io.Serializable {

    /**
     * (Required)
     * Path to json file with RuStore credentials params (`key_id` and `client_secret`).
     * How to get credentials see [[RU] Rustore API Getting Started](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-process/).
     * Plugin credential json example:
     * {
     *   "key_id": "<KEY_ID>",
     *   "client_secret": "<CLIENT_SECRET>"
     * }
     *
     * Type: String (Optional)
     * Default value: `null` (but plugin wait that you provide credentials by CLI params)
     * CLI: `--credentialsPath`
     */
    var credentialsPath: String? = null

    /**
     * (Optional)
     * CLI: `--publishType`
     * ----| 'instantly' – the application will be published immediately after the review process is completed.
     * ----| 'manual' – the application must be published manually by the developer after ther review process is completed.
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.PublishType.INSTANTLY
     * ----| ru.cian.rustore.publish.PublishType.MANUAL
     * Default value: `instantly`
     */
    var publishType = PublishType.INSTANTLY

    /**
     * (Optional)
     * The time in seconds to wait for the publication to complete. Increase it if you build is large.
     * Type: Long (Optional)
     * Default value: `300` // (5min)
     * CLI: `--requestTimeout`
     */
    var requestTimeout: Long? = null

    /**
     * (Optional)
     * Type of mobile services used in application.
     * For more details see param `servicesType` in documentation:
     * https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/apk-file-upload/file-upload-apk/
     * CLI: `--mobileServicesType`
     * ----| 'Unknown'
     * ----| 'HMS'
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.MobileServicesType.UNKNOWN
     * ----| ru.cian.rustore.publish.MobileServicesType.HMS
     * Default value: `Unknown`
     */
    var mobileServicesType: MobileServicesType = MobileServicesType.UNKNOWN

    /**
     * (Required)
     * Build file format.
     * See https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/upload-aab how to prepare project for loading of aab files.
     * Type: String (Optional)
     * CLI: `--buildFormat`, available values:
     * ----| 'apk'
     * ----| 'aab'
     * Gradle Extension DSL, available values:
     * ----| ru.cian.rustore.publish.BuildFormat.APK
     * ----| ru.cian.rustore.publish.BuildFormat.AAB
     * Default value: `apk`
     */
    var buildFormat: BuildFormat = BuildFormat.APK

    /**
     * (Optional)
     * Path to build file if you would like to change default path. "null" means use standard path for "apk" and "aab" files.
     * Type: String (Optional)
     * Default value: `null`
     * CLI: `--buildFile`
     */
    var buildFile: String? = null

    /**
     * (Optional)
     * Release Notes settings. For mote info see ReleaseNote param desc.
     * Type: List<ReleaseNote> (Optional)
     * Default value: `null`
     * CLI: (see ReleaseNotes param desc.)
     */
    var releaseNotes: List<ReleaseNote>? = null

    var releasePhase: ReleasePhaseExtension? = null

    /**
     * (Optional)
     * List of SEO tag ids for RuStore app listing.
     * See the tag id list in documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
     * Number of tags should not be greater than 5.
     * Default value: []
     * CLI: `--seoTagIds`. For example: `--seoTagIds=116,69`
     * Gradle Extension DSL: list of numeric tag ids, for example `listOf(116, 69)`
     */
    var seoTagIds: List<Int> = emptyList()

    /**
     * (Required)
     * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
     * Type: String
     * Default value: `null` (but plugin wait that you provide credentials by CLI params)
     * CLI: `--minAndroidVersion`
     */
    var minAndroidVersion: String = "8"

    /**
     * (Required)
     * Information about Developer.
     * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
     * Type: ru.cian.rustore.publish.DeveloperContacts (Required)
     * Default value: `null`
     * CLI: (see DeveloperContacts param desc.)
     */
    var developerContacts: DeveloperContacts? = null

    init {
        require(name.isNotBlank()) {
            "Name must not be blank nor empty"
        }
    }

    fun releasePhase(closure: Closure<ReleasePhaseExtension>): ReleasePhaseExtension {
        releasePhase = ReleasePhaseExtension()
        closure.delegate = releasePhase
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return releasePhase!!
    }

    override fun toString(): String {
        return "RustorePublishExtensionConfig(" +
            "name='$name', " +
            "credentialsPath='$credentialsPath', " +
            "publishType='$publishType', " +
            "requestTimeout='$requestTimeout', " +
            "mobileServicesType='$mobileServicesType', " +
            "seoTagIds='$seoTagIds', " +
            "buildFormat='$buildFormat', " +
            "buildFile='$buildFile', " +
            "releasePhase='$releasePhase', " +
            "releaseNotes='$releaseNotes', " +
            "minAndroidVersion='$minAndroidVersion', " +
            "developerContacts='$developerContacts'" +
            ")"
    }
}

open class ReleasePhaseExtension : java.io.Serializable {

    var percent: Double? = null

    constructor()

    constructor(percent: Double?) {
        this.percent = percent
    }

    override fun toString(): String {
        return "ReleasePhaseConfig(" +
            "percent='$percent'" +
            ")"
    }
}

open class ReleaseNote : java.io.Serializable {

    lateinit var lang: String
    lateinit var filePath: String

    constructor()

    constructor(lang: String, filePath: String) {
        this.lang = lang
        this.filePath = filePath
    }

    override fun toString(): String {
        return "ReleaseNote(" +
            "lang='$lang', " +
            "filePath='$filePath'" +
            ")"
    }
}

open class DeveloperContacts : java.io.Serializable {

    lateinit var email: String
    var website: String? = null
    var vkCommunity: String? = null

    constructor()

    constructor(email: String, website: String?, vkCommunity: String?) {
        this.email = email
        this.website = website
        this.vkCommunity = vkCommunity
    }

    override fun toString(): String {
        return "DeveloperContacts(" +
            "email='$email', " +
            "website='$website', " +
            "vkCommunity='$vkCommunity'" +
            ")"
    }
}

enum class BuildFormat(val fileExtension: String) {
    APK("apk"),
    AAB("aab"),
}

enum class MobileServicesType(val value: String) {
    HMS("HMS"),
    UNKNOWN("Unknown"),
}

enum class PublishType {
    /**
     * Manual publication. After review you should publish it manually;
     */
    MANUAL,

    /**
     * Automatically publish on all users after reviewing store approve;
     */
    INSTANTLY,

    /**
     * Delayed publication. You should set publishDateTime;
     */
//    DELAYED, // FIXME: Implement delayed publication after adding of `publishDateTime` API param;
}
