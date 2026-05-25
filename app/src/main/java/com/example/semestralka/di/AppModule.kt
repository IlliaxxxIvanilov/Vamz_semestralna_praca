package com.example.semestralka.di

import android.content.Context
import com.example.semestralka.data.remote.OverpassApiService
import com.example.semestralka.data.repository.PlacesRepositoryImpl
import com.example.semestralka.repository.PlacesRepository
import com.example.semestralka.worker.RefreshNotificationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OVERPASS_BASE_URL = "https://overpass-api.de/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(OVERPASS_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideOverpassApiService(retrofit: Retrofit): OverpassApiService =
        retrofit.create(OverpassApiService::class.java)

    @Provides
    @Singleton
    fun provideRefreshNotificationHelper(
        @ApplicationContext context: Context
    ): RefreshNotificationHelper = RefreshNotificationHelper(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        impl: PlacesRepositoryImpl
    ): PlacesRepository
}