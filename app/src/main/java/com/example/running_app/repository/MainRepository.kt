package com.example.running_app.repository

import androidx.room.Delete
import com.example.running_app.db.RunDAO
import com.example.running_app.models.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao : RunDAO
) {
    suspend fun insertRun(run : Run) =
        runDao.insertRun(run)

    suspend fun deleteRun(run : Run) =
        runDao.deleteRun(run)

    fun getAllRunsSortedByDate() =
        runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() =
        runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCalories() =
        runDao.getAllRunsSortedByCalories()

    fun getAllRunsSortedByMilliseconds() =
        runDao.getAllRunsSortedByMilliseconds()

    fun getAllRunsSortedByKM() =
        runDao.getAllRunsSortedKM()

    fun getTotalAverageSpeed() =
        runDao.getTotalAverageSpeed()

    fun getTotalCaloriesBurned() =
        runDao.getTotalCaloriesBurned()

    fun getTotalDistanceInMeters() =
        runDao.getTotalDistanceInMeters()

    fun getTotalTimeInMillis() =
        runDao.getTotalTimeInMilliseconds()
}