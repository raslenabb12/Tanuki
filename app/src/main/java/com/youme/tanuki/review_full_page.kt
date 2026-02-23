package com.youme.tanuki

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
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

class review_full_page : AppCompatActivity() {
    private var username: String? = null
    private var userpfp: String? = null
    private var reviewid: String? = null
    private var repository: MangaRepository=MangaRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reveiw_page)
        username = intent?.getStringExtra("username")
        userpfp = intent?.getStringExtra("userpfp")
        reviewid = intent?.getStringExtra("reviewid")
        Glide.with(this@review_full_page).load(userpfp).into(findViewById(R.id.imageView30))
        findViewById<TextView>(R.id.textView35).text=username
        findViewById<ImageButton>(R.id.imageButton7).setOnClickListener {
            finish()
        }
        findViewById<ImageView>(R.id.imageView30).setOnClickListener {
            val topSheet = FullScreenBottomSheet(userpfp)
            topSheet.show(supportFragmentManager, topSheet.tag)
        }

        lifecycleScope.launch {
            repository.getReviewInfo(reviewid.toString().toInt()).onSuccess {
                it?.let {
                    findViewById<LinearLayout>(R.id.user_box).setOnClickListener {button->
                        val intent = Intent(this@review_full_page, user_profile_search::class.java).apply {
                            putExtra("userid",it.user.id )
                            putExtra("username",it.user.name )
                        }
                        startActivity(intent)
                    }
                    findViewById<RelativeLayout>(R.id.loding).visibility=View.GONE
                    findViewById<ScrollView>(R.id.mainl).visibility=View.VISIBLE
                    Glide.with(this@review_full_page).load(it.media.coverImage.large).into(findViewById(R.id.imageView27))
                    findViewById<TextView>(R.id.textView36).text=it.summary
                    findViewById<ImageView>(R.id.imageView27).setOnClickListener {view ->
                        val topSheet = FullScreenBottomSheet(it.media.coverImage.large)
                        topSheet.show(supportFragmentManager, topSheet.tag)
                    }
                    findViewById<ProgressBar>(R.id.loadingBar).progress=it.score
                    findViewById<TextView>(R.id.textView39).text="${it.score}/100"
                    findViewById<TextView>(R.id.textView38).text=it.media.title.english?:it.media.title.romaji
                    findViewById<TextView>(R.id.textView37).apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(it.body, HtmlCompat.FROM_HTML_MODE_COMPACT,
                                URLImageGetter(this, this@review_full_page), null)
                        } else {
                            Html.fromHtml(it.body,HtmlCompat.FROM_HTML_MODE_COMPACT, URLImageGetter(this, this@review_full_page), null)
                        }
                    }
                }

            }
        }


    }
    class URLImageGetter(
        private val textView: TextView,
        private val context: Context
    ) : Html.ImageGetter {
        override fun getDrawable(source: String?): android.graphics.drawable.Drawable {
            val drawable = BitmapDrawable()
            drawable.setBounds(0, 0, 100, 100)
            source?.let { imageUrl ->
                (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                    try {
                        val bitmap = Glide.with(context)
                            .asBitmap()
                            .load(imageUrl)
                            .override(1024)
                            .submit()
                            .get()
                        withContext(Dispatchers.Main) {
                            val displayMetrics = context.resources.displayMetrics
                            val screenWidth = displayMetrics.widthPixels - textView.paddingLeft - textView.paddingRight
                            val scaleFactor = screenWidth.toFloat() / bitmap.width
                            val height = (bitmap.height * scaleFactor).toInt()
                            drawable.bitmap = bitmap
                            drawable.setBounds(0, 0, screenWidth, height)
                            textView.text = textView.text
                        }
                    } catch (e: Exception) {
                        Log.e("URLImageGetter", "Error loading image: $imageUrl", e)
                    }
                }
            }

            return drawable
        }
    }
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)

    }
}
