package com.imagepicker.facebook.callbacks

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.AccessToken
import com.facebook.FacebookRequestError
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.requests.FacebookAlbumsRequest
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.util.ArrayList

/**
 * @author james on 10/11/17.
 */

open class FacebookAlbumsCallback constructor(
        val albumsCallback: FacebookCallFactory.AlbumsCallback?,
        val activity: AppCompatActivity,
        val callbackStatus: AlbumsCallbackStatus
) : GraphRequest.Callback {

    //used to tell the job that it is finished or to reschedule the job if an error ocurred
    interface AlbumsCallbackStatus {
        fun onComplete()
        fun onError()
    }

    private val TAG = FacebookAlbumsCallback::class.java.simpleName
    private val JSON_NAME_ALBUM_NAME = "name"
    private val JSON_NAME_ALBUM_PHOTOS_COUNT = "count"

    override fun onCompleted(graphResponse: GraphResponse) {
        Log.d(TAG, "Graph response: " + graphResponse)

        val error = graphResponse.error
        if (checkForErrors(error, graphResponse)) return

        val responseJSONObject = graphResponse.jsonObject
        getResponseData(responseJSONObject, graphResponse)
        callbackStatus.onComplete()
    }

    private fun checkForErrors(error: FacebookRequestError?, graphResponse: GraphResponse): Boolean {
        if (error != null) {
            Log.e(TAG, "Received Facebook server error: " + error.toString())
            when (error.category) {
                FacebookRequestError.Category.LOGIN_RECOVERABLE -> {
                    Log.e(TAG, "Attempting to resolve LOGIN_RECOVERABLE error")
                    LoginManager.getInstance().resolveError(activity, graphResponse)
                    callbackStatus.onError()
                    return true
                }
                FacebookRequestError.Category.TRANSIENT -> {
                    FacebookCallFactory.getInstance(activity).getAlbums(albumsCallback)
                    callbackStatus.onError()
                    return true
                }
                else -> {
                    if (albumsCallback != null)
                        albumsCallback.onError(error.exception)
                    callbackStatus.onError()
                    return true
                }
            }
        }
        return false
    }

    private fun getResponseData(responseJSONObject: JSONObject?, graphResponse: GraphResponse) {
        if (responseJSONObject != null) {
            Log.d(TAG, "Response object: " + responseJSONObject.toString())
            val dataJSONArray = responseJSONObject.optJSONArray(FacebookCallFactory.JSON_NAME_DATA)
            val albumsList = ArrayList<FacebookAlbum>(dataJSONArray.length())
            for (albumIndex in 0 until dataJSONArray.length()) {
                try {
                    val albumJsonObject = dataJSONArray.getJSONObject(albumIndex)

                    val id = albumJsonObject.getString(FacebookCallFactory.JSON_NAME_ID)
                    val name = albumJsonObject.getString(JSON_NAME_ALBUM_NAME)
                    val photosCount = albumJsonObject.getString(JSON_NAME_ALBUM_PHOTOS_COUNT)
                    //todo: improve this part
                    val coverPhotoUrl = "https://graph.facebook.com/" + id + "/picture?type=small&access_token=" + AccessToken.getCurrentAccessToken().token
                    val album = FacebookAlbum(id, coverPhotoUrl, photosCount, name)
                    albumsList.add(album)
                } catch (je: JSONException) {
                    Log.e(TAG, "Unable to extract photo from JSON: " + responseJSONObject.toString(), je)
                } catch (mue: MalformedURLException) {
                    Log.e(TAG, "Invalid URL in JSON: " + responseJSONObject.toString(), mue)
                }
            }
            //check if there are more pages - see FB docs for the GRAPH API
            val request = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT)
            if (albumsCallback != null)
                albumsCallback.onAlbumsSuccess(albumsList, request != null)
        } else {
            Log.e(TAG, "No JSON found in graph response")
        }
    }
}
