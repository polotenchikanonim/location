package com.kas.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi

internal object MyPhoneStateListener {

    private lateinit var telephonyManager: TelephonyManager

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getSignalStrength(): MyCellInfo? {
        val cellInfoList = telephonyManager.allCellInfo
        for (i in cellInfoList.indices) {
            if (cellInfoList[i].isRegistered) {
                when {
                    cellInfoList[i] is CellInfoWcdma -> {
                        val cellInfoWcdma = cellInfoList[i] as CellInfoWcdma
                        val cellSignalStrengthWcdma = cellInfoWcdma.cellSignalStrength
                        return MyCellInfo(cellSignalStrengthWcdma.dbm,
                            cellSignalStrengthWcdma.asuLevel,
                            cellSignalStrengthWcdma.level)
                    }
                    cellInfoList[i] is CellInfoGsm -> {
                        val cellInfoGsm = cellInfoList[i] as CellInfoGsm
                        val cellSignalStrengthGsm = cellInfoGsm.cellSignalStrength
                        return MyCellInfo(cellSignalStrengthGsm.dbm, cellSignalStrengthGsm.asuLevel,
                            cellSignalStrengthGsm.level)
                    }
                    cellInfoList[i] is CellInfoLte -> {
                        val cellInfoLte = cellInfoList[i] as CellInfoLte
                        val cellSignalStrengthLte = cellInfoLte.cellSignalStrength
                        return MyCellInfo(cellSignalStrengthLte.dbm, cellSignalStrengthLte.asuLevel,
                            cellSignalStrengthLte.level)
                    }
                    cellInfoList[i] is CellInfoCdma -> {
                        val cellInfoCdma = cellInfoList[i] as CellInfoCdma
                        val cellSignalStrengthCdma = cellInfoCdma.cellSignalStrength
                        return MyCellInfo(cellSignalStrengthCdma.dbm,
                            cellSignalStrengthCdma.asuLevel,
                            cellSignalStrengthCdma.level)
                    }
                }
            }
        }
        return null
    }

    fun init(it: Context) {
        telephonyManager = it.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
}