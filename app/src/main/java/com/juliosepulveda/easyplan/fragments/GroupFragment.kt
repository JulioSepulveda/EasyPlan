package com.juliosepulveda.easyplan.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.juliosepulveda.easyplan.activities.NewGroupActivity
import com.juliosepulveda.easyplan.R
import kotlinx.android.synthetic.main.fragment_group.view.*

class GroupFragment : Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_group, container, false)


        rootView.fab.setOnClickListener { addNewGroup() }

        return rootView
    }

    private fun addNewGroup() {
        val intent = Intent(rootView.context, NewGroupActivity::class.java)
        startActivity(intent)
    }

}
