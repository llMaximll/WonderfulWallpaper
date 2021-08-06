package com.github.llmaximll.wonderfulwallpaper.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Response

fun <T, A> performGetOperation(
    databaseQuery: () -> Flow<T>,
    networkCall: suspend () -> Resource<A>,
    saveCallResult: suspend (A) -> Unit
): Flow<Resource<T>> =
    flow {
        emit(Resource.loading())
//        val sourceDB = databaseQuery.invoke().map { Resource.success(it) }
//        emitAll(sourceDB)

        val response = networkCall.invoke()
        if (response.status == Resource.Status.SUCCESS) {
//            saveCallResult(response.data!!)
            ((Resource.success((response.data as ImageList).hits)) as Resource<T>)?.let { emit(it) }
        } else if (response.status == Resource.Status.ERROR) {
            emit(Resource.error(response.message!!))
        }
    }