package com.cobox.utilites.crypto

import java.net.URLEncoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HMAC-SHA1(plain, private) -> Base64 -> URLEncode
 */
object Encrypt {

    private const val ALGO_HMAC = "HmacSHA256"

    /**
     * plain + private = secret
     */
    fun encrypt(plain: ByteArray, privateKey: String): ByteArray {
        val key = SecretKeySpec(
            privateKey.encodeToByteArray(), ALGO_HMAC
        )

        val secret = Mac.getInstance("HmacSHA256").let { mac ->
            mac.init(key)
            mac.doFinal(plain)
        }

        return Base64.getEncoder().encode(secret)
    }

    /**
     * plain + private = secret
     * secret + base64 = string secret
     */
    fun encrypt(plain: String, privateKey: String): String {
        val key = SecretKeySpec(
            privateKey.encodeToByteArray(), ALGO_HMAC
        )

        val secret = Mac.getInstance("HmacSHA256").let { mac ->
            mac.init(key)
            mac.doFinal(plain.encodeToByteArray())
        }

        return URLEncoder.encode(
            Base64.getEncoder().encodeToString(secret),
            Charsets.UTF_8
        )
    }

    fun base64(data: String): String =
        Base64.getEncoder().encodeToString(data.encodeToByteArray())

    fun url(data: String): String =
        URLEncoder.encode(data, Charsets.UTF_8)

}