package com.example.running_app.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.running_app.R
import com.example.running_app.ui.viewmodels.MainViewModel
import com.example.running_app.utils.REQUEST_CODE_LOCATION_PERMISSION
import com.example.running_app.utils.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

// when injecting into an android component like activity and etc
// also to allow the injection of fields
@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks{

    //This way we inject the viewModel but dagger manages the factory by itself so
    // by viewModels is necessary
    private val viewModel : MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()

        fab.setOnClickListener {
            //To navigate with the actions provided in the navigation resource
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }


    //https://stackoverflow.com/questions/60402490/difference-between-getcontext-and-requirecontext-when-using-fragments#:~:text=1%20Answer&text=getContext()%20returns%20a%20nullable,when%20one%20isn't%20available.
    private fun requestPermissions(){ // has the real context of the activity
        if(TrackingUtility.hasLocationPermissions(requireContext())){
            return
            //has permissions so nothing else to do
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            //has android Q
            EasyPermissions.requestPermissions(
                this,
                "This app needs the permissions for tracking",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        else{
            EasyPermissions.requestPermissions(
                this,
                "This app needs the permissions for tracking",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    // From the interface extended
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show() // to inform about how to enable permissions again
        }
        else{
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //do nothing
    }

    //This comes from native android
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //this is from the library imported
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}