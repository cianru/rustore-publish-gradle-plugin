package ru.cian.rustore.publish.models

import com.google.gson.annotations.SerializedName

internal data class Credential(
    @SerializedName("key_id")
    val keyId: String?,
    @SerializedName("client_secret")
    val clientSecret: String?
)
