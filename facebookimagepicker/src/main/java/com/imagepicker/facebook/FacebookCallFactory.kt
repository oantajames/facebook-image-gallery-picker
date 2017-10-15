package com.imagepicker.facebook

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.*
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import com.imagepicker.facebook.requests.FacebookLoginRequest
import com.imagepicker.facebook.requests.FacebookPhotosRequest

class FacebookCallFactory
private constructor(private var activity: AppCompatActivity) {
    //todo - look over this freaking part!
    val TAG = "FacebookCallFactory"

    private var nextGraphRequest: GraphRequest? = null
    var pendingRequest: BaseGraphRequest<*>? = null
    private var currentAlbumId: String? = null

    companion object : SingletonHolder<FacebookCallFactory, AppCompatActivity>(::FacebookCallFactory) {
        val JSON_NAME_DATA = "data"
        val JSON_NAME_ID = "id"
    }

//    companion object {
//
//        val JSON_NAME_DATA = "data"
//        val JSON_NAME_ID = "id"
//
//        @SuppressLint("StaticFieldLeak")
//        private var facebookJobScheduler: FacebookCallFactory? = null
//
//        fun getInstance(activity: AppCompatActivity): FacebookCallFactory {
//            if (facebookJobScheduler == null) {
//                facebookJobScheduler = FacebookCallFactory(activity)
//            }
//            if (facebookJobScheduler!!.activity != activity)
//                facebookJobScheduler!!.activity = activity;
//            return facebookJobScheduler!!
//        }
//    }

    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent, activity: AppCompatActivity) {
        val accessToken = AccessToken.getCurrentAccessToken()
        FacebookLoginRequest.getInstance(activity).onActivityResult(requestCode, resultCode, data)
    }

    fun executeRequest(request: BaseGraphRequest<*>) {
//        // If we don't have an access token - make a log-in request.
//        val accessToken = AccessToken.getCurrentAccessToken()
//        val loginRequest = FacebookLoginRequest.getInstance(activity)
//        if (loginRequest.startLogin(request, accessToken, pendingRequest, activity)) return
//
//        //todo - maybe create an AccessTokenVerification class for managing all this part
//        //Check if the access token has expired
//        if (accessToken != null) {
//            if (accessToken.isExpired) {
//                Log.i(TAG, "Access token has expired - refreshing")
//                pendingRequest = request
//                AccessToken.refreshCurrentAccessTokenAsync()
//                return
//            }
//        }
//        // Valid acces toke - > Execute request
//        request.onExecute()
    }

    fun getAlbums(albumsCallback: AlbumsCallback?) {
//        val albumsRequest = FacebookAlbumsRequest(pendingRequest, nextGraphRequest, albumsCallback, activity)
//        executeRequest(albumsRequest)
    }

    fun getPhotos(albumId: String, photosCallback: PhotosCallback?) {
//        currentAlbumId = albumId
//        val photosRequest = FacebookPhotosRequest(albumId,
//                pendingRequest,
//                nextGraphRequest,
//                photosCallback,
//                activity)
//        executeRequest(photosRequest)
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