package com.imagepicker.facebook.jobs

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.requests.FacebookLoginRequest

/**
 * @author james on 10/15/17.
 */
class LoginJob : BaseJob() {

    companion object {
        val BROADCAST_FACEBOOK_LOGIN_ERROR = "BROADCAST_FACEBOOK_LOGIN_ERROR"
    }

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        val loginRequest = FacebookLoginRequest.getInstance()
        loginRequest.startLogin(object : FacebookLoginResultCallback() {
            override fun onReqSuccess(loginResult: LoginResult) {
                when (FacebookJobManager.getInstance().currentJob) {
                    FacebookJobManager.getInstance().ALBUMS_JOB -> FacebookJobManager.getInstance().startAlbumsJob()
                    FacebookJobManager.getInstance().PHOTOS_JOB -> FacebookJobManager.getInstance().startPhotosJob()
                }
                jobFinished(jobParameters!!, false)
            }

            override fun onReqCancel() {
                sendErrorBroadcast()
                jobFinished(jobParameters!!, false)
            }

            override fun onReqError(facebookException: FacebookException) {
                sendErrorBroadcast()
                //don't reschedule the job, because we destroy the activity
                jobFinished(jobParameters!!, false)
            }
        })
        return true
    }

    private fun sendErrorBroadcast() {
        val intent = Intent(LoginJob.BROADCAST_FACEBOOK_LOGIN_ERROR)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }


}