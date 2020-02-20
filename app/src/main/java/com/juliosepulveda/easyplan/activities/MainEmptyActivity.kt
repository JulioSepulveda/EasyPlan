package com.juliosepulveda.easyplan.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.juliosepulveda.easyplan.activities.login.LoginActivity
import com.juliosepulveda.easyplan.utils.goToActivityWithoutHistory

class MainEmptyActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mAuth.currentUser == null){
            goToActivityWithoutHistory<LoginActivity>()
        }else{
            goToActivityWithoutHistory<MainActivity>()
        }
        finish()
    }
}
