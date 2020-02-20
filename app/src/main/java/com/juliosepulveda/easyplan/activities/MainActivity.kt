package com.juliosepulveda.easyplan.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.*
import com.juliosepulveda.easyplan.activities.login.LoginActivity
import com.juliosepulveda.easyplan.fragments.ChatFragment
import com.juliosepulveda.easyplan.fragments.GroupFragment
import com.juliosepulveda.easyplan.fragments.HuchaFragment
import com.juliosepulveda.easyplan.fragments.PlansFragment
import com.juliosepulveda.easyplan.adapters.PageAdapter
import com.juliosepulveda.easyplan.application.preferences
import com.juliosepulveda.easyplan.models.Group
import com.juliosepulveda.easyplan.R
import com.juliosepulveda.easyplan.utils.goToActivityWithoutHistory
import com.juliosepulveda.easyplan.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //Lista para almacenar los grupos
    private var list: ArrayList<Group> = ArrayList()

    //Para almacenar datos del usuario
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    //Para alamcenar colecciones (grupos)
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var groupDB: CollectionReference

    private var prevBottomSelected: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Incluimos el Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        setUpGroupDB()
        setUpCurrentUser()

        //Gestiona si se ha entrado a traves de un link para añadirle a un grupo
        dynamicLink()

        //Carga de los grupos disponibles para el usuario
        setGroups()

        //Control del movimiento entre los fragment del Bottom Navigation View
        setUpViewPager(getPageAdapter())
        setUpBottomNavigationBar()

    }

    override fun finish() {
        recreate()
    }

    private fun setUpGroupDB() {
        groupDB = store.collection("groups")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    private fun dynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) {
                    if (it != null){
                        val deepLink = it.link
                        toast(deepLink.toString())
                    }
                }
    }

    private fun setGroups() {

        var idTextView = 0
        groupDB.whereArrayContains("users", currentUser.uid)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            list.add(Group(document.get("name").toString(),
                                    document.get("admin").toString())
                            )
                        }

                        //Creamos todos los TextView necesarios para poner los grupos
                        for (grupo in list) {
                            creaBotonesGrupos(grupo.name, ++idTextView)
                        }
                        //Creamos el botón de crear nuevo grupo
                        creaBotonesGrupos(" + ", 0 )

                        //Marcamos el último botón pulsado en la última ejecución
                        val id = preferences.id
                        val textViewOld = findViewById<TextView>(id)
                        if (textViewOld != null)
                            textViewOld.setBackgroundResource(R.drawable.btn_ripple_sigin_dark)
                    }
                }
    }

    private fun creaBotonesGrupos(nombreGrupo : String, idTextView: Int) : TextView{
        //Creamos las propiedades de layout que tendrán los textos de los grupos.
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val textView = TextView(this)

        textView.layoutParams = lp
        textView.setTextColor(resources.getColor(R.color.colorPrimaryText, null))
        textView.gravity = Gravity.CENTER
        textView.setPadding(10,10,10,10)
        textView.typeface = Typeface.MONOSPACE
        textView.setBackgroundResource(R.drawable.btn_ripple_sigin)

        //Le ponemos un layout_margin de 5 dp
        val lm = textView.layoutParams as LinearLayout.LayoutParams
        lm.setMargins(5,5,5,5)
        textView.layoutParams = lm

        textView.text = nombreGrupo
        textView.id = idTextView
        textView.setOnClickListener(ButtonsOnClickListener(this))
        list_groups.addView(textView)

        return textView
    }

    private fun getPageAdapter(): PageAdapter {
        val adapter = PageAdapter(supportFragmentManager)
        adapter.addFragment(PlansFragment())
        adapter.addFragment(ChatFragment())
        adapter.addFragment(GroupFragment())
        adapter.addFragment(HuchaFragment())

        return adapter
    }

    private fun setUpViewPager(adapter: PageAdapter) {
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = adapter.count
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (prevBottomSelected == null) {
                    bottomNavigation.menu.getItem(0).isChecked = false
                } else {
                    prevBottomSelected!!.isChecked = false
                }
                bottomNavigation.menu.getItem(position).isChecked = true
                prevBottomSelected = bottomNavigation.menu.getItem(position)
            }
        })
    }

    private fun setUpBottomNavigationBar() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_plan -> {
                    viewPager.currentItem = 0; true
                }
                R.id.bottom_nav_chat -> {
                    viewPager.currentItem = 1; true
                }
                R.id.bottom_nav_group -> {
                    viewPager.currentItem = 2; true
                }
                R.id.bottom_nav_hucha -> {
                    viewPager.currentItem = 3; true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.contextual_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
          R.id.logOut -> {
              FirebaseAuth.getInstance().signOut()
              goToActivityWithoutHistory<LoginActivity>()
          }
        }
        return super.onOptionsItemSelected(item)
    }

    class ButtonsOnClickListener constructor (private val activity: Activity) : View.OnClickListener{

        override fun onClick(v: View?) {
            val textView: TextView = v as TextView

            //Si es el más abrimos la activity para crear un nuevo grupo
            //si no cogemos el botón antiguo para deseleccionarlo
            if (textView.id == 0){
                val intent = Intent(activity, NewGroupActivity::class.java)
                startActivity(activity, intent, null)
            }else{
                val id = preferences.id
                val textViewOld = activity.findViewById<TextView>(id)

                //Deseleccionamos el botón antiguo y lo guardamos en el sharedPreferences
                textViewOld.setBackgroundResource(R.drawable.btn_ripple_sigin)

                //Seleccionamos el botón pulsado y lo guardamos en el sharedPreferences
                preferences.group = textView.text.toString()
                preferences.id = textView.id
                textView.setBackgroundResource(R.drawable.btn_ripple_sigin_dark)

                activity.finish()



            }
        }

    }

}
