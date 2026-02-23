package com.youme.tanuki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class MangaViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _media_id = MutableStateFlow<String?>(null)
    private val _userid = MutableStateFlow<Int?>(null)
    private val _accesstoken = MutableStateFlow<String?>(null)
    private val _searchParameters = MutableStateFlow(MangaSearchParameters())
    val searchParameters: StateFlow<MangaSearchParameters> = _searchParameters.asStateFlow()
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()
    val media_id: StateFlow<String?> = _media_id.asStateFlow()
    val userid: StateFlow<Int?> = _userid.asStateFlow()
    val accesstoken: StateFlow<String?> = _accesstoken.asStateFlow()
    val mangaFlow: Flow<PagingData<Manga>> = searchParameters
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    MangaPagingSource(searchParams = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val mangaFlow_home: Flow<PagingData<Manga>> = searchParameters
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    prefetchDistance = 0,
                    maxSize = 10,
                    initialLoadSize = 10,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    MangaPagingSource(searchParams = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val Trending_manga: Flow<PagingData<Manga>> = _searchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    Treding_manga_repo(searchQuery = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val Trending_manga_home: Flow<PagingData<Manga>> = _searchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    maxSize = 10,
                    prefetchDistance = 0,
                    initialLoadSize = 10,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    Treding_manga_repo(searchQuery = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val popular_manga: Flow<PagingData<Manga>> = _searchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    popular_manga_repo(searchQuery = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val Favourites_pager: Flow<PagingData<Manga>> = _userid
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    Favourites_pagingsource(userid = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val followers_pager : Flow<PagingData<userdetails>> = combine(userid, accesstoken) { userId, accessToken ->
        userId to accessToken
    }.flatMapLatest {( userId, accessToken)  ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    followers_pager_repo(userid = userId,accesstoken=accessToken)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val following_pager: Flow<PagingData<userdetails>> = combine(userid, accesstoken) { userId, accessToken ->
        userId to accessToken
    }.flatMapLatest { (userId, accessToken) ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    following_pager_repo(userid =userId, accesstoken= accessToken)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val popular_manga_home: Flow<PagingData<Manga>> = _searchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    maxSize = 10,
                    prefetchDistance = 0,
                    initialLoadSize = 10,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    popular_manga_repo(searchQuery = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val getreviews: Flow<PagingData<review>> = _media_id
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    reveiws_view_pager(media_id = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val getreviews_home: Flow<PagingData<review>> = _media_id
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    maxSize = 10,
                    prefetchDistance = 0,
                    initialLoadSize = 10,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    reveiws_view_pager(media_id = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)
    val charactersFlow: Flow<PagingData<CharacterNode>> = _searchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    CharactersPagingSource(searchQuery = query)
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun clearSearch() {
        _searchQuery.value = null
    }
    fun updateSearchParameters(update: MangaSearchParameters.() -> MangaSearchParameters) {
        _searchParameters.value = update(_searchParameters.value)
    }
    fun setreview_mediaid(query: String?){
        _media_id.value=query
    }
    fun set_userid(query: Int?){
        _userid.value=query
    }
    fun set_accesstoken(query: String?){
        _accesstoken.value=query
    }
    fun setseachcharacte(query: String?){
        _searchQuery.value=query
    }
    fun setSearchQuery(query: String?) {
        updateSearchParameters { copy(query = if (query.isNullOrBlank()) null else query) }
    }

    fun setSortType(sort: MangaSortType) {
        updateSearchParameters { copy(sort = sort) }
    }

    fun setGenres(genres: List<String>) {
        updateSearchParameters { copy(genres = genres) }
    }
    fun setTags(tags: List<String>) {
        updateSearchParameters { copy(tags = tags) }
    }

    fun setStatus(status: MangaStatus?) {
        updateSearchParameters { copy(status = status) }
    }

    fun setFormat(format: MangaFormat?) {
        updateSearchParameters { copy(format = format) }
    }
    fun setCountryoforigin(country: CountryofOrigin?) {
        updateSearchParameters { copy(countryOfOrigin = country) }
    }
    fun setSeasonYear(year: Int?) {
        updateSearchParameters { copy(seasonYear = year) }
    }

}
