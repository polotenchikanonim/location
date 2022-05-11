package com.kas.app.repository.remote


import android.annotation.SuppressLint
import android.location.LocationManager
import android.util.Log
import com.google.gson.Gson
import com.kas.app.App.Companion.tag
import com.kas.app.room.LocPhoneGpsStatesEntity
import okhttp3.Callback
import okhttp3.Credentials.basic
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


class RepositoryRemoteImpl(private val locationManager: LocationManager?) :
    RepositoryServerLocation {

    private var okHttpClient = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val builder = Request.Builder().apply {
        url("http://0.0.0.0:5000/location")
        header("Authorization", basic("username", "password"))
    }

    override fun post(
        callbackHttp: Callback, data: String,
    ) {
        Log.d(tag, data)
        val body = data.toRequestBody(mediaType)
        okHttpClient.newCall(builder.method("POST", body).build()).enqueue(callbackHttp)
    }

    @SuppressLint("MissingPermission")
    override fun postData(
        provider: String, callbackTask: Callback, data: String?,
    ) {
        Log.d(tag, "$provider $data")
        locationManager?.getLastKnownLocation(provider)?.let {
            val task = LocPhoneGpsStatesEntity(0, System.currentTimeMillis() / 1000,
                it.latitude, it.longitude, message = "$data")

            post(callbackTask, Gson().toJson(task))
        }
    }
}