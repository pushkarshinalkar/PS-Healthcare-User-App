package com.pshealthcare.customer.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.FragmentRecordsBinding
import com.pshealthcare.customer.app.models.OrdersModel
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import com.pshealthcare.customer.app.utils.RecordsAdapter
import kotlin.math.sign

class RecordsFrag : Fragment() {

    private lateinit var binding: FragmentRecordsBinding
    private lateinit var pfun: PredefinedFunctions
    private lateinit var adapter: RecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pfun = context?.let { PredefinedFunctions(it) }!!

        val databaseRef = FirebaseDatabase.getInstance().reference.child("orders")
        val layoutManager = LinearLayoutManager(view.context)
        binding.rvRecords.layoutManager = layoutManager
        val userid = pfun.getSharedPrefData("primaryUserId","")!!
        pfun.fetchDataFromRDatabase(
            OrdersModel::class.java,
            databaseRef,
            userid,
            "userid",
            onSuccess = { ordersList ->
                if (ordersList.isEmpty()){
                    binding.imgNothingFound.visibility = View.VISIBLE
                    binding.textNothingFound.visibility = View.VISIBLE
                    pfun.saveSharedPrefData("no_of_orders","--")
                    pfun.saveSharedPrefData("last_order","--")
                }else {
                    binding.imgNothingFound.visibility = View.GONE
                    binding.textNothingFound.visibility = View.GONE
                    val mutableOrdersList = ordersList.toMutableList()
                    pfun.saveSharedPrefData("no_of_orders",mutableOrdersList.size.toString())
                    pfun.saveSharedPrefData("last_order",mutableOrdersList[mutableOrdersList.size-1].date)
                    adapter = RecordsAdapter(requireContext(), mutableOrdersList)
                    binding.rvRecords.adapter = adapter
                }


                binding.animLoadRecordFrag.visibility = View.GONE
            },
            onFailure = {
                // Handle failure
            }
        )
    }



}

