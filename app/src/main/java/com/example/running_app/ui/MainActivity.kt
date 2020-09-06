package com.example.running_app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.running_app.R
import com.example.running_app.utils.ACTION_SHOW_TRACKING_FRAGMENT
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

        //if the activity was destroyed but the service is running and the user touches it
        // then onCreate of this activity will be called and do this
        navigateToTrackingFragmentIfNeeded(intent)

        //Our custom toolBar
        setSupportActionBar(toolbar)

        //Setup our navigation
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /*Do nothing*/ }

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

    //if the service notification was touched but the activity is not destroyed
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){ // the intent action made for us
            //means was launched by the notification click
            navHostFragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}