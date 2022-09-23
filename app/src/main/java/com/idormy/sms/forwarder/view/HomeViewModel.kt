package com.idormy.sms.forwarder.view

import androidx.lifecycle.*
import com.idormy.sms.forwarder.data.Stats
import com.idormy.sms.forwarder.db.repositories.LoggerRepository
import com.idormy.sms.forwarder.db.repositories.RuleRepository
import com.idormy.sms.forwarder.db.repositories.SenderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val logger: LoggerRepository, private val rule: RuleRepository, private val sender: SenderRepository) : BaseViewModel() {

    private val _stats = MutableLiveData<Stats>()

    val openStats: LiveData<Stats> = _stats

    fun clean() {

    }

    fun stats() {
        viewModelScope.launch(Dispatchers.IO) {
            val obj = withContext(coroutineContext) {
                val ok = logger.okCount()
                val failed= logger.failedCount()
                val rule = rule.count()
                val sender = sender.count()
                return@withContext Stats("$ok", "$failed", "$rule", "$sender")
            }
            _stats.postValue(obj)
        }
    }
}

class HomeViewModelFactory(val logger: LoggerRepository, val rule: RuleRepository, val sender: SenderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(logger, rule, sender) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}