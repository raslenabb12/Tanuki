package com.youme.tanuki

import android.content.res.Resources
import android.util.Log
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class CharactersPagingAdapter(private val onItemClicker: (CharacterNode) -> Unit) : PagingDataAdapter<CharacterNode, CharactersPagingAdapter.MangaViewHolder>(DIFF_CALLBACK) {
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
                        .load(manga.image?.large)
                        .preload()
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.characterbox, parent, false)
        return MangaViewHolder(view,onItemClicker)
    }

    class MangaViewHolder(itemView: View,private val onItemClicker: (CharacterNode) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView8)
        private val favnumber: TextView = itemView.findViewById(R.id.textView10)
        fun bind(character: CharacterNode) {

            favnumber.text=character.favourites.toString()
            Glide.with(itemView.context).load(character.image?.large).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
            textView.text=character.name?.full.toString()
            itemView.setOnClickListener { onItemClicker(character) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CharacterNode>() {
            override fun areItemsTheSame(oldItem: CharacterNode, newItem: CharacterNode): Boolean = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: CharacterNode, newItem: CharacterNode): Boolean = oldItem == newItem
        }
    }
}
