package com.example.budgey

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudgeyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Firebase is automatically initialized through google-services plugin
        // Additional app-level initialization can be added here if needed
    }
}
