package com.idormy.sms.forwarder.view

import androidx.lifecycle.*
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.db.repositories.SenderRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SenderViewModel(private val repository: SenderRepository) : BaseViewModel() {

    private val _senders = MutableLiveData<List<Sender>>()

    val senders: LiveData<List<Sender>> = _senders

    fun loadAllSender() = launchAsync({
        repository.all
    }, {
        viewModelScope.launch {
            it.collectLatest {
                _senders.postValue(it)
            }
        }
    })

    fun save(sender: Sender) {
        viewModelScope.launch {
            if (sender.id > 0) {
                repository.update(sender)
            } else {
                repository.insert(sender)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    suspend fun get(id: Long) = repository.get(id)
}

class SenderViewModelFactory(private val repository: SenderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SenderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SenderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}