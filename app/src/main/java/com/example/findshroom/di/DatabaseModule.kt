package com.example.findshroom.di

import android.content.Context
import androidx.room.Room
import com.example.findshroom.data.dao.MapMarkerDao
import com.example.findshroom.data.dao.MushroomDao
import com.example.findshroom.data.database.FindShroomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FindShroomDatabase {
        return Room.databaseBuilder(
            context,
            FindShroomDatabase::class.java,
            "findshroom_database"
        ).build()
    }
    
    @Provides
    fun provideMushroomDao(database: FindShroomDatabase): MushroomDao {
        return database.mushroomDao()
    }
    
    @Provides
    fun provideMapMarkerDao(database: FindShroomDatabase): MapMarkerDao {
        return database.mapMarkerDao()
    }
}

