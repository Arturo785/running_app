package com.example.running_app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.running_app.R
import com.example.running_app.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*

// when injecting into an android component like activity and etc
// also to allow the injection of fields
@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run){

    //This way we inject the viewModel but dagger manages the factory by itself so
    // by viewModels is necessary
    private val viewModel : MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener {
            //To navigate with the actions provided in the navigation resource
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }
}