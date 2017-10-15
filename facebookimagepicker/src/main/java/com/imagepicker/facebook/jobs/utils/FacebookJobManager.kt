package com.imagepicker.facebook.jobs.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.AccessToken
import com.firebase.jobdispatcher.*
import com.imagepicker.facebook.jobs.AlbumsJob
import com.imagepicker.facebook.jobs.LoginJob
import com.imagepicker.facebook.jobs.PhotosJob
import com.imagepicker.facebook.jobs.VerifyAccessTokenJob
import com.imagepicker.facebook.requests.FacebookLoginRequest

/**
 * @author james on 10/13/17.
 */

class FacebookJobManager private constructor() {

    val TAG = FacebookJobManager::class.java.simpleName

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: FacebookJobManager? = null

        fun getInstance(): FacebookJobManager {
            if (INSTANCE == null) {
                INSTANCE = FacebookJobManager()
            }
            return INSTANCE!!
        }
    }

    val ALBUMS_JOB = "ALBUMS_JOB"
    val PHOTOS_JOB = "PHOTOS_JOB"
    val LOGIN_JOB = "LOGIN_JOB"
    val VERIFY_ACCESS_TOKEN_JOB = "VERIFY_ACCESS_TOKEN_JOB"

    var wasInitCalled = false

    var currentJob = "currentJob"
    var currentAlbumId = "albumId"
    lateinit var activity: AppCompatActivity
    lateinit var dispatcher: FirebaseJobDispatcher

    fun init(newActivity: AppCompatActivity) {
        dispatcher = FirebaseJobDispatcher(GooglePlayDriver(newActivity))
        //we need to make sure the activity is set on this request!
        FacebookLoginRequest.getInstance().activity = newActivity
        wasInitCalled = true
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent, activity: AppCompatActivity) {
        val accessToken = AccessToken.getCurrentAccessToken()
        FacebookLoginRequest.getInstance().onActivityResult(requestCode, resultCode, data)
    }

    fun getAlbums() {
        if (wasInitCalled) {
            currentJob = ALBUMS_JOB
            startVerifyAccessTokenJob()
        } else {
            Log.e(TAG, "You must call init first!")
        }
    }

    fun getPhotos(albumId: String) {
        if (wasInitCalled) {
            currentJob = PHOTOS_JOB
            startVerifyAccessTokenJob(albumId)
        } else {
            Log.e(TAG, "You must call init first!")
        }
    }

    internal fun startVerifyAccessTokenJob() {
        val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                .setService(VerifyAccessTokenJob::class.java)
                .setTag(VERIFY_ACCESS_TOKEN_JOB)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        dispatcher.mustSchedule(verifyTokenJob)
    }

    internal fun startVerifyAccessTokenJob(albumId: String) {
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

    internal fun startLoginJob() {
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

    internal fun startAlbumsJob() {
        val albumsJob: Job? = dispatcher.newJobBuilder()
                .setService(AlbumsJob::class.java)
                .setTag(ALBUMS_JOB)
                .setTrigger(Trigger.executionWindow(0, 0))
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .build()
        dispatcher.mustSchedule(albumsJob)
    }


    internal fun startPhotosJob() {
        val photosJob: Job? = dispatcher.newJobBuilder()
                .setService(PhotosJob::class.java)
                .setTag(PHOTOS_JOB)
                .setExtras(putAlbumId(currentAlbumId))
                .setTrigger(Trigger.executionWindow(0, 0))
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .build()
        dispatcher.mustSchedule(photosJob)
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