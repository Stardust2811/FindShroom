package com.example.findshroom.di

import android.content.Context
import com.example.findshroom.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    
    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository {
        return LocationRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(userDao: com.example.findshroom.data.dao.UserDao): UserRepository {
        return UserRepository(userDao)
    }
    
    @Provides
    @Singleton
    fun provideSubscriptionRepository(subscriptionDao: com.example.findshroom.data.dao.SubscriptionDao): SubscriptionRepository {
        return SubscriptionRepository(subscriptionDao)
    }
    
    @Provides
    @Singleton
    fun provideUserStatsRepository(userStatsDao: com.example.findshroom.data.dao.UserStatsDao): UserStatsRepository {
        return UserStatsRepository(userStatsDao)
    }
    
    @Provides
    @Singleton
    fun provideDiaryEntryRepository(diaryEntryDao: com.example.findshroom.data.dao.DiaryEntryDao): DiaryEntryRepository {
        return DiaryEntryRepository(diaryEntryDao)
    }
}

