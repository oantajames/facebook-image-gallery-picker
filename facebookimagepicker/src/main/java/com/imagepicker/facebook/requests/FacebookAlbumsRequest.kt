package com.imagepicker.facebook.requests

import android.app.Activity
import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.imagepicker.facebook.BaseGraphRequest
import com.imagepicker.facebook.callbacks.FacebookAlbumsCallback
import com.imagepicker.facebook.FacebookCallFactory

/**
 * @author james on 10/11/17.
 */
class FacebookAlbumsRequest constructor(
        private var pendingRequest: BaseGraphRequest<*>?,
        private var nextGraphRequest: GraphRequest?,
        private var albumsCallback: FacebookCallFactory.AlbumsCallback?,
        private val activity: Activity
) : BaseGraphRequest<FacebookCallFactory.AlbumsCallback>(albumsCallback) {

    private val GRAPH_PATH_ME_ALBUMS = "me/albums"
    private val PARAMETER_NAME_FIELDS = "fields"
    private val PARAMETER_VALUE_FIELDS = "id,name,count,cover_photo"

    override fun onExecute() {

        val albumsRequestCallback = FacebookAlbumsCallback(pendingRequest, nextGraphRequest, albumsCallback, activity)
        if (nextGraphRequest != null) {
            nextGraphRequest!!.callback = albumsRequestCallback
            nextGraphRequest!!.executeAsync()
            nextGraphRequest = null
            return
        }

        val parameters = Bundle()
        parameters.putString(PARAMETER_NAME_FIELDS, PARAMETER_VALUE_FIELDS)
        GraphRequest(AccessToken.getCurrentAccessToken(),
                GRAPH_PATH_ME_ALBUMS,
                parameters,
                HttpMethod.GET,
                albumsRequestCallback).executeAsync()
    }
}
