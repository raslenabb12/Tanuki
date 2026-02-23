package com.youme.tanuki

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class character_search : AppCompatActivity() {
    private lateinit var adapter2: MyAdapterMedia
    private lateinit var adapter: CharactersPagingAdapter
    private var initalload = 0
    private val loadStateAdapter = MangaLoadStateAdapter_characters()
    private val viewModel: MangaViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.character_search)
        setupLoadStateHandling()
        val recyclerView: RecyclerView = findViewById(R.id.reqs)
        val searchtext = findViewById<EditText>(R.id.editTextText)
        val searchbutton = findViewById<ImageButton>(R.id.imageButton4)
        adapter= CharactersPagingAdapter { character ->
            val bottomDrawer = BottomDrawerFragment(character)
            bottomDrawer.show(supportFragmentManager, bottomDrawer.tag)
        }
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            finish()
        }
        searchbutton.setOnClickListener {
            if (searchtext.text.isEmpty()) viewModel.clearSearch() else viewModel.setseachcharacte(searchtext.text.toString())
        }
        val gridLayoutManager = GridLayoutManager(this@character_search, 3).apply {
            initialPrefetchItemCount=20
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter.itemCount) 3 else 1
                }
            }
        }
        recyclerView.apply {
            layoutManager =gridLayoutManager
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0, 30)
            setItemViewCacheSize(20)
            adapter=this@character_search.adapter.withLoadStateFooter(
                footer = loadStateAdapter
            )
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        lifecycleScope.launch {
            viewModel.charactersFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
    private fun setupLoadStateHandling() {
        this@character_search.lifecycleScope.launch {
            this@character_search.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = loadStates.refresh is LoadState.Loading
                    if (loadStates.refresh is LoadState.Loading) {
                        loadStateAdapter.loadState = LoadState.Loading
                    }
                    val errorState = loadStates.refresh as? LoadState.Error
                        ?: loadStates.append as? LoadState.Error
                        ?: loadStates.prepend as? LoadState.Error
                    errorState?.error?.let { throwable ->
                        //showError(throwable.message ?: "")
                    }
                }
            }
        }
    }
}