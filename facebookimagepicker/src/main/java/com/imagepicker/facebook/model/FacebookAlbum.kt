package com.imagepicker.facebook.model

import android.os.Parcel
import android.os.Parcelable

/**
 * @author james on 10/10/17.
 */
class FacebookAlbum(val albumId: String, val coverPhotoUrl: String, val albumCount: String, val albumTitle: String) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(targetParcel: Parcel, flags: Int) {
        targetParcel.writeValue(albumId)
        targetParcel.writeValue(coverPhotoUrl)
        targetParcel.writeValue(albumCount)
        targetParcel.writeValue(albumTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FacebookAlbum> {
        override fun createFromParcel(parcel: Parcel): FacebookAlbum {
            return FacebookAlbum(parcel)
        }

        override fun newArray(size: Int): Array<FacebookAlbum?> {
            return arrayOfNulls(size)
        }
    }

}