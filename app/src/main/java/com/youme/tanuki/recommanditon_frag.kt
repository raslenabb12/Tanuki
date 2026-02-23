package com.youme.tanuki

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.time.Duration.Companion.seconds
class recommanditon_frag : Fragment(R.layout.recommendation_page_layout) {
    private var mangaName: String? = null
    private var mediaid: Int? = null
    private var imgurl: String? = null
    private var recommendation_manga: Job? = null
    private val adapter: recommandedMangaPagingAdapter by lazy { recommandedMangaPagingAdapter(::onMangaItemClick) }
    private lateinit var tokenManager: AniListTokenManager
    private val viewModel: RecommendationsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager=AniListTokenManager(requireContext())
        mangaName = arguments?.getString("Charactername")
        imgurl = arguments?.getString("imageurl")
        mediaid = arguments?.getInt("mediaid")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycleview = view.findViewById<RecyclerView>(R.id.res)
        recycleview.layoutManager=GridLayoutManager(requireContext(),3)
        recycleview.adapter=adapter
        recommendation_manga=viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecommendations(mediaId =mediaid!! ).flowWithLifecycle(viewLifecycleOwner.lifecycle).collect { details ->
                adapter.submitData(details)
            }
        }
        }
    }
    private fun onMangaItemClick(mangaItem: RecommendationNode) {
        val intent = Intent(requireContext(), MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.mediaRecommendation?.title?.english ?: mangaItem.mediaRecommendation?.title?.romaji)
            putExtra("imageurl", mangaItem.mediaRecommendation?.coverImage?.large)
            putExtra("mangaid", mangaItem.mediaRecommendation?.id)
        }
        startActivity(intent)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        recommendation_manga?.cancel()
        view?.findViewById<RecyclerView>(R.id.res)?.adapter=null
    }
}