package com.youme.tanuki

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import java.util.WeakHashMap

class favoiurets : Fragment(R.layout.manga_info_page) {
    private val repository: MangaRepository = MangaRepository()
    private lateinit var tokenManager: AniListTokenManager
    private lateinit var viewPager: ViewPager2
    private var favoriteJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager=AniListTokenManager(requireContext())
        val tabLayout = view.findViewById<TabLayout>(R.id.tf)
        viewPager = view.findViewById(R.id.aazzes)
        val titlebox=view.findViewById<TextView>(R.id.textView3)
        tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.white),Color.parseColor("#99C4DC"))
        //tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#99C4DC"))
        view.findViewById<LinearLayout>(R.id.title_box).visibility=View.GONE
        viewPager.adapter = FavPagerAdapter(requireActivity(), userid = arguments?.getInt("userid"))
        viewPager.offscreenPageLimit=2
        tabLayout.tabIconTint = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "MANGA"
                1 -> "CHARACTERS"
                else -> null
            }
        }.attach()

    }
    private  class FavPagerAdapter(
        fa: FragmentActivity,
        private var userid:Int?=null,
    ) : FragmentStateAdapter(fa) {
        private val fragmentReferences = WeakHashMap<Int, Fragment>()
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> Favourites_manga_frag().apply {
                    arguments = Bundle().apply {
                        putInt("userid", userid!!)
                    }
                }
                1->Fragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
            fragmentReferences[position] = fragment
            return fragment
        }
        fun getFragment(position: Int): Fragment? {
            return fragmentReferences[position]
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        favoriteJob?.cancel()
        viewPager.adapter = null
    }


}
