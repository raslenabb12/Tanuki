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

data class datarecom(
    val data:datarecompage
)
data class datarecompage (
    val Page:datarecom2
)

data class datarecom2(
    val recommendations : List<RecommendationNode>
)

class recommendations_manga_repo(private val mediaId: Int) : PagingSource<Int, RecommendationNode>() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecommendationNode> {
        val page = params.key ?: 1
        val perPage = params.loadSize
        return try {
            val responseJson = withContext(Dispatchers.IO) {
                val query = """
                    query (${"$"}page: Int, ${"$"}perPage: Int, ${"$"}mediaId: Int) {
                        Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                            recommendations(mediaId: ${"$"}mediaId,sort:RATING_DESC) {
                                rating
                                mediaRecommendation {
                                    id
                                    title {
                                        romaji
                                        english
                                    }
                                    countryOfOrigin
                                    coverImage {
                                        extraLarge
                                        large
                                        medium
                                    }
                                }
                            }
                            pageInfo {
                                total
                                currentPage
                                lastPage
                                hasNextPage
                                perPage
                            }
                        }
                    }
                """.trimIndent()

                val variables = mapOf(
                    "page" to page,
                    "perPage" to perPage,
                    "mediaId" to mediaId
                )

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

            val jsonAdapter = moshi.adapter(datarecom::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val recommendationsList = apiResponse?.data?.Page?.recommendations ?: emptyList()
            LoadResult.Page(
                data = recommendationsList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (recommendationsList.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecommendationNode>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}