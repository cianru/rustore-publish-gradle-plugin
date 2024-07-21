package ru.cian.rustore.publish.service

import ru.cian.rustore.publish.BuildFormat
import java.io.File

@SuppressWarnings("StringLiteralDuplication", "TooManyFunctions")
internal class MockRustoreService : RustoreService {

    override fun getToken(keyId: String, timestamp: String, signature: String): String {
        return "MockToken"
    }

    override fun createDraft(
        token: String,
        applicationId: String,
        whatsNew: String,
    ): Int {
        return -1
    }

    override fun uploadApkBuildFile(
        token: String,
        applicationId: String,
        mobileServicesType: String,
        versionId: Int,
        artifactFormat: BuildFormat,
        buildFile: File,
    ) {
        throw IllegalStateException("Test build file uploading is failed! It works as well")
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
