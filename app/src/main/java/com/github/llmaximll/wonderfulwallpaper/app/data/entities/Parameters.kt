package com.github.llmaximll.wonderfulwallpaper.app.data.entities

/**
 * Все параметры, необходимые для вывода ImageDetailFragment
 */
data class Parameters(
    val idImage: String,
    val page: Int,
    val q: String,
    val imageType: String,
    val orientation: String,
    val category: List<String>,
    val colors: List<String>,
    val editorsChoice: String,
    val positionImage: Int,
    val items: List<Image>?
)