# 0.5.1

##### Add
* [issue#15](https://github.com/cianru/rustore-publish-gradle-plugin/issues/15) Support of `publishType` param.
```groovy
    /**
     * (Optional)
     * CLI: `--publishType`
     * ----| 'instantly' – the application will be published immediately after the review process is completed.
     * ----| 'manual' – the application must be published manually by the developer after ther review process is completed.
     * Gradle Extenion DSL, available values:
     * ----| ru.cian.rustore.publish.PublishType.INSTANTLY
     * ----| ru.cian.rustore.publish.PublishType.MANUAL
     * Default value: `instantly`
     */  
  publishType = ru.cian.rustore.publish.PublishType.INSTANTLY
```

# 0.5.0

##### Add
* [issue#9](https://github.com/cianru/rustore-publish-gradle-plugin/issues/9) Support of AAB files. Now you can publish AAB files to Rustore.
Just use a new parameter `buildFormat` in your configuration:
```kotlin
configure<ru.cian.rustore.publish.RustorePublishExtension> {
    instances {
        register("release") {
            buildFormat = ru.cian.rustore.publish.BuildFormat.AAB 
        }
    }
}
```
if your file is large, you can increase the timeout by a new parameter `requestTimeout` in seconds:
```kotlin
configure<ru.cian.rustore.publish.RustorePublishExtension> {
    instances {
        register("release") {
            ...
            buildFormat = ru.cian.rustore.publish.BuildFormat.AAB 
            requestTimeout = 1800 // seconds;
            ....
        }
    }
}
```

##### Breaking Changes
* Remove support of Sonatype. It means that you can't use the plugin from Maven Central. You must to use the Gradle Portal. 
  To do this, you need to add the following code to your `settings.gradle.kts`:
  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
      }
  }
  ```
  

# 0.4.0

##### Breaking Changes
* [issue#8](https://github.com/cianru/rustore-publish-gradle-plugin/issues/8) The companyId in POST /public/auth/ will be deprecated by new documentation.
  According to [message](https://t.me/rustoredev/476) the `companyId` parameter of `POST /public/auth/` request is deprecated from 30 Jule 2024. Need to use `keyId` instead of. 
  
  You need to get `keyId` by [instruction](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-token/) and change the credentials file from:
  ```json
  {
     "company_id": "<COMPANY_ID>",
     "client_secret": "<CLIENT_SECRET>"
  }
  ```
  to:
  ```json
  {
     "key_id": "<KEY_ID>",
     "client_secret": "<CLIENT_SECRET>"
  }
  ```

# 0.3.2

##### Add
* [issue#5](https://github.com/cianru/rustore-publish-gradle-plugin/issues/5) Support of serviceType plugin param.


# 0.3.1

##### Add
* Support of Gradle 8+, Android Gradle Plugin 8+ and JDK17+


# 0.2.2

##### Fix
* Add mustRunAfter assemble and bundle tasks without side effect on configuration cache 


# 0.2.1

##### Fix
* ReleaseNotes doesn't work #1


# 0.2.0

First released version. Support:
* Publish APK in Rustore and submit it on all users after got store approve
