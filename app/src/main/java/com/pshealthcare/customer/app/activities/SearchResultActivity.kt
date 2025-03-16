package com.pshealthcare.customer.app.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivitySearchResultBinding
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toTestsModelList
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var pfun: PredefinedFunctions
    private lateinit var adapter: GenericAdapter<TestsModel>
    private lateinit var cart: MutableList<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementEnterTransition =
            TransitionInflater.from(this).inflateTransition(android.R.transition.move)
        window.sharedElementExitTransition =
            TransitionInflater.from(this).inflateTransition(android.R.transition.move)
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)



        cart = loadCart(this) // Load the cart from shared preferences

        binding.imgSearchBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchViewTests.requestFocus()

        binding.buttonSearchCheckout.setOnClickListener {

            val intent = Intent(this@SearchResultActivity, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("cart", ArrayList(cart))
            startActivity(intent)
        }

        binding.searchViewTests.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
//                val intent = Intent(context, SearchResultActivity::class.java)
//                intent.putExtra("searchText", query)
//                startActivity(intent)
                return true  // Set to true since you've handled the submission
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText != null) {
                    searchAndDisplayTests(newText)
                }
                // You can perform actions here if you want to react to each character change
                return true  // Set to true if you've handled the change
            }
        })

        binding.rvSearchResult.layoutManager = LinearLayoutManager(this)

