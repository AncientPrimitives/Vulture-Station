package com.cobox.iot.vulture.webgate

import io.vertx.core.json.JsonObject

class ResponseBuilder {

    private val responseJson = JsonObject()

    fun okay(
        code: Int = 200,
        result: String = "OK",
        message: String? = null,
        payload: JsonObject? = null
    ): ResponseBuilder = this.apply {
        responseJson.apply {
            put(
                "state",
                JsonObject().apply {
                    put("code", code)
                    put("result", result)
                }
            )
            message?.let { put("message", message) }
            payload?.let { put("payload", payload) }
        }
    }

    fun fail(
        code: Int = 404,
        result: String = "Failed",
        message: String? = null,
        payload: JsonObject? = null
    ): ResponseBuilder = this.apply {
        responseJson.apply {
            put(
                "state",
                JsonObject().apply {
                    put("code", code)
                    put("result", result)
                }
            )
            message?.let { put("message", message) }
            payload?.let { put("payload", payload) }
        }
    }

    fun build(): String
            = responseJson.encodePrettily()

}