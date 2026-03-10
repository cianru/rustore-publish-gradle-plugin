package ru.cian.rustore.publish.utils

import ru.cian.rustore.publish.BuildFormat
import java.io.File

internal interface BuildFileResolver {
    fun getBuildFile(buildFormat: BuildFormat): File?
}

internal class BuildFileProvider(
    private val rustoreLogger: RustoreLogger,
    private val apkDirectory: File?,
    private val bundleFile: File?,
) : BuildFileResolver {

    override fun getBuildFile(buildFormat: BuildFormat): File? {
        return when (buildFormat) {
            BuildFormat.APK -> findApk()
            BuildFormat.AAB -> bundleFile?.takeIf { it.exists() }
        }
    }

    private fun findApk(): File? {
        val directory = apkDirectory ?: return null
        if (!directory.exists()) return null

        rustoreLogger.v("Build File Directory: $directory")

        val apkFiles = directory
            .walkTopDown()
            .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
            .sortedBy { it.name }
            .toList()

        if (apkFiles.size > 1) {
            rustoreLogger.v("Multiple APK files found, selecting first: ${apkFiles.first().name}")
        }

        return apkFiles.firstOrNull()
    }
}
