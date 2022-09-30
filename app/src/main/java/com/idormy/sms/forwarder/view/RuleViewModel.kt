package com.idormy.sms.forwarder.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.RuleAndSender
import com.idormy.sms.forwarder.db.repositories.RuleRepository

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

    private val _rules: MutableLiveData<List<RuleAndSender>> = MutableLiveData(ArrayList())

    val rules: LiveData<List<RuleAndSender>> = _rules

    fun loadRules() = launchAsync({
        repository.getRuleAndSender()
    }, {rules ->
        if (rules.isNotEmpty()) {
            _rules.postValue(rules)
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