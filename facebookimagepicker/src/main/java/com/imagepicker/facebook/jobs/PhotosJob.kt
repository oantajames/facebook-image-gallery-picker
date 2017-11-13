package com.imagepicker.facebook.jobs

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.callbacks.FacebookPhotosRequestCallback
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.requests.FacebookPhotosRequest

/**
 * @author james on 10/15/17.
 */
class PhotosJob : BaseJob() {

    companion object {
        val PHOTOS_LIST = "PHOTOS_LIST"
        val BROADCAST_PHOTOS_SUCCESS = "BROADCAST_PHOTOS_SUCCESS"
        val BROADCAST_PHOTOS_ERROR = "BROADCAST_PHOTOS_ERROR"
        val HAS_NEXT_PAGE = "HAS_NEXT_PAGE"
    }

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        val photogetsCallback = FacebookPhotosRequestCallback(
                FacebookJobManager.getInstance().getAlbumId(jobParameters)!!,
                object : FacebookPhotosRequestCallback.PhotosCallbackStatus {
                    override fun onComplete(list: ArrayList<FacebookPhoto>, hasMorePages: Boolean) {
                        sendSuccessBroadcast(list, hasMorePages)
                        jobFinished(jobParameters!!, false)
                    }

                    override fun onError() {
                        sendErrorBroadcast()
                        //don't reschedule the job, because we destroy the activity
                        jobFinished(jobParameters!!, false)
                    }
                })

        val photosRequest = FacebookPhotosRequest(FacebookJobManager.getInstance().getAlbumId(jobParameters)!!, photogetsCallback)
        //check if there is a next page request ready to use
        if (jobParameters != null) {
            if (jobParameters.extras != null && jobParameters.extras!!.getBoolean(PhotosJob.HAS_NEXT_PAGE)) {
                photosRequest.nextGraphRequest = FacebookJobManager.getInstance().nextPageGraphRequest!!
            }
        }
        photosRequest.onExecute()
        return true
    }

    private fun sendSuccessBroadcast(list: ArrayList<FacebookPhoto>, hasMorePages: Boolean) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(PHOTOS_LIST, list)
        val intent = Intent(BROADCAST_PHOTOS_SUCCESS)
        intent.putExtras(bundle)
        intent.putExtra(HAS_NEXT_PAGE, hasMorePages)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendErrorBroadcast() {
        val intent = Intent(PhotosJob.BROADCAST_PHOTOS_ERROR)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

}
