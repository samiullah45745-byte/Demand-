package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demand_items")
data class DemandItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val unit: String,
    val quantity: String = "" // Keeping it as a string so it handles empty states and partial decimals nicely in TextField
)
