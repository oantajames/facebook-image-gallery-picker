package com.imagepicker.facebook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.widget.Button
import android.widget.ImageView
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.selected_image)

        var intentFilter = IntentFilter()
        intentFilter.addAction(FacebookJobManager.BROADCAST_FACEBOOK_PHOTO_SELECTED)
        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(broadcastReceiver, intentFilter)
        var button = findViewById<Button>(R.id.button2)

        button.setOnClickListener({
            var i = Intent(this, FacebookAlbumsActivity::class.java)
            startActivity(i)
        })
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == FacebookJobManager.BROADCAST_FACEBOOK_PHOTO_SELECTED) {
                if (intent.extras != null) {
                    val bundle: Bundle = intent.extras.getParcelable(FacebookJobManager.FACEBOOK_PHOTO)
                    val facebook: FacebookPhoto = bundle.getParcelable(FacebookJobManager.FACEBOOK_PHOTO)

                    Picasso.with(this@MainActivity)
                            .load(Uri.parse(facebook.photoUrl.toURI().toString()))
                            .into(imageView)
                }
            }
        }
    }

}
