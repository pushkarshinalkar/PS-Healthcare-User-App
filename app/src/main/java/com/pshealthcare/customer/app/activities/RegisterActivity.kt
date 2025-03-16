package com.pshealthcare.customer.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityRegisterBinding
import com.pshealthcare.customer.app.databinding.ActivitySigninBinding
import com.pshealthcare.customer.app.utils.PredefinedFunctions

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var pfun: PredefinedFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pfun = PredefinedFunctions(this)

        FirebaseApp.initializeApp(this)

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // found from google services .json
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        mAuth = FirebaseAuth.getInstance()

        // OTP login
        binding.buttonSendOtp.setOnClickListener {

            // verify login
            val email = binding.textMailLogin.text.toString()
            val password = binding.textPassLogin.text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration success
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        firebaseUser?.let {
                            pfun.saveSharedPrefData("primaryUserId", it.uid)
                            val intent = Intent(this, UpdateUserInfoActivity::class.java)
                            startActivity(intent)
                            finish()
                            // You can perform actions like sending a verification email here
//                            it.sendEmailVerification()
//                                .addOnCompleteListener { verificationTask ->
//                                    if (verificationTask.isSuccessful) {
//                                        Toast.makeText(
//                                            this,
//                                            "Verification email sent to ${it.email}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }


        binding.textSkip.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
            finish()
        }

        // Google Sign-In
        binding.btnSigninGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    // Google sign in
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pfun.saveSharedPrefData("primaryUserId", firebaseAuth.uid)
                startActivity(Intent(this, UpdateUserInfoActivity::class.java))
                finish()
            }
        }
    }

    override fun onStart() {

        val name = pfun.getSharedPrefData("user_name","")!!
        val birth_date = pfun.getSharedPrefData("birth_date","")!!
        val address = pfun.getSharedPrefData("address","")!!
        val city = pfun.getSharedPrefData("city","")!!
        val pincode = pfun.getSharedPrefData("pincode","")!!
        val mobile_no = pfun.getSharedPrefData("mobile_no","")!!

        if (name.isNotEmpty() && birth_date.isNotEmpty() && address.isNotEmpty() &&
            city.isNotEmpty() && pincode.isNotEmpty() && mobile_no.isNotEmpty()) {

            val currentUser = FirebaseAuth.getInstance().currentUser
            super.onStart()
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else if (currentUser != null) {
                // User is already signed in, redirect to the main activity.
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            val currentUser = FirebaseAuth.getInstance().currentUser
            super.onStart()
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                startActivity(Intent(this, UpdateUserInfoActivity::class.java))
                finish()
            }else if (currentUser != null) {
                // User is already signed in, redirect to the main activity.
                val intent = Intent(this, UpdateUserInfoActivity::class.java)
                startActivity(intent)
                finish()
            }
        }




    }
}

