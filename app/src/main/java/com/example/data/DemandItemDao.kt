package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DemandItemDao {
    @Query("SELECT * FROM demand_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<DemandItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: DemandItem)
    
    @Update
    suspend fun updateItem(item: DemandItem)

    @Query("DELETE FROM demand_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("SELECT COUNT(*) FROM demand_items")
    suspend fun getItemsCount(): Int
}
