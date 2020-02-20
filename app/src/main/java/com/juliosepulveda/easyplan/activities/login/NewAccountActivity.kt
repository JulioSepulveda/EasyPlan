package com.juliosepulveda.easyplan.activities.login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.*
import com.juliosepulveda.easyplan.R
import com.juliosepulveda.easyplan.utils.*
import kotlinx.android.synthetic.main.activity_new_account.*

class NewAccountActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)

        etEmailNewAccount.validate {
            etEmailNewAccount.error = if (isValidEmail(it)) null else getText(R.string.error_email)
        }
        etPasswordNewAccount.validate {
            etPasswordNewAccount.error = if (isValidPass(it)) null else getText(R.string.error_password)
        }
        etConfirmPass.validate {
            etConfirmPass.error = if (isValidConfirmPass(etPasswordNewAccount.text.toString(), it)) null else getText(R.string.error_confirm_pass)
        }

        btnCreateAccount.setOnClickListener {
            val email: String = etEmailNewAccount.text.toString()
            val pass: String = etPasswordNewAccount.text.toString()

            if (validation(email, pass, etConfirmPass.text.toString()))
                createAccount(email, pass)
            else
                toast(R.string.error_new_account)
        }
        btnGoBack.setOnClickListener { goBack() }
    }

    private fun goBack(){
        goToActivityWithoutHistory<LoginActivity> {}
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun createAccount(email: String, pass: String){
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this){ task ->
            if (task.isSuccessful){
                mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this){
                    toast(R.string.verified_email, Toast.LENGTH_LONG)
                    goToActivityWithoutHistory<LoginActivity> {}
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
            else{
                errorAuthFirebase(task.exception as FirebaseAuthException)
            }
        }
    }

    private fun validation (email: String, pass: String, confirmPass: String): Boolean{
        return email.isNotEmpty() &&
                etEmailNewAccount.error == null &&
                pass.isNotEmpty() &&
                etPasswordNewAccount.error == null &&
                pass == confirmPass &&
                etConfirmPass.error == null
    }

}
