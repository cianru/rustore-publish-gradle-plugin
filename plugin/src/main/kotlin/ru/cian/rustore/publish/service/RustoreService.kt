package ru.cian.rustore.publish.service

import ru.cian.rustore.publish.DeveloperContactsConfig
import java.io.File

/**
 * See documentation:
 * https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/publish_RuStore_API
 */
internal interface RustoreService {

    fun getToken(
        keyId: String,
        timestamp: String,
        signature: String,
    ): String

    @Suppress("LongParameterList")
    fun createDraft(
        token: String,
        applicationId: String,
        whatsNew: String,
        publishType: String,
        seoTags: List<Int>,
        minAndroidVersion: String,
        developerContacts: DeveloperContactsConfig,
    ): Int

    @Suppress("LongParameterList")
    fun uploadApkBuildFile(
        token: String,
        applicationId: String,
        mobileServicesType: String,
        versionId: Int,
        artifactFormat: RustoreBuildFormat,
        buildFile: File,
    )

    fun submit(
        token: String,
        applicationId: String,
        versionId: Int,
        priorityUpdate: Int,
    ): Boolean
}
