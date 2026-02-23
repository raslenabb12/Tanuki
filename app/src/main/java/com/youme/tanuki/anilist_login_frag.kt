package com.youme.tanuki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class anilist_login_frag : Fragment() {
    private lateinit var tokenManager: AniListTokenManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val rootView = inflater.inflate(R.layout.anilist_login_layout, container, false)
        tokenManager = AniListTokenManager(requireContext())
        return rootView
    }
    private fun startAuthentication() {
        val authIntent = AniListAuthActivity.createIntent(requireContext())
        startActivityForResult(authIntent, 9001)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001 ){
            val accesstoken = tokenManager.getAccessToken()
            if (accesstoken!=null){
                switchToLibrary()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button).setOnClickListener {
            startAuthentication()
        }
    }
    private fun switchToLibrary() {
        (activity as? MainActivity)?.let { mainActivity ->
            mainActivity.fragmentStateManager.switchFragment(
                library_frag(),
                R.id.fragmentContainerView
            )
            mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = "Library"
            mainActivity.findViewById<ImageButton>(R.id.imageButton5)?.visibility = View.GONE
            mainActivity.findViewById<ImageButton>(R.id.imageButton6)?.visibility = View.GONE
        }
    }
}