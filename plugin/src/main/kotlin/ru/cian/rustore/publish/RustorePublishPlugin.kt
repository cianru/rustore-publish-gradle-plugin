package ru.cian.rustore.publish

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

    @Suppress("DefaultLocale")
    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
        rustorePublishExtension: RustorePublishExtension,
    ) {
        val variantName = variant.name.capitalize()
        val publishTaskName = "${RustorePublishTask.TASK_NAME}$variantName"
        val publishTask = project.tasks.register<RustorePublishTask>(publishTaskName, variant)
        val extension = rustorePublishExtension.instances.find { it.name.equals(variant.name, ignoreCase = true) }

        scheduleTasksOrder(publishTask, project, variantName)
    }

    private fun scheduleTasksOrder(
        publishTask: TaskProvider<RustorePublishTask>,
        project: Project,
        variantName: String
    ) {
        project.gradle.projectsEvaluated {
            mustRunAfter(project, publishTask, "assemble$variantName")
            mustRunAfter(project, publishTask, "bundle$variantName")
        }
    }

    private fun mustRunAfter(
        project: Project,
        publishTask: TaskProvider<RustorePublishTask>,
        taskBeforeName: String,
    ) {
        if (project.tasks.findByName(taskBeforeName) != null) {
            val assembleTask = project.tasks.named(taskBeforeName).get()
            publishTask.get().mustRunAfter(assembleTask)
        }
    }
}
