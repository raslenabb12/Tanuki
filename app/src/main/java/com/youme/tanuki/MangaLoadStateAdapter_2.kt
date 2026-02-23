package com.youme.tanuki

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

class MangaLoadStateAdapter_2 : LoadStateAdapter<MangaLoadStateAdapter_2.LoadStateViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.sekelotn_manga_2, parent, false)
        )
    }
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    class LoadStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerFrameLayout = itemView.findViewById<ShimmerFrameLayout>(R.id.shrimmer)
        fun bind(loadState: LoadState) {
            shimmerFrameLayout.isVisible = loadState is LoadState.Loading
            if (loadState is LoadState.Loading) {
                shimmerFrameLayout.startShimmer()
            } else {
                shimmerFrameLayout.stopShimmer()
            }
        }
    }
}