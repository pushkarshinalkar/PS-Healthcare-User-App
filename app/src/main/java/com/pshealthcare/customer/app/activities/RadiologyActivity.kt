package com.pshealthcare.customer.app.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityRadiologyBinding
import com.pshealthcare.customer.app.models.CategoriesModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions

class RadiologyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRadiologyBinding
    private lateinit var pfun: PredefinedFunctions
    private lateinit var adapter: GenericAdapter<TestsModel>
    private lateinit var cart: MutableList<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRadiologyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pfun = PredefinedFunctions(this)

        cart = loadCart(this) // Load the cart from shared preferences

        getCategoryList()

        val searchView: SearchView = findViewById(R.id.searchView_tests_radiology)

        // Set OnQueryTextFocusChangeListener to handle the transition
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val intent = Intent(this@RadiologyActivity, SearchResultActivity::class.java)
                intent.putExtra("isRadiology",true)

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@RadiologyActivity,
                    searchView,
                    searchView.transitionName
                )

                searchView.clearFocus()

                startActivity(intent, options.toBundle())
            }
        }


        binding.imgSearchBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.rvSearchResult.layoutManager = LinearLayoutManager(this)

    }

    fun saveCart(context: Context, cart: List<TestsModel>) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("cart", cart.toJson())
        editor.apply()
    }

    fun loadCart(context: Context): MutableList<TestsModel> {
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

    fun String.toTestsModelList(): List<TestsModel> {
        val listType = object : TypeToken<List<TestsModel>>() {}.type
        return Gson().fromJson(this, listType)
    }

    private fun getCategoryList() {
        val database = FirebaseDatabase.getInstance()
        val categoryRef = database.reference.child("radiology_categories")

        pfun.fetchDataFromRDatabase(
            CategoriesModel::class.java,
            categoryRef,
            null,
            null,
            onSuccess = {
                var firstChipSelected = false

                it.forEachIndexed { index, category ->

                    if (category.name.isNotEmpty() && category.name != null && category.name != " ") {
                        val chip = Chip(this@RadiologyActivity)
                        chip.chipBackgroundColor = ContextCompat.getColorStateList(
                            this@RadiologyActivity,
                            R.color.bg_selector_chip
                        )
                        chip.setTextColor(
                            ContextCompat.getColorStateList(
                                this@RadiologyActivity,
                                R.color.text_selector_chip
                            )
                        )
                        chip.chipStrokeWidth = 2f
                        chip.text = category.name
                        chip.isCheckable = true
                        chip.chipStrokeColor = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this@RadiologyActivity,
                                R.color.border_selector_chip
                            )
                        )
                        binding.chipGroup.addView(chip)

                        if (category.image != "") {
                            Glide.with(chip)
                                .load(category.image)
                                .placeholder(R.drawable.ic_beaker)
                                .into(object : CustomTarget<Drawable>() {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        transition: Transition<in Drawable>?
                                    ) {
                                        chip.chipIcon = resource
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                        } else {
                            Glide.with(chip)
                                .load(R.drawable.ic_categories)
                                .into(object : CustomTarget<Drawable>() {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        transition: Transition<in Drawable>?
                                    ) {
                                        chip.chipIcon = resource
                                        chip.chipIconTint = ContextCompat.getColorStateList(
                                            this@RadiologyActivity,
                                            R.color.icon_selector_chip
                                        )
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                        }


                        chip.setOnClickListener {
                            if (!firstChipSelected) {
                                chip.isChecked = true
                                firstChipSelected = true
                            }
                            getTestsList(category.name, category.image)
                        }

                        if (index == 0 && !firstChipSelected) {
                            chip.isChecked = true
                            firstChipSelected = true
                            getTestsList(category.name, category.image)
                        }
                    }

                }
            },
            onFailure = {}
        )
    }

    private fun getTestsList(categoryName: String, image: String) {
        val database = FirebaseDatabase.getInstance()
        val testsRef = database.reference.child("radiology_tests").child(categoryName)

        pfun.fetchDataFromRDatabase(
            TestsModel::class.java,
            testsRef,
            null,
            null,
            onSuccess = {

                Log.d("ps_healthcare_debug", "getTestsList: $it")

                if (it.isEmpty()) {
                    binding.imgNothingFound.visibility = View.VISIBLE
                    binding.textNothingFound.visibility = View.VISIBLE
                } else {
                    binding.imgNothingFound.visibility = View.GONE
                    binding.textNothingFound.visibility = View.GONE
                }
                adapter = GenericAdapter<TestsModel>(
                    this@RadiologyActivity,
                    R.layout.item_rv_test,
                    onBind = { view, data, position ->
                        val myData = data

                        if (myData.name.isNotEmpty() && myData.price.isNotEmpty() && myData.name != " ") {
                            view.findViewById<TextView>(R.id.text_rvtest_title).text = myData.name
                            view.findViewById<TextView>(R.id.text_rvtest_price).text =
                                "₹" + myData.price
                            val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
                            val addsub_imageview = view.findViewById<ImageView>(R.id.imageView5)
                            Glide.with(this@RadiologyActivity)
                                .load(image)
                                .placeholder(R.drawable.ic_beaker_colourful)
                                .into(iconimgview)

                            // Check if the test is already in the cart and update the UI accordingly
                            if (cart.contains(data)) {
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(Color.parseColor("#00AEC9"))
                                Glide.with(this@RadiologyActivity)
                                    .load(R.drawable.ic_testtube_subtract)
                                    .placeholder(R.drawable.ic_beaker_colourful)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        this@RadiologyActivity,
                                        R.color.white
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            } else {
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                                Glide.with(this@RadiologyActivity)
                                    .load(R.drawable.ic_testtube_add)
                                    .placeholder(R.drawable.ic_beaker_colourful)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        this@RadiologyActivity,
                                        R.color.black
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            }

                            view.findViewById<CardView>(R.id.card_test_tick).setOnClickListener {
                                if (cart.contains(data)) {
                                    cart.remove(data)
                                    view.findViewById<CardView>(R.id.card_test_tick)
                                        ?.setCardBackgroundColor(Color.WHITE)
                                    Glide.with(this@RadiologyActivity)
                                        .load(R.drawable.ic_testtube_add)
                                        .placeholder(R.drawable.ic_beaker_colourful)
                                        .into(addsub_imageview)
                                    addsub_imageview.setColorFilter(
                                        ContextCompat.getColor(
                                            this@RadiologyActivity,
                                            R.color.black
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                } else {
                                    cart.add(data)
                                    view.findViewById<CardView>(R.id.card_test_tick)
                                        ?.setCardBackgroundColor(Color.parseColor("#00AEC9"))
                                    Glide.with(this@RadiologyActivity)
                                        .load(R.drawable.ic_testtube_subtract)
                                        .placeholder(R.drawable.ic_beaker_colourful)
                                        .into(addsub_imageview)
                                    addsub_imageview.setColorFilter(
                                        ContextCompat.getColor(
                                            this@RadiologyActivity,
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
//                                updateCartUI()
                                saveCart(this@RadiologyActivity, cart)
                            }
                        }

                    },
                    onItemClick = { clickedItem -> }
                )

                adapter.data = it
                binding.rvSearchResult.adapter = adapter

                binding.animLoadRadiology.visibility = View.GONE
            },
            onFailure = {
                Log.d("ps_healthcare_debug", "failure : $it")
            }
        )
    }
}
