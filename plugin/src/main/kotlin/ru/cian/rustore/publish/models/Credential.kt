package ru.cian.rustore.publish.models

import com.google.gson.annotations.SerializedName

internal data class Credential(
    @SerializedName("company_id")
    val companyId: String?,
    @SerializedName("client_secret")
    val clientSecret: String?
)
