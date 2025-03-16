package com.pshealthcare.customer.app.activities

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.models.MembersModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.utils.MembersAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class UpdateUserInfoActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var pincodeEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var updateInfoButton: Button
    private lateinit var animLoadUserDetails: LottieAnimationView
    private lateinit var userId: String  // Get the current user ID using FirebaseAuth
    private lateinit var pfun: PredefinedFunctions
    private lateinit var imgUpdateprofileBackbtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user_info) // Update layout name if different


        pfun = PredefinedFunctions(this)
        // Find view references
        nameEditText = findViewById(R.id.text_name)
        birthDateEditText = findViewById(R.id.birthDateEditText)
        addressEditText = findViewById(R.id.addressEditText)
        cityEditText = findViewById(R.id.cityEditText)
        pincodeEditText = findViewById(R.id.pincodeEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        updateInfoButton = findViewById(R.id.btn_update_info)
        imgUpdateprofileBackbtn = findViewById(R.id.img_updateprofile_backbtn)
        animLoadUserDetails = findViewById(R.id.anim_load_userDetails)


        userId = pfun.getSharedPrefData("primaryUserId","")!!



        imgUpdateprofileBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get current user ID (replace with your method to get user ID)


        val name = pfun.getSharedPrefData("user_name","")!!
        val birth_date = pfun.getSharedPrefData("birth_date","")!!
        val address = pfun.getSharedPrefData("address","")!!
        val city = pfun.getSharedPrefData("city","")!!
        val pincode = pfun.getSharedPrefData("pincode","")!!
        val mobile_no = pfun.getSharedPrefData("mobile_no","")!!

        if (name.isEmpty() && birth_date.isEmpty() && address.isEmpty() && city.isEmpty() && pincode.isEmpty() && mobile_no.isEmpty()) {

            fetchUserInfo()
        }
//        Toast.makeText(this@UpdateUserInfoActivity, birth_date, Toast.LENGTH_SHORT).show()

        nameEditText.setText(name)
        birthDateEditText.setText(birth_date)
        addressEditText.setText(address)
        cityEditText.setText(city)
        pincodeEditText.setText(pincode)
        mobileEditText.setText(mobile_no)

        animLoadUserDetails.visibility = View.GONE


        birthDateEditText.setOnFocusChangeListener { view, hasFocus ->

            if (hasFocus) {
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)

                        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        val dateString =
                            LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)

                        birthDateEditText.setText(dateString)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }
        }

        updateInfoButton.setOnClickListener {
            updateData()
        }


    }

    private fun updateData() {
        val name = nameEditText.text.toString().trim()
        val birthdate = birthDateEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val pincode = pincodeEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()

        if (name.isEmpty() || birthdate.isEmpty() || address.isEmpty() || city.isEmpty() || pincode.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        pfun.saveSharedPrefData("user_name", name)
        pfun.saveSharedPrefData("birth_date", birthdate)
        pfun.saveSharedPrefData("address", address)
        pfun.saveSharedPrefData("pincode", pincode)
        pfun.saveSharedPrefData("mobile_no", mobile)
        pfun.saveSharedPrefData("city", city)

        // Validate input fields (optional)

        val member = MembersModel(userId, name, "self", birthdate, address, city, pincode, mobile)

        // Get a reference to the user node
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Update user data
        userRef.setValue(member)
            .addOnSuccessListener {
                // Update successful
                showAlertDialog("Success", "Profile updated!")

                // Clear edit text fields (optional)
            }
            .addOnFailureListener { exception ->
                // Update failed
                showAlertDialog("Error", "Error updating information!")
                Log.e("Update Error", exception.message.toString())
            }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
//                clearEditTextFields()
                val intent = Intent(this@UpdateUserInfoActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun fetchUserInfo() {

        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
//        Toast.makeText(this@UpdateUserInfoActivity, userId, Toast.LENGTH_SHORT).show()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val name = dataSnapshot.child("name").value?.toString() ?: ""
                val birthdate = dataSnapshot.child("birthdate").value?.toString() ?: ""
                val address = dataSnapshot.child("address").value?.toString() ?: ""
                val city = dataSnapshot.child("city").value?.toString() ?: ""
                val pincode = dataSnapshot.child("pincode").value?.toString() ?: ""
                val mobile_no = dataSnapshot.child("mobile").value?.toString() ?: ""

                if (name.isNotEmpty() && birthdate.isNotEmpty() && address.isNotEmpty() &&
                    city.isNotEmpty() && pincode.isNotEmpty() && mobile_no.isNotEmpty()) {

                    pfun.saveSharedPrefData("user_name", name)
                    pfun.saveSharedPrefData("birth_date", birthdate)
                    pfun.saveSharedPrefData("address", address)
                    pfun.saveSharedPrefData("pincode", pincode)
                    pfun.saveSharedPrefData("mobile_no", mobile_no)
                    pfun.saveSharedPrefData("city", city)

                    animLoadUserDetails.visibility = View.GONE
                    Toast.makeText(this@UpdateUserInfoActivity, "User Details retrieved successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UpdateUserInfoActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error if needed
            }
        })

    }


    private fun clearEditTextFields() {
        nameEditText.text.clear()
        birthDateEditText.text.clear()
        addressEditText.text.clear()
        cityEditText.text.clear()
        pincodeEditText.text.clear()
        mobileEditText.text.clear()
    }
}
