package com.idormy.sms.forwarder.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.idormy.sms.forwarder.db.repositories.LoggerRepository
import com.idormy.sms.forwarder.utilities.MessageType

class LoggerViewModel(private val repository: LoggerRepository) : BaseViewModel() {

    fun loadLogger(type: MessageType) = repository.category(type.value).cachedIn(viewModelScope)
}

class LoggerViewModelFactory(private val repository: LoggerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoggerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoggerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}