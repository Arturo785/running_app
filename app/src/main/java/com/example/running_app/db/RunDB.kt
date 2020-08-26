package com.example.running_app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.running_app.models.Run

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunDB : RoomDatabase() {

    //Needed and implemented by Room
    abstract fun getRunDao() : RunDAO

    //does not need the singleton here because of dagger
}