package com.github.llmaximll.wonderfulwallpaper.app.data.remote

import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ImageService {

    @GET("/api/")
    suspend fun getImages(
        @Query("per_page") perPage: Int = 80,
        @Query("key") key: String,
        @Query("page") page: Int = 1,
        @Query("q") q: String = "",
        @Query("image_type") imageType: String,
        @Query("orientation") orientation: String,
        @Query("category") category: List<String>,
        @Query("colors") colors: List<String>,
        @Query("editors_choice") editorsChoice: String,
        @Query("safesearch") safeSearch: Boolean
    ): Response<ImageList>
}