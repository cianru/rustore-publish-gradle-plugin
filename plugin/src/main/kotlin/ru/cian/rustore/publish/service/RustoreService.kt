package ru.cian.rustore.publish.service

import java.io.File

/**
 * See documentation:
 * https://help.rustore.ru/rustore/for_developers/work_with_RuStore_API/publish_RuStore_API
 */
internal interface RustoreService {

    fun getToken(
        companyId: String,
        timestamp: String,
        signature: String,
    ): String

    fun createDraft(
        token: String,
        applicationId: String,
        whatsNew: String,
    ): Int

    fun uploadBuildFile(
        token: String,
        applicationId: String,
        versionId: Int,
        buildFile: File,
    ): Boolean

    fun submit(
        token: String,
        applicationId: String,
        versionId: Int,
        priorityUpdate: Int,
    ): Boolean

}
