package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.BusinessSettings
import com.mughalarts.gownordermanager.data.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val gownApp = getApplication<GownApplication>()
    private val repository = gownApp.repository

    val settings: StateFlow<BusinessSettings> =
        gownApp.preferences.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessSettings())

    fun observeOrder(id: Long): Flow<Order?> = repository.observeOrder(id)

    fun deleteOrder(order: Order, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(order)
            onDeleted()
        }
    }
}
