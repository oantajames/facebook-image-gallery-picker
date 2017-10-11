package com.imagepicker.facebook.view.photos

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.model.FacebookPhoto
import com.squareup.picasso.Picasso

/**
 * @author james on 10/11/17.
 */
class FacebookPhotosAdapter : RecyclerView.Adapter<FacebookPhotosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_facebook_album, parent, false)
        return ViewHolder(itemView)
    }

    var albumList: MutableList<FacebookPhoto> = mutableListOf<FacebookPhoto>()

    fun addAllItems(list: MutableList<FacebookPhoto>) {
        albumList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (holder != null) {
            val item = albumList.get(position)
            holder.setItem(item)
            holder.bindView()
        }
    }

    override fun getItemCount(): Int {
        return albumList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var photoItem: FacebookPhoto

        fun setItem(item: FacebookPhoto) {
            photoItem = item
        }

        private var imageView: ImageView = itemView.findViewById(R.id.facebook_cover_photo)

        fun bindView() {
            Picasso.with(itemView.context)
                    .load(Uri.parse(photoItem.thumbnailUrl.toURI().toString()))
                    .fit()
                    .centerCrop()
                    .into(imageView)
        }

    }

}