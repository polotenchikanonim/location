package com.kas.app.model

import android.content.Context
import android.content.SharedPreferences
import com.kas.app.App
import com.kas.app.App.Companion.secureId

class Config {

    companion object {
        @JvmStatic
        fun updateSecureId(field: String) {
            secureId = field
            val sharedPreferences: SharedPreferences =
                App.instance.getSharedPreferences("config", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("secureId", field)
            editor.apply()
        }
    }
}