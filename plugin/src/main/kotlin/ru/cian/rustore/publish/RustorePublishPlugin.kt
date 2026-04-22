package ru.cian.rustore.publish

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantSelector
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
        androidComponents.onVariants(androidComponents.selector().all() as VariantSelector) { variant ->
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
            val capitalizedVariantName = variant.name.capitalize()
            val publishTaskName = "${RustorePublishTask.TASK_NAME}$capitalizedVariantName"
            val publishTask = project.tasks.register<RustorePublishTask>(publishTaskName)

            publishTask.configure {
                description = "Upload and publish application build file " +
                    "to RuStore for ${variant.name} buildType"
                applicationId.set(variant.applicationId)
                variantName.set(variant.name)
                apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
                bundleFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
                builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                extensionConfig = extension
            }

            scheduleTasksOrder(publishTask, project, capitalizedVariantName)
        }
    }

    private fun scheduleTasksOrder(
        publishTask: TaskProvider<RustorePublishTask>,
        project: Project,
        variantName: String
    ) {
        project.afterEvaluate {
            mustRunAfter(publishTask, "assemble$variantName")
            mustRunAfter(publishTask, "bundle$variantName")
        }
    }

    private fun Project.mustRunAfter(
        publishTask: TaskProvider<RustorePublishTask>,
        taskBeforeName: String,
    ) {
        if (tasks.findByName(taskBeforeName) != null) {
            publishTask.configure {
                mustRunAfter(tasks.named(taskBeforeName))
            }
        }
    }
}
