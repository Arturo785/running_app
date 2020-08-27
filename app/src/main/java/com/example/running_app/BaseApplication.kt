package com.example.running_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application(){
    //https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
    /*The Application class in Android is the base class within an Android app that contains
    all other components such as activities and services. The Application class, or any subclass
    of the Application class, is instantiated before any other class when the process for your
    application/package is created.*/

/*    Your application is a context that is always running while your activities and services are running.

    It is also the first context to be created and the last to be destroyed. Thus, it surrounds the life cycle of your app.

    You can use the application class as a way to share data or components
    (for dependency injection for instance). For instance if you want to share a singleton between activities,
    you can create the instance in the application class and provide a getter, then all other contexts can get the singleton via*/


}