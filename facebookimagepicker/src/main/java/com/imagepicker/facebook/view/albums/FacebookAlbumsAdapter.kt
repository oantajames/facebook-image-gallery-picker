package com.imagepicker.facebook.view.albums

/**
 * @author james on 10/11/17.
 */

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.imagepicker.facebook.facebookimagepicker.R
import com.squareup.picasso.Picasso
import android.view.LayoutInflater
import com.imagepicker.facebook.model.FacebookAlbum

/**
 * @author james on 10/11/17.
 */
class FacebookAlbumsAdapter constructor(var albumAction: AlbumAction) : RecyclerView.Adapter<FacebookAlbumsAdapter.ViewHolder>() {

    interface AlbumAction {
        fun onAlbumClicked(albumId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_facebook_album, parent, false)
        return ViewHolder(itemView)
    }

    var albumList: MutableList<FacebookAlbum> = mutableListOf<FacebookAlbum>()

    fun addAllItems(list: MutableList<FacebookAlbum>) {
        albumList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (holder != null) {
            val item = albumList.get(position)
            holder.setItem(item)
            holder.bindViews()
            holder.albumView.setOnClickListener {
                albumAction.onAlbumClicked(albumList[position].albumId)
            }
        }
    }

    override fun getItemCount(): Int {
        return albumList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var albumItem: FacebookAlbum

        fun setItem(item: FacebookAlbum) {
            albumItem = item
        }

        var albumView = itemView
        var imageView: ImageView = itemView.findViewById(R.id.facebook_cover_photo)
        var albumTitleTextView: TextView = itemView.findViewById(R.id.facebook_album_title)
        var albumCountTextView: TextView = itemView.findViewById(R.id.facebook_album_count)

        fun bindViews() {
            albumTitleTextView.setText(albumItem.albumTitle)
            albumCountTextView.setText(albumItem.albumCount)
            Picasso.with(albumView.context)
                    .load(albumItem.coverPhotoUrl)
                    .fit()
                    .centerCrop()
                    .into(imageView)
        }

    }

}