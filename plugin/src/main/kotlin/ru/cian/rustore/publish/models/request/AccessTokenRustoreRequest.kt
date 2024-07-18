package ru.cian.rustore.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class AccessTokenRustoreRequest(
    @SerializedName("keyId")
    val keyId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("signature")
    val signature: String,
)
