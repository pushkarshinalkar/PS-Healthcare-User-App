package com.pshealthcare.customer.app.activities

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityBookingInfoBinding
import com.pshealthcare.customer.app.models.MembersModel
import com.pshealthcare.customer.app.models.OrdersModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.receivers.NotificationReceiver
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.OrdersAlarmManager
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class BookingInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingInfoBinding
    private lateinit var cart: MutableList<TestsModel>
    private lateinit var pfun: PredefinedFunctions
    private lateinit var order: OrdersModel
    private lateinit var orderRef: DatabaseReference
    private lateinit var orderKey: String
    private lateinit var dateString: String
    private lateinit var dr_name: String
    private lateinit var dr_spec: String
    private lateinit var dr_regno: String
    private lateinit var dr_mobileno: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pfun = PredefinedFunctions(this)


        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        dateString = currentDate.format(formatter)

        binding.imgBookingBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Generate the ID
        val generatedId = generateRandomId()
        // Assign it as the order key
        orderKey = generatedId
        // Set the order reference using the generated ID
        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderKey)

        cart = intent.getParcelableArrayListExtra("cart") ?: mutableListOf()
        dr_name = intent.getStringExtra("drName").toString()
        dr_spec = intent.getStringExtra("specialization").toString()
        dr_regno = intent.getStringExtra("regNo").toString()
        dr_mobileno = intent.getStringExtra("mobile").toString()

        var initialTotalItemPrice = 0
        for (item in cart) {
            initialTotalItemPrice += item.price.toInt()
        }
        binding.textTotalprice.text = "₹" + initialTotalItemPrice.toString()

        selfCardClick(initialTotalItemPrice.toString())

        binding.cardSelf.setOnClickListener {
            binding.spinnerTextlayoutSer.visibility = View.GONE
            selfCardClick(initialTotalItemPrice.toString())
        }

        binding.cardMember.setOnClickListener {
            membersCardClick(initialTotalItemPrice.toString())
        }

        binding.btnBooknow.setOnClickListener {

            order.date = dateString
//            if (checkPermissions()) {
            pfun.uploadDataToRDatabase(
                order,
                orderRef,
                onSuccess = {

//                    generatePdfInvoice(order)
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                        val newalarmManager = OrdersAlarmManager(this@BookingInfoActivity)
                        newalarmManager.scheduleAlarm(dateString)
                    } else {
                        Toast.makeText(
                            this@BookingInfoActivity,
                            "Device dosen't support remainders",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    updateIsNewOrder()

                    showInfoDialog("Success", "Order Placed Successfully",
                        R.drawable.ic_success,
                        onContinueClick = {
                            val intent =
                                Intent(this@BookingInfoActivity, MainActivity::class.java)
                            intent.putExtra("fromBooking", true)
                            startActivity(intent)
                            finish()
                            it.dismiss()
                        }
                    )

                },
                onFailure = {
                    // Handle failure
                }
            )
//            }
        }

        binding.btnOpencalender.setOnClickListener {
            openCalendar()
        }
    }

    fun updateIsNewOrder() {
        // Path to your data node, adjust according to your database structure
        val newOrderRef = FirebaseDatabase.getInstance().getReference("constants").child("is_new_order")

        // Set value to true
        newOrderRef.setValue(true)
            .addOnSuccessListener {
                // Successfully updated the value
//                println("Successfully updated is_new_order to true")
            }
            .addOnFailureListener { e ->
                // Failed to update the value
//                println("Failed to update is_new_order: ${e.message}")
            }
    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                // Check if the selected date is in the future
                if (selectedDate.after(Calendar.getInstance())) {
                    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    dateString = LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)
                    // Do something with the selected date
                    Toast.makeText(this, "Selected Date: $dateString", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please select a future date.", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun membersCardClick(totalPrice: String) {
        binding.cardMember.setCardBackgroundColor(Color.parseColor("#00AEC9"))
        binding.textCardMember.setTextColor(Color.parseColor("#FFFFFF"))

        binding.cardSelf.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        binding.textCardSelf.setTextColor(Color.parseColor("#000000"))

        val userId = pfun.getSharedPrefData("primaryUserId","")!!
        val databaseRef = FirebaseDatabase.getInstance().reference.child("family_members")

        pfun.fetchDataFromRDatabase(
            MembersModel::class.java,
            databaseRef,
            null,
            null,
            onSuccess = { membersList ->
                val filteredMembersList = membersList.filter { it.userid == userId }
                if (filteredMembersList.isEmpty()) {
                    binding.spinnerTextlayoutSer.visibility = View.GONE
                    Toast.makeText(this@BookingInfoActivity, "No Members Added! Selecting default details", Toast.LENGTH_SHORT).show()
                } else {
                    binding.spinnerTextlayoutSer.visibility = View.VISIBLE
                }
                val memberNames = filteredMembersList.map { it.name }
                val adapter = ArrayAdapter(
                    this@BookingInfoActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    memberNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.autoCompletespinnerTextSer.setAdapter(adapter)

                binding.autoCompletespinnerTextSer.setOnItemClickListener { parent, view, position, id ->
                    val selectedMember = filteredMembersList[position]
                    val userid = pfun.getSharedPrefData("primaryUserId", "")!!

                    binding.textVerifyname.text = selectedMember.name
                    binding.textVerifyaddress.text = "${selectedMember.address}, ${selectedMember.city}, ${selectedMember.pincode}"
                    binding.textVerifymobile.text = selectedMember.mobile

                    order = OrdersModel(
                        orderKey,
                        userid,
                        selectedMember.name!!,
                        selectedMember.relation,
                        selectedMember.birthdate!!,
                        selectedMember.address!!,
                        selectedMember.city!!,
                        selectedMember.pincode!!,
                        selectedMember.mobile!!,
                        totalPrice,
                        "0",
                        dateString,
                        "Not Approved",
                        "No Result",
                        "No Invoice",
                        "No Payment",
                        dr_name,
                        dr_spec,
                        dr_regno,
                        dr_mobileno,
                        cart
                    )
                }
            },
            onFailure = {
                // Handle failure
            }
        )

    }

    private fun selfCardClick(totalPrice: String) {
        binding.spinnerTextlayoutSer.visibility = View.GONE
        binding.cardSelf.setCardBackgroundColor(Color.parseColor("#00AEC9"))
        binding.textCardSelf.setTextColor(Color.parseColor("#FFFFFF"))

        binding.cardMember.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        binding.textCardMember.setTextColor(Color.parseColor("#000000"))

        val userid = pfun.getSharedPrefData("primaryUserId","")!!
        val name = pfun.getSharedPrefData("user_name","")!!
        val birth_date = pfun.getSharedPrefData("birth_date","")!!
        val address = pfun.getSharedPrefData("address","")!!
        val city = pfun.getSharedPrefData("city","")!!
        val pincode = pfun.getSharedPrefData("pincode","")!!
        val mobile_no = pfun.getSharedPrefData("mobile_no","")!!

        binding.textVerifyname.text = name
        binding.textVerifyaddress.text = "$address, $city, $pincode"
        binding.textVerifymobile.text = mobile_no



        order = OrdersModel(
            orderKey,
            userid,
            name!!,
            "Self",
            birth_date!!,
            address!!,
            city!!,
            pincode!!,
            mobile_no!!,
            totalPrice,
            "0",
            dateString,
            "Not Approved",
            "No Result",
            "No Invoice",
            "No Payment",
            dr_name,
            dr_spec,
            dr_regno,
            dr_mobileno,
            cart
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


    private fun generatePdfInvoice(order: OrdersModel) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(816, 1056, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f

        val boldPaint = Paint()
        boldPaint.color = Color.BLACK
        boldPaint.textSize = 12f
        boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var topMargin = 50f
        val leftMargin = 50f
        val centerMarginAlign = 300f

        // Corporate Office Info
        canvas.drawText("Corporate Office :", leftMargin, topMargin, boldPaint)
        canvas.drawText("PS Healthcare", leftMargin + 230, topMargin, boldPaint)
        // Draw the logo
        val logo = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_main_icon_noscale
        )  // Replace with your actual logo resource
        val logoScaled = Bitmap.createScaledBitmap(logo, 70, 70, false)
        canvas.drawBitmap(logoScaled, 650f, topMargin - 15, paint)

        topMargin += 20
        canvas.drawText("PS Healthcare", leftMargin, topMargin, paint)
        canvas.drawText("Pune", leftMargin + 230, topMargin, paint)
        topMargin += 20
        canvas.drawText("Sr/No 116/2, Chinchwad", leftMargin, topMargin, paint)
        canvas.drawText("Contact : +919890428257", leftMargin + 230, topMargin, paint)
        topMargin += 20
        canvas.drawText("Pune 411033", leftMargin, topMargin, paint)
        canvas.drawText("Email : pshealthcarelab@gmail.com", leftMargin + 230, topMargin, paint)
        topMargin += 40

        // Healthcare Service Report
        canvas.drawLine(50f, topMargin - 10, 766f, topMargin - 10, paint)
        boldPaint.textSize = 16f
        canvas.drawText("Healthcare Service Report", centerMarginAlign, topMargin + 10, boldPaint)
        boldPaint.textSize = 12f
        topMargin += 20
        canvas.drawLine(50f, topMargin, 766f, topMargin, paint)
        topMargin += 20

        // Patient Info
        canvas.drawText("Name :", leftMargin, topMargin, paint)
        canvas.drawText("Mrs. Pooja Kachi", leftMargin + 80, topMargin, boldPaint)
        canvas.drawText("Age/Gender :", leftMargin + 300, topMargin, paint)
        canvas.drawText("25/F", leftMargin + 400, topMargin, boldPaint)
        canvas.drawText("Rec.No :", leftMargin + 500, topMargin, paint)
        canvas.drawText("-", leftMargin + 600, topMargin, boldPaint)
        topMargin += 20
        canvas.drawText("Ref. By :", leftMargin, topMargin, paint)
        canvas.drawText("Self", leftMargin + 80, topMargin, boldPaint)
        canvas.drawText("Bill Type :", leftMargin + 300, topMargin, paint)
        canvas.drawText("Cash", leftMargin + 400, topMargin, boldPaint)
        canvas.drawText("Rec.Date :", leftMargin + 500, topMargin, paint)
        canvas.drawText("26-Feb-24", leftMargin + 600, topMargin, boldPaint)
        topMargin += 20
        canvas.drawText("Address :", leftMargin, topMargin, paint)
        canvas.drawText("Pune", leftMargin + 80, topMargin, boldPaint)
        canvas.drawText("Contact :", leftMargin + 500, topMargin, paint)
        canvas.drawText("-", leftMargin + 600, topMargin, boldPaint)
        topMargin += 40

        // Description Table
        topMargin += 20
        canvas.drawLine(50f, topMargin - 10, 766f, topMargin - 10, paint)
        topMargin += 20
        boldPaint.textSize = 14f
        canvas.drawText("Description", leftMargin, topMargin, boldPaint)
        canvas.drawText("Study Unit", leftMargin + 500, topMargin, boldPaint)
        canvas.drawText("Amount", leftMargin + 600, topMargin, boldPaint)
        topMargin += 20
        boldPaint.textSize = 12f

        canvas.drawText("QUAD MARKER WITH GRAPH -", leftMargin, topMargin, paint)
        canvas.drawText("1", leftMargin + 500, topMargin, paint)
        canvas.drawText("1450", leftMargin + 600, topMargin, paint)
        topMargin += 40

        // Financial Summary
        topMargin += 100
        canvas.drawLine(50f, topMargin - 10, 766f, topMargin - 10, paint)
        topMargin += 20
        canvas.drawText("Gross :", leftMargin + 500, topMargin, paint)
        canvas.drawText("1450", leftMargin + 600, topMargin, paint)
        topMargin += 20
        canvas.drawText("Discount :", leftMargin + 500, topMargin, paint)
        canvas.drawText("0", leftMargin + 600, topMargin, paint)
        topMargin += 20
        canvas.drawText("Amounts in words :", leftMargin, topMargin, paint)
        canvas.drawText(
            "One Thousand Four Hundred Fifty Rupees Only",
            leftMargin + 150,
            topMargin,
            boldPaint
        )
        canvas.drawText("Paid :", leftMargin + 500, topMargin, paint)
        canvas.drawText("1450", leftMargin + 600, topMargin, paint)
        topMargin += 20
        canvas.drawText("Balance Amount :", leftMargin + 500, topMargin, paint)
        canvas.drawText("0", leftMargin + 600, topMargin, paint)
        topMargin += 40

        // Acknowledgement
        canvas.drawLine(50f, topMargin - 10, 766f, topMargin - 10, paint)
        boldPaint.textSize = 14f
        canvas.drawText("Acknowledgement", centerMarginAlign + 20, topMargin + 10, boldPaint)
        topMargin += 20
        canvas.drawLine(50f, topMargin, 766f, topMargin, paint)
        topMargin += 20
        boldPaint.textSize = 12f
        canvas.drawText("Note:", leftMargin, topMargin, boldPaint)
        topMargin += 20
        canvas.drawText(
            "1\tThe receipt is not a formal invoice but serves as a record of the transaction.",
            leftMargin,
            topMargin,
            paint
        )
        topMargin += 20
        canvas.drawText(
            "2\tPlease do inform in advance if you are allergic to any ( Food, Medicine, Etc )",
            leftMargin,
            topMargin,
            paint
        )
        topMargin += 20
        canvas.drawText("3\tReport will be dispatch as per TAT time", leftMargin, topMargin, paint)
        topMargin += 20
        canvas.drawText(
            "4\tBy signing I/We acknowledged receipt of the above service.",
            leftMargin,
            topMargin,
            paint
        )
        topMargin += 40

        // Footer
        canvas.drawLine(50f, topMargin - 10, 766f, topMargin - 10, paint)
        topMargin += 20
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(
            "This is computer generated invoice no signature required.",
            leftMargin,
            topMargin,
            paint
        )
        topMargin += 20
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        topMargin += 20
        canvas.drawText("WISHING YOU A GOOD HEALTH.", centerMarginAlign, topMargin, paint)

        document.finishPage(page)

        val directoryPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/Invoices"
        val filePath = "$directoryPath/${order.orderid}.pdf"
        val file = File(directoryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val fileOutputStream = FileOutputStream(filePath)
        try {
            document.writeTo(fileOutputStream)
            // Show a success message or notification about the saved file
        } catch (e: IOException) {
            e.printStackTrace()
//            showAlertDialog("Error", "Failed to generate PDF Invoice")
        } finally {
            document.close()
        }
    }


    // Function to generate a random 8-digit number
    fun generateRandomId(): String {
        val randomNumber = (10000000..99999999).random()
        return "PSH$randomNumber"
    }

}
