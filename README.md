# RuStore Publishing

![Version](https://img.shields.io/badge/GradlePortal-0.3.0-green.svg)
![Version](https://img.shields.io/badge/Gradle-8.*-pink.svg)
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin allows you to publish the android release *.apk build file to the RuStore.

For publication the plugin used [Rustore API](https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/publish_RuStore_API)

# Table of contents
<!-- TOC -->
- [Features](#features)
- [Support versions](#support-versions)
- [Adding the plugin to your project](#adding-the-plugin-to-your-project)
    - [Using the Gradle plugin DSL](#using-the-gradle-plugin-dsl)
    - [Using the `apply` method](#using-the-apply-method)
    - [Configuring Plugin](#configuring-plugin)
    - [Plugin params](#plugin-params)
- [Plugin usage](#plugin-usage)

<!-- /TOC -->

# Features

The following features are available:

* Publish APK build file in RuStore
* Submit the build on all users after getting store approve
* Update Release Notes for publishing build (Release Notes)
* Separated settings for different configurations build types and flavors
* Support of Gradle Portal and Gradle DSL
* Support of Gradle 7.+
* Support of Configuration Cache

The following features are missing:

* Publish the build on a part of users (Release Phases)
* Change App Store Information: description, app icon, screenshots and etc.
* Rollout Holding

!!! MORE INFORMATION COMING SOON !!!

# Compatibility
The Android Gradle Plugin often changes the Variant API,
so a different version of AGP corresponds to a specific version of the current plugin

| AGP | Plugin |
|-----|--------|
| 7.+ | 0.2.2  |
| 8.+ | 0.3.0  |

# Adding the plugin to your project

in application module `./app/build.gradle`

## Using the Gradle plugin DSL

```
plugins {
    id("com.android.application")
    id("ru.cian.rustore-publish-gradle-plugin")
}
```

## Using the `apply` method

```
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

## Configuring Plugin

<details open>
<summary>Groovy</summary>

```groovy
rustorePublish {
    instances {
        release {
            credentialsPath = "$rootDir/rustore-credentials-release.json"
            releaseNotes = [
                new ru.cian.rustore.publish.ReleaseNote(
                    "ru-RU",
                    "$projectDir/release-notes-ru.txt"
                ),
            ]
            ...
        }
        debug {
            ...
        }
    }
}
```
</details>

<details>
<summary>Kotlin</summary>

```kotlin
rustorePublish {
    instances {
        create("release") {
            credentialsPath = "$rootDir/rustore-credentials-release.json"
            releaseNotes = listOf(
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$projectDir/release-notes-ru.txt"
                ),
            )
            ...
        }
        create("debug") {
            ...
        }
    }
}
```
</details>

Plugin supports different settings for different buildType and flavors.
For example, for `demo` and `full` flavors and `release` buildType just change instances like that:
```kotlin
rustorePublish {
    instances {
        demoRelease {
            credentialsPath = "$rootDir/rustore-credentials-demo-release.json"
            ...
        }
        fullRelease {
            credentialsPath = "$rootDir/rustore-credentials-full-release.json"
            ...
        }
    }
}
```

File `rustore-credentials.json` contains next json structure:
```json
{
  "company_id": "<COMPANY_ID>",
  "client_secret": "<CLIENT_SECRET>"
}
```
How to get credentials see [[RU] Rustore API Getting Started](https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/authorization_rustore_api_1).

## Plugin params
Where Priority(P), Required(R), Optional(O)

| param              | P | type              | default value | cli                                            | description                                                                                                                                                                                   |
|--------------------|---|-------------------|---------------|------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `credentialsPath`  | O | string            | null          | `--credentialsPath`                            | Path to json file with AppGallery credentials params (`company_id` and `client_secret`)                                                                                                        |
| `buildFile`        | O | string            | null          | `--buildFile`                                  | Path to build file. "null" means use standard path for "apk" and "aab" files.                                                                                                                 |
| `releaseNotes`     | O | List<ReleaseNote> | null          | `--releaseNotes` (see ReleaseNote param desc.) | Release Notes. For mote info see documentation below.                                                                                                                                         |

other params

| ReleaseNote(Object)  | P | type    | default value | cli                            | description                                                                                                           |
|----------------------|---|---------|---------------|--------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `lang`               | R | string  | null          | (See `--releaseNotes` desc.)   | Support only `ru-RU` lang.                                                                                            |
| `filePath`           | R | string  | null          | (See `--releaseNotes` desc.)   | Absolutely path to file with Release Notes for current `lang`. Release notes text must be less or equals to 500 sign. |

For CLI `--releaseNotes` use string type with format: `<lang>:<releaseNotes_FilePath>`.

<details>
<summary>For example</summary>

```bash
--releaseNotes="ru_RU:/home/<USERNAME>/str/project/release_notes_ru.txt"
```

</details>

# Plugin usage

Gradle generate `publishRustore*` task for all buildType and flavor configurations
```groovy
android {
    buildTypes {
        release {
            ...
        }
        debug {
            ...
        }
    }
}
```

**Note!** Before uploading build file you should build it. Be careful. Don't publish old build file.

```bash
./gradlew assembleRelease publishRustoreRelease
```

You can apply or override each plugin extension parameter dynamically by using CLI params. For example:

```bash
./gradlew assembleRelease publishRustoreRelease \
    --credentialsPath="/sample1/rustore-credentials.json"
```

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