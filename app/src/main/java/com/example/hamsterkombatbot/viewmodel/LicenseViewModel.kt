package com.example.hamsterkombatbot.viewmodel

import ValidateFailedResponse
import ValidateSuccessResponse
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hamsterkombatbot.MainApplication
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.api.LicenseAPI
import com.example.hamsterkombatbot.helper.PreferencesHelper
import kotlinx.coroutines.launch

class LicenseViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LicenseViewModel"
    }

    private val _licenseState = MutableLiveData<LicenseState>(LicenseState.Initial)
    val licenseState: LiveData<LicenseState>
        get() = _licenseState

    private lateinit var preferencesHelper: PreferencesHelper

    private lateinit var licenseAPI: LicenseAPI

    fun init() {
        val currentAccount = PreferencesHelper.getCurrentAccount()
        preferencesHelper =
            PreferencesHelper(getApplication<Application>().applicationContext, currentAccount)
        licenseAPI = LicenseAPI()

        val currentToken = preferencesHelper.getLicenseToken() ?: ""
        if (currentToken.isEmpty()) {
            _licenseState.value = LicenseState.EnterLicense
        } else {
            validateLicense(currentToken)
        }
    }

    fun validateLicense(token: String) {
        viewModelScope.launch {
            when (val result = licenseAPI.validateLicense(token)) {
                is ValidateFailedResponse -> {
                    _licenseState.value = LicenseState.Error(result.message)
                }
                is ValidateSuccessResponse -> {
                    _licenseState.value = LicenseState.Validated
                }
                else -> {
                    _licenseState.value =
                        LicenseState.Error(
                            MainApplication.appContext.getString(R.string.unknown_state)
                        )
                    Log.d(TAG, "Unknown error: $result")
                }
            }
        }
        _licenseState.value = LicenseState.Loading
    }

    fun activateLicense(license: String) {
        viewModelScope.launch {
            when (val result = licenseAPI.activateLicense(license)) {
                is ValidateFailedResponse -> {
                    _licenseState.value = LicenseState.Error(result.message)
                }
                is ValidateSuccessResponse -> {
                    preferencesHelper.setLicenseToken(result.data.token)
                    _licenseState.value = LicenseState.Activated(result.data.token)
                }
                else -> {
                    _licenseState.value =
                        LicenseState.Error(
                            MainApplication.appContext.getString(R.string.unknown_state)
                        )
                    Log.d(TAG, "Unknown error: $result")
                }
            }
        }
        _licenseState.value = LicenseState.Loading
    }

    fun removeToken() {
        preferencesHelper.setLicenseToken("")
        _licenseState.value = LicenseState.EnterLicense
    }

    sealed class LicenseState {
        data object Loading : LicenseState()

        data object Initial : LicenseState()

        data class Error(val message: String) : LicenseState()
        // enter license
        data object EnterLicense : LicenseState()
        // validated
        data object Validated : LicenseState()
        // not validated
        data class Activated(val token: String) : LicenseState()
    }
}
