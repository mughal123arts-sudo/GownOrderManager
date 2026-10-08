package com.mughalarts.gownordermanager

import android.app.Application
import com.mughalarts.gownordermanager.data.AppDatabase
import com.mughalarts.gownordermanager.data.AppPreferences
import com.mughalarts.gownordermanager.data.OrderRepository

class GownApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: OrderRepository by lazy { OrderRepository(database.orderDao()) }
    val preferences: AppPreferences by lazy { AppPreferences(this) }
}
