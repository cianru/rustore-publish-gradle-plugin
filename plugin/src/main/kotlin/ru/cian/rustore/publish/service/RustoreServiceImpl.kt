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

@SuppressWarnings("StringLiteralDuplication", "TooManyFunctions")
internal class RustoreServiceImpl(
    private val logger: Logger,
    private val baseEntryPoint: String,
    private val requestTimeout: Long?,
) : RustoreService {

    private val gson = Gson()
    private val httpClient = HttpClientHelper(logger, requestTimeout)

    override fun getToken(
        keyId: String,
        timestamp: String,
        signature: String,
    ): String {

        val bodyRequest = AccessTokenRustoreRequest(
            keyId = keyId,
            timestamp = timestamp,
            signature = signature,
        )

        logger.i("""
            curl --location --request POST \
            $baseEntryPoint/public/auth/ \
            --header 'Content-Type: application/json' \
            --data-raw '{
                "keyId": "$keyId",
                "timestamp": "$timestamp",
                "signature": "$signature"
            }'            
        """.trimIndent())

        val response = httpClient.post<AccessTokenResponse>(
            url = "$baseEntryPoint/public/auth/",
            body = gson.toJson(bodyRequest).toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
            ),
        )
        return response.body.jwe
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
            curl --location --request POST \
            $baseEntryPoint/public/v1/application/$applicationId/version \
            --header 'Content-Type: application/json' \
            --header 'Public-Token: $token' \
            --data-raw '{
                "whatsNew": "$whatsNew"
            }'            
        """.trimIndent())

        val response = httpClient.post<AppDraftResponse>(
            url = "$baseEntryPoint/public/v1/application/$applicationId/version",
            body = gson.toJson(bodyRequest).toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )

        if (response.code == "ERROR" && response.message != null) {

            val searchString = "ID ="
            val indexOf = response.message.indexOf(searchString)

            check(indexOf > 0) {
                "Can't detect previous app versionId. " +
                    "Server response message must contain '$searchString'"
            }

            val previousAppId = response.message.substring(indexOf + searchString.length + 1)
            logger.v("previousAppId='$previousAppId'")
            val deletePreviousVersionIdResult = deletePreviousDraft(
                token = token,
                packageName = applicationId,
                previousAppId = previousAppId,
            )

            check(deletePreviousVersionIdResult) {
                "Can't remove previous app versionId on server side."
            }

            return createDraft(
                token = token,
                applicationId = applicationId,
                whatsNew = whatsNew,
            )
        }

        logger.v("response=$response")
        return response.body
    }

    override fun uploadApkBuildFile(
        token: String,
        applicationId: String,
        mobileServicesType: String,
        versionId: Int,
        artifactFormat: RustoreBuildFormat,
        buildFile: File,
    ) {

        val fileBody = buildFile.asRequestBody(HttpClientHelper.MEDIA_TYPE_AAB)
        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", buildFile.name, fileBody)

        val requestFormArgument = artifactFormat.fileExtension

        val headers = mutableMapOf(
            "accept" to "application/json",
            "Public-Token" to token,
        )

        when (artifactFormat) {
            RustoreBuildFormat.APK -> {

                multipartBuilder
                    .addFormDataPart("servicesType", mobileServicesType)
                    .addFormDataPart("isMainApk", "true")

                logger.i(
                    """
                    curl --location --request POST \
                    --header 'Content-Type: application/json' \
                    --header 'Public-Token: $token' \            
                    --form servicesType=$mobileServicesType \
                    --form isMainApk=true \
                    --form file='@${buildFile.absolutePath}' \
                    $baseEntryPoint/public/v1/application/$applicationId/version/$versionId/$requestFormArgument
                    """.trimIndent()
                )
            }
            RustoreBuildFormat.AAB -> {
                logger.i(
                    """
                    curl --location --request POST \
                    --header 'Content-Type: application/json' \
                    --header 'Public-Token: $token' \            
                    --form file='@${buildFile.absolutePath}' \
                    $baseEntryPoint/public/v1/application/$applicationId/version/$versionId/$requestFormArgument
                    """.trimIndent()
                )
            }
        }

        val requestBody = multipartBuilder
            .build()

        val response = httpClient.post<UploadAppFileResponse>(
            url = "$baseEntryPoint/public/v1/application/$applicationId/version/$versionId/$requestFormArgument",
            body = requestBody,
            headers = headers
        )

        logger.v("response=$response")

        check(response.code == "OK") {
            "Build file uploading is failed! " +
                "Reason code: ${response.code}, " +
                "message: ${response.message}"
        }
    }

    @SuppressWarnings("MaxLineLength")
    override fun submit(
        token: String,
        applicationId: String,
        versionId: Int,
        priorityUpdate: Int
    ): Boolean {
        logger.i("""
            curl --location --request POST \
            $baseEntryPoint/public/v1/application/$applicationId/version/$versionId/commit?priorityUpdate=$priorityUpdate \
            --header 'Content-Type: application/json'
        """.trimIndent())

        val response = httpClient.post<SubmitPublicationResponse>(
            url = "$baseEntryPoint/public/v1/application/$applicationId/version/$versionId/commit?priorityUpdate=$priorityUpdate",
            body = "".toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )
        return response.code == "OK"
    }

    private fun deletePreviousDraft(
        token: String,
        packageName: String,
        previousAppId: String,
    ): Boolean {

        logger.i("""
            curl --location --request DELETE $baseEntryPoint/public/v1/application/$packageName/version/$previousAppId \
            --header 'Content-Type: application/json' \
            --header 'Public-Token: $token'            
        """.trimIndent())

        val response = httpClient.delete<DeleteAppDraftResponse>(
            url = "$baseEntryPoint/public/v1/application/$packageName/version/$previousAppId",
            body = "".toRequestBody(),
            headers = mapOf(
                "Content-Type" to "application/json",
                "Public-Token" to token,
            ),
        )

        logger.v("response=$response")

        return response.code == "OK"
    }

    companion object {
        const val DOMAIN_URL = "https://public-api.rustore.ru"
    }
}
