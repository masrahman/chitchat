package com.maasrahman.chitchat.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maasrahman.chitchat.R
import com.maasrahman.chitchat.utils.GlideApp
import kotlinx.android.synthetic.main.activity_image.*

class ImageViewActivity : AppCompatActivity() {
    var urlPhoto = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val bundle = intent
        if(bundle != null){
            urlPhoto = bundle.getStringExtra("photo")
            println("CEK PHOTO $urlPhoto")
            GlideApp.with(this@ImageViewActivity)
                .load(urlPhoto)
                .into(photoView)
        }

        close.setOnClickListener {
            finish()
        }
    }
}