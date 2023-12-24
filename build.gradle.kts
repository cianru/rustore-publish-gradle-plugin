// buildscript exist here for sample-groovy app;
buildscript {

    repositories {
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:" + libs.versions.androidGradlePlugin.get())
    }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.gradleDoctor)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

doctor {
    /**
     * Throw an exception when multiple Gradle Daemons are running.
     *
     * Windows is not supported yet, see https://github.com/runningcode/gradle-doctor/issues/84
     */
    disallowMultipleDaemons.set(true)

    /**
     * Warn when not using parallel GC. Parallel GC is faster for build type tasks and is no longer the default in Java 9+.
     */
    warnWhenNotUsingParallelGC.set(true)

    /**
     * Warn if using the Kotlin Compiler Daemon Fallback. The fallback is incredibly slow and should be avoided.
     * https://youtrack.jetbrains.com/issue/KT-48843
     */
    warnIfKotlinCompileDaemonFallback.set(true)

    /** Configuration properties relating to JAVA_HOME */
    javaHome {
        /**
         * Ensure that we are using JAVA_HOME to build with this Gradle.
         */
        ensureJavaHomeMatches.set(true)
        /**
         * Ensure we have JAVA_HOME set.
         */
        ensureJavaHomeIsSet.set(true)
        /**
         * Fail on any `JAVA_HOME` issues.
         */
        failOnError.set(true)
        /**
         * Extra message text, if any, to show with the Gradle Doctor message. This is useful if you have a wiki page or
         * other instructions that you want to link for developers on your team if they encounter an issue.
         */
        extraMessage.set("Gradle Doctor Issue!")
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}

dependencies {
    implementation(platform(libs.kotlinBom))
    implementation(libs.gson)
    implementation(libs.okHttp)
}
