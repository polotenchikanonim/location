package com.kas.app.room

import androidx.room.*


@Dao
interface SpyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: LocPhoneGpsStatesEntity)

    @Query("SELECT * FROM loc_phone_gps_states_entity")
    fun getAllTasks(): List<LocPhoneGpsStatesEntity>

    @Delete
    fun delete(entity: LocPhoneGpsStatesEntity)

    @Query("SELECT COUNT(*) FROM loc_phone_gps_states_entity")
    fun count(): Int

}