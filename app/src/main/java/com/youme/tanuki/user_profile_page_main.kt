package com.youme.tanuki

import android.content.Context
import android.content.SharedPreferences
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

class user_profile_main : Fragment(R.layout.manga_info_page) {
    private lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userid = sharedPreferences.getString("userid",null)
        view.findViewById<ImageButton>(R.id.imageButton2).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton3).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton2d).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton11).visibility=View.GONE
        view.findViewById<TextView>(R.id.textView41).visibility=View.GONE
        val tabLayout = view.findViewById<TabLayout>(R.id.tf)
        viewPager = view.findViewById(R.id.aazzes)
        view.findViewById<LinearLayout>(R.id.title_box).apply {
            visibility=View.GONE
        }
        userid?.let {
            viewPager.adapter = userPagerAdapter(requireActivity(), userid = userid.toInt(), c_user = true)
        }
        viewPager.offscreenPageLimit=2
        view.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {

        }
        tabLayout.tabIconTint = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(requireContext(), R.drawable.outline_person_24)
                else -> null
            }
        }.attach()
    }
    public  class userPagerAdapter(
        fa: FragmentActivity,
        private val userid: Int?,
        private val c_user: Boolean?,
    ) : FragmentStateAdapter(fa) {
        private val fragmentReferences = WeakHashMap<Int, Fragment>()
        override fun getItemCount() = 1
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> user_profile_info().apply {
                    arguments = Bundle().apply {
                        putInt("userid", userid!!)
                        putBoolean("c_user", c_user!!)
                    }
                }
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
        viewPager.adapter = null
    }


}
