package com.imagepicker.facebook.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import java.util.*

/**
 * @author james on 10/11/17.
 */
class FacebookLoginRequest {

    val TAG = FacebookLoginRequest::class.java.simpleName

    var activity: Activity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: FacebookLoginRequest? = null

        fun getInstance(): FacebookLoginRequest {
            if (INSTANCE == null) {
                INSTANCE = FacebookLoginRequest()
            }
            return INSTANCE!!
        }
    }

    private val PERMISSION_USER_PHOTOS = "user_photos"


    fun startLogin(loginResultCallback: FacebookLoginResultCallback, callbackManager:CallbackManager) {
        if (activity != null) {
            val loginManager = LoginManager.getInstance()
            //don't modify this to VAL otherwise the activity that implements the callback will be STATIC! ==> Memory leak!
            loginManager.registerCallback(callbackManager, loginResultCallback)
            loginManager.logInWithReadPermissions(activity, Arrays.asList(PERMISSION_USER_PHOTOS))
        } else {
            Log.e(TAG, "ACTIVITY IS NULL!")
        }
    }

}
