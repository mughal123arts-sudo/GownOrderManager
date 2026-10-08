package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.Order
import com.mughalarts.gownordermanager.util.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class OrderFilter(val label: String) {
    All("All"),
    Pending("Pending"),
    Paid("Paid")
}

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GownApplication>().repository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(OrderFilter.All)
    val filter: StateFlow<OrderFilter> = _filter.asStateFlow()

    // null = still loading
    val orders: StateFlow<List<Order>?> =
        combine(repository.allOrders, _query, _filter) { all, query, filter ->
            val text = query.trim()
            all.filter { order ->
                val matchesFilter = when (filter) {
                    OrderFilter.All -> true
                    OrderFilter.Pending -> order.paymentStatus == PaymentStatus.PENDING
                    OrderFilter.Paid -> order.paymentStatus == PaymentStatus.PAID
                }
                val matchesQuery = text.isEmpty() ||
                    order.customerName.contains(text, ignoreCase = true) ||
                    order.orderNumber.contains(text, ignoreCase = true)
                matchesFilter && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onFilterChange(value: OrderFilter) {
        _filter.value = value
    }
}
