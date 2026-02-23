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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.xml.sax.XMLReader

class BottomDrawerFragment(var character: CharacterNode) : BottomSheetDialogFragment() {

    private lateinit var adapter3: MyAdapterMedia
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.character_manga_connects, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollView = view.findViewById<ScrollView>(R.id.content)
        val nameTextView = view.findViewById<TextView>(R.id.textView16)
        val imageView = view.findViewById<ImageView>(R.id.imageView13)
        val recycleview = view.findViewById<RecyclerView>(R.id.resq)
        val description = view.findViewById<TextView>(R.id.textView18)
        val toggleArrow = view.findViewById<ImageView>(R.id.ivToggleArrow)
        val favouritesTextView = view.findViewById<TextView>(R.id.textView17)
        val genderbox : CardView = view.findViewById(R.id.gender1)
        val gendertype : ImageView = view.findViewById(R.id.imageView20)
        val bloodtype : TextView = view.findViewById(R.id.textView21)
        val bloodbox : CardView = view.findViewById(R.id.bloodtype1)
        val agenumber : TextView = view.findViewById(R.id.textView23)
        val agebox : CardView = view.findViewById(R.id.age1)
        if (character.age==null) agebox.visibility=View.GONE
        else agenumber.text=character.age.toString()
        if (character.gender == null) genderbox.visibility = View.GONE
        else {
            when (character.gender) {
                "Male" -> gendertype.setImageResource(R.drawable.baseline_male_24)
                "Female" -> gendertype.setImageResource(R.drawable.baseline_female_24)
                else->genderbox.visibility=View.GONE
            }

        }

        imageView.setOnClickListener {
            val topSheet = FullScreenBottomSheet(character.image?.large)
            topSheet.show(parentFragmentManager, topSheet.tag)
        }

        Glide.with(requireContext()).load(character.media.nodes.get(0).bannerImage?:character.media.nodes.random().coverImage.large)
            .transition(DrawableTransitionOptions.withCrossFade()).into(view.findViewById(R.id.imageView21))
        if (character.bloodType == null) bloodbox.visibility = View.GONE
        else bloodtype.text = character.bloodType

        nameTextView.text = character.name?.full
        Glide.with(requireContext()).load(character.image?.large).into(imageView)

        var isExpanded = false
        description.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(character.description, HtmlCompat.FROM_HTML_MODE_COMPACT,
                    review_full_page.URLImageGetter(this, requireContext()), null)
            } else {
                Html.fromHtml(character.description,
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                    review_full_page.URLImageGetter(this, requireContext()), null)
            }
        }
        toggleArrow.setOnClickListener {
            isExpanded=!isExpanded
            if (isExpanded) {
                description.maxLines = Int.MAX_VALUE
                animateHeight(description)
                toggleArrow.setImageResource(R.drawable.baseline_expand_less_24)
            } else {
                description.maxLines = 3
                animateHeight(description)
                toggleArrow.setImageResource(R.drawable.baseline_expand_more_24)
            }
        }
        adapter3 = MyAdapterMedia { manga ->
            val intent = Intent(requireContext(), MangaInfo::class.java).apply {
                putExtra("Charactername", manga.title.english ?: manga.title.romaji)
                putExtra("imageurl", manga.coverImage.large)
                putExtra("mangaid", manga.id)
            }
            startActivity(intent)
        }
        recycleview.layoutManager = GridLayoutManager(requireContext(), 3)
        recycleview.adapter = adapter3
        recycleview.isNestedScrollingEnabled=false
        favouritesTextView.text = character.favourites.toString()
        adapter3.submitList(character.media.nodes)
    }
    private fun animateHeight(view: TextView) {
        val targetHeight = if (view.maxLines == Int.MAX_VALUE) view.getFullHeight() else view.getCollapsedHeight()

        val currentHeight = view.height
        val animator = ObjectAnimator.ofInt(view, "height", currentHeight, targetHeight)
        animator.duration = 300
        animator.start()
    }
    private fun TextView.getFullHeight(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return measuredHeight
    }

    private fun TextView.getCollapsedHeight(): Int {
        val lineHeight = lineHeight
        val maxLines = 3
        return lineHeight * maxLines
    }
}