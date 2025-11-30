package com.example.findshroom.data.dao

import androidx.room.*
import com.example.findshroom.data.model.MapMarker
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMarkerDao {
    @Query("SELECT * FROM map_markers ORDER BY timestamp DESC")
    fun getAllMarkers(): Flow<List<MapMarker>>
    
    @Query("SELECT * FROM map_markers WHERE id = :id")
    suspend fun getMarkerById(id: Long): MapMarker?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MapMarker): Long
    
    @Update
    suspend fun updateMarker(marker: MapMarker)
    
    @Delete
    suspend fun deleteMarker(marker: MapMarker)
}

