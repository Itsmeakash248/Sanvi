package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val size: String,
    val color: String,
    val sellingPrice: Double,
    val costPrice: Double = 0.0,
    val stockQuantity: Int,
    val barcode: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
