package com.mughalarts.gownordermanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val schoolName: String,
    val contactNumber: String,
    val address: String,
    val orderDate: Long,
    val deliveryDate: Long,
    val blueSelected: Boolean,
    val blueQuantity: Int,
    val greenSelected: Boolean,
    val greenQuantity: Int,
    val mehroonSelected: Boolean,
    val mehroonQuantity: Int,
    val blackSelected: Boolean,
    val blackQuantity: Int,
    val totalGowns: Int,
    val totalAmount: Long,
    val advanceAmount: Long,
    val remainingBalance: Long,
    // Only "Pending" or "Paid"
    val paymentStatus: String,
    val specialInstructions: String,
    val createdAt: Long,
    val updatedAt: Long
)
