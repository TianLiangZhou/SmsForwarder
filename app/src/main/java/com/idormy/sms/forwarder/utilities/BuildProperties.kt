package com.idormy.sms.forwarder.utils

import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class BuildProperties private constructor() {
    private val properties: Properties = Properties()

    init {
        properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
    }

    fun containsKey(key: Any): Boolean {
        return properties.containsKey(key)
    }

    fun containsValue(value: Any): Boolean {
        return properties.containsValue(value)
    }

    fun getProperty(name: String?): String {
        return properties.getProperty(name)
    }

    fun getProperty(name: String?, defaultValue: String?): String {
        return properties.getProperty(name, defaultValue)
    }

    fun entrySet(): Set<Map.Entry<Any, Any>> {
        return properties.entries
    }

    val isEmpty: Boolean
        get() = properties.isEmpty

    fun keys(): Enumeration<*> {
        return properties.keys()
    }

    fun keySet(): Set<*> {
        return properties.keys
    }

    fun size(): Int {
        return properties.size
    }

    fun values(): Collection<*> {
        return properties.values
    }

    companion object {
        private var ourInstance: BuildProperties? = null

        @get:Throws(IOException::class)
        val instance: BuildProperties?
            get() {
                if (ourInstance == null) ourInstance = BuildProperties()
                return ourInstance
            }
    }
}