package com.imagepicker.facebook.jobs

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.FacebookCallFactory
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
        val HAS_MORES_PAGES = "HAS_MORE_PAGES"
    }

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        //TODO - how to make SURE that the acitivity has implemented the specific callback when we upcast it to the callback ?
        // Currently the activity can be either AlbumsActivity or PhotosActivity
        val photogetsCallback = FacebookPhotosRequestCallback(
                FacebookJobManager.getInstance().getAlbumId(jobParameters)!!,
                object : FacebookPhotosRequestCallback.PhotosCallbackStatus {
                    override fun onComplete(list: ArrayList<FacebookPhoto>, hasMorePages: Boolean) {
                        val bundle = Bundle()
                        bundle.putParcelableArrayList(PHOTOS_LIST, list)
                        val intent = Intent()
                        intent.putExtras(bundle)
                        intent.putExtra(HAS_MORES_PAGES, hasMorePages)
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                        //todo:register in the manifest the broadcast receiver
                        jobFinished(jobParameters!!, false)
                    }

                    override fun onError() {
                        jobFinished(jobParameters!!, true)
                    }
                })
        val photosRequest = FacebookPhotosRequest(FacebookJobManager.getInstance().getAlbumId(jobParameters)!!, photogetsCallback)
        photosRequest.onExecute()
        return true
    }
}
