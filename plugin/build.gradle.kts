import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.Properties

plugins {
    `java-gradle-plugin`
    `maven-publish`
    `signing`
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.kotlinSamWithReceiver)
    alias(libs.plugins.detekt)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.dokka)
}

apply(from = "$projectDir/config/gradle-portal.gradle")

// The plugin runs inside Gradle with Gradle's embedded Kotlin stdlib,
// so the Kotlin API/language version must not be newer than embedded Kotlin of the oldest supported Gradle (8.0 -> 1.8).
val minSupportedKotlin = KotlinVersion.KOTLIN_1_8

// `java-gradle-plugin` adds API of the running Gradle to `compileOnlyApi` (it brings Gradle's embedded Kotlin stdlib,
// unreadable by the compiler targeting Kotlin 1.8). Compile against API of the oldest supported Gradle instead,
// see `libs.gradleApiMinSupported` dependency below;
configurations.compileOnlyApi {
    dependencies.removeIf { it is FileCollectionDependency }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
}

kotlin {
    coreLibrariesVersion = "${minSupportedKotlin.version}.0"
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
        apiVersion.set(minSupportedKotlin)
        languageVersion.set(minSupportedKotlin)
        // Kotlin 1.8 is deprecated, but it's intentional (the same as `kotlin-dsl` does);
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}

// Same as `kotlin-dsl`: lambdas for Gradle `Action<T>` get `T` as receiver;
samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

detekt {

    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source.setFrom("src/main/java", "src/main/kotlin")

    // Specifying a baseline file. All findings stored in this file in subsequent runs of detekt.
    // A way of suppressing issues before introducing detekt.
    baseline = file("$projectDir/config/detekt/detekt-baseline.xml")

    // Builds the AST in parallel. Rules are always executed in parallel.
    // Can lead to speedups in larger projects. `false` by default.
    parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config.setFrom("$projectDir/config/detekt/detekt-config.yml")

    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = true

    // Turns on all the rules. `false` by default.
    allRules = false

    // Disables all default detekt rulesets and will only run detekt with custom rules
    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.
    disableDefaultRuleSets = false

    // Adds debug output during task execution. `false` by default.
    debug = false

    // If set to `true` the build does not fail when the
    // maxIssues count was reached. Defaults to `false`.
    ignoreFailures = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.html"))
        }
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

dokka {
    dokkaPublications.configureEach {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Functional tests apply the plugin to Android project and run it on several Gradle and AGP versions
// (oldest supported, latest 8.x and current). It requires network access and Android SDK;
val functionalTest: SourceSet = sourceSets.create("functionalTest")

gradlePlugin {
    testSourceSets(functionalTest)
}

// Test the plugin jar as consumers get it instead of class directories;
tasks.pluginUnderTestMetadata {
    pluginClasspath.setFrom(tasks.jar, configurations.runtimeClasspath)
}

val functionalTestMatrix = listOf(
    libs.versions.minSupportedGradle.get() to libs.versions.androidGradlePluginMinSupported.get(),
    libs.versions.functionalTestGradle8.get() to libs.versions.functionalTestAndroidGradlePlugin8.get(),
    GradleVersion.current().version to libs.versions.androidGradlePlugin.get(),
)

val androidSdkDir: Provider<String> = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText
    .map { text -> Properties().apply { load(text.reader()) }.getProperty("sdk.dir").orEmpty() }
    .filter { it.isNotBlank() }
    .orElse(providers.environmentVariable("ANDROID_HOME"))
    .orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs the plugin on Android projects with supported Gradle and AGP versions."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    systemProperty(
        "functionalTest.matrix",
        functionalTestMatrix.joinToString(",") { (gradle, agp) -> "$gradle:$agp" },
    )
    systemProperty("functionalTest.androidSdkDir", androidSdkDir.getOrElse(""))
    mustRunAfter(tasks.test)
}

// See issue: https://youtrack.jetbrains.com/issue/KT-46466/Kotlin-MPP-publishing-Gradle-7-disables-optimizations-because-of-task-dependencies#focus=Comments-27-6349423.0-0
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation(libs.kotlinDateTime)
    implementation(libs.gson)
    implementation(libs.okHttp)
    implementation(libs.mockServer)
    compileOnly(libs.gradleApiMinSupported)
    compileOnly(libs.androidGradlePluginApi)
    detektPlugins(libs.detektFormating)
    detektPlugins(libs.detektRules)

    testImplementation(platform(libs.test.junitBom))
    testImplementation(libs.test.junitJupiter)
    testRuntimeOnly(libs.test.junitPlatformLauncher)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.mockito)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.hamcreast)
    testImplementation(libs.test.assertk)
    testImplementation(libs.test.okhttpTls)
    testImplementation(libs.gradleApiMinSupported)
    testImplementation(libs.androidGradlePluginApi)

    "functionalTestImplementation"(platform(libs.test.junitBom))
    "functionalTestImplementation"(libs.test.junitJupiter)
    "functionalTestRuntimeOnly"(libs.test.junitPlatformLauncher)
}
