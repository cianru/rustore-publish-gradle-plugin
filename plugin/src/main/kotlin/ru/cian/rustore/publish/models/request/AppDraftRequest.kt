package ru.cian.rustore.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class AppDraftRequest(
    @SerializedName("whatsNew")
    val whatsNew: String,
)
