package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import android.content.Context
import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.pager.PagerAdapter
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesViewModel
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ImageRepository
) : ViewModel() {

    // Показывает можно ли начать новую загрузку страницы, false - можно, true - нет
    var loadingNewPageFlag = false

    lateinit var imageList: MutableList<Image>

    private val stateHandle = savedStateHandle
    var adapter: PagerAdapter? = null
    var currentItem = -1

    var page = 1
    var q = ""
    var imageType = ""
    var orientation = ""
    val category = mutableListOf<String>()
    val colors = mutableListOf<String>()
    var editorsChoice = ""
    var safeSearch = true

    /**
     * [getImages] добавляет в список элементы
     */
    fun getImages(
        context: Context,
        page: Int = this.page,
        q: String = this.q,
        imageType: String = this.imageType,
        orientation: String = this.orientation,
        category: List<String> = this.category,
        colors: List<String> = this.colors,
        editorsChoice: String = this.editorsChoice,
        safeSearch: Boolean = this.safeSearch
    ): Flow<Resource<List<Image>>> {
        return repository.getImages(
            key = context.getString(R.string.api_key),
            page = page,
            q = q,
            imageType = imageType,
            orientation = orientation,
            category = category,
            colors = colors,
            editorsChoice = editorsChoice,
            safeSearch = safeSearch
        )
    }

    fun onSaveState(stateRV: SparseArray<Parcelable>?) {
        stateHandle.set(ImagesViewModel.KEY_SAVE_RECYCLER_VIEW, stateRV)
    }

    fun onRestoreState(key: String): SparseArray<Parcelable>? {
        return stateHandle.get<SparseArray<Parcelable>?>(ImagesViewModel.KEY_SAVE_RECYCLER_VIEW)
    }

    companion object {
        const val KEY_SAVE_RECYCLER_VIEW = "key_save_recycler_view_detail"
    }
}