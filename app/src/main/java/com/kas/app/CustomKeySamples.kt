package com.kas.app


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase

class CustomKeySamples(
    private val context: Context,
    private var callback: NetworkCallback? = null,
) {

    /**
     * Set a subset of custom keys simultaneously.
     */
    @SuppressLint("HardwareIds")
    fun setSampleCustomKeys() {
        Firebase.crashlytics.setUserId("123456789")

        Firebase.crashlytics.setCustomKeys {
            key("Locale", locale)
            key("Screen Density", density)
            key("Google Play Services Availability", googlePlayServicesAvailability)
            key("Os Version", osVersion)
            key("Install Source", installSource)
            key("Preferred ABI", preferredAbi)

        }

        FirebaseCrashlytics.getInstance()
            .setUserId(Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID))
    }

    /**
     * Update network state and add a hook to update network state going forward.
     *
     * Note: This code is executed above API level N.
     */
    fun updateAndTrackNetworkState() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.let { updateNetworkCapabilityCustomKeys(it) }

        synchronized(this) {
            if (callback == null) {
                // Set up a callback to match our best-practices around custom keys being up-to-date
                val newCallback: NetworkCallback = object : NetworkCallback() {
                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        updateNetworkCapabilityCustomKeys(networkCapabilities)
                    }
                }
                callback = newCallback
                connectivityManager.registerDefaultNetworkCallback(newCallback)
            }
        }
    }


    private fun updateNetworkCapabilityCustomKeys(networkCapabilities: NetworkCapabilities) {
        Firebase.crashlytics.setCustomKeys {
            key("Network Bandwidth", networkCapabilities.linkDownstreamBandwidthKbps)
            key("Network Upstream", networkCapabilities.linkUpstreamBandwidthKbps)
            key("Network Metered", networkCapabilities
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
            key("Network Capabilities", networkCapabilities.toString())
        }
    }


    @Suppress("DEPRECATION")
    @Deprecated("Prefer updateAndTrackNetworkState, which does not require READ_PHONE_STATE")
    @SuppressLint("MissingPermission")
    fun addPhoneStateRequiredNetworkKeys() {
        val telephonyManager = context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val networkType: Int = telephonyManager.dataNetworkType

        Firebase.crashlytics.setCustomKeys {
            key("Network Type", networkType)
            key("Sim Operator", telephonyManager.simOperator)
        }
    }

    /**
     * Retrieve the locale information for the app.
     *
     * Supressed deprecation warning because that code path is only used below API Level N.
     */
    @Suppress("DEPRECATION")
    val locale: String
        get() =
            context
                .resources
                .configuration
                .locales[0].toString()

    /**
     * Retrieve the screen density information for the app.
     */
    private val density: Float
        get() = context
            .resources
            .displayMetrics.density

    /**
     * Retrieve the locale information for the app.
     */
    private val googlePlayServicesAvailability: String
        get() = if (GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(context) == 0
        ) "Unavailable" else "Available"

    /**
     * Return the underlying kernel version of the Android device.
     */
    private val osVersion: String
        get() = System.getProperty("os.version") ?: "Unknown"

    /**
     * Retrieve the preferred ABI of the device. Some devices can support
     * multiple ABIs and the first one returned in the preferred one.
     *
     * Supressed deprecation warning because that code path is only used below Lollipop.
     */
    @Suppress("DEPRECATION")
    val preferredAbi: String
        get() =
            Build.SUPPORTED_ABIS[0]

    /**
     * Retrieve the install source and return it as a string.
     *
     * Supressed deprecation warning because that code path is only used below API level R.
     */
    @Suppress("DEPRECATION")
    val installSource: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val info = context
                    .packageManager
                    .getInstallSourceInfo(context.packageName)

                // This returns all three of the install source, originating source, and initiating
                // source.
                "Originating: ${info.originatingPackageName ?: "None"}, " +
                        "Installing: ${info.installingPackageName ?: "None"}, " +
                        "Initiating: ${info.initiatingPackageName ?: "None"}"
            } catch (e: PackageManager.NameNotFoundException) {
                "Unknown"
            }
        } else {
            context.packageManager.getInstallerPackageName(context.packageName) ?: "None"
        }

}
