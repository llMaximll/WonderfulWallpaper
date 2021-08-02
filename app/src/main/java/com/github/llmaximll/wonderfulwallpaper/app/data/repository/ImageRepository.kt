package com.github.llmaximll.wonderfulwallpaper.app.data.repository

import androidx.lifecycle.LiveData
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.local.ImageDao
import com.github.llmaximll.wonderfulwallpaper.app.data.remote.ImageRemoteDataSource
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.app.utils.performGetOperation
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val remoteDataSource: ImageRemoteDataSource,
    private val localDataSource: ImageDao
) {

    fun getImages(key: String, page: Int): LiveData<Resource<List<Image>>> = performGetOperation(
        databaseQuery = { localDataSource.getImages() },
        networkCall = { remoteDataSource.getImages(key, page = page) },
        saveCallResult = { localDataSource.insertAll(it.hits) }
    )
}