plugins {
    id("com.android.application")
    id("kotlin-android")
    id("ru.cian.rustore-publish-gradle-plugin")
}

rustorePublish {
    instances {
        create("release") {
            credentialsPath = "$projectDir/rustore-credentials.json"
            mobileServicesType = ru.cian.rustore.publish.MobileServicesType.HMS
            buildFormat = ru.cian.rustore.publish.BuildFormat.AAB
            requestTimeout = 60
            seoTags = listOf(
                ru.cian.rustore.publish.SeoTag.LIFESTYLE,
                ru.cian.rustore.publish.SeoTag.ROMANTIC,
            )
            minAndroidVersion = "8"
            developerContacts = ru.cian.rustore.publish.DeveloperContacts(
                email = "mhelp@mysite.com",
                website = "www.mysite.com",
                vkCommunity = null,
            )
            releaseNotes = listOf(
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$projectDir/release-notes-ru.txt"
                ),
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "en-EN",
                    filePath = "$projectDir/release-notes-en.txt"
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(libs.versions.jvm.get()))
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
