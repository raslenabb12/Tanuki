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
data class MangaSearchParameters(
    val query: String? = null,
    val sort: MangaSortType = MangaSortType.START_DATE_DESC,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val status: MangaStatus? = null,
    val format: MangaFormat? = null,
    val countryOfOrigin : CountryofOrigin?=null,
    val seasonYear: Int? = null,
)
enum class MangaSortType {
    UPDATED_AT_DESC,
    POPULARITY_DESC,
    TRENDING_DESC,
    FAVORITES_DESC,
    SCORE_DESC,
    TITLE_ROMAJI,
    START_DATE_DESC
}

enum class MangaStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS
}

enum class MangaFormat {
    ONE_SHOT,
    NOVEL,
    MANGA
}
enum class CountryofOrigin {
    JP,
    KR,
    CN
}

class MangaPagingSource(
    private val searchParams: MangaSearchParameters
) : PagingSource<Int, Manga>() {
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
                    query (${"$"}page: Int, ${"$"}perPage: Int, ${"$"}search: String, ${"$"}sort: [MediaSort], ${"$"}genres: [String],${"$"}countryoforigin: CountryCode, ${"$"}tags: [String], ${"$"}status: MediaStatus, ${"$"}format: MediaFormat, ${"$"}seasonYear: Int) {
                      Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                        media(
                          type: MANGA,
                          sort: ${"$"}sort,
                          isAdult: false,
                          search: ${"$"}search,
                          genre_in: ${"$"}genres,
                          tag_in: ${"$"}tags,
                          status: ${"$"}status,
                          format: ${"$"}format,
                          countryOfOrigin: ${"$"}countryoforigin,
                          seasonYear: ${"$"}seasonYear
                        ) {
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

                val variables = mutableMapOf<String, Any?>(
                    "page" to page,
                    "perPage" to perPage,
                    "search" to searchParams.query,
                    "sort" to listOf(searchParams.sort.name),
                    "genres" to searchParams.genres.takeIf { it.isNotEmpty() },
                    "tags" to searchParams.tags.takeIf { it.isNotEmpty() },
                    "status" to searchParams.status?.name,
                    "format" to searchParams.format?.name,
                    "seasonYear" to searchParams.seasonYear,
                    "countryoforigin" to searchParams.countryOfOrigin?.name
                ).filterValues { it != null }
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

            val jsonAdapter = moshi.adapter(AniListResponse::class.java)
            val apiResponse = jsonAdapter.fromJson(responseJson)
            val mangaList = apiResponse?.data?.Page?.media ?: emptyList()
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