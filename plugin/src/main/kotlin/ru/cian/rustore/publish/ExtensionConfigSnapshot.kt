package ru.cian.rustore.publish

import org.gradle.api.Project
import java.io.Serializable

/**
 * Serializable snapshot of [RustorePublishExtensionConfig] for use at task execution
 * (configuration-cache safe: no Project reference).
 */
data class ExtensionConfigSnapshot(
    val credentialsPath: String?,
    val publishType: PublishType,
    val requestTimeout: Long?,
    val mobileServicesType: MobileServicesType,
    val buildFormat: BuildFormat,
    val buildFile: String?,
    val releasePhasePercent: Double?,
    val releaseNotePairs: List<ReleaseNotePath>?,
    val seoTags: List<SeoTag>,
    val minAndroidVersion: String,
    val developerContacts: DeveloperContactsSnapshot?,
) : Serializable

data class ReleaseNotePath(val lang: String, val path: String) : Serializable

data class DeveloperContactsSnapshot(
    val email: String,
    val website: String?,
    val vkCommunity: String?,
) : Serializable

object ExtensionConfigSnapshotMapper {
    fun buildExtensionSnapshot(
        project: Project,
        config: RustorePublishExtensionConfig,
    ): ExtensionConfigSnapshot {
        val releaseNotePairs = config.releaseNotes?.map { note ->
            ReleaseNotePath(note.lang, project.file(note.filePath).absolutePath)
        }
        val developerContacts = config.developerContacts?.let {
            DeveloperContactsSnapshot(
                email = it.email,
                website = it.website,
                vkCommunity = it.vkCommunity,
            )
        }
        return ExtensionConfigSnapshot(
            credentialsPath = config.credentialsPath,
            publishType = config.publishType,
            requestTimeout = config.requestTimeout,
            mobileServicesType = config.mobileServicesType,
            buildFormat = config.buildFormat,
            buildFile = config.buildFile,
            releasePhasePercent = config.releasePhase?.percent,
            releaseNotePairs = releaseNotePairs,
            seoTags = config.seoTags,
            minAndroidVersion = config.minAndroidVersion,
            developerContacts = developerContacts,
        )
    }
}
