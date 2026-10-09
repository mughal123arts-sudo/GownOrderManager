package com.mughalarts.gownordermanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<Order?>

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
