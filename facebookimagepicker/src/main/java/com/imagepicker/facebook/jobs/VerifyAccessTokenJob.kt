package com.imagepicker.facebook.jobs

import com.facebook.AccessToken
import com.firebase.jobdispatcher.JobParameters
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
            if (accessToken.declinedPermissions.contains("user_photos")) {
                FacebookJobManager.getInstance().startLoginJob()
            }
            when (FacebookJobManager.getInstance().currentJob) {
                FacebookJobManager.getInstance().ALBUMS_JOB -> FacebookJobManager.getInstance().startAlbumsJob(null)
                FacebookJobManager.getInstance().PHOTOS_JOB -> FacebookJobManager.getInstance().startPhotosJob(null)
            }
        }
        jobFinished(jobParameters!!, false)
        return false
    }

}
