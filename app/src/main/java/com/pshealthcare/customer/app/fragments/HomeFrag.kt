package com.pshealthcare.customer.app.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.AddFamilyActivity
import com.pshealthcare.customer.app.activities.MainActivity
import com.pshealthcare.customer.app.activities.SignInActivity
import com.pshealthcare.customer.app.activities.PackageDetailsActivity
import com.pshealthcare.customer.app.activities.RadiologyActivity
import com.pshealthcare.customer.app.activities.SearchResultActivity
import com.pshealthcare.customer.app.activities.UserCartActivity
import com.pshealthcare.customer.app.activities.ViewMembersActivity
import com.pshealthcare.customer.app.databinding.FragmentHomeBinding
import com.pshealthcare.customer.app.models.PackagesModel
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID


class HomeFrag : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var pfun: PredefinedFunctions

    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase

    private lateinit var imageUrl : Uri

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            uploadPrescriptionToFirebase(imageUrl)
        } else {
            Toast.makeText(requireContext(), "Failed to get file!", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        pfun = context?.let { PredefinedFunctions(it) }!!

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        val fade = Fade()
        activity?.window?.enterTransition = fade
        activity?.window?.exitTransition = fade

        binding.rvPackages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            fetchOfferImages()
            fetchPackages()
        }

        val searchView: SearchView = view.findViewById(R.id.searchView_tests_home)

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



        binding.apply {

            cardViewhistory.setOnClickListener {
                (activity as MainActivity).navigateToFragment(RecordsFrag())

            }

            cardViewcart.setOnClickListener {
                val userId = pfun.getSharedPrefData("primaryUserId","")!!
                if(userId == ""){

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

                }else{
                    val intent = Intent(requireContext(), UserCartActivity::class.java)
                    startActivity(intent)
                }
            }
            btnViewLabtests.setOnClickListener {
                (activity as MainActivity).navigateToFragment(TestsFrag())
            }
            cardViewmembers.setOnClickListener {

                val userId = pfun.getSharedPrefData("primaryUserId","")!!
                if(userId == ""){

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

                }else{
                    val intent = Intent(requireContext(), ViewMembersActivity::class.java)
                    startActivity(intent)
                }


            }
            cardAddmembers.setOnClickListener {
                val userId = pfun.getSharedPrefData("primaryUserId","")!!
                if(userId == ""){

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

                }else{
                    val intent = Intent(requireContext(), AddFamilyActivity::class.java)
                    startActivity(intent)
                }
            }
            btnViewRadiotests.setOnClickListener {
                val intent = Intent(requireContext(), RadiologyActivity::class.java)
                startActivity(intent)
            }

            val toggle = ActionBarDrawerToggle(
                activity, drawerHome, R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawerHome.addDrawerListener(toggle)
            toggle.syncState()

            imgSidemenu.setOnClickListener {
                if (drawerHome.isDrawerOpen(sideNavigationView)) {
                    drawerHome.closeDrawer(sideNavigationView)
                } else {
                    drawerHome.openDrawer(sideNavigationView)
                }
            }

            binding.btnUploadPrescription.setOnClickListener {

                val userId = pfun.getSharedPrefData("primaryUserId","")!!
                if(userId == ""){

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

                }else{

                    // on below line we are creating a new bottom sheet dialog.
                    val dialog = BottomSheetDialog(requireContext())
                    val view = layoutInflater.inflate(R.layout.bottomsheet_upload_prescription, null)

                    val cardOpenCamera = view.findViewById<CardView>(R.id.card_opencamera)
                    val cardOpenFilepicker = view.findViewById<CardView>(R.id.card_openFilepicker)

                    cardOpenCamera.setOnClickListener {
                        val userId = pfun.getSharedPrefData("primaryUserId", "")!!
                        if (userId == "") {

                            showInfoDialog("Login Required !",
                                "Sign Up or Login to explore all the features",
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
                            checkCameraPerm()
                        }
                    }
                    cardOpenFilepicker.setOnClickListener {
                        openFileManager()
                    }
                    dialog.setCancelable(true)
                    dialog.setContentView(view)
                    dialog.show()

                }
            }

//            binding.btnUploadPrescription.setOnClickListener {
//                val userId = pfun.getSharedPrefData("primaryUserId","")!!
//                if(userId == ""){
//
//                    showInfoDialog("Login Required !", "Sign Up or Login to explore all the features",
//                        R.drawable.ic_login,
//                        onContinueClick = {
//                            val intent =
//                                Intent(requireContext(), OtpRegisterActivity::class.java)
//                            startActivity(intent)
//                            requireActivity().finish()
//                            it.dismiss()
//                        }
//                    )
//
//                }else{
//                    checkCameraPerm()
//                }
//
//            }

        }

        binding.sideNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.menu_rateus -> {
////                    showRateUsPopup()
//                    true
//                }
//                R.id.menu_share -> {
////                    shareAppLink()
//                    true
//                }

                R.id.menu_whats -> {

                    val phoneNum = "+919545226514" // Assuming phone number starts with country code

                    val intent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "whatsapp://send?phone=$phoneNum"
                        )
                    )
                    startActivity(intent)
                    true
                }

                R.id.menu_report -> {
                    val recipient = "pshealthcarelab@gmail.com"
                    val subject = "Report Problems"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$recipient?subject=$subject")
                    }

                    startActivity(intent)
                    true
                }

                R.id.menu_logout -> {

                    // Clear shared preferences
                    val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    // Clear cache
                    requireContext().cacheDir.deleteRecursively()

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), SignInActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                    true
                }

                else -> false
            }
        }

    }

    private fun fetchOfferImages() {
        val database = FirebaseDatabase.getInstance()
        val offersRef = database.reference.child("offers")

        val imageList = ArrayList<SlideModel>() // Create image list

        offersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in 1..3) {
                    val imgUrl = dataSnapshot.child("offer_img_url_$i").getValue(String::class.java)
//                    Toast.makeText(requireContext(), imgUrl, Toast.LENGTH_SHORT).show()
                    imgUrl?.let {
                        imageList.add(SlideModel(it,ScaleTypes.FIT))
                    }
                }
                binding.imageSlider.setImageList(imageList)
                binding.animLoadSlider.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("HomeFrag", "Error fetching offer images: ${databaseError.message}")
                Snackbar.make(binding.root, "Error fetching offer images", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPackages() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("packages")
        pfun.fetchDataFromRDatabase(
            PackagesModel::class.java,
            databaseRef,
            null,
            null,
            onSuccess = {
                val adapter = GenericAdapter<PackagesModel>(
                    requireContext(),
                    R.layout.item_rv_packages,
                    onBind = { view, data, position ->
                        // Bind your data to the views within the item layout
                        val myData = data
                        view.findViewById<TextView>(R.id.text_packagename).text = myData.package_name
//                        Toast.makeText(requireContext(), myData.package_name, Toast.LENGTH_SHORT).show()
                        val packageImageView = view.findViewById<ImageView>(R.id.img_package)
                        Glide.with(requireContext())
                            .load(myData.img_url)
                            .placeholder(R.drawable.ic_beaker_colourful)
                            .into(packageImageView)

                        binding.hlineHome1.visibility = View.VISIBLE
                        binding.textView9.visibility = View.VISIBLE
                    },
                    onItemClick = { clickedItem ->
                        val testsList = clickedItem.tests.values.toList() // Convert map to list
                        val intent = Intent(context, PackageDetailsActivity::class.java)
                        intent.putParcelableArrayListExtra("testlist", ArrayList(testsList))
                        startActivity(intent)
                    }
                )

                adapter.data = it

                binding.rvPackages.adapter = adapter
            },
            onFailure = {
                Log.d("ps_healthcare_debug", "fetchPackages: " + it.toString())
            }
        )
    }




    private fun getExtension(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }

    private fun getEnteredEmailId(): String {

        return FirebaseAuth.getInstance().currentUser?.email.toString()
        // Implement logic to retrieve email ID from your UI (e.g., EditText)
        // Replace with your actual implementation // Placeholder until you implement email retrieval
    }


    private fun showPermDialog(description: String, img_perm : Int, onContinueClick: ((AlertDialog) -> Unit), onCancelClick: ((AlertDialog) -> Unit)) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_permissions, null)
        val textContinue = dialogView.findViewById<TextView>(R.id.text_perm_continue)
        val textNotnow = dialogView.findViewById<TextView>(R.id.text_perm_dismiss)
        val textDescription = dialogView.findViewById<TextView>(R.id.text_permission_description)
        val imgPermission = dialogView.findViewById<ImageView>(R.id.img_permission)

        Glide.with(this)
            .load(img_perm)
            .placeholder(R.drawable.ic_beaker)
            .into(imgPermission)

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

        textDescription.text = description
        textContinue.setOnClickListener {
            onContinueClick.invoke(dialog)
        }

        textNotnow.setOnClickListener {
            onCancelClick.invoke(dialog)
        }

    }

