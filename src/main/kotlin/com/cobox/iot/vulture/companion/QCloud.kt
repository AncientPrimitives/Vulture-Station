package com.cobox.iot.vulture.companion

import java.lang.StringBuilder
import java.nio.charset.Charset
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

object QCloud {
    const val appId: String = "1254116456"
    const val secretId: String = "AKIDLSGn2z4Ps2uUorC081ZoptgW5OO54gY9"
    const val secretKey: String = "SeJiqiIabKmxC8oEOoxoMddMmwoOedJi"
    const val signatureMethod: String = "HmacSHA256"

    fun signature(plain: String): String {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secretKey.toByteArray(Charset.forName("UTF-8")), signatureMethod)
        sha256HMAC.init(secretKey)

        val raw = sha256HMAC.doFinal(plain.toByteArray(Charset.forName("UTF-8")))
        val buffer = StringBuilder()
        for (item in raw) {
            buffer.append(Integer.toHexString(item.toInt() and 0xFF or 0x100).substring(1, 3))
        }
        return buffer.toString().toUpperCase()
    }
}