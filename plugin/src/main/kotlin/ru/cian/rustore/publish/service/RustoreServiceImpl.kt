package ru.cian.rustore.publish.service

import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.cian.rustore.publish.models.request.AccessTokenRustoreRequest
import ru.cian.rustore.publish.models.request.AppDraftRequest
import ru.cian.rustore.publish.models.response.AccessTokenResponse
import ru.cian.rustore.publish.models.response.AppDraftResponse
import ru.cian.rustore.publish.models.response.DeleteAppDraftResponse
import ru.cian.rustore.publish.models.response.SubmitPublicationResponse
import ru.cian.rustore.publish.models.response.UploadAppFileResponse
import ru.cian.rustore.publish.utils.Logger
import java.io.File

private const val DOMAIN_URL = "https://public-api.rustore.ru"

@SuppressWarnings("StringLiteralDuplication", "TooManyFunctions")
internal class RustoreServiceImpl constructor(
    private val logger: Logger
) : RustoreService {

    private val gson = Gson()
    private val httpClient = HttpClientHelper(logger)

    override fun getToken(
        companyId: String,
        timestamp: String,
        signature: String,
    ): String {

        val bodyRequest = AccessTokenRustoreRequest(
            companyId = companyId,
            timestamp = timestamp,
            signature = signature,
        )

        logger.i("""
        curl --location --request POST $DOMAIN_URL/public/auth/ \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "companyId": "$companyId",
            "timestamp": "$timestamp",
            "signature": "$signature"
        }'            
        """.trimIndent())

        val response = httpClient.post<AccessTokenResponse>(
            url = "$DOMAIN_URL/public/auth/",
            body = gson.toJson(bodyRequest).toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
            ),
        )
        return response.body.jwe
            ?: throw IllegalStateException("Can't get `accessToken`. Reason: '${response.code}, message=${response.message}'")
    }

    override fun createDraft(
        token: String,
        applicationId: String,
        whatsNew: String,
    ): Int {
        val bodyRequest = AppDraftRequest(
            whatsNew = whatsNew,
        )

        logger.i("""
        curl --location --request POST $DOMAIN_URL/public/v1/application/$applicationId/version \
        --header 'Content-Type: application/json' \
        --header 'Public-Token: $token' \
        --data-raw '{
            "whatsNew": "$whatsNew"
        }'            
        """.trimIndent())

        val response = httpClient.post<AppDraftResponse>(
            url = "$DOMAIN_URL/public/v1/application/$applicationId/version",
            body = gson.toJson(bodyRequest).toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )

        if (response.code == "ERROR") {
            if (response.message != null) {
                val searchString = "ID ="
                val indexOf = response.message?.indexOf(searchString)

                if (indexOf > 0) {
                    val previousAppId = response.message.substring(indexOf + searchString.length + 1)
                    logger.v("previousAppId='$previousAppId'")
                    val deletePreviousVersionIdResult = deletePreviousDraft(
                        token = token,
                        packageName = applicationId,
                        previousAppId = previousAppId,
                    )

                    if (!deletePreviousVersionIdResult) {
                        throw IllegalStateException("Can't remove previous app versionId")
                    }

                    return createDraft(
                        token = token,
                        applicationId = applicationId,
                        whatsNew = whatsNew,
                    )
                } else {
                    throw IllegalStateException("Can't detect previous app versionId")
                }
            }
        }

        logger.v("response=$response")
        return response.body
            ?: throw IllegalStateException("Can't get `accessToken`. Reason: '${response.code}, message=${response.message}'")
    }

    override fun uploadBuildFile(
        token: String,
        applicationId: String,
        versionId: Int,
        buildFile: File
    ): Boolean {

        val fileBody = buildFile.asRequestBody(HttpClientHelper.MEDIA_TYPE_AAB)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", buildFile.name, fileBody)
            .addFormDataPart("servicesType", "Unknown")
            .addFormDataPart("isMainApk", "true")
            .build()

        val headers = mutableMapOf(
            "accept" to "application/json",
            "Public-Token" to token,
        )

        val response = httpClient.post<UploadAppFileResponse>(
            url = "$DOMAIN_URL/public/v1/application/$applicationId/version/$versionId/apk",
            body = requestBody,
            headers = headers
        )

        logger.v("response=$response")

        if (response.code != "OK") {
            throw IllegalStateException("Build file uploading is failed!")
        }

        return true
    }

    override fun submit(
        token: String,
        applicationId: String,
        versionId: Int,
        priorityUpdate: Int
    ): Boolean {
        logger.i("""
        curl --location --request POST $DOMAIN_URL/public/v1/application/$applicationId/version/$versionId/commit?priorityUpdate=$priorityUpdate \
        --header 'Content-Type: application/json'
        """.trimIndent())

        val response = httpClient.post<SubmitPublicationResponse>(
            url = "$DOMAIN_URL/public/v1/application/$applicationId/version/$versionId/commit?priorityUpdate=$priorityUpdate",
            body = "".toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )
        return response.code == "OK"
            ?: throw IllegalStateException("Can't get `accessToken`. Reason: '${response.code}, message=${response.message}'")
    }

    private fun deletePreviousDraft(
        token: String,
        packageName: String,
        previousAppId: String,
    ): Boolean {

        logger.i("""
        curl --location --request DELETE $DOMAIN_URL/public/v1/application/$packageName/version/$previousAppId \
        --header 'Content-Type: application/json' \
        --header 'Public-Token: $token'            
        """.trimIndent())

        val response = httpClient.delete<DeleteAppDraftResponse>(
            url = "$DOMAIN_URL/public/v1/application/$packageName/version/$previousAppId",
            body = "".toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )

        logger.v("response=$response")

        return response.code == "OK"
    }
}
