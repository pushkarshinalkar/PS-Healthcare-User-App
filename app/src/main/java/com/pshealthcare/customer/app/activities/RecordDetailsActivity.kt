package com.pshealthcare.customer.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityRecordDetailsBinding
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.utils.GenericAdapter

class RecordDetailsActivity : AppCompatActivity() {

    private lateinit var recordTestList: MutableList<TestsModel>
    private lateinit var binding: ActivityRecordDetailsBinding
    private lateinit var adapter: GenericAdapter<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgOrderdetailsBackbtn.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()
        }

        val layoutManager = LinearLayoutManager(this@RecordDetailsActivity)
        binding.rvOrderdetails.layoutManager = layoutManager

        val recordlist: MutableList<TestsModel> = intent.getParcelableArrayListExtra("testlist") ?: mutableListOf()
        recordTestList = recordlist.filterNotNull().toMutableList()

        Log.e("Record", "onCreate: " + recordTestList.toString(), )


        // Set up the RecyclerView adapter
        adapter = GenericAdapter<TestsModel>(
            this,
            R.layout.rv_item_searchtest,
            onBind = { view, data, _ ->

                if (data.name.isNotEmpty() && data.price.isNotEmpty() && data.name != " ") {
                    // Bind your data to the views within the item layout
                    view.findViewById<TextView>(R.id.text_rvtest_title_search).text = data.name
                    view.findViewById<TextView>(R.id.text_rvtest_price_search).text = data.price
                }
            },
            onItemClick = { clickedItem ->

            }

        )

        // Set the adapter data and bind it to the RecyclerView
        adapter.data = recordTestList
        binding.rvOrderdetails.adapter = adapter
    }
}