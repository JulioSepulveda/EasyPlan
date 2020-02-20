package com.juliosepulveda.easyplan.utils

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthException
import java.util.regex.Pattern
import com.juliosepulveda.easyplan.R

/*Toast*/
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, duration).show()
fun Activity.toast(resourceId: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, resourceId, duration).show()

/* Intent dejando historial*/
inline fun <reified T : Activity>Activity.goToActivity(noinline init: Intent.() -> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.init()
    startActivity(intent)
}

/* Intent sin dejar historial*/
inline fun <reified T : Activity>Activity.goToActivityWithoutHistory(noinline init: Intent.() -> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.init()
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

/*Control de editText (email, password, confirmPassword)*/
fun EditText.validate(validation: (String) -> Unit){
    this.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            validation(editable.toString())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
}

/*Valida que el email introducido tiene formato de email*/
fun isValidEmail(email: String): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

/*Valida que el contraseña sea del formato */
fun isValidPass(pass: String): Boolean {
    //Necesita contener: 1 Numero / 1 Minúscula / 1 Mayuscula / Mínimo 4 caractéres
    val passPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$"
    val pattern = Pattern.compile(passPattern)
    return pattern.matcher(pass).matches()
}

/*Valida que haya escrito la confirmación de la contraseña igual que la contraseña*/
fun isValidConfirmPass(pass: String, confirmPass: String): Boolean{
    return pass == confirmPass
}

/*Personaliza los mensajes de error de la autentificación de Firebase*/
fun Activity.errorAuthFirebase (error: FirebaseAuthException): Unit = when (error.errorCode){
    "ERROR_EMAIL_ALREADY_IN_USE" -> toast(R.string.error_email_already_in_use)
    "ERROR_WRONG_PASSWORD" -> toast(R.string.error_wrong_password)
    else -> toast(R.string.error_default)
}

/*Infla los adaptadores*/
fun ViewGroup.inflate(layoutId: Int) = LayoutInflater.from(context).inflate(layoutId,this,false)!!
