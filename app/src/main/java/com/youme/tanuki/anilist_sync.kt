package com.youme.tanuki

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class MediaListItem(
    val id: Int,
    val status: String,
    val score: Double,
    val progress: Int,
    val media: Manga
)


data class MediaTitle(
    val userPreferred: String,
    val romaji: String,
    val english: String?
)

data class anilist_main (
    val data :MediaListItem
)
data class  dataviewe(
    val data:viewsr
)
data class viewsr(
    val Viewer:userdetails
)
data class  datavieweuser(
    val data:userder
)
data class userder(
    val User:userdetails,
    val followers_num:followers_num,
    val following_num : following_num
)
data class id_s(
    val id:Int
)
data class total_num(
    val total:Int
)
data class following_num(
    val following:List<id_s>,
    val pageInfo:total_num
)
data class followers_num(
    val followers:List<id_s>,
    val pageInfo:total_num
)
data class userdetails (
    val id :Int,
    val name:String,
    val avatar:CoverImage,
    val bannerImage: String?=null,
    val createdAt:Int?=null,
    val about:String?=null,
    val isFollowing:Boolean?=null,
    val unreadNotificationCount:Int?=null
)
class AniListSyncManager(private val accessToken: String) {
    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    suspend fun getuserid(): userdetails? {
        return withContext(Dispatchers.IO) {
            try {
                val query = """
                    query {
  Viewer {
    id
    name
    avatar {
      large
      medium
    }
    unreadNotificationCount
  }
}
                """.trimIndent()
                val requestBody = JSONObject().apply {
                    put("query", query)
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val jsonAdapter = moshi.adapter(dataviewe::class.java)
                val apiResponse = jsonAdapter.fromJson(responseBody)
                return@withContext apiResponse?.data?.Viewer
            } catch (e: Exception) {
                Log.e("AniListSync", "Error syncing manga list", e)
                null
            }
        }
    }
    suspend fun getuserdetailsfromid(userid: Int): userder? {
        return withContext(Dispatchers.IO) {
            try {
                val query = """
query (${"$"}userid:Int!){
  User (id:${"$"}userid){
    id
    name
    bannerImage
    createdAt
    avatar {
      large
      medium
    }
    about(asHtml:true)
  }
  followers_num:Page(perPage:1,page:1){
    followers(userId:${"$"}userid) {
      id
    }
    pageInfo{
      total
    }
  }
  following_num:Page(perPage:1,page:1){
    following(userId:${"$"}userid) {
      id
    }
    pageInfo{
      total
    }
  }
}            """.trimIndent()
                val variables = mapOf("userid" to userid)
                val requestBody = JSONObject().apply {
                    put("query", query)
                    put("variables", JSONObject(variables))
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val jsonAdapter = moshi.adapter(datavieweuser::class.java)
                val apiResponse = jsonAdapter.fromJson(responseBody)
                return@withContext apiResponse?.data
            } catch (e: Exception) {
                Log.e("AniListSync", "Error syncing manga list", e)
                null
            }
        }
    }
    data class MediaListCollectionResponse(
        val data: MediaListCollectionData
    )

    data class MediaListCollectionData(
        val MediaListCollection: MediaListCollection
    )

    data class MediaListCollection(
        val lists: List<MediaList>
    )

    data class MediaList(
        val entries: List<MediaListEntry>
    )

    data class MediaListEntry(
        val media: Media
    )

    data class Media(
        val recommendations: Recommendations
    )

    data class Recommendations(
        val nodes: List<RecommendationNode>
    )

    data class RecommendationNode(
        val rating: Int,
        val mediaRecommendation: MediaRecommendation
    )

    data class MediaRecommendation(
        val id: Int,
        val title: Title,
        val coverImage: CoverImage,
        val description: String?,
        val averageScore: Int?,
        val mediaListEntry:mediaListEntry?=null,
        val genres: List<String>
    )

    data class Title(
        val romaji: String?,
        val english: String?
    )

    data class CoverImage(
        val large: String
    )

    suspend fun getuserrecommandedmanga(userid:Int,status: String? = null): List<MediaRecommendation> {
        return withContext(Dispatchers.IO) {
            try {
                val query = """
                               query(${"$"}user:Int) {
              MediaListCollection(userId: ${"$"}user, type: MANGA, sort: SCORE_DESC) {
                lists {
                  entries {
                    media {
                      recommendations(sort: RATING_DESC) {
                        nodes {
                          rating
                          mediaRecommendation {
                            id
                            title {
                              romaji
                              english
                            }
                            coverImage {
                              large
                            }
                            mediaListEntry {
    id
    mediaId
        status
        progress
        startedAt {
        year
        month
        day
      }
        completedAt {
        year
        month
        day
      }
      }
                            description
                            averageScore
                            genres
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
                        """.trimIndent()
                val variables = mutableMapOf<String, Any?>(
                    "user" to userid
                )
                val requestBody = JSONObject().apply {
                    put("query", query)
                    put("variables", JSONObject(variables))
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext emptyList()
                parseMediaListResponserecommended(responseBody)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    private fun parseMediaListResponserecommended(responseBody: String): List<MediaRecommendation> {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(MediaListCollectionResponse::class.java)
        return adapter.fromJson(responseBody)?.let { response ->
            response.data.MediaListCollection.lists
                .flatMap { it.entries }
                .flatMap { it.media.recommendations.nodes }
                .map { it.mediaRecommendation }
        } ?: emptyList()
    }
    suspend fun syncMangaList(userid:Int,status: String? = null): List<MediaListItem> {
        return withContext(Dispatchers.IO) {
            try {
                val query = """
                    query(${"$"}user:Int) {
                        MediaListCollection(userId: ${"$"}user,type: MANGA) {
                            lists {
                                entries {
                                    id
                                    status
                                    score
                                    progress
                                    media {
                                        id
                                        isFavourite
                                        title {
                                            userPreferred
                                            romaji
                                            english
                                        }
                                        coverImage {
                                            medium
                                            large
                                        }
                                        chapters
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent()
                val variables = mutableMapOf<String, Any?>(
                    "user" to userid
                )
                val requestBody = JSONObject().apply {
                    put("query", query)
                    put("variables", JSONObject(variables))
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext emptyList()
                parseMediaListResponse(responseBody)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parseMediaListResponse(responseBody: String): List<MediaListItem> {
        val mediaList = mutableListOf<MediaListItem>()
        try {
            val json = JSONObject(responseBody)
            val lists = json.getJSONObject("data")
                .getJSONObject("MediaListCollection")
                .getJSONArray("lists")

            for (i in 0 until lists.length()) {
                val entries = lists.getJSONObject(i).getJSONArray("entries")
                for (j in 0 until entries.length()) {
                    val entry = entries.getJSONObject(j)
                    val mediaObject = entry.getJSONObject("media")
                    val titleObject = mediaObject.getJSONObject("title")
                    val coverImageObject = mediaObject.getJSONObject("coverImage")
                    mediaList.add(
                        MediaListItem(
                            id = entry.getInt("id"),
                            status = entry.getString("status"),
                            score = entry.getDouble("score"),
                            progress = entry.getInt("progress"),
                            media = Manga(
                                id = mediaObject.getInt("id"),
                                title = Title(
                                    userPreferred = titleObject.getString("userPreferred"),
                                    romaji = titleObject.getString("romaji"),
                                    english = if (titleObject.getString("english").indexOf("null")==-1) titleObject.getString("english") else null
                                ),
                                isFavourite = mediaObject.getBoolean("isFavourite"),
                                coverImage = CoverImage(
                                    medium = coverImageObject.getString("medium"),
                                    large = coverImageObject.getString("large")
                                ),
                                chapters = if (mediaObject.has("chapters") && !mediaObject.isNull("chapters"))
                                    mediaObject.getInt("chapters") else null
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AniListSync", "Error parsing response", e)
        }
        return mediaList
    }
}
class MangaLibraryViewModel2(
    private val accessToken: String
) : ViewModel() {
    private val syncManager = AniListSyncManager(accessToken)
    private val _mangaLibrary = MutableLiveData<List<MediaListItem>>()
    val mangaLibrary: MutableLiveData<List<MediaListItem>> = _mangaLibrary
    private var _user = MutableLiveData<userdetails>()
    private var _searchuser = MutableLiveData<userder>()
    private var userrecommanded = MutableLiveData<List<AniListSyncManager.MediaRecommendation>>()
    val user : MutableLiveData<userdetails> = _user
    val search_user : MutableLiveData<userder> = _searchuser
    val getuserrecom : MutableLiveData<List<AniListSyncManager.MediaRecommendation>> = userrecommanded
    fun fetchMangaLibrary(userid: Int, status: String? = null) {
        viewModelScope.launch {
            _mangaLibrary.value = syncManager.syncMangaList(userid,status)
        }
    }
    fun getuserid(){
        viewModelScope.launch {
            user.value = syncManager.getuserid()
        }
    }
    fun get_user_details_from_id(userid: Int){
        viewModelScope.launch {
            search_user.value = syncManager.getuserdetailsfromid(userid)
        }
    }
    fun getuserrecommanded(userid: Int){
        viewModelScope.launch {
            getuserrecom.value = syncManager.getuserrecommandedmanga(userid)
        }
    }

}