package com.cobox.iot.vulture.lang

import io.vertx.core.json.JsonObject

object EmptyJsonObject : JsonObject()

fun emptyJsonObject() = EmptyJsonObject