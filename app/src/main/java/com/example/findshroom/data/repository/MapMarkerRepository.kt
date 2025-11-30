package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.MapMarkerDao
import com.example.findshroom.data.model.MapMarker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MapMarkerRepository @Inject constructor(
    private val mapMarkerDao: MapMarkerDao
) {
    fun getAllMarkers(): Flow<List<MapMarker>> = mapMarkerDao.getAllMarkers()
    
    suspend fun getMarkerById(id: Long): MapMarker? = mapMarkerDao.getMarkerById(id)
    
    suspend fun insertMarker(marker: MapMarker): Long = mapMarkerDao.insertMarker(marker)
    
    suspend fun updateMarker(marker: MapMarker) = mapMarkerDao.updateMarker(marker)
    
    suspend fun deleteMarker(marker: MapMarker) = mapMarkerDao.deleteMarker(marker)
}

