package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.google.gson.GsonBuilder
import timber.log.Timber

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val recentItems = mutableListOf<Image>()
    private val items = mutableListOf<Image>()

    fun addItems(items: List<Image>) {
        Timber.v("adapter | items=${items.size}")
        val firstIndex = this.items.lastIndex + 1
        this.items.addAll(items)
        notifyItemRangeInserted(firstIndex, items.size)
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val image: String = GsonBuilder().create().toJson(items.getOrNull(position))
        Timber.v("createFragment($position) | items.size=${items.size}")
        return PagerFragment.newInstance(image)
    }
}