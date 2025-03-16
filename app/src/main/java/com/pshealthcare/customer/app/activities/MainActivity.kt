package com.pshealthcare.customer.app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.fragments.BlogsFrag
import com.pshealthcare.customer.app.fragments.HomeFrag
import com.pshealthcare.customer.app.fragments.ProfileFrag
import com.pshealthcare.customer.app.fragments.RecordsFrag
import com.pshealthcare.customer.app.fragments.TestsFrag
import com.pshealthcare.customer.app.utils.PredefinedFunctions

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private lateinit var pfun: PredefinedFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pfun = PredefinedFunctions(this)
        checkNotiPerm()
        navView = findViewById(R.id.bottomNavView)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFrag -> {
                    loadFragment(HomeFrag())
                    window?.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.main_status_color)
                    true
                }

                R.id.profileFrag -> {
                    val userId = pfun.getSharedPrefData("primaryUserId", "")!!
                    if (userId == "") {

                        showInfoDialog("Login Required !", "Sign Up or Login to explore all the features",
                            R.drawable.ic_login,
                            onContinueClick = {
                                val intent =
                                    Intent(this@MainActivity, SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                                it.dismiss()
                            }
                        )
                        false

                    } else {
                        loadFragment(ProfileFrag())
                        true
                    }
                }

                R.id.recordsFrag -> {
                    val userId = pfun.getSharedPrefData("primaryUserId", "")!!
                    if (userId == "") {

                        showInfoDialog("Login Required !", "Sign Up or Login to explore all the features",
                            R.drawable.ic_login,
                            onContinueClick = {
                                val intent =
                                    Intent(this@MainActivity, SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                                it.dismiss()
                            }
                        )

                        false

                    } else {
                        window?.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.main_status_color)
                        loadFragment(RecordsFrag())
                        true
                    }

                }

                R.id.testsFrag -> {
                    loadFragment(TestsFrag())
                    window?.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.main_status_color)
                    true
                }

                R.id.blogsFrag -> {
                    loadFragment(BlogsFrag())
                    window?.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.main_status_color)
                    true
                }

                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.homeFrag
        }

        if (intent.getBooleanExtra("fromBooking",false)){
            loadFragment(RecordsFrag())
            navView.selectedItemId = R.id.recordsFrag
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.anim_fade_in,
            R.anim.anim_fade_out,
            R.anim.anim_fade_in,
            R.anim.anim_fade_out
        )
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    fun navigateToFragment(fragment: Fragment) {
        loadFragment(fragment)
        // Sync the selected item in BottomNavigationView
        when (fragment) {
            is HomeFrag -> navView.selectedItemId = R.id.homeFrag
            is ProfileFrag -> navView.selectedItemId = R.id.profileFrag
            is RecordsFrag -> navView.selectedItemId = R.id.recordsFrag
            is TestsFrag -> navView.selectedItemId = R.id.testsFrag
            is BlogsFrag -> navView.selectedItemId = R.id.blogsFrag
        }
    }


    private fun checkNotiPerm() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {

                showPermDialog(
                    "PS Healthcare needs notification access to provide timely reminders regarding orders",
                    R.drawable.ic_notification,
                    onContinueClick = { dialog ->
                        val notiPermToRequest = mutableListOf<String>()
                        notiPermToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        // Check and add POST_NOTIFICATIONS permission if not granted
                        if (notiPermToRequest.isNotEmpty()) {
                            ActivityCompat.requestPermissions(
                                this,
                                notiPermToRequest.toTypedArray(),
                                1001
                            )
                        }
                        dialog.dismiss()
                    },
                    onCancelClick = { dialog ->
                        dialog.dismiss()
                    })
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            val deniedPermissions = mutableListOf<String>()
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            if (deniedPermissions.isEmpty()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Notification Permission Granted!",
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(
                    Color.parseColor("#00AEC9")
                ).setTextColor(Color.parseColor("#FFFFFF")).show()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Notification Permission Denied!",
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(
                    Color.parseColor("#00AEC9")
                ).setTextColor(Color.parseColor("#FFFFFF")).show()
            }
        }
//        if (requestCode == 1001) {
//            val deniedPermissions = mutableListOf<String>()
//            for (i in permissions.indices) {
//                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                    deniedPermissions.add(permissions[i])
//                }
//            }
//            if (deniedPermissions.isEmpty()) {
//                // All requested permissions are granted, proceed with scheduling the alarm
//                val alarmManager = OrdersAlarmManager(this@MainActivity)
//                alarmManager.scheduleAlarm("17-06-2024")
//            } else {
//                Toast.makeText(this, "Permissions denied: $deniedPermissions", Toast.LENGTH_LONG).show()
//            }
//        }
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
//
//    private fun showLoginAlertDialog(title: String, message: String) {
//        val alertDialog = AlertDialog.Builder(this)
//            .setTitle(title)
//            .setMessage(message)
//            .setPositiveButton("OK") { dialog, _ ->
//                val intent = Intent(this@MainActivity, OtpRegisterActivity::class.java)
//                startActivity(intent)
//                finish()
//
//                dialog.dismiss()
//            }
//            .create()
//        alertDialog.show()
//    }

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


//    fun navigateToFragment(fragment: Fragment) {
//        loadFragment(fragment)
//        // Sync the selected item in BottomNavigationView
//        when (fragment) {
//            is HomeFrag -> navView.selectedItemId = R.id.homeFrag
//            is ProfileFrag -> navView.selectedItemId = R.id.profileFrag
//            is RecordsFrag -> navView.selectedItemId = R.id.recordsFrag
//            is TestsFrag -> navView.selectedItemId = R.id.testsFrag
//            is BlogsFrag -> navView.selectedItemId = R.id.blogsFrag
//        }
//    }


//    private fun loadFragment(fragment: Fragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.setCustomAnimations(
//            R.anim.anim_fade_in,
//            R.anim.anim_fade_out,
//            R.anim.anim_fade_in,
//            R.anim.anim_fade_out
//        )
//        transaction.replace(R.id.fragmentContainerView, fragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
//    }

}
