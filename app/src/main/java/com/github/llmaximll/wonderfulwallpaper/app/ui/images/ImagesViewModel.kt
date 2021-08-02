package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private var page = 1

    fun getImages(context: Context, page: Int = this.page): LiveData<Resource<List<Image>>> {
        return repository.getImages(
            key = context.getString(R.string.api_key),
            page = page
        )
    }
}