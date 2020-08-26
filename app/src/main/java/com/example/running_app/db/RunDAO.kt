package com.example.running_app.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.running_app.models.Run

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run : Run)

    @Delete
    suspend fun deleteRun(run: Run)

    //************* SORT FUNCTIONS *******************************

    //suspend does not work with liveData
    @Query("SELECT * FROM run_table ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate() : LiveData<List<Run>>

    //suspend does not work with liveData
    @Query("SELECT * FROM run_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByMilliseconds() : LiveData<List<Run>>

    //suspend does not work with liveData
    @Query("SELECT * FROM run_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCalories() : LiveData<List<Run>>

    //suspend does not work with liveData
    @Query("SELECT * FROM run_table ORDER BY avgSpeedInKM DESC")
    fun getAllRunsSortedKM() : LiveData<List<Run>>

    //suspend does not work with liveData
    @Query("SELECT * FROM run_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance() : LiveData<List<Run>>


    //************* STADISTICS FUNCTIONS **************************

    @Query("SELECT SUM(timeInMillis) FROM run_table")
    fun getTotalTimeInMilliseconds(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getTotalDistanceInMeters(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKM) FROM run_table")
    fun getTotalAverageSpeed(): LiveData<Float>
}