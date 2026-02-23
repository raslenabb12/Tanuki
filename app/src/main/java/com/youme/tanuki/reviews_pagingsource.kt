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

data class revievm(
    val data:reviewsdp
)
data class reviewsdp(
    val Page : reviewpage
)
data class reviewpage(
    val reviews : List<review>
)
data class review(
    val id :Int,
    val summary : String,
    val score: Int,
    val rating : Int,
    val ratingAmount:Int,
    val user: userdetails,
    val body:String?=null,
    val createdAt:Int,
    val media: Manga
)
class reveiws_view_pager(private val media_id: String? = null) : PagingSource<Int, review>() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, review> {
        val page = params.key ?: 1
        val perPage = params.loadSize
        return try {
            val responseJson = withContext(Dispatchers.IO) {
                val query = """
                   query (${"$"}mediaid:Int,${"$"}page: Int,${"$"}perPage:Int) {
  Page(page: ${"$"}page,perPage:${"$"}perPage) {
    reviews(mediaId:${"$"}mediaid,mediaType:MANGA,sort: CREATED_AT_DESC) {
      id
      summary
      score
      rating
      ratingAmount
      createdAt
      user {
        id
        name
        avatar {
        large
          medium
        }
      }
      media {
        id
        title {
          userPreferred
          romaji
          english
        }
        type
        format
        coverImage {
        large
          medium
        }
      }
    }
    pageInfo {
      total
      currentPage
      hasNextPage
    }
  }
}
                """.trimIndent()

                val variables = mutableMapOf<String, Any?>(
                    "page" to page,
                    "perPage" to perPage
                ).apply {
                    media_id?.let { this["mediaid"] = it }
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

            val jsonAdapter = moshi.adapter(revievm::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val mangaList = apiResponse?.data?.Page?.reviews ?: emptyList()
            LoadResult.Page(
                data = mangaList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (mangaList.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, review>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}