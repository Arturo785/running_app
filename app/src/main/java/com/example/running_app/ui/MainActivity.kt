package com.example.running_app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.running_app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

// when injecting into an android component like activity and etc
// also to allow the injection of fields
// needed in here because it hosts fragments that are injected
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val TAG = "MainActivityLOG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Our custom toolBar
        setSupportActionBar(toolbar)

        //Setup our navigation
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        //We do this because they are certain fragments that we don't want the bottomNav to be visible
        navHostFragment.findNavController()
            .addOnDestinationChangedListener{_, destination,_ ->
                when(destination.id){
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }
}