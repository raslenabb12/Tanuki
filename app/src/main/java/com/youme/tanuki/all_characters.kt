package com.youme.tanuki


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class all_characters : Fragment() {
    private lateinit var adapter: CharactersPagingAdapter
    private lateinit var adapter2: MyAdapterMedia
    private val loadStateAdapter = MangaLoadStateAdapter_characters()
    private val viewModel: MangaViewModel by viewModels()
    private var all_characters: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.browse_manga_layout, container, false)
        adapter=CharactersPagingAdapter(){Character->
            val bottomDrawer = BottomDrawerFragment(Character)
            bottomDrawer.show(parentFragmentManager, bottomDrawer.tag)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.charactersFlow.flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoadStateHandling()
        val swipeRefreshLayout= view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val recyclerView: RecyclerView = view.findViewById(R.id.res)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3).apply {
            initialPrefetchItemCount=20
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter.itemCount) 3 else 1
                }
            }
        }
        recyclerView.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0, 30)
            setItemViewCacheSize(20)
            adapter=this@all_characters.adapter.withLoadStateFooter(
                footer = loadStateAdapter
            )
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.charactersFlow.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                    recyclerView.scrollToPosition(0)
                }
            }

            swipeRefreshLayout.isRefreshing=false
        }
    }
    private fun setupLoadStateHandling() {
        all_characters=viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    if (loadStates.refresh is LoadState.Loading) {
                        loadStateAdapter.loadState = LoadState.Loading
                    }
                    val errorState = loadStates.refresh as? LoadState.Error
                        ?: loadStates.append as? LoadState.Error
                        ?: loadStates.prepend as? LoadState.Error
                    errorState?.error?.let { throwable ->
                        if(throwable.message.toString().indexOf("Unable to resolve host \"graphql.anilist.co\": No address associated with hostname")!=-1){
                            showError("No Internet Connection" ?: "")
                            requireView().findViewById<LinearLayout>(R.id.no_internet_layout).visibility=View.VISIBLE
                        }else  showError(throwable.message ?: "")
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        all_characters?.cancel()
        view?.findViewById<RecyclerView>(R.id.res)?.adapter=null
    }
    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { adapter.retry() }
            .show()
    }
}