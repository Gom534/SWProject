package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MyImageView : AppCompatActivity() {
    private lateinit var imageView : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_view)
        imageView = findViewById(R.id.imageView3)
        val imageuri = intent.getStringExtra("uri")

        val uri = Uri.parse(imageuri)

        Glide.with(this@MyImageView)
            .load(uri)
            .into(imageView)
    }
}