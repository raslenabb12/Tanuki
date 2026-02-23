package com.youme.tanuki

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
data class AniListResponse3(
    val data: Data2
)

data class Data2(
    val User: User2
)

data class User2(
    val id: Int,
    val name: String,
    val favourites: Favourites
)

data class Favourites(
    val manga: Manga3
)

data class Manga3(
    val nodes: List<Manga>
)
class Favourites_pagingsource(private val userid: Int? = null) : PagingSource<Int, Manga>() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Manga> {
        val page = params.key ?: 1
        val perPage = params.loadSize
        return try {
            val responseJson = withContext(Dispatchers.IO) {
                val query = """
       query (${"$"}page:Int,${"$"}userid:Int,${"$"}perPage: Int){
  User(id:${"$"}userid) {
    id
    name
    favourites{
      manga (page:${"$"}page,   perPage:${"$"}perPage){
        nodes{
                                    id
                          title {
                            romaji
                            english
                          }
                          coverImage {
                            extraLarge
                            large
                            medium
                          }
                          chapters
                          countryOfOrigin
        }
        }
      }
    }
  }
                """.trimIndent()
                val variables = mutableMapOf<String, Any?>(
                    "page" to page,
                    "perPage" to perPage
                ).apply {
                    userid?.let { this["userid"] = it }
                }
                val requestBody = JSONObject().apply {
                    put("query", query)
                    put("variables", JSONObject(variables))
                }.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("HTTP error: ${response.code}")
                    response.body?.string() ?: throw Exception("Empty response body")
                }
            }
            val jsonAdapter = moshi.adapter(AniListResponse3::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val mangaList = apiResponse?.data?.User?.favourites?.manga?.nodes ?: emptyList()
            LoadResult.Page(
                data = mangaList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (mangaList.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, Manga>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}