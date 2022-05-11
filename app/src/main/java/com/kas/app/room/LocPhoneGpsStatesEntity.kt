package com.kas.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kas.app.App.Companion.secureId

val secure: String = secureId

@Entity(tableName = "loc_phone_gps_states_entity")
data class LocPhoneGpsStatesEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val Time: Long,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val usedSatellites: Int = 0,
    val avgSnr: Float = 0f,
    val satelliteCount: Int = 0,
    val gps: Boolean = false,
    val secureId: String = secure,
    var message: String? = null,
) {
    var level: Int = 0
    var asuLevel: Int = 0
    var dbm: Int = 0
}