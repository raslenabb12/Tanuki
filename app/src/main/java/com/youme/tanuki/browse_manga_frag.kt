package com.youme.tanuki

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class browse_manga_frag : Fragment() {
    private val viewModel: MangaViewModel by viewModels()
    private lateinit var viewModeluserrecommnaded: MangaLibraryViewModel2
    private val adapter: MangaPagingAdapter by lazy { MangaPagingAdapter(::onMangaItemClick) }
    private val loadStateAdapter = MangaLoadStateAdapter_vertiacle()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapterrecommanded: userMyAdapterMediaRelated
    private val popular_manga_adapter: PopularMangaPagingAdapter by lazy { PopularMangaPagingAdapter(::onMangaItemClic2) }
    private lateinit var tokenManager: AniListTokenManager
    private val reviews_adapter: reveiwsPagingAdapter by lazy { reveiwsPagingAdapter(::onreviewclick) }
    private val tredingadapter: TredingMangaPagingAdapter by lazy { TredingMangaPagingAdapter(::onMangaItemClic2) }
    companion object {
        private val sharedPool = RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 30)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val rootView = inflater.inflate(R.layout.browse_manga_layout2, container, false)
        tokenManager = AniListTokenManager(requireContext())
        viewModeluserrecommnaded = MangaLibraryViewModel2(tokenManager.getAccessToken().toString())
        sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mangaFlow.collectLatest {
                        adapter.submitData(it)
                    }
                }
                launch {
                    viewModel.popular_manga_home.collectLatest { pagingData ->
                        popular_manga_adapter.submitData(pagingData)
                    }
                }
                launch {
                    viewModel.Trending_manga_home.collectLatest { pagingData ->
                        tredingadapter.submitData(pagingData)
                    }
                }
                launch {
                    viewModel.getreviews_home.collectLatest { pagingData ->
                        reviews_adapter.submitData(pagingData)
                    }
                }

            }
        }
        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoadStateHandling()
        view.findViewById<ImageButton>(R.id.imageButton10).setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.fragmentStateManager.switchFragment(
                    recommanded_for_you(),
                    R.id.fragmentContainerView
                )
                mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Recommended For You"
                mainActivity.findViewById<ImageButton>(R.id.imageButton5)?.visibility = View.GONE
                mainActivity.findViewById<ImageButton>(R.id.imageButton6)?.visibility = View.GONE
            }
        }
        view.findViewById<ImageButton>(R.id.imageButton12).setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.fragmentStateManager.switchFragment(
                    trending_manga_frag(),
                    R.id.fragmentContainerView
                )
                mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Trending Manga"
                mainActivity.findViewById<ImageButton>(R.id.imageButton5)?.visibility = View.GONE
                mainActivity.findViewById<ImageButton>(R.id.imageButton6)?.visibility = View.VISIBLE
            }
        }
        view.findViewById<ImageButton>(R.id.imageButton103).setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.fragmentStateManager.switchFragment(
                    popular_manga_frag(),
                    R.id.fragmentContainerView
                )
                mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Popular Manga"
                mainActivity.findViewById<ImageButton>(R.id.imageButton5)?.visibility = View.GONE
                mainActivity.findViewById<ImageButton>(R.id.imageButton6)?.visibility = View.VISIBLE
            }
        }
        view.findViewById<ImageButton>(R.id.imageButton106).setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.fragmentStateManager.switchFragment(
                    reveiws_frag(),
                    R.id.fragmentContainerView
                )
                mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Reveiws"
                mainActivity.findViewById<ImageButton>(R.id.imageButton5)?.visibility = View.GONE
                mainActivity.findViewById<ImageButton>(R.id.imageButton6)?.visibility = View.GONE
            }
        }
        //view.findViewById<ImageButton>(R.id.imageButton13)
        val popularmanga_recycleview: RecyclerView = view.findViewById(R.id.recom4)
        popularmanga_recycleview.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        popularmanga_recycleview.adapter=popular_manga_adapter
        PagerSnapHelper().attachToRecyclerView(popularmanga_recycleview)
        popularmanga_recycleview.addItemDecoration(HorizontalSpaceItemDecoration2( (50 * resources.displayMetrics.density).toInt()))
        popularmanga_recycleview.addItemDecoration(HorizontalSpaceItemDecoration( (240 * resources.displayMetrics.density).toInt()))
        popularmanga_recycleview.addItemDecoration(ScaleItemDecoration())
        val itemWidth = (120 * resources.displayMetrics.density).toInt()
        val userid = sharedPreferences.getString("userid",null)
        userid?.let {
            view.findViewById<LinearLayout>(R.id.recommanded_box).visibility=View.VISIBLE
            viewModeluserrecommnaded.getuserrecommanded(userid?.toInt()!!)
            viewModeluserrecommnaded.getuserrecom.observe(viewLifecycleOwner){
                view.findViewById<View>(R.id.sekelton_1).visibility=View.GONE
                adapterrecommanded.submitList(it.filter { it.mediaListEntry==null }.take(10))
            }
        }
        val recyclerView2: RecyclerView = view.findViewById(R.id.recom)
        adapterrecommanded=userMyAdapterMediaRelated { manga->
            onMangaItemClic3(manga)
        }
        LinearSnapHelper().attachToRecyclerView(recyclerView2)
        val recyclerView3: RecyclerView = view.findViewById(R.id.trendig_manga)
        recyclerView3.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView3.addItemDecoration(HorizontalSpaceItemDecoration( itemWidth))
        recyclerView2.addItemDecoration(HorizontalSpaceItemDecoration(itemWidth))
        recyclerView3.adapter=tredingadapter
        LinearSnapHelper().attachToRecyclerView(recyclerView3)
        val recyclerView5: RecyclerView = view.findViewById(R.id.recent_added_manga)
        recyclerView5.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView5.adapter=adapter
        LinearSnapHelper().attachToRecyclerView(recyclerView5)
        recyclerView5.addItemDecoration(HorizontalSpaceItemDecoration(itemWidth))



        val recyclerView4: RecyclerView = view.findViewById(R.id.recent_reviews)
        recyclerView4.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView4.adapter=reviews_adapter
        PagerSnapHelper().attachToRecyclerView(recyclerView4)
        recyclerView2.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView2.adapter=adapterrecommanded
        val swipeRefreshLayout= view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
