package com.pshealthcare.customer.app.activities

import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.databinding.ActivityBlogContentBinding

class BlogContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBlogContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            textBlogtitle.text = intent.getStringExtra("title")
            textBlogcontent.text = intent.getStringExtra("content")
            val img_link = intent.getStringExtra("image")
            val vid_link = intent.getStringExtra("video")

            Glide.with(this@BlogContentActivity)
                .load(img_link)
                .into(binding.imgBlog)

            val mediaController = MediaController(this@BlogContentActivity)
            mediaController.setAnchorView(binding.vidBlog)
            binding.vidBlog.setMediaController(mediaController)

            binding.vidBlog.apply {
                setVideoPath(vid_link)
                setOnPreparedListener { mp ->
                    binding.progressBar.visibility = View.GONE
                    binding.vidBlog.visibility = View.VISIBLE
                    start()
                }
            }
        }
    }
}
