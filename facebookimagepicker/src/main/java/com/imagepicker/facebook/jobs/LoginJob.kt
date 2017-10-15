package com.imagepicker.facebook.jobs

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
            }

            override fun onReqError(facebookException: FacebookException) {
                jobFinished(jobParameters!!, true)
            }
        })
        return true
    }
}