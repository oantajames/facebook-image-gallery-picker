package com.imagepicker.facebook.jobs

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.callbacks.FacebookAlbumsRequestCallback
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import java.util.ArrayList

/**
 * @author james on 10/15/17.
 */
class AlbumsJob : BaseJob() {

    companion object {
        val ALBUMS_LIST = "ALBUMS_LIST"
        val HAS_MORES_PAGES = "HAS_MORE_PAGES"
        val SEND_ALBUM_LIST_BROADCAST = "SEND_ALBUM_LIST_BROADCAST"
    }

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        val albumCallback = FacebookAlbumsRequestCallback(object : FacebookAlbumsRequestCallback.AlbumsCallbackStatus {
            override fun onComplete(list: ArrayList<FacebookAlbum>, hasMorePages: Boolean) {
                val bundle = Bundle()
                bundle.putParcelableArrayList(AlbumsJob.ALBUMS_LIST, list)
                val intent = Intent(SEND_ALBUM_LIST_BROADCAST)
                intent.putExtras(bundle)
                intent.putExtra(AlbumsJob.HAS_MORES_PAGES, hasMorePages)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                jobFinished(jobParameters!!, false)
            }

            override fun onError() {
                jobFinished(jobParameters!!, true)
            }

        })
        val albumsRequest = FacebookAlbumsRequest(albumCallback)
        albumsRequest.onExecute()
        return true
    }
}
