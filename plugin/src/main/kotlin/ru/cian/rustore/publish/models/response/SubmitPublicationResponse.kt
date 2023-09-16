package ru.cian.rustore.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class SubmitPublicationResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String?,
)
