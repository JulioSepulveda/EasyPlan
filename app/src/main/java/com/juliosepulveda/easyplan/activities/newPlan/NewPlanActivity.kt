package com.juliosepulveda.easyplan.activities.newPlan

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.juliosepulveda.easyplan.R
import kotlinx.android.synthetic.main.activity_new_plan.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.juliosepulveda.easyplan.activities.MainActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.juliosepulveda.easyplan.application.preferences
import com.juliosepulveda.easyplan.models.Plan
import com.juliosepulveda.easyplan.utils.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class NewPlanActivity : AppCompatActivity() {

    //Para almacenar datos del usuario
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    //Para alamcenar colecciones (planes)
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var planDBRef: CollectionReference

    //Para almacenar los archivos de imagenes
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    private var imageLoad: Boolean = false
    private var imageFile: Uri? = null

    private var fechaDesde: Date? = null
    private var fechaHasta: Date? = null

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_plan)

        setUpPlanDB()
        setUpCurrentUser()

        ivNewPlan.setOnClickListener { getPictureFromInternalMemorie() }
        ivRotate.setOnClickListener { rotatePicture() }
        ivLocation.setOnClickListener { showMapsToLocation() }
        ivCalendar.setOnClickListener { showCalendar() }
        btnNewPlan.setOnClickListener { setNewPlan() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            ACTIVITY_SELECT_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val extras = data!!.data

                    ivNewPlan.setPadding(0,0,0,0)

                    Picasso.get()
                            .load(extras)
                            .transform(RoundedCornersTransformation(4, 0))
                            .resize (256, 256)
                            //.fit()
                            .centerCrop ()
                            .into(ivNewPlan)

                    ivNewPlan.imageTintList =  ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)).withAlpha(0)

                    imageFile = extras
                    imageLoad = true
                }
            }
            ACTIVITY_SELECT_DATE_DESDE -> {
                if (resultCode == Activity.RESULT_OK) {
                    fechaDesde = Date(data!!.getLongExtra("fecha", 0))
                    val intentHasta = Intent(this, CalendarActivity::class.java)
                    intentHasta.putExtra("tipo", ACTIVITY_SELECT_DATE_HASTA)
                    intentHasta.putExtra("desde", fechaDesde!!.time)
                    startActivityForResult(intentHasta, ACTIVITY_SELECT_DATE_HASTA)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else
                    tvDateNewPlan.text = ""
            }
            ACTIVITY_SELECT_DATE_HASTA -> {
                if (resultCode == Activity.RESULT_OK) {
                    fechaHasta = Date(data!!.getLongExtra("fecha", 0))

                    val formatter = SimpleDateFormat("dd-MM-yyyy")
                    tvDateNewPlan.text = "${formatter.format(fechaDesde)} a ${formatter.format(fechaHasta)}"
                }
                else
                    tvDateNewPlan.text = ""
            }
            ACTIVITY_MAP -> {
                if (resultCode == Activity.RESULT_OK) {
                    tvLocationNewPlan.text = data!!.getStringExtra("location")
                }
            }
        }
    }

    private fun setUpPlanDB() {
        planDBRef = store.collection("plans")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPictureFromInternalMemorie() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openStorageGallery()
        }else{
            requestPermissions(Array(1){Manifest.permission.READ_EXTERNAL_STORAGE},requestReadExternalStoragePermission )
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openStorageGallery()
            }
        }
    }

    private fun openStorageGallery(){
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE)
    }

    private fun rotatePicture() {
        if (imageLoad) {
            ivNewPlan.rotation = ivNewPlan.rotation + 90f
        }
    }

    private fun showMapsToLocation() {
        getPermissions()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("localizacion", tvLocationNewPlan.text)
            startActivityForResult(intent, ACTIVITY_MAP)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            toast(R.string.gps_permiso, Toast.LENGTH_LONG)
        }
    }

    private fun showCalendar() {
        val intentDesde = Intent(this, CalendarActivity::class.java)
        intentDesde.putExtra("tipo", ACTIVITY_SELECT_DATE_DESDE)
        startActivityForResult(intentDesde, ACTIVITY_SELECT_DATE_DESDE)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun getPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestFineLocationPermission)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), requestCoarseLocationPermission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNewPlan() {
        if (compruebaCampos()) {
            val fileName = "${etTitleNewPlan.text}_${currentUser.uid}_${LocalDateTime.now()}"

            val file = imageFile!!
            val fileRef: StorageReference = storageRef.child("imagesPlans/$fileName.jpg")

            fileRef.putFile(file)

            val plan = Plan(currentUser.uid,
                    etTitleNewPlan.text.toString(),
                    etDescNewPlan.text.toString(),
                    fechaDesde!!,
                    fechaHasta!!,
                    0,
                    fileName,
                    tvLocationNewPlan.text.toString(),
                    preferences.group)

            savePlan(plan)
        } else {
            toast(R.string.error_data_new_plan)
        }
    }

    private fun savePlan(plan: Plan) {
        val newPlan = HashMap<String, Any>()

        newPlan["user"] = plan.user
        newPlan["title"] = plan.title
        newPlan["description"] = plan.desc
        newPlan["dateFrom"] = plan.fechaDesde
        newPlan["dateTo"] = plan.fechaHasta
        newPlan["persons"] = plan.persons
        newPlan["image"] = plan.image
        newPlan["location"] = plan.location
        newPlan["group"] = plan.group

        planDBRef.add(newPlan)
                .addOnCompleteListener {
                    goToActivityWithoutHistory<MainActivity>()
                }
                .addOnFailureListener {
                    toast(R.string.save_new_plan)
                }
    }

    private fun compruebaCampos(): Boolean {
        return when {
            "" == etTitleNewPlan.text.toString() -> false
            "" == etDescNewPlan.text.toString() -> false
            "" == tvLocationNewPlan.text -> false
            "" == fechaDesde.toString() -> false
            "" == fechaHasta.toString() -> false
            "" == etPrice.text.toString() -> false
            else -> imageFile != null
        }
    }
}

