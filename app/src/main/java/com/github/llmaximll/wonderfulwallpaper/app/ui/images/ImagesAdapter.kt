package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.databinding.ItemImageBinding
import timber.log.Timber

class ImagesAdapter(
    private val callbacks: ImagesFragment.Callbacks?,
    private val viewModel: ImagesViewModel,
    private val context: Context
) : RecyclerView.Adapter<ImagesViewHolder>() {

    val recentItems = mutableListOf<Image>()
    val items = mutableListOf<Image>()

    fun addItems(items: List<Image>) {
        Timber.v("adapter | items=${items.size}")
        val firstIndex = this.items.lastIndex + 1
        this.items.addAll(items)
        notifyItemRangeInserted(firstIndex, items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagesViewHolder(binding, callbacks = callbacks, viewModel = viewModel, context = context)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}

class ImagesViewHolder(
    private val itemBinding: ItemImageBinding,
    private val callbacks: ImagesFragment.Callbacks?,
    private val viewModel: ImagesViewModel,
    private val context: Context
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
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(10)))
            .error(Glide.with(itemBinding.root).load(item.previewURL))
            .into(itemBinding.imageView)
            .clearOnDetach()
    }

    override fun onClick(v: View?) {
        val parameters = Parameters(
            idImage = image.id,
            page = viewModel.page,
            q = viewModel.q,
            imageType = viewModel.imageType,
            orientation = viewModel.orientation,
            category = viewModel.category,
            colors = viewModel.colors,
            editorsChoice = viewModel.editorsChoice,
            positionImage = adapterPosition,
            items = viewModel.adapter?.items?.toList()
        )
        callbacks?.onItemClicked(parameters)
    }
}