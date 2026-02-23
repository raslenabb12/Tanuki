package com.youme.tanuki

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsFragment : Fragment(R.layout.info_page) {
    private var mangaName: String? = null
    private lateinit var tokenManager: AniListTokenManager
    private var imgurl: String? = null
    private var tagsJob: Job? = null
    private var streamingSitesJob: Job? = null
    private var mangaDetailsJob: Job? = null
    private var glideRequestManager: RequestManager? = null
    private lateinit var youtubePlayerView: YouTubePlayerView
    private lateinit var adapter: MyAdapterMediaRelated
    private val viewModel: MangaViewModel2 by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        tokenManager=AniListTokenManager(requireContext())
        mangaName = arguments?.getString("Charactername")
        viewModel.fetchMangaDetails(tokenManager.getAccessToken().toString(),mangaName.toString())
        imgurl = arguments?.getString("imageurl")
        glideRequestManager = Glide.with(this)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val description = view.findViewById<TextView>(R.id.textView5)
        val toggleArrow = view.findViewById<ImageView>(R.id.ivToggleArrow)
        youtubePlayerView = view.findViewById(R.id.youtubePlayerView)
        view.findViewById<ImageView>(R.id.imageView3).apply {
            transitionName = "manga_cover"
            glideRequestManager?.load(imgurl)
                ?.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean = false
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }
                })
                ?.into(this)
        }
        val asscesstoken =tokenManager.getAccessToken()
        view.findViewById<TextView>(R.id.textView4).text = mangaName?.trim()
        mangaDetailsJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mangaDetails.flowWithLifecycle(viewLifecycleOwner.lifecycle).collect { details ->
                    details?.let { manga ->
                        tagsJob?.cancel()
                        streamingSitesJob?.cancel()
                        setuprecycleview(view, details.relations?.nodes!!)
                        view.findViewById<RelativeLayout>(R.id.statusbox).visibility = View.GONE
                        view.findViewById<ImageView>(R.id.imageView10)
                            .setImageResource(R.drawable.baseline_add_24)
                        view.findViewById<LinearLayout>(R.id.tags).removeAllViews()
                        view.findViewById<LinearLayout>(R.id.streaming_sites_box).removeAllViews()
                        view.findViewById<TextView>(R.id.textView47).text=details.stats.statusDistribution.sumOf { it.amount }.toString()
                        view.findViewById<TextView>(R.id.textView49).text=if (details.title?.english!=null && details.title.english!=details.title.romaji) details.title?.romaji else {view.findViewById<TextView>(R.id.textView49).visibility=View.GONE; ""}
                        if (details.isFavourite == true){
                            (context as MangaInfo).findViewById<ImageButton>(R.id.imageButton2).visibility=View.GONE
                            (context as MangaInfo).findViewById<ImageButton>(R.id.imageButton2d).visibility=View.VISIBLE
                        }
                        (context as MangaInfo).findViewById<TextView>(R.id.textView41).text=details.favourites.toString()
                        if (details.externalLinks.isNullOrEmpty()){
                            view.findViewById<LinearLayout>(R.id.streaming_sites_main).visibility=View.GONE
                        }
                        if (details.relations.nodes.none { it.type == "MANGA" }){
                            view.findViewById<TextView>(R.id.textView45).visibility=View.GONE
                        }
                        streamingSitesJob = viewLifecycleOwner.lifecycleScope.launch {
                            manga.externalLinks?.filter { it.type == "STREAMING" }?.forEach { link ->
                                add_streaming_sites(view, link)
                            }
                        }
                        if (details.mediaListEntry != null) {
                            details.mediaListEntry.let {
                                view.findViewById<RelativeLayout>(R.id.statusbox).visibility =
                                    View.VISIBLE
                                view.findViewById<ImageView>(R.id.imageView10)
                                    .setImageResource(R.drawable.baseline_edit_24)
                                view.findViewById<LinearLayout>(R.id.chapters).setOnClickListener {
                                    val bottomDrawer = entreyupdateFragment(details.mediaListEntry,details){
                                        if (it == "Done") {
                                            viewModel.fetchMangaDetails(
                                                tokenManager.getAccessToken().toString(),
                                                mangaName.toString()
                                            )
                                        }
                                    }
                                    bottomDrawer.show(parentFragmentManager, bottomDrawer.tag)
                                }
                                view.findViewById<TextView>(R.id.chaptercount).text =
                                    it.progress.toString() + "/"
                                view.findViewById<TextView>(R.id.textView25).text = it.status
                            }
                        } else {
                                view.findViewById<LinearLayout>(R.id.chapters).setOnClickListener {
                                    if (asscesstoken!=null){
                                        val bottomDrawer = entreyupdateFragment(null,details){
                                            if (it == "Done") {
                                                viewModel.fetchMangaDetails(
                                                    tokenManager.getAccessToken().toString(),
                                                    mangaName.toString()
                                                )
                                            }
                                        }
                                        bottomDrawer.show(parentFragmentManager, bottomDrawer.tag)
                                    }
                                    else  {showError("Login Required")}

                                }

                        }

                        view.findViewById<TextView>(R.id.allchapters).text =
                            (details.chapters ?: "?").toString()
                        description.text = manga.description ?: "No description"
                        view.findViewById<TextView>(R.id.textView6).text =
                            manga.startDate?.year.toString() + " - " + (manga.endDate?.year ?: "?")
                        view.findViewById<TextView>(R.id.textView107).text =
                            (manga.averageScore ?: 0).toString()+"/100"
                        view.findViewById<TextView>(R.id.textView102).text =
                            " " + manga.status?.lowercase().toString()
                                .replaceFirstChar { it.uppercaseChar() }
                        view.findViewById<TextView>(R.id.textView104).text =
                            manga.popularity.toString()
                        view.findViewById<RelativeLayout>(R.id.loading).visibility = View.GONE
                        view.findViewById<ImageView>(R.id.imageView3).setOnClickListener {
                            val topSheet = FullScreenBottomSheet(imgurl)
                            topSheet.show(parentFragmentManager, topSheet.tag)
                        }
                        details.bannerImage?.let {
                            view.findViewById<ImageView>(R.id.imageView4).setOnClickListener {
                                val topSheet = FullScreenBottomSheet(details.bannerImage)
                                topSheet.show(parentFragmentManager, topSheet.tag)
                            }
                        }
                        val banner = manga.bannerImage ?: manga.coverImage?.large
                        glideRequestManager?.load(banner)?.thumbnail(0.25f)?.transition(
                            DrawableTransitionOptions.withCrossFade(200)
                        )?.into(view.findViewById(R.id.imageView4))
                        view.findViewById<RelativeLayout>(R.id.main).visibility = View.VISIBLE

                        tagsJob = viewLifecycleOwner.lifecycleScope.launch {
                            manga.genres?.forEach { tag ->
                                tagss(view, tag)
                            }
                        }
                        lifecycle.addObserver(youtubePlayerView)
                        if (details.trailer != null) {
                            view.findViewById<TextView>(R.id.textView50).visibility=View.VISIBLE
                            glideRequestManager?.load(details.trailer.thumbnail)
                                ?.thumbnail(0.25f)?.transition(
                                DrawableTransitionOptions.withCrossFade(200)
                            )?.into(view.findViewById(R.id.imageView15))
                            youtubePlayerView.addYouTubePlayerListener(object :
                                AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    view.findViewById<ImageView>(R.id.imageView17)
                                        .setOnClickListener {
                                            view.findViewById<RelativeLayout>(R.id.thumnailbox).visibility =
                                                View.GONE
                                            youtubePlayerView.visibility = View.VISIBLE
                                            youTubePlayer.loadVideo(details.trailer.id, 0f)
                                        }

                                }
                            })
                        } else view.findViewById<CardView>(R.id.thumnail_main).visibility =
                            View.GONE

                    }
                }
            }
        }
        var isExpanded = false
        toggleArrow.setOnClickListener {
            isExpanded=!isExpanded
            if (isExpanded) {
                description.maxLines = Int.MAX_VALUE
                animateHeight(description)
                toggleArrow.setImageResource(R.drawable.baseline_expand_less_24)
            } else {
                description.maxLines = 3
                animateHeight(description)
                toggleArrow.setImageResource(R.drawable.baseline_expand_more_24)
            }
        }

    }
    private fun setuprecycleview(view: View,mangalist:List<Manga>){
        val recyclerView=view.findViewById<RecyclerView>(R.id.reqs)
        adapter=MyAdapterMediaRelated(){manga->
            val intent = Intent(requireContext(), MangaInfo::class.java)
            intent.putExtra("Charactername", manga.title.english?:manga.title.romaji)
            intent.putExtra("imageurl", manga.coverImage.large)
            intent.putExtra("mangaid", manga.id)
            startActivity(intent)
        }
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> rv.parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> rv.parent.requestDisallowInterceptTouchEvent(false)
                }
                return false
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        recyclerView.layoutManager=LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter=adapter
        adapter.submitList(mangalist.filter { it.type=="MANGA" })

    }
    private suspend fun tagss(view: View, tag:String){
        withContext(Dispatchers.Main){
            val inflater = LayoutInflater.from(context)
            val itemView = inflater.inflate(R.layout.genre, null)
            val container = view.findViewById<LinearLayout>(R.id.tags)
            itemView.findViewById<TextView>(R.id.textView11).text=tag
            itemView.setOnClickListener {
                val intent = Intent(requireContext(), manga_search::class.java)
                intent.putExtra("genre", tag)
                startActivity(intent)
            }
            container.addView(itemView)
        }
    }
    private suspend fun add_streaming_sites(view: View, tag:externalLinksdata){
        withContext(Dispatchers.Main){
            val inflater = LayoutInflater.from(context)
            val itemView = inflater.inflate(R.layout.streaming_sites_box, null)
            val container = view.findViewById<LinearLayout>(R.id.streaming_sites_box)
            itemView.findViewById<TextView>(R.id.textView43).text=tag.site
            itemView.findViewById<TextView>(R.id.textView44).text=tag.language?:""
            if (tag.language==null){
                itemView.findViewById<TextView>(R.id.textView44).visibility=View.GONE
            }
            if (tag.icon==null){
                itemView.findViewById<CardView>(R.id.bl).visibility=View.GONE
            }
            itemView.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tag.url))
                startActivity(browserIntent)
            }
            Glide.with(requireContext()).load(tag.icon).into(itemView.findViewById(R.id.imageView34))
            itemView.findViewById<ImageView>(R.id.imageView34).imageTintList= ColorStateList.valueOf(Color.parseColor(tag.color))
            container.addView(itemView)
        }
    }
    private fun animateHeight(view: TextView) {
        val targetHeight = if (view.maxLines == Int.MAX_VALUE) view.getFullHeight() else view.getCollapsedHeight()+15
        val currentHeight = view.height
        val animator = ObjectAnimator.ofInt(view, "height", currentHeight, targetHeight)
        animator.duration = 300
        animator.start()
    }


    private fun TextView.getFullHeight(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return measuredHeight
    }

    private fun TextView.getCollapsedHeight(): Int {
        val lineHeight = lineHeight
        val maxLines = 4
        return lineHeight * maxLines
    }
    override fun onDestroy() {
        super.onDestroy()
        youtubePlayerView.release()
    }
    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAction("Login") { startAuthentication() }
            .show()
    }
    private fun startAuthentication() {
        val authIntent = AniListAuthActivity.createIntent(requireContext())
        startActivityForResult(authIntent, 9001)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        tagsJob?.cancel()
        streamingSitesJob?.cancel()
        mangaDetailsJob?.cancel()
        view?.findViewById<RecyclerView>(R.id.reqs)?.adapter = null
        glideRequestManager?.clear(view?.findViewById<ImageView>(R.id.imageView3)!!)
        glideRequestManager?.clear(view?.findViewById<ImageView>(R.id.imageView4)!!)
        glideRequestManager = null
    }
}