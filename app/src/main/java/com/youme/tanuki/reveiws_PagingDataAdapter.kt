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

class reveiwsPagingAdapter(private val onItemClicker: (review) -> Unit,private val mediaid:Int?=null) : PagingDataAdapter<review, reveiwsPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
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
                        .load(manga.media.coverImage.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reviews_box, parent, false)
        return MangaViewHolder(view,onItemClicker,mediaid)
    }
    class MangaViewHolder(itemView: View,private val onItemClicker: (review) -> Unit,private val mediaid:Int?=null) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView26)
        private val useravater: ImageView = itemView.findViewById(R.id.imageView28)
        private val username: TextView = itemView.findViewById(R.id.textView29)
        private val title: TextView = itemView.findViewById(R.id.textView30)
        private val summri: TextView = itemView.findViewById(R.id.textView31)
        private val like: TextView = itemView.findViewById(R.id.textView34)
        private val dislike: TextView = itemView.findViewById(R.id.textView33)
        private val time: TextView = itemView.findViewById(R.id.textView28)
        fun bind(reveiw: review) {
            username.text=reveiw.user.name
            val instant = Instant.ofEpochSecond(reveiw.createdAt.toLong())
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            time.text=getTimeDifference(date)
            title.text="Review of ${reveiw.media.title.english?:reveiw.media.title.romaji}"
            summri.text=reveiw.summary
            like.text=reveiw.rating.toString()
            dislike.text=(reveiw.ratingAmount-reveiw.rating).toString()
            mediaid?.let {
                imageView.visibility=View.GONE
                title.visibility=View.GONE
            }
            Glide.with(itemView.context).load(reveiw.media.coverImage.large).transition(
                DrawableTransitionOptions.withCrossFade()).into(imageView)
            Glide.with(itemView.context).load(reveiw.user.avatar.large).transition(
                DrawableTransitionOptions.withCrossFade()).into(useravater)
            itemView.setOnClickListener { onItemClicker(reveiw) }
        }
        fun getTimeDifference(watchedDate: LocalDate): String {
            val currentDate = LocalDate.now()
            val days = ChronoUnit.DAYS.between(watchedDate, currentDate)
            val hours = ChronoUnit.HOURS.between(watchedDate.atStartOfDay(), LocalDateTime.now())
            val minutes = ChronoUnit.MINUTES.between(watchedDate.atStartOfDay(), LocalDateTime.now())
            val seconds = ChronoUnit.SECONDS.between(watchedDate.atStartOfDay(), LocalDateTime.now())

            return when {
                days > 0 -> {
                    when (days) {
                        1L -> "1 day ago"
                        2L -> "2 days ago"
                        in 3..10 -> "$days days ago"
                        else -> "$days days ago"
                    }
                }
                hours > 0 -> {
                    when (hours) {
                        1L -> "1 hour ago"
                        2L -> "2 hours ago"
                        in 3..10 -> "$hours hours ago"
                        else -> "$hours hours ago"
                    }
                }
                minutes > 0 -> {
                    when (minutes) {
                        1L -> "1 minute ago"
                        2L -> "2 minutes ago"
                        in 3..10 -> "$minutes minutes ago"
                        else -> "$minutes minutes ago"
                    }
                }
                seconds > 0 -> {
                    when (seconds) {
                        1L -> "1 second ago"
                        2L -> "2 seconds ago"
                        in 3..10 -> "$seconds seconds ago"
                        else -> "$seconds seconds ago"
                    }
                }
                else -> "Now"
            }
        }

    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<review>() {
            override fun areItemsTheSame(oldItem: review, newItem: review): Boolean = oldItem.media.title == newItem.media.title
            override fun areContentsTheSame(oldItem: review, newItem: review): Boolean = oldItem == newItem
        }
    }
}
