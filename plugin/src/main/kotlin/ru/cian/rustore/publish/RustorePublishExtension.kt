package ru.cian.rustore.publish

import groovy.lang.Closure
import org.gradle.api.Project

open class RustorePublishExtension(
    project: Project
) {

    val instances = project.container(RustorePublishExtensionConfig::class.java) { name ->
        RustorePublishExtensionConfig(name, project)
    }

    companion object {
        const val MAIN_EXTENSION_NAME = "rustorePublish"
    }
}

class RustorePublishExtensionConfig(
    val name: String,
    val project: Project
) {

    /**
     * For required property use GradleProperty class instance.
     * For example:
     *  var param by GradleProperty(project, String::class.java)
     */
    var credentialsPath: String? = null
    var publishType = PublishType.INSTANTLY
    var requestTimeout: Long? = null
    var mobileServicesType: MobileServicesType = MobileServicesType.UNKNOWN
    var buildFormat: BuildFormat = BuildFormat.APK
    var buildFile: String? = null
    var releaseTime: String? = null
    var releasePhase: ReleasePhaseExtension? = null
    var releaseNotes: List<ReleaseNote>? = null

    init {
        require(name.isNotBlank()) {
            "Name must not be blank nor empty"
        }
    }

    fun releasePhase(closure: Closure<ReleasePhaseExtension>): ReleasePhaseExtension {
        releasePhase = ReleasePhaseExtension()
        project.configure(releasePhase!!, closure)
        return releasePhase!!
    }

    override fun toString(): String {
        return "RustorePublishExtensionConfig(" +
            "name='$name', " +
            "credentialsPath='$credentialsPath', " +
            "deployType='$publishType', " +
            "requestTimeout='$requestTimeout', " +
            "mobileServicesType='$mobileServicesType', " +
            "buildFormat='$buildFormat', " +
            "buildFile='$buildFile', " +
            "releaseTime='$releaseTime', " +
            "releasePhase='$releasePhase', " +
            "releaseNotes='$releaseNotes'" +
            ")"
    }
}

open class ReleasePhaseExtension {

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

open class ReleaseNote {

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
