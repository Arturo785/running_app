package com.example.running_app.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.running_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {


    //HighOrder fun
    //https://www.geeksforgeeks.org/kotlin-higher-order-functions/

    //Our lambda method type unit, receives nothing
    private var yesListener: (() -> Unit)? = null

    //Receives a lambda and sets the var to it
    fun setYesListener(listener: () -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return  MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure you want to cancel the run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                yesListener?.let {
                    it() // calls the function if one was given
                }
            }
            .setNegativeButton("No"){dialogInter,_ ->
                dialogInter.cancel()
            }
            .create()
    }
}