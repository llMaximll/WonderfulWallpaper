package com.github.llmaximll.wonderfulwallpaper.app.data.remote

import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageList
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import javax.inject.Inject

class ImageRemoteDataSource @Inject constructor(
    private val imageService: ImageService
): BaseDataSource() {

    suspend fun getImages(key: String, page: Int): Resource<ImageList> =
        getResult { imageService.getImages(key = key, page = page) }
}