package com.pshealthcare.customer.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityViewMembersBinding
import com.pshealthcare.customer.app.models.MembersModel
import com.pshealthcare.customer.app.utils.MembersAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions

class ViewMembersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewMembersBinding
    private lateinit var pfun: PredefinedFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pfun = PredefinedFunctions(this)

        binding.rvFamilyMembers.layoutManager = GridLayoutManager(this@ViewMembersActivity, 2)

        val userId = pfun.getSharedPrefData("primaryUserId","")!!
        val databaseRef = FirebaseDatabase.getInstance().reference.child("family_members")

        pfun.fetchDataFromRDatabase(
            MembersModel::class.java,
            databaseRef,
            null,
            null,
            onSuccess = { membersList ->

                // Filter the list to include only members with matching userId
                val filteredMembersList = membersList.filter { it.userid == userId }.toMutableList()

                val adapter = MembersAdapter(this@ViewMembersActivity, filteredMembersList)

                if (filteredMembersList.isEmpty()){
                    binding.imgNothingFound.visibility = View.VISIBLE
                    binding.textNothingFound.visibility = View.VISIBLE
                }else {
                    binding.imgNothingFound.visibility = View.GONE
                    binding.textNothingFound.visibility = View.GONE
                }

                binding.rvFamilyMembers.adapter = adapter
                binding.animLoadViewmembers.visibility = View.GONE

            },
            onFailure = {
                // Handle failure
            }
        )

        binding.btnAddNewmember.setOnClickListener {
            val intent = Intent(this@ViewMembersActivity, AddFamilyActivity::class.java)
            startActivity(intent)
        }

        binding.imgMembersBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
