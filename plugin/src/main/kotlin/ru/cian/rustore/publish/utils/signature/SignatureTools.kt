package ru.cian.rustore.publish.utils.signature

interface SignatureTools {

    fun signData(data: String, privateKeyBase64: String): String
}