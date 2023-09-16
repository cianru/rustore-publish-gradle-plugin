package ru.cian.rustore.publish.utils

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

class SignatureUtils {
    companion object {

        /**
         * https://github.com/stfbee/fastlane-plugin-rustore/blob/d58746d9f351b78ca236db41ea22f3956381b8a0/lib/fastlane/plugin/rustore/helper/rustore_helper.rb#L32
         * ------------------
         * def self.rsa_sign(timestamp, company_id, private_key)
         *     key = OpenSSL::PKey::RSA.new("-----BEGIN RSA PRIVATE KEY-----\n#{private_key}\n-----END RSA PRIVATE KEY-----")
         *     signature = key.sign(OpenSSL::Digest.new('SHA512'), company_id + timestamp)
         *     Base64.encode64(signature)
         * end
         */
        fun signData(data: String, privateKeyBase64: String): String {
            try {
                val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)

                // Create private key object
                val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

                // Create object for data signature
                val signature = Signature.getInstance("SHA512withRSA")
                signature.initSign(privateKey)

                // Update signed data
                signature.update(data.toByteArray(Charsets.UTF_8))

                // Get signature and encode it
                val signatureBytes = signature.sign()
                return Base64.getEncoder().encodeToString(signatureBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}