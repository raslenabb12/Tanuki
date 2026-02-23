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

class MangaInfo : AppCompatActivity() {
    private val repository: MangaRepository = MangaRepository()
    private lateinit var tokenManager: AniListTokenManager
    private lateinit var viewPager: ViewPager2
    private var favoriteJob: Job? = null
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_info_page)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor= Color.parseColor("#222629")
        }
        tokenManager=AniListTokenManager(this@MangaInfo)
        val mangaName: String? = intent.getStringExtra("Charactername")
        val imagurl: String? = intent.getStringExtra("imageurl")
        val mangaid: Int = intent.getIntExtra("mangaid",0)
        val tabLayout = findViewById<TabLayout>(R.id.tf)
        viewPager = findViewById(R.id.aazzes)
        val titlebox=findViewById<TextView>(R.id.textView3)
        val manganame=if (mangaName.toString().length>17){
            "${mangaName?.substring(0,17)}..."
        }else{
            mangaName.toString()
        }
        findViewById<ImageButton>(R.id.imageButton11).setOnClickListener {
            val intent = Intent(this@MangaInfo, manga_reviews_and_fourms::class.java).apply {
                putExtra("mangaid", mangaid)
            }
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            favoriteJob?.cancel()
            lifecycleScope.launch {
                repository.Togglefaivorites(tokenManager.getAccessToken().toString(),mangaid).onSuccess {
                    findViewById<ImageButton>(R.id.imageButton2).visibility=View.GONE
                    findViewById<ImageButton>(R.id.imageButton2d).visibility=View.VISIBLE

                }.onFailure {
                    showError("Login Required")
                }
            }
        }
        findViewById<ImageButton>(R.id.imageButton2d).setOnClickListener {
            favoriteJob?.cancel()
            lifecycleScope.launch {

                repository.Togglefaivorites(tokenManager.getAccessToken().toString(),mangaid).onSuccess {
                    findViewById<ImageButton>(R.id.imageButton2d).visibility=View.GONE
                    findViewById<ImageButton>(R.id.imageButton2).visibility=View.VISIBLE

                }
            }
        }
        titlebox.text=manganame
        viewPager.adapter = DramaPagerAdapter(this, mangaName,imagurl, mediaid = mangaid)
        viewPager.offscreenPageLimit=2
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            finish()
        }
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                titlebox.text = when (position) {
                    0 -> manganame
                    1 -> "Characters"
                    2 -> "Stats"
                    3 -> "Recommendation"
                    else ->""
                }
            }
        }
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        tabLayout.tabIconTint = ContextCompat.getColorStateList(this, R.color.gray)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.baseline_menu_book_24)
                1 -> ContextCompat.getDrawable(this, R.drawable.baseline_people_outline_24)
                2 -> ContextCompat.getDrawable(this, R.drawable.baseline_data_usage_24)
                3 -> ContextCompat.getDrawable(this, R.drawable.baseline_recommend_24)
                else -> null
            }
        }.attach()
    }
    private  class DramaPagerAdapter(
        fa: FragmentActivity,
        private val characterName: String?,
        private val imgurl: String?,
        private val mediaid: Int?,
    ) : FragmentStateAdapter(fa) {
        private val fragmentReferences = WeakHashMap<Int, Fragment>()
        override fun getItemCount() = 4
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> DetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString("Charactername", characterName)
                        putString("imageurl", imgurl)
                        putInt("mediaid", mediaid!!)
                    }
                }
                1 -> Characters_frag().apply {
                    arguments = Bundle().apply {
                        putString("Charactername", characterName)
                        putString("imageurl", imgurl)
                        putInt("mediaid", mediaid!!)
                    }
                }
                2 -> stats_page_frag().apply {
                    arguments = Bundle().apply {
                        putString("Charactername", characterName)
                        putString("imageurl", imgurl)
                        putInt("mediaid", mediaid!!)
                    }
                }
                3 -> recommanditon_frag().apply {
                    arguments = Bundle().apply {
                        putString("Charactername", characterName)
                        putString("imageurl", imgurl)
                        putInt("mediaid", mediaid!!)
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
    private fun showError(message: String) {
        Snackbar.make(findViewById<LinearLayout>(R.id.main), message, Snackbar.LENGTH_LONG)
            .setAction("Login") {startAuthentication() }
            .show()
    }
    private fun startAuthentication() {
        val authIntent = AniListAuthActivity.createIntent(this@MangaInfo)
        startActivityForResult(authIntent, 9001)
    }
    override fun onDestroy() {
        super.onDestroy()
        favoriteJob?.cancel()
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        viewPager.adapter = null
    }


}
