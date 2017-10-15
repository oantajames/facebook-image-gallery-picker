package com.imagepicker.facebook.requests

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        private val activity: AppCompatActivity,
        private val callback: FacebookPhotosRequestCallback
) : BaseGraphRequest<FacebookCallFactory.PhotosCallback>(activity as FacebookCallFactory.PhotosCallback) {

    private val GRAPH_PATH_ME_PHOTOS = "/me/photos"
    private val PARAMETER_NAME_TYPE = "type"
    private val PARAMETER_VALUE_TYPE = "uploaded"
    private val PARAMETER_NAME_FIELDS = "fields"
    private val PARAMETER_VALUE_FIELDS = "id,link,picture,images"

    public override fun onExecute() {
        // If we already have a next page request ready - execute it now. Otherwise make a new request

        val parameters = Bundle()
        parameters.putString(PARAMETER_NAME_TYPE, PARAMETER_VALUE_TYPE)
        parameters.putString(PARAMETER_NAME_FIELDS, PARAMETER_VALUE_FIELDS)

        val request = GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + albumId + "/photos",
                parameters,
                HttpMethod.GET,
                callback)

        request.executeAsync()
    }
}