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

class CharactersPagingSource(private val searchQuery: String? = null) : PagingSource<Int, CharacterNode>() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterNode> {
        val page = params.key ?: 1
        val perPage = params.loadSize
        return try {
            val responseJson = withContext(Dispatchers.IO) {
                val query = """
                    query (${"$"}page: Int, ${"$"}perPage: Int, ${"$"}search: String) {
                        Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                            characters(sort: FAVOURITES_DESC, search: ${"$"}search) {
      name {
        full
      }
      image {
        large
        medium
      }
      favourites
      description(asHtml:true)
      bloodType
      gender
      age
      media (type:MANGA){
        nodes{
          id
            title {
              romaji
              english
            }
            coverImage {
              extraLarge
              medium
              large
            }
            type
            status
            countryOfOrigin
            bannerImage
            updatedAt
            isAdult
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
                    searchQuery?.let { this["search"] = it }
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
            val jsonAdapter = moshi.adapter(AniListResponse2::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val Characterslist = apiResponse?.data?.Page?.characters!!
            LoadResult.Page(
                data = Characterslist,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (Characterslist.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, CharacterNode>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}