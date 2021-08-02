package com.github.llmaximll.wonderfulwallpaper.app.data.entities

import com.google.gson.annotations.SerializedName

data class ImageList(
    val total: Int,
    val totalHits: Int,
    val hits: List<Image>
)