package com.youme.tanuki

import java.time.temporal.TemporalAmount

//latest manga data structure
data class AniListResponse(
    val data: DataWrapper
)

data class DataWrapper(
    val Page: Page
)

data class Page(
    val media: List<Manga>
)
data class AniListResponse2(
    val data: DataWrapper2
)

data class DataWrapper2(
    val Page: Page2
)

data class Page2(
    val characters: List<CharacterNode>
)

data class Manga(
    val id: Int,
    val title: Title,
    val coverImage: CoverImage,
    val isAdult: Boolean?=null,
    val chapters: Int?=null,
    val isFavourite:Boolean?=null,
    val status: String?=null,
    val countryOfOrigin :String?=null,
    val updatedAt: Long?=null,
    val trending:Int?=0,
    val type:String?=null,
    val startDate: Date?=null,
    val bannerImage:String?=null,
    val calculatedRank: Int? = null

)
data class mediaListEntry(
    val mediaId:Int?=null,
    val id :Int?=null,
    val status: String?,
    val progress : Int,
    val startedAt :Date?=null,
    val completedAt : Date?=null
)
data class MediaTag(
    val name: String
)

data class GetTagsResponse(
    val data: DataWrapper3?
)

data class DataWrapper3(
    val MediaTagCollection: List<MediaTag>?
)
data class Title(
    val userPreferred:String?=null,
    val romaji: String,
    val english: String?
)
data class CoverImage(
    val extraLarge:String?=null,
    val large: String,
    val medium: String,
    val color:String?=null
)
// manga details structure
data class MangaDetailResponse(
    val data: MangaDetailData?
)

data class MangaDetailData(
    val Media: MangaDetails?
)

data class MangaDetails(
    val id: Int,
    val title: Title?,
    val description: String?=null,
    val coverImage: CoverImage?,
    val startDate: Date?,
    val endDate: Date?,
    val status: String?,
    val chapters: Int?,
    val volumes: Int?,
    val genres: List<String>?,
    val tags: List<Tag>?,
    val averageScore: Int?,
    val isFavourite:Boolean?=false,
    val popularity: Int?,
    val bannerImage:String?=null,
    val favourites: Int?,
    val trailer : trailder?=null,
    val externalLinks : List<externalLinksdata>?=null,
    val isAdult: Boolean?,
    val siteUrl: String?,
    val characters: CharacterConnection?,
    val staff: StaffConnection?,
    val mediaListEntry:mediaListEntry?=null,
    val recommendations: RecommendationConnection?,
    val relations:Media?=null,
    val stats : status_data
)
data class status_data(
    val scoreDistribution : List<scoreDistribution>,
    val statusDistribution:List<statusDistribution>
)
data class statusDistribution(
    val status:String,
    val amount:Int,
)
data class scoreDistribution(
    val score :Int,
    val amount: Int
)
data class externalLinksdata(
    val site:String?=null,
    val type:String?=null,
    val icon:String?=null,
    val url:String?=null,
    val color: String?=null,
    val language:String?=null,
)
data class trailder(
    val id :String,
    val thumbnail:String
)
data class Date(
    var year: Int?,
    var month: Int?,
    var day: Int?
)

data class Tag(
    val name: String?,
    val description: String?
)

data class CharacterConnection(
    val edges: List<CharacterEdge>?
)

data class CharacterEdge(
    val node: CharacterNode?,
    val role: String?
)

data class CharacterNode(
    val name: CharacterName?,
    val image: CharacterImage?,
    val favourites:Int,
    val description :String?="No Description",
    val bloodType:String?=null,
    val gender  :String?=null,
    val age : String?=null,
    val media : Media
)
data class MangaConnection(
    val edges: List<MangaEdge>?
)
data class MangaEdge(
    val node: MangaDetails?
)
data class CharacterName(
    val full: String?
)
data class Media(
    val nodes : List<Manga>
)
data class CharacterImage(
    val medium: String,
    val large: String?
)

data class StaffConnection(
    val edges: List<StaffEdge>?
)

data class StaffEdge(
    val node: StaffNode?,
    val role: String?
)

data class StaffNode(
    val name: StaffName?
)

data class StaffName(
    val full: String?
)

data class RecommendationConnection(
    val nodes: List<RecommendationNode>?
)

data class RecommendationNode(
    val rating:Int,
    val mediaRecommendation: RecommendedMedia?
)

data class RecommendedMedia(
    val id: Int,
    val title: Title?,
    val countryOfOrigin:String,
    val coverImage: CoverImage?
)



