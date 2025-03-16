package com.pshealthcare.customer.app.utils



// how to use

//val adapter = GenericAdapter<MyDataClass>(
//    context,
//    R.layout.item_layout,
//    onBind = { view, data, position ->
//        // Bind your data to the views within the item layout
//        val myData = data as MyDataClass
//        // Example: view.findViewById<TextView>(R.id.textView).text = myData.someText
//    },
//    onItemClick = { clickedItem ->
//        // Handle item click
//        // Example: Toast.makeText(context, "Clicked: ${clickedItem.someText}", Toast.LENGTH_SHORT).show()
//    }
//)
//
//// Set the data for the adapter
//adapter.data = listOf(/* your list of MyDataClass objects */)
//
//// Set the adapter for your RecyclerView
//recyclerView.adapter = adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val context: Context,
    private val layoutResId: Int,
    private val onBind: (View, T, Int) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<GenericAdapter<T>.ViewHolder>() {

    var data: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        onBind(holder.itemView, item, position)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

