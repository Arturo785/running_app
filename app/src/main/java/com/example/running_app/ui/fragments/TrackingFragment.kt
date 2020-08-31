package com.example.running_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.running_app.R
import com.example.running_app.services.PolyLine
import com.example.running_app.services.TrackingService
import com.example.running_app.ui.viewmodels.MainViewModel
import com.example.running_app.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*


// when injecting into an android component like activity and etc
// also to allow the injection of fields
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking){

    //This way we inject the viewModel but dagger manages the factory by itself so
    // by viewModels is necessary
    private val viewModel : MainViewModel by viewModels()

    //The actual map object that will live in the map view
    private var map : GoogleMap? = null

    private var _isTracking = false
    private var _pathPoints = mutableListOf<PolyLine>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.getMapAsync{
            map = it
            addAllPolyLines() // means the view was created or recreated
        }

        subscribeObservers()
    }

    //The map has it's own lifecycle that needs to be override
    //region lifeCycle
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    //Not override because it may do the app crash
/*    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }*/

    //endregion

    //To call the service and start it with the given action
    private fun sendCommandToService(action : String)=
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


    //To only add the last two points retrieved
    private fun addLatestPolyLine(){
        // get the last list holded on the list is greater than 1
        if (_pathPoints.isNotEmpty() && _pathPoints.last().size > 1){
            val preLastLatLngPos = _pathPoints.last().size -2
            val preLastLatLng = _pathPoints.last()[preLastLatLngPos]

            val lastLatLng = _pathPoints.last().last()

            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng,lastLatLng)

            map?.addPolyline(polyLineOptions)
        }
    }

    //When recreating the fragment and add all the data
    private fun addAllPolyLines(){
        _pathPoints.forEach{
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)

            map?.addPolyline(polyLineOptions)
        }
    }

    private fun moveCameraToUser(){
        //Has at least one coordinate
        if (_pathPoints.isNotEmpty() && _pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                _pathPoints.last().last(),
                MAP_ZOOM // makes a zoom to the lastPoint received
            ))
        }
    }


    private fun toggleRun(){
        //Sends the action to start, pause or resume the service
        if (_isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //Fun called by the observed data from the service called by the button that gives the commands
    private fun updateTracking(isTracking : Boolean){
        this._isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }
        else{
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }


    private fun subscribeObservers(){
        //in a fragment always put viewLifecycleOwner as owner
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            _pathPoints = it // receives new PolyLines
            addLatestPolyLine() // draw into the map
            moveCameraToUser()
        })
    }

}