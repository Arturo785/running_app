package com.example.running_app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.running_app.R
import com.example.running_app.ui.MainActivity
import com.example.running_app.utils.*
import timber.log.Timber

// inherits from lifeCycleService instead of Service because needs to be observed from other parts
class TrackingService : LifecycleService() {

    //SERVICES
    /*Foreground
    Can't be killed by the android system
    A foreground service performs some operation that is noticeable to the user.
    For example, an audio app would use a foreground service to play an audio track.
    Foreground services must display a Notification. Foreground services continue running
    even when the user isn't interacting with the app.*/

/*  Background
    Can't be killed by the android system
    A background service performs an operation that isn't
    directly noticed by the user. For example, if an app used a service to compact its
    storage, that would usually be a background service.*/


    var _isFirstRun = true

    //part of the lifecycle of services, it starts the service
    //This is called when a component like an activity calls to start or do something with the service
    //in this case called by the button of runFragment
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (_isFirstRun){
                        startForegroundService()
                        _isFirstRun = false
                        Timber.d("Started service")
                    }
                    else{
                        Timber.d("Resumed service")
                    }
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

    private fun startForegroundService(){
        // a service from android OS that we need in order to show a notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Channel is only needed for Oreo or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //we are on android Oreo or later
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //not disappear when touching
            .setOngoing(true) // can't be swiped away
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running app")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent()) // declares what to do or where to go when the
            //notification is touched

        //Starts the service as foreground with the prev config
        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    //To define what to do when touching the notification
    private fun getMainActivityPendingIntent() =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this,MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT // constant to control what action is needed with the intent
            },
            FLAG_UPDATE_CURRENT // if the intent already existed it will update it instead of
        // creating a new one
        )

    //Needs Oreo or newer
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager : NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}