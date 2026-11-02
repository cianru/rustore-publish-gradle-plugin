## Pushing a release build to Gradle Plugin Portal

1. Edit the `plugin/gradle.properties` file:
   Remove `-alpha<number>` from the `VERSION_NAME` and set the version to the release version. For example: `1.0.0`.
1. Verify that the everything works, see [how to do the checks](03-publish-checks.md):
1. Upload binaries to Gradle's plugin portal from the project root directory:
   ```bash
   ./gradlew :plugin:publishPlugins
   ```
1. Check uploaded plugin and version at [Gradle Plugin Portal site](https://plugins.gradle.org/plugin/ru.cian.rustore-publish-gradle-plugin).
