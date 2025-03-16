package com.pshealthcare.customer.app.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.BlogContentActivity
import com.pshealthcare.customer.app.activities.SearchResultActivity
import com.pshealthcare.customer.app.databinding.ActivitySearchResultBinding
import com.pshealthcare.customer.app.databinding.FragmentBlogBinding
import com.pshealthcare.customer.app.databinding.FragmentTestsBinding
import com.pshealthcare.customer.app.models.BlogsModel
import com.pshealthcare.customer.app.models.TestsModel
import com.pshealthcare.customer.app.utils.GenericAdapter
import com.pshealthcare.customer.app.utils.PredefinedFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class BlogsFrag : Fragment() {

    private lateinit var binding: FragmentBlogBinding
    private lateinit var pfun: PredefinedFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pfun = context?.let { PredefinedFunctions(it) }!!

        binding.rvBlogs.layoutManager = LinearLayoutManager(requireContext())

        val databaseRef = FirebaseDatabase.getInstance().reference.child("blogs")

        pfun.fetchDataFromRDatabase(
            BlogsModel::class.java,
            databaseRef,
            null,
            null,
            onSuccess = {
                if (it.isEmpty()){
                    binding.imgNothingFound.visibility = View.VISIBLE
                    binding.textNothingFound.visibility = View.VISIBLE
                }
                val adapter = GenericAdapter<BlogsModel>(
                    requireContext(),
                    R.layout.item_rv_blog,
                    onBind = { view, data, position ->
                        // Bind your data to the views wit
                        // hin the item layout
                        val myData = data
                        view.findViewById<TextView>(R.id.text_rvblog_title).text = myData.blogTitle

                    },
                    onItemClick = { clickedItem ->

                        val intent = Intent(context, BlogContentActivity::class.java)
                        intent.putExtra("title", clickedItem.blogTitle)
                        intent.putExtra("content", clickedItem.article)
                        intent.putExtra("image", clickedItem.imageUrl)
                        intent.putExtra("video", clickedItem.videoUrl)
                        startActivity(intent)


                    }
                )

                adapter.data = it
                binding.rvBlogs.adapter = adapter

                binding.animLoadBlogFrag.visibility = View.GONE
            },
            onFailure = {

            })

    }



}