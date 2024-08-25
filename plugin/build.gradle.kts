plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
    alias(libs.plugins.detekt)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinJvm)
}

apply(from = "$projectDir/config/maven-publish.gradle")
apply(from = "$projectDir/config/gradle-portal.gradle")

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

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
}

tasks.withType<Test> {
    useJUnitPlatform {}
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
    implementation(platform(libs.kotlinBom))
    implementation(libs.kotlinDateTime)
    implementation(libs.gson)
    implementation(libs.okHttp)
    implementation(libs.mockServer)
    compileOnly(libs.androidGradlePlugin)
    detektPlugins(libs.detektFormating)
    detektPlugins(libs.detektRules)

    testImplementation(libs.test.junitJupiterApi)
    testImplementation(libs.test.junitJupiterEngine)
    testImplementation(libs.test.junitJupiterParams)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.mockito)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.hamcreast)
    testImplementation(libs.test.assertk)
    testImplementation(libs.androidGradlePlugin)
}
