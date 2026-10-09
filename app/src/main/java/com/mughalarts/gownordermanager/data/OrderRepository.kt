package com.mughalarts.gownordermanager.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val dao: OrderDao) {

    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val recentOrders: Flow<List<Order>> = dao.getRecentOrders()
    val totalOrders: Flow<Int> = dao.countAll()
    val pendingOrders: Flow<Int> = dao.countPending()
    val paidOrders: Flow<Int> = dao.countPaid()
    val totalGowns: Flow<Int> = dao.totalGowns()

    fun observeOrder(id: Long): Flow<Order?> = dao.observeById(id)

    suspend fun getOrder(id: Long): Order? = dao.getById(id)

    suspend fun insert(order: Order): Long = dao.insert(order)

    suspend fun update(order: Order) = dao.update(order)

    suspend fun delete(order: Order) = dao.delete(order)
}
