package com.example.hamsterkombatbot

import android.app.Application
import android.content.Context
import android.util.Log

class MainApplication : Application() {
    companion object {
        const val IS_DEBUG = true
        private const val TAG = "App"
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        appContext = applicationContext
    }
}
