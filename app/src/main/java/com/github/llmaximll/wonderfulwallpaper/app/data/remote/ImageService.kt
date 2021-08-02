package com.github.llmaximll.wonderfulwallpaper.app.data.remote

import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageService {

    @GET("/api/")
    suspend fun getImages(
        @Query("per_page") perPage: Int = 40,
        @Query("key") key: String,
        @Query("page") page: Int
    ): Response<ImageList>
}