package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val billId: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "",
    val customerPhone: String = "",
    val subtotal: Double,
    val discountType: String = "PERCENT", // "PERCENT" or "FIXED"
    val discountValue: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val paymentMethod: String = "CASH", // "CASH", "UPI", "CARD"
    val notes: String = ""
)
