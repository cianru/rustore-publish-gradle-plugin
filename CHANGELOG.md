# 0.5.7

### Features
* Support of Rustore Public API domain migration to certificates issued by Russian Trusted Root CA
([Rustore blog](https://www.rustore.ru/developer/blog/rustore-obnovlyaet-sertifikaty-i-domeny-chto-nuzhno-sdelat-razrabotchikam)).
New params are grouped in `networkSetting { ... }` block:
  * `apiBaseUrl` param (CLI: `--apiBaseUrl`) to choose API domain. Default value is `https://public-api.rustore.ru`,
    the new domain is `https://public-api-m.rustore.ru`.
  * `useBundledRussianTrustedRootCa` param (CLI: `--useBundledRussianTrustedRootCa`) to trust Russian Trusted Root CA
    bundled into the plugin. Default value is `false`.
  * `trustedCertificatePath` param (CLI: `--trustedCertificatePath`) to trust custom CA certificate file.
  * TLS errors are reported as `RustoreTlsException` with the list of ways to fix the certificate trust.

### Fix
* [issue#37](https://github.com/cianru/rustore-publish-gradle-plugin/issues/37) Rustore API errors without json body
(e.g. `504 Gateway Timeout` with `upstream request timeout` from Rustore gateway on large AAB upload) were masked
as `JsonSyntaxException` and `Request is failed. Something went wrong, please check request!`.
Now the plugin fails with `RustoreHttpException` that contains HTTP code, request method, url and response body:
```
Rustore API request failed: HTTP 504 Gateway Timeout for POST https://public-api.rustore.ru/public/v1/application/<package>/version/<versionId>/aab. Response body: 'upstream request timeout'
```
Note: `504 upstream request timeout` is a timeout on the Rustore gateway side, the `requestTimeout` param doesn't affect it.


# 0.5.6

### Add
* Added the optional `appType` param (Gradle Extension DSL `ru.cian.rustore.publish.AppTypes.MAIN` / `.GAMES`,
CLI `--appType=MAIN`) that sets the type of application version on draft creation.
If it is not set, the value is not sent to the Rustore API.
See documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version

### Breaking Change
* [issue#35](https://github.com/cianru/rustore-publish-gradle-plugin/issues/35) The `seoTags` param is renamed to `seoTagIds` and now accepts numeric RuStore tag ids (`List<Int>`, CLI `--seoTagIds=116,69`) 
instead of the `ru.cian.rustore.publish.SeoTag` enum. The `SeoTag` enum has been removed. 
See the tag id list in documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
```kotlin
  seoTagIds = listOf(116, 69)
```
I had to do this because the Rustore API developers change the values ids without warning, 
which breaks backward compatibility.

# 0.5.5

### Fix
* [issue#30](https://github.com/cianru/rustore-publish-gradle-plugin/issues/30) Configuration cache state could not be cached: Future was not completed yet 


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

### Add
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

### Add
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

### Add
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

### Breaking Changes
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

### Breaking Changes
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

### Add
* [issue#5](https://github.com/cianru/rustore-publish-gradle-plugin/issues/5) Support of serviceType plugin param.


# 0.3.1

### Add
* Support of Gradle 8+, Android Gradle Plugin 8+ and JDK17+


# 0.2.2

### Fix
* Add mustRunAfter assemble and bundle tasks without side effect on configuration cache 


# 0.2.1

### Fix
* ReleaseNotes doesn't work #1


# 0.2.0

First released version. Support:
* Publish APK in Rustore and submit it on all users after got store approve
