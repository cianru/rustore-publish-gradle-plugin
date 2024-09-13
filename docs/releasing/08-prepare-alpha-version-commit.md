## Prepare Next Snapshot Version Commit

1. Create new `alpha-<version>` Git branch
2. Open the plugin directory:
    ```
    cd ./plugin
    ```
3. Edit the `gradle.properties` file to set new `VERSION_NAME`+ `-alpha<number>` version. For example: `1.0.0-alpha01`.
4. Make a *signed* commit:
   ```bash
   git commit -m "Prepare next development version"
   ```
