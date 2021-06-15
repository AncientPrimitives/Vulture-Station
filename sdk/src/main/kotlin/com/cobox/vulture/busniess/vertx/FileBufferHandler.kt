package com.cobox.vulture.busniess.vertx

import com.cobox.utilites.log.Log
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerFileUpload
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.impl.FileUploadImpl
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

internal class FileBufferHandler(
    private val context: RoutingContext,
    private val contentLength: Long,
    private val defaultInitialBodyBufferSize: Int = FileUploadHandler.DEFAULT_INITIAL_BODY_BUFFER_SIZE,
    private val fileUploadHandler: FileUploadHandler
) : Handler<Buffer> {

    var body: Buffer? = null
    var failed = false
    var uploadCount = AtomicInteger()
    var cleanup = AtomicBoolean(false)
    var ended = false
    var uploadSize = 0L
    var isMultipart = false
    var isUrlEncoded = false

    private fun initBodyBuffer() {
        var initialBodyBufferSize: Int
        initialBodyBufferSize = if (contentLength < 0) {
            defaultInitialBodyBufferSize
        } else if (contentLength > MAX_PREALLOCATED_BODY_BUFFER_BYTES) {
            MAX_PREALLOCATED_BODY_BUFFER_BYTES
        } else {
            contentLength.toInt()
        }
        if (fileUploadHandler.bodyLimit != -1L) {
            initialBodyBufferSize = min(initialBodyBufferSize.toLong(), fileUploadHandler.bodyLimit).toInt()
        }
        body = Buffer.buffer(initialBodyBufferSize)
    }

    private fun makeUploadDir(fileSystem: FileSystem) {
        if (!fileSystem.existsBlocking(fileUploadHandler.uploadsDir)) {
            fileSystem.mkdirsBlocking(fileUploadHandler.uploadsDir)
        }
    }

    override fun handle(buff: Buffer) {
        if (failed) {
            return
        }
        uploadSize += buff.length().toLong()
        if (fileUploadHandler.bodyLimit != -1L && uploadSize > fileUploadHandler.bodyLimit) {
            failed = true
            cancelAndCleanupFileUploads()
            context.fail(413)
        } else {
            // multipart requests will not end up in the request body
            // url encoded should also not, however jQuery by default
            // post in urlencoded even if the payload is something else
            if (!isMultipart /* && !isUrlEncoded */) {
                if (body == null) {
                    initBodyBuffer()
                }
                body!!.appendBuffer(buff)
            }
        }
    }

    fun uploadEnded() {
        val count = uploadCount.decrementAndGet()
        // only if parsing is done and count is 0 then all files have been processed
        if (ended && count == 0) {
            doEnd()
        }
    }

    fun end() {
        // this marks the end of body parsing, calling doEnd should
        // only be possible from this moment onwards
        ended = true

        // only if parsing is done and count is 0 then all files have been processed
        if (uploadCount.get() == 0) {
            doEnd()
        }
    }

    fun doEnd() {
        if (failed) {
            cancelAndCleanupFileUploads()
            return
        }
        if (fileUploadHandler.deleteUploadedFilesOnEnd) {
            context.addBodyEndHandler { x: Void? -> cancelAndCleanupFileUploads() }
        }
        val req = context.request()
        if (fileUploadHandler.mergeFormAttributes && req.isExpectMultipart) {
            req.params().addAll(req.formAttributes())
        }
        context.body = body
        // release body as it may take lots of memory
        body = null
        context.next()
    }

    /**
     * Cancel all unfinished file upload in progress and delete all uploaded files.
     */
    private fun cancelAndCleanupFileUploads() {
        if (cleanup.compareAndSet(false, true) && fileUploadHandler.handleFileUploads) {
            for (fileUpload in context.fileUploads()) {
                val fileSystem = context.vertx().fileSystem()
                if (!fileUpload.cancel()) {
                    val uploadedFileName = fileUpload.uploadedFileName()
                    fileSystem.delete(
                        uploadedFileName
                    ) { deleteResult: AsyncResult<Void?> ->
                        if (deleteResult.failed()) {
                            Log.warn(
                                FileUploadHandler.TAG,
                                "Delete of uploaded file failed: $uploadedFileName",
                                deleteResult.cause()
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val MAX_PREALLOCATED_BODY_BUFFER_BYTES = 65535
    }

    init {
        // the request clearly states that there should
        // be a body, so we respect the client and ensure
        // that the body will not be null
        if (contentLength != -1L) {
            initBodyBuffer()
        }
        val fileUploads = context.fileUploads()
        val contentType = context.request().getHeader(HttpHeaders.CONTENT_TYPE)
        if (contentType == null) {
            isMultipart = false
            isUrlEncoded = false
        } else {
            val lowerCaseContentType = contentType.toLowerCase()
            isMultipart = lowerCaseContentType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString())
            isUrlEncoded =
                lowerCaseContentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
        }
        if (isMultipart || isUrlEncoded) {
            context.request().isExpectMultipart = true
            if (fileUploadHandler.handleFileUploads) {
                makeUploadDir(context.vertx().fileSystem())
            }
            context.request().uploadHandler { upload: HttpServerFileUpload ->
                if (fileUploadHandler.bodyLimit != -1L && upload.isSizeAvailable) {
                    // we can try to abort even before the upload starts
                    val size = uploadSize + upload.size()
                    if (size > fileUploadHandler.bodyLimit) {
                        failed = true
                        cancelAndCleanupFileUploads()
                        context.fail(413)
                        return@uploadHandler
                    }
                }
                if (fileUploadHandler.handleFileUploads) {
                    // we actually upload to a file with a generated filename
                    uploadCount.incrementAndGet()
                    val uuid = UUID.randomUUID().toString()
                    val uploadedFileName: String = File(fileUploadHandler.uploadsDir, upload.filename()).getPath()
                    val fileUpload = FileUploadImpl(uploadedFileName, upload)
                    fileUploads.add(fileUpload)
                    val fut = upload.streamToFileSystem(uploadedFileName)
                    fut.onComplete { ar: AsyncResult<Void> ->
                        if (fut.succeeded()) {
                            uploadEnded()
                        } else {
                            cancelAndCleanupFileUploads()
                            context.fail(ar.cause())
                        }
                    }
                }
            }
        }
        context.request().exceptionHandler { t: Throwable ->
            cancelAndCleanupFileUploads()
            if (t is DecoderException) {
                // bad request
                context.fail(400, t.cause)
            } else {
                context.fail(t)
            }
        }
    }
}
