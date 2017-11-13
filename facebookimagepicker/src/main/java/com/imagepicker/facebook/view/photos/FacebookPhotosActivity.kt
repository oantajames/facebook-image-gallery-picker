package com.imagepicker.facebook.view.photos

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.jobs.PhotosJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity

/**
 * @author james on 10/11/17.
 */

class FacebookPhotosActivity : AppCompatActivity(), BaseRecyclerAdapter.EndlessScrollListener, FacebookPhotosAdapter.PhotosAction {

    companion object {
        val FACEBOOK_PHOTO_ITEM = "FACEBOOK_PHOTO_ITEM"
    }

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    lateinit var facebookJobManager: FacebookJobManager
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookPhotosAdapter
    lateinit var progressBar: ProgressBar

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

        progressBar = findViewById(R.id.progress_bar)
        recyclerView = findViewById(R.id.facebook_recycler_view)
        facebookJobManager = FacebookJobManager.getInstance()
        adapter = FacebookPhotosAdapter(this@FacebookPhotosActivity)
        adapter.setEndlessScrollListener(this@FacebookPhotosActivity)


        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        if (albumId != null)
            facebookJobManager.getPhotos(albumId!!)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)
    }

    //todo - rxbroadcast
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == PhotosJob.BROADCAST_PHOTOS_SUCCESS) {
                if (intent.extras != null) {
                    val list: ArrayList<FacebookPhoto> = intent.extras.getParcelableArrayList(PhotosJob.PHOTOS_LIST)
                    Log.d(TAG, list.toString())
                    adapter.loadMoreItems = intent.extras.getBoolean(PhotosJob.HAS_NEXT_PAGE)
                    progressBar.visibility = View.INVISIBLE
                    adapter.addItems(list as MutableList<FacebookPhoto>)
                }
            } else if (action == PhotosJob.BROADCAST_PHOTOS_ERROR) {
                destroyAndNotifyUser()
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(PhotosJob.BROADCAST_PHOTOS_SUCCESS)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookJobManager.onActivityResult(requestCode, resultCode, data, this@FacebookPhotosActivity)
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
        FacebookJobManager.getInstance()
                .startPhotosJob(FacebookJobManager.getInstance().nextPageGraphRequest)

    }

    private fun destroyAndNotifyUser() {
        Toast.makeText(this@FacebookPhotosActivity, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        this@FacebookPhotosActivity.finish()
    }

    override fun onPhotosClicked(facebookItem: FacebookPhoto) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(FACEBOOK_PHOTO_ITEM, facebookItem)
        intent.putExtra(FACEBOOK_PHOTO_ITEM, bundle)
        setResult(Activity.RESULT_OK, intent)
        this@FacebookPhotosActivity.finish()
    }

}