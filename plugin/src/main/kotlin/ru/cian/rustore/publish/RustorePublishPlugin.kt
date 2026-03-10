package ru.cian.rustore.publish

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class RustorePublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withType<AppPlugin> {
            configureRustorePublish(project)
        }
    }

    private fun configureRustorePublish(project: Project) {
        val rustorePublishExtension = project.extensions.create<RustorePublishExtension>(
            RustorePublishExtension.MAIN_EXTENSION_NAME,
            project
        )

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants(androidComponents.selector().all()) { variant ->
            createTask(project, variant, rustorePublishExtension)
        }
    }

    @Suppress("DefaultLocale", "UnusedPrivateProperty")
    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
        rustorePublishExtension: RustorePublishExtension,
    ) {
        val extension = rustorePublishExtension.instances.find { it.name.equals(variant.name, ignoreCase = true) }
        if (extension != null) {
            val extensionSnapshot = ExtensionConfigSnapshotMapper.buildExtensionSnapshot(project, extension)
            val variantName = variant.name.replaceFirstChar { it.titlecase() }
            val publishTaskName = "${RustorePublishTask.TASK_NAME}$variantName"
            val publishTask = project.tasks.register<RustorePublishTask>(publishTaskName) {
                description = "Upload and publish application build file to RuStore for ${variant.name} buildType"
                applicationId.set(variant.applicationId)
                this.variantName.set(variant.name)
                apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
                bundleFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
                this.extensionConfig = extensionSnapshot
            }

            scheduleTasksOrder(publishTask, project, variantName)
        }
    }

    private fun scheduleTasksOrder(
        publishTask: TaskProvider<RustorePublishTask>,
        project: Project,
        variantName: String
    ) {
        listOf("assemble$variantName", "bundle$variantName").forEach { taskName ->
            publishTask.configure {
                mustRunAfter(project.tasks.named(taskName))
            }
        }
    }
}
