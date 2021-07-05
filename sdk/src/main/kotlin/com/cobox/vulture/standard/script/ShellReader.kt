package com.cobox.vulture.standard.script

import io.vertx.core.buffer.Buffer
import java.io.*
import java.nio.charset.Charset

class ShellReader: ScriptReader {
    private val input: InputStream
    private val reader: BufferedReader
    private var charset = Charsets.UTF_8

    override val isEOF: Boolean
        get() = false

    private var _isClosed = false

    override val isClosed: Boolean
        get() = _isClosed

    constructor(text: String) : super(text) {
        this.input = ByteArrayInputStream(text.toByteArray())
        this.reader = input.reader(charset).buffered()
    }

    constructor(buffer: Buffer, charset: Charset = Charsets.UTF_8) : super(buffer, charset) {
        this.input = ByteArrayInputStream(buffer.bytes)
        this.charset = charset
        this.reader = input.reader(charset).buffered()
    }

    constructor(path: String, charset: Charset = Charsets.UTF_8): super(path, charset) {
        this.input = FileInputStream(path)
        this.charset = charset
        this.reader = input.reader(charset).buffered()
    }

    override fun readLine(): String? {
        if (isClosed) return null
        return reader.readLine()
    }

    override fun close() {
        if (isClosed) return

        input.close()
        _isClosed = true
    }

}