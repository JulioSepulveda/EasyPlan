package com.juliosepulveda.easyplan.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.juliosepulveda.easyplan.activities.newPlan.NewPlanActivity
import com.juliosepulveda.easyplan.adapters.PlanAdapter
import com.juliosepulveda.easyplan.application.preferences
import com.juliosepulveda.easyplan.listeners.RecyclerPlanListener
import com.juliosepulveda.easyplan.models.Plan
import com.juliosepulveda.easyplan.R
import com.juliosepulveda.easyplan.utils.toast
import kotlinx.android.synthetic.main.fragment_plans.view.*
import java.util.*
import kotlin.collections.ArrayList

class PlansFragment : Fragment() {

    private lateinit var rootView: View
    private var list = ArrayList<Plan>()

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PlanAdapter
    private val layoutManager by lazy { LinearLayoutManager(context) }

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var plansDB: CollectionReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_plans, container, false)

        setPlansDB()
        setCurrentUser()

        //Control del Recycler View y llenado de los planes
        recycler = rootView.recyclerView as RecyclerView

        setRecyclerView()

        //Cuando se pulse el FAB
        rootView.fab.setOnClickListener { addNewPlan(rootView) }

        return rootView
    }

    private fun setPlansDB() {
        plansDB = store.collection("plans")
    }

    private fun setCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    private fun setRecyclerView() {

        plansDB.whereEqualTo("group", preferences.group)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            list.add(Plan(
                                    document.get("user").toString(),
                                    document.get("title").toString(),
                                    document.get("description").toString(),
                                    document.get("dateFrom") as Date,
                                    document.get("dateTo") as Date,
                                    document.get("persons").toString().toInt(),
                                    document.get("image").toString(),
                                    document.get("location").toString(),
                                    document.get("group").toString()))
                        }
                        recycler.setHasFixedSize(true)
                        recycler.itemAnimator = DefaultItemAnimator()
                        recycler.layoutManager = layoutManager

                        adapter = (PlanAdapter(list, object : RecyclerPlanListener {
                            override fun onClick(plan: Plan, position: Int) {
                                activity!!.toast("Prueba de que entro")
                            }
                        }))
                        recycler.adapter = adapter
                    }
                }
    }

    private fun addNewPlan(rootView: View) {

        val intent = Intent(rootView.context, NewPlanActivity::class.java)
        startActivity(intent)
    }

}