//    private fun checkFilesAccessPerm() {
//
//        if (Build.VERSION.SDK_INT <= 32) {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                showPermDialog(
//                    "PS Healthcare needs storage access so that you can upload your prescription document.",
//                    R.drawable.ic_folder,
//                    onContinueClick = { dialog ->
//                        val storagePermToRequest = mutableListOf<String>()
//                        storagePermToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//
//                        if (storagePermToRequest.isNotEmpty()) {
//                            ActivityCompat.requestPermissions(requireActivity(), storagePermToRequest.toTypedArray(), 1004)
//                        }
//                        dialog.dismiss()
//                    },
//                    onCancelClick = { dialog ->
//                        dialog.dismiss()
//                    })
//            } else {
//                // Open file manager
//                openFileManager()
//
//            }
//        } else {
//
//            openFileManager()
//        }
//
//    }

    val startForOpenFileManagerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val selectedFileUri = intent?.data
            // Handle the selected file URI (e.g., upload to Firebase Storage)
            if (selectedFileUri != null) {
                // Proceed with uploading the selected file to Firebase Storage
                uploadPrescriptionToFirebase(selectedFileUri)
            } else {
                Toast.makeText(requireContext(), "Failed to get file URI", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val startForCameraPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->

        if (isGranted) {
            // PERMISSION GRANTED
            Snackbar.make(binding.root, "Camera Permission Granted!", Snackbar.LENGTH_SHORT).setBackgroundTint(
                Color.parseColor("#00AEC9")).setTextColor(Color.parseColor("#FFFFFF")).show()
            // open camera
            openCamera()
        } else {
            // PERMISSION NOT GRANTED
            Snackbar.make(binding.root, "Camera Permission Denied!", Snackbar.LENGTH_SHORT).setBackgroundTint(
                Color.parseColor("#00AEC9")).setTextColor(Color.parseColor("#FFFFFF")).show()
        }

    }


    private fun openFileManager() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*" // Allow all file types
        startForOpenFileManagerResult.launch(intent)
    }


    private fun checkCameraPerm() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            showPermDialog(
                "PS Healthcare needs camera access so that you can upload prescription.",
                R.drawable.ic_camera,
                onContinueClick = { dialog ->
                    // Request the camera permission using the registered launcher
                    startForCameraPermissionResult.launch(Manifest.permission.CAMERA)
                    dialog.dismiss()
                },
                onCancelClick = { dialog ->
                    dialog.dismiss()
                }
            )
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {

            // Open camera
            openCamera()
        }
    }

    // camera functions
    private fun openCamera() {
        imageUrl = createImageUri()
        contract.launch(imageUrl)
    }

    private fun uploadPrescriptionToFirebase(selectedFileURI: Uri) {

        Toast.makeText(context, "Uploading . . .", Toast.LENGTH_SHORT).show()
        // Create a unique file name in Firebase Storage
        val fileName = UUID.randomUUID().toString() + "." + getExtension(selectedFileURI)
        // Get a reference to the file in Firebase Storage
        val storageRef = Firebase.storage.reference.child("prescriptions/$fileName")
        // Upload the file
        val uploadTask = storageRef.putFile(selectedFileURI)
        uploadTask.addOnFailureListener { exception ->
            // Handle upload failure
            Toast.makeText(requireContext(), "Upload failed: $exception", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { snapshot ->
            // Get the download URL after successful upload
            snapshot.metadata?.reference?.downloadUrl?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()

                    val userid = pfun.getSharedPrefData("primaryUserId","")!!
                    val name = pfun.getSharedPrefData("user_name","")!!
                    val address = pfun.getSharedPrefData("address","")!!
                    val mobile_no = pfun.getSharedPrefData("mobile_no","")!!


                    val prescriptionRef = Firebase.database.reference.child("prescriptions").child(userid)
                    val currentDate = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val dateString = currentDate.format(formatter)
                    val prescriptionData = hashMapOf(
                        "upload_date" to dateString,
                        "downloadUrl" to downloadUrl,
                        "userid" to userid,
                        "name" to name,
                        "address" to address,
                        "mobile_no" to mobile_no

                    )

                    prescriptionRef.setValue(prescriptionData)
                        .addOnSuccessListener {

                            showInfoDialog("Prescription Uploaded", "Our team will contact you shortly to discuss your requirements",
                                R.drawable.ic_prescription,
                                onContinueClick = {
                                    it.dismiss()
                                }
                            )
                        }
                        .addOnFailureListener { exception ->
                            // Handle database write failure
                            Toast.makeText(requireContext(), "Database write failed: $exception", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Handle download URL retrieval failure
                    Toast.makeText(requireContext(), "Failed to get download URL!", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            // Update a progress bar (optional)
        }
    }

    private fun createImageUri(): Uri {
        val image = File(requireContext().filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "com.pshealthcare.customer.app.fragments.FileProvider",
            image
        )
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