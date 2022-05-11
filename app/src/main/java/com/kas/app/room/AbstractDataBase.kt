package com.kas.app.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocPhoneGpsStatesEntity::class], version = 1)
abstract class AbstractDataBase : RoomDatabase() {
    abstract fun spyDao(): SpyDao
}