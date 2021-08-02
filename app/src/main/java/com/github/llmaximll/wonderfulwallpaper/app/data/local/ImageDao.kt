package com.github.llmaximll.wonderfulwallpaper.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images")
    fun getImages(): LiveData<List<Image>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(images: List<Image>)
}