package com.example.running_app.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.example.running_app.utils.ACTION_PAUSE_SERVICE
import com.example.running_app.utils.ACTION_START_OR_RESUME_SERVICE
import com.example.running_app.utils.ACTION_STOP_SERVICE
import timber.log.Timber

// inherits from lifeCycleService instead of Service because needs to be observed from other parts
class TrackingService : LifecycleService() {

    //part of the lifecycle of services, it starts the service
    //This is called when a component like an activity calls to start or do something with the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}