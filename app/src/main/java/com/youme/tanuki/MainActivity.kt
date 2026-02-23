package com.youme.tanuki

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: AniListTokenManager
    private lateinit var viewModel: MangaLibraryViewModel2
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    internal final lateinit var toggle:ActionBarDrawerToggle
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.myanimelist.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val malService = retrofit.create(MyAnimeListUser::class.java)
    internal lateinit var fragmentStateManager: FragmentStateManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_Tanuki)
        setContentView(R.layout.mainactivity)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        tokenManager = AniListTokenManager(this@MainActivity)
        val accessToken = tokenManager.getAccessToken()
        sharedPreferences = this@MainActivity.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userid = sharedPreferences.getString("userid",null)
        viewModel = MangaLibraryViewModel2(accessToken.toString())
        fragmentStateManager = FragmentStateManager(supportFragmentManager)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            val intent = Intent(this@MainActivity, character_search::class.java)
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            val intent = Intent(this@MainActivity, manga_search::class.java)
            startActivity(intent)
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.drawerArrowDrawable.color= Color.WHITE
        val headerView = layoutInflater.inflate(R.layout.logl, navigationView, false)
        val mainprofile= findViewById<CardView>(R.id.profilebox)
        viewModel.user.observe(this) { userProfile->
            if (userid==null){
                with(sharedPreferences.edit()) {
                    putString("userid", "${userProfile.id}")
                    apply()
                }
            }
            if (userProfile.unreadNotificationCount!=0){
                findViewById<CardView>(R.id.num_notf).visibility=View.VISIBLE
                findViewById<TextView>(R.id.textView53).text=userProfile.unreadNotificationCount.toString()
            }
            headerView.findViewById<CardView>(R.id.profilebox).visibility=View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withContext(Dispatchers.Main){
                        //circule profie icon in bottom navigation view
//                        Glide.with(this@MainActivity)
//                            .load(userProfile.avatar.medium)
//                            .circleCrop()
//                            .into(object : CustomTarget<Drawable>() {
//                                override fun onResourceReady(
//                                    resource: Drawable,
//                                    transition: Transition<in Drawable>?
//                                ) {
//                                    findViewById<BottomNavigationView>(R.id.rrz).itemIconTintList = null
//                                    findViewById<BottomNavigationView>(R.id.rrz).menu.findItem(R.id.profile).icon = resource
//
//                                }
//                                override fun onLoadCleared(placeholder: Drawable?) {
//                                }
//                            })




                        Glide.with(this@MainActivity).load(userProfile.avatar.medium).thumbnail(0.25f).transition(
                            DrawableTransitionOptions.withCrossFade(200)).into(headerView.findViewById<ImageView>(R.id.profile))
                        headerView.findViewById<TextView>(R.id.textView19).text=userProfile.name.replaceFirstChar { it.uppercaseChar() }
                        Glide.with(this@MainActivity).load(userProfile.avatar.medium).thumbnail(0.25f).transition(
                            DrawableTransitionOptions.withCrossFade(200)).into(mainprofile.findViewById<ImageView>(R.id.profile))
                        mainprofile.findViewById<TextView>(R.id.textView19).text=userProfile.name.replaceFirstChar { it.uppercaseChar() }
                        mainprofile.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(1500).withEndAction {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                mainprofile.animate().setInterpolator(AccelerateDecelerateInterpolator()).translationY(-1100f).setDuration(900).withEndAction {
                                    mainprofile.visibility=View.GONE
                                }.start()
                            }
                        }.start()
                    }
                } catch (e: Exception) {
                    Log.e("USER_PROFILE_ERRUER", "Error fetching user profile", e)
                }
            }
        }
        if (accessToken!=null){
            viewModel.getuserid()
        }
        val browseManga = headerView.findViewById<LinearLayout>(R.id.browse_manga)
        val trendingManga = headerView.findViewById<LinearLayout>(R.id.trending_manga)
        val popularCharacters = headerView.findViewById<LinearLayout>(R.id.popular_characters)
        val popularmanga = headerView.findViewById<LinearLayout>(R.id.popular_manga)
        val library = headerView.findViewById<LinearLayout>(R.id.library)
        val reviews = headerView.findViewById<LinearLayout>(R.id.reviews)
        val settings = headerView.findViewById<LinearLayout>(R.id.settings)
        val about = headerView.findViewById<LinearLayout>(R.id.about)
        headerView.findViewById<LinearLayout>(R.id.about).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/raslenabb12"))
            startActivity(browserIntent)
        }
        browseManga.setOnClickListener {
            fragmentStateManager.switchFragment(browse_manga_frag(), R.id.fragmentContainerView)
            toolbar.title = "Explore"
            closeDrawerWithDelay()
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.VISIBLE
        }
        findViewById<BottomNavigationView>(R.id.rrz).setOnItemSelectedListener {
            when (it.itemId){
                R.id.explore -> {
                    fragmentStateManager.switchFragment(browse_manga_frag(), R.id.fragmentContainerView)
                    toolbar.title = "Explore"
                    closeDrawerWithDelay()
                    findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
                    findViewById<ImageButton>(R.id.imageButton6).visibility = View.VISIBLE
                    toolbar.visibility=View.VISIBLE
                    toggle.isDrawerIndicatorEnabled = true
                    toolbar.navigationIcon = toggle.drawerArrowDrawable
                }
                R.id.library -> {
                    if (accessToken!=null){
                        fragmentStateManager.switchFragment(library_frag(), R.id.fragmentContainerView)
                        toolbar.title = "Library"
                    }
                    else{
                        fragmentStateManager.switchFragment(anilist_login_frag(), R.id.fragmentContainerView)
                        toolbar.title = "Anilist Login"

                    }
                    toggle.isDrawerIndicatorEnabled = false
                    toolbar.navigationIcon = it.icon
                    toolbar.visibility=View.VISIBLE
                    findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
                    findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
                }
                R.id.profile ->{
                    if (accessToken!=null){
                        fragmentStateManager.switchFragment(user_profile_main(), R.id.fragmentContainerView)
                        toolbar.title = "Profile"
                    }
                    else{
                        fragmentStateManager.switchFragment(anilist_login_frag(), R.id.fragmentContainerView)
                        toolbar.title = "Anilist Login"

                    }
                    toggle.isDrawerIndicatorEnabled = false
                    toolbar.navigationIcon = it.icon
                    findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
                    findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
                }
                R.id.fav ->{
                    if (accessToken!=null){
                        fragmentStateManager.switchFragment(favoiurets().apply {
                            arguments = Bundle().apply {
                                putInt("userid", userid?.toInt()!!)
                            }
                        }, R.id.fragmentContainerView)
                        toolbar.title = "Favourites"
                    }
                    else{
                        fragmentStateManager.switchFragment(anilist_login_frag(), R.id.fragmentContainerView)
                        toolbar.title = "Anilist Login"

                    }
                    toggle.isDrawerIndicatorEnabled = false
                    toolbar.navigationIcon = it.icon
                    findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
                    findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
                }

            }
            true
        }

        trendingManga.setOnClickListener {
            fragmentStateManager.switchFragment(trending_manga_frag(), R.id.fragmentContainerView)
            toolbar.title = "Trending Manga"
            closeDrawerWithDelay()
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.VISIBLE
        }
        popularCharacters.setOnClickListener {
            fragmentStateManager.switchFragment(all_characters(), R.id.fragmentContainerView)
            toolbar.title = "All Characters"
            closeDrawerWithDelay()
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.VISIBLE
        }
        library.setOnClickListener {
            closeDrawerWithDelay()
            if (accessToken!=null){
                fragmentStateManager.switchFragment(library_frag(), R.id.fragmentContainerView)
                toolbar.title = "Library"
            }
            else{
                fragmentStateManager.switchFragment(anilist_login_frag(), R.id.fragmentContainerView)
                toolbar.title = "Anilist Login"

            }
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE

        }
        popularmanga.setOnClickListener {
            fragmentStateManager.switchFragment(popular_manga_frag(), R.id.fragmentContainerView)
            toolbar.title = "Popular Manga"
            closeDrawerWithDelay()
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.VISIBLE
        }
        reviews.setOnClickListener {
            fragmentStateManager.switchFragment(reveiws_frag(), R.id.fragmentContainerView)
            toolbar.title = "Reveiws"
            closeDrawerWithDelay()
            findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
        }
        settings.setOnClickListener {
            toolbar.title = "Settings"
            closeDrawerWithDelay()
        }

        about.setOnClickListener {
            toolbar.title = "About"
            closeDrawerWithDelay()
        }
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.br -> {
//                    fragmentStateManager.switchFragment(browse_manga_frag(), R.id.fragmentContainerView)
//                    toolbar.title="Browse Manga"
//                    closeDrawerWithDelay()
//                    findViewById<ImageButton>(R.id.imageButton5).visibility=View.GONE
//                    findViewById<ImageButton>(R.id.imageButton6).visibility=View.VISIBLE
//                }
//                R.id.lib -> {
//                    fragmentStateManager.switchFragment(library_frag(), R.id.fragmentContainerView)
//                    toolbar.title="Library"
//                    closeDrawerWithDelay()
//                    findViewById<ImageButton>(R.id.imageButton5).visibility=View.GONE
//                    findViewById<ImageButton>(R.id.imageButton6).visibility=View.GONE
//                }
//                R.id.characters -> {
//                    fragmentStateManager.switchFragment(all_characters(), R.id.fragmentContainerView)
//                    toolbar.title="All Characters"
//                    closeDrawerWithDelay()
//                    findViewById<ImageButton>(R.id.imageButton6).visibility=View.GONE
//                    findViewById<ImageButton>(R.id.imageButton5).visibility=View.VISIBLE
//                }
//                R.id.treding_manga -> {
//                    fragmentStateManager.switchFragment(trending_manga_frag(), R.id.fragmentContainerView)
//                    toolbar.title="Trending Manga"
//                    closeDrawerWithDelay()
//                    findViewById<ImageButton>(R.id.imageButton5).visibility=View.GONE
//                    findViewById<ImageButton>(R.id.imageButton6).visibility=View.GONE
//                }
//            }
//            true
//        }
        navigationView.addHeaderView(headerView)
        toggle.syncState()
    }
    private fun closeDrawerWithDelay() {
        lifecycleScope.launch {
            drawerLayout.postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
            }, 50)
        }
    }
    class FragmentStateManager(private val fragmentManager: androidx.fragment.app.FragmentManager) {
        private var currentFragment: Fragment? = null
        fun switchFragment(fragment: Fragment, containerId: Int) {
            if (currentFragment?.javaClass == fragment.javaClass) return
            val transaction = fragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            transaction.replace(containerId, fragment)
            //-----(back to prv frag ::no need for now)-----
            //transaction.addToBackStack(null)
            transaction.commitAllowingStateLoss()
            currentFragment = fragment
        }
    }
    fun animateWidth(view: View, startWidth: Int, endWidth: Int, duration: Long,onAnimationEnd: () -> Unit) {
        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val newWidth = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.width = newWidth
            view.layoutParams = layoutParams
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }
            override fun onAnimationEnd(animation: Animator) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    onAnimationEnd()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
}