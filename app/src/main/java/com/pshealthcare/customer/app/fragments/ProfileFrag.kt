package com.pshealthcare.customer.app.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.UpdateUserInfoActivity
import com.pshealthcare.customer.app.databinding.FragmentProfileBinding
import com.pshealthcare.customer.app.models.MembersModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.models.TestsModel.CREATOR.toTestsModelList
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import java.text.SimpleDateFormat
import java.util.Locale


class ProfileFrag : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var pfun: PredefinedFunctions
    private lateinit var userRef: DatabaseReference
    private lateinit var cart: MutableList<TestsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val userRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid)

        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.main_theme_color)

        pfun = context?.let { PredefinedFunctions(it) }!!


        cart = loadCart(requireContext())
        val name = pfun.getSharedPrefData("user_name","")!!
        val birth_date = pfun.getSharedPrefData("birth_date","")!!
        val address = pfun.getSharedPrefData("address","")!!
        val city = pfun.getSharedPrefData("city","")!!
        val pincode = pfun.getSharedPrefData("pincode","")!!
        val mobile_no = pfun.getSharedPrefData("mobile_no","")!!

        val userid = pfun.getSharedPrefData("primaryUserId","")!!

        binding.textUsername.text = name
        binding.textUserid.text = mobile_no
        binding.textAgeProfile.text = birth_date
        binding.textCityProfile.text = city
        binding.textPincode.text = pincode
        binding.textAddress.text = address

        binding.textCartSize.text = cart.size.toString()
        binding.textNoOrders.text = pfun.getSharedPrefData("no_of_orders","--")
        binding.textLastOrderdate.text = formatDateString(pfun.getSharedPrefData("last_order","--").toString())
//        Toast.makeText(context, pfun.getSharedPrefData("last_order","--").toString(), Toast.LENGTH_SHORT).show()

            val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (acct != null) {
                Glide.with(requireContext())
                    .load(acct.photoUrl)
                    .into(binding.imgProfile)
            }

        userRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userid)
        fetchUserInfo()

        binding.btnEditProfile.setOnClickListener {

            val intent = Intent(requireContext(), UpdateUserInfoActivity::class.java)
            startActivity(intent)

        }


    }

    fun formatDateString(inputDate: String): String? {
        // Define the input and output date formats
        val inputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        return try {
            // Parse the input date string to a Date object
            val date = inputDateFormat.parse(inputDate)
            // Format the Date object to the desired output format
            date?.let {
                outputDateFormat.format(it)
            }
        } catch (e: Exception) {
            // Handle parse exceptions
            e.printStackTrace()
            return "--"
        }
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

    private fun fetchUserInfo() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userInfo = snapshot.getValue(MembersModel::class.java)
                    // Update UI with user info
                    userInfo?.let {
                        binding.textUsername.text = it.name
                        binding.textAgeProfile.text = it.birthdate
                        binding.textCityProfile.text = it.city
                        binding.textAddress.text = it.address
                        binding.textPincode.text = it.pincode
                    }
                } else {
                    // User does not exist in the database
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Error fetching user info
            }
        })
    }

}