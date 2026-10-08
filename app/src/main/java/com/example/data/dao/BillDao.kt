package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.BillEntity
import com.example.data.model.BillItemEntity
import com.example.data.model.BillWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Transaction
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBillsWithItems(): Flow<List<BillWithItems>>

    @Transaction
    @Query("SELECT * FROM bills WHERE billId = :billId LIMIT 1")
    fun getBillWithItemsById(billId: Long): Flow<BillWithItems?>

    @Transaction
    @Query("SELECT * FROM bills WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getTodayBills(startOfDay: Long, endOfDay: Long): Flow<List<BillWithItems>>

    @Query("DELETE FROM bills WHERE billId = :billId")
    suspend fun deleteBill(billId: Long)

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun countBills(): Int
}
