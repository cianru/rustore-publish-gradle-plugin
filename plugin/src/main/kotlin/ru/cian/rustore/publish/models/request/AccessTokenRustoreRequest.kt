package ru.cian.rustore.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class AccessTokenRustoreRequest(
    @SerializedName("companyId")
    val companyId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("signature")
    val signature: String,
)
