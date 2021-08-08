package com.github.llmaximll.wonderfulwallpaper.app.di

import android.content.Context
import com.github.llmaximll.wonderfulwallpaper.app.data.local.AppDatabase
import com.github.llmaximll.wonderfulwallpaper.app.data.local.ImageDao
import com.github.llmaximll.wonderfulwallpaper.app.data.remote.ImageRemoteDataSource
import com.github.llmaximll.wonderfulwallpaper.app.data.remote.ImageService
import com.github.llmaximll.wonderfulwallpaper.app.data.repository.ImageRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://pixabay.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideImageService(retrofit: Retrofit): ImageService = retrofit.create(ImageService::class.java)

    @Provides
    @Singleton
    fun provideImageRemoteDataSource(imageService: ImageService): ImageRemoteDataSource = ImageRemoteDataSource(imageService)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context) = AppDatabase.getDatabase(appContext)

    @Provides
    @Singleton
    fun provideImageDao(db: AppDatabase) = db.imageDao()

    @Provides
    @Singleton
    fun provideRepository(remoteDataSource: ImageRemoteDataSource, localDataSource: ImageDao) =
        ImageRepository(remoteDataSource, localDataSource)
}