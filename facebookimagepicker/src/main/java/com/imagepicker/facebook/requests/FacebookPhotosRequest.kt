package com.imagepicker.facebook.requests

import android.app.Activity
import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.FacebookCallFactory
import com.imagepicker.facebook.callbacks.FacebookPhotosRequestCallback

/**
 * @author james on 10/11/17.
 */
class FacebookPhotosRequest constructor(
        private val albumId: String,
        private var pendingRequest: BaseGraphRequest<*>?,
        private var nextGraphRequest: GraphRequest?,
        private val photosCallback: FacebookCallFactory.PhotosCallback?,
        private val activity: Activity
) : BaseGraphRequest<FacebookCallFactory.PhotosCallback>(photosCallback) {

    private val GRAPH_PATH_ME_PHOTOS = "/me/photos"
    private val PARAMETER_NAME_TYPE = "type"
    private val PARAMETER_VALUE_TYPE = "uploaded"
    private val PARAMETER_NAME_FIELDS = "fields"
    private val PARAMETER_VALUE_FIELDS = "id,link,picture,images"

    public override fun onExecute() {
        // If we already have a next page request ready - execute it now. Otherwise make a new request
        val photosGraphRequestCallback = FacebookPhotosRequestCallback(
                albumId,
                pendingRequest,
                nextGraphRequest,
                photosCallback,
                activity)

        if (nextGraphRequest != null) {
            nextGraphRequest!!.callback = photosGraphRequestCallback
            nextGraphRequest!!.executeAsync()
            nextGraphRequest = null
            return
        }
        val parameters = Bundle()
        parameters.putString(PARAMETER_NAME_TYPE, PARAMETER_VALUE_TYPE)
        parameters.putString(PARAMETER_NAME_FIELDS, PARAMETER_VALUE_FIELDS)

        val request = GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + albumId + "/photos",
                parameters,
                HttpMethod.GET,
                photosGraphRequestCallback)

        request.executeAsync()
    }
}