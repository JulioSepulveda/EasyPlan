package com.juliosepulveda.easyplan.activities.login

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.juliosepulveda.easyplan.activities.MainActivity
import com.juliosepulveda.easyplan.models.User
import com.juliosepulveda.easyplan.R

import com.juliosepulveda.easyplan.utils.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mGoogleApiClient: GoogleApiClient by lazy { getGoogleApiClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnSignInGoogle.setColorScheme(SignInButton.COLOR_DARK)
        btnSignInGoogle.setSize(SignInButton.SIZE_WIDE)

        btnSignIn.setOnClickListener { signInByEmail(etEmail.text.toString(), etPassword.text.toString()) }
        tvForgotPass.setOnClickListener { showAlertForResetPass(etEmail.text.toString()) }
        btnSignInGoogle.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGNIN)
        }
        btnNewAccount.setOnClickListener { goNewAccount() }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        toast(R.string.connection_google)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGNIN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                loginByGoogleAccountIntoFirebase(account!!)
            }
        }
    }

    private fun signInByEmail(email: String, pass: String) {
        if (isValidEmail(email) && isValidPass(pass)) {
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (mAuth.currentUser!!.isEmailVerified) {
                        saveUser()
                        goToActivityWithoutHistory<MainActivity> {}
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    } else {
                        toast(R.string.error_email_no_verified)
                    }
                } else {
                    errorAuthFirebase(task.exception as FirebaseAuthException)
                }
            }
        } else {
            toast(R.string.error_sigin)
        }
    }

    @SuppressLint("InflateParams")
    private fun showAlertForResetPass(email: String) {
        if (isValidEmail(email)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            builder.setTitle(R.string.alert_dialog_title)
            builder.setMessage(etEmail.text.toString())

            val viewInflated: View = LayoutInflater.from(this).inflate(R.layout.alert_reset_password, null)
            builder.setView(viewInflated)

            builder.setPositiveButton("Reset") { _, _ -> sendResetPass(etEmail.text.toString()) }
            builder.setNegativeButton("Cancel") { _, _ -> }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            toast(R.string.error_email)
        }
    }

    private fun sendResetPass(email: String) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this) {
            toast(R.string.email_reset_password, Toast.LENGTH_LONG)

            goToActivityWithoutHistory<LoginActivity>()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun getGoogleApiClient(): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        return GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun loginByGoogleAccountIntoFirebase(googleAccount: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (mGoogleApiClient.isConnected) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            saveUser()
        }
    }

    private fun saveUser() {
        var flagOk= false

        val user = User(
                mAuth.currentUser!!.uid,
                mAuth.currentUser!!.photoUrl.toString(),
                mAuth.currentUser!!.email!!,
                mAuth.currentUser!!.displayName!!)

        val newUser = HashMap<String, Any>()

        newUser["id"] = user.id
        newUser["image"] = user.image
        newUser["email"] = user.email
        newUser["name"] = user.name

        //Comprobamos que el usuario no exista en la colección users
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", user.id)
                .get()
                .addOnFailureListener{
                    FirebaseFirestore.getInstance().collection("users").add(newUser)
                            .addOnCompleteListener {
                                goToActivityWithoutHistory<MainActivity>()
                            }
                            .addOnFailureListener {
                                toast(R.string.save_new_user)
                            }
                }



        //if (flagOk) {

        //}
    }

    private fun goNewAccount() {
        goToActivity<NewAccountActivity> {}
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
