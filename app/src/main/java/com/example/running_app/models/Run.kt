package com.example.running_app.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "run_table")
data class Run(
    var img : Bitmap? = null,
    var timeStamp : Long = 0L,
    var avgSpeedInKM : Float = 0f,
    val distanceInMeters : Int = 0,
    var timeInMillis : Long = 0L,
    var caloriesBurned : Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null
}