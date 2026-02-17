package ru.cian.rustore.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class DeveloperContactsRequestModel(
    @SerializedName("email")
    val email: String,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("vkCommunity")
    val vkCommunity: String? = null,
)
