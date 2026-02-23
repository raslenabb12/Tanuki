package com.youme.tanuki

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

class user_profile_search : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_info_page)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor= Color.parseColor("#222629")
        }
        findViewById<ImageButton>(R.id.imageButton2).visibility=View.GONE
        findViewById<ImageButton>(R.id.imageButton3).visibility=View.GONE
        findViewById<ImageButton>(R.id.imageButton2d).visibility=View.GONE
        findViewById<ImageButton>(R.id.imageButton11).visibility=View.GONE
        findViewById<TextView>(R.id.textView41).visibility=View.GONE
        val userid: Int = intent.getIntExtra("userid",0)
        val username: String? = intent.getStringExtra("username")
        val tabLayout = findViewById<TabLayout>(R.id.tf)
        viewPager = findViewById(R.id.aazzes)
        val titlebox=findViewById<TextView>(R.id.textView3)
        titlebox.text=username
        viewPager.adapter = user_profile_main.userPagerAdapter(this, userid = userid,c_user = false)
        viewPager.offscreenPageLimit=2
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            finish()
        }
        tabLayout.tabIconTint = ContextCompat.getColorStateList(this, R.color.gray)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.outline_person_24)
                else -> null
            }
        }.attach()
    }

}
