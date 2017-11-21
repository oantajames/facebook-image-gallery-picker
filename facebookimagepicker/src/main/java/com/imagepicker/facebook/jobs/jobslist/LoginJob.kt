package com.imagepicker.facebook.jobs.jobslist

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.jobdispatcher.JobParameters
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import com.imagepicker.facebook.jobs.FacebookJobManager
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.requests.FacebookLoginRequest

/**
 * @author james on 10/15/17.
 */
class LoginJob : BaseJob() {

    companion object {
        val BROADCAST_FACEBOOK_LOGIN_ERROR = "BROADCAST_FACEBOOK_LOGIN_ERROR"
        val BROADCAST_FACEBOOK_LOGIN_CANCEL = "BROADCAST_FACEBOOK_LOGIN_CANCEL"
    }

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        val loginRequest = FacebookLoginRequest.getInstance()
        loginRequest.startLogin(object : FacebookLoginResultCallback() {
            override fun onReqSuccess(loginResult: LoginResult) {
                if (loginResult.recentlyDeniedPermissions.contains("user_photos")) {
                    Toast.makeText(baseContext, "We need all the permissions!", Toast.LENGTH_SHORT).show()
                    sendErrorBroadcast()
                    return
                }
                when (FacebookJobManager.getInstance().currentJob) {
                    FacebookJobManager.getInstance().ALBUMS_JOB -> FacebookJobManager.getInstance().startAlbumsJob(null)
                    FacebookJobManager.getInstance().PHOTOS_JOB -> FacebookJobManager.getInstance().startPhotosJob(null)
                }
                jobFinished(jobParameters!!, false)
            }

            override fun onReqCancel() {
                sendCancelBroadcast()
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

    private fun sendCancelBroadcast() {
        val intent = Intent(BROADCAST_FACEBOOK_LOGIN_ERROR)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendErrorBroadcast() {
        val intent = Intent(BROADCAST_FACEBOOK_LOGIN_ERROR)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

}