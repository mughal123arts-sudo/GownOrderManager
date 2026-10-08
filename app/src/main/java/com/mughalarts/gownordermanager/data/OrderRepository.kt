package com.mughalarts.gownordermanager.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val dao: OrderDao) {

    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val recentOrders: Flow<List<Order>> = dao.getRecentOrders()
    val totalOrders: Flow<Int> = dao.countAll()
    val pendingOrders: Flow<Int> = dao.countPending()
    val paidOrders: Flow<Int> = dao.countPaid()
    val totalGowns: Flow<Int> = dao.totalGowns()

    suspend fun insert(order: Order): Long = dao.insert(order)
}
