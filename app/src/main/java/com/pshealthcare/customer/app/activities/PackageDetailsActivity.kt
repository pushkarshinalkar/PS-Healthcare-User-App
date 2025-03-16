package com.pshealthcare.customer.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityPackageDetailsBinding
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toJson
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toTestsModelList
import com.pshealthcare.customer.app.utils.GenericAdapter

class PackageDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPackageDetailsBinding
    private lateinit var packageTestsList: MutableList<TestsModel>
    private lateinit var adapter: GenericAdapter<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPackageDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgPackagedetailsBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAddTestsPackage.setOnClickListener {

            if (packageTestsList.isNotEmpty()){
                addPackageTestsToCart()
            }
        }

        val layoutManager = LinearLayoutManager(this@PackageDetailsActivity)
        binding.rvPackagedetailsTestlist.layoutManager = layoutManager
        packageTestsList = intent.getParcelableArrayListExtra("testlist") ?: mutableListOf()

        if (packageTestsList.isEmpty()){
            binding.btnAddTestsPackage.visibility = View.GONE
            binding.imgNothingFound.visibility = View.VISIBLE
            binding.textNothingFound.visibility = View.VISIBLE
        }

        // Set up the RecyclerView adapter
        adapter = GenericAdapter<TestsModel>(
            this,
            R.layout.rv_item_searchtest,
            onBind = { view, data, _ ->

                if (data.name.isNotEmpty() && data.price.isNotEmpty() && data.name != " ") {
                    // Bind your data to the views within the item layout
                    view.findViewById<TextView>(R.id.text_rvtest_title_search).text = data.name
                    view.findViewById<TextView>(R.id.text_rvtest_price_search).text = data.price
                    val iconimgview = view.findViewById<ImageView>(R.id.img_item_test_search)
                    Glide.with(this)
                        .load(R.drawable.ic_beaker_colourful) // Assuming imageUrl is a URL
                        .into(iconimgview)
                }
            },
            onItemClick = { clickedItem ->

            }
        )

        // Set the adapter data and bind it to the RecyclerView
        adapter.data = packageTestsList
        binding.rvPackagedetailsTestlist.adapter = adapter

        binding.animLoadPackDetails.visibility = View.GONE
    }

    private fun addPackageTestsToCart() {
        // Load the existing cart
        val tests_package = packageTestsList

        // Add all tests from packageTestsList to the cart
        savePackage(this, tests_package)

        if (tests_package.isEmpty()) {
            Toast.makeText(this@PackageDetailsActivity, "Package is Empty", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this@PackageDetailsActivity, CheckoutActivity::class.java)
            intent.putExtra("isPackage",true)
            intent.putParcelableArrayListExtra("tests_package", ArrayList(tests_package))
            startActivity(intent)
        }
    }

    private fun saveCart(context: Context, cart: List<TestsModel>) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("cart", cart.toJson())
        editor.apply()
    }

    private fun loadCart(context: Context): MutableList<TestsModel> {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("cart", "")
        return if (!json.isNullOrEmpty()) {
            json.toTestsModelList().toMutableList()
        } else {
            mutableListOf()
        }
    }

    private fun savePackage(context: Context, cart: List<TestsModel>) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("tests_package", cart.toJson())
        editor.apply()
    }

    private fun loadPackage(context: Context): MutableList<TestsModel> {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("tests_package", "")
        return if (!json.isNullOrEmpty()) {
            json.toTestsModelList().toMutableList()
        } else {
            mutableListOf()
        }
    }



    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
