package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageFavorite
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _buttonsState = MutableStateFlow(true)
    val buttonsState = _buttonsState.asStateFlow()

    private val _favoriteState = MutableStateFlow(false)
    val favoriteState = _favoriteState.asStateFlow()

    private val _scaleState = MutableStateFlow(true)
    val scaleState = _scaleState.asStateFlow()

    fun toggleButtonsState(state: Boolean? = null) {
        _buttonsState.value = state ?: !_buttonsState.value
    }

    fun toggleFavoriteState(state: Boolean? = null) {
        _favoriteState.value = state ?: !_favoriteState.value
    }

    fun toggleScaleState(state: Boolean? = null) {
        _scaleState.value = state ?: !_scaleState.value
    }

    suspend fun getFavoriteImage(id: String): ImageFavorite? =
        repository.getFavoriteImage(id)

    fun insertFavoriteImage(imageFavorite: ImageFavorite) {
        repository.insertFavoriteImage(imageFavorite)
    }

    fun deleteFavoriteImages(favoriteImages: List<ImageFavorite>) {
        repository.deleteFavoriteImages(favoriteImages)
    }
}