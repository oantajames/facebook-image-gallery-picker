package com.imagepicker.facebook.view.albums

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.jobs.jobslist.AlbumsJob
import com.imagepicker.facebook.jobs.jobslist.LoginJob
import com.imagepicker.facebook.jobs.FacebookJobManager
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.FacebookGalleryActivity

class FacebookAlbumsFragment : android.support.v4.app.Fragment(),
        FacebookAlbumsAdapter.AlbumAction,
        BaseRecyclerAdapter.EndlessScrollListener {

    val TAG: String = FacebookAlbumsFragment::class.java.simpleName

    lateinit var facebookJobManager: FacebookJobManager
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookAlbumsAdapter
    lateinit var progressBar: ProgressBar
    lateinit var supportActionBar: ActionBar


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_facebook_album_gallery, container, false)
        recyclerView = view.findViewById(R.id.facebook_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        supportActionBar = (activity as AppCompatActivity).supportActionBar!!

        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.title = "Albums"

        facebookJobManager = FacebookJobManager.getInstance()

        initAdapter()
        facebookJobManager.getAlbums()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            FacebookJobManager.getInstance().onActivityResult(requestCode, resultCode, data)
        } else {
            Log.e(TAG, "OnActivityResult data is null!")
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity.applicationContext).unregisterReceiver(broadcastReceiver)
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(AlbumsJob.BROADCAST_ALBUM_SUCCESS)
        intentFilter.addAction(AlbumsJob.BROADCAST_ALBUM_ERROR)
        intentFilter.addAction(LoginJob.BROADCAST_FACEBOOK_LOGIN_ERROR)
        intentFilter.addAction(LoginJob.BROADCAST_FACEBOOK_LOGIN_CANCEL)
        LocalBroadcastManager.getInstance(activity.applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun initAdapter() {
        adapter = FacebookAlbumsAdapter(this)
        adapter.setEndlessScrollListener(this)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }


    override fun onLoadMore() {
        FacebookJobManager.getInstance()
                .startAlbumsJob(FacebookJobManager.getInstance().nextPageGraphRequest)
    }

    override fun onAlbumClicked(albumItem: FacebookAlbum) {
        FacebookGalleryActivity.Companion.startPhotosFragment(activity, albumItem.albumId, albumItem.albumTitle)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                LoginJob.BROADCAST_FACEBOOK_LOGIN_ERROR -> {
                    destroyAndNotifyUser()
                }
                LoginJob.BROADCAST_FACEBOOK_LOGIN_CANCEL -> {
                    activity.finish()
                }
                AlbumsJob.BROADCAST_ALBUM_SUCCESS -> {
                    if (intent.extras != null) {
                        val list: ArrayList<FacebookAlbum> = intent.extras.getParcelableArrayList(AlbumsJob.ALBUMS_LIST)
                        Log.d(TAG, list.toString())
                        adapter.loadMoreItems = intent.extras.getBoolean(AlbumsJob.HAS_NEXT_PAGE)
                        progressBar.visibility = View.INVISIBLE
                        adapter.addItems(list as MutableList<FacebookAlbum>)
                    }
                }
                AlbumsJob.BROADCAST_ALBUM_ERROR -> {
                    destroyAndNotifyUser()
                }
            }
        }
    }

    private fun destroyAndNotifyUser() {
        Toast.makeText(activity, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        activity.finish()
    }
}