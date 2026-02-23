package com.youme.tanuki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class following_frag : Fragment() {
    private val viewModel: MangaViewModel by viewModels()
    private var userid: Int? = null
    private lateinit var tokenManager: AniListTokenManager
    private lateinit var adapter: followersPagingAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val rootView = inflater.inflate(R.layout.browse_manga_layout, container, false)
        userid=arguments?.getInt("userid")
        tokenManager = AniListTokenManager(requireContext())
        userid?.let {
            viewModel.set_userid(userid)
            viewModel.set_accesstoken(tokenManager.getAccessToken())
        }
        adapter=followersPagingAdapter{user->
            val intent = Intent(requireContext(), user_profile_search::class.java).apply {
                putExtra("userid",user.id )
                putExtra("username",user.name )
            }
            startActivity(intent)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.following_pager.flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swipeRefreshLayout= view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val recyclerView: RecyclerView = view.findViewById(R.id.res)
        val gridLayoutManager = GridLayoutManager(requireContext(), 1).apply {
            initialPrefetchItemCount=20
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter.itemCount) 1 else 1
                }
            }
        }
        recyclerView.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0, 30)
            setItemViewCacheSize(20)
            adapter=this@following_frag.adapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.followers_pager.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                    recyclerView.scrollToPosition(0)
                }
            }

            swipeRefreshLayout.isRefreshing=false
        }

    }
    private fun onMangaItemClick(review: review) {
        val intent = Intent(requireContext(), review_full_page::class.java).apply {
            putExtra("username", review.user.name)
            putExtra("userpfp", review.user.avatar.large)
            putExtra("reviewid", review.id.toString())
        }
        startActivity(intent)
    }

}