package com.pshealthcare.customer.app.activities

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.models.MembersModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddFamilyActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var relationEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var pincodeEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var updateInfoButton: Button
    private lateinit var userId: String  // Get the current user ID using FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_family) // Update layout name if different


        val imgBackbtn = findViewById<ImageView>(R.id.img_family_backbtn)
        imgBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Find view references
        nameEditText = findViewById(R.id.text_name_family)
        relationEditText = findViewById(R.id.relationEditText_family)
        ageEditText = findViewById(R.id.birthDateEditText)
        addressEditText = findViewById(R.id.addressEditText_family)
        cityEditText = findViewById(R.id.cityEditText_family)
        pincodeEditText = findViewById(R.id.pincodeEditText_family)
        mobileEditText = findViewById(R.id.mobileEditText_family)
        updateInfoButton = findViewById(R.id.btn_add_member)


        ageEditText.setOnFocusChangeListener { view, hasFocus ->

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

                        ageEditText.setText(dateString)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }
        }
        // Get current user ID (replace with your method to get user ID)
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        updateInfoButton.setOnClickListener {
            updateData()
        }

        // Update button click listener

    }

    private fun updateData() {
        val name = nameEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim()
        val relation = relationEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val pincode = pincodeEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()

        // Check if all fields are filled
        if (name.isEmpty() || age.isEmpty() || relation.isEmpty() || address.isEmpty() || city.isEmpty() || pincode.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this@AddFamilyActivity, "Please fill in all the fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val member = MembersModel(userId, name, relation, age, address, city, pincode, mobile)

        // Get a reference to the user node
        val userRef = FirebaseDatabase.getInstance().getReference("family_members").child(name)

        // Update user data
        userRef.setValue(member)
            .addOnSuccessListener {
                // Update successful
                showInfoDialog("Success", "Family Members Updated",
                    R.drawable.ic_success,
                    onContinueClick = {

                        it.dismiss()
                        clearEditTextFields()
                        val intent = Intent(this@AddFamilyActivity, ViewMembersActivity::class.java)
                        startActivity(intent)
                    }
                )

            }
            .addOnFailureListener { exception ->
                // Update failed
                Toast.makeText(this@AddFamilyActivity, "Error updating information!", Toast.LENGTH_SHORT).show()
                Log.e("Update Error", exception.message.toString())
            }
    }

    private fun clearEditTextFields() {
        nameEditText.text.clear()
        ageEditText.text.clear()
        relationEditText.text.clear()
        addressEditText.text.clear()
        cityEditText.text.clear()
        pincodeEditText.text.clear()
        mobileEditText.text.clear()
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

        textDesrip.text = description
        btnOk.setOnClickListener {
            onContinueClick.invoke(dialog)
        }

    }


}
