package com.youme.tanuki

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit

class followersPagingAdapter(private val onItemClicker: (userdetails) -> Unit) : PagingDataAdapter<userdetails, followersPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            preloadNextItems(position,holder)
        }
    }
    private fun preloadNextItems(position: Int, holder: MangaViewHolder) {
        val preloadCount = 5
        val itemCount = snapshot().items.size
        for (i in 1..preloadCount) {
            if (position + i >= itemCount) break
            try {
                val nextItem = getItem(position + i)
                nextItem?.let { manga ->
                    Glide.with(holder.itemView.context)
                        .load(manga.avatar.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.followers_box, parent, false)
        return MangaViewHolder(view,onItemClicker)
    }
    class MangaViewHolder(itemView: View,private val onItemClicker: (userdetails) -> Unit,private val mediaid:Int?=null) : RecyclerView.ViewHolder(itemView) {
        private val useravater: ImageView = itemView.findViewById(R.id.imageView39)
        private val username: TextView = itemView.findViewById(R.id.textView60)
        private val follow: Button = itemView.findViewById(R.id.button9)
        private val unfollow: Button = itemView.findViewById(R.id.button10)
        fun bind(user: userdetails) {
            Glide.with(itemView.context).load(user.avatar.large).into(useravater)
            username.text=user.name
            itemView.setOnClickListener {
                onItemClicker(user)
            }
            if (user.isFollowing==true){ follow.visibility=View.GONE;unfollow.visibility=View.VISIBLE}
        }

    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<userdetails>() {
            override fun areItemsTheSame(oldItem: userdetails, newItem: userdetails): Boolean = oldItem.id== newItem.id
            override fun areContentsTheSame(oldItem: userdetails, newItem: userdetails): Boolean = oldItem == newItem
        }
    }
}
