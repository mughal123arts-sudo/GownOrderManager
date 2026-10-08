package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.Order
import com.mughalarts.gownordermanager.util.PaymentStatus
import com.mughalarts.gownordermanager.util.formatOrderNumber
import com.mughalarts.gownordermanager.util.todayMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GownColor(val label: String) {
    Blue("Blue"),
    Green("Green"),
    Mehroon("Mehroon"),
    Black("Black")
}

data class GownColorState(
    val selected: Boolean = false,
    val quantity: String = ""
) {
    /** Unchecked colors always count as zero. */
    val quantityValue: Int
        get() = if (selected) (quantity.toIntOrNull() ?: 0) else 0
}

data class NewOrderUiState(
    val customerName: String = "",
    val schoolName: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val deliveryDate: Long? = null,
    val colors: Map<GownColor, GownColorState> =
        GownColor.entries.associateWith { GownColorState() },
    val totalAmount: String = "",
    val advanceAmount: String = "",
    val specialInstructions: String = "",
    val errorMessage: String? = null
) {
    val totalGowns: Int
        get() = colors.values.sumOf { it.quantityValue }

    val totalAmountValue: Long
        get() = totalAmount.toLongOrNull() ?: 0L

    val advanceValue: Long
        get() = advanceAmount.toLongOrNull() ?: 0L

    val advanceTooHigh: Boolean
        get() = advanceValue > totalAmountValue

    val remainingBalance: Long
        get() = if (advanceTooHigh) 0L else totalAmountValue - advanceValue

    /** Only "Pending" or "Paid". */
    val paymentStatus: String
        get() = PaymentStatus.from(totalAmountValue, advanceValue)
}

class NewOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val gownApp = getApplication<GownApplication>()
    private val repository = gownApp.repository
    private val preferences = gownApp.preferences

    private val orderDate = todayMillis()
    private var saving = false

    private val _uiState = MutableStateFlow(NewOrderUiState())
    val uiState: StateFlow<NewOrderUiState> = _uiState.asStateFlow()

    val nextOrderNumber: StateFlow<String> =
        preferences.orderCounter
            .map { formatOrderNumber(it + 1) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), formatOrderNumber(1))

    val orderDateMillis: Long get() = orderDate

    private fun change(transform: (NewOrderUiState) -> NewOrderUiState) {
        _uiState.update { transform(it).copy(errorMessage = null) }
    }

    fun onCustomerName(value: String) = change { it.copy(customerName = value) }
    fun onSchoolName(value: String) = change { it.copy(schoolName = value) }
    fun onContactNumber(value: String) = change { it.copy(contactNumber = value) }
    fun onAddress(value: String) = change { it.copy(address = value) }
    fun onSpecialInstructions(value: String) = change { it.copy(specialInstructions = value) }

    fun onDeliveryDate(millis: Long?) {
        if (millis != null) change { it.copy(deliveryDate = millis) }
    }

    fun onColorSelected(color: GownColor, selected: Boolean) = change { state ->
        val newColorState =
            if (selected) state.colors.getValue(color).copy(selected = true) else GownColorState()
        state.copy(colors = state.colors + (color to newColorState))
    }

    fun onColorQuantity(color: GownColor, text: String) = change { state ->
        val digits = text.filter { it.isDigit() }.take(4)
        val newColorState = state.colors.getValue(color).copy(quantity = digits)
        state.copy(colors = state.colors + (color to newColorState))
    }

    fun onTotalAmount(text: String) =
        change { it.copy(totalAmount = text.filter { c -> c.isDigit() }.take(9)) }

    fun onAdvanceAmount(text: String) =
        change { it.copy(advanceAmount = text.filter { c -> c.isDigit() }.take(9)) }

    private fun validate(state: NewOrderUiState): String? {
        return when {
            state.customerName.isBlank() -> "Please enter the customer name."
            state.deliveryDate == null -> "Please choose a delivery date."
            state.totalGowns <= 0 -> "Select at least one gown color and enter its quantity."
            state.advanceTooHigh -> "Advance payment cannot be more than the total amount."
            else -> null
        }
    }

    fun saveOrder(onSaved: () -> Unit) {
        if (saving) return
        val s = _uiState.value
        val error = validate(s)
        val delivery = s.deliveryDate
        if (error != null || delivery == null) {
            _uiState.update { it.copy(errorMessage = error ?: "Please choose a delivery date.") }
            return
        }

        saving = true
        viewModelScope.launch {
            try {
                val number = preferences.nextOrderNumber()
                val now = System.currentTimeMillis()
                val blue = s.colors.getValue(GownColor.Blue)
                val green = s.colors.getValue(GownColor.Green)
                val mehroon = s.colors.getValue(GownColor.Mehroon)
                val black = s.colors.getValue(GownColor.Black)

                val order = Order(
                    orderNumber = formatOrderNumber(number),
                    customerName = s.customerName.trim(),
                    schoolName = s.schoolName.trim(),
                    contactNumber = s.contactNumber.trim(),
                    address = s.address.trim(),
                    orderDate = orderDate,
                    deliveryDate = delivery,
                    blueSelected = blue.selected,
                    blueQuantity = blue.quantityValue,
                    greenSelected = green.selected,
                    greenQuantity = green.quantityValue,
                    mehroonSelected = mehroon.selected,
                    mehroonQuantity = mehroon.quantityValue,
                    blackSelected = black.selected,
                    blackQuantity = black.quantityValue,
                    totalGowns = s.totalGowns,
                    totalAmount = s.totalAmountValue,
                    advanceAmount = s.advanceValue,
                    remainingBalance = s.remainingBalance,
                    paymentStatus = s.paymentStatus,
                    specialInstructions = s.specialInstructions.trim(),
                    createdAt = now,
                    updatedAt = now
                )
                repository.insert(order)
                _uiState.value = NewOrderUiState()
                onSaved()
            } finally {
                saving = false
            }
        }
    }
}
