package com.imagepicker.facebook.jobs

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.callbacks.FacebookAlbumsCallback
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import com.imagepicker.facebook.requests.FacebookLoginRequest


/**
 * @author james on 10/13/17.
 */
class FacebookJobScheduler constructor(context: Context) {

    val ALBUMS_JOB = "ALBUMS_JOB"
    val PHOTOS_JOB = "PHOTOS_JOB"
    val LOGIN_JOB = "LOGIN_JOB"

    ///todo : order the jobs in a qeue and execute them accordingly
    var dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

    fun startPhotosJob() {
        //todo
    }


    inner class VerifyAccessTokenJob constructor(val jobType: String) : BaseJob() {
        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken == null || accessToken.userId == null) {
                startLoginJob()
            } else {
                when (jobType) {
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

    inner class LoginJob constructor(var activity: AppCompatActivity, val jobType: String) : BaseJob() {
        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val loginRequest = FacebookLoginRequest.getInstance(activity)
            loginRequest.startLogin(activity, object : FacebookLoginResultCallback() {

                override fun onReqSuccess(loginResult: LoginResult) {
                    when (jobType) {
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

    fun startAlbumsJob() {
        val albumsJob: Job? = dispatcher.newJobBuilder()
                .setService(AlbumsJob::class.java)
                .setTag(LOGIN_JOB)
                .build()
        //todo : set job constraints regarding the network
        dispatcher.mustSchedule(albumsJob)
    }

    inner class AlbumsJob constructor(var activity: AppCompatActivity) : BaseJob() {

        override fun onJobStart(jobParameters: JobParameters?): Boolean {
            val albumCallback = FacebookAlbumsCallback(activity as FacebookCallFactory.AlbumsCallback
                    , activity,
                    object : FacebookAlbumsCallback.AlbumsCallbackStatus {
                        override fun onComplete() {
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

}