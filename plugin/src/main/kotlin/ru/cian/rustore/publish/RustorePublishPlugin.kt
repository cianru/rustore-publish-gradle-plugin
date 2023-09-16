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

    private val tasksMustRunAfter = mutableMapOf<String, String>()

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
        val applyConfigureOptimization = extension?.pluginSettings?.applyConfigureOptimization
        tasksMustRunAfter.put(publishTaskName, "assemble$variantName")
        tasksMustRunAfter.put(publishTaskName, "bundle$variantName")

        sheduleTasksOrder(applyConfigureOptimization, publishTask, project, variantName)
    }

    private fun sheduleTasksOrder(
        applyConfigureOptimization: Boolean?,
        publishTask: TaskProvider<RustorePublishTask>,
        project: Project,
        variantName: String
    ) {
        if (applyConfigureOptimization == true) {
            publishTask.configure {
    //            println("--> RustorePublishTask registered: ($publishTaskName)")
    //            project.tasks.forEach {
    //                println("--> (1): ${it.name}")
    //            }
    //                setMustRunAfter(
    //                    setOf(
    ////                        project.tasks.named("clean"),
    //                        project.tasks.named("assemble$variantName"),
    //                        project.tasks.named("bundle$variantName"),
    //                    )
    //                )
                if (project.tasks.findByName("assemble$variantName") != null) {
                    setMustRunAfter(setOf(project.tasks.named("assemble$variantName")))
                }
                if (project.tasks.findByName("bundle$variantName") != null) {
                    setMustRunAfter(setOf(project.tasks.named("bundle$variantName")))
                }
            }
        } else {
            project.tasks.whenTaskAdded {
                if (this.name == "assemble$variantName" || this.name == "bundle$variantName") {
                    publishTask.get().mustRunAfter(this)
                }
            }
        }
    }
}
