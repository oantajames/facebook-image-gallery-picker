package com.imagepicker.facebook.view.photos

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.jobs.jobslist.PhotosJob
import com.imagepicker.facebook.jobs.FacebookJobManager
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.FacebookGalleryActivity
import android.support.v7.app.AppCompatActivity
import com.imagepicker.facebook.view.FacebookGalleryActivity.Companion.FACEBOOK_PHOTO_ITEM


class FacebookPhotosFragment : android.support.v4.app.Fragment(),
        BaseRecyclerAdapter.EndlessScrollListener, FacebookPhotosAdapter.PhotosAction {

    companion object {

        fun getBundle(albumId: String, albumTitle: String): Bundle {
            val bundle = Bundle()
            bundle.putString(FacebookGalleryActivity.Companion.FACEBOOK_ALBUM_ID, albumId)
            bundle.putString(FacebookGalleryActivity.Companion.FACEBOOK_ALBUM_TITLE, albumTitle)
            return bundle
        }
    }

    val TAG: String = FacebookPhotosFragment::class.java.simpleName

    lateinit var facebookJobManager: FacebookJobManager
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookPhotosAdapter
    lateinit var progressBar: ProgressBar
    lateinit var supportActionBar: ActionBar

    var albumId: String? = null
    var albumTitle: String? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_facebook_album_gallery, container, false)

        if (arguments != null) {
            albumId = arguments.getString(FacebookGalleryActivity.Companion.FACEBOOK_ALBUM_ID)
            albumTitle = arguments.getString(FacebookGalleryActivity.Companion.FACEBOOK_ALBUM_TITLE)
        }

        supportActionBar = (activity as AppCompatActivity).supportActionBar!!

        supportActionBar.title = albumTitle

        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.facebook_recycler_view)
        facebookJobManager = FacebookJobManager.getInstance()
        adapter = FacebookPhotosAdapter(this)
        adapter.setEndlessScrollListener(this)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        if (albumId != null)
            facebookJobManager.getPhotos(albumId!!)

        return view
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity.applicationContext).unregisterReceiver(broadcastReceiver)
    }

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
        LocalBroadcastManager.getInstance(activity.applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            FacebookJobManager.getInstance().onActivityResult(requestCode, resultCode, data)
        } else {
            Log.e(TAG, "OnActivityResult data is null!")
        }
    }

    override fun onLoadMore() {
        FacebookJobManager.getInstance()
                .startPhotosJob(FacebookJobManager.getInstance().nextPageGraphRequest)

    }

    private fun destroyAndNotifyUser() {
        Toast.makeText(activity, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        activity.finish()
    }

    override fun onPhotosClicked(facebookItem: FacebookPhoto) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putParcelable(FACEBOOK_PHOTO_ITEM, facebookItem)
        intent.putExtra(FACEBOOK_PHOTO_ITEM, bundle)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

}