//        val recyclerView: RecyclerView = view.findViewById(R.id.res)
//        val gridLayoutManager = GridLayoutManager(requireContext(), 3).apply {
//            initialPrefetchItemCount=20
//            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//                override fun getSpanSize(position: Int): Int {
//                    return if (position == adapter.itemCount) 3 else 1
//                }
//            }
//        }
//        recyclerView.apply {
//            layoutManager =gridLayoutManager
//            setHasFixedSize(true)
//            setRecycledViewPool(sharedPool)
//            setItemViewCacheSize(10)
//            adapter=this@browse_manga_frag.adapter.withLoadStateFooter(
//                footer = loadStateAdapter
//            )
//            viewTreeObserver.addOnPreDrawListener {
//                startPostponedEnterTransition()
//                true
//            }
//        }
//        swipeRefreshLayout.setOnRefreshListener {
//            viewLifecycleOwner.lifecycleScope.launch {
//                viewModel.mangaFlow.collectLatest { pagingData ->
//                    adapter.submitData(pagingData)
//                    recyclerView.scrollToPosition(0)
//                }
//            }
//
//            swipeRefreshLayout.isRefreshing=false
//        }
//        adapter.addLoadStateListener { loadState ->
//
//        }
    }
    class HorizontalSpaceItemDecoration(private val itemWidth: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            view.layoutParams.width = itemWidth
        }
    }
    class HorizontalSpaceItemDecoration2(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = space
            }
        }
    }
    private fun setupLoadStateHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tredingadapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {

                            loadStateAdapter.loadState = LoadState.Loading
                        } else {

                            requireView().findViewById<View>(R.id.sekelaton_2).visibility = View.GONE
                        }
                    }
                }
                launch {
                    popular_manga_adapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {
                            loadStateAdapter.loadState = LoadState.Loading
                            //requireView().findViewById<LinearLayout>(R.id.no_internet_layout).visibility=View.GONE
                        } else {
                            requireView().findViewById<View>(R.id.sekelton_1_2).visibility = View.GONE
                        }
                    }
                }
                launch {
                    adapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {
                            requireView().findViewById<ScrollView>(R.id.scroll_main).visibility=View.VISIBLE
                            loadStateAdapter.loadState = LoadState.Loading
                            //requireView().findViewById<LinearLayout>(R.id.no_internet_layout).visibility=View.GONE
                        } else {
                            requireView().findViewById<View>(R.id.sekelaton_3).visibility =
                                View.GONE
                        }
                        //requireView().findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = loadStates.refresh is LoadState.Loading
                        val errorState = loadStates.refresh as? LoadState.Error
                            ?: loadStates.append as? LoadState.Error
                            ?: loadStates.prepend as? LoadState.Error
                        errorState?.error?.let { throwable ->
                            if (throwable.message.toString()
                                    .indexOf("Unable to resolve host \"graphql.anilist.co\": No address associated with hostname") != -1
                            ) {
                                showError("No Internet Connection" ?: "")

                            } else showError(throwable.message ?: "")
                            requireView().findViewById<ScrollView>(R.id.scroll_main).visibility=View.GONE
                        }
                    }
                }

            }
        }
    }
    class ScaleItemDecoration : RecyclerView.ItemDecoration() {
        private val scaleFactor = 0.8f

        override fun onDraw(
            c: Canvas,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val centerOfParent = parent.width / 2f
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val childCenter = (child.left + child.right) / 2f
                val distanceFromCenter = abs(centerOfParent - childCenter)
                val percentageFromCenter = (distanceFromCenter / parent.width).coerceAtMost(1f)
                val scale = 1f - (percentageFromCenter * (1f - scaleFactor))
                child.scaleX = scale
                child.scaleY = scale
            }
        }
    }
    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                adapter.retry();popular_manga_adapter.retry();tredingadapter.retry();reviews_adapter.retry() ;}
            .show()
    }
    private fun onMangaItemClic3(mangaItem: AniListSyncManager.MediaRecommendation) {
        val intent = Intent(requireContext(), MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
            putExtra("imageurl", mangaItem.coverImage.large)
            putExtra("mangaid", mangaItem.id)
        }
        startActivity(intent)
    }
    private fun onMangaItemClic2(mangaItem: Manga) {
        val intent = Intent(requireContext(), MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
            putExtra("imageurl", mangaItem.coverImage.large)
            putExtra("mangaid", mangaItem.id)
        }
        startActivity(intent)
    }
    private fun onMangaItemClick(mangaItem: Manga, cardView: CardView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            cardView,
            "manga_cover"
        )
        val intent = Intent(requireContext(), MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
            putExtra("imageurl", mangaItem.coverImage.large)
            putExtra("mangaid", mangaItem.id)
        }
        startActivity(intent, options.toBundle())
    }
    private fun onreviewclick(review: review) {
        val intent = Intent(requireContext(), review_full_page::class.java).apply {
            putExtra("username", review.user.name)
            putExtra("userpfp", review.user.avatar.large)
            putExtra("reviewid", review.id.toString())
        }
        startActivity(intent)
    }
    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.recom4)?.adapter=null
        view?.findViewById<RecyclerView>(R.id.recom)?.adapter=null
        view?.findViewById<RecyclerView>(R.id.trendig_manga)?.adapter=null
        view?.findViewById<RecyclerView>(R.id.recent_added_manga)?.adapter=null
        view?.findViewById<RecyclerView>(R.id.recent_reviews)?.adapter=null
        super.onDestroyView()
    }
}
