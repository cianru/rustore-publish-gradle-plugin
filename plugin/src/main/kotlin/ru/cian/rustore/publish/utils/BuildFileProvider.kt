package ru.cian.rustore.publish.utils

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import ru.cian.rustore.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(
    private val variant: ApplicationVariant,
    private val logger: Logger,
) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when (buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat(variant).singleOrNull()
            BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
        }
    }

    // FIXME(a.mirko): Remove after https://github.com/gradle/gradle/issues/16777
    // FIXME(a.mirko): Remove after https://github.com/gradle/gradle/issues/16775
    private fun getFinalApkArtifactCompat(variant: ApplicationVariant): List<File> {
        val apkDirectory = variant.artifacts.get(SingleArtifact.APK).get()
        logger.v("Build File Directory: $apkDirectory")
        return variant.artifacts.getBuiltArtifactsLoader().load(apkDirectory)
            ?.elements?.map { element -> File(element.outputFile) }
            ?: apkDirectory.asFileTree.matching { include("*.apk") }.map { it.absolutePath }.map { File(it) }
            ?: emptyList()
    }

    private fun getFinalBundleArtifactCompat(variant: ApplicationVariant): List<File> {
        val aabFile = variant.artifacts.get(SingleArtifact.BUNDLE).get().asFile
        return listOf(aabFile)
    }
}
