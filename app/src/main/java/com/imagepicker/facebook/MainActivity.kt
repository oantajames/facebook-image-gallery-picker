package com.imagepicker.facebook

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button = findViewById<Button>(R.id.button2)

        button.setOnClickListener({
            var i = Intent(this, FacebookAlbumsActivity::class.java)
            startActivity(i)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }
}
