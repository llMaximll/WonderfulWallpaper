package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.databinding.ItemImageBinding

class ImagesAdapter : RecyclerView.Adapter<ImagesViewHolder>() {

    val recentItems = mutableListOf<Image>()
    private val items = mutableListOf<Image>()

    fun addItems(items: List<Image>) {
        val firstIndex = this.items.lastIndex + 1
        this.items.addAll(items)
        notifyItemRangeInserted(firstIndex, items.size)
    }

    fun setItems(items: List<Image>) {
        this.items.clear()
        this.items.addAll(items)
        notifyItemRangeChanged(0, this.items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagesViewHolder((binding))
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}

class ImagesViewHolder(
    private val itemBinding: ItemImageBinding
) : RecyclerView.ViewHolder(itemBinding.root) {

    private lateinit var image: Image

    fun bind(item: Image) {
        this.image = item
        Glide.with(itemBinding.root)
            .load(item.webFormatURL.replace("_640", "_340"))
            .centerCrop()
            .into(itemBinding.imageView)
    }
}