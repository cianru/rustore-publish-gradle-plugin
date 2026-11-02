package ru.cian.rustore.publish;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Applies the plugin to an Android application with Kotlin DSL build script
 * and publishes a build file to the API stub on every Gradle/AGP pair from `functionalTest.matrix`.
 * <p>
 * The plugin classes are put on the root `buildscript` classpath together with AGP,
 * the same way as a published plugin is resolved by a consumer build.
 */
class RustorePublishPluginFunctionalTest {

    @TempDir
    Path projectDir;

    static Stream<String[]> gradleAndAgpVersions() {
        String matrix = System.getProperty("functionalTest.matrix");
        assertFalse(matrix == null || matrix.isBlank(), "`functionalTest.matrix` system property is not set");
        return Arrays.stream(matrix.split(",")).map(pair -> pair.split(":"));
    }

    @ParameterizedTest(name = "Gradle {0}, AGP {1}")
    @MethodSource("gradleAndAgpVersions")
    void publishesBuildFileToApiStub(String gradleVersion, String agpVersion) throws IOException {
        writeProject(agpVersion);

        BuildResult result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withGradleVersion(gradleVersion)
            .withArguments(
                ":app:publishRustoreRelease",
                "--apiStub",
                "--buildFile=" + projectDir.resolve("app-release.apk"),
                "--configuration-cache",
                "--stacktrace"
            )
            .forwardOutput()
            .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:publishRustoreRelease").getOutcome());
        assertTrue(
            result.getOutput().contains("Upload and submit build file - Successfully Done!"),
            "Build file must be uploaded and submitted"
        );
    }

    private void writeProject(String agpVersion) throws IOException {
        String androidSdkDir = System.getProperty("functionalTest.androidSdkDir", "");
        assertFalse(androidSdkDir.isBlank(), "Android SDK is not found: set `sdk.dir` in local.properties or ANDROID_HOME");

        Files.write(projectDir.resolve("plugin-classpath.txt"), pluginClasspath(), StandardCharsets.UTF_8);
        write("local.properties", "sdk.dir=" + androidSdkDir.replace("\\", "\\\\") + "\n");
        write("gradle.properties", "android.useAndroidX=true\n");
        write("settings.gradle.kts", "rootProject.name = \"functional-test\"\ninclude(\":app\")\n");
        write("build.gradle",
            "buildscript {\n"
                + "    repositories {\n"
                + "        google()\n"
                + "        mavenCentral()\n"
                + "    }\n"
                + "    dependencies {\n"
                + "        classpath \"com.android.tools.build:gradle:" + agpVersion + "\"\n"
                + "        classpath files(file(\"plugin-classpath.txt\").readLines())\n"
                + "    }\n"
                + "}\n"
        );
        write("credentials.json", "{\"key_id\": \"test-key-id\", \"client_secret\": \"test-client-secret\"}\n");
        write("app-release.apk", "apk stub");
        write("app/build.gradle.kts",
            "plugins {\n"
                + "    id(\"com.android.application\")\n"
                + "    id(\"ru.cian.rustore-publish-gradle-plugin\")\n"
                + "}\n"
                + "\n"
                + "android {\n"
                + "    namespace = \"ru.cian.rustore.functional\"\n"
                + "    compileSdk = 35\n"
                + "    defaultConfig {\n"
                + "        applicationId = \"ru.cian.rustore.functional\"\n"
                + "        minSdk = 21\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "rustorePublish {\n"
                + "    instances {\n"
                + "        create(\"release\") {\n"
                + "            credentialsPath = \"${rootDir}/credentials.json\"\n"
                + "            buildFormat = ru.cian.rustore.publish.BuildFormat.APK\n"
                + "            developerContacts = ru.cian.rustore.publish.DeveloperContacts(\n"
                + "                email = \"mhelp@mysite.com\",\n"
                + "                website = null,\n"
                + "                vkCommunity = null,\n"
                + "            )\n"
                + "            networkSetting {\n"
                + "                connectTimeout = 10\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
        );
    }

    private static List<String> pluginClasspath() throws IOException {
        try (InputStream stream = RustorePublishPluginFunctionalTest.class
            .getClassLoader()
            .getResourceAsStream("plugin-under-test-metadata.properties")) {
            assertFalse(stream == null, "`plugin-under-test-metadata.properties` is not found");
            Properties properties = new Properties();
            properties.load(stream);
            return Arrays.stream(properties.getProperty("implementation-classpath").split(File.pathSeparator))
                .map(path -> path.replace("\\", "/"))
                .collect(Collectors.toList());
        }
    }

    private void write(String path, String content) throws IOException {
        Path file = projectDir.resolve(path);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }
}
