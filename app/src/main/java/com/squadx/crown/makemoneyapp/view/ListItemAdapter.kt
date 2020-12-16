package com.squadx.crown.makemoneyapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squadx.crown.makemoneyapp.R
import com.squadx.crown.makemoneyapp.model.ListItem
import com.squadx.crown.makemoneyapp.util.ListItemType
import com.squadx.crown.makemoneyapp.view.viewholder.PrimaryLargeImageVH

class ListItemAdapter(private val mContext: Context, private val mDataset: List<ListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.li_primary_large_image, parent, false)
        return PrimaryLargeImageVH(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type == ListItemType.ARTICLE_URL || type == ListItemType.ARTICLE_HTML) {
            (holder as PrimaryLargeImageVH).bind(this, mDataset, mDataset[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mDataset[position].itemType
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }
}