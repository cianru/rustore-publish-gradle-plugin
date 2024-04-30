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
    var deployType = DeployType.PUBLISH
    var mobileServicesType: MobileServicesType = MobileServicesType.UNKNOWN
    var buildFormat: BuildFormat = BuildFormat.APK
    var buildFile: String? = null
    var releaseTime: String? = null
    var releasePhase: ReleasePhaseExtension? = null
    var releaseNotes: List<ReleaseNote>? = null

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("Name must not be blank nor empty")
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
            "deployType='$deployType', " +
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

enum class DeployType {
    /**
     * Deploy without draft saving and submit on users;
     */
    UPLOAD_ONLY,

    /**
     * Deploy and save as draft without submit on users;
     */
    DRAFT,

    /**
     * Deploy, save as draft and submit build on users;
     */
    PUBLISH,
}
