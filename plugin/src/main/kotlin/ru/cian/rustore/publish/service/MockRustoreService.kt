package ru.cian.rustore.publish.service

import java.io.File

private const val REQUEST_RETRIES = 5

@SuppressWarnings("StringLiteralDuplication", "TooManyFunctions")
internal class MockRustoreService : RustoreService {

    override fun getToken(companyId: String, timestamp: String, signature: String): String {
        return "MockToken"
    }

    override fun createDraft(
        token: String,
        applicationId: String,
        whatsNew: String,
        ): Int {
        return -1
    }

    override fun uploadBuildFile(
        token: String,
        applicationId: String,
        versionId: Int,
        buildFile: File
    ): Boolean {
        return true
    }

    override fun submit(
        token: String,
        applicationId: String,
        versionId: Int,
        priorityUpdate: Int
    ): Boolean {
        return true
    }
}
