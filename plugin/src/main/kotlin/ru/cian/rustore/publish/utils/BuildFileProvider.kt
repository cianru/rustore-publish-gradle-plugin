package ru.cian.rustore.publish.utils

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import ru.cian.rustore.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(
    private val apkDirectory: Directory?,
    private val builtArtifactsLoader: BuiltArtifactsLoader?,
    private val bundleFile: RegularFile?,
    private val logger: Logger,
) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when (buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat().singleOrNull()
            BuildFormat.AAB -> getFinalBundleArtifactCompat().singleOrNull()
        }
    }

    // FIXME(a.mirko): Remove after https://github.com/gradle/gradle/issues/16777
    // FIXME(a.mirko): Remove after https://github.com/gradle/gradle/issues/16775
    private fun getFinalApkArtifactCompat(): List<File> {
        val dir = apkDirectory ?: return emptyList()
        val loader = builtArtifactsLoader ?: return emptyList()
        logger.v("Build File Directory: $dir")
        return loader.load(dir)
            ?.elements?.map { element -> File(element.outputFile) }
            ?: dir.asFileTree.matching { include("*.apk") }.map { it.absolutePath }.map { File(it) }
            ?: emptyList()
    }

    private fun getFinalBundleArtifactCompat(): List<File> {
        val file = bundleFile?.asFile ?: return emptyList()
        return listOf(file)
    }
}
