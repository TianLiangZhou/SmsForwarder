package com.idormy.sms.forwarder.view

import android.util.Log
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
        launchAsync({
            if (sender.id > 0) {
                repository.update(sender)
            } else {
                repository.insert(sender)
            }
        }, {
            Log.d("SenderModel", "update insert sender $sender")
        })
    }

    fun delete(id: Long) {
        launchAsync({
            repository.delete(id)
        },{
            Log.d("SenderModel", "delete sender $id")
        })
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