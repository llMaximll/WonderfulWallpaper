package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ImageRepository
) : ViewModel() {

    private val stateHandle = savedStateHandle
    var adapter: ImagesAdapter? = null

    var page = 1

    fun getImages(context: Context, page: Int = this.page): Flow<Resource<List<Image>>> {
        return repository.getImages(
            key = context.getString(R.string.api_key),
            page = page
        )
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