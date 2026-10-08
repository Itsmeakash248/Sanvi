package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["billId"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["billId"])]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val billId: Long,
    val productId: Long,
    val productName: String,
    val category: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)
