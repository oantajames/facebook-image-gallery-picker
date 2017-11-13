package com.imagepicker.facebook

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity
import com.imagepicker.facebook.view.photos.FacebookPhotosActivity
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView

    private val FACEBOOK_PHOTO_RESULT = 11123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.selected_image)
        var button = findViewById<Button>(R.id.button2)
        button.setOnClickListener({
            val i = Intent(this, FacebookAlbumsActivity::class.java)
            startActivityForResult(i, FACEBOOK_PHOTO_RESULT)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FACEBOOK_PHOTO_RESULT) {
            if (data != null) {
                val bundle: Bundle = data.extras.getParcelable(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM)
                val facebookItem: FacebookPhoto = bundle.getParcelable(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM)

                Picasso.with(this@MainActivity)
                        .load(Uri.parse(facebookItem.photoUrl.toURI().toString()))
                        .into(imageView)
            }
        }
    }

}
