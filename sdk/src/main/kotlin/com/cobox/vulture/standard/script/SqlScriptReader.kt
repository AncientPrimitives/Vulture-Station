package com.cobox.vulture.standard.script

import io.vertx.core.buffer.Buffer
import java.nio.charset.Charset
import kotlin.math.min

class SqlScriptReader: ScriptReader {

    private lateinit var buffer: Buffer
    private var charset = Charsets.UTF_8

    private val bufferSize = 512
    private var position = 0
    private var endLimit = 0

    override val isEOF: Boolean
        get() = (position >= endLimit)

    constructor(content: String): this(Buffer.buffer(content))
    constructor(content: Buffer, charset: Charset = Charsets.UTF_8): super(content, charset) {
        this.buffer = content
        this.charset = charset
        this.endLimit = buffer.length()
    }

    override fun readLine(): String? {
        while(position < endLimit) {
            val end = min(position + bufferSize, endLimit)
            val content = buffer.getString(position, end, charset.name())

            // find ';'
            val sig = content.indexOfFirst { it == ';' }
            if (sig >= 0) {
                val cropContent = content.substring(0, sig + 1)
                position += cropContent.encodeToByteArray().size // Note：一个汉字两个字节
                return cropContent.trim()
            } else {
                position = end
            }
        }
        return null
    }

}