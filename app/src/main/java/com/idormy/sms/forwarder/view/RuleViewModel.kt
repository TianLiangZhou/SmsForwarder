package com.idormy.sms.forwarder.view

import androidx.lifecycle.*
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.repositories.RuleRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RuleViewModel(private val repository: RuleRepository) : BaseViewModel() {

//    val rules = repository.all.asLiveData()

    fun save(rule: Rule) {
        launchAsync({
            if (rule.id > 0) {
                repository.update(rule)
            } else {
                repository.insert(rule)
            }
        }, {})
    }

    fun delete(id: Long) {
        launchAsync({
            repository.delete(id)
        },{})
    }

    suspend fun get(id: Long) = repository.get(id)

    private val _rules: MutableLiveData<List<Rule>> = MutableLiveData(ArrayList())

    val rules: LiveData<List<Rule>> = _rules

    fun loadRules() = launchAsync({
        repository.all
    }, {rules ->
        viewModelScope.launch {
            rules.collectLatest {
                _rules.postValue(it)
            }
        }
    })

}

class RuleViewModelFactory(private val repository: RuleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RuleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}