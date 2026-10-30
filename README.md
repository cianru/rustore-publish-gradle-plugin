
[comment]: # (Markdown formating https://docs.github.com/ru/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax)

[//]: # (<p align="center">)
[//]: # (  <img src="docs/screenshots/logo.png" width="400">)
[//]: # (</p>)

<p align="center">
  <img src="docs/screenshots/header_cian_rustore.png">
  <h1 align="center">
    RuStore Publishing
  </h1>
</p>

[//]: # (![Version]&#40;https://img.shields.io/badge/GradlePortal-0.5.0-green.svg&#41;)
<img src="https://img.shields.io/maven-metadata/v.svg?label=Gradle%20Plugins%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fru%2Fcian%2Frustore-publish-gradle-plugin%2Fru.cian.rustore-publish-gradle-plugin.gradle.plugin%2Fmaven-metadata.xml" />
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin use [Rustore API](https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/publish_RuStore_API) to publish Android build file to the [RuStore](https://rustore.ru). 

:construction: _That's unofficial plugin. We made it for ourselves and are sharing it for you._

# Table of contents
<!-- TOC -->
- [Features](#features)
- [Compatibility](#compatibility)
- [Adding the plugin to your project](#adding-the-plugin-to-your-project)
    - [Using the Gradle plugin DSL](#using-the-gradle-plugin-dsl)
    - [Using the `apply` method](#using-the-apply-method)
    - [Quickstart Plugin Configuration](#quickstart-plugin-configuration)
    - [Full Plugin Configuration](#full-plugin-configuration)
- [Plugin usage](#plugin-usage)
- [CLI Plugin Configuration](#cli-plugin-configuration)
- [Promotion](#promotion)
- [License](#license)
<!-- /TOC -->

# Features

The following features are available:

- :white_check_mark: Publish APK and AAB build file in RuStore
- :white_check_mark: Submit the build on all users after getting store approve
- :white_check_mark: Update Release Notes for publishing build (Release Notes)
- :white_check_mark: Separated settings for different configurations build types and flavors
- :white_check_mark: Support of Gradle Portal and Gradle DSL
- :white_check_mark: Support of Configuration Cache
- :white_check_mark: Support of Gradle 8.+

The following features are missing:

- :children_crossing: Change App Store Information: description, app icon, screenshots and etc.
- :children_crossing: Publish the build on a part of users (Release Phases)

The following features are not available on Rustore API side yet:

- :no_entry: Rollout Holding

# Compatibility
The Android Gradle Plugin often changes the Variant API,
so a different version of AGP corresponds to a specific version of the current plugin

| AGP | Plugin                                                                     |
|-----|----------------------------------------------------------------------------|
| 7.+ | 0.2.2                                                                      |
| 8.+ | [latest](https://github.com/cianru/rustore-publish-gradle-plugin/releases) |

# Adding the plugin to your project

in application module `./app/build.gradle`

## Using the Gradle plugin DSL

```kotlin
plugins {
    id("com.android.application")
    id("ru.cian.rustore-publish-gradle-plugin")
}
```

## Using the `apply` method

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath "ru.cian.rustore-plugin:plugin:<VERSION>"
    }
}

apply plugin: 'com.android.application'
apply plugin: 'ru.cian.rustore-publish-gradle-plugin'
```

## Quickstart Plugin Configuration

```kotlin
rustorePublish {
  instances {
    create("release") {
      /**
       * Path to json file with RuStore credentials params (`key_id` and `client_secret`).
       * How to get credentials see [[RU] Rustore API Getting Started](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-process/).
       * Plugin credential json example:
       * {
       *   "key_id": "<KEY_ID>",
       *   "client_secret": "<CLIENT_SECRET>"
       * }
       *
       * Type: String (Optional)
       * Default value: `null` (but plugin wait that you provide credentials by CLI params)
       * CLI: `--credentialsPath`
       */
       credentialsPath = "$rootDir/rustore-credentials-release.json"

       /**
        * Build file format.
        * See https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/upload-aab how to prepare project for loading of aab files.
        * Type: String (Optional)
        * CLI: `--buildFormat`, available values:
        * ----| 'apk'
        * ----| 'aab'
        * Gradle Extension DSL, available values:
        * ----| ru.cian.rustore.publish.BuildFormat.APK
        * ----| ru.cian.rustore.publish.BuildFormat.AAB
        * Default value: `apk`
        */
       buildFormat = ru.cian.rustore.publish.BuildFormat.APK
    }
  }
}
```

<details>
<summary>Groovy</summary>

```groovy
rustorePublish {
    instances {
        release {
            credentialsPath = "$rootDir/rustore-credentials-release.json"
            buildFormat = "apk"
        }
    }
}
```
</details>

## Full Plugin Configuration

<details open>
<summary>Kotlin</summary>

```kotlin
rustorePublish {
  instances {
    create("release") {
      /**
       * (Required)
       * Path to json file with RuStore credentials params (`key_id` and `client_secret`).
       * How to get credentials see [[RU] Rustore API Getting Started](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-process/).
       * Plugin credential json example:
       * {
       *   "key_id": "<KEY_ID>",
       *   "client_secret": "<CLIENT_SECRET>"
       * }
       *
       * Type: String (Optional)
       * Default value: `null` (but plugin wait that you provide credentials by CLI params)
       * CLI: `--credentialsPath`
       */
      credentialsPath = "$rootDir/rustore-credentials-release.json"
        
      /**
       * (Required)
       * Build file format.
       * See https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/upload-aab how to prepare project for loading of aab files.
       * Type: String (Optional)
       * CLI: `--buildFormat`, available values:
       * ----| 'apk'
       * ----| 'aab'
       * Gradle Extension DSL, available values:
       * ----| ru.cian.rustore.publish.BuildFormat.APK
       * ----| ru.cian.rustore.publish.BuildFormat.AAB
       * Default value: `apk`
       */
      buildFormat = ru.cian.rustore.publish.BuildFormat.APK

       /**
        * (Optional)
        * Path to build file if you would like to change default path. "null" means use standard path for "apk" and "aab" files.
        * Type: String (Optional)
        * Default value: `null`
        * CLI: `--buildFile`
        */
       buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"

      /**
       * (Optional)
       * Type of mobile services used in application.
       * For more details see param `servicesType` in documentation:
       * https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/apk-file-upload/file-upload-apk/
       * CLI: `--mobileServicesType`
       * ----| 'Unknown'
       * ----| 'HMS'
       * Gradle Extension DSL, available values:
       * ----| ru.cian.rustore.publish.MobileServicesType.UNKNOWN
       * ----| ru.cian.rustore.publish.MobileServicesType.HMS
       * Default value: `Unknown`
       */
      mobileServicesType = ru.cian.rustore.publish.MobileServicesType.UNKNOWN

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

        /**
         * (Required)
         * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
         * Type: String
         * Default value: `8`
         * CLI: `--minAndroidVersion`
         */
        minAndroidVersion = "8"

        /**
         * (Required)
         * Information about Developer.
         * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
         * Type: ru.cian.rustore.publish.DeveloperContacts (Required)
         * Default value: `null`
         * CLI: (see DeveloperContacts param desc.)
         */
        developerContacts = ru.cian.rustore.publish.DeveloperContacts(
            email = "mhelp@mysite.com",
            website = "www.mysite.com",
            vkCommunity = null,
        )

        /**
         * (Optional)
         * Type of application version.
         * See description in https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/create-draft-version
         * CLI: `--appType`, available values:
         * ----| 'MAIN' – for non-game applications.
         * ----| 'GAMES' – for games.
         * Gradle Extension DSL, available values:
         * ----| ru.cian.rustore.publish.AppTypes.MAIN
         * ----| ru.cian.rustore.publish.AppTypes.GAMES
         * Default value: `null` (the value is not sent to the Rustore API)
         */
        appType = ru.cian.rustore.publish.AppTypes.MAIN

        /**
         * (Optional)
         * List of available SEO tags for RuStore app listing.
         * For more details see documentation: https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/app-tag-list
         * Number of tags should not be greater than 5.
         * Default value: []
         * CLI: `--seoTagIds`. For example: `--seoTagIds=116,69`
         * Gradle Extension DSL: list of numeric tag ids, for example `listOf(116, 69)`
         */
        seoTagIds = listOf(
            116, // Личные финансы
            67, // Покер 
        )

      /**
       * (Optional)
       * Network settings of requests to Rustore API.
       */
      networkSetting {
          /**
           * (Optional)
           * Base url of Rustore Public API.
           * Rustore moves Public API to the new domain with certificate issued by Russian Trusted Root CA,
           * see https://www.rustore.ru/developer/blog/rustore-obnovlyaet-sertifikaty-i-domeny-chto-nuzhno-sdelat-razrabotchikam
           * Available values:
           * ----| 'https://public-api.rustore.ru' – the domain with international certificate.
           * ----| 'https://public-api-m.rustore.ru' – the new domain with certificate issued by Russian Trusted Root CA.
           *       JVM must trust Russian Trusted Root CA: install it into JVM truststore
           *       or use `useBundledRussianTrustedRootCa` or `trustedCertificatePath` params.
           * Type: String (Optional)
           * Default value: `https://public-api.rustore.ru`
           * CLI: `--apiBaseUrl`
           */
          apiBaseUrl = "https://public-api.rustore.ru"

          /**
           * (Optional)
           * Trust Russian Trusted Root CA (Ministry of Digital Development) certificate bundled into the plugin
           * in addition to JVM default CAs. It's required for `https://public-api-m.rustore.ru` domain
           * if the certificate isn't installed into JVM truststore.
           * The certificate is applied only to plugin requests to Rustore API, JVM global settings are not changed.
           * Type: Boolean (Optional)
           * Default value: `false`
           * CLI: `--useBundledRussianTrustedRootCa`
           */
          useBundledRussianTrustedRootCa = false

          /**
           * (Optional)
           * Path to CA certificate file (X.509 in PEM or DER format, PEM file may contain several certificates)
           * to trust in addition to JVM default CAs. For example, Russian Trusted Root CA from https://www.gosuslugi.ru/crt
           * The certificate is applied only to plugin requests to Rustore API, JVM global settings are not changed.
           * Type: String (Optional)
           * Default value: `null`
           * CLI: `--trustedCertificatePath`
           */
          trustedCertificatePath = null

          /**
           * (Optional)
           * Timeout in seconds to establish TCP and TLS connection to Rustore API. `0` means no timeout.
           * Type: Long (Optional)
           * Default value: `30`
           * CLI: `--connectTimeout`
           */
          connectTimeout = 30

          /**
           * (Optional)
           * Timeout in seconds between data packets while reading Rustore API response,
           * including waiting for the response after build file upload. `0` means no timeout.
           * Type: Long (Optional)
           * Default value: `300` // (5min)
           * CLI: `--readTimeout`
           */
          readTimeout = 300

          /**
           * (Optional)
           * Timeout in seconds between data packets while sending request body to Rustore API,
           * including build file upload. It limits a pause, not the total upload time. `0` means no timeout.
           * Type: Long (Optional)
           * Default value: `60` // (1min)
           * CLI: `--writeTimeout`
           */
          writeTimeout = 60
      }

      /**
       * (Optional)
       * Release Notes settings. For mote info see ReleaseNote param desc.
       * Type: List<ReleaseNote> (Optional)
       * Default value: `null`
       * CLI: (see ReleaseNotes param desc.)
       */
      releaseNotes = listOf(
        /**
         * Release Note list item.
         */
        ru.cian.rustore.publish.ReleaseNote(
          /**
           * Description: Support only `ru-RU` lang.
           * Type: String (Required)
           * Default value: `null`
           * CLI: (See `--releaseNotes` desc.)
           */
          lang = "ru-RU",

          /**
           * Absolutely path to file with Release Notes for current `lang`. Release notes text must be less or equals to 500 sign.
           * Type: String (Required)
           * Default value: `null`
           * CLI: (See `--releaseNotes` desc.)
           */
          filePath = "$projectDir/release-notes-ru.txt"
        ),
      )
    }
    create("debug") {
      ...
    }
  }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
rustorePublish {
    instances {
        release {
            credentialsPath = "$rootDir/rustore-credentials-release.json"
            buildFormat = "apk"
            buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"
            seoTagIds = [
                116,
                69,
            ]
            mobileServicesType = "Unknown"
            appType = "MAIN"
            networkSetting {
                apiBaseUrl = "https://public-api.rustore.ru"
                useBundledRussianTrustedRootCa = false
                trustedCertificatePath = null
                connectTimeout = 30
                readTimeout = 300 // 5min
                writeTimeout = 60 // 1min
            }
            releaseNotes = [
                new ru.cian.rustore.publish.ReleaseNote(
                    "ru-RU",
                    "$projectDir/release-notes-ru.txt"
                ),
            ]
        }
        debug {
            ...
        }
    }
}
```
</details>

Also the plugin support different buildType and flavors.
So for `demo` and `full` flavors and `release` buildType just change instances like that:
```kotlin
rustorePublish {
    instances {
      create("release") {
          credentialsPath = "$rootDir/rustore-credentials-release.json"
          ...
      }
      create("demoRelease") {
          credentialsPath = "$rootDir/rustore-credentials-demo-release.json"
          ...
      }
      create("fullRelease") {
          credentialsPath = "$rootDir/rustore-credentials-full-release.json"
          ...
      }
    }
}
```

# Plugin usage

Gradle generate `publishRustore*` task for all buildType and flavor configurations.

**Note!** The plugin will publish already existed build file. Before uploading you should build it yourself. Be careful. Don't publish old build file.

```bash
./gradlew assembleRelease publishRustoreRelease
```

or 

```bash
./gradlew bundleRelease publishRustoreRelease
```

# CLI Plugin Configuration

You can apply or override each plugin extension parameter dynamically by using CLI params.
CLI params are more priority than gradle configuration params.

```bash
./gradlew assembleRelease publishRustoreRelease \
    --credentialsPath="/sample-kotlin/rustore-credentials.json" \
    --buildFormat=apk \
    --buildFile="/sample-kotlin/app/build/outputs/apk/release/app-release.apk" \
    --seoTagIds=116,69 \
    --mobileServicesType="Unknown" \
    --minAndroidVersion="8" \
    --appType="MAIN" \
    --apiBaseUrl="https://public-api-m.rustore.ru" \
    --useBundledRussianTrustedRootCa \
    --trustedCertificatePath="/home/<USERNAME>/certs/russian_trusted_root_ca.pem" \
    --connectTimeout=30 \
    --readTimeout=300 \
    --writeTimeout=60 \
    --releaseNotes="ru_RU:/home/<USERNAME>/str/project/release_notes_ru.txt"
```

# Rustore API domain migration

Rustore [moves](https://www.rustore.ru/developer/blog/rustore-obnovlyaet-sertifikaty-i-domeny-chto-nuzhno-sdelat-razrabotchikam)
Public API from `https://public-api.rustore.ru` to `https://public-api-m.rustore.ru`.
The new domain uses certificate issued by Russian Trusted Root CA (Ministry of Digital Development),
which is absent in the default JVM truststore. Both domains work in parallel now, so the plugin uses
the old domain by default.

To switch to the new domain set `apiBaseUrl = "https://public-api-m.rustore.ru"` in `networkSetting { ... }` block
and choose one of the ways to trust the certificate:

```kotlin
networkSetting {
    apiBaseUrl = "https://public-api-m.rustore.ru"
    useBundledRussianTrustedRootCa = true
}
```


1. Install Russian Trusted Root CA from https://www.gosuslugi.ru/crt into the truststore of JVM that runs Gradle
   (e.g. `keytool -importcert -cacerts -alias russian_trusted_root_ca -file russian_trusted_root_ca.cer`)
   or pass a custom truststore with `-Djavax.net.ssl.trustStore` in `org.gradle.jvmargs`. No plugin params are required.
2. Set `networkSetting.useBundledRussianTrustedRootCa = true` to use the certificate bundled into the plugin.
3. Set `networkSetting.trustedCertificatePath` to the certificate file managed by you.

Options 2 and 3 add the certificate only to plugin requests to Rustore API in addition to JVM default CAs,
JVM global settings are not changed. If TLS connection fails, the plugin throws `RustoreTlsException` with the list of these options.

# Promotion

<p align="left">
  <img src="docs/screenshots/huawei_appgallery_icon.png" width="64">
</p>

Also consider our [Gradle Plugin for publishing to Huawei AppGallery](https://github.com/cianru/huawei-appgallery-publish-gradle-plugin)

# License

```
Copyright 2023 Aleksandr Mirko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```