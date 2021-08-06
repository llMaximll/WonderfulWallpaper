package com.github.llmaximll.wonderfulwallpaper.app.data.remote

import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageList
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import javax.inject.Inject

class ImageRemoteDataSource @Inject constructor(
    private val imageService: ImageService
): BaseDataSource() {

    suspend fun getImages(
        key: String,
        page: Int,
        q: String,
        imageType: String,
        orientation: String,
        category: List<String>,
        colors: List<String>,
        editorsChoice: String
    ): Resource<ImageList> =
    getResult {
        imageService.getImages(
            key = key,
            page = page,
            q = q,
            imageType = imageType,
            orientation = orientation,
            category = category,
            colors = colors,
            editorsChoice = editorsChoice
        )
    }
}