package com.imagepicker.facebook.view

import android.support.v7.widget.RecyclerView

/**
 * @author james on 10/12/17.
 */
abstract class BaseRecyclerAdapter<H : RecyclerView.ViewHolder, D> : RecyclerView.Adapter<H>() {

    private var endlessScrollListener: EndlessScrollListener? = null

    private var scrollState = RecyclerView.SCROLL_STATE_IDLE
    var loadMoreItems: Boolean? = null
    protected var items: MutableList<D> = mutableListOf()

    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            scrollState = newState
        }
    }

    fun addItems(items: List<D>) {
        val oldSize = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(oldSize, getItemCount());
    }

    fun clearAll() {
        items.clear()
        loadMoreItems = null
        notifyDataSetChanged()
    }

    fun getItem(position: Int): D? {
        return if (isValidPosition(position)) items[position] else null
    }

    private fun isValidPosition(position: Int): Boolean {
        return position == 0 || position > 0 && position < itemCount
    }

    fun getItemList(): List<D> {
        return ArrayList(items)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    val isEmpty: Boolean
        get() = itemCount <= 0

    fun setEndlessScrollListener(endlessScrollListener: EndlessScrollListener) {
        this.endlessScrollListener = endlessScrollListener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView!!.addOnScrollListener(recyclerScrollListener)
    }

    override fun onBindViewHolder(holder: H, position: Int) {
        onBindViewHolder(holder, getItem(position), position)
        if (scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            if (loadMoreItems != null && loadMoreItems as Boolean) {
                if (endlessScrollListener != null) {
                    endlessScrollListener!!.onLoadMore()
                }
            }
        }
    }

    protected abstract fun onBindViewHolder(holder: H, itemData: D?, position: Int)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    interface EndlessScrollListener {
        fun onLoadMore()
    }

}

