package ru.cian.rustore.publish.utils

import ru.cian.rustore.publish.BuildFormat
import ru.cian.rustore.publish.Credentials
import ru.cian.rustore.publish.RustorePublishCli
import ru.cian.rustore.publish.PluginConfig
import ru.cian.rustore.publish.ReleaseNotesConfig
import ru.cian.rustore.publish.ReleasePhaseConfig
import ru.cian.rustore.publish.RustorePublishExtensionConfig
import java.io.File
import java.io.FileNotFoundException

internal class ConfigProvider(
    private val extension: RustorePublishExtensionConfig,
    private val cli: RustorePublishCli,
    private val buildFileProvider: BuildFileProvider,
    private val releaseNotesFileProvider: FileWrapper,
    private val applicationId: String,
) {

    fun getConfig(): PluginConfig {

        val requestTimeout = cli.requestTimeout?.toLongOrNull() ?: extension.requestTimeout
        val mobileServicesType = cli.mobileServicesType ?: extension.mobileServicesType
        val publishType = cli.publishType ?: extension.publishType
        val artifactFormat = cli.buildFormat ?: extension.buildFormat
        val customBuildFilePath: String? = cli.buildFile ?: extension.buildFile
        val releaseTime: String? = cli.releaseTime ?: extension.releaseTime
        val releasePhase = getReleasePhaseConfig()
        val credentialsConfig = getCredentialsConfig()
        val releaseNotes = getReleaseNotesConfig()

        val artifactFile = getBuildFile(customBuildFilePath, artifactFormat)

        val artifactFileExtension = artifactFile.extension
        val actualArtifactFormat = when (artifactFileExtension) {
            "apk" -> BuildFormat.APK
            "aab" -> BuildFormat.AAB
            else -> throw IllegalArgumentException(
                "Not allowed artifact file extension: `$artifactFileExtension`. " +
                    "It should be `apk` or `aab`. "
            )
        }

        return PluginConfig(
            credentials = credentialsConfig,
            publishType = publishType,
            requestTimeout = requestTimeout,
            mobileServicesType = mobileServicesType,
            artifactFormat = actualArtifactFormat,
            artifactFile = artifactFile,
            releaseTime = releaseTime,
            releasePhase = releasePhase,
            releaseNotes = releaseNotes,
            applicationId = applicationId,
        )
    }

    private fun getBuildFile(
        customBuildFilePath: String?,
        artifactFormat: BuildFormat
    ): File {

        val artifactFile = if (customBuildFilePath != null) {
            File(customBuildFilePath)
        } else {
            buildFileProvider.getBuildFile(artifactFormat)
        }

        if (artifactFile == null || !artifactFile.exists()) {
            throw FileNotFoundException(
                "$artifactFile (No such file or directory). Application build file is not found. " +
                    "Please run `assemble` or `bundle` task to build the application file before current task."
            )
        }

        if (artifactFormat.fileExtension != artifactFile.extension) {
            throw IllegalArgumentException(
                "Build file ${artifactFile.absolutePath} has wrong file extension " +
                    "that doesn't match with announced buildFormat($artifactFormat) plugin extension param."
            )
        }
        return artifactFile
    }

    @Suppress("ThrowsCount")
    private fun getCredentialsConfig(): Credentials {
        val credentialsFilePath = cli.credentialsPath ?: extension.credentialsPath
        val keyIdPriority: String? = cli.keyId
        val clientSecretPriority: String? = cli.clientSecret
        val credentials = lazy {
            if (credentialsFilePath.isNullOrBlank()) {
                throw FileNotFoundException(
                    "$extension (File path for credentials is null or empty. " +
                        "See the `credentialsPath` param description."
                )
            }
            val credentialsFile = File(credentialsFilePath)
            if (!credentialsFile.exists()) {
                throw FileNotFoundException(
                    "$extension (File (${credentialsFile.absolutePath}) " +
                        "with 'key_id' and 'client_secret' for access to Rustore Publish API is not found)"
                )
            }
            CredentialHelper.getCredentials(credentialsFile)
        }
        val keyId = keyIdPriority
            ?: credentials.value.keyId.nullIfBlank()
            ?: throw IllegalArgumentException(
                "(Rustore credential `keyId` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
            )
        val clientSecret = clientSecretPriority
            ?: credentials.value.clientSecret.nullIfBlank()
            ?: throw IllegalArgumentException(
                "(Rustore credential `clientSecret` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
            )
        return Credentials(keyId, clientSecret)
    }

    @Suppress("ThrowsCount")
    private fun getReleasePhaseConfig(): ReleasePhaseConfig? {
        val releasePhasePercent = cli.releasePhasePercent?.toDouble() ?: extension.releasePhase?.percent

        if (releasePhasePercent != null) {
            val releasePhase = ReleasePhaseConfig(
                percent = releasePhasePercent
            )
            checkReleasePhaseData(releasePhase)
            return releasePhase
        }
        return null
    }

    @Suppress("ThrowsCount")
    private fun checkReleasePhaseData(releasePhase: ReleasePhaseConfig) {
        if (releasePhase.percent <= 0 && releasePhase.percent > 100) {
            throw IllegalArgumentException(
                "Wrong percent release phase value = '${releasePhase.percent}'. " +
                    "Allowed values between 0 and 100 with up to two decimal places."
            )
        }
    }

    private fun getReleaseNotesConfig(): List<ReleaseNotesConfig>? {

        val releaseNotePairs = cli.releaseNotes?.split(";")?.map {
            val split = it.split(":")
            split[0] to split[1]
        } ?: extension.releaseNotes?.map {
            it.lang to it.filePath
        }

        return releaseNotePairs?.map {

            val lang = it.first
            require(lang.isNotBlank()) {
                "'lang' param must not be empty."
            }

            val filePath = it.second
            val file = releaseNotesFileProvider.getFile(filePath)
            require(file.exists()) {
                "File '$filePath' with Release Notes for '$lang' language is not exist."
            }

            val newFeatures = file.readText(Charsets.UTF_8)
            require(newFeatures.length <= RELEASE_NOTES_MAX_LENGTH) {
                "Release notes from '$filePath' for '$lang' language " +
                    "must be less or equals to $RELEASE_NOTES_MAX_LENGTH sign."
            }

            ReleaseNotesConfig(
                lang = lang,
                newFeatures = newFeatures
            )
        }
    }

    companion object {
        private const val RELEASE_NOTES_MAX_LENGTH = 500
    }
}
