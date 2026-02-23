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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.time.Duration.Companion.seconds

class Characters_frag : Fragment(R.layout.characters_page_layout) {
    private var mangaName: String? = null
    private var imgurl: String? = null
    private lateinit var adapter: MyAdapter
    private lateinit var adapter2: MyAdapter
    private lateinit var adapter3: MyAdapterMedia
    private lateinit var tokenManager: AniListTokenManager
    private var characersdetails: Job? = null
    private val viewModel: MangaViewModel2 by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager=AniListTokenManager(requireContext())
        mangaName = arguments?.getString("Charactername")
        imgurl = arguments?.getString("imageurl")

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycleview = view.findViewById<RecyclerView>(R.id.res)
        val recycleview2 = view.findViewById<RecyclerView>(R.id.vsq2)
        recycleview2.layoutManager=GridLayoutManager(requireContext(),3)
        recycleview.layoutManager=GridLayoutManager(requireContext(),3)
        adapter=MyAdapter(){characterNode ->
            val bottomDrawer = BottomDrawerFragment(characterNode.node!!)
            bottomDrawer.show(parentFragmentManager, bottomDrawer.tag)

        }
        adapter2=MyAdapter(){characterNode ->
            val bottomDrawer = BottomDrawerFragment(characterNode.node!!)
            bottomDrawer.show(parentFragmentManager, bottomDrawer.tag)

        }
        recycleview2.isNestedScrollingEnabled=false
        recycleview.isNestedScrollingEnabled=false
        viewModel.fetchMangaDetails(tokenManager.getAccessToken().toString(),mangaName.toString())
        recycleview.adapter=adapter
        recycleview2.adapter=adapter2
        characersdetails=viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mangaDetails.flowWithLifecycle(viewLifecycleOwner.lifecycle).collect {details ->
                if(details?.characters?.edges.isNullOrEmpty()){
                    view.findViewById<TextView>(R.id.alert).visibility=View.VISIBLE
                }
                else{
                    view.findViewById<TextView>(R.id.alert).visibility=View.GONE
                }
                if(details?.characters?.edges?.filter { it.role=="SUPPORTING" }.isNullOrEmpty()){
                    view.findViewById<LinearLayout>(R.id.support).visibility=View.GONE
                }
                else{
                    view.findViewById<LinearLayout>(R.id.support).visibility=View.VISIBLE
                }
                if(details?.characters?.edges?.filter { it.role=="MAIN" }.isNullOrEmpty()){
                    view.findViewById<LinearLayout>(R.id.mainrole).visibility=View.GONE
                }
                else{
                    view.findViewById<LinearLayout>(R.id.mainrole).visibility=View.VISIBLE
                }
                adapter2.submitList(details?.characters?.edges?.filter { it.role=="MAIN" })
                adapter.submitList(details?.characters?.edges?.filter { it.role=="SUPPORTING" })
            }
            
        }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        characersdetails?.cancel()
        view?.findViewById<RecyclerView>(R.id.res)?.adapter=null
         view?.findViewById<RecyclerView>(R.id.vsq2)?.adapter=null
    }
}