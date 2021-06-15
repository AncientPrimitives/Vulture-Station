package com.cobox.vulture.standard.xutil.vertx

import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse

object AsyncResultUtil {

    fun AsyncResult<HttpResponse<Buffer>>.onSuccess(
        handler: ((HttpResponse<Buffer>) -> Unit)
    ): AsyncResult<HttpResponse<Buffer>> {
        if (this.succeeded()) {
            handler(this.result())
        }
        return this
    }

    fun AsyncResult<HttpResponse<Buffer>>.onFailure(
        handler: ((Throwable) -> Unit)
    ): AsyncResult<HttpResponse<Buffer>> {
        if (this.failed()) {
            handler(this.cause())
        }
        return this
    }

}