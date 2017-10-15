package com.imagepicker.facebook.view.albums

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.facebook.FacebookSdk
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.jobs.AlbumsJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.photos.FacebookPhotosActivity

/**
 * @author james on 10/9/17.
 */
class FacebookAlbumsActivity : AppCompatActivity(),
        FacebookCallFactory.AlbumsCallback,
        FacebookAlbumsAdapter.AlbumAction,
        BaseRecyclerAdapter.EndlessScrollListener {

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    val FACEBOOK_ALBUM_ID = "FACEBOOK_ALBUM_ID"
    val FACEBOOK_ALBUM_TITLE = "FACEBOOK_ALBUM_TITLE"

    lateinit var facebookJobManager: FacebookJobManager
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookAlbumsAdapter
    lateinit var retryButton: Button
    var bollean: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)
        FacebookSdk.sdkInitialize(applicationContext)

        registerReceiver()
        FacebookJobManager.getInstance().init(this@FacebookAlbumsActivity)
        recyclerView = findViewById(R.id.facebook_recycler_view)


        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Albums"

        setRetryButton()

        facebookJobManager = FacebookJobManager.getInstance()

        initAdapter()
        facebookJobManager.getAlbums()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == AlbumsJob.SEND_ALBUM_LIST_BROADCAST) {
                if (intent.extras != null) {
                    val list: ArrayList<FacebookAlbum> = intent.extras.getParcelableArrayList(AlbumsJob.ALBUMS_LIST)
                    bollean = false
                    if (retryButton.visibility == View.VISIBLE)
                        retryButton.visibility = View.GONE
                    Log.d(TAG, list.toString())
                    adapter.loadMoreItems = intent.extras.getBoolean(AlbumsJob.HAS_MORES_PAGES)
                    adapter.addItems(list as MutableList <FacebookAlbum>)
                }
            } else {
                Log.e(TAG, "Bundle has no extras!")
            }
        }
    }


    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(AlbumsJob.SEND_ALBUM_LIST_BROADCAST)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setRetryButton() {
        retryButton = findViewById(R.id.retry_facebook_login)
        retryButton.setOnClickListener(View.OnClickListener {
            facebookJobManager.getAlbums()
        })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookJobManager.onActivityResult(requestCode, resultCode, data, this@FacebookAlbumsActivity)
    }

    override fun onLoadMore() {
        //todo -pagination is not working properly
        // facebookJobManager.getAlbums(this)
    }

    override fun onError(exception: Exception) {
        retryButton.visibility = View.VISIBLE
        val snackbar = Snackbar.make(recyclerView, "Ups! Something wrong happened, please try again.", Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onCancel() {
        retryButton.visibility = View.VISIBLE
    }

    override fun onAlbumsSuccess(albumsList: List<FacebookAlbum>, moreAlbums: Boolean) {

    }

    override fun onAlbumClicked(albumItem: FacebookAlbum) {
        val intent = Intent(baseContext, FacebookPhotosActivity::class.java)
        intent.putExtra(FACEBOOK_ALBUM_ID, albumItem.albumId)
        intent.putExtra(FACEBOOK_ALBUM_TITLE, albumItem.albumTitle)
        startActivity(intent)
    }

}