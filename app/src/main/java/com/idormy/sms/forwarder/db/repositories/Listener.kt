package com.idormy.sms.forwarder.db.repositories

interface Listener {
    fun onDelete(id: Long)
}