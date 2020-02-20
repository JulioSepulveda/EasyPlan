package com.juliosepulveda.easyplan.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.juliosepulveda.easyplan.listeners.RecyclerPlanListener
import com.juliosepulveda.easyplan.models.Plan
import com.juliosepulveda.easyplan.utils.inflate
import kotlinx.android.synthetic.main.recycler_plans.view.*
import java.io.File
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.juliosepulveda.easyplan.utils.CircleTransform
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class PlanAdapter (private val plans: List<Plan>, private val listener: RecyclerPlanListener): RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(com.juliosepulveda.easyplan.R.layout.recycler_plans))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(plans[position], listener)

    override fun getItemCount() = plans.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind (plan: Plan, listener: RecyclerPlanListener) = with(itemView){

            val mStorageRef: StorageReference = FirebaseStorage.getInstance().reference
            val fileRef: StorageReference = mStorageRef.child("imagesPlans/${plan.image}.jpg")
            val localFile: File = File.createTempFile("file_", ".jpg")

            fileRef.getFile(localFile)
                    .addOnSuccessListener {

                        Picasso.get()
                                .load(localFile)
                                //.transform(RoundedCornersTransformation (14, 0))
                                .resize(256,256)
                                .centerCrop()
                                .into(ivPlan)
                    }

            val formatter = SimpleDateFormat("dd-MM-yyyy")

            tvTitlePlan.text = plan.title
            tvFechaPlan.text = "${formatter.format(plan.fechaDesde)} a ${formatter.format(plan.fechaHasta)}"
            tvLocationPlan.text = plan.location
            tvNumberPartPlan.text = plan.persons.toString()

            userData(plan.user)

            setOnClickListener{ listener.onClick(plan, adapterPosition) }
        }

        private fun userData(userId: String){

            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnCompleteListener {
                        if (it.result!!.size() > 0){
                            val docUser = it.result!!.documents[0]
                            itemView.tvUserCreated.text = docUser.get("name").toString()

                            Picasso.get()
                                    .load(Uri.parse(docUser.get("image").toString()))
                                    .transform(CircleTransform())
                                    .into(itemView.ivUserImage)
                        }
                    }
        }
    }
}