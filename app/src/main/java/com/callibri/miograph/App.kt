package com.callibri.miograph

import android.app.Application
import android.content.SharedPreferences

class App : Application() {
    companion object {
        lateinit var prefs: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
    }
}