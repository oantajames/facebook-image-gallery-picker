package com.imagepicker.facebook.jobs.jobslist

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.callbacks.FacebookAlbumsRequestCallback
import com.imagepicker.facebook.jobs.FacebookJobManager
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import java.util.ArrayList
import javax.inject.Inject

/**
 * @author james on 10/15/17.
 */
class AlbumsJob : BaseJob() {

    companion object {
        val ALBUMS_LIST = "ALBUMS_LIST"
        val BROADCAST_ALBUM_SUCCESS = "BROADCAST_ALBUM_SUCCESS"
        val BROADCAST_ALBUM_ERROR = "BROADCAST_ALBUM_ERROR"
        val HAS_NEXT_PAGE = "HAS_NEXT_PAGE"
    }

    lateinit var facebookJobManager: FacebookJobManager

    override fun onJobStart(jobParameters: JobParameters?): Boolean {

        val albumCallback = FacebookAlbumsRequestCallback(object : FacebookAlbumsRequestCallback.AlbumsCallbackStatus {
            override fun onComplete(list: ArrayList<FacebookAlbum>, hasMorePages: Boolean) {
                sendSuccessBroadcast(list, hasMorePages)
                jobFinished(jobParameters!!, false)
            }

            override fun onError() {
                sendErrorBroadcast()
                //don't reschedule the job, because we destroy the activity
                jobFinished(jobParameters!!, false)
            }
        })
        val albumsRequest = FacebookAlbumsRequest(albumCallback)
        //check if there is a next page request ready to use
        if (jobParameters != null) {
            if (jobParameters.extras != null && jobParameters.extras?.getBoolean(HAS_NEXT_PAGE)!!) {
                albumsRequest.nextGraphRequest = facebookJobManager.nextPageGraphRequest!!
            }
        }
        albumsRequest.onExecute()
        return true
    }

    private fun sendSuccessBroadcast(list: ArrayList<FacebookAlbum>, hasMorePages: Boolean) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(ALBUMS_LIST, list)
        val intent = Intent(BROADCAST_ALBUM_SUCCESS)
        intent.putExtras(bundle)
        intent.putExtra(HAS_NEXT_PAGE, hasMorePages)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendErrorBroadcast() {
        val intent = Intent(BROADCAST_ALBUM_ERROR)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}
