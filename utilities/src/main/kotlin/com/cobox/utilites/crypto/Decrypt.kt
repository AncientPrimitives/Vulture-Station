package com.cobox.utilites.crypto

import java.net.URLDecoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * URLDecode -> Base64 -> HMAC-SHA1(plain, public)
 */
object Decrypt {

    /**
     * secret - public = plain
     */
    fun decrypt(data: ByteArray, publicKey: String): ByteArray {
        TODO()
    }

    /**
     * string secret - base64 = secret
     * secret - public = plain
     */
    fun decrypt(data: String, privateKey: String): String {
        TODO()
    }

    fun base64(data: String): String =
        Base64.getDecoder().decode(data).decodeToString()

    fun url(data: String): String =
        URLDecoder.decode(data, Charsets.UTF_8)

}