/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package com.idormy.sms.forwarder.preference

import androidx.preference.PreferenceDataStore
import com.idormy.sms.forwarder.db.dao.KeyValuePairDao
import com.idormy.sms.forwarder.db.model.KeyValuePair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class RoomPreferenceDataStore(private val kvPairDao: KeyValuePairDao) : PreferenceDataStore() {
    fun getBoolean(key: String): Boolean? {
        return runBlocking {
            kvPairDao.get(key)?.boolean
        }
    }
    fun getFloat(key: String): Float? {
        return runBlocking {
            kvPairDao.get(key)?.float
        }
    }
    fun getInt(key: String): Int? {
        return runBlocking {
            kvPairDao.get(key)?.long?.toInt()
        }
    }
    fun getLong(key: String): Long? {
        return runBlocking {
            kvPairDao.get(key)?.long
        }
    }
    fun getString(key: String): String? {
        return runBlocking {
            kvPairDao.get(key)?.string
        }
    }
    fun getStringSet(key: String): Set<String>? {
        return runBlocking {
            kvPairDao.get(key)?.stringSet
        }
    }

    override fun getBoolean(key: String, defValue: Boolean) = getBoolean(key) ?: defValue
    override fun getFloat(key: String, defValue: Float) = getFloat(key) ?: defValue
    override fun getInt(key: String, defValue: Int) = getInt(key) ?: defValue
    override fun getLong(key: String, defValue: Long) = getLong(key) ?: defValue
    override fun getString(key: String, defValue: String?) = getString(key) ?: defValue
    override fun getStringSet(key: String, defValue: MutableSet<String>?) = getStringSet(key) ?: defValue

    fun putBoolean(key: String, value: Boolean?) = if (value == null) remove(key) else putBoolean(key, value)
    fun putFloat(key: String, value: Float?) = if (value == null) remove(key) else putFloat(key, value)
    fun putInt(key: String, value: Int?) = if (value == null) remove(key) else putLong(key, value.toLong())
    fun putLong(key: String, value: Long?) = if (value == null) remove(key) else putLong(key, value)
    override fun putBoolean(key: String, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(value))
        }
        fireChangeListener(key)
    }
    override fun putFloat(key: String, value: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(value))
        }
        fireChangeListener(key)
    }
    override fun putInt(key: String, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(value.toLong()))
        }
        fireChangeListener(key)
    }
    override fun putLong(key: String, value: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(value))
        }
        fireChangeListener(key)
    }
    override fun putString(key: String, value: String?) = if (value == null) remove(key) else {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(value))
        }
        fireChangeListener(key)
    }
    override fun putStringSet(key: String, values: MutableSet<String>?) = if (values == null) remove(key) else {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.put(KeyValuePair(key).put(values))
        }
        fireChangeListener(key)
    }

    fun remove(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            kvPairDao.delete(key)
        }
        fireChangeListener(key)
    }

    private val listeners = HashSet<OnPreferenceDataStoreChangeListener>()
    private fun fireChangeListener(key: String) = listeners.forEach { it.onPreferenceDataStoreChanged(this, key) }
    fun registerChangeListener(listener: OnPreferenceDataStoreChangeListener) = listeners.add(listener)
    fun unregisterChangeListener(listener: OnPreferenceDataStoreChangeListener) = listeners.remove(listener)
}