package com.imagepicker.facebook.callbacks

import android.util.Log
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.FacebookCallFactory

/**
 * @author james on 10/11/17.
 */
class FacebookLoginResultCallback constructor(
        var pendingRequest: BaseGraphRequest<*>?
) : FacebookCallback<LoginResult> {

    override fun onSuccess(loginResult: LoginResult) {
        Log.d(FacebookCallFactory.TAG, "onSuccess( loginResult = " + loginResult.toString() + " )")
        //todo -this part is not working
        newAccessToken()
    }

    override fun onCancel() {
        Log.d(FacebookCallFactory.TAG, "onCancel()")
        if (pendingRequest != null) {
            pendingRequest!!.onCancel()
        }
    }

    override fun onError(facebookException: FacebookException) {
        Log.d(FacebookCallFactory.TAG, "onError( facebookException = $facebookException)", facebookException)
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