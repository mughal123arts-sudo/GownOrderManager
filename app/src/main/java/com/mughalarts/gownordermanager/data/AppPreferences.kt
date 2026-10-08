package com.mughalarts.gownordermanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gown_settings")

data class BusinessSettings(
    val businessName: String = "MUGHAL ARTS",
    val phone: String = "",
    val address: String = "",
    val footerText: String = "Thank you for your order!"
)

class AppPreferences(context: Context) {

    private val appContext = context.applicationContext

    private val keyBusinessName = stringPreferencesKey("business_name")
    private val keyPhone = stringPreferencesKey("business_phone")
    private val keyAddress = stringPreferencesKey("business_address")
    private val keyFooter = stringPreferencesKey("footer_text")
    private val keyOrderCounter = intPreferencesKey("order_counter")

    val settings: Flow<BusinessSettings> = appContext.dataStore.data.map { prefs ->
        val defaults = BusinessSettings()
        BusinessSettings(
            businessName = prefs[keyBusinessName] ?: defaults.businessName,
            phone = prefs[keyPhone] ?: defaults.phone,
            address = prefs[keyAddress] ?: defaults.address,
            footerText = prefs[keyFooter] ?: defaults.footerText
        )
    }

    val orderCounter: Flow<Int> = appContext.dataStore.data.map { prefs ->
        prefs[keyOrderCounter] ?: 0
    }

    suspend fun saveSettings(settings: BusinessSettings) {
        appContext.dataStore.edit { prefs ->
            prefs[keyBusinessName] = settings.businessName
            prefs[keyPhone] = settings.phone
            prefs[keyAddress] = settings.address
            prefs[keyFooter] = settings.footerText
        }
    }

    /** Returns the next order number (1, 2, 3...) and remembers it, so numbers are never reused. */
    suspend fun nextOrderNumber(): Int {
        var next = 0
        appContext.dataStore.edit { prefs ->
            next = (prefs[keyOrderCounter] ?: 0) + 1
            prefs[keyOrderCounter] = next
        }
        return next
    }
}
