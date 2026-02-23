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

class recommandedMangaPagingAdapter(private val onItemClicker: (RecommendationNode) -> Unit) : PagingDataAdapter<RecommendationNode, recommandedMangaPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
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
                        .load(manga.mediaRecommendation?.coverImage?.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recommanded_manga_box, parent, false)
        return MangaViewHolder(view,onItemClicker)
    }
    class MangaViewHolder(itemView: View,private val onItemClicker: (RecommendationNode) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val chapter: TextView = itemView.findViewById(R.id.textView2)
        private val flag: ImageView = itemView.findViewById(R.id.imageView2)
        private val rating: TextView = itemView.findViewById(R.id.textView12)
        fun bind(manga: RecommendationNode) {
            if (manga.mediaRecommendation?.countryOfOrigin=="JP") flag.setImageResource(R.drawable.japanflag)
            if (manga.mediaRecommendation?.countryOfOrigin=="KR") flag.setImageResource(R.drawable.southkoreaflag)
            if (manga.mediaRecommendation?.countryOfOrigin=="CN") flag.setImageResource(R.drawable.china)
            chapter.visibility=View.GONE
            val titl = manga.mediaRecommendation?.title?.english?:manga.mediaRecommendation?.title?.romaji
            textView.text = titl
            rating.text=manga.rating.toString()
            Glide.with(itemView.context).load(manga.mediaRecommendation?.coverImage?.large).thumbnail(0.25f)
                .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
            itemView.setOnClickListener { onItemClicker(manga) }
        }
    }
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecommendationNode>() {
            override fun areItemsTheSame(oldItem: RecommendationNode, newItem: RecommendationNode): Boolean = oldItem.mediaRecommendation?.id == newItem.mediaRecommendation?.id
            override fun areContentsTheSame(oldItem: RecommendationNode, newItem: RecommendationNode): Boolean = oldItem == newItem
        }
    }
}
