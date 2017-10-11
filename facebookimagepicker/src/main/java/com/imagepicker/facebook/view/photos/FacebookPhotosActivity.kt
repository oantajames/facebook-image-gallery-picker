package com.imagepicker.facebook.view.photos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity

/**
 * @author james on 10/11/17.
 */

class FacebookPhotosActivity : Activity(), FacebookCallFactory.PhotosCallback {
    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    lateinit var facebookCallFactory: FacebookCallFactory
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookPhotosAdapter
    var albumId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)
        recyclerView = findViewById(R.id.facebook_recycler_view)
        facebookCallFactory = FacebookCallFactory.getInstance(this@FacebookPhotosActivity)
        adapter = FacebookPhotosAdapter()
        val extras = intent.extras
        if (extras != null) {
            albumId = extras.getString(FacebookAlbumsActivity().FACEBOOK_ALBUM_ID)
        }
        // use a linear layout manager
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        if (albumId != null)
            facebookCallFactory.getPhotos(albumId!!, this@FacebookPhotosActivity)
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

    override fun onPhotosSuccess(facebookPhotoList: List<FacebookPhoto>, morePhotos: Boolean) {
        Log.d(TAG, facebookPhotoList.toString())
        //todo -add an on scroll listener and request next page if morePhotos is true
        adapter.addAllItems(facebookPhotoList as MutableList<FacebookPhoto>)
//        if (morePhotos && albumId != null) {
//            facebookCallFactory.getPhotos(albumId!!, this)
//        }
    }


}