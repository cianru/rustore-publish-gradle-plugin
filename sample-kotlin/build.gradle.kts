plugins {
    id("com.android.application")
    id("kotlin-android")
    id("ru.cian.rustore-publish-gradle-plugin")
}

rustorePublish {
    instances {
        create("release") {
            credentialsPath = "$projectDir/rustore-credentials.json"
            releaseNotes = listOf(
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$projectDir/release-notes-ru.txt"
                ),
            )
        }
    }
}

android {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    namespace = "ru.cian.rustore.sample.kotlin"

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
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvm.get()
    }
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(platform(libs.kotlinBom))
}
