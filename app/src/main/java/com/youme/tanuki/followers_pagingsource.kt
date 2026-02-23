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



data class datafollowers(
    val data:followers_page
)
data class followers_page(
    val Page:Page_data
)
data class Page_data(
    val followers:List<userdetails>
)
class followers_pager_repo(private val userid: Int? = null,private val accesstoken: String? = null) : PagingSource<Int, userdetails>() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, userdetails> {
        val page = params.key ?: 1
        val perPage = params.loadSize
        return try {
            val responseJson = withContext(Dispatchers.IO) {
                val query = """
                    query (${"$"}page: Int, ${"$"}perPage: Int,${"$"}userid:Int!) {
                      Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                      followers(userId:${"$"}userid) {
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
                accesstoken?.let {
                    request.addHeader("Authorization", "Bearer $accesstoken")
                }
                client.newCall(request.build()).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("HTTP error: ${response.code}")
                    response.body?.string() ?: throw Exception("Empty response body")
                }
            }
            val jsonAdapter = moshi.adapter(datafollowers::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val mangaList = apiResponse?.data?.Page?.followers ?: emptyList()
            LoadResult.Page(
                data = mangaList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (mangaList.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, userdetails>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}