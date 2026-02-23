package com.youme.tanuki

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.xml.sax.XMLReader
import java.util.WeakHashMap

class followers_and_following_main(var userid: Int,var inial_page:Int,var username:String) : BottomSheetDialogFragment() {
    private lateinit var viewPager: ViewPager2
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.manga_info_page, container, false)
    }
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.imageButton2).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton3).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton2d).visibility=View.GONE
        view.findViewById<ImageButton>(R.id.imageButton11).visibility=View.GONE
        view.findViewById<TextView>(R.id.textView41).visibility=View.GONE
        val titlebox=view.findViewById<TextView>(R.id.textView3)
        val tabLayout = view.findViewById<TabLayout>(R.id.tf)
        titlebox.text=username
        viewPager = view.findViewById(R.id.aazzes)
        viewPager.adapter = following_follows_PagerAdapter(
                requireActivity(),
                userid = userid
        )
        view.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            dismiss()
        }
        viewPager.currentItem=inial_page
        tabLayout.tabIconTint = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Followers"
                1->"Following"
                else -> null
            }
        }.attach()
    }
    private  class following_follows_PagerAdapter(
        fa: FragmentActivity,
        private val userid: Int?,
    ) : FragmentStateAdapter(fa) {
        private val fragmentReferences = WeakHashMap<Int, Fragment>()
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> followers_frag().apply {
                    arguments = Bundle().apply {
                        putInt("userid", userid!!)
                    }
                }
                1 -> following_frag().apply {
                    arguments = Bundle().apply {
                        putInt("userid", userid!!)
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
}