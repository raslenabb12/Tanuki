package com.youme.tanuki

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class recommanded_for_you : Fragment() {
    private lateinit var viewModeluserrecommnaded: MangaLibraryViewModel2
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapterrecommanded: userMyAdapterMediaRelated
    private lateinit var tokenManager: AniListTokenManager
    companion object {
        private val sharedPool = RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 30)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val rootView = inflater.inflate(R.layout.browse_manga_layout_recommanded_for_you, container, false)
        tokenManager = AniListTokenManager(requireContext())
        viewModeluserrecommnaded = MangaLibraryViewModel2(tokenManager.getAccessToken().toString())
        sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.res)
        val userid = sharedPreferences.getString("userid",null)
        viewModeluserrecommnaded.getuserrecommanded(userid?.toInt()!!)
        val swipeRefreshLayout= view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3).apply {
            initialPrefetchItemCount=20
        }
        adapterrecommanded=userMyAdapterMediaRelated{
                mangaItem->
            val intent = Intent(requireContext(), MangaInfo::class.java).apply {
                putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
                putExtra("imageurl", mangaItem.coverImage.large)
                putExtra("mangaid", mangaItem.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter=adapterrecommanded
        viewModeluserrecommnaded.getuserrecom.observe(viewLifecycleOwner){
            view.findViewById<View>(R.id.sekeleton).visibility=View.GONE
            adapterrecommanded.submitList(it.filter { it.mediaListEntry==null })
        }
        recyclerView.apply {
            layoutManager =gridLayoutManager
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0, 30)
            setItemViewCacheSize(20)
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        swipeRefreshLayout.setOnRefreshListener {
            viewModeluserrecommnaded.getuserrecommanded(userid?.toInt()!!)
            swipeRefreshLayout.isRefreshing=false
        }
    }
}
