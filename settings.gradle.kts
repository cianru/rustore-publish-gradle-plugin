include("plugin")

include(
    ":sample-kotlin",
    ":sample-groovy",
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val libsVersionFile = file("gradle/libs.versions.toml")
    val properties = java.util.Properties().apply {
        libsVersionFile.reader().use { load(it) }
    }
    val samplePublishVersion = properties.getProperty("sampleRustorePlugin").replace("\"", "")

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian.rustore-plugin:plugin:${samplePublishVersion}")
            }
        }
    }

    plugins {
        id("ru.cian.rustore-publish-gradle-plugin") version samplePublishVersion apply false
    }

    repositories {
        gradlePluginPortal()
        mavenLocal()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

plugins {
    id("com.gradle.develocity") version("4.6.1")
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        // To publish report add `-Pscan` to build command;
        val publishScan = settings.extra.has("scan")
        publishing.onlyIf { publishScan }
    }
}
