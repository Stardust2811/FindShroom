package com.example.findshroom.di

import com.example.findshroom.data.repository.GeminiRepository
import com.example.findshroom.data.repository.MapMarkerRepository
import com.example.findshroom.data.repository.MushroomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMushroomRepository(mushroomDao: com.example.findshroom.data.dao.MushroomDao): MushroomRepository {
        return MushroomRepository(mushroomDao)
    }
    
    @Provides
    @Singleton
    fun provideMapMarkerRepository(mapMarkerDao: com.example.findshroom.data.dao.MapMarkerDao): MapMarkerRepository {
        return MapMarkerRepository(mapMarkerDao)
    }
    
    @Provides
    @Singleton
    fun provideGeminiRepository(): GeminiRepository {
        return GeminiRepository()
    }
}

