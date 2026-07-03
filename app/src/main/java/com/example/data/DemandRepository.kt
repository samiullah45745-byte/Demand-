package com.example.data

import kotlinx.coroutines.flow.Flow

class DemandRepository(private val demandItemDao: DemandItemDao) {
    val allItems: Flow<List<DemandItem>> = demandItemDao.getAllItems()

    suspend fun insert(item: DemandItem) = demandItemDao.insertItem(item)
    
    suspend fun update(item: DemandItem) = demandItemDao.updateItem(item)

    suspend fun deleteById(id: Int) = demandItemDao.deleteItemById(id)
    
    suspend fun getItemsCount(): Int = demandItemDao.getItemsCount()
}
