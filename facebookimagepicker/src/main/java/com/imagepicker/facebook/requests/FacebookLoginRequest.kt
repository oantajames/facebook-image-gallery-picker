package com.imagepicker.facebook.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.SingletonHolder
import com.imagepicker.facebook.callbacks.FacebookLoginResultCallback
import com.imagepicker.facebook.view.albums.FacebookAlbumsActivity
import java.util.*

/**
 * @author james on 10/11/17.
 */
class FacebookLoginRequest {

    val TAG = FacebookLoginRequest::class.java.simpleName

    var activity: AppCompatActivity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: com.imagepicker.facebook.requests.FacebookLoginRequest? = null

        fun getInstance(): com.imagepicker.facebook.requests.FacebookLoginRequest {
            if (INSTANCE == null) {
                INSTANCE = FacebookLoginRequest()
            }
            return INSTANCE!!
        }
    }

    private val PERMISSION_USER_PHOTOS = "user_photos"

    var callbackManager: CallbackManager = CallbackManager.Factory.create()

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun startLogin(loginResultCallback: FacebookLoginResultCallback) {
        if (activity != null) {
            val loginManager = LoginManager.getInstance()
            //don't modify this to VAL otherwise the activity that implements the callback will be STATIC! ==> Memory leak!
            loginManager.registerCallback(callbackManager, loginResultCallback)
            //todo : somehow send the activity here using the PROTOCOL
            loginManager.logInWithReadPermissions(activity, Arrays.asList(PERMISSION_USER_PHOTOS))
        } else {
            Log.e(TAG, "ACTIVITY IS NULL!")
        }
    }

}
