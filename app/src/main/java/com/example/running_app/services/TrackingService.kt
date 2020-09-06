package com.example.running_app.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.running_app.R
import com.example.running_app.ui.MainActivity
import com.example.running_app.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng> //Lines
typealias PolyLinesList = MutableList<PolyLine> //multiple lists of lines

// inherits from lifeCycleService instead of Service because needs to be observed from other parts
@AndroidEntryPoint
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


    private var _isFirstRun = true


    //Injected properties
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var currentNotificationBuilder : NotificationCompat.Builder

    var _servicedKilled = false


    //Clock variables
    private val _timeRunInSeconds = MutableLiveData<Long>()
    private var _isTimerEnabled = false
    private var _lapTime = 0L
    private var _totalRunTime = 0L
    private var _timeStarted = 0L
    private var _lastSecondTimeStamp = 0L

    //To be able to observe the data from any part (STATIC)
    companion object{
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLinesList>()
        val _timeRunInMilliSeconds = MutableLiveData<Long>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) //an empty list
        _timeRunInSeconds.postValue(0L)
        _timeRunInMilliSeconds.postValue(0L)
    }

    //Sets the 1st list of polyLines to empty and post that empty result or if it is null
    // initialises the list
    //Update
    //What really does is
    // 1.- If is null initializes the lists
    // 2.- If the service is paused the fun is called again in the startTimer
    // and because of that adds a new list to the new set of polyLines
    // this is because if the fragment is recreated it will connect different lists (if existent)
    // that appear to be different lines instead of a continue one
    private fun addEmptyPolyLine() =
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    // makes a new LatLong and adds it to the last list of polyLines
    private fun addPathPoint(location : Location?){
        location?.let {
            val pos = LatLng(location.latitude,location.longitude)

            pathPoints.value?.apply {
                // applies it to the last PolyLine retrieved
                last().add(pos) // gets the last list
                pathPoints.postValue(this) //Posts changed list to the liveData
            }
        }
    }

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
                        startTimer()
                        Timber.d("Resumed service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder // Use as an auxiliar
        postInitialValues()
      //  fusedLocationProviderClient = FusedLocationProviderClient(this) // needed to track the location
        // injected

        isTracking.observe(this, Observer {
            updateLocationTracking(it) // changes the location
            updateNotificationTrackingState(it) //Changes the buttons of the notification
        })

    }

    private fun startForegroundService(){
        startTimer()
        // a service from android OS that we need in order to show a notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Channel is only needed for Oreo or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //we are on android Oreo or later
            createNotificationChannel(notificationManager)
        }

        //Starts the service as foreground with the prev config
        //Receives the baseNotification through injection
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        //We have the observer in here to make use of the notificationManager
        _timeRunInSeconds.observe(this, Observer {
            if (!_servicedKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L)) //to make it MS
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }


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

    //We ask the permission with easy permissions so it is not detected
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if (isTracking){ // if we are tracking and have permission to track
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL // how fast and how often we need the updates
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallBack,
                    Looper.getMainLooper())
            }
        }
        else{ // we are not tracking anymore
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }
    }


    //Our call back to add locations to out liveData
    private val locationCallBack = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!){ //if has value and the list of locations is not null
                result?.locations?.let {locations ->
                    locations.forEach{
                        addPathPoint(it) // add each locations to our liveData
                        Timber.d("New location: ${it.latitude}, ${it.longitude}")
                    }
                }
            }
        }
    }

    //to change the value observed and used within the service
    private fun pauseService(){
        isTracking.postValue(false)
        _isTimerEnabled = false
    }

    private fun startTimer(){
        addEmptyPolyLine() // to be able to save the tracking
        isTracking.postValue(true) // begins to track
        _timeStarted = System.currentTimeMillis()
        _isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //Time difference
                _lapTime = System.currentTimeMillis() - _timeStarted

                //Update the live data with new times
                _timeRunInMilliSeconds.postValue(_totalRunTime + _lapTime)

                //to simulate the seconds passed in a human way
                //and refresh the time in seconds every second
                if (_timeRunInMilliSeconds.value!! >= _lastSecondTimeStamp + 1000L){
                    _timeRunInSeconds.postValue(_timeRunInSeconds.value!! + 1)
                    _lastSecondTimeStamp += 1000L // to not lose the count
                }
                //to make it easy on the OS and not consume that much resources
                delay(TIMER_UPDATE_INTERVAL)
            }
            _totalRunTime += _lapTime
        }

    }

    //When clicked the notification buttons this is what will happen when touching the button in the notification
    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if (isTracking) "Pause" else "Resume"

        // creates the pending intent (what will happen when touching the button in the notification)
        //Uses getService cause it will attach a service
        val pendingIntent = if (isTracking){
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        }
        else{
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            getService(this,2, resumeIntent, FLAG_UPDATE_CURRENT)
        }


        // a service from android OS that we need in order to show a notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Removes previous actions given in the notification
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!_servicedKilled){
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)

            //As long it is the same ID it "updates" the notification
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun killService(){
        _servicedKilled = true // resets the initials values and kills the service
        _isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }


}