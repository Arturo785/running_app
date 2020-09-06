package com.example.running_app.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.running_app.R
import com.example.running_app.utils.KEY_FIRST_TIME_TOgGLE
import com.example.running_app.utils.KEY_NAME
import com.example.running_app.utils.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import kotlinx.android.synthetic.main.fragment_setup.etWeight
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup){

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject // provided from the appModule
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Not the first time opening the app
        if (!isFirstAppOpen){
            //to avoid returning to this fragment if back button is pressed
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPreferences()
            if (success){
                //To change to the run fragment
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
            else{
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePersonalDataToSharedPreferences() : Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOgGLE, false)
            .apply()

        val toolBarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolBarText
        return true
    }

}