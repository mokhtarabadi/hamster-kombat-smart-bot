package com.example.hamsterkombatbot.viewmodel

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hamsterkombatbot.MainApplication
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.activity.HamsterActivity
import com.example.hamsterkombatbot.service.MainForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServiceViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "ServiceViewModel"

        private const val REQUEST_CODE_POST_NOTIFICATION = 1000
    }

    private val _serviceState: MutableLiveData<ServiceState> =
        MutableLiveData(ServiceState.NotRunning)
    val serviceState: LiveData<ServiceState> = _serviceState

    private var pingJob: Job? = null

    private val serviceRunningReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == MainForegroundService.PONG_RESPONSE_ACTION) {
                    val isRunning = intent.getBooleanExtra("isRunning", false)

                    if (isRunning) {
                        _serviceState.value = ServiceState.Running
                    } else {
                        _serviceState.value = ServiceState.NotRunning
                    }

                    // Log.d(TAG, "Service running: $isRunning")
                }
            }
        }

    fun init() {
        pingJob =
            viewModelScope.launch {
                while (true) {
                    pingService()
                    delay(1000)
                    // Log.d(TAG, "Pinging service")
                }
            }
    }

    fun registerReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(
                serviceRunningReceiver,
                IntentFilter(MainForegroundService.PONG_RESPONSE_ACTION)
            )
    }

    fun unregisterReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(serviceRunningReceiver)
    }

    override fun onCleared() {
        super.onCleared()
        pingJob?.cancel()
    }

    private fun pingService() {
        val context = getApplication<Application>()

        val intent = Intent(context, MainForegroundService::class.java)
        intent.action = MainForegroundService.PING_ACTION
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun stopService() {
        val context = getApplication<Application>()

        val intent = Intent(context, MainForegroundService::class.java)
        intent.action = MainForegroundService.STOP_ACTION
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        _serviceState.value = ServiceState.NotRunning
    }

    fun checkPostNotificationPermission(activity: HamsterActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus =
                ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPostNotificationPermission(activity)
            } else {
                permissionGranted()
            }
        } else {
            permissionGranted()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationPermission(activity: HamsterActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE_POST_NOTIFICATION
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_POST_NOTIFICATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted()
            } else {
                permissionDenied()
            }
        }
    }

    private fun permissionGranted() {
        val context = getApplication<Application>()
        // start service
        val intent = Intent(context, MainForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)

        _serviceState.value = ServiceState.Started
    }

    private fun permissionDenied() {
        _serviceState.value =
            ServiceState.Error(
                MainApplication.appContext.getString(R.string.notification_permission_needed)
            )
    }

    sealed class ServiceState {
        data object NotRunning : ServiceState()

        data object Running : ServiceState()

        data object Started : ServiceState()

        data class Error(val message: String) : ServiceState()
    }
}
