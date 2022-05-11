package com.kas.app

import android.app.Application


class App : Application() {

    private val config = "config"

    override fun onCreate() {
        super.onCreate()
        instance = this
        initConfig()
        initDb()
        initCrashlytics()
    }

    private fun initCrashlytics() {
        val customKeySamples = CustomKeySamples(applicationContext)
        customKeySamples.setSampleCustomKeys()
        customKeySamples.updateAndTrackNetworkState()
    }

    private fun initConfig() {
        val sharedPreferences = getSharedPreferences(config, MODE_PRIVATE)
        secureId = sharedPreferences.getString("secureId", "").toString()
    }

    private fun initDb() {
        Database.init(this)
    }

    companion object {
        lateinit var instance: App
        var secureId = ""
        const val tag = "ChoreographerKt"
    }
}