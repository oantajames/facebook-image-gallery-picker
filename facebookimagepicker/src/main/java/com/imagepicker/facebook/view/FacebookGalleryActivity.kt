package com.imagepicker.facebook.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.facebook.FacebookSdk
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.jobs.FacebookJobManager
import com.imagepicker.facebook.view.albums.FacebookAlbumsFragment
import com.imagepicker.facebook.view.photos.FacebookPhotosFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.Menu

class FacebookGalleryActivity : AppCompatActivity() {

    companion object {
        val FACEBOOK_ALBUM_ID = "FACEBOOK_ALBUM_ID"
        val FACEBOOK_ALBUM_TITLE = "FACEBOOK_ALBUM_TITLE"
        val FRAGMENT_PHOTOS_TAG = "PHOTOS"
        val FRAGMENT_ALBUMS_TAG = "ALBUMS"
        val FACEBOOK_PHOTO_ITEM = "FACEBOOK_PHOTO_ITEM"

        fun startAlbumsFragment(activity: FragmentActivity) {
            val albumsFragment = FacebookAlbumsFragment()
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, albumsFragment, FRAGMENT_ALBUMS_TAG)
            transaction.addToBackStack("")
            transaction.commit()
        }

        fun startPhotosFragment(activity: FragmentActivity, albumId: String, albumTitle: String) {
            val bundle = FacebookPhotosFragment.Companion.getBundle(albumId, albumTitle)
            val photosFragment = FacebookPhotosFragment()
            photosFragment.arguments = bundle
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, photosFragment, FRAGMENT_PHOTOS_TAG)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_gallery)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        FacebookSdk.sdkInitialize(applicationContext)
        FacebookJobManager.getInstance().attachActivity(this)

        startAlbumsFragment(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        val backstackEntryCount = supportFragmentManager.backStackEntryCount
        if (backstackEntryCount == 1 || backstackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val albumsFragment: Fragment? = supportFragmentManager.findFragmentByTag(FacebookGalleryActivity.Companion.FRAGMENT_ALBUMS_TAG)
        val photosFragment: Fragment? = supportFragmentManager.findFragmentByTag(FacebookGalleryActivity.Companion.FRAGMENT_PHOTOS_TAG)
        if (albumsFragment != null) {
            albumsFragment.onActivityResult(requestCode, resultCode, data)
        } else if (photosFragment != null) {
            photosFragment.onActivityResult(requestCode, resultCode, data)
        }
    }

}