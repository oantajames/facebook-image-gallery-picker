package com.imagepicker.facebook.requests

import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.imagepicker.facebook.callbacks.FacebookAlbumsRequestCallback

/**
 * @author james on 10/11/17.
 */
class FacebookAlbumsRequest constructor(
        private val callback: FacebookAlbumsRequestCallback
) : BaseGraphRequest() {

    private val GRAPH_PATH_ME_ALBUMS = "me/albums"
    private val PARAMETER_NAME_FIELDS = "fields"
    private val PARAMETER_VALUE_FIELDS = "id,name,count,cover_photo"

    override fun onExecute() {
        val parameters = Bundle()
        parameters.putString(PARAMETER_NAME_FIELDS, PARAMETER_VALUE_FIELDS)
        GraphRequest(AccessToken.getCurrentAccessToken(),
                GRAPH_PATH_ME_ALBUMS,
                parameters,
                HttpMethod.GET,
                callback).executeAsync()
    }
}
