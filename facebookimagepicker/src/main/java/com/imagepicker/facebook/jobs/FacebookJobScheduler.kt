package com.imagepicker.facebook.jobs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.SingletonHolder
import com.imagepicker.facebook.callbacks.FacebookAlbumsRequestCallback
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import com.imagepicker.facebook.callbacks.FacebookPhotosRequestCallback
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import com.imagepicker.facebook.requests.FacebookLoginRequest
import com.imagepicker.facebook.requests.FacebookPhotosRequest


/**
 * @author james on 10/13/17.
 */

class FacebookJobScheduler
private constructor(var activity: AppCompatActivity) {

    companion object : SingletonHolder<FacebookJobScheduler, AppCompatActivity>(::FacebookJobScheduler)

    val ALBUMS_JOB = "ALBUMS_JOB"
    val PHOTOS_JOB = "PHOTOS_JOB"
    val LOGIN_JOB = "LOGIN_JOB"
    //set the current job when the getAlbums/getPhotos is called
    var currentJob = "currentJob"
    var currentAlbumId = "albumId"

    var dispatcher = FirebaseJobDispatcher(GooglePlayDriver(activity))

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
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(verifyTokenJob)
    }

    internal fun startVerifyAccessTokenJob(albumId: String) {
        val verifyTokenJob: Job? = dispatcher.newJobBuilder()
                .setService(VerifyAccessTokenJob::class.java)
                .setTag(LOGIN_JOB)
                .setExtras(putAlbumId(albumId))
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(verifyTokenJob)
    }


    inner class VerifyAccessTokenJob : BaseJob() {
        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken == null || accessToken.userId == null) {
                startLoginJob()
            } else {
                when (currentJob) {
                    ALBUMS_JOB -> startAlbumsJob()
                    PHOTOS_JOB -> startPhotosJob()
                }
            }
            return false
        }
    }

    fun startLoginJob() {
        val loginJob: Job? = dispatcher.newJobBuilder()
                .setService(LoginJob::class.java)
                .setTag(LOGIN_JOB)
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(loginJob)
    }

    inner class LoginJob : BaseJob() {
        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val loginRequest = FacebookLoginRequest.getInstance(activity)
            loginRequest.startLogin(activity, object : FacebookLoginResultCallback() {

                override fun onReqSuccess(loginResult: LoginResult) {
                    when (currentJob) {
                        ALBUMS_JOB -> startAlbumsJob()
                        PHOTOS_JOB -> startPhotosJob()
                    }
                    jobFinished(jobParameters!!, false)
                }

                override fun onReqCancel() {
                    //todo - ?
                }

                override fun onReqError(facebookException: FacebookException) {
                    jobFinished(jobParameters!!, true)
                }
            })
            return true
        }
    }

    internal fun startAlbumsJob() {
        val albumsJob: Job? = dispatcher.newJobBuilder()
                .setService(AlbumsJob::class.java)
                .setTag(ALBUMS_JOB)
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(albumsJob)
    }

    inner class AlbumsJob : BaseJob() {

        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val albumCallback = FacebookAlbumsRequestCallback(activity as FacebookCallFactory.AlbumsCallback
                    , activity,
                    object : FacebookAlbumsRequestCallback.AlbumsCallbackStatus {
                        override fun onComplete() {
                            startPhotosJob()
                            jobFinished(jobParameters!!, false)
                        }

                        override fun onError() {
                            jobFinished(jobParameters!!, true)
                        }

                    })
            val albumsRequest = FacebookAlbumsRequest(activity, albumCallback)
            albumsRequest.onExecute()
            return true
        }
    }

    inner class PhotosJob : BaseJob() {
        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            //TODO - how to make SURE that the acitivity has implemented the specific callback when we upcast it to the callback ?
            // Currently the activity can be either AlbumsActivity or PhotosActivity
            val photosCallback = FacebookPhotosRequestCallback(getAlbumId(jobParameters)!!,
                    activity as FacebookCallFactory.PhotosCallback,
                    activity,
                    object : FacebookPhotosRequestCallback.PhotosCallbackStatus {
                        override fun onComplete() {
                            jobFinished(jobParameters!!, false)
                        }

                        override fun onError() {
                            jobFinished(jobParameters!!, true)
                        }
                    })
            val photosRequest = FacebookPhotosRequest(getAlbumId(jobParameters)!!, activity, photosCallback)
            photosRequest.onExecute()
            return true
        }
    }

    internal fun startPhotosJob() {

        val photosJob: Job? = dispatcher.newJobBuilder()
                .setService(PhotosJob::class.java)
                .setTag(PHOTOS_JOB)
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(photosJob)
    }

    private fun getAlbumId(jobParameters: JobParameters?): String? {
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