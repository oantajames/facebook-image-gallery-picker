package com.imagepicker.facebook.callbacks

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.facebook.FacebookRequestError
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.imagepicker.facebook.FacebookCallFactory
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import com.imagepicker.facebook.model.FacebookAlbum
import com.imagepicker.facebook.model.FacebookPhoto

/**
 * @author james on 10/10/17.
 */

class FacebookPhotosRequestCallback constructor(
        var albumId: String,
        val callbackStatus: PhotosCallbackStatus
) : GraphRequest.Callback {

    interface PhotosCallbackStatus {
        fun onComplete(list: ArrayList<FacebookPhoto>, hasMorePages: Boolean)
        fun onError()
    }

    val TAG = FacebookPhotosRequestCallback::class.java.toString()

    val JSON_NAME_PICTURE = "picture"
    val JSON_NAME_IMAGES = "images"
    val JSON_NAME_WIDTH = "width"
    val JSON_NAME_HEIGHT = "height"
    val JSON_NAME_SOURCE = "source"

    override fun onCompleted(graphResponse: GraphResponse) {
        Log.d(TAG, "Graph response: " + graphResponse)
        val error = graphResponse.error
        if (checkForErrors(error, graphResponse)) return

        val responseJSONObject = graphResponse.jsonObject
        getResponseData(responseJSONObject, graphResponse)
    }

    private fun getResponseData(responseJSONObject: JSONObject?, graphResponse: GraphResponse) {
        if (responseJSONObject != null) {
            Log.d(TAG, "Response object: " + responseJSONObject.toString())
            val dataJSONArray = responseJSONObject.optJSONArray(FacebookCallFactory.JSON_NAME_DATA)
            val photoArrayList = ArrayList<FacebookPhoto>(dataJSONArray.length())
            for (photoIndex in 0 until dataJSONArray.length()) {
                try {
                    val photoJSONObject = dataJSONArray.getJSONObject(photoIndex)

                    val id = photoJSONObject.getString(FacebookCallFactory.JSON_NAME_ID)
                    val picture = photoJSONObject.getString(JSON_NAME_PICTURE)
                    val imageJSONArray = photoJSONObject.getJSONArray(JSON_NAME_IMAGES)

                    val largestImageSource = getLargestImageSource(imageJSONArray)
                    val photo = FacebookPhoto(URL(picture), URL(largestImageSource), id)

                    photoArrayList.add(photo)
                } catch (je: JSONException) {
                    Log.e(TAG, "Unable to extract photo data from JSON: " + responseJSONObject.toString(), je)
                } catch (mue: MalformedURLException) {
                    Log.e(TAG, "Invalid URL in JSON: " + responseJSONObject.toString(), mue)
                }

            }
            val nextGraphRequest = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT)
            callbackStatus.onComplete(photoArrayList, nextGraphRequest != null)
        } else {
            Log.e(TAG, "No JSON found in graph response")
        }
    }

    private fun checkForErrors(error: FacebookRequestError?, graphResponse: GraphResponse): Boolean {
        if (error != null) {
            Log.e(TAG, "Facebook error: " + error.toString())
            when (error.category) {
                FacebookRequestError.Category.LOGIN_RECOVERABLE -> {
                    Log.e(TAG, "LOGIN_RECOVERABLE ERROR")
//                    LoginManager.getInstance().resolveError(activity, graphResponse)
                    callbackStatus.onError()
                    return true
                }
                FacebookRequestError.Category.TRANSIENT -> {
                    //todo : is it required to retry this here ? move the logic to the FaceookJobScheduler and handle it there
//                    FacebookCallFactory.getInstance(activity).getPhotos(albumId, photosCallback)
                    callbackStatus.onError()
                    return true
                }
                else -> {
//                    if (photosCallback != null)
//                        photosCallback.onError(error.exception)
                    callbackStatus.onError()
                    return true
                }
            }
        }
        return false
    }

    private fun getLargestImageSource(imageJSONArray: JSONArray?): String? {
        if (imageJSONArray == null) return null

        val imageCount = imageJSONArray.length()
        var largestImageWidth = 0
        var largestImageSource: String? = null

        for (imageIndex in 0 until imageCount) {
            val imageJSONObject = imageJSONArray.getJSONObject(imageIndex)
            val width = imageJSONObject.getInt(JSON_NAME_WIDTH)

            if (width > largestImageWidth) {
                largestImageWidth = width
                largestImageSource = imageJSONObject.getString(JSON_NAME_SOURCE)
            }
        }
        return largestImageSource
    }

}
