package com.github.llmaximll.wonderfulwallpaper.app.data.entities

data class ImageList(
    val total: Int,
    val totalHits: Int,
    val hits: List<Image>
)