package com.github.llmaximll.wonderfulwallpaper.app.data.repository

import androidx.lifecycle.LiveData
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.local.ImageDao
import com.github.llmaximll.wonderfulwallpaper.app.data.remote.ImageRemoteDataSource
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.app.utils.performGetOperation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val remoteDataSource: ImageRemoteDataSource,
    private val localDataSource: ImageDao
) {

    fun getImages(
        key: String,
        page: Int,
        q: String,
        imageType: String,
        orientation: String,
        category: List<String>,
        colors: List<String>,
        editorsChoice: String,
        safeSearch: Boolean
    ): Flow<Resource<List<Image>>> =
    performGetOperation(
        databaseQuery = { localDataSource.getImages() },
        networkCall = { remoteDataSource.getImages(
            key = key,
            page = page,
            q = q,
            imageType = imageType,
            orientation = orientation,
            category = category,
            colors = colors,
            editorsChoice = editorsChoice,
            safeSearch = safeSearch
        ) },
        saveCallResult = { localDataSource.insertAll(it.hits) }
    )
}