package com.imagepicker.facebook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.*
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import com.imagepicker.facebook.requests.FacebookLoginRequest
import com.imagepicker.facebook.requests.FacebookPhotosRequest

class FacebookCallFactory
constructor(private val mActivity: Activity) {

    private var nextGraphRequest: GraphRequest? = null
    private var pendingRequest: BaseGraphRequest<*>? = null
    private var currentAlbumId: String? = null

    init {
        FacebookSdk.sdkInitialize(mActivity.applicationContext)
    }

    companion object {

        val TAG = "FacebookCallFactory"

        val JSON_NAME_DATA = "data"
        val JSON_NAME_ID = "id"

        @SuppressLint("StaticFieldLeak")
        private var facebookCallFactory: FacebookCallFactory? = null

        fun getInstance(activity: Activity): FacebookCallFactory {
            if (facebookCallFactory == null) {
                facebookCallFactory = FacebookCallFactory(activity)
            }
            return facebookCallFactory!!
        }
    }

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val accessToken = AccessToken.getCurrentAccessToken()
        //todo - make sure DATA is not null!
        FacebookLoginRequest.getInstance(pendingRequest, mActivity).onActivityResult(requestCode, resultCode, data)
    }

    fun executeRequest(request: BaseGraphRequest<*>) {
        // If we don't have an access token - make a log-in request.
        val accessToken = AccessToken.getCurrentAccessToken()
        val loginRequest = FacebookLoginRequest.getInstance(pendingRequest, mActivity)
        if (loginRequest.startedLoginProcess(request, accessToken)) return

        //todo - maybe create an AccessTokenVerification class for managing all this part
        //Check if the access token has expired
        if (accessToken != null) {
            if (accessToken.isExpired) {
                Log.i(TAG, "Access token has expired - refreshing")
                pendingRequest = request
                AccessToken.refreshCurrentAccessTokenAsync()
                return
            }
        }
        // We have a valid access token, so execute the request
        request.onExecute()
    }

    fun getAlbums(albumsCallback: AlbumsCallback?) {
        val albumsRequest = FacebookAlbumsRequest(pendingRequest, nextGraphRequest, albumsCallback, mActivity)
        executeRequest(albumsRequest)
    }

    fun getPhotos(albumId: String, photosCallback: PhotosCallback?) {
        currentAlbumId = albumId
        val photosRequest = FacebookPhotosRequest(albumId,
                pendingRequest,
                nextGraphRequest,
                photosCallback,
                mActivity)
        executeRequest(photosRequest)
    }

    fun resetFactory() {
        nextGraphRequest = null
    }

    interface BaseCallback {
        fun onError(exception: Exception)
        fun onCancel()
    }

    interface PhotosCallback : BaseCallback {
        fun onPhotosSuccess(facebookPhotoList: List<FacebookPhoto>, morePhotos: Boolean)
    }

    interface AlbumsCallback : BaseCallback {
        fun onAlbumsSuccess(albumsList: List<FacebookAlbum>, moreAlbums: Boolean)
    }


}