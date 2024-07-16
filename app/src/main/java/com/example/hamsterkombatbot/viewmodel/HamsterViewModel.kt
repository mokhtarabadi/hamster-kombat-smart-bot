package com.example.hamsterkombatbot.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.model.*
import com.example.hamsterkombatbot.model.payload.*
import com.example.hamsterkombatbot.model.response.*
import com.example.hamsterkombatbot.repository.HamsterRepository
import kotlinx.coroutines.launch

class HamsterViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HamsterViewModel"
    }

    private val repository = HamsterRepository(PreferencesHelper.getCurrentAccount())

    val hamsterState: LiveData<HamsterRepository.HamsterState> = repository.getState()
    val hamsterEvent: LiveData<HamsterRepository.HamsterEvent> = repository.getEvent()

    val currentClicker: LiveData<ClickerUser> = repository.getCurrentClicker()
    val currentCombo: LiveData<DailyCombo> = repository.getCurrentCombo()

    fun init() {
        viewModelScope.launch { repository.init() }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }

    fun autoTapper() {
        viewModelScope.launch { repository.autoTapper() }
    }

    fun autoBoost() {
        viewModelScope.launch { repository.autoBoost() }
    }

    fun smartUpgrade() {
        viewModelScope.launch { repository.smartUpgrade() }
    }

    fun autoCombo() {
        viewModelScope.launch { repository.autoCombo() }
    }
}
