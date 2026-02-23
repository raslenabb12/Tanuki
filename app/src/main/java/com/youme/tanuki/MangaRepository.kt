package com.youme.tanuki

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
data class datareeview(
    val data:daterev
)
data class daterev(
    val Review: review
)
class MangaRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
) {
    private fun convertHtmlToPlainText(html: String?): String {
        return html?.let {
            Jsoup.parse(it).text()
        } ?: ""
    }

    suspend fun fetchMangaDetails(accessToken:String?=null,name: String): Result<MangaDetails?> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query (${'$'}search: String) {
                  Media(search: ${'$'}search, type: MANGA ) {
                    id
                    title {
                      romaji
                      english
                      native
                    }
                    description
                    isFavourite
                    stats{
      scoreDistribution {
        score
        amount
      }
      statusDistribution {
        status
        amount
      }
    }
                    externalLinks {
      site
      type
      icon
      url
      color
      language
    }
                    coverImage {
                      extraLarge
                      large
                      medium
                    }
                    
                    status
                    chapters
                    favourites
                    trailer {
        id
        thumbnail
      }
                    genres
                    averageScore
                    popularity
                    bannerImage
                    startDate {
      year
      month
      day
    } 
    endDate {
      year
      month
      day
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
    characters {
      edges{
        node{
          name {
            full
          }
          image{
            medium
            large
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
            updatedAt
            isAdult
        }
      }
        }
        role
      }
      }
      relations {
        nodes {
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
            status
            type
            countryOfOrigin
            updatedAt
            isAdult
        }
      }
      
                  }
                }
            """.trimIndent()
            val variables = mapOf("search" to name)
            val requestBody = JSONObject().apply {
                put("query", query)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .apply {
                    accessToken?.let {
                        if (accessToken!="null") addHeader("Authorization", "Bearer $it")
                    }
                }
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonAdapter = moshi.adapter(MangaDetailResponse::class.java)
            val apiResponse = jsonAdapter.fromJson(responseBody)
            Result.success(apiResponse?.data?.Media?.copy( description = convertHtmlToPlainText(apiResponse.data?.Media?.description)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun searchMangas(query: String, page: Int = 1, perPage: Int = 10): Result<List<Manga>> = withContext(Dispatchers.IO) {
        try {
            val graphqlQuery = """
                query (${'$'}search: String, ${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(search: ${'$'}search, type: MANGA) {
                      id
                      title {
                        romaji
                        english
                      }
                      coverImage {
                        medium
                      }
                      status
                      chapters
                    }
                  }
                }
            """.trimIndent()

            val variables = mapOf(
                "search" to query,
                "page" to page,
                "perPage" to perPage
            )
            val requestBody = JSONObject().apply {
                put("query", graphqlQuery)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonAdapter = moshi.adapter(MangaSearchResponse::class.java)
            val apiResponse = jsonAdapter.fromJson(responseBody)
            Result.success(apiResponse?.data?.Page?.media ?: emptyList())
        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    suspend fun getAlltags(): Result<List<MediaTag>> = withContext(Dispatchers.IO) {
        try {
            val graphqlQuery = """
                query  {
  MediaTagCollection {
    name
  }
}
            """.trimIndent()
            val requestBody = JSONObject().apply {
                put("query", graphqlQuery)
            }.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonAdapter = moshi.adapter(GetTagsResponse::class.java)
            val apiResponse = jsonAdapter.fromJson(responseBody)
            Result.success(apiResponse?.data?.MediaTagCollection ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getReviewInfo(reviewId: Int): Result<review?> = withContext(Dispatchers.IO) {
        try {
            val graphqlQuery = """
            query(${"$"}reviewId: Int) {
                Review(id: ${"$"}reviewId) {
                    id
                    summary
                    score
                    rating
                    body(asHtml: true)
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
            }
        """.trimIndent()

            val variables = mapOf(
                "reviewId" to reviewId
            )

            val requestBody = JSONObject().apply {
                put("query", graphqlQuery)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonAdapter = moshi.adapter(datareeview::class.java)
            val apiResponse = jsonAdapter.fromJson(responseBody)

            Result.success(apiResponse?.data?.Review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateMangaProgress(
        accessToken: String,
        mediaId: Int,
        progress: Int? = null,
        status: String? = null,
        startedAt: Date? = null,
        completedAt: Date? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val mutation = """
            mutation(
                ${'$'}mediaId: Int, 
                ${'$'}status: MediaListStatus, 
                ${'$'}progress: Int,
                ${'$'}startedAt: FuzzyDateInput,
                ${'$'}completedAt: FuzzyDateInput
            ) {
                SaveMediaListEntry(
                    mediaId: ${'$'}mediaId,
                    status: ${'$'}status,
                    progress: ${'$'}progress,
                    startedAt: ${'$'}startedAt,
                    completedAt: ${'$'}completedAt
                ) {
                    id
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
            }
        """.trimIndent()
            val variables = buildMap<String, Any?> {
                put("mediaId", mediaId)
                if (status != null) put("status", status)
                if (progress != null) put("progress", progress)
                if (startedAt != null) put("startedAt", startedAt)
                if (completedAt != null) put("completedAt", completedAt)
            }
            val requestBody = JSONObject().apply {
                put("query", mutation)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            return@withContext if (response.isSuccessful && responseBody != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update manga progress: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e("MangaRepository", "Error updating manga progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
    suspend fun Togglefaivorites(
        accessToken: String,
        mediaId: Int
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val mutation = """
            mutation(${'$'}mangaId: Int) {
                ToggleFavourite(mangaId: ${'$'}mangaId) {
                    manga {
                        pageInfo {
                currentPage
            }
                    }
                }
            }
        """.trimIndent()
            val variables = buildMap<String, Any?> {
                put("mangaId", mediaId)
            }
            val requestBody = JSONObject().apply {
                put("query", mutation)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            return@withContext if (response.isSuccessful && responseBody != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update manga progress: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e("MangaRepository", "Error updating manga progress: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun removeMangaFromList(
        accessToken: String,
        mediaId: Int
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val mutation = """
            mutation(${'$'}mangaid: Int) {
                DeleteMediaListEntry(id: ${'$'}mangaid) {
                    deleted
                }
            }
        """.trimIndent()
            val variables = mapOf("mangaid" to mediaId)
            val requestBody = JSONObject().apply {
                put("query", mutation)
                put("variables", JSONObject(variables))
            }.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            return@withContext if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val deleted = jsonResponse.optJSONObject("data")
                    ?.optJSONObject("DeleteMediaListEntry")
                    ?.optBoolean("deleted") ?: false
                Result.success(deleted)
            } else {
                Result.failure(Exception("Failed to remove manga: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e("MangaRepository", "Error removing manga: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
data class MangaSearchResponse(
    val data: MangaSearchData?
)

data class MangaSearchData(
    val Page: MangaSearchPage?
)

data class MangaSearchPage(
    val media: List<Manga>?
)