## Pushing a SNAPSHOT build to local repository

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
2. Publish to local repository
   ```bash
   ./gradlew :plugin:publishToMavenLocal
   ```
3. Remove local repository to apply remote build repository
   ```bash
   rm -rv ~/.m2/repository/ru/cian/rustore-publish-gradle-plugin/
   ```
