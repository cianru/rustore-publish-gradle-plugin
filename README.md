
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

![Version](https://img.shields.io/badge/GradlePortal-0.4.0-green.svg)
![Version](https://img.shields.io/badge/Gradle-8.*-pink.svg)
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin use [Rustore API](https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/publish_RuStore_API) to publish Android *.apk build file to the [RuStore](https://rustore.ru). 

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

- :white_check_mark: Publish APK build file in RuStore
- :white_check_mark: Submit the build on all users after getting store approve
- :white_check_mark: Update Release Notes for publishing build (Release Notes)
- :white_check_mark: Separated settings for different configurations build types and flavors
- :white_check_mark: Support of Gradle Portal and Gradle DSL
- :white_check_mark: Support of Gradle 8.+
- :white_check_mark: Support of Configuration Cache

The following features are missing:

- :children_crossing: Change App Store Information: description, app icon, screenshots and etc.
- :children_crossing: Publish the build on a part of users (Release Phases)

The following features are not available on Rustore API side yet:

- :no_entry: Support of AppBundle
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

<details>
<summary>Snapshot builds are also available</summary>
___

You'll need to add the Sonatype snapshots repository.
Look for the actual version of the snapshot in the name of the opened `snapshot-<VERSION>` repository branch.

For general integration add next snippet in `./settings.gradle`

```kotlin
pluginManagement {

  resolutionStrategy {
    eachPlugin {
      if(requested.id.namespace == "ru.cian") {
        useModule("ru.cian.rustore-plugin:rustore-publish-gradle-plugin:<SNAPSHOT-VERSION>")
      }
    }
  }

  plugins {
    id("ru.cian.rustore-publish-gradle-plugin") version rustorePublish apply false
  }

  repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  }
}
```
___

</details>

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

<details>
<summary>Snapshot builds are also available</summary>
___

You'll need to add the Sonatype snapshots repository.
Look for the actual version of the snapshot in the name of the opened `snapshot-<VERSION>` repository branch.

```groovy
buildscript {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }

    dependencies {
        classpath "ru.cian.rustore-plugin:rustore-publish-gradle-plugin:<VERSION>-SNAPSHOT"
    }
}

apply plugin: 'com.android.application'
apply plugin: "ru.cian.rustore-publish-gradle-plugin"
```
___

</details>

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
       * Path to build file if you would like to change default path. "null" means use standard path for "apk" and "aab" files.
       * Type: String (Optional)
       * Default value: `null`
       * CLI: `--buildFile`
       */
      buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"
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
            buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"
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
       * Path to build file if you would like to change default path. "null" means use standard path for "apk" and "aab" files.
       * Type: String (Optional)
       * Default value: `null`
       * CLI: `--buildFile`
       */
      buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"

      /**
       * Type of mobile services used in application. Available values: [\"Unknown\", \"HMS\"].
       * For more details see param `servicesType` in documentation " +
       * https://www.rustore.ru/help/work-with-rustore-api/api-upload-publication-app/apk-file-upload/file-upload-apk/
       * Type: ru.cian.rustore.publish.MobileServicesType (Optional)
       * Default value: UNKNOWN
       * CLI: `--mobileServicesType`
       */
      mobileServicesType = ru.cian.rustore.publish.MobileServicesType.UNKNOWN

      /**
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
            buildFile = "$rootDir/app/build/outputs/apk/release/app-release.apk"
            mobileServicesType = "Unknown"
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
    --buildFile="/sample-kotlin/app/build/outputs/apk/release/app-release.apk" \
    --mobileServicesType="Unknown" \
    --releaseNotes="ru_RU:/home/<USERNAME>/str/project/release_notes_ru.txt"
```

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