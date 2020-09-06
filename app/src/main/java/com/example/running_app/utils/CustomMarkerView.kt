package com.example.running_app.utils

import android.content.Context
import com.example.running_app.models.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs : List<Run>,
    context  : Context,
    layoutId : Int) : MarkerView(context, layoutId){

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null){
            return
        }

        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

        tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKM}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = " ${run.distanceInMeters /1000f}km"
        tvDistance.text = distanceInKm

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        tvCaloriesBurned.text = caloriesBurned
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())

    }
}
