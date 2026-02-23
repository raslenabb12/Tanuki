package com.youme.tanuki

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MangaListItem(
    val node: MangaNode,
    val list_status: MangaListStatus
)

data class MangaNode(
    val id: Int,
    val title: String,
    val main_picture:MangaPicture
)
data class MangaListStatus(
    val status: String,
    val is_rereading: Boolean,
    val num_chapters_read: Int,
    val num_volumes_read: Int,
    val score: Int
)
data class MangaPicture(
    val medium :String,
    val large:String
)
data class MangaLibraryResponse(
    val data: List<MangaListItem>,
    val paging: MangaPaging
)

data class MangaPaging(
    val next: String? = null
)
data class user(
    val id :Int,
    val name:String,
    val location:String,
    val joined_at:String,
    val picture:String

)
interface MyAnimeListService {
    @GET("v2/users/@me/mangalist")
    suspend fun getUserMangaLibrary(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("fields") fields: String = "list_status"
    ): MangaLibraryResponse
}
interface MyAnimeListUser {
    @GET("v2/users/@me")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Query("fields") fields: String = "picture"
    ): user
}
class MangaLibrarySyncManager(private val accessToken: String) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.myanimelist.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val malService = retrofit.create(MyAnimeListService::class.java)

    suspend fun syncMangaLibrary(status: String? = null): List<MangaListItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = malService.getUserMangaLibrary(
                    token = "Bearer $accessToken",
                    status = status
                )
                response.data
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    suspend fun syncFullMangaLibrary(): List<MangaListItem> {
        val statuses = listOf("reading", "completed", "on_hold", "dropped", "plan_to_read")
        val allManga = mutableListOf<MangaListItem>()
        statuses.forEach { status ->
            allManga.addAll(syncMangaLibrary(status))
        }
        return allManga
    }
}

class MangaLibraryViewModel(
    private val accessToken: String
) : ViewModel() {
    private val syncManager = MangaLibrarySyncManager(accessToken)
    private val _mangaLibrary = MutableLiveData<List<MangaListItem>>()
    val mangaLibrary: MutableLiveData<List<MangaListItem>> = _mangaLibrary
    fun fetchMangaLibrary(status: String? = null) {
        viewModelScope.launch {
            _mangaLibrary.value = syncManager.syncMangaLibrary(status)
        }
    }
    fun fetchFullLibrary() {
        viewModelScope.launch {
            _mangaLibrary.value = syncManager.syncFullMangaLibrary()
        }
    }
}