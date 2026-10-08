package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.BusinessSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = getApplication<GownApplication>().preferences

    private val _settings = MutableStateFlow(BusinessSettings())
    val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = preferences.settings.first()
        }
    }

    private fun change(transform: (BusinessSettings) -> BusinessSettings) {
        _settings.update(transform)
        _saved.value = false
    }

    fun onBusinessName(value: String) = change { it.copy(businessName = value) }
    fun onPhone(value: String) = change { it.copy(phone = value) }
    fun onAddress(value: String) = change { it.copy(address = value) }
    fun onFooterText(value: String) = change { it.copy(footerText = value) }

    fun save() {
        viewModelScope.launch {
            preferences.saveSettings(_settings.value)
            _saved.value = true
        }
    }
}
