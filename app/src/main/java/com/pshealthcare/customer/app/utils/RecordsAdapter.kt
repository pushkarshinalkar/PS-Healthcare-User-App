package com.pshealthcare.customer.app.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.RecordDetailsActivity
import com.pshealthcare.customer.app.models.OrdersModel

class RecordsAdapter(
    private val context: Context,
    private val ordersList: MutableList<OrdersModel>
) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_placed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = ordersList[position]

        holder.name.text = order.name
        holder.date.text = order.date
        holder.status.text = order.status
        holder.totalPrice.text = "₹" + (order.total_price.toInt() - order.total_discount.toInt()).toString()

        when (order.status) {
            "Not Approved" -> {
                holder.actionStatus.text = "Cancel Order"
                holder.actionButtonOrder.setCardBackgroundColor(Color.parseColor("#FF6B6B"))
            }
            "Approved" -> {
                holder.actionStatus.text = "Get Invoice"
                holder.actionButtonOrder.setCardBackgroundColor(Color.parseColor("#5BC96A"))
//                holder.actionButtonOrder.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics)
            }
            "Completed" -> {
                holder.actionButtonOrder.setCardBackgroundColor(Color.parseColor("#00AEC9"))
                holder.actionStatus.text = "Get Report"
            }
        }

        holder.actionButtonOrder.setOnClickListener {
            if (order.status == "Not Approved") {
                AlertDialog.Builder(context).apply {
                    setTitle("Cancel Order")
                    setMessage("Are you sure you want to cancel this order?")
                    setPositiveButton("Yes") { _, _ ->
                        val orderRef = FirebaseDatabase.getInstance().reference.child("orders").child(order.orderid)
                        orderRef.removeValue().addOnSuccessListener {
                            ordersList.removeAt(position)
                            notifyItemRemoved(position)

                            Toast.makeText(context, "Order deleted", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to delete order", Toast.LENGTH_SHORT).show()
                        }
                    }
                    setNegativeButton("No", null)
                }.show()
            } else if (order.status == "Approved") {

                val fileName = "invoice_${System.currentTimeMillis()}.pdf"
                downloadPdfFromLink(order.invoice_link,fileName,"Invoice")

            } else if (order.status == "Completed") {

                val fileName = "lab_report_${System.currentTimeMillis()}.pdf"
                downloadPdfFromLink(order.result_link,fileName,"Lab Report")
            }
        }

        holder.viewShowTestList.setOnClickListener {
            val intent = Intent(context, RecordDetailsActivity::class.java)
            intent.putParcelableArrayListExtra("testlist", ArrayList(order.tests.filterNotNull()))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = ordersList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.text_rvorder_name)
        val date: TextView = itemView.findViewById(R.id.text_rvorder_date)
        val status: TextView = itemView.findViewById(R.id.text_rvorder_status)
        val totalPrice: TextView = itemView.findViewById(R.id.text_rvorder_totalprice)
        val actionStatus: TextView = itemView.findViewById(R.id.text_actionstatus)
        val actionButtonOrder: CardView = itemView.findViewById(R.id.card_actionbutton)
        val viewShowTestList: View = itemView.findViewById(R.id.view_showrecords_testlist)
    }


    private fun downloadPdfFromLink(pdfLink: String, fileName: String,downloadTitle: String) {
        // Use DownloadManager to download the PDF from the provided link
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(pdfLink)).apply {
            setTitle(downloadTitle)
            setDescription("Downloading PDF")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        downloadManager.enqueue(request)
    }





}

