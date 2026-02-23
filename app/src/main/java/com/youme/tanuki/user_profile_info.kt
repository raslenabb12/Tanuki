package com.youme.tanuki

import android.content.res.Resources
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import java.time.Instant
import java.time.ZoneId
import java.util.WeakHashMap
import javax.xml.transform.Source

class user_profile_info : Fragment(R.layout.user_profile_layout) {
    private var userid: Int? = null
    private var c_user: Boolean? = null
    private lateinit var tokenManager: AniListTokenManager
    private var glideRequestManager: RequestManager? = null
    private lateinit var viewModel: MangaLibraryViewModel2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userid=arguments?.getInt("userid")
        c_user=arguments?.getBoolean("c_user")
        tokenManager = AniListTokenManager(requireContext())
        val accessToken = tokenManager.getAccessToken()
        viewModel = MangaLibraryViewModel2(accessToken.toString())
        glideRequestManager=Glide.with(this)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.get_user_details_from_id(userid!!)
        viewModel.search_user.observe(requireActivity()) { userProfile->
            view.findViewById<TextView>(R.id.textView59).apply {
                visibility=if (userProfile.User.about==null)View.GONE else View.VISIBLE
            }
            userProfile.User.about?.let {
                view.findViewById<TextView>(R.id.textView59).apply {
                    visibility=View.VISIBLE
                    movementMethod = LinkMovementMethod.getInstance()
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(userProfile.User.about, HtmlCompat.FROM_HTML_MODE_COMPACT,
                            review_full_page.URLImageGetter(this, requireContext()), null)
                    } else {
                        Html.fromHtml(userProfile.User.about,
                            HtmlCompat.FROM_HTML_MODE_COMPACT,
                            review_full_page.URLImageGetter(this, requireContext()), null)
                    }
                }
            }
            view.findViewById<Button>(R.id.button6).apply {
                visibility=if(c_user == true)View.GONE else View.VISIBLE
            }
            view.findViewById<Button>(R.id.button11).setOnClickListener {
                if (c_user==true) {
                    (activity as? MainActivity)?.let { mainActivity ->
                        val toolbar: androidx.appcompat.widget.Toolbar = mainActivity.findViewById(R.id.toolbar)
                        mainActivity.findViewById<BottomNavigationView>(R.id.rrz).selectedItemId=R.id.library
                        mainActivity.fragmentStateManager.switchFragment(library_frag(), R.id.fragmentContainerView)
                        toolbar.title = "Library"
                        mainActivity.toggle.isDrawerIndicatorEnabled = false
                        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),R.drawable.outline_book_24)
                        toolbar.visibility=View.VISIBLE
                        mainActivity.findViewById<ImageButton>(R.id.imageButton5).visibility = View.GONE
                        mainActivity.findViewById<ImageButton>(R.id.imageButton6).visibility = View.GONE
                    }
                }
                else{
                    val topSheet = library_frag_drawer(userProfile.User.id)
                    topSheet.show(parentFragmentManager, topSheet.tag)
                }
            }
            view.findViewById<LinearLayout>(R.id.following).setOnClickListener {
                val topSheet = followers_and_following_main(userProfile.User.id,1,userProfile.User.name)
                topSheet.show(parentFragmentManager, topSheet.tag)
            }
            view.findViewById<LinearLayout>(R.id.followers).setOnClickListener {
                val topSheet = followers_and_following_main(userProfile.User.id,0,userProfile.User.name)
                topSheet.show(parentFragmentManager, topSheet.tag)
            }
            view.findViewById<TextView>(R.id.textView51).text=userProfile.User.name
            glideRequestManager?.load(userProfile.User.avatar.large)?.into(view.findViewById(R.id.imageView37))
            glideRequestManager?.load(userProfile.User.bannerImage)?.into(view.findViewById(R.id.imageView36))
            val instant = Instant.ofEpochSecond(userProfile.User.createdAt?.toLong()!!)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            view.findViewById<TextView>(R.id.textView55).text=userProfile.followers_num.pageInfo.total.toString()
            view.findViewById<TextView>(R.id.textView57).text=userProfile.following_num.pageInfo.total.toString()
            view.findViewById<TextView>(R.id.textView58).text=" Created At ${date.toString()}"
            view.findViewById<LinearLayout>(R.id.shimmier).visibility=View.GONE
            view.findViewById<RelativeLayout>(R.id.main).visibility=View.VISIBLE
        }
    }

}
