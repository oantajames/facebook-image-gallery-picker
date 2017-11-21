package com.imagepicker.facebook.jobs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.firebase.jobdispatcher.*
import com.imagepicker.facebook.jobs.jobslist.AlbumsJob
import com.imagepicker.facebook.jobs.jobslist.LoginJob
import com.imagepicker.facebook.jobs.jobslist.PhotosJob
import com.imagepicker.facebook.jobs.jobslist.VerifyAccessTokenJob
import com.imagepicker.facebook.requests.FacebookLoginRequest

/**
 * @author james on 10/13/17.
 */

class FacebookJobManager private constructor() {

    val TAG = FacebookJobManager::class.java.simpleName

    companion object {
        val BROADCAST_FACEBOOK_PHOTO_SELECTED = "BROADCAST_FACEBOOK_PHOTO_SELECTED"
        val FACEBOOK_PHOTO = "FACEBOOK_PHOTO"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: FacebookJobManager? = null

        fun getInstance(): FacebookJobManager {
            if (INSTANCE == null) {
                INSTANCE = FacebookJobManager()
            }
            return INSTANCE!!
        }
    }

    fun attachActivity(newActivity: Activity) {
        if (!isActivityAttached) {
            dispatcher = FirebaseJobDispatcher(GooglePlayDriver(newActivity.applicationContext))
            callbackManager = CallbackManager.Factory.create()
            //we need to make sure the activity is set on this request!
            FacebookLoginRequest.getInstance().activity = newActivity
            isActivityAttached = true
        } else {
            Log.e(TAG, "Activity is not attached to the facebook job manager!")
        }
    }

    val ALBUMS_JOB = "ALBUMS_JOB"
    val PHOTOS_JOB = "PHOTOS_JOB"
    val LOGIN_JOB = "LOGIN_JOB"
    val VERIFY_ACCESS_TOKEN_JOB = "VERIFY_ACCESS_TOKEN_JOB"

    var isActivityAttached = false

    var currentJob = "currentJob"
    var currentAlbumId = "albumId"
    var nextPageGraphRequest: GraphRequest? = null

    lateinit var activity: AppCompatActivity
    lateinit var dispatcher: FirebaseJobDispatcher
    lateinit var callbackManager: CallbackManager

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun getAlbums() {
        if (isActivityAttached) {
            currentJob = ALBUMS_JOB
            startVerifyAccessTokenJob()
        } else {
            Log.e(TAG, "You must call attachActivity first!")
        }
    }

    fun getPhotos(albumId: String) {
        if (isActivityAttached) {
            currentJob = PHOTOS_JOB
            startVerifyAccessTokenJob(albumId)
        } else {
            Log.e(TAG, "You must call attachActivity first!")
        }
    }

    // if nextGraphRequest is null, it means this is the first request!
    fun startAlbumsJob(nextGraphRequest: GraphRequest?) {
        if (isActivityAttached) {
            nextPageGraphRequest = nextGraphRequest
            val bundle = Bundle()
            bundle.putBoolean(AlbumsJob.HAS_NEXT_PAGE, nextGraphRequest != null)
            val albumsJob: Job? = dispatcher.newJobBuilder()
                    .setService(AlbumsJob::class.java)
                    .setTag(ALBUMS_JOB)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .setExtras(bundle)
                    .setConstraints(
                            Constraint.ON_ANY_NETWORK
                    )
                    .build()
            dispatcher.mustSchedule(albumsJob)
        }
    }

    // if nextGraphRequest is null, it means this is the first request!
    fun startPhotosJob(nextGraphRequest: GraphRequest?) {
        if (isActivityAttached) {
            nextPageGraphRequest = nextGraphRequest
            val bundle = Bundle()
            bundle.putBoolean(PhotosJob.HAS_NEXT_PAGE, nextGraphRequest != null)
            val albumsJob: Job? = dispatcher.newJobBuilder()
                    .setService(PhotosJob::class.java)
                    .setTag(ALBUMS_JOB)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .setExtras(bundle)
                    .setExtras(putAlbumId(currentAlbumId))
                    .setConstraints(
                            Constraint.ON_ANY_NETWORK
                    )
                    .build()
            dispatcher.mustSchedule(albumsJob)
        }
    }

    internal fun startVerifyAccessTokenJob() {
        if (isActivityAttached) {
            val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                    .setService(VerifyAccessTokenJob::class.java)
                    .setTag(VERIFY_ACCESS_TOKEN_JOB)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .build()
            dispatcher.mustSchedule(verifyTokenJob)
        }
    }

    internal fun startVerifyAccessTokenJob(albumId: String) {
        if (isActivityAttached) {
            currentAlbumId = albumId
            val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                    .setService(VerifyAccessTokenJob::class.java)
                    .setTag(VERIFY_ACCESS_TOKEN_JOB)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .setConstraints(
                            Constraint.ON_ANY_NETWORK
                    )
                    .build()
            dispatcher.mustSchedule(verifyTokenJob)
        }
    }

    internal fun startLoginJob() {
        if (isActivityAttached) {
            val loginJob: Job? = dispatcher.newJobBuilder()
                    .setService(LoginJob::class.java)
                    .setTag(LOGIN_JOB)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .setConstraints(
                            Constraint.ON_ANY_NETWORK
                    )
                    .build()
            dispatcher.mustSchedule(loginJob)
        }
    }

    internal fun getAlbumId(jobParameters: JobParameters?): String? {
        var extras: String? = null
        if (jobParameters != null) {
            extras = jobParameters.extras?.getString("ALBUM_ID")
        }
        return extras
    }

    internal fun putAlbumId(albumId: String): Bundle {
        val bundle = Bundle()
        bundle.putString("ALBUM_ID", albumId)
        return bundle
    }

}