package com.juliosepulveda.easyplan.application

import android.app.Application
import com.juliosepulveda.easyplan.utils.MySharedPreferences

val preferences: MySharedPreferences by lazy { MyApp.prefs!! }

class MyApp : Application() {

    companion object{
        var prefs: MySharedPreferences? = null
    }

    override fun onCreate() {
        super.onCreate()
        prefs = MySharedPreferences(applicationContext)
    }

}