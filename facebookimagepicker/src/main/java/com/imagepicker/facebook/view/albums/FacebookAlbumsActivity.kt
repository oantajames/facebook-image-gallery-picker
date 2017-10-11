package com.imagepicker.facebook.view.albums

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.view.photos.FacebookPhotosActivity


/**
 * @author james on 10/9/17.
 */
class FacebookAlbumsActivity : Activity(), FacebookCallFactory.AlbumsCallback, FacebookAlbumsAdapter.AlbumAction {

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    val FACEBOOK_ALBUM_ID = "FACEBOOK_ALBUM_ID"

    lateinit var facebookCallFactory: FacebookCallFactory
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookAlbumsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)
        recyclerView = findViewById(R.id.facebook_recycler_view)
        facebookCallFactory = FacebookCallFactory.getInstance(this@FacebookAlbumsActivity)
        adapter = FacebookAlbumsAdapter(this)
        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        facebookCallFactory.getAlbums(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookCallFactory.onActivityResult(requestCode, resultCode, data)
    }

    override fun onError(exception: Exception) {
        //todo
    }

    override fun onCancel() {
        //todo
    }

    //todo - check the part with more albums + do this when user is scrolling. Also get the first 10 albums only
    override fun onAlbumsSuccess(albumsList: List<FacebookAlbum>, moreAlbums: Boolean) {
        Log.d(TAG, albumsList.toString())
        adapter.addAllItems(albumsList as MutableList<FacebookAlbum>)
        if (moreAlbums) {
            facebookCallFactory.getAlbums(this)
        }
    }

    override fun onAlbumClicked(albumId: String) {
        val intent = Intent(baseContext, FacebookPhotosActivity::class.java)
        intent.putExtra(FACEBOOK_ALBUM_ID, albumId)
        startActivity(intent)
    }


}