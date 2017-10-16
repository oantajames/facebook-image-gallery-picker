package com.imagepicker.facebook.jobs.utils

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

/**
 * @author james on 10/13/17.
 *
 * Take a look here :
 * https://medium.com/google-developers/scheduling-jobs-like-a-pro-with-jobscheduler-286ef8510129
 */
abstract class BaseJob : JobService() {

    protected abstract fun onJobStart(jobParameters: JobParameters?): Boolean

    override fun onStopJob(p0: JobParameters?): Boolean {
        //onStopJob() is called by the system if the job is cancelled before being finished.
        // This generally happens when your job conditions are no longer being met,
        // such as when the device has been unplugged or if WiFi is no longer available.
        // So use this method for any safety checks and clean up you may need to do in response to a half-finished job.
        // Then, return true if you’d like the system to reschedule the job,
        // or false if it doesn’t matter and the system will drop this job.
        return true
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        //todo - maybe execute onJobStart on a background thread
        return onJobStart(p0)
    }

}