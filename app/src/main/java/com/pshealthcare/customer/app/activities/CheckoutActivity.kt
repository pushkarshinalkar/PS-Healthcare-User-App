package com.pshealthcare.customer.app.activities

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityCheckoutBinding
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.utils.GenericAdapter


class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var cart: MutableList<TestsModel>
    private lateinit var adapter: GenericAdapter<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var item_layout = R.layout.item_rv_remove_test
        cart = emptyList<TestsModel>().toMutableList()
        // Set default chip selection to "No" and hide input fields
        binding.chipNoDr.isChecked = true
        toggleInputFields(false)

        // Set up chip group listener to show/hide input fields
        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_yes_dr -> toggleInputFields(true)
                R.id.chip_no_dr -> toggleInputFields(false)
            }
        }

        val layoutManager = LinearLayoutManager(this@CheckoutActivity)
        binding.rvCheckoutItems.layoutManager = layoutManager

        binding.btnNext.setOnClickListener {

            // Get input values
            val drName = if (binding.chipYesDr.isChecked) binding.textNameDr.text.toString() else ""
            val specialization = if (binding.chipYesDr.isChecked) binding.textSpecilizationDr.text.toString() else ""
            val regNo = if (binding.chipYesDr.isChecked) binding.textRegistrationDr.text.toString() else ""
            val mobile = if (binding.chipYesDr.isChecked) binding.textMobileDr.text.toString() else ""

            if (cart.isEmpty()){
                Snackbar.make(binding.root, "Cart is Empty !", Snackbar.LENGTH_SHORT).setBackgroundTint(
                    Color.parseColor("#E74A4A")).setTextColor(Color.parseColor("#FFFFFF")).show()
            }else {

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    checkAlarmPerm()
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Toast.makeText(
                            this@CheckoutActivity,
                            "Device does not support Reminders",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val intent = Intent(this@CheckoutActivity, BookingInfoActivity::class.java)
                    intent.putParcelableArrayListExtra("cart", ArrayList(cart))



                    // Put input values into intent
                    intent.putExtra("drName", drName)
                    intent.putExtra("specialization", specialization)
                    intent.putExtra("regNo", regNo)
                    intent.putExtra("mobile", mobile)

                    startActivity(intent)
                }
            }

        }


        binding.imgCheckoutBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get the cart from the intent

        if (intent.getBooleanExtra("isPackage",false)){

            cart = intent.getParcelableArrayListExtra("tests_package") ?: mutableListOf()
            item_layout = R.layout.rv_item_searchtest
        }else {

            cart = intent.getParcelableArrayListExtra("cart") ?: mutableListOf()
        }
        if (cart.isEmpty()){
            binding.imgNothingFound.visibility = View.VISIBLE
            binding.textNothingFound.visibility = View.VISIBLE
        }else {
            binding.imgNothingFound.visibility = View.GONE
            binding.textNothingFound.visibility = View.GONE
        }

        Log.d("pshealthcare_debug", cart.toString())

        // Set up the RecyclerView adapter
        adapter = GenericAdapter<TestsModel>(
            this,
            item_layout,
            onBind = { view, data, _ ->
                // Bind your data to the views within the item layout

                if (intent.getBooleanExtra("isPackage",false)){
                    view.findViewById<TextView>(R.id.text_rvtest_title_search).text = data.name
                    view.findViewById<TextView>(R.id.text_rvtest_price_search).text = "₹" + data.price
                    val iconimgview = view.findViewById<ImageView>(R.id.img_item_test_search)
                    Glide.with(this)
                        .load(R.drawable.ic_beaker_colourful) // Assuming imageUrl is a URL
                        .into(iconimgview)
                }else {
                    view.findViewById<TextView>(R.id.text_rvtest_title).text = data.name
                    view.findViewById<TextView>(R.id.text_rvtest_price).text = "₹" + data.price
                    val iconimgview = view.findViewById<ImageView>(R.id.img_item_test)
                    Glide.with(this)
                        .load(R.drawable.ic_beaker_colourful) // Assuming imageUrl is a URL
                        .into(iconimgview)
                }

            },
            onItemClick = { clickedItem ->

                if (!intent.getBooleanExtra("isPackage",false)) {
                    // Remove the clicked item from the cart
                    cart.remove(clickedItem)

                    // Show a toast message
                    Toast.makeText(
                        this,
                        "${clickedItem.name} removed from Cart",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Calculate the total price
                    var totalItemPrice = 0
                    for (item in cart) {
                        totalItemPrice += item.price.toInt()
                    }

                    // Update the total price text view
                    binding.textTotalprice.text = "₹" + totalItemPrice.toString()

                    // Notify the adapter that the dataset has changed
                    adapter.notifyDataSetChanged()
                }
            }
        )

        // Set the adapter data and bind it to the RecyclerView
        adapter.data = cart
        binding.rvCheckoutItems.adapter = adapter

        binding.animLoadCheckout.visibility = View.GONE

        // Calculate the total price initially
        var initialTotalItemPrice = 0
        for (item in cart) {
            initialTotalItemPrice += item.price.toInt()
        }
        binding.textTotalprice.text = "₹" + initialTotalItemPrice.toString()
    }

    private fun toggleInputFields(show: Boolean) {
        binding.textInputLayoutNameDr.visibility = if (show) View.VISIBLE else View.GONE
        binding.textInputLayoutSpecilizationDr.visibility = if (show) View.VISIBLE else View.GONE
        binding.textInputLayoutRegistrationDr.visibility = if (show) View.VISIBLE else View.GONE
        binding.textInputLayoutMobileDr.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun checkAlarmPerm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Check and add SCHEDULE_EXACT_ALARM permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {

            showPermDialog(
                "PS Healthcare needs Alarm Access so that we can show you reminders. Tap Continue > PS Healthcare and allow permission",
                R.drawable.ic_clock,
                onContinueClick = { dialog ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    dialog.dismiss()
                },
                onCancelClick = { dialog ->
                    dialog.dismiss()
                })
        }
    }

    private fun showPermDialog(
        description: String,
        img_perm: Int,
        onContinueClick: ((AlertDialog) -> Unit),
        onCancelClick: ((AlertDialog) -> Unit)
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_permissions, null)
        val textContinue = dialogView.findViewById<TextView>(R.id.text_perm_continue)
        val textNotnow = dialogView.findViewById<TextView>(R.id.text_perm_dismiss)
        val textDescription = dialogView.findViewById<TextView>(R.id.text_permission_description)
        val imgPermission = dialogView.findViewById<ImageView>(R.id.img_permission)

        Glide.with(this)
            .load(img_perm)
            .placeholder(R.drawable.ic_beaker)
            .into(imgPermission)

        val builder = this.let { AlertDialog.Builder(it) }
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

        textDescription.text = description
        textContinue.setOnClickListener {
            onContinueClick.invoke(dialog)
        }

        textNotnow.setOnClickListener {
            onCancelClick.invoke(dialog)
        }

    }
}

