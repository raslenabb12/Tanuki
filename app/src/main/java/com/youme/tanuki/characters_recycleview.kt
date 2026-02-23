package com.youme.tanuki
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MyAdapter(
    private val onItemClicker: (CharacterEdge) -> Unit
) : ListAdapter<CharacterEdge, MyAdapter.MyViewHolder>(CharactersDiffCallback) {
    private var originalList: List<CharacterEdge> = emptyList()

    class MyViewHolder(
        itemView: View,
        private val onItemClicker: (CharacterEdge) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView8)
        private val favnumber: TextView = itemView.findViewById(R.id.textView10)
        private val main :CardView = itemView.findViewById(R.id.main)
        fun bind(character: CharacterEdge) {
            val layout =main.layoutParams
            layout.height=270
            main.layoutParams=layout
            favnumber.text=character.node?.favourites.toString()
            Glide.with(itemView.context).load(character.node?.image?.large).thumbnail(0.25f).transition(
                DrawableTransitionOptions.withCrossFade()).into(imageView)
            textView.text=character.node?.name?.full.toString()
            itemView.setOnClickListener { onItemClicker(character) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.characterbox, parent, false)
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
                        .load(manga.node?.image?.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
    override fun submitList(list: List<CharacterEdge>?) {
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
}
