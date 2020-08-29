package com.example.running_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.running_app.R
import com.example.running_app.services.TrackingService
import com.example.running_app.ui.viewmodels.MainViewModel
import com.example.running_app.utils.ACTION_START_OR_RESUME_SERVICE
import com.google.android.gms.maps.GoogleMap
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

        mapView.getMapAsync{
            map = it
        }
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

    //To call the service
    private fun sendCommandToService(action : String)=
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }




}