package com.cobox.vulture.busniess.vertx

import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.impl.RoutingContextInternal

class FileUploadHandler : BodyHandler {
    companion object {
        const val TAG = "FileUploadHandler"
        const val DEFAULT_INITIAL_BODY_BUFFER_SIZE = 1024 //bytess
    }

    internal var handleFileUploads = false
    internal var bodyLimit = BodyHandler.DEFAULT_BODY_LIMIT
    internal var uploadsDir: String? = null
    internal var mergeFormAttributes = BodyHandler.DEFAULT_MERGE_FORM_ATTRIBUTES
    internal var deleteUploadedFilesOnEnd = BodyHandler.DEFAULT_DELETE_UPLOADED_FILES_ON_END
    private var isPreallocateBodyBuffer = BodyHandler.DEFAULT_PREALLOCATE_BODY_BUFFER


    constructor(): this(true, BodyHandler.DEFAULT_UPLOADS_DIRECTORY)

    constructor(handleFileUploads: Boolean): this(handleFileUploads, BodyHandler.DEFAULT_UPLOADS_DIRECTORY)

    constructor(uploadDirectory: String): this(true, uploadDirectory)

    constructor(handleFileUploads: Boolean, uploadDirectory: String) {
        this.handleFileUploads = handleFileUploads
        setUploadsDirectory(uploadDirectory)
    }

    override fun handle(context: RoutingContext) {
        val request = context.request()
        if (request.headers().contains(HttpHeaders.UPGRADE, HttpHeaders.WEBSOCKET, true)) {
            context.next()
            return
        }
        // we need to keep state since we can be called again on reroute
        if (!(context as RoutingContextInternal).seenHandler(RoutingContextInternal.BODY_HANDLER)) {
            val contentLength = if (isPreallocateBodyBuffer) parseContentLengthHeader(request) else -1
            val handler = FileBufferHandler(context, contentLength, DEFAULT_INITIAL_BODY_BUFFER_SIZE, this)
            request.handler(handler)
            request.endHandler { v: Void? -> handler.end() }
            context.visitHandler(RoutingContextInternal.BODY_HANDLER)
        } else {
            // on reroute we need to re-merge the form params if that was desired
            if (mergeFormAttributes && request.isExpectMultipart) {
                request.params().addAll(request.formAttributes())
            }
            context.next()
        }
    }

    override fun setHandleFileUploads(handleFileUploads: Boolean): BodyHandler? {
        this.handleFileUploads = handleFileUploads
        return this
    }

    override fun setBodyLimit(bodyLimit: Long): BodyHandler? {
        this.bodyLimit = bodyLimit
        return this
    }

    override fun setUploadsDirectory(uploadsDirectory: String?): BodyHandler? {
        uploadsDir = uploadsDirectory
        return this
    }

    override fun setMergeFormAttributes(mergeFormAttributes: Boolean): BodyHandler? {
        this.mergeFormAttributes = mergeFormAttributes
        return this
    }

    override fun setDeleteUploadedFilesOnEnd(deleteUploadedFilesOnEnd: Boolean): BodyHandler? {
        this.deleteUploadedFilesOnEnd = deleteUploadedFilesOnEnd
        return this
    }

    override fun setPreallocateBodyBuffer(isPreallocateBodyBuffer: Boolean): BodyHandler? {
        this.isPreallocateBodyBuffer = isPreallocateBodyBuffer
        return this
    }

    private fun parseContentLengthHeader(request: HttpServerRequest): Long {
        val contentLength = request.getHeader(HttpHeaders.CONTENT_LENGTH)
        return if (contentLength == null || contentLength.isEmpty()) {
            -1
        } else try {
            val parsedContentLength = contentLength.toLong()
            if (parsedContentLength < 0) -1 else parsedContentLength
        } catch (ex: NumberFormatException) {
            -1
        }
    }
}