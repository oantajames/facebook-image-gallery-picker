package com.imagepicker.facebook.model

import android.os.Parcel
import android.os.Parcelable
import java.net.URL

/**
 * @author james on 10/15/17.
 */

class FacebookPhoto : Parcelable {
    var photoUrl: URL
    private var thumbnailUrl: URL
    private var photoId: String

    constructor(thumbnailURL: URL, fullURL: URL, id: String) {
        photoUrl = thumbnailURL
        thumbnailUrl = fullURL
        photoId = id
    }

    constructor(`in`: Parcel) {
        photoUrl = `in`.readValue(URL::class.java.classLoader) as URL
        thumbnailUrl = `in`.readValue(URL::class.java.classLoader) as URL
        photoId = `in`.readValue(String::class.java.classLoader) as String
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(targetParcel: Parcel, flags: Int) {
        targetParcel.writeValue(photoUrl)
        targetParcel.writeValue(thumbnailUrl)
        targetParcel.writeValue(photoId)
    }

    companion object CREATOR : Parcelable.Creator<FacebookPhoto> {
        override fun createFromParcel(parcel: Parcel): FacebookPhoto {
            return FacebookPhoto(parcel)
        }

        override fun newArray(size: Int): Array<FacebookPhoto?> {
            return arrayOfNulls(size)
        }
    }
}
