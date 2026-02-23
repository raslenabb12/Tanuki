package com.youme.tanuki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class RecommendationsViewModel : ViewModel() {
    fun getRecommendations(mediaId: Int): Flow<PagingData<RecommendationNode>> {
        val newResult = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { recommendations_manga_repo(mediaId) }
        ).flow.cachedIn(viewModelScope)
        return newResult
    }
}
