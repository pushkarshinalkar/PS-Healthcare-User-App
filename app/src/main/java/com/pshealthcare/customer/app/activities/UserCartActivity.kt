package com.pshealthcare.customer.app.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityBlogContentBinding
import com.pshealthcare.customer.app.databinding.ActivityUserCartBinding
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toJson
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toTestsModelList
import com.pshealthcare.customer.app.utils.GenericAdapter

class UserCartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserCartBinding
    private lateinit var cart: MutableList<TestsModel>
    private lateinit var adapter: GenericAdapter<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgCartBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cart = loadCart(this)
        if (cart.isEmpty()){
            binding.imgNothingFound.visibility = View.VISIBLE
            binding.textNothingFound.visibility = View.VISIBLE
        }else {
            binding.imgNothingFound.visibility = View.GONE
            binding.textNothingFound.visibility = View.GONE
        }

        val layoutManager = LinearLayoutManager(this@UserCartActivity)
        binding.rvCheckoutItems.layoutManager = layoutManager


        // Set up the RecyclerView adapter
        adapter = GenericAdapter(
            this,
            R.layout.item_rv_remove_test,
            onBind = { view, data, _ ->
                // Bind your data to the views within the item layout
                view.findViewById<TextView>(R.id.text_rvtest_title).text = data.name
                view.findViewById<TextView>(R.id.text_rvtest_price).text = "₹" + data.price
                val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
                Glide.with(this)
                    .load(R.drawable.ic_beaker_colourful) // Assuming imageUrl is a URL
                    .into(iconimgview)
            },
            onItemClick = { clickedItem ->
                // Remove the clicked item from the cart
                cart.remove(clickedItem)
                saveCart(this, cart)

                // Show a toast message
                Toast.makeText(
                    this,
                    "${clickedItem.name} removed from Cart",
                    Toast.LENGTH_SHORT
                ).show()

                // Calculate the total price
//                var totalItemPrice = 0
//                for (item in cart) {
//                    totalItemPrice += item.price.toInt()
//                }

                // Update the total price text view
//                binding.textTotalprice.text = "₹" + totalItemPrice.toString()

                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged()
            }
        )

        // Set the adapter data and bind it to the RecyclerView
        adapter.data = cart
        binding.rvCheckoutItems.adapter = adapter

        binding.animLoadCheckout.visibility = View.GONE

        // Calculate the total price initially
//        var initialTotalItemPrice = 0
//        for (item in cart) {
//            initialTotalItemPrice += item.price.toInt()
//        }
//        binding.textTotalprice.text = "₹" + initialTotalItemPrice.toString()
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

    private fun saveCart(context: Context, cart: List<TestsModel>) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("cart", cart.toJson())
        editor.apply()
    }
}