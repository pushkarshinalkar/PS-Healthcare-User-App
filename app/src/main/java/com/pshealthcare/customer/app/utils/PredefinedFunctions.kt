package com.pshealthcare.customer.app.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class PredefinedFunctions(private val activity: Context) {

    val sharedPreferences: SharedPreferences =
        activity.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

    // Selecting Image to ImageView
    companion object {
        const val REQUEST_IMAGE_GALLERY = 1001

    }



    // Usage

//    private val pfun = PredefinedFunctions(this)

//    fun onChooseImageClick() {
//        pfun.selectImgToView(yourImageView)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)

//        pfun.onActivityResult(requestCode, resultCode, data, yourImageView)
//    }


    //Get image url after passing to firebase Storage

    private fun getImageUriFromImageView(imageView: ImageView): Uri? {
        return try {
            val drawable = imageView.drawable
            val bitmap = (drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val path = MediaStore.Images.Media.insertImage(
                activity.contentResolver,
                bitmap,
                "Image",
                null
            )
            Uri.parse(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    //usage

//    pfun.uploadImgToFStorage(imageview,
//    onSuccess = { imageUrl ->
//        // Handle success, imageUrl contains the URL of the uploaded image
//        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
//    },
//    onFailure = { exception ->
//        // Handle failure
//        Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
//    }
//    )






    // upload data to firebase realtime storage

    //<T:Any> is used instead of dataclassObj: Any as a parameter because it provides better safety than setting datatype as Any in parameters
    fun <T : Any> uploadDataToRDatabase(
        dataclassObj: T,  //dataclassObj: Any  //Not used this due to better Alternative
        databaseReference: DatabaseReference,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {


        databaseReference.setValue(dataclassObj)
            .addOnSuccessListener {

//                Toast.makeText(activity, "New Match Uploaded", Toast.LENGTH_SHORT).show()
                onSuccess.invoke(true)
            }
            .addOnFailureListener { exception ->
                onFailure.invoke(exception)
                Log.e("Pfun", "Error uploading data: ${exception.message}")
//                Toast.makeText(activity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }

    }





    // fetch data from firebase realtime storage

    fun <T : Any> fetchDataFromRDatabase(
        dataclassObj: Class<T>,
        databaseReference: DatabaseReference,
        filterString: String? = null,
        propertyToCheck: String? = null,
        onSuccess: (List<T>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<T>()

                for (snapshot in dataSnapshot.children) {
                    try {
                        val data = snapshot.getValue(dataclassObj)
                        data?.let {
                            // Check if the filterString is null or the specified property contains the filterString

                            if (filterString == null){
                                dataList.add(it)
                            }else if (dataContainsFilterString(it, filterString, propertyToCheck)) {
                                dataList.add(it)
                            } else {
//                                Toast.makeText(activity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        onFailure.invoke(e)
                        return
                    }
                }

                onSuccess.invoke(dataList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure.invoke(error.toException())
            }
        })
    }

    // usage
//    pfun.fetchDataFromRDatabase(YourDataClass::class.java, reference,
//    filterString = "pizza",
//    propertyToCheck = "foodname",
//    onSuccess = { dataList ->
//        // Handle the retrieved data list
//    },
//    onFailure = { exception ->
//        // Handle the failure
//    }
//    )




    private fun <T : Any> dataContainsFilterString(data: T, filterString: String, propertyToCheck: String?): Boolean {
        if (propertyToCheck.isNullOrBlank()) {
            // If propertyToCheck is not specified, consider the entire object
            return data.toString().contains(filterString, ignoreCase = true)
        }

        // Reflectively access the specified property
        val propertyValue = try {
            val property = data::class.java.getDeclaredField(propertyToCheck)
            property.isAccessible = true
            property.get(data)
        } catch (e: Exception) {
            null
        }

        // Check if the property value contains the filterString
        return propertyValue?.toString()?.contains(filterString, ignoreCase = true) ?: false
    }















    // loading indicator functions

    private var progressDialog: ProgressDialog? = null

    fun startLoadingIndicator(message: String = "Loading...") {
        // Create and show the loading indicator
        progressDialog = ProgressDialog(activity)
        progressDialog?.setMessage(message)
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun stopLoadingIndicator() {
        // Dismiss the loading indicator if it's showing
        progressDialog?.dismiss()
        progressDialog = null
    }


    // add and get shared preference

    fun <T> saveSharedPrefData(key: String, data: T) {

        val editor = sharedPreferences.edit()

        when (data) {
            is String -> editor.putString(key, data)
            is Int -> editor.putInt(key, data)
            is Long -> editor.putLong(key, data)
            is Float -> editor.putFloat(key, data)
            is Boolean -> editor.putBoolean(key, data)
            else -> return
        }

        editor.apply()
    }

    fun <T> getSharedPrefData(key: String, defaultValue: T? = null): T? {

        return when (defaultValue) {
            is String -> sharedPreferences.getString(key, defaultValue) as T?
            is Int -> sharedPreferences.getInt(key, defaultValue) as T?
            is Long -> sharedPreferences.getLong(key, defaultValue) as T?
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T?
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T?
            else -> {
                defaultValue
            }
        }
    }


    // check network connectivity

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }



    // exit if app not paid

    //                                          this, "2023-01-01"
    fun checkAppExpirationForExit(context: Context,expirationDate: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        try {
            val expirationDateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expirationDate)
            val currentDateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate)

            if (currentDateObj != null) {
                if (currentDateObj.after(expirationDateObj)) {
                    (context as? Activity)?.finishAffinity()
                    exitProcess(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}