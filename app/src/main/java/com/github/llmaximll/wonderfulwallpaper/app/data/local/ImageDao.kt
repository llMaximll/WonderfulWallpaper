package com.github.llmaximll.wonderfulwallpaper.app.data.local

import androidx.room.*
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images")
    fun getImages(): Flow<List<Image>>

    @Query("SELECT * FROM favorite_images")
    suspend fun getFavoriteImages(): List<ImageFavorite>

    @Query("SELECT * FROM favorite_images WHERE id=:id")
    suspend fun getFavoriteImage(id: String): ImageFavorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(images: List<Image>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteImage(imageFavorite: ImageFavorite)

    @Delete
    fun deleteFavoriteImages(favoriteImages: List<ImageFavorite>)
}