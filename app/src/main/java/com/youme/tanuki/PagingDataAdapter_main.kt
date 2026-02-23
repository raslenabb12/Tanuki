package com.youme.tanuki

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MangaPagingAdapter(private val onItemClicker: (Manga,CardView) -> Unit) : PagingDataAdapter<Manga, MangaPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            preloadNextItems(position,holder)
        }
    }
    private fun preloadNextItems(position: Int, holder: MangaViewHolder) {
        val preloadCount = 10
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mangabox, parent, false)
        return MangaViewHolder(view,onItemClicker,view.findViewById<CardView>(R.id.box))
    }

    class MangaViewHolder(
        itemView: View,
        private val onItemClicker: (Manga, CardView) -> Unit,
        findViewById: CardView
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val flag: ImageView = itemView.findViewById(R.id.imageView2)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val adult: TextView = itemView.findViewById(R.id.textView7)
        private val las_chapter: TextView = itemView.findViewById(R.id.textView2)
        private val box: CardView = itemView.findViewById(R.id.box)
        fun bind(mangaItem: Manga) {
            if (mangaItem.countryOfOrigin=="JP") flag.setImageResource(R.drawable.japanflag)
            if (mangaItem.countryOfOrigin=="KR") flag.setImageResource(R.drawable.southkoreaflag)
            if (mangaItem.countryOfOrigin=="CN") flag.setImageResource(R.drawable.china)
            if (mangaItem.chapters.toString()=="null"){
                las_chapter.visibility=View.GONE
            }
            val titl = mangaItem.title.english?:mangaItem.title.romaji
            textView.text = titl
            if (mangaItem.isAdult == true){adult.visibility=View.VISIBLE}else {adult.visibility=View.GONE}
            las_chapter.apply {
                text = "Chapter-${mangaItem.chapters}"
                visibility = if (mangaItem.chapters.toString()=="null") View.GONE else View.VISIBLE
            }
            Glide.with(itemView.context).load(mangaItem.coverImage.large).override(200, 300).transition(
                DrawableTransitionOptions.withCrossFade()).into(imageView)
            itemView.setOnClickListener { onItemClicker(mangaItem,box) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Manga>() {
            override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean = oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean = oldItem == newItem
        }
    }
}
