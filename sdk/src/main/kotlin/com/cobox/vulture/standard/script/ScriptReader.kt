package com.cobox.vulture.standard.script

import io.vertx.core.buffer.Buffer
import java.nio.charset.Charset
import kotlin.math.min

abstract class ScriptReader {
    abstract val isEOF: Boolean
    constructor(text: String)
    constructor(buffer: Buffer, charset: Charset = Charsets.UTF_8)
    abstract fun readLine(): String?
}