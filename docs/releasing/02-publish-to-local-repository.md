## Pushing a SNAPSHOT build to local repository

1. Publish to local repository from the project root directory:
   ```bash
   ./gradlew :plugin:publishToMavenLocal
   ```
2. Remove local repository to apply remote build repository
   ```bash
   rm -rv ~/.m2/repository/ru/cian/rustore-publish-gradle-plugin/
   ```
