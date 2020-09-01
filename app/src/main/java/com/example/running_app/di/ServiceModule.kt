package com.example.running_app.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.running_app.R
import com.example.running_app.ui.MainActivity
import com.example.running_app.utils.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.running_app.utils.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


//Means that only will live as long as the service lives instead of the appComponent
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule{

    @ServiceScoped // only will be one instance when the service is alive, if destroyed another will be created
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(     //To define what to do when touching the notification
        @ApplicationContext app: Context
    ) =
        PendingIntent.getActivity( //Uses getActivity cause it will attach an activity
            app,
            0,
            Intent(app, MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT // constant to control what action is needed with the intent
            },
            PendingIntent.FLAG_UPDATE_CURRENT // if the intent already existed it will update it instead of
            // creating a new one
        )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent // given by the provide fun up
    ) = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false) //not disappear when touching
        .setOngoing(true) // can't be swiped away
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("Running app")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent) // declares what to do or where to go when the
    //notification is touched
}