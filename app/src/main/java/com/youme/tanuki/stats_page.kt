package com.youme.tanuki

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.time.Duration.Companion.seconds

class stats_page_frag : Fragment(R.layout.stats_page_layout) {
    private var mangaName: String? = null
    private var imgurl: String? = null
    private lateinit var tokenManager: AniListTokenManager
    private val viewModel: MangaViewModel2 by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager=AniListTokenManager(requireContext())
        mangaName = arguments?.getString("Charactername")
        imgurl = arguments?.getString("imageurl")
        viewModel.fetchMangaDetails(tokenManager.getAccessToken().toString(),mangaName.toString())

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mangaDetails.flowWithLifecycle(viewLifecycleOwner.lifecycle).collect { details ->
                       details?.let {
                           val barChart = view.findViewById<BarChart>(R.id.barChart)

                           val scoreDistribution = details?.stats?.scoreDistribution?.map {
                               Pair(it.score,it.amount)
                           }
                           val entries = scoreDistribution?.map { BarEntry(it.first.toFloat(), it.second.toFloat()) }
                           val dataSet = BarDataSet(entries, "Score Distribution",).apply {
                               color = Color.parseColor("#FFD700")
                               valueTextColor = Color.WHITE
                               valueTextSize = 12f
                           }
                           val data2 = BarData(dataSet)
                           data2.barWidth=5f
                           barChart.data = data2
                           barChart.setFitBars(true)
                           barChart.setBackgroundColor(Color.parseColor("#222222"))
                           barChart.animateY(1000)
                           barChart.axisLeft.apply {
                               isEnabled = false
                           }
                           barChart.xAxis.apply {
                               position = XAxis.XAxisPosition.BOTTOM
                               granularity = 10f
                               textColor = Color.WHITE
                               textSize = 12f
                               setDrawGridLines(false)
                               valueFormatter = object : ValueFormatter() {
                                   override fun getFormattedValue(value: Float): String {
                                       return value.toInt().toString()
                                   }
                               }
                           }

                           barChart.axisLeft.apply {
                               textColor = Color.WHITE
                               textSize = 12f
                           }
                           barChart.axisLeft.textColor = Color.WHITE
                           barChart.axisRight.isEnabled = false
                           barChart.legend.isEnabled = true
                           barChart.legend.textColor=Color.WHITE


                           barChart.description = Description().apply { text = "Score Distribution"
                               textColor=Color.WHITE
                           }

                           val donutChart = view.findViewById<PieChart>(R.id.donutChart)
                           val chartData =it.stats.statusDistribution.map {
                               Pair(it.status,it.amount)
                           }
                           donutChart.setBackgroundColor(Color.parseColor("#222222"))
                           val entries2 = chartData.map { PieEntry(it.second.toFloat(), it.first) }
                           val dataSet2 = PieDataSet(entries2, "").apply {
                               colors = listOf(
                                   Color.parseColor("#00c853"),
                                   Color.parseColor("#9e9e9e"),
                                   Color.parseColor("#5c6bc0"),
                                   Color.parseColor("#d50000"),
                                   Color.parseColor("#ffd600"),
                                   Color.parseColor("#f84e32")
                               )
                               valueTextColor = Color.WHITE
                               valueTextSize = 12f
                           }
                           val pieData = PieData(dataSet2)
                           donutChart.apply {
                               data = pieData
                               setUsePercentValues(false)
                               setDrawHoleEnabled(true)
                               holeRadius = 20f
                               transparentCircleRadius = 25f
                               setHoleColor(Color.parseColor("#222222"))
                               setEntryLabelTextSize(10f)
                               setEntryLabelColor(Color.WHITE)
                               setDrawEntryLabels(true)
                               description.isEnabled = true
                               description.text="Status Distribution"
                               description.textColor=Color.WHITE
                               legend.isEnabled = false
                               legend.orientation = Legend.LegendOrientation.VERTICAL
                               legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                               legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                               legend.setDrawInside(false)
                               legend.textColor=Color.WHITE
                               legend.xEntrySpace = 10f
                               legend.yEntrySpace = 5f
                           }
                           donutChart.invalidate()

                       }
                    }
            }
        }





    }
}