package com.idormy.sms.forwarder.provider

import com.idormy.sms.forwarder.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


object Http {
    val client = HttpClient(Android) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                isLenient = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = if (BuildConfig.DEBUG) {
                LogLevel.ALL
            } else {
                LogLevel.INFO
            }
        }
    }
}