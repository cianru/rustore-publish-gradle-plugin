plugins {
    id("com.android.application")
    id("kotlin-android")
//    id("ru.cian.rustore-publish")
    id("ru.cian.rustore-publish-gradle-plugin")
}

rustorePublish {
    instances {
        create("release") {
            credentialsPath = "$projectDir/rustore-credentials.json"
            deployType = ru.cian.rustore.publish.DeployType.DRAFT
            buildFormat = ru.cian.rustore.publish.BuildFormat.AAB
            releaseTime = "2025-10-21T06:00:00+0300"
            releasePhase = ru.cian.rustore.publish.ReleasePhaseExtension(
                percent = 1.0
            )
            releaseNotes = listOf(
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$projectDir/release-notes-ru.txt"
                ),
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "en-US",
                    filePath = "$projectDir/release-notes-en.txt"
                )
            )
            pluginSettings = ru.cian.rustore.publish.PluginSettings(
                applyConfigureOptimization = true
            )
        }
    }
}

android {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    buildToolsVersion = libs.versions.buildToolsVersion.get()

    defaultConfig {
        applicationId = "ru.cian.rustore.sample_kotlin"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.kotlinStdlib)
}
