package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.content.Context
import android.os.Parcelable
import androidx.core.view.forEach
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ImageRepository
) : ViewModel() {

    private val _mainState = MutableStateFlow(true)
    val mainState = _mainState.asStateFlow()

    private val stateHandle = savedStateHandle
    var adapter: ImagesAdapter? = null

    var page = 1
    var q = ""
    var imageType = ""
    var orientation = ""
    val category = mutableListOf<String>()
    val colors = mutableListOf<String>()
    var editorsChoice = ""

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
        editorsChoice: String = this.editorsChoice
    ): Flow<Resource<List<Image>>> {
        return repository.getImages(
            key = context.getString(R.string.api_key),
            page = page,
            q = q,
            imageType = imageType,
            orientation = orientation,
            category = category,
            colors = colors,
            editorsChoice = editorsChoice
        )
    }

    fun toggleMainState(currentState: Boolean? = null) {
        _mainState.value = currentState ?: !_mainState.value
    }

    fun setParamListFromTags(chipGroupList: List<ChipGroup>) {
        try {
            chipGroupList.forEach { group ->
                when (group.id) {
                    R.id.imageType_group -> {
                        imageType = ""
                        group.forEach { if ((it as Chip).isChecked) imageType = it.tag.toString() }
                    }
                    R.id.orientation_group -> {
                        orientation = ""
                        group.forEach { if ((it as Chip).isChecked) orientation = it.tag.toString() }
                    }
                    R.id.category_group -> {
                        category.clear()
                        group.forEach { if ((it as Chip).isChecked) category.add(it.tag.toString()) }
                    }
                    R.id.colors_group -> {
                        colors.clear()
                        group.forEach { if ((it as Chip).isChecked) colors.add(it.tag.toString()) }
                    }
                    R.id.choice_group -> {
                        editorsChoice = ""
                        group.forEach { if ((it as Chip).isChecked) editorsChoice = it.tag.toString() }
                    }
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    /**
     * Возвращает true, если был изменен фильтр, false - если нет
     */
    fun checkChangeState(chipGroupList: List<ChipGroup>): Boolean {
        chipGroupList.forEach { group ->
            val chipList = mutableListOf<String>()
            group.forEach { it as Chip
                if (it.isChecked) chipList.add(it.tag.toString())
            }
            when (group.id) {
                R.id.imageType_group -> {
                    if (chipList.getOrNull(0) != imageType) {
                        Timber.v("imageType")
                        return true
                    }
                }
                R.id.orientation_group -> {
                    if (chipList.getOrNull(0) != orientation) {
                        Timber.v("orientation")
                        return true
                    }
                }
                R.id.category_group -> {
                    if (chipList != category) {
                        Timber.v("category | $chipList : $category")
                        return true
                    }
                }
                R.id.colors_group -> {
                    if (chipList != colors) {
                        Timber.v("colors")
                        return true
                    }
                }
                R.id.choice_group -> {
                    if (chipList.getOrNull(0) != editorsChoice) {
                        if (chipList.getOrNull(0) == null) return false // Если не выбран элемент
                        Timber.v("editorsChoice | ${chipList.getOrNull(0)} : $editorsChoice")
                        return true
                    }
                }
            }
        }
        return false
    }

    fun onSaveState(stateRV: Parcelable?) {
        stateHandle.set(KEY_SAVE_RECYCLER_VIEW, stateRV)
    }

    fun onRestoreState(key: String): Parcelable? {
        return stateHandle.get<Parcelable?>(KEY_SAVE_RECYCLER_VIEW)
    }

    companion object {
        const val KEY_SAVE_RECYCLER_VIEW = "key_save_recycler_view"
    }
}