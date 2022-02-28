package com.idormy.sms.forwarder.provider

import com.idormy.sms.forwarder.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

object Http {
    val client = HttpClient(Android) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 3000
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer( kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = if (BuildConfig.DEBUG) {
                LogLevel.ALL
            } else {
                LogLevel.INFO
            }
        }
    }
}