//        lifecycleScope.launch {
//            val searchText = intent.getStringExtra("searchText") ?: ""
//            adapter = GenericAdapter(
//                this@SearchResultActivity,
//                R.layout.item_rv_test,
//                onBind = { view, data, _ ->
//                    val myData = data
//                    view.findViewById<TextView>(R.id.text_rvtest_title).text = myData.name
//                    view.findViewById<TextView>(R.id.text_rvtest_price).text = "₹" + myData.price
//                    val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
//                    val addsub_imageview = view.findViewById<ImageView>(R.id.imageView5)
//                    Glide.with(this@SearchResultActivity)
//                        .load(R.drawable.ic_beaker_colourful)
//                        .into(iconimgview)
//
//                    // Check if the test is already in the cart and update the UI accordingly
//                    if (cart.contains(data)) {
//                        view.findViewById<CardView>(R.id.card_test_tick)?.setCardBackgroundColor(
//                            Color.parseColor("#00AEC9"))
//                        Glide.with(this@SearchResultActivity)
//                            .load(R.drawable.ic_testtube_subtract)
//                            .placeholder(R.drawable.ic_beaker)
//                            .into(addsub_imageview)
//                        addsub_imageview.setColorFilter(ContextCompat.getColor(this@SearchResultActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
//                    } else {
//                        view.findViewById<CardView>(R.id.card_test_tick)?.setCardBackgroundColor(
//                            Color.parseColor("#FFFFFF"))
//                        Glide.with(this@SearchResultActivity)
//                            .load(R.drawable.ic_testtube_add)
//                            .placeholder(R.drawable.ic_beaker)
//                            .into(addsub_imageview)
//                        addsub_imageview.setColorFilter(ContextCompat.getColor(this@SearchResultActivity, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
//                    }
//
//                    view.findViewById<CardView>(R.id.card_test_tick).setOnClickListener {
//                        if (cart.contains(data)) {
//                            cart.remove(data)
//                            view.findViewById<CardView>(R.id.card_test_tick)?.setCardBackgroundColor(
//                                Color.WHITE)
//                            Glide.with(this@SearchResultActivity)
//                                .load(R.drawable.ic_testtube_add)
//                                .placeholder(R.drawable.ic_beaker)
//                                .into(addsub_imageview)
//                            addsub_imageview.setColorFilter(ContextCompat.getColor(this@SearchResultActivity, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
//                        } else {
//                            cart.add(data)
//                            view.findViewById<CardView>(R.id.card_test_tick)?.setCardBackgroundColor(
//                                Color.parseColor("#00AEC9"))
//                            Glide.with(this@SearchResultActivity)
//                                .load(R.drawable.ic_testtube_subtract)
//                                .placeholder(R.drawable.ic_beaker)
//                                .into(addsub_imageview)
//                            addsub_imageview.setColorFilter(ContextCompat.getColor(this@SearchResultActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
//                        }
//                        saveCart(this@SearchResultActivity, cart)
//                    }
//                },
//                onItemClick = { clickedItem ->
//
//                }
//            )
//
//            adapter.data = fetchTests(searchText)
//            binding.rvSearchResult.adapter = adapter
//
//            binding.animLoadSearchResults.visibility = View.GONE
//        }
    }

    fun searchAndDisplayTests(keyword: String) {
        lifecycleScope.launch {
//            val searchText = intent.getStringExtra("searchText") ?: ""
            val searchText = keyword
            adapter = GenericAdapter(
                this@SearchResultActivity,
                R.layout.item_rv_test,
                onBind = { view, data, _ ->
                    val myData = data

                    if (myData.name.isNotEmpty() && myData.price.isNotEmpty() && myData.name != " ") {
                        view.findViewById<TextView>(R.id.text_rvtest_title).text = myData.name
                        view.findViewById<TextView>(R.id.text_rvtest_price).text =
                            "₹" + myData.price
                        val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
                        val addsub_imageview = view.findViewById<ImageView>(R.id.imageView5)
                        Glide.with(this@SearchResultActivity)
                            .load(R.drawable.ic_beaker_colourful)
                            .into(iconimgview)

                        // Check if the test is already in the cart and update the UI accordingly
                        if (cart.contains(data)) {
                            view.findViewById<CardView>(R.id.card_test_tick)
                                ?.setCardBackgroundColor(
                                    Color.parseColor("#00AEC9")
                                )
                            Glide.with(this@SearchResultActivity)
                                .load(R.drawable.ic_testtube_subtract)
                                .into(addsub_imageview)
                            addsub_imageview.setColorFilter(
                                ContextCompat.getColor(
                                    this@SearchResultActivity,
                                    R.color.white
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        } else {
                            view.findViewById<CardView>(R.id.card_test_tick)
                                ?.setCardBackgroundColor(
                                    Color.parseColor("#FFFFFF")
                                )
                            Glide.with(this@SearchResultActivity)
                                .load(R.drawable.ic_testtube_add)
                                .into(addsub_imageview)
                            addsub_imageview.setColorFilter(
                                ContextCompat.getColor(
                                    this@SearchResultActivity,
                                    R.color.black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }

                        view.findViewById<CardView>(R.id.card_test_tick).setOnClickListener {
                            if (cart.contains(data)) {
                                cart.remove(data)
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(
                                        Color.WHITE
                                    )
                                Glide.with(this@SearchResultActivity)
                                    .load(R.drawable.ic_testtube_add)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        this@SearchResultActivity,
                                        R.color.black
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            } else {
                                cart.add(data)
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(
                                        Color.parseColor("#00AEC9")
                                    )
                                Glide.with(this@SearchResultActivity)
                                    .load(R.drawable.ic_testtube_subtract)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        this@SearchResultActivity,
                                        R.color.white
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            }
                            saveCart(this@SearchResultActivity, cart)
                        }
                    }
                },
                onItemClick = { clickedItem ->

                }
            )

            adapter.data = fetchTests(searchText)
            binding.rvSearchResult.adapter = adapter

            binding.animLoadSearchResults.visibility = View.GONE
        }
    }

    suspend fun fetchTests(searchText: String): List<TestsModel> {
        val allTests = mutableListOf<TestsModel>()

        // References to both nodes
        val testsRef = FirebaseDatabase.getInstance().getReference("tests")
        val radiologyTestsRef = FirebaseDatabase.getInstance().getReference("radiology_tests")

        // Fetch data from both references concurrently
        val testsSnapshot = testsRef.get().await()
        val radiologySnapshot = radiologyTestsRef.get().await()

        // Process both snapshots
        processSnapshot(testsSnapshot, searchText, allTests)
        processSnapshot(radiologySnapshot, searchText, allTests)

        // Handle UI based on the result
        if (allTests.isEmpty()) {
            binding.imgNothingFound.visibility = View.VISIBLE
            binding.textNothingFound.visibility = View.VISIBLE
        } else {
            binding.imgNothingFound.visibility = View.GONE
            binding.textNothingFound.visibility = View.GONE
        }

        return allTests
    }

    private fun processSnapshot(
        dataSnapshot: DataSnapshot,
        searchText: String,
        allTests: MutableList<TestsModel>
    ) {
        dataSnapshot.children.forEach { categorySnapshot ->
            categorySnapshot.children.forEach { testSnapshot ->
                val test = testSnapshot.getValue(TestsModel::class.java)
                if (test != null && test.name.lowercase(Locale.ROOT)
                        .contains(searchText.lowercase(Locale.ROOT))
                ) {
                    allTests.add(test)
                }
            }
        }
    }

    fun saveCart(context: Context, cart: List<TestsModel>) {
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

    fun List<TestsModel>.toJson(): String {
        return Gson().toJson(this)
    }
}

