# 0.5.3

### Fix
* [issue#29](https://github.com/cianru/rustore-publish-gradle-plugin/issues/29) Fix API erros: 

```
{
    "code":"ERROR",
    "message":"Minimal android version is not filled in",
    "body":null,
    "timestamp":"2026-02-08T22:34:43.728Z"
}
```
```
{
    "code":"ERROR",
    "message":"Developer contacts are not filled in",
    "body":null,
    "timestamp":"2026-02-08T23:29:03.035Z"
}
```


# 0.5.2

##### Add
* [issue#22](https://github.com/cianru/rustore-publish-gradle-plugin/issues/22) Support of `seoTags` param to control [SEO tags list](https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list).
```kotlin
  /**
   * (Optional)
   * List of available SEO tags for RuStore app listing.
   * For more details see documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
   * Number of tags should not be greater than 5.
   * Default value: []
   * CLI: `--seoTags`. For example: `--seoTags=LIFESTYLE,ROMANTIC`
   * Gradle Extension DSL, available values from ru.cian.rustore.publish.SeoTag
   */
  seoTags = listOf(
     ru.cian.rustore.publish.SeoTag.LIFESTYLE,
     ru.cian.rustore.publish.SeoTag.ROMANTIC,
  )  
```
Thanks to @iyakovlev for PR #22

# 0.5.1

##### Add
* [issue#15](https://github.com/cianru/rustore-publish-gradle-plugin/issues/15) Support of `publishType` param to control the publish process.
```kotlin
    /**
     * (Optional)
     * CLI: `--publishType`
     * ----| 'instantly' – the application will be published immediately after the review process is completed.
     * ----| 'manual' – the application must be published manually by the developer after ther review process is completed.
     * Gradle Extension DSL, available values:
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
