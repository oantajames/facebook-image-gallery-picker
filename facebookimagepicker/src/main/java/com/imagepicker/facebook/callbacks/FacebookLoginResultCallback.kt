package com.imagepicker.facebook.callbacks

import android.util.Log
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.imagepicker.facebook.BaseGraphRequest

/**
 * @author james on 10/11/17.
 */
abstract class FacebookLoginResultCallback : FacebookCallback<LoginResult> {

    private val TAG = FacebookLoginResultCallback::class.java.simpleName

    abstract fun onReqSuccess(loginResult: LoginResult)
    abstract fun onReqCancel()
    abstract fun onReqError(facebookException: FacebookException)

    override fun onSuccess(loginResult: LoginResult) {
        Log.d(TAG, "onSuccess( loginResult = " + loginResult.toString() + " )")
        onReqSuccess(loginResult)
    }

    override fun onCancel() {
        Log.d(TAG, "onCancel()")
        onReqCancel()
    }

    override fun onError(facebookException: FacebookException) {
        Log.d(TAG, "onError( facebookException = $facebookException)", facebookException)
        onReqError(facebookException)
    }

    //todo - do i need this anymore ?!!!!!!!
    private fun newAccessToken() {
//        if (pendingRequest != null) {
//            val pendingRequest = pendingRequest
//            this.pendingRequest = null
//            pendingRequest?.onExecute()
//        }
    }
}