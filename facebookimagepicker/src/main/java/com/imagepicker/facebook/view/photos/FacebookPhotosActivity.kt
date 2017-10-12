package com.imagepicker.facebook.view.photos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.MenuItem
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity

/**
 * @author james on 10/11/17.
 */

class FacebookPhotosActivity : AppCompatActivity(), FacebookCallFactory.PhotosCallback, BaseRecyclerAdapter.EndlessScrollListener {

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    lateinit var facebookCallFactory: FacebookCallFactory
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookPhotosAdapter
    var albumId: String? = null
    var albumTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)
        val extras = intent.extras
        if (extras != null) {
            albumId = extras.getString(FacebookAlbumsActivity().FACEBOOK_ALBUM_ID)
            albumTitle = extras.getString(FacebookAlbumsActivity().FACEBOOK_ALBUM_TITLE)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = albumTitle

        recyclerView = findViewById(R.id.facebook_recycler_view)
        facebookCallFactory = FacebookCallFactory.getInstance(this@FacebookPhotosActivity)
        adapter = FacebookPhotosAdapter()
        adapter.setEndlessScrollListener(this@FacebookPhotosActivity)


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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            android.R.id.home -> {
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onLoadMore() {
        //todo- pagination is not working properly!
//        if (albumId != null)
//            facebookCallFactory.getPhotos(albumId!!, this)
    }

    override fun onError(exception: Exception) {
        //todo
    }

    override fun onCancel() {
        //todo
    }

    override fun onPhotosSuccess(facebookPhotoList: List<FacebookPhoto>, morePhotos: Boolean) {
        Log.d(TAG, facebookPhotoList.toString())
        adapter.loadMoreItems = morePhotos
        adapter.addItems(facebookPhotoList as MutableList<FacebookPhoto>)
    }

}