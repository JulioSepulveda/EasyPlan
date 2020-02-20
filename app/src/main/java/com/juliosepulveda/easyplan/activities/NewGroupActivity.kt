package com.juliosepulveda.easyplan.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.juliosepulveda.easyplan.R.*
import com.juliosepulveda.easyplan.utils.ACTIVITY_SEND_LINKS
import com.juliosepulveda.easyplan.utils.goToActivityWithoutHistory
import com.juliosepulveda.easyplan.utils.toast
import kotlinx.android.synthetic.main.activity_new_group.*

class NewGroupActivity : AppCompatActivity() {

    //Para almacenar datos del usuario
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    //private lateinit var  shortLinkTask: Uri

    //Para alamcenar colecciones (grupos)
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var groupDBRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_new_group)

        setUpGroupDB()
        setUpCurrentUser()

        btnNewGroup.setOnClickListener { addNewgroup() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACTIVITY_SEND_LINKS -> saveGroup()
        }
    }

    private fun setUpGroupDB() {
        groupDBRef = store.collection("groups")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    private fun addNewgroup() {
        if ("" != etNameGroup.text.toString()) {
            sendLinks()
        } else {
            toast(string.error_no_name)
        }
    }

    private fun sendLinks() {
        val group = etNameGroup.text.toString()
        val appCode = "https://easyplan.page.link"
        val deepLink = Uri.parse("https://easyplan.page.link/addplan?group=${group}")
        val titleLink = "EasyPlan - Grupo: ${etNameGroup.text}"
        val descLink = "Has sido añadido al grupo ${etNameGroup.text} de la app EasyPlan. Pulsa en el link para acceder"


        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(deepLink)
                .setDomainUriPrefix(appCode)
                .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(titleLink)
                        .setDescription(descLink)
                        .build())
                .buildShortDynamicLink()
                .addOnSuccessListener { result ->
                    val shortLinkTask = result.shortLink

                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_TEXT,  shortLinkTask.toString())
                    startActivityForResult(i, ACTIVITY_SEND_LINKS)

                }
    }

    private fun saveGroup() {
        val newGroup = HashMap<String, Any>()

        newGroup["name"] = etNameGroup.text.toString().trim()
        newGroup["admin"] = currentUser.uid
        newGroup["users"] = arrayListOf(currentUser.uid)

        groupDBRef.add(newGroup)
                .addOnCompleteListener {
                    goToActivityWithoutHistory<MainActivity>()
                }
                .addOnFailureListener {
                    toast(string.save_new_group)
                }
    }
}