package com.imagepicker.facebook.callbacks

import android.util.Log
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.imagepicker.facebook.BaseGraphRequest

/**
 * @author james on 10/11/17.
 */
class FacebookLoginResultCallback constructor(
        var pendingRequest: BaseGraphRequest<*>?
) : FacebookCallback<LoginResult> {
    private val TAG = FacebookLoginResultCallback::class.java.simpleName

    override fun onSuccess(loginResult: LoginResult) {
        Log.d(TAG, "onSuccess( loginResult = " + loginResult.toString() + " )")
        newAccessToken()
    }

    override fun onCancel() {
        Log.d(TAG, "onCancel()")
        if (pendingRequest != null) {
            pendingRequest!!.onCancel()
        }
    }

    override fun onError(facebookException: FacebookException) {
        Log.d(TAG, "onError( facebookException = $facebookException)", facebookException)
        if (pendingRequest != null) {
            pendingRequest!!.onError(facebookException)
        }
    }

    private fun newAccessToken() {
        if (pendingRequest != null) {
            val pendingRequest = pendingRequest
            this.pendingRequest = null
            pendingRequest?.onExecute()
        }
    }
}