package com.imagepicker.facebook.view.albums

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.FacebookSdk
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.jobs.AlbumsJob
import com.imagepicker.facebook.jobs.LoginJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.photos.FacebookPhotosActivity

/**
 * @author james on 10/9/17.
 */
class FacebookAlbumsActivity : AppCompatActivity(),
        FacebookAlbumsAdapter.AlbumAction,
        BaseRecyclerAdapter.EndlessScrollListener {

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    val FACEBOOK_ALBUM_ID = "FACEBOOK_ALBUM_ID"
    val FACEBOOK_ALBUM_TITLE = "FACEBOOK_ALBUM_TITLE"

    val FACEBOOK_PHOTO_RESULT = 2223

    lateinit var facebookJobManager: FacebookJobManager
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookAlbumsAdapter
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)
        FacebookSdk.sdkInitialize(applicationContext)

        registerReceiver()
        FacebookJobManager.getInstance().init(this@FacebookAlbumsActivity)
        recyclerView = findViewById(R.id.facebook_recycler_view)
        progressBar = findViewById(R.id.progress_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Albums"

        facebookJobManager = FacebookJobManager.getInstance()

        initAdapter()
        facebookJobManager.getAlbums()
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(AlbumsJob.BROADCAST_ALBUM_SUCCESS)
        intentFilter.addAction(AlbumsJob.BROADCAST_ALBUM_ERROR)
        intentFilter.addAction(LoginJob.BROADCAST_FACEBOOK_LOGIN_ERROR)
        intentFilter.addAction(LoginJob.BROADCAST_FACEBOOK_LOGIN_CANCEL)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun initAdapter() {
        adapter = FacebookAlbumsAdapter(this)
        adapter.setEndlessScrollListener(this)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
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

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            facebookJobManager.onActivityResult(requestCode, resultCode, data, this@FacebookAlbumsActivity)
            if (requestCode == FACEBOOK_PHOTO_RESULT) {
                val bundle: Bundle = data.extras.getParcelable(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM)
                val facebookItem: FacebookPhoto = bundle.getParcelable(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM)
                setResult(facebookItem)
            }
        } else {
            Log.e(TAG, "OnActivityResult data is null!")
        }
    }

    override fun onLoadMore() {
        FacebookJobManager.getInstance()
                .startAlbumsJob(FacebookJobManager.getInstance().nextPageGraphRequest)
    }

    override fun onAlbumClicked(albumItem: FacebookAlbum) {
        val intent = Intent(baseContext, FacebookPhotosActivity::class.java)
        intent.putExtra(FACEBOOK_ALBUM_ID, albumItem.albumId)
        intent.putExtra(FACEBOOK_ALBUM_TITLE, albumItem.albumTitle)
        startActivityForResult(intent, FACEBOOK_PHOTO_RESULT)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                LoginJob.BROADCAST_FACEBOOK_LOGIN_ERROR -> {
                    destroyAndNotifyUser()
                }
                LoginJob.BROADCAST_FACEBOOK_LOGIN_CANCEL -> {
                    this@FacebookAlbumsActivity.finish()
                }
                AlbumsJob.BROADCAST_ALBUM_SUCCESS -> {
                    if (intent.extras != null) {
                        val list: ArrayList<FacebookAlbum> = intent.extras.getParcelableArrayList(AlbumsJob.ALBUMS_LIST)
                        Log.d(TAG, list.toString())
                        adapter.loadMoreItems = intent.extras.getBoolean(AlbumsJob.HAS_NEXT_PAGE)
                        progressBar.visibility = View.INVISIBLE
                        adapter.addItems(list as MutableList <FacebookAlbum>)
                    }
                }
                AlbumsJob.BROADCAST_ALBUM_ERROR -> {
                    destroyAndNotifyUser()
                }
            }
        }
    }

    private fun setResult(facebookPhoto: FacebookPhoto) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM, facebookPhoto)
        intent.putExtra(FacebookPhotosActivity.FACEBOOK_PHOTO_ITEM, bundle)
        intent.setAction("FACEBOOK")
        setResult(Activity.RESULT_OK, intent)
        this@FacebookAlbumsActivity.finish()
    }

    private fun destroyAndNotifyUser() {
        Toast.makeText(this@FacebookAlbumsActivity, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        this@FacebookAlbumsActivity.finish()
    }
}