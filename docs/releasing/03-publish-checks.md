# Before publishing checks

1. Verify that the everything works:
   ```bash
   ./gradlew clean check
   ```
1. Verify that the plugin works with supported Gradle and AGP versions:
   ```bash
   ./gradlew :plugin:functionalTest
   ```
1. Verify that the code style is correct:
   ```bash
   ./gradlew detekt
   ``` 
