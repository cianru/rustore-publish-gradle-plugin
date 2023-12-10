package ru.cian.rustore.publish.utils.signature

class MockSignatureTools : SignatureTools {

    override fun signData(data: String, privateKeyBase64: String): String {
        return ""
    }
}