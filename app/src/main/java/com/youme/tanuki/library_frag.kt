package com.youme.tanuki


import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class library_frag : Fragment() {
    private lateinit var tokenManager: AniListTokenManager
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var userid:Int?=null
    private lateinit var pagerAdapter:ViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.library_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = AniListTokenManager(requireContext())
        userid=arguments?.getInt("userid")
        proceedToMainApp(view)
    }
    private fun proceedToMainApp(view: View) {
        tabLayout = view.findViewById(R.id.tf)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.offscreenPageLimit = 2
        pagerAdapter = ViewPagerAdapter(requireActivity(),userid)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.adapter = null
        tabLayout.setupWithViewPager(null)
    }

}