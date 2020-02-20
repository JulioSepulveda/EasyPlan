package com.juliosepulveda.easyplan.utils

import android.content.Context

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MySharedPreferences(context: Context)  {

    //Nombre del fichero
    private val filename = "PrefsEasyPlan"

    //Instancia al fichero
    private val prefs = context.getSharedPreferences(filename, Context.MODE_PRIVATE)

    //variables de nuestro fichero

    //nombre del último textview pulsado
    var group: String
    get() = prefs.getString("group", "")
    set(value) = prefs.edit().putString("group", value).apply()

    //identificador del último texview pulsado
    var id: Int
    get() = prefs.getInt("id",0)
    set(value) = prefs.edit().putInt("id", value).apply()

}