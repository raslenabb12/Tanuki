package com.youme.tanuki
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MyAdapter4(
    private val onItemClicker: (MediaListItem) -> Unit,
    private val onlongItemPess: (MediaListItem) -> Unit
) : ListAdapter<MediaListItem, MyAdapter4.MyViewHolder>(recommantionDiffCallback) {
    private var originalList: List<MediaListItem> = emptyList()
    class MyViewHolder(
        itemView: View,
        private val onItemClicker: (MediaListItem) -> Unit,
        private val onlongItemPess: (MediaListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val chpatertext: TextView = itemView.findViewById(R.id.textView26)
        private val progressbar: ProgressBar = itemView.findViewById(R.id.progressBar3)
        private val fav: ImageView = itemView.findViewById(R.id.imageView33)
        fun bind(manga: MediaListItem) {
            textView.text=manga.media.title.english?:manga.media.title.romaji
            progressbar.max=manga.media.chapters?:0
            progressbar.progress=manga.progress
            if (manga.status=="COMPLETED"){
                if (manga.media.chapters!=null){
                    progressbar.max=manga.media.chapters
                    progressbar.progress=manga.progress
                }
                else{
                    progressbar.max=10
                    progressbar.progress=10
                }
            }
            if (manga.media.isFavourite == true){
                fav.visibility=View.VISIBLE
            }
            chpatertext.text=manga.progress.toString()+"/"+(manga.media.chapters?:"?").toString()
            Glide.with(itemView.context).load(manga.media.coverImage.large).thumbnail(0.25f).transition(
                DrawableTransitionOptions.withCrossFade(200)).into(imageView)
            itemView.setOnLongClickListener {
                onlongItemPess(manga)
                true
            }
            itemView.setOnClickListener { onItemClicker(manga) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.library_manga_box, parent, false)
        return MyViewHolder(view, onItemClicker,onlongItemPess)
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
                        .load(manga.media.coverImage.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }

    override fun submitList(list: List<MediaListItem>?) {
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
    object recommantionDiffCallback : DiffUtil.ItemCallback<MediaListItem>() {
        override fun areItemsTheSame(oldItem: MediaListItem, newItem: MediaListItem): Boolean {
            return oldItem.media.title == newItem.media.title
        }
        override fun areContentsTheSame(oldItem: MediaListItem, newItem: MediaListItem): Boolean {
            return oldItem == newItem
        }
    }
}
