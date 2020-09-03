package com.example.running_app.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.running_app.R
import com.example.running_app.models.Run
import com.example.running_app.services.PolyLine
import com.example.running_app.services.TrackingService
import com.example.running_app.ui.viewmodels.MainViewModel
import com.example.running_app.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round


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

    private var _currentTimeInMillis = 0L

    private var menu : Menu? = null

    //Mocked weight, not saved yet
    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // to allow have a menu in the fragment
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack() // centers the map to make the snapShot
            endRunAndSaveToDb() // //makes the snapshot and saves
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
            menu?.getItem(0)?.isVisible = true
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
            menu?.getItem(0)?.isVisible = true
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

        TrackingService._timeRunInMilliSeconds.observe(viewLifecycleOwner, Observer {
            _currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(_currentTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    //inflates and assigns our global menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Means we started a run
        if(_currentTimeInMillis > 0){
            this.menu?.getItem(0)?.isVisible = true // enables the cancel option
        }
    }

    private fun showCancelAlertDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure you want to cancel the run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                stopRun()
            }
            .setNegativeButton("No"){dialogInter,_ ->
                dialogInter.cancel()
            }
            .create()
        dialog.show()
    }

    //Sends the signal rto stop the service
    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cancel_tracking ->{
                showCancelAlertDialog() // shows if want to cancel the tracking
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Prepares the map to do the screenShot to save the run
    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        //Adds all the available lines into the bounds
        for (lines in _pathPoints){
            for (pos in lines){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt() // the padding
            )
        )
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot { bmp ->
            var distanceInMeters = 0

            _pathPoints.forEach{
                distanceInMeters += TrackingUtility.calculatePolyDistanceLenght(it).toInt()
            }

            val averageSpeed = round((distanceInMeters / 1000f) / (_currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                bmp,
                dateTimestamp,
                averageSpeed,
                distanceInMeters,
                _currentTimeInMillis,
                caloriesBurned
            )

            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView), // this is because we navigate to another view after this
                "Run saved successfully",
                Snackbar.LENGTH_LONG).show()

            stopRun()
        }
    }

}