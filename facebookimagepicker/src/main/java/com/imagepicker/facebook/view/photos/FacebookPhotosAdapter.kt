package com.imagepicker.facebook.view.photos

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.imagepicker.facebook.facebookimagepicker.R
import com.imagepicker.facebook.model.FacebookPhoto
import com.imagepicker.facebook.view.BaseRecyclerAdapter
import com.squareup.picasso.Picasso

/**
 * @author james on 10/11/17.
 */
class FacebookPhotosAdapter : BaseRecyclerAdapter<FacebookPhotosAdapter.ViewHolder, FacebookPhoto>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_facebook_photo, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, itemData: FacebookPhoto?, position: Int) {
        if (itemData != null) {
            holder.setItem(itemData)
            holder.bindView()
        }
    }

    override fun getItemCount(): Int {
        return getItemList().count()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var photoItem: FacebookPhoto

        fun setItem(item: FacebookPhoto) {
            photoItem = item
        }

        private var imageView: ImageView = itemView.findViewById(R.id.facebook_photo)

        fun bindView() {
            Picasso.with(itemView.context)
                    .load(Uri.parse(photoItem.photoUrl.toURI().toString()))
                    .fit()
                    .centerCrop()
                    .into(imageView)
        }

    }

}