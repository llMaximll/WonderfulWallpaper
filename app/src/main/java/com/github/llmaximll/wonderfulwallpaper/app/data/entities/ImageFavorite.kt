package com.github.llmaximll.wonderfulwallpaper.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_images")
data class ImageFavorite(
    @PrimaryKey
    val id: String
)