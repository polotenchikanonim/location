package com.kas.app.model

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.kas.app.App.Companion.tag
import com.kas.app.BuildConfig
import com.kas.app.SpyService


class Gps(
    private val locationManager: LocationManager,
    private val locationListenerGps: LocationListener,
    private val mGnssStatusCallback: GnssStatus.Callback,
    private val myThread: SpyService.MyThread,
) {
    private val minTime = if (BuildConfig.DEBUG) 5000L else 30000L
    private val minDistance = if (BuildConfig.DEBUG) 0f else 1f


    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(provider: String) {
        locationManager.let {
            it.requestLocationUpdates(provider, minTime, minDistance, locationListenerGps)
            it.registerGnssStatusCallback(mGnssStatusCallback, myThread.handler)
            Log.d(tag, "locationListenerGps registered")
//            it.registerGnssStatusCallback(mGnssStatusCallback)
//            if (it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                it.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, 5000, minDistance, locationListenerGps)
//                it.registerGnssStatusCallback(mGnssStatusCallback)
//                Log.d(tag, "locationListenerGps registered")
//            }
        }
    }

}