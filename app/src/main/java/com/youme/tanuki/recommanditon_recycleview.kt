package com.youme.tanuki
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MyAdapter2(
    private val onItemClicker: (RecommendationNode) -> Unit
) : ListAdapter<RecommendationNode, MyAdapter2.MyViewHolder>(recommantionDiffCallback) {
    private var originalList: List<RecommendationNode> = emptyList()
    class MyViewHolder(
        itemView: View,
        private val onItemClicker: (RecommendationNode) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recommanded_manga_box, parent, false)
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
                        .load(manga.mediaRecommendation?.coverImage?.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun submitList(list: List<RecommendationNode>?) {
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
    object recommantionDiffCallback : DiffUtil.ItemCallback<RecommendationNode>() {
        override fun areItemsTheSame(oldItem: RecommendationNode, newItem: RecommendationNode): Boolean {
            return oldItem.mediaRecommendation?.title == newItem.mediaRecommendation?.title
        }
        override fun areContentsTheSame(oldItem: RecommendationNode, newItem: RecommendationNode): Boolean {
            return oldItem == newItem
        }
    }
}
