package ru.cian.rustore.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class AccessTokenResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: AccessTokenRustoreBodyResponse
) {
    internal data class AccessTokenRustoreBodyResponse(
        @SerializedName("jwe")
        val jwe: String,
    )
}