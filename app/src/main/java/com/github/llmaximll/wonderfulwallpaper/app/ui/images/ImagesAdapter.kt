package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.databinding.ItemImageBinding
import timber.log.Timber

class ImagesAdapter(
    private val callbacks: ImagesFragment.Callbacks?
) : RecyclerView.Adapter<ImagesViewHolder>() {

    val recentItems = mutableListOf<Image>()
    private val items = mutableListOf<Image>()

    fun addItems(items: List<Image>) {
        val firstIndex = this.items.lastIndex + 1
        this.items.addAll(items)
        Timber.v("adapter | items=${this.items.size}")
        notifyItemRangeInserted(firstIndex, items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagesViewHolder(binding, callbacks = callbacks)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}

class ImagesViewHolder(
    private val itemBinding: ItemImageBinding,
    private val callbacks: ImagesFragment.Callbacks?
) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

    init {
        itemView.setOnClickListener(this)
    }

    private lateinit var image: Image

    fun bind(item: Image) {
        this.image = item
        Glide.with(itemBinding.root)
            .load(item.webFormatURL.replace("_640", "_340"))
            .thumbnail(Glide.with(itemBinding.root).load(item.previewURL).transform(CenterCrop()))
            .transition(withCrossFade())
            .transform(CenterCrop())
            .error(Glide.with(itemBinding.root).load(item.previewURL))
            .into(itemBinding.imageView)
            .clearOnDetach()
    }

    override fun onClick(v: View?) {
        callbacks?.onItemClicked(image)
    }
}