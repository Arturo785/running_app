package com.example.running_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.running_app.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// when injecting into an android component like activity and etc
// also to allow the injection of fields
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Just for test
    @Inject
    lateinit var  runDao : RunDAO

    val TAG = "MainActivityLOG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Just for test
        Log.d(TAG, "DAO HASH ${runDao.hashCode()}")

    }
}