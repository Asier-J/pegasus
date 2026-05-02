package com.example.pegasus

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Hilt's dependency-injection graph.
 * Sprint 03: required for `@AndroidEntryPoint` and `@HiltViewModel` to work.
 */
@HiltAndroidApp
class PegasusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "PegasusApplication started — Hilt graph initialized")
    }

    companion object {
        private const val TAG = "PegasusApplication"
    }
}
