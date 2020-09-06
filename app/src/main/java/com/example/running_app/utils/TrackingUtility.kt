package com.example.running_app.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.running_app.services.PolyLine
import pub.devrel.easypermissions.EasyPermissions
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

object TrackingUtility {

    //To manage permissions with and without android 10
    fun hasLocationPermissions(context: Context) =
        //Does not has android Q
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        //Has android Q
        else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }


    fun getFormattedStopWatchTime(ms: Long, includeMillis : Boolean = false) : String{
        var millis = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millis)

        //we reduce the Hours to only get the remain minutes
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        // we get the seconds
        millis -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        if (!includeMillis){
            //Makes this 12:00:00 or this 02:00:00
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }

        millis -= TimeUnit.SECONDS.toMillis(seconds)
        millis /= 10 // to make it two number digit

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (millis < 10) "0" else ""}$millis"
    }


    fun calculatePolyDistanceLenght(polyLine: PolyLine) : Float{
        var distance = 0f // the initial and default value

        for(i in 0..polyLine.size -2){ //-2 is to not get out of boundaries of array
            val pos1 = polyLine[i]
            val pos2 = polyLine[i+1]

            val result = FloatArray(1)

            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result // gets the result
            )
            distance += result[0] //add up the info
        }
        return distance
    }

    }
