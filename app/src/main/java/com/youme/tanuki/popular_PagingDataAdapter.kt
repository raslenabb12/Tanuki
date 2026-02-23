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

class PopularMangaPagingAdapter(private val onItemClicker: (Manga) -> Unit) : PagingDataAdapter<Manga, PopularMangaPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
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
                        .load(manga.coverImage.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.popular_manga_box, parent, false)
        return MangaViewHolder(view,onItemClicker)
    }

    class MangaViewHolder(itemView: View,private val onItemClicker: (Manga) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val flag: ImageView = itemView.findViewById(R.id.imageView2)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val trading: TextView = itemView.findViewById(R.id.textView12)
        private val rankbox: CardView = itemView.findViewById(R.id.rank)
        fun bind(mangaItem: Manga) {
            if (mangaItem.countryOfOrigin=="JP") flag.setImageResource(R.drawable.japanflag)
            if (mangaItem.countryOfOrigin=="KR") flag.setImageResource(R.drawable.southkoreaflag)
            if (mangaItem.countryOfOrigin=="CN") flag.setImageResource(R.drawable.china)
            val titl = mangaItem.title.english?:mangaItem.title.romaji
            trading.text="#${mangaItem.calculatedRank}"
            trading.setTextColor(Color.WHITE)
            rankbox.backgroundTintList= ColorStateList.valueOf(Color.parseColor(mangaItem.coverImage.color?:"#111111"))
            textView.text = titl
            Glide.with(itemView.context).load(mangaItem.coverImage.large).transition(
                DrawableTransitionOptions.withCrossFade()).into(imageView)
            itemView.setOnClickListener { onItemClicker(mangaItem) }
        }
    }
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Manga>() {
            override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean = oldItem == newItem
        }
    }
}
