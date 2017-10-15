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

//    companion object : SingletonHolder<FacebookJobManager, AppCompatActivity>(::FacebookJobManager)

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

    val TAG = FacebookJobManager::class.java.simpleName

    val ALBUMS_JOB = "ALBUMS_JOB"
    val PHOTOS_JOB = "PHOTOS_JOB"
    val LOGIN_JOB = "LOGIN_JOB"

    //set the current job when the getAlbums/getPhotos is called
    var currentJob = "currentJob"
    var currentAlbumId = "albumId"
    lateinit var activity: AppCompatActivity
    lateinit var dispatcher: FirebaseJobDispatcher
    var libraryInitiated: Boolean = false


    fun init(newActivity: AppCompatActivity) {
        if (!libraryInitiated) {
            dispatcher = FirebaseJobDispatcher(GooglePlayDriver(newActivity))
            //we need to make sure the activity is set on this request!
            FacebookLoginRequest.getInstance().activity = newActivity
            libraryInitiated = true
        } else {
            Log.e(TAG, "Library already initialised");
        }
    }

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent, activity: AppCompatActivity) {
        val accessToken = AccessToken.getCurrentAccessToken()
        FacebookLoginRequest.getInstance().onActivityResult(requestCode, resultCode, data)
    }

    fun getAlbums() {
        currentJob = ALBUMS_JOB
        startVerifyAccessTokenJob()
    }

    fun getPhotos(albumId: String) {
        currentJob = PHOTOS_JOB
        startVerifyAccessTokenJob(albumId)
    }

    internal fun startVerifyAccessTokenJob() {
        val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                .setService(VerifyAccessTokenJob::class.java)
                .setTag(LOGIN_JOB)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(verifyTokenJob)
    }

    internal fun startVerifyAccessTokenJob(albumId: String) {
        currentAlbumId = albumId
        val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                .setService(VerifyAccessTokenJob::class.java)
                .setTag("Verify_access_Token")
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(verifyTokenJob)
    }

    fun startLoginJob() {
        val loginJob: Job? = dispatcher.newJobBuilder()
                .setService(LoginJob::class.java)
                .setTag(LOGIN_JOB)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(loginJob)
    }

    internal fun startAlbumsJob() {
        val albumsJob: Job? = dispatcher.newJobBuilder()
                .setService(AlbumsJob::class.java)
                .setTag(ALBUMS_JOB)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(albumsJob)
    }


    internal fun startPhotosJob() {
        val photosJob: Job? = dispatcher.newJobBuilder()
                .setService(PhotosJob::class.java)
                .setTag(PHOTOS_JOB)
                .setExtras(putAlbumId(currentAlbumId))
                .setTrigger(Trigger.executionWindow(0, 0))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(photosJob)
    }

    fun getAlbumId(jobParameters: JobParameters?): String? {
        var extras: String? = null
        if (jobParameters != null) {
            extras = jobParameters.extras?.getString("ALBUM_ID")
        }
        return extras
    }

    private fun putAlbumId(albumId: String): Bundle {
        val bundle = Bundle()
        bundle.putString("ALBUM_ID", albumId)
        return bundle
    }

}