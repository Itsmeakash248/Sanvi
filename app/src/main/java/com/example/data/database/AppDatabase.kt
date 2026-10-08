package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BillDao
import com.example.data.dao.ProductDao
import com.example.data.model.BillEntity
import com.example.data.model.BillItemEntity
import com.example.data.model.ProductEntity

@Database(
    entities = [ProductEntity::class, BillEntity::class, BillItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sanvi_clothing_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
