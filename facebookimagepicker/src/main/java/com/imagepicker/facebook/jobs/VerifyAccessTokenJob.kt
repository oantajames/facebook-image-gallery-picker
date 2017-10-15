package com.imagepicker.facebook.jobs

import com.facebook.AccessToken
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.Trigger
import com.imagepicker.facebook.jobs.utils.BaseJob
import com.imagepicker.facebook.jobs.utils.FacebookJobManager

/**
 * @author james on 10/15/17.
 */

class VerifyAccessTokenJob : BaseJob() {

    override fun onJobStart(jobParameters: JobParameters?): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.userId == null) {
            FacebookJobManager.getInstance().startLoginJob()
        } else {
            when (FacebookJobManager.getInstance().currentJob) {
                FacebookJobManager.getInstance().ALBUMS_JOB -> FacebookJobManager.getInstance().startAlbumsJob()
                FacebookJobManager.getInstance().PHOTOS_JOB -> FacebookJobManager.getInstance().startPhotosJob()
            }
        }
        jobFinished(jobParameters!!, false)
        return false
    }

}
