package com.imagepicker.facebook.view.albums

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.imagepicker.facebook.view.photos.FacebookPhotosActivity

/**
 * @author james on 10/9/17.
 */
class FacebookAlbumsActivity : AppCompatActivity(), FacebookCallFactory.AlbumsCallback, FacebookAlbumsAdapter.AlbumAction, BaseRecyclerAdapter.EndlessScrollListener {

    val TAG: String = FacebookAlbumsActivity::class.java.simpleName

    val FACEBOOK_ALBUM_ID = "FACEBOOK_ALBUM_ID"
    val FACEBOOK_ALBUM_TITLE = "FACEBOOK_ALBUM_TITLE"

    lateinit var facebookCallFactory: FacebookCallFactory
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FacebookAlbumsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_album_gallery)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Albums"

        recyclerView = findViewById(R.id.facebook_recycler_view)
        facebookCallFactory = FacebookCallFactory.getInstance(this@FacebookAlbumsActivity)
        adapter = FacebookAlbumsAdapter(this)
        adapter.setEndlessScrollListener(this)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        facebookCallFactory.getAlbums(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookCallFactory.onActivityResult(requestCode, resultCode, data)
    }

    override fun onLoadMore() {
        //todo -pagination is not working properly
//        facebookCallFactory.getAlbums(this)
    }

    override fun onError(exception: Exception) {
        //todo
    }

    override fun onCancel() {
        //todo
    }


    override fun onAlbumsSuccess(albumsList: List<FacebookAlbum>, moreAlbums: Boolean) {
        Log.d(TAG, albumsList.toString())
        adapter.loadMoreItems = moreAlbums
        adapter.addItems(albumsList as MutableList<FacebookAlbum>)

    }

    override fun onAlbumClicked(albumItem: FacebookAlbum) {
        val intent = Intent(baseContext, FacebookPhotosActivity::class.java)
        intent.putExtra(FACEBOOK_ALBUM_ID, albumItem.albumId)
        intent.putExtra(FACEBOOK_ALBUM_TITLE, albumItem.albumTitle)
        startActivity(intent)
    }


}