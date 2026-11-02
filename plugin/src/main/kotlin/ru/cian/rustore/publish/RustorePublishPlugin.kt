package ru.cian.rustore.publish

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class RustorePublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin(ANDROID_APPLICATION_PLUGIN_ID) {
            configureRustorePublish(project)
        }
    }

    private fun configureRustorePublish(project: Project) {
        val rustorePublishExtension = project.extensions.create(
            RustorePublishExtension.MAIN_EXTENSION_NAME,
            RustorePublishExtension::class.java,
        )

        val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        androidComponents.onVariants(androidComponents.selector().all()) { variant ->
            createTask(project, variant, rustorePublishExtension)
        }
    }

    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
        rustorePublishExtension: RustorePublishExtension,
    ) {
        val extension = rustorePublishExtension.instances.find { it.name.equals(variant.name, ignoreCase = true) }
            ?: return
        val capitalizedVariantName = variant.name.replaceFirstChar { it.uppercaseChar() }
        val publishTaskName = "${RustorePublishTask.TASK_NAME}$capitalizedVariantName"

        project.tasks.register(publishTaskName, RustorePublishTask::class.java) {
            description = "Upload and publish application build file " +
                "to RuStore for ${variant.name} buildType"
            applicationId.set(variant.applicationId)
            variantName.set(variant.name)
            apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
            bundleFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            extensionConfig = extension
            // Build tasks are resolved lazily when the task graph is calculated,
            // so tasks registered by AGP after `onVariants` are found as well;
            mustRunAfter(
                project.provider {
                    listOf("assemble$capitalizedVariantName", "bundle$capitalizedVariantName")
                        .mapNotNull { project.tasks.findByName(it) }
                }
            )
        }
    }

    private companion object {

        const val ANDROID_APPLICATION_PLUGIN_ID = "com.android.application"
    }
}
