package com.youme.tanuki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.widget.Button
import android.widget.RemoteViews
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PageFragment : Fragment() {

    companion object {
        private var accessToken: String? = null
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int, userid: Int?): PageFragment {
            return PageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                    putInt("userid", userid?:0)
                }
            }
        }
    }
    private var position: Int = 0
    private lateinit var viewModel: MangaLibraryViewModel2
    private lateinit var adapter: MyAdapter4
    private lateinit var tokenManager: AniListTokenManager
    private var userid:String?=null
    private lateinit var sharedPreferences: SharedPreferences
    private val statuses = listOf("CURRENT", "PLANNING", "COMPLETED", "DROPPED", "PAUSED","REPEATING")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            position = it.getInt(ARG_POSITION)
            userid=if(it.getInt("userid")!=0) it.getInt("userid").toString() else null
        }
        tokenManager = AniListTokenManager(requireContext())
        if (accessToken == null) {
            accessToken = tokenManager.getAccessToken()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.library_stats_class_layout, container, false)
        tokenManager = AniListTokenManager(requireContext())
        sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
        if (userid==null) userid = sharedPreferences.getString("userid",null)
        viewModel = MangaLibraryViewModel2(accessToken.toString())
        val recyclerView = rootview.findViewById<RecyclerView>(R.id.reqs)
        recyclerView.layoutManager= GridLayoutManager(requireContext(),3)
        adapter= MyAdapter4 (
            onItemClicker = { manga->
            val intent = Intent(requireContext(), MangaInfo::class.java)
            intent.putExtra("Charactername", manga.media.title.english?:manga.media.title.romaji)
            intent.putExtra("imageurl", manga.media.coverImage.large)
                intent.putExtra("mangaid", manga.media.id)
            startActivity(intent)
        }, onlongItemPess = {manga->
                val dialogView = layoutInflater.inflate(R.layout.dialog_add_widget, null)
                val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
                    .setTitle("Add Widget")
                    .setView(dialogView)
                    .create()
                dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
                    showWidgetDialog(manga)
                    dialog.dismiss()
                }
                dialogView.findViewById<Button>(R.id.button3).setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
        })
        recyclerView.adapter=adapter
        if (userid==null) viewModel.getuserid() else  viewModel.fetchMangaLibrary(userid!!.toInt(),statuses[position])
        viewModel.mangaLibrary.observe(viewLifecycleOwner) { mangaList ->
            recyclerView.visibility=View.VISIBLE
            mangaList.let {
                rootview.findViewById<View>(R.id.skeleton).visibility=View.GONE
                adapter.submitList(mangaList.filter { it.status==statuses[position]})
            }
        }
        viewModel.getuserrecommanded(userid?.toInt()!!)
        viewModel.user.observe(viewLifecycleOwner) { user ->
            with(sharedPreferences.edit()) {
                putString("userid", "${user.id}")
                apply()
            }
            viewModel.fetchMangaLibrary(user.id,"")
        }
        return rootview
    }
    private fun showWidgetDialog(manga:MediaListItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (!Settings.canDrawOverlays(requireContext())) {
                requestOverlayPermission()
                return
            }
        }
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val appWidgetHost = AppWidgetHost(requireContext(), 1)
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val intent = Intent(requireContext(), MangaWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("manga_title", manga.media.title.english?:manga.media.title.romaji)
            putExtra("manga_image_url", manga.media.coverImage.large)
            putExtra("manga_read_chapters", manga.progress)
            putExtra("manga_all_chapters", manga.media.chapters)
        }
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appWidgetManager.requestPinAppWidget(
                ComponentName(requireContext(), MangaWidgetProvider::class.java),
                null,
                pendingIntent
            )
        }
    }
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${requireContext().packageName}")
        )
        startActivity(intent)
    }
}
    class ViewPagerAdapter(activity: FragmentActivity,var userid:Int?=null) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 6
        private val fragmentTitles = listOf(
            "CURRENT",
            "PLANNING",
            "COMPLETED",
            "DROPPED",
            "PAUSED",
            "REPEATING"
        )
        override fun createFragment(position: Int): Fragment {
            return PageFragment.newInstance(position,userid)
        }
        fun getPageTitle(position: Int): CharSequence {
            return fragmentTitles[position]
        }
    }
class MangaWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_manga_box)
            updateWidgetFromPreferences(context, appWidgetManager, appWidgetId)
        }
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val title = intent.getStringExtra("manga_title") ?: "Unknown Title"
                val progress = intent.getIntExtra("manga_read_chapters",0)
                val chapters_count =  intent.getIntExtra("manga_all_chapters",0)
                val imageUrl = intent.getStringExtra("manga_image_url") ?: ""
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && title != "Unknown Title" && imageUrl.isNotEmpty()) {
                    val views = RemoteViews(context.packageName, R.layout.widget_manga_box).apply {
                        setTextViewText(R.id.textView, title)
                        setTextViewText(R.id.textView26, "${progress}/${chapters_count}")
                    }
                    val prefs = context.getSharedPreferences("MangaWidgetPrefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("title_$appWidgetId", title)
                        putInt("progress_$appWidgetId", progress)
                        putInt("chapters_count_$appWidgetId", chapters_count)
                        putString("imageUrl_$appWidgetId", imageUrl)
                        apply()
                    }
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val component = ComponentName(context, MangaWidgetProvider::class.java)
                    Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .into(AppWidgetTarget(context, R.id.imageView, views, component))

                    appWidgetManager.updateAppWidget(component, views)

                }
            }
        }

    }
    private fun updateWidgetFromPreferences(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences("MangaWidgetPrefs", Context.MODE_PRIVATE)
        val title = prefs.getString("title_$appWidgetId", "Unknown Title")
        val progress = prefs.getInt("progress_$appWidgetId", 0)
        val chapters_count = prefs.getInt("chapters_count_$appWidgetId", 0)
        val imageUrl = prefs.getString("imageUrl_$appWidgetId", "")

        val views = RemoteViews(context.packageName, R.layout.widget_manga_box).apply {
            setTextViewText(R.id.textView, title)
            setTextViewText(R.id.textView26, "${progress}/${chapters_count}")
        }

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(AppWidgetTarget(context, R.id.imageView, views, appWidgetId))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

}