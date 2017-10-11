package com.imagepicker.facebook.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import java.util.*

/**
 * @author james on 10/11/17.
 */
class FacebookLoginRequest private constructor(
        private var pendingRequest: BaseGraphRequest<*>?,
        val activity: Activity) {

    //todo - check what happens if user doenst allow all permisions from facebook user_photos
    companion object {
        //todo - find a better solution for this part! -maybe move the callback manager and pass it here as an argumnet to the class constructor
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: com.imagepicker.facebook.requests.FacebookLoginRequest? = null

        fun getInstance(pendingRequest: BaseGraphRequest<*>?,
                        activity: Activity): com.imagepicker.facebook.requests.FacebookLoginRequest {
            if (INSTANCE == null) {
                INSTANCE = FacebookLoginRequest(pendingRequest, activity)
            }
            return INSTANCE!!
        }
    }

    private val PERMISSION_USER_PHOTOS = "user_photos"
    var callbackManager: CallbackManager = CallbackManager.Factory.create()

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun startedLoginProcess(request: BaseGraphRequest<*>, accessToken: AccessToken?): Boolean {
        if (accessToken == null || accessToken.userId == null) {
            val loginManager = LoginManager.getInstance()
            val callback = FacebookLoginResultCallback(pendingRequest)
            loginManager.registerCallback(callbackManager, callback)
            pendingRequest = request
            loginManager.logInWithReadPermissions(activity, Arrays.asList(PERMISSION_USER_PHOTOS))
            return true
        } else {
            return false
        }
    }

}