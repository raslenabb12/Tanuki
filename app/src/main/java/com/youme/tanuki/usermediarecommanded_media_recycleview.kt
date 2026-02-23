package com.youme.tanuki
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class userMyAdapterMediaRelated(
    private val onItemClicker: (AniListSyncManager.MediaRecommendation) -> Unit
) : ListAdapter<AniListSyncManager.MediaRecommendation, userMyAdapterMediaRelated.MyViewHolder>(MeidaDiffCallback2124) {
    private var originalList: List<AniListSyncManager.MediaRecommendation> = emptyList()
    class MyViewHolder(
        itemView: View,
        private val onItemClicker: (AniListSyncManager.MediaRecommendation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val chapter: TextView = itemView.findViewById(R.id.textView2)

        fun bind(manga: AniListSyncManager.MediaRecommendation) {
            chapter.visibility=View.GONE
            textView.text=manga.title.english?:manga.title.romaji
            Glide.with(itemView.context).load(manga.coverImage.large).thumbnail(0.25f).transition(
                DrawableTransitionOptions.withCrossFade()).into(imageView)
            itemView.setOnClickListener { onItemClicker(manga) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mangabox_related, parent, false)
        return MyViewHolder(view, onItemClicker)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val kdrama = getItem(position)
        holder.bind(kdrama)
        preloadNextItems(position,holder)
    }
    private fun preloadNextItems(position: Int, holder: MyViewHolder) {
        val preloadCount = 10
        val itemCount = currentList.size
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
    override fun submitList(list: List<AniListSyncManager.MediaRecommendation>?) {
        val initialLoad = list?.take(20)
        super.submitList(initialLoad)
        originalList = list ?: emptyList()
        Handler(Looper.getMainLooper()).postDelayed({
            super.submitList(originalList)
        }, 500)
    }
    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
    }
    object MeidaDiffCallback2124 : DiffUtil.ItemCallback<AniListSyncManager.MediaRecommendation>() {
        override fun areItemsTheSame(oldItem: AniListSyncManager.MediaRecommendation, newItem: AniListSyncManager.MediaRecommendation): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: AniListSyncManager.MediaRecommendation, newItem: AniListSyncManager.MediaRecommendation): Boolean {
            return oldItem == newItem
        }
    }
}
