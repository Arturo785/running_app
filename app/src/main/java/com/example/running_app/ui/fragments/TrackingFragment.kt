package com.example.running_app.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.running_app.R
import com.example.running_app.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


// when injecting into an android component like activity and etc
// also to allow the injection of fields
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking){

    //This way we inject the viewModel but dagger manages the factory by itself so
    // by viewModels is necessary
    private val viewModel : MainViewModel by viewModels()

}