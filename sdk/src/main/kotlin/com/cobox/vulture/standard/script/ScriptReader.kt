package com.cobox.vulture.standard.script

import io.vertx.core.buffer.Buffer
import java.io.Closeable
import java.nio.charset.Charset
import kotlin.math.min

abstract class ScriptReader: Closeable {
    abstract val isEOF: Boolean
    abstract val isClosed: Boolean
    constructor(text: String)
    constructor(path: String, charset: Charset = Charsets.UTF_8)
    constructor(buffer: Buffer, charset: Charset = Charsets.UTF_8)
    abstract fun readLine(): String?
}