package com.pshealthcare.customer.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.models.MembersModel

class MembersAdapter(
    private val context: Context,
    private val data: MutableList<MembersModel>
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_familymembers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = data[position]
        holder.nameTextView.text = member.name
        holder.relationTextView.text = member.relation

        holder.cardView.setOnLongClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Delete Member")
                setMessage("Are you sure you want to delete this member?")
                setPositiveButton("Yes") { _, _ ->
                    val databaseRef = FirebaseDatabase.getInstance().reference.child("family_members").child(member.name)
                    databaseRef.removeValue().addOnSuccessListener {
                        data.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Member deleted", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to delete member", Toast.LENGTH_SHORT).show()
                    }
                }
                setNegativeButton("No", null)
            }.show()
            true
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_membername)
        val relationTextView: TextView = itemView.findViewById(R.id.text_memberrelation)
        val cardView: CardView = itemView.findViewById(R.id.card_familymember)
    }
}
