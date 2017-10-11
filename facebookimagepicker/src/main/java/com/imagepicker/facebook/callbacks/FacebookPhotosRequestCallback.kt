package com.imagepicker.facebook.callbacks

import android.app.Activity
import android.util.Log
import com.facebook.FacebookRequestError
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.requests.FacebookPhotosRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * @author james on 10/10/17.
 */

class FacebookPhotosRequestCallback constructor(
        var albumId: String,
        var pendingRequest: BaseGraphRequest<*>?,
        var nextGraphRequest: GraphRequest?,
        private val photosCallback: FacebookCallFactory.PhotosCallback?,
        val activity: Activity
) : GraphRequest.Callback {

    val TAG = FacebookPhotosRequestCallback::class.java.toString()

    val JSON_NAME_PICTURE = "picture"
    val JSON_NAME_IMAGES = "images"
    val JSON_NAME_WIDTH = "width"
    val JSON_NAME_HEIGHT = "height"
    val JSON_NAME_SOURCE = "source"

    override fun onCompleted(graphResponse: GraphResponse) {
        Log.d(FacebookCallFactory.TAG, "Graph response: " + graphResponse)
        val error = graphResponse.error
        if (checkForErrors(error, graphResponse)) return

        val responseJSONObject = graphResponse.jsonObject
        getResponseData(responseJSONObject, graphResponse)
    }

    private fun getResponseData(responseJSONObject: JSONObject?, graphResponse: GraphResponse) {
        if (responseJSONObject != null) {
            Log.d(FacebookCallFactory.TAG, "Response object: " + responseJSONObject.toString())
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
                    Log.e(FacebookCallFactory.TAG, "Unable to extract photo data from JSON: " + responseJSONObject.toString(), je)
                } catch (mue: MalformedURLException) {
                    Log.e(FacebookCallFactory.TAG, "Invalid URL in JSON: " + responseJSONObject.toString(), mue)
                }

            }
            nextGraphRequest = graphResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT)
            if (photosCallback != null)
                photosCallback.onPhotosSuccess(photoArrayList, nextGraphRequest != null)
        } else {
            Log.e(FacebookCallFactory.TAG, "No JSON found in graph response")
        }
    }

    private fun checkForErrors(error: FacebookRequestError?, graphResponse: GraphResponse): Boolean {
        if (error != null) {
            Log.e(TAG, "Facebook error: " + error.toString())
            when (error.category) {
                FacebookRequestError.Category.LOGIN_RECOVERABLE -> {

                    Log.e(FacebookCallFactory.TAG, "Resolving LOGIN_RECOVERABLE error")
                    pendingRequest = FacebookPhotosRequest(albumId, pendingRequest, nextGraphRequest, photosCallback, activity)
                    LoginManager.getInstance().resolveError(activity, graphResponse)
                    return true
                }
                FacebookRequestError.Category.TRANSIENT -> {
                    FacebookCallFactory.getInstance(activity).getPhotos(albumId, photosCallback)
                    return true
                }
                else -> {
                    if (photosCallback != null)
                        photosCallback.onError(error.exception)
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