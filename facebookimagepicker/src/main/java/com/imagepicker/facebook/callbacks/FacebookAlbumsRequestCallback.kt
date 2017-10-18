package com.imagepicker.facebook.callbacks

import android.util.Log
import com.facebook.AccessToken
import com.facebook.FacebookRequestError
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.imagepicker.facebook.jobs.utils.FacebookJobManager
import com.imagepicker.facebook.model.FacebookAlbum
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.util.ArrayList

/**
 * @author james on 10/11/17.
 */

open class FacebookAlbumsRequestCallback constructor(
        val callbackStatus: AlbumsCallbackStatus
) : GraphRequest.Callback {

    val JSON_NAME_DATA = "data"
    val JSON_NAME_ID = "id"

    interface AlbumsCallbackStatus {
        fun onComplete(list: ArrayList<FacebookAlbum>, hasMorePages: Boolean)
        fun onError()
    }

    private val TAG = FacebookAlbumsRequestCallback::class.java.simpleName
    private val JSON_NAME_ALBUM_NAME = "name"
    private val JSON_NAME_ALBUM_PHOTOS_COUNT = "count"

    override fun onCompleted(graphResponse: GraphResponse) {
        Log.d(TAG, "Graph response: " + graphResponse)

        val error = graphResponse.error
        if (checkForErrors(error, graphResponse)) return

        val responseJSONObject = graphResponse.jsonObject
        getResponseData(responseJSONObject, graphResponse)
    }

    private fun checkForErrors(error: FacebookRequestError?, graphResponse: GraphResponse): Boolean {
        if (error != null) {
            Log.e(TAG, "Received Facebook server error: " + error.toString())
            when (error.category) {
                FacebookRequestError.Category.LOGIN_RECOVERABLE -> {
                    callbackStatus.onError()
                    return true
                }
                FacebookRequestError.Category.TRANSIENT -> {
                    FacebookJobManager.getInstance().getAlbums()
                    callbackStatus.onError()
                    return true
                }
                else -> {
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
            val dataJSONArray = responseJSONObject.optJSONArray(JSON_NAME_DATA)
            val albumsList = ArrayList<FacebookAlbum>(dataJSONArray.length())
            for (albumIndex in 0 until dataJSONArray.length()) {
                try {
                    val albumJsonObject = dataJSONArray.getJSONObject(albumIndex)

                    val id = albumJsonObject.getString(JSON_NAME_ID)
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
            var nextGraphRequest = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT)
            if (nextGraphRequest != null) {
                FacebookJobManager.getInstance().nextPageGraphRequest = nextGraphRequest
            }
            callbackStatus.onComplete(albumsList, nextGraphRequest != null)
        } else {
            Log.e(TAG, "No JSON found in graph response")
        }
    }
}
