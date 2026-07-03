package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.DemandRepository

class DemandApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "demand_database"
        ).build()
    }
    
    val repository by lazy {
        DemandRepository(database.demandItemDao())
    }
}
