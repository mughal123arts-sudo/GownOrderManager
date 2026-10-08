package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.Order
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GownApplication>().repository

    val totalOrders: StateFlow<Int> =
        repository.totalOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingOrders: StateFlow<Int> =
        repository.pendingOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val paidOrders: StateFlow<Int> =
        repository.paidOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalGowns: StateFlow<Int> =
        repository.totalGowns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // null = still loading
    val recentOrders: StateFlow<List<Order>?> =
        repository.recentOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
