package com.pshealthcare.customer.app.fragments

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.CheckoutActivity
import com.pshealthcare.customer.app.activities.SignInActivity
import com.pshealthcare.customer.app.activities.SearchResultActivity
import com.pshealthcare.customer.app.databinding.FragmentTestsBinding
import com.pshealthcare.customer.app.models.CategoriesModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toJson
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toTestsModelList
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions

class TestsFrag : Fragment() {

    private lateinit var binding: FragmentTestsBinding
    private lateinit var pfun: PredefinedFunctions

    private lateinit var cart: MutableList<TestsModel>
    private lateinit var adapter: GenericAdapter<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cart = loadCart(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pfun = context?.let { PredefinedFunctions(it) }!!

        updateCartUI()

        getCategoryList()

        val layoutManager = LinearLayoutManager(view.context)
        binding.rvTestslist.layoutManager = layoutManager

        binding.btnCheckout.setOnClickListener {

            val userId = pfun.getSharedPrefData("primaryUserId", "")!!
            if (userId == "") {

                showInfoDialog("Login Required !", "Sign Up or Login to explore all the features",
                    R.drawable.ic_login,
                    onContinueClick = {
                        val intent =
                            Intent(requireContext(), SignInActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                        it.dismiss()
                    }
                )

            } else {

                if (cart.isEmpty()) {
                    showInfoDialog("Cart is Empty!", "Add atleast one test to move forward",
                        R.drawable.ic_main_icon,
                        onContinueClick = {
                            it.dismiss()
                        }
                    )
                } else {
                    val intent = Intent(context, CheckoutActivity::class.java)
                    intent.putParcelableArrayListExtra("cart", ArrayList(cart))
                    startActivity(intent)
                }


            }

        }

        binding.imgDelete.setOnClickListener {
            cart.clear()
            updateCartUI()
            saveCart(requireContext(), cart)
            adapter.notifyDataSetChanged() // Refresh the RecyclerView to clear selection state
        }

        val searchView: SearchView = view.findViewById(R.id.searchView_tests_tests)

        // Set OnQueryTextFocusChangeListener to handle the transition
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val intent = Intent(context, SearchResultActivity::class.java)

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    searchView,
                    searchView.transitionName
                )

                searchView.clearFocus()

                startActivity(intent, options.toBundle())
            }
        }
    }

    private fun getCategoryList() {
        val database = FirebaseDatabase.getInstance()
        val categoryRef = database.reference.child("categories")

        pfun.fetchDataFromRDatabase(
            CategoriesModel::class.java,
            categoryRef,
            null,
            null,
            onSuccess = {
                var firstChipSelected = false

                it.forEachIndexed { index, category ->

                    if (category.name.isNotEmpty() && category.name != null && category.name != " ") {
                        val chip = Chip(requireContext())
                        chip.chipBackgroundColor = ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.bg_selector_chip
                        )
                        chip.setTextColor(
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.text_selector_chip
                            )
                        )
                        chip.chipStrokeWidth = 2f
                        chip.text = category.name
                        chip.isCheckable = true
                        chip.chipStrokeColor = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
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
                                            requireContext(),
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
        val testsRef = database.reference.child("tests").child(categoryName)

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
                    requireContext(),
                    R.layout.item_rv_test,
                    onBind = { view, data, position ->
                        val myData = data

                        if (myData.name.isNotEmpty() && myData.price.isNotEmpty() && myData.name != " ") {
                            view.findViewById<TextView>(R.id.text_rvtest_title).text = myData.name
                            view.findViewById<TextView>(R.id.text_rvtest_price).text =
                                "₹" + myData.price
                            val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
                            val addsub_imageview = view.findViewById<ImageView>(R.id.imageView5)
                            Glide.with(requireContext())
                                .load(image)
                                .placeholder(R.drawable.ic_beaker_colourful)
                                .into(iconimgview)

                            // Check if the test is already in the cart and update the UI accordingly
                            if (cart.contains(data)) {
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(Color.parseColor("#00AEC9"))
                                Glide.with(requireContext())
                                    .load(R.drawable.ic_testtube_subtract)
                                    .placeholder(R.drawable.ic_beaker_colourful)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            } else {
                                view.findViewById<CardView>(R.id.card_test_tick)
                                    ?.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                                Glide.with(requireContext())
                                    .load(R.drawable.ic_testtube_add)
                                    .placeholder(R.drawable.ic_beaker_colourful)
                                    .into(addsub_imageview)
                                addsub_imageview.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.black
                                    ), android.graphics.PorterDuff.Mode.SRC_IN
                                )
                            }

                            view.findViewById<CardView>(R.id.card_test_tick).setOnClickListener {
                                if (cart.contains(data)) {
                                    cart.remove(data)
                                    view.findViewById<CardView>(R.id.card_test_tick)
                                        ?.setCardBackgroundColor(Color.WHITE)
                                    Glide.with(requireContext())
                                        .load(R.drawable.ic_testtube_add)
                                        .placeholder(R.drawable.ic_beaker_colourful)
                                        .into(addsub_imageview)
                                    addsub_imageview.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.black
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                } else {
                                    cart.add(data)
                                    view.findViewById<CardView>(R.id.card_test_tick)
                                        ?.setCardBackgroundColor(Color.parseColor("#00AEC9"))
                                    Glide.with(requireContext())
                                        .load(R.drawable.ic_testtube_subtract)
                                        .placeholder(R.drawable.ic_beaker_colourful)
                                        .into(addsub_imageview)
                                    addsub_imageview.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                updateCartUI()
                                saveCart(requireContext(), cart)
                            }
                        }

                    },
                    onItemClick = { clickedItem -> }
                )

                adapter.data = it
                binding.rvTestslist.adapter = adapter

                binding.animLoadTestsFrag.visibility = View.GONE
            },
            onFailure = {
                Log.d("ps_healthcare_debug", "failure : $it")
            }
        )
    }

    private fun updateCartUI() {
        binding.textNoOftestsCart.text = cart.size.toString()
        var totalItemPrice = 0
        for (i in cart) {
            totalItemPrice += i.price.toInt()
        }
        binding.textPriceOftestsCart.text = "₹" + totalItemPrice.toString()
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

    private fun showInfoDialog(
        titleText: String,
        description: String,
        imgDrawable: Int,
        onContinueClick: ((AlertDialog) -> Unit)
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_info, null)
        val textDesrip = dialogView.findViewById<TextView>(R.id.text_info_description)
        val textSuccFail = dialogView.findViewById<TextView>(R.id.text_succ_or_fail)
        val imgDialog = dialogView.findViewById<ImageView>(R.id.img_info_dialog)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        Glide.with(this)
            .load(imgDrawable)
            .into(imgDialog)


        textSuccFail.text = titleText
        textDesrip.text = description

        val builder = this.let { AlertDialog.Builder(requireContext()) }
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        // Set dialog window width and position
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_curveshape)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),  // Set width to 85% of screen width
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        textDesrip.text = description
        btnOk.setOnClickListener {
            onContinueClick.invoke(dialog)
        }

    }

}


