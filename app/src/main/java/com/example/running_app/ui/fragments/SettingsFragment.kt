package com.example.running_app.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.running_app.R
import com.example.running_app.utils.KEY_NAME
import com.example.running_app.utils.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.etName
import kotlinx.android.synthetic.main.fragment_setup.etWeight
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings){

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDataFromSharedPref()

        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()

            if (success){
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(view, "Please fill all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun applyChangesToSharedPref() : Boolean{
        val nameText = etName.text.toString()
        val weightText = etWeight.text.toString()

        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()


        val toolbarText = "Let's go $nameText"
        requireActivity().tvToolbarTitle.text = toolbarText

        return true
    }

    private fun loadDataFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)

        etName.setText(name)
        etWeight.setText(weight.toString())
    }
}