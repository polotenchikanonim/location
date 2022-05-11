package com.kas.app

import android.content.Context
import androidx.room.Room
import com.kas.app.room.AbstractDataBase


class Database {

    companion object {

        private lateinit var database: AbstractDataBase

        private const val dbName = "monitoring.db"

        fun getDao() = database.spyDao()

        fun init(context: Context) {
//            if (database == null) {
            database = Room.databaseBuilder(
                context, AbstractDataBase::class.java, dbName
            ).build()
//            }
        }

    }
}