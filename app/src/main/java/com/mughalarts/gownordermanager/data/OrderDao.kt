package com.mughalarts.gownordermanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert
    suspend fun insert(order: Order): Long

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT 5")
    fun getRecentOrders(): Flow<List<Order>>

    @Query("SELECT COUNT(*) FROM orders")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE paymentStatus = 'Pending'")
    fun countPending(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE paymentStatus = 'Paid'")
    fun countPaid(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalGowns), 0) FROM orders")
    fun totalGowns(): Flow<Int>
}
